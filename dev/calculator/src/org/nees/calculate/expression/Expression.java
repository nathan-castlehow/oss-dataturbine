options {
  NODE_DEFAULT_VOID = true;	// only generate explicitly requested nodes
  NODE_PREFIX = "My";		// don't prefix them with AST
  MULTI = true;			// don't only use SimpleNode
  VISITOR = true;		// create Visitor interface
  STATIC = false;		// the parser is reusable
}

PARSER_BEGIN(Expression)

package org.nees.calculate.expression;

public class Expression {

	public double[] values;
	MyStart n;

	public static void main(String[] args)
	{
		// print a reasonable "human readable" from of the grammer
		String output = 
		"The grammer for expressions is:\n" +
		"  top: exp ';' \n" +
		"  exp: product [('+'|'-') product]* \n" +
		"  product: term [('*'|'\') term]* \n" +
		"  term: ('+'|'-') term \n" +
		"      | '(' exp ')' \n" +
		"      | <double number> \n" +
		"      | <channel specifier> \n\n" +
		"  where <double number> is a double percision floating point \n" +
		"      number such as 3, 4.58, .5, .5e-12, and 0.345e+14, \n" +
		"  and <channel specifier> is the ordinal, zero-based index \n" +
		"      of the channel in one or two digits, preceeded by the letter \n" +
		"     'c', such as c0, c00, c05, c12, and c99. ";
		System.out.println(output);
	} // main

  public void setup()
  throws Exception
  {
   	n = Start();
  }

  public void setValues(double[] v)
  {	
  	values = new double[v.length];
  	for (int i = 0; i < v.length; i++)
  	{
  		values[i] = v[i];
	}
  }
  
  public void printTree()
  throws Exception
  {
  	ExpressionVisitor v = new DumpVisitor();
    n.jjtAccept(v, null);
  }

  public double eval()
  throws Exception
  {
  	ExpressionVisitor v = new EvalVisitor(values);
    n.jjtAccept(v, null);
    return  ((EvalVisitor)v).getFinalValue();
  }

  public boolean[] getChannelList()
  throws Exception
  {
  	ExpressionVisitor v = new ChannelListVisitor();			
	n.jjtAccept(v, null);
	return ((ChannelListVisitor)v).getChannelList();
  }
}

PARSER_END(Expression)

SKIP :
{
  " "
| "\t"
| "\n"
| "\r"
| <"//" (~["\n","\r"])* ("\n"|"\r"|"\r\n")>
| <"/*" (~["*"])* "*" (~["/"] (~["*"])* "*")* "/">
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
  | < SPECIAL: "pi" | "PI" | "e" | "E" >
  | < FUNCTION0: "random">
  | < FUNCTION1: "abs" | "acos" | "asin" | "atan"
	  | "ceil" | "cos" | "exp" | "floor" | "log" 
	  | "round" | "sin" | "sqrt" | "tan" 
	  | "toDegrees" | "toRadians" >
  | < FUNCTION2 : "atan2" | "max" | "min" >
  | < LOGICAL: "T" | "F">
}

MyStart Start() #Start : {}
{
  expression() ";"
  { return jjtThis; }
}

void expression(): {}
{ LOOKAHEAD(conditional())
    conditional()
  | simpleArithmetic()
}

void simpleArithmetic(): {} // exp: product [{ ("+"|"-") product }];
{ product()
    ( "+" product() #Add(2)	// Add with 2 descendants
    | "-" product() #Sub(2)
    )*
}

void product():	{} // product: term [{ ("*"|"%"|"/") term }];
{ term()
    ( "*" term() #Mul(2)
    | "/" term() #Div(2)
    )*
}

void term(): {} // term: "+"term | "-"term | "("sum")" | Number;
{   "+" term()			// no need to make node
  | "-" term() #Minus		// insert sign change node
  | "(" expression() ")"
				// Lit must be patched to inser value node
  | ( <DOUBLE> { jjtThis.val = new Double(token.image); } ) #Lit
  | ( <CHANNEL> { jjtThis.val = token.image; } ) #Channel
  | ( <SPECIAL> { jjtThis.val = token.image; } ) #Special
  | ( <FUNCTION0> 
  		{ jjtThis.val = token.image; } 
  		"()" ) #Function0
  | ( <FUNCTION1> 
  		{ jjtThis.val = token.image; }
  		"(" expression() ")" ) #Function1
  | ( <FUNCTION2> 
  		{ jjtThis.val = token.image; }
  		"(" expression() "," expression() ")" )   #Function2
}

void conditional():{}
{
	booleanExpression() "?" term() ":" term() #Conditional(3)
}

void booleanExpression(): {}
{ booleanSubExpression()
	( "|" booleanSubExpression() #Or(2)
	)*
}

void booleanSubExpression(): {}
{ booleanTerm()
	( "&" booleanTerm() #And(2)
	)*
}

void booleanTerm(): {}
{ LOOKAHEAD(comparison())
	comparison()
	| "!" booleanTerm()	#Negation
	| "(" booleanExpression() ")"
	| ( <LOGICAL> { jjtThis.val = token.image; } )  #LogicalValue
}

void comparison(): {}
{ 
	term()
	(   "==" term() #EQ(2)
      | "!=" term() #NE(2)
      | "<" term() #LT(2)
      | ">" term() #GT(2)
      | "<=" term() #LE(2)
      | ">=" term() #GE(2)
    )
}
