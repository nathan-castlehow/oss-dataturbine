package com.rbnb.compat;
/** Class represents an internet address. This is simple a String for J2ME
 */
public class InetAddress
{
	/** Address represented
	 */
	public String addy;

	/** Default constructor, does nothing
	 */
	public InetAddress()
	{
	}
	
	/** Sets the Address to the specified String
	 */
	public InetAddress(String a)
	{
		addy = a;
	}

	/** Returns the address stored by the InetAddress
	 */
	public String getHostAddress()
	{
		return addy;
	}

	/** Returns the address stored by the InetAddress
	 */
	public String getHostName()
	{
		return addy;
	}

	/** Returns a new InetAddress from the specified String
	 */
	public static InetAddress getByName(String s)
	{	
		return new InetAddress(s);
	}

	/** Returns a localhost InetAddress
	 */
	public static InetAddress getLocalHost()
	{
		return new InetAddress("localhost");
	}

	/** Returns nothing, here for compatibility reasons
	 */
	public static InetAddress[] getAllByName(String s)
	{
		return null;
	}

	/** Specifies two InetAddresses are equal if they represent the same
	 * name
	 */
	public boolean equals(Object o)
	{
		return addy.equals( ((InetAddress)o).addy);
	}

	/** Hash code
	 */
	public int hashCode()
	{
		return addy.hashCode();
	}

}