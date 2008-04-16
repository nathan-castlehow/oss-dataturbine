package edu.sdsc.cleos;
/** @brief a class that will use the sink features of an rbnb plugin to
 * verify data exists in an rbnb ring buffer
 * @author ljmiller
 * @since 070128
 */

import com.rbnb.sapi.Sink;
import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;

import edu.sdsc.cleos.ISOtoRbnbTime;
import java.text.DecimalFormat;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nees.rbnb.RBNBBase;

public class TestNwpSink extends RBNBBase{
	private Sink		sink	= null;
	private double rbnbStart 	= Double.MAX_VALUE;
	private double rbnbDuration = -1.0;
	private ChannelMap	map		= null;
	private static Log 	log		= LogFactory.getLog(TestNwpSink.class.getName());
	private static DecimalFormat onePoint = new DecimalFormat("#.#");
	// a flag to put data requests into monitor mode - if false, the whole ring buffer is requested
	private boolean useMonitor = false;
	// a flag to put insert data into a database
	private boolean useDb = false;
	private String dbHost = null;
	// a flag to use only channels with Synthetic in their names
	private boolean useSynthetic = false;
	
	public TestNwpSink() {
		sink = new Sink();
		map = new ChannelMap();
	} // constructor
	
	
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
		sink.RequestRegistration(this.map);
		this.map = this.sink.Fetch(1000);
		
		for(int i=0; i<this.map.NumberOfChannels(); i++) {
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
	
	
	public void mapDump(ChannelMap cmap) {
		for (int i=0; i<cmap.NumberOfChannels(); i++) {
			double[] someData = cmap.GetDataAsFloat64(i);
			double[] someTimes = cmap.GetTimes(i);
			// @todo fixme - weird hack to get units
			String unitsInfo = this.map.GetUserInfo(this.map.GetIndex(cmap.GetName(i)));
			String[] unitSplit = unitsInfo.split("=");
		
			for (int j=0; j<someData.length; j++) {
				// @todo do DB insert here
				log.debug(cmap.GetName(i) + " - " + Double.toString(someData[j]) + " " +
						unitSplit[1] + " time:" + ISOtoRbnbTime.formatDate( (long)(someTimes[j]*1000) ));
			} // for j - data loop
		} // for i - cmap
	} // mapDump() 
	
	
	/** @brief a method hat will @return a channel map containing only channels with duration */
	public ChannelMap getCleanChannelMap() throws SAPIException {
		ChannelMap retval = new ChannelMap();
		double channelEndTime = -1;
		
		/** @TODO generalize this filter - it is hard-coded to exclude a fake-daq source with the
		 *  string Syntheic' in its label */
		for(int i=0; i < this.map.NumberOfChannels(); i++) {
			if (0 < this.map.GetTimeDuration(i) &&
					!this.map.GetName(i).matches("_.*")) {
				
				if (useSynthetic) {
					if (this.map.GetName(i).matches(".*Synthetic.*")) { // if use synthetic and it *is*
						retval.Add(this.map.GetName(i));
						/// @note copies over metadata for valid channels
						retval.PutUserInfo(retval.GetIndex(this.map.GetName(i)), this.map.GetUserInfo(i));
						channelEndTime = this.map.GetTimeStart(i) + this.map.GetTimeDuration(i);
					} // if synthetic
				} else if (!this.map.GetName(i).matches(".*Synthetic.*")) { //  if the name is not Synthetic
					retval.Add(this.map.GetName(i));
					/// @note copies over metadata for valid channels
					retval.PutUserInfo(retval.GetIndex(this.map.GetName(i)), this.map.GetUserInfo(i));
					channelEndTime = this.map.GetTimeStart(i) + this.map.GetTimeDuration(i);
				} // if synthetic
			} // if
		} // for i
		
		return retval;
	} // getCleanChannelMap()
	
	
	public static void main(String[] args) {
		// start from command line
		TestNwpSink thisSink = new TestNwpSink();
		if (thisSink.parseArgs(args) && thisSink.open())
		{
			// do some stuff
			ChannelMap reqMap=null;
			
			do {
				try {
					thisSink.refreshChannelMap();
					reqMap = thisSink.getCleanChannelMap();

					if (thisSink.isMonitoring()) {
						thisSink.getSink().Request(reqMap, 0, 0, "newest");
					} else {
						thisSink.getSink().Request(reqMap, thisSink.getRbnbStart(), thisSink.getRbnbDuration(), "absolute");
					}

					thisSink.getSink().Fetch(1000, reqMap);
					thisSink.mapDump(reqMap);
				} catch (Exception e) {
					log.warn("couldn't get data from the channel map: " + e);
				}	
			} while (thisSink.isMonitoring());
		} // if sink is up
	} // main()
	
	
	/* (non-Javadoc)
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
				"$LastChangedDate$\n" +
				"$LastChangedRevision$\n" +
				"$LastChangedBy$\n" +
				"$HeadURL$\n"
		);
	}
	

}
