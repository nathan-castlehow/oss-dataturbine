options {
  NODE_DEFAULT_VOID = true;	// only generate explicitly requested nodes
  NODE_PREFIX = "My";	// prefix with My instad of AST
  MULTI = true;			// don't only use SimpleNode
  VISITOR = true;		// create Visitor interface
  STATIC = false;
}

PARSER_BEGIN(NumberList)

package org.nees.calculate.numberList;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Vector;

public class NumberList {
 
  public double[] value()
  throws ParseException
  {
	  MyListStart n = ListStart();
	  NumberListVisitor v = new VectorVisitor();
      Vector vect =  (Vector)n.jjtAccept(v, new Vector());
      double[] out = new double[vect.size()];
      for (int i = 0; i < out.length; i++)
      {
      	out[i] = ((Double)vect.elementAt(i)).doubleValue();
      }
      return out;
  }

}

PARSER_END(NumberList)

SKIP :
{
  " "
| "\t"
}


TOKEN:				// defines token names
{
    < DOUBLE: 
          ( <DIGIT> )+ ( "." ( <DIGIT> )* )? ( <EXP> )?
	    | "." ( <DIGIT> )+ ( <EXP> )? >
  | < CHANNEL: "c" <DIGIT>
  		| "c" <DIGIT><DIGIT> >
  | < #DIGIT: ["0" - "9"] >
  | < #EXP: ["e", "E"] ( ["+", "-"] )? ( <DIGIT> )+ >
  | < EOL: "\n">
}

MyListStart ListStart() #ListStart : {}
{
  (list())? (<EOL> | <EOF>) { return jjtThis; }
}

void list(): {}
{ listItem() ("," listItem()) *
}

void listItem(): {}
{
  	( <DOUBLE> { jjtThis.value = new Double(token.image); } ) #ListValue
}

