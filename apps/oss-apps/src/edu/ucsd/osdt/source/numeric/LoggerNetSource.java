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
import edu.ucsd.osdt.source.numeric.LoggerNetParser;

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
	// rbnb
	private static final int DEFAULT_CACHE_SIZE = 900;
	private int rbnbCacheSize = DEFAULT_CACHE_SIZE;
	private static final int DEFAULT_ARCHIVE_SIZE = 0;
	private int rbnbArchiveSize = DEFAULT_ARCHIVE_SIZE;
	private ChannelMap cmap = null;
	private LoggerNetParser parser = null;
	
	public LoggerNetSource() {
		super(new BaseSource(), null);
		parser = new LoggerNetParser();
		logger = Logger.getLogger(LoggerNetSource.class.getName());
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
	
	
	/*! @brief Sets up the rbnb channel map using a LoggerNetParser */
	public void initCmap() throws IOException, SAPIException {
		StringBuffer mdBuffer = new StringBuffer();
		
		// junk line
		loggernetFileBuffer.readLine();
		
		String fileLine1 = loggernetFileBuffer.readLine();
		mdBuffer.append(fileLine1);
		logger.info("file line 1: " + fileLine1);
		mdBuffer.append("\n");
		
		String fileLine2 = loggernetFileBuffer.readLine();
		mdBuffer.append(fileLine2);
		logger.info("file line 2: " + fileLine2);
		mdBuffer.append("\n");
		
		// junk line
		loggernetFileBuffer.readLine();
		
		parser.parse(mdBuffer.toString());
		this.cmap = (ChannelMap)parser.get("cmap");
	}
	

	/*! @brief Sets up the connection to an rbnb server. */
	public void initRbnb() throws SAPIException, IOException {
		if (0 < rbnbArchiveSize) {
			myBaseSource = new BaseSource(rbnbCacheSize, "append", rbnbArchiveSize);
		} else {
			myBaseSource = new BaseSource(rbnbCacheSize, "none", 0);
		}
		this.initCmap();
		myBaseSource.OpenRBNBConnection(serverName, rbnbClientName);
		logger.config("Set up connection to RBNB on " + serverName +
				" as source = " + rbnbClientName);
		logger.config(" with RBNB Cache Size = " + rbnbCacheSize + " and RBNB Archive Size = " + rbnbArchiveSize);
		myBaseSource.Register(cmap);
		myBaseSource.Flush(cmap);
	}
	
	
	/*! @brief Gracefully closes the rbnb connection. */
	protected void closeRbnb() {
		if(myBaseSource == null) {
			return;
		}

		if (rbnbArchiveSize > 0) { // then close and keep the ring buffer
			myBaseSource.Detach();
		} else { // close and scrap the cache
			myBaseSource.CloseRBNBConnection();
		}
		logger.config("Closed RBNB connection");
	}
	
	
	/* @todo move this functionality to the parser
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
					
				
					dateString = lineSplit[i].substring(1, (lineSplit[i].length()-1) );
					lineData[i] = parser.getRbnbTimestamp(dateString);
					logger.fine("Nice date:" + ISOtoRbnbTime.formatDate((long)lineData[i]*1000) );
				
				
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
				String[] varChannels = (String[])parser.get("channels");
				cmap.PutDataAsFloat64(cmap.GetIndex(varChannels[i]), dataTmp);
				logger.fine("Posted data:" + someData[i] + " into channel: " + varChannels[i]);
				myBaseSource.Flush(cmap);
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
		try {	
			
			loggernet.initFile();
			loggernet.initRbnb();
			loggernet.processFile();
			loggernet.closeRbnb();
			
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
	
	
	/*! @brief Command-line processing */
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