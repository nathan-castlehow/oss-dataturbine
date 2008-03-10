/*
 * TimeLine.java
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
 *   $RCSfile: TimeLine.java,v $ 
 * 
 */
package org.nees.time;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map;
import java.util.Vector;

/**
 * A model and container for time ordered objects. Time is represented as floating
 * point seconds (double). This is assumed to the the floating point representation of
 * the starndard unix time (from miliseconds since the midnight leading into January
 * 1, 1970), but such an assumption is not necessary to use this class. For the
 * purposes of this class, the objects are just ordered by a double number.
 *
 * The objects are bounded. There is an initial read-only start time and end time.
 * Then the additional condition can be imposed of a "soft" start and end time
 * which can vary inside the original start and end time. Objects can not be
 * set or retrieved outside these soft boundaries.
 * 
 * @author Terry E. Weymouth
 */
public class TimeLine {

    // values from the entire range
    private double startTime;
    private double endTime;

    // current values for "zooming in"
    private double currentStartTime;
    private double currentEndTime;
    private double currentTime;

    // the time of the currentEvent (that is the event closest to the current time)
    // represented as Double because the keys of the event list are Double
    private Double mark = null;
    private Double upStream = null;
    private Double downStream = null;

    private boolean notify = true;
    private boolean batchNotify = false;
    private boolean currentEventChanged = false;
    private boolean eventsChanged = false;

    private TreeMap events = new TreeMap();

    public TimeLine(double startTime, double endTime) {
        setStartTime(startTime);
        setEndTime(endTime);
        resetTimes();
    }

    private Vector timeListeners = new Vector();

    /**
     * Resiter a TimeChangeListener that would like to be notified of changes in
     * this TimeLine.
     * 
     * @param listener the TimeChangeListener that will be notified.
     * 
     * @see TimeChangeListener
     * @see #removeTimeListener
     * @see #removeAllTimeListeners
     */
    public void addTimeListener(TimeChangeListener u) {
        timeListeners.addElement(u);
    }

    /**
     * Remove a TimeChangeListener from this list of listeners. If this TimeChangeListener
     * in not in the list, then this method has not effect on the list.
     * 
     * @param listener the TimeChangeListener that will be removed.
     * 
     * @see TimeChangeListener
     * @see #addTimeListener
     * @see #removeAllTimeListeners
     */
    public void removeTimeListener(TimeChangeListener u) {
        timeListeners.removeElement(u);
    }

    /**
     * Clear the list of TimeChangeListener.
     *
     * @see #addTimeListener(TimeChangeListener)
     */
    public void removeAllTimeListeners() {
        timeListeners.removeAllElements();
    }

    /**
     * Make sure that all listeners are notified of the full state
     * of this TimeLine
     */
    public void forceFullNotify() {
        boolean keep = batchNotify;
        batchNotify = false;
        notify = true;
        currentEventChanged = true;
        eventsChanged = true;
        notifyAllTimeListeners();
        batchNotify = keep;
    }

    protected void notifyAllTimeListeners() {
        if (batchNotify)
            return;
        if (notify || currentEventChanged || eventsChanged)
            for (Enumeration e = timeListeners.elements();
                e.hasMoreElements();
                ((TimeChangeListener)e.nextElement()).timeChanged(
                    this,
                    currentEventChanged,
                    eventsChanged)){}
        notify = false;
        currentEventChanged = false;
        eventsChanged = false;
    }

    /**
     * Add a time indexed object to the time line. Set a flag
     * to indicate that the list of marks has changed. Check to see
     * if the current mark (the object nearest the current time) has changed.
     * Notify all listeners of the change(s).
     * 
     * @param time of the object
     * @param value the object
     * 
     * @see #addTimeEventUnchecked
     */
    public void addTimeEvent(double time, Object value) {
        addTimeEventUnchecked(time, value);
        eventsChanged = true;
        checkMarkForChanged();
        notifyAllTimeListeners();
    }

    // determine if the event nearest the current time has changed
    private void checkMarkForChanged() {
        Double newMark = getCurrentMark();
        if (newMark == null) // there is no longer a marked event
            {
            if (mark == null)
                return;
            currentEventChanged = true;
            mark = null;
            return;
        }
        if (mark == null) {
            currentEventChanged = true;
            mark = newMark;
            return;
        }
        if (mark.equals(newMark))
            return;
        currentEventChanged = true;
        mark = newMark;
        return;
    }

