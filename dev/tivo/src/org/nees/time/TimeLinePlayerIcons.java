/*
 * TimeLinePlayerIcons.java
 * Created on Jun 13, 2005
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
 *   $RCSfile: TimeLinePlayerIcons.java,v $ 
 * 
 */
package org.nees.time;

import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 */
public class TimeLinePlayerIcons {

    public static ImageIcon stopIcon()
    {
        String name = "images/ArrowStop.gif";
        return createImageIcon(name,name);
    }

    public static ImageIcon leftIcon()
    {
        String name = "images/ArrowLeft.gif";
        return createImageIcon(name,name);
    }

    public static ImageIcon rightIcon()
    {
        String name = "images/ArrowRight.gif";
        return createImageIcon(name,name);
    }

    protected static ImageIcon createImageIcon(String path,
                                               String description) {
        URL imgURL = TimeLinePlayer.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL, description);
        } else {
            return null;
        }
    }
         
    public static void main(String[] args) {
        //      Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                test();
            }
        });
    }
    
    public static void test()
    {
        //      Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);

        //Create and set up the window.
        JFrame frame = new JFrame("Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JPanel top = new JPanel();
        top.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        JLabel label = new JLabel("start");
        top.add(label);

        ImageIcon i = TimeLinePlayerIcons.leftIcon();
        label = new JLabel(i);
        top.add(label);
        
        i = TimeLinePlayerIcons.stopIcon();
        label = new JLabel(i);
        top.add(label);
        
        i = TimeLinePlayerIcons.rightIcon();
        label = new JLabel(i);
        top.add(label);
        
        label = new JLabel("end");
        top.add(label);
        
        //Display the window.
        frame.getContentPane().add(top);
        frame.pack();
        frame.setVisible(true);
    }
    
}
