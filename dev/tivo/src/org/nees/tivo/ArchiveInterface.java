/*
 * ArchiveInterface.java
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
 *   $RCSfile: ArchiveInterface.java,v $ 
 * 
 */
package org.nees.tivo;

import java.util.Iterator;
import java.util.Vector;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * The mean by which an application can get information from a repository
 * on image sequences. Image sequences are aassumed to be stored as segments
 * which, in turn, are time-ordered sequences of images. Segments are named.
 * @author Terry E Weymouth
 * @version $Revision: 153 $ (CVS Revision number)
 */
public interface ArchiveInterface {
    public ArchiveSegmentInterface getSegmentByName(String name);
    public Iterator getSegmentsIterator(); // of ArchiveSegmentInterface
    public Vector getSegmentsVector(); // of ArchiveSegmentInterface
    public ArchiveSegmentInterface[] getSegmentsArray();
    public String nextDefaultSegmentName();

    /**
     * Retreive a property from this segment.
     * 
     * @param propertyKey the (String) key of the property to retreive.
     * 
     * @return the property value (String) associated with the propertyKey
     * 
     * @see java.util.Properties
     */
    public String getProperty(String propertyKey);

    /**
     * Add a property key-value pair to this segment.
     * 
     * @param propertyKey the (String) key of the property
     * @param propertyValue the (String) value of the property
     *
     * @see java.util.Properties
     */
    public void setProperty(String propertyKey, String propertyValue);

    public ArchiveSegmentInterface mergeOrderedSegments(
        String name, ArchiveSegmentInterface seg1, ArchiveSegmentInterface seg2)
        throws ArchiveException, FileNotFoundException, IOException;
    public ArchiveSegmentInterface importSegment(ArchiveSegmentImporter in);
    public void removeSegment(String name, boolean reallyRemove)
        throws ArchiveException;
    public ArchiveSegmentInterface makeNewCopyOfSegment(
        String fromName,
        String toName)
        throws ArchiveException, FileNotFoundException, IOException;
    public ArchiveSegmentInterface makeNewCopyOfSegment(
        String fromName,
        String toName,
        double startTime,
        double endTime)
        throws ArchiveException, FileNotFoundException, IOException;
}
