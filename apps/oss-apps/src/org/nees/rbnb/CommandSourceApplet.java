/*
 * Created on Mar 9, 2004
 */
package org.nees.rbnb;

import java.applet.Applet;
import java.awt.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.rbnb.sapi.*;

/**
 * @author terry
 */
public class CommandSourceApplet extends Applet {

	private static final String SERVER_NAME = "localhost";
	private static final String SERVER_PORT = "3333";
	private static final String SOURCE_NAME = "Command";
	private static final String CHANNEL_NAME = "CommandData";
	
	private String serverName = SERVER_NAME;
	private String serverPort = SERVER_PORT;
	private String server = serverName + ":" + serverPort;
	private String sourceName = SOURCE_NAME;
	private String channelName = CHANNEL_NAME;

	private static final int DEFAULT_CACHE_SIZE = 900;
	private int cacheSize = DEFAULT_CACHE_SIZE;
	private static final int DEFAULT_ARCHIVE_SIZE = 0;
	private int archiveSize = DEFAULT_ARCHIVE_SIZE;

	private Command[] commandArray = new Command[0];
	private Source source = null;
	private ChannelMap sMap;
	private int index;
	private boolean connected = false;

	private void setArgs() {
		String serverName = SERVER_NAME;
		String serverPort = SERVER_PORT;
		String sourceName = SOURCE_NAME;
		String channelName = CHANNEL_NAME;
		String param;

		param = getParameter("serverName");
		if (param != null)
			serverName = param;
		
		param = getParameter("serverPort");
		if (param != null)
			serverPort = param;
		
		param = getParameter("sourceName");
		if (param != null)
			sourceName = param;

		param = getParameter("channelName");
		if (param != null)
			channelName = param;

        if ((archiveSize > 0) && (archiveSize < cacheSize)){
            messagePanel.message("archiveSize is less the cacheSize; set to " +
                cacheSize);
            archiveSize = cacheSize;
        }

		setArgs(serverName + ":" + serverPort,sourceName,channelName);		
		
		commandArray = new Command[0];
		param = getParameter("numberOfCommands");
		if (param != null)
		{
			try
			{
				int n = Integer.parseInt(param);
				commandArray = new Command[n];		
			} catch (Exception ignore){}
		}

		for (int i = 0; i < commandArray.length; i++)
		{
			String paramCount = (i<9)?"0"+i:""+i;
			String nameParam = getParameter("commandName"+paramCount);
			String textParam = getParameter("commandText"+paramCount);
			String labelParam = getParameter("commandLabel"+paramCount);
			String valueParam = getParameter("commandDefault"+paramCount);

			if ((nameParam != null) && (textParam != null))
				commandArray[i] = new Command(nameParam, textParam, labelParam, valueParam);
			else
				commandArray[i] = new Command("empty","", null, null);
		}
	}
	
	private void setArgs(String server_host, String source_name, String channel_name)
	{
		server = server_host;
		serverText.setText(server);
		
		sourceName = source_name;
		sourceText.setText(sourceName);
		
		channelName = channel_name;
		channelText.setText(channelName);	
	}

	private void setArgsFromTextFields()
	{
		setArgs (	
			serverText.getText(),
			sourceText.getText(),
			channelText.getText()
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
	public void init() {
		setArgs();
		setLayout();
		repaint();
		messagePanel.message("initializing... ");
		messagePanel.message("Number of commands = " + commandArray.length);
		for (int i = 0; i < commandArray.length; i++)
			messagePanel.message("Command(" + i + ") = " + commandArray[i]);
	}

	public void start() {
		messagePanel.message("starting... ");
		messagePanel.message("Command array length: " + commandArray.length);
		for (int i = 0; i < commandArray.length; i++)
			messagePanel.message("Command: " + commandArray[i]);
		startStopButton.setLabel(STOP);
		startStopButton.setActionCommand(startStopButton.getLabel());
	}

	public void stop() {
		messagePanel.message("stopping... ");
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
	
	private TextField serverText = new TextField(SERVER_NAME + ":" + SERVER_PORT,40);
	private TextField sourceText = new TextField(SOURCE_NAME,40);
	private TextField channelText = new TextField(CHANNEL_NAME,40);

	MessagePanel messagePanel = new MessagePanel();

	private void setLayout()
	{
		setLayout(new BorderLayout());		
		
		Panel p = new Panel();
		p.setLayout(new GridLayout(3 + commandArray.length,2));
		p.add(serverText);
		p.add(new Label("Server Host : port"));
		p.add(sourceText);
		p.add(new Label("Source Name"));
		p.add(channelText);
		p.add(new Label("Channel Name"));
		for (int i = 0; i < commandArray.length; i++)
		{
			if (commandArray[i] != null)
			{
				p.add(new Button(commandArray[i].cover));
				Panel cp = new Panel();
				if (commandArray[i].label == null)
				{
					cp.add(new Label("No value"));
				}
				else
				{
					if (commandArray[i].value != null)
						cp.add(new TextField(commandArray[i].value,40));
					else
						cp.add(new TextField("",40));
					cp.add(new Label(commandArray[i].label));
				}
				p.add(cp);
			}
		}
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

	private class Command
	{
		String cover = "Cover";
		String text = "Text";
		String label = null;
		String value = null;
		
		Command(String c, String t, String l, String v)
		{
			if (v != null)
			{
				value = v;
				label = "Value: ";
			} 

			if (l != null) label = l;
		
			if (c != null) cover = c;
			if (t != null) text = t;
		}

		public String toString()
		{
			if (value != null)
				return "Command: " + cover + ", "
					+ text + ", "
					+ label + ", "
					+ value;
			else if (label != null)
				return "Command: " + cover + ", "
					+ text + ", "
					+ label;
			return "Command: " + cover + ", "
				+ text;
		}
	}

}
