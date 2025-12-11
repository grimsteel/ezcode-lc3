import java.util.ArrayList;

interface Expression {
  /** Number of LC-3 instructions needed for this expression */
  int instructionLength();
  /** Number of registers needed to evaluate this expression */
  int numRegisters();
  ArrayList<Instruction> emit(int startRegister);
}

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

  @Override
  public int instructionLength() {
    int sub = left.instructionLength() + right.instructionLength();
    switch (op) {
      case BinaryOperator.And:
      case BinaryOperator.Add:
        return sub + 1;
      case BinaryOperator.Or:
        // not both, and not answer
        return sub + 4;
      case BinaryOperator.Sub:
        // not + add 2nd, add answer
        return sub + 3;
      case BinaryOperator.Less:
      case BinaryOperator.LessEqual:
      case BinaryOperator.Greater:
      case BinaryOperator.GreaterEqual:
      case BinaryOperator.Equal:
      case BinaryOperator.NotEqual:
        // note: when this is the condition in a conditional block, the break is inlined
        // this is only used when making a boolean variable
        // subtraction (2), set false, break, set true
        return sub + 5;
      case BinaryOperator.Mul:
      case BinaryOperator.Div:
      case BinaryOperator.Rem:
        // move parameters (2) + call method + retrieve result (1)
        // call method
        return sub + 4;
      default: return sub;
    }
  }

  @Override
  public int numRegisters() {
    // 1. evaluate left -> r0, may use r0..rn
    // 2. evaluate right -> r1, may use r1..rn, r0 needed for (1) result
    // all operations can be calculated with only r0 and r1
    return 
      Math.max(left.numRegisters(), right.numRegisters() + 1);
  }

  @Override
  public ArrayList<Instruction> emit(int startRegister) {
    ArrayList<Instruction> instrs = new ArrayList<>();
    instrs.addAll(left.emit(startRegister));
    instrs.addAll(right.emit(startRegister + 1));
    
    switch (op) {
      case BinaryOperator.And:
        instrs.add(n)
      case BinaryOperator.Add:
        return sub + 1;
      case BinaryOperator.Or:
        // not both, and not answer
        return sub + 4;
      case BinaryOperator.Sub:
        // not + add 2nd, add answer
        return sub + 3;
      case BinaryOperator.Less:
      case BinaryOperator.LessEqual:
      case BinaryOperator.Greater:
      case BinaryOperator.GreaterEqual:
      case BinaryOperator.Equal:
      case BinaryOperator.NotEqual:
        // note: when this is the condition in a conditional block, the break is inlined
        // this is only used when making a boolean variable
        // subtraction (2), set false, break, set true
        return sub + 5;
      case BinaryOperator.Mul:
      case BinaryOperator.Div:
      case BinaryOperator.Rem:
        // move parameters (2) + call method + retrieve result (1)
        // call method
        return sub + 4;
      default: return sub;
    }
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

  @Override
  public int instructionLength() {
    if (op == UnaryOperator.Not)
      return expr.instructionLength() + 1;
    else
      // not + add 1
      return expr.instructionLength() + 2;
  }

  @Override
  public int numRegisters() {
    // no additional registers needed for this - just operate on result
    return expr.numRegisters();
  }

  @Override
  public ArrayList<Instruction> emit() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'emit'");
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

  @Override
  public int instructionLength() {
    return 1;
  }

  @Override
  public int numRegisters() {
    // load directly into register
    return 1;
  }

  @Override
  public ArrayList<Instruction> emit() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'emit'");
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

  @Override
  public int instructionLength() {
    return 1;
  }

  @Override
  public int numRegisters() {
    return 1;
  }

  @Override
  public ArrayList<Instruction> emit() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'emit'");
  }
}

class InputExpression implements Expression {
  @Override
  public String toString() {
    return String.format("InputExpression()");
  }

  @Override
  public int instructionLength() {
    // call input subroutine, load from input return address
    return 2;
  }

  @Override
  public int numRegisters() {
    // load result into register, input subroutine preserves registers
    return 1;
  }

  @Override
  public ArrayList<Instruction> emit() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'emit'");
  }
}
