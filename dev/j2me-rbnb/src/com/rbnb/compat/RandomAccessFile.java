package com.rbnb.compat;
/** Class here for compatibility. Not actually used because of the lack of Files in J2ME
 */

public class RandomAccessFile
{
	public RandomAccessFile(String nameI, String modeI)
	{

	}

	public RandomAccessFile(File fileI, String modeI)
	{

	}


	public int length()
	{
		return 1;
	}

	public void close() throws java.io.IOException
	{
	}

	public void seek(long i)
	{

	}

	public int read(byte[] b, int x, int length)
	{
		return 1;
	}

	public int write(byte[] b, int x, int length)
	{
		return 1;
	}

	public void write(int i)
	{
	}

}