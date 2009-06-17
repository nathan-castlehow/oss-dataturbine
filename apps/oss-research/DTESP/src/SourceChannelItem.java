
	import com.rbnb.sapi.ChannelMap;
	import com.rbnb.sapi.Source;
	import org.w3c.dom.Element;
	import org.xml.sax.SAXException;

	

	/**
	 * <pre>
	 * Data structure to save a configuration of a source channel
	 * fields
	 *   name- name of the channel
	 *   source_item- the source configuration class (initialized with string id)
	 *   channel_string- string to connect this channel 
	 *   channel_index- channel index associated with source (initialized when connected)
	 *   event_item - configuration of an event associated with this channel (initialized with string id)
	 */		
	public class 			SourceChannelItem
	{
		/**
		 * <pre>
		 * Create from XML file
		 * (see fields)
		 * attributes of xml:
		 * name, channel_string, source, and event
		 */
		
		public SourceChannelItem(Element e, DTESPConfigObj co) 
		{
			name				=				e.getAttribute("name");
			channel_string		=				e.getAttribute("channel_string");
			
			source_item			=co.GetSource(	e.getAttribute("source"));
			event_item			=co.GetEvent(	e.getAttribute("event"));
		}		
				
		/**
		 * Create explicitly
		 */
		public SourceChannelItem(String name_, SourceItem source_item_, String channel_string_, EventItem event_item_)
		{
			name= name_;
			channel_string	=channel_string_;
			source_item		=source_item_;
			event_item		=event_item_;
		}
		String				name;
		String				channel_string;
	    EventItem	 		event_item;
	    SourceItem         	source_item;	  
	    int					channel_index;
	};	

