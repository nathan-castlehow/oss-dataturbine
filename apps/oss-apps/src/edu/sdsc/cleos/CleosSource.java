package edu.sdsc.cleos;

import com.rbnb.sapi.Source;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.ChannelMap;
import java.util.Properties;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.StringTokenizer;

/*! @brief
 * Based on the class org.nees.rbnb.NeesSource
 * This class priveds a cover ot the RBNB Source class that supports both a
 * a _units channel and an _events channel on the source and provides methods
 * to set the units and events, respectively, on that channel.
 * <p>
 * Units can be set or fetched either by using a String or by using a
 * java.util.Properties object. It the units are repersented as a string then
 * the sting is a tab or comma seperated list of channel-name, unit-type pairs.
 * The channel-name and the unit-type are separated by an equal sign (=). On both
 * the channel-name and the unit-type, leading and trailing white space is stripped.
 * The only whitespace within a channel-name or a unit-type will be a blank.
 * Multiple blank sequences will the taken literally. So,for example, the units
 * of a NeesSource with three channels (Ch1, Channel2, and Channel 3) might look
 * like this: 
 *    Ch1 = meters    Channel2 = metric tons     Channel 3 = milliparsec
 * or this:
 *    Ch1 = meters, Channel2 = metric tons, Channel 3 = milliparsec
 * Alternatly, the list of channel-name, unit-type pairs can be represented as a
 * Properties object (java.until.Progerites), with the channel as the key an the units
 * as the value.
 * <p>
 * The _events channel is used to singnal signifigent events, especially to drive
 * other external processes, such as data gathering triggered on the start and end
 * of the run of an experment. Events are a String describing the event. Two special
 * events are predefined START_EVENT and END_EVENT.
 * @author Lawrence J. Miller
 */
