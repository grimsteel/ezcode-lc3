import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;


/** Represents a lexicon as specified by a lexicon file. The file should 
 *  consist of 1 terminal symbol per row with each row being made up of 3 
 *  whitespace-separated columns representing the category, terminal symbol, 
 *  and regex for the lexical rule.
 */
public class Grammar
{
	
	/** The list of lexical rules for this lexicon.
	 */
  public HashMap<String, Morpheme> morphemes = new HashMap<>();
	
	
	/** Initializes a list of lexical rules for a specified lexicon.
	 * 
	 * @param lex		The relative filepath of the lexicon file.
	 */
	public Grammar(String grammar) {
    addMorpheme(Morpheme.repeating("sequence", "statement"));
    addMorpheme(Morpheme.union("statement", new String[] {
      "semicolon",
      "assignment",
      "while_block",
      "if_block",
      "print_statement",
      "block"
    }));
    addMorpheme(Morpheme.terminal("semicolon", "SEMICOLON"));
    addMorpheme(Morpheme.sequence("assignment", new String[] {
      "identifier",
      "assign",
      "expression",
      "semicolon"
    }));
    addMorpheme(Morpheme.terminal("identifier", "IDENTIFIER"));
    addMorpheme(Morpheme.terminal("assign", "ASSIGN"));
    addMorpheme(Morpheme.sequence("while_block", new String[] {
      "while",
      "paren_expression",
      "statement"
    }));
    addMorpheme(Morpheme.terminal("while", "WHILE"));
    addMorpheme(Morpheme.terminal("if", "IF"));
    addMorpheme(Morpheme.terminal("else", "ELSE"));
    addMorpheme(Morpheme.terminal("lparen", "LPAREN"));
    addMorpheme(Morpheme.terminal("rparen", "RPAREN"));
    addMorpheme(Morpheme.terminal("lcurly", "LCURLY"));
    addMorpheme(Morpheme.terminal("rcurly", "RCURLY"));
    addMorpheme(Morpheme.terminal("print", "PRINT"));
    addMorpheme(Morpheme.sequence("if_block", new String[] {
      "if",
      "paren_expression",
      "statement",
      "else_block"
    }));
    addMorpheme(Morpheme.optional("else_block", "else_block_inner"));
    addMorpheme(Morpheme.sequence("else_block_inner", new String[] {
      "else",
      "statement"
    }));
    addMorpheme(Morpheme.sequence("print_statement", new String[] {
      "print",
      "lparen",
      "concat_list_optional",
      "rparen",
      "semicolon"
    }));
    addMorpheme(Morpheme.sequence("block", new String[] {
      "lcurly",
      "sequence",
      "rcurly"
    }));
    addMorpheme(Morpheme.sequence("paren_expression", new String[] {
      "lparen",
      "expression",
      "rcurly"
    }));
    addMorpheme(Morpheme.optional("concat_list_optional", "concat_list"));
    addMorpheme(Morpheme.sequence("concat_list", new String[] {
      "expression",
      "concat_list_multiple"
    }));
    addMorpheme(Morpheme.repeating("concat_list_multiple", "concat_list_additional"));
    addMorpheme(Morpheme.sequence("concat_list_additional", new String[] {
      "comma",
      "expression"
    }));
    addMorpheme(Morpheme.terminal("comma", "COMMA"));

    addMorpheme(Morpheme.union("expression", new String[] {
      "input_expression",
      "or_expr"
    }));
    addMorpheme(Morpheme.sequence("input_expression", new String[] {
      "input",
      "lparen",
      "rparen"
    }));

    addBinaryExpression("or", "and_expr");
    addBinaryExpression("and", "equal_expr");
    addBinaryExpression("equal", "rel_expr");
    addBinaryExpression("rel", "add_expr");
    addBinaryExpression("add", "mult_expr");
    addBinaryExpression("mult", "term");

    addMorpheme(Morpheme.union("term", new String[] {
      "ident",
      "bool",
      "float",
      "int",
      "char",
      "string",
      "paren_expression",
      "unary_expression"
    }));
    addMorpheme(Morpheme.union("equal", new String[] {
      "equal",
      "not_equal"
    }));    
    addMorpheme(Morpheme.union("rel", new String[] {
      "less_equal",
      "less",
      "greater_equal",
      "greater"
    })); 
    addMorpheme(Morpheme.union("add", new String[] {
      "plus",
      "minus"
    })); 
    addMorpheme(Morpheme.union("mult", new String[] {
      "multiply",
      "divide",
      "remainder"
    })); 

    addMorpheme(Morpheme.terminal("or", "OR"));
    addMorpheme(Morpheme.terminal("and", "AND"));
    addMorpheme(Morpheme.terminal("equal", "EQUAL"));
    addMorpheme(Morpheme.terminal("not_equal", "NOTEQUAL"));
    addMorpheme(Morpheme.terminal("less_equal", "LESSEQUAL"));
    addMorpheme(Morpheme.terminal("less", "LESS"));
    addMorpheme(Morpheme.terminal("greater_equal", "GREATEQUAL"));
    addMorpheme(Morpheme.terminal("greater", "GREATER"));
    addMorpheme(Morpheme.terminal("plus", "PLUS"));
    addMorpheme(Morpheme.terminal("minus", "MINUS"));
    addMorpheme(Morpheme.terminal("multiply", "MULTIPLY"));
    addMorpheme(Morpheme.terminal("divide", "DIVIDE"));
    addMorpheme(Morpheme.terminal("remainder", "REMAINDER"));
    addMorpheme(Morpheme.terminal("not", "NOT"));

    addMorpheme(Morpheme.terminal("ident", "IDENTIFIER"));
    addMorpheme(Morpheme.terminal("bool", "BOOLEAN"));
    addMorpheme(Morpheme.terminal("float", "FLOAT"));
    addMorpheme(Morpheme.terminal("int", "INTEGER"));
    addMorpheme(Morpheme.terminal("char", "CHARACTER"));
    addMorpheme(Morpheme.terminal("string", "STRING"));

    addMorpheme(Morpheme.sequence("unary_expression", new String[] {"unary_operator", "term"}));
    addMorpheme(Morpheme.union("unary_operator", new String[] {"plus", "minus", "not"}));
	}

  public void addBinaryExpression(String operator, String operands) {
    String exprName = String.format("%s_expr", operator);
    String repeatName =String.format("%s_repeat", operator);
    String rightName = String.format("%s_right", operator);

    addMorpheme(Morpheme.sequence(exprName, new String[] { operands, repeatName }));
    addMorpheme(Morpheme.repeating(repeatName, rightName));
    addMorpheme(Morpheme.sequence(rightName, new String[] { operator, operands }));
  }

  public void addMorpheme(Morpheme morpheme) {
    this.morphemes.put(morpheme.name, morpheme);
  }
}