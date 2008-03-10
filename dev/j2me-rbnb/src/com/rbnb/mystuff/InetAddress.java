package com.rbnb.mystuff;

public class InetAddress
{
	String addy;
	int port;
	public InetAddress()
	{
	}
	
	public InetAddress(String a, int p)
	{
		addy = a;
		port = p;
	}
	public String getHostAddress()
	{
		return addy;
	}


	public String getHostName()
	{
		return addy;
	}

	public static InetAddress getByName(String s)
	{	
		return new InetAddress(s, 80);
	}

	public static InetAddress getLocalHost()
	{
		return new InetAddress("localhost", 80);
	}

	public static InetAddress[] getAllByName(String s)
	{
		return null;
	}

}