/**
 * Defines the visitor interface
 * 
 * See PrintVistor.java and Interpreter.java
 */
public interface Visitor {
  public void visit(ASTNode node);
}
