/*
 * VersionCheck.java
 * Created on Aug 1, 2005
 */
package org.nees.iSight;

import quicktime.QTSession;
import quicktime.util.QTBuild;

/**
 * A static functions to report or test java, QuickTime, QuickTime for Java, and OS
 * versions. Used to generate warnings for non-tested situations.
 * 
 * @author Terry E. Weymouth
 */
public class VersionCheck {

    public static void report()
    {
        try{

            System.out.println("Java version: " + System.getProperty("java.version"));
            System.out.println("Java vender: " + System.getProperty("java.vender"));
            System.out.println("Java class version: " + System.getProperty("java.class.version"));

            System.out.println("OS architecture: " + System.getProperty("os.arch"));
            System.out.println("OS name: " + System.getProperty("os.name"));
            System.out.println("OS version: " + System.getProperty("os.version"));

            QTSession.open();
            System.out.println("QT version: " +
                QTSession.getMajorVersion() + "." +
                QTSession.getMinorVersion()
            );
            System.out.println("QTJ version: " +
                QTBuild.getVersion() + "." +
                QTBuild.getSubVersion()
            );
            QTSession.close();
            
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    // use, for example: warn(1,5,7,6,"Mac OS X");
    public static void warn(int jMajorVersion, int jMinorVersion, 
            int qtVersion, int qtjVersion, String osNameTest)
    {
        // test java verion
        String javaVersion = System.getProperty("java.version");
        if (javaVersion == null) javaVersion = "Unavialable";
        try
        {
            int point = javaVersion.indexOf(".");
            int majorVersion = new Integer(javaVersion.substring(0,point)).intValue();
            javaVersion = javaVersion.substring(point+1);
            point = javaVersion.indexOf(".");
            int minorVersion = new Integer(javaVersion.substring(0,point)).intValue();
            if ((majorVersion < jMajorVersion) 
                    || ((majorVersion == jMajorVersion) && (minorVersion < jMinorVersion)))
            {
                System.out.println("Warning. Expecting at least java version = "
                        + jMajorVersion + "." + jMinorVersion
                        + "; instead, found version = "
                        + majorVersion + "." + minorVersion);
            }
        }
        catch (Exception e)
        {
            System.out.println("Warning: Could not verify java version. Version reported as: "
                   + javaVersion);
        }
        
        // test qt adn qtj versions
        try
        {
            QTSession.open();
            int qtv = QTSession.getMajorVersion();
            if (qtv < qtVersion)
                System.out.println("Warning. Quick Time Version. Expecting at least "
                        + qtVersion + " but found " + qtv);
            int qtjv = QTBuild.getVersion();
            if (qtjv < qtjVersion)
                System.out.println("Warning. Quick Time Version. Expecting at least "
                        + qtjVersion + " but found " + qtjv);
            QTSession.close();            
        } catch (Exception e)
        {
            System.out.println("Warning: Could not verify QuickTime and/or " +
                    "QuickTime-for-java version.");
        }
        
        String osName = System.getProperty("os.name");
        if (!osName.equals(osNameTest))
            System.out.println("Warning this code has only been tested on "
                    + osNameTest + ";"
                    + " instead, found os = " + osName);
    }
    
    public static void main (String[] args)
    {
        report();
        System.out.println("");
        System.out.println("Testing warn(1,5,7,6,\"Mac OS X\"); ");
        warn(1,5,7,6,"Mac OS X");
    }
}
