/*
 * ArchiveToMovie.java
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
 *   $RCSfile: ArchiveToMovie.java,v $ 
 * 
 */
package org.nees.tivo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import java.util.Vector;
import java.util.Enumeration;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

import javax.media.Controller;
import javax.media.ControllerListener;
import javax.media.MediaLocator;
import javax.media.Format;
import javax.media.NoProcessorException;
import javax.media.Processor;
import javax.media.Manager;
import javax.media.DataSink;
import javax.media.Buffer;
import javax.media.Time;
import javax.media.EndOfMediaEvent;
import javax.media.ControllerEvent;
import javax.media.ConfigureCompleteEvent;
import javax.media.ResourceUnavailableEvent;
import javax.media.RealizeCompleteEvent;
import javax.media.PrefetchCompleteEvent;

import javax.media.control.TrackControl;

import javax.media.protocol.PullBufferDataSource;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PullBufferStream;
import javax.media.protocol.DataSource;
import javax.media.protocol.FileTypeDescriptor;

import javax.media.datasink.DataSinkListener;
import javax.media.datasink.DataSinkEvent;
import javax.media.datasink.EndOfStreamEvent;
import javax.media.datasink.DataSinkErrorEvent;
import javax.media.format.VideoFormat;

import com.sun.image.codec.jpeg.JPEGImageDecoder;
import com.sun.image.codec.jpeg.JPEGCodec;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import org.nees.rbnb.ArchiveUtility;
import org.nees.rbnb.TimeProgressListener;

/**
 * This class grabs images from an Archive Segment and saves them to a movie
 * file. The code is informed by the example VideoSampleBuilder in QuickTime for
 * Java by Chris Adamson, Copyright 2005, ISBN 0-596-00822-8, and used by
 * permission as per the section "Using Code Examples" in the Preface of that
 * that book.
 * 
 * @author Terry E. Weymouth
 */
public class ArchiveToMovie implements ControllerListener, DataSinkListener {

    private static final String MOVIE_FILE = "archive.mov";
    private static final String ARCHIVE_BASE = Archive.DEFAULT_ARCHIVE_NAME;

    private double startTime = 0.0;
    private double endTime = 0.0;
    private double duration = 0.0;
    private boolean useDuration = false;

    private String startTimeString;
    private String endTimeString;

    private String movieFile = MOVIE_FILE;
    private String archiveBase = ARCHIVE_BASE;
    private ArchiveInterface archive;

    private String segmentName = null;
    private ArchiveSegmentInterface segment;
    private ArchiveImageInterface[] imageArray;

    private double estimatedDuration;
    private double consumedTime;
    private long clockStartTime;

    private float movieFrameRate;
    private float speedupFactor = 1.0f;
    private int movieFrameHeight, movieFrameWidth;

    private String optionNotes = null;

    private boolean statusOk = true;
    private Vector statusMessages = new Vector();

    public static void main(String[] args) {
        ArchiveToMovie s = new ArchiveToMovie();
        s.reportStatus(s.getCVSVersionString());
        if (s.parseArgs(args) && s.setup())
            s.exec();
    }

    private String getCVSVersionString() {
        return "  CVS information... \n" + "  $Revision: 153 $\n"
                + "  $Date: 2007-09-24 13:10:37 -0700 (Mon, 24 Sep 2007) $\n"
                + "  $RCSfile: ArchiveToMovie.java,v $ \n";
    }

    protected boolean parseArgs(String[] args) throws IllegalArgumentException {
        try {
            CommandLine cmd = (new PosixParser()).parse(setOptions(), args);
            return setArgs(cmd);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Argument Exception: " + e);
        }
    }

