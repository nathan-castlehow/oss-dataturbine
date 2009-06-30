package dtesp;
import org.w3c.dom.Element;

/**
 * <pre>
 * Data structure to save a configuration of a esper query
 * fields
 *   name- name of the channel
 *   query_string- esper query string
 */		
public class 			QueryItem
{
	/**
	 * Create from XML
	 * (see fields)
	 * attributes of xml:
	 * name, query_string, and source_channel
	 */
	public QueryItem(Element e,DTESPConfigObj co) 
	{
		name						=						e.getAttribute("name");
		query_string				=						e.getAttribute("query_string");
		source_channel_item			=co.GetSourceChannel(	e.getAttribute("source_channel"));
	}	
	
	public QueryItem(String name_,String query_string_, SourceChannelItem source_channel_item_)
	{
		name				=name_;
		query_string		=query_string_;
		source_channel_item	=source_channel_item_;
	}    
	String				name;
	String 				query_string;
	SourceChannelItem 	source_channel_item;
};		
