package org.nees.rbnb;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;

/**
 * A base class for all MJPEG video sources.
 * 
 * @author Jason P. Hanley
 */
public abstract class MJPEGSource extends RBNBSource {
  private final static String DEFAULT_RBNB_CHANNEL = "video.jpg";
  private String rbnbChannelName = DEFAULT_RBNB_CHANNEL;
  
  private final static long RETRY_INTERVAL = 5000;
  private final static int HTTP_TIMEOUT = 10000;
  
  protected String hostName;
  
  /**
   * A variable to set what percentage of the archived frames are to be
   * cached by the rbnb server.
   */
  private static final double DEFAULT_CACHE_PERCENT = 10;
  private double rbnbCachePercent = DEFAULT_CACHE_PERCENT;
  
  private Thread timerThread;
  
  private boolean runit = false;
  
  protected Options setBaseOptions(Options opt) {
    super.setBaseOptions(opt);
    
    opt.addOption("C", true, "RBNB source channel name *" + DEFAULT_RBNB_CHANNEL);
    opt.addOption("A", true, "Video camera host name (required)");
    
    opt.addOption ("r", true, "length (in hours) to create the ring buffer for this source");
    opt.addOption ("m", true, "percentage (%) of the ring buffer specified in -r to cache in memory *" + DEFAULT_CACHE_PERCENT);
    
    return opt;
  }
  
  protected boolean setBaseArgs(CommandLine cmd) {
    if (!super.setBaseArgs(cmd)) {
      return false;
    }
    
    if (cmd.hasOption('C')) {
      String a = cmd.getOptionValue('C');
      if (a != null) {
        rbnbChannelName = a;
      }
    }
    
    if (cmd.hasOption ('m')) {
      if (! cmd.hasOption ('r')) {
         System.out.println ("The -m parameter is only used by this program in " +
               "conjunction with the -r parameter");
      } else {
         String a = cmd.getOptionValue ('m');
         if (a != null) {
            try {
                rbnbCachePercent = Double.parseDouble (a);
            } catch (NumberFormatException nf) {
                System.out.println("Please ensure to enter a numeric value for -m option. " + a + " is not valid!");
                return false;   
            }
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
		           double rbnbTime = Double.parseDouble (a);

		           archiveSize = RBNBFrameUtility.getFrameCountFromTime (rbnbTime, getFPS());

		           cacheSize = (int)Math.round (rbnbCachePercent/100.0 * archiveSize);

		        } catch (NumberFormatException nfe) {
		            System.out.println("Please ensure to enter a numeric value for -r option. " + a + " is not valid!");
		            return false;   
		        }
		     }
		  }
		}
		  
