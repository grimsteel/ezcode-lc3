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
  NearestDataBlock, // offset to nearest multiple of 512
  DataBlock, // actual data block address
  StackStart, // start of the stack
  CallMult,
  CallDiv,
  CallIPrint,
  CallBPrint,
  CallInput
}

enum DataType {
  Int,
  Float,
  Bool,
  String,
  Char
}

class DataBlock {
  private HashMap<String, Byte> variableIndices = new HashMap<>();
  private ArrayList<DataType> variableTypes = new ArrayList<>();
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
  
  public DataType getVariableType(String variableName) {
    if (!variableIndices.containsKey(variableName)) {
      throw new IllegalStateException(String.format("Variable %s has not been declared", variableName));
    }
    
    return variableTypes.get(variableIndices.get(variableName));
  }
  
  /**
  * Configure the memory addresses of the data block
  * Returns the mid address used for offset calcs
 */
  public short setAddress(short dataBlockStart) {
    short stringBlockStart = (short) (dataBlockStart + MAX_VARIABLES + MAX_CONSTANTS);
    for (String str : strings.keySet()) {
      byte constantIdx = strings.get(str);
      // update the constant value to the new value
      constantValues.set(constantIdx, stringBlockStart);
      // length + null term
      stringBlockStart += (short) (((String) str).length() + 1);
    }
    
    return (short) (dataBlockStart + MAX_VARIABLES);
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
      // length + null term
      totalLength += str.length() + 1;
    }
    short[] stringBlock = new short[totalLength];
    int pos = 0;
    for (String str : strings.keySet()) {
      for (int i = 0; i < str.length(); i++) {
        stringBlock[pos++] = (short) str.charAt(i);
      }
      stringBlock[pos++] = 0x0;
    }
    return stringBlock;
  }
  
  /**
  * Add a literal to the constant pool if needed
  * @param literal
  */
  public void addLiteral(Literal literal) {
    if (literal.canInline()) return;
    
    if (constantValues.size() >= MAX_CONSTANTS) {
      throw new IllegalStateException("Exceeded maximum number of constants (" + MAX_CONSTANTS + ")");
    }
    constantValues.add(literal.shortValue());
    literal.constantIndex = (byte) (constantValues.size() - 1);
    if (literal.type == LiteralType.String) {
      strings.put((String) literal.value, literal.constantIndex);
    }
  }
  
  public void addVariable(String variable, DataType type) {
    if (variableIndices.containsKey(variable)) {
      // ensure type consistency
      DataType existingType = variableTypes.get(variableIndices.get(variable));
      if (type != existingType) {
        throw new IllegalStateException(String.format("Cannot store value of type %s in variable %s of type %s.", type, variable, existingType));
      }
    }
    
    if (variableIndices.size() >= MAX_VARIABLES) {
      throw new IllegalStateException("Exceeded maximum number of variables (" + MAX_VARIABLES + ")");
    } else {
      byte idx = (byte) (variableIndices.size());
      variableIndices.put(variable, idx);
      variableTypes.add(type);
    }
  }
}
