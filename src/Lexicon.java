import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

/** Represents a lexicon as specified by a lexicon file. The file should 
 *  consist of 1 terminal symbol per row with each row being made up of 3 
 *  whitespace-separated columns representing the category, terminal symbol, 
 *  and regex for the lexical rule.
 */
public class Lexicon
{
  // harcoding the lexicon for portability
  private static final String EZCODE_LEXICON = """
  //CATEGORY      TERMINAL        REGEX
  //----------------------------------------------------------------------------------------
  hidden           WHITESPACE      \\s+
  hidden           COMMENT         ((//.*$)|(/[*].*[*]/))
  
  operator         NOTEQUAL        !=
  
  operator         MULTIPLY        \\*
  operator         DIVIDE          /
  operator         REMAINDER       %
  operator         PLUS            \\+
  operator         MINUS           -
  operator         NOT             !
  
  operator         LESSEQUAL       <=
  operator         LESS            <
  operator         GREATEQUAL      >=
  operator         GREATER         >
  operator         EQUAL           ==
  
  operator         ASSIGN          =
  
  operator         AND             &&
  operator         OR              \\|\\|
  
  keyword          IF              [iI][fF]
  keyword          ELSE            [eE][lL][sS][eE]
  keyword          WHILE           [wW][hH][iI][lL][eE]
  keyword          PRINT           [pP][rR][iI][nN][tT]
  keyword          INPUT           [iI][nN][pP][uU][tT]
  
  delimeter        LPAREN          \\(
  delimeter        RPAREN          \\)
  delimeter        LCURLY          \\{
  delimeter        RCURLY          \\}
  
  separator        SEMICOLON       ;
  separator        COMMA           ,
  
  literal          BOOLEAN         ([tT][rR][uU][eE]|[fF][aA][lL][sS][eE])
  literal          FLOAT           [-+]?(((\\d+(\\.\\d*)+)|(\\.\\d+))([eE][-+]?\\d+)?|inf|nan)
  literal          INTEGER         [-+]?(\\d+)
  literal          CHARACTER       '(.|\\\\.)'
  literal          STRING          "((:?.|\\\\.)*?)"
  
  identifier       IDENTIFIER      ([_a-zA-Z][_a-zA-Z0-9]*)""";
	
	/** The list of lexical rules for this lexicon.
	 */
	private List<Rule> myRules;
	
	
	/** Initializes a list of lexical rules for a specified lexicon.
	 * 
	 * @param lex		The relative filepath of the lexicon file.
	 */
	public Lexicon(String lex) {
	  try {
			myRules = readLexicon(new Scanner(new File(lex)));
		} catch (FileNotFoundException e) {
		  e.printStackTrace();
			System.exit(1);
		}
	}
	
	public Lexicon() {
	  myRules = readLexicon(new Scanner(EZCODE_LEXICON));
	}
	
	
	/** Reads the lexical ruleset for the language.
	 *
	 * @return		The list of the lexical rules in this lexicon
	 */
	private List<Rule> readLexicon(Scanner fin) {
		List<Rule> rules = new ArrayList<>();
		
		while (fin.hasNextLine()) {
			String[] rule = fin.nextLine().split("[ \t]+");
			if (rule.length == 3 && !rule[0].startsWith("//")) {
				rules.add(new Rule(rule));
			}
		}
		return rules;
	}
	
	
	
	/** Get the list of rules for the lexicon.
	 *  
	 * @return		The list of the lexical rules in this lexicon
	 */
	public List<Rule> getRules() {
		return myRules;
	}
	
	
	/** Get the list of terminals for the lexicon.
	 *  
	 * @return		The list of terminal symbols in this lexicon
	 */
	public List<String> getTerminals() {
		List<String> terminals = new ArrayList<>();
		for (Rule rule : myRules) {
			terminals.add(rule.terminal);
		}
		return terminals;
	}
	

}
