/*
 * Created on May 12, 2004
 * ******************************************************************************
 * CVS Header $Header: /disks/cvs/neesgrid/turbine/src/org/nees/rbnb/DataVideoGather.java,v 1.22 2005/07/26 19:01:12 weymouth Exp $
 * ******************************************************************************
 */
package org.nees.rbnb;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.File;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Iterator;
import java.util.Stack;
import java.util.Date;
import java.util.TimeZone;
import java.text.SimpleDateFormat;

import java.util.regex.*;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.rbnb.sapi.*;

/**
 * Gather data from multiple channels and make a video of the images on a
 * related video channel. The collection is done on a periotic bases and the
 * results are writen to a target file. The data is written to the file
 * using this format:
 * Active channels: ATL1,ATT1,ATL3,ATT3
 * Sample rate: 10.000000
 * Channel units: g,g,in,kip
 * Time ATL1 ATT1 ATL3 ATT3
 * 2002-11-13T15:48:55.26499 -0.006409 0.004272 -0.008850 -0.007935
 * 2002-11-13T15:48:55.36499 -0.005798 -0.003662 -0.009766 -0.006714
 * 2002-11-13T15:48:55.46499 -0.005798 -0.003662 -0.009766 -0.006714
 * 2002-11-13T15:48:55.56499 -0.005798 -0.003662 -0.009766 -0.006714
 * 
 * @author Terry E Weymouth
 * @version $Revision: 153 $
 * 
 */
public class DataVideoGather extends RBNBBase {
	
	private static final String SINK_NAME = "DataVideoGather";
	private static final String ARCHIVE_DIRECTORY = ".";
	private static final String DATA_FILE_NAME = "Data.txt";
	private static final String MOVIE_FILE_NAME = "Movie.mov";

	private static final String CHANNEL_PATTERN = "";
	private static final String CHANNEL_LIST = "";
	private static final boolean INCLUDE_HIDDEN = false;
	private static final boolean ZERO_FOR_MISSING = false;
	private static final String VIDEO_SOURCE_PATH = "";
	
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM d, yyyy h:mm aa");
	private static final SimpleDateFormat INPUT_FORMAT = new SimpleDateFormat("yyyy-MM-dd:hh:mm:ss.SSS");
	private static final SimpleDateFormat OUTPUT_FORMAT = new SimpleDateFormat("yyyy-MM-dd:hh:mm:ss.SSS");
	private static final TimeZone TZ = TimeZone.getTimeZone("GMT");

	static
	{
		DATE_FORMAT.setTimeZone(TZ);
		INPUT_FORMAT.setTimeZone(TZ);
		OUTPUT_FORMAT.setTimeZone(TZ);
	}

	private static float USER_GIVEN_FRAME_RATE = 1.0F;
	private static int ITEMS_TO_SKIP = 0;
	
//	private static long WAIT_TIME = 30*60; // in seconds
//	private static long COLLECTION_TIME = WAIT_TIME;
	private static long DURATION = 30*60; // in seconds

	static
	{
		DATE_FORMAT.setTimeZone(TZ);
		OUTPUT_FORMAT.setTimeZone(TZ);
	}

	private String sinkName = SINK_NAME;
	private String channelPathPattern = null;
	private String channelPathListString = null;
	private Vector channelPathList = new Vector();
	private String videoChannelPath = null;
	private String[] channelPathArray;
	private String[] shortNameArray;
	private boolean includeHidden = false;
	private String archiveDirectory = ARCHIVE_DIRECTORY;
	private String dataFileName = DATA_FILE_NAME;
	private String movieFileName = MOVIE_FILE_NAME;
	private float userGivenFrameRate = USER_GIVEN_FRAME_RATE;
	private int itemsToSkip = ITEMS_TO_SKIP;
	private boolean repeatMissingValue  = true;

	private boolean connected = false;
	private boolean runit = true;
	private Thread fetchDataThread;
//	private long waitTime = WAIT_TIME; // in seconds
//	private long waitTime = 60; // one minute
	private long durationTime = DURATION;
	private String endTimeString = null;

