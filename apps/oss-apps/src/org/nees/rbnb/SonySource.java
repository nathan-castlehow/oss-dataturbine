/*
 * Created on July 26, 2005
 */
package org.nees.rbnb;

import java.io.DataInputStream;
import java.io.IOException;

import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.Source;
import com.rbnb.sapi.SAPIException;

import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLine;

/**
 * SonySource
 * @author Jason P. Hanley
 * @author Terry E. Weymouth
 * 
 * adopted from org.nees.buffalo.video.AxisSource by Jason T. Hanley
 * 
 * SonySource - takes an image stream from an Sony RZ30N camera and put the
 * JPEG images into RBNB with timestamps. 
 * @see org.nees.buffalo.axis.AxisSource
 */
public class SonySource extends RBNBBase {

    private final static String DEFAULT_RBNB_SOURCE = "SonyVideo";
    private final static String DEFAULT_RBNB_CHANNEL = "video.jpg";
    private final static String CAMERA_HOST = "undefined";
    private final static int DEFAULT_CAMERA_FPS = 1;

    private final static long RETRY_INTERVAL = 1000;

    private String cameraURLString;
    private String cameraHost = CAMERA_HOST;
    private String rbnbSourceName = DEFAULT_RBNB_SOURCE;
    private String rbnbChannelName = DEFAULT_RBNB_CHANNEL;
    private int cameraFPS = DEFAULT_CAMERA_FPS;

    private static final int DEFAULT_CACHE_SIZE = 900;
    private int cacheSize = DEFAULT_CACHE_SIZE;
    private static final int DEFAULT_ARCHIVE_SIZE = 0;
    private int archiveSize = DEFAULT_ARCHIVE_SIZE;

    Source source = null;
    ChannelMap sMap;
    int index;
    boolean connected = false;

    Thread timerThread;
    boolean runit = false;
    boolean retry = true;

    public static void main(String[] args) {
        // start from command line
        SonySource a = new SonySource();
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
        return
            "  CVS information... \n" +
            "  $Revision: 153 $\n" +
            "  $Date: 2007-09-24 13:10:37 -0700 (Mon, 24 Sep 2007) $\n" +
            "  $RCSfile: SonySource.java,v $ \n";
    }

    /* (non-Javadoc)
     * @see org.nees.rbnb.RBNBBase#setOptions()
     */
    protected Options setOptions() {
        Options opt = setBaseOptions(new Options()); // uses h, s, p
        opt.addOption("A", true, "Sony RZ30N camera host (required)");
        opt.addOption("S", true, "RBNB Source Name *" + DEFAULT_RBNB_SOURCE);
        opt.addOption(
            "C",
            true,
            "RBNB Source Channel Name *" + DEFAULT_RBNB_CHANNEL);
        opt.addOption("f", true, "frame rate, Hz, *" + DEFAULT_CAMERA_FPS);
        opt.addOption("z", true, "cache size *" + DEFAULT_CACHE_SIZE);
        opt.addOption("Z", true, "archive size *" + DEFAULT_ARCHIVE_SIZE);
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

        if (cameraHost == null) {
            System.err.println(
                "Sony (RZ30N) Video Camera Host is required. Use SonySource -h for help");
            printUsage();
            return false;
        }
        
        if ((archiveSize > 0) && (archiveSize < cacheSize)){
            System.err.println(
                "a non-zero archiveSize = " + archiveSize + " must be greater then " +                    "or equal to cacheSize = " + cacheSize);
            return false;
        }

        cameraURLString =
            "http://"
                + cameraHost
                + "/image?";

        cameraURLString += "speed=" + cameraFPS;

        String cString =
            "Starting SonySource with... \n"
                + " camera URL = "
                + cameraURLString
                + ";\n RBNB Server = "
                + getServer()
                + "; RBNB Source name = "
                + rbnbSourceName
                + "; RBNB Channel name = "
                + rbnbChannelName;
        cString += "\nUse SonySource -h to see optional arguments.";

        return true;
    }

    public boolean connect() {
        if (connected)
            return connected;
        try {
            // Create a source and connect:
            if (archiveSize > 0)
                source = new Source(cacheSize, "create", archiveSize);
            else
                source = new Source(cacheSize, "none", 0);
            source.OpenRBNBConnection(getServer(), rbnbSourceName);
            connected = true;
            String cString =
                "Connecting SonySource with... \n"
                    + " camera URL = "
                    + cameraURLString
                    + ";\n RBNB Server = "
                    + getServer()
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
        source.CloseRBNBConnection();
        connected = false;
        source = null;
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
        System.out.println("SonySource: Started thread.");
    }

    public void stopThread() {
        runit = false;
        timerThread.interrupt();
        System.out.println("SonySource: Stopped thread.");
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
                "Failed to connect to axis Sony with " + cameraURLString);
            disconnect();
            return false;
        }

        DataInputStream dis = null;
        try {
            dis = new DataInputStream(cameraConnection.getInputStream());
        } catch (IOException e) {
            System.err.println("Failed to get data stream from Sony host.");
            disconnect();
            return false;
        }

        String delimiter = "";
        String contentType = "";
        int contentLength = 0;
        int deltaTime = 0;
        byte[] imageData;

        StringBuffer inputLine = new StringBuffer();
        int currImage = 0;
        boolean readingData = false;
        boolean gotHeader = false;

        double lastTime = 0;
        double time = 0;
        double frameRate = 0;
        double averageFrameRate = cameraFPS;

        boolean failed = false;

        while (true) {

            if (readingData) {
                
                if (true) System.exit(0);

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
                    imageData = new byte[100000];

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
                    frameRate = 1000 / (double)deltaTime;
                    averageFrameRate =
                        0.995 * averageFrameRate + 0.005 * frameRate;
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
                        System.err.println("Unexpected end of line on header data.");
                        failed = true;
                        break;
                    }

                    System.out.println("-->" + inputLine);
                    
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
                        System.out.println(inputLine);
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
}
