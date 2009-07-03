package dtesp.Config;
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
	 *   cache_size
	 *   archive_mode
	 *   archive_size
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
			
			
			
			if (e.hasAttribute("cache_size"))
				cacheSize=Integer.parseInt(e.getAttribute("cache_size"));

			if (e.hasAttribute("archive_mode"))
				archiveMode=e.getAttribute("archive_mode");

			if (e.hasAttribute("archive_size"))
				archiveSize=Integer.parseInt(e.getAttribute("archive_size"));					
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
		public String				name;
		public String				client;
		public String				connection_string;
	    
		public int					cacheSize=100;
		public String				archiveMode="none";
		public int					archiveSize=0;	    
	};
	
	
	
	