public class CleosSource
	// emualtes an extension -- extends Source
{
	/** The pedefined START event */
	public static final String START_EVENT = "Start";

	/** The pedefined END event */
	public static final String END_EVENT = "End";
	
	private static final Hashtable UNIT_CHANNEL = new Hashtable();
	private static final Hashtable EVENT_CHANNEL = new Hashtable();
	
	/** The name of the channel carrying unit type descriptions (e.g. _Units) */
	public static final String NEES_UNITS_SOURCE_NAME = "_Units";
	/** The name of the channel carrying event descriptions (e.g. _Event) */
	public static final String NEES_EVENT_SOURCE_NAME = "_Event";
	
	private String lastEvent;
	private String lastUnitsString;
	private Properties lastUnitsProperties;
	
	// used to emulate an Extensison to Source
	private Source source;
	// holds the serverAddress
	private String serverAddress;
	
	/**
	 * Create a NeesSource object (based on an RBNB source) with the default
	 * arguments. A _units channel will be created.
	 */
	public CleosSource()
	{
		source = new Source();
	}
	
	/**
	 * Create a NeesSource object (based on an RBNB source) with the standard RBNB
	 * arguments. An _units channel will be created.
	 */
	public CleosSource(int cacheSize, String archiveMode, int archiveSize)
	{
		source = new Source(cacheSize,archiveMode,archiveSize);
	}
	
	/**
	 * Post a units description to this channel. It will be given the timestamp
	 * correcponding to the current time.
	 * 
	 * @param unitsArgs
	 */
	public void postUnits(String units)
		throws IllegalArgumentException, SAPIException 
	{
		if (serverAddress == null)
			throw new SAPIException("You must connect to the server before you can" +
				" post units.");
		Properties unitsProp = null;
		unitsProp = parseUnitsString(units); // throws IllegalArgumentException
		appendPostString(NEES_UNITS_SOURCE_NAME,units);			
		lastUnitsProperties = unitsProp;
		lastUnitsString = units;
	}
	
	/**
	 * Post a units description to this channel. It will be given the timestamp
	 * correcponding to the current time.
	 * 
	 * @param propList
	 */
	public void postUnits(Properties unitsProp)
		throws IllegalArgumentException, SAPIException 
	{
		if (serverAddress == null)
			throw new SAPIException("You must connect to the server before you can" +
				" post units.");
		String key, value, posted = null;
		Enumeration keys = unitsProp.keys();
		while (keys.hasMoreElements())
		{
			key = (String)keys.nextElement();
			value = unitsProp.getProperty(key);
			if (value == null)
				throw new IllegalArgumentException("Encountered null value for key = " + key);
			if (posted != null) posted += ",";
			else posted = "";
			posted += key + "=" + value;
		}
		appendPostString(NEES_UNITS_SOURCE_NAME,posted);			
		lastUnitsProperties = unitsProp;
		lastUnitsString = posted;
	}
	
	/**
	 * Fetch (as a comma delimitted String) the latest units description.
	 * 
	 * @return the units description as a comma seperated String
	 */
	public String getLastUnitsString()
	{
		return lastUnitsString;
	}
	
	/**
	 * Fetch (as a Properties object) the latest units description.
	 * 
	 * @return the units description as a Properties object
	 */
	public Properties getLastUnitsProperties()
	{
		return lastUnitsProperties;
	}
	
	/**
	 * Post an event.
	 * 
	 * @param theEvent
	 * @throws SAPIException
	 */
	public void postEvent(String theEvent) throws SAPIException
	{
		if (serverAddress == null)
			throw new SAPIException("You must connect to the server before you can" +
				" post an event.");
		// post the event
		appendPostString(NEES_EVENT_SOURCE_NAME,theEvent);	
		lastEvent = theEvent;
	}

	/**
	 * Fetch last event posted
	 * 
	 * @return (String) the event
	 */	
	public String getLastEvent()
	{
		return lastEvent;
	}
	
	/**
	 * This utility method parses a string describing the unit types of channels into
	 * a Properties object that has a key of each channel. In that Porperties object
	 * the value of the key (channel) is the unit type for that channel. The
	 * description of channel-names and their unit-types are represented as a
	 * string. The sting is a tab or comma seperated list of channel-name, 
	 * unit-type pairs. The channel-name and the unit-type are separated by an
	 * equal sign (=). On both the channel-name and the unit-type, leading and
	 * trailing blank are stripped, which means that there can be blanks before
	 * or after either the channelName or the unit-type. The only whitespace within
	 * a channel-name or a unit-type will be a blank. Within the channel-name or the
	 * unit-type multiple blank sequences will the taken literally. So,for example,
	 * the units of three channels (Ch1, Channel2, and Channel 3) might look
	 * like this: 
	 *    Ch1 = meters    Channel2 = metric tons     Channel 3 = milliparsec
	 * (where the large gaps are tabs, e.g. \t), or this:
	 *    Ch1 = meters, Channel2 = metric tons, Channel 3 = milliparsec
	 * 
	 * @param units the string describing the channel unit types
	 * @return the Properties object containing each channel as a key
	 *         with the unit type as it's key value.
	 * @throws IllegalArgumentException
	 */
	public static Properties parseUnitsString(String units) 
		throws IllegalArgumentException
	{
		Properties ret = new Properties();
		//get seperate key=value strings
		if ((units.indexOf(",") > -1) || (units.indexOf("\t") > -1))
		{
			// parse out multiple pairs
			String token;
			StringTokenizer st = new StringTokenizer(units,",\t");
			while (st.hasMoreTokens())
			{
				token = st.nextToken();
				ret = addOneUnit(ret,token);
			}
		}
		else if (units.indexOf("=") > -1) // just a single unit?
			ret = addOneUnit(ret,units);
		else 
			throw new IllegalArgumentException("Units description is illformed: " + units);
		return ret;
	}

	/**
	 * This untility method suppports parseUnitsString. It accepts strings of the form:
	 * 		channel-name = unit-type
	 * and adds an entry into the Properties object that has the channel-name as the key
	 * and the unit-type as the value for that key.
	 * 
	 * @param ret
	 * @param token
	 * @return the Properties object
	 * @throws IllegalArgumentException
	 * 
	 * @see #parseUnitsString
	 */
	public static Properties addOneUnit(Properties ret, String token)
		throws IllegalArgumentException
	{
		// strip off leading and trailing blanks
		while (token.charAt(0) == ' ') token = token.substring(1);
		while (token.charAt(token.length()-1) == ' ')
			token = token.substring(0,token.length()-1);
		int pos = token.indexOf("=");
		if (pos == -1)
			throw new IllegalArgumentException(
				"Unit description must be 'channel-name=unit-type'");

		String key = token.substring(0,pos);
		// strip off leading and trailing blanks
		while ((key.length() > 1) && (key.charAt(0) == ' '))
			key = key.substring(1);
		while ((key.length() > 1) && (key.charAt(key.length()-1) == ' '))
			key = key.substring(0,key.length()-1);
		
		if (key.length() == 0)
			throw new IllegalArgumentException("blank channel name");
		
		String value = token.substring(pos+1,token.length());		
		// strip off leading and trailing blanks
		while ((value.length() > 1) && (value.charAt(0) == ' '))
			value = value.substring(1);
		while ((value.length() > 1) && (value.charAt(value.length()-1) == ' '))
			value = value.substring(0,value.length()-1);

		if (value.length() == 0)
			throw new IllegalArgumentException("blank unit type");
		
		ret.put(key,value);
		
		return ret;
	}

	private void appendPostString(String sourceName, String posted) throws SAPIException
	{
		if (serverAddress == null)
			throw new SAPIException("Internal error: server address is not assigned.");

		Source s;
		s = new Source(100,"append",400);
		try { // first attempt
			s.OpenRBNBConnection(serverAddress,NEES_UNITS_SOURCE_NAME);
		}
		catch (SAPIException e)
		{
			// second attempt
			s = new Source(100,"append",400);
			s.OpenRBNBConnection(serverAddress,NEES_UNITS_SOURCE_NAME);
		}
		ChannelMap cMap = new ChannelMap();
		int index = cMap.Add(GetClientName());
		cMap.PutMime(index,"text/plain");
		cMap.PutTimeAuto("timeofday");
		cMap.PutDataAsString(index,posted);
		s.Flush(cMap);
		s.Detach();
	}

	
	public Source getSapiSource() {
		return this.source;
	}
	
// ------------------ The methods of Source ------------------ 
// to emulate the extension to Source (this is needed because Source is final)
	public void ClearCache () throws SAPIException {source.ClearCache();}
	public ChannelMap Delete (ChannelMap toDelete) throws SAPIException 
		{return source.Delete(toDelete);}
	public ChannelMap Delete (ChannelMap toDelete, ChannelMap result)
		throws SAPIException
		{return source.Delete(toDelete,result);} 
	public void Detach () {source.Detach();}
	public int Flush (ChannelMap ch) throws SAPIException {return source.Flush(ch);}
	public int Flush (ChannelMap ch, boolean doSynch) throws SAPIException 
		{return source.Flush(ch,doSynch);}
	public void Register (ChannelMap cm) throws SAPIException {source.Register(cm);}

// ------------------- The methods of Client (which Source extends)
	public void CloseRBNBConnection() {source.CloseRBNBConnection();}
	public String GetClientName () {return source.GetClientName();}
	public String GetServerName () {return source.GetServerName();}

	public void OpenRBNBConnection () throws SAPIException
	{
		this.serverAddress = "localhost:3333";
		source.OpenRBNBConnection();}
	public void OpenRBNBConnection (String serverAddress, String clientName)
		throws SAPIException
	{
		this.serverAddress = serverAddress;
		source.OpenRBNBConnection(serverAddress, clientName);}
	public void OpenRBNBConnection 
		(String serverAddress, String clientName, String userName, String password)
		throws SAPIException
	{
		this.serverAddress = serverAddress;
		source.OpenRBNBConnection(serverAddress, clientName, userName, password); }
	public void SetRingBuffer(int cache, String mode, int archive)
		throws SAPIException
		{source.SetRingBuffer(cache, mode, archive);}
	public boolean VerifyConnection () {return source.VerifyConnection();}
}