    private Options setOptions() {
        Options opt = new Options();
        opt.addOption("f", true, "Movie file path *" + MOVIE_FILE);
        opt.addOption("a", true, "Archive base dir *" + ARCHIVE_BASE);
        opt.addOption("n", true, "Segment Name (required)");
        opt.addOption("F", true, "Speedup factor");
        opt.addOption("S", true, "Start time (defauts to start of ringbuffer)");
        opt.addOption("E", true,
                "End time (either Duration or End Time required)");
        opt.addOption("D", true,
                "Duration (either Duration or End Time required)");
        return opt;
    }

    protected boolean setArgs(CommandLine cmd) {

        useDuration = false;

        if (cmd.hasOption('f')) {
            String a = cmd.getOptionValue('F');
            if (a != null)
                movieFile = a;
        }
        if (cmd.hasOption('a')) {
            String a = cmd.getOptionValue('a');
            if (a != null)
                archiveBase = a;
        }
        if (cmd.hasOption('n')) {
            String a = cmd.getOptionValue('n');
            if (a != null)
                segmentName = a;
        }
        if (cmd.hasOption('S')) {
            String a = cmd.getOptionValue('S');
            if (a != null) {
                try {
                    startTimeString = a;
                    Date d = ArchiveUtility.getCommandFormat().parse(a);
                    long t = d.getTime();
                    startTime = ((double) t) / 1000.0;
                } catch (Exception e) {
                    System.err.println("Parse of start time failed " + a);
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
                    long t = d.getTime();
                    endTime = ((double) t) / 1000.0;
                } catch (Exception e) {
                    System.err.println("Parse of end time failed " + a);
                    printUsage();
                    return false;
                }
            }
        }
        if (cmd.hasOption('D')) {
            String a = cmd.getOptionValue('D');
            if (a != null) {
                try {
                    duration = Double.parseDouble(a);
                    useDuration = true;
                } catch (Exception e) {
                    System.err.println("Parse of Duration failed " + a);
                    printUsage();
                    return false;
                }
            }
        }
        if (cmd.hasOption('F')) {
            String a = cmd.getOptionValue('F');
            if (a != null) {
                try {
                    speedupFactor = Float.parseFloat(a);
                } catch (Exception e) {
                    System.err.println("Parse of Speedup Factor failed " + a);
                    printUsage();
                    return false;
                }
            }
        }

        if ((endTime != 0.0) && (duration != 0.0)) {
            System.err.println("  Warning: Both endTime and duration are set.");
            if (useDuration)
                System.err.println("  Useing duration");
            else
                System.err.println("  Useing endTime");
        }

        if (segmentName == null) {
            System.err.println("  Segment name is required.");
            return false;
        }

        try {
            archive = new Archive(archiveBase);
        } catch (ArchiveException e) {
            e.printStackTrace();
            System.err.println("Open of Archive failed.");
            return false;
        }

        return true;
    }

    protected void printUsage() {
        HelpFormatter f = new HelpFormatter();
        f.printHelp(this.getClass().getName(), setOptions());
        if (optionNotes != null) {
            System.out.println("Note: " + optionNotes);
        }
    }

    public boolean setup(ArchiveInterface theArchive, String segmentName,
            String movieFile, double startTime, double endTime,
            double duration, boolean useDuration, float speedupFactor) {

        ArchiveSegmentInterface seg = theArchive.getSegmentByName(segmentName);
        if (seg == null) {
            reportError("ArchiveToMovie: " + "Segment " + segmentName
                    + " is not in the archive.");
            return false;
        }

        // Check for output file extension.
        if (!movieFile.endsWith(".mov") && !movieFile.endsWith(".MOV")) {
            reportError("The output file name should end with a .mov extension");
            return false;
        }

        this.archive = theArchive;
        this.segmentName = segmentName;
        this.movieFile = movieFile;
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration;
        this.useDuration = useDuration;
        this.speedupFactor = speedupFactor;
        return setup();
    }

