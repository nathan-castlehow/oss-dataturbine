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

import edu.ucsd.osdt.util.ISOtoRbnbTime;
import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;
import edu.ucsd.osdt.util.RBNBBase;
import edu.ucsd.osdt.util.MDParserInterface;
//java
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.HashMap;
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
	private String DEFAULT_SDL_FILE_NAME = "Config.xml";
	private String sdlFileName = DEFAULT_SDL_FILE_NAME;
	private String DEFAULT_LN_FILE_NAME = "loggernet.dat";
	private String loggernetFileName = DEFAULT_LN_FILE_NAME;
	private static Logger logger = Logger.getLogger(SdlParser.class.getName());
	
	public SdlParser() {
		super(null, null);
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
	/*! @brief Handler for Config.xml, the sdl output file */
	public boolean parse(String mdFromInstr) {
		try {
			Document document;
			DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			document = documentBuilder.parse(sdlFileName);			
			XPath xp = XPathFactory.newInstance().newXPath();
			// has the target channels as column identifiers - <ValueColumnName>Column4</ValueColumnName>
			Node node = (Node)xp.evaluate("/Config/File[1]", document, XPathConstants.NODE);
			NodeList firstLevelList = node.getChildNodes();
			logger.finer("first level # of nodes: " + firstLevelList.getLength());
			
			
			// check out the first level
			for(int i=0; i<firstLevelList.getLength(); i++) {
				Node l1Node = firstLevelList.item(i);
				
				if(l1Node.getNodeName().compareTo("DataSeriesMapping") == 0) { // then there this node has children with metadata
					logger.info("got a data series");
					NodeList secondLevelList = l1Node.getChildNodes();
					
					for(int j=0; j<secondLevelList.getLength(); j++) { // check out the second level
						Node l2Node = secondLevelList.item(j);
						if(l2Node.getNodeName().compareTo("ValueColumnName") == 0) { // then this tells where to index in the Loggernet file for the channel name
							
							String columnLabel = l2Node.getChildNodes().item(0).getNodeValue();
							logger.info("got a value column: " + columnLabel);
							getColumnNumber(columnLabel);
							
						} // if
					} // for
				}
			} // for
		} catch(Exception e) {
			logger.severe("sumpin happened: " + e.toString());
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	protected int getColumnNumber(String columnLabelString) {
		// regex to get the number from "Column#"
		Pattern pattern = Pattern.compile("Column(\\d)+", Pattern.DOTALL);
		Matcher matcher = pattern.matcher(columnLabelString);
		int columnNumber = Integer.parseInt(matcher.group(1));
		logger.info("got column number: " + Integer.toString(columnNumber));
		return columnNumber;
	}
	
	
	public double getRbnbTimestamp(String instrTimestamp) {return -1;}
	
	
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
		
		sparse.parse(sparse.getSdlFileName());
		
	}
}