/*
 * Created on Feb 5, 2004
 *
 * A RBNB source that generates numbers in a bounded random walk.
 */

package org.nees.rbnb;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
 
import com.rbnb.sapi.*;
//import COM.Creare.Utility.ArgHandler; //for argument parsing
//import COM.Creare.Utility.RBNBProcess; //alternative to System.exit, so
									   //don't bring down servlet engine
import com.rbnb.utility.ArgHandler; //for argument parsing
import com.rbnb.utility.RBNBProcess; //alternative to System.exit, so
									   //don't bring down servlet engine

/**
 * @author terry
 *
 * A RBNB source that generates numbers in a bounded random walk.
 * The numbers are generate at a regular interval (specified by the timerInterval
 * which defaults to 1 second).
 * 
 */
public class WalkerSourceApplet extends Applet
	implements WalkerCommandActionListener
{

	SimpleRandomWalk base = new SimpleRandomWalk();
	
	private static final String SERVER_NAME = "localhost";
	private static final String SERVER_PORT = "3333";
	private static final String SOURCE_NAME = "RandomWalk";
	private static final String CHANNEL_NAME = "RandomWalkData";
	private static final String COMMAND_NAME = "Command";
	private static final String COMMAND_CHANNEL = "CommandData";
	private static final long TIMER_INTERVAL=1000;
	
	private String serverName = SERVER_NAME;
	private String serverPort = SERVER_PORT;
	private String server = serverName + ":" + serverPort;
	private String sourceName = SOURCE_NAME;
	private String channelName = CHANNEL_NAME;
	private String commandName = COMMAND_NAME;
	private String commandChannel = COMMAND_CHANNEL;
	private String commandPath = commandName + "/" + commandChannel;
	private long timerInterval = TIMER_INTERVAL;

	private static final int DEFAULT_CACHE_SIZE = 900;
	private int cacheSize = DEFAULT_CACHE_SIZE;
	private static final int DEFAULT_ARCHIVE_SIZE = 0;
	private int archiveSize = DEFAULT_ARCHIVE_SIZE;

	Source source = null;
	ChannelMap sMap;
	int index;
	boolean connected = false;
	
	Thread timerThread;
	boolean runit = false;
	
	CommandThread commandThread = null;
	
	private void setArgs() {
		String serverName = SERVER_NAME;
		String serverPort = SERVER_PORT;
		String sourceName = SOURCE_NAME;
		String channelName = CHANNEL_NAME;
		String timerString = "" + TIMER_INTERVAL;
		String commandName = COMMAND_NAME;
		String commandChannel = COMMAND_CHANNEL;
		String param;

		param = getParameter("serverName");
		if (param != null)
			serverName = param;
		
		param = getParameter("serverPort");
		if (param != null)
			serverPort = param;
		
		param = getParameter("source");
		if (param != null)
			sourceName = param;

		param = getParameter("channel");
		if (param != null)
			channelName = param;

		param = getParameter("timer");
		if (param != null)
			timerString = param;

		param = getParameter("commandName");
		if (param != null)
			commandName = param;

		param = getParameter("commandChannel");
		if (param != null)
			commandChannel = param;

        if ((archiveSize > 0) && (archiveSize < cacheSize)){
            messagePanel.message("archiveSize is less the cacheSize; set to " +
                cacheSize);
            archiveSize = cacheSize;
        }

		setArgs(serverName,serverPort,sourceName,channelName,timerString,
			commandName,commandChannel);		
	}

	private void setArgs(String serverName, String serverPort,
			String sourceName, String channelName, String timerString,
			String commandName, String commandChannel)
	{
		this.serverName = serverName;
		this.serverPort = serverPort;
		
		server = serverName + ":" + serverPort;
	
		this.commandName = commandName;
		this.commandChannel = commandChannel;
		
		commandPath = commandName + "/" + commandChannel;
		
		setArgs(server,sourceName,channelName,timerString);
	}

	private void setArgs(String server, String sourceName, 
			String ChannelName, String timerString)
	{
		long timerInterval = TIMER_INTERVAL;

		try {
			timerInterval = Long.parseLong(timerString);
		}  catch (Throwable ignore){}

		setArgs(server,sourceName,channelName,timerInterval);		
	}

	private void setArgs(String server, String sourceName,
		String channelName, long timerInterval)
	{
		this.server = server;
		serverText.setText(server);
		
		this.sourceName = sourceName;
		sourceText.setText(sourceName);
		
		this.channelName = channelName;
		channelText.setText(channelName);
		
		this.timerInterval = timerInterval;
		timerText.setText(""+timerInterval);
	}

	private void setArgsFromTextFields()
	{
		setArgs (	
			serverText.getText(),
			sourceText.getText(),
			channelText.getText(),
			timerText.getText()
		);
	}
	
	private void openConnection()
	{
		if (connected) return;
		
		try {
			disableStart();
			setArgsFromTextFields();
			messagePanel.message("Attempting connection to server...");
			if (archiveSize > 0)
				source=new Source(cacheSize, "create", archiveSize);
			else
				source=new Source(cacheSize, "none", 0);
			source.OpenRBNBConnection(server,sourceName);
			sMap = new ChannelMap();
			int index = sMap.Add(channelName);
			connected = true;
			messagePanel.message("Connection made to server...");
			enableStart();
			start();
			connectButton.setLabel(DISCONNECT);
			connectButton.setActionCommand(connectButton.getLabel());
		} catch (SAPIException se)
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
		source.CloseRBNBConnection();
		connectButton.setLabel(CONNECT);
		connectButton.setActionCommand(connectButton.getLabel());
	}

	private void startThread()
	{
		
		if (!connected) return;

		if (runit) return;
		
		// Use this inner class to hide the public run method
		Runnable r = new Runnable() {
			public void run() {
			  runWork();
			}
		};
		runit = true;
		timerThread = new Thread(r, "Timer");
		timerThread.start();
		messagePanel.message("Start: server = " + server);
		messagePanel.message("   source = " + sourceName + " with " + channelName);
		messagePanel.message("   timer interval = " + timerInterval);
		messagePanel.message("   command path = " + commandPath);
	}

	private void stopThread()
	{
		if (!connected) return;
		
		if (runit)
		{
			runit = false;
			timerThread.interrupt();
			messagePanel.message("Stopped thread.");
		}
	}
	
	private void runWork ()
	{
		try {
			while(connected && runit)
			{
				// Push data onto the server:
				// System.out.print("Put new data to server: ");
				sMap.PutTimeAuto("timeofday");
				double data[] = new double[1];
				data[0] = base.next();
				sMap.PutDataAsFloat64(index,data);
				messagePanel.message("" + data[0]);
				source.Flush(sMap);
				Thread.sleep(timerInterval);
			}
		} catch (SAPIException se) {
			// se.printStackTrace();
			messagePanel.message("SAPIException " + se + "; "); 
		} catch (InterruptedException e) {
			//e.printStackTrace();
			messagePanel.message("InterrupedExcetion; ");
		}
		timerThread = null;
	}
	
	private boolean isRunning()
	{
		return (connected && runit);
	}
	
	public void init() {
		setArgs();
		setLayout();
		repaint();
		messagePanel.message("initializing... ");
	}

	public void start() {
		messagePanel.message("starting... ");
		commandThread = new CommandThread(commandPath, messagePanel, this);
		commandThread.connect();
		commandThread.startThread();
		startThread();
		startStopButton.setLabel(STOP);
		startStopButton.setActionCommand(startStopButton.getLabel());
	}

	public void stop() {
		messagePanel.message("stopping... ");
		stopThread();
		commandThread.stopThread();
		commandThread.disconnect();
		commandThread = null;
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
	private TextField sourceText = new TextField(SOURCE_NAME,40);
	private TextField channelText = new TextField(CHANNEL_NAME,40);
	private TextField timerText = new TextField("" + TIMER_INTERVAL,40);

	MessagePanel messagePanel = new MessagePanel();

	private void setLayout()
	{
		setLayout(new BorderLayout());		

		Panel p = new Panel();
		p.setLayout(new GridLayout(2,4));
		p.add(serverText);
		p.add(new Label("Server Host : port"));
		p.add(sourceText);
		p.add(new Label("Source Name"));
		p.add(channelText);
		p.add(new Label("Channel Name"));
		p.add(timerText);
		p.add(new Label("Timer (millisec.)"));
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

	public void processCommand(String command) {
		messagePanel.message("Command received: " + command);
		if (command.equals("stop"))
		{
			stop();
		}
		else if (command.equals("start"))
		{
			start();
		}
	}

	private class CommandThread
	{
		
		Thread commandThread = null;
		Sink sink = null;
		String sinkName = "WalkerCommandSink";
		String requestPath;
		MessagePanel messagePanel;
		boolean connected;
		boolean runit;
		WalkerCommandActionListener w = null;
	
		public CommandThread(String path, MessagePanel message, WalkerCommandActionListener x)
		{
			w = x;
			requestPath = path;
			messagePanel = message;
		}
		
		/**
		 * 
		 */
		public void connect() {
			try {
				// Create a sink and connect:
				sink=new Sink();
				sink.OpenRBNBConnection(server,sinkName);
				sMap = new ChannelMap();
				index = sMap.Add(requestPath);
				sink.Subscribe(sMap,"newest");
				connected = true;
				messagePanel.message("CommandThread: Connection made to server = "
					+ server);
				messagePanel.message("    as " + sinkName 
					+ " requesting " + requestPath + ".");
			} catch (SAPIException se)
			{ 
				messagePanel.message("CommandThread: creation exception = " + se);
			}			
		}

		/**
		 * 
		 */
		public void disconnect() {
			sink.CloseRBNBConnection();
			sink = null;
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
			commandThread = new Thread(r, "CommandThread");
			commandThread.start();
			messagePanel.message("CommandThread: Started thread.");
		}
	
		public void stopThread()
		{
			runit = false;
			commandThread.interrupt();
			messagePanel.message("CommandThread: Stopped thread.");
		}
		
		private void runWork ()
		{
			try {
				while(isRunning())
				{
					ChannelMap m = sink.Fetch(-1);
					if (m == null)
					{
						messagePanel.message("CommandThread: fetch failed.");
						continue;
					}
					String[] st = m.GetDataAsString(index);
					messagePanel.message("CommandThread: Command(s) Received: ");
					for (int i = 0; i < st.length; i++)
					{
						System.out.println(st[i]);
						w.processCommand(st[i]);
					}
				}
			} catch (SAPIException se) {
				messagePanel.message("CommandThread: exception in fetch = " + se);
				stopThread();
			}
			commandThread = null;
		}
		
		public boolean isRunning()
		{
			return (connected && runit);
		}
	
	}

}