    public boolean setup() {

        segment = archive.getSegmentByName(segmentName);
        if (segment == null) {
            reportError("ArciveToMovie: " + "Segment " + segmentName
                    + " is not in the archive.");
            return false;
        }

        if (startTime == 0.0)
            startTime = segment.getStartTimeAsDouble();
        if (endTime == 0.0)
            endTime = segment.getEndTimeAsDouble();

        if (useDuration) {
            endTime = startTime + duration;
        }

        long startTimeL = (long) (startTime * 1000.0);
        startTimeString = ArchiveUtility.getCommandFormat().format(
                new Date(startTimeL));
        long endTimeL = (long) (endTime * 1000.0);
        endTimeString = ArchiveUtility.getCommandFormat().format(
                new Date(endTimeL));

        reportStatus("StartTime = " + startTimeString + "; \nEndTime = "
                + endTimeString + "; \nDuration = " + duration);

        if (startTime < segment.getStartTimeAsDouble()) {
            long time = segment.getStartTime();
            String timeString = ArchiveUtility.getCommandFormat().format(
                    new Date(time));
            reportStatus("Start time (" + startTimeString + ")"
                    + " is before then the earlest time " + "in the segment ("
                    + timeString + ") " + "adjusting startTime");
            startTime = segment.getStartTimeAsDouble();
            startTimeString = ArchiveUtility.getCommandFormat().format(
                    new Date(segment.getStartTime()));
        }

        if (endTime > segment.getEndTimeAsDouble()) {
            long time = segment.getEndTime();
            String timeString = ArchiveUtility.getCommandFormat().format(
                    new Date(time));
            reportStatus("End time (" + endTimeString + ")"
                    + " is greater then the greatest time in the segment ("
                    + timeString + ") adjusting endTime");
            endTime = segment.getEndTimeAsDouble();
            endTimeString = ArchiveUtility.getCommandFormat().format(
                    new Date(segment.getEndTime()));
        }

        // get latest time in archive segment and check against startTime
        if (startTime > segment.getEndTimeAsDouble()) {
            reportError("Start time is later then the latest time in the segment ("
                    + segmentName
                    + "). Start time is: "
                    + ArchiveUtility.getCommandFormat().format(
                            new Date((long) (startTime * 1000.0)))
                    + "; the latest time is: "
                    + ArchiveUtility.getCommandFormat().format(
                            new Date(segment.getEndTime())));
            return false;
        }

        // get earliest time in archive segment and check against endTime
        if (endTime < segment.getStartTimeAsDouble()) {
            reportError("End time is eariler then the earliest time in the segment ("
                    + segmentName
                    + "). End time is: "
                    + ArchiveUtility.getCommandFormat().format(
                            new Date((long) (endTime * 1000.0)))
                    + "; the earliest time is: "
                    + ArchiveUtility.getCommandFormat().format(
                            new Date(segment.getStartTime())));
            return false;
        }

        duration = endTime - startTime;

        reportStatus("Starting ArchiveToMovie with --" + "  \nMovie file = "
                + movieFile + "; \nSegment name = " + segmentName
                + "; \nStartTime = " + startTimeString + "; \nEndTime = "
                + endTimeString + "; \nDuration = " + duration);

        return true;
    }

