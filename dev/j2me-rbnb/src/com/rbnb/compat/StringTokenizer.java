package com.rbnb.compat;
/** Basic tokenizer class, mimics the java StringTokenizer
 */
public class StringTokenizer 
{

	/** String being tokenized
	 */
	private String string;
	/** delimiters being used
	 */
	private String delim;
	/** current location
	 */
	private int loc;
	/** return the delimiters
	 */
	private boolean retdelim;

	/** Basic constructor, uses default delimiters
	 */
	public StringTokenizer(String s)
	{
		this(s, " \t\r\n");
	}

	/** Uses the specified delimiters
	 */
	public StringTokenizer(String s, String d)
	{
		this(s, d, false);
	}

	/** Uses the specified delimiters and will or will not return the delimiters as tokens
	 */
	public StringTokenizer(String s, String d, boolean b)
	{
		string = s;
		delim = d;
		loc = 0;
		retdelim = b;
	}

	/** returns the number of tokens remaining
	 */
	public int countTokens()
	{
		int count = 0;
		boolean delim = false;
		for(int i = loc; i<string.length(); i++)
		{
			if(isDelim(string.charAt(i)))
			{
				if(!delim) 
				{
					delim = true;
					if(retdelim)
						count++;
				}
			}
			else
			{
				if(delim)
				{
					delim = false;
					count++;
				}
			}
		}
		return count;
	}

	/** Returns true if there are more tokens
	 */
	public boolean hasMoreTokens()
	{

		return loc < string.length();
	}


	private boolean isDelim(char c)
	{
		for(int i = 0; i<delim.length(); i++)
		{
			if(c == delim.charAt(i))
				return true;
		}
		return false;
	}

	/** returns the next token
	 */
	public String nextToken()
	{
		String ret = "";
		// get rid of initial whitespace
		while(isDelim(string.charAt(loc)) && loc < string.length())
		{
			loc++;
		}

		for(; loc<string.length(); loc++)
		{
			if(!isDelim(string.charAt(loc)))
				ret += string.charAt(loc);
			else
				break;

		}
		loc++;
		return ret;
	
	}

}