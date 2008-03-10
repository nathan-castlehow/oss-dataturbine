/*
 * Created on Jan 31, 2005
 */

package org.nees.rbnb;

import com.rbnb.sapi.Sink;
import java.util.Properties;
import java.util.Vector;
import java.util.Enumeration;
import java.util.StringTokenizer;

/**
 * A companion to NeesSource. A wrapper on an RBNB sink, that monitors for the
 * units on NeesSource, as delever by the _units channel. For more detailed description
 * of the representation of units and the source see the description of NeesSource in this
 * same package.
 * 
 * @author Nees Developemtn Group
 * @see NeesSource
 */
public class NeesSink extends Sink{

	// TODO: Complete NeesSink

	private String itsSourceChannel;
	
	private double lastTimestamp = -1.0;
	private String lastEvent;
	
	private Vector eventListenerList = new Vector();
	private Vector metadataListenerList = new Vector();
	
	/**
	 * Create a NeesSink object (based on an RBNB Sink) with the default
	 * arguments.
	 */
	public NeesSink(String sourceChannel)
	{
		super();
		itsSourceChannel = sourceChannel;
	}
	
	/**
	 * Fetch (as a comma delimitted String) the latest units description.
	 * 
	 * @return (String) the units description as a comma seperated String
	 */
	public String getUnitsString()
	{
		return "";
	}
	
	/**
	 * Fetch (as a Properties object) the latest units description.
	 * 
	 * @return (java.util.Properties) the units description
	 */
	public Properties getUnitsProperties()
	{
		Properties ret = new Properties();
		
		try
		{
			ret = NeesSource.parseUnitsString(getUnitsString());
		}
		catch (IllegalArgumentException e)
		{
			// not sure what to do here - tew; Feb 6, 2005
		}

		return ret;
	}

	/**
	 * Fetch the latest posted event
	 * 
	 * @return
	 */
	public String getEvent()
	{
		return "";
	}
	
	/**
	 * Fetch the timestamp of the last units or event fetched.
	 * 
	 * @return (double) the timestamp
	 */	
	public double getLastTimestamp()
	{
		return lastTimestamp;		
	}

	/**
	 * Add a listener for events on this channel
	 * 
	 * @param listener is the object listening for the event
	 */
	public void addEventListener(NeesSinkEventListener listener)
	{
		eventListenerList.addElement(listener);
	}
	
	/**
	 * Remove a listener for events on this channel
	 * 
	 * @param listener is the object listening for the event
	 */
	public void removeEventListener(NeesSinkEventListener listener)
	{
		eventListenerList.remove(listener);
	}
	
	/**
	 * Add a listener for events on this channel
	 * 
	 * @param listener is the object listening for the event
	 */
	public void addMetadataListener(NeesSinkMetadataListener listener)
	{
		metadataListenerList.addElement(listener);
	}
	
	/**
	 * Remove a listener for events on this channel
	 * 
	 * @param listener is the object listening for the event
	 */
	public void removeMetadataListener(NeesSinkMetadataListener listener)
	{
		metadataListenerList.remove(listener);
	}
	
	/**
	 * Clear the list of listeners for events on this channel
	 * 
	 * @param listener is the object listening for the event
	 */
	public void clearActionListenerList()
	{
		metadataListenerList.clear();
	}
	
	private void reportEvent(String neesEvent, double timestamp)
	{
		
		NeesSinkEventListener l;
		
		if (eventListenerList.isEmpty()) return;

		Enumeration e = eventListenerList.elements();
		while (e.hasMoreElements())
		{
			l = (NeesSinkEventListener)e.nextElement();
			l.processNeesSinkEvent(neesEvent,timestamp);
		}
	}


}
