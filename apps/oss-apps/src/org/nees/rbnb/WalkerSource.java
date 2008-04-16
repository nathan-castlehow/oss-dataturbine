/*
 * Created on Feb 5, 2004
 *
 * A RBNB source that generates numbers in a bounded random walk.
 */

package org.nees.rbnb;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.rbnb.sapi.*;

/**
 * A RBNB source that generates numbers in a bounded random walk.
 * The numbers are generate at a regular interval (specified by the timerInterval
 * which defaults to 1 second).
 * 
 * @author Terry E Weymouth
 *
 */
public class WalkerSource extends RBNBBase{
	
	// set the source for the random walk data
	private SimpleRandomWalk base = new SimpleRandomWalk();
	
	private static final String SOURCE_NAME = "RandomWalk";
	private static final String CHANNEL_NAME = "RandomWalkData";
	private static final long TIMER_INTERVAL=1000;
	private static final int CHANNEL_COUNT = 1;
	private static final int DEFAULT_CACHE_SIZE = 900;
	private int cacheSize = DEFAULT_CACHE_SIZE;
	private static final int DEFAULT_ARCHIVE_SIZE = 0;
	private int archiveSize = DEFAULT_ARCHIVE_SIZE;
	
	private String sourceName = SOURCE_NAME;
	private String channelName = CHANNEL_NAME;
	private long timerInterval = TIMER_INTERVAL;
	private int channelCount = CHANNEL_COUNT;
	
	NeesSource source = null;
	ChannelMap sMap;
	int index;
	boolean connected = false;
	
	Thread timerThread;
	boolean runit = false;
	
	public static void main(String[] args) {
		// start from command line
		WalkerSource w = new WalkerSource();
		if (w.parseArgs(args))
		{
			w.connect();
			w.startThread();
		}
	}
	
    protected String getCVSVersionString()
    {
        return
            "  CVS information... \n" +
            "  $Revision$\n" +
            "  $Date$\n" +
            "  $RCSfile: WalkerSource.java,v $ \n";
    }

	/* (non-Javadoc)
	 * @see org.nees.rbnb.RBNBBase#setOptions()
	 */
	protected Options setOptions() {
		Options opt = setBaseOptions(new Options()); // uses h, s, p
		opt.addOption("n",true,"source_name *" + SOURCE_NAME);
		opt.addOption("c",true,"channel_name *" + CHANNEL_NAME);
		opt.addOption("m",true,"number of channels *" + CHANNEL_COUNT);
		opt.addOption("t",true,"timer_interval *" + TIMER_INTERVAL);
		opt.addOption("z",true,"cache size *" + DEFAULT_CACHE_SIZE);
		opt.addOption("Z",true,"archive size *" + DEFAULT_ARCHIVE_SIZE);
		return opt;
	}

	/* (non-Javadoc)
	 * @see org.nees.rbnb.RBNBBase#setArgs(org.apache.commons.cli.CommandLine)
	 */
	protected boolean setArgs(CommandLine cmd)
	{
		if (!setBaseArgs(cmd)) return false;

		if (cmd.hasOption('n')) {
			String a=cmd.getOptionValue('n');
			if (a!=null) sourceName=a;
		}
		if (cmd.hasOption('c')) {
			String a=cmd.getOptionValue('c');
			if (a!=null) channelName=a;
		}
		if (cmd.hasOption('t')) {
			String a=cmd.getOptionValue('t');
			if (a!=null) timerInterval=Long.parseLong(a);
		}
		if (cmd.hasOption('z')) {
			String a=cmd.getOptionValue('z');
			if (a!=null)
			try
			{
				Integer i =  new Integer(a);
				int value = i.intValue();
				cacheSize = value;
				System.out.println("sizes " + cacheSize + "," + value);
			}
			catch (Exception ignore) {} 
		}
		if (cmd.hasOption('Z')) {
			String a=cmd.getOptionValue('Z');
			if (a!=null)
			try
			{
				Integer i =  new Integer(a);
				int value = i.intValue();
				cacheSize = value;
			}
			catch (Exception ignore) {} 
		}
		if (cmd.hasOption('m')) {
			String a=cmd.getOptionValue('m');
			if (a!=null)
			try
			{
				Integer i =  new Integer(a);
				int value = i.intValue();
				channelCount = value;
				System.out.println("channel count " + channelCount + "," + value);
			}
			catch (Exception ignore) {} 
		}
		
        if ((archiveSize > 0) && (archiveSize < cacheSize)){
            System.err.println(
                "a non-zero archiveSize = " + archiveSize + " must be greater then " +
                    "or equal to cacheSize = " + cacheSize);
            return false;
        }

		System.out.println("Starting WalkerSource on " + getServer() + " as " + sourceName);
		System.out.println("  Channel name = " + channelName + "; timer interval = " + timerInterval);
		System.out.println("  Cache Size = " + cacheSize + " Channel Count = " + channelCount);
		System.out.println("  Use WalkerSource -h to see optional parameters");
		
		return true;
	}

	public void connect()
	{
		try {
            // Create a source and connect:
            if (archiveSize > 0)
                source=new NeesSource(cacheSize, "create", archiveSize);
            else
                source=new NeesSource(cacheSize, "none", 0);
			source.OpenRBNBConnection(getServer(),sourceName);
			String units = CHANNEL_NAME + "=number";
			for (int i = 1; i < channelCount; i++)
			{
				units += "," + CHANNEL_NAME + i + "=number";
			}
			source.postUnits(units);
			connected = true;
			System.out.println("WalkerSource: Connection made to server = "
				+ getServer() + " as " + sourceName + " with " + channelName + ".");
		} catch (SAPIException se) { se.printStackTrace(); }
	}

	private void disconnect() {
		source.CloseRBNBConnection();
		connected = false;
		source = null;
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
		timerThread = new Thread(r, "Timer");
		timerThread.start();
		System.out.println("WalkerSource: Started thread.");
	}

	public void stopThread()
	{
		if (!connected) return;
		
		runit = false;
		timerThread.interrupt();
		System.out.println("WalkerSource: Stopped thread.");
	}
	
	private void runWork ()
	{
		String[] names = new String[channelCount];
		names[0] = channelName;
		for (int i = 1; i < channelCount; i++)
		{
			names[i] = channelName + i;
		}
		try 
		{
			while(connected && runit)
			{
				// Push data onto the server:
				// System.out.print("Put new data to server: ");
				sMap = new ChannelMap();
				int index[] = new int[channelCount];
				for (int i = 0; i < channelCount; i++)
					index[i] = sMap.Add(names[i]);
				sMap.PutTimeAuto("timeofday");
				for (int i = 0; i < channelCount; i++)
				{
					double data[] = new double[1];
					data[0] = base.next();
					sMap.PutDataAsFloat64(index[i],data);
				}
				// System.out.println("" + data[0]);
				source.Flush(sMap);
				Thread.sleep(timerInterval);
			}
		} catch (SAPIException se) {
			se.printStackTrace(); 
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		stop();
	}
	
	public boolean isRunning()
	{
		return (connected && runit);
	}

	/* (non-Javadoc)
	 * @see org.nees.rbnb.RBNBBase#start()
	 */
	public boolean start() {
		if (isRunning()) return false;
		if (connected) disconnect();
		connect();
		if (!connected) return false;
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
