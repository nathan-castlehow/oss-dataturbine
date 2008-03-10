/**
 * 
 */
package org.nees.archive.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.Properties;

import org.nees.archive.inter.ArchiveException;
import org.nees.archive.inter.ArchiveImageInterface;
import org.nees.archive.inter.ArchiveItemInterface;
import org.nees.archive.inter.ArchiveSegmentInterface;

class ImageSequenceSegmentImpl implements ArchiveSegmentInterface {

        private final Archive archive;
        String theBasePath, theName;
        File theBaseDir = null;
        double startTime = 0;
        double endTime = 0;
        Properties properties = new Properties();

        public ImageSequenceSegmentImpl(Archive archive, String name, String base) throws ArchiveException {
            this.archive = archive;
            theBasePath = base;
            theName = name;
            theBaseDir = new File(theBasePath);
            // force possible failures early
            if ((theBaseDir == null) || !theBaseDir.exists())
                throw new ArchiveException(
                    "Invalid segment - no base directory: " + theBasePath);
//   TODO: restore this
//            if ((theBaseDir.listFiles(notHiddenFilter) == null)
//                || (theBaseDir.listFiles(notHiddenFilter).length == 0))
//                throw new ArchiveException(
//                    "Invalid segment - base dir is empty: " + theBasePath);
            // create (if needed), force load and save of properties; 
            // force possilbe failures early
            File prop = new File(theBaseDir,ArchiveUtility.PROPERTIES_FILE_NAME);
            if (!prop.exists())
            {
                try
                {
                    prop.createNewFile();                    
                }
                catch (IOException e)
                {
                    throw new ArchiveException(
                        "Properties file " + ArchiveUtility.PROPERTIES_FILE_NAME + " unavailable " +
                            "for segment = " + name + "; IOException " +
                            e.toString());                    
                }
            }
            try{
                loadProperties();
                saveProperties();
            }
            catch (IOException e1)
            {
                throw new ArchiveException(
                    "Properties file " + ArchiveUtility.PROPERTIES_FILE_NAME + " unavailable " +
                        "for segment = " + name + "; IOException " +
                        e1.toString());
            }
            // force init of start and end time; detect possible failures
            getStartTimeAsDouble();
            getEndTimeAsDouble();
        }

        private void loadProperties() throws IOException
        {
            File f = new File(theBaseDir, ArchiveUtility.PROPERTIES_FILE_NAME);
            FileInputStream in = new FileInputStream(f);
            properties.load(in);
        }

        private void saveProperties() throws IOException {
            File f = new File(theBaseDir, ArchiveUtility.PROPERTIES_FILE_NAME);
            FileOutputStream out = new FileOutputStream(f);
            properties.store(out,getName());
        }

        public boolean isDeleted() {
            return (this.archive.getDeletedSegmentByName(getName()) != null);
        }

        public void setName(String name) {
            theName = name;
        }
        public String getName() {
            return theName;
        }
        public String toString() {
            return getName();
        }

        public double getStartTimeAsDouble() {
            if (startTime == 0.0) {
                long time = 0;
                File bottom = ArchiveUtility.recursivlyFindLeast(theBaseDir);
                try {
                    time = ArchiveUtility.makeTimeFromFilename(bottom);
                } catch (ParseException e) {
                    System.out.println(
                        "GetStartTime parse error: "
                            + bottom.getAbsolutePath());
                    new ArchiveException("Invalid Archive Segment - can not get startTime");
                }
                startTime = ((double)time) / 1000.0;
            }
            return startTime;
        }

        public double getEndTimeAsDouble() {
            if (endTime == 0) {
                long time = 0;
                File top = ArchiveUtility.recursivlyFindGreatest(theBaseDir);
                try {
                    time = ArchiveUtility.makeTimeFromFilename(top);
                } catch (ParseException e) {
                    System.out.println(
                        "GetEndTime parse error: " + top.getAbsolutePath());
                    new ArchiveException("Invalid Archive Segment - can not get endTime");
                }
                endTime = ((double)time) / 1000.0;
            }
            return endTime;
        }

        public ArchiveItemInterface[] getSortedArray(
            long startTime,
            long endTime) {
            File[] f =
                ArchiveUtility.getSortedFileArray(
                    theBasePath,
                    startTime,
                    endTime);
            //System.out.println("Get sorted array, lenth = " + f.length);
            ArchiveImageInterface[] ret = new ArchiveImageInterface[f.length];
            for (int i = 0; i < f.length; i++) {
                try {
                    //System.out.println("Get image = " + f[i].getAbsolutePath());
                    ret[i] = (ArchiveImageInterface)new ImageCoverImpl(this.archive, f[i]);
                } catch (ArchiveException e) {
                    return null;
                }
            }
            return ret;
        }

