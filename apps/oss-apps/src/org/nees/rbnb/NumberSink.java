/*
 * Created on Mar 5, 2004
 */
package org.nees.rbnb;

import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;
import java.text.SimpleDateFormat;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.rbnb.sapi.*;

/**
 * @author Terry E Weymouth
 */
public class NumberSink extends RBNBBase{
	
	private static final String SINK_NAME = "GetNumber";
	private static final String SOURCE_NAME = "RandomWalk";
	private static final String CHANNEL_NAME = "RandomWalkData";
	
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM d, yyyy h:mm:ss.SSS aa");
	private static final TimeZone TZ = TimeZone.getTimeZone("GMT");

	static
	{
		DATE_FORMAT.setTimeZone(TZ);
	}

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
		NumberSink s = new NumberSink();
		if (s.parseArgs(args))
		{
			s.exec();
			s.startThread();
		}
	}
	
    protected String getCVSVersionString()
    {
        return
            "  CVS information... \n" +
            "  $Revision: 153 $\n" +
            "  $Date: 2007-09-24 13:10:37 -0700 (Mon, 24 Sep 2007) $\n" +
            "  $RCSfile: NumberSink.java,v $ \n";
    }

	/* (non-Javadoc)
	 * @see org.nees.rbnb.RBNBBase#setOptions()
	 */
	protected Options setOptions() {
		Options opt = setBaseOptions(new Options()); // uses h, s, p
		opt.addOption("k",true,"Sink Name *" + SINK_NAME);
		opt.addOption("n",true,"Source Name *" + SOURCE_NAME);
		opt.addOption("c",true,"Source Channel Name *" + CHANNEL_NAME);
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
		
		System.out.println("Starting NumberSink on " + getServer() + " as " + sinkName);
		System.out.println("  Requesting " + requestPath);
		System.out.println("  Use NumberSink -h to see optional parameters");
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
			System.out.println("NumberSink: Connection made to server = "
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
		System.out.println("NumberSink: Started thread.");
	}

	public void stopThread()
	{
		runit = false;
		stringDataThread.interrupt();
		System.out.println("NumberSink: Stopped thread.");
	}
	
	private void runWork ()
	{
		try {
			while(isRunning())
			{
				ChannelMap m = sink.Fetch(-1);
				double[] times = m.GetTimes(index);
				double[] data = m.GetDataAsFloat64(index);
				
				for (int i = 0; i < data.length; i++)
				{
					long unixTime = (long)(times[i] * 1000.0); // convert sec to millisec
					String time = DATE_FORMAT.format(new Date(unixTime));
					System.out.println(time + "(GMT): " + data[i]);
				}
				System.out.println();
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
