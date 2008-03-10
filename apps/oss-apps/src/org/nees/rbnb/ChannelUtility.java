/*
 * Created on May 12, 2005
 */
package org.nees.rbnb;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.TimeZone;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.ChannelTree;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Sink;
import com.rbnb.sapi.Source;

/**
 * This is a set of static methods for doing verious things with RBNB channels, and with
 * the specialized NEES RBNB channel suport. See the classes listed below for exampels of
 * usage.
 * 
 * @author Terry E. Weymouth
 * @author Jason P. Hanley
 *
 * @see GrabDataMultipleSink
 * @see RbnbToFile 
 */
public class ChannelUtility {

	static ChannelUtility cover = new ChannelUtility();
	
    private static final SimpleDateFormat T_FORMAT = new SimpleDateFormat("yyyy-MM-dd:HH:mm:ss.SSS");
    private static final TimeZone TZ = TimeZone.getTimeZone("GMT");
    static
    {
        T_FORMAT.setTimeZone(TZ);
    }

    public static String niceTime(double dtime)
    {
        return niceTime((long)(dtime * 1000.0));
    }
  
    public static String niceTime(long time)
    {
        return T_FORMAT.format(new Date(time));
    }
    
	public static Vector getAllSourcesAsVector(String server, boolean includeHidden)
			throws SAPIException
	{
		Vector paths = new Vector();
		Sink sink=new Sink();
		// Create a sink and connect:
		sink.OpenRBNBConnection(server,"_GetChannels");
		
		// get all the channel paths that match the pattern
		ChannelMap sMap = new ChannelMap();
		sink.RequestRegistration();		
		sMap = sink.Fetch(-1,sMap);
		ChannelTree tree = ChannelTree.createFromChannelMap(sMap);
		sink.CloseRBNBConnection();
		sink = null;

		Iterator nodes = tree.iterator();
		while (nodes.hasNext())
		{
			ChannelTree.Node n = (ChannelTree.Node)nodes.next();
			// System.out.println("Checking " + n.getFullName() + ";" + n.getName());
			if (!includeHidden && n.getFullName().startsWith("_")) continue;
			if (n.getType() != ChannelTree.CHANNEL) continue;
			String name = n.getFullName();
			boolean isSource = false;
			ChannelTree.Node upNode = n.getParent();

			while ((!isSource) || (upNode != null))
			{
				if (upNode.getType() == ChannelTree.SOURCE) isSource = true;
				upNode = upNode.getParent();
			}
			if (isSource)
			{
				NodeCover node = cover.new NodeCover(n);
				if (!paths.contains(node)) paths.add(node);
			}
		}
		
		return paths;	
	}