        public ArchiveItemInterface getAtOrAfter(long time) {
            return getAtOrAfter(time, getStartTime(), getEndTime());
        }

        /* (non-Javadoc)
         * @see org.nees.tivo.ArchiveSegmentInterface#getAtOrAfter(long, long, long)
         */
        public ArchiveItemInterface getAtOrAfter(
            long time,
            long startTime,
            long endTime) {
            if (time < startTime)
                time = startTime;
            if (time > endTime)
                return null;
            File f = this.archive.recursivlyFindFirstGE(new File(theBasePath), time);
            if (f == null)
                return null;
            try {
                return (ArchiveImageInterface)new ImageCoverImpl(this.archive, f);
            } catch (ArchiveException e) {
                return null;
            }
        }

        /* (non-Javadoc)
         * @see org.nees.tivo.ArchiveSegmentInterface#getAtOrBefore(long)
         */
        public ArchiveItemInterface getAtOrBefore(long time) {
            return getAtOrBefore(time, getStartTime(), getEndTime());
        }

        /* (non-Javadoc)
         * @see org.nees.tivo.ArchiveSegmentInterface#getAtOrBefore(long, long, long)
         */
        public ArchiveItemInterface getAtOrBefore(
            long time,
            long startTime,
            long endTime) {
            if (time < startTime)
                return null;
            if (time > endTime)
                time = startTime;
            File f = this.archive.recursivlyFindFirstLE(new File(theBasePath), time);
            if (f == null)
                return null;
            try {
                return (ArchiveImageInterface)new ImageCoverImpl(this.archive, f);
            } catch (ArchiveException e) {
                return null;
            }
        }

        /**
         * @see org.nees.tivo.ArchiveSegmentInterface#compareTo(java.lang.Object)
         */
        public int compareTo(Object t) {
            if (!(t instanceof ArchiveSegmentInterface))
                throw new ClassCastException(
                    "CompareTo test object is not of class "
                        + "org.nees.tivo.ArchiveSegmentInterface; found "
                        + t.getClass().getName()
                        + " instead.");
            return compareTo((ArchiveSegmentInterface)t);
        }

        /**
         * @see org.nees.tivo.ArchiveSegmentInterface#compareTo(org.nees.tivo.ArchiveSegmentInterface)
         */
        public int compareTo(ArchiveSegmentInterface test) {
            return getName().compareTo(test.getName());
        }

        /* (non-Javadoc)
         * @see org.nees.tivo.ArchiveSegmentInterface#setStartTime(double)
         */
        public void setStartTime(double theTime) {
            startTime = theTime;
        }

        /* (non-Javadoc)
         * @see org.nees.tivo.ArchiveSegmentInterface#getDuration()
         */
        public long getDuration() {
            return getStartTime() - getEndTime();
        }

        /* (non-Javadoc)
         * @see org.nees.tivo.ArchiveSegmentInterface#getDurationAsDouble()
         */
        public double getDurationAsDouble() {
            return getStartTimeAsDouble() - getEndTimeAsDouble();
        }

        /* (non-Javadoc)
         * @see org.nees.tivo.ArchiveSegmentInterface#equals(org.nees.tivo.ArchiveSegmentInterface)
         */
        public boolean equals(ArchiveSegmentInterface test) {
            if (!(test instanceof ImageSequenceSegmentImpl))
                return false;
            ImageSequenceSegmentImpl t = (ImageSequenceSegmentImpl)test;
            return equals(t);
        }

        public boolean equals(ImageSequenceSegmentImpl test) {
            return getName().equals(test.getName());
        }

        /* (non-Javadoc)
         * @see org.nees.tivo.ArchiveSegmentInterface#getStartTime()
         */
        public long getStartTime() {
            return (long) (startTime * 1000.0);
        }

        /* (non-Javadoc)
         * @see org.nees.tivo.ArchiveSegmentInterface#getEndTime()
         */
        public long getEndTime() {
            return (long) (endTime * 1000.0);
        }

        /* (non-Javadoc)
         * @see org.nees.tivo.ArchiveSegmentInterface#getProperty(java.lang.String)
         */
        public String getProperty(String propertyKey) {
            return properties.getProperty(propertyKey);
        }

        /* (non-Javadoc)
         * @see org.nees.tivo.ArchiveSegmentInterface#setProperty(java.lang.String, java.lang.String)
         */
        public void setProperty(String propertyKey, String propertyValue) {
            if (propertyValue != null)
                properties.setProperty(propertyKey,propertyValue);
            try {
                saveProperties();
            } catch (IOException e) {
                //I have no idea what to do here... maybe it will be obvious on monday!
            }
        }

        public void setDeleted(boolean flag) {
            // TODO Auto-generated method stub
            
        }

        public String getInfo() {
            // TODO Auto-generated method stub
            return null;
        }

    }