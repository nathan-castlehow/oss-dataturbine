/*
 * Created on May 07, 2004, Terry E. Weymouth
 * ***********************************************************************************
 * CVS Header: $Header: /disks/cvs/neesgrid/turbine/src/org/nees/rbnb/DataVideoSink.java,v 1.18 2005/07/26 19:01:12 weymouth Exp $
 * ***********************************************************************************
 */
package org.nees.rbnb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.media.protocol.PullBufferDataSource;
import javax.media.protocol.PullBufferStream;

import java.util.Date;
import java.util.TimeZone;
import java.text.SimpleDateFormat;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.Image;

import javax.media.MediaLocator;
import javax.media.Time;
import javax.media.*;
import javax.media.control.*;
import javax.media.protocol.*;
import javax.media.protocol.DataSource;
import javax.media.datasink.*;
import javax.media.format.VideoFormat;
import javax.media.format.AudioFormat;
import javax.imageio.ImageIO;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.sun.image.codec.jpeg.*;

import com.sun.media.multiplexer.*;

import com.sun.image.codec.jpeg.ImageFormatException;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageDecoder;

import com.rbnb.sapi.*;

/**
 * Gets data from a data stream sink, and then fetches the
 * "nearest" video for each data fetched. 
 * @author Terry E. Weymouth
 * @version $Revision$
 */
public class DataVideoSink extends RBNBBase {
	
	double[] dataArray = new double[0];
	double[] timeArray = new double[0];
	
	Sink dataSink;
	Sink videoSink;

	boolean connected = false;
	
	int videoWidth = 480;
	int videoHeight = 320;
	
	byte [] prevImage = (byte[]) null;
	
	/* ***********           for testing               **************** */
	public static boolean DEBUG = false;

	private static final String SERVER_NAME = "neestpm.mcs.anl.gov";
	private static final String SERVER_PORT = "3333";
	private static final String SINK_NAME = "DataVideoSink";
	
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM d, yyyy h:mm:ss.SSS aa");
	private static final TimeZone TZ = TimeZone.getTimeZone("GMT");

	private static final SimpleDateFormat INPUT_FORMAT = new SimpleDateFormat("yyyy-MM-dd:hh:mm:ss.SSS");

	static
	{
		DATE_FORMAT.setTimeZone(TZ);
		INPUT_FORMAT.setTimeZone(TZ);
	}
    
	private static double USER_GIVEN_FRAME_RATE = 1.0;
	private static int ITEMS_TO_SKIP = 0;

	private static String t_sinkName = SINK_NAME;
	private static String t_dataSourcePath = null;
	private static String t_videoSourcePath = null;
	private static double t_startTime = -1;
	private static double t_duration = -1;
	private static double t_userGivenFrameRate = USER_GIVEN_FRAME_RATE;
	private static int t_itemsToSkip = ITEMS_TO_SKIP;
	 
	private static String t_outputURL;

	private static String t_serverName = SERVER_NAME;
	private static String t_serverPort = SERVER_PORT;
	private static String t_server = t_serverName + ":" + t_serverPort;

