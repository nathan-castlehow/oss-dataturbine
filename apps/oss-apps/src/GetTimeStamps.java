import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;
import java.text.SimpleDateFormat;

import com.rbnb.sapi.*;
import com.rbnb.utility.ArgHandler; //for argument parsing
//import COM.Creare.Utility.ArgHandler; //for argument parsing

public class GetTimeStamps {

	private static final String SERVER_NAME = "neestpm.sdsc.edu";
	private static final String SERVER_PORT = "3333";
	private static final String SINK_NAME = "GetTimeStamps";
	
	private static final String REQUEST_DEFAULT = "absolute";
	
	private static final int DEFAULT_CACHE_SIZE = 900;
	private int cacheSize = DEFAULT_CACHE_SIZE;
	private static final int DEFAULT_ARCHIVE_SIZE = 0;
	private int archiveSize = DEFAULT_ARCHIVE_SIZE;
	private static final boolean DEFAULT_VERBOSE = false;
	private boolean verbose = DEFAULT_VERBOSE;
	
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM d, yyyy h:mm:ss.SSS aa");
	private static final TimeZone TZ = TimeZone.getTimeZone("GMT");

	private static final SimpleDateFormat INPUT_FORMAT = new SimpleDateFormat("yyyy-MM-dd:hh:mm:ss.SSS");

	static
	{
		DATE_FORMAT.setTimeZone(TZ);
		INPUT_FORMAT.setTimeZone(TZ);
	}

	private String serverName = SERVER_NAME;
	private String serverPort = SERVER_PORT;
	private String server = serverName + ":" + serverPort;
	private String sinkName = SINK_NAME;
	private String requestPath;
	
	private double start = -1.0;
	private double duration = 0.0;
	private String type;
	
	public static void main(String[] args) {
		GetTimeStamps g = new GetTimeStamps();
		if (!g.setArgs(args)) return;
		g.exec();
	}
	
	private void printUsage() {
		System.out.println("GetTimeStamps: usage is...");		
		System.out.println("GetTimeStamps ");
		System.out.println("[-s Server Hostname *" + SERVER_NAME + "] ");
		System.out.println("[-p Server Port Number *" + SERVER_PORT + "] ");
		System.out.println("[-n Sink Name *" + SINK_NAME + " ]");
		System.out.println("-r Request Path - required");
		System.out.println("-a Start Time - required");
		System.out.println("[-d duration in seconds defaults to 0.0]");
		System.out.println("[-z End Time]");
		System.out.println("[-t type of request *" + REQUEST_DEFAULT + " ]");
		System.out.println("Note: times can either be yyyy-mm-dd:hh:mm:ss.nnn or");
		System.out.println("an arbitraty floating point number");
		System.out.println("Start-end takes precidence over Start-duration");		
	}

	public boolean setArgs(String[] args) {
		
		boolean useEnd = false;
		double end = 0.0;
		
		//parse args
		try {
			ArgHandler ah=new ArgHandler(args);
			if (ah.checkFlag('h')) {
				printUsage();
				return false;
			}
			if (ah.checkFlag('s')) {
				String a=ah.getOption('s');
				if (a!=null) serverName=a;
			}
			if (ah.checkFlag('p')) {
				String a=ah.getOption('p');
				if (a!=null) serverPort=a;
			}
			if (ah.checkFlag('n')) {
				String a=ah.getOption('n');
				if (a!=null) sinkName=a;
			}
			if (ah.checkFlag('t')) {
				String a=ah.getOption('t');
				if (a!=null) type=a;
			}
			if (ah.checkFlag('r')) {
				String a=ah.getOption('r');
				if (a!=null) requestPath=a;
			}
			if (ah.checkFlag('d')) {
				String a=ah.getOption('d');
				if (a!=null)
				{
					try
					{
						double value = Double.parseDouble(a);
						duration = value;
					}
					catch (Exception ex)
					{
						System.out.println("Failed to parse duration (" + a + "): " + ex);
						return false;
					}
				}
			}
			if (ah.checkFlag('a')) {
				String a=ah.getOption('a');
				if (a!=null)
				{
					try
					{
						double value = getTimeOrDouble(a);
						start = value;
					}
					catch (Exception ex)
					{
						System.out.println("Failed to parse start time (" + a + "): " + ex);
						return false;
					}
				}
			}
			if (ah.checkFlag('z')) {
				String a=ah.getOption('z');
				if (a!=null)
				{
					try
					{
						double value = getTimeOrDouble(a);
						end = value;
						useEnd = true;
					}
					catch (Exception ex)
					{
						System.out.println("Failed to parse end time (" + a + "): " + ex);
						return false;
					}
				}
			}
		} catch (Exception e) {
			System.err.println("GetTimeStamps argument exception "+e.getMessage());
			e.printStackTrace();
			return false;
		}

		if (requestPath == null)
		{
			System.out.println("Request path is required");
			printUsage();
			return false;
		}

		if (start < 0.0)
		{
			System.out.println("Start Time is required");
			printUsage();
			return false;
		}
		
		if (useEnd)
		{
			if (start > end)
			{
				System.out.println("End time (" + end + ") must come after start (" 
					+ start +")");
				return false;
			}
			
			duration = end - start;
		}
		
		if (! ( type.equals("absolute")
			||	type.equals("newest")
			||	type.equals("oldest")
			||	type.equals("aligned")
			||	type.equals("after")
			||	type.equals("modified")
			||	type.equals("next")
			||	type.equals("previous")		
			))
		{
			System.out.println("Invalid type = " + type);
			printUsage();
			return false;
		}

		server = serverName + ":" + serverPort;
		if (verbose)
		{	
			System.out.println("GetTimeStamps on " + server + " as " + sinkName);
			System.out.println("  Requesting " + requestPath);
			System.out.println("  Times: start = " + start + "; duration = " + duration
				+ "; type = " + type);
			System.out.println("  Use GetTimeStamps -h to see optional parameters");
		}
		return true;
	}

	private void exec() {
		
		try
		{
			// Create a sink and connect:
			Sink sink=new Sink();
			sink.OpenRBNBConnection(server,sinkName);
			ChannelMap sMap = new ChannelMap();
			int index = sMap.Add(requestPath);
			sink.Request(sMap, start, duration, type);
			ChannelMap rMap = sink.Fetch(-1,sMap);
			double[] times = rMap.GetTimes(0);
			for (int i = 0; i < times.length; i++)
			{
				String indexString = (i<10)?"00"+i:((i<100)?"0"+i:""+i);
				double dtime = times[i];
				long m = (long)(dtime * 1000.0); // convert sec to millisec
				String timeString = DATE_FORMAT.format(new Date(m));
				System.out.println(indexString + ": " + timeString + " -- " + dtime);
			}
			sink.CloseRBNBConnection();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static double getTimeOrDouble(String arg) throws Exception
	{
		double value = 0.0;
		boolean gotit = false;
		String reason = null;
				
		try{
			Date d = INPUT_FORMAT.parse(arg);
			long t = d.getTime();
			value = ((double)t)/1000.0;
			gotit = true;
		} catch (Exception e1)
		{
			reason = e1.toString();
			gotit = false;
		}

		if (!gotit)
		try {
			value = Double.parseDouble(arg);
			gotit = true;
		} catch (Exception e2)
		{
			reason = reason + "; " + e2.toString();
			gotit = false;
		}

		if (!gotit) 
			throw(new Exception("Failed to parse time " + arg 
				+ "; exception:" + reason));		
		
		return value;

	} // getTimeOrDouble

}
