package com.rbnb.compat;
import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;

/**
 * This class replaces the Java Socket class as it does not exist in J2ME. It uses the Connector interface 
 * instead and assumes that we are using a socket connection
 */

public class Socket 
{
	/** Address we are connecting to
	 */
	private  String address;
	/** port we are connecting to
	 */
	private  int port;
	/** Saving our input stream so we don't have to continually reopen it
	 */
	private  java.io.InputStream socketIn = null;
	/** Saving our output stream
	 */
	private  java.io.OutputStream socketOut = null;
	/** Underlying socket connection
	 */
	private  SocketConnection socket = null;

	/** Default constructor, does nothing
	 */
	public Socket() throws java.io.IOException
	{
	}

	/** Opens a socket to the specified InetAddress and port
	 */
	public Socket(com.rbnb.compat.InetAddress a, int port) throws java.io.IOException
	{
			this.address = a.addy;
			this.port = port;
			socket = (SocketConnection)Connector.open("socket://" + address + ":" + port);
	
	}

	/** Opens a socket to the specified address
	 */
	public Socket(String address, int port) throws java.io.IOException
	{
		this.address = address;
		this.port = port;
	}

	/** Does nothing, here for compatibility reasons
	 */
	public void setTcpNoDelay(boolean b)
	{
	}

	/** Does nothing, here for compatibility reasons
	 */
	public void setSoLinger(boolean b, int i)
	{
	}

	/** Closes the connection
	 */
	public void close() throws java.io.IOException
	{
		if(socketIn != null)
			socketIn.close();
		if(socketOut != null)
			socketOut.close();
		if(socket != null)
			socket.close();

	}

	/** Returns the local address
	 */
	public InetAddress getLocalAddress()
	{
		return new InetAddress(address);
	}

	/** Returns the Inetaddress
	 */
	public InetAddress getInetAddress()
	{
		return new InetAddress(address);
	}

	/** returns the port
	 */
	public int getLocalPort()
	{
		return port;
	}

	/** Returns the output stream from the connection
	 */
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

	/** Returns the inputstream from the connection
	 */
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

	/** Does nothing, here for compatibility reasons
	 */
	public void setSoTimeout(int to)
	{

	}

	/** Closes the output stream
	 */
	public void shutdownOutput() throws java.io.IOException
	{
		if(socketOut != null)
			socketOut.close();
	}
	
	/** Closes the input stream
	 */
	public void shutdownInput() throws java.io.IOException
	{
		if(socketIn != null)
			socketIn.close();
	}

	/** Here for compatibility reasons
	 */
	public Socket accept() 
	{
		return null;
	}	

	/** Returns true always
	 */
	public boolean equals(Object o)
	{
		return true;
	}


}