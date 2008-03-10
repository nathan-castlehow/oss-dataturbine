package org.nees.calculate.numberList;

import java.util.Vector;
import org.nees.calculate.base.Base;

public class VectorVisitor implements NumberListVisitor
{
  private int indent = 0;

  private String indentString() {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < indent; ++i) {
      sb.append(" ");
    }
    return sb.toString();
  }

  public Object visit(SimpleNode node, Object data) {
  	if (Base.trace)
	  	System.out.println(indentString() + node);
    ++indent;
    data = node.childrenAccept(this, data);
    --indent;
    return data;
  }
  
  public Object visit(MyListStart node, Object data)
  {return visit((SimpleNode)node, data);}
  public Object visit(MyListValue node, Object data)
  {
  	((Vector) data).addElement(node.value);
  	return visit((SimpleNode)node, data);
  }

}

/*end*/