package edu.ucsd.osdt.db.rbnb;

import com.rbnb.sapi.*;

import java.text.DecimalFormat;
import java.util.StringTokenizer;
import java.io.*;
import java.util.LinkedList;
import edu.ucsd.osdt.db.log.SysDLogger;

public class SinkClientManager {

	Sink sink;
	String className = "<SinkClientManager>";
	
	// local variables that need to be configured from the other parts later
	//String server = "niagara.sdsc.edu:3333";

	String server = "niagara.sdsc.edu:3333";

	// these should be specified elsewhere
	String sourceName = "FakeDAQ";
	String sinkName = "FakeDAQ";
	String sourceChannel0 = "0";
	String sourceChannel1 = "1";
	String sourcePath0 = sourceName + "/" + sourceChannel0;
	String sourcePath1 = sourceName + "/" + sourceChannel1;

	double lastTimeStamp;
	
	double startedAt0, endedAt0;

	SysDLogger sLogger = new SysDLogger("niagara.sdsc.edu", 514);
	
	public static void main(String[] args){(new SinkClientManager()).exec();}

	private void exec() {
		
		sink = new Sink();

		openRBNBConn(sink);
	
		ChannelMap cMap = createChannelMap(null);
	

		requestStartHelper startHelper = new requestStartHelper();
	
		double requestStartTime = startHelper.findStartTime(cMap);
		// double requestStartTime = startHelper.findStartTime(cMap);
		
		
		System.out.println (requestStartTime);
		
		sink.CloseRBNBConnection();
		sLogger.sysDMessage(className + ": Connection to " + server+ " is closed");
	} // exec


	public void openRBNBConn (Sink s) {
		try{
			sink.OpenRBNBConnection(server,sinkName);
			System.out.println("Connected");
			sLogger.sysDMessage(className + ": Connection to " + server+ " is opened");

		}

		catch(SAPIException t)
		{
			// 
			t.printStackTrace();
			System.out.println("Early abort!");

			sLogger.sysDMessage(className + ": Waiting to connect to " + server);
			waitForMins (1);
			
			openRBNBConn(s);
		}
	
	}
	

	public void waitForMins(int min) {
		try{

			System.out.println ("waiting");
			System.out.println(System.currentTimeMillis());
			Thread.sleep(min*1*1000);
			System.out.println(System.currentTimeMillis());
			
			System.out.println("Done.");

		
		}

		catch(InterruptedException e)
		{
			e.printStackTrace();
			System.out.println("Thread waitForMins function was interrupted");
			sLogger.sysDMessage(className + ": Waiting to connect to " + server);
		}
	}
	

	private ChannelMap createChannelMap (LinkedList channelNames)
	{
		// channel map names should come from the configuration steps
		// 
		// from the previous steps, the errors regarding the names should have
		// been verified.

		// create one channel map for two 
		ChannelMap cMap = new ChannelMap();

		try {
			int cMapIndex0 = cMap.Add(sourcePath0);
			int cMapIndex1 = cMap.Add(sourcePath1);
			
			System.out.println("First channel map index: " + cMapIndex0);
			System.out.println("Second channel map index: " + cMapIndex1);
			
		}
		
		catch (SAPIException e){
			// If there is a problem parsing the channel name.
			e.printStackTrace();
		}
		
		catch (NullPointerException e) {
			// If the channel name is null.
			e.printStackTrace();
		}
		
		catch (IllegalArgumentException e){
			e.printStackTrace();
		}
		
		try {
			sink.RequestRegistration(cMap);
			sink.Fetch(10000, cMap);
		} catch (SAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return cMap;
	}
	
	
	private void runQuery(String type, double time, double duration)
	{
		
		// for dt2db, we need absolute
		String inputRequest = "absolute";

		try{
			// new

			System.out.println("For: time = " + time + 
					"; duration = " + duration +
					"; request type = " + inputRequest);
			
			// create one channel map for two 
			ChannelMap cMap = new ChannelMap();
			int cMapIndex0 = cMap.Add(sourcePath0);
			int cMapIndex1 = cMap.Add(sourcePath1);
			
			sink.Request(cMap,time,duration,type);
			ChannelMap m = sink.Fetch(-1,cMap);

			int channelCount = m.NumberOfChannels();
			double[] times0 = null, times1 = null;

			if (channelCount == 0)
			{
				System.out.println("    --> no data returned");
				return;
			}
			else if (channelCount == 1)
			{
				String name = m.GetChannelList()[0];
				System.out.println("    --> data on only one channel (" + name + ")");
				if (name.equals(sourcePath0))
				{
					times0 = m.GetTimes(0);
				}
				else
				{
					times1 = m.GetTimes(0);
				}
			}
			else
			{
				System.out.println("    --> data on both channels");
				times0 = m.GetTimes(cMapIndex0);
				times1 = m.GetTimes(cMapIndex1);
			}

			System.out.print("    Times on Channel 0: ");
			if (times0 == null)
				System.out.print(" channel not returned");
			else if (times0.length == 0)
				System.out.print(" no data on channel");
			else for (int i = 0; i < times0.length; i++)
			{
				if (times0[i] != time)
					System.out.print(formatValue(times0[i]) + " ");
			}
			System.out.println();

			System.out.print("    Times on Channel 1: ");
			if (times1 == null)
				System.out.print(" channel not returned");
			else if (times1.length == 0)
				System.out.print(" no data on channel");
			else for (int i = 0; i < times1.length; i++)
				if (times1[i] != time)
				System.out.print(formatValue(times1[i]) + " ");
			System.out.println();

		}
		catch(Throwable t)
		{
			t.printStackTrace();
			System.out.println("Oops: bad query?");
		}
	} // runQuery
	
	private String formatValue(double d) {
		DecimalFormat myFormatter = new DecimalFormat("##0.00");
		String output = myFormatter.format(d);
		String pad = "      ";
		output = pad.substring(output.length()) + output;
		return output;
	}

	
	

}

