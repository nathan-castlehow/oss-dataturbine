package org.nees.rbnb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.nees.daq.ISOtoRbnbTime;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Source;

public class FileToAudioOnRbnb  extends RBNBBase {
    private static final String SOURCE_NAME = "FileDump";
    private static final String CHANNEL_NAME = "data.txt";

    private static final String ARCHIVE_DIRECTORY = ".";
    private static final String DATA_FILE_NAME = "Data.txt";
    static final String DELIMITER = "\t"; // package scope: also used by RbnbToFile !!

    private static final long REPLAY_INTERVAL = 0;

    private String sourceName = SOURCE_NAME;
    private String channelName = CHANNEL_NAME;

    private static final int DEFAULT_CACHE_SIZE = 900;
    private int cacheSize = DEFAULT_CACHE_SIZE;
    private static final int DEFAULT_ARCHIVE_SIZE = DEFAULT_CACHE_SIZE*2;
    private int archiveSize = DEFAULT_ARCHIVE_SIZE;

    private String archiveDirectory = ARCHIVE_DIRECTORY;
    private String dataFileName = DATA_FILE_NAME;
    private String delimiter = DELIMITER;
    private BufferedReader rd;
    
    Source source = null;
    ChannelMap sMap;
    int index;
    boolean connected = false;
    boolean detach = true;
    
    long replayInterval = REPLAY_INTERVAL;
    
    public static void main(String[] args) {
        // start from command line
        FileToRbnb w = new FileToRbnb();
        if (w.parseArgs(args))
        {
//            w.connect();
//            w.doIt();
//            w.disconnect();
        }
    }
    
    protected String getCVSVersionString()
    {
        return
            "  CVS information... \n" +
            "  $Revision$\n" +
            "  $Date$\n" +
            "  $RCSfile: FileToRbnb.java,v $ \n";
    }

    /* (non-Javadoc)
     * @see org.nees.rbnb.RBNBBase#setOptions()
     */
    protected Options setOptions() {
        Options opt = setBaseOptions(new Options()); // uses h, s, p
        opt.addOption("n",true,"source_name *" + SOURCE_NAME);
        opt.addOption("c",true,"channel_name *" + CHANNEL_NAME);
        opt.addOption("d",true,"Archive directory root *" + ARCHIVE_DIRECTORY);
        opt.addOption("D",true,"Data input file name *" + DATA_FILE_NAME);
        opt.addOption("S",true,"Data item seperator (default is tab, \\t)");
        opt.addOption("R",true,"Replay interval in microseconds *" + REPLAY_INTERVAL);
        opt.addOption("Q",false,"Supply this flag to force the RBNB ring buffer " +
            "to close when the application quits.");
        opt.addOption("z",true,"cache size *" + DEFAULT_CACHE_SIZE);
        opt.addOption("Z",true,"archive size *" + DEFAULT_ARCHIVE_SIZE);
        setNotes("The reply interval pauses between each data sent. " +
            "To send all the data without pause set the replay interval to zero.");
        return opt;
    }

    /* (non-Javadoc)
     * @see org.nees.rbnb.RBNBBase#setArgs(org.apache.commons.cli.CommandLine)
     */
    protected boolean setArgs(CommandLine cmd) {
        if (!setBaseArgs(cmd)) return false;

        if (cmd.hasOption('n')) {
            String a=cmd.getOptionValue('n');
            if (a!=null) sourceName=a;
        }
        if (cmd.hasOption('c')) {
            String a=cmd.getOptionValue('c');
            if (a!=null) channelName=a;
        }
        if (cmd.hasOption('d')) {
            String a=cmd.getOptionValue('d');
            if (a!=null) archiveDirectory=a;
        }
        if (cmd.hasOption('D')) {
            String a=cmd.getOptionValue('D');
            if (a!=null) dataFileName=a;
        }
        if (cmd.hasOption('Q')){
            detach = false;
        }
        if (cmd.hasOption('R')) {
            String a=cmd.getOptionValue('R');
            if (a!=null)
            try
            {
                long value = Long.parseLong(a);
                replayInterval = value;
            }
            catch (Exception ignore) {} 
        }       
        if (cmd.hasOption('z')) {
            String a=cmd.getOptionValue('z');
            if (a!=null)
            try
            {
                Integer i =  new Integer(a);
                int value = i.intValue();
                cacheSize = value;
            }
            catch (Exception ignore) {} 
        }
        if (cmd.hasOption('Z')) {
            String a=cmd.getOptionValue('Z');
            if (a!=null)
            try
            {
                Integer i =  new Integer(a);
                int value = i.intValue();
                archiveSize = value;
            }
            catch (Exception ignore) {} 
        }
        
        if (detach && (archiveSize == 0)){
            System.out.println("Warning, the detach flag is true and there is " +
                "no archive; you will may not be able to attach to this Source");
        }
        
        if ((archiveSize > 0) && (archiveSize < cacheSize)){
            System.out.println("Archive size = " + archiveSize + "; it must be " +
                "bigger than chache size; chache size = " + cacheSize);
            return false;
        }
        
        System.out.println("Starting FileToRbnb on " + getServer()
            + " as " + sourceName);
        System.out.println("  Channel name = " + channelName
            + "  Cache Size = " + cacheSize + "; Archive size = " + archiveSize);
        if (replayInterval > 0)
            System.out.println("Replay pause interval = " + replayInterval);
        if (detach)
            System.out.println("The Source will be detached on exit");
        System.out.println("  Use FileToRbnb -h to see optional parameters");
        
        return true;
    }
    
