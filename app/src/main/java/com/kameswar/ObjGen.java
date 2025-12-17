package com.kameswar;

import java.util.List;
import java.nio.*;
import java.io.*;

/**
* Native Java generator for the lc3tools `.obj` file format 
*/

class ObjGen {
  // 5 byte magic + 2 byte version
  private static int HEADER_SIZE = 7;
  // 2 byte instruction + 1 byte type + 4 byte length 
  private static int INSTRUCTION_SIZE = 7;
  public static ByteBuffer getObjFile(List<Instruction> input) throws IOException {
    
    ByteBuffer buffer = convertBin((short) 0x3000, input);
    buffer.flip();
    
    buffer.array();
    return buffer;
  }
  private static ByteBuffer convertBin(short orig, List<Instruction> input) {
    int size = HEADER_SIZE + (input.size() + 1) * INSTRUCTION_SIZE;
    for (Instruction i : input) {
      size += i.toString().length();
    }
    ByteBuffer out = ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN);
    // write header
    out.put(new byte[] { 0x1C, 0x30, 0x15, (byte) 0xC0, 0x01, 0x01, 0x01 });

    out.putShort(orig);
    // 0x01 = orig header
    out.put((byte) 0x01);
    out.putInt(0);

    for (Instruction instr : input) {
      out.putShort(instr.emit());
      String asm = instr.toString();
      // 0x01 = orig header
      out.put((byte) 0x00);
      out.putInt(asm.length());
    out.put(asm.getBytes());
    }

    return out;
  }
}
