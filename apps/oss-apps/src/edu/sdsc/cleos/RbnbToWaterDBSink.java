package edu.sdsc.cleos;
/** @brief a class that will use the sink features of an rbnb plugin to
 * verify data exists in an rbnb ring buffer
 * @author ljmiller
 * @since 070128
 */

import com.rbnb.sapi.ChannelTree;
import com.rbnb.sapi.Sink;
import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;

import edu.sdsc.cleos.ISOtoRbnbTime;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;


import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nees.rbnb.RBNBBase;

public class RbnbToWaterDBSink extends RBNBBase {
	private Sink		sink	= null;
	private double rbnbStart 	= Double.MAX_VALUE;
	private double rbnbDuration = -1.0;
	private ChannelMap	map		= null;
	private static Log 	log		= LogFactory.getLog(RbnbToWaterDBSink.class.getName());
	private static DecimalFormat onePoint = new DecimalFormat("#.#");
	// a flag to put data requests into monitor mode - if false, the whole ring buffer is requested
	private boolean useMonitor = false;
	// a flag to put insert data into a database
	private boolean useDb = false;
	private String dbHost = null;
	// a flag to use only channels with Synthetic in their names
	private boolean useSynthetic = false;
	
	private String pattern = "SatSim Source/*";
	
	private Hashtable<String, DBTableColName> lookupTable = null;
	private StringTokenizer st = null;
	
	private double lastTime = 0.0;
	
	private static int maxID = 2;
	
	public RbnbToWaterDBSink() {
		sink = new Sink();
		map = new ChannelMap();
		this.createDBTblColNames();
	} // constructor
	
	public void createDBTblColNames () {

		lookupTable = new Hashtable <String, DBTableColName> (); 
		
		lookupTable.put("AirTemp", new DBTableColName("DataValues", "AirTemp")); 
		lookupTable.put("Relative Humidity", new DBTableColName ("DataValues", "Water Vapor"));

	}
	
	
	public boolean open() {
		try {
			sink.OpenRBNBConnection(getServer(), getRBNBClientName());
			System.out.println("connected to the RBNB server");
		} catch(SAPIException sap) {
			log.error("couldn't connect to RBNB server: " + getServer());
			log.error(sap);
			return false;
		}
		return true;
	}
	
	public Sink getSink() {
		return this.sink;
	} // getSink()
	
	
	public double getRbnbStart() {
		return this.rbnbStart;
	} // getRbnbStart()
	
	
	public double getRbnbDuration() {
		return this.rbnbDuration;
	} // getRbnbDuration()
	
	
	public boolean isMonitoring() {
		return this.useMonitor;
	} // isMonitoring()
	
	
	public ChannelMap refreshChannelMap() throws SAPIException {
		//this.map = new ChannelMap();
		
		this.map.Clear();
		this.map.Add(this.pattern);
		this.sink.RequestRegistration(this.map);
		this.map = this.sink.Fetch(-1, this.map);
		
		for(int i=0; i<this.map.NumberOfChannels(); i++) {
			log.debug(this.map.GetName(i));
			if (0 < this.map.GetTimeDuration(i)) {
				
				if (this.map.GetTimeStart(i) < this.rbnbStart) {
					this.rbnbStart = this.map.GetTimeStart(i);
				} if (this.rbnbDuration < this.map.GetTimeDuration(i)) {
					rbnbDuration = this.map.GetTimeDuration(i);
				}
			} // if has any duration
		} // for

		return this.map;
	} // refreshChannelMap() 
	
	
	public LinkedList<String> generateQueries(ChannelMap cmap) {
		System.out.println("Generating queries (channel map)");
		BunchOfChannels allChannels = new BunchOfChannels();
		
		//log.warn("Hihihihihi");
		for (int i=0; i<cmap.NumberOfChannels(); i++) {

			int typeid = cmap.GetType(i);
			String typeName = cmap.TypeName(typeid);
			
			System.out.println(cmap.GetName(i)+ " has the type for the data channel"+ typeName);
			
			float[] someData = cmap.GetDataAsFloat32(i);
			double[] someTimes = cmap.GetTimes(i);
			// @todo fixme - weird hack to get units
			//String unitsInfo = this.map.GetUserInfo(this.map.GetIndex(cmap.GetName(i)));
			//String[] unitSplit = unitsInfo.split("=");
			
			String cName = extractName(cmap.GetName(i));
			//System.out.println("cName is " + cName);

			// @todo do DB insert here
			DBTableColName tblColName = getDBTblColName (cName);

			//System.out.println("table name is " + tblColName.tableName);
			//System.out.println("channel name is " + tblColName.colName);

			
			LinkedList <Double> data = new LinkedList <Double>();  
			LinkedList <Double> times = new LinkedList <Double>();  

			for (int j=0; j<someData.length; j++) {
				
				data.add(new Double (someData[j]));
				times.add(new Double (someTimes[j]));
				
				//log.debug(cmap.GetName(i) + " - " + Double.toString(someData[j]) + " " +
				//		//unitSplit[1] + 
				//		" time:" + ISOtoRbnbTime.formatDateForDB( (long)(someTimes[j]*1000) ) );
				
			} // for j - data loop

			ChannelDataTime cdt = new ChannelDataTime (tblColName, data, times);
			allChannels.addChannel(cdt);
			double lastTimeForThisChannel = someTimes[someTimes.length-1];
			if (lastTimeForThisChannel > lastTime) {
				lastTime = lastTimeForThisChannel;
			}
		} // for i - cmap
		
		
		return allChannels.generateQueries();
	} // mapDump() 

	
	private String extractName (String rbnbName) {

		st = new StringTokenizer (rbnbName, "/");
		int lastIndex = st.countTokens();
		String nwpChannelName = "";
		for (int temp=0; temp< lastIndex; temp++) {
			nwpChannelName = st.nextToken();
		}
		nwpChannelName = nwpChannelName.trim();
		
		return nwpChannelName;
	}
	


	
	public DBTableColName getDBTblColName (String nwpString) {
		DBTableColName match = lookupTable.get(nwpString);
		return match;
	}
	
	

	

