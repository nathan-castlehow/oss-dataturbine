/*
 * ArchiveToRbnb.java
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
 *   $Revision: 153 $
 *   $Date: 2007-09-24 13:10:37 -0700 (Mon, 24 Sep 2007) $
 *   $RCSfile: ArchiveToRbnb.java,v $ 
 * 
 */
package org.nees.tivo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import org.nees.rbnb.ArchiveUtility;
import org.nees.rbnb.TimeProgressListener;

import com.rbnb.sapi.*;

/**
 * Pushes the image stream from an archive segment into RBNB.
 * 
 * @author Terry E Weymouth
 * @see org.nees.rbnr.JpgLoaderSource
 */
public class ArchiveToRbnb {

    private static final String SOURCE_NAME = "Archive";
    private static final String CHANNEL_NAME = "video.jpg";
    
    private String sourceName = SOURCE_NAME;
    private String channelName = CHANNEL_NAME;

    private String segmentName = null;
    private ArchiveSegmentInterface segment;
    private ArchiveImageInterface[] imageArray;
    
    private String startTimeString;
    private long startTime = 0;
    private String endTimeString;
    private long endTime = 0;
    private long duration = 0;
    
    private boolean justPrint = false;  // true for debugging
    
    private String server;
    private Source source;
    private ChannelMap cMap;
    private int channelIndex;
    ChannelMap sMap;
    int index;
    boolean connected = false;
    boolean detach = true;
    
    private static final int DEFAULT_CACHE_SIZE = 900;
    private int rbnbCacheSize = DEFAULT_CACHE_SIZE;
    private static final int DEFAULT_ARCHIVE_SIZE = DEFAULT_CACHE_SIZE * 2;
    private int rbnbArchiveSize = DEFAULT_ARCHIVE_SIZE;

    private boolean statusOk = true;
    private Vector statusMessages = new Vector();
    
    Thread loaderDataThread;
    boolean runit = false;
    private boolean stillRunning = false; // actually stopped

    private static final double EST_CYCLE_TIME = 0.2; // 2/10's of a second per file
    private double estimatedDuration = 0.0;
    private double consumedTime = 0.0;
    private double lastNotify = 0.0;
    private int imageCount = 0;
    private long clockStartTime;

    protected String getCVSVersionString()
    {
        return
            "  CVS information... \n" +
            "  $Revision: 153 $\n" +
            "  $Date: 2007-09-24 13:10:37 -0700 (Mon, 24 Sep 2007) $\n" +
            "  $RCSfile: ArchiveToRbnb.java,v $ \n";
    }

    public boolean setup(String host, String port, String source, 
            String channel, ArchiveSegmentInterface seg) {
        return setup(host, port, source, channel, 0, 0, seg);
    }

    public boolean setup(String arg_host, String arg_port,
            String arg_sourceName, String arg_channelName, 
            long arg_startTime, long arg_endTime,
            ArchiveSegmentInterface arg_segment)
    {
        server = arg_host + ":" + arg_port;
        sourceName = arg_sourceName;
        channelName = arg_channelName;
        startTime = arg_startTime;
        endTime = arg_endTime;
        segment = arg_segment;
        segmentName = segment.getName();

        startTimeString = 
            (ArchiveUtility.getCommandFormat()).format(new Date(startTime));
        endTimeString = 
            (ArchiveUtility.getCommandFormat()).format(new Date(endTime));
        
        if ((rbnbArchiveSize > 0) && (rbnbArchiveSize < rbnbCacheSize)){
            System.err.println(
                "a non-zero archiveSize = " + rbnbArchiveSize + " must be greater then " +
                    "or equal to cacheSize = " + rbnbCacheSize);
            return false;
        }

        // get earliest and latest possible time

        long earliestTime = segment.getStartTime();
//        String earliestTimeString = 
//                (ArchiveUtility.getCommandFormat()).format(new Date(earliestTime));         

        long latestTime = segment.getEndTime();
//        String latestTimeString =
//                (ArchiveUtility.getCommandFormat()).format(new Date(latestTime));           
 
        if (startTime == 0)
        {
            startTime = earliestTime;
            startTimeString = 
                (ArchiveUtility.getCommandFormat()).format(new Date(startTime));
        }
        
        if (endTime == 0)
        {
            endTime = latestTime;
            endTimeString = 
                (ArchiveUtility.getCommandFormat()).format(new Date(endTime));
        }
        
        if ((rbnbArchiveSize > 0) && (rbnbArchiveSize < rbnbCacheSize)) {
            System.out.println("Archive size = " + rbnbArchiveSize + "; it must be " +
                "bigger than chache size; chache size = " + rbnbCacheSize);
            return false;
        }
        
        if (startTime > endTime)
        {
            System.out.println("Start time is greater then end time; exiting.");
            return false;
        }

        if (startTime > latestTime)
        {
            System.out.println("Start time comes after the latest time in the " +
                "archive; exiting");
            return false;
        }

        if (endTime < earliestTime)
        {
            System.out.println("End time is before the earlist time " +
                "in the archive; exiting");
            return false;
        }

        duration = endTime - startTime;
        
        imageArray = (ArchiveImageInterface[]) segment.getSortedArray(
                startTime, endTime);

        System.out.println("Starting ArchiveToRbnb on " + server + " as "
             + sourceName);
        System.out.println("  Channel name = " + channelName
            + "  Cache Size = " + rbnbCacheSize + "; Archive size = " + rbnbArchiveSize);
        if (detach)
            System.out.println("  (the Source will be detached on exit.)");
        System.out.println("  from images archived to segment " + segmentName);
        System.out.println("  with StartTime = " + startTimeString + "; EndTime = " 
            + endTimeString);
//        System.out.println("  while earliest time in the archive = " + earliestTimeString);
//        System.out.println("  and latest time in the archive = " + latestTimeString);
        System.out.println("");

        System.out.println("Found " + imageArray.length + " images for time the " +
                "time range from " + startTimeString + " to " + endTimeString);

            if (justPrint) // for debugging
            {
                if (imageArray.length > 0)
                {
                    System.out.println("The list of images that would have been send by this " +
                        "request are:");
                    for (int i = 0; i < imageArray.length; i++)
                    {
                        System.out.println("  " + imageArray[i].toString());
                    }
                    System.out.println("");
                }
                System.out.println("Exiting without sending image.");
                return false;
            }
            
        return true;
    }
    
