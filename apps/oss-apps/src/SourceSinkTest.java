
import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Sink;
import com.rbnb.sapi.Source;

public class SourceSinkTest {

    private static final String SOURCE_NAME = "SSTestSource";
    private static final String SOURCE_CHANNEL = "data";
    private static final String SINK_NAME = "SSTestSink";
    private static final String SERVER_NAME = "dev-neestpm.sdsc.edu";
//    private static final String SERVER_NAME = "localhost";
    private static final int NUBMER_OF_HITS = 100;
    
    private static final int FRAME_SIZE=4; // stereo, 16 bit sample
    private static final float ORIGINAL_SAMPLE_RATE = 44100.0f;
    private static final int AUDIO_BUFFER_SIZE = 44100;  // one quarter sec audio
    
// for checking the numbers!
//    private static final int FRAME_SIZE = 4; 
//    private static final float ORIGINAL_SAMPLE_RATE = 10*1000.0f;
//    private static final int AUDIO_BUFFER_SIZE = 10*1000;  // one quarter sec audio
    
    private static final int MAX_BUFFER_MULTIPLE = 10;
    
    private static int testBufferSize = 0;
    private boolean sinkDone = false;
    private boolean sourceDone = false;
    
    public static void main(String[] args) {
        
        String serverNameAndPort = SERVER_NAME;
        if (args.length > 0) serverNameAndPort = args[0];
        if (!serverNameAndPort.contains(":")) serverNameAndPort += ":3333";
        (new SourceSinkTest()).exec(serverNameAndPort);
    }

    private void exec(String server)
    {
        final String s = server;

        System.out.println("Start all tests (" + MAX_BUFFER_MULTIPLE + ")");
        int limit = MAX_BUFFER_MULTIPLE + 1;
        for (int multiple = 1; multiple < limit; multiple++)
        {
            testBufferSize = multiple * AUDIO_BUFFER_SIZE;
            printTestHeader(multiple);
            Thread sourceThread = new Thread(){
                public void run (){ execSource(s);}
            };
            Thread sinkThread = new Thread(){
                public void run (){ execSink(s);}
            };
            sinkDone = false;
            sourceDone = false;
            sourceThread.start();
            sinkThread.start();
            // wait for source to finish (which waits for sink to finish)
            while(!sourceDone)
            {
                try {Thread.sleep(100);} catch (Exception ignore) {}
            }
            printTestFooter(multiple);
        }
        System.out.println("All tests done (" + MAX_BUFFER_MULTIPLE + ")");
    }
    
    private void printTestHeader(int multiple) {
        System.out.println(
                "************" 
                + " Start test with buffer multiplier = " + multiple + " " +
                "************" 
                );
    }

    private void printTestFooter(int multiple) {
        System.out.println(
                "************" 
                + " End test with buffer multiplier = " + multiple + " " +
                "************" 
                );
    }

