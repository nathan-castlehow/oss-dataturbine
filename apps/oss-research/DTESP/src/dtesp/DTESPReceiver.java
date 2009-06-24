package dtesp;



import java.util.*;



import com.espertech.esper.client.*;
import com.espertech.esper.client.time.CurrentTimeEvent;
import com.espertech.esper.client.time.TimerControlEvent;





public class DTESPReceiver {


	
	
	/**
	 * Configuration obj
	 */
	DTESPConfigObj config_obj;
	

	
	public void SetConfigObj(DTESPConfigObj co)
	{
		config_obj=co;
	}
	 
	
	public void Init(DTESPConfigObj co)
	{
		sorted_rd_list=new ReceivedDataSortedByTime();
		
		SetConfigObj(co);
		Init_Esper();
		
		hashmap_channel_last_timestamp=new HashMap<String, Double> ();
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
     * Last time of esper we set 
     */	
	long last_saved_esper_time;
	

	/**
	 * Buffer for received data
	 */
	ReceivedDataSortedByTime sorted_rd_list;

	
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
//		if (!config_obj.bSubscribe)
		{
			// external time mode
			epRuntime.sendEvent(new TimerControlEvent(TimerControlEvent.ClockType.CLOCK_EXTERNAL));

			
			// esper time = time of requested data 
			last_saved_esper_time=new Double(config_obj.request_start).longValue();
			if (config_obj.request_start!=0 && config_obj.request_start!=-1)
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
        

    	

    
    public HashMap<String, Double> 	hashmap_channel_last_timestamp;
    

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
    
    
    
    protected Boolean Process(ReceivedDataSortedByTime  sorted_rd_list_)
    {
    	
    	if (sorted_rd_list.IsEmpty())
    		sorted_rd_list=sorted_rd_list_;
    	else
    		sorted_rd_list.Add(sorted_rd_list_);

    	
    	// all the channel is received and until list is empty. this means until every data is sent 
    	while (!sorted_rd_list.IsEmpty())
    	{
    		// first received data would be data with earliest data
    		ReceivedDataFromChannel rd=sorted_rd_list.GetFirstRd();
    		
    		
    		// get the data out
    		double data=rd.GetData();
    		double data_time=rd.GetTime();
    		SinkChannelItem c=rd.sink_channel;
    		
            if (data_time>sorted_rd_list.GetMinTimeOfLastTimeOfAllChannels())// && config_obj.bSubscribe)
            {
            	if (config_obj.output_level<3)
            		System.out.println("!declined DT "+ c.name +" : " + data+ " @ " + data_time);
            	return true;
            }

            
            sorted_rd_list.RemoveFirstElementAndSort();
            
            
            
            if (hashmap_channel_last_timestamp.containsKey(rd.sink_channel.name))
            {
        		Double lt=hashmap_channel_last_timestamp.get(rd.sink_channel.name);
            	if (data_time==lt)       
            		continue;
            	hashmap_channel_last_timestamp.remove(lt);
            }
            else
            	hashmap_channel_last_timestamp.put(rd.sink_channel.name, data_time);
            

    		//to eliminate duplicated data from last request  (*) we might not need this because adjustment of duration 
//            if (data_time==current_request_start && !b_first_request)       continue;
            

            
//            //if copy to
//            if (c.copy_to_source_channel!=null)
//            {
//            	double []d={data};
//            	double []t={data_time};
//            	SendToDT(d, t, c.copy_to_source_channel);
//            }


            
                                
            
        	if (config_obj.output_level<2)
        		System.out.println("DT "+ c.name +" : " + data+ " @ " + data_time);

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
    	}    
        return true;
  }
    

}








