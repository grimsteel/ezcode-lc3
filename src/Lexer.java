import java.util.List;
import java.util.regex.Matcher;
import java.util.*;
import java.io.*;
import java.awt.*;


/** Represents a general lexer that uses a specified lexicon to perform lexical
 *  analysis to generate the list of tokens in a specified source code file.
 */
public class Lexer {


	/** The list of tokens derived from a specified source code file.
	 */
	private List<Token> myTokens = new ArrayList<>();
	
	
	/** Lexically analyzes the specified source according to the specified 
	 *  lexicon. The list of tokens is then printed to the console.
	 * 
	 * @param srcFile	The name of the source code file to be compiled
	 */
	public Lexer(String srcFile, boolean printDebug) {
		this(srcFile, null, printDebug);
	}
	
	
	/** Lexically analyzes the specified source according to the specified 
	 *  lexicon. The list of tokens is then written to the specified file, 
	 *  (overwriting any previous contents should the file already exist) and
	 *  printed to the console.
	 * 
	 * @param lexFile	The name of the lexicon file
	 * @param srcFile	The name of the source code file to be compiled
	 * @param outFile	The name of the output file for the list of tokens
	 */
	public Lexer(String src, String out, boolean printDebug) {		
		this.parseSource(src, new Lexicon());
				
		if (out == null) {
			int i = src.lastIndexOf(".");
			out = src.substring(0, i) + "_TOKENS" + src.substring(i);
		}
		
		if (printDebug) {
		  System.out.print(this);
		}
	}
	
	
	/** Reads the contents of the source code to be compiled.
	 * 
	 * @param srcFile		The name of the source code file.
	 * @return				The entire source code as a single string.
	 */
	private static String readSrcFile(String srcFile) {
		String srcText = "";
		try {
			Scanner fin = new Scanner(new File(srcFile));
			while (fin.hasNextLine()) { srcText += fin.nextLine() + "\n"; }
			fin.close();
		} catch (Exception e) { e.printStackTrace(); }
		return srcText;
	}
	
	
	/** Parses the source code into a list of Token objects, which are then 
	 *  appended to myTokens.
	 *
	 * @param srcFile	The name of the source code file to be compiled
	 * @param lexicon	The set of production rules for this language
	 */
	private void parseSource(String srcFile, Lexicon lexicon) {
		String[] text = Lexer.readSrcFile(srcFile).split("\n");
		List<Rule> rules = lexicon.getRules();
    
    for (int line = 0; line < text.length; line++) {
      int col = 0;

      outer: while (col < text[line].length()) {

        for (Rule rule : rules) {
          Matcher matcher = rule.matcher.matcher(text[line].substring(col));
          // must find match + be present at start
          if (!matcher.find()) continue;

          // don't include whitespace in token list
          if (!rule.category.equals("hidden")) {
            Object value = null;
            // extract value
            if (matcher.groupCount() >= 1) {
              value = rule.parseValue(matcher.group(1));
            }
            Token token = new Token(line + 1, col + 1, rule.terminal, value);
            myTokens.add(token);
          }

          // advance
          col += matcher.end();
          continue outer;
        }

        // Print the error
        // col/line is 0-indexed
        error(srcFile, line + 1, col + 1, "No matching token found", text[line]);
        break;
      }
    }

		myTokens.add(new Token(text.length, 0, "EOF", null));
	}
	
	
	/** Prints the lexical analysis of the source code to the specified file, 
	 *  overwriting any previous contents of the file if it already exists.
	 * 
	 * @param out		The name of the output file.
	 */
	private void printFile(String out) {
		try {
			PrintWriter fout = new PrintWriter(out);
			fout.println(this);
			fout.close();
		} catch (Exception e) { e.printStackTrace(); }
	}
	
	
	/** Get the list of tokens for the source code file.
	 *  
	 * @return		The list of tokens
	 */
	public List<Token> getTokens() { return myTokens; }
	
	
	/** Prints an error message to the standard error output stream.
	 *
	 * @param file		The name of the source code file
	 * @param line		The line number of the error
	 * @param col		The column number of the error
	 * @param msg		The error message
	 * @param text		The full source code listing
	 */
	private void error(String file, int line, int col, String msg, String text) {
		System.err.println(file + ":" + line + ": " + msg);
		System.err.println(text);
		while (--col > 0) { System.err.print(" "); }
		System.err.println("^");
	}
	
	
	/** Returns a string consisting of a list of all tokens identified during
	 *  this lexical analysis.
	 * 
	 * @return		The list of all tokens in the source code file
	 */
	@Override
	public String toString() {
		String s = "";

		Formatter f = new Formatter();
		f.format("|  %5s  %5s  %-15s  %s\n", "Line", "Col", "Terminal", "Value");
		f.format("|  %5s  %5s  %-15s  %s\n", "----", "---", "--------------", "-----");
		s += f.toString();
		
		for (Token token : myTokens) {
			s += "|  " + token + "\n";
		}
		return s;
	}
	
}
