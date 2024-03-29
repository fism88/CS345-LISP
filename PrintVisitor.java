
public class PrintVisitor implements Visitor {

  public void visit(ASTNode node) {
    this.visit(node, 0);
  }

  public String stringVisit(ASTNode node) {
    String result = "";

    result += node.type_str;
    if (node.value != null) {
      result += " " + node.value;
    }
    
    for (ASTNode child: node.children) {
      result += " " + this.stringVisit(child).trim();
    }
    return "(" + result + ") ";
  }

  private void visit(ASTNode node, int depth) {
    String result = "";
 
    for (int i = 0; i < depth; i++) {
      result += " ";
    }

    result += node.type_str;
    if (node.value != null) {
      result += " " + node.value;
    }
    
    System.out.println(result);

    for (ASTNode child: node.children) {
      this.visit(child, depth + 1);
    }
  }

  
}
