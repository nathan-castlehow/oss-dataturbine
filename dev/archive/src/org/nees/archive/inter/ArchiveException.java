/*
 * ArchiveException.java
 * Created February, 2006
 * 
 * This work is supported in part by the George E. Brown, Jr. Network
 * for Earthquake Engineering Simulation (NEES) Program of the National
 * Science Foundation under Award Numbers CMS-0117853 and CMS-0402490.
 * 
 * Source information...
 *   $LastChangedRevision:543 $
 *   $LastChangedDate:2006-03-13 14:08:34 -0500 (Mon, 13 Mar 2006) $
 *   $HeadURL:https://svn.nees.org/svn/telepresence/dataturbine-dev/archive/src/org/nees/archive/inter/ArchiveException.java $
 *   $LastChangedBy:weymouth $
 * 
 */
package org.nees.archive.inter;

/**
 * A subclass to identify Exceptions specific to the Archive.
 * @author Terry E Weymouth
 * @version $LastChangedRevision:543 $ (Source Revision number)
 */

public class ArchiveException extends Exception {
    public ArchiveException() {
        super();
    }
    public ArchiveException(String message) {
        super(message);
    }
    public ArchiveException(Throwable t)
    {
        super(t);
    }
    public ArchiveException(String message, Throwable t) {
        super(message,t);
    }
}