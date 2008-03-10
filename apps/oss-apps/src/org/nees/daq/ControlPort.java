/*!
@file ControlPort.java
@brief DAQ interface, this portion is the 'control channel' TCP port
@author Paul Hubbard 
@author Modified by Terry E Weymouth March 2004
@date Thu May 08 2003.
@version CVS:$Revision: 153 $
*/

package org.nees.daq;

import java.net.*;
import java.io.*;
import java.lang.String.*;
import java.util.StringTokenizer;
import java.util.Vector;

/*!
    @brief Mediates the connection between DAQ and any internal requester
    
    This class presents a DAQ control port with support to get the DAQ channel list.
    One uses this to connect to the controller, get the list of availabe channels,
    and subscribe or unsubscribe to a specific channel, by name. THe data for
    the subscribed channels is delivered on a seperate socket (see DaqToRbnb)
    
    @see DaqToRbnb
 */
public class ControlPort {
    private BufferedReader    rd;
    private BufferedWriter    wr;
    private boolean connected;

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

		System.out.println("Created DAQ Control Port");
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
			throw new IOException("DAQ Not connected.");
		}
        
		System.out.println("Requesting DAQ unit list");
		wr.write(list_command + '\n');
		wr.flush();

		raw_string = rd.readLine();

		System.out.println("For units, got: >>" + raw_string + "<<");

		// Have the comma-delimted list, now need to parse same
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
	    @brief Routine to query and parse the list of upperbounds per channel. 
	    @note DAQ comms must be open and ready.
	    @retval ChannelList List of channels, array of Strings
     */
    public String[] getUpperbounds()
	    throws IOException
    {
	    String      list_command = "list-upperbounds";
	    String      delimiter = ",\t";
	    String      raw_string = "";
	    String[]    result;
	    Vector		ubs = new Vector();
        
	    // No DAQ, no can do
	    if(!connected) {
		    throw new IOException("DAQ Not connected.");
	    }
    
	    System.out.println("Requesting DAQ upperbound list");
	    wr.write(list_command + '\n');
	    wr.flush();

	    raw_string = rd.readLine();

	    System.out.println("For upperbounds, got: >>" + raw_string + "<<");

	    // Have the comma-delimted list, now need to parse same
	    StringTokenizer tokens = new StringTokenizer(raw_string, delimiter);
	
	    String tok;
	    while(tokens.hasMoreTokens()) {
		    tok = tokens.nextToken();
		    //strip leading and trailing blanks
		    while ((tok.length() > 1) && (tok.startsWith(" ")))
	    		tok = tok.substring(1);
		    while ((tok.length() > 1) && (tok.endsWith(" ")))
		    	tok = tok.substring(0,tok.length()-1);
		    // Add channel to list only when it is a number
                    try {
                        Float f = Float.valueOf(tok.trim());	
		        ubs.addElement(tok);
                    } catch (NumberFormatException nfe) {
                        System.out.println("Invalid upperbound value.");
                    }
	    }
	    // convert channel list to array
	    result = new String[ubs.size()];
	
	    for (int i = 0; i < ubs.size(); i++)
	    	result[i] = (String) ubs.elementAt(i);
		
	    return(result);
    }

    /*!
        @brief Routine to query and parse the list of lowerbounds per channel. 
        @note DAQ comms must be open and ready.
        @retval ChannelList List of channels, array of Strings
     */
    public String[] getLowerbounds()
    	throws IOException
    {
    	String      list_command = "list-lowerbounds";
    	String      delimiter = ",\t";
    	String      raw_string = "";
    	String[]    result;
    	Vector		lbs = new Vector();
    	
    	// No DAQ, no can do
    	if(!connected) {
    		throw new IOException("DAQ Not connected.");
    	}

    	System.out.println("Requesting DAQ lowerbound list");
    	wr.write(list_command + '\n');
    	wr.flush();

    	raw_string = rd.readLine();

    	System.out.println("For lowerbounds, got: >>" + raw_string + "<<");

    	// Have the comma-delimted list, now need to parse same
    	StringTokenizer tokens = new StringTokenizer(raw_string, delimiter);

    	String tok;
    	while(tokens.hasMoreTokens()) {
    		tok = tokens.nextToken();
    		//strip leading and trailing blanks
    		while ((tok.length() > 1) && (tok.startsWith(" ")))
    			tok = tok.substring(1);
    		while ((tok.length() > 1) && (tok.endsWith(" ")))
    			tok = tok.substring(0,tok.length()-1);

                // Add channel to list only when it is a number
                try {
                    Float f = Float.valueOf(tok.trim());
                    lbs.addElement(tok);
                } catch (NumberFormatException nfe) {
                    System.out.println("Invalid lowerbound value.");
                }
    	}
    	// convert channel list to array
    	result = new String[lbs.size()];

    	for (int i = 0; i < lbs.size(); i++)
    		result[i] = (String) lbs.elementAt(i);
    	
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
        String[]    result = new String[0];
        Vector		channels = new Vector();
            
        // No DAQ, no can do
        if(!connected) {
            throw new IOException("DAQ Not connected.");
        }
        
		System.out.println("Requesting DAQ channel list");
		wr.write(list_command + '\n');
		wr.flush();

		raw_list = rd.readLine();

		System.out.println("For Channels, got: " + raw_list);

		// Have the comma-delimted list, now need to parse same
		StringTokenizer tokens = new StringTokenizer(raw_list, delimiter);
		
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
        
    /*!
        @brief Internal routine to subscribe or unsubscribe from a DAQ channel aka port
        @param channel Channel name
        @param subscribe Boolean toggle - if true, subscribe, if false unsubscribe
        @note Idempotent - OK to subscribe/unsub more than once
     
     */
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
			throw new IOException("DAQ not connected");
		}

		//System.out.println(stdout[cmd_idx] + " channel " + channel);
		
		wr.write(commands[cmd_idx] + " " + channel + "\n");
		wr.flush();

		//System.out.println(stdout[cmd_idx] + "for -->"+ channel + "<-- sent.");
				
		String response = rd.readLine();
		
		//System.out.println("Response is: " + response);
		
		// check for correct response
		if(response!=null  &&  response.startsWith(responses[cmd_idx])) {
		    return;
		}else {
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
