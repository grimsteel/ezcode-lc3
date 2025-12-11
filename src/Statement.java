import java.util.ArrayList;

enum StatementType {
  Assignment,
  Conditional,
  Print,
  Block
}

interface Statement {
  public StatementType getType();
}

class Assignment implements Statement {
  public String ident;
  public Expression expr;

  public Assignment(String ident, Expression expr) {
    this.ident = ident;
    this.expr = expr;
  }

  @Override
  public StatementType getType() {
    return StatementType.Assignment;
  }

  @Override
  public String toString() {
    return String.format("Assignment(%s, %s)", ident, expr);
  }
}
class ConditionalBlock implements Statement {
  public Expression condition;
  public Statement body;
  public Statement elseBody;
  public boolean repeat; // true if while

  public ConditionalBlock(Expression condition, Statement body, Statement elseBody, boolean repeat) {
    this.condition = condition;
    this.body = body;
    this.elseBody = elseBody;
    this.repeat = repeat;
  }

  @Override
  public StatementType getType() {
    return StatementType.Conditional;
  }

  @Override
  public String toString() {
    return String.format("ConditionalBlock(%s, %s, %s, repeat=%s)", condition, body, elseBody, repeat);
  }
}
class PrintStatement implements Statement {
  ArrayList<Expression> params;
  public PrintStatement(ArrayList<Expression> params) {
    this.params = params;
  }
  @Override
  public StatementType getType() {
    return StatementType.Print;
  }

  @Override
  public String toString() {
    return String.format("PrintStatement(%s)", params);
  }
}
class Block implements Statement {
  public ArrayList<Statement> body;

  public Block(ArrayList<Statement> body) {
    this.body = body;
  }

  @Override
  public StatementType getType() {
    return StatementType.Block;
  }

  @Override
  public String toString() {
    return String.format("Block(%s)", body);
  }
}
class EmptyStatement implements Statement {
  @Override
  public StatementType getType() {
    // no type
    return null;
  }

  @Override
  public String toString() {
    return String.format("EmptyStatement()");
  }
}
