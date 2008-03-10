/*
 * Created on Feb 4, 2005
 */
package org.nees.rbnb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.FileFilter;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Vector;
import java.util.Enumeration;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.rbnb.sapi.*;

/**
 * This class grabs images from an RBNB image source and saved then to a directory
 * structure where the image for the time stamp yyyy-MM-dd:hh:mm:ss.nnn is saved to
 * the file yyyyMMddhhmmssnnn.jpg on the directory path base-dir/yyyy/MM/dd/hh/mm/.
 * The splaying of files to directory structures is done to assue that no directory
 * overflows its index table. The complinment to this class is, JpgLoaderSink, which
 * loads the images that correspond to a given time range, back into RBNB.
 * 
 * @see JpgLoaderSink
 * 
 * @author Terry E. Weymouth
 */
public class JpgSaverSink extends RBNBBase {

	private static final String SINK_NAME = "JpgSaver";
	private static final String SOURCE_NAME = "VideoSource";
	private static final String CHANNEL_NAME = "video.jpg";

    public static final String PROPERTIES_FILE_NAME = ".properties";
	
	private String sinkName = SINK_NAME;
	private String sourceName = SOURCE_NAME;
	private String channelName = CHANNEL_NAME;
	private String requestPath = sourceName + "/" + channelName;

    private static final double DEFAULT_DURATION = 120.0;
	private String startTimeString = "now";
	private double startTime = 0.0;
	private String endTimeString = "forever";
	private double endTime = 0.0;
	private long early = 0;
	double estimatedDuration = DEFAULT_DURATION;
	double consumedTime = 0.0;
	double lastNotify = 0.0;
	
	private Sink sink = null;
	private boolean connected = false;
	private boolean statusOk = true;
	private Vector statusMessages = new Vector();
	
	private Thread stringDataThread;
	private boolean runit = false; // to signal stop
	private boolean stillRunning = false; // actually stopped
	
	public static final String BASE_DIR = "JpgStore";
	private String baseDir = BASE_DIR;
	
	public static void main(String[] args) {
		JpgSaverSink s = new JpgSaverSink();
		if (s.parseArgs(args) && s.setup())
			s.startThread();
	}

    protected String getCVSVersionString()
    {
        return
            "  CVS information... \n" +
            "  $Revision: 153 $\n" +
            "  $Date: 2007-09-24 13:10:37 -0700 (Mon, 24 Sep 2007) $\n" +
            "  $RCSfile: JpgSaverSink.java,v $ \n";
    }

