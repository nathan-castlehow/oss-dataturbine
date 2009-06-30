package dtesp;
import dtesp.Config.*;
import dtesp.Config.SourceItem;


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
	

	/**
	 * Initialization
	 * 
	 */
	
	public void Init(ConfigObj co)
	{
		SetConfigObj(co);
		Init_DT();
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
        	System.out.println("Connecting Source "+s.name+": "+s.connection_string+" ("+s.cacheSize+","+s.archiveMode+","+s.archiveSize+")");
	        try
	        {
	            s.source = new Source(s.cacheSize,s.archiveMode,s.archiveSize); 	
	            
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
		        config_obj.request_start=FindOldestTime();
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

        
        


        
		if (!config_obj.bSubscribe)
			current_request_start=config_obj.request_start;
     

    }
    
    
    public double FindOldestTime()
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
            return -1;
        }
        
        
		DateFormat df=DateFormat.getInstance();
		Calendar c=Calendar.getInstance();
		c.setTimeInMillis((long)oldest_time*1000);
		
		System.out.println("Requested time "+df.format(c.getTime()));
     
		return oldest_time;
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
		catch (SAPIException e)
		{
		
		}
			
	}
    	

    
    
    
    void StartMeasure()
    {
    	start_tick=System.currentTimeMillis();
    }
    
	long start_tick;
	long duration_not_to_include=0;
	

    
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

      	
		        	// duration smaller by very little amount to eliminate getting duplicated data   
//		        	double duration_=Double.longBitsToDouble(Double.doubleToLongBits(duration)-1);
		        	// we request data
	                for (SinkItem s : config_obj.hmap_sink_item.values())
	        	        if (!s.channel_item_list.isEmpty())
       			        	s.sink.Request(s.cmap, current_request_start,duration, "absolute");
	        	        
            	}
        		sorted_rd_list.Clear();
            	
            	
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
    	                    
    	                    
    	                    
    	                    // copy to source
    	                    
    	                    if (c.copy_to_source_channel!=null)
    	                    {
    	                    	SendToDT(data,data_time,c.copy_to_source_channel);
    	                    }

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








