/*
 * TimeScrollBar.java
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
 *   $RCSfile: TimeScrollBar.java,v $ 
 * 
 */
package org.nees.time;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.Border;

import org.nees.time.TimeLine;
import org.nees.time.TimePointPanel;
import org.nees.time.TimeRangePanel;

/**
 * Dispalys a "double decker" scroll bar based on a time line model (see TimeLine, in this
 * package. Links together a TimePointPanel and a TimeRangePane through the suppleied
 * TimeLine model. It has a defaut border set (a compound border with a black line on
 * the outside and 7 pixels of pading on the inside).
 * 
 * @see TimeLine
 * @see TimePointPanel
 * @see TimeRangePane
 *  
 * @author Terry E Weymouth
 */
public class TimeScrollBar extends JPanel {
    TimePointPanel timeControl;
    TimeRangePanel timeRangeControl;

    public TimeScrollBar(TimeLine tl) {
        timeControl = new TimePointPanel(tl);
        timeRangeControl = new TimeRangePanel(tl);

        Border blackline = BorderFactory.createLineBorder(Color.black);
        Border empty = BorderFactory.createEmptyBorder(7, 7, 7, 7);
        Border compound = BorderFactory.createCompoundBorder(blackline, empty);

        setLayout(new BorderLayout());
        setBorder(compound);

        add(timeControl, BorderLayout.NORTH);
        add(timeRangeControl, BorderLayout.SOUTH);

    }

    public void resetTimeLine(TimeLine tl) {
        timeControl.resetTimeLine(tl);
        timeRangeControl.resetTimeLine(tl);
        invalidate();
    }
} // TimeScrollBar