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
    instrs.add(LoadStoreRegOffset.str((byte) 1, (byte) 0, datablock.getVariableOffset(ident)));
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
    throw new UnsupportedOperationException("emit not implemented for ConditionalBlock");
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
      // result in r0
      instrs.addAll(expr.emit(r0, datablock));
      // push to stack
      instrs.addAll(StackUtils.push(r0));
      instrs.add(Jsr.jsr((short) 0).special(SpecialAddress.CallPrint));
      
      instrs.add(AddAnd.andWithLiteral(r0, r0, (byte) 0x00));
      instrs.add(AddAnd.addWithLiteral(r0, r0, (byte) 0x1f));
      instrs.add(AddAnd.addWithLiteral(r0, r0, (byte) 0x01));
      instrs.add(Trap.out());
    }
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
