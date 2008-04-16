/*
 * Created on December 12, 2003
 * Modified to become a source for the Panasonic BLC10,
 * Paul Hubbard June 2006.
 */
package org.nees.rbnb;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;
import java.util.Date;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.Source;
import com.rbnb.sapi.SAPIException;

import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLine;

import org.nees.rbnb.RBNBFrameUtility;

/**
* PanaSource
 * @author Jason P. Hanley
 * @author Terry E. Weymouth
 * @author Lawrence J. Miller
 * 
 * adopted from org.nees.buffalo.video.AxisSource by Jason T. Hanley
 * 
 * PanaSource - takes an image stream from an axis box and put the
 * JPEG images into RBNB with timestamps. 
 * @see org.nees.buffalo.axis.AxisSource
 */
public class PanaSource extends RBNBBase {
   
   private final static String DEFAULT_RBNB_SOURCE = "PanaVideo";
   private final static String DEFAULT_RBNB_CHANNEL = "video.jpg";
   private final static String DEFAULT_FRAME_RES = "352x240";
   private final static String DEFAULT_FRAME_HIGHRES = "640x480";
   private final static String CAMERA_HOST = "undefined";
   private final static int DEFAULT_CAMERA_FPS = 30;
   private final static int DEFAULT_CAMERA_NUMBER = 1;
   
   private final static long RETRY_INTERVAL = 1000;
   
   private String cameraURLString;
   private String cameraHost = CAMERA_HOST;
   private String cameraRes = DEFAULT_FRAME_HIGHRES;
   private String rbnbSourceName = DEFAULT_RBNB_SOURCE;
   private String rbnbChannelName = DEFAULT_RBNB_CHANNEL;
   private int cameraFPS = DEFAULT_CAMERA_FPS;
   private int cameraNumber = DEFAULT_CAMERA_NUMBER;
   
   private boolean usePassword = false;
   private String username = null;
   private String password = null;
   
   private static final int DEFAULT_CACHE_SIZE = 900;
   private int cacheSize = DEFAULT_CACHE_SIZE;
   private static final int DEFAULT_ARCHIVE_SIZE = 0;
   private int archiveSize = DEFAULT_ARCHIVE_SIZE;
   
   /** LJM 060519
    * variable to hold the time (in hours) desired for the length of the ring buffer
    * user to calculate cache and archive.
    */
   private double rbTime = -1.0;
   /** a variable to set what percentage of the archived frames are to be
    * cached by the rbnb server.
    */
   private static final double DEFAULT_CACHE_PERCENT = 10;
   private double rbCachePercent = DEFAULT_CACHE_PERCENT;
   
   Source source = null;
   ChannelMap sMap;
   int index;
   boolean connected = false;
   
   Thread timerThread;
   boolean runit = false;
   boolean retry = true;
   
   public static void main(String[] args) {
      // start from command line
      PanaSource a = new PanaSource();
      if (a.parseArgs(args)) {
         // test connect for early failure detection
         if (!a.connect()) {
            System.out.println("Connect not connect to RBNB");
            return;
         }
         a.disconnect();
         a.startThread();
      }
   }
   
   protected String getCVSVersionString()
   {
      return (
            "$LastChangedDate$\n" +
            "$LastChangedRevision$" +
            "$LastChangedBy$" +
            "$HeadURL$"
             );
   }
   
