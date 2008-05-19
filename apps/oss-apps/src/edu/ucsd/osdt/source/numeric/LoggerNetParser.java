/*!
@file LoggerNetParser.java
@brief A parser to process instrument metadata to form a dataturbine channel map in conjunction with MDParserInterface.java
@author Lawrence J. Miller 
@date Mon May 19 2008
@note $HeadURL:http://nladr-cvs.sdsc.edu/svn-private/NEON/telepresence/dataturbine/trunk/src/edu/sdsc/cleos/ISOtoRbnbTime.java $
@note $LastChangedRevision:129M $
@author $LastChangedBy:(local) $
@date $LastChangedDate:2007-03-16 15:30:24 -0700 (Fri, 16 Mar 2007) $
*/

package edu.ucsd.osdt.source.numeric;

import edu.ucsd.osdt.util.MetaDataParser;
import com.rbnb.sapi.ChannelMap;

public class LoggerNetParser extends MetaDataParser {
	
	public LoggerNetParser() {
		super();
	}
	
	public ChannelMap getCmap() {
		return this.cmap;
	}
	
	public String[] getChannels() {
		return this.channels;
	}
	
	public String[] getUnits() {
		return this.units;
	}
	
	public boolean parse(String mdFromInstr) {
		this.cmap = new ChannelMap();
		this.channels = new String[1];
		this.units = new String[channels.length];
		return true;
	}
}