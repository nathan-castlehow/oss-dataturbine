package edu.ucsd.osdt.source;

import edu.ucsd.osdt.util.RBNBBase;
import com.rbnb.sapi.Source;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.ChannelMap;
import java.util.logging.Logger;

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
 * @todo include the channels that key the trackKML plugin
 */
public class BaseSource extends com.rbnb.sapi.Source 
{

	// holds the serverAddress
	private String serverAddress;
	private int rbnbArchiveSize;
	private int rbnbCacheSize;
	/*! @var logger that needs to be instantiated by the derived class with "logger = Logger.getLogger(DerivedClass.class.getName());" */
	private static Logger logger;
	
	
	public BaseSource() {
		super();
	}
	
	
	public BaseSource(int cacheSize, String archiveMode, int archiveSize) {
		super(cacheSize, archiveMode, archiveSize);
	}
	
	/*! @fn postDoubles loads the @param doubleData into the @param cmap channelmap and then flushes to this sources dataturbine connection
	 * @note establish the convention of putting the timestamp as the last element of the data array */
	protected void postDoubles(double[] doubleData, ChannelMap cmap) throws SAPIException {
		this.Flush(cmap);
	}
}			
