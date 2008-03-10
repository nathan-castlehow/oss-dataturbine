/*
 * ArchiveSegmentInterface.java
 * Created May, 2005
 * 
 * COPYRIGHT © 2005, THE REGENTS OF THE UNIVERSITY OF MICHIGAN,
 * ALL RIGHTS RESERVED; see the file COPYRIGHT.txt in this folder for details
 * 
 * This work is supported in part by the George E. Brown, Jr. Network
 * for Earthquake Engineering Simulation (NEES) Program of the National
 * Science Foundation under Award Numbers CMS-0117853 and CMS-0402490.
 * 
 * CVS information...
 *   $Revision: 153 $
 *   $Date: 2007-09-24 13:10:37 -0700 (Mon, 24 Sep 2007) $
 *   $RCSfile: ArchiveSegmentInterface.java,v $ 
 * 
 */
package org.nees.tivo;

/**
 * The means by which an application can get information from a repository
 * on data sequences. Data sequences are aassumed to be stored as segments
 * which, in turn, are time-ordered sequences data items. Segments are named.
 * A segment "contains" a sequences of data times (ArchiveItemInterface) that
 * are ordered with respect to time.
 * 
 * @author Terry E Weymouth
 * @version $Revision: 153 $ (CVS Revision number)
 * @
 */
public interface ArchiveSegmentInterface extends Comparable {

    public final static String PROPERTY_KEY_Date_Created = "DateCreated";
    public final static String PROPERTY_KEY_Server = "SourceServer";
    public final static String PROPERTY_KEY_Channel = "SourceChannel";
        
    /** Set the name of the segment. Must be unique. As it is used to retrieve
     * the segment.
     */
    public void setName(String name);
    /** Get the name of the segment. */
    public String getName();
    
    /**
     * Retreive a property from this segment.
     * 
     * @param propertyKey the (String) key of the property to retreive.
     * 
     * @return the property value (String) associated with the propertyKey
     * 
     * @see java.util.Properties
     */
    public String getProperty(String propertyKey);

    /**
     * Add a property key-value pair to this segment.
     * 
     * @param propertyKey the (String) key of the property
     * @param propertyValue the (String) value of the property
     *
     * @see java.util.Properties
     */
    public void setProperty(String propertyKey, String propertyValue);

    /**
     * @param the RBNB style date-time stamp of the start time
     * of this archive segment representation (e.g number of seconds
     * since Midnight leading into January 1, 1970, as a double); assumed GMT.
     * 
     * @see #getStartTime()
     * @see #getStartTimeAsDouble()
     * @see #getDuration()
     * @see #getDurationAsDouble()
     * @see #getEndTime()
     * @see #getEndTimeAsDouble()
     */
    public void setStartTime(double theTime);

    /**
     * @return the unix-style date-time stamp for the start
     * of this archive segment representation (e.g number of
     * milliseconds since Midnight leading into January 1, 1970);
     * assumed GMT.
     *
     * @see #setStartTime() 
     * @see #getStartTimeAsDouble()
     * @see #getDuration()
     * @see #getDurationAsDouble()
     * @see #getEndTime()
     * @see #getEndTimeAsDouble()
     */
    public long getStartTime();

    /**
     * @return the RBNB style date-time stamp the start
     * of this archive segment representation (e.g number of seconds
     * since Midnight leading into January 1, 1970); assumed GMT.
     * 
     * @see #setStartTime() 
     * @see #getStartTime()
     * @see #getDuration()
     * @see #getDurationAsDouble()
     * @see #getEndTime()
     * @see #getEndTimeAsDouble()
     */
    public double getStartTimeAsDouble();

    /**
     * A point data item has a zero duration. Usually the case for data
     * samples and images.
     * 
     * @return the number of milliseconds in duration.
     * 
     * @see #setStartTime() 
     * @see #getStartTime()
     * @see #getStartTimeAsDouble()
     * @see #getDurationAsDouble()
     * @see #getEndTime()
     * @see #getEndTimeAsDouble()
     */
    public long getDuration();

