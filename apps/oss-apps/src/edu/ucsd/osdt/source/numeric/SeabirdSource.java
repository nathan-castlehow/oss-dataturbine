/*!
 * @file SeabirdSource.java
 * @author Lawrence J. Miller <ljmiller@sdsc.edu>
 * @author $LastChangedBy$
 * @author Cyberinfrastructure Laboratory for Environmental Observing Systems (CLEOS)
 * @author San Diego Supercomputer Center (SDSC)
 * @date $LastChangedDate$
 * @version $LastChangedRevision$
 * @note $HeadURL$
 */

package edu.ucsd.osdt.source.numeric;
import edu.ucsd.osdt.source.BaseSource;
import edu.ucsd.osdt.source.numeric.SeabirdParser;
import edu.ucsd.osdt.util.RBNBBase;
import edu.ucsd.osdt.util.ISOtoRbnbTime;


//rbnb
import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Source;
//rxtx
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;
//java
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.logging.Logger;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

/*! @brief A driver program that interfaces to the Seacat CTD profiler via
 * rs232, gathers and formats the data stream, and then puts this data stream
 * onto a DataTurbine ring buffer. */
class SeabirdSource extends BaseSource
{
	// serial port / seabird
	public static final String DEFAULT_SEABIRD_PORT = "COM1";
	private String seabirdPort = DEFAULT_SEABIRD_PORT;
	public static final int DEFAULT_SAMPLE_PERIOD = 5000;
	private int seabirdSamplePeriod = DEFAULT_SAMPLE_PERIOD;
	private SerialPort serialPort = null;
	private InputStream serialPortInputStream;
	private OutputStream serialPortOutputStream;
	private BufferedWriter writeToBird = null;
	private BufferedReader readFromBird = null;
	private SeabirdParser seabirdParser = null;
	// rbnb
	private static final int DEFAULT_CACHE_SIZE = 900;
	private int rbnbCacheSize = DEFAULT_CACHE_SIZE;
	private static final int DEFAULT_ARCHIVE_SIZE = 0;
	private int rbnbArchiveSize = DEFAULT_ARCHIVE_SIZE;
	private RBNBBase mRBNBBase = null;
	private ChannelMap cmap = null;
	 
	// timezone offset from GMT
	private double timeOffset = 0.0;

	public static final String DEFAULT_FILE_NAME = "none";
	private String fileName = DEFAULT_FILE_NAME;
	boolean writeFile = false;
	DataOutputStream fileOut = null;
	private static Logger logger = Logger.getLogger(SeabirdSource.class.getName());
	

