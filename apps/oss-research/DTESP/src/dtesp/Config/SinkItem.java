package dtesp.Config;


import org.w3c.dom.Element;




/**
 * <pre>
 * Data structure to save a configuration of a sink (DT server)
 * fields
 *   name- name of the sink
 *   connection_string- string used for connection
 *   copy_to_source- copy current sink to a source
 */	
public class 			SinkItem
{
	
	/**
	 * <pre>
	 * Parse from xml file.
	 * 
	 *   attributes:
	 *   name
	 *   client
	 *   connection_string
	 *   copy_to_source
	 */
	public SinkItem(Element e, ConfigObj co) 
	{
		name= 				e.getAttribute("name");
		client=				e.getAttribute("client");
		connection_string=	e.getAttribute("connection_string");
		copy_to_source	=	e.getAttribute("copy_to_source");
	}		
	
	public SinkItem(String name_, String client_, String connection_string_)
	{
		name= name_;
		client=client_;
		connection_string=connection_string_;
	}
	
	public String					name;
	public String					client;
	public String					connection_string;
    public String 					copy_to_source;
};

