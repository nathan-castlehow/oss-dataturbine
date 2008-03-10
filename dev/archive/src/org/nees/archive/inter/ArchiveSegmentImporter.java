/*
 * ArchiveSegmentImporter.java
 * Created February, 2006
 * 
 * This work is supported in part by the George E. Brown, Jr. Network
 * for Earthquake Engineering Simulation (NEES) Program of the National
 * Science Foundation under Award Numbers CMS-0117853 and CMS-0402490.
 * 
 * Source information...
 *   $LastChangedRevision:543 $
 *   $LastChangedDate:2006-03-13 14:08:34 -0500 (Mon, 13 Mar 2006) $
 *   $HeadURL:https://svn.nees.org/svn/telepresence/dataturbine-dev/archive/src/org/nees/archive/inter/ArchiveSegmentImporter.java $
 *   $LastChangedBy:weymouth $
 * 
 */
package org.nees.archive.inter;

import java.util.Properties;
import java.util.Vector;

/**
 * @author Terry E Weymouth
 * @version $LastChangedRevision:543 $ (Source Revision number)
 */
public interface ArchiveSegmentImporter {

    // moved to ArchiveProperty
    // public final static String PROPERTY_KEY_Date_Created = "DateCreated";
    // public final static String PROPERTY_KEY_Server = "SourceServer";
    // public final static String PROPERTY_KEY_Channel = "SourceChannel";

    public long getStartTime();
    public double getStartTimeAsDouble();
    public long getEndTime();
    public double getEndTimeAsDouble();
    public long getDuration();
    public double getDurationAsDouble();
    public Properties getProperties();
    public boolean isRunning();
    public boolean isDone();
    public float getPercentDone();
    public void startImport()
        throws ArchiveException;
    public void stopImport();
    public Vector<ArchiveSegmentInterface> getSegments()
        throws ArchiveException;
}
