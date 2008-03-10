/*
 * CaptureTest.java
 * Created on Jun 24, 2005
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
 *   $RCSfile: CaptureTest.java,v $ 
 * 
 */

import java.text.NumberFormat;

import org.nees.rbnb.JpgSaverSink;
import org.nees.rbnb.TimeProgressListener;

/**
 * This class illustrates how the callable version of JpgSaverSink might
 * be called. It also serves as a referance implementation, a regression test,
 * and an aid for debugging and testing further developments.
 * 
 * @author Terry E. Weymouth
 */
public class CaptureTest {

    static final long TEN_SECONDS = 1000 * 10;
    
    static final String CVS_INFO = "CVS Information \n" +
        "  $Revision: 153 $ \n" +
        "  $Date: 2007-09-24 13:10:37 -0700 (Mon, 24 Sep 2007) $ \n" +
        "  $RCSfile: CaptureTest.java,v $"; 

    // the capture class    
    JpgSaverSink captureControl = null;
    boolean captureDone = false;

    public static void main(String[] args)
    {
        System.out.println("Running Capture test from: " + CVS_INFO);
        
        CaptureTest ct = new CaptureTest();
        ct.startCature();
        while (!ct.done())
        {
            // you could do somthing else while 
            // the capture is happening and check
            // from time to time for it to be done
            // see also the method
            //     reportCaptureProgress
            // below
            try { Thread.sleep(TEN_SECONDS);}
            catch (InterruptedException ignore) {}
            
        }
        ct.reportResults();
        ct.cleanUp();
    }

    // this method sets up the args and starts the capture which
    // runs on it's own thread
    private void startCature()
    {
        
        // the RBNB host and port, e.g.
        String captureHost = "neestpm.sdsc.edu";
        String capturePort = "3333";

        // the capture Sink name - this is arbitraty, e.g.
        String captureSinkName = "_Capture";

        // the RBNB "path" to the channel that you would like to
        // get the image stream from, e.g.
        String captureSourcePath = "M1 cam2/video.jpg";

        // the path to the base directory where the directory
        // structure containing the images will be placed, e.g.
        String base = "ImageArchive/ImageStream_M1_cam2";

        // The RBNB style time for the start and end of the capture,
        // with the NEES convention of the number of seconds
        // from Jan 1, 1970. A value of 0.0 means "now". e.g.
        long unixTime = System.currentTimeMillis(); // current time...
        double captureStartTime = ((double)unixTime) / 1000.0; // in seconds...
        double captureEndTime = captureStartTime + 40.0; // 40 seconds later

        try {
            // prepare to start capture thread
            captureDone = false;
            captureControl = new JpgSaverSink();
            captureControl.setup(
                captureHost,
                capturePort,
                captureSinkName,
                captureSourcePath,
                base,
                captureStartTime,
                captureEndTime);
                
            // the progress listener reports progress on the capture
            // see also the thred in this class     
            captureControl.addTimeProgressListener(new TimeProgressListener() {
                public void progressUpdate
                    (double estimatedDuration, double consumedTime)
                {
                    reportCaptureProgress(consumedTime, estimatedDuration);
                }
            });
            
            // ... and here we go!!
            captureControl.startThread();

        } catch (Throwable e) {
            e.printStackTrace();
            System.exit(-1);
        }
    } // captureSetup()

    // used to report progress on the capture; this method gets called
    // from the capture thread; while the capture is in progress
    // it should not take too much compute time
    private void reportCaptureProgress
        (double consumedTime, double estimatedDuration)
    {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(4);
        nf.setMinimumFractionDigits(1);

        System.out.println("Capture Progress:" +            " estimated duration = " + nf.format(estimatedDuration) +
            " consumed time  = " + nf.format(consumedTime));
        
        // check to see if the capture is reporting "finished"
        if (consumedTime == estimatedDuration) // the capture is done!
        {
            System.out.println("Capture done!");
            captureDone = true;
            // you could now use this thread to do something else...
            // but, it should be allowed to return and quit eventually
        }
        
        // also, one can force the capture to stop at any time...
        // if, for example, it is stopped from a GUI by the user
        // with this code:
        //
        // if (captureContol != null)
        //    captureControl.stopThread();
    }

    // this method checks to see if the captures progress is done
    // see the method reportCaptureProgress, above
    // this method is, very likely overkill, it just illustrates
    // all the ways that one can interact with the progress of the
    // capture thread
    private boolean done() {

        return captureDone
            || !captureControl.isRunning()
            || !captureControl.isStatusOk();
            
        // captureControl.isRunning() and captureControl.isStatusOk() can
        // be called at any time to get the "state" of the capture thread
        // one can also call captureControl.getStatusMessages() at any time
        // to get the status message history (see reportResults(), below)
    }

    // report the status on completion of the thread
    private void reportResults() {
        System.out.println("");
        String[] s = captureControl.getStatusMessages();
        if (s.length == 0)
        {
            if (captureControl.isStatusOk())
                System.out.println("Capture done with nothing to report.");
            else
                System.out.println("Capture finished with an error but " +
                    "nothing to report");
        }
        else
        {
            if (captureControl.isStatusOk())
                System.out.println("Capture report:");
            else
                System.out.println("Capture error report:");
            for (int i = 0; i < s.length; i++) {
                System.out.println("  " + s[i]);
            }
        }
    }

    // clean up the capture thread...
    private void cleanUp() {
        if (captureControl == null) return; // lost the thread??

        captureControl.stopThread();
        int retries = 0;
        // wait for the thread to stop; but, not too long
        while (captureControl.isStillRunning() && (retries++ < 20)) {
            try {
                Thread.sleep(100);
            } catch (Exception ignore) {}
        }
        if (captureControl.isStillRunning()) // the thread is hung?
        {
            System.out.println("Capture Demo: Some unexpected " +                "problems with the capture thread?  " +
                "It is likely that the thread is hung.");
        }
        captureControl = null;
    }

    // also, if the capture was ablorted, but sure to delete the
    // direcotry tree of the captured files, i.e.
    //        System.out.println("Deleting temporary dir tree at "
    //            + root.getAbsolutePath());
    //        boolean worked = recursivelyDelete(root);
    //        if (worked)
    //            System.out.println("Delted: " + root.getAbsolutePath());
    //        else
    //            System.out.println(
    //                "Faild to delete: " + root.getAbsolutePath());
    //
    // where recursivelyDelete is defined, for example, as...
    //
    //        private boolean recursivelyDelete(File root) {
    //            boolean didIt = true;
    //            File[] fl = root.listFiles();
    //            if (fl == null) // it is a file not a directory
    //                return root.delete();
    //            else
    //                for (int i = 0; i < fl.length; i++) {
    //                    didIt = didIt && recursivelyDelete(fl[i]);
    //                }
    //            return didIt && root.delete();
    //        }
}
