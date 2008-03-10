/*
 * ArchiveUtility.java
 * Created on May 13, 2005; major revision in March 2006
 * 
 * This work is supported in part by the George E. Brown, Jr. Network
 * for Earthquake Engineering Simulation (NEES) Program of the National
 * Science Foundation under Award Numbers CMS-0117853 and CMS-0402490.
 * 
 * Source information...
 *   $LastChangedRevision: 153 $
 *   $LastChangedDate: 2007-09-24 13:10:37 -0700 (Mon, 24 Sep 2007) $
 *   $HeadURL: file:///Users/hubbard/code/cleos-svn/cleos-rbnb-apps-dev/archive/src/org/nees/archive/impl/ArchiveUtility.java $
 *   $LastChangedBy: ljmiller $
 *  
 */
package org.nees.archive.impl;

import java.io.File;
import java.io.FileFilter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Vector;

public class ArchiveUtility
{
    public static final String DEFAULT_ARCHIVE_NAME = "ArchiveStore";
    public static final String PROPERTIES_FILE_NAME = ".properties";

	public static final String EXTENSION= ".jpg"; 
	
	private static final SimpleDateFormat COMMAND = new SimpleDateFormat("yyyy-MM-dd:HH:mm:ss.SSS");
	private static final SimpleDateFormat FILE = new SimpleDateFormat("yyyyMMddHHmmssSSS");
	private static final SimpleDateFormat YEAR = new SimpleDateFormat("yyyy");
	private static final SimpleDateFormat MONTH = new SimpleDateFormat("MM");
	private static final SimpleDateFormat DAY = new SimpleDateFormat("dd");
	private static final SimpleDateFormat HOUR = new SimpleDateFormat("HH");
	private static final SimpleDateFormat MIN = new SimpleDateFormat("mm");
	private static final TimeZone TZ = TimeZone.getTimeZone("GMT");
    static public final SimpleDateFormat DATE_FORMAT =
        new SimpleDateFormat("MMM d, yyyy h:mm:ss.SSS aa");

	static
	{
		COMMAND.setTimeZone(TZ);
		FILE.setTimeZone(TZ);
		YEAR.setTimeZone(TZ);
		MONTH.setTimeZone(TZ);
		DAY.setTimeZone(TZ);
		HOUR.setTimeZone(TZ);
		MIN.setTimeZone(TZ);
        DATE_FORMAT.setTimeZone(TZ);
	}
//
//  ------------------------  Utility Methods --------------------------------
//
//  These utility methods know about the directory strucutre of the archive and
//  are used by this class, by JpgLoaderSource, and by the package org.nees.tivo
//  They should probably be moved to some abstract class in that package or to
//  the implementation of ArchiveInterace, Archive, in that package.
//  It's a work in progress - tew - May 8, 2005.
//

    private static String getVersionString()
    {
        return
            "Version information... \n" +
            " $LastChangedRevision:543 $\n" +
            " $LastChangedDate:2006-03-13 14:08:34 -0500 (Mon, 13 Mar 2006) $\n" +
            " $HeadURL:https://svn.nees.org/svn/telepresence/dataturbine-dev/archive/src/org/nees/archive/inter/ArchiveAudioStreamInterface.java $\n" +
            " $LastChangedBy:weymouth $\n";
    }
                
	private static FileFilter archiveFileFilter = new FileFilter(){
        public boolean accept(File f) {
            if (f.isDirectory()) return true;
            if (f.getName().endsWith(".jpg")) return true;
            if (f.getName().endsWith(".jpeg")) return true;
            if (f.getName().endsWith(".JPG")) return true;
            if (f.getName().endsWith(".JPEG")) return true;
            return false;
        }
	};

	public static File makePathFromTime(String base, long time)
	{
		Date t = new Date(time);
		String path = 
			YEAR.format(t) + File.separator + 
			MONTH.format(t) + File.separator + 
			DAY.format(t) + File.separator + 
			HOUR.format(t) + File.separator + 
			MIN.format(t) + File.separator + 
			FILE.format(new Date(time)) + EXTENSION;
		File ret = new File(base, path);
		return ret;
	}
	
	public static long makeTimeFromFilename(File f) throws ParseException
	{
		String filename = f.getName();
		// remove assumed extension
		if (filename.endsWith(EXTENSION))
			filename = filename.substring(0,filename.length()-EXTENSION.length());
		return (FILE.parse(filename)).getTime();
	}
	
	public static SimpleDateFormat getCommandFormat()
	{
		return COMMAND;
	}
	
	public static boolean confirmCreateDirPath(File testPath)
	{
		if (testPath.exists()) return true;
		return testPath.mkdirs();
	}

	public static File[] getSortedFileArray(String baseDir,long startTime, long endTime)
	{
		File[] fl = getFiles(baseDir,startTime,endTime);
		return sortFiles(fl);
	} // end sortFiles() 

	private static File[] getFiles(String baseDir, long startTime, long endTime)
	{
		Vector allFiles = 
			recursivelyFindFile(new Vector(), new File(baseDir), startTime, endTime);
		File[] fl = new File[allFiles.size()];
		fl = (File[]) allFiles.toArray(fl);
		return fl;
	}

	private static Vector recursivelyFindFile(Vector v, File f, 
		long startTime, long endTime)
	{
		File[] fileList = f.listFiles(archiveFileFilter);
		if (fileList == null) // this is not a directory!!
		{
			long time;
			try {
				time = makeTimeFromFilename(f);
				if ((startTime <= time) && (time <= endTime))
				{
					v.add(f);
				}
			} catch (ParseException e) {
				System.out.println("Parse Exception on conversion of filename to" +
					"time = " + f.getName());
			}
			return v;
		}
		for (int i = 0; i < fileList.length; i++)
		{
			v = recursivelyFindFile(v,fileList[i],startTime,endTime);
		}
		return v;
	}

	public static File[] sortFiles(File[] fl)
	{
		// implements a simple insersion sort
		// the list before the dividing line is sorted, lexicially
		// least to greatest
		int in, out;
		for(out=1; out<fl.length; out++) // out is dividing line
		{
			File temp = fl[out];		// remove marked for insertion
				// determin where to insert
			in = out;				// start shifting at out (backwards)
			while(in>0 &&			// until smaller one found,
				fl[in-1].toString().compareTo(temp.toString())>0)
			{
				fl[in] = fl[in-1];		// shift item to the right
				--in;					// go left one position
			}
			fl[in] = temp;        // insert marked item
		} // end for
		return fl;
	}

	public static File recursivlyFindGreatest(File file) {
		File[] fileList = file.listFiles(archiveFileFilter);
		if (fileList == null) return file;
		File top = fileList[0];
		for (int i = 1; i < fileList.length; i++)
		{
			if (top.toString().compareTo(fileList[i].toString()) < 0) top = fileList[i];
		}
		return recursivlyFindGreatest(top);
	}

	public static File recursivlyFindLeast(File file) {
		File[] fileList = file.listFiles(archiveFileFilter);
		if (fileList == null) return file;
		File bottom = fileList[0];
		for (int i = 1; i < fileList.length; i++)
		{
			if (bottom.toString().compareTo(fileList[i].toString()) > 0) bottom = fileList[i];
		}
		return recursivlyFindLeast(bottom);
	}
	}