		return true;
	}
  
  /**
   * Get the name of the RBNB channel for this MJPEG video.
   * 
   * @return  the name of the channel
   */
  public String getRBNBChannelName() {
    return rbnbChannelName;
  }
  
  /**
   * Get the host name of the MJPEG video server.
   * 
   * @return  the host name of the MJPEG video server
   */
  public String getHostName() {
    return hostName;
  }
  
  /**
   * Get the frames per second of the MJPEG video.
   * 
   * @return  the frames per second of video
   */
  protected abstract int getFPS();
  
  /**
   * Get the URL to the MJPEG video feed.
   * 
   * @return  the URL to the MJPEG video feed
   */
  protected abstract URL getMJPEGURL();
  
  /**
   * 
   *
   */
  protected URLConnection getCameraConnection() {
  	
    URL cameraURL = getMJPEGURL();
    if (cameraURL == null) {
      System.err.println("Camera URL is null.");
      disconnect();
      return null;
    }
    
    URLConnection cameraConnection;
    try {
        cameraConnection = cameraURL.openConnection();
        cameraConnection.setReadTimeout(HTTP_TIMEOUT);
        cameraConnection.connect();
    } catch (IOException e) {
        System.err.println(
            "Failed to connect to video host with " + cameraURL);
        disconnect();
        return null;
    }
  	
    return cameraConnection;
  }
  
  
  private void startThread() {
    Runnable r = new Runnable() {
      public void run() {
        runWork();
      }
    };
    
    runit = true;
    timerThread = new Thread(r, "Timer");
    timerThread.start();
    
    System.out.println("Started thread.");
  }

  private void stopThread() {
    runit = false;
    timerThread.interrupt();
    
    System.out.println("Stopped thread.");
  }

  private void runWork() {
    boolean retry = true;
    while (retry) {
      if (connect()) {
        retry = !execute();
      }
      
      disconnect();
      
      if (retry) {
        try {
          Thread.sleep(RETRY_INTERVAL);
        } catch (Exception e) {}
        
        System.out.println("Some problem. Retrying!");
      }
    }
    
    System.out.println("Done!");
    
    stop();
  }

  private boolean execute() {
    if (!isConnected())
        return false;

    ChannelMap cmap = new ChannelMap();
    int channelId;
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

    URLConnection cameraConnection = getCameraConnection();
/*    
    URL cameraURL = getMJPEGURL();
    if (cameraURL == null) {
        System.err.println("Camera URL is null.");
        disconnect();
        return false;
    }

    URLConnection cameraConnection;
    try {
        cameraConnection = cameraURL.openConnection();
        cameraConnection.setReadTimeout(HTTP_TIMEOUT);
        cameraConnection.connect();
    } catch (IOException e) {
        System.err.println(
            "Failed to connect to video host with " + cameraURL);
        disconnect();
        return false;
    }
*/
    String contentType = cameraConnection.getHeaderField("Content-Type");
    if (contentType == null) {
        System.err.println("Failed to find content type in stream.");
        disconnect();
        return false;
    }
    

    System.out.println("contentType :" + contentType);
    String[] fields = contentType.split(";");
    String delimiter = new String();
    for (int i=0; i<fields.length; i++) {
    	fields[i] = fields[i].trim();
      if (fields[i].toLowerCase().startsWith("boundary=")) {
        delimiter = fields[i].substring(9);
        break;
      }
    }

    if (delimiter.length() == 0) {
        System.err.println("Failed to find delimiter.");
        disconnect();
        return false;
    }

    DataInputStream dis = null;
    try {
        dis = new DataInputStream(cameraConnection.getInputStream());
    } catch (IOException e) {
        System.err.println("Failed to get data stream from D-Link host.");
        disconnect();
        return false;
    }

    int contentLength = 0;
    byte[] imageData;

    StringBuffer inputLine = new StringBuffer();
    boolean readingData = false;
    boolean gotHeader = false;

    boolean failed = false;

    long previousTimeStamp = -1;
    double averageFPS = -1;
    long images = 0;

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

                try {
                    cmap.PutDataAsByteArray(channelId, imageData);
                } catch (SAPIException e) {
                    System.err.println(
                        "Failed to put image data into channel map.");
                    failed = true;
                    break;
                }

                try {
                    getSource().Flush(cmap, true);
                } catch (SAPIException e) {
                    System.err.println(
                        "Failed to flush output data to server.");
                    failed = true;
                    break;
                }

                images++;

                long timeStamp = System.currentTimeMillis();
                if (previousTimeStamp != -1) {
                  double fps = 1000d/(timeStamp-previousTimeStamp);
                  if (averageFPS == -1) {
                    averageFPS = fps;
                  } else {
                    averageFPS = 0.95*averageFPS + 0.05*fps;
                  }

                  long roundedAverageFPS = Math.round(averageFPS);
                  if (images % roundedAverageFPS == 0) {
                    System.out.print(roundedAverageFPS + " fps   \r");
                  }
                }
                previousTimeStamp = timeStamp;

            }

            readingData = false;
            gotHeader = false;

            contentLength = 0;

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


                String line = inputLine.toString().trim();
                if (line.equals(delimiter)) {
                    gotHeader = true;
                } else if (
                    line.toLowerCase().startsWith(
                        "content-length")) {
                    contentLength =
                        Integer.parseInt(inputLine.substring(16));
                } else if (
                    gotHeader
                        && line.length() == 0) {
                    readingData = true;
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
        System.err.println("Failed to close connect to D-Link host.");
    }

    return !failed;
  }

  public boolean isRunning() {
    return (isConnected() && runit);
  }

  public boolean start() {
    if (isRunning()) {
      return false;
    }
    
    if (isConnected()) {
      disconnect();
    }
    
    connect();
    
    if (!isConnected()) {
      return false;
    }
    
    startThread();
    
    return true;
  }

  public boolean stop() {
    if (!isRunning()) {
      return false;
    }
    
    stopThread();
    
    disconnect();
    
    return true;
  }
}