    public void postFileWithDefaults(String serverName, String serverPort, 
            File file, String postChannelName)
    {
        if (setArgs(serverName, serverPort, SOURCE_NAME, postChannelName, 
                file.getParent(), file.getName(), REPLAY_INTERVAL,
                true, DEFAULT_CACHE_SIZE, DEFAULT_ARCHIVE_SIZE))
        {
            connect();
            doIt();
            disconnect();
        }
    }
    
    /**
     * Set the class instance parameters. 
     * 
     * @param serverName - the name of the RBNB server (e.g. neestpm.sdsc.edu)
     * @param serverPort - the (String) RBNB server port (e.g. 3333)
     * @param sourceName - the name of the RBNB source to generate
     * @param channelName - the name of the RBNB channel to generate
     * @param archiveDirectory - the name of the archive directory to use
     * @param dataFileName - the name of the file on the archive directory
     *      to use
     * @param replayInterval - if > 0, the milliseconds to pause between
     *      each data item (to simulate real time sending of data, e.g. 
     *      a replay)
     * @param keepLive - (booelan) if true, then keep the RBNB source and
     *      channel up after the call quits, that is "detach" instead of 
     *      close; if false, close (and loose) the source/channel at the
     *      end of the send
     * @param rbnbCacheSize - (long) the RBNB cache size
     * @param rbnbArchiveSize - (long) the RBNB archive size (if > 0, it
     *      must be >= the sache size)
     * 
     * @return (boolean) true if the parameters are valid; false otherwise
     * 
     */
    public boolean setArgs(String serverName, String serverPort, String  sourceName,
            String channelName, String archiveDirectory, String dataFileName,
            long replayInterval, boolean keepLive, int rbnbCacheSize,
            int rbnbArchiveSize)
    {
        this.setServerName(serverName);
        this.setServerName(serverPort);
        this.sourceName = sourceName;
        this.channelName = channelName;
        this.archiveDirectory = archiveDirectory;
        this.dataFileName = dataFileName;
        this.replayInterval = replayInterval;
        this.detach = !keepLive;
        this.cacheSize = rbnbCacheSize;
        this.archiveSize = rbnbArchiveSize;
        
        if (detach && (archiveSize == 0)){
            System.out.println("Warning, the detach flag is true and there is " +
                "no archive; you will may not be able to attach to this Source");
        }
        
        if ((archiveSize > 0) && (archiveSize < cacheSize)){
            System.out.println("Archive size = " + archiveSize + "; it must be " +
                "bigger than chache size; chache size = " + cacheSize);
            return false;
        }
        
        System.out.println("Starting FileToRbnb on " + getServer()
            + " as " + sourceName);
        System.out.println("  Channel name = " + channelName
            + "  Cache Size = " + cacheSize + "; Archive size = " + archiveSize);
        if (replayInterval > 0)
            System.out.println("Replay pause interval = " + replayInterval);
        if (detach)
            System.out.println("The Source will be detached on exit");
        System.out.println("  Use FileToRbnb -h to see optional parameters");
        
        return true;
    }

    private void connect()
    {
        System.out.println("FileToRbnb: Attempting to connect to server = "
            + getServer() + " as " + sourceName + " with " + channelName + ".");
        try {
            // Create a source and connect:
            if (archiveSize > 0)
                source=new Source(cacheSize, "create", archiveSize);
            else
                source=new Source(cacheSize, "none", 0);
            source.OpenRBNBConnection(getServer(),sourceName);
            connected = true;
            System.out.println("FileToRbnb: Connection made to server = "
                + getServer() + " as " + sourceName + " with " + channelName + ".");
        } catch (SAPIException se) { se.printStackTrace(); }
        
        if (connected)
        {
            // attmpt to open file
            String path = archiveDirectory + "/" + dataFileName;
            File probe = new File(path);

            // does it exist and is it readable
            if (probe != null && probe.exists() && probe.canRead())
            {
                if (rd != null)
                {
                    try {
                        rd.close();
                    } catch (IOException ignore) {}
                    rd = null;
                }
                try
                {
                    rd = new BufferedReader(new FileReader(probe));
                    System.out.println("Sucessfully connected to " + path);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    disconnect();
                }
                if (rd == null)
                {
                    System.out.println("Failed to open file stream " + path);
                    disconnect();
                }
            }
            else // data not available
            {
                System.out.println("Data unavailable: path...");
                if (probe == null)
                    System.out.println("Could not open file.");
                else if (!probe.exists())
                    System.out.println("File does not exist");
                else if (!probe.canRead())
                    System.out.println("File is unreadable");
                disconnect();
            }
        }
    }

