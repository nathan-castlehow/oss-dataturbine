package org.nees.audioExample;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

import org.nees.rbnb.AudioHelp;
import org.nees.rbnb.AudioHelp.UserInfoHolder;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Sink;
import com.rbnb.sapi.Source;

public class RbnbThroughPutTest {

    private static final String SOURCE_NAME = "ThroughPutTestSource";
    private static final String SOURCE_CHANNEL = "data";
    private static final String SINK_NAME = "ThroughPutTestSink";
    private static final String SERVER_NAME = "dev-neestpm.sdsc.edu";
    private static final int NUBMER_OF_HITS = 100;
    
    private boolean sinkDone = false;
    
    public static void main(String[] args) {
        
        String serverNameAndPort = SERVER_NAME;
        if (args.length > 0) serverNameAndPort = args[0];
        if (!serverNameAndPort.contains(":")) serverNameAndPort += ":3333";
        (new RbnbThroughPutTest()).exec(serverNameAndPort);
    }

    private void exec(String server)
    {
        final String s = server;
        
        Thread sourceThread = new Thread(){
            public void run (){ execSource(s);}
        };
        Thread sinkThread = new Thread(){
            public void run (){ execSink(s);}
        };
        sinkDone = false;
        sourceThread.start();
        try {Thread.sleep(5000);} catch (Exception ignore){}
        sinkThread.start();
    }
    
    private void execSource(String server)
    {
        Source source = null;
        
        try {

            System.out.println("Source Thread Started.");
        
            // connect
            source = new Source(NUBMER_OF_HITS, "none", 0);
            source.OpenRBNBConnection(server, SOURCE_NAME);

            System.out.println("Connection made to server = "
                    + server + " as " + SOURCE_NAME + " with " + SOURCE_CHANNEL
                    + ".");
        
            AudioFormat audioFormat = AudioHelp.findBestFormatForCapture();
            if (audioFormat == null) {
                throw new Exception(
                        "Unable to discover a sutable format for capture.");
            }

            DataLine.Info info = new DataLine.Info(TargetDataLine.class,
                    audioFormat);

            System.out.println("Selected format = " + audioFormat);
            System.out.println("With info = " + info);

            if (!AudioSystem.isLineSupported(info)) {
                System.out.println("Did not find sound input line.");
                System.out.println("Line matching " + info + " not supported.");
                throw new Exception(
                        "Can not find any audio device for sound capture.");
            }

            TargetDataLine inLine = (TargetDataLine) AudioSystem.getLine(info);

            int frameSizeInBytes = audioFormat.getFrameSize();
            int bufferLengthInFrames = inLine.getBufferSize() / 8;
            int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
            byte[] data = new byte[bufferLengthInBytes];
            
            System.out.println("Frame Size = " + frameSizeInBytes);
            System.out.println("Buffer Size = " + bufferLengthInBytes);
            System.out.println("Rate = " + audioFormat.getSampleRate());
            System.out.println("Seconds per buffer = " 
                    + ((float)bufferLengthInFrames)/ audioFormat.getSampleRate());

            AudioHelp.UserInfoHolder infoHolder = (new AudioHelp()).new UserInfoHolder(
                    audioFormat, System.currentTimeMillis());

            String userData = infoHolder.toString();

            // send user info
            ChannelMap cm = new ChannelMap();
            cm.Add(SOURCE_CHANNEL);
            cm.PutUserInfo(0, userData);
            cm.PutMime(0, "text/plain");
            source.Register(cm);

            // send data
            cm = new ChannelMap();
            cm.Add(SOURCE_CHANNEL);
            cm.PutMime(0, "audio/basic");

            for (int i = 0; i < NUBMER_OF_HITS; i++)
            {
//                System.out.println("Sending " + i);
                // send the data
                double time = i;
                cm.PutTime(time,0.0);
                cm.PutDataAsByteArray(0,data);
                source.Flush(cm);
            }
            
        } catch (SAPIException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
   
        // all done, wait for sink to finish
        while(!sinkDone)
        {
            try {Thread.sleep(100);} catch (Exception ignore) {}
        }

        //disconnect
        if (source != null) source.CloseRBNBConnection();
        
        System.out.println("Source Thread Finished.");
    }
    
    private void execSink(String server)
    {
        Sink sink = null;
        long totalBufferLength = 0;
        int bufferCount = 0;
        
        try 
        {
            System.out.println("Sink Thread Started.");
    
            String requestPath = SOURCE_NAME + "/" + SOURCE_CHANNEL;
            sink=new Sink();
            sink.OpenRBNBConnection(server,SINK_NAME);
            ChannelMap cm = new ChannelMap();
            int index = cm.Add(requestPath);
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
    
            AudioFormat sentFormat = new AudioFormat(userInfoHolder.encoding,
                    userInfoHolder.sampleRate, userInfoHolder.sampleSize, 
                    userInfoHolder.channels,userInfoHolder.frameSize,
                    userInfoHolder.frameRate, userInfoHolder.bigEndian);
            
            DataLine.Info info = new DataLine.Info(SourceDataLine.class,sentFormat);
    
            System.out.println("Given format = " + sentFormat);
            System.out.println("With info = " + info);

            cm = new ChannelMap();
            index = cm.Add(requestPath);
            sink.Subscribe(cm,"newest");
            System.out.println("Connection made to server = "
                + server + " as " + SINK_NAME 
                + " requesting " + requestPath + ".");
            
            int mark = 0;
            while (mark < (NUBMER_OF_HITS -1))
            {
                ChannelMap m = sink.Fetch(-1);
                if (m.GetChannelList().length < 1)
                {
                    // we're hosed
                    throw new SAPIException("Unexpected empty channel list");
                }
                double[] times = m.GetTimes(index);
                double time = times[0];
                mark = (int) time;
                System.out.println("mark = " + mark);
                byte [][] data= m.GetDataAsByteArray(index);
                byte [] playback = data[0];
                totalBufferLength += playback.length;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        // all done
        if (bufferCount == 0)
            System.out.println("No Buffers received");
        else
        {
            double averageBufferLength = ((double)totalBufferLength)/bufferCount;
            System.out.println("Average buffer length = " + (averageBufferLength));
        }
        if (sink != null) sink.CloseRBNBConnection();
        sinkDone = true;
        System.out.println("Sink Thread Finished.");
    }
}
