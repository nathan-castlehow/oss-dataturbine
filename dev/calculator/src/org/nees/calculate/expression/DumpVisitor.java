package org.nees.calculate.expression;

public class DumpVisitor implements ExpressionVisitor
{
  private int indent = 0;
  
  private String indentString() {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < indent; ++i) {
      sb.append(" ");
    }
    return sb.toString();
  }
  
  private void print(SimpleNode n)
  {
    if (n.val == null)
	  	System.out.println(indentString() + n);
	else
	  	System.out.println(indentString() + n + " = " + n.val);
  }

  private Object process(SimpleNode node, Object data)
  {
  	print(node);
    ++indent;
    data = node.childrenAccept(this, data);
    --indent;
    return data;
  }

  public Object visit(SimpleNode node, Object data) {
    System.out.println(indentString() + node +
		       ": acceptor not unimplemented in subclass?");
    ++indent;
    data = node.childrenAccept(this, data);
    --indent;
    return data;
  }

  public Object visit(MyStart node, Object data) { return process(node, data); }
  public Object visit(MyAdd node, Object data) { return process(node, data); }
  public Object visit(MySub node, Object data) { return process(node, data); }
  public Object visit(MyMul node, Object data) { return process(node, data); }
  public Object visit(MyDiv node, Object data) { return process(node, data); }
  public Object visit(MyMinus node, Object data) { return process(node, data); }
  public Object visit(MyFunction0 node, Object data) { return process(node, data); }
  public Object visit(MyFunction1 node, Object data) { return process(node, data); }
  public Object visit(MyFunction2 node, Object data) { return process(node, data); }
  public Object visit(MySpecial node, Object data) { return process(node, data); }
  public Object visit(MyLit node, Object data) { return process(node, data); }
  public Object visit(MyChannel node, Object data) { return process(node, data); }

  public Object visit(MyConditional node, Object data) { return process(node, data); }
  public Object visit(MyLogicalValue node, Object data) { return process(node, data); }
  public Object visit(MyOr node, Object data) { return process(node, data); }
  public Object visit(MyAnd node, Object data) { return process(node, data); }
  public Object visit(MyNegation node, Object data) { return process(node, data); }

    public Object visit(MyLT node, Object data) { return process(node, data); }
    public Object visit(MyLE node, Object data) { return process(node, data); }
    public Object visit(MyGT node, Object data) { return process(node, data); }
    public Object visit(MyGE node, Object data) { return process(node, data); }
    public Object visit(MyEQ node, Object data) { return process(node, data); }
    public Object visit(MyNE node, Object data) { return process(node, data); }
}

/*end*/