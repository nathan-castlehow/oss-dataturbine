/*
Copyright 2007 Creare Inc.

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at 

http://www.apache.org/licenses/LICENSE-2.0 

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License.
*/

package com.rbnb.api;

/**
 * Time handling class for RBNB applications.
 * <p>
 * This class contains methods for manipulating time information within the
 * RBNB framework. These are helper methods for users of the RBNB API and for
 * implementors of the DataTurbine.
 * <p>
 *
 * @author Ian Brown
 *
 * @since V2.0
 * @version 07/21/2004
 */

/*
 * Copyright 2004 Creare Inc.
 * All Rights Reserved
 *
 *
 *   Date      By	Description
 * MM/DD/YYYY
 * ----------  --	-----------
 * 07/21/2004  INB	Changed zzz in SimpleDateFormat to z.
 * 01/11/2001  INB	Created.
 *
 *
 */
public final class Time {

    /**
     * date formatter for displaying dates.
     * <p>
     *
     * @author Ian Brown
     *
     * @since V2.0
     * @version 07/21/2004
     */
    /*private final static java.text.SimpleDateFormat formatter =
	new java.text.SimpleDateFormat
	("dd-MMM-yyyy z HH:mm:ss.SSS");*/

    /**
     * Gets an RBNB time value (double) representing the current time.
     * <p>
     *
     * @author Ian Brown
     *
     * @return the current time of day.
     * @since V2.0
     * @version 11/06/2002
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/11/2001  INB	Created.
     *
     */
    public final static double now() {
	return (System.currentTimeMillis()/1000.);
    }

    /**
     *
     * Converts the input RBNB time value (double) to a string based on the
     * fact that the input represents a number of seconds since January 1st,
     * 1970.
     * <p>
     * The output is a string of the form:
     * <p>
     * DD-MMM-YYYY TMZ HH:MM:SS.sss
     * <p>
     * This method assumes that it should use the system's idea of time
     * zone. The input value is assumed to be in UTC.
     * <p>
     *
     * @author Ian Brown
     *
     * @param timeI  the time.
     * @return the string representation.
     * @since V2.0
     * @version 05/10/2001
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 01/11/2001  INB	Created.
     *
     */
    public final static String since1970(double timeI) {

	// Convert the time to a Java <code>java.util.Date</code>. This will
	// lose any information that is less than 1 millisecond, so we also
	// grab that information for later.
	java.util.Date time = new java.util.Date
	    ((long) Math.floor(timeI*1000.));
	double submilliseconds = Math.abs(IEEEremainder(timeI,
							     .001)*
					  1000.);

	// Format the time.
	//String timeR = formatter.format(time);
	String timeR = dateFormat(time);

	// For now, we'll allow for nanoseconds.
	if (submilliseconds >= 1e-6) {

	    // Add additional digits.
	    String zeros = "";

	    for (int idx = 0; idx < 6; ++idx) {
		int value = (int) Math.floor(submilliseconds*10.);

		if (value == 0) {
		    zeros += "0";
		} else {
		    timeR += zeros + value;
		    zeros = "";
		}
	    }
	}

	return (timeR);
    }

    /**
     *
     * Converts the input string to a RBNB time (double).
     * <p>
     * The input is a string of the form:
     * <p>
     * DD-MMM-YYYY TMZ HH:MM:SS.sss
     * <p>
     * This method assumes that it should use the system's idea of time
     * zone. The input value is assumed to be in UTC.
     * <p>
     *
     * @author Eric Friets
     *
     * @param timeStringI  the time string.
     * @return the time as a double
     * @since V2.6
     * @version 04/20/2005
     */

    /*
     *
     *   Date      By	Description
     * MM/DD/YYYY
     * ----------  --	-----------
     * 04/20/2005  EMF	Created.
     *
     */
    public final static double fromFormattedString(String timeStringI) 
	throws Exception {

	//for now, ignore sub-millisecond values
	if (timeStringI.length()==24) timeStringI=timeStringI.concat(".000");
	else if (timeStringI.length()<28) timeStringI=timeStringI.concat("000");
	if (timeStringI.length()>28) timeStringI=timeStringI.substring(0,28);
	//java.util.Date date=formatter.parse(timeStringI);
	java.util.Date date=dateParse(timeStringI);
	return (date.getTime())/1000.0;
    }

	/* new functions to replace the Date class */
	private static String dateFormat(java.util.Date date)
	{
		return date.toString();
	}

	private static java.util.Date dateParse(String toParse)
	{
		com.rbnb.compat.StringTokenizer t = new com.rbnb.compat.StringTokenizer(toParse, " :.");
		String month = t.nextToken();
		int day = Integer.parseInt(t.nextToken());
		int year = Integer.parseInt(t.nextToken());
		String temp = null;
		int hour=0, min=0, sec=0;
		t.nextToken(); // burn the z
		hour = Integer.parseInt(t.nextToken());
		min = Integer.parseInt(t.nextToken());
		sec = Integer.parseInt(t.nextToken());
		int ms = Integer.parseInt(t.nextToken());
		int mon;
		if(month.equals("Jan"))
			mon = 1;
		else if(month.equals("Feb"))
			mon = 2;
		else if(month.equals("Mar"))
			mon = 3;
		else if(month.equals("Apr"))
			mon = 4;
		else if(month.equals("May"))
			mon = 5;
		else if(month.equals("Jun"))
			mon = 6;
		else if(month.equals("Jul"))
			mon = 7;
		else if(month.equals("Aug"))
			mon = 8;
		else if(month.equals("Sep"))
			mon = 9;
		else if(month.equals("Oct"))
			mon = 10;
		else if(month.equals("Nov"))
			mon = 11;
		else
			mon = 12;

		long yeardiff = year - 1970;
		long total = 0;
		total += yeardiff*365*24*60*60*1000;
		int leaps = (int)(yeardiff + 2)/4;
		total += leaps*24*60*60*1000;
		int nowleap;
		if( (yeardiff + 2)%4 == 0)
			nowleap = 1;
		else
			nowleap = 0;
		long[] daysdiff = {0, 31, 59+nowleap, 90+nowleap, 120+nowleap, 151+nowleap, 181+nowleap, 212+nowleap, 243+nowleap, 273+nowleap, 304+nowleap, 334+nowleap};
		total += daysdiff[mon-1]*24*60*60*1000;
		total += (day-1)*24*60*60*1000;
		total += hour*60*60*1000;
		total += sec*1000;
		total += min*60*1000+ms;

		return new java.util.Date(total);

	}

	private static double IEEEremainder(double x, double y)
	{
		double xy = x/y;
		double inf = 0;
		double nan = 0;
		if(x == inf)
			return nan;
		if(y == 0)
			return nan;
		if(y == inf)
			return x;
		return (x - y*xy);

	}
	
}