	/**
	 * Transparently adds strings of the complete channel path for those Source
	 * channels who's names match a pattern string. This method uses a seperate
	 * sink channel for the requests for channel names and closes that sink whe
	 * finished. Matching channel path string that are already in the list are
	 * not added.
	 *  
 	 * @param server (String) is the host-name:port of the server
	 * @param includeHidden (boolean) is true if you want the normally hidden
	 *        channels
	 * @param channelPathPattern (String) is the "perl-like" regular expression
	 *        for the channels to match; for example "a.*" matches all channel
	 *        names starting with "a" and "..test" matches all six character
	 *        channel names ending with "test".			
	 * @param paths (Vector) the list of channel names to which the matching
	 *        channel names are added. Pass in an empty Vector if there is no
	 *        prior list. The vector is undated in place.
	 * 
	 * @return (Vector) the update, original, list of channels with the added
	 *         matching channels, returned for convienance.
	 * 
	 * @see appendChannelListFromString
	 * 
	 * @throws SAPIException
	 * 
	 */
	public static Vector appendChannelListFromPattern(String server, boolean includeHidden,
		String channelPathPattern, Vector paths) throws SAPIException
	{
		Sink sink=new Sink();
		// Create a sink and connect:
		sink.OpenRBNBConnection(server,"_GetChannels");
		
		// get all the channel paths that match the pattern
		ChannelMap sMap = new ChannelMap();
		sink.RequestRegistration();		
		sMap = sink.Fetch(-1,sMap);
		ChannelTree tree = ChannelTree.createFromChannelMap(sMap);
		sink.CloseRBNBConnection();
		sink = null;
		
		Pattern p = Pattern.compile(channelPathPattern);
		// for each channel path, check match, collect matches...
		
		Iterator nodes = tree.iterator();
		while (nodes.hasNext())
		{
			ChannelTree.Node n = (ChannelTree.Node)nodes.next();
			// System.out.println("Checking " + n.getFullName() + ";" + n.getName());
			if (!includeHidden && n.getFullName().startsWith("_")) continue;
			if (n.getType() != ChannelTree.CHANNEL) continue;
			String name = n.getFullName();
			Matcher m = p.matcher(name);
			if (m.matches())
			{
				boolean isSource = false;
				ChannelTree.Node upNode = n.getParent();
				while ((!isSource) || (upNode != null))
				{
					if (upNode.getType() == ChannelTree.SOURCE) isSource = true;
					upNode = upNode.getParent();
				}
				if (isSource)
				{
					NodeCover node = cover.new NodeCover(n);
					if (!paths.contains(node)) paths.add(node);
				}
			}					
		}
		
		return paths;
	} // appendChannelListFromPattern

	/**
	 * Transparently adds to a the complete channel path for those Source
	 * channels who's names match a pattern string. This method uses a seperate
	 * sink channel for the requests for channel names and closes that sink whe
	 * finished. Matching channel path string that are already in the list are
	 * not added.
	 *  
	 * @param server (String) is the host-name:port of the server
	 * @param includeHidden (boolean) is true if you want the normally hidden
	 *        channels
	 * @param channelPathListString (String) a comma seperated list of channel
	 *        path names to consider; only those names that are actual channels
	 *        on this server are added to the list; white space before and
	 *        after each name in the list is ignored. Use appendChannelListFromPattern
	 *        to match channels with path names that have leading or trailing white space.
	 * @param paths (Vector) the list of channel names to which the matching
	 *        channel names are added. Pass in an empty Vector if there is no
	 *        prior list. The vector is undated in place.
	 * 
	 * @return (Vector) the update, original, list of channels with the added
	 *         matching channels, returned for convienance.
	 * 
	 * @throws SAPIException
	 * 
	 * @see appendChannelListFromString
	 * 
	 */
	public static Vector appendChannelListFromString(String server, boolean includeHidden,
		String channelPathListString, Vector paths) throws SAPIException
	{
		Sink sink=new Sink();
		StringTokenizer st = new StringTokenizer(channelPathListString,","); 

		// Create a sink and connect:
		sink.OpenRBNBConnection(server,"_GetChannels");
			
		// get all the channel paths that match the pattern
		ChannelMap sMap = new ChannelMap();
		sink.RequestRegistration();		
		sMap = sink.Fetch(-1,sMap);
		ChannelTree tree = ChannelTree.createFromChannelMap(sMap);
		sink.CloseRBNBConnection();
		sink = null;

		while (st.hasMoreTokens()) {
			String path = st.nextToken().trim();							

			ChannelTree.Node n = tree.findNode(path);

			if (n == null) continue;
			if (n.getType() != ChannelTree.CHANNEL) continue;

			String name = n.getFullName();
			boolean isSource = false;
			ChannelTree.Node upNode = n.getParent();

			while ((!isSource) || (upNode != null))
			{
				if (upNode.getType() == ChannelTree.SOURCE) isSource = true;
				upNode = upNode.getParent();
			}
			if (isSource)
			{
				NodeCover node = cover.new NodeCover(n);
				if (!paths.contains(node)) paths.add(node);
			}
		} // while next token

		return paths;
	} // appendChannelListFromString

