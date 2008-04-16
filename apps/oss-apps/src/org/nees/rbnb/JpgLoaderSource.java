/*
 * Created on Feb 4, 2005
 */
package org.nees.rbnb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileInputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.TimeZone;
import java.util.Vector;
import java.text.ParseException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import org.nees.rbnb.RBNBFrameUtility;

import com.rbnb.sapi.*;

/**
 * This is a compliment to JpgSaverSink; it will load, into an RBNB ring buffer
 * the image stream captured by JpgSaverSink.
 * 
 * @author Terry E Weymouth
 * @see org.nees.rbnr.JpgSaverSink
 */
public class JpgLoaderSource extends RBNBBase {

  private static final String SOURCE_NAME = "Archive";

  private static final String CHANNEL_NAME = "video.jpg";

  private String sourceName = SOURCE_NAME;

  private String channelName = CHANNEL_NAME;

  private String startTimeString;

  private long startTime = 0;

  private String endTimeString;

  private long endTime = 0;

  private boolean justPrint = false;

  private static final String BASE_DIR = JpgSaverSink.BASE_DIR;

  private String baseDir = BASE_DIR;

  private Source source;

  private ChannelMap cMap;

  private int channelIndex;

  ChannelMap sMap;

  int index;

  boolean connected = false;

  private static final int DEFAULT_CACHE_SIZE = 1024;

  private int cacheSize = DEFAULT_CACHE_SIZE;

  private static final int DEFAULT_ARCHIVE_SIZE = 0; //will be calculated based on number of files(frames)

  private int archiveSize = DEFAULT_ARCHIVE_SIZE;

  private static final String DEFAULT_ARCHIVE_MODE = "append";

  private String archiveMode = DEFAULT_ARCHIVE_MODE;

  /**
   * LJM 060519 variable to hold the time (in hours) desired for the length of
   * the ring buffer user to calculate cache and archive.
   */
  private double rbTime = -1.0;

  /**
   * a variable to set what percentage of the archived frames are to be cached
   * by the rbnb server.
   */
  private double rbCachePercent = 0.1;

  private boolean statusOk = true;

  private Vector statusMessages = new Vector();

  Thread loaderDataThread;

  boolean runit = false;

  private boolean stillRunning = false; // actually stopped

  //release 3.6 -- pause time no longer used
//  private static final long REPLAY_INTERVAL = 0;

//  private long pauseTime = REPLAY_INTERVAL;

  private static final double EST_CYCLE_TIME = 0.2; // 2/10's of a second per
                                                    // file

  private double estimatedDuration = 0.0;

  private double consumedTime = 0.0;

  private double lastNotify = 0.0;

  private int filesSent = 0;

  private File[] fileList;

  public static void main(String[] args) {
    JpgLoaderSource s = new JpgLoaderSource();
    if (s.parseArgs(args) && s.exec())
      s.startThread();
  }

