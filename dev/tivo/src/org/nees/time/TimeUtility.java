package org.nees.time;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class TimeUtility {

    private static final SimpleDateFormat TIME_FORMAT =
        new SimpleDateFormat("MMM d, yyyy h:mm:ss.SSS aa");
    private static final TimeZone TZ = TimeZone.getTimeZone("GMT");
    static {
        TIME_FORMAT.setTimeZone(TZ);
    }

    public static String timeToString(double d) { // d: time in seconds
        return TIME_FORMAT.format(new Date((long) (d * 1000.0)));
    }
}
