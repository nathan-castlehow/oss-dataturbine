/*
 * Created on Feb 4, 2005
 */
package org.nees.rbnb;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import quicktime.QTException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import org.nees.iSight.ISightToByteStream;
import org.nees.iSight.VersionCheck;

import com.rbnb.sapi.*;

/**
 * This grabs images from an iSight Camera and pushes it to RBNB.
 * 
 * @author Terry E Weymouth
 */
public class ISightToRbnb extends RBNBBase {

    private static final String SOURCE_NAME = "iSight";
    private static final String CHANNEL_NAME = "video.jpg";

    private String sourceName = SOURCE_NAME;
    private String channelName = CHANNEL_NAME;

    private Source source;
    private ChannelMap cMap;
    private int channelIndex;

    boolean connected = false;

    private static final int DEFAULT_CACHE_SIZE = 900;
    private int cacheSize = DEFAULT_CACHE_SIZE;
    private static final int DEFAULT_ARCHIVE_SIZE = DEFAULT_CACHE_SIZE * 2;
    private int archiveSize = DEFAULT_ARCHIVE_SIZE;

    private boolean statusOk = true;
    private Vector statusMessages = new Vector();

    Thread captureThread;
    boolean runit = false;

    private double estimatedDuration = 0.0;
    private double consumedTime = 0.0;
    private double lastNotify = 0.0;
    private int frameCount = 0;

    public static void main(String[] args) {
        ISightToRbnb s = new ISightToRbnb();
        if (s.parseArgs(args) && s.exec())
            s.startThread();
    }

    protected String getCVSVersionString() {
        return "  CVS information... \n"
                + "  $Revision$\n"
                + "  $Date$\n"
                + "  $RCSfile: ISightToRbnb.java,v $ \n";
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
        opt.addOption("z", true, "cache size *" + DEFAULT_CACHE_SIZE);
        opt.addOption("Z", true, "archive size *" + DEFAULT_ARCHIVE_SIZE);
        setNotes("Read jpg images from an iSight camera... probably only works on"
                + " a Mac, and has only been tested with OS X 10.4 and java 1.5");
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
        if (cmd.hasOption('z')) {
            String a = cmd.getOptionValue('z');
            if (a != null)
                try {
                    Integer i = new Integer(a);
                    int value = i.intValue();
                    cacheSize = value;
                } catch (Exception ignore) {
                }
        }
        if (cmd.hasOption('Z')) {
            String a = cmd.getOptionValue('Z');
            if (a != null)
                try {
                    Integer i = new Integer(a);
                    int value = i.intValue();
                    archiveSize = value;
                } catch (Exception ignore) {
                }
        }

        return finishSetup();
    }

    // to use programaticially (see main)
    // if (setup(args) && exec())
    // startThread();
    public boolean setup(String server, String port, String arg_sourceName,
            String arg_channelName, int arg_cacheSize, int arg_archiveSize) {
        
        // Java 1.5; QuickTime 7; QuickTime for Java 6; Mox OS X
        VersionCheck.warn(1,5,7,6,"Mac OS X");
        
        setServerName(server);
        setServerPort(port);
        sourceName = arg_sourceName;
        channelName = arg_channelName;
        cacheSize = arg_cacheSize;
        archiveSize = arg_archiveSize;

        return finishSetup();
    }

    public boolean finishSetup() {

        if ((archiveSize > 0) && (archiveSize < cacheSize)) {
            System.err.println("a non-zero archiveSize = " + archiveSize
                    + " must be greater then " + "or equal to cacheSize = "
                    + cacheSize);
            return false;
        }

        System.out.println("Starting ISightToRbnb on " + getServer() + " as "
                + sourceName);
        System.out.println("  Channel name = " + channelName
                + "  Cache Size = " + cacheSize + "; Archive size = "
                + archiveSize);
        System.out.println("  Use ISightToRbnb -h to see optional parameters");
        System.out.println("");

        return true;
    }

    public boolean exec() {
        try {
            // Create a source and connect:
            if (archiveSize > 0)
                source = new Source(cacheSize, "create", archiveSize);
            else
                source = new Source(cacheSize, "none", 0);
            source.OpenRBNBConnection(getServer(), sourceName);
            cMap = new ChannelMap();
            channelIndex = cMap.Add(channelName);
            cMap.PutMime(channelIndex, "image/jpeg");
            cMap.PutTimeAuto("TimeOfDay");
            source.Register(cMap);
            connected = true;
            System.out.println("ISightToRbnb: Connection made to server = "
                    + getServer() + " as " + sourceName + "/" + channelName);
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
        captureThread = new Thread(r, "Data Thread");
        captureThread.start();
        System.out.println("ISightToRbnb: Started thread.");
    }

    public void stopThread() {
        runit = false;
        if (captureThread != null)
            captureThread.interrupt();
        System.out.println("ISightToRbnb: Stopped thread.");
    }

    private void runWork() {
        statusMessages = new Vector();
        frameCount = 0;
        consumedTime = 0.0;
        notifyAllListeners();
        long start = System.currentTimeMillis();
        long old = start;
        
        try {
            ISightToByteStream camera = new ISightToByteStream(640,480);
            while (isRunning()) {
                byte[] buffer = camera.getNextByteArray();
                cMap.PutDataAsByteArray(channelIndex, buffer);
                source.Flush(cMap);

                 if (lastNotify + 10.0 < consumedTime) {
                    notifyAllListeners();
                    lastNotify = consumedTime;
                }
            } // end while
            statusOk = true;
            addToStatus("Normal Completion");
        } catch (SAPIException se) {
            se.printStackTrace();
            addToStatus("SAPIException");
            statusOk = false;
        } catch (QTException e) {
            e.printStackTrace();
            addToStatus("QTException");
            statusOk = false;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            addToStatus("IllegalArgumentException");
            statusOk = false;
        } catch (IOException e) {
            e.printStackTrace();
            addToStatus("IOException");
            statusOk = false;
        }
        stopThread();
        disconnect();
        captureThread = null;
        estimatedDuration = consumedTime;
        notifyAllListeners();
        addToStatus("Done.");
    }

    private void disconnect() {
        source.CloseRBNBConnection();
        connected = false;
        source = null;
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
     * @return the running status of the current capture thread, or the
     *         completion status of the last thread run
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
