enum Opcode {
  Branch(0x0),
  Add(0x1),
  LoadDirect(0x2),
  StoreDirect(0x3),
  JumpSubroutine(0x4),
  And(0x5),
  Load(0x6),
  Store(0x7),
  ReturnFromInterrupt(0x8),
  Not(0x9),
  LoadIndirect(0xA),
  StoreIndirect(0xB),
  Jump(0xC),
  Reserved(0xD),
  LoadEffectiveAddress(0xE),
  Trap(0xF);

  public byte opcode;
  Opcode(int opcode) {
    this.opcode = (byte) opcode;
  }
}

interface Instruction {
  public Opcode getOpcode();
  public short emit();
}

class Branch implements Instruction {
  short offset9;
  boolean n, z, p;
  
  public Opcode getOpcode() { return Opcode.Branch; }
  
  public Branch(short offset9, boolean n, boolean z, boolean p) {
    this.offset9 = offset9;
    this.n = n;
    this.z = z;
    this.p = p;
  }
  
  public static Branch eq(short offset9) {
    return new Branch(offset9, false, true, false);
  }

  public static Branch ne(short offset9) {
    return new Branch(offset9, true, false, true);
  }

  public static Branch lt(short offset9) {
    return new Branch(offset9, true, false, false);
  }

  public static Branch le(short offset9) {
    return new Branch(offset9, true, true, false);
  }

  public static Branch ge(short offset9) {
    return new Branch(offset9, false, true, true);
  }

  public static Branch gt(short offset9) {
    return new Branch(offset9, false, false, true);
  }

  public static Branch any(short offset9) {
    return new Branch(offset9, true, true, true);
  }
  
  @Override
  public short emit() {
    short result = 0;
    // 4 highest bits: opcode
    result |= Opcode.Branch.opcode << 12;
    // n, z, p bits
    result |= (n ? 1 : 0) << 11;
    result |= (z ? 1 : 0) << 10;
    result |= (p ? 1 : 0) << 9;
    // offset9: lower 9 bits (sign-extended)
    result |= (offset9 & 0x1FF);
    return (short) result;
  }
}

/**
* `add` and `and` are basically the same, so one class for both
*/
class AddAnd implements Instruction {
  byte dest;
  byte left;
  boolean isLiteral;
  // literal or register
  byte right;
  
  Opcode opcode;
  
  public Opcode getOpcode() {
    return opcode;
  }

  private AddAnd(Opcode opcode, byte dest, byte left, boolean isLiteral, byte rightLiteralOrReg) {
    this.opcode = opcode;
    this.dest = dest;
    this.left = left;
    this.isLiteral = isLiteral;
    this.right = rightLiteralOrReg;
  }

  @Override
  public short emit() {
    short result = 0;
    // 4 highest bits: opcode
    result |= opcode.opcode << 12;
    // next 3 bits: dest register
    result |= (dest & 0x7) << 9;
    // next 3 bits: left register
    result |= (left & 0x7) << 6;
    if (isLiteral) {
      // bit 5 = 0, next 3 bits: right register
      result |= (right & 0x7);
    } else {
      // bit 5 = 1, next 5 bits: immediate
      result |= 1 << 5;
      result |= (right & 0x1F);
    }
    return result;
  }
  
  public static AddAnd addWithLiteral(byte dest, byte left, byte literal) {
    return new AddAnd(Opcode.Add, dest, left, true, literal);
  }

  public static AddAnd addWithRegister(byte dest, byte left, byte rightReg) {
    return new AddAnd(Opcode.Add, dest, left, false, rightReg);
  }

  public static AddAnd andWithLiteral(byte dest, byte left, byte literal) {
    return new AddAnd(Opcode.And, dest, left, true, literal);
  }

  public static AddAnd andWithRegister(byte dest, byte left, byte rightReg) {
    return new AddAnd(Opcode.And, dest, left, false, rightReg);
  }
}

/**
* Instructions with one register and an offset9: LD, LDI, LEA, ST, STI
*/
class LoadStore implements Instruction {
  Opcode opcode;
  byte reg;
  short offset9;

  private LoadStore(Opcode opcode, byte reg, short offset9) {
    this.opcode = opcode;
    this.reg = reg;
    this.offset9 = offset9;
  }

  @Override
  public Opcode getOpcode() {
    return opcode;
  }

  @Override
  public short emit() {
    short result = 0;
    // 4 highest bits: opcode
    result |= opcode.opcode << 12;
    // next 3 bits: register
    result |= (reg & 0x7) << 9;
    // offset9: lower 9 bits
    result |= (offset9 & 0x1FF);
    return result;
  }

  // Static initialization methods for each instruction
  public static LoadStore ld(byte dest, short offset9) {
    return new LoadStore(Opcode.Load, dest, offset9);
  }

  public static LoadStore ldi(byte dest, short offset9) {
    return new LoadStore(Opcode.LoadIndirect, dest, offset9);
  }

  public static LoadStore lea(byte dest, short offset9) {
    return new LoadStore(Opcode.LoadEffectiveAddress, dest, offset9);
  }

