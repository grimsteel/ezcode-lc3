public class Tester
{
	
	public static void main(String[] args) {
		String lex = "EZCode_lex.txt";
		
		Lexer lexer = new Lexer(lex, "HelloWorld.txt");
    Parser p = new Parser(lexer.getTokens());
    
    System.out.println(p.parseSequence(true));

		/*new Lexer(lex, "HelloWorld.txt");
		new Lexer(lex, "GuessingGame.txt");
		new Lexer(lex, "Factorial.txt");
		new Lexer(lex, "Foo.txt");*/
	}

}
