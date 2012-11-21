
public class PrintVisitor extends Visitor {

  public void visit(ASTNode node) {
    this.visit(node, 0);
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
