/**
 * A SpecialAddress is a "flag" value in a load/store
 * instruction instead of an actual offset. This is because
 * they are not known immediately during the first scan.
 * 
 * Instructions with a SpecialAddress will get an actual offset 
 * towards the end of compilation when these addresses can be resolved.
 */
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

  /** The address of a local variable. Will index into the variable table */
  public static SpecialAddress variable(String name) {
    SpecialAddress addr = SpecialAddress.LocalVariable;
    addr.variableName = name;
    return addr;
  }
}