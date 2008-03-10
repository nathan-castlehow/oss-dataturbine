/*!
@file ControlPort.java
@brief DAQ interface, this portion is the 'control channel' TCP port
@date Thu May 08 2003.
@version CVS:$Revision:3101 $
@note $HeadURL: file:///Users/hubbard/code/cleos-svn/cleos-rbnb-apps/trunk/src/edu/sdsc/cleos/ControlPort.java $
@note $LastChangedRevision: 153 $
@author $LastChangedBy: ljmiller $
@date $LastChangedDate: 2007-09-24 13:10:37 -0700 (Mon, 24 Sep 2007) $
@brief Mediates the connection between DAQ and any internal requester
This class presents a DAQ control port with support to get the DAQ channel list.
One uses this to connect to the controller, get the list of availabe channels,
and subscribe or unsubscribe to a specific channel, by name. THe data for
the subscribed channels is delivered on a seperate socket (see DaqToRbnb)
@see edu.sdsc.cleos.NwpToRbnb
*/

package edu.sdsc.cleos;

import java.net.*;
import java.io.*;
import java.lang.String.*;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*!
   
 */
public class ControlPort {
    private BufferedReader    rd;
    private BufferedWriter    wr;
    private boolean connected;
    static Log log = LogFactory.getLog(ControlPort.class.getName());

    /*! 
        @brief Constructor - get reader/writer from socket
        @param socket TCP socket to DAQ control port
        @note We assume the socket is in place before we are called
    */
    public ControlPort(Socket socket)
		throws UnknownHostException, IOException 
    {
		rd = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		wr = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		connected = true;

		log.info("Created NWP Control Port");
    }
    
    /*! @brief thread-safe wrapper to read from the daq control socket
     * @return the response from the daq as a string */
    public synchronized String readFromControl() throws IOException {
    	String retval = "";
    	retval = rd.readLine();
    	log.debug("Read \"" + retval + "\" from daq control");
    	return retval;
    }
    
    /*! @brief thread-safe wrapper to write to the daq control socket
     * @param a String to be written to the control socket */
    public synchronized void writeToControl(String commandToWrite) throws IOException {
    	wr.write(commandToWrite + '\n');
		wr.flush();
		log.debug("Wrote \"" + commandToWrite + "\" to daq control");
    }
    
    /*! @brief thread-safe wrapper to do an atomic command/response pair which
     * is intended for status queries */
    public synchronized String writeReadControl(String commandToWrite) throws IOException {
    	String retval = "";
    	wr.write(commandToWrite + '\n');
		wr.flush();
		retval = rd.readLine();
    	//log.debug("Wrote \"" + commandToWrite + "\" and Read \"" + retval + "\" from daq control");
    	return retval;
    }
    
    /*! @brief parse units from "get-channel-info" per channel and populate units
     *  @param channelList the list of channels for which to get units, which has the number of
     *   elements in the @return String array
     *  @note since the nwp doesn't report a list of units as spec'ed in the neesdaq protocol */
    public String[] getNwpUnits(String[] channelList) throws IOException {
    	String unitCmd = "get-channel-info ";
    	/*! @note the isi nwp command protocol has inconsistencies that need to be managed; this omits the last 2 log channels */
    	int listLengthAdjustment = 2;
    	
    	String[] retval = new String[channelList.length];
    	log.debug("Requesting NWP unit list");
    	String[] nwpResponse = null;
    	for(int i=0; i<channelList.length - listLengthAdjustment; i++) {
    		/*! @note this will produce a response of the form:
    		 * OK: Channel info: HMP45H,Vaisala HMP45A Humidity,Vaisala,HMP45A,B3310003,Relative Humidity,%RH */
    		nwpResponse = writeReadControl(unitCmd + channelList[i]).split(",");
    		retval[i] = nwpResponse[6];
    	}
    	
    	/*! @note the NWPLOG and GPSLOG channels are not formatted the same as the other channels */
    	retval[retval.length - listLengthAdjustment] = "string";
    	retval[retval.length - listLengthAdjustment + 1] = "string";
    	
    	return retval;
    }
    
    /*! @brief parse sensor make and measurement type from "get-channel-info" per channel and populate sensType and measType hashtables
     *  @param sensType hashtable for sensor types, keyed by channel label
     *  @param measType hashtable for measurement types, keyed by channel label */
    // todo
    public void getSensorMetadata(String[] channelList, Hashtable<String, String> sensType, Hashtable<String, String> measType) throws IOException {
    	String unitCmd = "get-channel-info ";
    	/*! @note the isi nwp command protocol has inconsistencies that need to be managed; this omits the last 2 log channels */
    	int listLengthAdjustment = 2;
    	
    	String[] nwpResponse = null;
    	for(int i=0; i<channelList.length - listLengthAdjustment; i++) {
    		/*! @note this will produce a response of the form:
    		 * OK: Channel info: HMP45H,Vaisala HMP45A Humidity,Vaisala,HMP45A,B3310003,Relative Humidity,%RH */
    		nwpResponse = writeReadControl(unitCmd + channelList[i]).split(",");
    		sensType.put(channelList[i], nwpResponse[2] + " " + nwpResponse[3]);
    		measType.put(channelList[i], nwpResponse[5]);	
    	}	
    }
    
