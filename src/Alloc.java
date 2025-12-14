import java.util.ArrayList;
import java.util.HashMap;

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
  NearestDataBlock,
  CallMult,
  CallDiv;
}

class DataBlock {
  private HashMap<String, Byte> variableIndices = new HashMap<>();
  public ArrayList<Short> constantValues = new ArrayList<>();
  // map of string to constant index
  private HashMap<String, Byte> strings = new HashMap<>();
  
  private static final byte MAX_VARIABLES = 32;
  private static final byte MAX_CONSTANTS = 31;
  
  public byte getConstantOffset(byte constantIndex) {
    // we store constants from [0, 31]
    return constantIndex;
  }
  
  public byte getVariableOffset(String variableName) {
    return (byte) (-1 - variableIndices.get(variableName));
  }
  
  /**
  * Configure the memory addresses of the strings
  * returns the new start of the string block
 */
  public short setStringAddress(short stringBlockStart) {
    for (String str : strings.keySet()) {
      byte constantIdx = strings.get(str);
      // update the constant value to the new value
      constantValues.set(constantIdx, stringBlockStart);
      stringBlockStart += (short) (((String) str).length());
    }
    return stringBlockStart;
  }
  
  /**
  * Emit the data block as a short array with all constant values
  */
  public short[] emitDataBlock() {
    short[] data = new short[MAX_VARIABLES + MAX_CONSTANTS];
    // Init all variables to 0
    for (int i = 0; i < MAX_VARIABLES; i++) {
      data[i] = 0;
    }
    // Fill the next MAX_CONSTANTS shorts with constant values (or 0 if not enough constants)
    for (int i = 0; i < MAX_CONSTANTS; i++) {
      if (i < constantValues.size()) {
        data[MAX_VARIABLES + i] = constantValues.get(i);
      } else {
        data[MAX_VARIABLES + i] = 0;
      }
    }
    return data;
  }

  /**
  * Emit full string block array
  */
  public short[] emitStringBlock() {
    // Calculate total length
    int totalLength = 0;
    for (String str : strings.keySet()) {
      totalLength += str.length();
    }
    short[] stringBlock = new short[totalLength];
    int pos = 0;
    for (String str : strings.keySet()) {
      for (int i = 0; i < str.length(); i++) {
        stringBlock[pos++] = (short) str.charAt(i);
      }
    }
    return stringBlock;
  }
  
  /**
  * Add a literal to the constant pool if needed
  * @param literal
  */
  public void addLiteral(Literal literal) {
    if (constantValues.size() >= MAX_CONSTANTS) {
      throw new IllegalStateException("Exceeded maximum number of constants (" + MAX_CONSTANTS + ")");
    }
    constantValues.add(literal.shortValue());
    literal.constantIndex = (byte) (constantValues.size() - 1);
    if (literal.type == LiteralType.String) {
      strings.put((String) literal.value, literal.constantIndex);
    }
  }
  
  public void addVariable(String variable) {if (variableIndices.containsKey(variable)) return;
    if (variableIndices.size() >= MAX_VARIABLES) {
      throw new IllegalStateException("Exceeded maximum number of variables (" + MAX_VARIABLES + ")");
    } else {
      byte idx = (byte) (variableIndices.size() - 1);
      variableIndices.put(variable, idx);
    }
  }
}
