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

public class RbnbToDbSink extends RBNBBase {
	private Sink		sink	= null;
	private double rbnbStart 	= Double.MAX_VALUE;
	private double rbnbDuration = -1.0;
	private ChannelMap	map		= null;
	private static Log 	log		= LogFactory.getLog(RbnbToDbSink.class.getName());
	private static DecimalFormat onePoint = new DecimalFormat("#.#");
	// a flag to put data requests into monitor mode - if false, the whole ring buffer is requested
	private boolean useMonitor = false;
	// a flag to put insert data into a database
	private boolean useDb = false;
	private String dbHost = null;
	// a flag to use only channels with Synthetic in their names
	private boolean useSynthetic = false;
	
	private String pattern = "James Reserve NEON PoP/NWP/*";
	
	private Hashtable<String, DBTableColName> lookupTable = null;
	private StringTokenizer st = null;
	
	private double lastTime = 0.0;
	
	public RbnbToDbSink() {
		sink = new Sink();
		map = new ChannelMap();
		this.createDBTblColNames();
	} // constructor
	
	public void createDBTblColNames () {

		lookupTable = new Hashtable <String, DBTableColName> (); 
		
		lookupTable.put("LI190QS", new DBTableColName("S_Quantum_Sensor", "Par")); 
		lookupTable.put("BF3TR", new DBTableColName ("Sunshine_Duration", "Global_Radiation"));
		lookupTable.put("BF3DR", new DBTableColName ("Sunshine_Duration", "Diffusion_Radiation"));
		lookupTable.put("BF3SS", new DBTableColName ("Sunshine_Duration", "Sunshine_State"));
		
		lookupTable.put("PTB210", new DBTableColName ("S_Barometer", "Pressure"));
		
		lookupTable.put("HMP45T", new DBTableColName ("S_Airtemp_Humidity", "Temperature"));
		lookupTable.put("HMP45H", new DBTableColName ("S_Airtemp_Humidity", "Relative_Humidity"));
		
		lookupTable.put("WXT510T", new DBTableColName ("Weather", "Air_Temperature"));
		lookupTable.put("WXT510H", new DBTableColName ("Weather", "Relative_Humidity"));
	    lookupTable.put("WXT510P", new DBTableColName ("Weather", "Barometer_Pressure"));
	    lookupTable.put("WXT510RA", new DBTableColName ("Weather", "Rain_Fall_Accum"));
	    lookupTable.put("WXT510RD", new DBTableColName ("Weather", "Rain_Duration"));
	    lookupTable.put("WXT510RI", new DBTableColName ("Weather", "Rain_Intensity"));
	    lookupTable.put("WXT510HA", new DBTableColName ("Weather", "Hail_Fall_Accum"));
	    lookupTable.put("WXT510HD", new DBTableColName ("Weather", "Hail_Duration"));
	    lookupTable.put("WXT510HI", new DBTableColName ("Weather", "Hail_Intensity"));
		lookupTable.put("WXT510RPI", new DBTableColName ("Weather", "Rain_Peak_Intensity"));
	    lookupTable.put("WXT510HPI", new DBTableColName ("Weather", "Hail_Peak_Intensity"));
		lookupTable.put("WXT510WDMIN", new DBTableColName ("Weather", "Wind_Direction_Min"));
		lookupTable.put("WXT510WDAVG", new DBTableColName ("Weather", "Wind_Direction_Avg"));
		lookupTable.put("WXT510WDMAX", new DBTableColName ("Weather", "Wind_Direction_Max"));
		lookupTable.put("WXT510WSMIN", new DBTableColName ("Weather", "Wind_Speed_Min"));
		lookupTable.put("WXT510WSAVG", new DBTableColName ("Weather", "Wind_Speed_Avg"));
		lookupTable.put("WXT510WSMAX", new DBTableColName ("Weather", "Wind_Speed_Max"));
		
		lookupTable.put("0", new DBTableColName ("Fake_Daq", "Ch0"));
		lookupTable.put("1", new DBTableColName ("Fake_Daq", "Ch1"));
		lookupTable.put("2", new DBTableColName ("Fake_Daq", "Ch2"));
		lookupTable.put("3", new DBTableColName ("Fake_Daq", "Ch3"));
		lookupTable.put("4", new DBTableColName ("Fake_Daq", "Ch4"));
		lookupTable.put("5", new DBTableColName ("Fake_Daq", "Ch5"));
		lookupTable.put("6", new DBTableColName ("Fake_Daq", "Ch6"));
		lookupTable.put("7", new DBTableColName ("Fake_Daq", "Ch7"));
		lookupTable.put("8", new DBTableColName ("Fake_Daq", "Ch8"));
		lookupTable.put("9", new DBTableColName ("Fake_Daq", "Ch9"));
		lookupTable.put("10", new DBTableColName ("Fake_Daq", "Ch10"));
		lookupTable.put("11", new DBTableColName ("Fake_Daq", "Ch11"));
		lookupTable.put("12", new DBTableColName ("Fake_Daq", "Ch12"));
		lookupTable.put("13", new DBTableColName ("Fake_Daq", "Ch13"));
		lookupTable.put("14", new DBTableColName ("Fake_Daq", "Ch14"));
		lookupTable.put("15", new DBTableColName ("Fake_Daq", "Ch15"));
		
	}
	
	
	public boolean open() {
		try {
			sink.OpenRBNBConnection(getServer(), getRBNBClientName());
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
		
		BunchOfChannels allChannels = new BunchOfChannels();
		
		//log.warn("Hihihihihi");
		for (int i=0; i<cmap.NumberOfChannels(); i++) {

			int typeid = cmap.GetType(i);
			String typeName = cmap.TypeName(typeid);
			//log.warn(cmap.GetName(i)+ " has the type for the data channel"+ typeName);
			
			double[] someData = cmap.GetDataAsFloat64(i);
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
		

		
		public String generateQuery (Double timeIndex) {

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
			
			String insert = "INSERT INTO ";

			String columnNames = "time_tag, ";
			String columnValues =  "'" + ISOtoRbnbTime.formatDateForDB( (long)(timeIndex*1000) ) + "-8', ";
			
			if (colNames.size() == 0) {
				return null;
			}
			
			else {
				for (int i = 0; i < (colNames.size() - 1); i++) {
					columnNames += colNames.get(i) + ", ";
					columnValues += data.get(i) + ", ";
				}	

				columnNames += colNames.get(colNames.size() - 1);
				columnValues += data.get(data.size() - 1);

				String theQuery = insert + "SS."+ this.tableName + "(" + columnNames
						+ ") VALUES ( " + columnValues + ")";

				//log.debug ("Creating query for this time " + timeIndex.toString());
				return theQuery;
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
				queries.add(this.generateQuery(times.get(i)));
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
		

		ChannelMap retval = new ChannelMap();
		double channelEndTime = -1;
		
		retval.Add(pattern);
		this.sink.RequestRegistration(retval);
		retval = this.sink.Fetch(-1, retval);

		for (int i = 0; i < this.map.GetChannelList().length; i++) {
			log.debug("This map has the channel: " + this.map.GetName(i));
		}
		//log.debug ("this.map.GetName = " + this.map.GetName(i));
		
		/**
		 * This is for getChannelMap names
		 * 
		 */

		
		for(int i=0; i < this.map.NumberOfChannels(); i++) {
			log.debug("Here is the list of channels:  " + retval.GetName(i) + " ** type = "+ retval.GetType(i));
			
			
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
				} else if (this.map.GetName(i).matches(".*NWP.*")) { //  if the name is not Synthetic
					
					//log.debug("the name of the channel map is:  >>>"+ this.map.GetName(i));
					//log.debug("We are about to get name for this map");
					retval.Add(this.map.GetName(i));
					//log.debug ("this.map.GetName = " + this.map.GetName(i));
					
					/// @note copies over metadata for valid channels
					retval.PutUserInfo(retval.GetIndex(this.map.GetName(i)), this.map.GetUserInfo(i));
					
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
		RbnbToDbSink thisSink = new RbnbToDbSink();
		

		
		if (thisSink.parseArgs(args) && thisSink.open()) {
			// do some stuff
			ChannelMap reqMap = null;
			System.out.println(thisSink.getStartupMessage());

			SSDBOps db = new SSDBOps();
			// Connection dbc = db.DBConnect();

			// do {
			try {
				Connection dbc = db.DBConnect();

				thisSink.refreshChannelMap();

				reqMap = thisSink.getCleanChannelMap();

				double duration = 10000.0;

				if (thisSink.isMonitoring()) {
					System.out.println("the last time stamp is "
							+ (thisSink.lastTime));

					thisSink.getSink().Subscribe(reqMap, "newest", 1);
					// thisSink.getSink().Request(reqMap, thisSink.lastTime,
					// duration+100.0, "newest");

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
					// thisSink.getSink().Fetch(600000, reqMap);
					thisSink.getSink().Fetch(600000, reqMap);
					// thisSink.getSink().Fetch(6000000, reqMap);
					dbc = db.DBConnect();

					// log.debug("After fetching");

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
				"$LastChangedDate:2007-04-10 22:04:28 -0700 (Tue, 10 Apr 2007) $\n" +
				"$LastChangedRevision:156M $\n" +
				"$LastChangedBy:(local) $\n" +
				"$HeadURL:http://nladr-cvs.sdsc.edu/svn-private/NEON/telepresence/dataturbine/trunk/src/edu/sdsc/cleos/RbnbToDbSink.java $\n"
		);
	}
	

}
