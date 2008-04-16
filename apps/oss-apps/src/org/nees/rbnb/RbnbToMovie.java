/*
 * RbnbToMovie.java
 * Created August, 2005
 * 
 * COPYRIGHT © 2005, THE REGENTS OF THE UNIVERSITY OF MICHIGAN,
 * ALL RIGHTS RESERVED; see the file COPYRIGHT.txt in this folder for details
 * 
 * This work is supported in part by the George E. Brown, Jr. Network
 * for Earthquake Engineering Simulation (NEES) Program of the National
 * Science Foundation under Award Numbers CMS-0117853 and CMS-0402490.
 * 
 * CVS information...
 *   $Revision$
 *   $Date$
 *   $RCSfile: RbnbToMovie.java,v $ 
 * 
 */
package org.nees.rbnb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.FileFilter;
import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Vector;
import java.util.Enumeration;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.media.ControllerListener;
import javax.media.MediaLocator;
import javax.media.Format;
import javax.media.Processor;
import javax.media.Manager;
import javax.media.DataSink;
import javax.media.Buffer;
import javax.media.Time;
import javax.media.EndOfMediaEvent;
import javax.media.ControllerEvent;
import javax.media.ConfigureCompleteEvent;
import javax.media.ResourceUnavailableEvent;
import javax.media.RealizeCompleteEvent;
import javax.media.PrefetchCompleteEvent;

import javax.media.control.TrackControl;

import javax.media.protocol.PullBufferDataSource;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PullBufferStream;
import javax.media.protocol.DataSource;
import javax.media.protocol.FileTypeDescriptor;

import javax.media.datasink.DataSinkListener;
import javax.media.datasink.DataSinkEvent;
import javax.media.datasink.EndOfStreamEvent;
import javax.media.datasink.DataSinkErrorEvent;
import javax.media.format.VideoFormat;
import javax.media.format.AudioFormat;
import javax.imageio.ImageIO;

import com.sun.image.codec.jpeg.JPEGImageDecoder;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.rbnb.sapi.*;

/**
 * This class grabs images from an RBNB image source and saves them to a movie
 * file. The code is informed by the example VideoSampleBuilder in QuickTime for
 * Java by Chris Adamson, Copyright 2005, ISBN 0-596-00822-8, and used by
 * permission as per the section "Using Code Examples" in the Preface of
 * that that book. 
 * 
 * @author Terry E. Weymouth
 */
