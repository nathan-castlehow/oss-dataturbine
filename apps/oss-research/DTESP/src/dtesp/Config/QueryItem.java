package dtesp.Config;
import org.w3c.dom.Element;

/**
 * <pre>
 * Data structure to save a configuration of a esper query
 * fields
 *   name					- name of the channel
 *   query_string			- esper query string
 *   source_channel_name 	- name of channel where result of query is saved
 */		
public class 			QueryItem
{
	/**
	 * Create from XML
	 * (see fields)
	 * attributes of xml:
	 * name, query_string, and source_channel
	 */
	public QueryItem(Element e,ConfigObj co) 
	{
		name						=						e.getAttribute("name");
		query_string				=						e.getAttribute("query_string");
		source_channel_name			=						e.getAttribute("source_channel");
	}	
	
	public QueryItem(String name_,String query_string_, String source_channel_name_)
	{
		name				=name_;
		query_string		=query_string_;
		source_channel_name	=source_channel_name_;
	}    
	public String				name;
	public String 				query_string;
	public String			 	source_channel_name;
};		