   /* (non-Javadoc)
      * @see org.nees.rbnb.RBNBBase#setOptions()
      */
   protected Options setOptions() {
      Options opt = setBaseOptions(new Options()); // uses h, s, p, v
      opt.addOption("A", true, "Panasonic camera host (required)");
      opt.addOption("S", true, "RBNB Source Name *" + DEFAULT_RBNB_SOURCE);
      opt.addOption(
                    "C",
                    true,
                    "RBNB Source Channel Name *" + DEFAULT_RBNB_CHANNEL);
      opt.addOption("H", false, "* (high res, 640x480)");
      opt.addOption("L", false, "(low res, 352x240)");
      opt.addOption("R",true,"Set an arbitrary resolution, " +
                    "a string that is sent to the camera unaltered;" +
                    "defaults to no resolution parameter");
      opt.addOption("n", true, "camera number *" + DEFAULT_CAMERA_NUMBER);
      opt.addOption("f", true, "frame rate, Hz, *" + DEFAULT_CAMERA_FPS);
      opt.addOption("U", true, "username (no default)");
      opt.addOption("P", true, "password  (no default)");
      opt.addOption("z", true, "cache size *" + DEFAULT_CACHE_SIZE);
      opt.addOption("Z", true, "archive size *" + DEFAULT_ARCHIVE_SIZE);
      // LJM 060521
      opt.addOption ("r", true, "length (in hours) to create the ring buffer for this source");
      opt.addOption ("m", true, "percentage (%) of the ring buffer specified in -r to cache in memory *" + DEFAULT_CACHE_PERCENT);
      return opt;
   }
   
   protected boolean setArgs(CommandLine cmd) {
      
      if (!setBaseArgs(cmd))
         return false;
      
      if (cmd.hasOption('A')) {
         String a = cmd.getOptionValue('A');
         if (a != null)
            cameraHost = a;
      }
      
      cameraRes = null;
      
      // Low res, no arguments
      if (cmd.hasOption('L')) {
         cameraRes = DEFAULT_FRAME_RES;
      }
      // High res, no arguments
      if (cmd.hasOption('H')) {
         cameraRes = DEFAULT_FRAME_HIGHRES;
      }
      if (cmd.hasOption('R'))
      {
         cameraRes = cmd.getOptionValue('R');
      }
      // frames per second, int argument
      if (cmd.hasOption('f')) {
         String a = cmd.getOptionValue('f');
         if (a != null)
            try {
               Integer i = new Integer(a);
               int value = i.intValue();
               cameraFPS = value;
            } catch (Exception ignore) {}
      }
      if (cmd.hasOption('n')) {
         String a = cmd.getOptionValue('n');
         if (a != null)
            try {
               Integer i = new Integer(a);
               int value = i.intValue();
               cameraNumber = value;
            } catch (Exception ignore) {}
      }
      if (cmd.hasOption('S')) {
         String a = cmd.getOptionValue('S');
         if (a != null)
            rbnbSourceName = a;
      }
      if (cmd.hasOption('C')) {
         String a = cmd.getOptionValue('C');
         if (a != null)
            rbnbChannelName = a;
      }
      if (cmd.hasOption('U')) {
         usePassword = true;
         String a = cmd.getOptionValue('U');
         if (a != null)
            username = a;
      }
      if (cmd.hasOption('P')) {
         usePassword = true;
         String a = cmd.getOptionValue('P');
         if (a != null)
            password = a;
      }
      if (cmd.hasOption('z')) {
         String a = cmd.getOptionValue('z');
         if (a != null)
            try {
               Integer i = new Integer(a);
               int value = i.intValue();
               cacheSize = value;
            } catch (Exception ignore) {}
      }
      if (cmd.hasOption('Z')) {
         String a = cmd.getOptionValue('Z');
         if (a != null)
            try {
               Integer i = new Integer(a);
               int value = i.intValue();
               archiveSize = value;
            } catch (Exception ignore) {}
      }

//    LJM 060521
      if (cmd.hasOption ('m')) {
         if (! cmd.hasOption ('r')) {
            System.out.println ("The -m parameter is only used by this program in " +
                  "conjunction with the -r parameter");
         } else {
            String a = cmd.getOptionValue ('m');
            if (a != null) {
               try {
                  double value = Double.parseDouble (a);
                  rbCachePercent = value;
               } catch (Exception ignore) {}
            } // if not null
         } // else
      } // m
      if (cmd.hasOption ('r')) {
         // if the user specified frames, over-ride this calcultaion
         if (cmd.hasOption('z') || cmd.hasOption('Z')) {
            System.out.println ("The values specified from -z and/or -Z will be " +
                  "used to create the ring buffer, rather than the cache and " +
                  "archive values calculated from the -r parameter.");
         } else { // calculate the parameters for cache and archive
            String a = cmd.getOptionValue ('r');
            if (a != null) {
               try {
                  double value = Double.parseDouble (a);
                  rbTime = value;
                  int framesToSet = RBNBFrameUtility.getFrameCountFromTime (rbTime,  cameraFPS);
                  archiveSize = framesToSet;
                  cacheSize = (int)Math.round (rbCachePercent/100.0 * framesToSet);
               } catch (Exception ignore) {}
            } // if not null
         } // else
        } // r
      
      if (cameraHost == null) {
         System.err.println(
                            "Axis Video Camera Host is required. Use PanaSource -h for help");
         printUsage();
         return false;
      }
      
      if ((archiveSize > 0) && (archiveSize < cacheSize)){
         System.err.println(
                            "a non-zero archiveSize = " + archiveSize + " must be greater then " +
                            "or equal to cacheSize = " + cacheSize);
         return false;
      }
      
      cameraURLString =
         "http://"
         + cameraHost
         + "/nphMotionJpeg?Resolution="      
 		 + cameraRes + "&Quality=Standard";
      
      String cString =
         "Starting PanaSource with... \n"
         + " camera URL = "
         + cameraURLString
         + ";\n RBNB Server = "
         + getServer()
         + "; RBNB Source name = "
         + rbnbSourceName
         + "; RBNB Channel name = "
         + rbnbChannelName;
      cString += "\nUse PanaSource -h to see optional arguments.";
      
      return true;
   }
   