	// This should be fixed now
//	private double uglyOffset = 5.0;
	
	private String[] parameterNameArray;
	private String[] parameterTypeArray;
	private Object[] parameterDefaultArray;
	
	public static void main(String[] args) {
		DataVideoGather d = new DataVideoGather();
		if (d.parseArgs(args))
		{
			d.testConnect();
			d.startThread();
		}
	} // main

    protected String getCVSVersionString()
    {
        return
            "  CVS information... \n" +
            "  $Revision: 153 $\n" +
            "  $Date: 2007-09-24 13:10:37 -0700 (Mon, 24 Sep 2007) $\n" +
            "  $RCSfile: DataVideoGather.java,v $ \n";
    }

	/* (non-Javadoc)
	 * @see org.nees.rbnb.RBNBBase#setOptions()
	 */
	protected Options setOptions() {
		Options opt = setBaseOptions(new Options()); // uses h, s, p
		opt.addOption("k",true,"Sink Name *" + SINK_NAME);
		opt.addOption("c",true,"Data Channels Path Pattern - Perl-like pattern to match");
		opt.addOption("C",true,"Data Channels list - comma seperated list of Paths");
		opt.addOption("x",false,"Flag to include Hidden channels");
		opt.addOption("Z",false,"Flag for 'zero for missing data'; otherwise last value repeats");
		opt.addOption("v",true,"Video Source path - no path means no movie");
		opt.addOption("d",true,"Archive directory root *" + ARCHIVE_DIRECTORY);
		opt.addOption("M",true,"Movie output file name *" + MOVIE_FILE_NAME);
		opt.addOption("D",true,"Data output file name *" + DATA_FILE_NAME);
		opt.addOption("f",true,"Frame Rate of final movie *" + USER_GIVEN_FRAME_RATE);
		opt.addOption("i",true,"Items to skip in data for each movie frame *" + 
			ITEMS_TO_SKIP);
		opt.addOption("t",true,"end time; detaults to 'now'");
		opt.addOption("W",true,"duration in seconds *" + DURATION);
		setNotes("start time can either be yyyy-mm-dd:hh:mm:ss.nnn or " +			"an arbitraty floating point number");
		return opt;
	} // getTimeOrDouble

