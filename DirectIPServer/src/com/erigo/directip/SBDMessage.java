
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
 * SBDMessage
 * 
 * This class handles parsing and storing data for one complete Mobile
 * Originated SBD message.
 * 
 * Modification History
 * Date		Programmer	Action
 * -----------------------------------
 * 04/09/2010	JPW		Created
 *
 */

package com.erigo.directip;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Source;

public class SBDMessage {
	
	// Array containing the total raw message: protocol number, length, and content fields
	private byte[] rawMessage = null;
	
	// Protocol revision number; should be 1
	public byte protocolRevNum = 0;
	
	// Length of the msg content
	public int length = -1;
	
	// The raw binary content - will contain the child information elements
	public byte[] content = null;
	
	// The potential information elements that may be within this message
	private MOHeaderIE headerIE = null;
	private MOLocationIE locationIE = null;
	private MOPayloadIE payloadIE = null;
	
	/*************************************************************************
	 * 
	 * Constructor
	 * 
	 * Read the raw message from the input stream.  Avoid doing any real
	 * parsing/processing on the buffer at this point.
	 * 
	 * Copyright 2010 Erigo Technologies
	 * 
	 * Modification History
	 * Date		Programmer	Action
	 * -----------------------------------
	 * 04/09/2010	JPW		Created
	 *
	 */
	public SBDMessage(BufferedInputStream bisI) throws IOException {
		// In actuality, incoming message shouldn't be larger than a few hundred bytes
		byte[] bArray = new byte[4096];
		int arrayPtr = 0;
		int zeroAvailableCounter = 0;
		
		while ( (bisI.available() > 0) || (zeroAvailableCounter < 3) ) {
			int numAvailable = bisI.available();
			if (numAvailable == 0) {
				// Give the input stream a chance to fill before reading more;
				// this has occasionally been needed, because we will sometimes
				// get an initial "available" value of 0, but then if we wait a
				// bit we will actually get content from the input stream.
				// System.err.println("Sleep to see if we get more msg; num read = " + arrayPtr);
				try { Thread.sleep(10);	} catch (InterruptedException e) {	}
				++zeroAvailableCounter;
				continue;
			}
			int numRead = bisI.read(bArray, arrayPtr, numAvailable);
			if (numRead == -1) {
				// Reached end of stream
				break;
			} else if (numRead == 0) {
				// Allow the input stream to fill before reading more;
				// not sure if this is really needed.
				try { Thread.sleep(10);	} catch (InterruptedException e) {	}
			} else {
				arrayPtr = arrayPtr + numRead;
			}
		}
		// Copy content from our temporary array to the final array
		rawMessage = Arrays.copyOf(bArray, arrayPtr);
	}
	
