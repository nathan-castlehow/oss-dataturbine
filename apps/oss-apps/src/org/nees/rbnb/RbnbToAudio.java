package org.nees.rbnb;

import java.text.SimpleDateFormat;
//import java.util.Date;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.util.TimeZone;
import java.util.concurrent.ArrayBlockingQueue;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Sink;

public class RbnbToAudio
extends RBNBBase
implements Observer 
{

    private static final String SINK_NAME = "GetAudio";
    private static final String SOURCE_NAME = AudioToRbnb.SOURCE_NAME;
    private static final String CHANNEL_NAME = AudioToRbnb.CHANNEL_NAME;
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM d, yyyy h:mm:ss.SSS aa");
    private static final TimeZone TZ = TimeZone.getTimeZone("GMT");

    static
    {
        DATE_FORMAT.setTimeZone(TZ);
    }

    private String sinkName = SINK_NAME;
    private String sourceName = SOURCE_NAME;
    private String channelName = CHANNEL_NAME;
    private String requestPath = sourceName + "/" + channelName;

    private Sink sink = null;
    private ChannelMap sMap;
    private int index;
    private boolean connected = false;
    
    private Thread rbnbFetchThread;
    private boolean runit = false;
    private Thread playbackThread;
    private static Thread mainThread;
    private static boolean finished = false;

    private AudioFormat selectedFormat = null;
    private SourceDataLine outLine = null;
    private ArrayBlockingQueue<byte[]> fifo = null;
        
    public static void main(String[] args) {
        RbnbToAudio s = new RbnbToAudio();
        if (s.parseArgs(args))
        {
            try {
                s.setUp();
                s.startThread();
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
        opt.addOption("k",true,"Sink Name *" + SINK_NAME);
        opt.addOption("n",true,"Source Name *" + SOURCE_NAME);
        opt.addOption("c",true,"Source Channel Name *" + CHANNEL_NAME);
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
        if (cmd.hasOption('k')) {
            String a=cmd.getOptionValue('k');
            if (a!=null) sinkName=a;
        }

        requestPath = sourceName + "/" + channelName;
        
        System.out.println("Starting RbnbToAudio on " + getServer() + " as " + sinkName);
        System.out.println("  Requesting " + requestPath);
        System.out.println("  Use RbnbToAudio -h to see optional parameters");
        return true;
    }
    
    public void setUp() throws Exception
    {
        // Create a sink and connect:
        sink=new Sink();
        sink.OpenRBNBConnection(getServer(),sinkName);
        ChannelMap cm = new ChannelMap();
        index = cm.Add(requestPath);
        sink.RequestRegistration();
        sink.Fetch(-1,cm);

        if (cm.GetChannelList().length < 1)
        {
            throw new Exception("Request channel unavailable = " + requestPath);
        }
        
        // get and parse user info
        String mimeType = cm.GetMime(index);
        System.out.println("MIME Type = " + mimeType);
        
        String userInfo = cm.GetUserInfo(index);
        System.out.println("User Info:   " + userInfo);
        
        if ((userInfo == null) || (userInfo.length() == 0))
        {
            throw new Exception("Unable to obtain format information!");
        }

        AudioHelp.UserInfoHolder userInfoHolder 
            = (new AudioHelp()).new UserInfoHolder(userInfo);
        
        System.out.println("Parsed info: " + userInfo.toString());

        selectedFormat = new AudioFormat(userInfoHolder.encoding,
                userInfoHolder.sampleRate, userInfoHolder.sampleSize, 
                userInfoHolder.channels,userInfoHolder.frameSize,
                userInfoHolder.frameRate, userInfoHolder.bigEndian);
        
        DataLine.Info info = new DataLine.Info(SourceDataLine.class,selectedFormat);

        System.out.println("Given format = " + selectedFormat);
        System.out.println("With info = " + info);

        // get connection to line for audio output

        if (!AudioSystem.isLineSupported(info)) {
            System.out.println("Did not find sound input line.");
            System.out.println("Line matching " + info + " not supported.");
            throw new Exception(
                    "Can not find any audio device for sound playback.");
        }

        try {
            outLine = (SourceDataLine) AudioSystem.getLine(info);
            outLine.open(selectedFormat, outLine.getBufferSize());
        } catch (LineUnavailableException ex) { 
            System.out.println("Unable to open the line: " + ex);
            return;
        }

        System.out.println("Found and opened sound output line.");

        // fifo will connect reading thread with playback thread
        fifo = new ArrayBlockingQueue<byte[]>(20,true);
        
        // make the connection for the data (audio)
        sMap = new ChannelMap();
        index = sMap.Add(requestPath);
        sink.Subscribe(sMap,"newest");
        connected = true;
        System.out.println("RbnbToAudio: Connection made to server = "
            + getServer() + " as " + sinkName 
            + " requesting " + requestPath + ".");
 
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
    }
    
    public void startThread()
    {
        
        if (!connected) return;
        
        // Use this inner classes to hide the public run method
        Runnable rRead = new Runnable() {
            public void run() {
              runForRead();
            }
        };
        Runnable rPlayback = new Runnable() {
            public void run() {
              runForPlayback();
            }
        };

        runit = true;

        rbnbFetchThread = new Thread(rRead, "AudioRbnbRead");
        rbnbFetchThread.start();
        System.out.println("RbnbToAudio: Start read thread requested.");
        playbackThread = new Thread(rPlayback, "AudioRbnbPlayback");
        playbackThread.start();
        System.out.println("RbnbToAudio: Start playback thread requested.");
    }

    public void stopThread()
    {
        runit = false;
        System.out.println("RbnbToAudio: Stop thread requested.");
    }
    
    private void runForRead ()
    {
        System.out.println("Reading thread started.");       
        try {
            while(isRunning())
            {
                
                ChannelMap m = sink.Fetch(10000);
                if (m.GetChannelList().length < 1)
                {
                    // we'er hosed
                    throw new SAPIException("Unexpected empty channel list");
                }
//                double[] times = m.GetTimes(index);
//                long unixTime = (long)(times[0] * 1000.0); // convert sec to millisec
//                String time = DATE_FORMAT.format(new Date(unixTime));
//                System.out.println(time + "(GMT)");
                byte [][] data= m.GetDataAsByteArray(index);
                byte [] playback = data[0];
                fifo.put(playback);
            }
        } catch (SAPIException se) {
            se.printStackTrace();
            stopThread();
        } catch (InterruptedException e) {
            e.printStackTrace();
            stopThread();
        }
        sink.CloseRBNBConnection();
        sink=null;
        rbnbFetchThread = null;
        System.out.println("Reading thread stopped.");       
    }

    private void runForPlayback()
    {
        System.out.println("Playback thread started.");               
        outLine.start();
        while (isRunning())
        {
            byte[] playback = fifo.poll();
            if (playback != null)
                outLine.write(playback, 0, playback.length);
            else
            {
                System.out.println("Running on empty!");
                while (fifo.remainingCapacity() > 1)
                {
                    System.out.println("Filling fifo queue...");
                    try {Thread.sleep(100);} catch (Exception e){}
                }
            }
        }
        Iterator<byte[]> i = fifo.iterator();
        while (i.hasNext())
        {
            byte[] playback = i.next();
            outLine.write(playback, 0, playback.length);
        }
        outLine.drain();
        outLine.stop();
        outLine.close();
        outLine = null;
        playbackThread = null;
        finished = true;
        if (mainThread != null) mainThread.interrupt();
        System.out.println("Playback thread stopped.");               
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
