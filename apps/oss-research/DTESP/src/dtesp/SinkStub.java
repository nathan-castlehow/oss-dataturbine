package dtesp;
import dtesp.Config.*;


import java.text.DateFormat;
import java.util.*;

import com.rbnb.sapi.*;







public class SinkStub {


	
	
	
	/**
	 * Configuration obj
	 */
	ConfigObj config_obj;

	public void SetConfigObj(ConfigObj co)
	{
		config_obj=co;
	}
	
	Dtesp dtesp;

	/**
	 * Initialization
	 * 
	 */
	
	public void Init(ConfigObj co, Dtesp d)
	{
		SetConfigObj(co);
		Init_DT();
		dtesp=d;
	}
	 

	/**
	 * Runtime list
	 */
	public HashMap<String, SinkRuntime> 			list_sink					= new HashMap<String, SinkRuntime>();
	public HashMap<String, SinkChannelRuntime> 		list_sink_channel			= new HashMap<String, SinkChannelRuntime>();
	

    
    /**
     * <pre>
     * Prepare DT connection. Create and connect source, source channel, sink, and sink channel
     * Data will be saved in DT by <SaveData> node.  
     */
    
    public void Init_DT()
    {
    	
        
        // Create sink        
        for (SinkItem s : config_obj.hmap_sink_item.values())
        {
        	SinkRuntime sr= new SinkRuntime();
        	sr.conf=s;
        	
        	System.out.println("Connecting Sink "+s.name+": "+s.connection_string);
	        try
	        {
	            sr.sink = new Sink(); 	// Default sink is 100-frame cache, no archive
	            // connect!
	            sr.sink.OpenRBNBConnection(s.connection_string, s.client);
	        
	            sr.cmap= new ChannelMap();
	            
	            list_sink.put(s.name,sr);
	        
	        

		        for (SinkChannelItem sci: config_obj.hmap_sink_channel_item.values())
		        {
		        	if (sci.sink_name.compareTo(s.name)==0)
			        {
			            // Create sink channel		        	
			        	System.out.println(" Adding SinkChannel "+sci.name+": "+sci.channel_string);
			        	SinkChannelRuntime scr= new SinkChannelRuntime();
			        	scr.conf=sci;
			        	scr.sink=sr;
			        			        	
			        	
			        	sr.cmap.Add(scr.conf.channel_string);
			        	sr.channel_list.add(scr);
	
			            list_sink_channel.put(sci.name,scr);
			        }
		        }
	        	// if subscribe
		        if (config_obj.bSubscribe)
		        	sr.sink.Subscribe(sr.cmap);
	        
	        } catch (SAPIException se) {
	            System.out.println("Error on Turbine - not connected");
	            return;
	        }

	        
	        
        }
        
        

        
        

        current_request_start=-1;
        // not using subscribe
		if (!config_obj.bSubscribe)
		{
	        // load start time if we need to fetch from the start
			if (config_obj.request_start==-1)
				current_request_start=FindOldestTime();
			else
				current_request_start=config_obj.request_start;
		}
        
        
		
		
//        // if we need to copy sink to a source
//        // Create sink        
//        for (SinkItem s : config_obj.hmap_sink_item.values())
//        {
//        	if (s.copy_to_source.isEmpty()) continue;
//        	
//        	SinkRuntime sr=list_sink.get(s.copy_to_source);
//	        
//        	// Create sink channel		        	
//	        for (SinkChannelRuntime cr:sr.channel_list)
//	        {
//	        	System.out.println("Adding Copy to SourceChannel"+cr.conf.name+": "+cr.conf.channel_string);
//	        	
//	        	cr.copy_to_source_id=
//	        	
//	        	SourceChannelItem src_c=new SourceChannelItem("Copy__"+cr.conf.name,s.copy_to_source,cr.conf.channel_string,"",false);
//	        	
//	        	cr.copy_to_source_channel=src_c;
//		        try 
//		        {
//		            src_c.channel_index = src_c.source_item.cmap.Add(src_c.channel_string);
//		        }catch (SAPIException se) {
//		            System.out.println("Error adding to channel map!");
//		            return;
//		        }
//	        }
//	        
//        }		

        
        


      
     

    }
    
    
    public double FindOldestTime()
    {
		double oldest_time=-1;
        try 
        {
	        for (SinkRuntime sr : list_sink.values())
		        if (!sr.channel_list.isEmpty())
			        sr.sink.Request(sr.cmap, 0,0, "oldest");
		        
			
			// fetch from all the channel
			for (SinkRuntime sr:list_sink.values())
			{
				ChannelMap outmap = sr.sink.Fetch(1000);
		
				if (outmap.GetIfFetchTimedOut())	continue;
		    
				for (SinkChannelRuntime cr:sr.channel_list)
				{
		            int chanIdx = outmap.GetIndex(cr.conf.channel_string);
		           
		            if(chanIdx >= 0)
		            {
		                double[] data_time = outmap.GetTimes(chanIdx);
		                
		                
		                // set to latest time
		                if (oldest_time>=data_time[0] || oldest_time==-1)
		                	oldest_time=data_time[0];
		            }
				}
			}
				
        }
        catch (SAPIException se) {
            System.out.println("Error finding oldest time");
            return -1;
        }
        
        
		DateFormat df=DateFormat.getInstance();
		Calendar c=Calendar.getInstance();
		c.setTimeInMillis((long)oldest_time*1000);
		
		System.out.println("Requested time "+df.format(c.getTime()));
     
		return oldest_time;
    }


        
  

	
	/** 
     *  Start time of the request
     */	
	double current_request_start;
	
	
    /** 
     * until when all input channel has been received 
     */	
	double time_all_channel_received;

        