	/*! @brief A constructor that simply constructs the super class and adds a
	 * shutdown hook to trap ctrl-c. */
	public SeabirdSource() {
		super();
		mRBNBBase = new RBNBBase(this);
		rbnbClientName = "Seabird";
		seabirdParser = new SeabirdParser();
		if(writeFile) {
			try {
				fileOut = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)));
				logger.config("Opened file: " + fileName);
			} catch(FileNotFoundException fe) {
				logger.severe("Could not open the output file: " + fileName + ": " + fe.toString());
			}
		}
		computeDefaultTimeOffset();
		/*! @note Add in a hook for ctrl-c's and other abrupt death */
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				logger.info("Shutdown hook activated for " + SeabirdSource.class.getName() + ". Exiting.");
				closeRbnb();
				try {
					closeSerialPort();
				} catch (IOException ioe) {
					logger.severe("Problem closing serial port.");
					logger.severe(ioe.toString());
				}
				Runtime.getRuntime().halt(0);
			} // run ()
		}); // addHook
	}


	/*! @brief Initiaizes the serial port (usu. spec'ed on the command line) using rxtx
	 * @param portName the system device name of the serial port. */
	public void initSerialPort(String portName) throws IOException {
		CommPortIdentifier portId = null;
		Enumeration portList = CommPortIdentifier.getPortIdentifiers();;

		/* @note check the available ports to validate the specified serial port */
		while (portList.hasMoreElements()) {
			portId = (CommPortIdentifier) portList.nextElement();
			if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				logger.fine("Found serial port:" + portId.getName());
				if (portId.getName().equals(portName)) { // then found the target port
					try {
						serialPort = (SerialPort) portId.open("Seabird->rxtx", 64);

						serialPortInputStream = serialPort.getInputStream();
						readFromBird = new BufferedReader(new InputStreamReader(serialPortInputStream));
						serialPortOutputStream = serialPort.getOutputStream();
						writeToBird = new BufferedWriter(new OutputStreamWriter(serialPortOutputStream));
						serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8,
								SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
						logger.config("Initialized " + portId.getName() + " to 9600n81");
						return; /* @note do not continue with serial port enumeration; duplicate devs cause trouble */
					} catch (Exception e) { // generalized because rxtx can throw many kinds of exceptions
						throw new IOException(e.toString());
					}
				} // if match
			} // if serial port
		} // while

		throw new IOException("Requested port \"" + portName + "\" not found");
		
	}


	/*! @brief Sets up the rbnb channel map according to info gathered by a SeabirdParser instance
	 * @see edu.sdsc.cleos.SeabirdParser. */
	public void initCmap() throws SAPIException {
		cmap = new ChannelMap();
		// numeric data channels
		String[] rbnbChannels = (String[])seabirdParser.get("channels");
		String[] rbnbUnits = (String[])seabirdParser.get("units");
		for(int i=0; i<rbnbChannels.length; i++) {
			cmap.Add(rbnbChannels[i]);
			cmap.PutMime(cmap.GetIndex(rbnbChannels[i]), "application/octet-stream");
		}
		for(int i=0; i<rbnbUnits.length; i++) {
			cmap.PutUserInfo(cmap.GetIndex(rbnbChannels[i]), "units=" + rbnbUnits[i]);
		}
		// metadata string channels
		String[] metadataChannels = (String[])seabirdParser.get("metadata-channels");
		for(int i=0; i<metadataChannels.length; i++) {
			cmap.Add(metadataChannels[i]);
			cmap.PutMime(cmap.GetIndex(metadataChannels[i]), "text/plain");
		}
	}


	/*! @brief Sets up the connection to an rbnb server. */
	public void initRbnb() throws SAPIException {
		if (rbnbArchiveSize > 0) {
			super(rbnbCacheSize, "append", rbnbArchiveSize);
		} else {
			super(rbnbCacheSize, "none", 0);
		}
		this.initCmap();
		OpenRBNBConnection(mRBNBBase.getServer(), mRBNBBase.getRBNBClientName());
		logger.config("Set up connection to RBNB on " + mRBNBBase.getServer() +
				" as source = " + mRBNBBase.getRBNBClientName());
		logger.config(" with RBNB Cache Size = " + rbnbCacheSize + " and RBNB Archive Size = " + rbnbArchiveSize);
		this.Register(cmap);
		this.Flush(cmap);
	}


	/*! @brief Gracefully shuts down the serial port using rxtx. */
	protected void closeSerialPort() throws IOException {
		if (serialPort != null) {
			serialPort.notifyOnDataAvailable(false);
			serialPort.removeEventListener();
			if (serialPortInputStream != null) {
				serialPortInputStream.close();
				serialPort.close();
			}
			serialPortInputStream = null;       
		}
		if (serialPortOutputStream != null) {
			serialPortOutputStream.close();
			serialPortOutputStream = null;
		}

		if(writeToBird != null) {
			writeToBird.close();
		}
		if(readFromBird != null) {
			readFromBird.close();
		}

		writeToBird = null;
		readFromBird = null;
		serialPort = null;
		logger.config("Closed serial port");
	}


	/*! @brief Gracefully closes the rbnb connection. */
	protected void closeRbnb() {
	
		if (rbnbArchiveSize > 0) { // then close and keep the ring buffer
			this.Detach();
		} else { // close and scrap the cache
			this.CloseRBNBConnection();
		}
		logger.config("Closed RBNB connection");
	}

	
	/*! @return this instance's SeabirdParser */
	protected SeabirdParser getParser() {
		return this.seabirdParser;
	}
	

	/*! @brief Accumulates serial reads to get response of the config setup "ds" command.
	 * @todo generalize this method into a command handler */
	protected String getSeabirdStatus() throws IOException {
		String cmd = "ds";
		writeToBird.write("\n\r");
		writeToBird.flush();
		logger.finest("Clear line:" + readFromBird.readLine());

		/*! @note send the actual seabird command */
		writeToBird.write(cmd, 0, cmd.length());
		writeToBird.write("\n\r");
		writeToBird.flush();
		logger.finest("Wrote command:" + cmd);
		StringBuffer readBuffer = new StringBuffer();
		String lineRead = null;

		/*! @note empirically determined last line */
		/*! @todo do this properly with regexes */
		String lastLineString = "output salinity = yes, output sound velocity = no";
		boolean lastLine = false;

		/*! @note read and acccumulte all lines on the stream */
		while(! lastLine && ( (lineRead = readFromBird.readLine()) != null )) {
			readBuffer.append(lineRead);
			readBuffer.append("\n");
			lastLine = lineRead.matches(lastLineString) || lineRead.matches("S>time out");
		}
		/*! @bug file writes do not work */
		if(writeFile) {
			fileOut.write(readBuffer.toString().getBytes());
		}
		return readBuffer.toString();
	}


	/*! @brief Accumulates serial reads to get response of the calibration setup "dcal" command.
	 * @todo generalize this method into a command handler */
	protected String getSeabirdCal() throws IOException {
		String cmd = "dcal";
		writeToBird.write("\n\r");
		writeToBird.flush();
		logger.finest("Clear line:" + readFromBird.readLine());

		/*! Send the actual command */
		writeToBird.write(cmd, 0, cmd.length());
		writeToBird.write("\n\r");
		writeToBird.flush();
		logger.finest("Wrote command:" + cmd);

		StringBuffer readBuffer = new StringBuffer();
		String lineRead = null;

		/*! @note examine last line as a token comms have ended */
		/*! @todo do this properly with regexes */
		// SIO Seacat
		// String lastLineString = "    EXTFREQSF = 1.000120e+00";
		// MCR Seacat
		String lastLineString = "    EXTFREQSF = 9.999942e-01";
		
		boolean lastLine = false;

		/*! @note read and acccumulte all lines on the stream */
		while( ! lastLine && ( (lineRead = readFromBird.readLine()) != null) ) {
			readBuffer.append(lineRead);
			readBuffer.append('\n');

			lastLine = 	(lineRead.compareTo(lastLineString) == 0) ||
						(lineRead.compareTo("S>time out") == 0);
		}
		return readBuffer.toString();
	}


	/*! @runs seabird individual sampling command "ds" in a loop whose rate is
	 * controlled by sleeping at a specified period */
	public void seabird2RbnbPolling() throws IOException, SAPIException {	
		String cmd = "ts";
		String echoCmd = "echo=no";
		writeToBird.write("\n\r");
		writeToBird.flush();
		logger.finest("Clear line:" + readFromBird.readLine());
		String lineRead = null;
		
		/*! @note turn off command echo from seabird
		writeToBird.write(echoCmd, 0, echoCmd.length());
		writeToBird.write("\n\r");
		writeToBird.flush();
		*/
		
		/*! @note Send the actual command, read and acccumulte all lines on the stream */
		do {
			mySleep(seabirdSamplePeriod);
			writeToBird.write(cmd, 0, cmd.length());
			writeToBird.write("\n\r");
			writeToBird.flush();
			// clean up command prompts command echos from seabird
			if( (lineRead != null) && (lineRead.compareTo("S>ts") != 0) && (lineRead.compareTo("S>") != 0)) {
				try {
					if(lineRead.startsWith("ts")) { // then the 'ts' command echo is prepended... sanitize
						String[] readTmp = lineRead.split("ts ");
						lineRead = readTmp[1];
					}
					logger.finer("Data line:" + lineRead);
					double[] dataArray = seabirdParser.getData(lineRead);
					postData(dataArray);
				} catch(ParseException pe) {
					logger.fine("Parsing seabird sample string. " + pe.toString());
				} catch(SeabirdException se) {
					logger.fine("Seabird hiccup. Skipping data point: " + lineRead);
					logger.fine(se.toString());
				} catch(ArrayIndexOutOfBoundsException ae) {
					logger.fine("Seabird hiccup. Skipping data point: " + lineRead);
					logger.fine(ae.toString());
				}
			}
		} while((lineRead = readFromBird.readLine()) != null);
	}

	
	/*! @brief Reads data lines reported by a seabird that has been configured to sample autonomously with "StartNow". */
	protected void seabird2RbnbAuto() {
	}

	
	/*! @brief Puts data on the dataturbine ring buffer.
	 * @todo factor out the metadata posting, which was left here to share the timestamp with the numeric data stream
	 * @param data an array of data points; the last element is an rbnb timestamp (i.e. seconds since the epoch) */
	protected void postData(double[] data) throws SAPIException {
		double adjustedTime = (data[data.length-1] + timeOffset);
		logger.finer("Date adjusted for timezone:" + adjustedTime);
		logger.finer( "Nice adjusted time:" + ISOtoRbnbTime.formatDate((long)adjustedTime*1000) );
		cmap.PutTime(adjustedTime, 0.0);
		
		/*! @bug sometimes the seabird returns a munged string with the wrong number of tokens */
		if(seabirdParser.getChannels().length < data.length-1) {
			logger.fine("data[] is of unexpected length:" + seabirdParser.getChannels().length);
			return;
		}
		
		try {
			/*! @note the data array is delivered with the timestamp in the last element */
			for(int i=0; i<data.length-1; i++) {
				double[] dataTmp = new double[1];
				dataTmp[0] = data[i];
				cmap.PutDataAsFloat64(cmap.GetIndex(seabirdParser.getChannels()[i]), dataTmp);
				logger.finer("Posted data:" + data[i] + " into channel:" + seabirdParser.getChannels()[i]);
			}
			
			String[] metadataChannels = (String[])seabirdParser.get("metadata-channels");
			String model = (String)seabirdParser.get("model");
			String serial = (String)seabirdParser.get("serial");
			cmap.PutDataAsString(cmap.GetIndex(metadataChannels[0]), model);
			cmap.PutDataAsString(cmap.GetIndex(metadataChannels[1]), serial);
			logger.finer("Posted metadata:" + model + ":" + serial + " to channels:" + metadataChannels[0] + ":" + metadataChannels[1]);
			
			logger.finer("Posted data and metadata");
			
		} catch (SAPIException sae) {
			sae.printStackTrace();
			throw sae;
		}
		this.Flush(cmap);
	}
	

	/*! @brief Setup of command-line args, acquisition of metadata, and launch of main program loop. */
	/*****************************************************************************/
	public static void main(String[] args) {
		SeabirdSource seabird = new SeabirdSource();
		if(! seabird.mRBNBBase.parseArgs(args)) {
			logger.severe("Unable to process command line. Terminating.");
			System.exit(1);
		}
		// setup
		try {
			seabird.initSerialPort(seabird.seabirdPort);
			// setup metadata
			logger.info("Reading metadata from seabird...");
			seabird.getParser().put("ds", seabird.getSeabirdStatus());
			logger.finest("\"ds\" from parser:\n" + (String)seabird.getParser().get("ds"));
			seabird.getParser().put("dcal", seabird.getSeabirdCal());
			logger.finest("\"dcal\" from parser:\n" + (String)seabird.getParser().get("dcal"));
			seabird.getParser().parseMetaData();
			
			seabird.initRbnb();
		} catch(IOException ioe) {
			logger.severe("Unable to communicate with serial port. Terminating: " + ioe.toString());
			System.exit(2);
		} catch(SAPIException sae) {
			logger.severe("Unable to communicate with DataTurbine server. Terminating: " + sae.toString());
			System.exit(3);
		}
		
		try {
			// action
			logger.info("Polling seabird at a period of:" + seabird.seabirdSamplePeriod + "ms...");
			seabird.seabird2RbnbPolling();
		} catch(IOException ioe) {
			logger.severe("Unable to read serial port: " + ioe.toString());
		} catch(SAPIException sae) {
			logger.severe("Unable to post data to RBNB: " + sae.toString());
		}
	} // main()
	/*****************************************************************************/

	
	/*! @note both the offset to GMT and Daylight Savings time
		@note borrowed from @see edu.sdsc.cleos.DaqToRbnb */
	protected void computeDefaultTimeOffset() {
		Calendar calendar = new GregorianCalendar();
		long tz = calendar.get(Calendar.ZONE_OFFSET);
		long dt = calendar.get(Calendar.DST_OFFSET);
		logger.finer("Time Zone offset: "
			   + (-((double)(tz/1000))/(60.0*60.0))); // in hours
		logger.finer("Daylight Savings Time offset (h): "
			   + (-((double)(dt/1000))/(60.0*60.0))); // in hours
		// Time zone offset
		timeOffset = - (double)((tz + dt)/1000); // in seconds 
		logger.finer("timeOffset: " + timeOffset);
	}
	
	
	/*! @brief Command-line processing.
	 * @note required by interface RBNBBase */
	protected Options setOptions() {
		Options opt = mRBNBBase.setBaseOptions(new Options()); // uses h, v, s, p, S

		opt.addOption("P",true, "Serial port to read *" + DEFAULT_SEABIRD_PORT);
		opt.addOption("z",true, "DataTurbine cache size *" + DEFAULT_CACHE_SIZE);
		opt.addOption("Z",true, "Dataturbine archive size *" + DEFAULT_ARCHIVE_SIZE);
		
		opt.addOption("f",true,"Output file name *" + DEFAULT_FILE_NAME);
		opt.addOption("r",true,"Data sample polling rate (ms) *" + DEFAULT_SAMPLE_PERIOD);
		double hours = timeOffset/(60.0*60.0);
		opt.addOption("o",true," time offset, floating point, hours to GMT *" + hours);

		return opt;
	} // setOptions()


	/*! @brief Command-line processing.
	 * @note required by interface RBNBBase */
	protected boolean setArgs(CommandLine cmd) throws IllegalArgumentException {
		if (!mRBNBBase.setBaseArgs(cmd)) return false;

		if(cmd.hasOption('P')) { // seabird serial port
			String v = cmd.getOptionValue("P");
			seabirdPort = v;
		}
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
		if(cmd.hasOption('f')) { // output file name
			String v = cmd.getOptionValue("f");
			writeFile = true;
			fileName = v;
		}
		if(cmd.hasOption('r')) { // sampling period
			String a = cmd.getOptionValue("r");
			if(a!=null) {
				try {
					Integer i =  new Integer(a);
					int value = i.intValue();
					seabirdSamplePeriod = value;
				} catch(Exception e) {
					logger.severe("Enter a numeric value for -r option. " + a + " is not valid!");
					return false;   
				}
			}
		}
		if (cmd.hasOption('o')) {
			String a=cmd.getOptionValue('o');
			if (a!=null)
				try {
					double value =  Double.parseDouble(a); // in hours
					timeOffset = (value*60.0*60.0); // in seconds
					logger.config("Timezone offset to: " + timeOffset/(60.0*60.0) + "h from GMT");
				}
			catch (NumberFormatException nf) {
				System.out.println("Error: enter a numeric value for -o option. " + a + " is not valid!");
				return false;   
			}
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
				"$LastChangedDate$\n" +
				"$LastChangedRevision$\n" +
				"$LastChangedBy$\n" +
				"$HeadURL$"
		);
	}


	/*! @brief Convenience to hide exception "handling" (not sure what to do if thread sleep gets interrupted). */
	protected void mySleep(int millis) {
		try {
			/*! @bug sleeps for 2x longer than you tell it... why? */
			Thread.sleep(millis/2);
		} catch(InterruptedException ie) {
			logger.severe("Thread sleep interrupted: " + ie.toString());
		}
	}


	/*! @brief Eventual replacement for the "seabird2RbnbPolling" method.  */
	protected class serialPortTimeoutTask extends TimerTask {

		protected long sleepTime;

		/*!   */
		public serialPortTimeoutTask(long millis) {
			super();
			sleepTime = millis;
		}


		/*!   */
		public void run() {
			try {
				Thread.sleep(sleepTime);
			} catch(InterruptedException ie) {
				logger.severe("Thread sleep interrupted: " + ie.toString());
			}
			// fire an action event
		}
	} // inner class


} // class
