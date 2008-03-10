/*
 * Created on Mar 5, 2004
 */
package org.nees.rbnb;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.TimeZone;
import java.text.SimpleDateFormat;

import com.rbnb.sapi.*;

/**
 * @author terry
 */
public class NumberSinkApplet extends Applet {
	
	private static final String SERVER_NAME = "localhost";
	private static final String SERVER_PORT = "3333";
	private static final String SINK_NAME = "GetNumber";
	private static final String SOURCE_NAME = "RandomWalk";
	private static final String CHANNEL_NAME = "RandomWalkData";
	
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM d, yyyy h:mm aa");
	private static final TimeZone TZ = TimeZone.getTimeZone("GMT");

	static
	{
		DATE_FORMAT.setTimeZone(TZ);
	}

	private String serverName = SERVER_NAME;
	private String serverPort = SERVER_PORT;
	private String server = serverName + ":" + serverPort;
	private String sinkName = SINK_NAME;
	private String sourceName = SOURCE_NAME;
	private String channelName = CHANNEL_NAME;
	private String requestPath = sourceName + "/" + channelName;

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
		String sourceName = SOURCE_NAME;
		String channelName = CHANNEL_NAME;
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

		param = getParameter("sourceName");
		if (param != null)
			sourceName = param;

		param = getParameter("channelName");
		if (param != null)
			channelName = param;

		setArgs(serverName,serverPort,sinkName,sourceName,channelName);		
	}

	private void setArgs(String serverName, String serverPort,
		String sinkName, String sourceName, String channelName)
	{
		this.serverName = serverName;
		this.serverPort = serverPort;
		
		server = serverName + ":" + serverPort;
	
		setArgs(server,sinkName,sourceName,channelName);
	}

	private void setArgs(String server, String sinkName,
		String sourceName, String channelName)
	{
		this.server = server;
		serverText.setText(server);
		
		this.sinkName = sinkName;
		sinkText.setText(sinkName);

		this.sourceName = sourceName;
		sourceText.setText(sourceName);
		
		this.channelName = channelName;
		channelText.setText(channelName);
		
		requestPath = sourceName + "/" + channelName;
		
	}

	private void setArgsFromTextFields()
	{
		setArgs (	
			serverText.getText(),
			sinkText.getText(),
			sourceText.getText(),
			channelText.getText()
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
			sMap = new ChannelMap();
			index = sMap.Add(requestPath);
			sink.Subscribe(sMap,"newest");
			connected = true;
			messagePanel.message("Connection made to server...");
			messagePanel.message("on " + server + " as " + sinkName);
			messagePanel.message("  Requesting " + requestPath);
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
		stringDataThread = new Thread(r, "StringData");
		stringDataThread.start();
		messagePanel.message("NumberSink: Started thread.");
	}

	public void stopThread()
	{
		runit = false;
		stringDataThread.interrupt();
		messagePanel.message("NumberSink: Stopped thread.");
	}
	
	private void runWork ()
	{
		try {
			while(isRunning())
			{
				ChannelMap m = sink.Fetch(-1);
				if (m == null)
				{
					messagePanel.message("Data fetch failed.");
					continue;
				}
				double timeStamp = m.GetTimeStart(index);
				double[] data = m.GetDataAsFloat64(index);
				long unixTime = (long)(timeStamp * 1000.0); // convert sec to millisec
				String time = DATE_FORMAT.format(new Date(unixTime));
				messagePanel.message("Data Received (for " + time + " GMT): ");
				for (int i = 0; i < data.length; i++)
				{
					messagePanel.message("  " + i + ": " + data[i]);
				}
			}
		} catch (SAPIException se) {
			se.printStackTrace(); 
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
		messagePanel.message("Starting NumberSinkAppler on " + server + " as " + sinkName);
		messagePanel.message("  Requesting " + requestPath);
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
	private TextField sourceText = new TextField(SOURCE_NAME,40);
	private TextField channelText = new TextField(CHANNEL_NAME,40);

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
		p.add(sourceText);
		p.add(new Label("Source Name"));
		p.add(channelText);
		p.add(new Label("Channel Name"));
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
}