	class BunchOfChannels {
		public LinkedList <TableForChannel> tables;
		public LinkedList <String> queries;
		
		public BunchOfChannels (ChannelDataTime cdt) {
			TableForChannel tbl = new TableForChannel (cdt);
			this.tables = new LinkedList <TableForChannel> ();
			this.tables.add(tbl);
			this.queries = new LinkedList<String>();
		}
		
		public BunchOfChannels () {
			this.tables = new LinkedList <TableForChannel> ();
			this.queries = new LinkedList<String>();
		}
		
		public void addChannel (ChannelDataTime cdt) {
			if (this.tables.size() == 0 ) {
				TableForChannel tbl = new TableForChannel (cdt);
				this.tables.add(tbl);
			}
			else {
				boolean not_inserted = true;
				int i = 0;
				
				while (not_inserted && (i < this.tables.size())) {
					TableForChannel temp_tbl = this.tables.get(i);
					if (temp_tbl.addChannel(cdt)) {
						not_inserted = false;
					}
					i++;
				}
				if (not_inserted) {
					TableForChannel tbl = new TableForChannel (cdt);
					this.tables.add(tbl);
				}
			}
		}
		
		public LinkedList <String> generateQueries () {
			LinkedList <String> queries = new LinkedList <String> ();

			//System.out.println("Size of the tables is " +  this.tables.size() );

			for (int i = 0; i < this.tables.size();i++) {
				
				TableForChannel tbl = this.tables.get(i);
				queries.addAll( (tbl.generateQueries(tbl.syncTimes())));
				
				
			}
			return queries;
		}
		
		public double GetLatestTime () {
			
			Double lastTime = -1.0;
			Double tempTime = 0.0;
			for (int i = 0; i < this.tables.size(); i++) {
				
				TableForChannel tbl = this.tables.get(i);
				LinkedList <Double> times = tbl.syncTimes() ;
				tempTime = times.get(times.size()-1);
				
				if (lastTime > tempTime) {
					lastTime = tempTime;
				}
				
			}
			return lastTime;
		}
	}

	class TableForChannel {

		public String tableName;
		public LinkedList <ChannelDataTime> channelColumns;

		public TableForChannel (ChannelDataTime cdt) {
			this.tableName=cdt.tableName;
			this.channelColumns = new LinkedList <ChannelDataTime>();
			this.channelColumns.add(cdt);
		}

		public TableForChannel (String name, ChannelDataTime cdt) {
			this.tableName=name;
			this.channelColumns = new LinkedList <ChannelDataTime>();
			this.channelColumns.add(cdt);
		}
		
