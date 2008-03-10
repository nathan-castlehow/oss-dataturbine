/*
 * TimeScrollBarTest.java
 * Created May, 2005
 * Last Updated: May 19, 2005
 * 
 * COPYRIGHT © 2005, THE REGENTS OF THE UNIVERSITY OF MICHIGAN,
 * ALL RIGHTS RESERVED; see the file COPYRIGHT.txt in this folder for details
 * 
 * This work is supported in part by the George E. Brown, Jr. Network
 * for Earthquake Engineering Simulation (NEES) Program of the National
 * Science Foundation under Award Numbers CMS-0117853 and CMS-0402490.
 * 
 * CVS revision $Revision: 153 $
 */
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.NumberFormat;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.nees.time.TimeChangeListener;
import org.nees.time.TimeLine;
import org.nees.time.TimeLinePlayerPanel;
import org.nees.time.TimeScrollBar;
import org.nees.time.TimeUtility;

/**
 * This is a demo application that shows the capibilities of the TimeLine model,
 * the TimeLinePlayer controller, and contoller/viewer combinations embeded in
 * TimeLinePlayerPane, TimePointPanel, and TimeRangePanel. This essentially
 * uses the model-view-controller pardigm to implement a time scrolling applicatiom.
 * 
 * @author Terry E. Weymouth
 * 
 * @see org.nees.time.TimeLine
 * @see org.nees.time.TimeLinePlayerPanel
 * @see org.nees.time.TimePointPanel
 * @see import org.nees.time.TimeRangePanel 
 */
public class TimeScrollBarTest extends JFrame {


    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance();
    static {
        NUMBER_FORMAT.setMaximumFractionDigits(4);
        NUMBER_FORMAT.setMinimumFractionDigits(1);
    }
    
    JFrame frame;

    public static void main(String[] args) {
        TimeScrollBarTest s = new TimeScrollBarTest();
        s.init();
        s.start();
    }

    private void init() {
        frame = this;

        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        frame.setTitle("ScrollerTest");

        TimeLine tl = makeTimeLine();

        JPanel top = new JPanel();
        top.setBorder(BorderFactory.createLineBorder(Color.black));

        top.setLayout(new BoxLayout(top, BoxLayout.PAGE_AXIS));
        top.add(new TimeLabelPanel(tl)); // inner class
        top.add(new TimeScrollBar(tl));
        TimeLinePlayerPanel tlp = new TimeLinePlayerPanel(tl);
        tlp.getPlayer().setRealTime(false);
        top.add(tlp);

        new SelectionWatcher(tl); // inner class

        tl.forceFullNotify();

        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(top, BorderLayout.CENTER);

    }

    private TimeLine makeTimeLine() {
        long unixTime = System.currentTimeMillis(); // current time...
        double minTime = ((double)unixTime) / 1000.0; // in seconds...
        // truncate to nearest hour...
        minTime = Math.floor(minTime / (60.0 * 60.0)) * 60.0 * 60.0;
        
        // shift it a bit
        minTime += 1.000111111;
            
        // plus three days
        // double maxTime = minTime + (60.0 * 60.0 * 24.0 * 3.0);

        // plus 3 hours
        // double maxTime = minTime + (60.0 * 60.0 * 3.0);

        // plus 3 minutes
        double maxTime = minTime + (60.0 * 3.0);
        
        // plus 3 seconds
        // double maxTime = minTime + (3.0);
      
        TimeLine tl = new TimeLine(minTime, maxTime);
        
        System.out.println("Start Time: " + TimeUtility.timeToString(minTime));
        System.out.println("End Time: " + TimeUtility.timeToString(maxTime));
        System.out.println("Range : " + NUMBER_FORMAT.format(maxTime-minTime));
        System.out.println("");
        
        // generate some fake events;
        Random r = new Random();
        double fake = minTime;
        double range = maxTime - minTime;
        double chunck = range / 10; // roughly 5 events ??
        while (fake < maxTime) {
            Double event = new Double(fake);
            tl.addTimeEvent(fake, event);
            double inc = (Math.abs(r.nextGaussian()) + 1.0) * chunck;
            fake += inc;
        }
        fake = maxTime;
        Double event = new Double(fake);
        tl.addTimeEvent(fake, event);

        return tl;
    }

