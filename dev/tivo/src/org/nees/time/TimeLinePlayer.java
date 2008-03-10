/*
 * TimeLinePlayer.java
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
 *   $RCSfile: TimeLinePlayer.java,v $ 
 * 
 */
package org.nees.time;

import java.util.Enumeration;
import java.util.Vector;

/**
 * A control class that supports the "playing" (as with a VCR) of a TimeLine.
 * The player does this by modifying the currentTime of the time line, through
 * calls to the methods of TimeLine.
 * 
 * The TimeLine is played by incrementing the time by an interval set with this
 * player. The default interval is 1.0, e.g. one second.
 * 
 * @author Terry E Weymouth
 * 
 * @see TimeLine
 */
public class TimeLinePlayer {

    private TimeLine timeLine;
    private double playInterval = 1.0;
    private boolean realTime = true;
    private boolean playing = false;
    private double actualInterval = 1.0;

    Thread runningThread = null;

    TimeLinePlayer(TimeLine tl) {
        timeLine = tl;
    }

    /**
     * Set the TimeLine that this player plays.
     * 
     * @param the TimeLine
     */
    public void setNewTimeLine(TimeLine t) {
        stop();
        timeLine = t;
    }

    /** Position the currentTime of the time line to the currentStartTime */
    public void toFirstFrame() {
        timeLine.setCurrentTime(timeLine.getCurrentStartTime());
    }

    /** Position the currentTime of the timeline to the surrentEndTime */
    public void toLastFrame() {
        timeLine.setCurrentTime(timeLine.getCurrentEndTime());
    }

    /**
     * Position the currentTime of the timeLine to the time of the next
     * event on the time line if such an event exists. If there is no
     * such event, the currentTime of the timeLine remains unchanged.
     */
    public void toNextFrame() {
        timeLine.forwardToNextMark();
    }

    /**
     * Position the currentTime of the timeLine to the time of the previous
     * event on the time line if such an event exists. If there is no
     * such event, the currentTime of the timeLine remains unchanged.
     */
    public void toPreviousFrame() {
        timeLine.backToPreviousMark();
    }

    /**
     * @return true if the current play direction is "forward", false otherwise
     */
    public boolean isPlayingForward() {
        return (actualInterval > 0);
    }

    /**
     * reverse the direction of play or (if not currently playing) the direction
     * of the next play
     */
    public void reverse() {
        actualInterval = -actualInterval;
        notifyAllListeners();
    }

    /**
     * Start the timeLine playing, in the backward direciton.
     */
    public void playBackward() {
        if (isPlayingForward())
            reverse();
        if (!isPlaying())
            play(); // does its own notify!
        else
            notifyAllListeners();
    }

    /**
     * Start the time line playing, in the forward direction.
     */
    public void playForward() {
        if (!isPlayingForward())
            reverse();
        if (!isPlaying())
            play(); // does its own notify!
        else
            notifyAllListeners();
    }

    /**
     * @true is this TimeLinePlayer is currently playing, false otherwise
     */
    public boolean isPlaying() {
        return playing;
    }

    /**
     * Stop, suspend, cease the playing of the timeline.
     * @see #stop()
     */
    public void pause() {
        // currently stop and pause are the same.
        playing = false;
        if (runningThread != null)
            runningThread.interrupt();
    }

    /**
     * Stop, suspend, cease the playing of the timeline.
     * @see #pause()
     */
    public void stop() {
        // currently stop and pause are the same.
        pause();
    }

    /**
     * Start, resume, the playing of the timeLine.
     */
    public void play() {
        // notify of run, pause, and stop in thread code
        if (isPlaying())
            return;
        Runnable r = new Runnable() {
                // hide public method
    public void run() {
                runTask();
            }
        };
        runningThread = new Thread(r);
        playing = true;
        runningThread.start();
    }

