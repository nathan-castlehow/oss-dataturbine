
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
 * DirectIPServer
 * 
 * Open a server socket and read binary SBD packets sent from the Iridium
 * gateway.
 * 
 * Modification History
 * Date		Programmer	Action
 * -----------------------------------
 * 02/11/2010	JPW		Created
 *
 */

package com.erigo.directip;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Source;

public class DirectIPServer {
	
	public final static int defaultServerPort = 1537;
	public final static String defaultRBNBAddr = "localhost:3333";
	
	/*************************************************************************
	 * 
	 * Main method.  Open the server socket and then read and parse SBD
	 * packets.
	 * 
	 * Copyright 2010 Erigo Technologies
	 * 
	 * Modification History
	 * Date		Programmer	Action
	 * -----------------------------------
	 * 02/11/2010	JPW		Created
	 *
	 */
	public static void main(String[] args) {
		
		boolean bVerbose = false;
		long arrivalTimestampMillis = 0;
		double arrivalTimestamp = 0.0;
		String arrivalTimeStr = null;
		
		//
		// Parse command line args
		//
		int serverPort = defaultServerPort;
		String rbnbAddr = defaultRBNBAddr;
		if (args.length == 1) {
			printUsage();
			System.exit(1);
		} else if (args.length >= 2) {
			try {
				serverPort = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				System.err.println("Could not parse server port number from given argument (" + args[0] + ")");
				printUsage();
				System.exit(1);
			}
			rbnbAddr = args[1];
			if (args.length >= 3) {
				// User is indicating whether they want to print debug
				if (args[2].startsWith("y")) {
					bVerbose = true;
				}
			}
		}
		
		//
		// Open server socket
		//
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(serverPort);
			System.err.println("Opened server socket on port " + serverPort);
		} catch (IOException e) {
			System.err.println("Could not open server socket on port " + serverPort + ":");
			System.err.println(e);
			System.exit(1);
		}
		
		//
		// Open RBNB connection
		//
		Source src = new Source(100, "append", 100000);
		try {
			src.OpenRBNBConnection(rbnbAddr, "SBD");
			System.err.println("Connected to RBNB at " + rbnbAddr);
		} catch (SAPIException e) {
			System.err.println("Error connecting to RBNB server at " + rbnbAddr + ":");
			System.err.println(e);
			try {
				serverSocket.close();
			} catch (IOException ioe) {
				// Nothing to do
			}
			System.exit(1);
		}
		
		//
		// Main loop: read and parse SBD packets
		//
		while (true) {
			// Buffer to hold the main SBD message
			SBDMessage message = null;
			try {
				// Open connections
				Socket clientSocket = serverSocket.accept();
				InputStream in = clientSocket.getInputStream();
				
				arrivalTimestampMillis =  System.currentTimeMillis();
				arrivalTimestamp = arrivalTimestampMillis/1000.0;
				message = new SBDMessage(new BufferedInputStream(in));
				
				// Close connections
				in.close();
				clientSocket.close();
			} catch (IOException e) {
				System.err.println("Failed to get new SBD message.");
				System.err.println(e);
				continue;
			}
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");
			sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
			Date date = new Date(arrivalTimestampMillis);
			arrivalTimeStr = sdf.format(date);
			
			// Parse the message
			try {
				message.parse();
				System.err.println(
					"[" +
					arrivalTimeStr +
					"] Received message from IMEI " +
					message.headerIE.imei);
				if (bVerbose) {
					System.err.println(message);
				}
			} catch (Exception e) {
				System.err.println(
					new String("Caught exception parsing SBD message:\n" + e));
				continue;
			}
			
			// Add data to the RBNB:
			// 1. Raw SBD packet
			// 2. Arrival timestamp
			// 3. Latency channel
			ChannelMap cm = new ChannelMap();
			cm.PutTime(arrivalTimestamp, 0);
			int idx;
			try {
			    // Raw SBD packet
			    idx = cm.Add(new String(message.headerIE.imei + "/SBD"));
			    cm.PutDataAsByteArray(idx, message.rawMessage );
			    // Arrival timestamp
			    idx = cm.Add(new String(message.headerIE.imei + "/time_of_arrival"));
			    cm.PutDataAsString(idx, new String(arrivalTimeStr + "\n"));
			    // Latency channel
			    idx = cm.Add(new String(message.headerIE.imei + "/_Latency"));
			    double latency = arrivalTimestamp - (double)message.headerIE.time;
			    cm.PutDataAsString(idx, new String(latency + "\n"));
			    src.Flush(cm);
			} catch (SAPIException e) {
			    System.err.println("Error sending data to RBNB:\n" + e);
			}
			
			// Send all other data to RBNB
			message.sendDataToRBNB(src,message.headerIE.imei,arrivalTimestamp);
		}
		
		// Not currently reachable
		//serverSocket.close();
		
	}
	
	/*************************************************************************
	 * 
	 * Print usage information.
	 * 
	 * Copyright 2010 Erigo Technologies
	 * 
	 * Modification History
	 * Date		Programmer	Action
	 * -----------------------------------
	 * 04/06/2010	JPW		Created
	 *
	 */
	public static void printUsage() {
		System.err.println("Usage:");
		System.err.println("    java -jar directipserver.jar <server port> <RBNB host:port> <y|n verbose mode>");
		System.err.println("    If no arguments are given, the following defaults are used:");
		System.err.println("        server port = " + defaultServerPort);
		System.err.println("        connect to RBNB at " + defaultRBNBAddr);
		System.err.println("        no verbose output");
	}
	
}
