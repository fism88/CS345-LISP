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
    System.out.println("--> " + result);
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
    else if (node instanceof ASTNode.SubNode) {
      return this.visitSubNode(node);
    }
    else if (node instanceof ASTNode.MultNode) {
      return this.visitMultNode(node);
    }
    else if (node instanceof ASTNode.DivNode) {
      return this.visitDivNode(node);
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
    else if (node instanceof ASTNode.SCombinatorNode) {
      return this.visitSCombinatorNode(node);
    }
    else if (node instanceof ASTNode.KCombinatorNode) {
      return this.visitKCombinatorNode(node);
    }
    else if (node instanceof ASTNode.BCombinatorNode) {
      return this.visitBCombinatorNode(node);
    }
    else if (node instanceof ASTNode.CCombinatorNode) {
      return this.visitCCombinatorNode(node);
    }
    else if (node instanceof ASTNode.YCombinatorNode) {
      return this.visitYCombinatorNode(node);
    }
    else if (node instanceof ASTNode.EmptyNode) {
      return this.visitEmptyNode(node);
    }
    else if (node instanceof ASTNode.PrlenNode) {
      return this.visitPrlenNode(node);
    }
    else if (node instanceof ASTNode.PrsumNode) {
      return this.visitPrsumNode(node);
    }
    else if (node instanceof ASTNode.PrprodNode) {
      return this.visitPrprodNode(node);
    }
    else if (node instanceof ASTNode.PrmapNode) {
      return this.visitPrmapNode(node);
    }
    

    return null;
  }

  private EnvValue visitAppNode(ASTNode node) {
    if (Parser.DEBUGGING) {
      System.out.println("Saw AppNode");
    }

    // function application: (f 1 2 ...)
    if (node.children.size() == 2) {
      EnvValue.Closure closure = (EnvValue.Closure) this.eval_visit(node.children.get(0)); 

      EnvValue.List args = (EnvValue.List) this.eval_visit(node.children.get(1));

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
      
      if (Parser.DEBUGGING) {
        System.out.println(" env: " + env);
      }

      return this.eval_visit(node.children.get(1));
    }
    return null;
  }

  private EnvValue visitAppRecNode(ASTNode node) {
    if (Parser.DEBUGGING) System.out.println("Saw AppRecNode");

    ASTNode.ListNode param_names = (ASTNode.ListNode) node.children.get(0);
    ASTNode.ListNode args = (ASTNode.ListNode) node.children.get(2);

    // assume only one function in let-rec
    for (int i = 0; i < param_names.children.size(); i++) {
      String fun_name = param_names.children.get(i).value;
      ASTNode fun_def = args.children.get(i);
      Environment new_env = CyclicBindAndInterpret(fun_name, fun_def, this.env);
      this.env = new Environment(new_env);
    }

    return this.eval_visit(node.children.get(1));
  }

  private EnvValue visitIdNode(ASTNode node) {
    if (Parser.DEBUGGING) System.out.println("Saw IdNode");

    EnvValue sub = (EnvValue) this.env.get(node.value);

    if (Parser.DEBUGGING) {
      System.out.println(node.value + " gives " + sub + " from " + this.env);
    }

    return sub;
  }

  private EnvValue visitNumNode(ASTNode node) {
    if (Parser.DEBUGGING) System.out.println("Saw NumNode");

    return new EnvValue.Num(Double.parseDouble(node.value));
  }

  private EnvValue visitFunNode(ASTNode node) {
    if (Parser.DEBUGGING) System.out.println("Saw FunNode");
    // need to create closure and return 
    ASTNode body = node.children.get(0);
    ASTNode list_node = node.children.get(1);
    ArrayList<ASTNode> arg_symbols = list_node.children; 

    ArrayList arg_ids = new ArrayList<String>();
    for (ASTNode symbol: arg_symbols) {
      arg_ids.add(symbol.value);
    }

    EnvValue.Closure closure = new EnvValue.Closure(body, env, arg_ids);
    
    return closure;
  }

  private EnvValue visitSymbolNode(ASTNode node) {
    if (Parser.DEBUGGING) System.out.println("Saw SymbolNode (error)");
    return null;
  }

  private EnvValue visitListNode(ASTNode node) {
    if (Parser.DEBUGGING) System.out.println("Saw ListNode");

    ArrayList values = new ArrayList<EnvValue>();
    for (ASTNode n: node.children) {
      values.add(this.eval_visit(n));
    }

    return new EnvValue.List(values);
  }

  private EnvValue visitAddNode(ASTNode node) {
    if (Parser.DEBUGGING) System.out.println("Saw AddNode");

    EnvValue.Num arg1 = (EnvValue.Num) this.eval_visit(node.children.get(0));
    EnvValue.Num arg2 = (EnvValue.Num) this.eval_visit(node.children.get(1));

    // add args and return result
    return new EnvValue.Num(arg1.value + arg2.value);
  }

  private EnvValue visitSubNode(ASTNode node) {
    if (Parser.DEBUGGING) System.out.println("Saw SubNode");
    EnvValue.Num arg1 = (EnvValue.Num) this.eval_visit(node.children.get(0));
    EnvValue.Num arg2 = (EnvValue.Num) this.eval_visit(node.children.get(1));

    return new EnvValue.Num(arg1.value - arg2.value);
  }

  private EnvValue visitMultNode(ASTNode node) {
    if (Parser.DEBUGGING) System.out.println("Saw MultNode");
    EnvValue.Num arg1 = (EnvValue.Num) this.eval_visit(node.children.get(0));
    EnvValue.Num arg2 = (EnvValue.Num) this.eval_visit(node.children.get(1));

    return new EnvValue.Num(arg1.value * arg2.value);
  }

  private EnvValue visitDivNode(ASTNode node) {
    if (Parser.DEBUGGING) System.out.println("Saw DivNode");
    EnvValue.Num arg1 = (EnvValue.Num) this.eval_visit(node.children.get(0));
    EnvValue.Num arg2 = (EnvValue.Num) this.eval_visit(node.children.get(1));

    return new EnvValue.Num(arg1.value / arg2.value);
  }

  private EnvValue visitCarNode(ASTNode node) {
    if (Parser.DEBUGGING) System.out.println("Saw CarNode");
    return this.eval_visit(node.execute());
  }

  private EnvValue visitCdrNode(ASTNode node) {
    if (Parser.DEBUGGING) System.out.println("Saw CdrNode");
    return this.eval_visit(node.execute());
  }
  
  private EnvValue visitConsNode(ASTNode node) {
    if (Parser.DEBUGGING) System.out.println("Saw ConsNode");
    return this.eval_visit(node.execute());
  }

  private EnvValue visitIfNode(ASTNode node) {
    if (Parser.DEBUGGING) System.out.println("Saw IfNode");

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
    if (Parser.DEBUGGING) System.out.println("Saw EqualsNode");

    EnvValue.Num arg1 = (EnvValue.Num) this.eval_visit(node.children.get(0));
    EnvValue.Num arg2 = (EnvValue.Num) this.eval_visit(node.children.get(1));

    if (arg1.value == arg2.value)
      return new EnvValue.Num(TRUE);
    else
      return new EnvValue.Num(FALSE);
  }

  private EnvValue visitSCombinatorNode(ASTNode node) {
    if (Parser.DEBUGGING) System.out.println("Saw SCombinatorNode");

    // (s f g x) = (f x (g x))
    EnvValue x = this.eval_visit(node.children.get(2));
    EnvValue.Closure f = (EnvValue.Closure) this.eval_visit(node.children.get(0));
    EnvValue.Closure g = (EnvValue.Closure) this.eval_visit(node.children.get(1));

    ArrayList<EnvValue> gArgs = new ArrayList<EnvValue>();
    ArrayList<EnvValue> fArgs = new ArrayList<EnvValue>();

    gArgs.add(x);
    
    fArgs.add(x);
    fArgs.add(g.eval(gArgs));

    return f.eval(fArgs);
  }

  private EnvValue visitKCombinatorNode(ASTNode node) {
    if (Parser.DEBUGGING) System.out.println("Saw KCombinatorNode");

    return this.eval_visit(node.children.get(0));
  }

  private EnvValue visitBCombinatorNode(ASTNode node) {
    if (Parser.DEBUGGING) System.out.println("Saw BCombinatorNode");

    EnvValue x = this.eval_visit(node.children.get(2));
    EnvValue.Closure f = (EnvValue.Closure) this.eval_visit(node.children.get(0));
    EnvValue.Closure g = (EnvValue.Closure) this.eval_visit(node.children.get(1));

    ArrayList<EnvValue> gArgs = new ArrayList<EnvValue>();
    ArrayList<EnvValue> fArgs = new ArrayList<EnvValue>();

    gArgs.add(x);

    fArgs.add(g.eval(gArgs));

    return f.eval(fArgs);
  }

  private EnvValue visitCCombinatorNode(ASTNode node) {
    if (Parser.DEBUGGING) System.out.println("Saw CCombinatorNode");
    
    EnvValue x = this.eval_visit(node.children.get(2));
    EnvValue.Closure f = (EnvValue.Closure) this.eval_visit(node.children.get(0));
    EnvValue.Closure g = (EnvValue.Closure) this.eval_visit(node.children.get(1));

    ArrayList<EnvValue> fArgs = new ArrayList<EnvValue>();
    fArgs.add(x);
    fArgs.add(g);

    return f.eval(fArgs);
  }

  private EnvValue visitYCombinatorNode(ASTNode node) {
    if (Parser.DEBUGGING) System.out.println("Saw YCombinatorNode");

    // using (Y f) = (f (lambda (x) ((Y f) x)))
    EnvValue.Closure f = (EnvValue.Closure) this.eval_visit(node.children.get(0));

    ArrayList<EnvValue> fArgs = new ArrayList<EnvValue>();
   
    // make lambda body
    ArrayList<ASTNode> arg_symbols = new ArrayList<ASTNode>();
    arg_symbols.add(new ASTNode.IdNode("__x__"));
    ASTNode body = new ASTNode.AppNode(node, arg_symbols);

    // make lambda node
    ArrayList<ASTNode> fun_params = new ArrayList<ASTNode>();
    fun_params.add(new ASTNode.SymbolNode("__x__"));
    ASTNode fun_node = new ASTNode.FunNode(body, fun_params);

    // interpret lambda to get a closure
    EnvValue.Closure result = (EnvValue.Closure) this.eval_visit(fun_node);    
   
    // evaluate the closure
    ArrayList<EnvValue> args = new ArrayList<EnvValue>();
    args.add(result);
    return f.eval(args);
  }

  private EnvValue visitEmptyNode(ASTNode node) {
    if (Parser.DEBUGGING) System.out.println("Saw EmptyNode");
   
    EnvValue.List list = (EnvValue.List) this.eval_visit(node.children.get(0));

    if (list.elements.length == 0)
      return new EnvValue.Num(TRUE);
    else
      return new EnvValue.Num(FALSE);
  }
  private EnvValue visitPrlenNode(ASTNode node) {
    if (Parser.DEBUGGING) System.out.println("Saw PrlenNode");

    return null; 
  }
  private EnvValue visitPrsumNode(ASTNode node) {
    if (Parser.DEBUGGING) System.out.println("Saw PrsumNode");
    return null;
  }
  private EnvValue visitPrprodNode(ASTNode node) {
    if (Parser.DEBUGGING) System.out.println("Saw PrprodNode");
    return null;
  }
  private EnvValue visitPrmapNode(ASTNode node) {
    if (Parser.DEBUGGING) System.out.println("Saw PrmapNode");
    return null;
  }
}
