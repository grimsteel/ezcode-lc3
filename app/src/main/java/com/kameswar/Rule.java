package com.kameswar;

import java.util.InputMismatchException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Represents a lexical rule associating a terminal symbol with its category
 *  (e.g., "operator", "delimiter", etc.) and the regular expression that 
 *  describes strings matching the rule.
 */
public class Rule {
	
	public final String category;
	public final String terminal;
	public final String regex;
  public final Pattern matcher;
	
	
	/** Constructs a lexical rule consisting of a category, terminal symbol, 
	 *  and a regular expression describing strings that match the rule.
	 *
	 * @param rule		An array containing the category, terminal, and regex
	 */
	public Rule(String[] rule) {
		category = rule[0];
		terminal = rule[1];
		regex = rule[2];
    // match at beginning
    matcher = Pattern.compile("^" + regex, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
	}
	
	
	/** A string representation of this lexical rule.
	 * 
	 * @return		A string in the form "(category) TERMINAL: regex"
	 */
	@Override
	public String toString() {
		return "(" + category + ") " + terminal + ": " + regex;
	}

  public Object parseValue(String matchedGroup) {
    switch (terminal) {
      case "INTEGER":
        return Integer.valueOf(matchedGroup);
      case "FLOAT":
        // special cases
        if (matchedGroup.toLowerCase().endsWith("nan")) return Double.NaN;
        else if (matchedGroup.toLowerCase().endsWith("inf")) {
          if (matchedGroup.charAt(0) == '-') return Double.NEGATIVE_INFINITY;
          else return Double.POSITIVE_INFINITY;
        }
        return Double.valueOf(matchedGroup);
      case "BOOLEAN":
        return Boolean.valueOf(matchedGroup);
      case "CHARACTER":
        if (matchedGroup.length() != 1) {
          throw new InputMismatchException("Character literals must have a length of 1");
        }
        return matchedGroup.charAt(0);
      default:
        return matchedGroup;
    }
  }
}
