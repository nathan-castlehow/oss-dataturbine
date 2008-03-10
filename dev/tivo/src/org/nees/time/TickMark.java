/*
 * TickMark.java
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
 *   $RCSfile: TickMark.java,v $ 
 * 
 * Copied unmodified from code developed by Lars Schumann 2003/09/01
 */
package org.nees.time;

/**
 * A utility for computing the positions for tickmarks on a scale.
 * (Nice work Lars! - tew)
 * 
 * @author Lars Schumann
 */
class TickMark {
    public static final double aSmallNumber = 1e-6;
    public static final double log10 = Math.log(10);

    double min;
    double max;
    double step;
    int count;

    TickMark() {}

    /**
     * Construct a new set of scaling factors for tick marks on a scale
     * @param min - the desired min value
     * @param max - the desired max value
     * @param averageCnt - the desired number of tickmarks
     */
    TickMark(double min, double max, int averageCnt) {
        set(min, max, averageCnt);
    }

    /**
     * Reset the set of scaling factors for tick marks on a scale
     * @param min - the desired min value
     * @param max - the desired max value
     * @param averageCnt - the desired number of tickmarks
     */
    protected void set(double l, double h, int averageCnt) {
        double diff = Math.abs(h - l) / (double)averageCnt;
        double log = Math.log(diff) / log10;
        double floor = Math.floor(log);
        double rest = Math.pow(10, log - floor);

        int x;
        if (rest - aSmallNumber < 1.0)
            x = 1;
        else if (rest - aSmallNumber < 2.0)
            x = 2;
        else if (rest - aSmallNumber < 5.0)
            x = 5;
        else {
            x = 1;
            floor++;
        }

        step = (double)x * Math.pow(10, floor);
        min = Math.floor(l / step) * step;
        max = Math.ceil(h / step) * step;
        count = 1 + (int)Math.round((max - min) / step);
    }

    protected double min() {
        return min;
    }
    protected double max() {
        return max;
    }
    protected double getStep() {
        return step;
    }
    protected int getCount() {
        return count;
    }
}
