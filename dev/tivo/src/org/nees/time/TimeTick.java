package org.nees.time;

import java.awt.Color;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * A utility for computing the positions for tickmarks on a time scale.
 * @author Terry Weymouth
 */
public class TimeTick {

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance();
    static {
        NUMBER_FORMAT.setMaximumFractionDigits(4);
        NUMBER_FORMAT.setMinimumFractionDigits(1);
    }
    
    private static final SimpleDateFormat DAY_FORMAT =
        new SimpleDateFormat("MMM d, yyyy");
    private static final SimpleDateFormat HOUR_FORMAT =
        new SimpleDateFormat("hh:mm");
    private static final SimpleDateFormat MIN_FORMAT =
        new SimpleDateFormat(":mm");
    private static final SimpleDateFormat SEC_FORMAT =
        new SimpleDateFormat(":ss");
    private static final SimpleDateFormat PART_SEC_FORMAT =
        new SimpleDateFormat(".SSS");
    private static final TimeZone TZ = TimeZone.getTimeZone("GMT");
    static {
        DAY_FORMAT.setTimeZone(TZ);
        HOUR_FORMAT.setTimeZone(TZ);
        MIN_FORMAT.setTimeZone(TZ);
        SEC_FORMAT.setTimeZone(TZ);
        PART_SEC_FORMAT.setTimeZone(TZ);
    }

    protected static final double aSmallNumber = 1e-6;
    protected static final double log10 = Math.log(10);

    private double min;
    private double max;
    private double step;
    private int count;
    private int partIndex = -1;

    protected final int NORMAL_WIDTH = 0;

    protected final double minute = 60.0;
    protected final double hour = minute * 60.0;    
    protected final double day = hour * 24.0;
    
    private final String[] partNames = { // for debugging
            "day", "half day", "6 hour", "3 hour", 
            "hour", "half hour", "20 min", "qurter hour", "10 min", "5 min", "2 min", 
            "min", "30 sec", "20 sec", "15 sec", "10 sec", "5 sec", "2 sec", "sec"
    };
    
    private final double[] parts = { // from largest to smallest
        day , // day
        hour * 12.0, // half day
        hour * 6.0, // quarter day
        hour * 3.0, // 3 hours
        hour, // hour
        minute * 30.0, // 1/2 hour
        minute * 20.0, // 1/3 hour
        minute * 15.0, // 1/4 hour
        minute * 10.0, // 10 min
        minute * 5.0, // 5 min
        minute * 2.0, // 2 min
        minute, // one min
        30.0, 20.0, 15.0, 10.0, 5.0, 2.0, // parts of a min
        1.0 // seconds
        };

    /**
     * Construct a new set of scaling factors for tick marks on a time scale.
     * The times, min and max, are assumed to be in seconds since the epoch.
     * 
     * @param min - the desired min value (seconds)
     * @param max - the desired max value (seconds)
     * @param averageCnt - the desired number of tickmarks
     */
    protected TimeTick(double minVal, double maxVal, int minX, int maxX, int minStep) {
        set(minVal, maxVal, minX, maxX, minStep);
    }

    protected TimeTick() {}
    
    protected void set(double minVal, double maxVal, int minX, int maxX, int minXStep) {
        double range = maxVal - minVal;
        int rangeX = maxX - minX;
        int approxCount = rangeX/minXStep;
        
        if ((range == 0.0) || (rangeX == 0))
        {
            setValues (minVal, maxVal, 0, -1);
            return;
        }
        
        // To determine the basic unit for division of the time range into
        // steps find the step that results in the step of X is larger the smallest
        // possilbe that is larger then minStep;

        // if the range is larger then the largest part... e.g. a day...
        if (range > parts[0]) // start the search in multiple days...
        {
            // use "standard" ticks for sort cut
            TickMark tm = new TickMark(0.0, range/day, approxCount);
            if (tm.step >= 1.0){
                // day(s) work...
                setValues(minVal, maxVal, tm.step * day, -1);
                return;
            }
        }
        
        // search from largest to smallest for the "step" size that the
        // first one under the minStep size and back off by one
        int testXStep;
        for (int i = 0; i < parts.length; i ++) {
           testXStep = (int)(((double)rangeX)*(parts[i]/range));
           if (testXStep <= minXStep) // got it
           {
               setValues(minVal, maxVal, parts[i-1], i-1);
               return;
           }
        }
        
        // the "ideal" tick is a second or less - use standard ticks
        TickMark tm = new TickMark(0.0, range, approxCount);
        setValues(minVal, maxVal, tm.step, parts.length);
    }