	/**
	 * Get a matching array of unit Strings for the channel path names in the
	 * imput array. A unit String of "" is returned where there is no matching
	 * channel with units. This method uses a seperate RBNB channel.
	 * 
	 * @param server (String) is the host-name:port of the server
	 * @param channelPathArray the channel path names for matching to those that
	 *        have unit Strings
	 * 
	 * @return an array of String with units for each channel in the imput array
	 *         or "" when none such exists.
	 * 
	 * @throws SAPIException
	 * 
	 * Note: copied and modified from Jason's method getUnits in
	 * org.nees.buffalo.rbnb.dataviewer.ChannelListPanel, Jason P. Hanley author
	 * version of Spring 2005
	 */	
	public static String[] getUnits(String server, String[] channelPathArray)
			throws SAPIException
	{
		//subscribe to all units channels
		Sink sink = new Sink();
		sink.OpenRBNBConnection(server,"_GetUnits");
		ChannelMap unitsChannelMap = new ChannelMap();
		unitsChannelMap.Add("_Units/*");
		
		//get the latest unit information
		sink.Request(unitsChannelMap, 0, 0, "newest");
		
		//fetch the unit channel data
		unitsChannelMap = sink.Fetch(100); // either it's there or it's not
		
		if (unitsChannelMap.GetIfFetchTimedOut())
		{
			System.out.println("Get Units failed!");
		}

		sink.CloseRBNBConnection();
		sink = null;
		
		HashMap units = new HashMap();

		String[] channels = unitsChannelMap.GetChannelList();
		for (int i=0; i<channels.length; i++) {
			String channelName = channels[i];
			String parent = channelName.substring(channelName.lastIndexOf("/")+1);
			int channelIndex = unitsChannelMap.GetIndex(channelName);
			String[] data = unitsChannelMap.GetDataAsString(channelIndex);
			String newestData = data[data.length-1];
			String[] channelTokens = newestData.split("\t|,");
			for (int j=0; j<channelTokens.length; j++) {
				String[] tokens = channelTokens[j].split("=");
				if (tokens.length == 2) {
					String channel = parent + "/" + tokens[0].trim();
					String unit = tokens[1].trim();
					units.put(channel, unit);
				} else {
					System.out.println("Invalid unit string: " + channelTokens[j] + ".");
				}
			}
		}
		
		String[] channelUnits = new String[channelPathArray.length];
		for (int i = 0; i < channelUnits.length; i++)
		{
			channelUnits[i] = (String)units.get(channelPathArray[i]);
			if (channelUnits[i] == null) channelUnits[i] = "";
		}
		return channelUnits;
	} // getUnits()

	/**
	 * Get the eariest time (in the standard RBNB representation) in the ring buffer
	 * for the given request path on the given RBNB server.
	 * @param server (String) in the form host:port (e.g. localhost:3333)
	 * @param requestPath (String) the channal path of the Soruce
	 * @return (double) the time
	 * @throws SAPIException
	 */
	public static double getEarliestTime(String server, String requestPath)
			throws SAPIException
	{
		// get earlest time in the ring buffer and check against endTime
		Sink sink=new Sink();
		sink.OpenRBNBConnection(server,"_GetTime");
			
		ChannelMap sMap = new ChannelMap();
		sMap.Add(requestPath);
		sink.Request(sMap, 0.0, 0.0, "oldest");
		ChannelMap rMap = sink.Fetch(-1,sMap);
		sink.CloseRBNBConnection();
		sink = null;
		int index = rMap.GetIndex(requestPath);
        if (index <0) 
            throw new SAPIException("For server = " + server 
                + "; channel not found: " + requestPath);
		return rMap.GetTimeStart(index);
	}

