import java.util.ArrayList;

public class Tester
{	
	public static void main(String[] args) throws Exception {
		String lex = "EZCode_lex.txt";
    String programName = "hello";
    String objName = String.format("%s.obj", programName);
		
		Lexer lexer = new Lexer(lex, String.format("tests/%s.ezc", programName), false);
		DataBlock db = new DataBlock();
    Parser p = new Parser(lexer.getTokens(), db);
    Codegen cg = new Codegen(p.parseSequence(/* root */ true), (short) 0x3000, db);
    ArrayList<Instruction> instrs = cg.generate();

    System.out.printf("Writing obj file to assembly bytecode to %s\n", objName);
    ObjGen.writeObjFile(instrs,  objName);
	}

}