		public boolean addChannel (ChannelDataTime cdt) {
			if (this.tableName == cdt.tableName) {
				this.channelColumns.add(cdt);
				return true;
			}
			else return false;
		}
		

		
		public LinkedList <String> generateQuery (Double timeIndex) {
			
			System.out.println("Start generating a query..");
			int numberOfChannels = this.channelColumns.size();
			LinkedList <String> colNames = new LinkedList <String>();
			LinkedList <Double> data = new LinkedList <Double> ();
			
			// group the channels into the tables
			for (int i=0; i < numberOfChannels; i ++) {
				ChannelDataTime currChannel = this.channelColumns.get(i);
				
				if (currChannel.dataExist(timeIndex)) {
					data.add(currChannel.lookupData(timeIndex));
					colNames.add(currChannel.colName);
				}	
			}

			/**
			 * INSERT INTO [RBNB].[dbo].[DataValues]
           		([ValueID]
           		,[DataValue]
           		,[LocalDateTime]
           		,[UTCOffset]
           		,[DateTimeUTC]
           		,[SiteID]
           		,[VariableID]
           		,[CensorCode]
           		,[MethodID]
           		,[SourceID]
           		,[QualityControlLevelID])
     		VALUES
           		(2,
                  999,
                  '1/1/2006 11:59:59 PM',
                  -7.0,
                  '1/1/2006 4:59:59 PM'
           		,1
           		,1
           		,'nc'
           		,0
           		,1
           		,0)
			 */

			String insert = "INSERT INTO ";
			
			String columnNames = "LocalDateTime, ";			
			String columnValues =  "'" + ISOtoRbnbTime.formatDateForDB( (long)(timeIndex*1000) ) + "', ";
			columnNames += "UTCOffset, ";			
			columnValues +=  " -8, ";
			columnNames += "DateTimeUTC, ";			
			columnValues +=  "'" + ISOtoRbnbTime.formatDateForDB( (long)(timeIndex*1000 -60*60*8*1000) ) + "' ,";
			columnNames += "SiteID, ";			
			columnValues +=  "1, ";
			columnNames += "CensorCode, ";			
			columnValues +=  "'nc', ";
			columnNames += "MethodID, ";			
			columnValues +=  "0, ";
			columnNames += "SourceID, ";			
			columnValues +=  "1, ";
			columnNames += "QualityControlLevelID, ";			
			columnValues +=  "0, ";
			
							
			
			if (colNames.size() == 0) {
				return null;
			}
			
			else {
				String theQueryAT = null;
				String theQueryWV = null;
				
				for (int i = 0; i < colNames.size(); i++) {
					String tempStr = colNames.get(i);
					
					if (tempStr == "AirTemp") {
						
						String columnNamesAT = new String (columnNames);
						String columnValuesAT = new String (columnValues);
						
						columnNamesAT += "ValueID, ";
						maxID = maxID +1;
						columnValuesAT += String.valueOf(maxID) + ", ";

						columnNamesAT+= "VariableID, ";
						columnNamesAT += "DataValue";
						if ( i == colNames.size()) 
							columnNamesAT += " , ";
						
						columnValuesAT += "2, ";
						columnValuesAT += data.get(i);
						if ( i == colNames.size()) 
							columnValuesAT += " , ";

						theQueryAT = insert + this.tableName + " ( " + columnNamesAT
						+ " ) VALUES ( " + columnValuesAT + ")";

					}
					else if (tempStr == "Water Vapor") {
						String columnNamesWV = new String (columnNames);
						String columnValuesWV = new String (columnValues);

						columnNamesWV += "ValueID, ";
						maxID = maxID +1;
						columnValuesWV += String.valueOf(maxID) + ", ";

						columnNamesWV += "VariableID, ";
						columnNamesWV += "DataValue ";
						if ( i == colNames.size()) 
							columnNames += " , ";
						
						columnValuesWV += "1,";						
						columnValuesWV += data.get(i);
						if ( i == colNames.size()) 
							columnValuesWV += " , ";

						theQueryWV = insert + this.tableName + " ( " + columnNamesWV
						+ " ) VALUES ( " + columnValuesWV + ")";

					}
					else {
						columnNames += colNames.get(i);
						if ( i == colNames.size()) 
							columnNames += " , ";

						columnValues += data.get(i);
						if ( i == colNames.size()) 
							columnValues += " , ";

					}
					
				}



				//log.debug ("Creating query for this time " + timeIndex.toString());
				System.out.print(theQueryAT);
				System.out.print(theQueryWV);

				LinkedList <String> queries = new LinkedList <String> ();
				queries.add(theQueryAT);
				queries.add(theQueryWV);
				return queries;
			}
		}
		
