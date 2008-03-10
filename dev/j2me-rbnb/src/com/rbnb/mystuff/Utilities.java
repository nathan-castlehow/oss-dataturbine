package com.rbnb.mystuff;

public class Utilities
{
	public static final boolean isWhitespace(char c)
	{
		// missing u000A and u000D
		if(c == '\u2007' || c == '\u00A0' || c == '\u202F'
			|| c == '\u0009' || c == '\u001C' || c == '\u001D' || c == '\u001E' || c == '\u001F'
			|| c == '\u000B' || c == '\u000C' || c == ' ' || c == '\t' || c == '\n' || c == '\r')
		{
			return true;
		}
		else 
		{
			return false;
		}


	}

	public static String dateFormat(java.util.Date d)
	{
		//System.out.println("kkkkkkkkkk " + d);
		return "1322 22 22";
	}

	public static java.util.Date SimpleDateFormat(String str, String d)
	{
		//System.out.println("jjjjjjjjjjj " + str + ", " + d);
		return new java.util.Date(1000);
	}

	public static boolean interrupted(Thread t)
	{
		return false;
	}

	public static int getArrayLength(Object arrayI)
	{
		int elements;
		if (arrayI instanceof boolean[]) 
		{
			elements = ((boolean[]) arrayI).length;
		} 
		else if (arrayI instanceof byte[]) 
		{
			elements = ((byte[]) arrayI).length;
		} 
		else if (arrayI instanceof double[]) 
		{
			elements = ((double[]) arrayI).length;
		} 
		else if (arrayI instanceof float[]) 
		{
			elements = ((float[]) arrayI).length;
		} 
		else if (arrayI instanceof int[]) 
		{
			elements = ((int[]) arrayI).length;
		} 
		else if (arrayI instanceof long[]) 
		{
			elements = ((long[]) arrayI).length;
		} 
		else if (arrayI instanceof short[]) 
		{
			elements = ((short[]) arrayI).length;
		} 
		else if (arrayI instanceof String[]) 
		{
			elements  = ((String[]) arrayI).length;
		}
		else
			elements = ((Object[]) arrayI).length;
		return elements;
	}
	
	public static java.util.Vector cloneVector(java.util.Vector v)
	{
		java.util.Vector ret;
		if(v instanceof com.rbnb.utility.SortedVector)
		{
			ret = new com.rbnb.utility.SortedVector();
		}
		else
		{
			ret = new java.util.Vector();
		}
		for(int i = 0; i<v.size(); i++)
		{
			//System.out.println("cloning " + v.elementAt(i).getClass());
			ret.addElement(v.elementAt(i));
			try 
			{
			//	ret.addElement(((com.rbnb.api.Serializable)v.elementAt(i)).clone());
			}
			catch(Exception e) { e.printStackTrace(); }
		}
		return v;
	}
	public static int lastIndexOf(String base, String str)
	{
		int result = 0, temp = 0;
		result = temp = base.indexOf(str);
		while(temp != -1) 
		{
			result = temp;
			temp = base.indexOf(str, result+1);
		}
		return result;
	}

	public static boolean isNumber(Object o)
	{
		return ((o instanceof Byte) || (o instanceof Double) || (o instanceof Float) ||
		(o instanceof Integer) || (o instanceof Long) || (o instanceof Short));

	}

	public static double round(double a)
	{
		if( (a-(int)a)*10 >= 5 )
			return Math.ceil(a);
		else
			return Math.floor(a);
	}

	public static double pow(double a, double b)
	{
		//System.err.println("Pow called!!!!!!!!!!!!!!!!!" + a + ", " + b);
		double ret = 1;
		for(int i = 0; i<b; i++)
			ret *= a;
		return ret; // fix later
	}

	public static Object arrayGet(Object arrayI, int index) throws IllegalArgumentException,
															  ArrayIndexOutOfBoundsException
	{
		if (arrayI instanceof boolean[]) 
		{
			return new Boolean(((boolean[]) arrayI)[index]);
		} 
		else if (arrayI instanceof byte[]) 
		{
			return new Byte(((byte[]) arrayI)[index]);
		} 
		else if (arrayI instanceof double[]) 
		{
			return new Double(((double[]) arrayI)[index]);
		} 
		else if (arrayI instanceof float[]) 
		{
			return new Float(((float[]) arrayI)[index]);
		} 
		else if (arrayI instanceof int[]) 
		{
			return new Integer(((int[]) arrayI)[index]);
		} 
		else if (arrayI instanceof long[]) 
		{
			return new Long(((long[]) arrayI)[index]);
		} 
		else if (arrayI instanceof short[]) 
		{
			return new Short(((short[]) arrayI)[index]);
		} 
		else if (arrayI instanceof String[]) 
		{
			return ((String[]) arrayI)[index];
		}
		else if(arrayI instanceof Object[])
		{
			return ((Object[]) arrayI)[index];
		}
		else
			throw new IllegalArgumentException("Not an array argument");

	}
}