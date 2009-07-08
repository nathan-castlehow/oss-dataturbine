package dtesp;
import dtesp.Config.*;


import java.text.DateFormat;
import java.util.*;

import com.rbnb.sapi.*;




/**
 * <pre>
 * Initialize sink connection. Initialize start and end time of fetch.
 * Request and fetch data. If not using subscribe, only fetch until all data is received.
 */



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
     * Prepare DT connection. Create and connect sink, and sink channel
     * Set time to start request. If not subscribe, set when all data has been received.  
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
        

        // set start time of the request 
		if (!config_obj.bSubscribe)
		{
	        // not using subscribe
			if (config_obj.request_start==-1)
		        // set start time to oldest time of input data
				current_request_start=FindOldestTime();
			else
				current_request_start=config_obj.request_start;
		}
		else
	        current_request_start=-1;

		
		// if using subscribe, we don't need last time of data to proceed. We just proceed as we get the data
		if (!config_obj.bSubscribe) 
		{
			UpdateTimeAllChannelReceived();
		}		
    }
    
    /**
     * Find oldest time of all channels
     */
    
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

        

	/**
	 * Find last time of channel over all channels
	 */
	
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
		            	// if data exist
		                double[] data_time;
		                data_time = outmap.GetTimes(chanIdx);
		                
		                
		
		                if (time_all_channel_received>=data_time[0] || time_all_channel_received==-1)
		                	time_all_channel_received=data_time[0];                
		            }
		            else
		            	// no data, fail
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
    	

	/**
	 * last time of the request
	 */
	Boolean IsEndOfTheRequest()
	{
		return current_request_start>=config_obj.end_time && config_obj.end_time!=-1;		
	}
    
    
	/**
	 * get valid request duration 
	 */
    
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
		return true;
	}
	
	/**
	 * Request data
	 */
	void Request()
	{
    	double duration=GetRequestDuration();
   	
    	
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
        
        	        
	}
	

    
    /** 
     * <pre>
     * Fetch data and save in ReceivedDataSortedByTime.
     * If copy_to_source option set, save to source. 
     * 
     */ 
    
    
	
	
    
    
     
    protected ReceivedDataSortedByTime Fetch()
    {
		if (!config_obj.bSubscribe) Request();
	
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
	                
	                // add data
	                sorted_rd_list.Add(rd);
	                
	                
	                
	                
	                
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


    /**
     * CleanUp: Closes connection
     */
    
    void CleanUp()
    {
        // close connection

        for (SinkRuntime si:list_sink.values())
        {
        	if (si.sink.VerifyConnection())
        		si.sink.CloseRBNBConnection();
        }
        
        list_sink.clear();
        
        return;
    }
}








