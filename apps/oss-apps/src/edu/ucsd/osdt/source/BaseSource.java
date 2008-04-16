package edu.ucsd.osdt.source;

import edu.ucsd.osdt.util.RBNBBase;
import com.rbnb.sapi.Source;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.ChannelMap;
import java.util.Properties;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.StringTokenizer;

/*! 
 * @file BaseSource.java
 * @brief
 * This class provides the core functionality needed by OSDT for source driver
 * applications and extends the RBNB com.rbnb.sapi.Source class
 * @author Lawrence J. Miller
 * @note $HeadURL$
 * @note $LastChangedRevision$
 * @author $LastChangedBy$
 * @date $LastChangedDate$
 * 
 * @todo make an abstact "generateCMap", or perhaps an interface to set up
 * channel names, units, and metadata
 * @todo include the channels that key the trackKML plugin
 */
public class BaseSource extends com.rbnb.sapi.Source 
{

	// holds the serverAddress
	private String serverAddress;
	private int rbnbArchiveSize;
	private int rbnbCacheSize;
	
	public BaseSource()
	{
		super();
	}
	
	
	public BaseSource(int cacheSize, String archiveMode, int archiveSize)
	{
		super(cacheSize,archiveMode,archiveSize);
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