  public static LoadStore st(byte src, short offset9) {
    return new LoadStore(Opcode.Store, src, offset9);
  }

  public static LoadStore sti(byte src, short offset9) {
    return new LoadStore(Opcode.StoreIndirect, src, offset9);
  }
}

/**
* Instructions with two registers and an offset6: LDR, STR
*/
class LoadStoreRegOffset implements Instruction {
  Opcode opcode;
  byte reg1; // destination for LDR, source for STR
  byte reg2; // base register
  byte offset6;

  private LoadStoreRegOffset(Opcode opcode, byte reg1, byte reg2, byte offset6) {
    this.opcode = opcode;
    this.reg1 = reg1;
    this.reg2 = reg2;
    this.offset6 = offset6;
  }

  @Override
  public Opcode getOpcode() {
    return opcode;
  }

  @Override
  public short emit() {
    short result = 0;
    // 4 highest bits: opcode
    result |= opcode.opcode << 12;
    // next 3 bits: reg1
    result |= (reg1 & 0x7) << 9;
    // next 3 bits: reg2 (base)
    result |= (reg2 & 0x7) << 6;
    // offset6: lower 6 bits
    result |= (offset6 & 0x3F);
    return result;
  }

  // Static initialization methods for each instruction
  public static LoadStoreRegOffset ldr(byte dest, byte base, byte offset6) {
    return new LoadStoreRegOffset(Opcode.LoadDirect, dest, base, offset6);
  }

  public static LoadStoreRegOffset str(byte src, byte base, byte offset6) {
    return new LoadStoreRegOffset(Opcode.StoreDirect, src, base, offset6);
  }
}

class Trap implements Instruction {
  byte intCode;

  public Trap(byte trapVector) {
    this.intCode = trapVector;
  }

  @Override
  public Opcode getOpcode() {
    return Opcode.Trap;
  }

  @Override
  public short emit() {
    short result = 0;
    // 4 highest bits: opcode
    result |= Opcode.Trap.opcode << 12;
    // lower 8 bits: trap vector
    result |= (intCode & 0xFF);
    return result;
  }

  // Built-in traps
  public static Trap getc() {
    return new Trap((byte) 0x20);
  }

  public static Trap out() {
    return new Trap((byte) 0x21);
  }

  public static Trap puts() {
    return new Trap((byte) 0x22);
  }

  public static Trap halt() {
    return new Trap((byte) 0x25);
  }
}

class Not implements Instruction {
  byte dest;
  byte src;

  public Not(byte dest, byte src) {
    this.dest = dest;
    this.src = src;
  }

  @Override
  public Opcode getOpcode() {
    return Opcode.Not;
  }

  @Override
  public short emit() {
    short result = 0;
    // 4 highest bits: opcode
    result |= Opcode.Not.opcode << 12;
    // next 3 bits: dest register
    result |= (dest & 0x7) << 9;
    // next 3 bits: src register
    result |= (src & 0x7) << 6;
    // bits 5-0: all 1s for NOT instruction
    result |= 0x3F;
    return result;
  }
}

class Jmp implements Instruction {
  byte baseReg;

  public Jmp(byte baseReg) {
    this.baseReg = baseReg;
  }

  @Override
  public Opcode getOpcode() {
    return Opcode.Jump;
  }

  @Override
  public short emit() {
    short result = 0;
    // 4 highest bits: opcode
    result |= Opcode.Jump.opcode << 12;
    // next 3 bits: unused (should be 0)
    // next 3 bits: base register
    result |= (baseReg & 0x7) << 6;
    // lower 6 bits: unused (should be 0)
    return result;
  }

  public static Jmp ret() {
    // ret is a jmp to r7
    return new Jmp((byte) 7);
  }
}


class Jsr implements Instruction {
  boolean isRegisterMode; // true for JSRR, false for JSR
  byte baseReg;           // used in jsrr
  short offset11;         // used in jsr 

  private Jsr(boolean isRegisterMode, byte baseReg, short offset11) {
    this.isRegisterMode = isRegisterMode;
    this.baseReg = baseReg;
    this.offset11 = offset11;
  }

  @Override
  public Opcode getOpcode() {
    return Opcode.JumpSubroutine;
  }

  @Override
  public short emit() {
    short result = 0;
    // 4 highest bits: opcode
    result |= Opcode.JumpSubroutine.opcode << 12;
    if (isRegisterMode) {
      // bit 11 = 0 for JSRR
      // bits 10-9: 0
      // bits 8-6: base register
      result |= (baseReg & 0x7) << 6;
      // bits 5-0: 0
    } else {
      // bit 11 = 1 for JSR
      result |= 1 << 11;
      // bits 10-0: offset11
      result |= (offset11 & 0x7FF);
    }
    return result;
  }

  // Static initialization methods
  public static Jsr jsr(short offset11) {
    return new Jsr(false, (byte)0, offset11);
  }

  public static Jsr jsrr(byte baseReg) {
    return new Jsr(true, baseReg, (short)0);
  }
}
