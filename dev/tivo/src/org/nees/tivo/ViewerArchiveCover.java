/*
 * ViewerArchiveCover.java
 * Created May, 2005
 * Last Updated: June 2, 2005
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
 *   $RCSfile: ViewerArchiveCover.java,v $ 
 * 
 */
package org.nees.tivo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

class ViewerArchiveCover implements ArchiveInterface {
    ArchiveInterface itsArchive;
    HashMap segments = new HashMap();
    HashMap deletedSegments = new HashMap();

    protected ViewerArchiveCover(ArchiveInterface a) {
        itsArchive = a;
        updateMapFromTrueArchive();
    }

    protected void updateMapFromTrueArchive() {
        System.out.println("Update segment list");
        Archive a = (Archive)itsArchive;
        a.updateSegmentsFromDir();
        Iterator it = itsArchive.getSegmentsIterator();
        while (it.hasNext()) {
            ArchiveSegmentInterface segI = (ArchiveSegmentInterface)it.next();
            String name = segI.getName();
            if (getSegmentByName(name) == null) {
                ViewerSegmentHolder h = new ViewerSegmentHolder(segI);
                segments.put(h.getName(), h);
            }
        }
    }

    public ArchiveSegmentInterface getSegmentByName(String name) {
        // may return null!
        return (ArchiveSegmentInterface)segments.get(name);
    }

    public Iterator getSegmentsIterator() {
        return segments.values().iterator();
    }

    public Vector getSegmentsVector() {
        return new Vector(segments.values());
    }

    public ArchiveSegmentInterface[] getSegmentsArray() {
        Vector v = getSegmentsVector();
        return (ArchiveSegmentInterface[])v.toArray(
            new ArchiveSegmentInterface[v.size()]);
    }

    public void addCoveredSegment(ViewerSegmentHolder seg) {
        segments.put(seg.getName(), seg);
    }

    public String nextDefaultSegmentName() {
        return itsArchive.nextDefaultSegmentName();
    }

    public ArchiveSegmentInterface mergeOrderedSegments(
        String name, ArchiveSegmentInterface seg1, ArchiveSegmentInterface seg2)
        throws ArchiveException, FileNotFoundException, IOException
    {
        ArchiveSegmentInterface seg =
            itsArchive.mergeOrderedSegments(name, seg1, seg2);
        if (seg == null)
            return null;
        ViewerSegmentHolder segh = new ViewerSegmentHolder(seg);
        addCoveredSegment(segh);
        return segh;
    }

    public ArchiveSegmentInterface importSegment(ArchiveSegmentImporter in) {
        ArchiveSegmentInterface seg = itsArchive.importSegment(in);
        if (seg == null)
            return null;
        ViewerSegmentHolder segh = new ViewerSegmentHolder(seg);
        addCoveredSegment(segh);
        return segh;
    }

    public void removeSegment(String name, boolean reallyRemove)
        throws ArchiveException {
        ViewerSegmentHolder seg = (ViewerSegmentHolder)getSegmentByName(name);
        if (seg == null)
            return;
        segments.remove(name);
        if (!reallyRemove)
            deletedSegments.put(name, seg);
        itsArchive.removeSegment(name, reallyRemove);
    }

    public ArchiveSegmentInterface makeNewCopyOfSegment(
        String fromName,
        String toName)
        throws ArchiveException, FileNotFoundException, IOException {
        ArchiveSegmentInterface seg =
            itsArchive.makeNewCopyOfSegment(fromName, toName);
        if (seg == null)
            return null;
        ViewerSegmentHolder segh = new ViewerSegmentHolder(seg);
        addCoveredSegment(segh);
        return segh;
    }

    public ArchiveSegmentInterface makeNewCopyOfSegment(
        String fromName,
        String toName,
        double startTime,
        double endTime)
        throws ArchiveException, FileNotFoundException, IOException {
        ArchiveSegmentInterface seg =
            itsArchive.makeNewCopyOfSegment(
                fromName,
                toName,
                startTime,
                endTime);
        if (seg == null)
            return null;
        ViewerSegmentHolder segh = new ViewerSegmentHolder(seg);
        addCoveredSegment(segh);
        return segh;
    }

    public String getProperty(String propertyKey) {
        return itsArchive.getProperty(propertyKey);
    }

    public void setProperty(String propertyKey, String propertyValue) {
        itsArchive.setProperty(propertyKey,propertyValue);
    }
    
} // ViewerArchiveCover