    public boolean connect()
    {
        try {
            // Create a source and connect:
            if (rbnbArchiveSize > 0)
                source=new Source(rbnbCacheSize, "create", rbnbArchiveSize);
            else
                source=new Source(rbnbCacheSize, "none", 0);
            source.OpenRBNBConnection(server,sourceName);
            cMap = new ChannelMap();
            channelIndex = cMap.Add(channelName);
            connected = true;
            System.out.println("ArchiveToRbnb: Connection made to server = "
                + server);
        } catch (SAPIException se) {
            se.printStackTrace();
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
        loaderDataThread = new Thread(r, "Data Thread");
        loaderDataThread.start();
        System.out.println("ArchiveToRbnb: Started thread.");
    }

    public void stopThread()
    {
        runit = false;
        if (loaderDataThread != null)        
            loaderDataThread.interrupt();
        System.out.println("ArchiveToRbnb: Stopped thread.");
    }
    
    private void runWork ()
    {
        clockStartTime = System.currentTimeMillis();
        statusMessages = new Vector();
        imageCount = 0;
        estimatedDuration = (EST_CYCLE_TIME * (double)imageArray.length);
        consumedTime = 0.0;
        notifyAllListeners();
        long start = System.currentTimeMillis();
        long old = start;
        try {
            for (int i = 0; i < imageArray.length; i++)
            {
                long timeStamp = imageArray[i].getTime();
                double time = ((double)timeStamp)/1000.0;
                cMap.PutTime(time,0.0);
                cMap.PutMime(channelIndex, "image/jpeg");
                byte[] buffer = readImageFully(i);
                cMap.PutDataAsByteArray(channelIndex, buffer);
                source.Flush(cMap);

                imageCount++;
                
                old = System.currentTimeMillis();
                
                updateTimeEstimate();
                if (lastNotify + 10.0 < consumedTime)
                {
                    notifyAllListeners();
                    lastNotify = consumedTime;
System.out.println("Send " + imageCount + " files in " + (old-start) + " milliseconds for " +
    "an average of " + ((old-start)/imageCount) + " milliseconds per file");                    
                }
            } // end for
            System.out.println("Send " + imageCount + " files in " + (old-start) + " milliseconds for " +
                    "an average of " + ((old-start)/imageCount) + " milliseconds per file");                    
            statusOk = true;
            addToStatus("Normal Completion");
        } catch (SAPIException se) {
            se.printStackTrace();
            addToStatus("SAPIException");
            statusOk = false;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            addToStatus("FileNotFoundException");
            statusOk = false;
        } catch (IOException e) {
            e.printStackTrace();
            addToStatus("IOException");
            statusOk = false;
        }
        stopThread();
        disconnect();
        loaderDataThread = null;
        estimatedDuration = consumedTime;
        notifyAllListeners();
        addToStatus("Done.");
    }
    
    private void disconnect() {
        if (detach)
        {
            try {Thread.sleep(500);} catch (Exception ignore){}
            source.Detach();
        }
        else
            source.CloseRBNBConnection();
        connected = false;
        source = null;
    }

    public boolean isRunning()
    {
        return (connected && runit);
    }
    
    private void addToStatus(String s) {
        if (s == null) return;
        if (s.length() == 0) return;
        statusMessages.addElement(s);
    }
    
    public boolean isStillRunning()
    {
        return stillRunning;
    }

    private void updateTimeEstimate()
    {
        if (imageArray == null)
        {
            estimatedDuration = duration;
            consumedTime = 0.0;
            return;
        }
        long now = System.currentTimeMillis();
        long runningTime = clockStartTime - now;
        double runningTimeD = ((double)runningTime)/1000.0;
        double secondsPer = runningTimeD/((double)imageCount);
        estimatedDuration = secondsPer * ((double)imageArray.length);
        consumedTime = runningTimeD;
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
                    progressUpdate(estimatedDuration,consumedTime)){}
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
    
    int bufferSize = 24000;
    
    private byte[] readImageFully(int n) throws IOException {
        byte[] buf = new byte[bufferSize];
        byte[] next = new byte[bufferSize];
        int count;
        int nextCount;
        InputStream in = imageArray[n].getImageInputStream();
        count = in.read(buf);

        while ((nextCount = in.read(next)) > 0) {
            byte[] newBuf = new byte[count + nextCount];
            System.arraycopy(buf, 0, newBuf, 0, count);
            System.arraycopy(next, 0, newBuf, count, nextCount);
            buf = newBuf;
            count += nextCount;
            bufferSize = count;
            System.out.println("Buffer size increased to " + bufferSize);
        }

        byte[] lastBuf = new byte[count];
        System.arraycopy(buf, 0, lastBuf, 0, count);

        return lastBuf;
    }

}
