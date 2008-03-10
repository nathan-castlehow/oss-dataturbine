/*
 * TimeLinePlayerPanel.java
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
 *   $RCSfile: TimeLinePlayerPanel.java,v $ 
 * 
 * Copied, modified, from code developed by Lars Schumann 2003/09/01
 */
package org.nees.time;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.ColorModel;
import java.awt.image.MemoryImageSource;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JSlider;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * This JPanel presents a VCR-like GUI to interface a TimeLinePlayer control.
 * It displays buttons for go-to-first, go-to-prev, play-backward, pause,
 * play-forward, go-to-next, go-to-last, all functions of the player. If also
 * implements a simple player listener that reflects the state of the player
 * in the GUI.
 * 
 * @author Terry E Weymouth
 */
public class TimeLinePlayerPanel 
    extends JPanel
    implements ActionListener, ChangeListener
{
    TimeLine timeLine;
    TimeLinePlayer player;

    JLabel playerFeedback;
    static final int SLIDER_MIN = 0;
    static final int SLIDER_MAX = 400;
    static final int SLIDER_INIT = 200;    //initial slider position

    static final double FACTOR_DIVIVEND = 100.0;

    double midRate;
    
    /**
     * Create a GUI based on a TimeLinePlayer controller
     * based on the TimeLine model supplied.
     * @param the TimeLine model interacting with the GUI.
     */
    public TimeLinePlayerPanel(TimeLine tl) {
        player = new TimeLinePlayer(tl);
        player.setRealTime(true);
        resetTimeLine(tl);
        addPlayerListener(player);
        setup();
    }

    /** @return the TimeLinePlayer for this panel */
    public TimeLinePlayer getPlayer() {
        return player;
    }

    /**
     * Switch this TimeLinePlayerPanel and its TimeLinePlayer so
     * that they reflect a new TimeLine. Discard the old time line.
     * 
     * @param t the new TimeLine
     */
    public void setNewTimeLine(TimeLine t) {
        resetTimeLine(t);
    }
    
    private void resetTimeLine(TimeLine t)
    {
        timeLine = t;
        double startTime = timeLine.getStartTime();
        double endTime = timeLine.getEndTime();
        midRate = (endTime - startTime) / 1000.0;
        player.setNewTimeLine(t);
        player.setPlayInterval(midRate);
    }

    private void setup() {
        int w = Toolbar.buttonWidth;
        int h = Toolbar.buttonHeight;

        FlowLayout layout = new FlowLayout(FlowLayout.CENTER, 0, 0);
        setLayout(layout);

        JLabel l = new JLabel("Play:");
        add(l);

        JPanel intervalPanel = new JPanel();
        intervalPanel.setLayout(new BoxLayout(intervalPanel, BoxLayout.X_AXIS));

        l = new JLabel("<html><font size=-2><b>slower</b></font></html>");
        intervalPanel.add(l);

        JSlider rateSlider = new JSlider(JSlider.HORIZONTAL,
                SLIDER_MIN, SLIDER_MAX, SLIDER_INIT);
        rateSlider.addChangeListener(this);
        intervalPanel.add(rateSlider);

        l = new JLabel("<html><font size=-2><b>faster</b></font></html>");
        intervalPanel.add(l);

        Border b =
            BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(2, 2, 2, 2),
                BorderFactory.createLineBorder(Color.BLACK)
            );
        intervalPanel.setBorder(b);

        add(intervalPanel);

        add(makeImageButton(Toolbar.imgFirst, Toolbar.cmdFirst, w, h));
        add(makeImageButton(Toolbar.imgPrev, Toolbar.cmdPrev, w, h));
        add(makeImageButton(Toolbar.imgBack, Toolbar.cmdBack, w, h));
        add(makeImageButton(Toolbar.imgPause, Toolbar.cmdPause, w, h));
        add(makeImageButton(Toolbar.imgPlay, Toolbar.cmdPlay, w, h));
        add(makeImageButton(Toolbar.imgNext, Toolbar.cmdNext, w, h));
        add(makeImageButton(Toolbar.imgLast, Toolbar.cmdLast, w, h));
        l = playerFeedback = new JLabel(stopIcon); // for player feedback
        l.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        add(l);
    }

    private JButton makeImageButton(
        int[] pixels,
        String command,
        int buttonWidth,
        int buttonHeight) {
        Image img =
            createImage(
                new MemoryImageSource(
                    buttonWidth,
                    buttonHeight,
                    ColorModel.getRGBdefault(),
                    pixels,
                    0,
                    buttonWidth));
        ImageIcon ic = new ImageIcon(img);
        JButton b = new JButton(ic);
        b.setActionCommand(command);
        b.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        b.addActionListener(this);
        return b;
    }

    public void stateChanged(ChangeEvent e) {
        if (!(e.getSource() instanceof JSlider)) return;
        JSlider source = (JSlider)e.getSource();
        if (!source.getValueIsAdjusting()) {
            double power = ((double)(source.getValue()))/FACTOR_DIVIVEND;
            double factor = Math.pow(10.0,power)/100.0;
            double rate = midRate*factor;
System.out.println("factor = " + factor + ", rate = " + rate);
            player.setPlayInterval(rate);          
        }
    }
    
    public void actionPerformed(ActionEvent ev) {
        if (ev.getSource() instanceof JButton) {
            JButton hit = (JButton)ev.getSource();
            if (hit.getActionCommand().equals(Toolbar.cmdFirst))
                player.toFirstFrame();
            if (hit.getActionCommand().equals(Toolbar.cmdPrev))
                player.toPreviousFrame();
            if (hit.getActionCommand().equals(Toolbar.cmdBack))
                player.playBackward();
            if (hit.getActionCommand().equals(Toolbar.cmdPause))
                player.pause();
            if (hit.getActionCommand().equals(Toolbar.cmdPlay))
                player.playForward();
            if (hit.getActionCommand().equals(Toolbar.cmdNext))
                player.toNextFrame();
            if (hit.getActionCommand().equals(Toolbar.cmdLast))
                player.toLastFrame();
        }
    }

    private void addPlayerListener(TimeLinePlayer thisPlayer) {
        TimeLinePlayerStateListener l = new TimeLinePlayerStateListener() {
            public void stateChanged(TimeLinePlayer p) {
                reflectStateChange();
            }
        };
        thisPlayer.addListener(l);
    }

    ImageIcon forwardIcon = TimeLinePlayerIcons.rightIcon();
    ImageIcon backwardIcon = TimeLinePlayerIcons.leftIcon();
    ImageIcon stopIcon = TimeLinePlayerIcons.stopIcon();
    
    private void reflectStateChange() {
        // when initializing...
        if (playerFeedback == null)
            return;
        ImageIcon i = stopIcon;
        if (player.isPlaying())
        {
            i = forwardIcon;
            if (!player.isPlayingForward())
                i = backwardIcon;
        }
        playerFeedback.setIcon(i);
    }

}
