import java.util.List;

public class Tester
{
	
	public static void main(String[] args) {
		String lex = "EZCode_lex.txt";
		
		Lexer lexer = new Lexer(lex, "test.txt");
    Parser p = new Parser(lexer.getTokens());
    
    Expression a = p.parseExpression();
    System.out.println(a);
    System.out.println(a.numRegisters());
    System.out.println(a.instructionLength());

    List<Instruction> instrs = a.emit((byte) 0);
    for (Instruction i : instrs) {
      System.out.println(i);
    }

		/*new Lexer(lex, "HelloWorld.txt");
		new Lexer(lex, "GuessingGame.txt");
		new Lexer(lex, "Factorial.txt");
		new Lexer(lex, "Foo.txt");*/
	}

}
