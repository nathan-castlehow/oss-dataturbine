import org.w3c.dom.Element;



/**
 * <pre>
 * Data structure to save a configuration of a sink channel
 * fields
 *   name- name of the channel
 *   sink- sink class owns this channel (initialized with string id of sink)
 *   channel_string- name of channel to be used for connection 
 *   event- name of esper event associated with the channel 
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
	public SinkChannelItem(Element e, DTESPConfigObj co) 
	{
		name				=				e.getAttribute("name");
		channel_string		=				e.getAttribute("channel_string");
		
		SinkItem si			=co.GetSink(	e.getAttribute("sink"));
		si.AddChannel(this);
		sink_item=si;
		event_item			=co.GetEvent(	e.getAttribute("event"));
	}		
	
	/**
	 * Creating explicitly specifying parameters
	 */
	public SinkChannelItem(String name_, SinkItem si, String channel_string_, EventItem event_item_)
	{
		name= name_;
		si.AddChannel(this);
		sink_item=si;
		channel_string	=channel_string_;
		event_item		=event_item_;
	}
	String			name;
	String			channel_string;
	SinkItem		sink_item;
    EventItem 		event_item;
    double			last_data_time=-1;
};