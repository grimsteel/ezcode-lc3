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
  @Override
  public StatementType getType() {
    return StatementType.Assignment;
  }
}
class ConditionalBlock implements Statement {
  public Expression condition;
  public Statement body;
  public boolean repeat; // true if while

  @Override
  public StatementType getType() {
    return StatementType.Conditional;
  }
}
class PrintStatement implements Statement {
  @Override
  public StatementType getType() {
    return StatementType.Print;
  }
}
class Block implements Statement {
  public ArrayList<Statement> body;

  @Override
  public StatementType getType() {
    return StatementType.Block;
  }
}
