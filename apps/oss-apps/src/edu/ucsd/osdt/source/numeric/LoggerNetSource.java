/*!
 * @file LoggerNetSource.java
 * @author Lawrence J. Miller <ljmiller@sdsc.edu>
 * @author $LastChangedBy$
 * @author Cyberinfrastructure Laboratory for Environmental Observing Systems (CLEOS)
 * @author San Diego Supercomputer Center (SDSC)
 * @date $LastChangedDate$
 * @version $LastChangedRevision$
 * @note $HeadURL$
 */
package edu.ucsd.osdt.source.numeric;


import edu.ucsd.osdt.util.RBNBBase;
import edu.ucsd.osdt.util.ISOtoRbnbTime;
import edu.ucsd.osdt.source.BaseSource;

//rbnb
import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Source;
// java
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import edu.ucsd.osdt.util.ISOtoRbnbTime;

/*! @brief A Dataturbine source accumulator that parses and puts Campbell
 *  Loggernet data onto the ring buffer. */
class LoggerNetSource extends RBNBBase {
	private String DEFAULT_FILE_NAME = "loggernet.dat";
	private String loggernetFileName = DEFAULT_FILE_NAME;
	private BufferedReader loggernetFileBuffer = null;
	private String[] channels = null;
	private String[] units = null;
	// rbnb
	private static final int DEFAULT_CACHE_SIZE = 900;
	private int rbnbCacheSize = DEFAULT_CACHE_SIZE;
	private static final int DEFAULT_ARCHIVE_SIZE = 0;
	private int rbnbArchiveSize = DEFAULT_ARCHIVE_SIZE;
	private BaseSource source = null;
	private ChannelMap cmap = null;
	// java
	private static Logger logger = Logger.getLogger(LoggerNetSource.class.getName());
	
