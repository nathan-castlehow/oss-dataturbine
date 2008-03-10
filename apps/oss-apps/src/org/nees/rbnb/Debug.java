/*
 * Created on Jan 5, 2005
 * To supply a gobal debug option.
 */
package org.nees.rbnb;

/**
 * Privides a global debug flag and a flag guarded print trace
 *  @author Terry Weymouth
 *
 */
public class Debug {

	/**
	 * Debug flag. True to turn on debug printing; false to turn it off
	 * defauts to true
	 */
	public static boolean debug = true;
	
	/**
	 * To print a message that is guarded by the debug flag 
	 * @param message
	 */
	public static void debugPrint(String message)
	{
		if (debug) System.err.println(message);
	}
}
