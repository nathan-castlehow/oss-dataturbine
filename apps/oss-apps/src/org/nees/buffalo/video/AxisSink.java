package org.nees.buffalo.video;

import java.awt.BorderLayout;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Sink;

import com.rbnb.utility.ArgHandler; //for argument parsing
//import COM.Creare.Utility.ArgHandler; //for argument parsing

/**
 * AxisSink
 * 
 * @author Jason P. Hanley
 * 
 * AxisSink - A simple viewer of a JPEG data channel. Only
 * shows the newest data put into RBNB from the source.
 * Takes 1 argument:
 * 1. full channel name of video stream to view
 *
 * (Assumes rbnb.jar is on the CLASSPATH)
 * example usage:
 *   java org.nees.buffalo.axis.AxisSink "JPEGThumbaniler/camera3thumb"
 * Where the channel name, "JPEGThumbaniler/camera3thumb", is the thumbnail 
 * version created above. Use the other channel name to view the full 
 * resolution, full frame rate stream.
 */

public class AxisSink {

	private final static String DEFAULT_HOST = "localhost:3333";
	private final static String DEFAULT_SINK_NAME = "AxisVideoSink";

	private String rbnbHostName = DEFAULT_HOST;
	private String rbnbSinkName = DEFAULT_SINK_NAME;
	private String sourceName = null;
	private int timeoutInterval = 1000; // one second
	private int retryCount = 30;
	
	private static final int DEFAULT_CACHE_SIZE = 900;
	private int cacheSize = DEFAULT_CACHE_SIZE;
	private static final int DEFAULT_ARCHIVE_SIZE = 0;
	private int archiveSize = DEFAULT_ARCHIVE_SIZE;
			
	private void printUsage() {
		System.out.println("AxisSink: usage is...");		
		System.out.print("AxisSink ");
		System.out.print("[-h server_hostname *localhost:3333] ");
		System.out.print("[-n sink_name *AxisVideoSink] ");
		System.out.print("[-s source_name (required) Example: \"AxisVideoSource/camera\"] ");
		System.out.println();
	}

	/**
	 * 
	 */
	public AxisSink(String[] args) {
		
		// parse args
		try {
			ArgHandler ah=new ArgHandler(args);
			if (ah.checkFlag('h')) {
				String a=ah.getOption('h');
				if (a!=null) rbnbHostName=a;
			}
			if (ah.checkFlag('n')) {
				String a=ah.getOption('n');
				if (a!=null) rbnbSinkName=a;
			}
			if (ah.checkFlag('s')) {
				String a=ah.getOption('s');
				if (a!=null) sourceName=a;
			}
		} catch (Exception e) {
			System.err.println("AxisSink argument exception "+e.getMessage());
			e.printStackTrace();
			System.exit(0);
		}

		if (sourceName == null)
		{
			printUsage();
			System.out.println("Source name (-s) is required. Example: AxisVideoSource/camera");		
			System.exit(0);
		}

		System.out.println("Starting AxisSink on " + rbnbHostName + " as " + rbnbSinkName);
		System.out.println("  Watching for data (with Monitor) from " + sourceName);				
		
		try {
	
			JFrame frame = new JFrame("SwingApplication");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			JLabel image = new JLabel();
			JLabel label = new JLabel();
			frame.getContentPane().add(image, BorderLayout.CENTER);
			frame.getContentPane().add(label, BorderLayout.SOUTH);
			frame.pack();
			frame.setVisible(true);
	
			Sink mySink = new Sink();
			mySink.OpenRBNBConnection(rbnbHostName, rbnbSinkName);
	
			ChannelMap reqmap = new ChannelMap();
			int inputChannelIndex = reqmap.Add(sourceName);
	
			mySink.Monitor(reqmap, 0);
			
			byte[] imageData;
			
			int numberRetries = 0;
			int imageIndex = 0;
			double startTime = 0;
			double oldStartTime = 0;
			double durationTime = 0;
			double averageInputSampleRate = 30;		
			while (true) {		

				ChannelMap getmap = mySink.Fetch(timeoutInterval);
				
				if (getmap.GetIfFetchTimedOut()) {
					if (++numberRetries == retryCount) {
						System.out.println("Failed to get any data after "
							+ retryCount + " retries.");				
						break;
					} else {
						System.out.println("Data request timed out (" +
							numberRetries + " out of " + retryCount + "), retrying.");
						continue;
					}
				} 
				
				numberRetries = 0;
				oldStartTime = startTime;
						
				imageData = getmap.GetData(inputChannelIndex);
				startTime = getmap.GetTimeStart(inputChannelIndex);
				durationTime = getmap.GetTimeDuration(inputChannelIndex);

				double sampleRate;			
				if (imageIndex > 0) {
					sampleRate = 1/(startTime-oldStartTime);
				} else {
					sampleRate = 30;
				}
			
				if (Double.isInfinite(sampleRate)) {
					sampleRate = averageInputSampleRate;
				}
		
				averageInputSampleRate = averageInputSampleRate*0.995 + sampleRate*0.005;
				
				if (imageIndex % 30 == 0) System.out.print("Receiving " + ((double)Math.round(averageInputSampleRate*10))/10 + " fps    \r");
						 
				image.setIcon(new ImageIcon(imageData));
				label.setText(new Date((long)(startTime*1000)).toString());
				if (imageIndex == 0) frame.pack();									 
			 	frame.repaint();
			 				
				imageIndex++;
			}
			
			mySink.CloseRBNBConnection();
			
		} catch (SAPIException e) {
			e.printStackTrace();
		}
		
		System.exit(-1);
	}

	/**
	 * Standard main. Just creates and call an instance of the class.
	 * @param args
	 */
	public static void main(String[] args) {
		new AxisSink(args);
	}

}