		public LinkedList <Double> syncTimes() {
			if (this.channelColumns.size() == 0)
				return null;
			else {

				LinkedList <Double> times = new LinkedList <Double> ();
				ChannelDataTime currChan = this.channelColumns.get(0);
				LinkedList <Double> firstColTimes = currChan.getTime();
				
				for (int i = 0; i < firstColTimes.size(); i++) {
					times.add (firstColTimes.get(i));
					//System.out.println ("Time is " + firstColTimes.get(i));
				}
				
				for (int i =1; i< this.channelColumns.size(); i++) {
					currChan = this.channelColumns.get(i);
					times = currChan.mergeTime(times);
				}
				return times;
			}
		}
		
		public LinkedList <String> generateQueries (LinkedList <Double> times ) {
			
			LinkedList <String> queries = new LinkedList <String> ();
			
			
			for (int i= 0; i < times.size(); i++) {
				queries.addAll(this.generateQuery(times.get(i)));
			}

			
			Hashtable <Double, Double> forDebug = new Hashtable <Double, Double>();
			
			for (int i= 0; i < times.size(); i++) {

				if (forDebug.containsKey(times.get(i))) {
					log.debug ("\n\n\n\n ********** this time occurs multiple times" + times.get(i));
				
				}
				forDebug.put(times.get(i), new Double (1.0));
				
			}
			
			

			
			return queries;
		}
	}
	
	class ChannelDataTime {
		public timeDataSet timeAndData;
		public DBTableColName id;
		public String tableName;
		public String colName;
		
		public ChannelDataTime (DBTableColName name, LinkedList <Double>d, LinkedList<Double> times) {
			//System.out.println("Trying to create ChannelDataTime");

			this.timeAndData = new timeDataSet (d, times);
			this.id = name;
			this.tableName = id.tableName;
			this.colName =id.colName;
		}
		
		public boolean compareChannelTime (ChannelDataTime ch) {
			return this.timeAndData.checkTimes(ch.timeAndData);
		}
		
		public LinkedList <Double> mergeTime (ChannelDataTime ch) {
			return this.timeAndData.mergeTime(ch.timeAndData);
		}
		
		public LinkedList <Double> mergeTime (LinkedList<Double> time) {
			return this.timeAndData.mergeTime(time);
		}
		
		public LinkedList <Double> getTime () {
			return this.timeAndData.getTime();
		}
		
		public Double lookupData (Double timeIndex) {
			return this.timeAndData.lookupData(timeIndex);
		}

		public boolean dataExist (Double timeIndex) {
			return this.timeAndData.dataExist(timeIndex);
		}
		
		public boolean compareTableName (ChannelDataTime cdt) {
			if (this.tableName == cdt.tableName) {
				return true;
			}
			else return false;
		}
		
	}


	
	class timeDataSet {
		public LinkedList <Double>data ;
		public LinkedList <Double>time ;
		
		public timeDataSet (LinkedList <Double> d, LinkedList <Double> t) {
			this.data = new LinkedList <Double> ();
			this.time = new LinkedList <Double> ();
			
			//System.out.println("Trying to create TimeDataSet");
			for (int i = 0; i < d.size(); i++) {
				this.data.add( d.get(i));
				this.time.add( t.get(i));
				//System.out.println(data.get(i));
			}
		}
		
		public boolean checkTimes (timeDataSet td) {
			int tdSize = td.time.size();
			int timeSize = this.time.size();
			
			if ( tdSize != timeSize) {
				return false;
			}
			else {
				for (int i = 0; i < timeSize; i++) {
					Double thisD = this.time.get(i);
					Double anotherD = td.time.get(i);
					if (thisD != anotherD) {
						return false;
					}
				}
				return true;
			}
		}
		
		public LinkedList <Double> getTime () {
			return this.time;
		}
		public LinkedList <Double> mergeTime (timeDataSet td) {
			int tdSize = td.time.size();
			int timeSize = this.time.size();
	
			LinkedList <Double> mergedTime = new LinkedList <Double>();
			
			for (int i = 0; i < tdSize; i++ ) {
				Double oneTime = td.time.get(i);
				mergedTime.add(oneTime);
			}
				
			for (int j = 0; j < timeSize; j++) {
				Double anotherTime = this.time.get(j);
				boolean exist = false;
				
				for (int i= 0; i < tdSize; i++ ) {
					Double oneTime = td.time.get(i);
		
					if (anotherTime == oneTime) {
						exist = true;
					}
				}
				if (!exist) {
					mergedTime.add(anotherTime);
				}
			}
			
			return mergedTime;
		}

			
		