    // get the time stamp, as a Double, of the event nearest currentTime
    // if it exists, otherwise null
    private Double getCurrentMark() {
        // if there are no marks return null
        if (events.size() == 0)
            return null;

        Double probe = new Double(currentTime);
        if (events.get(probe) != null)
            return probe; // there is an event for this time!

        // if upStream or downStream is not set or
        // current time has moved outside their boundaries...
        if ((upStream == null)
            || (downStream == null)
            || (currentTime < downStream.doubleValue())
            || (currentTime > upStream.doubleValue())) {
            SortedMap head = events.headMap(probe);
            SortedMap tail = events.tailMap(probe);
            if (head.size() == 0)
                downStream = null;
            else
                downStream = (Double)events.headMap(probe).lastKey();
            if (tail.size() == 0)
                upStream = null;
            else
                upStream = (Double)events.tailMap(probe).firstKey();
        }
        if (upStream == null)
            return downStream;
        if (downStream == null)
            return upStream;
        if ((upStream.doubleValue() - probe.doubleValue())
            < (probe.doubleValue() - downStream.doubleValue()))
            return upStream;
        return downStream;
    }

    /**
     * Add a time indexed object to the time line. No checking or notificaiton
     * takes place. Use this method for batch updates of the list of events.
     * Typicially iterate through the list with this method, and then
     * call forceFullNotify.
     * 
     * @param time of the object
     * @param value the object
     * 
     * @see #addTimeEvent
     * @see #forceFullNotify
     */
    private void addTimeEventUnchecked(double time, Object value) {
        Double key = new Double(time);
        events.put(key, value);
    }

    /**
     * @return an Iterator of Double's that mark the times of the objects
     * in the events list for this TimeLine
     */
    public Iterator marks() {
        final Iterator items = events.entrySet().iterator();
        return new Iterator() {
            public boolean hasNext() {
                return items.hasNext();
            }
            public Object next() {
                return ((Map.Entry)items.next()).getKey();
            }
            public void remove() {
                items.remove();
            }
        };
    }

    /**
     * @return an array of the time stamps on the objects in the
     * events list for this TimeLine
     */
    public double[] getMarksArray() {
        double[] array = new double[events.size()];
        int i = 0;
        for (Iterator m = marks();
            m.hasNext();
            array[i++] = ((Double)m.next()).doubleValue()){}
        return array;
    }

    /**
     * @return if such exists, the Object at the current mark time,
     * otherwise null. The mark time is maintained to an index of an event, when
     * events exist with in the bounds of currentStartTime and currentEndTime.
     * 
     * @see #getCurrentEventTime
     */
    public Object getCurrentEventObject() {
        if (mark == null)
            return null;
        return events.get(mark);
    }

    /**
     * @return if such exists, the index time of Object closest to the current time
     * (as a Double), otherwise null.
     * 
     * @see #getCurrentEventObject
     */
    public Double getCurrentEventTime() {
        return mark;
    }

    /**
     * @return an Iterator of Map.Entry's of the entries in the time line
     * 
     * (A Map.Entry is a key-value pair)
     * 
     * @see java.util.Map#Entry
     */
    public Iterator entries() {
        return events.entrySet().iterator();
    }

    /**
     * Fetch the first object that is inside current start time and current end time,
     * inclusive, and is at or after the supplied time.
     * 
     * @param time on the time line for the serach
     * @return the Object found, or null if none exists
     */
    public Object onOrAfter(double time) {
        if (time > currentEndTime)
            return null;
        if (time < currentStartTime)
            time = currentStartTime;
        Double probe = new Double(time);
        if (events.containsKey(probe))
            return events.get(probe);
        // since there is no entry for probe...
        SortedMap tail = events.tailMap(probe);
        if (tail.size() == 0)
            return null;
        return events.get(tail.firstKey());
    }

    /**
     * Fetch the first object that is inside current start time and current end time,
     * inclusive, and is at or before the supplied time.
     * 
     * @param time on the time line for the serach
     * @return the Object found, or null if none exists
     */
    public Object onOrBefore(double time) {
        if (time < currentStartTime)
            return null;
        if (time > currentEndTime)
            time = currentEndTime;
        Double probe = new Double(time);
        if (events.containsKey(probe))
            return events.get(probe);
        // since there is no entry for probe...
        SortedMap head = events.headMap(probe);
        if (head.size() == 0)
            return null;
        return events.get(head.lastKey());
    }

