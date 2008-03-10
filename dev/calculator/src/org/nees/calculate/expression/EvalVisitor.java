package org.nees.calculate.expression;

import java.util.Stack;
import org.nees.calculate.base.Base;

public class EvalVisitor implements ExpressionVisitor
{
  private int indent = 0;
  private double[] values;
  private Stack stack = new Stack();
  private double finalValue = 0;

  public EvalVisitor(double[] v)
  {
  	resetValues(v);
  }
  
  public void resetValues(double[] v)
  {
  	values = v;
  	finalValue = 0.0;
  }
  
  public double getFinalValue(){
  	return finalValue;
  }
  
  private String indentString() {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < indent; ++i) {
      sb.append(" ");
    }
    return sb.toString();
  }
  
  private void print(SimpleNode n)
  {
  	if (Base.trace) System.out.println(indentString() + n);
  }
  
  private void print(SimpleNode n, String label)
  {
  	if (Base.trace) System.out.println(indentString() + n + ": " + label);
  }
  
  private void print(MyLogicalValue n, boolean b)
  {
  	if (Base.trace) System.out.println(indentString() + n + " = " + b);
  }
  
  private void print(MyLit n)
  {
   	if (Base.trace) System.out.println(indentString() + n + " = " + n.val);
  }
  
  private void print(MyChannel n, double v)
  {
  	if (Base.trace) 
  	  	System.out.println(indentString() + n + 
  		"(" + n.val + ")" + " = " + v);
  }

  private Object process(SimpleNode node, Object data)
  {
    ++indent;
    data = node.childrenAccept(this, data);
    --indent;
    return data;
  }

  private void printResults(Double r)
  {
  	if (Base.trace) System.out.println(indentString() + "==>" + r);
  }

  private void printResults(Boolean r)
  {
  	if (Base.trace) System.out.println(indentString() + "-->" + r);
  }
  
  public Object visit(SimpleNode node, Object data) {
    System.out.println(indentString() + node +
		       ": WARNING acceptor not unimplemented in subclass?");
    ++indent;
    data = node.childrenAccept(this, data);
    --indent;
    return data;
  }

  public Object visit(MyStart node, Object data)
  {
  	finalValue = 0.0; // in case an exception occures
  	print(node);
  	data = process(node, data);
  	finalValue = ((Double)stack.pop()).doubleValue();
  	return data;
  }
  
  public Object visit(MyAdd node, Object data)
  {
  	print(node);
  	Object ret = process(node, data);
  	Double v2 = (Double)stack.pop();
  	Double v1 = (Double)stack.pop();
  	Double ex = new Double((v1.doubleValue() + v2.doubleValue()));
  	printResults(ex);
  	stack.push(ex);
  	return ret;
  }
  
  public Object visit(MySub node, Object data)
  {
  	print(node);
  	Object ret = process(node, data);
  	Double v2 = (Double)stack.pop();
  	Double v1 = (Double)stack.pop();
  	Double ex = new Double((v1.doubleValue() - v2.doubleValue()));
  	printResults(ex);
  	stack.push(ex);
  	return ret;
  }
  
  public Object visit(MyMul node, Object data)
  {
  	print(node);
  	Object ret = process(node, data);
  	Double v2 = (Double)stack.pop();
  	Double v1 = (Double)stack.pop();
  	Double ex = new Double((v1.doubleValue() * v2.doubleValue()));
  	printResults(ex);
  	stack.push(ex);
  	return ret;
  }
  
  public Object visit(MyDiv node, Object data)
  {
  	print(node);
  	Object ret = process(node, data);
  	Double v2 = (Double)stack.pop();
  	Double v1 = (Double)stack.pop();
  	Double ex = new Double((v1.doubleValue() / v2.doubleValue()));
  	printResults(ex);
  	stack.push(ex);
  	return ret;
  }
  
  public Object visit(MyMinus node, Object data)
  {
  	print(node);
  	Object ret = process(node, data);
  	Double v1 = (Double)stack.pop();
  	Double ex = new Double(- v1.doubleValue());
  	printResults(ex);
  	stack.push(ex);
  	return ret;
  }
  
  public Object visit(MyFunction0 node, Object data)
  {
  	// random is the only single valued function
  	print(node,(String)node.val);
  	Object ret = process(node, data);
  	Double ex = new Double(0.0);
  	printResults(ex);
  	stack.push(ex);
  	return ret;
  }
  
  // | < FUNCTION1: "abs" | "acos" | "asin" | "atan"
  //  | "ceil" | "cos" | "exp" | "floor" | "log" 
  //  | "round" | "sin" | "sqrt" | "tan" 
  //  | "toDegrees" | "toRadians" >
  public Object visit(MyFunction1 node, Object data)
  {
  	String f = (String)node.val;
  	print(node,f);
  	Object ret = process(node, data);
  	double n = ((Double)stack.pop()).doubleValue();
  	if (f.equals("abs")) n = Math.abs(n);
  	else if (f.equals("acos")) n = Math.acos(n);
  	else if (f.equals("asin")) n = Math.asin(n);
  	else if (f.equals("atan")) n = Math.atan(n);
  	else if (f.equals("ceil")) n = Math.ceil(n);
  	else if (f.equals("cos")) n = Math.cos(n);
  	else if (f.equals("exp")) n = Math.exp(n);
  	else if (f.equals("sin")) n = Math.sin(n);
  	else if (f.equals("floor")) n = Math.floor(n);
  	else if (f.equals("log")) n = Math.log(n);
  	else if (f.equals("round")) n = Math.round(n);
  	else if (f.equals("sin")) n = Math.sin(n);
  	else if (f.equals("sqrt")) n = Math.sqrt(n);
  	else if (f.equals("tan")) n = Math.tan(n);
  	else if (f.equals("toDegrees")) n = Math.toDegrees(n);
  	else if (f.equals("toRadians")) n = Math.toRadians(n);
	else // should not happen but checking anyhow.
		System.out.println("Unrecoginzed function: " + f);
  	Double ex = new Double(n);
  	printResults(ex);
  	stack.push(ex);
  	return ret;
  }
  
  // | < FUNCTION2 : "atan2" | "max" | "min" >
  public Object visit(MyFunction2 node, Object data)
  {
  	String f = (String)node.val;
  	print(node,f);
  	Object ret = process(node, data);
  	double d2 = ((Double)stack.pop()).doubleValue();
  	double d1 = ((Double)stack.pop()).doubleValue();
  	if (f.equals("atan2")) d1 = Math.atan2(d1,d2);
  	else if (f.equals("max")) d1 = Math.max(d1,d2);
  	else if (f.equals("min")) d1 = Math.min(d1,d2);
	else // should not happen but checking anyhow.
		System.out.println("Unrecoginzed function: " + f);
   	Double ex = new Double(d1);
  	printResults(ex);
  	stack.push(ex);
  	return ret;
  }
  
  public Object visit(MySpecial node, Object data)
  {
  	// either PI or E
  	String f = (String)node.val;
  	print(node,f);
  	double n = 0.0;
  	if (f.equals("pi") || f.equals("PI"))
		n = Math.PI;
	else
		n = Math.E;
	Double ex = new Double(n);
  	printResults(ex);
  	stack.push(ex);
  	return data;
  }
  
  public Object visit(MyLit node, Object data)
  {
  	print(node);
  	stack.push(node.val);
  	return data;
  }
  
  public Object visit(MyChannel node, Object data)
  {
    String index_st = ((String)node.val).substring(1);
    int index = Integer.parseInt(index_st);
  	print(node, values[index]);
    stack.push(new Double(values[index]));
  	return data;
  }

  public Object visit(MyConditional node, Object data)
  {
  	print(node);
  	Object ret = process(node, data);
	double d2 = ((Double)stack.pop()).doubleValue();
	double d1 = ((Double)stack.pop()).doubleValue();
	boolean b = ((Boolean)stack.pop()).booleanValue();
	Double ex = new Double(b?d1:d2);
  	printResults(ex);
  	stack.push(ex);
  	return data;
  }

  public Object visit(MyOr node, Object data)
  {
  	print(node);
  	Object ret = process(node, data);
  	Boolean b2 = (Boolean)stack.pop();
  	Boolean b1 = (Boolean)stack.pop();
  	Boolean ex = new Boolean((b1.booleanValue() || b2.booleanValue()));
  	printResults(ex);
  	stack.push(ex);
  	return ret;
  }

  public Object visit(MyAnd node, Object data)
  {
  	print(node);
  	Object ret = process(node, data);
  	Boolean b2 = (Boolean)stack.pop();
  	Boolean b1 = (Boolean)stack.pop();
  	Boolean ex = new Boolean((b1.booleanValue() && b2.booleanValue()));
  	printResults(ex);
  	stack.push(ex);
  	return ret;
  }

  public Object visit(MyNegation node, Object data)
  {
  	print(node);
  	Object ret = process(node, data);
  	Boolean b1 = (Boolean)stack.pop();
  	Boolean ex = new Boolean(!b1.booleanValue());
  	printResults(ex);
  	stack.push(ex);
  	return ret;
  }

  public Object visit(MyLogicalValue node, Object data)
  {
    String flagSt = (String)node.val;
    boolean b = flagSt.equals("T");
    print(node,b);
    stack.push(new Boolean(b));
  	return data;
  }
  
  public Object visit(MyLT node, Object data)
  {
	print(node);
	Object ret = process(node, data);
	Double v2 = (Double)stack.pop();
	Double v1 = (Double)stack.pop();
	Boolean ex = new Boolean((v1.doubleValue() < v2.doubleValue()));
	printResults(ex);
	stack.push(ex);
	return ret;
  }

  public Object visit(MyLE node, Object data)
  {
	print(node);
	Object ret = process(node, data);
	Double v2 = (Double)stack.pop();
	Double v1 = (Double)stack.pop();
	Boolean ex = new Boolean((v1.doubleValue() <= v2.doubleValue()));
	printResults(ex);
	stack.push(ex);
	return ret;
  }

  public Object visit(MyGT node, Object data)
  {
	print(node);
	Object ret = process(node, data);
	Double v2 = (Double)stack.pop();
	Double v1 = (Double)stack.pop();
	Boolean ex = new Boolean((v1.doubleValue() > v2.doubleValue()));
	printResults(ex);
	stack.push(ex);
	return ret;
  }

  public Object visit(MyGE node, Object data)
  {
	print(node);
	Object ret = process(node, data);
	Double v2 = (Double)stack.pop();
	Double v1 = (Double)stack.pop();
	Boolean ex = new Boolean((v1.doubleValue() >= v2.doubleValue()));
	printResults(ex);
	stack.push(ex);
	return ret;
  }

  public Object visit(MyEQ node, Object data)
  {
	print(node);
	Object ret = process(node, data);
	Double v2 = (Double)stack.pop();
	Double v1 = (Double)stack.pop();
	Boolean ex = new Boolean((v1.doubleValue() == v2.doubleValue()));
	printResults(ex);
	stack.push(ex);
	return ret;
  }

  public Object visit(MyNE node, Object data)
  {
	print(node);
	Object ret = process(node, data);
	Double v2 = (Double)stack.pop();
	Double v1 = (Double)stack.pop();
	Boolean ex = new Boolean((v1.doubleValue() != v2.doubleValue()));
	printResults(ex);
	stack.push(ex);
	return ret;
  }

} // EvalVisitor
