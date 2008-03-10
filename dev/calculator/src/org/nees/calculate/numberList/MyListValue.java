package org.nees.calculate.numberList;

public class MyListValue extends SimpleNode {

	public Double value;
	
  public MyListValue(int id) {
    super(id);
  }

  public MyListValue(NumberList p, int id) {
    super(p, id);
  }
  
  public String toString()
  {
  	return super.toString() + " = " + value;
  }

  /** Accept the visitor. **/
  public Object jjtAccept(NumberListVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
