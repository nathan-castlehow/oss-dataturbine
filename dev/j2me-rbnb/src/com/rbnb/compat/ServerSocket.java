package com.rbnb.compat;
/** J2ME not meant to be as a server, so we have this stub for future use.
 */
public class ServerSocket 
{
	public ServerSocket()
	{
	}

	public ServerSocket(com.rbnb.compat.InetAddress a, int port)
	{
	}

	public ServerSocket(int i1, int i2)
	{
	}

	public void close()
	{
	}

	public void setSoTimeout(int i)
	{
	}

	public Socket accept()
	{
		return null;
	}
}