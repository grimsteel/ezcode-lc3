import java.util.*;

public class Tester
{	
  private static String getOutputFile(String input, String output) {
    // they provided output filename
    if (output != null) return output;
    
    int dotIndex = input.lastIndexOf('.');
    // replace or add file extension
    if (dotIndex != -1) {
      return input.substring(0, dotIndex) + ".obj";
    } else {
      return input + ".obj";
    }
  }
	public static void main(String[] args) throws Exception {
	  // parse arguments
		HashSet<String> flags = new HashSet<>();
		
		String sourceFile = null;
		String outputFile = null;
		
		for (String arg : args) {
		  if (arg.startsWith("--")) flags.add(arg.substring(2).toLowerCase());
			else if (arg.startsWith("-")) flags.add(arg.substring(1));
			else if (sourceFile == null) sourceFile = arg;
			else if (outputFile == null) outputFile = arg;
			else {
			  // too many positionals
			  System.err.printf("Error: unexpected positional argument: `%s`\n", arg);
				System.exit(1);
			}
		}
		
		// print version
		if (flags.contains("version") || flags.contains("V")) {
		  System.err.println("ezcode-lc3 v0.1");
			return;
		}
		
		if (sourceFile == null || flags.contains("help")) {
		  // print help message
			System.err.println("USAGE: ezcode-lc3.jar [--help] [--verbose|-v] [--version|-V] SOURCE-FILE [DESTINATION-FILE]\n");
			System.err.println("EZCode to LC3 object file compiler. Recieves EZCode source as input and produces LC3-Tools .obj file bytecode.\n  If DESTINATION-FILE is not provided, the source filename with the `.obj` extension will be used.\n  Passing the --verbose flag will enable debug printing of the lexed token stream as well as the parsed syntax tree.");
			if (sourceFile == null)
			  System.exit(1);
			else
			  return;
		}
		
		outputFile = getOutputFile(sourceFile, outputFile);
		
		boolean printDebug = flags.contains("v") || flags.contains("verbose");
		
    System.out.printf("Compiling EZCode source from %s to %s\n\n", sourceFile, outputFile);
		
    System.out.println("Lexing source...");
		Lexer lexer = new Lexer(sourceFile, printDebug);
		System.out.printf("|- Successfully lexed %d tokens.\n\n", lexer.getTokens().size());
		
		DataBlock db = new DataBlock();
		
		System.out.println("Parsing tokens...");
    Parser p = new Parser(lexer.getTokens(), db);
    ArrayList<Statement> statements = p.parseSequence(/* root */ true);
    if (printDebug) {
      for (Statement st : statements) {
        System.out.printf("|  %s\n", st);
      }
    }
    System.out.printf("|- Successfully parsed %d statements.\n\n", statements.size());
    
    System.out.println("Generating code...");
    Codegen cg = new Codegen(statements, (short) 0x3000, db);
    ArrayList<Instruction> instrs = cg.generate();
    System.out.printf("|- Successfully generated %d instructions (including builtins).\n\n", instrs.size());

    System.out.printf("Writing assembly bytecode to %s\n", outputFile);
    ObjGen.writeObjFile(instrs, outputFile);
	}

}
