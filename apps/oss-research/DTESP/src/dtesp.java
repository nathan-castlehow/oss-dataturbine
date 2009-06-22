


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;
import com.rbnb.sapi.*;


import com.espertech.esper.client.*;
import com.espertech.esper.client.time.CurrentTimeEvent;
import com.espertech.esper.client.time.TimerControlEvent;





public class dtesp {


	/**
	 * 	Main method to start dtesp
	 */
	public static void main(String args[])
	{
		dtesp dtesp_=new dtesp();
		DTESPConfigObjCreator coc=new DTESPConfigObjCreator();  
		
		if (args.length==0)
			dtesp_.SetConfigObj(coc.CreateFromXml("setting.xml"));
		else
		{
			dtesp_.SetConfigObj(coc.CreateFromXml(args[0]));
		}
		dtesp_.run();
	}
	
	
	/**
	 * Configuration obj
	 */
	DTESPConfigObj config_obj;
	

	
	public void SetConfigObj(DTESPConfigObj co)
	{
		config_obj=co;
	}
	 
    
/**
 * <pre>
 * Start service
 *  
 * !Do SetConfigObj before you run!
 * 
 * 1. Initialize data turbine
 * 2. Initialize Esper
 * 3. Start Fetching data 
 */
    public void run()
    {
    	Init_DT();
    	Init_Esper();
    	Fetch();
    }
    
    /**
     * <pre>
     * Prepare DT connection. Create and connect source, source channel, sink, and sink channel
     * Data will be saved in DT by <SaveData> node.  
     */
    