    public void exec() {
        try {

            estimatedDuration = duration;
            consumedTime = 0.0;
            clockStartTime = System.currentTimeMillis();

            long startTimeL = (long) (startTime * 1000.0);
            long endTimeL = (long) (endTime * 1000.0);

            imageArray = (ArchiveImageInterface[]) segment.getSortedArray(
                    startTimeL, endTimeL);

            determineFrameRateHeightAndWidth();

            reportStatus("Estimated Frame Rate = " + movieFrameRate);
            reportStatus("Move Frame height = " + movieFrameHeight
                    + "; width = " + movieFrameWidth);

            if (!doMovie(movieFrameHeight, movieFrameWidth, movieFrameRate))
                reportStatus("Somthing wrong with the movie process.");

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void determineFrameRateHeightAndWidth() throws IOException,
            ArchiveException {
        // use first image for height and width
        JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(imageArray[0]
                .getImageInputStream());

        // decode JPEG image to raw image
        BufferedImage bi;
        try {
            bi = decoder.decodeAsBufferedImage();
            movieFrameHeight = bi.getHeight();
            movieFrameWidth = bi.getWidth();
        } catch (IOException e) {
            System.out
                    .println("Failed to decode input JPEG image, for height and width.");
            throw e;
        }

        double start = segment.getStartTimeAsDouble();
        double end = segment.getEndTimeAsDouble();
        int recordCount = imageArray.length;
        if (recordCount < 2) {
            throw new ArchiveException(
                    "Not enough records to estimate frame rate; "
                            + "recordCount = " + recordCount);
        }
        movieFrameRate = ((float) (((double) (recordCount - 1)) / (end - start)));
        movieFrameRate = movieFrameRate * speedupFactor;
    }

    private boolean doMovie(int height, int width, float frameRate)
            throws NoProcessorException, IOException {
        Processor p;

        MediaLocator outML = new MediaLocator("file:" + movieFile);

        if (outML == null) {
            reportStatus("Failed to form URL from movie file path = "
                    + movieFile);
            return false;
        }

        PullBufferDataSource dataSource = (PullBufferDataSource) new ArchiveSegmentDataSource(
                width, height, frameRate);

        p = Manager.createProcessor(dataSource);

        p.addControllerListener(this);

        // Put the Processor into configured state so we can set
        // some processing options on the processor.
        p.configure();
        if (!waitForState(p, Processor.Configured)) {
            reportError("Failed to configure the processor.");
            return false;
        }

        // Set the output content descriptor to QuickTime.
        p.setContentDescriptor(new ContentDescriptor(
                FileTypeDescriptor.QUICKTIME));

        // Query for the processor for supported formats.
        // Then set it on the processor.
        TrackControl tcs[] = p.getTrackControls();
        Format f[] = tcs[0].getSupportedFormats();
        // if (f == null || f.length <= 0) {
        // reportError("The mux does not support the input format: "
        // + tcs[0].getFormat());
        // return false;
        // }
        //
        // reportStatus("Found " + f.length + " formats");
        //
        // for (int i = 0; i < f.length; i++)
        // reportStatus("Found " + f[i]);
        //
        tcs[0].setFormat(f[0]);

        reportStatus("Setting the track format to: " + f[0]);

        // We are done with programming the processor. Let's just
        // realize it.
        p.realize();
        if (!waitForState(p, Controller.Realized)) {
            reportStatus("Failed to realize the processor.");
            return false;
        }

        // ContentDescriptor[] descriptors = p.getSupportedContentDescriptors();
        // for (int n = 0; n < descriptors.length; n++) {
        // reportStatus("Desc: " + descriptors[n].toString());
        // }
        //
        // DataSource output = p.getDataOutput();
        //
        // reportStatus("DataSource type: ");
        // Class cls = output.getClass();
        // while (cls != null) {
        // reportStatus(cls.toString());
        // cls = cls.getSuperclass();
        // }

        // Now, we'll need to create a DataSink.
        DataSink dsink;
        if ((dsink = createDataSink(p, outML)) == null) {
            reportError("Failed to create a DataSink for the given output MediaLocator: "
                    + outML);
            return false;
        }

        dsink.addDataSinkListener(this);

        reportStatus("start processing...");

        // OK, we can now start the actual transcoding.
        try {
            p.start();
            dsink.start();
        } catch (Exception e) {
            reportError("IO error during processing");
            return false;
        }

        return true;
    }

    /**
     * Create the DataSink.
     */
    private DataSink createDataSink(Processor p, MediaLocator outML) {

        DataSource ds;

        if ((ds = p.getDataOutput()) == null) {
            reportError("Something is really wrong: the processor does not have an output DataSource");
            return null;
        }

        DataSink dsink;

        try {
            reportStatus("Creating DataSink for: " + outML);
            dsink = Manager.createDataSink(ds, outML);
            dsink.open();
        } catch (Exception e) {
            reportStatus("Cannot create the DataSink: " + e);
            return null;
        }

        return dsink;
    }

    // ------------------- notification utilities ---------------------

    private void addToStatus(String s) {
        if (s == null)
            return;
        if (s.length() == 0)
            return;
        statusMessages.addElement(s);
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

    private void reportError(String msg) {
        statusOk = false;
        System.err.println(msg);
        addToStatus(msg);
    }

    private void reportStatus(String msg) {
        System.out.println(msg);
        addToStatus(msg);
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
                .nextElement()).progressUpdate(estimatedDuration, consumedTime)) {
        }
    }


    // ------------------- image processing methods ---------------------

    Object waitSyncController = new Object();

    boolean stateTransitionOK = true;

    /**
     * Block until the processor has transitioned to the given state. Return
     * false if the transition failed.
     */
    boolean waitForState(Processor p, int state) {
        synchronized (waitSyncController) {
            try {
                while (p.getState() < state && stateTransitionOK)
                    waitSyncController.wait();
            } catch (Exception e) {
            }
        }
        return stateTransitionOK;
    }

    /**
     * Controller Listener. (API)
     */
    public void controllerUpdate(ControllerEvent evt) {

        if (evt instanceof ConfigureCompleteEvent
                || evt instanceof RealizeCompleteEvent
                || evt instanceof PrefetchCompleteEvent) {
            synchronized (waitSyncController) {
                stateTransitionOK = true;
                waitSyncController.notifyAll();
            }
        } else if (evt instanceof ResourceUnavailableEvent) {
            synchronized (waitSyncController) {
                stateTransitionOK = false;
                waitSyncController.notifyAll();
            }
        } else if (evt instanceof EndOfMediaEvent) {
            evt.getSourceController().stop();
            evt.getSourceController().close();
        }
    }

    Object waitFileSync = new Object();

    boolean fileDone = false;

    boolean fileSuccess = true;

    /**
     * Block until file writing is done.
     */
    boolean waitForFileDone() {
        synchronized (waitFileSync) {
            try {
                while (!fileDone)
                    waitFileSync.wait();
            } catch (Exception e) {
            }
        }
        return fileSuccess;
    }

    /**
     * Data sync listener for connection ????
     */
    public void dataSinkUpdate(DataSinkEvent evt) {

        if (evt instanceof EndOfStreamEvent) {
            synchronized (waitFileSync) {
                fileDone = true;
                waitFileSync.notifyAll();
            }
        } else if (evt instanceof DataSinkErrorEvent) {
            synchronized (waitFileSync) {
                fileDone = true;
                fileSuccess = false;
                waitFileSync.notifyAll();
            }
        }
    }

    /**
     * A PullBufferDataSource to read a JPEG stream of images from an archive
     * segment and turn that into a JMF stream. The DataSource is not seekable
     * or positionable.
     */
    private class ArchiveSegmentDataSource extends PullBufferDataSource {

        ArchiveSegmentSourceStream streams[];

        ArchiveSegmentDataSource(int width, int height, float frameRate) {
            streams = new ArchiveSegmentSourceStream[1];
            streams[0] = new ArchiveSegmentSourceStream(width, height,
                    frameRate);
        }

        public void setLocator(MediaLocator source) {
        }

        public MediaLocator getLocator() {
            return null;
        }

        /**
         * Content type is of RAW since we are sending buffers of video frames
         * without a container format.
         */
        public String getContentType() {
            return ContentDescriptor.RAW;
        }

        public void connect() {
            // when does this get called?
            reportStatus("ArchiveSegmentDataSource connect");
        }

        public void disconnect() {
            // when does this get called?
            reportStatus("ArchiveSegmentDataSource disconnect");
        }

        public void start() {
            // when does this get called?
            reportStatus("ArchiveSegmentDataSource start");
        }

        public void stop() {
            // when does this get called?
            reportStatus("ArchiveSegmentDataSource stop");
        }

        /**
         * Return the ArchiveSegmentSourceStream.
         */
        public PullBufferStream[] getStreams() {
            return streams;
        }

        /**
         * We could have derived the duration from the number of frames and
         * frame rate. But for the purpose of this program, it's not necessary.
         */
        public Time getDuration() {
            return DURATION_UNKNOWN;
        }

        public Object[] getControls() {
            return new Object[0];
        }

        public Object getControl(String type) {
            return null;
        }

    } // ArchiveSegmentDataSource

    /**
     * The source stream to go along with ImageDataSource.
     */
    private class ArchiveSegmentSourceStream implements PullBufferStream {

        int width, height;

        VideoFormat format;

        int imageCount = 0;

        boolean ended = false;

        public ArchiveSegmentSourceStream(int width, int height, float frameRate) {
            this.width = width;
            this.height = height;

            format = new VideoFormat(VideoFormat.JPEG, new Dimension(width,
                    height), Format.NOT_SPECIFIED, Format.byteArray, frameRate);
        }

        /**
         * As I understand it, this would require read ahead and buffering. RBNB
         * does the buffering as well as anything will.
         */
        public boolean willReadBlock() {
            return false;
        }

        /**
         * This is called from the Processor to read a frame worth of video
         * data.
         */
        public void read(Buffer buf) throws IOException {

            long timeStampL = 0l;
            if (imageCount < imageArray.length)
                timeStampL = imageArray[imageCount].getTime();
            else
                timeStampL = imageArray[imageArray.length - 1].getTime();
            double timeStamp = ((double) timeStampL) / 1000.0;

            updateTimeEstimate();
            if ((imageCount % 10) == 0)
                notifyAllListeners();
            
            if (statusOk && (timeStamp <= endTime)
                    && (imageCount < imageArray.length)) {
                byte[] data = readImageFully(imageCount);
                buf.setOffset(0);
                buf.setLength(data.length);
                buf.setData(data);
                buf.setFormat(format);
                if ((imageCount % 10) == 0)
                    buf.setFlags(buf.getFlags() | Buffer.FLAG_KEY_FRAME);
                imageCount++;
            } else // at the end!
            {
                if (statusOk)
                    reportStatus("Nomal exit at the RBNB record with time stamp = "
                            + ArchiveUtility.getCommandFormat().format(
                                    new Date(timeStampL)));
                // We are done. Set EndOfMedia.
                setEOMBuffer(buf);
                reportStatus("Wrote " + imageCount + " iamges to the movie.");
                estimatedDuration = consumedTime;
                notifyAllListeners();
                reportStatus("Segment to movie, done.");
                ended = true;
            }
            return;
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

        int bufferSize = 24000;

        private byte[] readImageFully(int index) throws IOException {
            byte[] buf = new byte[bufferSize];
            byte[] next = new byte[bufferSize];
            int count;
            int nextCount;
            InputStream in = imageArray[index].getImageInputStream();
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

        /*
         * Set the buffer to represent an empty EOM buffer
         */
        private void setEOMBuffer(Buffer buf) {
            buf.setEOM(true);
            buf.setOffset(0);
            buf.setLength(0);
        }

        /**
         * Return the format of each video frame. That will be JPEG.
         */
        public Format getFormat() {
            return format;
        }

        public ContentDescriptor getContentDescriptor() {
            return new ContentDescriptor(ContentDescriptor.RAW);
        }

        public long getContentLength() {
            return 0;
        }

        public boolean endOfStream() {
            return ended;
        }

        public Object[] getControls() {
            return new Object[0];
        }

        public Object getControl(String type) {
            return null;
        }
    } // ArchiveSegmentSourceStream

}