    /**
     * Reset currect start and end time to their outside boundaries, and current time
     * to the start time.
     */
    public void resetTimes() {
        batchNotify = true;
        setCurrentEndTime(endTime);
        // set end time first as they both default to zero
        setCurrentStartTime(startTime);
        setCurrentTime(startTime);
        batchNotify = false;
        notifyAllTimeListeners();
    }

    private void setStartTime(double d) {
        double old = startTime;
        startTime = d;
        if (old != d)
            notify = true;
        notifyAllTimeListeners();
    }
    /** @return the original (outside) start time */
    public double getStartTime() {
        return startTime;
    }

    /** @param set the current start time; note that this can
     * not be less then base start time, and it can not be greater
     * then the current end time. It is forced to those limits if
     * an attempt is made to set it outside of them. Also, if setting
     * the current start time leaves the current time outsdie the
     * current start time, the currect time is changed to the
     * current start time.
     */
    public void setCurrentStartTime(double d) {
        double old = currentStartTime;
        if (d < startTime)
            d = startTime;
        if (d > currentEndTime)
            d = currentEndTime;
        currentStartTime = d;
        if (currentTime < currentStartTime) {
            boolean save = batchNotify;
            batchNotify = true;
            setCurrentTime(currentStartTime);
            batchNotify = save;
        }
        if (old != d)
            notify = true;
        notifyAllTimeListeners();
    }
    /** @return the current start time */
    public double getCurrentStartTime() {
        return currentStartTime;
    }

    /** @param set the current time; note that this can
     * not be less then current start time, and it can not be greater
     * then the current end time. It is forced to those limits if
     * and attempt is made to set it outside of them.
     */
    public void setCurrentTime(double d) {
        double old = currentTime;
        if (d < currentStartTime)
            d = currentStartTime;
        if (d > currentEndTime)
            d = currentEndTime;
        currentTime = d;
        if (old != d)
            notify = true;
        checkMarkForChanged();
        notifyAllTimeListeners();
    }
    /** @return the current time */
    public double getCurrentTime() {
        return currentTime;
    }

    /** @param set the current end time; note that this can
     * not be greater then base end time, and it can not be less
     * then the current start time. It is forced to those limits if
     * an attempt is made to set it outside of them. Also, if setting
     * the current end time leaves the current time outsdie the
     * current end time, the currect time is changed to the
     * current end time.
     */
    public void setCurrentEndTime(double d) {
        double old = currentEndTime;
        if (d > endTime)
            d = endTime;
        if (d < currentStartTime)
            d = currentStartTime;
        currentEndTime = d;
        if (currentTime > currentEndTime) {
            boolean save = batchNotify;
            batchNotify = true;
            setCurrentTime(currentEndTime);
            batchNotify = save;
        }
        if (old != d)
            notify = true;
        notifyAllTimeListeners();
    }
    /** @return the current end time */
    public double getCurrentEndTime() {
        return currentEndTime;
    }
    
    public double getAverageInterval()
    {
        double sum = 0.0;
        double[] times = getMarksArray();
        int count = times.length;
        
        for (int i = 1; i < count; i++)
            sum += times[i] - times[i-1];
        
        if (count < 2) return 0.0;
        return (sum / (double) count);
    }

    /** @param set the original (outside) end time */
    private void setEndTime(double d) {
        double old = endTime;
        endTime = d;
        if (old != d)
            notify = true;
        notifyAllTimeListeners();
    }
    /** @return the end time */
    public double getEndTime() {
        return endTime;
    }

    /**
     * Sets current time to be the time of the next event, or currentEndTime
     * in the case that the next event is outside currentEndTime; if no next
     * event exists, the time is unchanged.
     */
    public void forwardToNextMark() {
        SortedMap tail = events.tailMap(mark); // greater then or equal to
        if (tail.size() == 0)
            return;
        Double key = (Double)tail.firstKey();
        // are we on the mark, if so get the next event
        if (mark.equals(key)) {
            // is it the only one?
            if (tail.size() == 1)
                return;
            Iterator i = tail.entrySet().iterator();
            // skip the first one
            i.next();
            key = (Double) ((Map.Entry)i.next()).getKey();
        }
        setCurrentTime(key.doubleValue());
    }

    /**
     * Sets current time to be the time of the previous event, or currentStartTime
     * in the case that the previous event is outside currentStartTime; if no previous
     * event exists, the time is unchanged.
     */
    public void backToPreviousMark() {
        SortedMap head = events.headMap(mark); // strictly less then
        if (head.size() == 0)
            return;
        setCurrentTime(((Double)head.lastKey()).doubleValue());
    }

}