	public static void main(String[] args) {

		DataVideoSink s = new DataVideoSink();

		try {
			if (s.parseArgs(args))
			{
				if (s.connect(t_server,t_sinkName))
				{
					System.out.println("DataVideoSink: Connection made to server = "
						+ t_server + " as " + t_sinkName + " for " + t_dataSourcePath
						+ " and as " + t_sinkName + "_video" + " for " + t_videoSourcePath + ".");
					System.out.println("Set to request a fetch of time and duration = " 
						+ t_startTime + "," + t_duration);

					s.fetchData(t_dataSourcePath, t_startTime, t_duration);

					int items = s.getNumberOfItems();
					System.out.println("Number of Items is " + items);					

					s.makeMovie((float)t_userGivenFrameRate, 
						t_outputURL, t_videoSourcePath, t_itemsToSkip);

					double [] times = s.getTimeArray();
					double [] data = s.getDataArray();
					
					DEBUG = true;
					
					System.out.println("Number of Items is " + items);					
					for (int i = 0; i < items; i++)
					{
						long unixTime = (long)(times[i] * 1000.0); // convert sec to millisec
						String time = DATE_FORMAT.format(new Date(unixTime));

						System.out.println("  " + i 
							+ " ("+ times[i] + " - " + time + "): " 
							+ data[i]); 
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			s.printUsage();
		}
		System.exit(0);
	}
	
    protected String getCVSVersionString()
    {
        return
            "  CVS information... \n" +
            "  $Revision$\n" +
            "  $Date$\n" +
            "  $RCSfile: DataVideoSink.java,v $ \n";
    }

	/* (non-Javadoc)
	 * @see org.nees.rbnb.RBNBBase#setOptions()
	 * @see org.nees.rbnb.RBNBBase#setNotes()
	 */
	protected Options setOptions() {
		Options opt = setBaseOptions(new Options()); // uses h, s, p
		opt.addOption("s",true,"Server Hostname *" + SERVER_NAME);
		opt.addOption("p",true,"Server Port Number *" + SERVER_PORT);
		opt.addOption("k",true,"Sink Name *" + SINK_NAME);
		opt.addOption("f",true,"Frame Rate in final movie *" + USER_GIVEN_FRAME_RATE);
		opt.addOption("i",true,"Items to skip in data for each movie frame *" + 
			ITEMS_TO_SKIP);
		opt.addOption("d",true,"Data Source Path - required");
		opt.addOption("v",true,"Video Source path - required");
		opt.addOption("a",true,"Start Time - required");
		opt.addOption("z",true,"End Time - required");
		opt.addOption("m",true,"Movie output file URL - required");
		setNotes("times can either be yyyy-mm-dd:hh:mm:ss.nnn or" +			"an arbitraty floating point number");
		return opt;
	}
	
	/* (non-Javadoc)
	 * @see org.nees.rbnb.RBNBBase#setArgs(org.apache.commons.cli.CommandLine)
	 */
	protected boolean setArgs(CommandLine cmd) {

		if (!setBaseArgs(cmd)) return false;

//		if (cmd.hasOption('A')) {
//			String a=cmd.getOptionValue('A');
		double t_endTime = -1;
		if (cmd.hasOption('k')) {
			String a=cmd.getOptionValue('k');
			if (a!=null) t_sinkName=a;
		}
		if (cmd.hasOption('d')) {
			String a=cmd.getOptionValue('d');
			if (a!=null) t_dataSourcePath=a;
		}
		if (cmd.hasOption('v')) {
			String a=cmd.getOptionValue('v');
			if (a!=null) t_videoSourcePath=a;
		}
		if (cmd.hasOption('m')) {
			String a=cmd.getOptionValue('m');
			if (a!=null) t_outputURL=a;
		}
		if (cmd.hasOption('a')) {
			String a=cmd.getOptionValue('a');
			if (a!=null)
			{
				try
				{
					double value = getTimeOrDouble(a);
					t_startTime = value;
				}
				catch (Exception ex)
				{
					System.out.println("Failed to parse start time (" + a + "): " + ex);
				}
			}
		}
		if (cmd.hasOption('z')) {
			String a=cmd.getOptionValue('z');
			if (a!=null)
			{
				try
				{
					double value = getTimeOrDouble(a);
					t_endTime = value;
				}
				catch (Exception ex)
				{
					System.out.println("Failed to parse end time (" + a + "): " + ex);
				}
			}
		}
		if (cmd.hasOption('f')) {
			String a=cmd.getOptionValue('f');
			if (a!=null)
			{
				try
				{
					double value = Double.parseDouble(a);
					t_userGivenFrameRate = value;
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
					t_itemsToSkip = value;
				}
				catch (Exception ex)
				{
					System.out.println("Failed to parse Frame Rate (" + a + "): " + ex);
				}
			}
		}

		if ((t_startTime > 0) && (t_endTime > 0))
		{
			t_duration = t_endTime - t_startTime;
			if (t_duration < 0)
			{
				System.out.println("End Time must be after Start Time.");
				printUsage();
				return false;
			}
		}

		t_server = t_serverName + ":" + t_serverPort;
		
		if (t_startTime < 0)
		{
			System.out.println("Start Time is required.");
			printUsage();
			return false;
		}

		if (t_duration < 0 ) 
		{
			System.out.println("End Time is required.");
			printUsage();
			return false;
		}

		if (t_dataSourcePath == null)
		{
			System.out.println("Data Source Path is required.");
			printUsage();
			return false;
		}

		if (t_videoSourcePath == null)
		{
			System.out.println("Video Source Path is required.");
			printUsage();
			return false;
		}
		
		if (t_outputURL == null)
		{
			System.out.println("Output File URL is required.");
			printUsage();
			return false;
		}

		return true;
	} // setArgs

	public static double getTimeOrDouble(String arg) throws Exception
	{
		double value = 0.0;
		boolean gotit = false;
		String reason = null;
				
		try{
			Date d = INPUT_FORMAT.parse(arg);
			long t = d.getTime();
			value = ((double)t)/1000.0;
			gotit = true;
		} catch (Exception e1)
		{
			reason = e1.toString();
			gotit = false;
		}

		if (!gotit)
		try {
			value = Double.parseDouble(arg);
			gotit = true;
		} catch (Exception e2)
		{
			reason = reason + "; " + e2.toString();
			gotit = false;
		}

		if (!gotit) 
			throw(new Exception("Failed to parse time " + arg 
				+ "; exception:" + reason));		
		
		return value;

	} // getTimeOrDouble

/* ***********       end of "for testing"             **************** */

	/**
	 * Connect to the RBNB server with two sinks one for a data stream and one for
	 * a coordinated video stream
	 * @return boolean connected true when susessfully connected otherwise false
	 */
	public boolean connect(String server, String sinkName)
	{
		ChannelMap sMap;
		try {
			// Create a sinks and connect:
			dataSink = new Sink();
			dataSink.OpenRBNBConnection(server,sinkName);
			
			videoSink = new Sink();
			videoSink.OpenRBNBConnection(server,sinkName + "_video");
			
			connected = true;
		} catch (SAPIException se) { se.printStackTrace(); }
		return connected;
	} // connect()

	/**
	 * Disconnect the RBNB connection
	 */
	public void disconnect()
	{
		dataSink.CloseRBNBConnection();
		videoSink.CloseRBNBConnection();
		connected = false;
	}
	/**
	 * Fetches data according to the values supplied
	 * @param startTime (double) the start time in seconds for the event stream desired
	 * @param duration (double) the duration in secodns for the event stream desired
	 */
	public void fetchData(String dataSourcePath, double startTime, double duration)
	{
		fetchData(dataSourcePath, startTime, duration, "absolute");
	}
	
	/**
	 * Fetch both the data and the time values for a single channel, 
	 * @param dataSourcePath (String) the RBNB path to the source for the data itmes
	 * @param startTime (double) the start time in seconds for the event stream desired
	 * @param duration (double) the duration in secodns for the event stream desired
	 * @param fetchType (String) the RBNB fetch type 
	 * @see the Request method of the RBNB Sink class 
	 */
	public void fetchData(String dataSourcePath,
		double startTime, double duration, String requestType)
	{
		dataArray = new double[0];
		timeArray = new double[0];
		
		if (!connected) return;

		if (DEBUG) System.out.println("Attempting fetch (at "
			+ dataSourcePath + ") with: "
			+ "start time = " + startTime + ", "
			+ "duration = " + duration + ", "
			+ "request type = " + requestType + ". "
			);
		try {
			ChannelMap dataMap = new ChannelMap();
			int dataIndex = dataMap.Add(dataSourcePath);
			dataSink.Request(dataMap, startTime, duration, requestType);
			ChannelMap res = dataSink.Fetch(-1,dataMap);
			if (res != null)
			{
				if (res.NumberOfChannels() < 1)
				{
					if (DEBUG) System.out.println("no channels returned in fetch");
					return;
				}
				dataArray = res.GetDataAsFloat64(dataIndex);
				timeArray = res.GetTimes(dataIndex);
			}
		} catch (SAPIException se) {
			se.printStackTrace();
			return;
		} catch (IllegalStateException se) {
			// not connected ot RBNB server
			se.printStackTrace(); 
			return;
		} catch (IllegalArgumentException se) {
			// bad parameters on Request
			se.printStackTrace(); 
			return;
		}
		
	} // fatchData
	/**
	 * Make a movie (extension *.mov) of the images that corresond to the
	 * times currently in the time array (from the last data fetch). The images for
	 * the movie are from the image source specified by videoSourcePath. The movie is
	 * written to disk at the file specified by oURL (ie "file:/tmp/movie.mov). The
	 * items in TimeArray are treated as time marks for the images, intermitent time
	 * items can be skipped, using dataItemsToSkip. To find a image for every time
	 * stamp in timeArray, set dataItemsToSkip to zero (0). Setting dataItemsToSkip
	 * to 9 would use timeArray[0], timeArray[10], timeArray[20], etc. This throws
	 * exceptions when the movie can not be built or written.
	 * 
	 * @param frameRate (float) the frame rate, in frames per second
	 * @param oURL
	 * @param videoSourcePath
	 * @param dataItemsToSkip
	 * @throws Exception
	 */
	public void makeMovie(float frameRate,
		String oURL, String videoSourcePath,
		int dataItemsToSkip) throws Exception
	{

		setWidthHeight(videoSourcePath);
		
		PullBufferDataSource ds = (PullBufferDataSource)
			new RBNBImageData(
				getWidth(), getHeight(), frameRate,
				videoSourcePath,dataItemsToSkip);

		// Generate the output media locators.
		JpegImagesToMovie imageToMovie = new JpegImagesToMovie();
		MediaLocator oml;
		
		if ((oml = imageToMovie.createMediaLocator(oURL)) == null) {
			throw new Exception("Cannot build media locator from: " + oURL);
		}
		
		imageToMovie.makeMovieFromPullBufferDataSource(
			getWidth(), getHeight(), frameRate, ds, oml);
    					
	}

	/**
	 * Returns the number of items fetched by the last fetch request
	 * @return
	 */
	public int getNumberOfItems() {
		return timeArray.length;
	} // getNumberOfItems

	/**
	 * Returns the array of times corresponding to the data items fetched
	 * @return double timeArray
	 */
	public double[] getTimeArray() {
		return timeArray;
	} // getTimeArray

	/**
	 * Returns the arrat of data items
	 * @return double[] dataArray
	 */
	public double[] getDataArray() {
		return dataArray;
	} // getDataArray
	
	
	/**
	 * @return
	 */
	private int getWidth() {
		return videoWidth;
	}

	/**
	 * @return
	 */
	private int getHeight() {
		return videoHeight;
	}
	
	private void setWidth(int val)
	{
		videoWidth = val;
	}

	private void setHeight(int val)
	{
		videoHeight = val;
	}

	private void setWidthHeight(String videoSourcePath)
	{
		try
		{
			if (timeArray.length > 0)
			{
				// ugly correction -- this should be fixed!
//				double UGLY_CORRECTION = uglyOffset * 60.0 * 60.0; // uglyOffset hrs in seconds
//	
//				System.out.println("Ugly correction = " + UGLY_CORRECTION + "(" +uglyOffset + ")");
				
//				byte[] image = getRBNBImage(videoSourcePath, timeArray[0]+UGLY_CORRECTION);
				byte[] image = getRBNBImage(videoSourcePath, timeArray[0]);
				JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(new ByteArrayInputStream(image));
				BufferedImage bi;
				bi = decoder.decodeAsBufferedImage();
				videoWidth = bi.getWidth();
				videoHeight = bi.getHeight();
			}
			else
			{
				videoWidth = 480;
				videoHeight = 320;
			}
		}
		catch (Exception e)
		{
			System.out.println("Possible problem? (setWidthHeight) = " + e);
			videoWidth = 480;
			videoHeight = 320;			
		}
	}
	
	/**
	 * Get the "nearest" image after the time specified. Throws exceptions if the
	 *   requests times out or if the data is not availavbe.
	 * @param videoSourcePath (String) the RBNB path of the video source
	 * 	(ie "InterestingVideoSource/Video.jpg")
	 * @param videoTime (double) the RBNB time stamp to start from
	 * @return byte[] the "nearest" image after the time specified
	 * @throws SAPIException when the request times out or when request
	 * 	channel is unavailable.
	 */
	public byte[] getRBNBImage(String videoSourcePath, double videoTime)
		throws SAPIException
	{
		if (DEBUG)
		{
			long unixTime = (long)(videoTime * 1000.0); // convert sec to millisec
			String time = DATE_FORMAT.format(new Date(unixTime));
//			System.out.println("Video Source Path = " + videoSourcePath);
			System.out.println("Request time stamp = " + time);
		}
		ChannelMap videoMap = new ChannelMap();
		videoMap.Clear();
		int videoIndex = videoMap.Add(videoSourcePath);
		// after carfully studying the probelm using GetTimeRange
		// I determined that the correct type for the problem is
		// Absolute, do not change it without testing your hypothesis
		// throughly and carefully. Terry Weymouth, Jan 18, 2005.
		double duration = 0.0;
		videoSink.Request(videoMap,videoTime,duration,"Absolute");
		ChannelMap retMap = videoSink.Fetch(5000);
		if (retMap.GetIfFetchTimedOut())
			throw new SAPIException("Request on channel (" + videoSourcePath + ") timed out. No Data.");
		if ((retMap.NumberOfChannels() < 1)
				|| (!retMap.GetName(0).equals(videoSourcePath)))
			throw new SAPIException("Requested channel (" + videoSourcePath + ") no data");
		if (DEBUG)
		{
			double[] realTimes = retMap.GetTimes(0);
			double realD = retMap.GetTimeDuration(0);
			// System.out.println("length = " + realTimes.length + ", duration = " + realD);
			double realtime = realTimes[0];
			double diff = realtime - videoTime;
			long realUnixTime = (long)(realtime * 1000.0); // convert sec to millisec
			String realTimeString = DATE_FORMAT.format(new Date(realUnixTime));
//			System.out.println("The numbers " + videoTime + "," + realtime + "," + diff);
			System.out.print("Actual time stamp  = " + realTimeString);
			if (Math.abs(diff) < 0.1)
				System.out.println("; diff < 0.1 sec");
			else
				System.out.println("; diff(sec/10) = " + ((int)(diff*10.0)));
//			System.out.println("number of times = " + realTimes.length);
		}
		return retMap.GetDataAsByteArray(0)[0];
	}
	
	/**
	 * A DataSource to read from a list of JPEG images from RBNB and
	 * turn that into a stream of JMF buffers.
	 * The DataSource is not seekable or positionable.
	 */
	private class RBNBImageData extends PullBufferDataSource
	{

		RBNBImageDataStream streams[];

		RBNBImageData(int width, int height, float frameRate,
			String videoSourcePath ,int dataItemsToSkip)
		{
			streams = new RBNBImageDataStream[1];
			streams[0] = new RBNBImageDataStream(width, height, frameRate,
					videoSourcePath, dataItemsToSkip);
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
		}

		public void disconnect() {
		}

		public void start() {
		}

		public void stop() {
		}

		/**
		 * Return the ImageSourceStreams.
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
	}
	
	class RBNBImageDataStream implements PullBufferStream
	{

		String videoSourcePath;
		int dataItemsToSkip;
		VideoFormat format;

		int width;
		int height;

		int nextImage = 0;	// index of the next image to be read.
		boolean ended = false;

		public RBNBImageDataStream(int width, int height, float frameRate,
			String videoSourcePath ,int dataItemsToSkip)
		{

			format = new VideoFormat(VideoFormat.JPEG,
					new Dimension(getWidth(), getHeight()),
					Format.NOT_SPECIFIED,
					Format.byteArray,
					frameRate);

			this.videoSourcePath = videoSourcePath;
			this.dataItemsToSkip = dataItemsToSkip;
			this.width = width;
			this.height = height;		
		}

		/**
		 * We should never need to block assuming data are read from files.
		 */
		public boolean willReadBlock() {
			return false;
		}

		/**
		 * This is called from the Processor to read a frame worth
		 * of video data.
		 */
		public void read(Buffer buf) throws IOException {

			if (DEBUG) System.out.println("image " + nextImage);
			// Check if we've finished all the frames.
			if (nextImage >= timeArray.length) {
				// We are done.  Set EndOfMedia.
				System.err.println("Array: Done processing all images.");
				buf.setEOM(true);
				buf.setOffset(0);
				buf.setLength(0);
				ended = true;
				return;
			}
			
			// ugly correction -- this should be fixed
//			double UGLY_CORRECTION = uglyOffset * 60.0 * 60.0; // uglyOffset hrs in seconds
//
//			System.out.println("Ugly correction = " + UGLY_CORRECTION + "(" +uglyOffset + ")");
			
			byte[] data;
			try {
//				data = getRBNBImage(videoSourcePath, timeArray[nextImage] + UGLY_CORRECTION);
				data = getRBNBImage(videoSourcePath, timeArray[nextImage]);
				prevImage = data;
			} catch (SAPIException e) {
				if (prevImage == null)
				{
					System.out.println("Image fatch failed, skipping: " + e);
					data = new byte[0];
					buf.setData(data);
					buf.setOffset(0);
					buf.setLength(0);
					nextImage += dataItemsToSkip + 1;
					return;
				}
				data = prevImage;
			}
			nextImage += dataItemsToSkip + 1;

			buf.setOffset(0);
			buf.setLength(data.length);
			buf.setFormat(format);
			buf.setFlags(buf.getFlags() | buf.FLAG_KEY_FRAME);
			buf.setData(data);

			//	   Check the height and width of the read image to make sure that it is what 
			//	   is expected - if not resize and update buffer

			JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(new ByteArrayInputStream(data));

			//	   decode JPEG image to raw image
			BufferedImage bi;
			bi = decoder.decodeAsBufferedImage();
			if ( height != bi.getHeight() || width != bi.getWidth() ) {
				System.out.println("Resizing... to "+width+" x "+height);
		
				Image newimg = bi.getScaledInstance(width, height, 0);
	
				System.out.println("Converting back to buffered image...");
	
				BufferedImage dest = null;
				dest = new BufferedImage(width, height, dest.TYPE_INT_RGB);
				dest.getGraphics().drawImage((Image) newimg, 0, 0, null);
	
				System.out.println("resized height="+dest.getHeight()+" w="+dest.getWidth());
				
				setHeight(dest.getHeight());
				setWidth(dest.getWidth());
				
				// encode scaled image as JPEG image                            
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
				System.out.println("Encoding resized image");
				encoder.encode(dest);
				System.out.println("Setting image as buffer");
				// get JPEG image data as byte array
				byte[] newdata = out.toByteArray();
				buf.setData(newdata);
				System.out.println("Length="+newdata.length);
				buf.setLength(newdata.length);
			} // End if must resize

		} // read

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
	}

}

