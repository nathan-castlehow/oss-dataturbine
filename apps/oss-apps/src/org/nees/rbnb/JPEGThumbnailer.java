package org.nees.rbnb;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.util.Date;
import java.util.TimeZone;
import java.text.SimpleDateFormat;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Sink;
import com.rbnb.sapi.Source;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageDecoder;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

/**
 * This class will take in a RBNB channel name containing JPEG image data
 * and outputs a scaled image (at a sub-sampled rate) on the specified channel.
 * This class assumes that the frame rate of the input source is higher then the
 * sample reate of of the output. 
 * The JPEG image will be scaled by 0.5 in both width and height.
 * 
 * Usage for JPEGThumbnailer...
 * -v RBNB Sink path for input video (required) 
 * [-r RBNB Server Name *localhost]
 * [-p RBNB Server Port *3333]
 * [-s RBNB Source (output) Name *TSource]
 * [-c RBNB Source (output) Channel *thumbnail.jpg]
 * [-f desired output frame rate (in seconds)*1.0]
 * 
 * default value are noted by the *
 *
 * 
 * @author Jason P. Hanley
 */
public class JPEGThumbnailer extends RBNBBase {

	private final static String DEFAULT_RBNB_OUTPUT_NAME = "TSource";
	private final static String DEFAULT_RBNB_OUTPUT_CHANNEL = "thumbnail.jpg";
	private final static String DEFAULT_RBNB_SINK_NAME = "JPEGThumbanilerSink";
	private final static String REQUIRED_INPUT_PATH = "";
	private final static double DEFAULT_SAMPLE_RATE = 1.0; // secondes
	private static final String CACHE_SIZE_NAME = "CacheSize";	

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM d, yyyy h:mm:ss aa");
	private static final TimeZone TZ = TimeZone.getTimeZone("GMT");

	static
	{
		DATE_FORMAT.setTimeZone(TZ);
	}

	private String rbnbSourceName = DEFAULT_RBNB_OUTPUT_NAME;
	private String rbnbSourceChannel = DEFAULT_RBNB_OUTPUT_CHANNEL;
	
	private String rbnbSinkName = DEFAULT_RBNB_SINK_NAME;
	
	private String rbnbInputPath = null; // required

	private double outputSampleRate = DEFAULT_SAMPLE_RATE; // seconds

	private static final int DEFAULT_CACHE_SIZE = 900;
	private int cacheSize = DEFAULT_CACHE_SIZE;
	private static final int DEFAULT_ARCHIVE_SIZE = 0;
	private int archiveSize = DEFAULT_ARCHIVE_SIZE;
				
	Sink sink;
	Source source;
	ChannelMap getmap;
	ChannelMap reqmap;
	ChannelMap cmap;
	int outputChannelIndex;
	int inputChannelIndex;

	private boolean connected = false;
	Thread theThread;
	boolean runit = false;
	
    protected String getCVSVersionString()
    {
        return
            "  CVS information... \n" +
            "  $Revision: 153 $\n" +
            "  $Date: 2007-09-24 13:10:37 -0700 (Mon, 24 Sep 2007) $\n" +
            "  $RCSfile: JPEGThumbnailer.java,v $ \n";
    }

	/* (non-Javadoc)
	 * @see org.nees.rbnb.RBNBBase#setOptions()
	 */
	protected Options setOptions() {
		Options opt = setBaseOptions(new Options()); // uses h, s, p
		opt.addOption("v",true,"RBNB Sink path for input video (required) ");
		opt.addOption("s",true,"RBNB Source (output) Name *" + DEFAULT_RBNB_OUTPUT_NAME);
		opt.addOption("c",true,"RBNB Source (output) Channel *" + DEFAULT_RBNB_OUTPUT_CHANNEL);
		opt.addOption("f",true,"desired output frame rate (in seconds) *" + DEFAULT_SAMPLE_RATE);
		opt.addOption("z",true,"cache size *" + DEFAULT_CACHE_SIZE);
		opt.addOption("Z",true,"archive size *" + DEFAULT_ARCHIVE_SIZE);
		return opt;
	}
	
