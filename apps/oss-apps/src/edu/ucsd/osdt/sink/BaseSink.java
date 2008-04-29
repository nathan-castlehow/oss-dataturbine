package edu.ucsd.osdt.sink;

import edu.ucsd.osdt.util.RBNBBase;
import com.rbnb.sapi.Source;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.ChannelMap;
import java.util.logging.Logger;

/*! 
 * @file BaseSink.java
 * @brief
 * This class provides the core functionality needed by OSDT for sink clients
 * applications and extends the RBNB com.rbnb.sapi.Sink class
 * @author Lawrence J. Miller
 * @note $HeadURL$
 * @note $LastChangedRevision$
 * @author $LastChangedBy$
 * @date $LastChangedDate$
 * 
 * @todo set up a chanel map for the different types of requests
 */
public class BaseSink extends com.rbnb.sapi.Sink 
{

	// holds the serverAddress
	private String serverAddress;
	/*! @var logger that needs to be instantiated by the derived class with "logger = Logger.getLogger(DerivedClass.class.getName());" */
	private static Logger logger;
	
	public BaseSink()
	{
		super();
	}
	
	public BaseSink(String varServerAddress)
	{
		super();
		serverAddress = varServerAddress;
	}
	
	/*! @brief gets the canonical channel map that can be processed further */
	public ChannelMap getChannelMap() throws SAPIException
	{
		ChannelMap retval = new ChannelMap();
		RequestRegistration(retval);
		return retval;
	}
}			
