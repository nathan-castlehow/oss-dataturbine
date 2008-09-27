/*!
 * @file LoggerNetSrc.java
 * @author Peter Shin <pshin@sdsc.edu>
 * @author $LastChangedBy: peter.shin.sdsc $
 * @author Cyberinfrastructure Laboratory for Environmental Observing Systems (CLEOS)
 * @author San Diego Supercomputer Center (SDSC)
 * @date $LastChangedDate: 2008-08-20 16:34:02 -0700 (Wed, 20 Aug 2008) $
 * @version $LastChangedRevision: 125 $
 * @note $HeadURL: https://oss-dataturbine.googlecode.com/svn/trunk/apps/oss-apps/src/edu/ucsd/osdt/source/numeric/LoggerNetSource.java $
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
//java

import java.io.*;


import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import edu.ucsd.osdt.util.ISOtoRbnbTime;
import edu.ucsd.osdt.source.numeric.LoggerNetParams;

import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TimeZone;

/*! @brief A Dataturbine source accumulator that parses and puts Campbell
 *  Loggernet data onto the ring buffer. */
public class LoggerNetSrc extends RBNBBase{

	private String DEFAULT_FILE_NAME = "loggernet.dat";
	private String loggernetFileName = DEFAULT_FILE_NAME;
	private BufferedReader loggernetFileBuffer = null;
	private String DEFAULT_CFG_FILE = "LoggerNetParam.xml";
	
	private String TempFileName = null;
	private String cfgFileName = null;
	private String delimiter = null;
	
	private boolean appendMode = false;
	private boolean timeZoneOffset = false;
	
	private int SensorInfoLineNumber = 1;
	private int ChannelNameLineNumber = 2;
	private int UnitLineNumber = 3;
	private int [] ExtraInfoLineNumbers = {};
	private int DataLineNumber = 4;
	
	protected ChannelMap cmap = null;
	protected String[] channels = null;
	protected String[] units = null;
	private String lineNumFilePath;
	
	

