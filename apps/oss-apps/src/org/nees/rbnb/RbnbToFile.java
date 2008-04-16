/*
 * Created on May 10, 2005
 */
package org.nees.rbnb;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.File;
import java.util.Vector;
import java.util.Iterator;
import java.util.Date;
import java.util.TimeZone;
import java.util.Arrays;
import java.text.SimpleDateFormat;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Sink;

/**
 * Gather data from multiple channels and write the results into a file.
 * In the default operation, some assumptions are made that the data is "timed" 
 * by a particular channel and the the data for the other channels have identical
 * timestamps. This assumption can be relaxed. The code will
 * work if the data on the other channesl have timestamps that are different. If the
 * timestamps are syncronized, one channel can be identified as the "timing channel"
 * and data will be timestamped according to that channel. In the case that the data
 * is not syncronized, however, data will be grouped according to it's nearness to the
 * latest time, and put on RBNB when the time span of that grouping exceeds a small
 * threshold. In that case, a record with missing values may be generated.
 * 
 * The data is written to the file using this format:
 * 
 * Active channels: ATL1,ATT1,ATL3,ATT3
 * Channel units: g,g,in,kip
 * 
 * Time ATL1    ATT1    ATL3    ATT3
 * 2002-11-13T15:48:55.26499    2.71828E0   3.1415E0    0.0000E0    6.6600E2
 * 2002-11-13T15:48:55.41499    8.67531E6   6.0200E23       4.2000E1 
 * 
 * Values are seperated by tab characters (\t) and missing values are indicated
 * with a sequence of two tab in a row and no intervining space (e.g. \t\t). Note
 * scientific notation for the numbers.
 * 
 * See  http://it.nees.org/documentation/pdf/TR-2004-35.pdf, specificially the
 * section entitled (13) Data File Format. 
 * 
 * For an application that captures each channel to a seperate file with its own
 * timestamp, see GrabDataMultipleSink.
 * 
 * @see GrabDataMultipleSink
 * @see FileToRbnb
 * 
 * @author Terry E Weymouth
 * @version CVS $Revision$
 * 
 */
public class RbnbToFile  extends RBNBBase {

	private static final String SINK_NAME = "CollectData";
	private static final String ARCHIVE_DIRECTORY = ".";
	private static final String DATA_FILE_NAME = "Data.txt";
	
	private static final SimpleDateFormat OUTPUT_FORMAT = new SimpleDateFormat("yyyy-MM-dd:hh:mm:ss.SSS");
	private static final SimpleDateFormat COMMAND = new SimpleDateFormat("yyyy-MM-dd:HH:mm:ss.SSS");
	private static final TimeZone TZ = TimeZone.getTimeZone("GMT");

    private static final String delimiter = FileToRbnb.DELIMITER;
    
	static
	{
		OUTPUT_FORMAT.setTimeZone(TZ);
		COMMAND.setTimeZone(TZ);
	}

	private String archiveDirectory = ARCHIVE_DIRECTORY;
	private String dataFileName = DATA_FILE_NAME;
	private String sinkName = SINK_NAME;
	private String channelPathPattern = null;
	private String channelPathListString = null;
	private String timingChannelName = null;
	private String[] channelPathArray;
	private String[] shortNameArray;
	private String[] channelUnits;
	private boolean includeHidden = false;
	private double[] fill = null;
	
	private String startTimeString = "now";
	private double startTime = 0.0;
	private String endTimeString = "forever";
	private double endTime = 0.0;
	private double duration = 0.0;

	private Sink sink = null;
	private boolean connected = false;
	
	private Thread gatherThread;
	private boolean runit = false;
	
//	private String outFilePath = null;
	private PrintWriter	out = null;
	
	public static void main(String[] args) {
		// start from command line
		RbnbToFile w = new RbnbToFile();
		if (w.parseArgs(args))
		{
			w.exec();
		}
	}

    protected String getCVSVersionString()
    {
        return
            "  CVS information... \n" +
            "  $Revision$\n" +
            "  $Date$\n" +
            "  $RCSfile: RbnbToFile.java,v $ \n";
    }

