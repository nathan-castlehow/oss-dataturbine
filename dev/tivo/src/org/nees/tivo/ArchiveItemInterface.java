/*
 * ArchiveItemInterface.java
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
 *   $RCSfile: ArchiveItemInterface.java,v $ 
 * 
 */
package org.nees.tivo;

/**
 * @author Terry E Weymouth
 * @version $Revision: 153 $ (CVS Revision number)
 */
public interface ArchiveItemInterface extends Comparable {

    /** 
     * @return the MIME type of this data item. This is a read-only field, presumed
     * to be set my the underlying implementation
     */
    public String getMime();

    /**
     * Required for the Comperable interface. Quoting Sun's documentaiton:
     * "Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is
     * less  than, equal to, or greater than the specified object."
     * 
     * @param Object must be an implementation of ArchiveItemInterface
     * 
     * @return the value 0 if the argument is an ArchiveItemInterface whos
     * time ordering is equivlent to this ArchiveItemInterface;
     * a value less than 0 if the argument is an ArchiveItemInterface whos
     * time ordering follows (is greater then) this ArchiveItemInterface;
     * and a value greater than 0 if the argument is an ArchiveItemInterface whos
     * time ordering preceeds (is less than) this this ArchiveItemInterface.
     * 
     * It is recommended that each implementer also implement a type specific
     * interface: compareTo(MyItemData) etc.
     * 
     * @see java.lang.Comperable
     * 
     */
    public int compareTo(Object test);

    /**
     * Two ArchiveItemInterface representation are equals if they represent
     * the same underlying data item.
     * @param test the ArchiveItemInterface object to compare
     * @return true if these two instances of the interface represent the same
     * underlying object.
     */
    public boolean equals(ArchiveItemInterface test);

    /**
     * @return the unix-style date-time stamp of this archive
     * Item representation (e.g number of milliseconds since
     * Midnight leading into January 1, 1970); assumed GMT.
     * 
     * @see #getTimeAsDouble()
     * @see #getDuration()
     * @see #getDurationAsDouble()
     */
    public long getTime();

    /**
     * @return the RBNB style date-time stamp of this archive
     * object (e.g number of seconds since Midnight leading
     * into January 1, 1970); assumed GMT. This can be use
     * when greater the millisecond accurecy is desired.
     * 
     * @see #getTime()
     * @see #getDuration()
     * @see #getDurationAsDouble()
     */
    public double getTimeAsDouble();

    /**
     * A point data item has a zero duration. Usually the case for data
     * sampels and imagess.
     * 
     * @return the number of milliseconds in duration.
     * 
     * @see #getTime()
     * @see #getTimeAsDouble()
     * @see #getDurationAsDouble()
     */
    public long getDuration();

    /**
     * @return the number of seconds in duration
     * 
     * @see #getTime()
     * @see #getTimeAsDouble()
     * @see #getDuration()
     */
    public double getDurationAsDouble();

    /**
     * @return true is this DataItemInterface covers multiple underlying data
     * items. In which case it should implement the ArchiveDataInterface
     * 
     * @see ArchiveMultiItemInterface
     */
    public boolean hasMultipleItems();

}