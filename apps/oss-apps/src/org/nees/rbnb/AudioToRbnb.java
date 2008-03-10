package org.nees.rbnb;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import javax.media.format.RGBFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.AudioFormat;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.nees.rbnb.AudioHelp.UserInfoHolder;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Source;

/**
 * This NEES RBNB Utility captures audio and records it an RBNB for a fised duration
 * or until the program is interupted. See AudioToRbnb -h for arguments.
 * 
 * @author Terry E Weymouth
 *
 */
public class AudioToRbnb
extends RBNBBase
implements Observer 
{
    
    // these are used by RbnbToAudio!
    static final String SOURCE_NAME = "AudioCaputre";
    static final String CHANNEL_NAME = "audio.snd";
    
    private String sourceName = SOURCE_NAME;
    private String channelName = CHANNEL_NAME;

    private static final int DEFAULT_CACHE_SIZE = 900;
    private int cacheSize = DEFAULT_CACHE_SIZE;
    private static final int DEFAULT_ARCHIVE_SIZE = 0;
    private int archiveSize = DEFAULT_ARCHIVE_SIZE;
    
    private static final double DEFAUT_DURATION = 10.0 * 60.0; // ten minutes
//    private static final double DEFAUT_DURATION = 20.0; // twoenty seconds
    private double duration = DEFAUT_DURATION;
        
    private Source source = null;
    private ChannelMap cMap;
    private boolean connected = false;
    
    private Thread captureThread;
    private boolean runit = false;
    private static Thread mainThread;
    private static boolean finished = false;
    
    private TargetDataLine inLine = null; // audio input device
//    private AudioInputStream inStream = null;
    private AudioFormat selectedFormat = null; // the audio fomat used

    public static void main(String[] args) {
        AudioToRbnb c = new AudioToRbnb();
        if (c.parseArgs(args))
        {
            try {
                c.setup();
                c.startThread();
            } catch (Exception e) {
                e.printStackTrace();
                finished = true;
            }
            // These last statments becasue the audio seems to start threads
            // that do not stop, so without System.exit, the application does not quite!
            mainThread = Thread.currentThread();
            while (!finished) try {Thread.sleep(100);} catch (Throwable t){}
            System.exit(0);
        }
    }
    
    public AudioToRbnb() {
    }

    public AudioToRbnb(String server_host, String server_port,
            String source_name, String channel_name, 
            int archiveSize, int cacheSize,
            double duration_parameter)
    {
        this.setServerName(server_host);
        this.setServerPort(server_port);
        sourceName = source_name;
        channelName = channel_name;
        duration = duration_parameter;

        System.out.println("Starting AudioToRbnb on " +  getServer() + " as " + sourceName);
        System.out.println("  Channel name = " + channelName);
        System.out.println("  running for " + duration + " seconds.");
        System.out.println("  With RBNB cache size = " + cacheSize + "; " +
                "and RBNB archive size = " + archiveSize);
        System.out.println("  Use AudioToRbnb -h to see optional parameters");
    }

    
    protected String getCVSVersionString()
    {
        return
            "  CVS information... \n" +
            "  $Revision: 153 $\n" +
            "  $Date: 2007-09-24 13:10:37 -0700 (Mon, 24 Sep 2007) $\n" +
            "  $RCSfile: $ \n";
    }

    /* (non-Javadoc)
     * @see org.nees.rbnb.RBNBBase#setOptions()
     */
    protected Options setOptions() {
        Options opt = setBaseOptions(new Options()); // uses h, s, p
        opt.addOption("n",true,"source_name *" + SOURCE_NAME);
        opt.addOption("c",true,"channel_name *" + CHANNEL_NAME);
        opt.addOption("d",true,"duration (floating point seconds) *" + DEFAUT_DURATION);
        opt.addOption("z", true, "cache size *" + DEFAULT_CACHE_SIZE);
        opt.addOption("Z", true, "archive size *" + DEFAULT_ARCHIVE_SIZE);
        return opt;
    }
    
    /* (non-Javadoc)
     * @see org.nees.rbnb.RBNBBase#setArgs(org.apache.commons.cli.CommandLine)
     */
    protected boolean setArgs(CommandLine cmd) {

        if (!setBaseArgs(cmd)) return false;

        if (cmd.hasOption('n')) {
            String a=cmd.getOptionValue('n');
            if (a!=null) sourceName=a;
        }
        if (cmd.hasOption('c')) {
            String a=cmd.getOptionValue('c');
            if (a!=null) channelName=a;
        }
        if (cmd.hasOption('d')) {
            String a = cmd.getOptionValue('d');
            if (a != null)
                try {
                    Double d = new Double(a);
                    double value = d.doubleValue();
                    duration = value;
                } catch (Exception ignore) {}
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
    
        if ((archiveSize > 0) && (archiveSize < cacheSize)){
            System.err.println(
                "a non-zero archiveSize = " + archiveSize + " must be greater then " +
                    "or equal to cacheSize = " + cacheSize);
            return false;
        }

        System.out.println("Starting AudioToRbnb on " + getServer() + " as " + sourceName);
        System.out.println("  Channel name = " + channelName);
        System.out.println("  running for " + duration + " seconds.");
        System.out.println("  With RBNB cache size = " + cacheSize + "; " +
                "and RBNB archive size = " + archiveSize);
        System.out.println("  Use AudioToRbnb -h to see optional parameters");
        
        return true;
    }
    
    public void setup() throws Exception {
// I did not use this, because it seems that the shutdown thread gets called AFTER
// the threads are stoped, and that means that the audio does not get wrapped up.
// This is an assumption that I only checked briefly, it could be that there is a 
// a way to use shutdown that I have not figured out yet... but...
//        // create a shutdown handler
//        Runtime.getRuntime().addShutdownHook(new Thread() {
//            public void run() {
//                shutdown();
//            }
//        });
// I'm using sig handler instead (which is risky because it is not supported!)
        
        // add a signal handler
        try {
            SignalHandler sh = new SignalHandler();
            sh.addObserver(this);
            sh.handleSignal("HUP");
            sh.handleSignal("INT");
            sh.handleSignal("TERM");
        } catch (Throwable x) {
            // SignalHandler failed to instantiate: maybe the classes do not
            // exist, or the API has changed, or something else went wrong;
            System.out.println("Unexpected excpetion in Signal Handler set up.");
            throw new Exception(x);
        }

        // Create a source and connect:
        if (archiveSize > 0)
            source = new Source(cacheSize, "create", archiveSize);
        else
            source = new Source(cacheSize, "none", 0);
        source.OpenRBNBConnection(getServer(), sourceName);
        System.out.println("AudioToRbnb: Connection made to server = "
                + getServer() + " as " + sourceName + " with " + channelName
                + ".");

        // get the best format that is supported and its audio input device...

        AudioFormat inFormat = AudioHelp.findBestFormatForCapture();
        if (inFormat == null) {
            throw new Exception(
                    "Unable to discover a sutable format for capture.");
        }
        DataLine.Info info = new DataLine.Info(TargetDataLine.class,
                inFormat);

        System.out.println("Selected format = " + inFormat);
        System.out.println("With info = " + info);

        if (!AudioSystem.isLineSupported(info)) {
            System.out.println("Did not find sound input line.");
            System.out.println("Line matching " + info + " not supported.");
            throw new Exception(
                    "Can not find any audio device for sound capture.");
        }

        inLine = (TargetDataLine) AudioSystem.getLine(info);
        inLine.open(inFormat, inLine.getBufferSize());

//        AudioFormat(AudioFormat.Encoding encoding, float sampleRate, 
//                int sampleSizeInBits, int channels, int frameSize, float frameRate,
//                boolean bigEndian)
//        selectedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
//                1000.0f,16,inFormat.getChannels(),2*inFormat.getChannels(),1000.0f,
//                inFormat.isBigEndian());
//        inStream = 
//            new AudioInputStream(
//                    new AudioInputStream(inLine),
//                    selectedFormat,inLine.getBufferSize());
        selectedFormat = inFormat;
        System.out.println("Found and opened sound input line.");
        
        AudioHelp.UserInfoHolder infoHolder = (new AudioHelp()).new UserInfoHolder(
                selectedFormat, System.currentTimeMillis());

        String userData = infoHolder.toString();

        ChannelMap cm = new ChannelMap();
        cm.Add(channelName);
        cm.PutUserInfo(0, userData);
        cm.PutMime(0, "text/plain");
        source.Register(cm);

        cMap = new ChannelMap();
        cMap.Add(channelName);
        cMap.PutMime(0, "audio/basic");
        cMap.PutTimeAuto("timeofday");
        connected = true;
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
        captureThread = new Thread(r, "AudioCaptureToRBNB");
        captureThread.start();
    }

    public void stopThread()
    {
        runit = false;
        captureThread.interrupt();
    }
    
    private void runWork ()
    {
        System.out.println("Thread Started.");
 
        int frameSizeInBytes = selectedFormat.getFrameSize();
        int bufferLengthInFrames = inLine.getBufferSize() / 8;
        int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
        byte[] data = new byte[bufferLengthInBytes];
        int numBytesRead;

        inLine.start();

        double time = 0.0; // in seconds
        long nowTime, sysTime = System.currentTimeMillis();
        long blipTime = sysTime;
        try {
            while(isRunning() && (time < duration))
            {
//                if((numBytesRead = inStream.read(data, 0, bufferLengthInBytes)) == -1) {
//                    break;
//                }
                if((numBytesRead = inLine.read(data, 0, bufferLengthInBytes)) == -1) {
                    break;
                }
                if (numBytesRead == data.length)
                    cMap.PutDataAsByteArray(0,data);
                else
                {
                    System.out.println("Truncated.");
                    if (numBytesRead > 0)
                    {
                        byte[] truncatedData = new byte[numBytesRead];
                        System.arraycopy(data,0,truncatedData,0,numBytesRead);
                        cMap.PutDataAsByteArray(0,truncatedData);                        
                    }
                }
                source.Flush(cMap);

                nowTime = System.currentTimeMillis();
                time = (nowTime - sysTime)/1000.0; // in seconds
                if ((nowTime - blipTime) > 1000) // every second
                {
                    System.out.print("*"); // blip
                    blipTime = nowTime;
                }
            }
            runit = false;
        } catch (SAPIException se) {
            se.printStackTrace(); 
//        } catch (IOException e) {
//            e.printStackTrace();
        }
        System.out.println("End of thread loop");
        wrapup();
    }

    private void wrapup()
    {
        // we reached the end of the stream.  stop and close the line.
        inLine.stop();
        inLine.close();
        inLine = null;
        System.out.println("Line done");
        // close or keep the RBNB thread
        if (archiveSize > 0)
            source.Detach();
        else
            source.CloseRBNBConnection();
        connected = false;
        source = null;
        System.out.println("Source done");
        // ... and then thread is done.
        System.out.println("Thread Stopped.");
        captureThread = null;
        finished = true;
        if (mainThread != null) mainThread.interrupt();
    }

    public boolean isRunning()
    {
        return (connected && runit);
    }
    
    public void update(Observable arg0, Object arg1) {
        if (!isRunning()) return;
        // we got a "shutdown" request
        System.out.println("Early Shutdown Requested...");
        stopThread();
    }

}
