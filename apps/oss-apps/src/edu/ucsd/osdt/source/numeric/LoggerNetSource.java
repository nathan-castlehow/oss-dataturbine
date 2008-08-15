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

import java.io.*;


import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import edu.ucsd.osdt.util.ISOtoRbnbTime;
import java.util.TimeZone;

/*! @brief A Dataturbine source accumulator that parses and puts Campbell
 *  Loggernet data onto the ring buffer. */
class LoggerNetSource extends RBNBBase {
	private String DEFAULT_FILE_NAME = "loggernet.dat";
	private String loggernetFileName = DEFAULT_FILE_NAME;
	private BufferedReader loggernetFileBuffer = null;
	// rbnb

	private String TempFileName = null;

	private LoggerNetParser parser = null;
	
	
	public LoggerNetSource() {
		super(new BaseSource(), null);
		parser = new LoggerNetParser();
		logger = Logger.getLogger(LoggerNetSource.class.getName());
		/*! @note Add in a hook for ctrl-c's and other abrupt death */
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				logger.info("Shutdown hook activated for " + getClass().getName() + ". Exiting.");
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
	public ChannelMap generateCmap() throws IOException, SAPIException {
		StringBuffer mdBuffer = new StringBuffer();
		
		// junk line
		loggernetFileBuffer.readLine();
		String fileLine1 = loggernetFileBuffer.readLine();
		mdBuffer.append(fileLine1);
		logger.info("file line 1: " + fileLine1);
		mdBuffer.append("\n");
		
		//String fileLine2 = loggernetFileBuffer.readLine();
		//mdBuffer.append(fileLine2);
		//logger.info("file line 2: " + fileLine2);
		//mdBuffer.append("\n");
		// junk line
		//loggernetFileBuffer.readLine();
		
		parser.parse(mdBuffer.toString());
		return (ChannelMap)parser.get("cmap");
	}
	

	/*! @brief Sets up the connection to an rbnb server. */
	public void initRbnb() throws SAPIException, IOException {
		if (0 < rbnbArchiveSize) {
			myBaseSource = new BaseSource(rbnbCacheSize, "append", rbnbArchiveSize);
		} else {
			myBaseSource = new BaseSource(rbnbCacheSize, "none", 0);
		}
		this.cmap = generateCmap();
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
		
		logger.finer("processFile() lanuched");
		
		while((lineRead = loggernetFileBuffer.readLine()) != null) {
			//logger.finer("HELLOOOOOOO   Lineread:" + lineRead);
			lineSplit = lineRead.split(",");
			// gotta convert from strings to doubles the old-fashioned way - the first element is a timestamp
			lineData = new double[lineSplit.length];
			for(int i=0; i<lineSplit.length; i++) {
				logger.finer("Data token:" + lineSplit[i]);
				if(i==0) { // timestamp - handle specially
					dateString = lineSplit[i].substring(1, (lineSplit[i].length()-1) );
					logger.fine("Campbell date string: " + dateString);
					
					TimeZone tz = TimeZone.getDefault();
					lineData[i] = parser.getRbnbTimestamp(dateString) + tz.getRawOffset();
					logger.fine("Nice date:" + ISOtoRbnbTime.formatDate((long)lineData[i]*1000) + " for timestamp: " + Double.toString(lineData[i]) );
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
		// put data onto the ring buffer - skips first element, which is the rbnb timestamp
		cmap.PutTime(someData[0], 0.0);
		for(int i=1; i<someData.length; i++) {
				double[] dataTmp = new double[1];
				dataTmp[0] = someData[i];
				String[] varChannels = (String[])parser.get("channels");
				cmap.PutDataAsFloat64(cmap.GetIndex(varChannels[i]), dataTmp);
				logger.finer("Posted data:" + dataTmp[0] + " into channel: " + varChannels[i] + " : " + cmap.GetIndex(varChannels[i]));
			}				
			myBaseSource.Flush(cmap);
	}
	

	private boolean tempFileExists() {
		
		try {
			File f= new File (this.TempFileName);
			if (f.exists()) {
				return true;
			}
			else return false;
		}
		catch (NullPointerException e) {
			logger.severe("No temporary file name suggested");
			return false;
		}
	}
	
	
	private String acquireDataFromInstrument() {
		try {
			loggernetFileBuffer = new BufferedReader(new FileReader(this.loggernetFileName));
		}
		catch (FileNotFoundException e) {
			logger.severe("Loggernet file doesn't exist");
			return null;
		}
		try {
			loggernetFileBuffer.readLine();
			loggernetFileBuffer.readLine();
			String data = loggernetFileBuffer.readLine();
			data = data.trim();
			data = data + "\n";
			return data;
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	

	
	private String acquireAllFromInstrument() {
		try {
			loggernetFileBuffer = new BufferedReader(new FileReader(this.loggernetFileName));
		}
		catch (FileNotFoundException e) {
			logger.severe("Loggernet file doesn't exist");
			return null;
		}
		try {
			String data = loggernetFileBuffer.readLine();
			data = data.trim();
			data = data + "\n";
			data += loggernetFileBuffer.readLine();
			data = data.trim();
			data = data + "\n";
			data += loggernetFileBuffer.readLine();
			data = data.trim();
			data = data + "\n";
			return data;
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	
	
	
	
	private void appendToTempFile(String someLine) {
		BufferedWriter tempFile = null;
		try {
			boolean appendTrue = true;
			tempFile= new BufferedWriter(new FileWriter(this.TempFileName, appendTrue));
		}
		catch (FileNotFoundException e) {
			logger.severe("Loggernet file doesn't exist");
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		try {
			tempFile.write (someLine);
			tempFile.flush();
		}
		catch (IOException e) {
			e.printStackTrace();
			return;
		}
		return;
	}
	
	
	
	private boolean sendTempDataIntoDTSource() {
		String lineRead = null;
		String[] lineSplit = null;
		String dateString = null;
		double[] lineData = null;
		
		logger.finer("processFile() lanuched");
		
		BufferedReader tempF;
		try {
			tempF = new BufferedReader(new FileReader(this.TempFileName));
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
			
		try {
			// first two lines are metadata
			tempF.readLine();
			tempF.readLine();
			
			while((lineRead = tempF.readLine()) != null) {
				//logger.finer("HELLOOOOOOO   Lineread:" + lineRead);
				lineSplit = lineRead.split(",");
				// gotta convert from strings to doubles the old-fashioned way - the first element is a timestamp
				lineData = new double[lineSplit.length];
				for(int i=0; i<lineSplit.length; i++) {
					logger.finer("Data token:" + lineSplit[i]);
					if(i==0) { // timestamp - handle specially
						dateString = lineSplit[i].substring(1, (lineSplit[i].length()-1) );
						logger.fine("Campbell date string: " + dateString);
						
						TimeZone tz = TimeZone.getDefault();
						lineData[i] = parser.getRbnbTimestamp(dateString) + tz.getRawOffset();
						logger.fine("Nice date:" + ISOtoRbnbTime.formatDate((long)lineData[i]*1000) + " for timestamp: " + Double.toString(lineData[i]) );
					} else if(lineSplit[i].equals("\"NAN\"")) {
						lineData[i] = Double.NaN;
					} else { // it's a double
						lineData[i] = Double.parseDouble(lineSplit[i]);
					}
				}
				postData(lineData);
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // while
		return false;
	}
	
	
	public ChannelMap parseChannelMap(String channelsString)  {		
		
		
		logger.finer("channelsString: " + channelsString);
		String[] channelsTmp = channelsString.split(",");
		String[] channels = new String[channelsTmp.length];
		
		//String unitsString = cmdReader.readLine();
		//logger.finer("unitsString: " + unitsString);
		//String[] unitsTmp = unitsString.split(",");
		//units = new String[unitsTmp.length];
		
		//if( (channelsTmp.length != unitsTmp.length) || (channelsTmp.length == 0) ) {
		//	return false;
		//} else { // input makes sense
			// clean off the double quotes from each channel names and unit labels (first and last character of each string)
			Pattern pattern = Pattern.compile("\"(.*)\"", Pattern.DOTALL);
			Matcher matcher = null;
			for(int i=0; i<channelsTmp.length; i++) {
				// channels
				matcher = pattern.matcher(channelsTmp[i]);
				if(matcher.find()) {
					channels[i] = matcher.group(1).trim();
					logger.finer(channels[i]);
				}
				// units
		//		matcher = pattern.matcher(unitsTmp[i]);
		//		if(matcher.find()) {
		//			units[i] = matcher.group(1).trim();
		//			logger.finer(units[i]);
		//		}
			}
			
			this.cmap = new ChannelMap();
			
			// assume all data are doubles
			for(int i=0; i<channelsTmp.length; i++) {
				try {
					this.cmap.Add(channels[i]);
				} catch (SAPIException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				this.cmap.PutMime(cmap.GetIndex(channels[i]), "application/octet-stream");
			//	this.cmap.PutUserInfo(cmap.GetIndex(channels[i]), "units=" + units[i]);
			}
	//		this.put("channels", channels);
			//this.put("units", units);
//			this.put("cmap", cmap);
			return this.cmap;
	
	}
	
	
	
	private boolean deleteTempFile() {
		try {
			File f= new File (this.TempFileName);
			if (f.delete()) {
				return true;
			}
			else return false;
		}
		catch (SecurityException e) {
			logger.severe("Temporary file not deleted");
			return false;
		}
	}
	
	

	public void removeFirstTreeLinesFromFile(String file) {

		try {

			File inFile = new File(file);

			if (!inFile.isFile()) {
				System.out.println("Parameter is not an existing file");
				return;
			}

			//Construct the new file that will later be renamed to the original filename. 
			File tempFile = new File(inFile.getAbsolutePath() + ".tmp");

			BufferedReader br = new BufferedReader(new FileReader(file));
			PrintWriter pw = new PrintWriter(new FileWriter(tempFile));

			String line = null;
			
			int lineCounter = 0;
			//Read from the original file and write to the new 
			//unless content matches data to be removed.
			while ((line = br.readLine()) != null) {
				lineCounter +=1;
				
				if (lineCounter >3) {

					pw.println(line);
					pw.flush();
				}
			}
			pw.close();
			br.close();

			//Delete the original file
			if (!inFile.delete()) {
				System.out.println("Could not delete file");
				return;
			} 

			//Rename the new file to the filename the original file had.
			if (!tempFile.renameTo(inFile))
				System.out.println("Could not rename file");

		}
		catch (FileNotFoundException ex) {
			ex.printStackTrace();
		}
		catch (IOException ex) {
			ex.printStackTrace();
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
			
			if (loggernet.tempFileExists()) {
				
				if (loggernet.sendTempDataIntoDTSource()) {
					loggernet.deleteTempFile();
				}
				else {
					String acquiredData = loggernet.acquireDataFromInstrument();
					loggernet.appendToTempFile (acquiredData);
				}
			}
			else {
				loggernet.initFile();
				loggernet.initRbnb();
				loggernet.processFile();
				loggernet.closeRbnb();
			}
			
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
		Options opt = setBaseOptions(new Options()); // uses h, v, s, p, S, t

		opt.addOption("z",true, "DataTurbine cache size *" + DEFAULT_CACHE_SIZE);
		opt.addOption("Z",true, "Dataturbine archive size *" + DEFAULT_ARCHIVE_SIZE);
		opt.addOption("f",true, "Input LoggerNet file name *" + DEFAULT_FILE_NAME);
		opt.addOption("t",true, "Temporary File Name ");
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
		
		if (cmd.hasOption('t')) {
			String a = cmd.getOptionValue('t');
			if (a!=null) {
				this.TempFileName = a;
			}
			else	return false;
		}
		return true;
		
		
	} // setArgs()
	
} // class