	/*!
		@brief Routine to query and parse the list of units per channel. 
		@note DAQ comms must be open and ready.
		@retval ChannelList List of channels, array of Strings
	 */
	public String[] getUnits()
		throws IOException
	{
		String      list_command = "list-units";
		String      delimiter = ",\t";
		String      raw_string = "";
		String[]    result;
		Vector		units = new Vector();
            
		// No DAQ, no can do
		if(!connected) {
			throw new IOException("NWP Not connected.");
		}
		try {
			log.debug("Requesting NWP unit list");
			raw_string = writeReadControl(list_command + '\n');
		} catch(IOException ioe) {
			log.error("communicating with daq control channel" + ioe);
			ioe.printStackTrace();
		}

		StringTokenizer tokens = new StringTokenizer(raw_string, delimiter);
		
		String tok;
		while(tokens.hasMoreTokens()) {
			tok = tokens.nextToken();
			//strip leading and trailing blanks
			while ((tok.length() > 1) && (tok.startsWith(" ")))
				tok = tok.substring(1);
			while ((tok.length() > 1) && (tok.endsWith(" ")))
				tok = tok.substring(0,tok.length()-1);
			// Add channel to list
			units.addElement(tok);
		}
		// convert channel list to array
		result = new String[units.size()];
		
		for (int i = 0; i < units.size(); i++)
			result[i] = (String) units.elementAt(i);
			
		return(result);
	}

    /*!
        @brief Routine to query and parse the list of DAQ channels. 
        @note DAQ comms must be open and ready.
        @retval ChannelList List of channels, array of Strings
     */
    public String[] getChannels()
    	throws IOException
    {
        String      list_command = "list-channels";
        String      delimiter = ",";
        String      raw_list = "";
        String		processed_list = "";
        String[]    result = new String[0];
        Vector		channels = new Vector();
            
        // No DAQ, no can do
        if(!connected) {
            throw new IOException("NWP Not connected.");
        }

		try {
			log.debug("Requesting NWP channel list");
			raw_list = writeReadControl(list_command + '\n');
			/*! @note chop "OK: Channels: " off the front */
			processed_list = raw_list.split(":")[2].trim();
			/*! @note for neesdaq protocol, do processed = raw */
		} catch(IOException ioe) {
			log.error("Communicating with daq control channel" + ioe);
			ioe.printStackTrace();
		}
		
		// Have the comma-delimted list, now need to parse same
		StringTokenizer tokens = new StringTokenizer(processed_list, delimiter);
		
		String tok;
		while(tokens.hasMoreTokens()) {
			tok = tokens.nextToken();
			// Add channel to list
			channels.addElement(tok);
		}
		// convert channel list to array
		result = new String[channels.size()];
		
		for (int i = 0; i < channels.size(); i++)
			result[i] = (String) channels.elementAt(i);
			
		return(result);
    }
        
    /*! @brief Internal routine to subscribe or unsubscribe from a DAQ channel aka port
        @param channel Channel name
        @param subscribe Boolean toggle - if true, subscribe, if false unsubscribe
        @note Idempotent - OK to subscribe/unsub more than once */
    private void subUnsub(String channel, boolean subscribe)
    	throws IOException
    {
		String[]    commands = {"open-port", "close-port"};
		String[]    responses = {"Streaming", "Stopping"};
		String[]    stdout = {"Subscribing to", "Unsubscribing from"};
		int         cmd_idx;
		
		if(subscribe)
			cmd_idx = 0;
		else
			cmd_idx = 1;
		
		if(!connected) {
			throw new IOException("NWP not connected");
		}

		String response = "No Response";
		try{
			//log.debug(stdout[cmd_idx] + " channel " + channel);
			response = writeReadControl(commands[cmd_idx] + " " + channel + "\n");
		} catch(IOException ioe) {
			log.error("Communicating with daq control channel" + ioe);
			ioe.printStackTrace();
		}
		
		// check for correct response
		if(response!=null  &&  response.startsWith(responses[cmd_idx])) {
		    return;
		}else{
		    throw new IOException("Response Error on channel " + channel +
		                       " :" + response);
		}
    }

    /*! 
        @brief Subscribe to a DAQ channel.

        This method turns on the data flow from the specified DAQ channel.
        If the connection (to a socket) was not made, if the connection failes,
        if the attempt to write to the socket fails, or if the channel is not
        available, then an IOException will be thrown.
        
        @throws IOException 
        
        @param channel DAQ channel ID
    */
    public void subscribe(String channel)
    	throws IOException
    {
		subUnsub(channel, true);
    }

    /*! 
        @brief Unsubscribe from a previously subscribed DAQ channel.
        
        This method turns off data flow from the specified DAQ channel.
        If the connection (to a socket) was not made, if the connection failes,
        if the attempt to write to the socket fails, or if the channel is not
        available, then an IOException will be thrown.
        
        @throws IOExcption
        @param channel DAQ channel ID
    */
    public void unsubscribe(String channel)
    	throws IOException
    {
		subUnsub(channel, false);
    }
}