	public LoggerNetSrc() {
		super(new BaseSource(), null);
		logger = Logger.getLogger(LoggerNetSrc.class.getName());
				
		/*! @note Add in a hook for ctrl-c's and other abrupt death */
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				logger.info("Shutdown hook activated for " + getClass().getName() + ". Exiting.");
				closeRbnb();
				Runtime.getRuntime().halt(0);
			} // run ()
		}); // addHook
	}

	/*@brief Setting up the parameters from XML file
	 * 
	 */
	public void getParamsFromCF () {
        LoggerNetParams lxp = new LoggerNetParams();
        
		try {
        	
            Properties properties = lxp.readProperties(this.cfgFileName);
            /*
             * Display all properties information
             */
            properties.list(System.out);
 
            this.cfgFileName = properties.getProperty("ConfigFilePath");
            this.loggernetFileName = properties.getProperty("LoggerNetDataFilePath");
            this.TempFileName = properties.getProperty("tempFilePath");
            this.lineNumFilePath = properties.getProperty("lineNumFilePath");
            
            String appendModeStr = properties.getProperty("AppendMode");
            if (appendModeStr.equals("Yes")) {
            	this.appendMode = true;
            }
                        
            this.delimiter = properties.getProperty("Delimiter");
            if (delimiter.equals("comma")) 
            	delimiter = ",";
            if (delimiter.equals("tab")) 
            	delimiter = "\t";
            
			try {
				Integer i =  new Integer(properties.getProperty("SensorInfoLineNumber"));
				this.SensorInfoLineNumber = i.intValue();
			} catch (Exception e) {
				logger.severe("Enter a numeric value for SensorInfoLineNumber. ");
			} 

			try {
				Integer i =  new Integer(properties.getProperty("ChannelNameLineNumber"));
				this.ChannelNameLineNumber = i.intValue();
			} catch (Exception e) {
				logger.severe("Enter a numeric value for ChannelNameLineNumber. ");
			} 
            
			try {
				Integer i =  new Integer(properties.getProperty("UnitLineNumber"));
				this.UnitLineNumber = i.intValue();
			} catch (Exception e) {
				logger.severe("Enter a numeric value for UnitLineNumber. ");
			} 

			try {
				String [] nums = properties.getProperty("ExtraInfoLineNumbers").split(",");
				this.ExtraInfoLineNumbers = new int [nums.length];
				for (int i=0; i< nums.length; i++) {
					this.ExtraInfoLineNumbers[i] = new Integer(nums[i]).intValue();
				}
			} catch (Exception e) {
				logger.severe("Enter a numeric value for ExtraInfoLineNumber. ");
			} 

			try {
				Integer i =  new Integer(properties.getProperty("DataLineNumber"));
				this.DataLineNumber = i.intValue();
			} catch (Exception e) {
				logger.severe("Enter a numeric value for DataLineNumber. ");
			} 

			
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

	

	private int getLineNum() {
		int lineNum = 1;
		BufferedReader br = null;
		try {
			br = this.initFile (this.lineNumFilePath);
		}
		catch (Exception e) {
			
		}
		
		try {
			String line = br.readLine();
			lineNum = Integer.parseInt(line);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return lineNum;
	}

	
	private void prepareMetaData () {
		
		this.channels = this.parseLine( this.loggernetFileName, this.ChannelNameLineNumber, this.delimiter);
		this.units = this.parseLine( this.loggernetFileName, this.UnitLineNumber, this.delimiter);
		
		int numExtras = this.ExtraInfoLineNumbers.length;
		
		for (int i =0; i < numExtras; i++ ) {
			String[] tempLine = this.parseLine(this.loggernetFileName, this.ExtraInfoLineNumbers[i], this.delimiter);
			if (tempLine != null) {
				for (int j=0; j < channels.length; j++) {
					if (this.units == null) {
						this.units = tempLine;
						continue;
					}
					else {
						this.units[j] = this.units[j] + "\n" + tempLine[j];
					}
				}
			}
		}	
	}
	
	
	private void processFileAppend() {
		
		this.prepareMetaData();
		// metadata info is ready

		// create a channel map with channels and units
		if (this.createChMap()) {
			// if successful, then put data 
			// get line number and start processing the data
			int dataLineNum = this.getLineNum();
			
			processDataLines(this.loggernetFileName, dataLineNum);
			
			// also save the data line number again
			System.out.println("channel map created");
			this.createLineNumFile();

			this.closeRbnb();
			
		}
		else {
			// otherwise, don't do anything (it'll be processed later)
			System.out.println("channel map not created");
		}
		
	} // processFileAppend()

		
	private boolean processDataLines(String fp, int ln) {
		
		BufferedReader lnr = null;
		
		try {
			lnr = new BufferedReader (new FileReader (fp));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("LoggerNet File cannot be read");
		}
		
		String unitLine = null;

		try {
			if (ln < 0) {
				lnr.close();
				return true;
			}
			
			else {
				int counter = 0;
				for (counter = 0 ; counter <= ln; counter++)
					unitLine = lnr.readLine();
				
				counter = ln;
				while (unitLine != null) {
					processOneDataLine(unitLine);
					unitLine = lnr.readLine();
					counter++;
				}
				counter--;
				this.DataLineNumber = counter;
				
				lnr.close();
				this.closeRbnb();
				return true;
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	
	private boolean processOneDataLine (String dl) {
		
		String [] data = dl.split(this.delimiter);
		double [] doubleData = new double [data.length];
		
		for (int i =0; i < data.length; i++) {
			data[i] = data[i].replace("\"", "");
		}
		
		for (int i=0; i < data.length; i++) {
			
			if (i==0) {
				String dateString = data[0];
				System.out.println("Campbell date string: " + dateString);

				TimeZone tz = TimeZone.getDefault();
				doubleData[0] = this.getRbnbTimestamp(dateString) + ((double) tz.getRawOffset() / 1000.0);
				System.out.println("Nice date:" + ISOtoRbnbTime.formatDate((long)doubleData[0]*1000) + " for timestamp: " + Double.toString(doubleData[0]) );
			}
			
			else if (data[i].equals("NAN")) {
				doubleData[i] = Double.NaN;
			} 
			else { // it's a double
				doubleData[i] = Double.parseDouble(data[i]);
			}
		}
		
		
		try {
			postData(doubleData);
			return true;
		} catch (SAPIException e) {
			// TODO Auto-generated catch block
			System.out.println ("Couldn't post the data");
			e.printStackTrace();
			return false;
		}
	} // while
	
	
	
	
	private boolean createChMap () {
		System.out.println("RBNB archival size is " + this.rbnbArchiveSize);
		this.rbnbArchiveSize = 100;
		if (0 < rbnbArchiveSize) {
			myBaseSource = new BaseSource(rbnbCacheSize, "append", rbnbArchiveSize);
		} else {
			myBaseSource = new BaseSource(rbnbCacheSize, "none", 0);
		}
		try {
			myBaseSource.OpenRBNBConnection(serverName, rbnbClientName);
			logger.config("Set up connection to RBNB on " + serverName +
					" as source = " + rbnbClientName);
			logger.config(" with RBNB Cache Size = " + rbnbCacheSize + " and RBNB Archive Size = " + rbnbArchiveSize);
		}
		catch (SAPIException se) {
			System.out.println("SAPI Exception at createChMap");
			return false;
		}

		this.cmap = new ChannelMap();

		// assume all data are doubles
		for(int i=0; i< this.channels.length; i++) {
			
			try {
				this.cmap.Add(this.channels[i]);
				this.cmap.PutMime(cmap.GetIndex(this.channels[i]), "application/octet-stream");
				
				if (this.units != null) {
					this.cmap.PutUserInfo(cmap.GetIndex(this.channels[i]), this.units[i]);
				}
				
			}
			catch (Exception ne) {
				ne.printStackTrace();
				System.out.println("SAPI Exception at createChMap");

				return false;
			}
		}

		try {
			myBaseSource.Register(this.cmap);
			myBaseSource.Flush(this.cmap);
		}
		catch (SAPIException se) {
			System.out.println("SAPI Exception at createChMap");
			return false;
		}

		return true;
	}
	
	
	public String[] parseLine (String fp, int ln, String del) {
		
		BufferedReader lnr = null;
		
		try {
			lnr = new BufferedReader (new FileReader (fp));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			logger.severe("LoggerNet File cannot be read");
		}
		
		String unitLine = null;
		try {
			if (ln < 0) {
				lnr.close();
				return null;
			}
			
			else {
				for (int i = 0 ; i <= ln; i++)
					unitLine = lnr.readLine();
				logger.finer("item line: " + unitLine);
				lnr.close();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (unitLine == null) return null;
		
		String [] units = unitLine.split(del);
		for (int i =0; i < units.length; i++) {
			units[i] = units[i].replace("\"", "");
			System.out.println ("item = " + units[i]);
		}
		
		return units;
	}
	
	
	
	public boolean parse(String cmdFromInstr) throws IOException, SAPIException {		

		LineNumberReader cmdReader = new LineNumberReader(new StringReader(cmdFromInstr));
		
		
		String[] channelsTmp = this.parseLine(cmdFromInstr, this.ChannelNameLineNumber, this.delimiter);
		
		
		this.cmap = new ChannelMap();

		// assume all data are doubles
		for(int i=0; i<channelsTmp.length; i++) {
			try {
				this.cmap.Add(channels[i]);
				this.cmap.PutMime(cmap.GetIndex(channels[i]), "application/octet-stream");
				//	this.cmap.PutUserInfo(cmap.GetIndex(channels[i]), "units=" + units[i]);
			}
			catch (Exception ne) {
				createTempFile();
			}
			
		}

		return true;
	}
	
	
	private void createLineNumFile() {
		BufferedWriter tempFile = null;
		try {
			tempFile= new BufferedWriter(new FileWriter(this.lineNumFilePath));
		}
		catch (IOException e) {
			logger.severe("Line number file cannot be created");
			e.printStackTrace();
			return;
		}
		try {
			tempFile.write (Integer.toString(this.DataLineNumber));
			tempFile.flush();
			tempFile.close();
		}
		catch (IOException e) {
			e.printStackTrace();
			return;
		}
		return;
	}

	
	/*! @brief instantiates file reading operations */
	public void initFile() throws FileNotFoundException {
		loggernetFileBuffer = new BufferedReader(new FileReader(loggernetFileName));
	}

	/*! @brief instantiates file reading operations */
	public BufferedReader initFile(String fp) throws FileNotFoundException {
		BufferedReader br = new BufferedReader(new FileReader(fp));
		return br;
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

		this.parse(mdBuffer.toString());
		return this.cmap;
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
	private boolean processFile() {
		
		this.prepareMetaData();
		// metadata info is ready

		// create a channel map with channels and units
		if (this.createChMap()) {
			// if successful, then put data 
			// get line number and start processing the data
			
			if (processDataLines(this.loggernetFileName, this.DataLineNumber))  {
				this.closeRbnb();
				return true;
			}
			else return false;
		}
		else {
			// otherwise, create a tempfile.
			this.createTempFile();
			System.out.println("channel map not created");
			return false;
		}
	} // processFile()


	
	
	
	private void prepareTempMetaData () {
		
		this.channels = this.parseLine( this.TempFileName, this.ChannelNameLineNumber, this.delimiter);
		this.units = this.parseLine( this.TempFileName, this.UnitLineNumber, this.delimiter);
		
		int numExtras = this.ExtraInfoLineNumbers.length;
		
		for (int i =0; i < numExtras; i++ ) {
			String[] tempLine = this.parseLine(this.TempFileName, this.ExtraInfoLineNumbers[i], this.delimiter);
			if (tempLine != null) {
				for (int j=0; j < channels.length; j++) {
					if (this.units == null) {
						this.units = tempLine;
						continue;
					}
					else {
						this.units[j] = this.units[j] + "\n" + tempLine[j];
					}
				}
			}
		}	
	}

	
	
	
	
	private void postData(double[] someData) throws SAPIException {
		// put data onto the ring buffer - skips first element, which is the rbnb timestamp
		this.cmap.PutTime(someData[0], 0.0);
		for(int i=1; i<someData.length; i++) {
			double[] dataTmp = new double[1];
			dataTmp[0] = someData[i];
			String[] varChannels = channels;
			this.cmap.PutDataAsFloat64(cmap.GetIndex(varChannels[i]), dataTmp);
			System.out.println("Posted data:" + dataTmp[0] + " into channel: " + varChannels[i] + " : " + cmap.GetIndex(varChannels[i]));
		}				
		myBaseSource.Flush(this.cmap);
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
	

	private boolean fileExists(String fileName) {

		try {
			File f= new File (fileName);
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

			String line = null;
			
			int counter = 0;
			for (counter = 0 ; counter <= this.DataLineNumber; counter++)
				line = loggernetFileBuffer.readLine();
			
			return line;
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
			String newline = System.getProperty("line.seperator");

			String data = loggernetFileBuffer.readLine();

			data = data.trim();
			data = data + newline;

			data += loggernetFileBuffer.readLine();
			data = data.trim();
			data = data + newline;

			data += loggernetFileBuffer.readLine();
			data = data.trim();
			data = data + newline;

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
	
		this.prepareMetaData();
		// metadata info is ready

		// create a channel map with channels and units
		if (this.createChMap()) {
			// if successful, then put data 
			// get line number and start processing the data
			if (this.processTempDataLines(this.TempFileName, this.DataLineNumber)) {
				// delete this portion
				return true;
			}
			
			this.closeRbnb();
			
		}
		else {
			// otherwise, create a tempfile.
			String newStr = this.acquireAllFromInstrument();
			this.appendToTempFile(newStr);
			System.out.println("temp data added");
			return false;
		}
		
		return true;
		
	}
	
	
	private boolean processTempDataLines(String fp, int ln) {
		
		BufferedReader lnr = null;
		
		try {
			lnr = new BufferedReader (new FileReader (fp));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("LoggerNet File cannot be read");
		}
		
		String unitLine = null;

		try {
			if (ln < 0) {
				lnr.close();
				return true;
			}
			
			else {
				int counter = 0;
				for (counter = 0 ; counter <= ln; counter++)
					unitLine = lnr.readLine();
				
				counter = ln;
				while (unitLine != null) {
					if (processOneDataLine(unitLine)) {
						unitLine = lnr.readLine();
						counter++;
					}
					else {
						this.removeDataLinesFromFile(this.TempFileName, counter);
						return false;
					}
				}
				
				lnr.close();
				this.closeRbnb();
				return true;
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
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
//		this.put("cmap", cmap);
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



	public void removeOneTempPortionFromFile(String file) {

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

			//Read from the original file and write to the new file
			// skipping first three lines
			while ((line = br.readLine()) != null) {
				lineCounter +=1;

				if (lineCounter >this.DataLineNumber) {

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

	
	
	public void removeDataLinesFromFile(String file, int lastSentLine) {

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

			//Read from the original file and write to the new file
			// skipping first three lines
			while ((line = br.readLine()) != null) {
				lineCounter +=1;

				if (lineCounter >lastSentLine) {
					pw.println(line);
					pw.flush();
				}
				if (lineCounter < this.DataLineNumber) {
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

	
	
	
	
	/*! @action: 1. reads the loggernet file content
	 *           2. creates a tempFile
	 *           3. writes the content to a file. 
	 */
	private void createTempFile() {
		try {
			loggernetFileBuffer = new BufferedReader(new FileReader(this.loggernetFileName));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String newline = System.getProperty("line.seperator");

		String allData = "";
		String line = "";

		try {
			while ( (line = loggernetFileBuffer.readLine()) != null)
			{
				allData += line + newline;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		try {

			PrintWriter pw = new PrintWriter(new FileWriter(this.TempFileName));
			pw.print(allData);
			pw.flush();
			pw.close();
		}
		catch (FileNotFoundException ex) {
			ex.printStackTrace();
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}


	}


	public double getRbnbTimestamp(String loggernetDate) {
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
		logger.fine("ISO8601:" + iso8601String);
		
		ISOtoRbnbTime rbnbTimeConvert = new ISOtoRbnbTime(iso8601String);
		
		System.out.println ("original date string = " + loggernetDate);
		System.out.println ("ISO date string = " + iso8601String);
		
		return rbnbTimeConvert.getValue();
	}
	



	
	
	

	/*! @brief Setup of command-line args, acquisition of metadata, and launch of main program loop. */
	/*****************************************************************************/
	public static void main(String[] args) {
		LoggerNetSrc loggernet = new LoggerNetSrc();
		
		if(! loggernet.parseArgs(args)) {
			logger.severe("Unable to process command line. Terminating.");
			System.exit(1);
		}
		
		loggernet.getParamsFromCF ();
		
		if (loggernet.appendMode) {
			// do the append mode
			if (loggernet.fileExists(loggernet.lineNumFilePath)) {
				// then read the file and parse the ch info and data
				int lnum = loggernet.getLineNum();
				
			}
			else {
				// create a line number file from the cfg file.
				loggernet.createLineNumFile();
			}
			// process the file in append mode.
			loggernet.processFileAppend();
			
		}
		
		else {

			if (loggernet.tempFileExists()) {

				if (loggernet.sendTempDataIntoDTSource()) {
					loggernet.deleteTempFile();
				}
				else {
					String acquiredData = loggernet.acquireDataFromInstrument();
					loggernet.appendToTempFile(acquiredData);
				}
			}
			else {
				if (loggernet.processFile()) return;

				else {
					loggernet.createTempFile();
				}
			}
		} 

	} // main()





	/*****************************************************************************/


	/*! @brief Command-line processing */
	protected Options setOptions() {
		Options opt = setBaseOptions(new Options()); // uses h, v, s, p, S, t

		opt.addOption("z",true, "DataTurbine cache size *" + DEFAULT_CACHE_SIZE);
		opt.addOption("Z",true, "Dataturbine archive size *" + DEFAULT_ARCHIVE_SIZE);
		opt.addOption("f",true, "Input LoggerNet file name *" + DEFAULT_FILE_NAME);
		opt.addOption("c",true, "Configuration file name *" + DEFAULT_CFG_FILE);
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
			String v = cmd.getOptionValue('f');
			this.loggernetFileName = v;
		}

		if(cmd.hasOption('c')) { // loggernet file name
			String v = cmd.getOptionValue('c');
			this.cfgFileName = v;
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