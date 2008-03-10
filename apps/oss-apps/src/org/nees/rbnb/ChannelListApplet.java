/*
 * Created on Mar 25, 2004
 */
package org.nees.rbnb;

import java.applet.Applet;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.rbnb.sapi.*;
/*
 * @author terry
 */
public class ChannelListApplet extends Applet {
	
	private static final String SERVER_NAME = "localhost";
	private static final String SERVER_PORT = "3333";
	private static final String SINK_NAME = "ListChannelApplet";
	
	private String serverName = SERVER_NAME;
	private String serverPort = SERVER_PORT;
	private String server = serverName + ":" + serverPort;
	private String sinkName = SINK_NAME;

	Sink sink = null;
	ChannelMap sMap;
	int index;
	boolean connected = false;
	
	Thread stringDataThread;
	boolean runit = false;

	private void setArgs() {
		String serverName = SERVER_NAME;
		String serverPort = SERVER_PORT;
		String sinkName = SINK_NAME;
		String param;

		param = getParameter("serverName");
		if (param != null)
			serverName = param;
		
		param = getParameter("serverPort");
		if (param != null)
			serverPort = param;
		
		param = getParameter("sinkName");
		if (param != null)
			sinkName = param;

		setArgs(serverName,serverPort,sinkName);		
	}

	private void setArgs(String serverName, String serverPort, String sinkName)
	{
		this.serverName = serverName;
		this.serverPort = serverPort;
		
		server = serverName + ":" + serverPort;
	
		setArgs(server,sinkName);
	}

	private void setArgs(String server, String sinkName)
	{
		this.server = server;
		serverText.setText(server);
		
		this.sinkName = sinkName;
		sinkText.setText(sinkName);
	}

	private void setArgsFromTextFields()
	{
		setArgs (	
			serverText.getText(),
			sinkText.getText()
		);
	}
	
	public void openConnection()
	{
		if (connected) return;
		
		try {
			disableStart();
			setArgsFromTextFields();
			messagePanel.message("Attempting connection to server...");
			// Create a sink and connect:
			sink=new Sink();
			sink.OpenRBNBConnection(server,sinkName);
			connected = true;
			messagePanel.message("ChannelListApplet: Connection made to server = "
				+ server + " requesting channel list.");
			enableStart();
			start();
			connectButton.setLabel(DISCONNECT);
			connectButton.setActionCommand(connectButton.getLabel());
		}
		catch (SAPIException se)
		{
			messagePanel.message("SAPIException = " + se);
		}
	}
	
	private void enableStart() {
		startStopButton.setEnabled(true);
	}

	private void disableStart() {
		startStopButton.setEnabled(false);
	}

	private void closeConnection()
	{
		if (!connected) return;
		stop();
		disableStart();
		connected = false;
		sink.CloseRBNBConnection();
		connectButton.setLabel(CONNECT);
		connectButton.setActionCommand(connectButton.getLabel());
	}

	public void startThread()
	{
		
		if (!connected) return;
		
		// Use this inner class to hide the public run method
		Runnable r = new Runnable() {
			public void run() {
			  runWork();
			}
		};
		runit = true;
		stringDataThread = new Thread(r, "ChannelListThread");
		stringDataThread.start();
		messagePanel.message("ChannelListApplet: Started thread.");
	}

	public void stopThread()
	{
		runit = false;
		stringDataThread.interrupt();
		messagePanel.message("ChannelListApplet: Stopped thread.");
	}
	
	private void runWork ()
	{
		try {
			while(isRunning())
			{
				sMap = new ChannelMap();
				sink.RequestRegistration();		
				sMap = sink.Fetch(-1,sMap);
				ChannelTree tr = ChannelTree.createFromChannelMap(sMap);
				messagePanel.message(tr.toString());
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ignore) {}
			}
		}
		catch (SAPIException se)
		{
			messagePanel.message("SAPIException = " + se);
		}
		stringDataThread = null;
	}
	
	public boolean isRunning()
	{
		return (connected && runit);
	}

	public void init() {
		setArgs();
		setLayout();
		repaint();
		messagePanel.message("initializing... ");
		messagePanel.message("Starting channelListApplet on " + server + " as " + sinkName);
	}

	public void start() {
		messagePanel.message("starting... ");
		startThread();
		startStopButton.setLabel(STOP);
		startStopButton.setActionCommand(startStopButton.getLabel());
	}

	public void stop() {
		messagePanel.message("stopping... ");
		stopThread();
		startStopButton.setLabel(START);
		startStopButton.setActionCommand(startStopButton.getLabel());
	}

	public void destroy() {
		messagePanel.message("preparing for unloading...");
		closeConnection();
	}

	private static final String CONNECT = "Connect";
	private static final String DISCONNECT = "Disconnect";
	private Button connectButton = new Button(CONNECT);
	
	private static final String START = "Start";
	private static final String STOP = "Stop";
	private Button startStopButton = new Button(START);
	
	private TextField serverText =
		new TextField(SERVER_NAME + ":" + SERVER_PORT,40);
	private TextField sinkText = new TextField(SINK_NAME,40);

	MessagePanel messagePanel = new MessagePanel();

	private void setLayout()
	{
		setLayout(new BorderLayout());		

		Panel p = new Panel();
		p.setLayout(new GridLayout(2,4));
		p.add(serverText);
		p.add(new Label("Server Host : port"));
		p.add(sinkText);
		p.add(new Label("Sink Name"));
		add("North",p);
			
		p = new Panel();
		p.add(connectButton);
		p.add(startStopButton);		
		add("South", p);
		
		p = new Panel();
		p.add(messagePanel);
		add("Center", p);
	
		startStopButton.setEnabled(false);
		startStopButton.setActionCommand(startStopButton.getLabel());
		startStopButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					startStopAction(ev.getActionCommand());
				}
			}
		);
		
		connectButton.setActionCommand(connectButton.getLabel());
		connectButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					connectAction(ev.getActionCommand());
				}
			}
		);
	
	}
	
	private void startStopAction(String command)
	{
		if (command.equals(STOP))
			stop();
		else
			start();
	}

	private void connectAction(String command)
	{
		if (command.equals(CONNECT))
			openConnection();
		else
			closeConnection();
	}

	private void displayChannelList() {
		try {
			sMap = new ChannelMap();
			sink.RequestRegistration();		
			sMap = sink.Fetch(-1,sMap);
			ChannelTree tr = ChannelTree.createFromChannelMap(sMap);
			messagePanel.message(tr.toString());
		} catch (SAPIException se) { se.printStackTrace(); }
	}
	
}