    private void execSource(String server)
    {
        Source source = null;
        long totalBufferLength = 0;
        int bufferCount = 0;
        long time = 0;
        try {

            System.out.println("Source Thread Started.");
        
            // connect
            source = new Source(NUBMER_OF_HITS, "none", 0);
            source.OpenRBNBConnection(server, SOURCE_NAME);

            System.out.println("Connection made to server = "
                    + server + " as " + SOURCE_NAME + " with " + SOURCE_CHANNEL
                    + ".");
        
            // send data
            ChannelMap cm = new ChannelMap();
            cm.Add(SOURCE_CHANNEL);

            // make sure the sink gets a change to start and connect
            try {Thread.sleep(5000);} catch (Exception ignore){}

            byte[] data = new byte[testBufferSize];

            long startTime = System.currentTimeMillis();

            for (int i = 0; i < NUBMER_OF_HITS; i++)
            {
//                System.out.println("Sending " + i);
                // send the data
                double rbnbTime = i;
                cm.PutTime(rbnbTime,0.0);
                cm.PutDataAsByteArray(0,data);
                totalBufferLength += data.length;
                bufferCount++;
                source.Flush(cm);
            }
            time = System.currentTimeMillis() - startTime;

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

        double targetBufferTime = testBufferSize / (FRAME_SIZE * ORIGINAL_SAMPLE_RATE);
        double targetRate = ORIGINAL_SAMPLE_RATE;

        double dtime = ((double)time)/1000.0; // time in seconds
        double averageBufferLength = ((double)totalBufferLength)/
            (double)(bufferCount); // bytes/buffer
        double averageBufferTime = dtime/bufferCount; // sec/buffer
        double byteRate = averageBufferLength/averageBufferTime; // bytes/sec
        double kByteRate = byteRate/1000.0; // KBytes/sec
        double sampleRate = byteRate/((double)FRAME_SIZE); // samples/sec 
        System.out.println("Source: Buffer count = " + bufferCount +
                ", total time (ms) = " + time + ", total bytes send = " + totalBufferLength);
        System.out.println("Source: Average buffer length (KB) " + averageBufferLength);
        System.out.println("Source: Average rate (KB/sec) " + kByteRate);
        System.out.println("Source: Target buffer time " + targetBufferTime);
        System.out.println("Source: Average time (sec/buffer) " + averageBufferTime);
        System.out.println("Source: Desired sample rate (Hz) " + targetRate);
        System.out.println("Source: Estimated sample rate (Hz) " + sampleRate);

        //disconnect
        if (source != null) source.CloseRBNBConnection();
        
        System.out.println("Source Thread Finished.");
        sourceDone = true;
    }
    
    private void execSink(String server)
    {
        Sink sink = null;
        long totalBufferLength = 0;
        int bufferCount = 0;
        long time = 0;
        
        try 
        {
            System.out.println("Sink Thread Started.");
            
            // make sure the source gets a change to start
            try {Thread.sleep(5000);} catch (Exception ignore){}

            String requestPath = SOURCE_NAME + "/" + SOURCE_CHANNEL;
            sink=new Sink();
            sink.OpenRBNBConnection(server,SINK_NAME);
            ChannelMap cm = new ChannelMap();
            int index = cm.Add(requestPath);
            sink.Subscribe(cm,"newest");
            System.out.println("Connection made to server = "
                + server + " as " + SINK_NAME 
                + " requesting " + requestPath + ".");
            
            int mark = 0;
            long startTime = System.currentTimeMillis();
            while (mark < (NUBMER_OF_HITS -1))
            {
                ChannelMap m = sink.Fetch(-1);
                if (m.GetChannelList().length < 1)
                {
                    // we're hosed
                    throw new SAPIException("Unexpected empty channel list");
                }
                double[] times = m.GetTimes(index);
                mark = (int) times[0];
//                System.out.println("mark = " + mark);
                byte [][] data= m.GetDataAsByteArray(index);
                byte [] playback = data[0];
                totalBufferLength += playback.length;
                bufferCount++;
            }
            time = System.currentTimeMillis() - startTime;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        // all done
        if (bufferCount == 0 || time == 0)
            System.out.println("No Buffers received");
        else
        {
            double targetBufferTime = testBufferSize / (FRAME_SIZE * ORIGINAL_SAMPLE_RATE);
            double targetRate = ORIGINAL_SAMPLE_RATE;

            double dtime = ((double)time)/1000.0; // time in seconds
            double averageBufferLength = ((double)totalBufferLength)/
                (double)(bufferCount); // bytes/buffer
            double averageBufferTime = dtime/bufferCount; // sec/buffer
            double byteRate = averageBufferLength/averageBufferTime; // bytes/sec
            double kByteRate = byteRate/1000.0; // KBytes/sec
            double sampleRate = byteRate/((double)FRAME_SIZE); // samples/sec 
            System.out.println("Sink: Buffer count = " + bufferCount +
                    ", total time (ms) = " + time + ", total bytes send = " + totalBufferLength);
            System.out.println("Sink: Average buffer length (KB) " + averageBufferLength);
            System.out.println("Sink: Average rate (KB/sec) " + kByteRate);
            System.out.println("Sink: Target buffer time " + targetBufferTime);
            System.out.println("Sink: Average time (sec/buffer) " + averageBufferTime);
            System.out.println("Sink: Desired sample rate (Hz) " + targetRate);
            System.out.println("Sink: Estimated sample rate (Hz) " + sampleRate);
            
        }
        if (sink != null) sink.CloseRBNBConnection();
        sinkDone = true;
        System.out.println("Sink Thread Finished.");
    }
}
