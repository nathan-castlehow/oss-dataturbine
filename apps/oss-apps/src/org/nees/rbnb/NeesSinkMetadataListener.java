/*
 * Created on Feb 1, 2005
 */
package org.nees.rbnb;

import org.w3c.dom.Document;

/**
 * An interface for a "call back" for metadata events; used by NeesSink.
 * 
 * @author Nees Development Team
 * 
 * @see NeesSink
 */
public interface NeesSinkMetadataListener {

	/**
	 * A method to process events as they arrive
	 * @param theEvent
	 */
	public void processNeesSinkMetadata(Document theXml, double timestamp);
	
}
