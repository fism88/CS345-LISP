import java.util.ArrayList;
import java.util.Arrays;

public class Interpreter implements Visitor {

  static final int TRUE = 1;
  static final int FALSE = 0;

  Environment env;

  public Interpreter() {
    env = new Environment();
  }

  public Interpreter(Environment _env) {
    this.env = _env;
  }

  /**
   * This is used to support recursion, following PLAI chapters 9 and 10.
   */
  static Environment CyclicBindAndInterpret(String id, ASTNode expr, Environment env) {
    EnvironmentValue dummy = new EnvironmentValue.Num(1729);

    Environment new_env = new Environment(env);
    new_env.put(id, dummy);

    Interpreter interp = new Interpreter(new_env);
    EnvironmentValue.Closure closure = (EnvironmentValue.Closure) interp.eval_visit(expr);

    closure.env.put_rec(id, closure);

    return closure.env;
  }

  public void visit(ASTNode node) {
    EnvironmentValue result = this.eval_visit(node);
    System.out.println("--> " + result);
  }

  public EnvironmentValue eval_visit(ASTNode node) {
    if (node instanceof ASTNode.AppNode)
      return this.visitAppNode(node);
    if (node instanceof ASTNode.AppRecNode)
      return this.visitAppRecNode(node);
    else if (node instanceof ASTNode.FunNode)
      return this.visitFunNode(node);
    else if (node instanceof ASTNode.ListNode)
      return this.visitListNode(node);
    else if (node instanceof ASTNode.SymbolNode)
      return this.visitSymbolNode(node);
    else if (node instanceof ASTNode.IdNode)
      return this.visitIdNode(node);
    else if (node instanceof ASTNode.NumNode)
      return this.visitNumNode(node);
    else if (node instanceof ASTNode.AddNode)
      return this.visitAddNode(node);
    else if (node instanceof ASTNode.SubNode)
      return this.visitSubNode(node);
    else if (node instanceof ASTNode.MultNode)
      return this.visitMultNode(node);
    else if (node instanceof ASTNode.DivNode)
      return this.visitDivNode(node);
    else if (node instanceof ASTNode.CarNode)
      return this.visitCarNode(node);
    else if (node instanceof ASTNode.CdrNode)
      return this.visitCdrNode(node);
    else if (node instanceof ASTNode.ConsNode)
      return this.visitConsNode(node);
    else if (node instanceof ASTNode.IfNode)
      return this.visitIfNode(node);
    else if (node instanceof ASTNode.EqualsNode)
      return this.visitEqualsNode(node);
    else if (node instanceof ASTNode.SCombinatorNode)
      return this.visitSCombinatorNode(node);
    else if (node instanceof ASTNode.KCombinatorNode)
      return this.visitKCombinatorNode(node);
    else if (node instanceof ASTNode.BCombinatorNode)
      return this.visitBCombinatorNode(node);
    else if (node instanceof ASTNode.CCombinatorNode)
      return this.visitCCombinatorNode(node);
    else if (node instanceof ASTNode.YCombinatorNode)
      return this.visitYCombinatorNode(node);
    else if (node instanceof ASTNode.EmptyNode)
      return this.visitEmptyNode(node);
    
    return null;
  }

  private EnvironmentValue visitAppNode(ASTNode node) {
    if (Parser.DEBUGGING) System.out.println("Saw AppNode");

    // An AppNode with two children is a function application
    if (node.children.size() == 2) {
      EnvironmentValue.Closure closure = (EnvironmentValue.Closure) this.eval_visit(node.children.get(0)); 
      EnvironmentValue.List arguments = (EnvironmentValue.List) this.eval_visit(node.children.get(1));
      return closure.eval(arguments);
    }
    // An AppNode with three children is the result of a let converted to a lambda application
    else if (node.children.size() == 3) {
      ASTNode.ListNode parameter_names = (ASTNode.ListNode) node.children.get(0);
      EnvironmentValue.List arguments = (EnvironmentValue.List) this.eval_visit(node.children.get(2));

      for (int i = 0; i < parameter_names.children.size(); i++) {
        String param_name = parameter_names.children.get(i).value;
        EnvironmentValue arg = arguments.elements[i];
        this.env.put(param_name, arg);
      }
      
      if (Parser.DEBUGGING) {
        System.out.println(" env: " + env);
      }

      return this.eval_visit(node.children.get(1));
    }
    return null;
  }

  private EnvironmentValue visitAppRecNode(ASTNode node) {
    if (Parser.DEBUGGING) System.out.println("Saw AppRecNode");

    ASTNode.ListNode parameter_names = (ASTNode.ListNode) node.children.get(0);
    ASTNode.ListNode arguments = (ASTNode.ListNode) node.children.get(2);

    for (int i = 0; i < parameter_names.children.size(); i++) {
      String fun_name = parameter_names.children.get(i).value;
      ASTNode fun_def = arguments.children.get(i);
      Environment new_env = CyclicBindAndInterpret(fun_name, fun_def, this.env);
      this.env = new Environment(new_env);
    }

    return this.eval_visit(node.children.get(1));
  }

