import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;
import java.text.SimpleDateFormat;

import org.nees.rbnb.ChannelUtility;

import com.rbnb.sapi.*;

/**
 * A compainoin to TestPush; captures all data from test source and report
 * number of channels, and the average frame rate and data rate on each channel.
 * 
 * @author Terry E Weymouth
 * @since January 27, 2005
 * @see TestPush
 */
public class TestPull {

	public static String sinkName = "TestPullSink";
	public static String sourceName = TestPush.sourceName;
	public static String channelName = TestPush.channelName;
	
	private String hostname;
	private String serverName;
    private String sourcePath;

	private Sink sink;
	private boolean connected = false;
	
	private Thread[] itsThread;
	private boolean runit;
	
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM d, yyyy h:mm:SS.sss aa");
	private static final TimeZone TZ = TimeZone.getTimeZone("GMT");

	static
	{
		DATE_FORMAT.setTimeZone(TZ);
	}

	public static void main(String[] args) {
		(new TestPull()).exec(args);
	}
	
    protected String getCVSVersionString()
    {
        return
            "  CVS information... \n" +
            "  $Revision: 153 $\n" +
            "  $Date: 2007-09-24 13:10:37 -0700 (Mon, 24 Sep 2007) $\n" +
            "  $RCSfile: TestPull.java,v $ \n";
    }

	private void printUsage()
	{
		System.out.println("");
		System.out.print("Usage: java TestPull ");
		System.out.print(" <hostname> <soucepath>");
		System.out.println("");		
		System.out.println("  hosetname - String e.g. neestpm.sdsc.edu");
        System.out.println("  sourcepath - String e.g. data/data.txt");
		System.out.println("");
		System.out.println("E.G. java TestPull neestpm.sdsc.edu data/data.txt");
		System.out.println("");
	}
	
	private void exec(String[] args)
	{
		if (parseArgs(args))
		{
			if (connect())
                getAllTimes();
		}
	}

	private boolean parseArgs(String[] args) {
		if (args.length < 2)
		{
			System.out.println("Missing required argument.");
			printUsage();
			return false;
		}
		if (args.length > 2)
		{
			System.out.println("Unexpected surplus arguments.");
		}

		hostname = args[0];
        sourcePath = args[1];

		System.out.println("");
		System.out.println("Args are:");
		System.out.println("  host = " + hostname);
        System.out.println("  source path = " + sourcePath);
		System.out.println("");

		serverName = hostname + ":3333";

		return true;
	}

	public boolean connect()
	{
		System.out.println("TestPull: Attempting to connect to server = "
			+ serverName + " as sink = " + sinkName + ".");
		connected = false;
		try {
			sink = new Sink();
			sink.OpenRBNBConnection(serverName,sinkName);
			System.out.println("TestPull: Connected to server = "
				+ serverName + " as sink = " + sinkName + ".");
			connected = true;
		} catch (SAPIException se) {
			se.printStackTrace();
		}
		return connected;
	}

	private void disconnect() {
		sink.CloseRBNBConnection();
		connected = false;
		sink = null;
	}
    
    public void getAllTimes()
    {
        if (!connected)
        {
            System.out.println("Not connected; request failed.");
            return;
        }
        
        try
        {
            double startTime = ChannelUtility.getEarliestTime(serverName,sourcePath);
            double endTime = ChannelUtility.getLatestTime(serverName,sourcePath);
            startTime -= 1.0;
            endTime += 1.0;
            double duration = endTime = startTime;

            long startTimeL = (long)(startTime*1000.0); // seconds to milliseconds            
            long endTimeL = (long)(endTime*1000.0);     // seconds to milliseconds            

            System.out.println("Attempting fetch for...");
            System.out.println("  Start time = " + DATE_FORMAT.format(new Date(startTimeL)));
            System.out.println("  End time = " + DATE_FORMAT.format(new Date(endTimeL)));

            ChannelMap cm = new ChannelMap();
            
            cm.Add(sourcePath);
            
            sink.Request(cm,startTime,duration,"absolute");
            
            ChannelMap req = sink.Fetch(10000,cm);
            if (req.GetIfFetchTimedOut())
                throw new Exception("Fetch timed out");
            
            int index = req.GetIndex(sourcePath);
            
            if (index < 0)
                throw new Exception("No data for searchPath = " + sourcePath);
            
            double times[] = req.GetTimes(index);
            
            System.out.println("Fetch returned " + times.length + " records");
            
            for (int i = 0; i < times.length; i++)
            {
                System.out.println(DATE_FORMAT.format(new Date((long)(times[i]*1000.0))));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
	
}
