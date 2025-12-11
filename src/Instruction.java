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

  public int opcode;
  Opcode(int opcode) {
    this.opcode = opcode;
  }
}

interface Instruction {
  
}