	/**
	 * Get the latest time (in the standard RBNB representation) in the ring buffer
	 * for the given request path on the given RBNB server.
	 * @param server (String) in the form host:port (e.g. localhost:3333)
	 * @param requestPath (String) the channal path of the Soruce
	 * @return (double) the time
	 * @throws SAPIException
	 */
	public static double getLatestTime(String server, String requestPath)
			throws SAPIException
	{
		// get earlest time in the ring buffer and check against endTime
		Sink sink=new Sink();
		sink.OpenRBNBConnection(server,"_GetTime");
			
		ChannelMap sMap = new ChannelMap();
		sMap.Add(requestPath);
		sink.Request(sMap, 0.0, 0.0, "newest");
		ChannelMap rMap = sink.Fetch(-1,sMap);
		sink.CloseRBNBConnection();
		sink = null;
		int index = rMap.GetIndex(requestPath);
        if (index <0) 
            throw new SAPIException("For server = " + server 
                + "; channel not found: " + requestPath);
		return rMap.GetTimeStart(index) + rMap.GetTimeDuration(index);
	}
	
    /**
     * Get the MIME types of the array of source paths on the server.
     * 
     * @param server the String of the server host:port, e.g. neestpm.sdsc.edu:3333
     * @param sourcePaths an String array of the Source paths 
     * @return an String array of the corresponding MINE types
     * @throws SAPIException
     */
	public static String[] getMimeTypes(String server, String[] sourcePaths)
			throws SAPIException
	{
		String[] mimeTypes = new String[sourcePaths.length];

		Sink sink = new Sink();
		sink.OpenRBNBConnection(server,"_GetMime");
		ChannelMap m = new ChannelMap();
		for (int i = 0; i < sourcePaths.length; i++)
			m.Add(sourcePaths[i]);
		sink.Subscribe(m);
		m = sink.Fetch(-1);
		ChannelTree tree = ChannelTree.createFromChannelMap(m);
		sink.CloseRBNBConnection();
		sink = null;
		for (int i = 0; i < mimeTypes.length; i++)
		{
			int ndx = m.GetIndex(sourcePaths[i]);
			if (ndx == -1) mimeTypes[i] = "";
			else mimeTypes[i] = m.GetMime(ndx);
			if (mimeTypes[i] == null) mimeTypes[i] = "";
		}
		return mimeTypes;
	}

    /**
     * Inner class to support equals behaivor for ChanneTree.Node, which is not
     * currently implemented (as of Jan 2005).
     */	
	public class NodeCover // for equals behaivor
	{
		ChannelTree.Node node;
		Class[] equalsArgs = {Object.class};
		
		NodeCover(ChannelTree.Node n)
		{
			node = n;
		}
		public String getName() { return node.getName(); }
		public String getFullName() { return node.getFullName(); }
		public boolean equals(Object o){
			if (!(o instanceof NodeCover)) return false;
			NodeCover c = (NodeCover)o;
			return node.getFullName().equals(c.node.getFullName());
		}
	} //NodeCover	

	/**
     * Test of MIME types
	 * @param args
	 */
    public static void main(String[] args)
	{
		String server = "neestpm.sdsc.edu:3333";
		Vector v = null;
		try {
			v = getAllSourcesAsVector(server, false);
		} catch (SAPIException e) {
			System.out.println("Exception in get source. Server = " + server);
			return;
		}
		if (v == null)
		{
			System.out.println("Null. No Sources for server = " + server);
			return;
		}
		if (v.size() == 0)
		{
			System.out.println("Empty. No Sources for server = " + server);
			return;
		}
		String[] s = new String[v.size()];
		for (int i = 0; i < s.length; i++)
		{
			s[i] = ((NodeCover)v.elementAt(i)).getFullName();
		}
		String[] m = null;
		try {
			m = getMimeTypes(server, s);
		} catch (SAPIException e1) {
			e1.printStackTrace();
			System.out.println("Exception in get mime types. Server = " + server);
			return;
		}
		for (int i = 0; i < m.length; i++)
		{
			System.out.println(s[i] + ":" + m[i]);
		}
	}
    