    private void setValues (double sMin, double sMax, double sStep, int sIndex)
    {
        partIndex = sIndex;
        step = sStep;
        min = Math.floor(sMin / step) * step;
        max = Math.ceil(sMax / step) * step;
        count = 1 + (int)Math.round((max - min) / step);
//        System.out.println("Range : " + NUMBER_FORMAT.format(max-min));
//        System.out.println("Step: " + NUMBER_FORMAT.format(getStep()));
//        System.out.println("Count: " + getCount());
//        System.out.println("PartName: " + getPartName());
    }

    protected double getMin() { return min; }
    protected double getMax() { return max; }
    protected int getCount() { return count; }
    protected double getStep() { return step; }

    protected String getPartName()
    {
        if (step == 0.0)
            return "Warning: Zero Step!!";
        
        if (partIndex < 0) // one or more days
            return NUMBER_FORMAT.format(step/day) + " day(s)";
        if (partIndex >= parts.length) // less the one second
            return NUMBER_FORMAT.format(step) + " of a sec";            

        // between one day and one second
        return partNames[partIndex];
    }

    private static int iSecond = 1;
    private static int iMinute = iSecond * 60;
    private static int iHour = iMinute * 60;
    private static int iDay = iHour * 24;
    
    private static int[] tickTypeTimes = { // in decreasing order
        iDay, iHour * 12, iHour * 6, iHour, 
        iMinute * 30, iMinute * 15, iMinute, 
        iSecond * 30, iSecond 
    };
    
    private static long[] tickTypeTests = new long[tickTypeTimes.length];
    
    static {
        for (int i = 0; i < tickTypeTimes.length; i++){
            tickTypeTests[i] = 1000 * (long)tickTypeTimes[i];
        }
    }

    private static int[] tickTypeHeights = {
        15, 15, 8, 8, 6, 6, 6, 4, 4, 2
    };
    private static boolean[] tickTypeDoubleFlags = {
        true, true, true, true, 
        false, false, true, 
        false, true, false
    };
    private static Color[] tickTypeColors = {
        Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK, 
        Color.DARK_GRAY, Color.DARK_GRAY, Color.DARK_GRAY, 
        Color.BLUE, Color.BLUE, Color.RED
    };

    private static SimpleDateFormat[] tickTypeFomats = {
        DAY_FORMAT, HOUR_FORMAT, HOUR_FORMAT, HOUR_FORMAT, 
        MIN_FORMAT, MIN_FORMAT, MIN_FORMAT, 
        SEC_FORMAT, SEC_FORMAT, PART_SEC_FORMAT
    };
    
    public static int tickTypeForTime(double d) {
        long probe = (long)(d * 1000.0);
//System.out.println("Probe: " + probe + ", d: " + d);
        for (int i = 0; i < tickTypeTests.length; i++){
            if (probe%tickTypeTests[i] == 0)
                return i;
        }
        return tickTypeTimes.length;
    }

    public static int tickHeightForType(int type) {
        if (type < 0) return 0;
        if (type >= tickTypeHeights.length) return 0;
        return tickTypeHeights[type];
    }
    
    public static boolean tickDoubleFlagForType(int type) {
        if (type < 0) return false;
        if (type >= tickTypeHeights.length) return false;
        return tickTypeDoubleFlags[type];
    }
    

    public static Color tickColorForType(int type) {
        if (type < 0) return Color.BLACK;
        if (type >= tickTypeColors.length) return Color.BLACK;
        return tickTypeColors[type];
    }

    public static String tickLabelForTime(int type, double d) {
        if (type < 0) type = 0;
        if (type >= tickTypeFomats.length) type = tickTypeFomats.length - 1;
        return 
            tickTypeFomats[type].format(new Date((long)(d * 1000.0)));
    }

}
