/**********************************************************************************
*
**********************************************************************************/

// package
package org.nees.rbnb;

// imports
import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.apache.xml.serialize.*;
import java.net.*;
import java.io.*;

/**
* <p>Xml is a DOM XML helper object adapted from the object of the same name in CHEF.
* It has been extended as a utility class for the RBNB NEES Project.</p>
* @author The Chef team
* @author Glenn Golden
* @author Terry E Weymouth
* @version CVS Revision number $Revision: 153 $
*/
public class Xml
{

	public static final String XML_TAG_FOR_ERROR_MESSAGE = "errorMessage";
	public static final String XML_TAG_FOR_STATUS_MESSAGE = "statusMessage";

	/**
	* Create a new DOM Document.
	* @return A new DOM document.
	*/
	public static Document createDocument()
	{
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();

			return doc;
		}
		catch (Exception any)
		{
			return null;
		}

	}	// createDocument

	/**
	* Read a DOM Document from xml in a file.
	* @param name The file name for the xml file.
	* @return A new DOM Document with the xml contents.
	*/
	public static Document readDocument(String name)
	{
		try
		{
			// parse the xml from the url (code taken from log4j)
			InputStreamReader in = new InputStreamReader(new FileInputStream(name), "UTF-8");
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = dbf.newDocumentBuilder();
			InputSource inputSource = new InputSource(in);
			Document doc = docBuilder.parse(inputSource);
			return doc;
		}
		catch (Exception any)
		{
			return null;
		}

	}	// readDocument

	/**
	* Read a DOM Document from xml in a string.
	* @param in The string containing the XML
	* @return A new DOM Document with the xml contents.
	*/
	public static Document readDocumentFromString(String in)
	{
		try
		{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = dbf.newDocumentBuilder();
			InputSource inputSource = new InputSource(new StringReader(in));
			Document doc = docBuilder.parse(inputSource);
			return doc;
		}
		catch (Exception any)
		{
			return null;
		}

	}	// readDocumentFromString

	/**
	* Write a DOM Document to an xml file.
	* @param doc The DOM Document to write.
	* @param fileName The complete file name path.
	*/
	public static void writeDocument(Document doc, String fileName)
				throws IOException
	{
		OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8");
		// Note: using xerces %%% is there a org.w3c.dom way to do this?
		XMLSerializer s = new XMLSerializer(out, new OutputFormat(
							"xml", "UTF-8", true));
		s.serialize(doc);
		out.close();

    }	// writeDocument

   	/**
	* Write a DOM Document to an output stream.
	* @param doc The DOM Document to write.
	* @param out The output stream.
	*/
	public static String writeDocumentToString(Document doc) throws IOException
	{
		StringWriter sw = new StringWriter();
		// Note: using xerces %%% is there a org.w3c.dom way to do this?
		XMLSerializer s = new XMLSerializer(sw, new OutputFormat(
								"xml", "UTF-8", true /*doc*/));
		s.serialize(doc);

		sw.flush();
		return sw.toString();

    }	// writeDocument
    
    public static String errorXml(String problem, String reason)
    {
    	return
    		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +    		"<" + XML_TAG_FOR_ERROR_MESSAGE +">\n" +    		"  <problem>" +
    			 problem +    		"  </problem>\n" +    		"  <reason>" +
				reason +    		"  </reason>\n" +    		"</" + XML_TAG_FOR_ERROR_MESSAGE +">";
    }

	/**
	 * @param string
	 * @return
	 */
	public static String statusXml(String string) {
		return
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<" + XML_TAG_FOR_STATUS_MESSAGE +">" +
				string +
			"</" + XML_TAG_FOR_STATUS_MESSAGE +">\n";
	}

}	// Xml

/**********************************************************************************
*
* $Header: /disks/cvs/neesgrid/turbine/src/org/nees/rbnb/Xml.java,v 1.4 2004/06/29 07:41:31 weymouth Exp $
*
**********************************************************************************/
