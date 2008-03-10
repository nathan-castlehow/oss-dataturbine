/*
 * TimeScrollPanel.java
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
 *   $RCSfile: TimeScrollPanel.java,v $ 
 * 
 * Copied, modified, from code developed by Lars Schumann 2003/09/01
 */
package org.nees.time;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;

import javax.swing.JPanel;

/**
 * An abstract class infrastructure support for the GUI classes for showing time
 * scrollers based on a time line. For deatils see TimePointPanel and TimeRangePanel.
 * These three classes, together, implement a Model-Viewer-Controler for viewing and
 * scrolling through the model of an event list that is embodied in TimeLine. For
 * and example using both the TimeRangePanel and the TimePointPanel, see TimeScrollBar.
 * 
 * @author Terry E. Weymouth
 * @author Lars Schumann
 * 
 * @see TimePointPanel
 * @see TimeRangePanel
 * @see TimeScrollBar
 */
public abstract class TimeScrollPanel
    extends JPanel
    implements MouseListener, MouseMotionListener, TimeChangeListener {
    protected static final double aSmallNumber = 1e-6;

    protected static final Color COLOR_SMALL_GRID = new Color(0x999999);
    protected static final Color COLOR_LARGE_GRID = Color.black;
    protected static final Color COLOR_MARKERS = new Color(0x6666FF);
    protected static final Color COLOR_DRAG = new Color(0xFF6666);
    protected static final Color COLOR_SLIDER = Color.black;
    protected static final Color COLOR_TIME_MARK = Color.orange;
    protected static final Color COLOR_SELECTED_MARK = Color.red;
    
    protected static final boolean REALTIME = true;

    protected static final int DRAG_NOTHING = 0;
    protected static final int DRAG_START = 1;
    protected static final int DRAG_TIME = 2;
    protected static final int DRAG_END = 3;

    protected static final int PADDING = 6;

    protected String message;

    protected Dimension minDim = new Dimension(300, 30);

    protected double grid = 0.1;
    protected int currentMode = DRAG_NOTHING;

    protected boolean realTime = REALTIME;
    protected TickMark tm = new TickMark();
    protected TimeTick timeTick = new TimeTick();

    protected int pressed, min, max, dragMin, dragMax;
    protected int baseYparameter = 20;

    protected Image offScreen;
    protected Graphics offGC;
    protected Dimension offSize;

    protected long oldTime = 0;
    protected long newTime = 0;

    protected TimeLine timeLine;
    protected double[] marksArray = new double[0];
    protected double selectedMarkTime = -1.0;

    /**
     * Initialize and return the JPanel for this GUI
     * 
     * @param timeLine the TimeLine model that is linked to this GUI.
     */
    public TimeScrollPanel(TimeLine t) {
        addMouseListener(this);
        addMouseMotionListener(this);
        resetTimeLine(t);
    }

    public void resetTimeLine(TimeLine t) {
        if (timeLine != null)
            timeLine.removeTimeListener(this);
        timeLine = t;
        t.addTimeListener(this);
    }

    /**
     * Implements the API for TimeChangeListener
     */
    public void timeChanged(
        TimeLine t,
        boolean currentMarkChanged,
        boolean marksChanged) {
        if (marksChanged)
            updataMarks();
        if (currentMarkChanged) {
            Double time = timeLine.getCurrentEventTime();
            if (time != null)
                selectedMarkTime = time.doubleValue();
        }
        updateGraphics();
    }

    public boolean isRealTime() {return realTime;}
    public boolean getRealTime() {return realTime;}
    public void setRealTime(boolean flag) {realTime = flag;}
    
    private void updataMarks() {
        marksArray = timeLine.getMarksArray();
    }

    protected double snapToGrid(double v) {
        return grid * Math.round(v / grid);
    }

    protected void paintSliderOvalMark(Graphics gc, int x0, int y0) {
        int x = x0-3;
        gc.drawLine(x, y0-3, x, y0-8);  x++;
        gc.drawLine(x, y0-2, x, y0-9);  x++;
        gc.drawLine(x, y0-1, x, y0-10); x++;
        gc.drawLine(x, y0-1, x, y0-10); x++;
        gc.drawLine(x, y0-1, x, y0-10); x++;
        gc.drawLine(x, y0-2, x, y0-9);  x++;
        gc.drawLine(x, y0-3, x, y0-8);
    }

    protected void paintUnderTriangleMarker(Graphics gc, int x, int y) {
        gc.drawLine(x,     y, x,     y); y++ ;
        gc.drawLine(x - 1, y, x + 1, y); y++ ;
        gc.drawLine(x - 2, y, x + 2, y); y++ ;
        gc.drawLine(x - 3, y, x + 3, y); y++ ;
        gc.drawLine(x - 4, y, x + 4, y);
    }

     protected void paintGrid(Graphics gc, double startTime, double endTime, 
        Color c, double g, int y1, int y2)
    {
        gc.setColor(c);
        double x1 = g * Math.ceil(startTime / g - aSmallNumber);
        double x2 = g * Math.floor(endTime / g + aSmallNumber);
        for (double i = x1; i < x2 + aSmallNumber; i += g) {
            double t = (i - startTime) / (endTime - startTime);
            int x = (int)Math.round(min + t * (max - min));
            gc.drawLine(x, y1, x, y2);
        }
    }
    
    protected void paintRealTimeGrid
        (Graphics gc, double startTime, double endTime, TimeTick tt,
                int minX, int maxX, int baseY)
    {

        //TODO: Move this?
        tt.set(startTime, endTime, minX, maxX, 10);

//        System.out.println("Start time: " + TimeUtility.timeToString(startTime));
        // startTime, endTime, step are in seconds.

        double step = tt.getStep();
        
        Graphics2D g2;
        g2 = (Graphics2D) gc;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                            RenderingHints.VALUE_RENDER_QUALITY);
        FontRenderContext frc = g2.getFontRenderContext();        
        Font f = new Font("Monospaced", Font.PLAIN, 10);

        double x1 = step * Math.ceil(startTime / step - aSmallNumber);
        double x2 = step * Math.floor(endTime / step + aSmallNumber);
        double t;
        int baseType = TimeTick.tickTypeForTime(step);
        int x, type, textX, textXWidth;
        String s;
        TextLayout tl2;
        for (double time = x1; time < x2 + aSmallNumber; time += step) {
            t = (time - startTime) / (endTime - startTime);
            x = (int)Math.round(min + t * (max - min));

            type = TimeTick.tickTypeForTime(time);
//System.out.println("Type: " + type);
            gc.setColor(TimeTick.tickColorForType(type));                    
            gc.drawLine(x, baseY+2, x, baseY-TimeTick.tickHeightForType(type));
            if (type < baseType) { // ... for "major" divisions
                if (TimeTick.tickDoubleFlagForType(type))
                {
                    gc.drawLine(x-1, baseY+2, x-1, baseY-TimeTick.tickHeightForType(type));
                    gc.drawLine(x+1, baseY+2, x+1, baseY-TimeTick.tickHeightForType(type));
                }
                s = TimeTick.tickLabelForTime(type, time);
                tl2 = new TextLayout(s, f, frc);
                textXWidth = ((int)tl2.getBounds().getWidth());
                textX = x - (textXWidth/2);
                if (textX < minX) textX = minX;
                if (textX > (maxX - (textXWidth/2))) textX = maxX - textXWidth;
                tl2.draw(g2, textX, baseY - 10);
            }
        }
    }

    public Graphics getOffscreenGraphics() {
        Dimension d = getSize();

        if ((offScreen == null) || !offSize.equals(d)) {
            offSize = d;
            offScreen = createImage(offSize.width, offSize.height);
            offGC = offScreen.getGraphics();
            offGC.setFont(getFont());
        }
        return offGC;
    }

    protected abstract void paintSurface(Graphics gc);

    protected void paintComponent(Graphics g) {
        Graphics gr;
        //if (Controller.doubleBuffer) 
        gr = getOffscreenGraphics();
        // else gr = g;
        if (isOpaque())
            paintBackground(gr);
        paintSurface(gr);
        //if (Controller.doubleBuffer)
        paintOffScreenGraphics(g);
    }

    private void paintBackground(Graphics g) {
        Dimension d = getSize();
        g.setColor(getBackground());
        g.fillRect(0, 0, d.width, d.height);
    }

    private void paintOffScreenGraphics(Graphics g) {
        g.drawImage(offScreen, 0, 0, null);
    }

    protected void updateGraphics() {
        revalidate();
        repaint();
    }

    //1.1 event handling
    public void mouseClicked(MouseEvent e) {}

    public abstract void mousePressed(MouseEvent e);

    public abstract void mouseReleased(MouseEvent e);

    public void mouseMoved(MouseEvent e) {}

    public abstract void mouseDragged(MouseEvent e);

    public void mouseExited(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}

    public Dimension getPreferredSize() {
        return minDim;
    }
    public Dimension getMinimumSize() {
        return minDim;
    }
    public Dimension getMaximumSize() {
        return minDim;
    }

    /** @param set the message */
    public void setMessage(String string) {
        message = string;
    }
    /** @return the message String*/
    public String getMessage() {
        return message;
    }

}
