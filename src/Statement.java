import java.util.ArrayList;

interface Statement {
  ArrayList<Instruction> emit(DataBlock datablock);
}

class Assignment implements Statement {
  public String ident;
  public Expression expr;

  public Assignment(String ident, Expression expr) {
    this.ident = ident;
    this.expr = expr;
  }
  
  @Override
  public ArrayList<Instruction> emit(DataBlock datablock) {
    ArrayList<Instruction> instrs = new ArrayList<Instruction>();
    // Evaluate the expression, result in r0
    instrs.addAll(expr.emit((byte) 0, datablock));
    // data block offset
    instrs.add(LoadStore.ld((byte) 1, (byte) 0).special(SpecialAddress.NearestDataBlock));
    // store at specific offset
    instrs.add(LoadStoreRegOffset.str((byte) 0, (byte) 1, datablock.getVariableOffset(ident)));
    return instrs;
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
  public ArrayList<Instruction> emit(DataBlock datablock) {
    DataType dt = condition.getType(datablock);
    if (dt != DataType.Bool) {
      throw new IllegalStateException(String.format("Conditional expression must evaluate to a Bool, found %s", dt));
    }
    
    ArrayList<Instruction> instrs = new ArrayList<Instruction>();
    byte r0 = 0;
    byte r1 = 1;
    
    // if the condition is a relational BinaryExpression, we can inline
    if (condition instanceof BinaryExpression && ((BinaryExpression) condition).op.isRelational()) {
      BinaryExpression exp = (BinaryExpression) condition;
      // run sub expressions
      instrs.addAll(exp.left.emit(r0, datablock)); // result in r0
      instrs.addAll(exp.right.emit(r1, datablock)); // result in r1
      // subtraction
      instrs.add(new Not(r1, r1));
      instrs.add(AddAnd.addWithLiteral(r1, r1, (byte) 1));
      instrs.add(AddAnd.addWithRegister(r0, r0, r1));
      // we jump over the true block if the condition is false
      instrs.add(Branch.fromBinaryOperator(exp.op, (short) 0).invert());
    } else {
      // can't inline
      instrs.addAll(condition.emit(r0, datablock));
      // reset cc
      instrs.add(AddAnd.addWithLiteral(r0, r0, (byte) 0));
      // jump if false
      instrs.add(Branch.eq((short) 0));
    }
    
    int branchIdx = instrs.size() - 1;
    
    ArrayList<Instruction> bodyInstrs = body.emit(datablock);
    instrs.addAll(bodyInstrs);
    
    // normally we just have to jump over if block if condition false
    int mainBranchLength = bodyInstrs.size();
      
    // jump over else and emit else
    if (elseBody != null) {
      ArrayList<Instruction> elseInstrs = elseBody.emit(datablock);
      
      // branch over else
      instrs.add(Branch.any((short) elseInstrs.size()));
      instrs.addAll(elseInstrs);
      
      // jump over jump at end of if block
      mainBranchLength++;
    }
  
    if (repeat) {
      // jump over this jump too
      mainBranchLength++;
      
      // jump back to beginning
      instrs.add(Branch.any((short) -(instrs.size() +1)));
    }
    
    // update branch offset
    ((Branch) instrs.get(branchIdx)).offset9 = (short) mainBranchLength;
    
    return instrs;
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
  public ArrayList<Instruction> emit(DataBlock datablock) {    
    ArrayList<Instruction> instrs = new ArrayList<Instruction>();
    byte r0 = 0;
    for (Expression expr : params) {
      DataType type = expr.getType(datablock);
        
      // result in r0
      instrs.addAll(expr.emit(r0, datablock));
      switch (type) {
        case Int:
          // push to stack
          instrs.addAll(StackUtils.push(r0));
          instrs.add(Jsr.jsr((short) 0).special(SpecialAddress.CallIPrint));
          break;
        case Char:
          instrs.add(Trap.out());
          break;
        case String:
          instrs.add(Trap.puts());
          break;
        case Bool:
          // push to stack
          instrs.addAll(StackUtils.push(r0));
          instrs.add(Jsr.jsr((short) 0).special(SpecialAddress.CallBPrint));
          break;
        case Float:
          break;
      }
    }
    // newline
    instrs.add(AddAnd.andWithLiteral(r0, r0, (byte) 0x00));
    instrs.add(AddAnd.addWithLiteral(r0, r0, (byte) 0x0a));
    instrs.add(Trap.out());
    return instrs;
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
  public ArrayList<Instruction> emit(DataBlock datablock) {
    // exec each individual statement
    ArrayList<Instruction> instrs = new ArrayList<Instruction>();
    for (Statement stmt : body) {
      instrs.addAll(stmt.emit(datablock));
    }
    return instrs;
  }

  @Override
  public String toString() {
    return String.format("Block(%s)", body);
  }
}
class EmptyStatement implements Statement {
  @Override
  public ArrayList<Instruction> emit(DataBlock datablock) {
    // this gets output when you do `;;` - emit a nop
    ArrayList<Instruction> instrs = new ArrayList<Instruction>();
    instrs.add(Branch.nop());
    return instrs;
  }
  @Override
  public String toString() {
    return String.format("EmptyStatement()");
  }
}
