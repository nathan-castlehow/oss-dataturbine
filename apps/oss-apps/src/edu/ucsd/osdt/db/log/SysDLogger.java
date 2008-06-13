package edu.ucsd.osdt.db.log;

import java.net.*;

public class SysDLogger {

	public String serverName = "localhost";
	public int serverPort = 514;
	
	public SysDLogger (String sysDServer, int servPort) {
		try {

			serverName = sysDServer;
			serverPort = servPort;
			
			String mesg = "new SysDLogger is created";
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
	

	public SysDLogger (String sysDServer, int servPort, String mesg ) {
		try {
			
			serverName = sysDServer;
			serverPort = servPort;
			
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


	public boolean sysDMessage (String mesg) {
		try {
		
			String sysDServer = serverName;
			int servPort = serverPort;
			
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
			return true;
		} catch (Exception e) {
			System.err.println(e);
			return false;
		}
		
	}
	
	public static void main(String args[]) {
		SysDLogger s1 = new SysDLogger ("niagara.sdsc.edu", 514, "Logger Testing");
		s1.sysDMessage("sysDMessage function is called");
		return;
	}
}
