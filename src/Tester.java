import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;

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
    String programName = "test";
    String binName = String.format("%s.bin", programName);
    String objName = String.format("%s.obj", programName);
		
		Lexer lexer = new Lexer(lex, String.format("%s.ezc", programName));
    Parser p = new Parser(lexer.getTokens());
    
    Expression a = p.parseExpression();
    System.out.println(a);
    System.out.println(a.numRegisters());
    System.out.println(a.instructionLength());

    List<Instruction> instrs = a.emit((byte) 0);

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