	/* (non-Javadoc)
	 * @see org.nees.rbnb.RBNBBase#setOptions()
	 */
	protected Options setOptions() {
		Options opt = setBaseOptions(new Options()); // uses h, s, p
		opt.addOption("k",true,"Sink Name *" + SINK_NAME);
		opt.addOption("n",true,"Source Name *" + SOURCE_NAME);
		opt.addOption("c",true,"Source Channel Name *" + CHANNEL_NAME);
		opt.addOption("d",true,"Base directory path *" + BASE_DIR);
		opt.addOption("S",true,"Start time (defauts to now)");
		opt.addOption("E",true,"End time (defaults to forever)");
		setNotes("Writes jpg imamges between start time and end time to " +			"the directory structure starting at the base directory. " +			"Time format is yyyy-mm-dd:hh:mm:ss.nnn.");
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
		if (cmd.hasOption('d')) {
			String a=cmd.getOptionValue('d');
			if (a!=null) baseDir=a;
		}
		if (cmd.hasOption('S')) {
			String a=cmd.getOptionValue('S');
			if (a!=null)
			{
				try
				{
					startTimeString = a;
					Date d = ArchiveUtility.getCommandFormat().parse(a);
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
					Date d = ArchiveUtility.getCommandFormat().parse(a);
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

		if ((startTime != 0.0) && (endTime != 0.0) && (startTime >= endTime))
		{
			System.out.println("  StartTime = " + startTimeString 
                    + "; EndTime = " + endTimeString);
			System.out.println("End time (" + endTime + ") " +				"does not come after start time (" + startTime + ").");
			return false;
		}
		
		requestPath = sourceName + "/" + channelName;
		
		return true;
	}

	// this version for programatic use
    // to use programaticly:
    //      if (setup(args))
    //          startThread();
	public boolean setup(String serverName, String serverPort, String sinkName,
			String requestPath, String baseDir, double startTime, double endTime)
	{
		
		if (startTime == 0.0)
			startTimeString = "now";
		else
		{
			long time = (long)(startTime * 1000.0);
			startTimeString
                = ArchiveUtility.getCommandFormat().format(new Date(time));
		}

		if (endTime == 0.0)
			endTimeString = "forever";
		else
		{
			long time = (long)(endTime * 1000.0);
			endTimeString 
                = ArchiveUtility.getCommandFormat().format(new Date(time));
		}
		
        if ((startTime != 0.0) && (endTime != 0.0) && (startTime >= endTime))
        {
            System.out.println("Error: StartTime = " + startTimeString
                + "; EndTime = " + endTimeString);
            System.out.println("End time (" + endTime + ") " +
                "does not come after start time (" + startTime + ").");
            return false;
        }

		this.setServerName(serverName);
		this.setServerPort(serverPort);
		this.sinkName = sinkName;
		this.requestPath = requestPath;
		this.baseDir = baseDir;
		this.startTime = startTime;
		this.endTime = endTime;
		// also startTimeString and endTimeString; above
		return setup();
	}

	public boolean setup()
	{
		try {
			System.out.println("Starting JpgSaverSink on " + getServer() 
                    + " as " + sinkName);
			System.out.println("  Requesting " + requestPath);
			System.out.println("  Images archived to " + baseDir);
			System.out.println("  StartTime = " + startTimeString 
                    + "; EndTime = " + endTimeString);
			System.out.println("");
            
			double earlyTime = ChannelUtility.getEarliestTime(getServer(),requestPath);
			if ((endTime != 0.0) && (endTime < earlyTime))
			{
				System.out.println("Warning: end time comes before earliest time in " +					"the ring buffer.");				System.out.println("Earliest time is: " + 
				ArchiveUtility.getCommandFormat().format(new Date((long)(earlyTime * 1000.0))));
				System.out.println(".");
			}
			else if (earlyTime == 0.0)
			{
				System.out.println("Warning: unable to get earliest time " +					"from channel ring buffer.");
			}
		
			// get latest time in the ring buffer and check against startTime
            early = 0;
			double lateTime = ChannelUtility.getLatestTime(getServer(),requestPath);
			if ((startTime != 0.0) && (lateTime < startTime))
			{
    			System.out.println("Wanring: start time is after the end of " +
    				"the ring buffer.");
                System.out.println("Start time is: " + 
                ArchiveUtility.getCommandFormat().format(new Date((long)(startTime * 1000.0))));
    			System.out.println("Latest time is: " + 
    			ArchiveUtility.getCommandFormat().format(new Date((long)(lateTime * 1000.0))));
                early = (long)(1000.0 *(startTime-lateTime));
  			}
			else if (lateTime == 0.0)
			{
				System.out.println("Warning: unable to get latest time " +
					"from channel ring buffer.");
			}

			if ((startTime > 0) && (endTime > 0))
			{
				estimatedDuration = endTime - startTime;
            }
            else
                estimatedDuration = DEFAULT_DURATION;

			// Create a sink and connect:
			sink=new Sink();
			sink.OpenRBNBConnection(getServer(),sinkName);

			connected = true;
			System.out.println("JpgSaverSink: Connection made to server = "
				+ getServer() + " as " + sinkName 
				+ " requesting " + requestPath
				+ " and arcived to directory path "
				+ baseDir + ".");
		} catch (SAPIException se)
		{
            se.printStackTrace();
            statusOk = false;
            addToStatus("Error: " + se.getLocalizedMessage());
			if (sink != null) disconnect();
			return false;
		}
		return true;
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
		stillRunning = true;
		stringDataThread = new Thread(r, "Data Thread");
		stringDataThread.start();
		System.out.println("JpgSaverSink: Started thread.");
	}

	public void stopThread()
	{
		runit = false;
	}
	

	private void runWork ()
	{
        // isStillRunning is set to true in startThread (see end of method)
		statusMessages = new Vector();
		boolean skipping = false;
		int skippingCount = 0;
		int fileCount = 0;
		int dryCount = 0;
        double lastNotify = 0.0;
		statusOk = true;
        
        if (early != 0)
        {
            NumberFormat nf = NumberFormat.getInstance();
            nf.setMaximumFractionDigits(4);
            nf.setMinimumFractionDigits(1);

            String text = "Waiting " + early + " millisesonds (" +
                nf.format(((double)(early))/1000.0) + 
                " seconds) for the ring buffer to catch up to startTime...";
            
            System.out.println(text);
            addToStatus(text);

            try {
                Thread.sleep(early);
            } catch (InterruptedException ignore) {}
        }
        
		try {
            Properties p = new Properties();
            File base = new File(baseDir);
            ArchiveUtility.confirmCreateDirPath(base);
            File f = new File(baseDir, PROPERTIES_FILE_NAME);
            f.createNewFile();
			ChannelMap sMap = new ChannelMap();
			sMap.Add(requestPath);
			if (startTime == 0.0)
				sink.Subscribe(sMap);
			else
				sink.Subscribe(sMap,startTime,0.0,"absolute");
			while(isRunning())
			{
				if (dryCount > 10) // 10 tries is 10 seconds
				{   // the connection is not working??
					statusOk = false;
					addToStatus("The Connection is not responding to Fetch");
					break;
				}
				ChannelMap m = sink.Fetch(1000); // a second
				if (m.GetIfFetchTimedOut())
				{
					dryCount++; 
					System.out.println("JpgSaverSink: fetch timed out " + dryCount);
					continue;
				}
				else dryCount = 0;
				int index = m.GetIndex(requestPath);
				if (index < 0)
				{
					statusOk = false;
					addToStatus("Request path not found on server: " + requestPath);
					break;
				}
				double timeStamp = m.GetTimeStart(index);
				if ((startTime > 0.0) && (timeStamp < startTime))
				{
					if (!skipping)
					{
						long time = (long)(timeStamp * 1000.0);
						String timeStampString = ArchiveUtility.getCommandFormat().format(new Date(time));
						System.out.println("Time stamp - " + timeStampString + " - " +
							"comes before start time - " + startTimeString + " Skipping records...");
					}
					skippingCount++;
					skipping = true;
					continue;
				}
				if ((endTime > 0.0) && (timeStamp > endTime))
				{
					long time = (long)(timeStamp * 1000.0);
					String timeStampString = ArchiveUtility.getCommandFormat().format(new Date(time));
                    if (fileCount == 0)
                    {
                        String text = "Time stamp - " + timeStampString + " - " +
                            "comes after end time - " + endTimeString + " - " +
                            "exiting.";
                        System.out.println(text);
                        addToStatus(text);
                    }
                    else
                    {
                        String text = "Nomal exit at records with time stamp = "
                            + timeStampString;
                        System.out.println(text);
                        addToStatus(text);
                    }
					break;
				}
				if (skipping && skippingCount > 0)
				{
					long time = (long)(timeStamp * 1000.0);
					String timeStampString = ArchiveUtility.getCommandFormat().format(new Date(time));
					System.out.println("Time stamp - " + timeStampString + " - " +
						"now inside startTime - " + startTimeString + " - " +
						"running.");
					System.out.println("Skipped " + skippingCount + " records.");
					skipping = false;
					skippingCount = 0;
				}
				if (startTime == 0.0)
				{
					startTime = timeStamp;
					if (endTime > 0.0) estimatedDuration = endTime - startTime;
				}
				consumedTime = timeStamp - startTime;
				if ((endTime == 0.0) && (consumedTime > (estimatedDuration - 10.0)))
					estimatedDuration = consumedTime * 1.5;
				
				if (consumedTime > (lastNotify + 10.0))
				{   // notify every "10 seconds of data"
					lastNotify = consumedTime;
					notifyAllListeners();
				}
				long unixTime = (long)(timeStamp * 1000.0); // convert sec to millisec
				File testFile = ArchiveUtility.makePathFromTime(baseDir,unixTime);
				if (ArchiveUtility.confirmCreateDirPath(testFile.getParentFile()))
				{
					FileOutputStream out = new FileOutputStream(testFile);
					out.write(m.GetData(index));
					out.close();
					fileCount++;
				}
			}
		} catch (SAPIException se) {
			se.printStackTrace();
            statusOk = false;
            addToStatus("Error: " + se.getLocalizedMessage());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
            statusOk = false;
            addToStatus("Error: " + e.getLocalizedMessage());
		} catch (IOException e) {
			e.printStackTrace();
            statusOk = false;
            addToStatus("Error: " + e.getLocalizedMessage());
        }
		System.out.println("Wrote " + fileCount + " files.");
		addToStatus("Wrote " + fileCount + " files.");
		stopThread();
		disconnect();
		estimatedDuration = consumedTime;
		notifyAllListeners();		
		stringDataThread = null;
		stillRunning = false;
        System.out.println("JpgSaverSink: Stopped thread.");
	}
	
	private void addToStatus(String s) {
		if (s == null) return;
		if (s.length() == 0) return;
		statusMessages.addElement(s);
	}

	private void disconnect() {
		sink.CloseRBNBConnection();
		connected = false;
		sink = null;
	}

	public boolean isRunning()
	{
		return (connected && runit);
	}
	
	public boolean isStillRunning()
	{
		return stillRunning;
	}

	Vector listeners = new Vector();
	public void addTimeProgressListener(TimeProgressListener l)
	{
		listeners.addElement(l);
	}
	
	public void removeTimeProgressListener(TimeProgressListener l)
	{
		listeners.removeElement(l);
	}

	public void removeAllTimeProgressListeners()
	{
		listeners.removeAllElements();
	}
	
	private void notifyAllListeners()
	{
		for (Enumeration e = listeners.elements();
			e.hasMoreElements();
			((TimeProgressListener)e.nextElement()).
                    progressUpdate(estimatedDuration,consumedTime));
	}

	/**
	 * @return the running status of the current capture thread, or the
	 * completion status of the last thread run
	 */
	public boolean isStatusOk() {
		return statusOk;
	}

	/**
	 * @return the String array that is a trace of status messages from the
	 * execution of the last thread
	 */
	public String[] getStatusMessages() {
		String[] messages = new String[statusMessages.size()];
		int i = 0;
		for (Enumeration e = statusMessages.elements();e.hasMoreElements();)
		{
			messages[i++] = (String)e.nextElement();
		}
		return messages;
	}
}