    /**
     * @return the number of seconds in duration
     * 
     * @see #setStartTime() 
     * @see #getStartTime()
     * @see #getStartTimeAsDouble()
     * @see #getDuration()
     * @see #getEndTime()
     * @see #getEndTimeAsDouble()
     */
    public double getDurationAsDouble();

    /**
     * @return the unix-style date-time stamp for the end
     * of this archive segment representation (e.g number of
     * milliseconds since Midnight leading into January 1, 1970);
     * assumed GMT.
     *
     * @see #setStartTime()
     * @see #getStartTime()
     * @see #getStartTimeAsDouble()
     * @see #getDuration()
     * @see #getDurationAsDouble()
     * @see #getEndTimeAsDouble()
     */
    public long getEndTime();

    /**
     * @return the RBNB style date-time stamp of this for the end
     * of this archive segment representation (e.g number of seconds
     * since Midnight leading into January 1, 1970); assumed GMT. This
     * can be use when greater the millisecond accurecy is desired.
     * 
     * @see #setStartTime() 
     * @see #getStartTime()
     * @see #getStartTimeAsDouble()
     * @see #getDuration()
     * @see #getDurationAsDouble()
     * @see #getEndTime()
     */
    public double getEndTimeAsDouble();

    /**
     * @return an ordered array of the archive items in this segment that fall
     * between startTime and endTime, in the case that the items have duration
     * and span startTime, or endTime, those endpoints will be included. This means
     * that sucessive calls to getSortedArray in which a the startTime of the
     * second call is the endTime of the previous call could have the same
     * segment (equals) as the last item returned by the first call and the first item
     * of the second call.
     */
    public ArchiveItemInterface[] getSortedArray(long startTime, long endTime);

    /**
     * @return the first archive item whos entire span (startTime to startTime + duration)
     * is at or after the time given and which is included in or overlapping the interval
     * described by startTime and endTime, if such an item exists. Otherwise return null.
     * 
     * @see #getSortedArray
     */
    public ArchiveItemInterface getAtOrAfter(
        long time,
        long startTime,
        long endTime);

    /**
     * @return the first archive item whos entire span (startTime to startTime + duration)
     * is at or after the time given from among all those in the segment, if such
     * an item exists. Otherwise return null.
     */
    public ArchiveItemInterface getAtOrAfter(long time);

    /**
     * @return the first archive item whos entire span (startTime to startTime + duration)
     * is at or before the time given and which is included in or overlapping the interval
     * described by startTime and endTime, if such an item exists. Otherwise return null.
     * 
     * @see #getSortedArray
     */
    public ArchiveItemInterface getAtOrBefore(
        long time,
        long startTime,
        long endTime);

    /**
     * @return the first archive item whos entire span (startTime to startTime + duration)
     * is at or after the time given from among all those in the segment, if such
     * an item exists. Otherwise return null.
     */
    public ArchiveItemInterface getAtOrBefore(long time);

    /**
     * Required for the Comperable interface. Quoting Sun's documentaiton:
     * "Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is
     * less  than, equal to, or greater than the specified object."
     *  @return the value 0 if the argument is an ArchiveImageCover whos
     * time ordering is equivlent to this ArchiveImageCover;
     * a value less than 0 if the argument is an ArchiveImageCover whos
     * time ordering follows (is greater then) this ArchiveImageCover;
     * and a value greater than 0 if the argument is an ArchiveImageCover whos
     * time ordering preceeds (is less than) this this ArchiveImageCover. */
    public int compareTo(Object test);

    /**
     * The specific test for object of this type.
     * @param test
     * @return 0, less then zero, greater then zero. In accordance with the
     * java Comperable Interface
     *
     * @see #compareTo(Object) 
     * @see java.lang.Comperable
     */
    public int compareTo(ArchiveSegmentInterface test);

    /**
     * Objects of this interface are equals only when they represent the same segment and
     * if they are equals they must represent the same segment; in otherwords for any
     * two instances of a particular implementation of ArchiveSegmentInterface, s1 and s2,
     * s1.equals(s2) = s2.equals(s1), and s1.equals(s2) if and only if s1 and s2 represent
     * the same segment.
     * 
     * @param test the ArchiveSegmentInterface object to test
     * @return true if they represent the same underlying segment
     */
    public boolean equals(ArchiveSegmentInterface test);
}
