package com.kameswar;

import java.util.ArrayList;

interface Expression {
  /** Number of registers needed to evaluate this expression */
  int numRegisters();
  ArrayList<Instruction> emit(byte startRegister, DataBlock datablock);
  
  DataType getType(DataBlock datablock);
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
  
  public boolean isRelational() {
    switch (this) {
      case Less:
      case LessEqual:
      case Greater:
      case GreaterEqual:
      case Equal:
      case NotEqual:
        return true;
      default:
        return false;
    }
  }
  
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
  
  @Override
  public String toString() {
    switch (this) {
      case And: return "&&";
      case Add: return "+";
      case Or: return "||";
      case Sub: return "-";
      case Less: return "<";
      case LessEqual: return "<=";
      case Greater: return ">";
      case GreaterEqual: return ">=";
      case Equal: return "==";
      case NotEqual: return "!=";
      case Mul: return "*";
      case Div: return "/";
      case Rem: return "%";
      default: return "?";
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
  public DataType getType(DataBlock datablock) {
    DataType leftType = left.getType(datablock);
    DataType rightType = right.getType(datablock);

    boolean hasString = leftType == DataType.String || rightType == DataType.String;

    switch (op) {
      case And:
      case Or:
        if (hasString) break;
        if (leftType == rightType && leftType == DataType.Bool) {
          // both bools: assume logical
          return DataType.Bool;
        }
        // assume bitwise. follow java behavior and coerce everything to int
        return DataType.Int;
      case Add:
      case Sub:
      case Mul:
      case Div:
      case Rem:
        if (hasString) break;
        // mul/div/rem don't make as much practical sense but allow it and coerce to int
        return DataType.Int;
      case Less:
      case LessEqual:
      case Greater:
      case GreaterEqual:
        // doesn't make sense for strings
        if (hasString) break;
        return DataType.Bool;
      case Equal:
      case NotEqual:
        // This is always supported. Reference equality for strings.
        return DataType.Bool;
    }
    
    // if we haven't returned, it's not supported
    throw new IllegalStateException(String.format("%s and %s are invalid operands for %s", leftType, rightType, op));
  }

  @Override
  public int numRegisters() {
    // 1. evaluate left -> r0, may use r0..rn
    // 2. evaluate right -> r1, may use r1..rn, r0 needed for (1) result
    // all operations can be calculated with only r0 and r1 except mul/div/rem which require register for mem address calc
    int sub = Math.max(left.numRegisters(), right.numRegisters() + 1);;
    return sub;
  }

  @Override
  public ArrayList<Instruction> emit(byte r0, DataBlock datablock) {
    byte r1 = (byte) (r0 + 1);
    byte r2 = (byte) (r0 + 2);
    ArrayList<Instruction> instrs = new ArrayList<>();
    // run sub expressions
    instrs.addAll(left.emit(r0, datablock)); // result in r0
    instrs.addAll(right.emit(r1, datablock)); // result in r1

    switch (op) {
      case And:
        instrs.add(AddAnd.andWithRegister(r0, r0, r1));
        break;
      case Add:
        instrs.add(AddAnd.addWithRegister(r0, r0, r1));
        break;
      case Or:
        // DeMorgan's
        instrs.add(new Not(r0, r0));
        instrs.add(new Not(r1, r1));
        instrs.add(AddAnd.andWithRegister(r0, r0, r1));
        instrs.add(new Not(r0, r0));
        break;
      case Sub:
        // invert second
        instrs.add(new Not(r1, r1));
        instrs.add(AddAnd.addWithLiteral(r1, r1, (byte) 1));
        // add
        instrs.add(AddAnd.addWithRegister(r0, r0, r1));
        break;
      case Less:
      case LessEqual:
      case Greater:
      case GreaterEqual:
      case Equal:
      case NotEqual:
        // note: when this is the condition in a conditional block, the break is inlined
        // this is only used when making a boolean variable
        // subtraction (2), set false, break, set true
        // invert second
        instrs.add(new Not(r1, r1));
        instrs.add(AddAnd.addWithLiteral(r1, r1, (byte) 1));
        // add - store in r1
        instrs.add(AddAnd.addWithRegister(r1, r0, r1));
        // r0 = 0
        instrs.add(AddAnd.andWithLiteral(r0, r0, (byte) 0x0)); 
        // reset condition code
        instrs.add(AddAnd.addWithLiteral(r1, r1, (byte) 0x0));
        // if the condition does _not_ match, skip over the +1
        instrs.add(Branch.fromBinaryOperator(op, (short) 1).invert());
        // r0 = 1
        instrs.add(AddAnd.addWithLiteral(r0, r0, (byte) 0x1));
        break;
      case Mul:
      case Div:
      case Rem:
        // push stack
        instrs.addAll(StackUtils.push(r0, r1));
        instrs.add(Jsr.jsr((short) 0).special(op == BinaryOperator.Mul ? SpecialAddress.CallMult : SpecialAddress.CallDiv));
        if (op == BinaryOperator.Mul) {
          instrs.addAll(StackUtils.pop(r0));
        } else {
          // div pushes the quotient first, so -2
          byte stackOffset = op == BinaryOperator.Rem ? (byte) -1 : (byte) -2;
          instrs.add(StackUtils.at(r0, stackOffset));
          // adjust SP
          instrs.add(StackUtils.adjustSP((byte) -2));
        }
        break;
    }
    
    return instrs;
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
  
  @Override
  public String toString() {
    switch (this) {
      case Negate: return "-";
      case Not: return "!";
      default: return "?";
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
  public int numRegisters() {
    // no additional registers needed for this - just operate on result
    return expr.numRegisters();
  }
  
  @Override
  public DataType getType(DataBlock datablock) {
    DataType type = expr.getType(datablock);

    if (type != DataType.String) {    
      switch (op) {
        case Not:
          if (type == DataType.Bool) {
            // logical
            return DataType.Bool;
          }
          // bitwise: coerce to int
          return DataType.Int;
        case Negate:
          // coerce to int
          return DataType.Int;
      }
    }
    
    // if we haven't returned, it's not supported
    throw new IllegalStateException(String.format("%s cannot be applied to %s", op, type));
  }

  @Override
  public ArrayList<Instruction> emit(byte r0, DataBlock datablock) {
    ArrayList<Instruction> instrs = new ArrayList<>();
    instrs.addAll(expr.emit(r0, datablock));
    switch (op) {
      case Not:
        instrs.add(new Not(r0, r0));
        break;
      case Negate:
        instrs.add(new Not(r0, r0));
        instrs.add(AddAnd.addWithLiteral(r0, r0, (byte) 1));
        break;
    }
    return instrs;
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
  public int numRegisters() {
    // load directly into register
    return 1;
  }
  
  @Override
  public DataType getType(DataBlock datablock) {
    // get variable type from datablock. this also handles undefined variables
    return datablock.getVariableType(ident);
  }

  @Override
  public ArrayList<Instruction> emit(byte r0, DataBlock datablock) {
    ArrayList<Instruction> instrs = new ArrayList<>();
    // get the data block offset
    instrs.add(LoadStore.ld(r0, (short) 0).special(SpecialAddress.NearestDataBlock));
    // load from offset
    instrs.add(LoadStoreRegOffset.ldr(r0, r0, datablock.getVariableOffset(ident)));
    
    return instrs;
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
  public byte constantIndex;

  public Literal(LiteralType type, Object value) {
    this.type = type;
    this.value = value;
  }

  @Override
  public String toString() {
    return String.format("Literal(%s, %s)", type, value);
  }

  @Override
  public int numRegisters() {
    return 1;
  }
  
  public short shortValue() {
    short value = 0;

    switch (type) {
      case String:
        // placeholder when we don't know address
        value = 0;
        break;
      case Float:
        throw new UnsupportedOperationException("Floats are not yet supported");
      case Int:
        value = ((Integer) this.value).shortValue();
        break;
      case Char:
        value = (short) ((Character) this.value).charValue();
        break;
      case Bool:
        value = (short) (((Boolean) this.value).booleanValue() ? 1 : 0);
        break;
    }
    
    return value;
  }
  
  public boolean canInline() {
    if (type == LiteralType.String) return false;
    
    // whether we can inline in a single add instruction
    short value = shortValue();
    // 6 bit signed
    return value >= -32 && value < 32;
  }

  @Override
  public DataType getType(DataBlock datablock) {
    switch (type) {
      case Bool: return DataType.Bool;
      case Float: return DataType.Float;
      case Int: return DataType.Int;
      case Char: return DataType.Char;
      case String: return DataType.String;
      default: throw new IllegalStateException("Unknown literal type");
    }
  }

  @Override
  public ArrayList<Instruction> emit(byte r0, DataBlock datablock) {
    ArrayList<Instruction> instrs = new ArrayList<>();
    
    if (canInline()) {
      instrs.add(AddAnd.andWithLiteral(r0, r0, (byte) 0));
      instrs.add(AddAnd.addWithLiteral(r0, r0, (byte) shortValue()));
    } else {
      // get the data block offset
      instrs.add(LoadStore.ld(r0, (short) 0).special(SpecialAddress.NearestDataBlock));
      // load from offset
      instrs.add(LoadStoreRegOffset.ldr(r0, r0, datablock.getConstantOffset(constantIndex)));
    }
    return instrs;
  }
}

class InputExpression implements Expression {
  @Override
  public String toString() {
    return String.format("InputExpression()");
  }

  @Override
  public int numRegisters() {
    // load result into register, input subroutine preserves registers
    return 1;
  }

  @Override
  public DataType getType(DataBlock datablock) {
    return DataType.Int;
  }

  @Override
  public ArrayList<Instruction> emit(byte r0, DataBlock datablock) {
    ArrayList<Instruction> instrs = new ArrayList<>();
    instrs.add(Jsr.jsr((short) 0).special(SpecialAddress.CallInput));
    instrs.addAll(StackUtils.pop((byte) r0));
    return instrs;
  }
}
