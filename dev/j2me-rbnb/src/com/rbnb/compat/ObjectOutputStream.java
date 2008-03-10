package com.rbnb.compat;
/** Class here for compatibility. Not actually used due to the limitations of J2ME and
 * serialization.
 */

public class ObjectOutputStream extends java.io.DataOutputStream
{

	public ObjectOutputStream()
	{
		super(null);

	}

	public void writeObject(Object o) throws java.io.IOException
	{
		System.out.println("trying to write " + o);
	}

}