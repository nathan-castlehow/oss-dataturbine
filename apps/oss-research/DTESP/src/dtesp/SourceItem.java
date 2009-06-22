import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.Source;
import org.w3c.dom.Element;

	/**
	 * <pre>
	 * Data structure to save a configuration of a source (DT server)
	 * fields
	 *   name- name of the channel
	 *   client- name of this client
	 *   source- source class (initialized with string id of source)
	 *   connection_string- connection string to be used for connection 
	 *   cmap- channel map class to be used with this source 
	 */	
	public class 			SourceItem
	{
		/**
		 * Create from xml
		 * attributes of xml(see fields):
		 * 		name, client, and connection_string
		 */
		public SourceItem(Element e) 
		{
			name= 					e.getAttribute("name");
			client=					e.getAttribute("client");
			connection_string=		e.getAttribute("connection_string");
		}		
		
		/**
		 * Create explicitly 
		 */
		public SourceItem(String name_, String client_, String connection_string_)
		{
			name= name_;
			client=client_;
			connection_string=connection_string_;
		}
		String				name;
		String				client;
		Source          	source;
	    String				connection_string;
	    ChannelMap			cmap;
	};
	
	
	
	