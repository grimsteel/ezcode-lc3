package com.kameswar;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/** Represents a lexicon as specified by a lexicon file. The file should 
 *  consist of 1 terminal symbol per row with each row being made up of 3 
 *  whitespace-separated columns representing the category, terminal symbol, 
 *  and regex for the lexical rule.
 */
public class Lexicon
{
  // harcoding the lexicon for portability
  private static final String EZCODE_LEXICON =
  "//CATEGORY      TERMINAL        REGEX\n" +
  "//----------------------------------------------------------------------------------------\n" +
  "hidden           WHITESPACE      \\s+\n" +
  "hidden           COMMENT         ((//.*$)|(/[*].*[*]/))\n" +
  "\n" +
  "operator         NOTEQUAL        !=\n" +
  "\n" +
  "operator         MULTIPLY        \\*\n" +
  "operator         DIVIDE          /\n" +
  "operator         REMAINDER       %\n" +
  "operator         PLUS            \\+\n" +
  "operator         MINUS           -\n" +
  "operator         NOT             !\n" +
  "\n" +
  "operator         LESSEQUAL       <=\n" +
  "operator         LESS            <\n" +
  "operator         GREATEQUAL      >=\n" +
  "operator         GREATER         >\n" +
  "operator         EQUAL           ==\n" +
  "\n" +
  "operator         ASSIGN          =\n" +
  "\n" +
  "operator         AND             &&\n" +
  "operator         OR              \\|\\|\n" +
  "\n" +
  "keyword          IF              [iI][fF]\n" +
  "keyword          ELSE            [eE][lL][sS][eE]\n" +
  "keyword          WHILE           [wW][hH][iI][lL][eE]\n" +
  "keyword          PRINT           [pP][rR][iI][nN][tT]\n" +
  "keyword          INPUT           [iI][nN][pP][uU][tT]\n" +
  "\n" +
  "delimeter        LPAREN          \\(\n" +
  "delimeter        RPAREN          \\)\n" +
  "delimeter        LCURLY          \\{\n" +
  "delimeter        RCURLY          \\}\n" +
  "\n" +
  "separator        SEMICOLON       ;\n" +
  "separator        COMMA           ,\n" +
  "\n" +
  "literal          BOOLEAN         ([tT][rR][uU][eE]|[fF][aA][lL][sS][eE])\n" +
  "literal          FLOAT           [-+]?(((\\d+(\\.\\d*)+)|(\\.\\d+))([eE][-+]?\\d+)?|inf|nan)\n" +
  "literal          INTEGER         [-+]?(\\d+)\n" +
  "literal          CHARACTER       '(.|\\\\.)'\n" +
  "literal          STRING          \"((:?.|\\\\.)*?)\"\n" +
  "\n" +
  "identifier       IDENTIFIER      ([_a-zA-Z][_a-zA-Z0-9]*)";
	
	/** The list of lexical rules for this lexicon.
	 */
	private List<Rule> myRules;
	
	
	public Lexicon() {
	  myRules = readLexicon(EZCODE_LEXICON);
	}
	
	
	/** Reads the lexical ruleset for the language.
	 *
	 * @return		The list of the lexical rules in this lexicon
	 */
	private List<Rule> readLexicon(String in) {
		List<Rule> rules = new ArrayList<>();
    String[] lines  = in.split("\n");
		
	  for (String line : lines) {
      String trimmed = line.trim();
      if (trimmed.length() == 0) continue;
			String[] rule = trimmed.split("[ \t]+");
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
