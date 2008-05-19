/*!
@file MDParserInterface.java
@brief An interface to be used by source driver proggrams to form a dataturbine channel map by querying a connected instrument
@author Lawrence J. Miller 
@date Mon May 19 2008
@note $HeadURL:http://nladr-cvs.sdsc.edu/svn-private/NEON/telepresence/dataturbine/trunk/src/edu/sdsc/cleos/ISOtoRbnbTime.java $
@note $LastChangedRevision:129M $
@author $LastChangedBy:(local) $
@date $LastChangedDate:2007-03-16 15:30:24 -0700 (Fri, 16 Mar 2007) $
*/

package edu.ucsd.osdt.util;
import com.rbnb.sapi.ChannelMap;

public interface MDParserInterface {
	/*! @brief accessor for acomplete channel map, which can be populated with metadata in a device-specific way */
	public ChannelMap getCmap();
	/*! @brief accessor for a channel list, which is universal */
	public String[] getChannels();
	/*! @brief accessor for physical units for each channel in the channel list */
	public String[] getUnits();
	/*! @brief device-specific method to process information that is made available by the instrument to populate implementations with channels and a channel map */ 
	public boolean parse(String mdFromInstr);
}