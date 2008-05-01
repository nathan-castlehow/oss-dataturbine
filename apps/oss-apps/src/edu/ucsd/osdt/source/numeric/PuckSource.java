/*!
 * @file PuckSource.java
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
import edu.ucsd.osdt.util.RBNBBase;

// puck
import org.mbari.puck.Puck;
import org.mbari.puck.Puck_1_3;

//rbnb
import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Source;
//rxtx
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;
//java
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Enumeration;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

class PuckSource extends RBNBBase
{
	// serial port / puck
	public static final String DEFAULT_PUCK_PORT = "COM1";
	private String puckPort = DEFAULT_PUCK_PORT;
	private SerialPort serialPort = null;
	private InputStream serialPortInputStream;
	private OutputStream serialPortOutputStream;
	private BufferedWriter writeToPuck = null;
	private BufferedReader readFromPuck = null;
	private Puck_1_3 puck = null;
	
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
		logger = Logger.getLogger(this.getClass().getName());
		rbnbClientName = "PUCK";	
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
						serialPort = (SerialPort) portId.open(this.getClass().getName(), 1000);

						serialPortInputStream = serialPort.getInputStream();
						readFromPuck = new BufferedReader(new InputStreamReader(serialPortInputStream));
						serialPortOutputStream = serialPort.getOutputStream();
						writeToPuck = new BufferedWriter(new OutputStreamWriter(serialPortOutputStream));
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
			myBaseSource.SetRingBuffer(rbnbCacheSize, "append", rbnbArchiveSize);
		} else {
			myBaseSource.SetRingBuffer(rbnbCacheSize, "none", 0);
		}
		this.initCmap();
		this.myBaseSource.OpenRBNBConnection(getServer(), getRBNBClientName());
		logger.config("Set up connection to RBNB on " + getServer() +
				" as source = " + getRBNBClientName());
		logger.config(" with RBNB Cache Size = " + rbnbCacheSize + " and RBNB Archive Size = " + rbnbArchiveSize);
		this.myBaseSource.Register(cmap);
		this.myBaseSource.Flush(cmap);
	}

	/*! a puck datasheet generally looks like:
		UUID        : 3e6b8f72-c5f8-11dc-95ff-0800200c9a66
		ver         : 2
		size        : 96
		man id      : 1
		man model   : 0
		man version : 0
		man serial  : 62232003
		name        : MBARI Reference Design PUCK
	*/
	private void initCmap() throws SAPIException
	{
		this.cmap = new ChannelMap();
		cmap.Add("UUID");
		cmap.Add("ver");
		cmap.Add("size");
		cmap.Add("man id");
		cmap.Add("man model");
		cmap.Add("man version");
		cmap.Add("man serial");
		cmap.Add("name");
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

		if(writeToPuck != null) {
			writeToPuck.close();
		}
		if(readFromPuck != null) {
			readFromPuck.close();
		}

		writeToPuck = null;
		readFromPuck = null;
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
	
	/*! @brief the main execution */
	public static void main(String[] args)
	{
		try
		{
			PuckSource app = new PuckSource();
			app.parseArgs(args);
			app.initSerialPort(app.puckPort);
			app.initRbnb();
			
			// put some strings into rbnb
			app.puck = new Puck_1_3(app.serialPort);
			app.puck.setPuckMode(3);
			Puck.Datasheet datasheet = app.puck.readDatasheet();
			logger.info("\n"+datasheet.toString());
			app.puck.setInstrumentMode();
			app.puck.close();
			//System.exit(0);
		} catch(Exception e) {
			logger.severe("croak! " + e.toString());
		}
	}
	
	/*! @brief Command-line processing. */
	protected Options setOptions() {
		Options opt = setBaseOptions(new Options()); // uses h, v, s, p, S

		opt.addOption("P",true, "Serial port to read *" + DEFAULT_PUCK_PORT);
		opt.addOption("z",true, "DataTurbine cache size *" + DEFAULT_CACHE_SIZE);
		opt.addOption("Z",true, "Dataturbine archive size *" + DEFAULT_ARCHIVE_SIZE);

		return opt;
	} // setOptions()


	/*! @brief Command-line processing. */
	protected boolean setArgs(CommandLine cmd) throws IllegalArgumentException {
		if (! setBaseArgs(cmd)) return false;

		if(cmd.hasOption('P')) { // puck serial port
			String v = cmd.getOptionValue("P");
			puckPort = v;
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