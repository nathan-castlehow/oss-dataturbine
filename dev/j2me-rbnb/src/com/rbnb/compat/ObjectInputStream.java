package com.rbnb.compat;
/** Class here for compatibility. Not actually used due to the limitations of J2ME and
 * serialization.
 */
public class ObjectInputStream extends java.io.DataInputStream
{

	public ObjectInputStream()
	{
		super(null);

	}

	public Object readObject() throws java.io.IOException
	{
		return null;
	}

}