	/* (non-Javadoc)
	 * @see org.nees.rbnb.RBNBBase#setArgs(org.apache.commons.cli.CommandLine)
	 */
	protected boolean setArgs(CommandLine cmd) {
		if (!setBaseArgs(cmd)) return false;

		if (cmd.hasOption('k')) {
			String a=cmd.getOptionValue('k');
			if (a!=null) sinkName=a;
		}
		if (cmd.hasOption('c')) {
			String a=cmd.getOptionValue('c');
			if (a!=null) channelPathPattern=a;
		}
		if (cmd.hasOption('C')) {
			String a=cmd.getOptionValue('C');
			if (a!=null) channelPathListString=a;
		}
		if (cmd.hasOption('W')) {
			String a=cmd.getOptionValue('W');
			if (a!=null)
			{
				try
				{
					long value = Long.parseLong(a);
					durationTime = value;
				}
				catch (Exception ex)
				{
					System.out.println("Failed to parse Collection Time (" + a + "): " + ex);
				}
			}
		}			
		if (cmd.hasOption('t')) {
			String a=cmd.getOptionValue('t');
			if (a!=null) endTimeString = a;
		}
		if (cmd.hasOption('x')) {
			String a=cmd.getOptionValue('x');
			if (a!=null)
			{
				if (a.equals("true")) includeHidden = true;
				if (a.equals("false")) includeHidden = false;
			} 
		}
		if (cmd.hasOption('v')) {
			String a=cmd.getOptionValue('v');
			if (a!=null) videoChannelPath=a;
		}
		if (cmd.hasOption('d')) {
			String a=cmd.getOptionValue('d');
			if (a!=null) archiveDirectory=a;
		}
		if (cmd.hasOption('M')) {
			String a=cmd.getOptionValue('M');
			if (a!=null) movieFileName=a;
		}
		if (cmd.hasOption('D')) {
			String a=cmd.getOptionValue('D');
			if (a!=null) dataFileName=a;
		}
		if (cmd.hasOption('f')) {
			String a=cmd.getOptionValue('f');
			if (a!=null)
			{
				try
				{
					float value = Float.parseFloat(a);
					userGivenFrameRate = value;
				}
				catch (Exception ex)
				{
					System.out.println("Failed to parse Frame Rate (" + a + "): " + ex);
				}
			}
		}
		if (cmd.hasOption('i')) {
			String a=cmd.getOptionValue('i');
			if (a!=null)
			{
				try
				{
					int value = Integer.parseInt(a);
					itemsToSkip = value;
				}
				catch (Exception ex)
				{
					System.out.println("Failed to parse Frame Rate (" + a + "): " + ex);
				}
			}
		}
		if (cmd.hasOption('Z')) {
			repeatMissingValue = false;
		}			

		System.out.println("User Supplied parameter (or relivent default value) are:");
		System.out.println("sink name = " + sinkName);
		System.out.println("data channel path pattern = " + channelPathPattern);
		System.out.println("data channel list = " + channelPathListString);
		System.out.println("include hidden channel value = " + includeHidden);
		System.out.println("zero for missing data = " + !repeatMissingValue);
		System.out.println("video source path = " + videoChannelPath);
		System.out.println("archive directory = " + archiveDirectory);
		System.out.println("movie file name = " + movieFileName);
		System.out.println("data file name = " + dataFileName);
		System.out.println("frame rate of final movie = " + userGivenFrameRate);
		System.out.println("items to skip = " + itemsToSkip);
//		System.out.println("wait time between cycles = " + waitTime);
		System.out.println("duration = " + durationTime);
		System.out.println("end time string " + endTimeString);
		System.out.println("");
		System.out.println("Use DataVideoGather -h to see parameters");
		System.out.println("");

		setupChannelList();
		
		System.out.println("Starting DataVideoGather on " + getServer() + " as " + sinkName);
		System.out.println("Gatering data from: ");
		for (int i = 0; i < channelPathArray.length; i++)
		{
			System.out.println("  " + channelPathArray[i]);
		}
		if ((videoChannelPath != null) && (!videoChannelPath.equals("")))
		{
			System.out.println("Gatering video from:  " + videoChannelPath);
			System.out.println("Frame rate = " + userGivenFrameRate + " frames per second.");
			System.out.println("Data frames to skip between video frame = " + itemsToSkip);
		}
		
		return true;
	}

	private void setupChannelList()
	{
		channelPathList = new Vector();

		if ((channelPathPattern != null) && !channelPathPattern.equals(""))
			appendChannelListFromPattern();
		if ((channelPathListString != null) && !channelPathListString.equals(""))
			appendChannelListFromString();
	
		Iterator channels = channelPathList.iterator();
		if (!channels.hasNext())
		{
			System.out.println("DataVideoGather: No data channels to monitor.");
			this.printUsage();
			System.exit(0);
		}
			
		channelPathArray = new String[channelPathList.size()];
		shortNameArray = new String[channelPathList.size()];
		for (int i = 0; i < channelPathArray.length; i++)
		{
			ChannelTree.Node candidate = (ChannelTree.Node)channels.next();
			channelPathArray[i] = candidate.getFullName();
			shortNameArray[i] = candidate.getName();
		}
	}