	/*************************************************************************
	 * 
	 * Parse the content of the SBD message.
	 * 
	 * This code is based on the content layout specified in NAL Research
	 * document TN2007-637-V1.0.0, "Additional Information On DirectIP SBD,
	 * Technical Note".
	 * 
	 * Copyright 2010 Erigo Technologies
	 * 
	 * Modification History
	 * Date		Programmer	Action
	 * -----------------------------------
	 * 04/09/2010	JPW		Created
	 *
	 */
	public void parse() throws Exception {
		
		//
		// Parse the SBD message
		//
		// Make sure the length of rawMessage is at least 3 (1 byte for the ID and 2 bytes for the length)
		if (rawMessage == null) {
			throw new Exception("SBD message is null");
		} else if (rawMessage.length < 3) {
			throw new Exception(new String("Illegal length of SBD message: " + rawMessage.length));
		}
		
		int bytePointer = 0;
		
		// Protocol revision number
		protocolRevNum = rawMessage[bytePointer];
		if (protocolRevNum != 1) {
			throw new Exception(
				new String(
					"Unknown SBD protocol revision number: expecting 1, got " +
					protocolRevNum));
		}
		
		// Content length
		byte[] ieLenB = new byte[2];
		ieLenB[0] = rawMessage[bytePointer+1];
		ieLenB[1] = rawMessage[bytePointer+2];
		ByteBuffer ieLenBB = ByteBuffer.wrap(ieLenB);
		length = (int)ieLenBB.getShort();
		
		// SBD content
		// Check the length of rawMessage again
		int expectedLen = 3 + length;
		if (rawMessage.length < expectedLen) {
			throw new Exception(
				new String(
					"Illegal length of SBD message: expecting " +
					expectedLen +
					", got " +
					rawMessage.length));
		}
		content = new byte[length];
		for (int i = 0; i < length; ++i) {
			content[i] = rawMessage[bytePointer+3+i];
		}
		
		//
		// Parse the child information element contents
		//
		ByteArrayInputStream bais = new ByteArrayInputStream(content);
		while(bais.available() > 0) {
			// Read the Information Element ID, so we know which type of
			// object to create
			try {
				InformationElement.checkInputStreamAvailability(bais,1);
			} catch (IOException ioe) {
				throw new Exception(
					new String("Error parsing ID from Information Element: " + ioe));
			}
			byte[] idB = new byte[1];
			int numRead = bais.read(idB);
			if (numRead != 1) {
				throw new Exception("Error parsing ID from Information Element");
			}
			switch (idB[0]) {
			case 0x01:
				// Mobile Originated Header Information Element
				try {
					headerIE = new MOHeaderIE(idB[0],bais);
				} catch (Exception e) {
					throw new Exception(new String("Error parsing MO Header IE: " + e));
				}
				break;
			case 0x02:
				// Mobile Originated Payload Information Element
				try {
					payloadIE = new MOPayloadIE(idB[0],bais);
				} catch (Exception e) {
					throw new Exception(new String("Error parsing MO Payload IE: " + e));
				}
				break;
			case 0x03:
				// Mobile Originated Location Information Element
				try {
					locationIE = new MOLocationIE(idB[0],bais);
				} catch (Exception e) {
					throw new Exception(new String("Error parsing MO Location IE: " + e));
				}
				break;
			default:
				throw new Exception(
					new String("Parsing SBD message: invalid Information Element ID: " + idB[0]));
			}
		}
	}
	
	/*************************************************************************
	 * 
	 * Send parsed data to the given RBNB Source.
	 * 
	 * Copyright 2010 Erigo Technologies
	 * 
	 * Modification History
	 * Date		Programmer	Action
	 * -----------------------------------
	 * 04/15/2010	JPW		Created
	 *
	 */
	public void sendDataToRBNB(Source srcI) {
		
		if (headerIE == null) {
			// Can't send data to RBNB, because we won't know the time
			System.err.println("Can't send data to RBNB - no header IE");
			return;
		}
		long time = headerIE.time;
		try {
			headerIE.sendDataToRBNB(srcI,time);
		} catch (SAPIException e) {
			System.err.println("Error sending header IE data to RBNB:\n" + e);
		}
		if (locationIE != null) {
			try {
				locationIE.sendDataToRBNB(srcI,time);
			} catch (SAPIException e) {
				System.err.println("Error sending location IE data to RBNB:\n" + e);
			}
		}
		if (payloadIE != null) {
			try {
				payloadIE.sendDataToRBNB(srcI, time);
			} catch (SAPIException e) {
				System.err.println("Error sending payload IE data to RBNB:\n" + e);
			}
		}
		
	}
	
	/*************************************************************************
	 * 
	 * Print information from this SBD message.
	 * 
	 * Copyright 2010 Erigo Technologies
	 * 
	 * Modification History
	 * Date		Programmer	Action
	 * -----------------------------------
	 * 04/09/2010	JPW		Created
	 *
	 */
	public String toString() {
		StringBuffer sb =
			new StringBuffer(
				new String(
					"SBD message:\n" +
					"\tProtocol revision number: " + protocolRevNum + "\n" +
					"\tOverall message length: 3 byte header plus " + length + " bytes payload\n" +
					"\tContent:\n" +
					InformationElement.toHexString("\t",content) +
					"\n\n"));
		if (headerIE != null) {
			sb.append(headerIE);
		}
		if (locationIE != null) {
			sb.append(locationIE);
		}
		if (payloadIE != null) {
			sb.append(payloadIE);
		}
		return sb.toString();
	}
	
}
