import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SubroutineAssembler {
  private static String ASM_FILE_NAME = "mul";
  public static void main(String[] args) throws IOException, InterruptedException {
    System.err.printf("Assembling %s.asm\n", ASM_FILE_NAME);
    new ProcessBuilder("../lc3tools/assembler", String.format("%s.asm", ASM_FILE_NAME))
      .start()
      .waitFor();
    File mulFile = new File(String.format("%s.obj", ASM_FILE_NAME));
    // read binary content
    byte[] contents = Files.readAllBytes(mulFile.toPath());
    
    ArrayList<Short> instrs = new ArrayList<>();
    
    boolean startWriting = false;
    
    HashMap<String, Integer> subroutineOffsets = new HashMap<>();
    Pattern startLabel = Pattern.compile("^(\\w+):");
    
    // skip past magic (5) + version (2)
    int idx = 7;
    while(idx < contents.length) {
      short instr = (short) ((contents[idx] & 0xff) | (((short) contents[idx + 1]) << 8));
      idx += 2;
      boolean orig = contents[idx] == 0x01;
      idx++;
      int labelLength = (contents[idx] & 0xff) | ((contents[idx + 1] & 0xff) << 8) | ((contents[idx + 2] & 0xff) << 16) + ((contents[idx + 3] & 0xff) << 24);
      idx += 4;
      String label = new String(Arrays.copyOfRange(contents, idx, idx + labelLength), StandardCharsets.UTF_8);
      idx += labelLength;
      
      if (startWriting && !orig) {
        instrs.add(instr);
        // the only labels on the same line as instructions are subroutines
        Matcher m = startLabel.matcher(label);
        if (m.find()) {
          String subroutineName = m.group(1);
          subroutineOffsets.put(subroutineName, instrs.size() - 1);
        }
      }
      
      // look for our flag label
      if (label.startsWith("ezcode_lc3_start_assemble")) {
        startWriting = true;
      }
    }
    
    System.err.printf("Parsed %d instructions\n", instrs.size());
    
    // print the BuiltinUtils class
    
    System.out.println("public class BuiltinUtils {");
    System.out.print("  public static short[] code = {");
    boolean isFirst = true;
    for (short instr : instrs) {
      if (isFirst) {
        System.out.printf("(short) 0x%04X", instr);
      } else {
        // print preceding comma
        System.out.printf(", (short) 0x%04X", instr);
      }
      isFirst = false;
    }
    System.out.println("};");
    
    for (String key : subroutineOffsets.keySet()) {
      System.out.printf("  public static int %s_offset = %d;%n", key, subroutineOffsets.get(key));
    }
    System.out.println("}");
  }
}