	public void appendChannelListFromPattern()
	{
		try {
			// Create a sink and connect:
			Sink sink=new Sink();
			sink.OpenRBNBConnection(getServer(),sinkName);
			
			// get all the channel paths that match the pattern
			ChannelMap sMap = new ChannelMap();
			sink.RequestRegistration();		
			sMap = sink.Fetch(-1,sMap);
			ChannelTree tree = ChannelTree.createFromChannelMap(sMap);

			Pattern p = Pattern.compile(channelPathPattern);
			// for each channel path, check match, collect matches...
			
			Iterator nodes = tree.iterator();
			while (nodes.hasNext())
			{
				ChannelTree.Node n = (ChannelTree.Node)nodes.next();
				// System.out.println("Checking " + n.getFullName() + ";" + n.getName());
				if (!includeHidden && n.getFullName().startsWith("_")) continue;
				if (n.getType() != ChannelTree.CHANNEL) continue;
				String name = n.getFullName();
				Matcher m = p.matcher(name);
				if (m.matches())
				{
//					System.out.println("Matches");
					boolean isSource = false;
					ChannelTree.Node upNode = n.getParent();
					while ((!isSource) || (upNode != null))
					{
						if (upNode.getType() == ChannelTree.SOURCE) isSource = true;
						upNode = upNode.getParent();
					}
					if (isSource)
					{
						// System.out.println("... and is a source.");
						channelPathList.add(n);
					}
					else
					{
						// System.out.println("... and is NOT a source.");
					}
				}					
			}
			
		} catch (SAPIException se) { se.printStackTrace(); }
	} // appendChannelListFromPattern

	public void appendChannelListFromString()
	{
		try
		{
			StringTokenizer st = new StringTokenizer(channelPathListString,","); 
	
			// Create a sink and connect:
			Sink sink=new Sink();
			sink.OpenRBNBConnection(getServer(),sinkName);
				
			// get all the channel paths that match the pattern
			ChannelMap sMap = new ChannelMap();
			sink.RequestRegistration();		
			sMap = sink.Fetch(-1,sMap);
			ChannelTree tree = ChannelTree.createFromChannelMap(sMap);
	
			Pattern p = Pattern.compile(channelPathPattern);
			// for each channel path, check match, collect matches...

			while (st.hasMoreTokens()) {
				String path = st.nextToken();
//				System.out.println("Checking " + path);
							
				ChannelTree.Node n = tree.findNode(path);
				if (n == null) continue;
				if (n.getType() != ChannelTree.CHANNEL) continue;
				String name = n.getFullName();
//				System.out.println("Found it...");
				boolean isSource = false;
				ChannelTree.Node upNode = n.getParent();
				while ((!isSource) || (upNode != null))
				{
					if (upNode.getType() == ChannelTree.SOURCE) isSource = true;
					upNode = upNode.getParent();
				}
				if (isSource)
				{
//					System.out.println("... and is a source.");
					channelPathList.add(n);
				}
				else
				{
//					System.out.println("... and is NOT a source.");
				}
			} // while next token
		} catch (SAPIException se) { se.printStackTrace(); }
	} // appendChannelListFromString
	
	private void testConnect()
	{
		try {
			// test the connection to the server
			Sink sink = new Sink();
			sink.OpenRBNBConnection(getServer(),sinkName);
			connected = true;
			System.out.println("DataVideoGather: Test connection made to server = "
				+ getServer() + " as " + sinkName +".");
			sink.CloseRBNBConnection();
		} catch (SAPIException se) { se.printStackTrace(); }
	}
	
	private void disconnect()
	{
		// nothing, really, to do as the connections are closed each cycle???
		connected = false;
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
		fetchDataThread = new Thread(r, "DataVideoGather");
		fetchDataThread.start();
		System.out.println("DataVideoGather: Started thread.");
	}

	public void stopThread()
	{
		runit = false;
		fetchDataThread.interrupt();
		System.out.println("DataVideoGather: Stopped thread.");
	}
	
