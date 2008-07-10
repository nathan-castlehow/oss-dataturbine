/*!
 * @file SeabirdParser.java
 * @author Lawrence J. Miller <ljmiller@sdsc.edu>
 * @author $LastChangedBy$
 * @author Cyberinfrastructure Laboratory for Environmental Observing Systems (CLEOS)
 * @author San Diego Supercomputer Center (SDSC)
 * @date $LastChangedDate$
 * @version $LastChangedRevision$
 * @note $HeadURL$
 */

package edu.ucsd.osdt.source.numeric;
import edu.ucsd.osdt.util.ISOtoRbnbTime;
//java
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*! @brief A class to encapsulate channel data and metadata parsing logic for seabird output; extends a hashtable.
 * @see edu.sdsc.cleos.SeabirdSource
 * @see java.util.Hashtable */
class SeabirdParser extends Hashtable
{	
	private static Logger logger = Logger.getLogger(SeabirdParser.class.getName());
	/*! @var monthMap a Hashmap used to convert month abbreviations to serial numbers */
	private HashMap<String, String> monthMap;
	private String[] channelNames;
	private String[] units;
	private String[] metadataChannels;
	
	
	/*! @brief Constructor statically initializes data structures.
	 * @todo do this dynamically from a "ds" */
	public SeabirdParser() {
		super();
		initMonthMap();
		/*! @todo infer this from the setup config read off the seabird */
		
		// configuration for MCR Seacat
		channelNames = new String[8];
		channelNames[0] = "Temperature";
		channelNames[1] = "Conductivity";
		channelNames[2] = "Pressure";
		channelNames[3] = "Salinity";
		channelNames[4] = "Voltage 0";
		channelNames[5] = "Voltage 1";
		channelNames[6] = "Voltage 2";
		channelNames[7] = "Voltage 3";
		units = new String[channelNames.length];
		units[0] = "C";
		units[1] = "S/M";
		units[2] = "dbar";
		units[3] = "psu";
		units[4] = "V";
		units[5] = "V";
		units[6] = "V";
		units[7] = "V";
		metadataChannels = new String[2];
		metadataChannels[0] = "Model";
		metadataChannels[1] = "Serial Number";
		
		// configuration for SIO Seacat
		/*channelNames = new String[8];
		channelNames[0] = "Temperature";
		channelNames[1] = "Conductivity";
		channelNames[2] = "Pressure";
		channelNames[3] = "Voltage 0";
		channelNames[4] = "Voltage 1";
		channelNames[5] = "Voltage 2";
		channelNames[6] = "Voltage 3";
		channelNames[7] = "Voltage 4";
		units = new String[channelNames.length];
		units[0] = "C";
		units[1] = "S/M";
		units[2] = "dbar";
		units[3] = "V";
		units[4] = "V";
		units[5] = "V";
		units[6] = "V";
		units[7] = "V";
		metadataChannels = new String[2];
		metadataChannels[0] = "Model";
		metadataChannels[1] = "Serial Number";*/
	}

	
	/*! @brief Placeholder that will eventually the number and labels of channels. */
	public void parseMetaData() {
		put("channels", channelNames);
		put("units", units);
		put("metadata-channels", metadataChannels);
		
		if(get("ds") != null) {
			/*! @note S>ds bit is needed to clean up seabird cmd echo */
			parseAndPut((String)get("ds"), "\\A[S>ds]+[\\s]+(.+) SERIAL NO\\. [0-9]+ ", "model");
			parseAndPut((String)get("ds"), "SERIAL NO\\. ([0-9]+) ", "serial");
		} else { //  hard-code info from MCR
			put("model", "SeacatPlus V 1.7");
			put("serial", "4974");
		}
	}
	
	
	/*! @brief Applies a regex and put the match into the hashtable.
	 * @param regexTarget a string from which to parse with the regex
	 * @param regex a regular expression containing a capture group
	 * @param hashKey a key into which to put the captured string into this Hashtable */
	protected void parseAndPut(String regexTarget, String regex, String hashKey) {
		logger.finer("Checking:" + regexTarget + " for: " + regex);
		Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE | Pattern.DOTALL);
		Matcher matcher = pattern.matcher(regexTarget);
		if(matcher.find()) {
			logger.finer("Matched group:" + matcher.group(1) + " for key:" + hashKey);
			put(hashKey, matcher.group(1).trim());
			logger.finer(hashKey + ":" + (String)get(hashKey) + " entered into hash");
		} else {
			logger.finer("No match for:" + hashKey);
		}
	}
	
	
	/*! @brief Tokenizes a line of seabird data into an array of doubles, the last element of which is an rbnb timestamp.
	 * @param seabirdLine a single data line read from the seabird, composed of comma-delimited deciaml values and two timesatmp tokens at the end
	 * @return an array of data points; the last element is an rbnb timestamp (i.e. seconds since the epoch) */
	public double[] getData (String seabirdLine) throws ParseException, SeabirdException {
		logger.fine("getData line:" + seabirdLine);
		String[] seabirdLineSplit = seabirdLine.split("[\\s]*,[\\s]*");
		StringBuffer dateBuffer = new StringBuffer();
		
		for(int i=0; i<seabirdLineSplit.length; i++) {
			logger.finest("Split:" + seabirdLineSplit[i]);
		}
		
		/*! @note tokenize the seabird date and package for conversion to ISO8601 */
		dateBuffer.append(seabirdLineSplit[seabirdLineSplit.length-2]);
		dateBuffer.append(" ");
		dateBuffer.append(seabirdLineSplit[seabirdLineSplit.length-1]);
		
		double timeNow = getRBNBDate(dateBuffer.toString());
		
		logger.finer("Seabird date string:" + dateBuffer.toString());
		logger.finer("RBNB date:" + timeNow);
		/*! @note ISOtoRbnbTime.formatDate needs millis from the epoch */
		logger.finer("Nice RBNB date:" + ISOtoRbnbTime.formatDate((long)(timeNow*1000)) );
		
		double[] retval = new double[seabirdLineSplit.length-1];
		for(int i=0; i<retval.length-1; i++) {
			try {
				retval[i] = Double.parseDouble(seabirdLineSplit[i]);
			} catch(NumberFormatException nfe) {
				throw new SeabirdException("Tokens munged:" + seabirdLineSplit[i]);
			}
		}
		/*! @note add newly computed RBNB time as the last element */
		retval[retval.length-1] = timeNow;
		
		return retval;
	}
	
	
	/*! @brief Gets the seabird-formatted date string, converts it to ISO8601, then converts that to rbnb seconds since the epoch.
	 * @param seabirdDate string representing the date as reported by the seabird
	 * @return rbnb timestamp (i.e. seconds since the epoch)*/
	public double getRBNBDate(String seabirdDate) throws ParseException, SeabirdException {
		/*! @note ISORbnbTime uses ISO8601 timestamp, e.g. 2003-08-12T19:21:22.30095 */
		/*! @note from seabird: 01 Feb 2008 11:51:41 */
		String[] seabirdDateTokens = seabirdDate.split(" ");
		StringBuffer retval = new StringBuffer();
		
		if(seabirdDateTokens.length != 4) {
			throw new SeabirdException("Bad date string from Seabird: " + seabirdDate);
		}
		
		// build the ISO string
		// year
		retval.append(seabirdDateTokens[2]);
		retval.append("-");
		// month
		String monthNumber = monthMap.get(seabirdDateTokens[1]);
		retval.append(monthNumber);
		retval.append("-");
		// day
		retval.append(seabirdDateTokens[0]);
		retval.append("T");
		// time
		retval.append(seabirdDateTokens[3]);
		retval.append(".00000");
		String iso8601String = retval.toString();
		logger.finer("ISO8601:" + iso8601String);
		ISOtoRbnbTime rbnbTimeConvert = new ISOtoRbnbTime(iso8601String);
		return rbnbTimeConvert.getValue();
	}
	
	
	/*! @brief Convenience to efficiently convert from seabird month names to ISO month numbers. */
	private void initMonthMap() {
		monthMap = new HashMap(12);
		monthMap.put("Jan", "01");
		monthMap.put("Feb", "02");
		monthMap.put("Mar", "03");
		monthMap.put("Apr", "04");
		monthMap.put("May", "05");
		monthMap.put("Jun", "06");
		monthMap.put("Jul", "07");
		monthMap.put("Aug", "08");
		monthMap.put("Sep", "09");
		monthMap.put("Oct", "10");
		monthMap.put("Nov", "11");
		monthMap.put("Dec", "12");
	}
	
	
	/*!   */
	public String[] getChannels() {
		return this.channelNames;
	}
	
	              
	/*! @note follow convention of RBNBBase */
	protected String getCVSVersionString() {
		return getSVNVersionString();
	}

	
	/*! @note svn keywords */
	protected String getSVNVersionString() {
		return(
				"$LastChangedDate$\n" +
				"$LastChangedRevision$\n" +
				"$LastChangedBy$\n" +
				"$HeadURL$"
		);
	}
} // class