  private EnvironmentValue visitIdNode(ASTNode node) {
    if (Parser.DEBUGGING) System.out.println("Saw IdNode");

    EnvironmentValue sub = (EnvironmentValue) this.env.get(node.value);

    if (Parser.DEBUGGING) {
      System.out.println(node.value + " gives " + sub + " from " + this.env);
    }

    return sub;
  }

  private EnvironmentValue visitNumNode(ASTNode node) {
    if (Parser.DEBUGGING) System.out.println("Saw NumNode");

    return new EnvironmentValue.Num(Double.parseDouble(node.value));
  }

  private EnvironmentValue visitFunNode(ASTNode node) {
    if (Parser.DEBUGGING) System.out.println("Saw FunNode");
    // need to create closure and return 
    ASTNode body = node.children.get(0);
    ASTNode list_node = node.children.get(1);
    ArrayList<ASTNode> arg_symbols = list_node.children; 

    ArrayList arg_ids = new ArrayList<String>();
    for (ASTNode symbol: arg_symbols) {
      arg_ids.add(symbol.value);
    }

    EnvironmentValue.Closure closure = new EnvironmentValue.Closure(body, env, arg_ids);
    
    return closure;
  }

  private EnvironmentValue visitSymbolNode(ASTNode node) {
    if (Parser.DEBUGGING) System.out.println("Saw SymbolNode (error)");
    return null;
  }

  private EnvironmentValue visitListNode(ASTNode node) {
    if (Parser.DEBUGGING) System.out.println("Saw ListNode");

    ArrayList values = new ArrayList<EnvironmentValue>();
    for (ASTNode n: node.children) {
      values.add(this.eval_visit(n));
    }

    return new EnvironmentValue.List(values);
  }

  private EnvironmentValue visitAddNode(ASTNode node) {
    if (Parser.DEBUGGING) System.out.println("Saw AddNode");

    EnvironmentValue.Num arg1 = (EnvironmentValue.Num) this.eval_visit(node.children.get(0));
    EnvironmentValue.Num arg2 = (EnvironmentValue.Num) this.eval_visit(node.children.get(1));

    return new EnvironmentValue.Num(arg1.value + arg2.value);
  }

  private EnvironmentValue visitSubNode(ASTNode node) {
    if (Parser.DEBUGGING) System.out.println("Saw SubNode");
    EnvironmentValue.Num arg1 = (EnvironmentValue.Num) this.eval_visit(node.children.get(0));
    EnvironmentValue.Num arg2 = (EnvironmentValue.Num) this.eval_visit(node.children.get(1));

    return new EnvironmentValue.Num(arg1.value - arg2.value);
  }

  private EnvironmentValue visitMultNode(ASTNode node) {
    if (Parser.DEBUGGING) System.out.println("Saw MultNode");
    EnvironmentValue.Num arg1 = (EnvironmentValue.Num) this.eval_visit(node.children.get(0));
    EnvironmentValue.Num arg2 = (EnvironmentValue.Num) this.eval_visit(node.children.get(1));

    return new EnvironmentValue.Num(arg1.value * arg2.value);
  }

  private EnvironmentValue visitDivNode(ASTNode node) {
    if (Parser.DEBUGGING) System.out.println("Saw DivNode");
    EnvironmentValue.Num arg1 = (EnvironmentValue.Num) this.eval_visit(node.children.get(0));
    EnvironmentValue.Num arg2 = (EnvironmentValue.Num) this.eval_visit(node.children.get(1));

    return new EnvironmentValue.Num(arg1.value / arg2.value);
  }

  private EnvironmentValue visitCarNode(ASTNode node) {
    if (Parser.DEBUGGING) System.out.println("Saw CarNode");

    ASTNode list = node.children.get(0);
    return this.eval_visit(list.children.get(0));
  }

  private EnvironmentValue visitCdrNode(ASTNode node) {
    if (Parser.DEBUGGING) System.out.println("Saw CdrNode");

    ASTNode list = node.children.get(0);
    ArrayList<ASTNode> result = new ArrayList<ASTNode>();
    for (int i = 1; i < list.children.size(); i++) {
      result.add(list.children.get(i));
    }

    return this.eval_visit(new ASTNode.ListNode(result));
  }
  
  private EnvironmentValue visitConsNode(ASTNode node) {
    if (Parser.DEBUGGING) System.out.println("Saw ConsNode");

    ArrayList<ASTNode> result = new ArrayList<ASTNode>();
    result.add(node.children.get(0));
    for (ASTNode n: node.children.get(1).children) {
      result.add(n);
    }

    return this.eval_visit(new ASTNode.ListNode(result));
  }

  private EnvironmentValue visitIfNode(ASTNode node) {
    if (Parser.DEBUGGING) System.out.println("Saw IfNode");

    EnvironmentValue.Num arg1 = (EnvironmentValue.Num) this.eval_visit(node.children.get(0));

    if (arg1.value == TRUE) {
      EnvironmentValue.Num arg2 = (EnvironmentValue.Num) this.eval_visit(node.children.get(1));
      return arg2;
    }
    else {
      EnvironmentValue.Num arg3 = (EnvironmentValue.Num) this.eval_visit(node.children.get(2));
      return arg3;
    }
  }
  
