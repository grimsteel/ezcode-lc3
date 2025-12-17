package com.kameswar;

import java.util.ArrayList;
import java.util.List;

public class Parser {
  DataBlock dataBlock;
  
  List<Token> tokens;
  int pos = 0;
  
  Token peek() {
    return tokens.get(pos);
  }
  Token peek2() {
    return tokens.get(pos + 1);
  }
  boolean expect(String terminal) {
    if (peek().getTerminal().equals(terminal)) {
      // advance past
      pos++;
      return true;
    } else {
      return false;
    }
  }
  String expect(String[] terminals) {
    String current = peek().getTerminal();
    for (String terminal : terminals) {
      if (current.equals(terminal)) {
        pos++;
        return terminal;
      }
    }
    return null;
  }
  
  public Parser(List<Token> tokens, DataBlock db) {
    this.tokens = tokens;
    this.dataBlock = db;
  }
  
  ArrayList<Statement> parseSequence(boolean root) {
    ArrayList<Statement> stmts = new ArrayList<>();
    while (true) {
      if (root && expect("EOF")) {
        break;
      }
      
      Statement stmt = parseStatement();
      if (stmt == null) {
        if (root) {
          // only print an error if this is the root
          System.out.printf("Unexpected %s\n", peek());
        }
        break;
      }
      stmts.add(stmt);
    }
    return stmts;
  }
  
  Statement parseStatement() {
    if (expect("SEMICOLON")) {
      return new EmptyStatement();
    }
    Statement stmt;

    stmt = parseAssignmentStatement();
    if (stmt != null) return stmt;

    stmt = parseWhileStatement();
    if (stmt != null) return stmt;

    stmt = parseIfStatement();
    if (stmt != null) return stmt;

    stmt = parsePrintStatement();
    if (stmt != null) return stmt;

    stmt = parseBlock();
    if (stmt != null) return stmt;

    return null;
  }
  
  Assignment parseAssignmentStatement() {
    int currentPos = pos;
    
    Object identObj = peek().getValue();
    if (expect("IDENTIFIER") && expect("ASSIGN")) {
      // now we can cast
      String ident = (String) identObj;
      
      Expression expr = parseExpression();
      if (expr != null && expect("SEMICOLON")) {
        // register the variable
        dataBlock.addVariable(ident, expr.getType(dataBlock));
        
        return new Assignment(ident, expr);
      }
    }
    // reset pos
    pos = currentPos;
    return null;
  }
  
  ConditionalBlock parseWhileStatement() {
    int currentPos = pos;
    
    if (expect("WHILE")) {
      Expression condition = parseParen();
      if (condition != null) {
        Statement stmt = parseStatement();
        if (stmt != null) {
          return new ConditionalBlock(condition, stmt, null, true);
        }
      }
    }
    // reset pos
    pos = currentPos;
    return null;
  }
  
  ConditionalBlock parseIfStatement() {
    int currentPos = pos;
    
    if (expect("IF")) {
      Expression condition = parseParen();
      if (condition != null) {
        Statement stmt = parseStatement();
        if (stmt != null) {
          // else is optional
          int beforeElse = pos;
          if (expect("ELSE")) {
            Statement elseStmt = parseStatement();
            if (elseStmt != null) {
              return new ConditionalBlock(condition, stmt, elseStmt, false);
            }
          }
          // no else statement
          pos = beforeElse;
          return new ConditionalBlock(condition, stmt, null, false);
        }
      }
    }
    // reset pos
    pos = currentPos;
    return null;
  }
  
  PrintStatement parsePrintStatement() {
    int currentPos = pos;
    
    if (expect("PRINT") && expect("LPAREN")) {
      ArrayList<Expression> params = parseConcatList();
      if (params != null && expect("RPAREN") && expect("SEMICOLON")) {
        return new PrintStatement(params);
      }
    }
    // reset pos
    pos = currentPos;
    return null;
  }
  
  Block parseBlock() {
    int currentPos = pos;
    if (expect("LCURLY")) {
      ArrayList<Statement> stmts = parseSequence(false);
      if (stmts != null && expect("RCURLY")) {
        return new Block(stmts);
      }
    }
    // reset pos
    pos = currentPos;
    return null;
  }
  
