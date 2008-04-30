/*!
 * @file SeabirdSource.java
 * @author Lawrence J. Miller <ljmiller@sdsc.edu>
 * @author $LastChangedBy: ljmiller.ucsd $
 * @author Cyberinfrastructure Laboratory for Environmental Observing Systems (CLEOS)
 * @author San Diego Supercomputer Center (SDSC)
 * @date $LastChangedDate: 2008-04-29 14:26:50 -0700 (Tue, 29 Apr 2008) $
 * @version $LastChangedRevision: 45 $
 * @note $HeadURL: https://oss-dataturbine.googlecode.com/svn/trunk/apps/oss-apps/src/edu/ucsd/osdt/source/numeric/SeabirdSource.java $
 */

package edu.ucsd.osdt.source.numeric;
import edu.ucsd.osdt.source.BaseSource;
import edu.ucsd.osdt.util.RBNBBase;

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

class PuckSource extends RBNBBase
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
	private ChannelMap cmap = null;
	 
	// timezone offset from GMT
	private double timeOffset = 0.0;
	
	public PuckSource()
	{
		super(new BaseSource(), null);
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
						logger.info("Initialized " + portId.getName() + " to 9600n81");
						return; /* @note do not continue with serial port enumeration; duplicate devs cause trouble */
					} catch (Exception e) { // generalized because rxtx can throw many kinds of exceptions
						throw new IOException(e.toString());
					}
				} // if match
			} // if serial port
		} // while

		throw new IOException("Requested port \"" + portName + "\" not found");
		
	}
	
	/*! @brief Sets up the connection to an rbnb server. */
	public void initRbnb() throws SAPIException {
		if (rbnbArchiveSize > 0) {
			myBaseSource = new BaseSource(rbnbCacheSize, "append", rbnbArchiveSize);
		} else {
			myBaseSource = new BaseSource(rbnbCacheSize, "none", 0);
		}
		this.initCmap();
		this.myBaseSource.OpenRBNBConnection(getServer(), getRBNBClientName());
		logger.config("Set up connection to RBNB on " + getServer() +
				" as source = " + getRBNBClientName());
		logger.config(" with RBNB Cache Size = " + rbnbCacheSize + " and RBNB Archive Size = " + rbnbArchiveSize);
		this.myBaseSource.Register(cmap);
		this.myBaseSource.Flush(cmap);
	}

	private void initCmap()
	{
		this.cmap = new ChannelMap();
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
			this.myBaseSource.Detach();
		} else { // close and scrap the cache
			this.myBaseSource.CloseRBNBConnection();
		}
		logger.config("Closed RBNB connection");
	}
	
	/*! @brief Command-line processing.
	 * @note required by interface RBNBBase */
	protected Options setOptions() {
		Options opt = setBaseOptions(new Options()); // uses h, v, s, p, S

		opt.addOption("P",true, "Serial port to read *" + DEFAULT_SEABIRD_PORT);
		opt.addOption("z",true, "DataTurbine cache size *" + DEFAULT_CACHE_SIZE);
		opt.addOption("Z",true, "Dataturbine archive size *" + DEFAULT_ARCHIVE_SIZE);
		
		opt.addOption("r",true,"Data sample polling rate (ms) *" + DEFAULT_SAMPLE_PERIOD);
		double hours = timeOffset/(60.0*60.0);
		opt.addOption("o",true," time offset, floating point, hours to GMT *" + hours);

		return opt;
	} // setOptions()


	/*! @brief Command-line processing. */
	protected boolean setArgs(CommandLine cmd) throws IllegalArgumentException {
		if (! setBaseArgs(cmd)) return false;

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

	
} // class