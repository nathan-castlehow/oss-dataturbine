/*
 * Created on Apr 27, 2004
 */
package org.nees.rbnb;

import java.util.Properties;
import java.util.Enumeration;

import java.io.IOException;
import java.io.StringReader;
import java.io.BufferedReader;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

/**
 * @author terry
 */
public class MetadataPropertyList extends Properties {
	
	static final String PROPERTY_TAG = "metadata-property-list";
	
	public String toXmlString() throws IOException
	{
		Document doc = Xml.createDocument();
		Node node, root = doc.createElement(PROPERTY_TAG);
		Enumeration e = this.keys();
		String key, value;
		while (e.hasMoreElements())
		{
			key = (String) e.nextElement();
			value = this.getProperty(key);
			node = doc.createElement(key);
			node.appendChild(doc.createTextNode(value));
			root.appendChild(node);
		}
		doc.appendChild(root);

		String out = "";
		try
		{
			out = Xml.writeDocumentToString(doc);
		} catch (IOException e1) {
			throw new IOException("MetadataPropertyList Failed to generate xml string " + e1);
		}
		return out;
	}
	
	public void initializeFromXml(String xml) throws IllegalArgumentException, IOException
	{
		this.clear();
		addFromXml(xml);
	}
	
	public void addFromXml(String xml)
	{
		// convert to doc	
		Document doc = Xml.readDocumentFromString(xml);

		// get the root element
		Element root = doc.getDocumentElement();
		if (!(root.getTagName().equals(PROPERTY_TAG)))
			throw new IllegalArgumentException(
				"XML string does not have root tag with " + PROPERTY_TAG
				+ "; found " + root.getTagName() + " instead.");

		// process the children
		NodeList children = root.getChildNodes();
		final int length = children.getLength();
		for(int i = 0; i < length; i++)
		{
			Node child = children.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE)
			{
				Element element = (Element)child;
				String key = element.getTagName();
				Node textNode = child.getChildNodes().item(0);
				String value = textNode.getNodeValue();
				this.setProperty(key,value);				
			}
		}
	}
}