	private void runWork ()
	{
		long lastTime = System.currentTimeMillis();
		try {
//			while(isRunning())
//			{
//				doOneCycle();
//				long actualWait = (waitTime * 1000) - (System.currentTimeMillis() - lastTime);
//				lastTime = System.currentTimeMillis();
//				System.out.println("Actual Wait Time = " + actualWait);
//				if (actualWait < 0) actualWait = 0;
//				Thread.sleep(actualWait);				
//			}
		
			// modified to do one cycle only so that it can be 
			// scripted; tew Aug 02, 2004
			if (isRunning())
				doOneCycle();
				
			
				
		} catch (Exception se) {
			se.printStackTrace(); 
		}
		fetchDataThread = null;
	}
	
	public boolean isRunning()
	{
		return (connected && runit);
	}

	private void doOneCycle()
	{
		System.out.println("One Cycle:");
//		System.out.println("Wait time = " + waitTime);
		System.out.println("Duration time = " + durationTime);
		
		long startTime = System.currentTimeMillis();
		double dataEndTime = 0.0;
		
		if (endTimeString != null)
		try
		{
			dataEndTime = getTimeOrDouble(endTimeString);
		}
		catch (Throwable ignore){}

		DataVideoSink.DEBUG = true;
//		DataVideoSink.setUglyOffest(uglyOffset);
		DataVideoSink dataVideoSink = new DataVideoSink();
		dataVideoSink.connect(getServer(),sinkName + "_data");

		double[][] data = new double[channelPathArray.length][0];
		double[][] time = new double[channelPathArray.length][0];
		
		// collectionTime (long) in secods
		double duration = (double)durationTime; // in seconds
		
		if (dataVideoSink.connect(getServer(),sinkName))
		{
			for (int i = 0; i < channelPathArray.length; i ++)
			{
				String dataSourcePath = channelPathArray[i];
				if (dataEndTime > 0.0)
					dataVideoSink.fetchData(dataSourcePath, dataEndTime - duration, duration,"absolute");
				else
					// with "newest" time is figured backward from the present...
					dataVideoSink.fetchData(dataSourcePath, 0.0, duration,"newest");
				data[i] = dataVideoSink.getDataArray();
				time[i] = dataVideoSink.getTimeArray();
			}
			
//			for (int i = 0; i < channelPathArray.length; i ++)
//			{
//				System.out.println("Array lengths (" + i + ") are: "
//					+ data[i].length + "," + time[i].length);
//			}
			
			double[] referenceTime = time[0];
			double[][] filledData = new double[channelPathArray.length][referenceTime.length];
			int[] indexFront = new int[channelPathArray.length]; // initally zero
			int[] lastUsed = new int[channelPathArray.length];
			
			// force initial condition; no index last used
			for (int i = 0; i < lastUsed.length; i++) lastUsed[i] = -1;

			// for each possible data slot determine the data with the correct time
			// filled in slots are eather duplications or zero			
			for (int timeIndex = 0; timeIndex < referenceTime.length; timeIndex++)
			{
				// channel 0 is the referance channel, it's data value are always there
				filledData[0][timeIndex] = data[0][timeIndex];
				for (int channelIndex = 1; channelIndex < channelPathArray.length; channelIndex++)
				{
//System.out.println(" " + timeIndex + "," + channelIndex 
//	+ "," + lastUsed[channelIndex] + "," + indexFront[channelIndex]);

					if (lastUsed[channelIndex] == indexFront[channelIndex])
					{
						// filling data with and index that was already used
						if (repeatMissingValue)
							filledData[channelIndex][timeIndex]
								= data[channelIndex][indexFront[channelIndex]];
						else
							filledData[channelIndex][timeIndex] = 0.0;
					}
					else
					{
						filledData[channelIndex][timeIndex]
							= data[channelIndex][indexFront[channelIndex]];
						lastUsed[channelIndex] = indexFront[channelIndex];
					}
//System.out.print(" " + timeIndex + "," + channelIndex 
//	+ "," + referenceTime[timeIndex] + "," + time[channelIndex][indexFront[channelIndex]]);
//if (referenceTime[timeIndex] >= time[channelIndex][indexFront[channelIndex]])
//System.out.println(" yes");
//else
//System.out.println(" no");
					if (referenceTime[timeIndex] >= time[channelIndex][indexFront[channelIndex]])
					{
						if (indexFront[channelIndex] < (data[channelIndex].length - 1))
							++indexFront[channelIndex];
					}
				}
				
//				System.out.println("Time Index = " + timeIndex);
//				System.out.println("Filled data: ");
//				for (int i = 0; i < channelPathArray.length; i++)
//				{
//					System.out.print("  " + filledData[i][timeIndex]);
//				}
//				System.out.println();
//				System.out.println("Last Used and Front are: ");
//				for (int i = 0; i < channelPathArray.length; i++)
//				{
//					System.out.println("  " + i + ": " + lastUsed[i] + "," + indexFront[i]);
//				}
				
			}

			try {
				writeDataToFile(shortNameArray,referenceTime,filledData);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if ((videoChannelPath != null) && (!videoChannelPath.equals("")))
			{
				if (!probeVideo())
					System.out.println("No matching video available ");
				else
				{
					String outputURL = "file:" + archiveDirectory 
						+ "/" + movieFileName; 
					try {
						dataVideoSink.makeMovie(userGivenFrameRate, 
							outputURL, videoChannelPath, itemsToSkip);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}

		}
		dataVideoSink.disconnect();
		long endTime = System.currentTimeMillis();
		long elaspedTime = endTime - startTime;
		System.out.println("Cycle took " + elaspedTime + " milliseconds.");
	}
	
	private boolean probeVideo()
	{
		// TODO: fix test for vidoe data
		return true;
	}
	
	private void writeDataToFile(String[] names, double[] times, double[][] data) throws IOException
	{
		String path = archiveDirectory + "/" + dataFileName;
		PrintWriter	out = new PrintWriter(new FileWriter(path));
		
		// write header
		/*
		* Active channels: ATL1,ATT1,ATL3,ATT3
		* Sample rate: 10.000000
		* Channel units: g,g,in,kip (unknown at this time!)
		* Time ATL1 ATT1 ATL3 ATT3
		*/
		out.print("Active channels: ");
		out.print(names[0]);
		for (int i = 1; i < names.length; i++)
		{
			out.print("," + names[i]);
		}
		out.println();

		double rate = 0.0;
		for (int i = 1; i < times.length; i++)
		{
			rate += times[i]-times[i-1]; // in seconds
		}
		rate = ((double)(times.length - 1))/rate;
		out.println("Sample rate: " + rate);
		
		// Channel units are unknown at this time!
		
		out.print("Time");
		for (int i = 0; i < names.length; i++)
		{
			out.print(" " + names[i]);
		}
		out.println();

		// write data

		for (int i = 0; i < times.length; i++)
		{
			long unixTime = (long)(times[i] * 1000.0); // convert sec to millisec
			//  SimpleDateFormat("yyyy-MM-dd:hh:mm:ss.SSS");
			String time = OUTPUT_FORMAT.format(new Date(unixTime));
			time = time.substring(0,10) + "T" + time.substring(11);
			out.print(time);
			for (int j = 0; j < data.length; j++)
			{
				out.print(" " + data[j][i]);
			}
			out.println();
			out.flush();
		}
		out.close();
	}


	/* (non-Javadoc)
	 * @see org.nees.rbnb.RBNBBase#start()
	 */
	public boolean start() {
		if (isRunning()) return false;
		if (connected) disconnect();
		setupChannelList();
		testConnect();
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

	private double getTimeOrDouble(String arg) throws Exception
	{
		double value = 0.0;
		boolean gotit = false;
				
		try{
			Date d = INPUT_FORMAT.parse(arg);
			long t = d.getTime();
			value = ((double)t)/1000.0;
			gotit = true;
		} catch (Exception ignore)
		{
			gotit = false;
		}

		if (!gotit)
		try {
			value = Double.parseDouble(arg);
			gotit = true;
		} catch (Exception ignore)
		{
			gotit = false;
		}

		if (!gotit) throw(new Exception("Failed to parse time " + arg));		
		
		return value;

	}

}
