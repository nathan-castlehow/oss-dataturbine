/*
 * TestArchiveSource.java
 * Created on Aug 15, 2005
 * 
 * This work is supported in part by the George E. Brown, Jr. Network
 * for Earthquake Engineering Simulation (NEES) Program of the National
 * Science Foundation under Award Numbers CMS-0117853 and CMS-0402490.
 */

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.rbnb.sapi.*;

/**
 * @author Terry E. Weymouth
 */
public class ArchiveSource {

    private static final SimpleDateFormat T_FORMAT = new SimpleDateFormat("yyyy-MM-dd:HH:mm:ss.SSS");
    private static final TimeZone TZ = TimeZone.getTimeZone("GMT");
    static
    {
        T_FORMAT.setTimeZone(TZ);
    }

    private static final String serverName = "localhost:3333";
    private static final String sourceName = "TestArchiveSource";
    private static final String sourceChannel = "data.txt";
    public static final String channelPath = sourceName + "/" + sourceChannel;
    
    private int cacheSize = 100;
    private String archiveType = "append";
    private int archiveSize = 1000;

    private int blockCount = 100;
    private int itemCount = 100;
    private long interval = 500; // in milliseconds
    
    private Source source;
    
    public static void main(String[] args) {
        (new ArchiveSource()).exec();
    }
    
    private void exec()
    {
        try
        {
            System.out.println("Starting data to archive run.");

            long now = System.currentTimeMillis();
            now = 1000 * (now/1000); // truncate to seconds
            long start = now;
            System.out.println("Start Time = " + T_FORMAT.format(new Date(now)));

            source = new Source(cacheSize,archiveType,archiveSize);
            source.OpenRBNBConnection(serverName,sourceName);
            System.out.println("TestPush: Connected to server = "
                + serverName + " as source = " + sourceName + ".");
            
            ChannelMap map = new ChannelMap();
            int index = map.Add(sourceChannel);
            double value = 0.0;
                    
            for (int block = 0; block < blockCount; block++)
            {
                // Push data onto the server:
                for (int i = 0; i < itemCount; i++)
                {
                    double theTime = ((double)now)/1000.0;
                    now += interval;
    
                    // put time
                    map.PutTime(theTime,0.0);
                    // put data on channel
                    double data[] = new double[1];
                    data[0] = Math.sin(value);
                    value += Math.PI/50.0;
                    map.PutDataAsFloat64(index,data);
                }
                source.Flush(map);
                // try {Thread.sleep(10);} catch (Exception ignore) {}
               // System.out.println("Blip " + block);
            }
            now -= interval;
            
            source.Detach();
            System.out.println("End Time = " + T_FORMAT.format(new Date(now)));
            double total = ((double)(now - start))/1000.0;
            System.out.println("Total time (seconds) = " + total); 
            System.out.println("End of data to archive run.");
        } catch (SAPIException se) {
            se.printStackTrace();
        } // try
    } // exec
} // class

