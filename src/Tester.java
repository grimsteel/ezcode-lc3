import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;

public class Tester
{
  /**
   * Print a short value as a binary string with 0s and 1s. The length will be exactly 16
   */
  private static String printBinaryShort(short value) {
    String res = Integer.toBinaryString(value);
    // java sign extends these, so negative numbers have extra 1s
    if (res.length() > 16)
      res = res.substring(res.length() - 16, res.length());
    // pads with spaces, replace spaces with 0s
    return String.format("%16s", res).replace(' ', '0');
  }
	
	public static void main(String[] args) throws Exception {
		String lex = "EZCode_lex.txt";
    String programName = "hello";
    String binName = String.format("%s.bin", programName);
    String objName = String.format("%s.obj", programName);
		
		Lexer lexer = new Lexer(lex, String.format("tests/%s.ezc", programName));
		DataBlock db = new DataBlock();
    Parser p = new Parser(lexer.getTokens(), db);
    
    ArrayList<Statement> a = p.parseSequence(true);
    ArrayList<Instruction> instrs = new ArrayList<>();
    
    // stack initialization
    instrs.add(LoadStore.ld((byte) 6, (byte) 1)); // load stack pointer
    instrs.add(Branch.any((short) 1)); // skip SP value
    instrs.add(Word.special(SpecialAddress.StackStart));           // 0
    
    // Pass 1: Emit instructions
    ArrayList<Instruction> programInstructions = new ArrayList<>();
    for (Statement stmt : a) {
      programInstructions.addAll(stmt.emit(db));
    }
    
    // Pass 2: Insert data block addresses
    short coverageEnd = 0; // non inclusive
    while (coverageEnd < programInstructions.size()) {
      // the ld instruction uses a 9 bit offset, which means it can address -255 + 256 from the instruction (PC incremented before offset)
      short nextDataBlockAddress = (short) (coverageEnd + 0x100);
      short nextDataBlockJump = (short) (nextDataBlockAddress - 1);
      if (programInstructions.size() <= nextDataBlockAddress) {
        programInstructions.add(Word.special(SpecialAddress.DataBlock));
      } else {
        // insert in the middle
        programInstructions.add(nextDataBlockJump, Branch.any((short) 1));
        programInstructions.add(nextDataBlockAddress, Word.special(SpecialAddress.DataBlock));
      }
      
      // now we cover a full 512 more
      coverageEnd += 0x200;
    }
    
    // Pass 3: Add correct data block address offsets
    for (int i = 0; i < programInstructions.size(); i++) {
      if (programInstructions.get(i) instanceof UnresolvedSpecialInstruction) {
        // cast
        UnresolvedSpecialInstruction special = (UnresolvedSpecialInstruction) programInstructions.get(i);
        if (special.getSpecialType() == SpecialAddress.NearestDataBlock) {
          // set address to nearest multiple of 0x200 + 0x100
          int nearestDataBlockAddr = (i & ~0x1FF) | 0x100;
          // cap at end
          if (nearestDataBlockAddr >= programInstructions.size()) {
            nearestDataBlockAddr = programInstructions.size() - 1;
          }
          // get offset (pc incremented before offset used)
          int offset = nearestDataBlockAddr - (i + 1);
          special.setAddress((short) offset);
        }
      }
    }
    
    instrs.addAll(programInstructions);
    instrs.add(Trap.halt());
    
    // subroutines
    short subroutineAddress = (short) (0x3000 + instrs.size());
    for (short instr : BuiltinUtils.routines) {
      instrs.add(new Word(instr));
    }
    
    // update stack address
    short stackAddress = (short) (0x3000 + instrs.size());
    ((Word) instrs.get(2)).word = stackAddress;
    
    // emit stack block (256 shorts)
    for (int i = 0; i < 256; i++) {
      instrs.add(new Word((short) 0));
    }
    
    short dbAddress =  (short) (0x3000 + instrs.size());
    short dbMidAddress = db.setAddress(dbAddress);
    
    // emit data block (variables + constants)
    short[] dataBlock = db.emitDataBlock();
    for (short s : dataBlock) {
      instrs.add(new Word(s));
    }
    // emit string block
    short[] stringBlock = db.emitStringBlock();
    for (short s : stringBlock) {
      instrs.add(new Word(s));
    }
    
    // Pass 4: Update remaining specials
    for (Instruction i : instrs) {
      if (i instanceof UnresolvedSpecialInstruction) {
        UnresolvedSpecialInstruction special = (UnresolvedSpecialInstruction) i;
        SpecialAddress type = special.getSpecialType();
        if (type != null) {
          switch (type) {
            case SpecialAddress.DataBlock:
              special.setAddress(dbMidAddress);
              break;
            case SpecialAddress.StackStart:
              special.setAddress(stackAddress);
              break;
            case SpecialAddress.CallMult:
              special.setAddress((short) (subroutineAddress + BuiltinUtils.mul_offset));
              break;
            case SpecialAddress.CallDiv:
              special.setAddress((short) (subroutineAddress + BuiltinUtils.div_offset));
              break;
            case SpecialAddress.CallPrint:
              // TODO: type checking and more print types
              special.setAddress((short) (subroutineAddress + BuiltinUtils.iprint_offset));
              break;
            default:
              break;
          }
        }
      }
    }

    System.out.printf("Writing assembly bytecode to %s\n", binName);
    BufferedWriter writer = new BufferedWriter(new FileWriter(binName));    

    writer.write(printBinaryShort((short) 0x3000) + '\n');
    for (Instruction i : instrs) {
      System.out.println(i);
      writer.write(printBinaryShort(i.emit()) + '\n');
    }

    writer.close();

    System.out.printf("Converting bytecode %s to object file %s\n", binName, objName);
    new ProcessBuilder("lc3tools/assembler", binName)
      .inheritIO()
      .start();
	}

}
