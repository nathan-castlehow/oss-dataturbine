/*
 * Publish.java
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
 *   $Revision$
 *   $Date$
 *   $RCSfile: PublishTest.java,v $ 
 * 
 */

import java.io.File;
import java.text.NumberFormat;
import java.text.ParseException;

import org.nees.rbnb.JpgLoaderSource;
import org.nees.rbnb.TimeProgressListener;
import org.nees.rbnb.ArchiveUtility;

/**
 * This class illustrates how the callable version of JpgLoaderSource might
 * be called. It also serves as a referance implementation, a regression test,
 * and an aid for debugging and testing further developments.
 * 
 * @author Terry E. Weymouth
 */
public class PublishTest {

    static final long TEN_SECONDS = 1000 * 10;
    
    static final String CVS_INFO = "CVS Information \n" +
        "  $Revision$ \n" +
        "  $Date$ \n" +
        "  $RCSfile: PublishTest.java,v $"; 

    // the publish class    
    JpgLoaderSource publishControl = null;
    
    // this flag used to check for normal compleation
    boolean publishDone = false;    

    public static void main(String[] args)
    {
        System.out.println("Running Publish test -- " + CVS_INFO);
        
        PublishTest ct = new PublishTest();
        ct.startPublish();
        while (!ct.done())
        {
            // you could do somthing else while 
            // the publish is happening and check
            // from time to time for it to be done
            // see also the method
            //     reportPublishProgress
            // below
            try { Thread.sleep(TEN_SECONDS);}
            catch (InterruptedException ignore) {}
            
        }
        ct.reportResults();
        ct.cleanUp();
    }

    // this method sets up the args and starts the publication which
    // runs on it's own thread
    private void startPublish()
    {
       
        publishDone = false;
        
        // the RBNB host and port, e.g.
        String publishHost = "neestpm.sdsc.edu";
        String publishPort = "3333";

        // the publish Source name and Channel name - this is arbitraty, e.g.
        String publishSourceName = "_Publish";
        String publishChannelName = "video.jpg";

        // the path to the base directory where the directory
        // structure containing the images will be placed, e.g.
        String base = "ImageArchive/ImageStream_M1_cam2";

        // a start time and end time from the range of the archive,
        // here we use the ArchiveUtility to get the first and last
        // time in the archive; times in seconds since Jan 1, 1970
        File baseDir = new File(base);
System.out.println("Base dir = " + baseDir.getAbsolutePath());
        long startTime = 0;
        long endTime = 0;
        try {
            startTime =
                ArchiveUtility.makeTimeFromFilename(
                    ArchiveUtility.recursivlyFindLeast(baseDir));
            endTime = ArchiveUtility.makeTimeFromFilename(
                ArchiveUtility.recursivlyFindGreatest(baseDir));
        } catch (ParseException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        // a playback pause time to simulate the original image
        // stream image interval... milliseconds
        // long playbackPauseTime = 100; // 1/10 of a second.
        long playbackPauseTime = 0; // play as fast as possible
        
        // RBNB cache and archive
        int cacheSize = 900;
        int archiveSize = 1800;
        
        // The RBNB style time for the start and end of the publish,
        // with the NEES convention of the number of seconds
        // from Jan 1, 1970. A value of 0.0 means "now". e.g.

        // prepare to start publish thread
        publishControl = new JpgLoaderSource();
        publishControl.setup(
            publishHost,
            publishPort,
            publishSourceName,
            publishChannelName,
            startTime,
            endTime,
            playbackPauseTime,
            base,
            cacheSize,
            archiveSize);
            
        // the progress listener reports progress on the publish
        // see also the thred in this class     
        publishControl.addTimeProgressListener(new TimeProgressListener() {
            public void progressUpdate
                (double estimatedDuration, double consumedTime)
            {
                reportPublishProgress(consumedTime, estimatedDuration);
            }
        });
        
        if (!publishControl.exec())
            System.exit(-1);
            
        // ... and here we go!!
        publishControl.startThread();

    } // publishSetup()

    // used to report progress on the publish; this method gets called
    // from the publish thread; while the publish is in progress
    // it should not take too much compute time
    private void reportPublishProgress
        (double consumedTime, double estimatedDuration)
    {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(4);
        nf.setMinimumFractionDigits(1);

        System.out.println("Publish Progress:" +            " estimated duration = " + nf.format(estimatedDuration) +
            " consumed time  = " + nf.format(consumedTime));
        
        // check to see if the publish is reporting "finished"
        if (consumedTime == estimatedDuration) // the publish is done!
        {
            System.out.println("Publish done!");
            // you could now use this thread to do something else...
            // but, it should be allowed to return and quit eventually
        }
        
        // also, one can force the publish to stop at any time...
        // if, for example, it is stopped from a GUI by the user
        // with this code:
        //
        // if (publishContol != null)
        //    publishControl.stopThread();
    }

    // this method checks to see if the publishs progress is done
    // see the method reportPublishProgress, above
    // this method is, very likely overkill, it just illustrates
    // all the ways that one can interact with the progress of the
    // publish thread
    private boolean done() {

        return publishDone
            || !publishControl.isRunning()
            || !publishControl.isStatusOk();
            
        // publishControl.isRunning() and publishControl.isStatusOk() can
        // be called at any time to get the "state" of the publish thread
        // one can also call publishControl.getStatusMessages() at any time
        // to get the status message history (see reportResults(), below)
    }

    // report the status on completion of the thread
    private void reportResults() {
        System.out.println("");
        String[] s = publishControl.getStatusMessages();
        if (s.length == 0)
        {
            if (publishControl.isStatusOk())
                System.out.println("Publish done with nothing to report.");
            else
                System.out.println("Publish finished with an error but " +
                    "nothing to report");
        }
        else
        {
            if (publishControl.isStatusOk())
                System.out.println("Publish report:");
            else
                System.out.println("Publish error report:");
            for (int i = 0; i < s.length; i++) {
                System.out.println("  " + s[i]);
            }
        }
    }

    // clean up the publish thread...
    private void cleanUp() {
        if (publishControl == null) return; // lost the thread??

        publishControl.stopThread();
        int retries = 0;
        // wait for the thread to stop; but, not too long
        while (publishControl.isStillRunning() && (retries++ < 20)) {
            try {
                Thread.sleep(100);
            } catch (Exception ignore) {}
        }
        if (publishControl.isStillRunning()) // the thread is hung?
        {
            System.out.println("Publish Demo: Some unexpected " +                "problems with the publish thread?  " +
                "It is likely that the thread is hung.");
        }
        publishControl = null;
    }

    // also, if the publish was ablorted, but sure to delete the
    // direcotry tree of the published files, i.e.
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
