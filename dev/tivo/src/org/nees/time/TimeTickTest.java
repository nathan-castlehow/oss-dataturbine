package org.nees.time;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class TimeTickTest {

    private static final SimpleDateFormat TIME_FORMAT =
        new SimpleDateFormat("MMM d, yyyy h:mm:ss.SSS aa");
    private static final TimeZone TZ = TimeZone.getTimeZone("GMT");
    static {
        TIME_FORMAT.setTimeZone(TZ);
    }

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance();
    static {
        NUMBER_FORMAT.setMaximumFractionDigits(4);
        NUMBER_FORMAT.setMinimumFractionDigits(1);
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        (new TimeTickTest()).exec();
    }

    public void exec()
    {
        long unixTime = System.currentTimeMillis(); // current time...
        double startTime = ((double)unixTime) / 1000.0; // in seconds...
        startTime = Math.floor(startTime / (60.0 * 60.0)) * 60.0 * 60.0; // to nearest hour

        TimeTick tt = new TimeTick();
        double endTime = startTime;
    
        System.out.println("Start.");

        for (int i = 1; i < 121; i ++)
        {
            
            endTime = startTime + ((double)i) * tt.minute;
     
            int minX = 100;
            int maxX = 1100;

            System.out.println("=====");

            tt.set(startTime, endTime, minX, maxX, 100);
            
            System.out.println("Start Time: " + TIME_FORMAT.format(new Date((long)(startTime * 1000.0))));
            System.out.println("End Time: " + TIME_FORMAT.format(new Date((long)(endTime * 1000.0))));
            System.out.println("Range : " + NUMBER_FORMAT.format(endTime-startTime));
            System.out.println("Step: " + NUMBER_FORMAT.format(tt.getStep()));
            System.out.println("Count: " + tt.getCount());
            System.out.println("PartName: " + tt.getPartName());
            System.out.println("Product: " + NUMBER_FORMAT.format(tt.getCount() * tt.getStep()));
        }
        System.out.println("=====");
        System.out.println("Done.");
    }
}