  protected String getCVSVersionString() {
    return ("$LastChangedDate$\n"
        + "$LastChangedRevision$"
        + "$LastChangedBy$"
        + "$HeadURL$");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.nees.rbnb.RBNBBase#setOptions()
   */
  protected Options setOptions() {
    Options opt = setBaseOptions(new Options()); // uses h, s, p
    opt.addOption("n", true, "Source Name *" + SOURCE_NAME);
    opt.addOption("c", true, "Source Channel Name *" + CHANNEL_NAME);
    opt.addOption("d", true, "Base directory path *" + BASE_DIR);
    opt.addOption("X", false,
        "Just print the list of files that would be sent;"
            + " don't actually send them.");
    opt.addOption("S", true,
        "Start time (defaults earlest time in Base Directory)");
    opt.addOption("E", true,
        "End time (defaults latest time in Base Directory)");
    // removed at 3.6 release, decided that its not serving any purpose
    // other than controlling the upload rate (Not the frame rate)
/*    opt.addOption("R", true, "Replay interval in microseconds *"
        + REPLAY_INTERVAL + "; an inter-image time; "
        + "introduce a pause between sent images to simulate a frame rate; "
        + "int in milliseconds; set to zero to suppress pause");
*/        
    opt.addOption("z", true, "cache size *" + DEFAULT_CACHE_SIZE);
    opt.addOption("Z", true, "archive size *" + DEFAULT_ARCHIVE_SIZE);
    setNotes("Read jpg images between start time and end time from "
        + "the directory structure starting at the base directory "
        + "and posts them to the specified channel. "
        + "Time format is yyyy-mm-dd:hh:mm:ss.nnn.\n");
    return opt;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.nees.rbnb.RBNBBase#setArgs(org.apache.commons.cli.CommandLine)
   */
  protected boolean setArgs(CommandLine cmd) {

    if (!setBaseArgs(cmd))
      return false;

    if (cmd.hasOption('X')) {
      justPrint = true;
    }
    if (cmd.hasOption('n')) {
      String a = cmd.getOptionValue('n');
      if (a != null)
        sourceName = a;
    }
    if (cmd.hasOption('c')) {
      String a = cmd.getOptionValue('c');
      if (a != null)
        channelName = a;
    }
    if (cmd.hasOption('d')) {
      String a = cmd.getOptionValue('d');
      if (a != null)
        baseDir = a;
    }
/*    
    if (cmd.hasOption('R')) {
      String a = cmd.getOptionValue('R');
      if (a != null) {
        try {
          long value = (new Long(a)).longValue();
          pauseTime = value;
        } catch (Exception e) {
          System.out.println("Parse of pause time failed " + a);
          printUsage();
          return false;
        }
      }
    }
*/    
    if (cmd.hasOption('S')) {
      String a = cmd.getOptionValue('S');
      if (a != null) {
        try {
          startTimeString = a;
          Date d = ArchiveUtility.getCommandFormat().parse(a);
          startTime = d.getTime();
        } catch (Exception e) {
          System.out.println("Parse of start time failed " + a);
          printUsage();
          return false;
        }
      }
    }
    if (cmd.hasOption('E')) {
      String a = cmd.getOptionValue('E');
      if (a != null) {
        try {
          endTimeString = a;
          Date d = ArchiveUtility.getCommandFormat().parse(a);
          endTime = d.getTime();
        } catch (Exception e) {
          System.out.println("Parse of end time failed " + a);
          printUsage();
          return false;
        }
      }
    }

    if (cmd.hasOption('z')) {
      String a = cmd.getOptionValue('z');
      if (a != null)
        try {
          Integer i = new Integer(a);
          int value = i.intValue();
          cacheSize = value;
          System.out.println("sizes " + cacheSize + "," + value);
        } catch (NumberFormatException nfe) {
          System.out
              .println("Please ensure to enter a numeric value for -z option. "
                  + a + " is not valid!");
          return false;
        }
    }

    if (cmd.hasOption('Z')) {
      String a = cmd.getOptionValue('Z');
      if (a != null)
        try {
          Integer i = new Integer(a);
          int value = i.intValue();
          archiveSize = value;
        } catch (NumberFormatException nfe) {
          System.out
              .println("Please ensure to enter a numeric value for -Z option. "
                  + a + " is not valid!");
          return false;
        }
    }

    return finishSetup();
  }

  // to use programaticially (see main)
  // if (setup(args) && exec())
  // startThread();
  public boolean setup(String server, String port, String arg_sourceName,
      String arg_channelName, long arg_startTime, long arg_endTime,
      long arg_pauseTime, String arg_baseDir, int arg_cacheSize,
      int arg_archiveSize) {
    setServerName(server);
    setServerPort(port);
    sourceName = arg_sourceName;
    channelName = arg_channelName;
    startTime = arg_startTime;
    endTime = arg_endTime;
//    pauseTime = arg_pauseTime;
    baseDir = arg_baseDir;
    cacheSize = arg_cacheSize;
    archiveSize = arg_archiveSize;

    startTimeString = (ArchiveUtility.getCommandFormat()).format(new Date(
        startTime));
    endTimeString = (ArchiveUtility.getCommandFormat())
        .format(new Date(endTime));

    return finishSetup();
  }

  public boolean finishSetup() {

    if ((archiveSize > 0) && (archiveSize < cacheSize)) {
      System.err.println("a non-zero archiveSize = " + archiveSize
          + " must be greater then " + "or equal to cacheSize = " + cacheSize);
      return false;
    }

    // get earliest and latest possible time

    long earliestTime;
    String earliestTimeString;
    File bottom = ArchiveUtility.recursivlyFindLeast(new File(baseDir));
    try {
      earliestTime = ArchiveUtility.makeTimeFromFilename(bottom);
      earliestTimeString = (ArchiveUtility.getCommandFormat()).format(new Date(
          earliestTime));
    } catch (ParseException e) {
      System.out.println("Parse Exception in attempting to determine "
          + "earliest from file name " + bottom);
      System.out.println("Does the image store contain extra files? Exiting.");
      return false;
    }

    long latestTime;
    String latestTimeString;
    File top = ArchiveUtility.recursivlyFindGreatest(new File(baseDir));
    try {
      latestTime = ArchiveUtility.makeTimeFromFilename(top);
      latestTimeString = (ArchiveUtility.getCommandFormat()).format(new Date(
          latestTime));
    } catch (ParseException e) {
      System.out.println("Parse Exception in attempting to determine "
          + "latest time from file name " + top);
      System.out.println("Does the image store contain extra files? Exiting.");
      return false;
    }

    if (startTime == 0) {
      startTime = earliestTime;
      startTimeString = (ArchiveUtility.getCommandFormat()).format(new Date(
          startTime));
    }

    if (endTime == 0) {
      endTime = latestTime;
      endTimeString = (ArchiveUtility.getCommandFormat()).format(new Date(
          endTime));
    }
    if ((archiveSize > 0) && (archiveSize < cacheSize)) {
      System.out.println("Archive size = " + archiveSize + "; it must be "
          + "bigger than chache size; chache size = " + cacheSize);
      return false;
    }
/*
    if (pauseTime < 0) {
      System.out.println("Negitive pause time not permitted = " + pauseTime);
      return false;
    }
*/
    System.out.println("Starting JpgLoaderSource on " + getServer() + " as "
        + sourceName);
    System.out.println("  Channel name = " + channelName + "  Cache Size = " + cacheSize);
    System.out.println("  from images archived to " + baseDir);
    System.out.println("  with StartTime = " + startTimeString + "; EndTime = "
        + endTimeString);
    System.out.println("  while earliest time in the archive = "
        + earliestTimeString);
    System.out
        .println("  and latest time in the archive = " + latestTimeString);
/*
    if (pauseTime > 0)
      System.out.println("  interframe pause time = " + pauseTime);
*/      
    System.out.println("  Use JpgLoaderSource -h to see optional parameters");
    System.out.println("");

    if (startTime > endTime) {
      System.out.println("Start time is greater then end time; exiting.");
      return false;
    }

    if (startTime > latestTime) {
      System.out.println("Start time comes after the latest time in the "
          + "archive; exiting");
      return false;
    }

    if (endTime < earliestTime) {
      System.out.println("End time is before the earlist time "
          + "in the archive; exiting");
      return false;
    }

    return true;
  }

  public boolean exec() {
    // create ordered file list
    fileList = ArchiveUtility.getSortedFileArray(baseDir, startTime, endTime);

    System.out.println("Found " + fileList.length + " files for time the "
        + "time range from " + startTimeString + " to " + endTimeString);
/*
    if (pauseTime > 0) {
      double playTime = ((double) (fileList.length * pauseTime) / 1000.0);
      System.out.println("Playback time will be approximatly " + playTime
          + " seconds (interframe time = " + pauseTime + " miliseconds).");
    }
*/    
    System.out.println("");

    if (justPrint) {
      if (fileList.length > 0) {
        System.out
            .println("The list of files that would have been send by this "
                + "request are:");
        for (int i = 0; i < fileList.length; i++) {
          System.out.println("  " + fileList[i]);
        }
        System.out.println("");
      }
      System.out.println("Exiting without sending files.");
      return true;
    }

    if (archiveSize == 0) {
      archiveSize = fileList.length + 1024;
      System.out.println("1024 added to required Archive size of " + fileList.length + " -- Archive size: " + archiveSize);
    }
    
    try {
      source = new Source(cacheSize, archiveMode, archiveSize);
      source.OpenRBNBConnection(getServer(), sourceName);
      cMap = new ChannelMap();
      channelIndex = cMap.Add(channelName);
      connected = true;
      System.out.println("JpgLoaderSource: Connection made to server = "
          + getServer() + " as " + sourceName
          + " and drawing from the archive at directory path " + baseDir + ".");
      System.out.println("with RBNB Cache Size = " + cacheSize
          + " and RBNB Archive Size = " + archiveSize);
    } catch (SAPIException se) {
      se.printStackTrace();
      return false;
    }
    return true;
  }

  public void startThread() {

    if (!connected)
      return;

    // Use this inner class to hide the public run method
    Runnable r = new Runnable() {
      public void run() {
        runWork();
      }
    };
    runit = true;
    loaderDataThread = new Thread(r, "Data Thread");
    loaderDataThread.start();
    System.out.println("JpgLoaderSource: Started thread.");
  }

  public void stopThread() {
    runit = false;
    if (loaderDataThread != null)
      loaderDataThread.interrupt();
    System.out.println("JpgLoaderSource: Stopped thread.");
  }

  private void runWork() {
    statusMessages = new Vector();
    filesSent = 0;
    estimatedDuration = (EST_CYCLE_TIME * (double) fileList.length);
/*
    if (pauseTime > (long) (EST_CYCLE_TIME * 1000.0))
      estimatedDuration = (pauseTime * fileList.length) / 1000.0;
*/  
    consumedTime = 0.0;
    notifyAllListeners();
    long start = System.currentTimeMillis();
    long old = start;
    try {
      for (int i = 0; i < fileList.length; i++) {
        File f = fileList[i];
        long timeStamp = ArchiveUtility.makeTimeFromFilename(f);
        double time = ((double) timeStamp) / 1000.0;
        cMap.PutTime(time, 0.0);
        cMap.PutMime(channelIndex, "image/jpeg");
        FileInputStream in = new FileInputStream(f);
        byte[] buffer = new byte[(int) f.length()];
        in.read(buffer);
        cMap.PutDataAsByteArray(channelIndex, buffer);
        source.Flush(cMap);
        in.close();

        // remove at 3.6 release. Its not really simulating the frame rate as indicated below!
        // simulate frame rate -- 
/*        long now = System.currentTimeMillis();

        if (pauseTime > 0) {
          if (pauseTime > (now - old)) {
            try {
              Thread.sleep(pauseTime - (now - old));
            } catch (InterruptedException ignore) {
            }
          }
        }
*/        
        old = System.currentTimeMillis();

        filesSent++;
        consumedTime = (old - start) / 1000.0; // milliseconds to seconds
        double revisedEst = ((double) fileList.length)
            * (consumedTime / (double) filesSent);
        estimatedDuration = (0.9 * estimatedDuration) + (0.1 * revisedEst);

        if (lastNotify + 10.0 < consumedTime) {
          notifyAllListeners();
          lastNotify = consumedTime;
          
//          System.out.println("Send " + filesSent + " files in " + (old - start)
//              + " milliseconds for " + "an average of "
//              + ((old - start) / filesSent) + " milliseconds per file");
        }
      } // end for
      
      long end = System.currentTimeMillis();
      System.out.println("Sent " + filesSent + " files in " + (end - start)
          + " milliseconds for an average of "
          + ((end - start) / filesSent) + " milliseconds per file");
     
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
    } catch (ParseException e) {
      e.printStackTrace();
      addToStatus("ParseException");
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
    // LJM 060519 if cache or archive, then detach
    if ((cacheSize != 0 || archiveSize != 0) && source != null) {
      source.Detach(); // close and keep cache and archive
    } else if (source != null) { // they are both zero; close and scrap
      source.CloseRBNBConnection();
    } else {
    }
    source = null;
    connected = false;
  }

  public boolean isRunning() {
    return (connected && runit);
  }

  private void addToStatus(String s) {
    if (s == null)
      return;
    if (s.length() == 0)
      return;
    statusMessages.addElement(s);
  }

  public boolean isStillRunning() {
    return stillRunning;
  }

  Vector listeners = new Vector();

  public void addTimeProgressListener(TimeProgressListener l) {
    listeners.addElement(l);
  }

  public void removeTimeProgressListener(TimeProgressListener l) {
    listeners.removeElement(l);
  }

  public void removeAllTimeProgressListeners() {
    listeners.removeAllElements();
  }

  private void notifyAllListeners() {
    for (Enumeration e = listeners.elements(); e.hasMoreElements(); ((TimeProgressListener) e
        .nextElement()).progressUpdate(estimatedDuration, consumedTime))
      ;
  }

  /**
   * @return the running status of the current capture thread, or the completion
   *         status of the last thread run
   */
  public boolean isStatusOk() {
    return statusOk;
  }

  /**
   * @return the String array that is a trace of status messages from the
   *         execution of the last thread
   */
  public String[] getStatusMessages() {
    String[] messages = new String[statusMessages.size()];
    int i = 0;
    for (Enumeration e = statusMessages.elements(); e.hasMoreElements();) {
      messages[i++] = (String) e.nextElement();
    }
    return messages;
  }
}