  ArrayList<Expression> parseConcatList() {
    Expression first = parseExpression();
    if (first == null) {
      return null;
    }
    ArrayList<Expression> exprs = new ArrayList<>();
    exprs.add(first);
    while (true) {
      int initialPos = pos;
      if (expect("COMMA")) {
        Expression next = parseExpression();
        if (next == null) {
          // move before comma and break
          pos = initialPos;
          break;
        }
        exprs.add(next);
      } else {
        // no more items
        break;
      }
    }
    return exprs;
  }
  
  Expression parseExpression() {
    Expression input = parseInputExpression();
    if (input != null) {
      return input;
    }
    return parseOr();
  }
  
  InputExpression parseInputExpression() {
    int initialPos = pos;
    if (expect("INPUT") && expect("LPAREN") && expect("RPAREN")) {
      return new InputExpression();
    }
    pos = initialPos;
    return null;
  }
  
  
  /** utility interface for cleaner binary expression parsing */
  interface ExpressionParser {
    Expression parse();
  }
  
  Expression parseBinaryExpression(String[] expectedOperators, ExpressionParser nextParser) {
    int initialPos = pos;
    
    Expression left = nextParser.parse();
    if (left != null) {
      while (true) {
        int startPos = pos;
        
        BinaryOperator op = BinaryOperator.fromString(expect(expectedOperators));
        if (op == null) {
          // go before the operand and quit
          pos = startPos;
          break;
        }
        // get rhs
        Expression right = nextParser.parse();
        if (right == null) {
          // go before the operand and quit
          pos = startPos;
          break;
        }
        
        // left associativity - collapse into LHS
        left = new BinaryExpression(op, left, right);
      }
      
      return left;
    }
    
    // couldn't match left
    pos = initialPos;
    return null;
  }
  
  // binary utility methods
  Expression parseOr() {
    return parseBinaryExpression(new String[] { "OR" }, this::parseAnd);
  }
  Expression parseAnd() {
    return parseBinaryExpression(new String[] { "AND" }, this::parseEqual);
  }
  Expression parseEqual() {
    return parseBinaryExpression(new String[] { "EQUAL", "NOTEQUAL" }, this::parseRel);
  }
  Expression parseRel() {
    return parseBinaryExpression(new String[] { "LESSEQUAL", "LESS", "GREATEQUAL", "GREATER" }, this::parseAdd);
  }
  Expression parseAdd() {
    return parseBinaryExpression(new String[] { "PLUS", "MINUS" }, this::parseMult);
  }
  Expression parseMult() {
    return parseBinaryExpression(new String[] { "MULTIPLY", "DIVIDE", "REMAINDER" }, this::parseTerm);
  }
  
  public Expression parseUnary() {
    Token next = peek();
    switch (next.getTerminal()) {
      case "PLUS":
      case "MINUS":
      case "NOT":
        // advance past
        pos++;
        Expression term = parseTerm();
        if (term != null) {
          if (next.getTerminal().equals("PLUS")) {
            // unary plus does nothing
            return term;
          } else {
            return new UnaryExpression(UnaryOperator.fromString(next.getTerminal()), term);
          }
        } else {
          // go before plus/minus/not - this didn't work
          pos--;
          return null;
        }
      default:
        return null;
    }
  }
  
  public Expression parseParen() {
    int currentPos = pos;
    if (expect("LPAREN")) {
      Expression expr = parseExpression();
      if (expr != null && expect("RPAREN")) {
        return expr;
      }
    }
    // reset pos
    pos = currentPos;
    return null;
  }
  
  public Expression parseTerm() {
    Token next = peek();
    switch (next.getTerminal()) {
      case "IDENTIFIER":
        pos++;
        return new Ident((String) next.getValue());
      case "BOOLEAN":
      case "FLOAT":
      case "INTEGER":
      case "CHARACTER":
      case "STRING":
        pos++;
        Literal l = new Literal(LiteralType.getType(next.getTerminal()), next.getValue());
        // register this literal
        dataBlock.addLiteral(l);
        return l;
    }
    
    Expression paren = parseParen();
    if (paren != null) return paren;
    
    return parseUnary();
  }
}
