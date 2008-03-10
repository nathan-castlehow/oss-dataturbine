/*
 * Created on Mar 5, 2004
 */
package org.nees.rbnb;

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.rbnb.sapi.*;

/**
 * @author terry
 */
public class StringSink extends RBNBBase {
	
	private static final String SINK_NAME = "GetSting";
	private static final String SOURCE_NAME = "Command";
	private static final String CHANNEL_NAME = "CommandData";
	
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
	
	public static void main(String[] args) {
		StringSink w = new StringSink();
		if (w.parseArgs(args))
		{
			w.exec();
			w.startThread();
		}
	}
	
    protected String getCVSVersionString()
    {
        return
            "  CVS information... \n" +
            "  $Revision: 153 $\n" +
            "  $Date: 2007-09-24 13:10:37 -0700 (Mon, 24 Sep 2007) $\n" +
            "  $RCSfile: StringSink.java,v $ \n";
    }

	/* (non-Javadoc)
	 * @see org.nees.rbnb.RBNBBase#setOptions()
	 */
	protected Options setOptions() {
		Options opt = setBaseOptions(new Options()); // uses h, s, p
		opt.addOption("k",true,"Sink Name *" + SINK_NAME);
		opt.addOption("n",true,"source_name *" + SOURCE_NAME);
		opt.addOption("c",true,"channel_name *" + CHANNEL_NAME);
		return opt;
	}
	
	/* (non-Javadoc)
	 * @see org.nees.rbnb.RBNBBase#setArgs(org.apache.commons.cli.CommandLine)
	 */
	protected boolean setArgs(CommandLine cmd) {

		if (!setBaseArgs(cmd)) return false;

		if (cmd.hasOption('n')) {
			String a=cmd.getOptionValue('n');
			if (a!=null) sourceName=a;
		}
		if (cmd.hasOption('c')) {
			String a=cmd.getOptionValue('c');
			if (a!=null) channelName=a;
		}
		if (cmd.hasOption('k')) {
			String a=cmd.getOptionValue('k');
			if (a!=null) sinkName=a;
		}

		requestPath = sourceName + "/" + channelName;
		
		System.out.println("Starting StringSink on " + getServer() + " as " + sinkName);
		System.out.println("  Requesting " + requestPath);
		System.out.println("  Use StringSink -h to see optional parameters");
		return true;
	}
	
	public void exec()
	{
		try {
			// Create a sink and connect:
			sink=new Sink();
			sink.OpenRBNBConnection(getServer(),sinkName);
			sMap = new ChannelMap();
			index = sMap.Add(requestPath);
			sink.Subscribe(sMap,"newest");
			connected = true;
			System.out.println("StringSink: Connection made to server = "
				+ getServer() + " as " + sinkName 
				+ " requesting " + requestPath + ".");
		} catch (SAPIException se) { se.printStackTrace(); }
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
		System.out.println("StringSink: Started thread.");
	}

	public void stopThread()
	{
		runit = false;
		stringDataThread.interrupt();
		System.out.println("StringSink: Stopped thread.");
	}
	
	private void runWork ()
	{
		try {
			while(isRunning())
			{
				ChannelMap m = sink.Fetch(-1);
				if (m == null)
				{
					System.out.println("Data fetch failed.");
					continue;
				}
				String[] st = m.GetDataAsString(index);
				System.out.println("Command(s) Received: ");
				for (int i = 0; i < st.length; i++)
				{
					System.out.println(st[i]);
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

}
