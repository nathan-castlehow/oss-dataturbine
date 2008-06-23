package edu.ucsd.osdt.db.rbnb;

import com.rbnb.sapi.*;

import java.text.DecimalFormat;
import java.util.StringTokenizer;
import java.io.*;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.HashMap;

import edu.ucsd.osdt.db.log.SysDLogger;
import edu.ucsd.osdt.db.Config;
import edu.ucsd.osdt.db.ConfigUtil;
import edu.ucsd.osdt.db.dt2dbMap;

public class SinkClientManager {

	Sink sink;
	String className = "<SinkClientManager>";
	
	// local variables that need to be configured from the other parts later
	//String server = "niagara.sdsc.edu:3333";

	String server = "niagara-dev.sdsc.edu:3333";
	//String sinkName = "FakeDAQ";
	
	// these should be specified elsewhere

	long lastTimeStamp;
	
	SysDLogger sLogger = null;
	
	double durationSeconds = 20.0;
	
	long requestStartTimeMilli = System.currentTimeMillis();
	double requestStartTimeDouble = requestStartTimeMilli / 1000.0;
	double lastTimeStampDouble = requestStartTimeDouble - durationSeconds;
	
	Config cfg = null;
	
	LinkedList <String>chNames = new LinkedList <String> ();
	long extraFetchTime = 10000L;
	ChannelMap cMap1 = null;
	String dataModel = "RowModel";
	HashMap <String, dt2dbMap> mapper = null;
	
	public SinkClientManager () {
		String sourceName = "FakeDAQ";
		
		String sourceChannel0 = "0";
		String sourceChannel1 = "1";
		String sourcePath0 = sourceName + "/" + sourceChannel0;
		String sourcePath1 = sourceName + "/" + sourceChannel1;

		// hard coding the channel names

		this.chNames.add(sourcePath0);
		this.chNames.add(sourcePath1);
	
		System.out.println(">>" + sourcePath0 + "<<");
		sLogger = new SysDLogger("niagara.sdsc.edu", 514);
		
	}
	
	public void setConfig (Config cfg) {
		this.server = cfg.getRbnbServerAddress()+ ":" + cfg.getRbnbServerPort();
		System.out.println("The rbnb server name is:" + "<"+this.server+">");

		String sysServer = cfg.getSysLogServerAddress();
		System.out.println("The sysLog server name is:" + "<"+sysServer+">");

		this.sLogger = new SysDLogger(sysServer, 514);
		
		this.durationSeconds = cfg.getDurationSeconds();
		System.out.println ("Duration seconds from cfg = "+ this.durationSeconds );
		
		LinkedList <String> nList = cfg.getChNames();
		for (int i=0; i< nList.size(); i++) {
			this.chNames.add( nList.get(i) );
			System.out.println("Channel path is: >>" + chNames.get(i)+"<<");
		}
	
		this.dataModel = cfg.getDataModel();
		this.mapper = cfg.getDt2dbMap();
		
	}
	
	
	public static void main(String[] args){(new SinkClientManager()).exec();}

	public void exec() {
		
		sink = new Sink();

		openRBNBConn(sink);
	
		// map between the dt and db.
		
		
		// channel map is created
		cMap1 = createChannelMap(this.chNames);
		System.out.println ("Channel Map is created");
		
		String [] list = cMap1.GetChannelList();
		for (int ii=0; ii<list.length; ++ii) {
			list[ii]=cMap1.GetName(ii);
			System.out.println ("Channel Name in exec() is " + list[ii]);
		}

		
		requestStartHelper startHelper = new requestStartHelper();
	
		//long requestStartTime = startHelper.findStartTime(cMap);
		// double requestStartTime = startHelper.findStartTime(cMap);
		//long requestStartTime = startHelper.readLastTimeFromFile ("/Users/petershin/Documents/hi.txt");

		//requestStartTime = System.currentTimeMillis();
			
		
		System.out.println("last time stamp is " + startHelper.formatDate((long) (lastTimeStampDouble * 1000.0)));
		System.out.println("current time is "+ startHelper.formatDate(System.currentTimeMillis()));

		// keep requesting
		runQuery("absolute", cMap1, lastTimeStampDouble , durationSeconds);
		System.out.println ("Running a new query");
		
		this.sink.CloseRBNBConnection();
		
		System.out.println(className + ": Connection to " + server+ " is closed");
		//sLogger.sysDMessage(className + ": Connection to " + server+ " is closed");
		
		lastTimeStampDouble = lastTimeStampDouble + durationSeconds;
		exec();
		
		
	} // exec


	
	
