import java.util.ArrayList;
import java.util.Arrays;

public class Interpreter extends Visitor {

  static final int TRUE = 1;
  static final int FALSE = 0;

  Environment env;

  public Interpreter() {
    env = new Environment();
  }

  public Interpreter(Environment _env) {
    this.env = _env;
  }

  static Environment CyclicBindAndInterpret(String id, ASTNode expr, Environment env) {
    EnvValue dummy = new EnvValue.Num(1729);

    Environment new_env = new Environment(env);
    new_env.put(id, dummy);

    Interpreter interp = new Interpreter(new_env);
    EnvValue.Closure closure = (EnvValue.Closure) interp.eval_visit(expr);

    closure.env.put_rec(id, closure);

    return closure.env;
  }

  public void visit(ASTNode node) {
    EnvValue result = this.eval_visit(node);
    System.out.println(this.env);
    System.out.println(result);
  }

  public EnvValue eval_visit(ASTNode node) {
    if (node instanceof ASTNode.AppNode) {
      return this.visitAppNode(node);
    }
    if (node instanceof ASTNode.AppRecNode) {
      return this.visitAppRecNode(node);
    }
    else if (node instanceof ASTNode.FunNode) {
      return this.visitFunNode(node);
    }
    else if (node instanceof ASTNode.ListNode) { 
      return this.visitListNode(node);
    }
    else if (node instanceof ASTNode.SymbolNode) {
      return this.visitSymbolNode(node);
    }
    else if (node instanceof ASTNode.IdNode) {
      return this.visitIdNode(node);
    }
    else if (node instanceof ASTNode.NumNode) {
      return this.visitNumNode(node);
    }
    else if (node instanceof ASTNode.AddNode) {
      return this.visitAddNode(node);
    } 
    else if (node instanceof ASTNode.CarNode) {
      return this.visitCarNode(node);
    } 
    else if (node instanceof ASTNode.CdrNode) {
      return this.visitCdrNode(node);
    } 
    else if (node instanceof ASTNode.ConsNode) {
      return this.visitConsNode(node);
    }
    else if (node instanceof ASTNode.IfNode) {
      return this.visitIfNode(node);
    }
    else if (node instanceof ASTNode.EqualsNode) {
      return this.visitEqualsNode(node);
    }
 
    return null;
  }

  private EnvValue visitAppNode(ASTNode node) {
    System.out.println("Saw AppNode");

    // function application: (f 1 2 ...)
    if (node.children.size() == 2) {
      String fun_id = node.children.get(0).value;
      System.out.println(fun_id);
      EnvValue.Closure closure = (EnvValue.Closure) env.get(fun_id);

      EnvValue.List args = (EnvValue.List) this.eval_visit(node.children.get(1));

      System.out.println("closure = " + closure);
      return closure.eval(args);
    }
    // represents let->lambda: app (a1 a2 ...) C (b1 b2 ...)
    else if (node.children.size() == 3) {
      // the parameter names        
      ASTNode.ListNode param_names = (ASTNode.ListNode) node.children.get(0);

      // evalute the arguments
      EnvValue.List args = (EnvValue.List) this.eval_visit(node.children.get(2));

      // error if param_names.length != args.length
      // error if any param_name is not an IdNode (or SymbolNode?)
      for (int i = 0; i < param_names.children.size(); i++) {
        String param_name = param_names.children.get(i).value;
        EnvValue arg = args.elements[i];
        this.env.put(param_name, arg);
      }
      
//      System.out.println(" len 3");
//      System.out.println(" parameters: " + param_names.children);
//      System.out.println(" arguments: " + Arrays.toString(args.elements));
      System.out.println(" env: " + env);

      return this.eval_visit(node.children.get(1));
    }
    return null;
  }

