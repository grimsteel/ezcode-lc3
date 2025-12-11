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
  Equal,
  NotEqual,
  Mul,
  Div,
  Rem;
  
  public static BinaryOperator fromString(String opStr) {
    if (opStr == null) return null;
    switch (opStr) {
      case "AND": return And;
      case "OR": return Or;
      case "PLUS": return Add;
      case "MINUS": return Sub;
      case "LESS": return Less;
      case "LESSEQUAL": return LessEqual;
      case "GREATER": return Greater;
      case "GREATEQUAL": return GreaterEqual;
      case "MULTIPLY": return Mul;
      case "DIVIDE": return Div;
      case "REMAINDER": return Rem;
      case "EQUAL": return Equal;
      case "NOTEQUAL": return NotEqual;
      default: return null;
    }
  }
}
class BinaryExpression implements Expression {
  public BinaryOperator op;
  public Expression left;
  public Expression right;

  public BinaryExpression(BinaryOperator op, Expression left, Expression right) {
    this.op = op;
    this.left = left;
    this.right = right;
  }

  @Override
  public String toString() {
    return String.format("BinaryExpression(%s, %s, %s)", left, op, right);
  }
}

enum UnaryOperator {
  Negate,
  Not;
  
  public static UnaryOperator fromString(String opStr) {
    switch (opStr) {
      case "MINUS": return Negate;
      case "NOT": return Not;
      default: return null;
    }
  }
}
class UnaryExpression implements Expression {
  public UnaryOperator op;
  public Expression expr;

  public UnaryExpression(UnaryOperator op, Expression expr) {
    this.op = op;
    this.expr = expr;
  }

  @Override
  public String toString() {
    return String.format("UnaryExpression(%s, %s)", op, expr);
  }
}

class Ident implements Expression {
  public String ident;

  public Ident(String ident) {
    this.ident = ident;
  }

  @Override
  public String toString() {
    return String.format("Ident(%s)", ident);
  }
}

enum LiteralType {
  Bool,
  Float,
  Int,
  Char,
  String;

  public static LiteralType getType(String typeName) {
    switch (typeName.toUpperCase()) {
      case "BOOLEAN": return Bool;
      case "FLOAT": return Float;
      case "INTEGER": return Int;
      case "CHARACTER": return Char;
      case "STRING": return String;
      default: return null;
    }
  }
}

class Literal implements Expression {
  public LiteralType type;
  public Object value;

  public Literal(LiteralType type, Object value) {
    this.type = type;
    this.value = value;
  }

  @Override
  public String toString() {
    return String.format("Literal(%s, %s)", type, value);
  }
}

class InputExpression implements Expression {
  @Override
  public String toString() {
    return String.format("InputExpression()");
  }
}