	public    void SetTimeAllChannelReceived() 
    {

		try
		{
	        for (SinkRuntime s : list_sink.values())
		        if (!s.channel_list.isEmpty())
			        s.sink.Request(s.cmap, 0,0, "newest");
		        
			
		    time_all_channel_received=-1;
		    Boolean received_from_all_channel=true;
			// fetch from all the channel
			for (SinkRuntime s:list_sink.values())
			{
				ChannelMap outmap = s.sink.Fetch(1000);
		
				if (outmap.GetIfFetchTimedOut())
				{
		        	received_from_all_channel=false;
		        	continue;
				}
		    
				for (SinkChannelRuntime c:s.channel_list)
				{
		            int chanIdx = outmap.GetIndex(c.conf.channel_string);
		           
		            if(chanIdx >= 0)
		            {
		                double[] data_time;
		                data_time = outmap.GetTimes(chanIdx);
		                
		                
		                // set to last time
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
		catch (SAPIException e)
		{
		
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
     * @return 
     */ 
    
    
	
	
    
    
     
    protected ReceivedDataSortedByTime Fetch()
    {


    	
    	
        try 
        {
            
  
            
        	
            
        	ReceivedDataSortedByTime sorted_rd_list			= new ReceivedDataSortedByTime();
        	
        	



        	int retry_times=0;
        	double next_request_start=0;
        	
        	
        	
        	
        	
        	// main loop starts
            while (true) 
            {
            	long current_tick=System.currentTimeMillis();




            	
            	

          	
            	// request data for next window if not subscribe
            	if (!config_obj.bSubscribe)
            	{
		        	//fetch only until all the data is received
		        	double duration=config_obj.request_duration;
		        	if (current_request_start+config_obj.request_duration>time_all_channel_received)
		        	{
		        		duration=time_all_channel_received-current_request_start;
		        	}
		        	
		        	if (current_request_start+duration>config_obj.end_time && config_obj.end_time!=-1)
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
                    		System.out.println("waiting for data .. Duration of operation "+dtesp.GetRunningTime());	        				
		        		
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
		                	dtesp.AddWaitDuration(System.currentTimeMillis()-current_tick);
		        		
		        		retry_times++;
		        		continue;
		        	}
		        	else
		        		retry_times=0;

      	
		        	// duration smaller by very little amount to eliminate getting duplicated data   
//		        	double duration_=Double.longBitsToDouble(Double.doubleToLongBits(duration)-1);
		        	// we request data
	                for (SinkRuntime s : list_sink.values())
	        	        if (!s.channel_list.isEmpty())
       			        	s.sink.Request(s.cmap, current_request_start,duration, "absolute");
	        	        
            	}
        		sorted_rd_list.Clear();
            	
            	
            	// fetch all the data
            	for (SinkRuntime s:list_sink.values())
            	{
            		ChannelMap outmap = s.sink.Fetch(1000);
            		
                
            		for (SinkChannelRuntime c:s.channel_list)
            		{
    	                // Look up channel index
    	                int chanIdx = outmap.GetIndex(c.conf.channel_string);
    	               
    	                // If channel index is less than zero, then no data in the fetch                
    	                if(chanIdx >= 0)
    	                {
    	                    double[] data;
    	                    double[] data_time;
    	                    // Note that we have to know how to parse the data type!
    	                    data = outmap.GetDataAsFloat64(chanIdx);
    	                    data_time = outmap.GetTimes(chanIdx);
    	                    
    	                    ReceivedDataFromChannel rd= new ReceivedDataFromChannel();
    	                    
    	                    rd.sink_channel_name=c.conf.name;
    	                    rd.event_name=c.conf.event_name;
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

            	if (!config_obj.bSubscribe)
            	{
            		current_request_start=next_request_start;
            	}
    			try
    			{
    				Thread.sleep(100);
    			}
    			catch (Exception e)
    			{
    			}            		
              	
            	
            	return sorted_rd_list;

            		
            }
        }catch (SAPIException mse) 
        {
            System.out.println("Error reading data!");
        }
		return null;
    }


    void DT_CleanUp()
    {
        // close connection

        for (SinkRuntime si:list_sink.values())
        {
        	if (si.sink.VerifyConnection())
        		si.sink.CloseRBNBConnection();
        }
        System.out.println("Done, exiting.");
        
        return;
    }
}