		public LinkedList <Double> mergeTime (LinkedList <Double> tList) {
			int tdSize = tList.size();
	
			//System.out.println ("tdSize is " + tdSize);
			//System.out.println ("timeSize is " + timeSize);
			
			LinkedList <Double> mergedTime = new LinkedList <Double>();
			
			for (int i = 0; i < tdSize; i++ ) {
				Double oneTime = tList.get(i);
				mergedTime.add(oneTime);
			}
				
			for (int j = 0; j < mergedTime.size(); j++) {
				Double mTime = mergedTime.get(j);
				boolean exist = false;
				
				for (int i= 0; i < tdSize; i++ ) {
					Double oneTime = tList.get(i);
		
					if (mTime == oneTime) {
						exist = true;
					}
				}
				if (!exist) {
					mergedTime.add(mTime);
				}
			}
			
			return mergedTime;
		}
		
		public boolean dataExist (Double timeIndex) {
			return this.time.contains(timeIndex);
		}
		public Double lookupData (Double timeIndex) {
			int ind = this.time.indexOf(timeIndex);
			return this.data.get(ind);
		}
		
		
		public void display () {
			for (int i = 0; i < this.time.size(); i++) {
				System.out.println (Double.toString(this.time.get(i)));
			}
		}
	}
	
	class DBTableColName {
		public String tableName;
		public String colName;
		
		public DBTableColName (String t, String c) {
			this.tableName=t;
			this.colName = c;
		}
		
	}
	
	
	
	
	
