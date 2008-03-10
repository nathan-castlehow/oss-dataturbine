/*
 * TimeRangePanel.java
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
 *   $RCSfile: TimeRangePanel.java,v $ 
 * 
 * 
 * Copied, modified, from code developed by Lars Schumann 2003/09/01
 */
package org.nees.time;

import java.awt.*;
import java.awt.event.*;

/**
 * Presents a GUI of a time range slider that display the startTime, currentStartTime,
 * currentTime, currentEndTime, and endTime of a TimeLine. The use can manipulate
 * the currentStartTime, and the currentEnd Time. The currentTime can not be
 * manipulated by this GUI. That is done using a TimePointPanel. For an example
 * using this panel in conjunction with a TimeRangePanel, see TimeScrollBar.
 * 
 * @author Terry E. Weymouth
 * @author Lars Schumann
 * 
 * @see TimePointPanel
 * @see TimeScrollPanel
 * @see TimeScrollBar.
 */
public class TimeRangePanel extends TimeScrollPanel {

    /**
     * Initialize and return the JPanel for this GUI
     * 
     * @param timeLine the TimeLine model that is linked to this GUI.
     */
    public TimeRangePanel(TimeLine t) {
        super(t);
    }

    protected void paintSurface(Graphics gc) {
        Dimension extent = getSize();
        gc.setColor(Color.black);

        FontMetrics fm = gc.getFontMetrics();

        if (message != null) {
            gc.setColor(Color.red);
            int x = (extent.width - fm.stringWidth(message)) / 2;
            int y = extent.height / 2;
            gc.drawString(message, x, y);
        } else {
            double startTime = timeLine.getStartTime();
            double endTime = timeLine.getEndTime();
            double currentStartTime = timeLine.getCurrentStartTime();
            double currentEndTime = timeLine.getCurrentEndTime();
            double currentTime = timeLine.getCurrentTime();

            Insets border = getInsets();
            min = border.left + PADDING;
            max = extent.width - 1 - border.right - PADDING;
            
            // Note: baseYparameter set in superclass (TimeScrollPanel)
            
            // paint grid
            if (isRealTime())
            {
                //TODO: here
                paintRealTimeGrid(gc, startTime, endTime, timeTick, min, max, baseYparameter);
            }
            else
            {
                tm.set(startTime, endTime, 8);
                grid = 0.1 * tm.getStep();

                paintGrid(gc, startTime, endTime, COLOR_SMALL_GRID, grid, 6, 11);
                paintGrid(gc, startTime, endTime, COLOR_LARGE_GRID, 10.0 * grid, 4, 13);
            }

            int baseY = baseYparameter;
            
            // paint main bar
            gc.setColor(Color.darkGray);
            gc.drawLine(min, baseY, max, baseY);
            gc.drawLine(min, baseY+1, max, baseY+1);

            // paint start time marker
            gc.setColor(currentMode == DRAG_START ? COLOR_DRAG : COLOR_MARKERS);
            double t = (currentStartTime - startTime) / (endTime - startTime);
            int x = (int)Math.round(min + t * (max - min));
            paintUnderTriangleMarker(gc, x,baseY+4);

            // paint end time marker
            gc.setColor(currentMode == DRAG_END ? COLOR_DRAG : COLOR_MARKERS);
            t = (currentEndTime - startTime) / (endTime - startTime);
            x = (int)Math.round(min + t * (max - min));
            paintUnderTriangleMarker(gc, x,baseY+4);

            // paint current time marker
            gc.setColor(new Color(0x666666));
            t = (currentTime - startTime) / (endTime - startTime);
            x = (int)Math.round(min + t * (max - min));
            int y = baseY - 10;
            gc.drawLine(x - 3, y, x + 3, y); y++;
            gc.drawLine(x - 2, y, x + 2, y); y++;
            gc.drawLine(x - 1, y, x + 1, y); y++;
            gc.drawLine(x,     y, x,     y);
        }
    }

    public void mousePressed(MouseEvent e) {
        //if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
        //stupis SGI bug :(
        if ((e.getModifiers() & InputEvent.BUTTON2_MASK) == 0
            && (e.getModifiers() & InputEvent.BUTTON3_MASK) == 0) {
            pressed = e.getX();
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
        Dimension extent = getSize();
        if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
            Insets border = getInsets();
            min = border.left + PADDING;
            max = extent.width - 1 - border.right - PADDING;

            double startTime = timeLine.getStartTime();
            double endTime = timeLine.getEndTime();
            double currentStartTime = timeLine.getCurrentStartTime();
            double currentEndTime = timeLine.getCurrentEndTime();

            // check if a marker should be dragged
            if (currentMode == DRAG_NOTHING) {
                double t =
                    (currentStartTime - startTime) / (endTime - startTime);
                int xStart = (int)Math.round(min + t * (max - min));

                t = (currentEndTime - startTime) / (endTime - startTime);
                int xEnd = (int)Math.round(min + t * (max - min));

                if (Math.abs(xStart - pressed) < 4) {
                    currentMode = DRAG_START;
                    dragMin = min;
                    dragMax = xEnd - 9;
                }

                if (Math.abs(xEnd - pressed) < 4) {
                    currentMode = DRAG_END;
                    dragMin = xStart + 9;
                    dragMax = max;
                }
            }

            // do the actual dragging
            if (currentMode != DRAG_NOTHING) {
                double x = e.getX();
                if (x < dragMin)
                    x = dragMin;
                if (x > dragMax)
                    x = dragMax;

                double t =
                    startTime + (x - min) / (max - min) * (endTime - startTime);
                if (e.isControlDown())
                    t = snapToGrid(t);

                if (currentMode == DRAG_START) {
                    timeLine.setCurrentStartTime(t);
                }
                if (currentMode == DRAG_END) {
                    timeLine.setCurrentEndTime(t);
                }
            }
        }
    }

}
