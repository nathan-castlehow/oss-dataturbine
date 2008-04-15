package edu.ucsd.osdt.db.log;

import java.io.*;
import java.net.*;

public class SysDLogger {

	public SysDLogger (String sysDServer, int servPort, String mesg ) {
		try {
			
			// 1 means user message, code 0 is alert. 
			mesg = "<10>"+mesg;
			
			byte[] message = mesg.getBytes();

			// Get the internet address of the specified host
			InetAddress address = InetAddress.getByName(sysDServer);

			// Initialize a datagram packet with data and address
			DatagramPacket packet = new DatagramPacket(message, message.length,
					address, servPort);

			// Create a datagram socket, send the packet through it, close it.
			DatagramSocket dsocket = new DatagramSocket();
			dsocket.send(packet);
			dsocket.close();
		} catch (Exception e) {
			System.err.println(e);
		}
	}


	public static void main(String args[]) {
		SysDLogger s1 = new SysDLogger ("niagara.sdsc.edu", 514, "Logger Testing");
		return;
	}
}
