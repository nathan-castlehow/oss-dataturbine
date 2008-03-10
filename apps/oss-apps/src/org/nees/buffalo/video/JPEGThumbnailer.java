package org.nees.buffalo.video;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.util.Date;
import java.util.TimeZone;
import java.text.SimpleDateFormat;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Sink;
import com.rbnb.sapi.Source;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageDecoder;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

import com.rbnb.utility.ArgHandler; //for argument parsing
import com.rbnb.utility.RBNBProcess; //alternative to System.exit, so
									   //don't bring down servlet engine
//import COM.Creare.Utility.ArgHandler; //for argument parsing
//import COM.Creare.Utility.RBNBProcess; //alternative to System.exit, so
									 //don't bring down servlet engine

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
public class JPEGThumbnailer {

	private final static String DEFAULT_RBNB_SERVER = "localhost";
	private final static String DEFAULT_RBNB_PORT = "3333";
	private final static String DEFAULT_RBNB_OUTPUT_NAME = "TSource";
	private final static String DEFAULT_RBNB_OUTPUT_CHANNEL = "thumbnail.jpg";
	private final static String DEFAULT_RBNB_SINK_NAME = "JPEGThumbanilerSink";
	private final static double DEFAULT_SAMPLE_RATE = 1.0; // secondes

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM d, yyyy h:mm:ss aa");
	private static final TimeZone TZ = TimeZone.getTimeZone("GMT");

	static
	{
		DATE_FORMAT.setTimeZone(TZ);
	}

	private String rbnbServerName = DEFAULT_RBNB_SERVER;
	private String rbnbServerPort = DEFAULT_RBNB_PORT;
	private String rbnbHostName = rbnbServerName + ":" + rbnbServerPort;
	private String rbnbSourceName = DEFAULT_RBNB_OUTPUT_NAME;
	private String rbnbSourceChannel = DEFAULT_RBNB_OUTPUT_CHANNEL;
	
	private String rbnbSinkName = DEFAULT_RBNB_SINK_NAME;
	
	private String rbnbInputPath = null; // required

	private double outputSampleRate = DEFAULT_SAMPLE_RATE; // seconds

	private static final int DEFAULT_CACHE_SIZE = 900;
	private int cacheSize = DEFAULT_CACHE_SIZE;
	private static final int DEFAULT_ARCHIVE_SIZE = 0;
	private int archiveSize = DEFAULT_ARCHIVE_SIZE;
	
	private static void printUsage()
	{
		System.out.println("Usage for JPEGThumbnailer...");
		System.out.println("  -v RBNB Sink path for input video (required) ");
		System.out.println("  [-r RBNB Server Name *"+ DEFAULT_RBNB_SERVER + "]");
		System.out.println("  [-p RBNB Server Port *" + DEFAULT_RBNB_PORT + "]");
		System.out.println("  [-s RBNB Source (output) Name *" + DEFAULT_RBNB_OUTPUT_NAME + "]");
		System.out.println("  [-c RBNB Source (output) Channel *" + DEFAULT_RBNB_OUTPUT_CHANNEL + "]");
		System.out.println("  [-f desired output frame rate (in seconds) *" + DEFAULT_SAMPLE_RATE + "]");
		System.out.println("  [-z cache size *" + DEFAULT_CACHE_SIZE + "]");
		System.out.println("[-Z archive size *" + DEFAULT_ARCHIVE_SIZE + "]");
	}
	
	public JPEGThumbnailer(String[] args)
	{
		//parse args
		try {
			ArgHandler ah=new ArgHandler(args);
			if (ah.checkFlag('h')) {
				printUsage();
				RBNBProcess.exit(0);				
			}
			if (ah.checkFlag('r')) {
				String a=ah.getOption('r');
				if (a!=null) rbnbServerName=a;
			}
			if (ah.checkFlag('p')) {
				String a=ah.getOption('p');
				if (a!=null) rbnbServerPort=a;
			}
			if (ah.checkFlag('s')) {
				String a=ah.getOption('s');
				if (a!=null) rbnbSourceName=a;
			}
			if (ah.checkFlag('c')) {
				String a=ah.getOption('c');
				if (a!=null) rbnbSourceChannel=a;
			}
			if (ah.checkFlag('v')) {
				String a=ah.getOption('v');
				if (a!=null) rbnbInputPath=a;
			}
			if (ah.checkFlag('f')) {
				String a=ah.getOption('f');
				if (a!=null) outputSampleRate = Double.parseDouble(a);
			}
			if (ah.checkFlag('z')) {
				String a=ah.getOption('z');
				if (a!=null)
				try
				{
					Integer i =  new Integer(a);
					int value = i.intValue();
					cacheSize = value;
				}
				catch (Exception ignore) {} 
			}
			if (ah.checkFlag('Z')) {
				String a=ah.getOption('Z');
				if (a!=null)
				try
				{
					Integer i =  new Integer(a);
					int value = i.intValue();
					cacheSize = value;
				}
				catch (Exception ignore) {} 
			}
		} catch (Exception e) {
			System.err.println("JPEGThumbnailer argument exception "+e.getMessage());
			e.printStackTrace();
			RBNBProcess.exit(0);
		}

		if (rbnbInputPath == null)
		{
			System.err.println("The source/channel path for the video source is required. "
				+ "Use JPEGThumbnailer -h for help");
			RBNBProcess.exit(0);
		}

		rbnbHostName = rbnbServerName + ":" + rbnbServerPort;		
	}
	
	private void execute() {

		Source source;
		if (archiveSize > 0)
			source=new Source(cacheSize, "create", archiveSize);
		else
			source=new Source(cacheSize, "none", 0);
		try {
			source.OpenRBNBConnection(rbnbHostName, rbnbSourceName);
		} catch (SAPIException e) {
			System.err.println("Failed to connect to RBNB server for source.");
			e.printStackTrace();
			return;				
		}			

		// select channel to output data to
		ChannelMap cmap = new ChannelMap();
		int outputChannelIndex = -1;
		try {
			outputChannelIndex = cmap.Add(rbnbSourceChannel);
		} catch (SAPIException e) {
			System.err.println("Failed to add output channel to channel map.");
			e.printStackTrace();
			return;				
		}			
		cmap.PutMime(outputChannelIndex, "image/jpeg");			
				
		// The Sink to get the source images
		Sink sink = new Sink();
		ChannelMap reqmap = new ChannelMap();
		int inputChannelIndex = -1;
		try
		{
			// connect and set up with monitoring 
			sink.OpenRBNBConnection(rbnbHostName, rbnbSinkName);
			inputChannelIndex = reqmap.Add(rbnbInputPath);
			sink.Monitor(reqmap,0);
			System.out.println("JPGThumNailer: Connection made to sever = "
				+ rbnbHostName + " as " + rbnbSinkName 
				+ " requesting " + rbnbInputPath + ".");
		}
		catch (SAPIException se)
		{
			se.printStackTrace();
			return;
		}
	
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
			ChannelMap getmap = null;
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
						
			startTime = getmap.GetTimeStart(inputChannelIndex);
			durationTime = getmap.GetTimeDuration(inputChannelIndex);
			imageData = getmap.GetData(inputChannelIndex);

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
			System.out.println("Thumbnail sent for " + time + " GMT)");
			
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
		JPEGThumbnailer t = new JPEGThumbnailer(args);
		t.execute();
	}
}
