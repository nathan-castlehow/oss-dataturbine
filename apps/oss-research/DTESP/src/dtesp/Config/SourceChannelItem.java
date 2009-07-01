package dtesp.Config;


	import org.w3c.dom.Element;

	

	/**
	 * <pre>
	 * Data structure to save a configuration of a source channel
	 * fields
	 *   name- name of the channel
	 *   source_item- the source configuration class (initialized with string id)
	 *   channel_string- string to connect this channel 
	 *   event_item - configuration of an event associated with this channel (initialized with string id)
	 *   is_zero_one_graph - This channel is 0 or 1. Save this channel as a form of a bar graph
	 */		
	public class 			SourceChannelItem
	{
		/**
		 * <pre>
		 * Create from XML file
		 * (see fields)
		 * attributes of xml:
		 * name, channel_string, source, event, and is_zero_one_graph
		 */
		
		public SourceChannelItem(Element e, ConfigObj co) 
		{
			name				=				e.getAttribute("name");
			channel_string		=				e.getAttribute("channel_string");
			
			event_name			=				e.getAttribute("source");
			source_name			=				e.getAttribute("event");
			
			is_zero_one_graph	=				e.getAttribute("zero_one_graph").compareTo("1")==0;
			
	
		}		
				
		/**
		 * Create explicitly
		 */
		public SourceChannelItem(String name_, String source_item_, String channel_string_, String event_item_, Boolean is_zero_one_graph_)
		{
			name= name_;
			channel_string	=channel_string_;
			event_name		=source_item_;
			source_name		=event_item_;
			
			is_zero_one_graph=is_zero_one_graph_;
		}
		public String				name;
		public String				channel_string;
	
		public String	 			event_name;
	    public String     	    	source_name;	  
	    public Boolean				is_zero_one_graph;
	};	

