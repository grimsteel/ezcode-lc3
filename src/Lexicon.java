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
	
	/** The list of lexical rules for this lexicon.
	 */
	private List<Rule> myRules;
	
	
	/** Initializes a list of lexical rules for a specified lexicon.
	 * 
	 * @param lex		The relative filepath of the lexicon file.
	 */
	public Lexicon(String lex) {
		myRules = readLexicon(lex);
	}
	
	
	/** Reads the lexical ruleset for the language.
	 *
	 * @return		The list of the lexical rules in this lexicon
	 */
	private List<Rule> readLexicon(String lex) {
		List<Rule> rules = new ArrayList<>();
		try {
			Scanner fin = new Scanner(new File(lex));
			while (fin.hasNextLine()) {
				String[] rule = fin.nextLine().split("[ \t]+");
				if (rule.length == 3 && !rule[0].startsWith("//")) {
					rules.add(new Rule(rule));
				}
			}
		} catch (FileNotFoundException e) { e.printStackTrace(); }
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