	public LoggerNetSource() {
		super(new BaseSource());
		/*! @note Add in a hook for ctrl-c's and other abrupt death */
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				logger.info("Shutdown hook activated for " + SeabirdSource.class.getName() + ". Exiting.");
				closeRbnb();
				Runtime.getRuntime().halt(0);
			} // run ()
		}); // addHook
	}
	
	
	/*! @brief instantiates file reading operations */
	public void initFile() throws FileNotFoundException {
		loggernetFileBuffer = new BufferedReader(new FileReader(loggernetFileName));
	}
	
	
	/*! @brief Sets up the rbnb channel map */
	public void initCmap() throws IOException, SAPIException {
		cmap = new ChannelMap();
		/* todo setp channels and units from file header like these:
		 	"TIMESTAMP","RECORD","AirTemp_C_Max","AirTemp_C_TMx","AirTemp_C_Min","AirTemp_C_TMn","AirTemp_C_Avg","RH_Avg","Rain_mm_Tot","WindSp_ms_Max","WindSp_ms_TMx","BP_mbar_Avg","SoilTemp_C_Avg","SoilWVC_Avg"
			"TS","RN","Deg C","Deg C","Deg C","Deg C","Deg C","%","mm","meters/second","meters/second","mbar","Deg C","%"
		 */
		loggernetFileBuffer.readLine();
		String[] channelsTmp = loggernetFileBuffer.readLine().split(",");
		String[] unitsTmp = loggernetFileBuffer.readLine().split(",");
		channels = new String[channelsTmp.length];
		units = new String[unitsTmp.length];
		
		// clean off the double quotes
		for(int i=0; i<channelsTmp.length; i++) {
			channels[i] = channelsTmp[i].substring(1, channelsTmp[i].length()-1);
			units[i] = unitsTmp[i].substring(1, unitsTmp[i].length()-1);
		}
		
		loggernetFileBuffer.readLine();
		logger.finer("Channel length:" + Integer.toString(channels.length) + " Unit length:" + Integer.toString(units.length));
		for(int i=0; i<channels.length; i++) {
			cmap.Add(channels[i]);
			cmap.PutUserInfo( cmap.GetIndex(channels[i]), ("units="+units[i]) );
			cmap.PutMime(cmap.GetIndex(channels[i]), "application/octet-stream");
		}
	}
	

	/*! @brief Sets up the connection to an rbnb server. */
	public void initRbnb() throws SAPIException, IOException {
		if (rbnbArchiveSize > 0) {
			source=new BaseSource(rbnbCacheSize, "append", rbnbArchiveSize);
		} else {
			source=new BaseSource(rbnbCacheSize, "none", 0);
		}
		this.initCmap();
		myBaseSource.OpenRBNBConnection(serverName, rbnbClientName);
		logger.config("Set up connection to RBNB on " + serverName +
				" as source = " + rbnbClientName);
		logger.config(" with RBNB Cache Size = " + rbnbCacheSize + " and RBNB Archive Size = " + rbnbArchiveSize);
		source.Register(cmap);
		source.Flush(cmap);
	}
	
	
	/*! @brief Gracefully closes the rbnb connection. */
	protected void closeRbnb() {
		if(source == null) {
			return;
		}

		if (rbnbArchiveSize > 0) { // then close and keep the ring buffer
			source.Detach();
		} else { // close and scrap the cache
			source.CloseRBNBConnection();
		}
		logger.config("Closed RBNB connection");
	}
	
	
	public double getRBNBDate(String loggernetDate) {
		/*! @note ISORbnbTime uses ISO8601 timestamp, e.g. 2003-08-12T19:21:22.30095 */
		/*! @note from loggernet: "2007-11-12 07:30:00" */
		String[] loggernetDateTokens = loggernetDate.split(" ");
		StringBuffer retval = new StringBuffer();
		
		retval.append(loggernetDateTokens[0]);
		retval.append("T");
		retval.append(loggernetDateTokens[1]);
		// time
		retval.append(".00000");
		String iso8601String = retval.toString();
		logger.finer("ISO8601:" + iso8601String);
		ISOtoRbnbTime rbnbTimeConvert = new ISOtoRbnbTime(iso8601String);
		return rbnbTimeConvert.getValue();
	}
	
	
	/* todo parse out data from line like this:
	"2007-11-12 07:30:00",0,1.994,1.885,253.6,18.72,0,24.27,84.5,542.1,381.2,0.19,533.3,0.272,739.2,0.134,402.2,0.037,91.5,0,26.02,0.011,12.23,22.56,13.08,0.209,0.302,0.148
	*/
	private void processFile() throws IOException, SAPIException {
		String lineRead = null;
		String[] lineSplit = null;
		String dateString = null;
		double[] lineData = null;
		
		while((lineRead = loggernetFileBuffer.readLine()) != null) {
			logger.finer("Lineread:" + lineRead);
			lineSplit = lineRead.split(",");
			// gotta convert from strings to doubles the old-fashioned way - the first element is a timestamp
			lineData = new double[lineSplit.length];
			for(int i=0; i<lineSplit.length; i++) {
				logger.finer("Data token:" + lineSplit[i]);
				if(i==0) { // timestamp - handle specially
					
				
					dateString = lineSplit[i].substring( 1, (lineSplit[i].length()-1) );
					lineData[i] = getRBNBDate(dateString);
					logger.finer( "Nice date:" + ISOtoRbnbTime.formatDate((long)lineData[i]*1000) );
				
				
				
				} else if(lineSplit[i].equals("\"NAN\"")) {
					lineData[i] = Double.NaN;
				} else { // it's a double
					lineData[i] = Double.parseDouble(lineSplit[i]);
				}
			}
			postData(lineData);
		} // while
	} // processFile()
	
	
	private void postData(double[] someData) throws SAPIException {
		// put data onto the ring buffer
			for(int i=1; i<someData.length; i++) {
				cmap.PutTime(someData[0], 0.0);
				double[] dataTmp = new double[1];
				dataTmp[0] = someData[i];
				cmap.PutDataAsFloat64(cmap.GetIndex(channels[i]), dataTmp);
				logger.finer("Posted data:" + someData[i] + " into channel:" + channels[i]);
				source.Flush(cmap);
			}
	}
	
	
	/*! @brief Setup of command-line args, acquisition of metadata, and launch of main program loop. */
	/*****************************************************************************/
	public static void main(String[] args) {
		LoggerNetSource loggernet = new LoggerNetSource();
		if(! loggernet.parseArgs(args)) {
			logger.severe("Unable to process command line. Terminating.");
			System.exit(1);
		}
		// setup
		try {
			
			loggernet.initFile();
			loggernet.initRbnb();
			loggernet.processFile();
			
		} catch(SAPIException sae) {
			logger.severe("Unable to communicate with DataTurbine server. Terminating: " + sae.toString());
			sae.printStackTrace();
			System.exit(3);
		} catch(FileNotFoundException fnf) {
			logger.severe("Unable to open input data file:" + loggernet.loggernetFileName + ". Terminating: " + fnf.toString());
			System.exit(4);
		} catch(IOException ioe) {
			logger.severe("Unable to read input data file:" + loggernet.loggernetFileName + ". Terminating: " + ioe.toString());
			System.exit(5);
		}
	} // main()
	/*****************************************************************************/
	
	
	/*! @brief Command-line processing.
	 * @note required by interface RBNBBase */
	protected Options setOptions() {
		Options opt = setBaseOptions(new Options()); // uses h, v, s, p, S

		opt.addOption("z",true, "DataTurbine cache size *" + DEFAULT_CACHE_SIZE);
		opt.addOption("Z",true, "Dataturbine archive size *" + DEFAULT_ARCHIVE_SIZE);
		opt.addOption("f",true, "Input LoggerNet file name *" + DEFAULT_FILE_NAME);

		return opt;
	} // setOptions()


	/*! @brief Command-line processing.
	 * @note required by interface RBNBBase */
	protected boolean setArgs(CommandLine cmd) throws IllegalArgumentException {
		if (!setBaseArgs(cmd)) return false;

	if(cmd.hasOption('z')) {
			String a=cmd.getOptionValue('z');
			if(a!=null) {
				try {
					Integer i =  new Integer(a);
					int value = i.intValue();
					rbnbCacheSize = value;
				} catch(Exception e) {
					logger.severe("Enter a numeric value for -z option. " + a + " is not valid!");
					return false;   
				}
			} // if
		}	    
		if (cmd.hasOption('Z')) {
			String a=cmd.getOptionValue('Z');
			if (a!=null) {
				try {
					Integer i =  new Integer(a);
					int value = i.intValue();
					rbnbArchiveSize = value;
				} catch (Exception e) {
					logger.severe("Enter a numeric value for -Z option. " + a + " is not valid!");
					return false;   
				} 
			}
		}
		if(cmd.hasOption('f')) { // loggernet file name
			String v = cmd.getOptionValue("f");
			loggernetFileName = v;
		}
		return true;
	} // setArgs()
	
} // class