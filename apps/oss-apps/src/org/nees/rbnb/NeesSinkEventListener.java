/*
 * Created on Feb 1, 2005
 */
package org.nees.rbnb;

/**
 * An interface for a "call back" for events used by NeesSink.
 * 
 * @author Nees Development Team
 * 
 * @see NeesSink
 */
public interface NeesSinkEventListener {

	/**
	 * A method to process events as they arrive
	 * @param theEvent
	 */
	public void processNeesSinkEvent(String theEvent, double timestamp);
	
}
