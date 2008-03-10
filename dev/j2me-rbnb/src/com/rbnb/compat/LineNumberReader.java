package com.rbnb.compat;
/** LineNumberReader mirrors the version in J2SE. This is not present
 * in J2ME, so we mimic its functionality.
 */

public class LineNumberReader //extends java.io.InputStreamReader
{
	/** Saving the line, 0 based
	 */
	private int line;
	/** Backing stream reader
	 */
	private java.io.InputStreamReader reader;

	/** Creates a new LineInputReader from the InputStream given
	 */
	public LineNumberReader(java.io.InputStreamReader in)
	{
		reader = in;
		line = 0;
	}

	/** Reads until we reach a \n or a \r character
	 */
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

	/** Closes the stream.
	 */
	public void close() throws java.io.IOException
	{
		reader.close();
	}
}