	private void exec() {
        try {
            setupChannelArrays();
            channelUnits = ChannelUtility.getUnits(getServer(),
                    channelPathArray);
            initDataFill();
            if (connect())
            {
                initializeFile();
                startThread();
            }
        } catch (Throwable t) {
            if (out != null)
                out.close();
            if (connected)
                disconnect();
            t.printStackTrace();
        }
    }

	/* (non-Javadoc)
	 * @see org.nees.rbnb.RBNBBase#setOptions()
	 */
	protected Options setOptions() {
		Options opt = setBaseOptions(new Options()); // uses h, s, p
		opt.addOption("k",true,"Sink Name *" + SINK_NAME);
		opt.addOption("c",true,"Data Channels Path Pattern - Perl-like pattern to match");
		opt.addOption("C",true,"Data Channels list - comma seperated list of Paths");
		opt.addOption("T",true,"Data Channel for timing - defaults to the first channel in the list");
		opt.addOption("x",false,"Flag to include Hidden channels");
		opt.addOption("d",true,"Archive directory root *" + ARCHIVE_DIRECTORY);
		opt.addOption("f",true,"Output file name *" + DATA_FILE_NAME);
		opt.addOption("S",true,"Start time (defauts to now)");
		opt.addOption("E",true,"End time (defaults to forever)");
		opt.addOption("D",true,"Duration, floating point seconds");
		setNotes("Writes data between start time and end time to " +
			"the specified file on the given directory" +
			"Time format is yyyy-mm-dd:hh:mm:ss.nnn. " +			"a zero or unspecified start time means 'now'; " +			"a zero or unspecified end time means 'forever'. " +			"If a duration is specifed, it overrides End time.");
		return opt;
	} // setOptions();

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
		if (cmd.hasOption('T')) {
			String a=cmd.getOptionValue('T');
			if (a!=null) timingChannelName=a;
		}
		if (cmd.hasOption('x')) {
			includeHidden = true;
		}
		if (cmd.hasOption('d')) {
			String a=cmd.getOptionValue('d');
			if (a!=null) archiveDirectory=a;
		}
		if (cmd.hasOption('f')) {
			String a=cmd.getOptionValue('f');
			if (a!=null) dataFileName=a;
		}
		if (cmd.hasOption('S')) {
			String a=cmd.getOptionValue('S');
			if (a!=null)
			{
				try
				{
					startTimeString = a;
					Date d = COMMAND.parse(a);
					long t = d.getTime();
					startTime = ((double)t)/1000.0;				
				}
				catch (Exception e)
				{
					System.out.println("Parse of start time failed " + a);
					printUsage();
					return false;
				}
			}
		}
		if (cmd.hasOption('E')) {
			String a=cmd.getOptionValue('E');
			if (a!=null)
			{
				try
				{
					endTimeString = a;
					Date d = COMMAND.parse(a);
					long t = d.getTime();
					endTime = ((double)t)/1000.0;				
				}
				catch (Exception e)
				{
					System.out.println("Parse of end time failed " + a);
					printUsage();
					return false;
				}
			}
		}
		if (cmd.hasOption('D')) {
			String a=cmd.getOptionValue('D');
			if (a!=null)
			{
				try
				{
					duration = Double.parseDouble(a);
				}
				catch (Exception e)
				{
					System.out.println("Parse of duration failed " + a);
					printUsage();
					return false;
				}
			}
		}

		if (duration > 0.0)
		{
			long unixTime = System.currentTimeMillis();
			startTimeString = COMMAND.format(new Date(unixTime));
			startTime = ((double)unixTime)/1000.0; // in seconds
			double time = startTime + duration;
			endTimeString = COMMAND.format(new Date((long)(time * 1000.0)));
			if (endTime > 0.0)
			{
				System.out.println("Warning - both duration and end time were specified, " +					"duration overrides endtime. New endtime is " + endTimeString);
			}			
			endTime = time;
			System.out.println("Using duration updates endTime and set startTime when it is not set.");
			System.out.println("  StartTime = " + startTimeString + "; EndTime = " + endTimeString);
		}

		if ((startTime != 0.0) && (endTime != 0.0) && (startTime >= endTime))
		{
			System.out.println("StartTime = " + startTimeString + "; EndTime = " + endTimeString);
			System.out.println("  End time (" + endTime + ") " +
				"does not come after start time (" + startTime + ").");
			return false;
		}

