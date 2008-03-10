/*
 * TimePointPanel.java
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
 *   $RCSfile: TimePointPanel.java,v $ 
 * 
 * Copied, modified, from code developed by Lars Schumann 2003/09/01
 */
package org.nees.time;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

/**
 * Presents a GUI of a time slider that display the currentTime, currentStartTime, and
 * currentEndTime of a TimeLine. It also displays the time positions of any events on
 * the time line. The user of this GUI can scroll the currentTime between the
 * currentStartTime and the currentEndTime. The time of the selected event (the
 * event closest to the currentTime in the timeLine) is highlighted. For an example
 * using this panel in conjunction with a TimeRangePanel, see TimeScrollBar.
 * 
 * @author Terry E. Weymouth
 * @author Lars Schumann
 * 
 * @see TimeRangePanel
 * @see TimeScrollPanel
 * @see TimeScrollBar
 */
public class TimePointPanel extends TimeScrollPanel {

    /**
     * Initialize and return the JPanel for this GUI
     * 
     * @param timeLine the TimeLine model that is linked to this GUI.
     */
    public TimePointPanel(TimeLine t) {
        super(t);
    }

    protected void paintSurface(Graphics gc) {
        Dimension extent = getSize();
        gc.setColor(Color.black);

        // Note: baseY set in superclass (TimeScrollPanel)
        
        FontMetrics fm = gc.getFontMetrics();

        if (getMessage() != null) {
            gc.setColor(Color.red);
            int x = (extent.width - fm.stringWidth(getMessage())) / 2;
            int y = extent.height / 2;
            gc.drawString(getMessage(), x, y);
        } else {
            double currentStartTime = timeLine.getCurrentStartTime();
            double currentEndTime = timeLine.getCurrentEndTime();
            double currentTime = timeLine.getCurrentTime();

            Insets border = getInsets();
            min = border.left + PADDING;
            max = extent.width - 1 - border.right - PADDING;

            if (isRealTime())
            {
                //TODO: here
                paintRealTimeGrid(gc, currentStartTime, currentEndTime, timeTick,
                     min, max, baseYparameter);
            }
            else
            {
                tm.set(currentStartTime, currentEndTime, 20);
                grid = 0.1 * tm.getStep();

                paintGrid(gc, currentStartTime, currentEndTime,
                        COLOR_SMALL_GRID, grid, 3, 8);
                paintGrid(gc, currentStartTime, currentEndTime,
                        COLOR_LARGE_GRID, 10.0 * grid, 1, 10);
            }

            int baseY = baseYparameter;
            
            gc.setColor(Color.darkGray);
            gc.drawLine(min, baseY, max, baseY);
            gc.drawLine(min, baseY+1, max, baseY+1);

            paintEventMarks(gc, min, max, marksArray);

            // paint time slider
            double t =
                (currentTime - currentStartTime)
                    / (currentEndTime - currentStartTime);
            int x = (int)Math.round(min + t * (max - min));
            Color sliderColor =
                (currentMode == DRAG_TIME ? COLOR_DRAG : COLOR_SLIDER);
            gc.setColor(sliderColor);
            paintSliderOvalMark(gc, x, baseY);
        }
    }

    private void paintEventMarks(
        Graphics gc,
        int eventMin,
        int eventMax,
        double[] marks) {
        
        double currentStartTime = timeLine.getCurrentStartTime();
        double currentEndTime = timeLine.getCurrentEndTime();
        gc.setColor(COLOR_TIME_MARK);

        int baseY = baseYparameter;
        
        for (int i = 0; i < marks.length; i++) {
            if (marks[i] < currentStartTime)
                continue;
            if (marks[i] > currentEndTime)
                continue;
            double t =
                (marks[i] - currentStartTime)
                    / (currentEndTime - currentStartTime);
            int x = (int)Math.round(eventMin + t * (eventMax - eventMin));
            paintUnderTriangleMarker(gc, x, baseY+4);
        }
        if ((currentStartTime <= selectedMarkTime)
            && (selectedMarkTime <= currentEndTime)) {
            gc.setColor(COLOR_SELECTED_MARK);
            double t =
                (selectedMarkTime - currentStartTime)
                    / (currentEndTime - currentStartTime);
            int x = (int)Math.round(eventMin + t * (eventMax - eventMin));
            paintUnderTriangleMarker(gc, x, baseY+4);
        }
    }

    public void mousePressed(MouseEvent e) {
        //if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
        //stupis SGI bug :(
        if ((e.getModifiers() & InputEvent.BUTTON2_MASK) == 0
            && (e.getModifiers() & InputEvent.BUTTON3_MASK) == 0) {
            Dimension extent = getSize();

            Insets border = getInsets();
            min = border.left + PADDING;
            max = extent.width - 1 - border.right - PADDING;

            double x = e.getX();
            if (x < min)
                x = min;
            if (x > max)
                x = max;

            double currentStartTime = timeLine.getCurrentStartTime();
            double currentEndTime = timeLine.getCurrentEndTime();
            double t =
                currentStartTime
                    + (x - min)
                        / (max - min)
                        * (currentEndTime - currentStartTime);
            if (e.isControlDown())
                t = snapToGrid(t);

            currentMode = DRAG_TIME;

            timeLine.setCurrentTime(t);
            // note: graphics get updated when the model get updated
        }
    }

    public void mouseReleased(MouseEvent e) {
        if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
            if (currentMode != DRAG_NOTHING) {
                currentMode = DRAG_NOTHING;
                // the time does not change, force an update, no notification necessary
                updateGraphics();
            }
        }
    }

    public void mouseDragged(MouseEvent e) {
        if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
            Dimension extent = getSize();
            Insets border = getInsets();
            min = border.left + PADDING;
            max = extent.width - 1 - border.right - PADDING;

            double x = e.getX();
            if (x < min)
                x = min;
            if (x > max)
                x = max;

            double currentStartTime = timeLine.getCurrentStartTime();
            double currentEndTime = timeLine.getCurrentEndTime();
            double t =
                currentStartTime
                    + (x - min)
                        / (max - min)
                        * (currentEndTime - currentStartTime);
            if (e.isControlDown())
                t = snapToGrid(t);

            timeLine.setCurrentTime(t);
            // note: graphics get updated when the model get updated
        }
    }

}