    public void Init_DT()
    {
    	
        
        // Create source
        for (SourceItem s : config_obj.hmap_source_item.values())
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
        for (SourceChannelItem c:config_obj.hmap_source_channel_item.values())
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
        for (SinkItem s : config_obj.hmap_sink_item.values())
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
			        if (config_obj.bSubscribe)
			        	// if realtime subscribe
			        	s.sink.Subscribe(s.cmap);

		        }
		        catch (SAPIException se) {
		            System.out.println("Error adding to channel map!");
		            return;
		        }
	        }
	        
        }
        
        
        // save sample data
        {
        
	        Iterator<SaveDataItem> i=config_obj.list_save_data_item.iterator();
	        
	        while (i.hasNext())
	        {
	        	SaveDataItem s=i.next();
	        	
	        	if (s.time_to_insert>0) continue;
	        	
	        	// send sample data to DT
	        	SendToDT(s.list_data,s.list_time,s.sci);
	        	
	        	// remove it from list
	        	i.remove();
	        }
        }
        
        

        // not using subscribe
		if (!config_obj.bSubscribe)
		{
	        // load start time if we need to fetch from the start
			if (config_obj.request_start==-1)
			{
				double oldest_time=-1;
		        try 
		        {
			        for (SinkItem s : config_obj.hmap_sink_item.values())
				        if (!s.channel_item_list.isEmpty())
					        s.sink.Request(s.cmap, 0,0, "oldest");
				        
					
					// fetch from all the channel
					for (SinkItem s:config_obj.hmap_sink_item.values())
					{
						ChannelMap outmap = s.sink.Fetch(1000);
				
						if (outmap.GetIfFetchTimedOut())	continue;
				    
						for (SinkChannelItem c:s.channel_item_list)
						{
				            int chanIdx = outmap.GetIndex(c.channel_string);
				           
				            if(chanIdx >= 0)
				            {
				                double[] data_time = outmap.GetTimes(chanIdx);
				                
				                
				                // set to lastest time
				                if (oldest_time>=data_time[0] || oldest_time==-1)
				                	oldest_time=data_time[0];
				            }
						}
					}
						
		        }
		        catch (SAPIException se) {
		            System.out.println("Error finding oldest time");
		            return;
		        }
		        
		        config_obj.request_start=oldest_time;
		        
			}
		}
        
        
		
		
        // if we need to copy sink to a source
        // Create sink        
        for (SinkItem s : config_obj.hmap_sink_item.values())
        {
        	if (s.copy_to_source==null) continue;
        	
	        
        	// Create sink channel		        	
	        for (SinkChannelItem c:s.channel_item_list)
	        {
	        	System.out.println("Adding Copy to SourceChannel"+c.name+": "+c.channel_string);
	        	
	        	SourceChannelItem src_c=new SourceChannelItem("Copy_"+c.name,s.copy_to_source,c.channel_string,null,false);
	        	c.copy_to_source_channel=src_c;
		        try 
		        {
		            src_c.channel_index = src_c.source_item.cmap.Add(src_c.channel_string);
		        }catch (SAPIException se) {
		            System.out.println("Error adding to channel map!");
		            return;
		        }
	        }
	        
        }		

        
        


        

     

    }

        
        
    /**
     * Send data to Data Turbine 
     * @param data			array of data (double[])
     * @param time			array of time (double[])
     * @param source_channel_item	source channel object(SourceChannelItem)
     */

    void SendToDT(double []data, double []time, SourceChannelItem source_channel_item)
    {
       	ChannelMap 	output_cmap		=source_channel_item.source_item.cmap;
	    Source 		source			=source_channel_item.source_item.source;
	    int			channel_index	=source_channel_item.channel_index;
	    try
	    {
    	    
    	    // On nees, we assume that octet-stream data is double-precision float
 	    	output_cmap.PutTimes(time);
    	    output_cmap.PutMime(channel_index, "application/octet-stream");
    	    output_cmap.PutDataAsFloat64(channel_index, data);
    	    
    	    source.Flush(output_cmap, false);
    	}
	    catch (SAPIException mse) 
    	{
    	    System.out.println("Error saving data!");
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
     *  Start time of the request
     */	
	double current_request_start;
    /** 
     * Last time of esper we set 
     */	
	long last_saved_esper_time;
	
	
    /** 
     * until when all input channel has been received 
     */	
	double time_all_channel_received;

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
		epRuntime = epService.getEPRuntime();
		
		// (If not real time)Set esper time
		if (!config_obj.bSubscribe)
		{
			current_request_start=config_obj.request_start;
			// external time mode
			epRuntime.sendEvent(new TimerControlEvent(TimerControlEvent.ClockType.CLOCK_EXTERNAL));

			// esper time = time of requested data 
			last_saved_esper_time=new Double(current_request_start).longValue();
			epRuntime.sendEvent(new CurrentTimeEvent(last_saved_esper_time));
			
		}

		// Register event that will be sent to esper
		for (EventItem ei:config_obj.hmap_event_item.values())
		{		
        	System.out.println("Creating Event "+ei.name+": field "+ei.field);
			
	        Map<String, Object> definition = new LinkedHashMap<String, Object>();
	        definition.put(ei.field, double.class);
	        epService.getEPAdministrator().getConfiguration().addEventType(ei.name, definition);
		}
		
		// Create query and register listener if we want to 
		for (QueryItem qi:config_obj.list_query_item)
		{		
        	System.out.println("Adding Query "+qi.name+": "+qi.query_string);
			EPStatement statement = epService.getEPAdministrator().createEPL(qi.query_string);
			
			// if we are going to sent to some source
			if (qi.source_channel_item!=null)
				statement.addListener(new EsperEventListener(qi.source_channel_item,this));			
		}
		
    }
        

    void SetTimeAllChannelReceived() throws SAPIException
    {
        for (SinkItem s : config_obj.hmap_sink_item.values())
	        if (!s.channel_item_list.isEmpty())
		        s.sink.Request(s.cmap, 0,0, "newest");
	        
		
	    time_all_channel_received=-1;
	    Boolean received_from_all_channel=true;
		// fetch from all the channel
		for (SinkItem s:config_obj.hmap_sink_item.values())
		{
			ChannelMap outmap = s.sink.Fetch(1000);
	
			if (outmap.GetIfFetchTimedOut())
			{
	        	received_from_all_channel=false;
	        	continue;
			}
	    
			for (SinkChannelItem c:s.channel_item_list)
			{
	            int chanIdx = outmap.GetIndex(c.channel_string);
	           
	            if(chanIdx >= 0)
	            {
	                double[] data_time;
	                data_time = outmap.GetTimes(chanIdx);
	                
	                
	                // set to lastest time
	                if (c.last_data_time<=data_time[0])
	                	c.last_data_time=data_time[0];
	
	                if (time_all_channel_received>=data_time[0] || time_all_channel_received==-1)
	                	time_all_channel_received=data_time[0];                
	            }
	            else
	            	received_from_all_channel=false;
			}
		}
			
		if (!received_from_all_channel)
			time_all_channel_received=-1;
		
		
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


    	
//    	{
//    		
//    		double [] data=new double[100];
//    		double [] time=new double[100];
//    		
//    		int k;
//    		for (k=0;k<data.length;k++)
//    			time[k]=k;
//    		
//    		
//        Iterator<SaveDataItem> i=config_obj.list_save_data_item.iterator();
//        
//        while (i.hasNext())
//        {
//        	SaveDataItem s=i.next();
//
//        	Random r=new Random();
//    		for (k=0;k<data.length;k++)
//    			data[k]=r.nextDouble();
//        	
//        	double []dd={0};
//        	double []dt={0};
//        	
//        	// send sample data to DT
//    		for (k=0;k<data.length;k++)
//    		{
//    			dd[0]=data[k];
//    			dt[0]=time[k];
//    			SendToDT(dd,dt,s.sci);
//    		}
//        	
//        	
//        	// remove it from list
//        	i.remove();
//        }
//    	}
//    	
//    	try
//    	{
//    		while (true)
//    			Thread.sleep(100);
//    	}
//    	catch (Exception e)
//    	{
//    		
//    	}
    	
    	
        try 
        {
            
            System.out.println("start fetching data...");
  
            
        	
            
        	ReceivedDataSortedByTime sorted_rd_list			= new ReceivedDataSortedByTime();
        	
        	



        	if (!config_obj.bSubscribe)
        		SetTimeAllChannelReceived();
        	int retry_times=0;
        	double next_request_start=0;
        	
        	
        	long start_tick=System.currentTimeMillis();
        	long duration_not_to_include=0;
        	

        	// cleared after client received first request
        	Boolean b_first_request=true;
        	
        	
        	// main loop starts
            while (true) 
            {
            	long current_tick=System.currentTimeMillis();

            	
            	// if time_to_insert ms is passed, insert sample data
            	{
	                Iterator<SaveDataItem> i=config_obj.list_save_data_item.iterator();
	                
	                while (i.hasNext())
	                {
	                	SaveDataItem s=i.next();
	                	
	                	if (s.time_to_insert>current_tick-start_tick) continue;
	                	
	                	// send sample data to DT
	                	SendToDT(s.list_data,s.list_time,s.sci);
	                	
	                	// remove it from list
	                	i.remove();
	                }
            	}
            	// don't include time for sending the sample data
            	duration_not_to_include+=System.currentTimeMillis()-current_tick;
            	
            	
            	
            	// request data for next window if not subscribe
            	if (!config_obj.bSubscribe)
            	{
		        	//fetch only until all the data is received
		        	double duration=config_obj.request_duration;
		        	if (current_request_start+config_obj.request_duration>time_all_channel_received)
		        	{
		        		duration=time_all_channel_received-current_request_start;
		        	}
		        	
		        	if (current_request_start+config_obj.request_duration>config_obj.end_time && config_obj.end_time!=-1)
		        	{
		        		duration=config_obj.end_time-current_request_start;
		        	}
		        	
	        		next_request_start=current_request_start+duration;
		        	
		        	
		        	// if no data to fetch
		        	if (duration<=0)
		        	{
		        		if (current_request_start+duration>=config_obj.end_time && config_obj.end_time!=-1)
		        		{
                    		System.out.println("End of request range");
                    		while (true)
                    		{
                    			try {Thread.sleep(1000);} catch (Exception e) {}
                    		}
		        			
		        		}
		        		
		        		if (retry_times==0 && config_obj.output_level<4)
                    		System.out.println("waiting for data .. Duration of operation "+ 	(current_tick-start_tick-duration_not_to_include)/1000);	        				
		        		
		            	current_tick=System.currentTimeMillis();

		        		if (retry_times>0)
		        		{
		        			// sleep 100 ms before finding out the last time of all channels again
		        			retry_times=1;
		        			try
		        			{
		        				Thread.sleep(100);
		        			}
		        			catch (Exception e)
		        			{
		        			}
		        		}
		        		
		        		// finding out the last time of all channels
		        		SetTimeAllChannelReceived();

		        		if (retry_times!=0)
		                	duration_not_to_include+=System.currentTimeMillis()-current_tick;
		        		
		        		retry_times++;
		        		continue;
		        	}
		        	else
		        		retry_times=0;

		        	// we request data
	                for (SinkItem s : config_obj.hmap_sink_item.values())
	        	        if (!s.channel_item_list.isEmpty())
       			        	s.sink.Request(s.cmap, current_request_start,duration, "absolute");
	        	        
            		sorted_rd_list.Clear();
            	}
            	
            	
            	// fetch all the data
            	for (SinkItem s:config_obj.hmap_sink_item.values())
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
    	                    
    	                    ReceivedDataFromChannel rd= new ReceivedDataFromChannel();
    	                    
    	                    rd.sink_channel=c;
    	                    rd.data=data;
    	                    rd.data_time=data_time;
    	                    
    	                    // add the earliest time of data of the channel and data
    	                    sorted_rd_list.Add(rd.GetTime(), rd);
    	                    
    	                    
    	                    if (c.last_data_time<=rd.GetLastTime())
    	                    	c.last_data_time=rd.GetLastTime();
    	                    
    	                    
    	                    
//    	                    // copy to source
//    	                    
//    	                    if (c.copy_to_source_channel!=null)
//    	                    {
//    	                    	SendToDT(data,data_time,c.copy_to_source_channel);
//    	                    }

    	                }
    	               
            		}
            		
            	}
            	
            	
            	
            	
            	
            	
            	// all the channel is received and until list is empty. this means until every data is sent 
            	while (!sorted_rd_list.IsEmpty())
            	{
            		// first received data would be data with earliest data
            		ReceivedDataFromChannel rd=sorted_rd_list.GetFirstRd();
            		
            		
            		// get the data out
            		double data=rd.GetData();
            		double data_time=rd.GetTime();
            		SinkChannelItem c=rd.sink_channel;
            		
                    if (data_time>time_all_channel_received && config_obj.bSubscribe)
                    {
                    	if (config_obj.output_level<3)
                    		System.out.println("!declined DT "+ c.name +" : " + data+ " @ " + current_request_start+ " "+ data_time);
                    	break;
                    }

                    sorted_rd_list.RemoveFirst();

            		//to eliminate duplicated data from last request
                    if (data_time==current_request_start && !b_first_request)       continue;
                    

                    
                    //if copy to
                    if (c.copy_to_source_channel!=null)
                    {
//                    	double []d={1+new Random().nextDouble()};
                    	double []d={data};
                    	double []t={data_time};
                    	SendToDT(d, t, c.copy_to_source_channel);
                    	
                    	try {
                        	FileWriter outFile = new FileWriter(config_obj.config_name+"s.txt",true);
//                        	PrintWriter out = new PrintWriter(outFile);
            	            
                        	String line=" "+c.name+" : "+data+" : "+data_time;
                        	BufferedWriter writer = new BufferedWriter(outFile);
                        	
                        	writer.write(line);
                        	writer.newLine();
                        	
                       
//                        	outFile.write(" "+c.name+" : "+data+" : "+data_time);
                        	
                        	writer.flush();
                        	writer.close();
                        	}
                        	catch (Exception e)
                        	{
                        	}
                        	
                    }


                	try {
                    	FileWriter outFile = new FileWriter(config_obj.config_name+"a.txt",true);
//                    	PrintWriter out = new PrintWriter(outFile);
        	            
                    	String line=" "+c.name+" : "+data+" : "+data_time;
                    	BufferedWriter writer = new BufferedWriter(outFile);
                    	
                    	writer.write(line);
                    	writer.newLine();
                    	
                   
//                    	outFile.write(" "+c.name+" : "+data+" : "+data_time);
                    	
                    	writer.flush();
                    	writer.close();
                    	}
                    	catch (Exception e)
                    	{
                    	}
                    
                                        
                    
                	if (config_obj.output_level<2)
                		System.out.println("DT "+ c.name +" : " + data+ " @ " + current_request_start+ " "+ data_time);

                    // send new time to esper 
                    
                    
                    long l_data_time=new Double(data_time).longValue();
                    if (last_saved_esper_time<l_data_time)
                    {
                    	// to make time advancement less than maximum time granuality 
                    	long time_advancement=l_data_time-last_saved_esper_time;
                    	if (time_advancement>config_obj.maximum_time_granuality)
                    	{
                    		long temp_time=last_saved_esper_time;
                    		for (;temp_time+config_obj.maximum_time_granuality<data_time;)
                    		{
                    			temp_time+=config_obj.maximum_time_granuality;
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
                    	sorted_rd_list.Add(rd.GetTime(),rd);
                    }
                    else
                    	rd.Clear();
            	}    
                	
            	
            	if (!config_obj.bSubscribe)
            	{
            		current_request_start=next_request_start;
            	}
            		
            	b_first_request=false;
            		
            }
        }catch (SAPIException mse) 
        {
            System.out.println("Error reading data!");
        }


        // close connection
        for (SourceItem si:config_obj.hmap_source_item.values())
        {
        	if (si.source.VerifyConnection())
        		si.source.CloseRBNBConnection();
        }
        for (SinkItem si:config_obj.hmap_sink_item.values())
        {
        	if (si.sink.VerifyConnection())
        		si.sink.CloseRBNBConnection();
        }
        System.out.println("Done, exiting.");
        
        return;
    }
}








