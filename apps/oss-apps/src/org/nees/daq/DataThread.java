/*! 
@file DataThread.java
@date May 8, 2003
@author Paul Hubbard
@brief Handles data feed from DAQ to turbine
@version CVS:$Revision$
*/
package org.nees.daq;

import java.net.*;
import java.io.*;
import java.util.*;
import com.rbnb.sapi.*;

/*!
 * @brief For a java thread to monitor for DAQ data.
 * 
 * Creates a runable for a java thread to monitor and relay data. Only relays data to
 * those listeners that have been added. This thread sorts the data by
 * channel name and that listeners are added by channel name. So, a listener gets only
 * the data for that channel.
 * 
 * @see DaqListener
 * @see #addListener
 */
public class DataThread implements Runnable{

	private BufferedReader rd;
	private Thread theThread;
	private boolean running = false;
	
	private DaqListener[] listenerList = new DaqListener[0];
	
	private static final String EVENT_TOKEN = "event";
	
    /*! 
        @brief Just connect input stream to socket
        @note assumes TCP socket already connected
        @note Constructor - tell JVM we are discardable on exit
    */    
    public DataThread(Socket socket)
    	throws IOException
    {
        try {
            socket.setKeepAlive(true);
            System.out.println("Enabled TCP keepalive on socket");
        }
        catch (SocketException se) {
            System.out.println("Exception while enabling TCP keepalive on socket!");
            throw se;
        }
		rd = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }
    
    /*!
        @brief Start the thread up
     */
    public void start() 
    {
    	if (theThread != null) return;
    	theThread = new Thread(this);
    	running = true;
    	theThread.start();
    }
    
    //! Stop the thread, by setting boolean flag
    public void stop()
    {
		running = false;    	
    }

    /*!
        @brief Main method
        
        While thre are no errors and stop has not been called, loop "forever" to
        get and parse data from the DAQ. Convert the timestamps to our internal format,
        tokenize the data into channel/value, and call those listeners with the channel
        name that corresponds to the channel.
     
        @note Assumes implementation of postData on the DaqListerner is keeping up
        no double or circular buffering. Might need to be modified.
		@note All DAQ channel data is assumed to be floating point numbers.
    */
    public void run () {

		String fromDAQ;
		StringTokenizer st;
		try {
			while(((fromDAQ = rd.readLine()) != null) && running) {
				// Want to split out the timestamp, all up to tab char
				st = new StringTokenizer(fromDAQ, "\t\n");
				
				if (!st.hasMoreTokens()) {
					System.err.println("Error parsing data line.");
					continue;
				}
				
				String firstToken = st.nextToken();				
				if (firstToken.compareToIgnoreCase(EVENT_TOKEN) == 0) {
					parseEvent(st);
				} else {
					parseData(firstToken, st);
				}
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAPIException e) {
			e.printStackTrace();
		}
		finally
		{
			theThread = null;
			running = false;
		}
   }

	private void parseEvent(StringTokenizer st) throws SAPIException {
		ISOtoRbnbTime timestamp;
		double time;
		
		String tsString = st.nextToken();		
		timestamp = new ISOtoRbnbTime(tsString);
		if (!timestamp.is_valid) {
			System.err.println("Warning: timestamp not valid: " + tsString);
			return;
		}
		time = timestamp.getValue();

		postTimestamp(time);
		
		String type = st.nextToken();
		String content = null;
		if (st.hasMoreTokens()) {
			content = st.nextToken();
		}
		
		postEvent(type, content);
	}
   
	private void parseData(String tsString, StringTokenizer st) throws SAPIException {
		ISOtoRbnbTime timestamp;
		double time;
		String channelName;
		double data;		

		timestamp = new ISOtoRbnbTime(tsString);
		if (!timestamp.is_valid) {
			System.err.println("Warning: timestamp not valid: " + tsString);
			return;
		}
		time = timestamp.getValue();

		postTimestamp(time);

		while (st.hasMoreTokens()) {
			channelName = st.nextToken();
			data = Double.parseDouble(st.nextToken());
			postData(channelName,data);
		}

		endTick();  	 
	}
    
	/**
	 * @param time
	 */
	private void postTimestamp(double time) throws SAPIException
	{
		for (int i = 0; i < listenerList.length; i++)
		{
			listenerList[i].postTimestamp(time);
		}
	}

	private void postData(String channelName, double data) throws SAPIException
	{
		for (int i = 0; i < listenerList.length; i++)
		{
			listenerList[i].postData(channelName,data);
		}
	}
	
	private void postEvent(String type, String content) throws SAPIException
	{
		for (int i = 0; i < listenerList.length; i++)
		{
			listenerList[i].postEvent(type, content);
		}
	}	

	private void endTick() throws SAPIException
	{
		for (int i = 0; i < listenerList.length; i++)
		{
			listenerList[i].endTick();
		}
	}

	/*!
        @brief Add listener to the listener list
		
		Add a DaqListener to the list of listeners. The listener at four palces in
		the data processing loop (see the run() method): registerChannel(),
		postTimestamp(), postData(), and abort(), for details see the DaqListener
		Interface.
				
		@note All DAQ channel data is assumed to be floating point numbers.
	
        @param l DaqListener
        @bug this will overwrite a previous listener with the same channel name!
        @see DaqListener
     */
	public void addListener(DaqListener l)
	{
		// the list of listeners is implemented as an array. Listeners are
		// added infrequently and removed even less frequently, the list of
		// listeners is accessed often. Note: duplicates are not allowed.
		
		boolean found = false;
		
		DaqListener[] newList = new DaqListener[listenerList.length+1];
		for (int i = 0; i< listenerList.length; i++)
		{
			if (listenerList[i].equals(l)) found = true;
			newList[i] = listenerList[i];
		}
		if (!found)
		{
			newList[listenerList.length] = l;
			listenerList = newList;
		}
	}
	
    /*!
        @brief Remove listener from the channel list
        
        Remove the DaqListener for the named channel from the channel list.
        
        @param name Channel name
        @param l DaqListener
        @note I need to document this better!
        @see DaqListener
     */
	public void removeListener(DaqListener l)
	{
		// the list of listeners is implemented as an array. Listeners are
		// added infrequently and removed even less frequently, the list of
		// listeners is accessed often. Note: duplicates are not allowed

		boolean found = false;
		int mark = 0;
		DaqListener[] newList = new DaqListener[listenerList.length-1];
		for (int i = 0; i < listenerList.length; i++)
		{
			if (listenerList[i].equals(l)) found = true;
			else
			{
				if (mark < newList.length)
					newList[mark] = listenerList[i];
				mark++;
			}
		}
		if (!found)
		{
			listenerList = newList;
		}
	}
	
	public boolean isRunning()
	{
		return running;
	}
}