	/* (non-Javadoc)
	 * @see org.nees.rbnb.RBNBBase#setArgs(org.apache.commons.cli.CommandLine)
	 */
	protected boolean setArgs(CommandLine cmd) {
		if (!setBaseArgs(cmd)) return false;

		if (cmd.hasOption('s')) {
			String a=cmd.getOptionValue('s');
			if (a!=null) rbnbSourceName=a;
		}
		if (cmd.hasOption('c')) {
			String a=cmd.getOptionValue('c');
			if (a!=null) rbnbSourceChannel=a;
		}
		if (cmd.hasOption('v')) {
			String a=cmd.getOptionValue('v');
			if (a!=null) rbnbInputPath=a;
		}
		if (cmd.hasOption('f')) {
			String a=cmd.getOptionValue('f');
			if (a!=null) outputSampleRate = Double.parseDouble(a);
		}
		if (cmd.hasOption('z')) {
			String a=cmd.getOptionValue('z');
			if (a!=null)
			try
			{
				Integer i =  new Integer(a);
				int value = i.intValue();
				cacheSize = value;
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

        if ((archiveSize > 0) && (archiveSize < cacheSize)){
            System.err.println(
                "a non-zero archiveSize = " + archiveSize + " must be greater then " +
                    "or equal to cacheSize = " + cacheSize);
            return false;
        }

		if ((rbnbInputPath == null) || (rbnbInputPath.equals("")))
		{
			System.err.println("The source/channel path for the video source is required. "
				+ "Use JPEGThumbnailer -h for help");
			return false;
		}

		return true;
	}

	private void connect()
	{
		if (archiveSize > 0)
			source=new Source(cacheSize, "create", archiveSize);
		else
			source=new Source(cacheSize, "none", 0);
		try {
			source.OpenRBNBConnection(getServer(), rbnbSourceName);
		} catch (SAPIException e) {
			System.err.println("Failed to connect to RBNB server for source.");
			e.printStackTrace();
			return;				
		}			
	
		// select channel to output data to
		cmap = new ChannelMap();
		outputChannelIndex = -1;
		try {
			outputChannelIndex = cmap.Add(rbnbSourceChannel);
		} catch (SAPIException e) {
			System.err.println("Failed to add output channel to channel map.");
			e.printStackTrace();
			return;				
		}			
		cmap.PutMime(outputChannelIndex, "image/jpeg");			
					
		// The Sink to get the source images
		sink = new Sink();
		reqmap = new ChannelMap();
		inputChannelIndex = -1;
		try
		{
			// connect and set up with monitoring 
			sink.OpenRBNBConnection(getServer(), rbnbSinkName);
			inputChannelIndex = reqmap.Add(rbnbInputPath);
			sink.Monitor(reqmap,0);
			System.out.println("JPGThumNailer: Connection made to server = "
				+ getServer() + " as " + rbnbSinkName 
				+ " requesting " + rbnbInputPath + ".");
		}
		catch (SAPIException se)
		{
			se.printStackTrace();
			return;
		}
		
		connected = true;

	}
	
	private void disconnect()
	{
		sink.CloseRBNBConnection();
		sink = null;
		source.CloseRBNBConnection();
		source = null;
		connected = false;
	}
		
	private void execute() {

		byte[] imageData;
		double startTime = 0;
		double durationTime = 0;
		long sleepTime = (long) (1000.0 * outputSampleRate); // milliseconds
		long lastTime = System.currentTimeMillis();
		long actualSleepTime;
		while (true) { //loop forever, receiving JPEG frames
			//adjsut sleep time by how long the look took
			actualSleepTime = sleepTime - (System.currentTimeMillis() - lastTime);
			if (actualSleepTime < 0) actualSleepTime = 0;
			try { Thread.sleep(actualSleepTime); } catch (Exception ignore) {}
			lastTime = System.currentTimeMillis();
			// see if any data is available from source
			getmap = null;
			try {
				getmap = sink.Fetch(0);
			} catch (SAPIException e) {
				System.err.println("Failed to fetch input data, retrying.");
				continue;
			}					
				
			// no data received, try again
			if (getmap.GetIfFetchTimedOut()) {
				System.err.println("Failed to fetch input data, retrying.");
				continue;
			}
						
			startTime = getmap.GetTimeStart(0);
			durationTime = getmap.GetTimeDuration(0);
			imageData = getmap.GetData(0);

			JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(new ByteArrayInputStream(imageData));
	
			// decode JPEG image to raw image
			BufferedImage bi;
			try {
				bi = decoder.decodeAsBufferedImage();
			} catch (IOException e){
				System.err.println("Failed to decode input JPEG image, skipping.");
				continue;
			}

			// scale both width and height by 0.5
			AffineTransformOp op = new AffineTransformOp(AffineTransform.getScaleInstance(0.5, 0.5), null);
			bi = op.filter(bi, null);
			
			// encode scaled image as JPEG image				
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
			JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(bi);
			param.setQuality(0.75f, false);			
			try {
				encoder.encode(bi, param);
			} catch (IOException e) {
				System.err.println("Failed to encode output JPEG image, skipping.");
				continue;
			}				

			// get JPEG image data as byte array
			imageData = out.toByteArray();

			// put data in channel map, preserving original time stamp
			try {
				cmap.PutTime(startTime, durationTime);
				cmap.PutDataAsByteArray(outputChannelIndex, imageData);
			} catch (SAPIException e) {
				System.err.println("Failed to put output data to channel map, skipping.");
				continue;				
			}	
			long unixTime = (long)(startTime * 1000.0); // convert sec to millisec
			String time = DATE_FORMAT.format(new Date(unixTime));
			// System.out.println("Thumbnail sent for " + time + " GMT)");
			
			// send data to RBNB server
			try {
				source.Flush(cmap, true);
			} catch (SAPIException e) {
				System.err.println("Failed to flush output data to server, skipping.");
				continue;				
			}		
		}
	}

	public static void main(String[] args) {
		JPEGThumbnailer t = new JPEGThumbnailer();
		if (t.parseArgs(args))
		{
			t.connect();
			t.startThread();
		}
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
		theThread = new Thread(r, "Timer");
		theThread.start();
		System.out.println("WalkerSource: Started thread.");
	}

	public void stopThread()
	{
		if (!connected) return;
		
		runit = false;
		theThread.interrupt();
		System.out.println("WalkerSource: Stopped thread.");
	}
	
	private void runWork ()
	{
		execute();
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