    private void runTask() {
        notifyAllListeners(); // started from somewhere
        long lastUnixTime;
        long thisUnixTime = System.currentTimeMillis();
        while (isPlaying()) {
            lastUnixTime = thisUnixTime;
            double currentTime = timeLine.getCurrentTime();
            double nextTime = currentTime + actualInterval;
            if (isPlayingForward()
                && (nextTime >= timeLine.getCurrentEndTime())) {
                timeLine.setCurrentTime(timeLine.getCurrentEndTime());
                playing = false;
                continue;
            }
            if (!isPlayingForward()
                && (nextTime <= timeLine.getCurrentStartTime())) {
                timeLine.setCurrentTime(timeLine.getCurrentStartTime());
                playing = false;
                continue;
            }
            timeLine.setCurrentTime(nextTime);
            long waitTime = 0;
            if (!realTime)
                waitTime = 100; // 10th of a second
            else {
                waitTime = (long)playInterval;
                // adjust for the time the task took
                thisUnixTime = System.currentTimeMillis();
                waitTime -= (thisUnixTime - lastUnixTime);
                if (waitTime < 0)
                    waitTime = 0;
            }
            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException ignore) {}
        } // while
        runningThread = null;
        notifyAllListeners(); // stopped now
    } // runTask

    /**
     * the time between steps in the player thread; if the the realtime flag is
     * set, then it is also the time that the player thread waits
     * 
     * @return the time in milliseconds
     * @see setPlayInterval(long)
     */
    public long getPlayIntervalAsLong() {
        return ((long) (getPlayIntervalAsDouble() * 1000.0));
    }

    /**
     * the time between steps in the player thread; if the the realtime flag is
     * set, then it is also the time that the player thread waits
     * 
     * @return the time in seconds
     * 
     * @see setPlayInterval(double)
     */
    public double getPlayIntervalAsDouble() {
        return playInterval;
    }

    /**
     * the time between steps in the player thread; if the the realtime flag is
     * set, then it is also the time that the player thread waits
     * 
     * @param l the time in milliseconds
     * 
     * @see setPlayInterval(double)
     */
    public void setPlayInterval(long l) {
        setPlayInterval(((double)l) / 1000.0);
    }

    /**
     * the time between steps in the player thread; if the the realtime flag is
     * set, then it is also the time that the player thread waits
     * 
     * @param d the time in seconds
     * @see setPlayInterval(long)
     * @see getPlayIntervalAsLong
     * @see getPlayIntervalAsDouble
     */
    public void setPlayInterval(double d) {
        if (d <= 0.0)
            return;
        playInterval = d;
System.out.println("TimeLinePlayer -- playInterval = " + playInterval);
        // is the current play direction forward or backward
        if (isPlayingForward())
            actualInterval = playInterval;
        else
            actualInterval = -playInterval;
        notifyAllListeners();
    }

    /**
     * If true then the player thread waits for the play interval on each step;
     * defaults to true. It is is false, then the player waits a tenth of a second
     * at each step regardless of the time interval of the playing.
     * 
     * @see setPlayInterval(double)
     */
    public boolean isRealTime() {
        return realTime;
    }

    /**
     * If true then the player thread waits for the play interval on each step;
     * defaults to false.
     */
    public void setRealTime(boolean b) {
        realTime = b;
    }

    private Vector listeners = new Vector();

    /**
     * Add a player listener. Listeners are notified with the player starts or stops,
     * changes direction, and when the interval for playback changes.
     * 
     * @param listener the TimeLinePlayerStateListener requesting notification.
     */
    public void addListener(TimeLinePlayerStateListener l) {
        listeners.addElement(l);
    }

    /**
     * Remove a player listener from the list of listeners. If this player listener
     * is not on the list, then the list is uneffected.
     * 
     * @param listener the TimeLinePlayerStateListener being removed.
     */
    public void removeListener(TimeLinePlayerStateListener l) {
        listeners.removeElement(l);
    }

    /**
     * Clear the list of player listeners. 
     */
    public void removeAllListeners() {
        listeners.removeAllElements();
    }

    protected void notifyAllListeners() {
        for (Enumeration e = listeners.elements();
            e.hasMoreElements();
            ((TimeLinePlayerStateListener)e.nextElement()).stateChanged(this)){}
    }

}
