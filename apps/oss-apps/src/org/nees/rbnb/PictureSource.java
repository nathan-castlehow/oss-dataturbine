/*
 * This RBNB object supplies a "psudo video" stream by sending
 * a series of JPG images.
 * 
 * Created on Feb 5, 2004
 *  Derived from Axis Source (Dec 12, 2003) by Jason P. Hanley
 */
package org.nees.rbnb;

/**
 * @author terry
 * @author Jason P. Hanley
 */

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.MediaTracker;
import java.awt.Component;
import java.net.URL;

import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;

import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.Source;
import com.rbnb.sapi.SAPIException;

public class PictureSource extends RBNBBase{
	private byte[][] fileBuffer;
	private String[] text;
	private int current = 0;
	private Thread timerThread = null;
	private int channelId = -1;
	private Source source = null;	
	private ChannelMap cmap = null;

	private static final String SOURCE_NAME = "FakeVideoSource";
	private static final String CHANNEL_NAME = "Duke";
	private static final String PICTURE_PATH = "./duke/Duke";
	private static final int PICTURE_COUNT = 10;
	private static final long TIMER_INTERVAL=1000;

	private String sourceName = SOURCE_NAME;
	private String channelName = CHANNEL_NAME;
	private String picturePath = PICTURE_PATH;
	private int pictureCount = PICTURE_COUNT;
	private long timerInterval = TIMER_INTERVAL;

	private static final int DEFAULT_CACHE_SIZE = 900;
	private int cacheSize = DEFAULT_CACHE_SIZE;
	private static final int DEFAULT_ARCHIVE_SIZE = 0;
	private int archiveSize = DEFAULT_ARCHIVE_SIZE;

	private boolean connected = false;
	private boolean running = false;
	
	public static void main(String[] args) {
		// start from command line
		PictureSource p = new PictureSource();
		if (p.parseArgs(args)) p.start();
	}
	
    protected String getCVSVersionString()
    {
        return
            "  CVS information... \n" +
            "  $Revision$\n" +
            "  $Date$\n" +
            "  $RCSfile: PictureSource.java,v $ \n";
    }

	/* (non-Javadoc)
	 * @see org.nees.rbnb.RBNBBase#setOptions()
	 */
	protected Options setOptions() {
		Options opt = setBaseOptions(new Options()); // uses h, s, p
		opt.addOption("N",true,"source_name *FakeVideoSource");
		opt.addOption("C",true,"channel_name *video.jpg");
		opt.addOption("P",true,"pathname_for_pictures *./duke/Duke");
		opt.addOption("K",true,"picture_count *10");
		opt.addOption("t",true,"timer_interval *1000");
		opt.addOption("z",true,"cache size *" + DEFAULT_CACHE_SIZE);
		opt.addOption("Z",true,"archive size *" + DEFAULT_ARCHIVE_SIZE);
		return opt;
	}