	/** @brief a method hat will @return a channel map containing only channels with duration */
	public ChannelMap getCleanChannelMap() throws SAPIException {
		
		System.out.println("Getting a clean channel map");
		ChannelMap retval = new ChannelMap();
		double channelEndTime = -1;
		
		retval.Add(pattern);
		this.sink.RequestRegistration(retval);
		retval = this.sink.Fetch(-1, retval);

		for (int i = 0; i < this.map.GetChannelList().length; i++) {
			System.out.println("This map has the channel: " + this.map.GetName(i));
		}
		//log.debug ("this.map.GetName = " + this.map.GetName(i));
		
		/**
		 * This is for getChannelMap names
		 * 
		 */

		
		for(int i=0; i < this.map.NumberOfChannels(); i++) {
			System.out.println("Here is the list of channels:  " + retval.GetName(i) + " ** type = "+ retval.GetType(i));
			
			
			if (0 < this.map.GetTimeDuration(i) &&
					!this.map.GetName(i).matches("_.*")) {
				
				if (useSynthetic) {
					if (this.map.GetName(i).matches(".*Synthetic.*")) { // if use synthetic and it *is*
						log.debug("Adding this channel map:  >>>"+ this.map.GetName(i));
						retval.Add(this.map.GetName(i));
						/// @note copies over metadata for valid channels
						retval.PutUserInfo(retval.GetIndex(this.map.GetName(i)), this.map.GetUserInfo(i));
						channelEndTime = this.map.GetTimeStart(i) + this.map.GetTimeDuration(i);
					} // if synthetic
				} else if (this.map.GetName(i).matches(".*SatSim.*")) { //  if the name is not Synthetic
					
					System.out.println("the name of the channel map is:  >>>"+ this.map.GetName(i));
					//log.debug("We are about to get name for this map");
					retval.Add(this.map.GetName(i));
					//log.debug ("this.map.GetName = " + this.map.GetName(i));
					
					/// @note copies over metadata for valid channels
					//retval.PutUserInfo(retval.GetIndex(this.map.GetName(i)), this.map.GetUserInfo(i));
					
					//log.debug("We are about to get the duration time");
					channelEndTime = this.map.GetTimeStart(i) + this.map.GetTimeDuration(i);
					//log.debug("Here is the start time" + this.map.GetTimeStart(i));
					//log.debug("Here is the duration time" + this.map.GetTimeDuration(i));
					//log.debug("Here is the end time" + (this.map.GetTimeDuration(i) + this.map.GetTimeStart(i)));
					
				} // if synthetic
			} // if
		} // for i

		return retval;
	} // getCleanChannelMap()
	
	
	public static void main(String[] args) {
		// start from command line
		RbnbToWaterDBSink thisSink = new RbnbToWaterDBSink();
		

		
		if (thisSink.parseArgs(args) && thisSink.open()) {
			// do some stuff
			ChannelMap reqMap = null;
			System.out.println(thisSink.getStartupMessage());

			WaterDBOps db = new WaterDBOps();
			// Connection dbc = db.DBConnect();

			// do {
			try {
				Connection dbc = db.DBConnect();

				thisSink.refreshChannelMap();

				reqMap = thisSink.getCleanChannelMap();
				System.out.println(reqMap);
				
				double duration = 10000.0;

				
				if (thisSink.isMonitoring()) {
					System.out.println("the last time stamp is "
							+ (thisSink.lastTime));

					
					thisSink.getSink().Subscribe(reqMap, "newest", 1);
					
					//thisSink.getSink().Request(reqMap, thisSink.lastTime, duration+100.0, "newest");

				} else {
					// log.debug("Inside else");
					System.out.println("inside else \n");

					thisSink.getSink().Request(reqMap, thisSink.getRbnbStart(),
							thisSink.getRbnbDuration(), "absolute");
				}
			} catch (Exception e) {
				e.printStackTrace();
				log.warn("couldn't subscribe " + e);
			}

			do {
				Connection dbc = null;
				try {
					
					System.out.println("inside the do loop");
					
					// thisSink.getSink().Fetch(600000, reqMap);
					thisSink.getSink().Fetch(70000, reqMap);
					// thisSink.getSink().Fetch(6000000, reqMap);
					dbc = db.DBConnect();

					// log.debug("After fetching");
					
					thisSink.maxID = db.getMaxID();
					
					System.out.println("max ID = " + thisSink.maxID);
					

					LinkedList<String> queries = thisSink.generateQueries(reqMap);
					
					for (int i = 0; i < queries.size(); i++) {
						// System.out.println("Insert Starting Query = " +
						// queries.get(i));
						db.DBInsert(dbc, queries.get(i));
						// System.out.println("Insert Ended");
					}
					
				} catch (Exception e) {
					e.printStackTrace();
					log.warn("couldn't get data from the channel map: " + e);
				}
				
				try {
					db.DBCloseConnection(dbc);
				} catch (SQLException e) {
					e.printStackTrace();
					log.warn("couldn't get data from the database connection: " + e);
				}
				
			} while (thisSink.isMonitoring());

/*			try {
				db.DBCloseConnection(dbc);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace(); 
			}*/
		} // if sink is up
	} // main()
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.nees.rbnb.RBNBBase#setOptions()
	 */
	protected Options setOptions() {
		Options opt = setBaseOptions(new Options()); // uses h, v, s, p
		opt.addOption("m",false,"flag to set real-time monitor mode");
		opt.addOption("d",true,"database host to connect to");
		opt.addOption("f",false,"flag to use *only* channels with \"Synthetic\" in their names");
		return opt;
	}

	
	/*!
	 * @brief function to parse the arguments (retuired by superclass)
	 * @see org.nees.rbnb.RBNBBase#setArgs(org.apache.commons.cli.CommandLine)
	 */
	protected boolean setArgs(CommandLine cmd) {
		if (!setBaseArgs(cmd)) return false;

		if (cmd.hasOption('m')) {
			useMonitor = true;
		}
		
		if (cmd.hasOption('d')) {
			useDb = true;
			dbHost = cmd.getOptionValue('d');
		}
		
		if (cmd.hasOption('f')) {
			useSynthetic = true;
		}
		return true;
	}
	
	
	public String getStartupMessage() {
		String retval = "";
		
		if (useMonitor) {
			retval += ("MONITORING\n");
		} else {
			retval += ("NOT MONITORING\n");
		}
		
		if (useDb) {
			retval += ("using DB at: " + dbHost + "\n");
		} else {
			retval += ("NO DB\n");
		}
		
		
		return retval;
	}
	
	protected String getCVSVersionString ()
	{
		return (
				"$LastChangedDate: 2007-12-17 16:13:22 -0800 (Mon, 17 Dec 2007) $\n" +
				"$LastChangedRevision: 208 $\n" +
				"$LastChangedBy: ljmiller $\n" +
				"$HeadURL: file:///Users/hubbard/code/cleos-svn/cleos-rbnb-apps/trunk/src/edu/sdsc/cleos/RbnbToWaterDBSink.java $\n"
		);
	}
	

}