public class RbnbToMovie extends RBNBBase
implements ControllerListener, DataSinkListener
{

	private static final String SINK_NAME = "RbnbToMovie";
	private static final String SOURCE_NAME = "VideoSource";
	private static final String CHANNEL_NAME = "video.jpg";

    public static final String MOVIE_FILE = "Movie";
    private String movieFile = MOVIE_FILE;
	
	private String sinkName = SINK_NAME;
	private String requestPath = SOURCE_NAME + "/" + CHANNEL_NAME;

	private String startTimeString = "first records's time";
	private double startTime = 0.0;
	private String endTimeString = "undefined";
	private double endTime = 0.0;
    private double duration = 0.0;
    private boolean useDuration = false;
	private long early = 0;
	double estimatedDuration = 0.0;
	double consumedTime = 0.0;
	double lastNotify = 0.0;
	
	private Sink sink = null;
	private boolean statusOk = true;
	private Vector statusMessages = new Vector();
	
	private Thread stringDataThread;
	private boolean runit = false; // to signal stop
	private boolean stillRunning = false; // actually stopped

    private int movieFrameHeight;
    private int movieFrameWidth;
    private float movieFrameRate;
    
	public static void main(String[] args) {
		RbnbToMovie s = new RbnbToMovie();
		if (s.parseArgs(args) && s.setup())
            s.exec();
	}

    protected String getCVSVersionString()
    {
        return
            "  CVS information... \n" +
            "  $Revision$\n" +
            "  $Date$\n" +
            "  $RCSfile: RbnbToMovie.java,v $ \n";
    }

	/* (non-Javadoc)
	 * @see org.nees.rbnb.RBNBBase#setOptions()
	 */
	protected Options setOptions() {
		Options opt = setBaseOptions(new Options()); // uses h, s, p, v
		opt.addOption("K",true,"RBNB Sink Name *" + SINK_NAME);
		opt.addOption("P",true,"RBNB path to Source *" + SOURCE_NAME
            + "/" + CHANNEL_NAME);
		opt.addOption("F",true,"Movie file path *" + MOVIE_FILE);
		opt.addOption("S",true,"Start time (defauts to start of ringbuffer)");
		opt.addOption("E",true,"End time (either Duration or End Time required)");
        opt.addOption("D",true,"Duration (either Duration or End Time required)");
		setNotes("Writes jpg images between start time and end time to " +			"a movie file of the given type.\n" +
            "Either duration or End Time are required. StartTime defaults to 'now'.\n" +
            "If both duration and endTime are given, duration takes precidence.\n" +			"Time format is yyyy-mm-dd:hh:mm:ss.nnn. Duration is flaoting point seconds.");
		return opt;
	}
	
	/* (non-Javadoc)
	 * @see org.nees.rbnb.RBNBBase#setArgs(org.apache.commons.cli.CommandLine)
	 */
	protected boolean setArgs(CommandLine cmd) {

		if (!setBaseArgs(cmd)) return false;

        useDuration = false;

		if (cmd.hasOption('P')) {
			String a=cmd.getOptionValue('P');
			if (a!=null) requestPath=a;
		}
		if (cmd.hasOption('K')) {
			String a=cmd.getOptionValue('K');
			if (a!=null) sinkName=a;
		}
		if (cmd.hasOption('F')) {
			String a=cmd.getOptionValue('F');
			if (a!=null) movieFile=a;
		}
//        if (cmd.hasOption('t')) {
//            String a=cmd.getOptionValue('t');
//            if (a!=null) movieType=a;
//        }
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
        if (cmd.hasOption('D')) {
            String a=cmd.getOptionValue('D');
            if (a!=null)
            {
                try
                {
                    duration = Double.parseDouble(a);
                    useDuration = true;
                }
                catch (Exception e)
                {
                    System.out.println("Parse of Duration failed " + a);
                    printUsage();
                    return false;
                }
            }
        }

        if ((endTime == 0.0) && (duration == 0.0))
        {
            System.out.println("  One of Duration or EndTime are required. " +                "Both were unspecified.");
            return false;
        }
		
		return true;
	}

	// this version for programatic use
    // to use programaticly:
    //      if (setup(args))
    //          exec();
	public boolean setup(String serverName, String serverPort, String sinkName,
			String requestPath, String movieFile, String movieType, 
            double startTime, double endTime, double duration, boolean useDuration)
	{
		
		if (startTime == 0.0)
			startTimeString = "first record's time";
		else
		{
			long time = (long)(startTime * 1000.0);
			startTimeString
                = ArchiveUtility.getCommandFormat().format(new Date(time));
		}

		if (endTime == 0.0)
			endTimeString = "unspecified";
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

        // Check for output file extension.
        if (!movieFile.endsWith(".mov") && !movieFile.endsWith(".MOV")) {
            System.out.println("The output file name should end with a .mov extension");
            return false;
        }

		this.setServerName(serverName);
		this.setServerPort(serverPort);
		this.sinkName = sinkName;
		this.requestPath = requestPath;
		this.startTime = startTime;
		this.endTime = endTime;
		// also startTimeString and endTimeString; above
        this.duration = duration;
        this.movieFile = movieFile;
//        this.movieType = movieType;
		return setup();
	}

	public boolean setup()
	{

		System.out.println("Starting JpgSaverSink on " + getServer() 
                + " as " + sinkName);
		System.out.println("  Requesting = " + requestPath);
		System.out.println("  Movie file = " + movieFile);
        System.out.println("  StartTime = " + startTimeString 
                + "; EndTime = " + endTimeString
                + "; Duration = " + duration);
		System.out.println("");

        // attempt to set up conditions so that the first desired image
        // is read from the RBNB ring buffer on the first read
              
        if ((endTime == 0.0) && (startTime != 0.0)) endTime = startTime + duration;          

        try{
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
                while (early != 0)
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

                    early = 0l;                    
                    lateTime = ChannelUtility.getLatestTime(getServer(),requestPath);
                    if (lateTime != 0.0)
                    {
                        if (lateTime < startTime)
                            early = (long)(1000.0 *(startTime-lateTime));
                        System.out.println("Latest time is now: " + 
                        ArchiveUtility.getCommandFormat().format(new Date((long)(lateTime * 1000.0))));
                    }
                }
  			}
			else if (lateTime == 0.0)
			{
				System.out.println("Warning: unable to get latest time " +
					"from channel ring buffer.");
			}
            
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

    private void exec()
    {
        try
        {
            readUntilFirstRecord(); // get and set firstImage
            // set movieFrameRate, movieFrameHeight, movieFrameWidth
            // may set startTime
            
            if (useDuration)
            {
                endTime = startTime + duration;
            }
        
            duration = endTime - startTime;
            if (duration < 0.0) duration = 0.0;

            long time = (long)(startTime * 1000.0);
            startTimeString
                = ArchiveUtility.getCommandFormat().format(new Date(time));
            time = (long)(endTime * 1000.0);
            endTimeString 
                = ArchiveUtility.getCommandFormat().format(new Date(time));

            System.out.println("StartTime = " + startTimeString 
                    + "; EndTime = " + endTimeString
                    + "; Duration = " + duration);

            estimatedDuration = duration;

            determineFrameRateHeightAndWidth(); 

            System.out.println("Estimated Frame Rate = " + movieFrameRate);
            System.out.println("Move Frame height = " + movieFrameHeight +
                "; width = " + movieFrameWidth);

            if (!doMovie(movieFrameHeight, movieFrameWidth, movieFrameRate))
                System.out.println("Somthing wrong with the movie process.");

        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
    }
    
    // this method will read records and discard them until the image record
    // between startTime and endTime, at which point it will return true.
    // if the read fails, it will return false.
    
    byte[] firstImage;
    private void readUntilFirstRecord() throws SAPIException
    {
        System.out.println("Reading first record...");
        double timeStamp = 0.0;
        connect();
        ChannelMap sMap = new ChannelMap();
        sMap.Add(requestPath);
        sink.Subscribe(sMap,startTime,0.0,"absolute");            
        do { // read next record
            ChannelMap m = sink.Fetch(1000);
            int index = m.GetIndex(requestPath);
            if (index < 0)
                throw new SAPIException("No records on channel = " + requestPath);
            timeStamp = m.GetTimeStart(index);
            firstImage = m.GetData(index);
            long time = (long)(timeStamp * 1000.0);
            String timeStampString = ArchiveUtility.getCommandFormat().format(new Date(time));
            System.out.println("Read record with Time stamp = " + timeStampString);
        } while (timeStamp < startTime);
        disconnect();
        long time = (long)(timeStamp * 1000.0);
        String timeStampString = ArchiveUtility.getCommandFormat().format(new Date(time));
        System.out.println("Got first record. Time stamp = " + timeStampString);
        if (startTime == 0.0)
        {
            startTime = timeStamp;
            System.out.println("Start time was unspecified. " +                "Setting Start Time = " + timeStampString);
        }
    }
    
    private void determineFrameRateHeightAndWidth() throws SAPIException, IOException
    {
        // use first image for height and width
        JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(
                new ByteArrayInputStream(firstImage));
    
        // decode JPEG image to raw image
        BufferedImage bi;
        try {
            bi = decoder.decodeAsBufferedImage();
            movieFrameHeight = bi.getHeight();
            movieFrameWidth = bi.getWidth();
        } catch (IOException e){
            System.err.println("Failed to decode input JPEG image, for height and width.");
            throw e;
        }
        
        System.out.println("Records for frame rate...");
        double timeStamp = 0.0;
        movieFrameRate = 30.0f;
        double start = 0.0;
        double end = 0.0;

        int recordCount = 0;

        connect();
        ChannelMap sMap = new ChannelMap();
        sMap.Add(requestPath);
        sink.Subscribe(sMap,startTime,0.0,"absolute");
        while ((recordCount < 20) && (timeStamp < endTime))
        {
            recordCount++;
            ChannelMap m = sink.Fetch(1000);
            int index = m.GetIndex(requestPath);
            if (index < 0)
                throw new SAPIException("No records on channel = " + requestPath);
            timeStamp = m.GetTimeStart(index);

            long time = (long)(timeStamp * 1000.0);
            String timeStampString = ArchiveUtility.getCommandFormat().format(new Date(time));

            if (start == 0.0) start = timeStamp;
            end = timeStamp;
        }
        disconnect();
        if (recordCount < 2)
        {
            throw new SAPIException("Not enough records to estimate frame rate; " +                "recordCount = " + recordCount);
        }
        movieFrameRate = (float)((end - start)/((double)(recordCount - 1)));
    }
    
    private boolean doMovie(int height, int width, float frameRate) throws Exception
    {
        Processor p;

        MediaLocator outML = new MediaLocator("file:" + movieFile);

        if (outML == null)
        {
            System.out.println("Failed to form URL from movie file path = " + movieFile);
        }

        PullBufferDataSource dataSource = (PullBufferDataSource)
            new RbnbDataSource(width, height, frameRate);
        
        try {
            System.err.println("creating processor for the RBNB datasource ...");
            p = Manager.createProcessor(dataSource);
        } catch (Exception e) {
            System.err.println("Yikes!  Cannot create a processor from the data source.");
            throw e;
        }

        p.addControllerListener(this);

        // Put the Processor into configured state so we can set
        // some processing options on the processor.
        p.configure();
        if (!waitForState(p, p.Configured)) {
            System.err.println("Failed to configure the processor.");
            return false;
        }

        // Set the output content descriptor to QuickTime. 
        p.setContentDescriptor(new ContentDescriptor(FileTypeDescriptor.QUICKTIME));

        // Query for the processor for supported formats.
        // Then set it on the processor.
        TrackControl tcs[] = p.getTrackControls();
        Format f[] = tcs[0].getSupportedFormats();
        if (f == null || f.length <= 0) {
            System.err.println("The mux does not support the input format: " + tcs[0].getFormat());
            return false;
        }

        System.out.println("Found count="+f.length);

        for ( int i=0; i<f.length;i++ ) System.out.println("Found "+f[i]);

        tcs[0].setFormat(f[0]);

        System.err.println("Setting the track format to: " + f[0]);

        // We are done with programming the processor.  Let's just
        // realize it.
        p.realize();
        if (!waitForState(p, p.Realized)) {
            System.err.println("Failed to realize the processor.");
            return false;
        }

        ContentDescriptor[] descriptors = p.getSupportedContentDescriptors();
        for (int n = 0; n < descriptors.length; n++) {
            System.out.println("Desc: " + descriptors[n].toString());
        }

        DataSource output = p.getDataOutput();

        System.out.println("DataSource type: ");
        Class cls = output.getClass();
        while (cls != null) {
            System.out.println(cls.toString());
            cls = cls.getSuperclass();
        }

        // Now, we'll need to create a DataSink.
        DataSink dsink;
        if ((dsink = createDataSink(p, outML)) == null) {
            System.err.println("Failed to create a DataSink for the given output MediaLocator: " + outML);
            return false;
        }

        dsink.addDataSinkListener(this);

        System.err.println("start processing...");

        // OK, we can now start the actual transcoding.
        try {
            p.start();
            dsink.start();
        } catch (Exception e) {
            System.err.println("IO error during processing");
            return false;
        }
        
        return true;
    }
    
    /**
     * Create the DataSink.
     */
    private DataSink createDataSink(Processor p, MediaLocator outML) {

        DataSource ds;
    
        if ((ds = p.getDataOutput()) == null) {
            System.err.println("Something is really wrong: the processor does not have an output DataSource");
            return null;
        }
    
        DataSink dsink;
    
        try {
            System.err.println("- create DataSink for: " + outML);
            dsink = Manager.createDataSink(ds, outML);
            dsink.open();
        } catch (Exception e) {
            System.err.println("Cannot create the DataSink: " + e);
            return null;
        }
    
        return dsink;
    }

    private void connect() throws SAPIException
    {
        // Create a sink and connect:
        sink=new Sink();
        sink.OpenRBNBConnection(getServer(),sinkName);
    }

	private void disconnect() {
		sink.CloseRBNBConnection();
		sink = null;
	}

    // ------------------- notification utilities ---------------------

    private void addToStatus(String s) {
        if (s == null) return;
        if (s.length() == 0) return;
        statusMessages.addElement(s);
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
    

    Object waitSyncController = new Object();
    boolean stateTransitionOK = true;

    /**
     * Block until the processor has transitioned to the given state.
     * Return false if the transition failed.
     */
    boolean waitForState(Processor p, int state) {
        synchronized (waitSyncController) {
            try {
                while (p.getState() < state && stateTransitionOK)
                    waitSyncController.wait();
            } catch (Exception e) {}
        }
        return stateTransitionOK;
    }

   /**
     * Controller Listener. (API)
     */
    public void controllerUpdate(ControllerEvent evt) {
    
        if (evt instanceof ConfigureCompleteEvent ||
            evt instanceof RealizeCompleteEvent ||
            evt instanceof PrefetchCompleteEvent) {
            synchronized (waitSyncController) {
            stateTransitionOK = true;
            waitSyncController.notifyAll();
            }
        } else if (evt instanceof ResourceUnavailableEvent) {
            synchronized (waitSyncController) {
            stateTransitionOK = false;
            waitSyncController.notifyAll();
            }
        } else if (evt instanceof EndOfMediaEvent) {
            evt.getSourceController().stop();
            evt.getSourceController().close();
        }
    }

    Object waitFileSync = new Object();
    boolean fileDone = false;
    boolean fileSuccess = true;

    /**
     * Block until file writing is done. 
     */
    boolean waitForFileDone() {
        synchronized (waitFileSync) {
            try {
            while (!fileDone)
                waitFileSync.wait();
            } catch (Exception e) {}
        }
        return fileSuccess;
    }

    /**
     * Data sync listener for connection ????
     */
    public void dataSinkUpdate(DataSinkEvent evt) {
    
        if (evt instanceof EndOfStreamEvent) {
            synchronized (waitFileSync) {
            fileDone = true;
            waitFileSync.notifyAll();
            }
        } else if (evt instanceof DataSinkErrorEvent) {
            synchronized (waitFileSync) {
            fileDone = true;
            fileSuccess = false;
            waitFileSync.notifyAll();
            }
        }
    }


    /**
     * A PullBufferDataSource to read a JPEG stream of images from RBNB
     * and turn that into a JMF stream.
     * The DataSource is not seekable or positionable.
     */
    private class RbnbDataSource extends PullBufferDataSource {
    
        RbnbSourceStream streams[];
    
        RbnbDataSource(int width, int height, float frameRate)
        throws SAPIException
        {
            streams = new RbnbSourceStream[1];
            streams[0] = new RbnbSourceStream(width, height, frameRate);
        }
        
        public void setLocator(MediaLocator source) {
        }
        
        public MediaLocator getLocator() {
            return null;
        }
        
        /**
         * Content type is of RAW since we are sending buffers of video
         * frames without a container format.
         */
        public String getContentType() {
            return ContentDescriptor.RAW;
        }
        
        public void connect() {
            // this should be the main RBNB connect? 
            System.out.println("RbnbDataSource connect");
        }
        
        public void disconnect() {
            // this should be the main RBNB disconnect? 
            System.out.println("RbnbDataSource disconnect");
        }
        
        public void start() {
            // when does this get called?
            System.out.println("RbnbDataSource start");
        }
        
        public void stop() {
            // when does this get called?
            System.out.println("RbnbDataSource stop");
        }
        
        /**
         * Return the RbnbSourceStream.
         */
        public PullBufferStream[] getStreams() {
            return streams;
        }
        
        /**
         * We could have derived the duration from the number of
         * frames and frame rate.  But for the purpose of this program,
         * it's not necessary.
         */
        public Time getDuration() {
            return DURATION_UNKNOWN;
        }
        
        public Object[] getControls() {
            return new Object[0];
        }
        
        public Object getControl(String type) {
            return null;
        }
        
    } // RbnbDataSource
    
    /**
     * The source stream to go along with ImageDataSource.
     */
    private class RbnbSourceStream implements PullBufferStream {
    
        int width, height;
        VideoFormat format;
        
        int imageCount = 0;
        int dryCount = 0;
        boolean ended = false;
        
        public RbnbSourceStream(int width, int height, float frameRate)
            throws SAPIException
        {
            this.width = width;
            this.height = height;
        
            format = new VideoFormat(VideoFormat.JPEG,
                    new Dimension(width, height),
                    Format.NOT_SPECIFIED,
                    Format.byteArray,
                    frameRate);
            
            connect();
            ChannelMap sMap = new ChannelMap();
            sMap.Add(requestPath);
            sink.Subscribe(sMap,startTime,0.0,"absolute");
        }
        
        /**
         * As I understand it, this would require read ahead and buffering.
         * RBNB does the buffering as well as anything will.
         */
        public boolean willReadBlock() {
            return false;
        }
        
        /**
         * This is called from the Processor to read a frame worth
         * of video data.
         */
        public void read(Buffer buf) throws IOException {
        
            double timeStamp = 0.0;
            int index = 0;
            ChannelMap m = null;
            
            try
            {
                m = sink.Fetch(1000); // a second
                if (m.GetIfFetchTimedOut()) dryCount++; 
                    else dryCount = 0;            
            }
            catch (SAPIException e)
            {
                statusOk = false;
                addToStatus("The Connection is not responding to Fetch after "
                + dryCount + " attempts.");
            }
            
            if (dryCount > 10) statusOk = false;
            else if (dryCount > 0)
            {   // when data fatch failes but may still be possible...
                buf.setOffset(0);
                buf.setLength(0);
                buf.setData(null);
                return;
            }

            if (statusOk)
            {
                index = m.GetIndex(requestPath);
                if (index >= 0)
                    timeStamp = m.GetTimeStart(index);
            }

            if (index < 0) statusOk = false;
            
            // if not end conditions, then read data
            if (statusOk && (timeStamp <= endTime))
            {
                byte[] data = (m.GetData(index));
                buf.setOffset(0);
                buf.setLength(data.length);
                buf.setFormat(format);
                buf.setFlags(buf.getFlags() | buf.FLAG_KEY_FRAME);
                buf.setData(data);
                imageCount++;
            }
            else // at the end!            
            {
                if (dryCount > 10) // 10 tries is 10 seconds
                {   // the connection is not working??
                    statusOk = false;
                    addToStatus("The Connection is not responding to Fetch after "
                    + dryCount + " attempts at one second each.");
                }
                else if (index < 0)
                {
                    statusOk = false;
                    addToStatus("Request path not found on server: " + requestPath);
                }
                else if (statusOk) // e.g. timeStamp is "after" end time
                {
                    long time = (long)(timeStamp * 1000.0);
                    String timeStampString = ArchiveUtility.getCommandFormat().format(new Date(time));
                    String text = "Nomal exit at the RBNB record with time stamp = "
                        + timeStampString;
                    addToStatus(text);
                }

                // We are done.  Set EndOfMedia.
                System.err.println("Done reading all images.");
                buf.setEOM(true);
                buf.setOffset(0);
                buf.setLength(0);
                String text = "Wrote " + imageCount + " iamges to the movie.";
                System.out.println(text);
                addToStatus(text);
                disconnect();
                estimatedDuration = consumedTime;
                notifyAllListeners();       
                stringDataThread = null;
                stillRunning = false;
                System.out.println("RbnbToMovie: Stopped thread.");
                ended = true;
                return;
            }

        }
        
        /**
         * Return the format of each video frame.  That will be JPEG.
         */
        public Format getFormat() {
            return format;
        }
        
        public ContentDescriptor getContentDescriptor() {
            return new ContentDescriptor(ContentDescriptor.RAW);
        }
        
        public long getContentLength() {
            return 0;
        }
        
        public boolean endOfStream() {
            return ended;
        }
        
        public Object[] getControls() {
            return new Object[0];
        }
        
        public Object getControl(String type) {
            return null;
        }
    } // RbnbSourceStream

}