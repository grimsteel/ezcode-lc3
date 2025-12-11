interface Expression {}

enum BinaryOperator {
  And,
  Or,
  Add,
  Sub,
  Less,
  LessEqual,
  Greater,
  GreaterEqual,
  Mul,
  Div,
  Rem
}
class BinaryExpression implements Expression {
  public BinaryOperator op;
  public Expression left;
  public Expression right;
}

enum UnaryOperator {
  Negate,
  Not
}
class UnaryExpression implements Expression {
  public UnaryExpression op;
  public Expression expr;
}

class Ident implements Expression {
  public String ident;
}

enum LiteralType {
  Bool,
  Float,
  Int,
  Char,
  String
}
class Literal implements Expression {
  public LiteralType type;
  public Object value;
}
