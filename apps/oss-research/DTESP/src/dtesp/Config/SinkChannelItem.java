package dtesp.Config;

import org.w3c.dom.Element;



/**
 * <pre>
 * Data structure to save a configuration of a sink channel
 * fields
 *   name- name of the channel
 *   sink- sink name who owns this channel (initialized with string id of sink)
 *   channel_string- name of channel to be used for connection 
 *   event- name of esper event associated with the channel 
 *   
 */	
public class 			SinkChannelItem
{
	/**
	 * <pre>
	 * Parse from xml file.
	 *
	 *   attributes of xml:
	 *   name- name of the channel
	 *   sink- name of the sink
	 *   channel_string- name of channel 
	 *   event- name of esper event associated with the channel 
	 */		
	public SinkChannelItem(Element e, ConfigObj co) 
	{
		name				=				e.getAttribute("name");
		channel_string		=				e.getAttribute("channel_string");

		sink_name			=				e.getAttribute("sink");
		event_name			=				e.getAttribute("event");
	}		
	
	/**
	 * Creating explicitly specifying parameters
	 */
	public SinkChannelItem(String name_, String si, String channel_string_, String event_item_)
	{
		name= name_;
		sink_name=si;
		channel_string	=channel_string_;
		event_name		=event_item_;
	}
	public String			name;
	public String			channel_string;
	public String			sink_name;
	public String	 		event_name;
};