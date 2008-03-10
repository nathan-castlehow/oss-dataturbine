/*
 * Version.java
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
 *   $RCSfile: Version.java,v $ 
 * 
 */
package org.nees.tivo;

import java.util.StringTokenizer;

/**
 * The "release" version of this package, and some utility methods. The version
 * is a string composed of a header, a major version number (int), a minor
 * version numer (int), and a release number (int), in this form:
 * header + major + "." + minor + "." + release, e.g. Version 1.2.3
 * The default constructor delivers the current version.
 * 
 * @author Terry Weymouth
 */
public class Version {
    // There should be some way to get this from CVS!!!
    private int major = 0;
    private int minor = 2;
    private int release = 0;
    private static String header = "Version ";

    /** The default Version is the current version */
    public Version() {}
    /**
     * Construct an arbitrary Version
     * @param major (int) the major version number
     * @param minor (int) the minor version number
     * @param release (int) the release number
     */
    public Version(int major, int minor, int release)
    {
        this.major = major;
        this.minor = minor;
        this.release = release;
    }
    
    /**
     * @return the String of the current version;
     * for example "Version 1.2.3"
     */
	public String toString()
	{
		return header + major + "." + minor + "." + release; 
	}
	
    /**
     * A static function to get the current (defaul) Version as a String
     * @return the String representation of the current version
     */
   	public static String versionString()
	{
		return (new Version()).toString();
	}
    
    /**
     * Parses a String into a Version.
     * 
     * @param s -- a string representation of the version (e.g. 
     * "Version 1.2.3"
     * @return the Version corresponding to the String or null (if the parse
     * fails)
     */
    public static Version parseVersion(String s)
    {
        if (!s.startsWith(header)) return null;
        s = s.substring(header.length());
        StringTokenizer t = new StringTokenizer(s,".");
        String sma,smi,sre;
        int ma, mi, re;
        Version ret = null;
        try
        {
            if ((sma = t.nextToken()) == null) return null;
            if ((smi = t.nextToken()) == null) return null;
            if ((sre = t.nextToken()) == null) return null;
            ma = Integer.parseInt(sma);
            mi = Integer.parseInt(smi);
            re = Integer.parseInt(sre);
            ret = new Version(ma, mi, re);
        } catch (Throwable x) {return null;}
        return ret;
    }

    /**
     * @return true if the test object is a Version or a String
     * that matches this Version.
     */    
    public boolean equals(Object o)
    {
        if (o instanceof Version)
        {
            Version test = (Version)o;
            return equals(test);
        }
        if (o instanceof String)
        {
            String st = (String)o;
            return equals(st);
        }
        return false;
    }

    /**
     * @return true is the String matches this Version.
     */
    public boolean equals(String s)
    {
        Version test = parseVersion(s);
        if (test == null) return false;
        return equals(test);
    }
    
    /**
     * @return true if the Version matches this Version.
     */    
    public boolean equals(Version test)
    {
        if (this.major != test.major) return false;
        if (this.minor != test.minor) return false;
        if (this.release != test.release) return false;
        return true;
    }
    
    /** @return the header protion of the version as a String */
    public static String getHeader() { return header; }
    /** @return the major version number as an int */
    public int getMajor() { return major; }
    /** @return the minor version number as an int */
    public int getMinor() { return minor; }
    /** @return the release number as an int */
    public int getRelease() { return release; }

}

