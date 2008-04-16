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
 * 
 * @author Terry E Weymouth
 */
public class NumberSubSample extends RBNBBase {

	private static final String SINK_NAME = "NumberSubSampleSink";
	private static final String SOURCE_NAME = "NumberSubSampleSource";
	private static final String CHANNEL_NAME = "SubSample";
	private static final int SKIP = 4;
	private static final String REQUEST_PATH = "undefined";

	private String sinkName = SINK_NAME;
	private String sourceName = SOURCE_NAME;
	private String channelName = CHANNEL_NAME;
	private String requestPath = REQUEST_PATH;
	private int skip = SKIP;
	private int count = 0;

	private static final int DEFAULT_CACHE_SIZE = 900;
	private int cacheSize = DEFAULT_CACHE_SIZE;
	private static final int DEFAULT_ARCHIVE_SIZE = 0;
	private int archiveSize = DEFAULT_ARCHIVE_SIZE;
			
	 
	Sink sink = null;
	Source source = null;
	boolean sinkConnected = false;
	boolean sourceConnected = false;	
	
	Thread stringDataThread;
	boolean runit = false;
	
	public static void main(String[] args) {
		NumberSubSample s = new NumberSubSample();
		if (s.parseArgs(args))
		{
			s.connect();
			s.startThread();
		}
	}
	
    protected String getCVSVersionString()
    {
        return
            "  CVS information... \n" +
            "  $Revision$\n" +
            "  $Date$\n" +
            "  $RCSfile: NumberSubSample.java,v $ \n";
    }

	/* (non-Javadoc)
	 * @see org.nees.rbnb.RBNBBase#setOptions()
	 */
	protected Options setOptions() {
		Options opt = setBaseOptions(new Options()); // uses h, s, p
		opt.addOption("S",true,"Source Name *" + SOURCE_NAME);
		opt.addOption("C",true,"Source Channel Name *" + CHANNEL_NAME);
		opt.addOption("K",true,"Sink Name *" + SINK_NAME);
		opt.addOption("x",true,"Samples to skip *" + SKIP);
		opt.addOption("z",true,"cache size *" + DEFAULT_CACHE_SIZE);
		opt.addOption("Z",true,"archive size *" + DEFAULT_ARCHIVE_SIZE);
		opt.addOption("P",true,"Request Path (required) e.g. \"Source/data\" ");
		return opt;
	}

	/* (non-Javadoc)
	 * @see org.nees.rbnb.RBNBBase#setArgs(org.apache.commons.cli.CommandLine)
	 */
	protected boolean setArgs(CommandLine cmd) {
		if (!setBaseArgs(cmd)) return false;

		if (cmd.hasOption('S')) {
			String a=cmd.getOptionValue('S');
			if (a!=null) sourceName=a;
		}
		if (cmd.hasOption('C')) {
			String a=cmd.getOptionValue('C');
			if (a!=null) channelName=a;
		}
		if (cmd.hasOption('K')) {
			String a=cmd.getOptionValue('K');
			if (a!=null) sinkName=a;
		}
		if (cmd.hasOption('P'))
		{
			String a=cmd.getOptionValue('P');
			if (a!=null) requestPath=a;
		}
		
		if(requestPath.equals(REQUEST_PATH))
		{
			System.out.println("Request Path is required");
			printUsage();
			return false;
		}

        if ((archiveSize > 0) && (archiveSize < cacheSize)){
            System.err.println(
                "a non-zero archiveSize = " + archiveSize + " must be greater then " +
                    "or equal to cacheSize = " + cacheSize);
            return false;
        }

		System.out.println("Starting NumberSubSample on " + getServer() + " as " + sinkName);
		System.out.println("  Requesting " + requestPath + ";\n  supplying number as " +
			"source = " + sourceName + ", channel = " + channelName);
		System.out.println("  Use NumberSubSample -h to see optional parameters");
		return true;
	}
	

	public void connect()
	{
		try {
            // Create a source and connect:
            if (archiveSize > 0)
                source=new Source(cacheSize, "create", archiveSize);
            else
                source=new Source(cacheSize, "none", 0);
            sink.OpenRBNBConnection(getServer(),sinkName);
			ChannelMap sMap = new ChannelMap();
			int index = sMap.Add(requestPath);
			sink.Subscribe(sMap,"newest");
			sinkConnected = true;
			System.out.println("NumberSubSample: Sink connection made to server = "
				+ getServer() + " as " + sinkName 
				+ " requesting " + requestPath + ".");
		} catch (SAPIException se) { se.printStackTrace(); }
		
		try {
			// Create a source and connect:
			if (archiveSize > 0)
				source=new Source(cacheSize, "create", archiveSize);
			else
				source=new Source(cacheSize, "none", 0);
			source.OpenRBNBConnection(getServer(),sourceName);
			sourceConnected = true;
			System.out.println("NumberSubSample: Source connection made to server = "
				+ getServer() + " as " + sourceName + " with " + channelName + ".");
		} catch (SAPIException se) { se.printStackTrace(); }
			
	}
	
	private void disconnect() {
		source.CloseRBNBConnection();
		source = null;
		sourceConnected = false;
		sink.CloseRBNBConnection();
		sink = null;
		sinkConnected = false;
	}

	private boolean connected()
	{
		return sourceConnected && sinkConnected;
	}
	
	public void startThread()
	{
		
		if (!connected()) return;
		
		// Use this inner class to hide the public run method
		Runnable r = new Runnable() {
			public void run() {
			  runWork();
			}
		};
		runit = true;
		stringDataThread = new Thread(r, "NumberSubSample");
		stringDataThread.start();
		System.out.println("NumberSubSample: Started thread.");
	}

	public void stopThread()
	{
		runit = false;
		stringDataThread.interrupt();
		System.out.println("NumberSubSample: Stopped thread.");
	}
	
	private void runWork ()
	{
		try {
			while(isRunning())
			{
				ChannelMap inMap = sink.Fetch(-1);
				double[] data = inMap.GetDataAsFloat64(0);
				double start = inMap.GetTimeStart(0);
				double duration = inMap.GetTimeDuration(0);
				count++;
				if (count > (skip + 1))
				{
					count = 0;
					ChannelMap outMap = new ChannelMap();
					int index = outMap.Add(channelName);
					outMap.PutTime(start, duration);
					outMap.PutDataAsFloat64(index,data);
					source.Flush(outMap);
				}
			}
		} catch (SAPIException se) {
			se.printStackTrace(); 
		}
		stringDataThread = null;
	}
	
	public boolean isRunning()
	{
		return (connected() && runit);
	}

	/* (non-Javadoc)
	 * @see org.nees.rbnb.RBNBBase#start()
	 */
	public boolean start() {
		if (isRunning()) return false;
		if (connected()) disconnect();
		connect();
		if (!connected()) return false;
		startThread();
		return true;
	}

	/* (non-Javadoc)
	 * @see org.nees.rbnb.RBNBBase#stop()
	 */
	public boolean stop() {
		if (!isRunning()) return false;
		stopThread();
		disconnect();
		return true;
	}

}
