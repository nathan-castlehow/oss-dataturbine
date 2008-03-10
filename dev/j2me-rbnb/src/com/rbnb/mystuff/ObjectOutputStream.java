package com.rbnb.mystuff;

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