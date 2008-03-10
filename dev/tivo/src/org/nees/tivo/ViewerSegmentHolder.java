/*
 * ViewerSegmentHolder.java
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
 *   $RCSfile: ViewerSegmentHolder.java,v $ 
 * 
 */
package org.nees.tivo;

import org.nees.time.TimeLine;

class ViewerSegmentHolder implements ArchiveSegmentInterface {
    private ArchiveSegmentInterface itsSegment;

    private String name;

    private TimeLine timeLine;

    ViewerSegmentHolder(ArchiveSegmentInterface theSeg) {
        itsSegment = theSeg;
        name = itsSegment.getName();
        double startTime = itsSegment.getStartTimeAsDouble();
        double endTime = itsSegment.getEndTimeAsDouble();
        timeLine = setTimeLine();
    }

    //  ---------- implementation of ArchiveSegmentInterface
    public String getName() {
        return name;
    }
    public long getStartTime() {
        return itsSegment.getStartTime();
    }
    public long getEndTime() {
        return itsSegment.getEndTime();
    }
    public ArchiveItemInterface[] getSortedArray(long st, long et) {
        st = Math.max(st, getStartTime());
        et = Math.min(et, getEndTime());
        return itsSegment.getSortedArray(st, et);
    }
    public ArchiveItemInterface getAtOrAfter(
        long time,
        long startTime,
        long endTime) {
        return itsSegment.getAtOrAfter(time, startTime, endTime);
    }
    public ArchiveItemInterface getAtOrAfter(long time) {
        return itsSegment.getAtOrAfter(time, getStartTime(), getEndTime());
    }

    public ArchiveItemInterface getAtOrBefore(
        long time,
        long startTime,
        long endTime) {
        return itsSegment.getAtOrBefore(time, startTime, endTime);
    }
    public ArchiveItemInterface getAtOrBefore(long time) {
        return itsSegment.getAtOrBefore(time, getStartTime(), getEndTime());
    }
    public int compareTo(Object t) {
        return itsSegment.compareTo(t);
    }
    public int compareTo(ArchiveSegmentInterface t) {
        return itsSegment.compareTo(t);
    }
    public void setStartTime(double t) {
        itsSegment.setStartTime(t);
    }
    public double getStartTimeAsDouble() {
        return itsSegment.getStartTimeAsDouble();
    }
    public long getDuration() {
        return itsSegment.getDuration();
    }
    public double getDurationAsDouble() {
        return itsSegment.getDurationAsDouble();
    }
    public double getEndTimeAsDouble() {
        return itsSegment.getEndTimeAsDouble();
    }
    public boolean equals(ArchiveSegmentInterface test) {
        if (test == null) return false;
        return itsSegment.equals(((ViewerSegmentHolder)test).itsSegment);
    }
    public void setName(String name) {
        itsSegment.setName(name);
    }
    public String getProperty(String propertyKey) {
        return itsSegment.getProperty(propertyKey);
    }
    public void setProperty(String propertyKey, String propertyValue) {
        itsSegment.setProperty(propertyKey,propertyValue);
    }

    //  ---------- end of implementation of ArchiveSegmentInterface

    private TimeLine setTimeLine() {
        double startTimeD = getStartTimeAsDouble();
        double endTimeD = getEndTimeAsDouble();
        TimeLine tl = new TimeLine(startTimeD, endTimeD);
        long startTime = (long) (startTimeD * 1000.0);
        long endTime = (long) (endTimeD * 1000.0);
        ArchiveItemInterface[] a = getSortedArray(startTime, endTime);
        for (int i = 0; i < a.length; i++)
            tl.addTimeEvent(a[i].getTimeAsDouble(), a[i]);
        return tl;
    }

    public TimeLine getTimeLine() {
        return timeLine;
    }

    public void resetStartEndTimes() {
        timeLine.resetTimes();
    }

    public String toString() {
        return getName();
    }

} // ViewerSegmentHolder