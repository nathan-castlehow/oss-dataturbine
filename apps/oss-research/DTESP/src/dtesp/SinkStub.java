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
	
	Dtesp 		dtesp;
	SourceStub 	source_stub;

	/**
	 * Initialization
	 * 
	 */
	
	public void Init(ConfigObj co, Dtesp d)
	{
		Init(co,d, null);
	}
	
	public void Init(ConfigObj co, Dtesp d, SourceStub st)
	{
		SetConfigObj(co);
		Init_DT();
		dtesp=d;
		source_stub=st;
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

        

	public    void UpdateTimeAllChannelReceived() 
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
			System.out.println("Error UpdateTimeAllChannelReceived");
		}
			
	}
    	

	Boolean IsEndOfTheRequest()
	{
		return current_request_start>=config_obj.end_time && config_obj.end_time!=-1;		
	}
    
    
    
	double	GetRequestDuration()
	{
    	//fetch only until all the data is received and not after the end time from the configuration (config_obj.end_time)
    	double duration=config_obj.request_duration;
    	if (current_request_start+config_obj.request_duration>time_all_channel_received)
    	{
    		duration=time_all_channel_received-current_request_start;
    	}
    	
    	if (current_request_start+duration>config_obj.end_time && config_obj.end_time!=-1)
    	{
    		duration=config_obj.end_time-current_request_start;
    	}

    	return duration;
	}
    
	Boolean IsDataExistToFetch()
	{
    	double duration=GetRequestDuration();
		
		if (config_obj.bSubscribe) return true;
		if (duration<=0) return false;
		
    	
    	
		try 
		{
	    	// we request data
	        for (SinkRuntime s : list_sink.values())
		        if (!s.channel_list.isEmpty())
		        	s.sink.Request(s.cmap, current_request_start,duration, "absolute");
		}
		catch (SAPIException e)
		{
			System.out.println("Error @IsDataExistToFetch, requsting new data");
		}
		
		
		// advance start time of request 

    	current_request_start=current_request_start+duration;
        
        
        return true;
	        
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
	
    	ReceivedDataSortedByTime sorted_rd_list			= new ReceivedDataSortedByTime();
	
		
		// fetch all the data
		for (SinkRuntime s:list_sink.values())
		{
			ChannelMap outmap;
			try
			{
				outmap = s.sink.Fetch(1000);
			}
			catch (SAPIException e)
			{	
				System.out.println("Error fetching at SinkStub.Fetch()");
				return null;
			}
			
	    
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
	                
	                
	                
                    // copy to source
                    
                    if (s.conf.copy_to_source!=null && !s.conf.copy_to_source.isEmpty())
                    {
                    	source_stub.SendToDT(data,data_time,c.conf.name);
                    }
	
	            }
	           
			}
			
		}
	
		
		return sorted_rd_list;
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








