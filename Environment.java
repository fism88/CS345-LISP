import java.util.LinkedHashMap;
import java.util.Arrays;

public class Environment {

  private static final boolean IS_REC_SUB = true;
  private static final boolean NOT_REC_SUB = false;

  private static class Entry {
    boolean e1;
    Object e2;

    public Entry(boolean rec_sub_status, Object value) {
      this.e1 = rec_sub_status;
      this.e2 = value;
    }
  }

  LinkedHashMap<String, Entry> environment;
  public Environment() {
    environment = new LinkedHashMap<String, Entry>();
  }

  public Environment(Environment env) {
    this.environment = (LinkedHashMap) env.environment.clone();
  }

  public Object get(String s) {
    Entry entry = environment.get(s);
    if (Parser.DEBUGGING) { 
      System.out.println("looked up '" + s + "': ");
      System.out.println("  entry is " + entry); 
    }
    return entry.e2;
  }

  public void put(String s, Object v) {
    environment.put(s, new Entry(this.NOT_REC_SUB, v));
  }

  public void put_rec(String s, Object v) {
    environment.put(s, new Entry(this.IS_REC_SUB, v));
  }

  public String toString() {
    String result = "";

    int depth = 0;
    for (String s: environment.keySet()) {
      depth += 1;
      Entry entry = environment.get(s);
      if (entry.e1 == this.NOT_REC_SUB) {
        result = "(aSub " + s + " " + entry.e2.toString() + " " + result; 
      }
      else if (entry.e1 == this.IS_REC_SUB) {
        EnvValue.Closure tmp = (EnvValue.Closure) entry.e2;
        result = 
          "(aRecSub " + s + " (closureV " + Arrays.toString(tmp.arg_ids) + 
          " (" + tmp.body.toString().replaceAll("\n", " ").trim() + ") " +
          "(...))" + result;
      }
    }
     
    result += "(mtSub)";

    for (int i = 0; i < depth; i++) {
      result += ")";
    }

    return result;
  }
}
