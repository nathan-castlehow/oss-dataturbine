/*! 
@file DataThread.java
@date May 8, 2003
@author Paul Hubbard
@brief Handles data feed from DAQ to turbine
@version CVS:$Revision: 153 $
@note $HeadURL: file:///Users/hubbard/code/cleos-svn/cleos-rbnb-apps/trunk/src/edu/sdsc/cleos/DataThread.java $
@note $LastChangedRevision: 153 $
@author $LastChangedBy: ljmiller $
@date $LastChangedDate: 2007-09-24 13:10:37 -0700 (Mon, 24 Sep 2007) $
@brief For a java thread to monitor for DAQ data.
Creates a runable for a java thread to monitor and relay data. Only relays data to
those listeners that have been added. This thread sorts the data by
channel name and that listeners are added by channel name. So, a listener gets only
the data for that channel.
@see DaqListener
@see #addListener
*/
package edu.sdsc.cleos;

import java.net.*;
import java.io.*;
import java.util.*;
import com.rbnb.sapi.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.sdsc.cleos.DaqListener;
import edu.sdsc.cleos.ISOtoRbnbTime;

public class DataThread implements Runnable{

	private BufferedReader rd;
	private Thread theThread;
	private boolean running = false;
	
	private DaqListener[] listenerList = new DaqListener[0];
	
	private static final String EVENT_TOKEN = "event";
	static Log log = LogFactory.getLog(DataThread.class.getName());
	
    /*! @brief Just connect input stream to socket
        @note assumes TCP socket already connected
        @note Constructor - tell JVM we are discardable on exit */    
    public DataThread(Socket socket)
    	throws IOException
    {
        try {
            socket.setKeepAlive(true);
            log.info("Enabled TCP keepalive on DataThread socket");
        }
        catch (SocketException se) {
            log.warn("Exception while enabling TCP keepalive on DataThread socket!");
            throw se;
        }
		rd = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }
    
    /*! @brief Start the thread up */
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

    /*! @brief Main method
        
        While thre are no errors and stop has not been called, loop "forever" to
        get and parse data from the DAQ. Convert the timestamps to our internal format,
        tokenize the data into channel/value, and call those listeners with the channel
        name that corresponds to the channel.
     
        @note Assumes implementation of postData on the DaqListerner is keeping up
        no double or circular buffering. Might need to be modified.
		@note All DAQ channel data is assumed to be floating point numbers. */
    public void run () {

		String fromDAQ;
		StringTokenizer st;
		try {
			while(((fromDAQ = rd.readLine()) != null) && running) {
				// Want to split out the timestamp, all up to tab char
				st = new StringTokenizer(fromDAQ, "\t\n");
				//log.debug("fromDAQ: " + fromDAQ);
				if (!st.hasMoreTokens()) {
					log.error("Error parsing data line.");
					continue;
				}
				
				String firstToken = st.nextToken();				
				if (firstToken.compareToIgnoreCase(EVENT_TOKEN) == 0) {
					parseEvent(st);
				} else {
					//log.debug("firstToken: " + firstToken);
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
   } // run ()

	private void parseEvent(StringTokenizer st) throws SAPIException {
		ISOtoRbnbTime timestamp;
		double time;
		
		String tsString = st.nextToken();		
		timestamp = new ISOtoRbnbTime(tsString);
		if (!timestamp.is_valid) {
			log.warn("timestamp not valid: " + tsString);
			return;
		}
		time = timestamp.getValue();

		postTimestamp(time * 1000.);
		log.debug("posted from datathread: " + ISOtoRbnbTime.formatDate((long)(time * 1000.)));
		
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
		String dataStringToken;

		//log.debug("Operating on tsString: " + tsString);
		timestamp = new ISOtoRbnbTime(tsString);
		if (!timestamp.is_valid) {
			log.warn("timestamp not valid: " + tsString);
			return;
		}
		time = timestamp.getValue();
		postTimestamp(time);
		// log.debug("datathread thinks it posted TIME: " + ISOtoRbnbTime.formatDate((long)(time*1000)));

		while (st.hasMoreTokens()) {
			channelName = st.nextToken();
			dataStringToken = st.nextToken();
			/*! @note special case in the isi nwp protocol is that the log channels have are typed as strings */
			/*! @todo call a string-specific post method */
			if(channelName.endsWith("LOG")) { // then this is a log channel of type string
				postString(channelName, dataStringToken);
			} else { // it's numeric data
			try {
				/*! @bug this gets the whole timestamp only when the fractional second is not .0000 why? fixme */
				data = Double.parseDouble(dataStringToken);
				postData(channelName,data);
			} catch (Throwable t) {
				//log.error("Bad data parse from string: " + dataStringToken);	
			}
		}
			
			
			
		} // while

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
	
	private void postString(String channelName, String stringData) throws SAPIException
	{
		for (int i = 0; i < listenerList.length; i++)
		{
			listenerList[i].postString(channelName, stringData);
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

	/*! @brief Add listener to the listener list
		
		Add a DaqListener to the list of listeners. The listener at four places in
		the data processing loop (see the run() method): registerChannel(),
		postTimestamp(), postData(), and abort(), for details see the DaqListener
		Interface.
				
		@note All DAQ channel data is assumed to be floating point numbers.
	
        @param l DaqListener
        @bug this will overwrite a previous listener with the same channel name!
        @see DaqListener */
	public void addListener(DaqListener l)
	{
		/*! @note the list of listeners is implemented as an array. Listeners are
		added infrequently and removed even less frequently, the list of
		listeners is accessed often. Note: duplicates are not allowed. */
		
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
	
    /*! @brief Remove listener from the channel list
        
        Remove the DaqListener for the named channel from the channel list.
        
        @param name Channel name
        @param l DaqListener
        @note I need to document this better!
        @see DaqListener */
	public void removeListener(DaqListener l)
	{
		/*! @note the list of listeners is implemented as an array. Listeners are
		added infrequently and removed even less frequently, the list of
		listeners is accessed often. Note: duplicates are not allowed */

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
