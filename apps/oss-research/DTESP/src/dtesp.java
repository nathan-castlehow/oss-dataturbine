


import java.text.DateFormat;
import java.util.*;
import com.rbnb.sapi.*;
import java.io.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


import com.espertech.esper.client.*;
import com.espertech.esper.client.time.CurrentTimeEvent;
import com.espertech.esper.client.time.TimerControlEvent;





public class dtesp {


	/**
	 * 	Main method to start dtesp
	 */
	public static void main(String args[])
	{
		dtesp sink=new dtesp();
		sink.run();
	}
	

	/**
	 * Load XML setting file
	 * 	 * @param fn - filename
	 * 
	 */
	protected void LoadXml(String fn)
	{
		Document dom=null;
		

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		try {

			//Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();

			//parse using builder to get DOM representation of the XML file
			dom = db.parse(fn);


		}catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		}catch(SAXException se) {
			se.printStackTrace();
		}catch(IOException ioe) {
			ioe.printStackTrace();
		}
	
		Element docEle = dom.getDocumentElement();
		
		NodeList nl ;
		

		// Start parsing nodes
		nl = docEle.getElementsByTagName("Setting");
		if(nl != null && nl.getLength() > 0) 
			for(int i = 0 ; i < nl.getLength();i++) SetEnvironment((Element)nl.item(i));
		
		
		nl = docEle.getElementsByTagName("RequestTime");
		if(nl != null && nl.getLength() > 0) 
			for(int i = 0 ; i < nl.getLength();i++) SetTime((Element)nl.item(i));

		
		nl = docEle.getElementsByTagName("Source");
		if(nl != null && nl.getLength() > 0) 
			for(int i = 0 ; i < nl.getLength();i++) AddSource(new SourceItem((Element)nl.item(i)));
		
		nl = docEle.getElementsByTagName("Sink");
		if(nl != null && nl.getLength() > 0) 
			for(int i = 0 ; i < nl.getLength();i++) AddSink(new SinkItem((Element)nl.item(i)));
		
		nl = docEle.getElementsByTagName("Event");
		if(nl != null && nl.getLength() > 0) 
			for(int i = 0 ; i < nl.getLength();i++) AddEvent(new EventItem((Element)nl.item(i)));

		nl = docEle.getElementsByTagName("SinkChannel");
		if(nl != null && nl.getLength() > 0) 
			for(int i = 0 ; i < nl.getLength();i++) AddSinkChannel(new SinkChannelItem((Element)nl.item(i),this));

		nl = docEle.getElementsByTagName("SourceChannel");
		if(nl != null && nl.getLength() > 0) 
			for(int i = 0 ; i < nl.getLength();i++) AddSourceChannel(new SourceChannelItem((Element)nl.item(i),this));

