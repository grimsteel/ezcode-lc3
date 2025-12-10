import java.util.Formatter;



/** Represents a lexical "token" that has been extracted from a source code
 *  file. Each token is represented by its value, the terminal symbol that it
 *  corresponds to, and the line/column number in which the value appears in 
 *  the source code file.
 */
public class Token {
	
	private int myLine;
	private int myColumn;
	private String myTerminal;
	private Object myValue;
	
	
	/** Represents a lexical token extracted from a source code file.
	 *
	 * @param line		The line number where they token appears in the source file
	 * @param column	The column where the token appears in the source file
	 * @param terminal	The terminal symbol this token represents
	 * @param value		The literal text for this token
	 */
	public Token(int line, int column, String terminal, Object value) {
		myLine = line;
		myColumn = column;
		myTerminal = terminal;
		myValue = value;
	}
	
	
	/** Gets the line number where the token appears in the source code file.
	 * 
	 * @return		The token's line number in the source code file
	 */
	public int getLine() { return myLine; }

	
	/** Gets the column number where the token appears in the source code file.
	 * 
	 * @return		The token's column number in the source code file
	 */
	public int getColumn() { return myColumn; }

	
	/** Gets the terminal symbol associated with the token
	 * 
	 * @return		The terminal symbol for the token
	 */
	public String getTerminal() { return myTerminal; }


	/** Gets the value of the token as an Object that best represents the type
	 *  of data (i.e., Integer, Boolean, Double, Character, etc.).
	 * 
	 * @return		The value of the token
	 */
	public Object getValue() { return myValue; }
	
	
	/** Returns a formatted string representation of this token, including its
	 *  line number, column number, terminal symbol, and literal text.
	 * 
	 * @return		The string representation of this token
	 */
	public String toString() {		
		Formatter f = new Formatter();
		f.format("%5d  %5d  %-15s  %s", myLine, myColumn, myTerminal, myValue);
		String s = f.toString();
		f.close();
		return s;
	}
	

}

