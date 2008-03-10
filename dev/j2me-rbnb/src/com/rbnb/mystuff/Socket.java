package com.rbnb.mystuff;
import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;

public class Socket 
{
	private  String address;
	private  int port;
	private  java.io.InputStream socketIn = null;
	private  java.io.OutputStream socketOut = null;
	private  SocketConnection socket = null;

	public Socket() throws java.io.IOException
	{
	}

	public Socket(com.rbnb.mystuff.InetAddress a, int port) throws java.io.IOException
	{
		//if(socket == null)
		//{
			this.address = a.addy;
			this.port = port;
			socket = (SocketConnection)Connector.open("socket://" + address + ":" + port);
		//}
	}

	public Socket(String address, int port) throws java.io.IOException
	{
		this.address = address;
		this.port = port;
	}

	public void setTcpNoDelay(boolean b)
	{
	}
	public void setSoLinger(boolean b, int i)
	{
	}
	public void close()
	{
		

	}

	public InetAddress getLocalAddress()
	{
		return new InetAddress(address, port);
	}

	public InetAddress getInetAddress()
	{
		return new InetAddress(address, port);
	}

	public int getLocalPort()
	{
		return port;
	}

	public java.io.OutputStream getOutputStream() 
	{
		
		if(socketOut == null)
		{
			try 
			{
				socketOut = socket.openOutputStream();
		
			}
			catch(java.io.IOException e) {System.out.println("failed IOException");}
			
		}
		
		return socketOut;
	}

	public java.io.InputStream getInputStream() 
	{

		if(socketIn == null)
		{
			try 
			{
				socketIn = socket.openInputStream();
			}
			catch(Exception e) { e.printStackTrace(); }

		}
		
		return socketIn;
	}

	public void setSoTimeout(int to)
	{

	}

	public void shutdownOutput()
	{
		System.out.println("called shutdown output stub");
	}
	
	public void shutdownInput()
	{
		System.out.println("called shutdown stub");
	}

	public Socket accept()
	{
		System.out.println("called accept stub");
		return null;
	}	

	public boolean equals(Object o)
	{
		System.err.println("!!!!!!!!!!!!!!!!");
		return true;
	}


}