		nl = docEle.getElementsByTagName("Query");
		if(nl != null && nl.getLength() > 0) 
			for(int i = 0 ; i < nl.getLength();i++) AddQuery(new QueryItem((Element)nl.item(i),this));
		
	}
	
	
	
	/**
	 * <pre>
	 *  set environment from xml file 
	 *  tag <Setting>
	 *  
	 *  Attributes:
	 *   esper_time_granuality_minute: 	maximum time step esper can advance at once (in minutes)
	 *   esper_time_granuality_sec:     maximum time step esper can advance at once (in sec)
	 */
	protected void SetEnvironment(Element e)
	{
		try
		{
			maximum_time_granuality=new Integer(e.getAttribute("esper_time_granuality_minute"))*60*1000;
		}		catch (Exception e_) {}
		
		try
		{
			maximum_time_granuality=new Integer(e.getAttribute("esper_time_granuality_sec"))*1000;
		}		catch (Exception e_) {}
	
	}
	
	/**
	 * <pre>
	 *  Set time of data want to retrieve from xml file
	 *  Tag <Requesttime> 
	 *  
	 *  Example: 4/10/2009 AM 5:57:0
	 *  <RequestTime year="2009" month="4" date="10" hour="5" minute="57" second="0"></RequestTime>
	 *  
	 *  Attributes:
	 *  request_time_window_min: length of data window to be requested for one fetch instruction in minutes   
	 */
	protected void SetTime(Element e)
	{
    	Calendar c=new GregorianCalendar();
    	c.clear();
    	
		try
		{
			int t= 	new Integer(e.getAttribute("year"));
			c.set(Calendar.YEAR,t);
		}		catch (Exception e_) {}
		
		try
		{
			int t= 	new Integer(e.getAttribute("month"));
			c.set(Calendar.MONTH,t-1);
		}		catch (Exception e_) {}

		try
		{
			int t= 	new Integer(e.getAttribute("date"));
			c.set(Calendar.DATE,t);
		}		catch (Exception e_) {}
		
		try
		{
			int t= 	new Integer(e.getAttribute("hour"));
			c.set(Calendar.HOUR,t);
		}		catch (Exception e_) {}

		try
		{
			int t= 	new Integer(e.getAttribute("minute"));
			c.set(Calendar.MINUTE,t);
		}		catch (Exception e_) {}

		try
		{
			int t= 	new Integer(e.getAttribute("second"));
			c.set(Calendar.SECOND,t);
		}		catch (Exception e_) {}

		try
		{
			int t= 	new Integer(e.getAttribute("request_time_window_min"));
			request_duration=t*60;
		}		catch (Exception e_) {}
		
		
		request_start=c.getTimeInMillis()/1000;
		bRealTime=false;
		DateFormat df=DateFormat.getInstance();
		
    	System.out.println("Requested time "+df.format(c.getTime()));

	}
	
	
	/**
	 * <pre>
	 * Data structure to save a configuration of a sink (DT server)
	 * fields
	 *   name- name of the sink
	 *   sink- sink class created for this sink
	 *   connection_string- string used for connection
	 *   channel_item_list- list of sink channel configurations 
	 */	
	protected class 			SinkItem
	{
		
		/**
		 * <pre>
		 * Parse from xml file.
		 * 
		 *   attributes:
		 *   name
		 *   client
		 *   connection_string
		 */
		public SinkItem(Element e) 
		{
			name= 				e.getAttribute("name");
			client=				e.getAttribute("client");
			connection_string=	e.getAttribute("connection_string");
		}		
		
		public SinkItem(String name_, String client_, String connection_string_)
		{
			name= name_;
			client=client_;
			connection_string=connection_string_;
		}
		
		public void AddChannel(SinkChannelItem sci)
		{
			channel_item_list.add(sci);
		}
		String					name;
		String					client;
		Sink          			sink;
	    String					connection_string;
	    ChannelMap				cmap;
	    List<SinkChannelItem>	channel_item_list= new LinkedList<SinkChannelItem>();		
	};
	
	
	/**
	 * <pre>
	 * Data structure to save a configuration of a sink channel
	 * fields
	 *   name- name of the channel
	 *   sink- sink class owns this channel (initialized with string id of sink)
	 *   channel_string- name of channel to be used for connection 
	 *   event- name of esper event associated with the channel 
	 */	
	protected class 			SinkChannelItem
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
		public SinkChannelItem(Element e, dtesp ts) 
		{
			name				=				e.getAttribute("name");
			channel_string		=				e.getAttribute("channel_string");
			
			SinkItem si			=ts.GetSink(	e.getAttribute("sink"));
			si.AddChannel(this);
			sink_item=si;
			event_item			=ts.GetEvent(	e.getAttribute("event"));
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
	};
	
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
	protected class 			SourceItem
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
	protected class 			SourceChannelItem
	{
		/**
		 * <pre>
		 * Create from XML file
		 * (see fields)
		 * attributes of xml:
		 * name, channel_string, source, and event
		 */
		
		public SourceChannelItem(Element e, dtesp ts) 
		{
			name				=				e.getAttribute("name");
			channel_string		=				e.getAttribute("channel_string");
			
			source_item			=ts.GetSource(	e.getAttribute("source"));
			event_item			=ts.GetEvent(	e.getAttribute("event"));
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

	/**
	 * <pre>
	 * Data structure to save a configuration of a event
	 * fields
	 *   name- name of the event
	 *   field- name of the field to be used in esper
	 */		
	protected class 			EventItem
	{
		public EventItem(Element e) 
		{
			name				=e.getAttribute("name");
			field				=e.getAttribute("field");
		}		
						
		public EventItem(String name_, String field_)
		{
			name	=name_;
			field	=field_;
		}
		String				name;
		String				field;
	};	
	
	/**
	 * <pre>
	 * Data structure to save a configuration of a esper query
	 * fields
	 *   name- name of the channel
	 *   query_string- esper query string
	 *   event_item- output event of query 
	 */		
	protected class 			QueryItem
	{
		/**
		 * Create from XML
		 * (see fields)
		 * attributes of xml:
		 * name, query_string, and source_channel
		 */
		public QueryItem(Element e,dtesp ts) 
		{
			name						=						e.getAttribute("name");
			query_string				=						e.getAttribute("query_string");
			source_channel_item			=ts.GetSourceChannel(	e.getAttribute("source_channel"));
		}	
		
		public QueryItem(String name_,String query_string_, EventItem event_item_, SourceChannelItem source_channel_item_)
		{
			name				=name_;
			query_string		=query_string_;
			source_channel_item	=source_channel_item_;
		}    
		String				name;
		String 				query_string;
		SourceChannelItem 	source_channel_item;
	};		

	
	/**
	 * List of source configuration(SourceItem)
	 */
	HashMap<String, SourceItem> 		hmap_source_item			= new HashMap<String, SourceItem>();
	/**
	 * List of sink configuration(SinkItem)
	 */
	HashMap<String, SinkItem> 			hmap_sink_item				= new HashMap<String, SinkItem>();			
	/**
	 * List of source channel configuration(SourceChannelItem)
	 */
	HashMap<String, SourceChannelItem> 	hmap_source_channel_item	= new HashMap<String, SourceChannelItem>();
	/**
	 * List of sink channel configuration(SinkChannelItem)
	 */
	HashMap<String, SinkChannelItem> 	hmap_sink_channel_item		= new HashMap<String, SinkChannelItem>();
	/**
	 * List of event configuration(EventItem)
	 */
	HashMap<String, EventItem>			hmap_event_item				= new HashMap<String, EventItem>();
	/**
	 * List of query configuration(QueryItem)
	 */
	List<QueryItem>						list_query_item				= new LinkedList<QueryItem>();
	
	
	/**
	 * Add source configuration to list
	 */
	public SourceItem AddSource(SourceItem si)
	{
		hmap_source_item.put(si.name, si);
		return si;
	}
	/**
	 * Retrieve source by its name 
	 */
	public SourceItem GetSource(String name)
	{
		return hmap_source_item.get(name);
	}

	/**
	 * Add sink configuration to list
	 */
	public SinkItem AddSink(SinkItem si)
	{
		hmap_sink_item.put(si.name, si);
		return si;
	}
	/**
	 * Retrieve sink by its name 
	 */
	public SinkItem GetSink(String name)
	{
		return hmap_sink_item.get(name);
	}
	
	/**
	 * Add source channel configuration to list
	 */	
	public dtesp AddSourceChannel(SourceChannelItem sci)
	{
		hmap_source_channel_item.put(sci.name, sci);
		return this;
	}
	
	/**
	 * Add sink channel configuration to list
	 */	
	public dtesp AddSinkChannel(SinkChannelItem sci)
	{
		hmap_sink_channel_item.put(sci.name, sci);
		return this;
	}
	
	/**
	 * Retrieve source channel by its name 
	 */	
	public SourceChannelItem	GetSourceChannel(String name)
	{
		return hmap_source_channel_item.get(name);
	}
	/**
	 * Add event configuration to list
	 */	
	public dtesp AddEvent(EventItem ei)
	{
		hmap_event_item.put(ei.name, ei);
		return this;
	}
	/**
	 * Retrieve event channel by its name 
	 */	
	public EventItem GetEvent(String name)
	{
		return hmap_event_item.get(name);
	}
	/**
	 * Add query configuration to list
	 */	
	public dtesp AddQuery(QueryItem qi)
	{
		list_query_item.add(qi);
		return this;
	}
	
	

    
/**
 * Start service
 * 1. Loads XML
 * 2. Initialize data turbine
 * 3. Initialize Esper
 * 4. Start Fetching data 
 */
    public void run()
    {
    	LoadXml("setting.xml");
    	Init_DT();
    	Init_Esper();
    	Fetch();
    }
    
    /**
     * Prepare DT connection. Create and connect source, source channel, sink, and sink channel 
     */
    
    public void Init_DT()
    {
    	
        
        // Create source
        for (SourceItem s : hmap_source_item.values())
        {
        	System.out.println("Connecting Source "+s.name+": "+s.connection_string);
	        try
	        {
	            s.source = new Source(); 	// Default source is 100-frame cache, no archive
	            
	            // connect!
	            s.source.OpenRBNBConnection(s.connection_string, s.client);
	        
	            s.cmap= new ChannelMap();
	        
	        } catch (SAPIException se) {
	            System.out.println("Error on Turbine - not connected");
	            return;
	        }
        }

        
        

        // Attach source channel
        for (SourceChannelItem c:hmap_source_channel_item.values())
        {
        	System.out.println("Adding SourceChannel "+c.name+": "+c.channel_string);        	
	        try 
	        {
	            c.channel_index = c.source_item.cmap.Add(c.channel_string);
	        }catch (SAPIException se) {
	            System.out.println("Error adding to channel map!");
	            return;
	        }
        }

        

     
        // Create sink        
        for (SinkItem s : hmap_sink_item.values())
        {
        	System.out.println("Connecting Sink "+s.name+": "+s.connection_string);
	        try
	        {
	            s.sink = new Sink(); 	// Default sink is 100-frame cache, no archive
	            // connect!
	            s.sink.OpenRBNBConnection(s.connection_string, s.client);
	        
	            s.cmap= new ChannelMap();
	        
	        } catch (SAPIException se) {
	            System.out.println("Error on Turbine - not connected");
	            return;
	        }
	        
	        if (!s.channel_item_list.isEmpty())
	        {
		        try 
		        {
		            // Create sink channel		        	
			        for (SinkChannelItem c:s.channel_item_list)
			        {
			        	System.out.println("Adding SinkChannel"+c.name+": "+c.channel_string);
			        	s.cmap.Add(c.channel_string);
			        }
			        if (bRealTime)
			        	// if realtime subscribe
			        	s.sink.Subscribe(s.cmap);

		        }
		        catch (SAPIException se) {
		            System.out.println("Error adding to channel map!");
		            return;
		        }
	        }
	        
        }
    }

        
        


        
        
        

    
    /**
     * <pre> Listener that listen to esper event and sends to dt
     */
    public class EsperEventListener implements UpdateListener
    {
    	SourceChannelItem 	source_channel_item;
    	dtesp				dtesp_;


        public EsperEventListener(SourceChannelItem sci, dtesp dtesp__)
        {
        	source_channel_item=sci;
        	dtesp_=dtesp__;
        }
        
    	
    	public void update(EventBean[] newEvents, EventBean[] oldEvents) 
    	{
    	    double[]          data = {0};
    	    EventBean event = newEvents[0];
    	    
    	    
    	    double v=Double.parseDouble(event.get(source_channel_item.event_item.field).toString());
    	    
    	    System.out.println("E "+source_channel_item.name+" : " + v);

    	    ChannelMap 	output_cmap		=source_channel_item.source_item.cmap;
    	    Source 		source			=source_channel_item.source_item.source;
    	    int			channel_index	=source_channel_item.channel_index;
    	    try
    	    {
	    	    data[0] = v ;
				if (dtesp_.bRealTime)
	    	    	// if real time
	    	    	output_cmap.PutTimeAuto("timeofday");
	    	    else
	    	    {
	    	    	// if not, use esper time
	    	    	double[] time={0};
	    	    	time[0]=dtesp_.last_saved_esper_time;
	    	    	output_cmap.PutTimes(time);
	    	    }
	    	    
	    	    // On nees, we assume that octet-stream data is double-precision float
	    	    output_cmap.PutMime(channel_index, "application/octet-stream");
	    	    output_cmap.PutDataAsFloat64(channel_index, data);
	    	    
	    	    source.Flush(output_cmap, false);
	    	}
    	    catch (SAPIException mse) 
	    	{
	    	    System.out.println("Error saving data!");
	    	}
    	    
    	}    	
    }

    
    /** 
     *  Esper service object
     */
    public EPServiceProvider	epService;
    /** 
     *  Esper runtime object
     */
	public EPRuntime 			epRuntime;
	
	
    /** 
     *  If this is true, esper will process data in real time. Time stamp from data turbine will be discarded, and time of running this application will be used.
     *  If data of certain time is requested, then set to false;
     */	
	public boolean bRealTime;
	
    /** 
     *  Start time of the request
     */	
	double request_start;
    /** 
     *  Data duration for one fetch
     */	
	double request_duration=60; // 1 min
    /** 
     * Last time of esper we set 
     */	
	long last_saved_esper_time;
    /** 
     * maximum of time advancement for esper
     * 
     *   Example:
     *   Current esper time is 0 sec. maximum_time_granuality is .5 sec. Next data came at 2 sec. 
     *   Then, esper time will be set at .5 sec, 1 sec, 1.5 sec, and 2 sec.
     */	
	long maximum_time_granuality=60000; // 1 min
	

    /** 
     * <pre>Initialize esper
     * 
     * 1. (If not real time)Set esper time
     * 2. Register event that will be sent to esper
     * 3. Create query and register listener if we want to 
     */

    protected void Init_Esper()
    {
		epService = EPServiceProviderManager.getDefaultProvider();
		
		// (If not real time)Set esper time
		if (!bRealTime)
		{
			epRuntime = epService.getEPRuntime();
			
			// external time mode
			epRuntime.sendEvent(new TimerControlEvent(TimerControlEvent.ClockType.CLOCK_EXTERNAL));

			// esper time = time of requested data 
			last_saved_esper_time=new Double(request_start*1000).longValue();
			epRuntime.sendEvent(new CurrentTimeEvent(last_saved_esper_time));
			
		}

		// Register event that will be sent to esper
		for (EventItem ei:hmap_event_item.values())
		{		
        	System.out.println("Creating Event "+ei.name+": field "+ei.field);
			
	        Map<String, Object> definition = new LinkedHashMap<String, Object>();
	        definition.put(ei.field, double.class);
	        epService.getEPAdministrator().getConfiguration().addEventType(ei.name, definition);
		}
		
		// Create query and register listener if we want to 
		for (QueryItem qi:list_query_item)
		{		
        	System.out.println("Adding Query "+qi.name+": "+qi.query_string);
			EPStatement statement = epService.getEPAdministrator().createEPL(qi.query_string);
			
			// if we are going to sent to some source
			if (qi.source_channel_item!=null)
				statement.addListener(new EsperEventListener(qi.source_channel_item,this));			
		}
		
    }
        


    /** 
     * <pre>Main loop for fetching data\n
     * 
     * 1. (Only if you are requesting data at certain time (replaying old data)) For each sink channel request data
     * 
     * 2. Fetch data for each channel, and save it in a array
     * 
     * 3. We have to send to esper in time order. Data within a channel is in time order, but we don't know time order of data over all channels. So insert earliest data from each channel to sorted list. 
     * 
     * 4. Before setting esper time to the data time, check if time difference between last esper time is greater than maximum_time_granuality.
     * If so, time is advancing too much at once. We traverse intermediate time and set it to esper time to the data time.
     * 
     * 5. Send the earliest data in the sorted list to esper, and remove it. Add next data of the channel to the sorted list. Repeat from 4 until all the data is sent to esper. 
     * </pre> 
     */ 
     
    protected void Fetch()
    {
		
        try 
        {
            
            System.out.println("Waiting for data...");
  
            
            /**
             * 
             * Temporary class needed for processing data in time order
             * This represents time stamped data received from a channel. 
             * 
             */
        	class ReceivedDataFromChannel
        	{
        		SinkChannelItem		sink_channel;
        		double[] data;						// data received from one fetch in a channel
        		double[] data_time;					// time stamp associated with it
        		int index=0;						// how many data has been sent?
        		
        		// have we sent all?
        		boolean bIsempty()
        		{
        			if (index>=data.length) return false;
        			return true;
        		}
        		
        		double 	GetData()       		{return data[index];}
        		double 	GetTime()       		{return data_time[index];}
        		void 	Next()        			{index++;}						// get next data
        		void 	Clear()					{index=0; data=null; data_time=null;}
        	};    
        	
        	
            /**
             * 
             * Temporary class needed for processing data in time order
             * This implements a sorted linked list of time and index pair in time order.
             * So first node will be an index of channel with earliest data.
             * We add all the first data and index pair of the channels into the sorted linked list. 
             * And we process data in the channel whose index is first node in the sorted linked list. 
             * 
             */
        	
        	class ChannelIndexSortedByTime
        	{
        		LinkedList<Double>	time_list	=new LinkedList<Double>();		// sorted linked list of time in ascending order of time
        		LinkedList<Integer>	index_list	=new LinkedList<Integer>();		// sorted linked list of ReceivedDataFromChannel index in ascending order of time 
        		
        		public void Clear()
        		{
        			index_list.clear();
        			time_list.clear();
        		}
        		
        		/**
        		 * Add time and index. List will be sorted in time order
        		 * @param time       time
        		 * @param cindex     channel index
        		 */
        		public void Add(double time, int cindex)
        		{
        			int index=0;
        			ListIterator<Double> iter			=time_list.listIterator();
        			ListIterator<Integer> iter_cindex	=index_list.listIterator();
        			for (;iter.hasNext();)
        			{
        				iter_cindex.next();
        				if (time<iter.next())
        				{
        					iter.add(time);
        					iter_cindex.add(cindex);
        					return;
        				}
        			index++;
        			}
					time_list.addLast(time);
					index_list.addLast(cindex);
        		}
        	
        		/**
        		 * Return the channel index whose data is earliest
        		 * @return channel index
        		 */
        		public int PopFirstChannelIndex()
        		{
        			time_list.removeFirst();
        			int index=index_list.getFirst();
        			index_list.removeFirst();
        			return index;
        		}
        		
        		public boolean IsEmpty()
        		{
        			return time_list.isEmpty();
        		}
        	};
            
        	ArrayList<ReceivedDataFromChannel> list_received_data 	= new ArrayList<ReceivedDataFromChannel>();
        	ChannelIndexSortedByTime sorted_channel_index			= new ChannelIndexSortedByTime();
        	
        	
        	// Prepare and instantiate classes instead of doing this in main loop for time efficiency   
            for (SinkItem s : hmap_sink_item.values())
  			        for (SinkChannelItem c:s.channel_item_list)
  			        		list_received_data.add(new ReceivedDataFromChannel());


            // count of how channel has been received 
            int recieved_channel_count;
            
            
            while (true) 
            {
     	
            	// we request data at certain point if we are not doing in real time
            	if (!bRealTime)
	                for (SinkItem s : hmap_sink_item.values())
	        	        if (!s.channel_item_list.isEmpty())
	        	        {
	        		        try 
	        		        {
	        			        for (SinkChannelItem c:s.channel_item_list)
	        			        {
	        			        	s.cmap.Add(c.channel_string);
	        			        }
	        			        s.sink.Request(s.cmap, request_start,request_duration, "absolute");
	        		        }
	        		        catch (SAPIException se) 
	        		        {
	        		            System.out.println("Error requsting channel!");
	        		            return;
	        		        }
	        	        }
	        	        
                

            	sorted_channel_index.Clear();
            	recieved_channel_count=0;
            	
            	for (SinkItem s:hmap_sink_item.values())
            	{
            		ChannelMap outmap = s.sink.Fetch(1000);
            		
                
            		for (SinkChannelItem c:s.channel_item_list)
            		{
    	                // Look up channel index
    	                int chanIdx = outmap.GetIndex(c.channel_string);
    	               
    	                // If channel index is less than zero, then no data in the fetch                
    	                if(chanIdx >= 0)
    	                {
    	                    double[] data;
    	                    double[] data_time;
    	                    // Note that we have to know how to parse the data type!
    	                    data = outmap.GetDataAsFloat64(chanIdx);
    	                    data_time = outmap.GetTimes(chanIdx);
    	                    
    	                    // used ReceivedDataFromChannel instance from already created pool
    	                    ReceivedDataFromChannel rd=list_received_data.get(recieved_channel_count);
    	                    rd.Clear();
    	                    
    	                    rd.sink_channel=c;
    	                    rd.data=data;
    	                    rd.data_time=data_time;
    	                    
    	                    // add the earliest time of data of the channel and it's index
    	                    sorted_channel_index.Add(rd.GetTime(), recieved_channel_count);
    	                    
    	                    recieved_channel_count++;
    	                }
            		}
            	}
            	
            	
            	// until list is empty. this means until every data is sent
            	while (!sorted_channel_index.IsEmpty())
            	{
            		// first channel index would be channel with earliest data
            		int index=sorted_channel_index.PopFirstChannelIndex();
            		ReceivedDataFromChannel rd=list_received_data.get(index);
            		
            		
            		// get the data out
            		double data=rd.GetData();
            		double data_time=rd.GetTime();
            		SinkChannelItem c=rd.sink_channel;
            		
            		
            		//to eliminate duplicated data from last request
                    if (data_time==request_start) break;
                    
                    
                    System.out.println("DT "+ c.name +" : " + data+ " @ " + request_start+ " "+ data_time);

                    // send new time to esper 
                    
                    
                    long l_data_time=new Double(data_time*1000).longValue();
                    if (last_saved_esper_time<l_data_time)
                    {
                    	// to make time advancement less than maximum time granuality 
                    	long time_advancement=l_data_time-last_saved_esper_time;
                    	if (time_advancement>maximum_time_granuality)
                    	{
                    		long temp_time=last_saved_esper_time;
                    		for (;temp_time+maximum_time_granuality<data_time;)
                    		{
                    			temp_time+=maximum_time_granuality;
                    			// advance time according to maximum_time_granuality 
                            	epRuntime.sendEvent(new CurrentTimeEvent(temp_time));
                    		}
                    	}
                    	
                    	epRuntime.sendEvent(new CurrentTimeEvent(l_data_time));
                    	last_saved_esper_time=l_data_time;
                    }
                    
                    
                    // send data to esper
                    Map<String, Object> data_ = new LinkedHashMap<String, Object>();
                    data_.put(c.event_item.field, data);
                    
                    epService.getEPRuntime().sendEvent(data_, c.event_item.name);                        
                    
                    
                    // if the channel has more data, push it into list so that rest of the data can be processed
                    if (!rd.bIsempty())
                    {
                    	rd.Next();
                    	sorted_channel_index.Add(rd.GetTime(),index);
                    }
                    else
                    	rd.Clear();
            	}    
                	
            	
            	if (!bRealTime)
            	{
            		request_start+=request_duration;
            	}
            		
            		
            }
        }catch (SAPIException mse) 
        {
            System.out.println("Error reading data!");
        }

        // close connection
        for (SourceItem si:hmap_source_item.values())
        {
        	if (si.source.VerifyConnection())
        		si.source.CloseRBNBConnection();
        }
        for (SinkItem si:hmap_sink_item.values())
        {
        	if (si.sink.VerifyConnection())
        		si.sink.CloseRBNBConnection();
        }
        System.out.println("Done, exiting.");
        
        return;
    }
}








