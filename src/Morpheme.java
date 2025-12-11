import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

enum MorphemeType {
  Repeating, // String id
  Union, // ArrayList
  Optional, // String id
  Sequence, // ArrayList
  Terminal // String id
}

public class Morpheme {
  public Object value;
  public MorphemeType type;
  public String name;

  public Morpheme(String name, Object value, MorphemeType type) {
    this.value = value;
    this.type = type;
    this.name = name;
  }

  public static Morpheme union(String name, String[] target) {
    return new Morpheme(name, Arrays.asList(target), MorphemeType.Union);
  }
  public static Morpheme repeating(String name, String target) {
    return new Morpheme(name, target, MorphemeType.Repeating);
  }
  public static Morpheme optional(String name, String target) {
    return new Morpheme(name, target, MorphemeType.Optional);
    
  }
  public static Morpheme terminal(String name, String target) {
    return new Morpheme(name, target, MorphemeType.Terminal);
  }
  public static Morpheme sequence(String name, String[] sequence) {
    return new Morpheme(name, Arrays.asList(sequence), MorphemeType.Sequence);
  }
  
  public ParsedMorpheme toParsed(Object parsedValue) {
    return new ParsedMorpheme(name, parsedValue, type);
  }
}

class ParsedMorpheme {
  public Object value;
  public MorphemeType type;
  public String name;
  public ParsedMorpheme(String name, Object value, MorphemeType type) {
    this.value = value;
    this.type = type;
    this.name = name;
  }
}
