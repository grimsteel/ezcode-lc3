
enum SpecialAddress {
  // we intersperse util offsets throughout 
  NearestUtilOffset,
  CallMult,
  CallDiv,
  Alu0,
  Alu1,
  AluRet0,
  AluRet1,
  LocalVariable;

  public String variableName;

  public static SpecialAddress variable(String name) {
    SpecialAddress addr = SpecialAddress.LocalVariable;
    addr.variableName = name;
    return addr;
  }
}