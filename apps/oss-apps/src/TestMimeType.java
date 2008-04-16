import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.nees.rbnb.RBNBBase;

import com.rbnb.sapi.*;

import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * The "mime type" entry is read from the terminal input, posted, retreived,
 * and printed. It is posted to and retrieved from the RBNB server/port (-s/-p).
 */
public class TestMimeType extends RBNBBase{

	String postedMimeType = "";
	boolean running = false;
	boolean sourceConnected = false;
	boolean sinkConnected = false;
	
	BufferedReader in =
		new BufferedReader(new InputStreamReader(System.in));

	Source source;
	Sink sink;
	private static final String SOURCE_NAME = "TestSource";
	private static final String SINK_NAME = "TestSink";
	private static final String CHANNEL_NAME = "MimeType";
	
	Thread theThread;
	
	public static void main(String[] args) {
		TestMimeType t = new TestMimeType();
		if (t.parseArgs(args))
			t.exec();
	}

    protected String getCVSVersionString()
    {
        return
            "  CVS information... \n" +
            "  $Revision$\n" +
            "  $Date$\n" +
            "  $RCSfile: TestMimeType.java,v $ \n";
    }

	protected Options setOptions() {
		Options opt = setBaseOptions(new Options()); // uses h, s, p
		return opt;
	}

	protected boolean setArgs(CommandLine cmd) {
		if (!setBaseArgs(cmd)) return false;
		return true;
	}

	private void exec() {
		if (connect())
		{
			// have to send before listening for first time??
			startFetchThread();
			postedMimeType = "none";
			prime();
			while (!postedMimeType.equals("exit"))
			{
				postedMimeType = getMimeTypeFromUser();
				postMimeType(postedMimeType);
			}
			System.out.println("Stop send thread.");
		}
		else
		{
			System.out.println("Failed to connect, quiting.");
		}
	}

	private String getMimeTypeFromUser() {
		try{ Thread.sleep(200);} catch (Throwable ignore) {}
		System.out.println("Enter mime type to test (e.g. text/html); 'exit' to quit:");
		String mt = "none";
		try {
			mt = in.readLine();
		}
		catch (Throwable ignore){}
		return mt;
	}

	private void prime()
	{
		ChannelMap sMap = new ChannelMap();
		try {
			int index = sMap.Add(CHANNEL_NAME);
			sMap.PutTimeAuto("timeofday");
			double data[] = new double[1];
			data[0] = 0.0;
			sMap.PutDataAsFloat64(index,data);
			source.Flush(sMap);
		} catch (SAPIException e) {
			e.printStackTrace();
		}
	}

	private void postMimeType(String mt) {
		// Push mimetype to the server:
		ChannelMap sMap = new ChannelMap();
		try {
			System.out.println("-->Building send.");
			int index = sMap.Add(CHANNEL_NAME);
			sMap.PutMime(index,mt);
			sMap.PutTimeAuto("timeofday");
			double data[] = new double[1];
			data[0] = 0.0;
			sMap.PutDataAsFloat64(index,data);
			source.Flush(sMap);
			System.out.println("-->Sent data with mime type = " + mt);
		} catch (SAPIException e) {
			e.printStackTrace();
		}
	}
	
	private boolean connect()
	{
		source = new Source();
		sink = new Sink();

		try {
			// Create a source and connect:
			source.OpenRBNBConnection(getServer(),SOURCE_NAME);
			sourceConnected = true;
			System.out.println(
				"Source connected as " + SOURCE_NAME + " for " + CHANNEL_NAME);
			sink.OpenRBNBConnection(getServer(),SINK_NAME);
			sinkConnected = true;
			System.out.println(
				"Sink connected as " + SINK_NAME + " for request = "
					+ SOURCE_NAME + "/" + CHANNEL_NAME);
		} catch (SAPIException se) { se.printStackTrace(); }
		
		return connected();
	}

	private boolean connected() {
		return sourceConnected && sinkConnected;
	}

	private void disconnect() {
		source.CloseRBNBConnection();
		sourceConnected = false;		
		source = null;
		sink.CloseRBNBConnection();
		sinkConnected = false;		
		sink = null;		
	}

	private void startFetchThread()
	{
		
		if (!connected()) return;
		
		// Use this inner class to hide the public run method
		Runnable r = new Runnable() {
			public void run() {
			  runWork();
			}
		};
		running = true;
		theThread = new Thread(r, SOURCE_NAME + CHANNEL_NAME);
		theThread.start();
		System.out.println("Started sink thread.");
	}

	private void stopFetchThread()
	{
		running = false;
		theThread.interrupt();
		System.out.println("Stopping sink thread.");
	}
	
	private boolean isRunning()
	{
		return (connected() && running);
	}

	private void runWork ()
	{
		try {
			ChannelMap request = new ChannelMap();
			request.Add(SOURCE_NAME + "/" + CHANNEL_NAME);
			sink.Subscribe(request,0.0,0.0,"newest");
			while(isRunning())
			{
				System.out.println("<--Initiating Fetch request.");
				ChannelMap m = sink.Fetch(-1);
				System.out.println("<--Fetch complete.");
				String[] cl = m.GetChannelList();
				String mt;
				for (int i = 0; i < cl.length; i++)
				{
					mt = m.GetMime(i);
					if (mt.equals("exit"))
					{
						disconnect();
						stopFetchThread();
					}
					else
						System.out.println("<--Mime type on channel " + cl[i] + " is " + mt);
				}
			}
		} catch (SAPIException se) {
			se.printStackTrace(); 
		}
	}
	
}