  private EnvironmentValue visitEqualsNode(ASTNode node) {
    if (Parser.DEBUGGING) System.out.println("Saw EqualsNode");

    EnvironmentValue.Num arg1 = (EnvironmentValue.Num) this.eval_visit(node.children.get(0));
    EnvironmentValue.Num arg2 = (EnvironmentValue.Num) this.eval_visit(node.children.get(1));

    if (arg1.value == arg2.value)
      return new EnvironmentValue.Num(TRUE);
    else
      return new EnvironmentValue.Num(FALSE);
  }

  private EnvironmentValue visitSCombinatorNode(ASTNode node) {
    if (Parser.DEBUGGING) System.out.println("Saw SCombinatorNode");

    // (s f g x) = (f x (g x))
    EnvironmentValue x = this.eval_visit(node.children.get(2));
    EnvironmentValue.Closure f = (EnvironmentValue.Closure) this.eval_visit(node.children.get(0));
    EnvironmentValue.Closure g = (EnvironmentValue.Closure) this.eval_visit(node.children.get(1));

    ArrayList<EnvironmentValue> gArgs = new ArrayList<EnvironmentValue>();
    ArrayList<EnvironmentValue> fArgs = new ArrayList<EnvironmentValue>();

    gArgs.add(x);
    
    fArgs.add(x);
    fArgs.add(g.eval(gArgs));

    return f.eval(fArgs);
  }

  private EnvironmentValue visitKCombinatorNode(ASTNode node) {
    if (Parser.DEBUGGING) System.out.println("Saw KCombinatorNode");

    return this.eval_visit(node.children.get(0));
  }

  private EnvironmentValue visitBCombinatorNode(ASTNode node) {
    if (Parser.DEBUGGING) System.out.println("Saw BCombinatorNode");

    EnvironmentValue x = this.eval_visit(node.children.get(2));
    EnvironmentValue.Closure f = (EnvironmentValue.Closure) this.eval_visit(node.children.get(0));
    EnvironmentValue.Closure g = (EnvironmentValue.Closure) this.eval_visit(node.children.get(1));

    ArrayList<EnvironmentValue> gArgs = new ArrayList<EnvironmentValue>();
    ArrayList<EnvironmentValue> fArgs = new ArrayList<EnvironmentValue>();

    gArgs.add(x);

    fArgs.add(g.eval(gArgs));

    return f.eval(fArgs);
  }

  private EnvironmentValue visitCCombinatorNode(ASTNode node) {
    if (Parser.DEBUGGING) System.out.println("Saw CCombinatorNode");
    
    EnvironmentValue x = this.eval_visit(node.children.get(2));
    EnvironmentValue.Closure f = (EnvironmentValue.Closure) this.eval_visit(node.children.get(0));
    EnvironmentValue.Closure g = (EnvironmentValue.Closure) this.eval_visit(node.children.get(1));

    ArrayList<EnvironmentValue> fArgs = new ArrayList<EnvironmentValue>();
    fArgs.add(x);
    fArgs.add(g);

    return f.eval(fArgs);
  }

  private EnvironmentValue visitYCombinatorNode(ASTNode node) {
    if (Parser.DEBUGGING) System.out.println("Saw YCombinatorNode");

    // using (Y f) = (f (lambda (x) ((Y f) x)))
    EnvironmentValue.Closure f = (EnvironmentValue.Closure) this.eval_visit(node.children.get(0));
    ArrayList<EnvironmentValue> fArgs = new ArrayList<EnvironmentValue>();

//    Parser parser = new Parser();
//    parser.parse

    // make lambda body
    ArrayList<ASTNode> arg_symbols = new ArrayList<ASTNode>();
    arg_symbols.add(new ASTNode.IdNode("__x__"));
    ASTNode body = new ASTNode.AppNode(node, arg_symbols);

    // make lambda node
    ArrayList<ASTNode> fun_params = new ArrayList<ASTNode>();
    fun_params.add(new ASTNode.SymbolNode("__x__"));
    ASTNode fun_node = new ASTNode.FunNode(body, fun_params);

    // interpret lambda to get a closure
    EnvironmentValue.Closure result = (EnvironmentValue.Closure) this.eval_visit(fun_node);    
   
    // evaluate the closure
    ArrayList<EnvironmentValue> arguments = new ArrayList<EnvironmentValue>();
    arguments.add(result);
    return f.eval(arguments);
  }

  private EnvironmentValue visitEmptyNode(ASTNode node) {
    if (Parser.DEBUGGING) System.out.println("Saw EmptyNode");
   
    EnvironmentValue.List list = (EnvironmentValue.List) this.eval_visit(node.children.get(0));

    if (list.elements.length == 0)
      return new EnvironmentValue.Num(TRUE);
    else
      return new EnvironmentValue.Num(FALSE);
  }
}