   public PanaSource() {
      // Install the custom authenticator for password protected sites
      Authenticator.setDefault(new MyAuthenticator());
      // LJM 060522
      /* Add in a hook for ctrl-c's and other abrupt death */
      Runtime.getRuntime ().addShutdownHook (new Thread () {
         public void run () {
           try {
              disconnect ();
              System.out.println ("Shutdown hook for " + PanaSource.class.getName ());
           } catch (Exception e) {
              System.out.println ("Unexpected error closing " + PanaSource.class.getName ());
           }
         } // run ()
      }); //addHook
   } // constructor
   
   public boolean connect() {
      if (connected)
         return connected;
      try {
         // Create a source and connect:
         if (archiveSize > 0)
            source = new Source(cacheSize, "append", archiveSize);
         else
            source = new Source(cacheSize, "none", 0);
         source.OpenRBNBConnection(getServer(), rbnbSourceName);
         connected = true;
         String cString =
            "Connecting PanaSource with... \n"
            + " camera URL = "
            + cameraURLString
            + ";\n RBNB Server = "
            + getServer()
            + "; RBNB Cache Size = "
            + cacheSize
            + "; RBNB Archive Size = "
            + archiveSize
            + "; RBNB Source name = "
            + rbnbSourceName
            + "; RBNB Channel name = "
            + rbnbChannelName;
         System.out.println(cString);
      } catch (SAPIException se) {
         se.printStackTrace();
      }
      return connected;
   }
   
   private void disconnect() {
      // LJM 060519 if cache or archive, then detach
      if ( (cacheSize != 0 || archiveSize != 0) && source != null ) {
         source.Detach (); // close and keep cache and archive
      } else if (source != null) { // they are both zero; close and scrap
         source.CloseRBNBConnection();
      } else {}
      source = null;
      connected = false;
   }
   
   public void startThread() {
      
      // Use this inner class to hide the public run method
      Runnable r = new Runnable() {
         public void run() {
            runWork();
         }
      };
      runit = true;
      timerThread = new Thread(r, "Timer");
      timerThread.start();
      System.out.println("PanaSource: Started thread.");
   }
   