    private void start() {
        frame.pack();
        frame.setVisible(true);
    }

    // a demo listener; listening for changes in "event" on the time line
    private class SelectionWatcher implements TimeChangeListener {
        public SelectionWatcher(TimeLine tl) {
            tl.addTimeListener(this);
        }

        public void timeChanged(
            TimeLine t,
            boolean currentMarkChanged,
            boolean marksChanged) {
            if (currentMarkChanged) {
                String time =
                    TimeUtility.timeToString(t.getCurrentEventTime().doubleValue());
                System.out.println("New selection of Object at time: " + time);
            }
        }
    } // class SelectionWatcher

    // a demo listener; listening and displaying all the timeLine time paramters
    private class TimeLabelPanel extends JPanel implements TimeChangeListener {
        TimeLine timeLine;
        JLabel startTimeLabel,
            endTimeLabel,
            currentStartTimeLabel,
            currentEndTimeLabel,
            currentTimeLabel,
            selectedTimeLabel;

        TimeLabelPanel(TimeLine t) {
            timeLine = t;
            t.addTimeListener(this);
            setUpGraphics();
            setTimeFromTimeLine();
        }

        // TimeChangeListener Interface
        public void timeChanged(
            TimeLine t,
            boolean currentMarkChanged,
            boolean marksChanged) {
            setTimeFromTimeLine();
        }

        private void setUpGraphics() {
            JLabel stTitle = new JLabel("      Start Time: ");
            JLabel etTitle = new JLabel("        End Time: ");
            JLabel cstTitle = new JLabel(" Edit Start Time: ");
            JLabel cetTitle = new JLabel("   Edit End Time: ");
            JLabel ctTitle = new JLabel("Current Time: ");
            JLabel selTitle = new JLabel("Time of last selected item: ");

            String filler = TimeUtility.timeToString(0.0);
            startTimeLabel = new JLabel(filler);
            endTimeLabel = new JLabel(filler);
            currentStartTimeLabel = new JLabel(filler);
            currentEndTimeLabel = new JLabel(filler);
            currentTimeLabel = new JLabel(filler);
            selectedTimeLabel = new JLabel(filler);

            setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
            JPanel line = new JPanel();
            line.setLayout(new BoxLayout(line, BoxLayout.LINE_AXIS));
            line.add(stTitle);
            line.add(startTimeLabel);
            line.add(etTitle);
            line.add(endTimeLabel);

            add(line);

            line = new JPanel();
            line.setLayout(new BoxLayout(line, BoxLayout.LINE_AXIS));
            line.add(cstTitle);
            line.add(currentStartTimeLabel);
            line.add(cetTitle);
            line.add(currentEndTimeLabel);
            add(line);

            line = new JPanel();
            line.setLayout(new BoxLayout(line, BoxLayout.LINE_AXIS));
            line.add(ctTitle);
            line.add(currentTimeLabel);
            add(line);

            line = new JPanel();
            line.setLayout(new BoxLayout(line, BoxLayout.LINE_AXIS));
            line.add(selTitle);
            line.add(selectedTimeLabel);
            add(line);
        }

        private void setTimeFromTimeLine() {
            setStartTime(timeLine.getStartTime());
            setEndTime(timeLine.getEndTime());
            setCurrentStartTime(timeLine.getCurrentStartTime());
            setCurrentEndTime(timeLine.getCurrentEndTime());
            setCurrentTime(timeLine.getCurrentTime());
            setSelectedTime(timeLine.getCurrentEventTime().doubleValue());
        }

        private void setStartTime(double t) {
            startTimeLabel.setText(TimeUtility.timeToString(t));
        }

        private void setEndTime(double t) {
            endTimeLabel.setText(TimeUtility.timeToString(t));
        }

        private void setCurrentStartTime(double t) {
            currentStartTimeLabel.setText(TimeUtility.timeToString(t));
        }

        private void setCurrentEndTime(double t) {
            currentEndTimeLabel.setText(TimeUtility.timeToString(t));
        }

        private void setCurrentTime(double t) {
            currentTimeLabel.setText(TimeUtility.timeToString(t));
        }

        private void setSelectedTime(double t) {
            selectedTimeLabel.setText(TimeUtility.timeToString(t));
        }

    } // class TimeLabelPanel

}
