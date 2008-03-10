import com.rbnb.sapi.*;

/**
 * A compainoin to TestPull; generates data from test source. The user can control
 * number of channels, and the average frame rate and data rate on each channel.
 * 
 * @author Terry E Weymouth
 * @since January 27, 2005
 * @see TestPush
 */
public class TestPush {

	private double rate = 1000.0;
	private int items = 1;
	private int channels = 100;
	private int cache = 1000;
	private String hostname;
	private String serverName;
	public static String sourceName = "TestPushSource";
	public static String channelName = "Channel";

	private Source source;
	private boolean connected = false;
	
	private Thread[] itsThread;
	private boolean runit;
	private long loopWaitTime = 100;
	
	public static void main(String[] args) {
		(new TestPush()).exec(args);
	}
	
	private void printUsage()
	{
		System.out.println("");
		System.out.print("Usage: java TestPush ");
		System.out.print(" <hostname> <number of channels>");
		System.out.println(" <items per frame> <data rate> <cache size>");
		System.out.println("");		
		System.out.println("  hosetname - String e.g. neestpm.sdsc.edu");
		System.out.println("  number of channels - int - the number of RBNB channes on source");
		System.out.println("  items per frame - int - how many data items to pack in a frame");
		System.out.println("  data rate - float - number of data items per second");
		System.out.println("  cache size - int - RBNB cache, suggested value 1000");
		System.out.println("");
		System.out.println("E.G. java TestPush neestpm.sdsc.edu 5 1 100.0 1000");
		System.out.println("");
	}
	
	private void exec(String[] args)
	{
		if (parseArgs(args))
		{
			if (connect())
				startThreads();
		}
	}

	private boolean parseArgs(String[] args) {
		if (args.length < 5)
		{
			System.out.println("Missing required arguments.");
			printUsage();
			return false;
		}
		if (args.length > 5)
		{
			System.out.println("Unexpected surplus arguments.");
		}

		hostname = args[0];
		try
		{
			channels = Integer.parseInt(args[1]);
			items = Integer.parseInt(args[2]);			
			rate = Double.parseDouble(args[3]);
			cache = Integer.parseInt(args[4]);
		}
		catch (Throwable t)
		{
			t.printStackTrace();
			System.out.println();
			printUsage();
			return false;
		}

		if (channels < 1)
		{
			System.out.println("Nubmer of channels too small, Set to 1");
			channels = 1;
		}
		if (channels > 999)
		{
			System.out.println("Nubmer of channels too large, Set to 999");
			channels = 999;
		}
		if (items < 1)
		{
			System.out.println("Nubmer of items per frame too small, Set to 1");
			items = 1;
		}
		if (items > 999)
		{
			System.out.println("Nubmer of items per frame too large, Set to 999");
			items = 999;
		}
		
		if (cache < 0)
		{
			System.out.println("Nubmer of frames too small, Set to 0");
			cache = 0;
		}

		System.out.println("");
		System.out.println("Args are:");
		System.out.println("  host = " + hostname);
		System.out.println("  channels = " + channels);
		System.out.println("  items per frame = " + items);
		System.out.println("  data rate = " + rate);
		System.out.println("  cache size = " + cache);
		System.out.println("");

		serverName = hostname + ":3333";
		loopWaitTime = (long)(1000.0/rate); // milliseconds
		itsThread = new Thread[channels];

		return true;
	}

	public boolean connect()
	{
		System.out.println("TestPush: Attempting to connect to server = "
			+ serverName + " as source = " + sourceName + ".");
		connected = false;
		try {
			// Create a source and connect:
			if (cache == 0)
				source = new Source();
			else
				source= new Source(cache, "none", 0);
			source.OpenRBNBConnection(serverName,sourceName);
			System.out.println("TestPush: Connected to server = "
				+ serverName + " as source = " + sourceName + ".");
			connected = true;
		} catch (SAPIException se) {
			se.printStackTrace();
		}
		return connected;
	}

	private void disconnect() {
		source.CloseRBNBConnection();
		connected = false;
		source = null;
	}

	public void startThreads()
	{
		
		if (!connected) return;
		runit = true;

		String name;
		for (int i = 0; i < channels; i++)
		{
			if (i > 99) name = "" + i;
			else if (i > 9) name = "0" + i;
			else name = "00" + i;
			
			name = channelName + name;
			// Use this inner class to hide the public run method
			final String argName = name;
			Runnable r = new Runnable() {
				public void run() {
				  runWork(argName);
				}
			};
			itsThread[i] = new Thread(r, name);
			itsThread[i].start();
		}
		
		System.out.println("TestPush: Started threads.");
	}

	public void stopThreads()
	{
		runit = false;
		for (int i = 0; i < channels; i++)
		{
			itsThread[i].interrupt();
		}
		System.out.println("TestPush: Stopped threads.");
	}
	
	private void runWork (String channelName)
	{
		System.out.println("Running a thread for channel = " + channelName);
		try 
		{
			long waitTime, now, lastNow = System.currentTimeMillis();
			ChannelMap cMap = new ChannelMap();
			int index = cMap.Add(channelName);
			cMap.PutTimeAuto("timeofday");
			while(isRunning())
			{
				// Push data onto the server:
				for (int i = 0; i < items; i++)
				{
					//simulate a "zero cost" data source
					now = System.currentTimeMillis();
					waitTime = loopWaitTime - (now-lastNow);
					if (waitTime >= 0)
						Thread.sleep(waitTime);
					else
						System.out.println("Send loop behind by "
							+ (-waitTime) + " milliseconds.");
					lastNow = System.currentTimeMillis();
					// put data on channel
					double data[] = new double[1];
					data[0] = 100.0;
					cMap.PutDataAsFloat64(index,data);
				}
				source.Flush(cMap);
			}
		} catch (SAPIException se) {
			se.printStackTrace(); 
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isRunning()
	{
		return (connected && runit);
	}
	
}