   public void stopThread() {
      runit = false;
      timerThread.interrupt();
      System.out.println("PanaSource: Stopped thread.");
   }
   
   public void runWork() {
      
      retry = true;
      while (retry) {
         if (!connect()) 
            retry = false;
         else{
            boolean done = false;
            while (!done) {
               done = !execute();
               try {
                  Thread.sleep(RETRY_INTERVAL);
               } catch (Exception e) {}
            }
         }
         disconnect();
         if (retry)
            System.out.println("Some problem. Retrying!");
      }
      System.out.println("Done!");
      stop();
   }
   
   private boolean execute() {
      if (!connected)
         return false;
      
      ChannelMap cmap = new ChannelMap();
      int channelId = -1;
      try {
         channelId = cmap.Add(rbnbChannelName);
      } catch (SAPIException e) {
         System.err.println(
                            "Failed to add video channel to channel map; name = "
                            + rbnbChannelName);
         disconnect();
         return false;
      }
      cmap.PutTimeAuto("timeofday");
      cmap.PutMime(channelId, "image/jpeg");
      
      URL cameraURL = null;
      try {
         cameraURL = new URL(cameraURLString);
      } catch (MalformedURLException e) {
         System.err.println("URL is malformed; URL = " + cameraURLString);
         disconnect();
         return false;
      }
      
      URLConnection cameraConnection = null;
      try {
         cameraConnection = cameraURL.openConnection();
      } catch (IOException e) {
         System.err.println(
                            "Failed to connect to axis host with " + cameraURLString);
         disconnect();
         return false;
      }
      
      DataInputStream dis = null;
      try {
         dis = new DataInputStream(cameraConnection.getInputStream());
      } catch (IOException e) {
         System.err.println("Failed to get data stream from axis host.");
         //            e.printStackTrace();
         disconnect();
         return false;
      }
      
      String delimiter = "";
      String contentType = "";
      int contentLength = 0;
      byte[] imageData;
      
      StringBuffer inputLine = new StringBuffer();
      int currImage = 0;
      boolean readingData = false;
      boolean gotHeader = false;
      
      Date dateTemp = null;
      long lastTime = 0;
      long time = 0;
      long deltaTime = 0;
      long frameRate = 0;
      double averageFrameRate = cameraFPS;
      
      boolean failed = false;
      
      // pfh why limited to 1 million? BUG?
      // tew can not find any reason... changed June 15, 2005
      //		while(currImage < 1000000) {
      while (true) {
         
         if (readingData) {
            
            if (contentLength > 0) {
               imageData = new byte[contentLength];
               try {
                  dis.readFully(imageData);
               } catch (IOException e) {
                  System.err.println(
                                     "Failed to read JPEG image data from data stream.");
                  failed = true;
                  break;
               }
            } else { //this works but is could be implemented better
               int index = 0;
               imageData = new byte[1000000];
               
               while (index < imageData.length) {
                  if (index >= 4
                      && (char)imageData[index - 4] == '\r'
                      && (char)imageData[index - 3] == '\n'
                      && (char)imageData[index - 2] == '\r'
                      && (char)imageData[index - 1] == '\n') {
                     index = index - 4;
                     break;
                  }
                  
                  try {
                     imageData[index++] = dis.readByte();
                  } catch (IOException e) {
                     System.err.println(
                                        "Failed to read JPEG image data from data stream.");
                     failed = true;
                     break;
                  }
               }
               
               if (failed)
                  break;
               
               if (index == imageData.length) {
                  System.err.println("Ran out of space in data buffer.");
                  continue;
               }
               
            }
            
            if (currImage > 0) {
               dateTemp = new Date ();
               dateTemp.getTime ();
               time = dateTemp.getTime ();
               deltaTime = time - lastTime;
               lastTime = time;
               if (deltaTime != 0) {
                  frameRate = 1000 / deltaTime;
                  averageFrameRate =
                     0.995 * averageFrameRate + 0.005 * frameRate;
               } else { // deltaTime == 0
                  // do nothing for now... this *should* be an unreachable case 
               }
               
               // Debug test output for frogBug case #2580
               /*
                System.out.println ("*** LJM fps: " + frameRate +
                                    "\n*** time: " + time +
                                    "\n*** lastTime: " + lastTime +
                                    "\n*** deltaT: " + deltaTime + 
                                    "\n*** cameraFPS: " + cameraFPS + 
                                    "\n*** averageFrameRate: " + averageFrameRate
                                    ); 
                */
            }
            
            try {
               cmap.PutDataAsByteArray(channelId, imageData);
            } catch (SAPIException e) {
               System.err.println(
                                  "Failed to put image data into channel map.");
               failed = true;
               break;
            }
            
            try {
               source.Flush(cmap, true);
            } catch (SAPIException e) {
               System.err.println(
                                  "Failed to flush output data to server.");
               failed = true;
               break;
            }
            
            if (currImage % 30 == 0)
               System.out.print(
                                ((long) (averageFrameRate * 10)) / ((double)10)
                                + " fps  \r");
            
            readingData = false;
            gotHeader = false;
            
            delimiter = "";
            contentType = "";
            contentLength = 0;
            deltaTime = 0;
            
            currImage++;
            
         } else {
            char c;
            try {
               c = (char)dis.readByte();
            } catch (IOException e) {
               System.err.println("Failed to read header data.");
               failed = true;
               break;
            }
            
            if (c == '\r') {
               try {
                  dis.readByte(); // get \n also
               } catch (IOException e) {
                  System.err.println("Failed to read header data.");
                  failed = true;
                  break;
               }
               
               if (inputLine.toString().startsWith("--")) {
                  delimiter = inputLine.substring(2);
                  gotHeader = true;
               } else if (
                          inputLine.toString().toLowerCase().startsWith(
                                                                        "content-type")) {
                  contentType = inputLine.substring(14);
               } else if (
                          inputLine.toString().toLowerCase().startsWith(
                                                                        "content-length")) {
                  contentLength =
                  Integer.parseInt(inputLine.substring(16));
               } else if (
                          inputLine.toString().toLowerCase().startsWith(
                                                                        "delta-time")) {
                  deltaTime = Integer.parseInt(inputLine.substring(12));
               } else if (
                          gotHeader
                          && inputLine.toString().trim().length() == 0) {
                  readingData = true;
               } else if (inputLine.toString().trim().length() != 0) {
                  System.out.println("Received unexpected data.");
               }
               
               inputLine = new StringBuffer();
            } else {
               inputLine.append(c);
            }
         }
      }
      
      try {
         dis.close();
      } catch (IOException e) {
         System.err.println("Failed to close connect to axis host.");
      }
      
      return !failed;
      }
   
   public boolean isRunning() {
      return (connected && runit);
   }
   
   /* (non-Javadoc)
      * @see org.nees.rbnb.RBNBBase#start()
      */
   public boolean start() {
      if (isRunning())
         return false;
      if (connected)
         disconnect();
      connect();
      if (!connected)
         return false;
      startThread();
      return true;
   }
   
   /* (non-Javadoc)
      * @see org.nees.rbnb.RBNBBase#stop()
      */
   public boolean stop() {
      if (!isRunning())
         return false;
      stopThread();
      disconnect();
      return true;
   }
   
   /*
    * (non-Javadoc)
    * create an Anthenticator for URL access when a username and password is
    * required 
    */
   private class MyAuthenticator extends Authenticator {
      // This method is called when a password-protected URL is accessed
      protected PasswordAuthentication getPasswordAuthentication() {
         if ((username != null) && (password != null)) {
            return new PasswordAuthentication(
                                              username,
                                              password.toCharArray());
         } else
            return null;
      }
      
   }
   }
