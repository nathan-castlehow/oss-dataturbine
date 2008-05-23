/*!
 * @file LoggerNetParser.java
 * @author Lawrence J. Miller <ljmiller@sdsc.edu>
 * @author $LastChangedBy$
 * @author Cyberinfrastructure Laboratory for Environmental Observing Systems (CLEOS)
 * @author San Diego Supercomputer Center (SDSC)
 * @date $LastChangedDate$
 * @version $LastChangedRevision$
 * @note $HeadURL$
 */

package edu.ucsd.osdt.source.numeric;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import edu.ucsd.osdt.util.MetaDataParser;
import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;

public class LoggerNetParser extends MetaDataParser {
	
	public LoggerNetParser() {
		super();
		logger = Logger.getLogger(getClass().getName());
	}
	
	public ChannelMap getCmap() {
		return (ChannelMap)this.get("cmap");
	}
	
	public String[] getChannels() {
		return (String[])this.get("channels");
	}
	
	public String[] getUnits() {
		return (String[])this.get("units");
	}
	
	public boolean parse(String cmdFromInstr) throws IOException, SAPIException {
		BufferedReader cmdReader = new BufferedReader(new StringReader(cmdFromInstr));
		String[] channelsTmp = cmdReader.readLine().split(",");
		channels = new String[channelsTmp.length];
		String[] unitsTmp = cmdReader.readLine().split(",");
		units = new String[unitsTmp.length];
		
		if( (channelsTmp.length != unitsTmp.length) || (channelsTmp.length == 0) ) {
			return false;
		} else { // input makes sense
			// clean off the double quotes from each channel names and unit labels (first and last character of each string)
			Pattern pattern = Pattern.compile("\"(.*)\"", Pattern.DOTALL);
			Matcher matcher = null;
			for(int i=0; i<channelsTmp.length; i++) {
				// channels
				matcher = pattern.matcher(channelsTmp[i]);
				if(matcher.find()) {
					channels[i] = matcher.group(1).trim();
					logger.finer(channels[i]);
				}
				// units
				matcher = pattern.matcher(unitsTmp[i]);
				if(matcher.find()) {
					units[i] = matcher.group(1).trim();
					logger.finer(units[i]);
				}
			}
			
			this.cmap = new ChannelMap();
			
			// assume all data are doubles
			for(int i=0; i<channelsTmp.length; i++) {
				this.cmap.Add(channels[i]);
				this.cmap.PutMime(cmap.GetIndex(channels[i]), "application/octet-stream");
				this.cmap.PutUserInfo(cmap.GetIndex(channels[i]), "units=" + units[i]);
			}
			this.put("channels", channels);
			this.put("units", units);
			this.put("cmap", cmap);
			return true;
		}
	}
} // class