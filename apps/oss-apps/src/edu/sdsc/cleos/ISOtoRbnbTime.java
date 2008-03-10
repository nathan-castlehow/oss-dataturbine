/*!
@file ISOtoRbnbTime.java
@brief Convert DAQ timestamps into RBNB format, thus preserving sample times.
@author Paul Hubbard 
@date Tue Aug 12 2003.
@note Based on code from Chuck Severance, extensively cleaned up by Terry Weymouth
@version CVS:$Revision:129M $
@note $HeadURL:http://nladr-cvs.sdsc.edu/svn-private/NEON/telepresence/dataturbine/trunk/src/edu/sdsc/cleos/ISOtoRbnbTime.java $
@note $LastChangedRevision:129M $
@author $LastChangedBy:(local) $
@date $LastChangedDate:2007-03-16 15:30:24 -0700 (Fri, 16 Mar 2007) $
*/

package edu.sdsc.cleos;

import java.util.*;
import java.util.logging.Logger;

//! Convert DAQ timestamps into RBNB format, thus preserving sample times.
//! @note uses ISO8601 timestamp, e.g. 2003-08-12T19:21:22.30095
// Warning: timestamp not valid: 2004-07-22T13:41:15
public class ISOtoRbnbTime {
	public static boolean DEBUG = false;
    private int year;
    private int month;
    private int day;
    private int hour;
    private int min;
    private int sec;
    private double frac;
    private String ISOString;
    private double convertedTStamp;
    public boolean is_valid;
    private static Logger logger = Logger.getLogger(ISOtoRbnbTime.class.getName());

    /*
     @brief Constructor, converts input to internal time
     @param ISOString ISO8601 timestamp, e.g. 2003-08-12T19:21:22.30095
    */
    public ISOtoRbnbTime(String ISOString)
    {
        is_valid = false;
        this.ISOString = ISOString;
        convert(ISOString);
    }
    
    /*! 
        @brief Internal do-the-work function
        @note Input is ISO8601 timestamp, e.g. 2003-08-12T19:21:22.30095 
        @param ISOString ISO8601 timestamp, e.g. 2003-08-12T19:21:22.30095
    */
    private void convert(String ISOString) {
	
        StringTokenizer st = new StringTokenizer(ISOString, "T-:\t\n. ");
	try
	{
	    year = Integer.parseInt(st.nextToken());
	    month = Integer.parseInt(st.nextToken());
	    day = Integer.parseInt(st.nextToken());
	    hour = Integer.parseInt(st.nextToken());
	    min = Integer.parseInt(st.nextToken());
	    sec = Integer.parseInt(st.nextToken());
	    is_valid = true;
	} catch (Exception ignore) {}
	
	// fractional part is optional; to accommodate alternate format
	frac = 0.0;
	try
	{
		/*! @note chop off the Z
		  * @todo refactor this using general regexes */
		String fracString = st.nextToken();
	    frac = Double.parseDouble("." + fracString.substring(0, fracString.length()-2));
	} catch (Exception e) {
		logger.fine("parsing fractional part of:" + ISOString);
		//e.printStackTrace();
	}

	// Java time starts from year 1900, so subtract that offset
	if (year > 1900) 
	    year -= 1900;

	// Months are indexed from zero, too. Stoopid. (Jan is month zero).
	month -= 1;

	convertedTStamp = (double)(Date.UTC(year,month,day,hour,min,sec)/1000.0);  // convert to second
	convertedTStamp += frac;  // add fraction of second
	if (DEBUG) 
	    debugPrint();
    }
    
    /*
     @brief Return pre-computed result
     @retval time RBNB-format timestamp, double, seconds since the epoc
    */
    public double getValue() {
        return(convertedTStamp);
    }
    
    public void debugPrint()
    {
    	logger.finer("Debug print of ISOString " + ISOString + "\n" +
    			"   Parsed into " + year + "." + month + "." + day + "." + hour + "." + min + "." + sec + "." + frac + "\n" +
    			"   Converted timestamp = " + convertedTStamp + "\n" +
    			"   Confirm Date/Time: " + (formatDate((long) (convertedTStamp*1000.))) );
    	//logger.finer("Current time: " + System.currentTimeMillis());
    }
    
/** @brief formats milis as as human-readable date string */
    public static String formatDate (long milis) {
    	java.text.SimpleDateFormat format = new 
    	java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    	Date date = new Date(milis);
    	return format.format(date);
    }
    
    /** @brief formats milis as as human-readable date string */
    public static String formatDateForDB (long milis) {
    	java.text.SimpleDateFormat format = new 
    	java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	Date date = new Date(milis);
    	return format.format(date);
    }

}

