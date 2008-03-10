package org.nees.rbnb;
/** a class that provides utility methods to interconvert between rbnb
  * frames and other parameters of interest
  * @author Lawrence J. Miller <ljmiler@sdsc.edu>
  * @since 060519
  */

public final class RBNBFrameUtility {

/** a method that will
  * @return the number of rbnb frames
  * that will correspond to the
  * @param time desired to be spanned (in days) by these frames given
  * @param flush rate of a ChannelMap (in Hz)
  */
public static int getFrameCountFromTime (double hours, double flushRate) {
   double seconds = hours * 60.0 * 60.0;
   double frames = seconds * flushRate;
   return (int)frames;
} // getFrameCountFromTime ()


public static int getFrameCountFromTime (double hours, int flushRate) {
   double seconds = hours * 60.0 * 60.0;
   double frames = seconds * (double)flushRate;
   return (int)frames;
} // getFrameCountFromTime ()



/** left "CVS" in name for legacy compatibility */
public static String getCVSVersionString ()
{
    return (
    "$LastChangedDate: 2007-09-24 13:10:37 -0700 (Mon, 24 Sep 2007) $\n" +
    "$LastChangedRevision: 153 $" +
    "$LastChangedBy: ljmiller $" +
    "$HeadURL: file:///Users/hubbard/code/cleos-svn/cleos-rbnb-apps/trunk/src/org/nees/rbnb/RBNBFrameUtility.java $"
    );
} // getCVSVersionString ()
} // class