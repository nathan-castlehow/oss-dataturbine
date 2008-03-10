package com.rbnb.mystuff;

public class LineNumberReader //extends java.io.InputStreamReader
{
	private int line;
	private java.io.InputStreamReader reader;

	public LineNumberReader(java.io.InputStreamReader in)
	{
		reader = in;
		line = 0;
	}
	public String readLine() throws java.io.IOException
	{
		String ret = "";
		char c;
		while( (c = (char)reader.read()) != -1)
		{
			if(c == '\n' || c == '\r')
				break;
			ret += c;
		}
		line++;
		if(ret.length() == 0)
			return readLine();
		return ret;
	}

	public void close() throws java.io.IOException
	{
		reader.close();
	}
}