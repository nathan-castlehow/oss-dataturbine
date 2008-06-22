package edu.ucsd.osdt.db.rbnb;

import com.rbnb.sapi.*;

import java.text.DecimalFormat;
import java.util.StringTokenizer;
import java.io.*;
import java.util.LinkedList;
import java.util.ArrayList;

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

	long lastTimeStamp;
	
	double startedAt0, endedAt0;

	SysDLogger sLogger = new SysDLogger("niagara.sdsc.edu", 514);
	
	public static void main(String[] args){(new SinkClientManager()).exec();}

	private void exec() {
		
		sink = new Sink();

		openRBNBConn(sink);
	
		// map between the dt and db.
		

		
		// hard coding the channel names
		LinkedList <String> chNames = new LinkedList <String> ();
		chNames.add(sourcePath0);
		chNames.add(sourcePath1);
		
		// channel map is created
		ChannelMap cMap = createChannelMap(chNames);
	
		
		requestStartHelper startHelper = new requestStartHelper();
	
		//long requestStartTime = startHelper.findStartTime(cMap);
		// double requestStartTime = startHelper.findStartTime(cMap);
		long requestStartTime = startHelper.readLastTimeFromFile ("/Users/petershin/Documents/hi.txt");

		requestStartTime = System.currentTimeMillis();
		
		System.out.println (requestStartTime);
		lastTimeStamp = requestStartTime;
		
		double durationSeconds = 60000.0;
		
		System.out.println(startHelper.formatDate(lastTimeStamp));
		
		requestStartTime = System.currentTimeMillis();
		double lastTimeStampDouble = requestStartTime / 1000.0;
		
		
		// keep requesting
		runQuery("absolute", cMap, lastTimeStampDouble , durationSeconds);
				
		sink.CloseRBNBConnection();
		
		System.out.println(className + ": Connection to " + server+ " is closed");
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
			Thread.sleep(min*60*1000);
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
	

	private ChannelMap createChannelMap (LinkedList <String> channelNames)
	{
		// channel map names should come from the configuration steps
		// 
		// from the previous steps, the errors regarding the names should have
		// been verified.

		// create one channel map for two 
		

		ChannelMap cMap = new ChannelMap();

		for (int i=0; i< channelNames.size(); i++) {
			
			try {
				int cMapIndex0 = cMap.Add( channelNames.get(i));

				System.out.println("Second channel map index: " + cMapIndex0);

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
		}

		
/***	try {
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
		
*/	
		try {
			sink.RequestRegistration(cMap);
			sink.Fetch(10000, cMap);
		} catch (SAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return cMap;
	}
	
	
	/**
	 * @param type
	 * @param cMap
	 * @param time
	 * @param duration
	 */
	private void runQuery(String type, ChannelMap cMap, double time, double duration)
	{
		
		// for dt2db, we need absolute
		String inputRequest = "absolute";


		System.out.println("For: time = " + time + 
				"; duration = " + duration +
				"; request type = " + inputRequest);

		// if the request did not go through, the rest of the operations
		// are meaningless.  Therefore, the operation is done within the try
		// catch block
		
		try{
			// requesting the data and fetching them.
			sink.Request(cMap,time,duration,type);
			ChannelMap m = sink.Fetch(-1,cMap);

			// getting the number of channels that have data in themselves.
			int channelCount = m.NumberOfChannels();
			
			ArrayList listOfChannelDataArrays = new ArrayList();

			ArrayList <double[]> listOfChannelTimesArrays = new ArrayList <double[]> ();
			
			int [] channelTypes = null;
			
			double[] times0 = null, times1 = null;
			double[] data0 = null;
			double[] data1 = null;
			
			channelTypes = storeChannelTypes (m, channelCount);
			
			listOfChannelDataArrays = getDataFromChannels (m, channelTypes, channelCount);
			listOfChannelTimesArrays = getTimesFromChannels (m, channelCount);

			
			
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
				times0 = m.GetTimes(0);
				data0 = m.GetDataAsFloat64(0);
				
				times1 = m.GetTimes(1);
				data1 = m.GetDataAsFloat64(1);
			}

			
			System.out.print("    Times on Channel 0: ");
			if (times0 == null)
				System.out.print(" channel not returned");
			else if (times0.length == 0)
				System.out.print(" no data on channel");
			else for (int i = 0; i < times0.length; i++)
			{
				System.out.print(times0[i] + " ");
				
				if (times0[i] != time)
					System.out.print(data0[i] + " ");
			}
			System.out.println();

			System.out.print("    Times on Channel 1: ");
			if (times1 == null)
				System.out.print(" channel not returned");
			else if (times1.length == 0)
				System.out.print(" no data on channel");
			else for (int i = 0; i < times1.length; i++)
			{
				if (times1[i] != time)
					System.out.print(data1[i] + " ");
			}

		}
		
		catch (SAPIException e) {
			e.printStackTrace();
			
			// verify whether the connection is still alive, 
			// 
			// if the connection is still alive, request again.
			if (sink.VerifyConnection()) {
				waitForMins(1);
			}
			else {
				openRBNBConn (sink);
			}
			// if the connection is lost, restart the request session.
			
		}
		
	} // runQuery
	

	private ArrayList <double []> getTimesFromChannels(ChannelMap m, int channelCount) {
		ArrayList <double[]> arrList = new ArrayList<double[]>();
		for (int i=0; i<channelCount; i++) {
			arrList.add(m.GetTimes(i));
		}
		return arrList;
	}

	private ArrayList getDataFromChannels(ChannelMap m, int[] channelTypes, int channelCount) {
		ArrayList arrList = new ArrayList();

//		com.rbnb.sapi.ChannelMap
//		public static final int 	TYPE_BYTEARRAY 	10
//		public static final int 	TYPE_FLOAT32 	7
//		public static final int 	TYPE_FLOAT64 	8
//		public static final int 	TYPE_INT16 	4
//		public static final int 	TYPE_INT32 	5
//		public static final int 	TYPE_INT64 	6
//		public static final int 	TYPE_INT8 	3
//		public static final int 	TYPE_STRING 	9
//		public static final int 	TYPE_UNKNOWN 	0
//		public static final int 	TYPE_USER 	11

		
		int channelTypeNum = 0;
		for (int i=0; i<channelCount; i++) {
			channelTypeNum = channelTypes[i];

			if (channelTypeNum == ChannelMap.TYPE_BYTEARRAY)
			{
				byte [][] resultArr = m.GetDataAsByteArray(i);
			}
			else if (channelTypeNum == ChannelMap.TYPE_FLOAT32)
			{
				float[] resultArr = m.GetDataAsFloat32(i);
			}
			else if (channelTypeNum == ChannelMap.TYPE_FLOAT64)
			{
				double[] resultArr = m.GetDataAsFloat64(i);
			}
			else if (channelTypeNum == ChannelMap.TYPE_INT16)
			{
				short [] resultArr = m.GetDataAsInt16(i);
			}
			else if (channelTypeNum == ChannelMap.TYPE_INT32)
			{
				int [] resultArr = m.GetDataAsInt32(i);
			}
			else if (channelTypeNum == ChannelMap.TYPE_INT64)
			{
				long [] resultArr = m.GetDataAsInt64(i);
			}
			else if (channelTypeNum == ChannelMap.TYPE_INT8)
			{
				byte [] resultArr = m.GetDataAsInt8(i);
			}
			else if (channelTypeNum == ChannelMap.TYPE_STRING)
			{
				String [] resultArr = m.GetDataAsString(i);
			}
			else
			{
				Object resultArr = m.GetDataAsArray(i);
			}
			arrList.add(i);
		}
		return arrList;
	}

	
	
	private int [] storeChannelTypes (ChannelMap m, int channelCount) { 
		int [] channelTypes = new int [channelCount];
		for (int i=0; i< channelCount; i++) {
			channelTypes[i] = m.GetType(i);
			System.out.println("Channel type is " + channelTypes[i]);
		}
		return channelTypes;
	}		
	
	
	
	

}

