/*
 * ViewerUtilites.java
 * Created on Jun 3, 2005
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
 *   $RCSfile: ViewerUtilities.java,v $ 
 * 
 */
package org.nees.tivo;

/**
 * @author Terry E Weymouth
 */
public class ViewerUtilities {
    public static String html(String s) {
        return "<html>" + s + "</html>";
    }
    public static String red(String s) {
        return "<font color=red>" + s + "</font>";
    }
    public static String gray(String s) {
        return "<font color=gray>" + s + "</font>";
    }
    public static String big(String s) {
        return "<font size=+1>" + s + "</font>";
    }
    public static String small(String s) {
        return "<font size=-1>" + s + "</font>";
    }
    public static String verySmall(String s) {
        return "<font size=-2>" + s + "</font>";
    }
    public static String bold(String s) {
        return "<b>" + "</b>";
    }
    public static String htmlRed(String s) {
        return html(red(s));
    }
    public static String htmlGray(String s) {
        return html(gray(s));
    }
    public static String htmlSmall(String s) {
        return html(small(s));
    }
    public static String htmlVerySmall(String s) {
        return html(verySmall(s));
    }
    public static String htmlSmallBold(String s) {
        return html(small(bold(s)));
    }
}
