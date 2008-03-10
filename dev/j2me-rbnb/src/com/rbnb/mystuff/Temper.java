package com.rbnb.mystuff;
import java.util.*;
public class Temper extends java.io.OutputStream
{
	private Vector v = new Vector();

	public void close()
	{
		System.out.println("closed");
	}

	public void write(byte[] b)
	{
		for(int i = 0; i<b.length; i++)
			v.addElement(new Byte(b[i]));
System.out.println(v);
	}

	public void write(int b)
	{
		v.addElement(new Byte((byte)b));
System.out.println(v);
	}

	public void write(byte[] b, int off, int len)
	{
		for(int i = off; i<len+off; i++)
			v.addElement(new Byte(b[i]));
		System.out.println(v);
	}

}