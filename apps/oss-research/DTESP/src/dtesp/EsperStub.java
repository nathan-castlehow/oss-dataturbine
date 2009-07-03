package dtesp;
import dtesp.Config.*;


import java.util.*;



import com.espertech.esper.client.*;
import com.espertech.esper.client.time.CurrentTimeEvent;
import com.espertech.esper.client.time.TimerControlEvent;





public class EsperStub {


	
	
	/**
	 * Configuration obj
	 */
	ConfigObj config_obj;
	

	
	public void SetConfigObj(ConfigObj co)
	{
		config_obj=co;
	}
	 
	Dtesp dtesp;
	
	
	public void Init(ConfigObj co, Dtesp d, SourceStub st)
	{
		sorted_rd_list=new ReceivedDataSortedByTime();
		dtesp=d;
		source_stub=st;

		
		SetConfigObj(co);
		Init_Esper();
		
		hashmap_channel_last_timestamp=new HashMap<String, Double> ();
	}
    
  
	/**
	 * list of esper event listener
	 */
	public Vector<EsperEventListener>	list_event_listener=new Vector<EsperEventListener>();

	/**
	 * Source Stub to save result;
	 */
	public SourceStub source_stub;
	
	

	
    
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
			if (config_obj.request_start!=0 && config_obj.request_start!=-1)
				last_saved_esper_time=0;
			else
				last_saved_esper_time=new Double(config_obj.request_start*1000).longValue();
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
			if (!qi.source_channel_name.isEmpty())
			{
		        SourceChannelItem sci=config_obj.hmap_source_channel_item.get(qi.source_channel_name);
				EsperEventListener eel
					=new EsperEventListener(
							sci
							,config_obj.hmap_event_item.get(sci.event_name).field
							,source_stub
							,this);
				statement.addListener(eel);
				list_event_listener.add(eel);
			}
		}
		
    }
        

    	

    /*
     * saving last time data is received for each channel
     */
    
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
    	
    	double last_time=-1;
    	
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
    		String event_name=rd.event_name;
    		
            if (data_time>sorted_rd_list.GetMinTimeOfLastTimeOfAllChannels() && config_obj.bSubscribe)
            {
            	if (config_obj.output_level<3)
            		System.out.println("!declined DT "+ event_name +" : " + data+ " @ " + data_time);
            	return true;
            }

            
            
            
            
            
            sorted_rd_list.RemoveFirstElementAndSort();
            
            
            
            if (hashmap_channel_last_timestamp.containsKey(rd.sink_channel_name))
            {
        		Double lt=hashmap_channel_last_timestamp.get(rd.sink_channel_name);
            	if (data_time==lt)       
            		continue;
            	hashmap_channel_last_timestamp.remove(lt);
            }
            else
            	hashmap_channel_last_timestamp.put(rd.sink_channel_name, data_time);

            
            
            // Send to DT only time has actually advanced
            if (last_time!=data_time)
            	for (EsperEventListener e:list_event_listener)
            		e.SendToDT();

            
            last_time=data_time;
            



            
                                
            
        	if (config_obj.output_level<2)
        		System.out.println("DT "+ event_name +" : " + data+ " @ " + data_time);

            // send new time to esper 
            
            
            long l_data_time=new Double(data_time*1000).longValue();
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
            data_.put(config_obj.hmap_event_item.get(event_name).field, data);
            
            epService.getEPRuntime().sendEvent(data_, rd.event_name);                        

            

            
            // if the channel has more data, push it into list so that rest of the data can be processed
    	}    
    	
    	

    	// send to DT just in case something has not been sent
    	for (EsperEventListener e:list_event_listener)
    		e.SendToDT();
    	
    	
    	
        return true;
  }
    

}








