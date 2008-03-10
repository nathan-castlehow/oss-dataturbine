package org.nees.archive.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.nees.archive.inter.ArchiveException;
import org.nees.archive.inter.ArchiveItemInterface;
import org.nees.archive.inter.ArchiveSegmentInterface;
import org.nees.archive.inter.ArchiveItemInterface.TYPE;

public class SegmentCover implements ArchiveSegmentInterface {

    private TYPE type;
    private long startTime;
    private long endTime;
    private File file;
    private String name;
    private Properties properties;
    private boolean deleted = false;
    
    public SegmentCover(TYPE type, long startTime, long endTime, File file) {
        this.type = type;
        this.startTime = startTime;
        this.endTime = endTime;
        this.file = file;
        setName(file.getName());
    }

    public String toString()
    {
        return name + "(" + file.getName() + ")";
    }
    
    public String getInfo()
    {
        SimpleDateFormat f = ArchiveUtility.DATE_FORMAT;
        Date start = new Date(startTime);
        Date end = new Date(endTime);
        
        return "Segment: " + toString() + ": \n" +
                "  Type = " + type + "\n" +
                "  StarTime = " + f.format(start) + "\n" +
                "  EndTime = " + f.format(end);
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getProperty(String propertyKey) throws ArchiveException {
        if (properties == null)
        {
            properties = new Properties();
            File f = new File(file, ArchiveUtility.PROPERTIES_FILE_NAME);
            System.out.println("Looking for properties file in " + f.getAbsolutePath());
            FileInputStream in;
            try {
                in = new FileInputStream(f);
                properties.load(in);
            } catch (FileNotFoundException e) {
                throw new ArchiveException("No Properties file", e);
            } catch (IOException e) {
                throw new ArchiveException("Malforned properties file",e);
            }
        }
        return properties.getProperty(propertyKey);
    }

    public void setProperty(String propertyKey, String propertyValue) throws ArchiveException {
        if (properties == null)
        {
            properties = new Properties();
            File f = new File(file, ArchiveUtility.PROPERTIES_FILE_NAME);
            System.out.println("Looking for properties file in " + f.getAbsolutePath());
            FileInputStream in;
            try {
                in = new FileInputStream(f);
                properties.load(in);
            } catch (FileNotFoundException e) {
                throw new ArchiveException("No Properties file", e);
            } catch (IOException e) {
                throw new ArchiveException("Malforned properties file",e);
            }
        }
        properties.setProperty(propertyKey,propertyValue);
    }

    public long getStartTime() {
        return startTime;
    }

    public double getStartTimeAsDouble() {
        return (((double)getStartTime())/1000.0); // convert milliseconds to seconds
    }

    public long getDuration() {
        return getEndTime() - getStartTime();
    }

    public double getDurationAsDouble() {
        return (((double)getDuration())/1000.0); // convert milliseconds to seconds
    }

    public long getEndTime() {
        return endTime;
    }

    public double getEndTimeAsDouble() {
        return (((double)getEndTime())/1000.0); // convert milliseconds to seconds
    }

    public ArchiveItemInterface[] getSortedArray(long startTime, long endTime) {
        // TODO Auto-generated method stub
        return null;
    }

    public ArchiveItemInterface getAtOrAfter(long time, long startTime, long endTime) {
        // TODO Auto-generated method stub
        return null;
    }

    public ArchiveItemInterface getAtOrAfter(long time) {
        // TODO Auto-generated method stub
        return null;
    }

    public ArchiveItemInterface getAtOrBefore(long time, long startTime, long endTime) {
        // TODO Auto-generated method stub
        return null;
    }

    public ArchiveItemInterface getAtOrBefore(long time) {
        // TODO Auto-generated method stub
        return null;
    }

    public int compareTo(Object test) {
        // TODO Auto-generated method stub
        return 0;
    }

    public int compareTo(ArchiveSegmentInterface test) {
        // TODO Auto-generated method stub
        return 0;
    }

    public boolean equals(ArchiveSegmentInterface test) {
        if (!(test instanceof SegmentCover)) return false;
        SegmentCover realTest = (SegmentCover)test;
        return realTest.file.equals(this.file);
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean flag) {
        deleted = flag;
    }

}
