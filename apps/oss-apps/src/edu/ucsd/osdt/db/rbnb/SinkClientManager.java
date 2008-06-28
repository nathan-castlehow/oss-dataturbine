package edu.ucsd.osdt.db.rbnb;

import com.rbnb.sapi.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.StringTokenizer;
import java.io.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;
import java.util.List;

import edu.ucsd.osdt.db.log.SysDLogger;
import edu.ucsd.osdt.db.Config;
import edu.ucsd.osdt.db.ConfigUtil;
import edu.ucsd.osdt.db.dt2dbMap;
import edu.ucsd.osdt.db.DbOperator;

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
		this.cfg = cfg;
		
	}
	
	
	public static void main(String[] args){(new SinkClientManager()).exec();}

	public void exec() {
		
		sink = new Sink();

		openRBNBConn(sink);
	
		// map between the dt and db.
		
		System.out.println ("# of channel names are " + this.chNames.size());
		try {
			Thread.sleep (1000L);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		
		for (int i=0; i< this.chNames.size();i++) {
			System.out.println("channel names are: " +chNames.get(i) );
			try {
				Thread.sleep (1000L);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// channel map is created
		cMap1 = createChannelMap(this.chNames);
		System.out.println ("Channel Map is created");
		
		String [] list = cMap1.GetChannelList();
		for (int ii=0; ii<list.length; ++ii) {
			list[ii]=cMap1.GetName(ii);
			//System.out.println ("Channel Name in exec() is " + list[ii]);
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

				//System.out.println("channel map index: " + cMapIndex0);

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
				//System.out.println ("Channel Name in createChannelMap is " + list[ii]);
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
				//System.out.println ("Channel Name in runQuery is " + list[ii]);
			}

			
			// requesting the data and fetching them.
			this.sink.Subscribe(cMap,time,duration,type);
			ChannelMap m = this.sink.Fetch( (long) (duration*1000 + this.extraFetchTime) ,cMap);

			// getting the number of channels that have data in themselves.
			int channelCount = m.NumberOfChannels();
			
			LinkedList <LinkedList> listOfChannelDataLists = new LinkedList <LinkedList>();
			LinkedList <LinkedList> listOfChannelTimesLists = new LinkedList <LinkedList> ();
			
			
			int [] channelTypes = null;
			
			channelTypes = storeChannelTypes (m, channelCount);
			
			listOfChannelDataLists = getDataFromChannels (m, channelTypes, channelCount);
			listOfChannelTimesLists = getTimesFromChannels (m, channelCount);
			
			//System.out.println("size of the list of data channels: " + listOfChannelDataLists.size());

			//System.out.println("the channel type for the first channel is "+ channelTypes[0]);
			//System.out.println("the first value of data channels: " + ( listOfChannelDataLists.get(0).get(0)));
			
			//System.out.println("size of the mapper:  " + this.mapper.size());
			
			LinkedList queries = generateQueries (m, channelTypes, channelCount, listOfChannelDataLists, listOfChannelTimesLists);
			
			//System.out.println(queries.get(0));
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
	

	
	private LinkedList<String> getColNames(ChannelMap m, int channelCount) {
		LinkedList <String> mappedColNames = new LinkedList <String> ();
		
		for (int i=0; i <channelCount; i++) {
			String tempName = m.GetName(i);
			dt2dbMap tempDdm = this.mapper.get(tempName);
			mappedColNames.add(tempDdm.getColName());
		}
		
		return mappedColNames;
	}

	private LinkedList<String> getTblNames(ChannelMap m, int channelCount) {
		LinkedList <String> mappedTblNames = new LinkedList <String> ();
		
		
		for (int i=0; i <channelCount; i++) {
			String tempName = m.GetName(i);
			dt2dbMap tempDdm = this.mapper.get(tempName);
			mappedTblNames.add(tempDdm.getTableName());
		}
		
		return mappedTblNames;
	}

	private LinkedList<String> getFetchedChNames(ChannelMap m, int channelCount) {
		
		LinkedList <String> fetchedChNames = new LinkedList <String> ();
		for (int i=0; i<channelCount; i++) {
			fetchedChNames.add(m.GetName(i));
		}
		return fetchedChNames;
	}

	
	private LinkedList <String> generateQueries(ChannelMap m, int[] channelTypes, int chCount, LinkedList <LinkedList> listOfChannelDataLists,
			LinkedList <LinkedList> listOfChannelTimesLists) {
		LinkedList <String> queries =null;
		if (this.dataModel.equals("RowModel")) {
			queries = generateRowModelQueries (m, channelTypes, chCount, listOfChannelDataLists, listOfChannelTimesLists);
		}
		else {
			queries = gerateEAVModelQueries (m, channelTypes, chCount, listOfChannelDataLists, listOfChannelTimesLists);
		}
		return null;
	}


	
	
	
	private LinkedList<String> gerateEAVModelQueries(
			ChannelMap m,
			int[] chTypes,
			int chCount,
			LinkedList <LinkedList> listOfChannelDataArrays,
			LinkedList <LinkedList> listOfChannelTimesArrays) {

		LinkedList <String> queries = new LinkedList <String> ();
		LinkedList <String> fetchedChNames = new LinkedList <String> ();
		LinkedList <String> mappedTblNames = new LinkedList <String> ();
		LinkedList <String> mappedColNames = new LinkedList <String> ();

		fetchedChNames = getFetchedChNames (m, chCount);
		mappedTblNames = getTblNames (m, chCount);
		mappedColNames = getColNames (m, chCount);
		
		return null;
	}

	
	private LinkedList<String> generateRowModelQueries(
			ChannelMap m,
			int [] chTypes,
			int chCount,
			LinkedList <LinkedList> listOfChannelDataLists,
			LinkedList <LinkedList> listOfChannelTimesLists) {

		//System.out.println ("execusting generateRowModelQueries");
		
		LinkedList <String> queries = new LinkedList <String> ();
		LinkedList <String> fetchedChNames = new LinkedList <String> ();
		LinkedList <String> mappedTblNames = new LinkedList <String> ();
		LinkedList <String> mappedColNames = new LinkedList <String> ();

		fetchedChNames = getFetchedChNames (m, chCount);
		mappedTblNames = getTblNames (m, chCount);
		mappedColNames = getColNames (m, chCount);
		

		boolean done = false;
		
		LinkedList <Integer> sameTimeInd = null;
		
		int dataChNum = listOfChannelDataLists.size();
		int timeChNum = listOfChannelTimesLists.size();
		
		int[] timesCurrIndex = new int[dataChNum];
		int[] timesMaxIndex = new int [dataChNum];
		
		for (int i=0; i < dataChNum; i++) {
			timesCurrIndex[i] = 0;
			timesMaxIndex[i] = (listOfChannelTimesLists.get(i)).size();
			
			System.out.println ("channel index ("+ i+") has "+timesMaxIndex[i] + " elements");
		}

		if (listOfChannelTimesLists.size() == 0) {
			return null;
		}
		
		DbOperator dbop = new DbOperator();
		dbop.setConfig(this.cfg);
		
		// find the min time index.
		Double minTimeDouble = (Double) listOfChannelTimesLists.get(0).get(0);
		double minTime = minTimeDouble.doubleValue();
		double tempTime = minTime;

		Double tempTimeDouble = new Double(0.0);
		
		for (int i=0; i< chCount; i++) {
			tempTimeDouble = (Double) listOfChannelTimesLists.get(i).get(0);
			tempTime = tempTimeDouble.doubleValue();
			
			if (tempTime < minTime) {
				minTime = tempTime;
			}
		}
		//System.out.println ("min time is " + minTime);
		
		
		while (!done) {

			// find the starting point
			for (int i=0; i<chCount; i++) {
				
				if (timesCurrIndex[i] < timesMaxIndex[i]) {
					minTimeDouble = (Double) listOfChannelTimesLists.get(i).get(timesCurrIndex[i]);
					minTime = minTimeDouble.doubleValue();
				}
			}
				
			// find the min time among channels
			for (int i=0; i< chCount; i++) {
				
				int currIndex = timesCurrIndex[i];
				int maxIndex = timesMaxIndex[i];

				if (currIndex < maxIndex) {
					tempTimeDouble = (Double) listOfChannelTimesLists.get(i).get(currIndex);
					tempTime = tempTimeDouble.doubleValue();

					if (tempTime <= minTime) {
						minTime = tempTime;
						System.out.println ("min time is " + minTime);
					}
				}
			}
			
			if (minTime == this.lastTimeStampDouble) {
				// we exclude the starting point values in order to avoid
				// redundant data.
				// Because rbnb's request/subscribe methods have inclusive start/
				// duration time, we need to manually check this case.
			}

			else {
				
				// find all the channels with the same min time
				sameTimeInd = new LinkedList <Integer>();
				
				for (int i=0; i< chCount; i++) {
					
					int currIndex = timesCurrIndex[i];
					int maxIndex = timesMaxIndex[i];

					if (currIndex < maxIndex) {

						currIndex = timesCurrIndex[i];
						//System.out.println (">>>current index of the channel: " + currIndex);
						tempTimeDouble = (Double) listOfChannelTimesLists.get(i).get(currIndex);
						tempTime = tempTimeDouble.doubleValue();

						if (minTime == tempTime) {
							sameTimeInd.add(new Integer(i));
						}
					}
				}
				
				System.out.println ("number of channels with the same time: " + sameTimeInd.size());
				
				
				LinkedList <String> colVals = new LinkedList <String>();
				LinkedList <String> colNames = new LinkedList <String>();
				LinkedList <String> tblNames = new LinkedList <String>();
				
				// generate one query with all the same timestamps
				// very tricky!!!
				// cast carefully..
				for (int i=0; i< sameTimeInd.size(); i++) {

					// idx contains the channel number.
					Integer chIdxInt = (Integer) sameTimeInd.get(i);
					int chIdx = chIdxInt.intValue();
					
					// this gives the Channel array with time
					LinkedList <Double> chTime1 = listOfChannelTimesLists.get(chIdx);

					// prepare to access this channel using the current index
					int currChIndex = timesCurrIndex[chIdx];
					Double corrTimeDouble = (Double) chTime1.get(currChIndex);
					double corrTime = corrTimeDouble.doubleValue();
					int chDataTypeInfo = chTypes[chIdx];

					// prepare the statement for the data part
					// 
					// we will not deal with binary objects yet.
					// for the binary objects 
					String dataPart = prepareDataStatement (currChIndex, chDataTypeInfo, chIdx, listOfChannelDataLists);
					colVals.add(dataPart);
					colNames.add(mappedColNames.get(chIdx));
					tblNames.add(mappedTblNames.get(chIdx));
					
					// advance the index
					timesCurrIndex[chIdx] = timesCurrIndex[chIdx] +1; 
					
					System.out.println ("For the same timed data points in channel #" + chIdx + " has the current idx " + timesCurrIndex[chIdx]);
				}
				
				// generate a query with all the column names, table names, data values

				queries.addAll(dbop.GenerateRowModelInsertQueries(tblNames, colNames, colVals, minTime));
				
				boolean allDone = true;
				for (int i=0; i< chCount; i++) {
					if (timesCurrIndex[i] < timesMaxIndex[i]) {
						allDone = false;
					}
					
					System.out.println ("Advancing the curr index in channel " + timesCurrIndex[i] + " where the max ind is " + timesMaxIndex[i]);

				}
				done = allDone;
			
			} //while
			
			
		}
		dbop.ExecuteQueries( queries);
		
		return null;
	}

	
	private String prepareDataStatement(
			int currChIndex, 
			int chDataTypeInfo,
			int chIdx, 
			LinkedList <LinkedList> listOfChannelDataLists) {
		
		String resultStr;
		
		if (chDataTypeInfo == ChannelMap.TYPE_FLOAT32) {
			// float
			LinkedList <Float> dataList = (LinkedList <Float>) listOfChannelDataLists.get(chIdx);
			Float dataPointFloat = (Float) dataList.get(currChIndex);
			resultStr = dataPointFloat.toString();
		}
		else if (chDataTypeInfo == ChannelMap.TYPE_FLOAT64) {
			// double
			LinkedList <Double> dataList = (LinkedList <Double>) listOfChannelDataLists.get(chIdx);
			Double dataPointFloat = (Double) dataList.get(currChIndex);
			resultStr = dataPointFloat.toString();
		}
		else if (chDataTypeInfo== ChannelMap.TYPE_INT32) {
			// int
			LinkedList <Integer> dataList = (LinkedList <Integer>) listOfChannelDataLists.get(chIdx);
			Integer dataPointFloat = (Integer) dataList.get(currChIndex);
			resultStr = dataPointFloat.toString();
		}
		else if (chDataTypeInfo == ChannelMap.TYPE_INT64 ) {
			// long
			LinkedList <Long> dataList = (LinkedList <Long>) listOfChannelDataLists.get(chIdx);
			Long dataPointFloat = (Long) dataList.get(currChIndex);
			resultStr = dataPointFloat.toString();
		}

		else if (chDataTypeInfo == ChannelMap.TYPE_INT8) {
			// short
		
			LinkedList <Byte> dataList = (LinkedList <Byte>) listOfChannelDataLists.get(chIdx);
			Byte dataPointFloat = (Byte) dataList.get(currChIndex);
			resultStr = dataPointFloat.toString();
		}
		else {
			resultStr = "unknown type";
		}
		return resultStr;
		
	}


	
	private LinkedList <LinkedList> getTimesFromChannels(ChannelMap m, int channelCount) {
		LinkedList <LinkedList> allChannels = new LinkedList <LinkedList> ();
		LinkedList <Double> timeInCh = new LinkedList <Double> ();
		
		for (int i=0; i<channelCount; i++) {
			double [] timeStampsInCh = m.GetTimes(i);
			
			for (int j=0; j<timeStampsInCh.length; j++) {
				timeInCh.add(new Double(timeStampsInCh[j]));
			}
			
			allChannels.add(timeInCh);
			timeInCh = new LinkedList <Double>();
			
		}
		return allChannels;
	}

	private LinkedList <LinkedList> getDataFromChannels(ChannelMap m, int[] channelTypes, int channelCount) {
		
		LinkedList <LinkedList> allChannels = new LinkedList <LinkedList> ();
		LinkedList dataInCh = new LinkedList ();
	
		channelCount = m.GetChannelList().length;
		System.out.println ("Data channel length is " + channelCount);
		
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
				
				for (int j=0; j<resultArr.length; j++) {
					dataInCh.add(new Float(resultArr[j]));
				}
				
				allChannels.add(dataInCh);
			}
			else if (channelTypeNum == ChannelMap.TYPE_FLOAT64)
			{
				double[] resultArr = m.GetDataAsFloat64(i);

				for (int j=0; j<resultArr.length; j++) {
					dataInCh.add(new Double(resultArr[j]));
				}
				
				allChannels.add(dataInCh);

			}
			else if (channelTypeNum == ChannelMap.TYPE_INT16)
			{
				short [] resultArr = m.GetDataAsInt16(i);
				
				for (int j=0; j<resultArr.length; j++) {
					dataInCh.add(new Short(resultArr[j]));
				}
				
				allChannels.add(dataInCh);

			}
			else if (channelTypeNum == ChannelMap.TYPE_INT32)
			{
				int [] resultArr = m.GetDataAsInt32(i);

				for (int j=0; j<resultArr.length; j++) {
					dataInCh.add(new Integer(resultArr[j]));
				}
				
				allChannels.add(dataInCh);

			}
			else if (channelTypeNum == ChannelMap.TYPE_INT64)
			{
				long [] resultArr = m.GetDataAsInt64(i);
				
				for (int j=0; j<resultArr.length; j++) {
					dataInCh.add(new Long(resultArr[j]));
				}
				
				allChannels.add(dataInCh);

			}
			else if (channelTypeNum == ChannelMap.TYPE_INT8)
			{
				byte [] resultArr = m.GetDataAsInt8(i);
				
				for (int j=0; j<resultArr.length; j++) {
					dataInCh.add(new Byte(resultArr[j]));
				}
				
				allChannels.add(dataInCh);

			}
			else if (channelTypeNum == ChannelMap.TYPE_STRING)
			{
				String [] resultArr = m.GetDataAsString(i);
				
				for (int j=0; j<resultArr.length; j++) {
					dataInCh.add(new String(resultArr[j]));
				}
				
				allChannels.add(dataInCh);

			}
			else
			{
				Object resultArr = m.GetDataAsArray(i);
			}
			
			dataInCh = new LinkedList();
		}
		
		for (int i=0; i<channelCount; i++) {
			System.out.println("Channel " + i + " has "+ allChannels.get(i).size() + "elements");
			System.out.println("channel names are " + m.GetName(i));
			try {
				Thread.sleep(1000L);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return allChannels;
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

