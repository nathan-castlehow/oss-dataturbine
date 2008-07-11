/*!
 * @file SdlParser.java
 * @author Lawrence J. Miller <ljmiller@sdsc.edu>
 * @author $LastChangedBy$
 * @author Cyberinfrastructure Laboratory for Environmental Observing Systems (CLEOS)
 * @author San Diego Supercomputer Center (SDSC)
 * @date $LastChangedDate$
 * @version $LastChangedRevision$
 * @note $HeadURL$
 */
package edu.ucsd.osdt.source.numeric;

import edu.ucsd.osdt.source.numeric.LoggerNetParser;
import edu.ucsd.osdt.util.ISOtoRbnbTime;
import edu.ucsd.osdt.util.RBNBBase;
import edu.ucsd.osdt.util.MDParserInterface;
import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;

//java
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Enumeration;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

//xml
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

class SdlParser extends RBNBBase implements MDParserInterface {
	private String DEFAULT_SDL_FILE_NAME = "C:\\Program Files\\CUAHSI HIS\\ODM SDL\\Config.xml";
	private String sdlFileName = DEFAULT_SDL_FILE_NAME;
	private String DEFAULT_LN_FILE_NAME = "loggernet.dat";
	private String loggernetFileName = DEFAULT_LN_FILE_NAME;
	private BufferedReader loggernetFileBuffer = null;
	private LoggerNetParser loggernetParser = null;
	private static Logger logger = Logger.getLogger(SdlParser.class.getName());
	private ArrayList<Integer> sdlMappedColumns = null;
	
	
	public SdlParser() {
		super(null, null);
		sdlMappedColumns = new ArrayList();
		loggernetParser = new LoggerNetParser();
	}
	
	
	// accessors and mutators
	public String getSdlFileName() {
		return this.sdlFileName;
	}
	
	
	// abstract methods from interface
	public ChannelMap getCmap() {return null;}
	public String[] getChannels() {return null;}
	public String[] getUnits() {return null;}
	
	
	/////////////////////
	/*! @brief Sets up the rbnb channel map using a LoggerNetParser */
	public ChannelMap generateCmap() throws IOException, SAPIException {
		ChannelMap cmapRetval = new ChannelMap();
		String[] loggernetChannels = null;
		String[] loggernetUnits = null;
		StringBuffer mdBuffer = new StringBuffer();
		
		// junk line
		loggernetFileBuffer.readLine();
		String fileLine1 = loggernetFileBuffer.readLine();
		mdBuffer.append(fileLine1);
		logger.finer("file line 1: " + fileLine1);
		mdBuffer.append("\n");
		
		String fileLine2 = loggernetFileBuffer.readLine();
		mdBuffer.append(fileLine2);
		logger.finer("file line 2: " + fileLine2);
		mdBuffer.append("\n");
		// junk line
		loggernetFileBuffer.readLine();
		
		loggernetParser.parse(mdBuffer.toString());
		loggernetChannels = (String[])loggernetParser.get("channels");
		loggernetUnits = (String[])loggernetParser.get("units");
		
		// add only those channels that are indexed by the sdl file; these are in sdlMappedColumns
		Object[] sdlColumns = sdlMappedColumns.toArray();
		for(int i=0; i<sdlColumns.length; i++) {
			int sdlColumn = ( (Integer)(sdlColumns[i]) ).intValue();
			logger.info("sdl column: " + sdlColumn + " maps to loggernet channel: " + loggernetChannels[sdlColumn+1]); // zero indexed, so add 1
			logger.info("make cmap channel \"" + loggernetChannels[sdlColumn+1] + "\" with units: \"" + loggernetUnits[sdlColumn+1] + "\"");
		}
		
		return cmapRetval;
	}
	////////////////////////
	
	/*! @brief Handler for Config.xml, the sdl output file - does nothing with the parameter */
	public boolean parse(String mdFromSdl) {
		try {
			Document document;
			DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			document = documentBuilder.parse(sdlFileName);			
			XPath xp = XPathFactory.newInstance().newXPath();
			// has the target channels as column identifiers - <ValueColumnName>Column4</ValueColumnName>
			Node node = (Node)xp.evaluate("/Config/File[1]", document, XPathConstants.NODE);
			NodeList firstLevelList = node.getChildNodes();
			logger.finer("first level # of nodes: " + firstLevelList.getLength());
			
			for(int i=0; i<firstLevelList.getLength(); i++) { // check out the first level
				Node l1Node = firstLevelList.item(i);
				
				if(l1Node.getNodeName().compareTo("DataSeriesMapping") == 0) { // then there this node has children with metadata
					logger.finer("got a data series");
					NodeList secondLevelList = l1Node.getChildNodes();
					
					for(int j=0; j<secondLevelList.getLength(); j++) { // check out the second level
						Node l2Node = secondLevelList.item(j);
						if(l2Node.getNodeName().compareTo("ValueColumnName") == 0) { // then this tells where to index in the Loggernet file for the channel name
							
							String columnLabel = l2Node.getChildNodes().item(0).getNodeValue();
							logger.finer("got a value column: " + columnLabel);
							sdlMappedColumns.add(new Integer(getColumnNumber(columnLabel)));
							
						} // if
					} // for
				} // if
			} // for
			
			logger.finer("# of elements in arraylist: " + sdlMappedColumns.size());
			
		} catch(Exception e) {
			logger.severe("sumpin happened: " + e.toString());
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	
	protected int getColumnNumber(String columnLabelString) {
		// regex to get the number from "Column#"
		Pattern pattern = Pattern.compile("Column(\\d)?", Pattern.DOTALL);
		Matcher matcher = pattern.matcher(columnLabelString);
		matcher.find();
		int columnNumber = Integer.parseInt(matcher.group(1));
		logger.finer("got column number: " + Integer.toString(columnNumber));
		return columnNumber;
	}
	
	
	public double getRbnbTimestamp(String instrTimestamp) {return -1;}
	
	
	public void initFile() throws FileNotFoundException {
		loggernetFileBuffer = new BufferedReader(new FileReader(loggernetFileName));
	}
	
	
	protected Options setOptions() {
		Options opt = setBaseOptions(new Options()); // uses h, v, s, p, S

		opt.addOption("s", true, "SDL configuration file name *" + DEFAULT_SDL_FILE_NAME);
		opt.addOption("f", true, "Input LoggerNet file name *" + DEFAULT_LN_FILE_NAME);

		return opt;
	} // setOptions()


	/*! @brief Command-line processing.
	 * @note required by interface RBNBBase */
	protected boolean setArgs(CommandLine cmd) throws IllegalArgumentException {
		if (!setBaseArgs(cmd)) return false;

		if(cmd.hasOption('s')) { // sdl file name
			String v = cmd.getOptionValue("s");
			sdlFileName = v;
		}
		if(cmd.hasOption('f')) { // loggernet file name
			String v = cmd.getOptionValue("f");
			loggernetFileName = v;
		}
		return true;
	} // setArgs()
	
	
	/**/
	public static void main(String[] args) {
		SdlParser sparse = new SdlParser();
		if(! sparse.parseArgs(args)) {
			logger.severe("Unable to process command line. Terminating.");
			System.exit(1);
		}
		
		try {
			sparse.initFile();
			sparse.parse(null);
			sparse.generateCmap();
		} catch(FileNotFoundException fne) {
			logger.severe("couldn't find the specified loggernet file");
		} catch(IOException ioe) {
			logger.severe(ioe.toString());
		} catch(SAPIException sae) {
			logger.severe(sae.toString());
		}
		
	}
} // class