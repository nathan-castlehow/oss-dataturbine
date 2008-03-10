/*!
 * @file LoggerNetSource.java
 * @file $HeadURL: file:///Users/hubbard/code/cleos-svn/cleos-rbnb-apps-dev/turbine-dev/src/edu/sdsc/cleos/LoggerNetSource.java $
 * @author Lawrence J. Miller <ljmiller@sdsc.edu>
 * @author Sameer Tilak <tilak@sdsc.edu>
 * @author $LastChangedBy: ljmiller $
 * @author Cyberinfrastructure Laboratory for Environmental Observing Systems (CLEOS)
 * @author San Diego Supercomputer Center (SDSC)
 * @date $LastChangedDate: 2008-02-27 17:14:17 -0800 (Wed, 27 Feb 2008) $
 * @version $LastChangedRevision: 259 $
 */
package edu.sdsc.cleos;

import edu.sdsc.cleos.RBNBBase;

//rbnb
import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Source;
// java
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

class LoggerNetSource extends RBNBBase {
	private String DEFAULT_FILE_NAME = "loggernet.dat";
	private String loggernetFileName = DEFAULT_FILE_NAME;
	// rbnb
	private static final int DEFAULT_CACHE_SIZE = 900;
	private int rbnbCacheSize = DEFAULT_CACHE_SIZE;
	private static final int DEFAULT_ARCHIVE_SIZE = 0;
	private int rbnbArchiveSize = DEFAULT_ARCHIVE_SIZE;
	private CleosSource source = null;
	private ChannelMap cmap = null;
	// java
	private static Logger logger = Logger.getLogger(LoggerNetSource.class.getName());
	
	public LoggerNetSource() {
		super();
	}
	
	
	/*! @brief Sets up the rbnb channel map */
	public void initCmap() throws SAPIException {
		cmap = new ChannelMap();
		/* todo setp channels and units from file header like these:
		 	"TIMESTAMP","RECORD","AirTemp_C_Max","AirTemp_C_TMx","AirTemp_C_Min","AirTemp_C_TMn","AirTemp_C_Avg","RH_Avg","Rain_mm_Tot","WindSp_ms_Max","WindSp_ms_TMx","BP_mbar_Avg","SoilTemp_C_Avg","SoilWVC_Avg"
			"TS","RN","Deg C","Deg C","Deg C","Deg C","Deg C","%","mm","meters/second","meters/second","mbar","Deg C","%"
		 */ 
		
	}

	
	private double[] processFile(String fileName) {
		double[] retval = new double[1];
		/* todo parse out data from line like this:
		"2008-01-23 07:00:00",71,32.25,"2008-01-22 13:14:10",20.12,"2008-01-23 04:36:40",24.67,79.8,1.016,7.781,"2008-01-22 10:46:40",1009,27.38,0.468 */
		return retval;
	}
	
	
	private void postData(double[] someData) {
		// put data onto the ring buffer
	}
	

	/*! @brief Sets up the connection to an rbnb server. */
	public void initRbnb() throws SAPIException {
		if (rbnbArchiveSize > 0) {
			source=new CleosSource(rbnbCacheSize, "append", rbnbArchiveSize);
		} else {
			source=new CleosSource(rbnbCacheSize, "none", 0);
		}
		this.initCmap();
		source.OpenRBNBConnection(getServer(), getRBNBClientName());
		logger.config("Set up connection to RBNB on " + getServer() +
				" as source = " + getRBNBClientName());
		logger.config(" with RBNB Cache Size = " + rbnbCacheSize + " and RBNB Archive Size = " + rbnbArchiveSize);
		source.Register(cmap);
		source.Flush(cmap);
	}
	
	
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
	
	
	/*! @note required by interface RBNBBase */
	protected String getCVSVersionString() {
		return getSVNVersionString();
	}


	/*! @note svn keywords */
	protected String getSVNVersionString() {
		return(
				"$LastChangedDate: 2008-02-27 17:14:17 -0800 (Wed, 27 Feb 2008) $\n" +
				"$LastChangedRevision: 259 $\n" +
				"$LastChangedBy: ljmiller $\n" +
				"$HeadURL: file:///Users/hubbard/code/cleos-svn/cleos-rbnb-apps-dev/turbine-dev/src/edu/sdsc/cleos/LoggerNetSource.java $"
		);
	}
	
} // class