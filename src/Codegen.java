import java.util.ArrayList;

public class Codegen {
  ArrayList<Statement> stmts;
  ArrayList<Instruction> instrs = new ArrayList<>();
  ArrayList<Instruction> programInstructions = new ArrayList<>();
  DataBlock db;
  short orig, dbMidAddress, stackAddress, subroutineAddress;
  
  public Codegen(ArrayList<Statement> s, short orig, DataBlock db) {
    this.stmts = s;
    this.db = db;
    this.orig = orig;
  }
  
  public ArrayList<Instruction> generate() {
    initStack();
    emitCodeInstructions();
    insertDbAddresses();
    instrs.addAll(programInstructions);
    emitStaticBlocks();
    resolveSpecials();
    return instrs;
  }
  
  private short currentAddress() {
    return (short) (orig + instrs.size());
  }
  
  
  /** Add instructions to initialize R6 to the stack address */
  private void initStack() {
    // stack initialization
    instrs.add(LoadStore.ld((byte) 6, (byte) 1)); // load stack pointer
    instrs.add(Branch.any((short) 1)); // skip SP value
    instrs.add(Word.special(SpecialAddress.StackStart));
  }
  
  /** Pass 1: Emit instructions directly generated from program code */
  private void emitCodeInstructions() {
    for (Statement stmt : stmts) {
      programInstructions.addAll(stmt.emit(db));
    }
    programInstructions.add(Trap.halt());
  }
  
  /** Passes 2 and 3: Insert data block addresses and resolve specials pointing to them */
  private void insertDbAddresses() {
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
    
  }
  
  private void emitStaticBlocks() {
    // subroutines
    subroutineAddress = currentAddress();
    for (short instr : BuiltinUtils.code) {
      instrs.add(new Word(instr));
    }
    
    // update stack address
    stackAddress = currentAddress();
    ((Word) instrs.get(2)).word = stackAddress;
    
    // emit stack block (256 shorts)
    for (int i = 0; i < 256; i++) {
      instrs.add(new Word((short) 0));
    }
    
    short dbAddress =  currentAddress();
    dbMidAddress = db.setAddress(dbAddress);
    
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
  }
    
  private void resolveSpecials() {
    // Pass 4: Update remaining specials
    for (int i = 0; i < instrs.size(); i++) {
      short subroutinePCOffset = (short) (subroutineAddress - (i + 1)); // pc incremented before add
    
      if (instrs.get(i) instanceof UnresolvedSpecialInstruction) {
        UnresolvedSpecialInstruction special = (UnresolvedSpecialInstruction) instrs.get(i);
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
              special.setAddress((short) (BuiltinUtils.mul_offset + subroutinePCOffset));
              break;
            case SpecialAddress.CallDiv:
              special.setAddress((short) (BuiltinUtils.div_offset + subroutinePCOffset));
              break;
            case SpecialAddress.CallIPrint:
              special.setAddress((short) (BuiltinUtils.iprint_offset + subroutinePCOffset));
              break;
            case SpecialAddress.CallBPrint:
              special.setAddress((short) (BuiltinUtils.bprint_offset + subroutinePCOffset));
              break;
            default:
              break;
          }
        }
      }
    }
  }
}
