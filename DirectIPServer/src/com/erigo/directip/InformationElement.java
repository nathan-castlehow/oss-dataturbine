
/*****************************************************************************
 * 
 * Copyright 2010 Erigo Technologies LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * InformationElement
 * 
 * This class represents all of the DirectIP Information Elements.  Each IE
 * consists of the following parts:
 * 
 * 1 byte ID
 * 2 byte length
 * byte array containing the body of this information element
 * 
 * Modification History
 * Date		Programmer	Action
 * -----------------------------------
 * 04/06/2010	JPW		Created
 *
 */

package com.erigo.directip;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Source;

public abstract class InformationElement {
	
	// table to convert a nibble to a hex char
	private static char[] hexChar = {
		'0' , '1' , '2' , '3' ,
		'4' , '5' , '6' , '7' ,
		'8' , '9' , 'A' , 'B' ,
		'C' , 'D' , 'E' , 'F'};
	
	// The ID - tells us what kind of Information Element this is
	public byte ID = 0;
	
	// Length of the content array
	public int length = -1;
	
	// The raw binary content stored in this Information Element
	public byte[] content = null;
	
	/*************************************************************************
	 * 
	 * Constructor
	 * 
	 * Parse the Information Element content by reading from the given
	 * Input Stream.
	 * 
	 * Copyright 2010 Erigo Technologies
	 * 
	 * Modification History
	 * Date		Programmer	Action
	 * -----------------------------------
	 * 04/06/2010	JPW		Created
	 *
	 */
	public InformationElement(byte IDI, InputStream inI) throws IOException {
		
		ID = IDI;
		
		int numRead = 0;
		
		// Information Element content length
		checkInputStreamAvailability(inI,2);
		byte[] msgLenB = new byte[2];
		numRead = inI.read(msgLenB);
		if (numRead != 2) {
			throw new IOException("Error reading the message length from InputStream");
		}
		ByteBuffer msgLenBB = ByteBuffer.wrap(msgLenB);
		length = (int)msgLenBB.getShort();
		
		// Information Element content
		checkInputStreamAvailability(inI,length);
		content = new byte[length];
		numRead = inI.read(content);
		if (numRead != length) {
			throw new IOException(
				new String(
					"Error reading the Information Element content from InputStream - wrong length; expected " +
					length +
					", got " +
					numRead));
		}
		
	}
	
	/*************************************************************************
	 * 
	 * Does the given Input Stream have the desired number of bytes currently
	 * available?  If not, then sleep for a momment and check again.  If it
	 * still doesn't, then throw an exception.
	 * 
	 * Copyright 2010 Erigo Technologies
	 * 
	 * Modification History
	 * Date		Programmer	Action
	 * -----------------------------------
	 * 04/06/2010	JPW		Created
	 *
	 */
	public static void checkInputStreamAvailability(InputStream inI, int numDesiredI) throws IOException {
		if (inI.available() < numDesiredI) {
			// Wait to see if more data is coming
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// Nothing to do
			}
			// Check availability again
			if (inI.available() < numDesiredI) {
				throw new IOException(
					new String(
						"Needed number of bytes (" +
						numDesiredI +
						") not available from InputStream"));
			}
		}
	}
	
	/*************************************************************************
	 * 
	 * Abstract method that needs to be implemented by subclasses.
	 * 
	 * Copyright 2010 Erigo Technologies
	 * 
	 * Modification History
	 * Date		Programmer	Action
	 * -----------------------------------
	 * 04/15/2010	JPW		Created
	 * 10/11/2010	JPW		Add folderNameI argument.
	 *
	 */
	public abstract void sendDataToRBNB(Source srcI, String folderNameI, double timestampI) throws SAPIException;
	
	/*************************************************************************
	 * 
	 * Return a hex string representation of the given byte buffer.
	 * 
	 * Copyright 2010 Erigo Technologies
	 * 
	 * Modification History
	 * Date		Programmer	Action
	 * -----------------------------------
	 * 04/06/2010	JPW		Created
	 *
	 */
	public static String toHexString (String prefixStrI, byte[] bI) {
		
		StringBuffer sb = new StringBuffer( bI.length * 2 + 50);
		sb.append(prefixStrI);
		for ( int i=0; i<bI.length; i++ ) {
			// Have a newline every 16 bytes
			if ( (i != 0) && ((i%16) == 0) ) {
				sb.append("\n" + prefixStrI);
			}
			// look up high nibble char
			sb.append( hexChar [( bI[i] & 0xf0 ) >>> 4] );
			// look up low nibble char
			sb.append( hexChar [bI[i] & 0x0f] );
			// Add a space between bytes
			sb.append(" ");
		}
		return sb.toString();
		
	}
	
	/*************************************************************************
	 * 
	 * Return a string containing the content of this Information Element.
	 * 
	 * Copyright 2010 Erigo Technologies
	 * 
	 * Modification History
	 * Date		Programmer	Action
	 * -----------------------------------
	 * 04/06/2010	JPW		Created
	 *
	 */
	public String toString(String prefixStrI, String elementTypeI) {
		
		StringBuffer sb = new StringBuffer();
		
		sb.append(new String(prefixStrI + "ID: " + ID + " (" + elementTypeI + ")\n"));
		sb.append(new String(prefixStrI + "\tcontent length: " + length + "\n"));
		String hexStr = toHexString(prefixStrI+"\t",content);
		sb.append(prefixStrI + "\tcontent:\n" + hexStr);
		
		return sb.toString();
		
	}
	
}
