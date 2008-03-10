/*
 * ArchiveInterface.java
 * Created February, 2006
 * 
 * This work is supported in part by the George E. Brown, Jr. Network
 * for Earthquake Engineering Simulation (NEES) Program of the National
 * Science Foundation under Award Numbers CMS-0117853 and CMS-0402490.
 * 
 * Source information...
 *   $LastChangedRevision:543 $
 *   $LastChangedDate:2006-03-13 14:08:34 -0500 (Mon, 13 Mar 2006) $
 *   $HeadURL:https://svn.nees.org/svn/telepresence/dataturbine-dev/archive/src/org/nees/archive/inter/ArchiveInterface.java $
 *   $LastChangedBy:weymouth $
 * 
 */
package org.nees.archive.inter;

import java.util.Iterator;
import java.util.Vector;

/**
 * The means by which an application can get information from a repository
 * on data Segments. Each archive segment is a named collection of time-ordered data
 * (numeric, image, or audio data). A segment has a startTime and 
 * duration (with endTime = startTime + duration).
 *
 * @see ArchiveSegmentInterface
 * 
 * @author Terry E Weymouth
 * @version $LastChangedRevision:543 $ (Source Revision number)
 */
public interface ArchiveInterface {
    
    /**
     * Get the specified archive segment.
     * 
     * @param name (String) the name of the segment.
     * @return the segment specified by the name given.
     */
    public ArchiveSegmentInterface getSegmentByName(String name);
    
    /**
     * Get an Iterator over the archive segments in this archive.
     * 
     * @return the Iterator.
     */
    public Iterator<ArchiveSegmentInterface> getSegmentsIterator();

    /**
     * Get a Vector of the archive segments in this archive.
     * 
     * @return the Vector.
     */
    public Vector<ArchiveSegmentInterface> getSegmentsVector();

    /**
     * Get an array of the archive segments in this archive.
     * 
     * @return the array.
     */
    public ArchiveSegmentInterface[] getSegmentsArray();

    /**
     * Get the next available default segment name for this archive.
     * 
     * @return (String) the name.
     */
    public String nextDefaultSegmentName();

    /**
     * Retreive a property from the archive.
     * 
     * @param propertyKey the (String) key of the property to retreive.
     * @return the property value (String) associated with the propertyKey
     * 
     * @see ArchiveProperty
     * @see java.util.Properties
     */
    public String getProperty(String propertyKey);

    /**
     * Set (replace) a property key-value pair to the archive.
     * 
     * @param propertyKey the (String) key of the property
     * @param propertyValue the (String) value of the property
     *
     * @see ArchiveProperty
     * @see java.util.Properties
     */
    public void setProperty(String propertyKey, String propertyValue);

    /**
     * Merge two segments into a new segment that preserves the data and ordering of
     * the original segments. It is assumed that the underlying implementation does
     * something to perserve, or propigate the properties of the two segments.
     * 
     * @param name (String) of the new segment
     * @param seg1 the first of the segments to merge
     * @param seg2 the second of the segments to merge
     * @return the new segment
     * @throws ArchiveException
     * @throws FileNotFoundException
     * @throws IOException
     */
    public ArchiveSegmentInterface mergeSegments(
        String name, ArchiveSegmentInterface seg1, ArchiveSegmentInterface seg2)
        throws ArchiveException;
    
    /**
     * Import the segment described by the ArchiveSegmentImporter; this should
     * be implemented in such a way that multiple imports can happen at the same time
     * and will not (within the limits of the computing, network, and disk resources)
     * interfear with each other. In other words, no critical resources should be
     * locked, no critical path blocked.
     * 
     * @param in the representation of the source to import a new segment
     * @return the new segment
     * 
     * @throws ArchiveException
     */
    public ArchiveSegmentInterface importSegment(ArchiveSegmentImporter in)
        throws ArchiveException;

    /**
     * Remove a segment from the archive. An implementation of this method would best
     * mark the segment as removed and leave the actual removal to a background thread
     * or a defered garbage collection process.
     * 
     * @param name (String) the name of the segment to remove from the archive
     * @throws ArchiveException
     */
    public void removeSegment(String name)
        throws ArchiveException;

    /**
     * In the case that any segments are marked deleted, this method effects
     * their actual removel. It blocks until all marked segments are removed.
     *
     * @see purge(String)
     * @see purge(ArchiveSegmentInterface)
     * 
     * @throws ArchiveException
     */
    public void purge()
        throws ArchiveException;
    
    /**
     * In the case that the named segment is marked deleted, this method effects
     * its actual removel. It blocks until that marked segments is removed.
     *
     * @see purge()
     * @see purge(ArchiveSegmentInterface)
     * 
     * @throws ArchiveException
     */
    public void purge(String segmentName)
        throws ArchiveException;

    /**
     * In the case that the indicated segment is marked deleted, this method effects
     * its actual removel. It blocks until that marked segments is removed.
     *
     * @see purge()
     * @see purge(String)
     * 
     * @throws ArchiveException
     */
    public void purge(ArchiveSegmentInterface seg)
        throws ArchiveException;

    /**
     * Make a new (deep) copy of the segment indicated by fromName and name it toName.
     * The copy process should create copies of all of the elements of the segment.
     * Throws a ArchiveException exception if fromName does not exist, if toName already
     * exists, or if the segment can not be copied in its entirety.
     * 
     * @param fromName (String) the name of the segment to be copied
     * @param toName (String) the name of the new segment
     * @return
     * @throws ArchiveException
     */
    public ArchiveSegmentInterface makeNewCopyOfSegment(
        String fromName,
        String toName)
        throws ArchiveException;

    /**
     * Make a new (deep) copy of the data in the segment indicated by fromName and 
     * bounded by startTime and endTime. Include data whos startTime falls on or after
     * the startTime of the interval. Include data whos startTime falls before the
     * end time (but who duration may extend past the endTime). Name the new segment
     * toName. The copy process should create copies of all of the elements of the segment.
     * Throws a ArchiveException exception if fromName does not exist, if toName already
     * exists, or if the segment can not be copied in its entirety.
     * 
     * @param fromName
     * @param toName
     * @param startTime
     * @param endTime
     * @return
     * @throws ArchiveException
     */
    public ArchiveSegmentInterface makeNewCopyOfSegment(
        String fromName,
        String toName,
        double startTime,
        double endTime)
        throws ArchiveException;
}
