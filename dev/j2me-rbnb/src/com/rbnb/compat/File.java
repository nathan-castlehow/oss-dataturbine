package com.rbnb.compat;
/** This is a filler class so that we can compile in J2ME.
 * For the most part, J2ME won't use File objects, so this should not be an issue.
 */
public class File 
{

	public void mkdirs()
	{
	}

	public void mkdir()
	{}

	public String getPath()
	{
		return "";
	}

	public String[] list(Object s)
	{
		return new String[2];
	}
	
	public String getAbsolutePath()
	{
		return "";
	}

	public File(String s, String s2)
	{}

	public boolean renameTo(File s)
	{
		return false;
	}
	public File(String s)
	{}
	
	public String[] list()
	{
		return null;
	}
	public File()
	{
	}

	public boolean isDirectory()
	{
		return false;
	}

	public String getName()
	{
		return "";
	}

	public void delete()
	{
	}

	public File(File f, String s)
	{
	}
	public boolean exists()
	{ return true; }

}