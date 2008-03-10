/*
 * ArchiveSink.java
 * Created on Aug 15, 2005
 * 
 * This work is supported in part by the George E. Brown, Jr. Network
 * for Earthquake Engineering Simulation (NEES) Program of the National
 * Science Foundation under Award Numbers CMS-0117853 and CMS-0402490.
 */

import com.rbnb.sapi.*;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.TimeZone;

/**
 * @author Terry E. Weymouth
 */
public class ArchiveSink {

    private static final SimpleDateFormat T_FORMAT = new SimpleDateFormat("yyyy-MM-dd:HH:mm:ss.SSS");
    private static final TimeZone TZ = TimeZone.getTimeZone("GMT");
    static
    {
        T_FORMAT.setTimeZone(TZ);
    }

    private static final String sinkName = "ArchiveSink";
    private String sourcePath = ArchiveSource.channelPath;    
    
    private String serverName = "localhost:3333";
    private int timeout = 20000;

    private Sink sink;
    
    public static void main(String[] args) {
        (new ArchiveSink()).exec();
    }
    
    private void exec()
    {
        try {
            sink = new Sink();
            sink.OpenRBNBConnection(serverName,sinkName);

            System.out.println("ArchiveSink: Connected to server = "
                    + serverName + " as sink = " + sinkName + ".");
            System.out.println("sourcePath: "+sourcePath);
            
            ChannelMap sMap = new ChannelMap();
            sMap.Add(sourcePath);
            sink.Request(sMap, 0.0, 0.0, "oldest");
            long systime = System.currentTimeMillis();
            ChannelMap rMap = sink.Fetch(timeout,sMap);
            System.out.println("First Fetch took " 
                    + (System.currentTimeMillis() - systime) + " milliseconds"); 
            if (rMap.GetIfFetchTimedOut())
                throw new SAPIException("Timeout: Can not get start time.");
            int index = rMap.GetIndex(sourcePath);
            if (index <0) 
                throw new SAPIException("Index: Can not get start time.");
            double startTime = rMap.GetTimeStart(index);

            sMap = new ChannelMap();
            sMap.Add(sourcePath);
            sink.Request(sMap, 0.0, 0.0, "newest");
            systime = System.currentTimeMillis();
            rMap = sink.Fetch(timeout,sMap);
            System.out.println("Second Fetch took " 
                    + (System.currentTimeMillis() - systime) + " milliseconds");
            if (rMap.GetIfFetchTimedOut())
                throw new SAPIException("Timeout: Can not get end time.");
            index = rMap.GetIndex(sourcePath);
            if (index <0) 
                throw new SAPIException("Index: Can not get end time.");
            double endTime = rMap.GetTimeStart(index) + rMap.GetTimeDuration(index);

            startTime -= 1.0;
            endTime += 1.0;
            double duration = endTime = startTime;

            System.out.println("Attempting fetch for...");
            System.out.println("  Start time = " + 
                    T_FORMAT.format(new Date((long)(startTime * 1000.0))));
            System.out.println("  End time = " + 
                    T_FORMAT.format(new Date((long)(endTime * 1000.0))));
                    
            ChannelMap cm = new ChannelMap();
            cm.Add(sourcePath);
            sink.Request(cm,startTime,duration,"absolute");
            
            ChannelMap req = sink.Fetch(timeout,cm);
            if (req.GetIfFetchTimedOut())
                throw new Exception("Timeout: no data");
            
            index = req.GetIndex(sourcePath);
            
            if (index < 0)
                throw new Exception("Index: no data for");
            
            double times[] = req.GetTimes(index);
            double data[] =req.GetDataAsFloat64(index);
            
            System.out.println("Fetch returned " + times.length + " records");
            
            int limit = times.length;
 
            if (times.length != data.length)
            {
                System.out.println("WARNING times and data arrays are not the same length.");
                System.out.println("  times.length = " + times.length +
                    "; and data.length = " + data.length);
                limit = Math.min(times.length,data.length);
                System.out.println("  using length = " + limit);
            }
            
            
            DecimalFormat df1 = (DecimalFormat)NumberFormat.getIntegerInstance();
            df1.applyPattern("####0");
            
            DecimalFormat df2 = (DecimalFormat)NumberFormat.getInstance();
            df2.applyPattern("0.0000");
            
            if(limit > 10) limit = 10;  // mjm, no spam
            for (int i = 0; i < limit; i++)
            {
                System.out.println(df1.format(i) + ": " + df2.format(data[i])
                    + "(" +  T_FORMAT.format(new Date((long)(times[i] * 1000.0)))
                    + ")");
            }
            sink.CloseRBNBConnection();
        } catch (Exception se) {
            se.printStackTrace();
        } // try
    } // exec
} // class
