/*!
 * @file SdlParser.java
 * @author Lawrence J. Miller <ljmiller@sdsc.edu>
 * @author $LastChangedBy: ljmiller.ucsd $
 * @author Cyberinfrastructure Laboratory for Environmental Observing Systems (CLEOS)
 * @author San Diego Supercomputer Center (SDSC)
 * @date $LastChangedDate: 2008-04-30 15:57:47 -0700 (Wed, 30 Apr 2008) $
 * @version $LastChangedRevision: 49 $
 * @note $HeadURL: https://oss-dataturbine.googlecode.com/svn/trunk/apps/oss-apps/src/edu/ucsd/osdt/source/numeric/SeabirdSource.java $
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
	
	public SdlParser() {
		super(null, null);
	}
	
	// abstract methods from interface
	public ChannelMap getCmap() {return null;}
	public String[] getChannels() {return null;}
	public String[] getUnits() {return null;}
	
	
	
	/////////////////////
	public boolean parse(String mdFromInstr) {
		try {
			Document document;
			DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			document = documentBuilder.parse(sdlFileName);			
			XPath xp = XPathFactory.newInstance().newXPath();
			// has data channel names
			Node node = (Node)xp.evaluate("/Config/File/DataSeriesMapping[1]", document, XPathConstants.NODE);
			NodeList nodeList = node.getChildNodes();
			
			logger.info("# of nodes: " + nodeList.getLength());
			
			for(int i=0; i<nodeList.getLength(); i++) {
				
				Node anode = nodeList.item(i);
				if(anode.getChildNodes().getLength() == 1) { // then there this node has a value node
					logger.info("node #" + i + ": " + anode.getNodeName() + " has value: " + anode.getChildNodes().item(0).getNodeValue());
				}
			} // for
		} catch(Exception e) {
			logger.severe("sumpin happened: " + e.toString());
			return false;
		}
		return true;
	}
	
	
	
	
	public double getRbnbTimestamp(String instrTimestamp) {return -1;}
	
	
	protected Options setOptions() {
		Options opt = setBaseOptions(new Options()); // uses h, v, s, p, S

		opt.addOption("s", true, "SDL configuration file name*" + DEFAULT_SDL_FILE_NAME);
		opt.addOption("f", true, "Input LoggerNet file name*" + DEFAULT_LN_FILE_NAME);

		return opt;
	} // setOptions()


	/*! @brief Command-line processing.
	 * @note required by interface RBNBBase */
	protected boolean setArgs(CommandLine cmd) throws IllegalArgumentException {
		if (!setBaseArgs(cmd)) return false;

		if(cmd.hasOption('s')) { // loggernet file name
			String v = cmd.getOptionValue("f");
			sdlFileName = v;
		}
		if(cmd.hasOption('f')) { // loggernet file name
			String v = cmd.getOptionValue("f");
			loggernetFileName = v;
		}
		return true;
	} // setArgs()
	
}