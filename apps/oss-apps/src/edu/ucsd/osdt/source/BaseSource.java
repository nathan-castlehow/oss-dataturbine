package edu.ucsd.osdt.source;

import edu.ucsd.osdt.util.RBNBBase;
import com.rbnb.sapi.Source;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.ChannelMap;
import java.util.Properties;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.StringTokenizer;

/*! @brief
 * This class provides a extends the RBNB Source class
 * @author Lawrence J. Miller
 */
public class BaseSource extends com.rbnb.sapi.Source 
{

	// holds the serverAddress
	private String serverAddress;
	private RBNBBase mRBNBBase;
	private int rbnbArchiveSize;
	private int rbnbCacheSize;
	
	/**
	 * Create a NeesSource object (based on an RBNB source) with the default
	 * arguments. A _units channel will be created.
	 */
	public BaseSource()
	{
		super();
		mRBNBBase = new RBNBBase(this);
	}
	
	/**
	 * Create a NeesSource object (based on an RBNB source) with the standard RBNB
	 * arguments. An _units channel will be created.
	 */
	public BaseSource(int cacheSize, String archiveMode, int archiveSize)
	{
		super(cacheSize,archiveMode,archiveSize);
		mRBNBBase = new RBNBBase(this);
	}
	
	/**
	 * Post a units description to this channel. It will be given the timestamp
	 * correcponding to the current time.
	 * 
	 * @param unitsArgs
	 */
	public void postUnits(String Channel, String units)
		throws IllegalArgumentException, SAPIException 
	{
		 /* if (mRBNBBase.getserverAddress()== null)
			throw new SAPIException("You must connect to the server before you can" +
				" post units."); */
		
		
	}
}			
