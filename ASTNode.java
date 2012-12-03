import java.util.ArrayList;

public class ASTNode {

  static final String NUM = "num";
  static final String ID  = "id";
  static final String APP = "app";
  static final String APPREC = "apprec";
  static final String FUN = "fun";
  static final String SYMBOL = "'";
  static final String LIST = "list";
  static final String ADD = "add";
  static final String SUB = "sub";
  static final String MULT = "mult";
  static final String DIV = "div";
  static final String CAR = "car";
  static final String CDR = "cdr";
  static final String CONS = "cons";
  static final String IF = "if";
  static final String EQUALS = "equals";
  static final String S_COMBINATOR = "s-comb";
  static final String K_COMBINATOR = "k-comb";
  static final String B_COMBINATOR = "b-comb";
  static final String C_COMBINATOR = "c-comb";
  static final String Y_COMBINATOR = "y-comb";
  static final String PRLEN = "prlen"; 
  static final String PRSUM = "prsum";
  static final String PRPROD = "prprod";
  static final String PRMAP = "prmap";
  static final String EMPTY = "empty";

  String type_str;
  String value;
  ArrayList<ASTNode> children;

  public ASTNode(String type_str, String value, ASTNode... children) {
    this.type_str = type_str;
    this.value = value;
    this.children = new ArrayList<ASTNode>();
    for (ASTNode child: children) {
      this.AddChild(child);
    }
  }

  public void AddChild(ASTNode child) {
    this.children.add(child);
  }

  public void accept(Visitor visitor) {
    visitor.visit(this);
  }

  public ASTNode execute() {
    return null;
  }

  public String toString() {
    return this.getString(0); 
  }

  public String getString(int depth) {
    String result = "";
    
    for (int i = 0; i < depth; i++) {
      result += " ";
    }

    result += this.type_str;
    if (this.value != null) {
      result += " " + this.value;
    }
    result += "\n";

    for (ASTNode child: this.children) {
      result += child.getString(depth + 1);
    }

    return result;
  }

  /**
   * We define a class for each different ASTNode
   */

  /** 
   * The symbol node is distinguished from the IdNode in that we 
   * never use a SymbolNode to lookup a value in the environment
   */
  public static class SymbolNode extends ASTNode {
    public SymbolNode(String symbol) {
      super(ASTNode.SYMBOL, symbol);
    }
  }
  
  public static class IdNode extends ASTNode {
    public IdNode(String id) {
      super(ASTNode.ID, id);
    }
  }

  public static class NumNode extends ASTNode {
    public NumNode(String num) {
      super(ASTNode.NUM, num);
    }
  }
  
  public static class FunNode extends ASTNode {
    public FunNode(ASTNode body, ArrayList<ASTNode> arg_symbols) {
      super(ASTNode.FUN, null);
      this.AddChild(body);
      this.AddChild(new ASTNode.ListNode(arg_symbols));
    }
  }

  public static class AppNode extends ASTNode {
    public AppNode(ASTNode arg_symbol, ArrayList<ASTNode> values) {
      super(ASTNode.APP, null);
      this.AddChild(arg_symbol);
      this.AddChild(new ASTNode.ListNode(values));
    }

    public AppNode(ArrayList<ASTNode> arg_symbols, ASTNode body, ArrayList<ASTNode> values) {
      super(ASTNode.APP, null);
      this.AddChild(new ASTNode.ListNode(arg_symbols));
      this.AddChild(body);
      this.AddChild(new ASTNode.ListNode(values));
    }
  }
 
  public static class AppRecNode extends ASTNode {
    public AppRecNode(ArrayList<ASTNode> arg_symbols, ASTNode body, ArrayList<ASTNode> values) {
      super(ASTNode.APPREC, null);
      this.AddChild(new ASTNode.ListNode(arg_symbols));
      this.AddChild(body);
      this.AddChild(new ASTNode.ListNode(values));
    }
  } 
  
  public static class ListNode extends ASTNode {
    public ListNode(ArrayList<ASTNode> elements) {
      super(ASTNode.LIST, null);
      for (ASTNode n: elements) {
        this.AddChild(n);
      }
    }
  }
  
  public static class CarNode extends ASTNode {
    public CarNode(ASTNode list) {
      super(ASTNode.CAR, null);
      this.children.add(list);
    }
  }

  public static class CdrNode extends ASTNode {
    public CdrNode(ASTNode list) {
      super(ASTNode.CDR, null);
      this.children.add(list);
    }
  }

  public static class ConsNode extends ASTNode {
    public ConsNode(ASTNode arg, ASTNode list) {
      super(ASTNode.CONS, null);
      this.children.add(arg);
      this.children.add(list);
    }
  }

  public static class IfNode extends ASTNode {
    public IfNode(ASTNode conditional, ASTNode exprOne, ASTNode exprTwo) {
      super(ASTNode.IF, null);
      this.children.add(conditional);
      this.children.add(exprOne);
      this.children.add(exprTwo);
    }
  }

  public static class EqualsNode extends ASTNode {
    public EqualsNode(ASTNode exprOne, ASTNode exprTwo) {
      super(ASTNode.EQUALS, null);
      this.children.add(exprOne);
      this.children.add(exprTwo);
    }
  }

  public static class AddNode extends ASTNode {
    public AddNode(ASTNode lhs, ASTNode rhs) {
      super(ASTNode.ADD, null);
      this.AddChild(lhs);
      this.AddChild(rhs);
    }
  }

  public static class SubNode extends ASTNode {
    public SubNode(ASTNode lhs, ASTNode rhs) {
      super(ASTNode.SUB, null);
      this.AddChild(lhs);
      this.AddChild(rhs);
    }
  }

  public static class MultNode extends ASTNode {
    public MultNode(ASTNode lhs, ASTNode rhs) {
      super(ASTNode.MULT, null);
      this.AddChild(lhs);
      this.AddChild(rhs);
    }
  }

  public static class DivNode extends ASTNode {
    public DivNode(ASTNode lhs, ASTNode rhs) {
      super(ASTNode.DIV, null);
      this.AddChild(lhs);
      this.AddChild(rhs);
    }
  }

  public static class SCombinatorNode extends ASTNode {
    public SCombinatorNode(ASTNode f, ASTNode g, ASTNode x) {
      super(ASTNode.S_COMBINATOR, null);
      this.AddChild(f);
      this.AddChild(g);
      this.AddChild(x);
    }
  }
  
  public static class KCombinatorNode extends ASTNode {
    public KCombinatorNode(ASTNode x, ASTNode y) {
      super(ASTNode.K_COMBINATOR, null);
      this.AddChild(x);
      this.AddChild(y);
    }
  }

  public static class BCombinatorNode extends ASTNode {
    public BCombinatorNode(ASTNode f, ASTNode g, ASTNode x) {
      super(ASTNode.B_COMBINATOR, null);
      this.AddChild(f);
      this.AddChild(g);
      this.AddChild(x);
    }
  }

  public static class CCombinatorNode extends ASTNode {
    public CCombinatorNode(ASTNode f, ASTNode g, ASTNode x) {
      super(ASTNode.C_COMBINATOR, null);
      this.AddChild(f);
      this.AddChild(g);
      this.AddChild(x);
    }
  }

  public static class YCombinatorNode extends ASTNode {
    public YCombinatorNode(ASTNode f) {
      super(ASTNode.Y_COMBINATOR, null);
      this.AddChild(f);
    }
  }
  
  public static class EmptyNode extends ASTNode {
    public EmptyNode(ASTNode list) {
      super(ASTNode.EMPTY, null);
      this.AddChild(list);
    }
  }
}