		System.out.println("User Supplied parameters (or relivent default values) are:");
		System.out.println("  Server (combines host and port) = " + getServer());
		System.out.println("  Sink name = " + sinkName);
		System.out.println("  Data Channel Path pattern = " + channelPathPattern);
		System.out.println("  Data Channel Path list = " + channelPathListString);
		System.out.println("  Timing Channel name = " + timingChannelName);
		System.out.println("  Flag to include hidden channel value = " + includeHidden);
		System.out.println("  Archive directory = " + archiveDirectory);
		System.out.println("  Data File name = " + dataFileName);
		System.out.println("  Start Time = " + startTimeString);
		System.out.println("  End Time = " + endTimeString);
		System.out.println("  Duration = " + duration);
		System.out.println("");
		System.out.println("Use RbnbToFile -h to see parameters");
		System.out.println("");

		return true;
	} // setArgs(CommandLine cmd)

    /**
     * Set the class instance parameters. 
     * 
     * @param serverName - the name of the RBNB server (e.g. neestpm.sdsc.edu)
     * @param serverPort - the (String) RBNB server port (e.g. 3333)
     * @param sinkName - the name of the RBNB sink to use (null for default 
     *      of "CollectData" - see the static variable SINK_NAME)
     * @param channelPathArray - (String[]) if non-null, the (initial) array 
     *      of Source/Channel names on the server; all such names that match
     *      name of Source/Channel paths on the server will be captured to
     *      the file
     * @param timingChannelName - (String) the name of a channel to take
     *      (and force) the time stamps from; if this channel path is 
     *      non-null and on the server, them the time stamps on this channel
     *      are used as the time stamps for each successive data on each
     *      recorded channel, ignoring the time stamps on those channels; 
     *      if this is null, then each channels data is stored at its own 
     *      timestamp with data within a small time epsilon assumed to be
     *      from identical time stamps.
     * @param includeHidden - (booelan) if true include the "hidden channels"
     *      on the server in the list of candidate channels for the
     *      channelPathArray and timingChannelName; if false, the hidden 
     *      channels are not available for consideration
     * @param archiveDirectory - the name of the archive directory to use
     * @param dataFileName - the name of the file on the archive directory
     *      to use
     * @param startTime - (long) the start time of the data to gather (all
     *      channels); if 0 then start immediatly (using local "now" as the 
     *      start time) and treat the end time as the duration
     * @param endTime - (long) the end time of the data to gather (all 
     *      channels); if start time is zero, this is time is treated 
     *      as the duration (added to "now" to get the real end time)
     * 
     * @return (boolean) true if the parameters are valid, false otherwise
     * 
     */
    public boolean setArgs(String serverName, String serverPort, 
            String  sinkName,
            String[] channelPathArray, String timingChannelName,
            boolean includeHidden,
            String  archiveDirectory, String dataFileName,
            double startTime, double endTime)
    {
        this.setServerName(serverName);
        this.setServerPort(serverPort);
        this.sinkName = sinkName;
        this.channelPathArray = channelPathArray;
        this.timingChannelName = timingChannelName;
        this.archiveDirectory = archiveDirectory;
        this.dataFileName = dataFileName;
        this.includeHidden = includeHidden;
        this.startTime = startTime;
        this.endTime = endTime;

        if (duration > 0.0)
        {
            long unixTime = System.currentTimeMillis();
            startTimeString = COMMAND.format(new Date(unixTime));
            startTime = ((double)unixTime)/1000.0; // in seconds
            double time = startTime + duration;
            endTimeString = COMMAND.format(new Date((long)(time * 1000.0)));
            if (endTime > 0.0)
            {
                System.out.println("Warning - both duration and end time were specified, " +
                    "duration overrides endtime. New endtime is " + endTimeString);
            }           
            endTime = time;
            System.out.println("Using duration updates endTime and set startTime when it is not set.");
            System.out.println("  StartTime = " + startTimeString + "; EndTime = " + endTimeString);
        }

        if ((startTime != 0.0) && (endTime != 0.0) && (startTime >= endTime))
        {
            System.out.println("StartTime = " + startTimeString + "; EndTime = " + endTimeString);
            System.out.println("  End time (" + endTime + ") " +
                "does not come after start time (" + startTime + ").");
            return false;
        }

        System.out.println("User Supplied parameters (or relivent default values) are:");
        System.out.println("  Server (combines host and port) = " + getServer());
        System.out.println("  Sink name = " + sinkName);
        System.out.println("  Data Channel Path pattern = " + channelPathPattern);
        System.out.println("  Data Channel Path list = " + channelPathListString);
        System.out.println("  Timing Channel name = " + timingChannelName);
        System.out.println("  Flag to include hidden channel value = " + includeHidden);
        System.out.println("  Archive directory = " + archiveDirectory);
        System.out.println("  Data File name = " + dataFileName);
        System.out.println("  Start Time = " + startTimeString);
        System.out.println("  End Time = " + endTimeString);
        System.out.println("  Duration = " + duration);
        System.out.println("");
        System.out.println("Use RbnbToFile -h to see parameters");
        System.out.println("");
        
        return true;
    }
    
	private void setupChannelArrays() throws IllegalArgumentException, SAPIException
	{
		Vector channelPathList = new Vector();
		Iterator channels;

		if ((channelPathPattern != null) && !channelPathPattern.equals(""))
			channelPathList = ChannelUtility.appendChannelListFromPattern(
					getServer(), includeHidden,	channelPathPattern, channelPathList);
		if ((channelPathListString != null) && !channelPathListString.equals(""))
			channelPathList = ChannelUtility.appendChannelListFromString(
					getServer(), includeHidden,	channelPathListString, channelPathList);
		
		if (channelPathList.size() == 0)
		{
			String message = "RbnbToFile: No data channels to monitor: ";
			if (channelPathPattern == null)
				message += "channelPathPattern is null";
			else if (!channelPathPattern.equals(""))
				message += "channelPathPattern is empty (zero length)";
			else
				message += "channelPathPattern = " + channelPathPattern;
				
			message += " and ";

			if (channelPathListString == null)
				message += "channelPathListString is null";
			else if (!channelPathListString.equals(""))
				message += "channelPathListString is empty (zero length)";
			else
				message += "channelPathListString = " + channelPathListString;
			throw new IllegalArgumentException(message);
		}

		// the default timing channel is the read of the list, the first channel
		// if the user has singified a different channel, then move that channel
		//    to the head of the list, or (if it is not in the list) add it at
		//    the head of the list
		if (timingChannelName != null) // move this channel to the head of the list	
		{
			ChannelUtility.NodeCover found = null;
			channels = channelPathList.iterator();
			while (channels.hasNext() && (found == null))
			{
				ChannelUtility.NodeCover candidate = 
						(ChannelUtility.NodeCover)channels.next();
				if (candidate.getFullName().equals(timingChannelName))
					 found = candidate;
			}
			if (found == null)
			{
				channelPathList = ChannelUtility.appendChannelListFromString(
					getServer(), includeHidden,	timingChannelName, channelPathList);
				// if there, it will be the last element
				found = (ChannelUtility.NodeCover)channelPathList.lastElement();
				if (!(found.getFullName().equals(timingChannelName)))
					throw new IllegalArgumentException("Timing channel not found");
			}
			channelPathList.remove(found);
			channelPathList.insertElementAt(found,0);
		}
		
		channelPathArray = new String[channelPathList.size()];
		shortNameArray = new String[channelPathList.size()];
		channels = channelPathList.iterator();

		for (int i = 0; i < channelPathArray.length; i++)
		{
			ChannelUtility.NodeCover candidate = (ChannelUtility.NodeCover)channels.next();
			channelPathArray[i] = candidate.getFullName();
			shortNameArray[i] = candidate.getName();
		}
	} // setupChannelList()

	private void initDataFill() {
		fill = new double[channelPathArray.length];
		Arrays.fill(fill,0);
	}

	private boolean connect() throws SAPIException 
	{
		// connect to the server
		sink = new Sink();
		sink.OpenRBNBConnection(getServer(),sinkName);
		ChannelMap cMap = new ChannelMap();
		for (int i = 0; i < channelPathArray.length; i++)
		{
			cMap.Add(channelPathArray[i]);
		}
		sink.Subscribe(cMap);
		System.out.println("RbnbToFile: Connected on " + getServer() + " as " + sinkName);
		System.out.println("Gatering data from: ");
		for (int i = 0; i < channelPathArray.length; i++)
		{
			if (channelUnits[i].length() > 0)
				System.out.println("  " + channelPathArray[i] +
				"; units = " + channelUnits[i]);
			else
				System.out.println("  " + channelPathArray[i]);
		}
		System.out.println("With timing channel = " + channelPathArray[0]);
		connected = true;
		
		return connected;
	} // connect()
	
	private void disconnect()
	{
		sink.CloseRBNBConnection();
		connected = false;
	}
	
	/* (non-Javadoc)
	 * @see org.nees.rbnb.GrabDataMultipleSInk#initializeFile
	 */
	private void initializeFile() throws IOException {
        initializeFile(archiveDirectory, dataFileName, shortNameArray, channelUnits);
    }
    
    public void initializeFile(String archiveDirectory, String dataFileName,
            String[] shortNameArray, String[] channelUnits) throws IOException {

        String outFilePath = archiveDirectory + File.separator + dataFileName;
        System.out.println("Initializing file at " + outFilePath);
        File probe = new File(outFilePath);
        if (probe.exists()) {
            System.out.println("Aborting: File already exists at: "
                    + outFilePath);
            throw new IOException("File already exists: " + outFilePath);
        }

        out = new PrintWriter(new FileWriter(outFilePath));

        // write header
        /*
         * Active channels: ATL1,ATT1,ATL3 Channel units: g,g,in,kip
         * 
         * Time ATL1 ATT1 ATL3 ATT3
         */
        out.print("Active channels: ");
        out.print(shortNameArray[0]);
        for (int i = 1; i < shortNameArray.length; i++) {
            out.print("," + shortNameArray[i]);
        }
        out.println();

        out.print("Channel units: ");
        out.print(channelUnits[0]);
        for (int i = 1; i < shortNameArray.length; i++) {
            out.print("," + channelUnits[i]);
        }
        out.println();

        out.println();

        // data 'column' headers
        out.print("Time");
        for (int i = 0; i < shortNameArray.length; i++) {
            out.print(delimiter + shortNameArray[i]);
        }
        out.println();

    } // initializeFile()
	
	/* (non-Javadoc)
	 * @see org.nees.rbnb.GrabDataMultipleSInk#writeLineToFile
	 */
	public void writeLineToFile(double time, double[] data) throws IOException
	{
		// write data
		long unixTime = (long)(time * 1000.0); // convert sec to millisec
		//  SimpleDateFormat("yyyy-MM-dd:hh:mm:ss.SSS");
		String timeStr = OUTPUT_FORMAT.format(new Date(unixTime));
		timeStr = timeStr.substring(0,10) + "T" + timeStr.substring(11);
		out.print(timeStr);
		for (int i = 0; i < data.length; i++)
		{
			out.print(delimiter + data[i]);
		}
		out.println();
		out.flush();
	} // writeLineToFile
    
    /*
     * Write a line of data to the file were some values may be missing...
     * @see writeLineToFile(double time, double[] data)
     */
    public void writeLineToFile(double time, double[] data, boolean[] yes) throws IOException
    {
        // write data
        long unixTime = (long)(time * 1000.0); // convert sec to millisec
        //  SimpleDateFormat("yyyy-MM-dd:hh:mm:ss.SSS");
        String timeStr = OUTPUT_FORMAT.format(new Date(unixTime));
        timeStr = timeStr.substring(0,10) + "T" + timeStr.substring(11);
        out.print(timeStr);
        for (int i = 0; i < data.length; i++)
        {
            if (yes[i])
                out.print(delimiter + data[i]);
            else
                out.print(delimiter);
        }
        out.println();
        out.flush();
    } // writeLineToFile
    
	public void closeFile()
	{
		out.close();		
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
		gatherThread = new Thread(r, "RbnbToFile");
		gatherThread.start();
		System.out.println("RbnbToFile: Started thread.");
	}

	public void stopThread()
	{
		runit = false;
		gatherThread.interrupt();
		System.out.println("RbnbToFile: Stopped thread.");
	}
	
	public boolean isRunning()
	{
		return (connected && runit);
	}

	private void runWork ()
	{
		double[] fetchTime = new double[1];
		double[] fetchData = null;
		try {
			while(isRunning())
			{
				fetchData = fetchData(fetchTime);
				writeLineToFile(fetchTime[0],fetchData);
			}
		} catch (Exception se) {
			se.printStackTrace(); 
		}
		closeFile();
		disconnect();
		gatherThread = null;
	} // runWork ()

	private double[] fetchData(double[] fetchTime)
		throws SAPIException
	{
		TimeIterator time = new TimeIterator();
		DataIterator data = new DataIterator();
		
		while (! (time.hasNext() && data.hasNext()))
			updateTimeAndData(time, data);
	
		fetchTime[0] = ((double[])(time.next()))[0];
		return (double[])data.next();		
	}

	private void updateTimeAndData(TimeIterator time, DataIterator data)
		throws SAPIException
	{
		ChannelMap cMap = sink.Fetch(-1);
		int channelIndex = cMap.GetIndex(channelPathArray[0]);
		if (channelIndex < 0) throw new SAPIException("Could not access channle " +			channelPathArray[0]);
		double[] baseTimes = cMap.GetTimes(channelIndex);
		double[][] timeHolder = new double[channelPathArray.length][0];
		double[][] dataHolder = new double[channelPathArray.length][0];
		for (int i = 0; i < channelPathArray.length; i++)
		{
			channelIndex = cMap.GetIndex(channelPathArray[i]);
			if (channelIndex > -1)
			{
				timeHolder[i] = cMap.GetTimes(channelIndex);
				dataHolder[i] = cMap.GetDataAsFloat64(channelIndex);
			}
		}
		
		double[][] dataArray = new double[baseTimes.length][channelPathArray.length];
		int[] frontier = new int[channelPathArray.length];
		Arrays.fill(frontier, 0);
		
		for (int tIndex = 0; tIndex < baseTimes.length; tIndex++ )
		{
			double now = baseTimes[tIndex];
			for (int cIndex =0; cIndex < channelPathArray.length; cIndex++)
			{
				double value = 0.0;
				if ((frontier[cIndex] < timeHolder[cIndex].length)
						&& (now <= timeHolder[cIndex][frontier[cIndex]]))
				{
					value = dataHolder[cIndex][frontier[cIndex]];
				}
				fill[cIndex] = value;
				dataArray[tIndex][cIndex] = value;
				++(frontier[cIndex]);
			}
		}
		time.update(baseTimes);
		data.update(dataArray);
	}
	
	/* (non-Javadoc)
	 * @see java.util.Iterator
	 */
	private class TimeIterator implements Iterator
	{
		Vector queue = new Vector();
		double[] times = new double[0];
		int index = 0;
		double[] time = new double[1];
		
		public boolean hasNext() {
			if (index < times.length) return true;
			// the array is exhausted
			if (queue.size() > 0) return true;
			// the backing store is exhausted
			return false;
		}

		public Object next() {
			if (!hasNext()) return null;
			if (index < times.length)
			{
				time[0] = times[index];
				index++;
				return time;
			}
			index = 0;
			times = (double [])queue.elementAt(0);
			queue.remove(0);
			return next();
		}

		public void remove() {
		}
		
		private void update(double[] update)
		{
			queue.add(update);
		}
	} // class TimeIterator
	
	/* (non-Javadoc)
	 * @see java.util.Iterator
	 */
	private class DataIterator implements Iterator
	{
		Vector queue = new Vector();
		double[][] dataGroup = new double[0][0];
		int index = 0;
		double[] data = null;
		
		public boolean hasNext() {
			if (index < dataGroup.length) return true;
			// the array is exhausted
			if (queue.size() > 0) return true;
			// the backing store is exhausted
			return false;
		}

		public Object next() {
			if (!hasNext()) return null;
			if (index < dataGroup.length)
			{
				data = dataGroup[index];
				index++;
				return data;
			}
			index = 0;
			dataGroup = (double [][])queue.elementAt(0);
			queue.remove(0);
			return next();
		}

		public void remove() {
		}
		
		private void update(double[][] update)
		{
			queue.add(update);
		}

	} // class DataIterator

}
