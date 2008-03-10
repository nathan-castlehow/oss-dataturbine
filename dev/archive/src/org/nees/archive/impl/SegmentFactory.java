package org.nees.archive.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;

import org.nees.archive.inter.ArchiveProperty;
import org.nees.archive.inter.ArchiveSegmentInterface;
import org.nees.archive.inter.ArchiveItemInterface;
import org.nees.archive.inter.ArchiveItemInterface.TYPE;

public class SegmentFactory {

    public static String getVersionString()
    {
        return
            "Version information... \n" +
            " $LastChangedRevision:543 $\n" +
            " $LastChangedDate:2006-03-13 14:08:34 -0500 (Mon, 13 Mar 2006) $\n" +
            " $HeadURL:https://svn.nees.org/svn/telepresence/dataturbine-dev/archive/src/org/nees/archive/inter/ArchiveAudioStreamInterface.java $\n" +
            " $LastChangedBy:weymouth $\n";
    }
    
    public static ArchiveSegmentInterface 
    createSegmentFromFile(File file) {
        // verify file
        if (!file.exists())
        {
            System.out.println("Segment Factory - file (" +
                    file.getName()+") not found. Returning null.");
            return null;            
        }
        if (!file.canRead())
        {
            System.out.println("Segment Factory - can not write to file (" +
                    file.getName()+"). Returning null.");
            return null;            
        }
        // check for properties file
        Properties properties = new Properties();
        try {
            File f = new File(file, ArchiveUtility.PROPERTIES_FILE_NAME);
            System.out.println("Looking for properties file in " + file.getName());
            FileInputStream in;
            in = new FileInputStream(f);
            properties.load(in);
        } catch (FileNotFoundException e) {
            System.out.println("Segment Factory - Properties file not found. Returning null.");
            return null;
        } catch (IOException e) {
            System.out.println("Segment Factory - Segment file IO Error. Returning null.");
            return null;
        }

        // confirm required properties type, startTime, endTime
        String typeString = null;
        TYPE type = null;
        try
        {
            typeString = properties.getProperty(ArchiveProperty.PROPERTY_TYPE);
            type = TYPE.valueOf(TYPE.class,typeString);
        } catch (Throwable t)
        {
            System.out.println("Segment Factory - no Segment type = " + typeString);
            return null;
        }
        String startTimeString 
            = properties.getProperty(ArchiveProperty.PROPERTY_START_TIME);
        String endTimeString
            = properties.getProperty(ArchiveProperty.PROPERTY_END_TIME);
        long startTime = 0;
        long endTime = 0;
        try
        {
            startTime = systemTimeFromString(startTimeString);
            endTime = systemTimeFromString(endTimeString);
        }
        catch (Throwable t)
        {
            System.out.println("Segment Factory; ill specified time(s)" +
                    " StartTime = " + startTimeString +
                    "; EndTime = " + endTimeString);
            return null;
        }

        // create from file based on type

        return (ArchiveSegmentInterface) 
            new SegmentCover(type, startTime, endTime, file);
    }

    private static long systemTimeFromString(String timeString) 
    throws ParseException
    {
        SimpleDateFormat f = ArchiveUtility.DATE_FORMAT;
        Date now = new Date();
        Date d = f.parse(timeString);
        return d.getTime();
    }

}