    private void disconnect() {
        if (detach)
            source.Detach();
        else
            source.CloseRBNBConnection();
        connected = false;
        source = null;
        if (rd != null)
            {
                try {
                    rd.close();
                } catch (IOException ignore) {}
                rd = null;
            }
        }
    
    private void doIt ()
    {
        String in = "";
        boolean header = true;
        if (!connected)
            return;
        String[] units = new String[0];

        try {

            // Note does not process units! Units line in header
            // possible header lines are
            // Active channels: top_disp_X,lateral_load_X,...
            // Sample rate: 1.0
            // Channel units: in,ki ....

            // skip all headers
            while (header && ((in = rd.readLine()) != null)) {
                in = in.trim();
                if (in.length() == 0)
                    header = false;
                else {
                    String unitsHeader = "Channel units: ";
                    if (in.startsWith(unitsHeader)) {
                        String unitsString = in.substring(unitsHeader.length());
                        StringTokenizer unitsST = new StringTokenizer(
                                unitsString, ",");
                        Vector unitsV = new Vector();
                        while (unitsST.hasMoreElements()) {
                            unitsV.addElement(unitsST.nextElement());
                        }
                        units = (String[]) unitsV.toArray(new String[unitsV
                                .size()]);
                        if (unitsV.size() == 0)
                            System.out.println("Empty units header string?: "
                                    + in);
                    } else
                        System.out.println("Skipping header line: " + in);
                }
            }

            if (in == null) {
                return;
            }
            // the header ends with a blank line
            
            if ((in = rd.readLine()) == null) return;

            // the first non-header line should be a list of channel names
            System.out.println("Channel list line: " + in);

            // parse out the channel names and discard any path information
            // e.q. FromDaq/LoadCell_z will be sent as LoadCell_z
            StringTokenizer st = new StringTokenizer(in, delimiter); // seperated
            // by blanks
            Vector channelNames = new Vector();

            st.nextToken(); // Skip "Time" !!!

            while (st.hasMoreTokens())
                channelNames.add(st.nextToken());

            if (channelNames.size() == 0) {
                System.out.println("Empty channel list");
                return;
            }
            String[] channelNameArray = new String[channelNames.size()];

            Enumeration en = channelNames.elements();
            int pos = 0;
            String name;
            for (int i = 0; ((i < channelNameArray.length) && en
                    .hasMoreElements()); i++) {
                name = (String) en.nextElement();
                pos = name.lastIndexOf('/');
                if (pos > -1)
                    name = name.substring(pos + 1);
                channelNameArray[i] = name;
            }

            // what to do with the units...
            System.out.println("Warning: units are not posted");
            /*
            System.out.print("Units not posted, but they are: ");
            System.out.print(channelNameArray[0] + "= " + units[0]);
            int end = units.length;
            if (units.length != channelNameArray.length) {
                System.out.println("Warning, number of units does not match "
                        + "number of channels: units = " + units.length
                        + " channels = " + channelNameArray.length);
                if (end > channelNameArray.length)
                    end = channelNameArray.length;
            }
            for (int i = 1; i < end; i++) {
                System.out.print(", " + channelNameArray[i] + "= " + units[i]);
            }
            System.out.println();
            */
            
            // set up channels
            int[] channalIndexArray = new int[channelNameArray.length];
            ChannelMap map = new ChannelMap();
            for (int n = 0; n < channelNameArray.length; n++) {
                channalIndexArray[n] = map.Add(channelNameArray[n]);
                System.out.println("Added Channel " + channelNameArray[n]
                        + " with index = " + channalIndexArray[n]);
            }
            source.Register(map);

            while (((in = rd.readLine()) != null) && connected) {
                st = new StringTokenizer(in, " ");

                // the first token is the time stamp
                String tsString = st.nextToken();
                ISOtoRbnbTime timestamp = new ISOtoRbnbTime(tsString);
                if (!timestamp.is_valid) {
                    System.out.println("Warning: timestamp not valid: "
                            + tsString);
                }
                double time = timestamp.getValue();
                map.PutTime(time, 0.0);

                System.out.println("Time = " + ChannelUtility.niceTime(time));

                // The rest of line is floting point data values
                int i = 0;
                while (st.hasMoreTokens()) {
                    String item = st.nextToken().trim();
                    if ((item != null) && (item.length() == 0))
                        item = null;
                    if (item != null) {
                        // post data to channel i
                        double[] data = new double[1];
                        try{
                            data[0] = Double.parseDouble(st.nextToken());
                            map.PutDataAsFloat64(channalIndexArray[i], data);
                        } catch (NumberFormatException ignore) {}
                    }
                    i++;
                }
                source.Flush(map);
                try {
                    if (replayInterval > 0)
                        Thread.sleep(replayInterval);
                } catch (Exception ignore) {
                }
            }
        } catch (Exception e) {
            System.out.println("IO Error: " + e);
        }

    }
    
}
