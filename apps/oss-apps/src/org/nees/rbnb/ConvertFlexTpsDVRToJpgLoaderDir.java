/*
 * Created on Jan 12, 2006
 */

package org.nees.rbnb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
// Yutaka
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.StringTokenizer;
import java.util.TimeZone;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

/**
 * ConvertFlexTpsDVRToJpgLoaderDir
 * @author Terry E. Weymouth
 *
 * Convert a FlexTps DVR directory structure to the archive directory structure
 * that supports JpgLoaderSource (see JpgSaverSink)
 * 
 * @see JpgLoaderSource
 * @see JpgSaverSink
 */
public class ConvertFlexTpsDVRToJpgLoaderDir {

    File fromDir = null;
    File toDir = null;
    File timestampFile = null;
    
    private static final String FROM_TIMESTAMP_FILENAME = "Timestamps.txt";
    
    public static void main(String[] args) {
        // start from command line
        ConvertFlexTpsDVRToJpgLoaderDir a = new ConvertFlexTpsDVRToJpgLoaderDir();
        if (a.parseArgs(args))
        {
            a.exec();
        }
        else a.printUsage();
    }

    protected String getCVSVersionString()
    {
        return
            "  CVS information... \n" +
            "  $Revision$\n" +
            "  $Date$\n" +
            "  $RCSfile:  $ \n";
    }

    protected void printUsage() {
        HelpFormatter f = new HelpFormatter();
        f.printHelp(this.getClass().getName(),setOptions());
    }

    protected boolean parseArgs(String[] args) throws IllegalArgumentException
    {
        try {
            CommandLine cmd = (new PosixParser()).parse(setOptions(), args);
            return setArgs(cmd);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Argument Exception: " + e);
        }
    }
    
   /* (non-Javadoc)
     * @see org.nees.rbnb.RBNBBase#setOptions()
     */
    protected Options setOptions() {
        Options opt = new Options();
        opt.addOption("F",true,"From: base dir path of the FlexTps DVR directory");
        opt.addOption("T",true,"To: base dir path of the JpgLoaderSource dirctory");
        return opt;
    }

    /* (non-Javadoc)
     * @see org.nees.rbnb.RBNBBase#setArgs(org.apache.commons.cli.CommandLine)
     */
    protected boolean setArgs(CommandLine cmd)
    {
        String fromBase = null;
        if (cmd.hasOption('F')) {
            String a=cmd.getOptionValue('F');
            if (a!=null) fromBase=a;
        }

        String toBase = null;
        if (cmd.hasOption('T')) {
            String a=cmd.getOptionValue('T');
            if (a!=null) toBase=a;
        }

        if ((fromBase == null) && (toBase == null))
        {
            System.out.println("Both from base dir and to base dir are required.");
            return false;
        }
        
        if (fromBase == null)
        {
            System.out.println("From base dir is required.");
            return false;
        }
        
        if (toBase == null)
        {
            System.out.println("From to dir is required.");
            return false;
        }
        
        fromDir = new File(fromBase);
        toDir = new File(toBase);
        
        if (!fromDir.exists())
        {
            System.out.println("From dir, " + fromBase + ", does not exist.");
            return false;
        }
        
        if (!fromDir.isDirectory())
        {
            System.out.println("From dir, " + fromBase + ", is not a directory.");
            return false;
        }
        
        timestampFile = new File(fromDir,FROM_TIMESTAMP_FILENAME);
        if (!timestampFile.exists())
        {
            System.out.println("The timstamp file for the FlexTPX DRV, " 
                    + timestampFile.getAbsolutePath() + ", does not exist.");
            return false;
        }
        
        if (!toDir.exists())
        {
            System.out.println("To dir, " + toBase + ", does not exist.");
            return false;
        }
        
        if (!toDir.isDirectory())
        {
            System.out.println("To dir, " + toBase + ", is not a directory.");
            return false;
        }
        
        if (!toDir.canWrite())
        {
            System.out.println("To dir, " + toBase + ", is not writable.");
            return false;
        }
        
        System.out.println("Attempting to copy-convert from FlexTPS to JpgLoader format.");
        System.out.println("From: " + fromDir.getPath());
        System.out.println("To: " + toDir.getPath());
        System.out.println("Using timestamp file: " + timestampFile.getPath() );
        return true;
    }

    private void exec() {
        int fileCount = 0;
        String inLine = null;
        try {
            BufferedReader in = new BufferedReader(new FileReader(timestampFile));
            // spik header line
            in.readLine();
            while (null != (inLine = in.readLine()))
            {
                Holder h = parseLine(inLine);
                if ((h.filename == null) || (h.timestamp == 0))
                {
                    System.out.println("Could not get filename and/or timestamp: " +
                            inLine);
                    continue;
                }
                if (copyFile(h.timestamp, h.filename, fromDir, toDir))
                    fileCount++;
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found " + e);
            System.out.println("Process aborted.");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("IO Exception " + e);
            System.out.println("Process aborted.");
            e.printStackTrace();
        } catch (ParseException e) {
            System.out.println("Parse Exception " + e);
            System.out.println("... while parsing this line: " + inLine);
            System.out.println("Process aborted.");
            e.printStackTrace();
        } finally
        {
            System.out.println("Copied " + fileCount + " files.");
        }
    }
    
    private Holder parseLine(String inLine) throws ParseException {
        StringTokenizer st = new StringTokenizer(inLine);
        
        Holder h = new Holder();
        h.filename = null;
        h.timestamp = 0;
     
        if (st.hasMoreElements())
        {
            String timestampStr = st.nextToken();
            if (timestampStr != null)
                h.timestamp = parseISO8601Date(timestampStr);
        }
        else return h;

        if (st.hasMoreElements())
            st.nextToken();
        else return h;

        if (st.hasMoreElements())
            h.filename = st.nextToken();

        return h;
    }

    private boolean copyFile(long timestamp, String filename, File fromDir2, File toDir2) throws IOException {
        File from = new File(fromDir,filename);
        if (!from.exists())
        {
            System.out.println("Copy: from File does not exist " 
                    + from.getPath() + " -- skipping.");
            return false;
        }
        
        File to = ArchiveUtility.makePathFromTime(toDir.getAbsolutePath(),timestamp);
        ArchiveUtility.confirmCreateDirPath(to.getParentFile());
// Yutaka        
        FileInputStream fromReader = new FileInputStream(from);
        FileOutputStream toWriter = new FileOutputStream(to);
        
        int count = 0;
// Yutaka        
        byte[] buffer = new byte[1024*128];
        while (0 < (count = fromReader.read(buffer)))
        {
            toWriter.write(buffer,0,count);
        }
        
        System.out.println("Copied from " + from.getPath() + " to " + to.getPath());
        
        return true;
    }

    private long parseISO8601Date(String dateString) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date time = sdf.parse(dateString);
// Yutaka
//        String subSecondsString = dateString.substring(20, dateString.length()-1);
        String subSecondsString = dateString.substring(20, 23);
        int subSeconds = Integer.parseInt(subSecondsString);

        return time.getTime() + subSeconds;
    }

    private class Holder
    {
        long timestamp;
        String filename;
    }
}