	/* (non-Javadoc)
	 * @see org.nees.rbnb.RBNBBase#setArgs(org.apache.commons.cli.CommandLine)
	 */
	protected boolean setArgs(CommandLine cmd) {
		if (!setBaseArgs(cmd)) return false;

		if (cmd.hasOption('N')) {
			String a=cmd.getOptionValue('N');
			if (a!=null) sourceName=a;
		}
		if (cmd.hasOption('C')) {
			String a=cmd.getOptionValue('C');
			if (a!=null) channelName=a;
		}
		if (cmd.hasOption('P')) {
			String a=cmd.getOptionValue('P');
			if (a!=null) picturePath=a;
		}
		if (cmd.hasOption('K')) {
			String a=cmd.getOptionValue('K');
			if (a!=null) pictureCount=Integer.parseInt(a);
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
		
        if ((archiveSize > 0) && (archiveSize < cacheSize)){
            System.err.println(
                "a non-zero archiveSize = " + archiveSize + " must be greater then " +
                    "or equal to cacheSize = " + cacheSize);
            return false;
        }

		System.out.println("Starting PictureSource on " + getServer() + " as " + sourceName);
		System.out.println("  Channel name = " + channelName + "; timer interval = " + timerInterval);
		System.out.println("  Using images " + picturePath + "01.jpg"
				+ " through " + picturePath 
				+ (pictureCount<10?("0" + pictureCount):("" + pictureCount)) + ".jpg");
		System.out.println("  Use PictureSource -h to see optional parameters");
		return true;
	}
	

	public boolean loadImageFiles() {
		fileBuffer = new byte[10][];
		text = new String[10];

		current = 0;
		
		URL fig = null;
		try {
			for (int i = 1; i < (pictureCount + 1); i++ )
			{
				text[i-1] = picturePath + ((i<10)?("0" + i):(""+i)) + ".jpg";
			}
			
			for (int i = 0; i < text.length; i++)
			{
				File f = new File(text[i]);
				FileInputStream in = new FileInputStream(f);
				int fileLength = (int)f.length();
				byte[] buffer = new byte[fileLength];
				in.read(buffer);
				fileBuffer[i] = buffer;
				System.out.println("Read " + text[i] + " for " + fileLength + " bytes");
			}
	
			return true;
		}
		catch (Exception e)
		{
			System.out.println("Loading Image files failed at " + current + "; " + e);
			e.printStackTrace();
			return false;
		}
	}

	private byte[] next()
	{
		byte[] ret = null;
		int l = (int)fileBuffer.length;
		current = ( current + 1 ) % l;
		ret = fileBuffer[current];
		return ret;
	}
	
	private boolean exec()
	{
		connect();
		if (!connected) return false;
		startThread();
		return true;
	}
	
	private void connect() {
		connected = false;
		if (archiveSize > 0)
			source=new Source(cacheSize, "create", archiveSize);
		else
			source=new Source(cacheSize, "none", 0);
		try {
			source.OpenRBNBConnection(getServer(), sourceName);
		} catch (SAPIException e) {
			System.err.println("Failed to connect to RBNB server.");
			source = null;
			return;
		}

		cmap = new ChannelMap();
		cmap.PutTimeAuto("timeofday");
		channelId = -1;
		try {
			channelId = cmap.Add(channelName);
		} catch (SAPIException e) {
			System.err.println("Failed to add video channel to channel map.");
			disconnect();
			return;
		}
		cmap.PutMime(channelId, "image/jpeg");
		System.out.println("ChannelId = " + channelId);
		connected = true;
	} // readySource

	private void startThread() {
		// Use this inner class to hide the public run method
		Runnable r = new Runnable() {
			public void run() {
				runWork();
			}
		};
		running = true;
		timerThread = new Thread(r, "Timer");
		timerThread.start();
		System.out.println("PictureSource: Started thread.");
	}

	public void stopThread()
	{
		if (!connected) return;
		
		running = false;
		timerThread.interrupt();
		System.out.println("PictureSource: Stopped thread.");
	}

	private void runWork ()
	{
		try
		{
			while(isRunning())
			{
				if (!oneStep()) break;
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					return;
				}
			}
		}
		catch (Throwable t)
		{
			System.out.println("PictureSource: error while running -- " + t);
		}
		if (connected) disconnect();
		running = false;
	}
			
	private boolean oneStep()
	{
		try{
			byte[] imageData = next();
			cmap.PutDataAsByteArray(channelId, imageData);
			source.Flush(cmap);
//			System.out.println("On channel " + channelId + ": sent image file " + text[current] + " for "
//				+ imageData.length + " bytes");
		} catch (Exception e){
			System.err.println("Failed to send image " + current);
			e.printStackTrace();
			disconnect();
			return false;
		}
		return true;
	}

	private void disconnect()
	{
		source.CloseRBNBConnection();
		source = null;
		cmap = null;
		connected = false;				
	}
	
	public boolean isRunning()
	{
		return (connected && running);
	}

	/* (non-Javadoc)
	 * @see org.nees.rbnb.RBNBBase#start()
	 */
	public boolean start() {
		if (isRunning()) return false;
		if (connected) disconnect();
		if (loadImageFiles())
		{
			return exec();
		}
		return false;
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