  private EnvValue visitAppRecNode(ASTNode node) {
    System.out.println("Saw AppRecNode");
    ASTNode.ListNode param_names = (ASTNode.ListNode) node.children.get(0);
    ASTNode.ListNode args = (ASTNode.ListNode) node.children.get(2);

    // assume only one function in let-rec
    String fun_name = param_names.children.get(0).value;
    ASTNode fun_def = args.children.get(0);
    
    Environment new_env = CyclicBindAndInterpret(fun_name, fun_def, this.env);

    this.env = new Environment(new_env);
    return this.eval_visit(node.children.get(1));
  }

  private EnvValue visitIdNode(ASTNode node) {
    System.out.println("Saw IdNode");

    EnvValue sub = (EnvValue) this.env.get(node.value);
    System.out.println(node.value + " gives " + sub + " from " + this.env);

    return sub;
  }

  private EnvValue visitNumNode(ASTNode node) {
    System.out.println("Saw NumNode");

    return new EnvValue.Num(Double.parseDouble(node.value));
  }

  private EnvValue visitFunNode(ASTNode node) {
    // need to create closure and return 
    ASTNode body = node.children.get(0);
    ASTNode list_node = node.children.get(1);
    ArrayList<ASTNode> arg_symbols = list_node.children; 

    ArrayList arg_ids = new ArrayList<String>();
    for (ASTNode symbol: arg_symbols) {
      arg_ids.add(symbol.value);
    }

//    Environment _env = new Environment(this.env); 

//    System.out.println("Saw FunNode");
//    System.out.println("  parameter " + arg_id);
//    System.out.println("  body\n" + body);
//    System.out.println("  this.env: " + this.env);
//    System.out.println("  copied env: " + _env);

    EnvValue.Closure closure = new EnvValue.Closure(body, env, arg_ids);
    
    return closure;
  }
  private EnvValue visitSymbolNode(ASTNode node) {
    System.out.println("Saw SymbolNode (error)");
    return null;
  }
  private EnvValue visitListNode(ASTNode node) {
    System.out.println("Saw ListNode");

    ArrayList values = new ArrayList<EnvValue>();
    for (ASTNode n: node.children) {
      values.add(this.eval_visit(n));
    }

    return new EnvValue.List(values);
  }

  private EnvValue visitAddNode(ASTNode node) {
    System.out.println("Saw AddNode");
    EnvValue.Num arg1 = (EnvValue.Num) this.eval_visit(node.children.get(0));
    EnvValue.Num arg2 = (EnvValue.Num) this.eval_visit(node.children.get(1));

    // add args and return result
    return new EnvValue.Num(arg1.value + arg2.value);
  }

  private EnvValue visitCarNode(ASTNode node) {
    System.out.println("Saw CarNode");
    return this.eval_visit(node.execute());
  }

  private EnvValue visitCdrNode(ASTNode node) {
    System.out.println("Saw CdrNode");
    return this.eval_visit(node.execute());
  }
  
  private EnvValue visitConsNode(ASTNode node) {
    System.out.println("Saw ConsNode");
    return this.eval_visit(node.execute());
  }

  private EnvValue visitIfNode(ASTNode node) {
    System.out.println("Saw IfNode");
    EnvValue.Num arg1 = (EnvValue.Num) this.eval_visit(node.children.get(0));

    if (arg1.value == TRUE) {
      EnvValue.Num arg2 = (EnvValue.Num) this.eval_visit(node.children.get(1));
      return arg2;
    }
    else {
      EnvValue.Num arg3 = (EnvValue.Num) this.eval_visit(node.children.get(2));
      return arg3;
    }
  }
  
  private EnvValue visitEqualsNode(ASTNode node) {
    System.out.println("Saw EqualsNode");
    EnvValue.Num arg1 = (EnvValue.Num) this.eval_visit(node.children.get(0));
    EnvValue.Num arg2 = (EnvValue.Num) this.eval_visit(node.children.get(1));

    if (arg1.value == arg2.value)
      return new EnvValue.Num(TRUE);
    else
      return new EnvValue.Num(FALSE);
  }
}
