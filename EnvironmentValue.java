import java.util.Arrays;
import java.util.ArrayList;

public class EnvironmentValue {

  static final String NUMV = "numV";
  static final String CLOSUREV = "closureV";
  static final String LIST = "list";
  static final String UNKNOWN = "?";

  String type_name;

  public EnvironmentValue() {
    type_name = "?"; 
  }

  public EnvironmentValue(String name) {
    this.type_name = name;
  }
  
  public String toString() {
    return this.type_name;
  }

  public static class Num extends EnvironmentValue {
    double value;

    public Num(double value) {
      super(EnvironmentValue.NUMV);
      this.value = value;
    }

    public String toString() {
      return "(" + this.type_name + " " + this.value + ")";
    }
  }

  public static class List extends EnvironmentValue {
    EnvironmentValue[] elements;

    public List(EnvironmentValue[] elements) {
      this.elements = elements;
    }

    public List(ArrayList<EnvironmentValue> elements) {
      this.elements = elements.toArray(new EnvironmentValue[0]);
    }

    public String toString() {
      String result = "(list ";
      for (EnvironmentValue n: this.elements) {
        result += n.toString() + " ";
      }

      result = result.trim() + ")";

      return result;
    }
  }

  public static class Closure extends EnvironmentValue {
    String[] arg_ids;
    ASTNode body;
    Environment env;

    public Closure(ASTNode body, Environment env, ArrayList<String> arg_ids) {
      super(EnvironmentValue.CLOSUREV);
      this.body = body;
      if (Parser.SCOPING.equals(Parser.STATIC_SCOPE))
        this.env = new Environment(env);
      else
        this.env = env;
      this.arg_ids = arg_ids.toArray(new String[0]);
    }
    
    public EnvironmentValue eval(EnvironmentValue.List args) {
      if (Parser.DEBUGGING) {
        System.out.println("in closure.eval(1)");
        System.out.println("  " + this.toString());
        System.out.println("  closure.env= " + this.env);
      }

      Environment new_env = new Environment(this.env);

      for (int i = 0; i < args.elements.length; i++) {
        new_env.put(this.arg_ids[i], args.elements[i]);
      }
      
      Interpreter interp = new Interpreter(new_env);
      EnvironmentValue result = interp.eval_visit(this.body);
      
      return result;
    }

    public EnvironmentValue eval(ArrayList<EnvironmentValue> args) {
      EnvironmentValue.List tmp = new EnvironmentValue.List(args);
      return this.eval(tmp);
    }

    public String toString() {
      PrintVisitor pv = new PrintVisitor();

      String result = "(" + this.type_name + " " + Arrays.toString(this.arg_ids) + " ";

      result += pv.stringVisit(this.body);
      result += this.env.toString() + ")";
      return result;
    }

    public String toStringNoEnvironment() {
      PrintVisitor pv = new PrintVisitor();

      String result = "(" + this.type_name + " " + Arrays.toString(this.arg_ids) + " ";

      result += pv.stringVisit(this.body);
      result += "(...)) ";
      return result;
    }
  }
}