	public void openRBNBConn (Sink s) {
		try{
			this.sink = new Sink();
			this.sink.OpenRBNBConnection(server, className);
			System.out.println(className + ": Connection to " + server+ " is opened");

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

			System.out.println ("waiting for " + min + " min");
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
	

	public ChannelMap createChannelMap (LinkedList <String> channelNames)
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

				System.out.println("channel map index: " + cMapIndex0);

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

		try {
			this.sink.RequestRegistration(cMap);
			this.sink.Fetch(-1, cMap);
			
			String [] list = cMap.GetChannelList();
			for (int ii=0; ii<list.length; ++ii) {
				list[ii]=cMap.GetName(ii);
				System.out.println ("Channel Name in createChannelMap is " + list[ii]);
			}

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
	public void runQuery(String type, ChannelMap cMap, double time, double duration)
	{
		
		// for dt2db, we need absolute


		System.out.println("For: time = " + time + 
				"; duration = " + duration +
				"; request type = " + type);

		// if the request did not go through, the rest of the operations
		// are meaningless.  Therefore, the operation is done within the try
		// catch block
		
		try{
			
			if (this.sink.VerifyConnection()) {
				System.out.println ("Connection is verified");
			}
			
			String [] list = cMap.GetChannelList();
			for (int ii=0; ii<list.length; ++ii) {
				list[ii]=cMap.GetName(ii);
				System.out.println ("Channel Name in runQuery is " + list[ii]);
			}

			
			// requesting the data and fetching them.
			this.sink.Subscribe(cMap,time,duration,type);
			ChannelMap m = this.sink.Fetch( (long) (duration + this.extraFetchTime) ,cMap);

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

			LinkedList queries = generateQueries (m, channelCount, listOfChannelDataArrays, listOfChannelTimesArrays);
			
			
/**			
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
*/			
			

		}
		
		catch (SAPIException e) {
			e.printStackTrace();
			
			System.out.println("Request failed");
			// verify whether the connection is still alive, 
			// 
			// if the connection is still alive, request again.
			if (this.sink.VerifyConnection()) {
				waitForMins(1);
				runQuery(type, cMap, time, duration);
			}
			else {
				openRBNBConn (this.sink);
			}
			// if the connection is lost, restart the request session.
			
		}
		
	} // runQuery
	

	private LinkedList <String> generateQueries(ChannelMap m, int chCount, ArrayList listOfChannelDataArrays,
			ArrayList<double[]> listOfChannelTimesArrays) {
		LinkedList <String> queries =null;
		if (this.dataModel.equals("RowModel")) {
			queries = generateRowModelQueries (m, chCount, listOfChannelDataArrays, listOfChannelTimesArrays);
		}
		else {
			queries = gerateEAVModelQueries (m, chCount, listOfChannelDataArrays, listOfChannelTimesArrays);
		}
		return null;
	}


	
	
	
	private LinkedList<String> gerateEAVModelQueries(
			ChannelMap m,
			int chCount,
			ArrayList listOfChannelDataArrays,
			ArrayList<double[]> listOfChannelTimesArrays) {
		
		LinkedList <String> queries = new LinkedList <String> ();

		boolean done = false;
		
		LinkedList <Integer> sameTimeInd = null;
		
		int dataChNum = listOfChannelDataArrays.size();
		int timeChNum = listOfChannelTimesArrays.size();
		
		int[] timesCurrIndex = new int[dataChNum];
		int[] timesMaxIndex = new int [dataChNum];
		
		for (int i=0; i < dataChNum; i++) {
			timesCurrIndex[i] = 0;
			timesMaxIndex[i] = listOfChannelTimesArrays.get(i).length;
		}
		
		while (done) {
			// find the min time
			double minTime = Double.MAX_VALUE;
			double tempTime = 0.0;
			
			for (int i=0; i< timeChNum; i++) {
				tempTime = listOfChannelTimesArrays.get(i) [timesCurrIndex[i]];
				if (tempTime < minTime) {
					minTime = tempTime;
				}
			}
			
			// find all the channels with the min time
			sameTimeInd = new LinkedList <Integer>();
			for (int i=0; i< timeChNum; i++) {
				tempTime = listOfChannelTimesArrays.get(i) [timesCurrIndex[i]];
				if (minTime == tempTime) {
					sameTimeInd.add(new Integer(i));
				}
			}
			
			LinkedList <String> colVals = new LinkedList <String>();
			LinkedList <String> colNames = new LinkedList <String>();
			
			// generate queries
			// very tricky!!!
			// cast carefully..
			for (int i=0; i< sameTimeInd.size(); i++) {
				
				int idx = sameTimeInd.get(i);
				listOfChannelDataArrays.get(timesCurrIndex[idx]);
				
			}
		}
		return null;
	}

	private LinkedList<String> generateRowModelQueries(
			ChannelMap m,
			int chCount,
			ArrayList listOfChannelDataArrays,
			ArrayList<double[]> listOfChannelTimesArrays) {
		// TODO Auto-generated method stub
		return null;
	}

	
	private ArrayList <double []> getTimesFromChannels(ChannelMap m, int channelCount) {
		ArrayList <double[]> arrList = new ArrayList<double[]>();
		for (int i=0; i<channelCount; i++) {
			arrList.add(m.GetTimes(i));
		}
		return arrList;
	}

	private ArrayList getDataFromChannels(ChannelMap m, int[] channelTypes, int channelCount) {
		ArrayList arrList = new ArrayList();

	
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

