/*
 * FileFilterTest.java
 * Created on Jul 12, 2005
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
 *   $RCSfile: FileFilterTest.java,v $ 
 */
 
import java.io.FileFilter;
import java.io.File;

/**
 * @author Terry E. Weymouth
 */
public class FileFilterTest {

    private FileFilter archiveFileFilter = new FileFilter(){
        public boolean accept(File f) {
            if (f.isDirectory()) return true;
            if (f.getName().endsWith(".jpg")) return true;
            if (f.getName().endsWith(".jpeg")) return true;
            if (f.getName().endsWith(".JPG")) return true;
            if (f.getName().endsWith(".JPEG")) return true;
            System.out.println("Not matched: " + f.getAbsolutePath());
            return false;
        }
    };

    public static void main(String[] args)
    {
//        String filename = "ArchiveStore";
        String filename = args[0];
        FileFilterTest t = new FileFilterTest();
        File f = new File(filename);
        System.out.println("Testing folder: " + f.getAbsolutePath());
        System.out.println();
        t.exec(f);
    }
    
    public void exec(File f)
    {
        recursivlyTouchFiles(f);
    }

    public void recursivlyTouchFiles(File file)
    {
        File[] fileList = file.listFiles(archiveFileFilter);
        if (fileList == null) return;
        for (int i = 1; i < fileList.length; i++)
        {
            recursivlyTouchFiles(fileList[i]);
        }
    }
}