    /**
     * Parse a string of metadata values into a Properties table. The String
     * representation is a comma seperated list of "name=value" pairs. A name
     * and value can contain any character other then "," and "=". 
     * 
     * @param metadataString a comma seperated list of name-value pairs, e.g.
     *      "units=m,name=DAQ/25,type=flaot64,author=Terry E. Weymouth"
     * @return Properties of the name-value pairs
     * 
     * @see packMetadata
     */
    public static Properties parseMetadata(String metadataString)
    {
        Properties results = new Properties();
        
        String[] part = metadataString.split(",");
        String key, value, exp[];
        
        for (int i = 0; i < part.length; i ++)
        {
            exp = part[i].trim().split("=");
            key = exp[0].trim();
            value = exp[1].trim();
            results.setProperty(key,value);
        }
        
        return results;
    }

    /**
     * Put a Properties table into a metadata string. The String
     * representation is a comma seperated list of "name=value" pairs. A name
     * and value can contain any character other then "," and "=". 
     * 
     * @param prop the Properies table of name-value pairs
     * @return metadata String a comma seperated list of name-value pairs, e.g.
     *      "units=m,name=DAQ/25,type=flaot64,author=Terry E. Weymouth"
     * 
     * @see parseMetadata
     */
    public static String packMetadata(Properties prop)
    {
        String results = "";
        
        Enumeration keys = prop.keys();
        String key;
        String value;
        
        if (keys.hasMoreElements())
        {
            key = (String)keys.nextElement();
            value = (String)prop.getProperty(key);
            results = key + "=" + value;

            while(keys.hasMoreElements())
            {
                key = (String)keys.nextElement();
                value = (String)prop.getProperty(key);
                results += "," + key + "=" + value;
            }
        }
        
        return results;
    }
    
    /**
     * Send the metadata represented by the Properties table, p, on the channel
     * denoted by channelName, of the given source. 
     * 
     * NOTE: the source must be conneted. It is a good idea to post the metadata
     * before sending any data. This method uses the RBNB methods PutUserInfo
     * (of ChannelMap) and Register (of Source).
     * 
     * @param source the target Source
     * @param channelName the channel path to which this metadata applies
     * @param p Properties of name-value pairs that represent the metadata (see
     *  packMetadata for the representations of metadata).
     * 
     * @throws SAPIException
     * 
     * @see packMetadata
     */
    public static void sendMetadata
            (Source source, String channelName, Properties p)
        throws SAPIException
    {
        if (!source.VerifyConnection())
            throw new SAPIException("sendMetadata: Source is not connected.");

        String metadata = packMetadata(p);

        ChannelMap cm = new ChannelMap();
        int index = cm.Add(channelName);
        
        cm.PutMime(index,"text/xml");
        cm.PutUserInfo(index,metadata);
        source.Register(cm);
    }

    /**
     * Get the metadata from a source channel (specified as the full channle path,
     * e.g. DaqFeed/2-4). The metadata is returned as a Properties table. See
     * packMetadata for the formats of the metadata.
     * 
     * @param server the server (e.g. neestpm.sdsc.edu:3333)
     * @param channelPath 
     * @return Properteis tabel of the name-value pairs
     * 
     * @throws SAPIException
     * 
     * @see packMetadata
     */
    public static Properties getMetadata(String server, String channelPath)
        throws SAPIException
    {
        String metadata = "";

        Sink sink = new Sink();
        sink.OpenRBNBConnection(server,"_GetMetadata");

        ChannelMap request = new ChannelMap();
        request.Add(channelPath);
        sink.RequestRegistration(request);
        ChannelMap cm = sink.Fetch(100);
        
        if (cm.GetIfFetchTimedOut())
            throw new SAPIException("Fetch of metadata timed out");
        
        int index = cm.GetIndex(channelPath);
        if (index != 0) throw new SAPIException("No metadata for channel");
        metadata = cm.GetUserInfo(index);

        sink.CloseRBNBConnection();

        return parseMetadata(metadata);
    }
}
