package com.rbnb.compat;
/** Class used for compatibility for dates. Actually just calls the utility version of date parsing
 */

public class SimpleDateFormat
{
	public SimpleDateFormat(String format)
	{

	}

	/** Parses a string to be of the basic form MMM dd yyyy (HH:MM:SS)
	 */
	public java.util.Date parse(String s)
	{

		return com.rbnb.compat.Utilities.SimpleDateFormat(s);
	}

	/** Returns the default toString of a date
	 */
	public String format(java.util.Date d)
	{ 
		return d.toString();
	}




}