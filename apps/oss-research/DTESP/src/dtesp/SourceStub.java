package dtesp;
import dtesp.Config.*;


import java.util.*;

import com.rbnb.sapi.*;






/**
 * SourceStub class for saving data to data turbine
 */

public class SourceStub {


	
	
	
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
	public HashMap<String, SourceRuntime> 			list_source					= new HashMap<String, SourceRuntime>();
	public HashMap<String, SourceChannelRuntime> 	list_source_channel			= new HashMap<String, SourceChannelRuntime>();
	public LinkedList<SaveDataRuntime>				list_save_data				= new LinkedList<SaveDataRuntime>();
    
    
    /**
     * <pre>
     * Prepare DT connection. Create and connect source, and source channel.
     * Prepare source for copy_to_source option and sample/temporary data to save(SaveDataItem)
     */
    
    public void Init_DT()
    {
    	
        
        // Create source
        for (SourceItem s : config_obj.hmap_source_item.values())
        {
        	SourceRuntime sr=new SourceRuntime();
        	
        	System.out.println("Connecting Source "+s.name+": "+s.connection_string+" ("+s.cacheSize+","+s.archiveMode+","+s.archiveSize+")");
	        try
	        {
	            sr.source = new Source(s.cacheSize,s.archiveMode,s.archiveSize); 	
	            
	            // connect!
	            sr.source.OpenRBNBConnection(s.connection_string, s.client);
	        
	            sr.cmap= new ChannelMap();
	            sr.conf=s;
	            
	            list_source.put(s.name, sr);
	        
	        } catch (SAPIException se) {
	            System.out.println("Error on Turbine - not connected");
	            return;
	        }
	        
	        
        }

 
        

        // Attach source channel
        for (SourceChannelItem c:config_obj.hmap_source_channel_item.values())
        {
        	System.out.println(" Adding SourceChannel "+c.name+": "+c.channel_string);        	
        	SourceChannelRuntime cr=new SourceChannelRuntime();
        	
        	SourceRuntime sr=list_source.get(c.source_name);
        	if (sr==null)
        	{
            	System.out.println(" Source not found!");
            	continue;
        	}
        	
        	cr.source=sr;
        	cr.conf=c;

	        try 
	        {
	            cr.channel_index = sr.cmap.Add(c.channel_string);
	        }catch (SAPIException se) {
	            System.out.println("Error adding to channel map!");
	            return;
	        }
	        
	        list_source_channel.put(c.name,cr);
        }

     
        
        // save sample data
        for (SaveDataItem s:config_obj.list_save_data_item)
        {
        	SaveDataRuntime sdr=new SaveDataRuntime(s);
        	
        	if (s.time_to_insert<=0)
        		SendToDT(sdr.data,sdr.time,sdr.conf.source_channel_name);
        	else
        		list_save_data.add(sdr);
        }


        
        
      // Prepare source for copy_to_source option 
      for (SinkItem s : config_obj.hmap_sink_item.values())
      {
    	  // check if the sink has copy_to_source option
      	if (s.copy_to_source==null || s.copy_to_source.isEmpty()) continue;
      	
        for (SinkChannelItem c : config_obj.hmap_sink_channel_item.values())
        {
        	// check for sink channel attached to the sink
        	if (s.name.compareTo(c.sink_name)!=0) continue;
        	
        	// if found
        	System.out.println(" Adding Copy to SourceChannel"+c.name+": "+c.channel_string);
       	
        	
        	SourceChannelRuntime cr=new SourceChannelRuntime();
        	
        	// find source runtime 
        	SourceRuntime sr=list_source.get(s.copy_to_source);
        	cr.source=sr;

        	if (sr==null)
        	{
        		// not found
            	System.out.println(" Source not found!");
            	continue;
        	}
        	
        	// create & copy configuration from source channel configuration
        	SourceChannelItem n_config=new SourceChannelItem(c.name,s.copy_to_source,c.channel_string,"",false);
        	cr.conf=n_config;

        	try 
	        {
	            cr.channel_index = sr.cmap.Add(c.channel_string);
	        }catch (SAPIException se) {
	            System.out.println("Error adding to channel map!");
	            return;
	        }
	        
	        list_source_channel.put(n_config.name,cr);
        }
      }
	        
      
      

    }
    
    
    /**
     * Save temporary/sample data at time defined by configuration
     */
    void ProcessSaveData()
    {

        Iterator<SaveDataRuntime> i=list_save_data.iterator();
        
        while (i.hasNext())
        {
        	SaveDataRuntime s=i.next();
        	
        	// check time
        	if (s.conf.time_to_insert>dtesp.GetTimeFromStart()) continue;
        	// send sample data to DT
        	SendToDT(s.data,s.time,s.conf.source_channel_name);
        	
        	// remove it from list
        	i.remove();
        }
            	
    }
    

    /**
     * <pre>
     * Main function
     * Save temporary/sample data
     */
    Boolean Process()
    {

      ProcessSaveData(); 
      
      return true;
    }
    
    
    /**
     * Send data to Data Turbine 
     * @param data			array of data (double[])
     * @param time			array of time (double[])
     * @param channel_name	name of source channel name
     */
    
    void SendToDT(double []data, double []time, String channel_name)
    {
    	SendToDT(data, time, list_source_channel.get(channel_name));
    }
    

    /**
     * minimum time interval between two data need for making a bar graph form
     */
    final double BAR_GRAPH_PRECISION=0.00001;
    
    /**
     * <pre>
     * Send data to Data Turbine
     * If bar graph option is set, write in bar graph form 
     * @param data			array of data (double[])
     * @param time			array of time (double[])
     * @param cr			SourceChannelRuntime
     */

    void SendToDT(double []data, double []time, SourceChannelRuntime cr)
    {
       	ChannelMap 	output_cmap		=cr.source.cmap;
	    Source 		source			=cr.source.source;
	    int			channel_index	=cr.channel_index;
	    
	    
	    if (cr.conf.is_bar_graph && cr.last_data!=data[0] && !cr.first_write)
	    {
	    	double []data_=new double[data.length+1];
	    	double []time_=new double[data.length+1];
	    	
	    	int i;
	    	for (i=1;i<data.length+1;i++)
	    	{
	    		data_[i]=data[i-1];
//	    		time_[i]=Double.longBitsToDouble(Double.doubleToLongBits(time[i-1])+1);
	    		time_[i]=time[i-1];
	    	}
	    	
	    	data_[0]=cr.last_data;
	    	time_[0]=time[0]-BAR_GRAPH_PRECISION;
//	    	time_[0]=Double.longBitsToDouble(Double.doubleToLongBits(time[0])+1);
	    	
	    	data=data_;
	    	time=time_;
	    	
	    }
	    
	    
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
	    
	    
	    cr.first_write=false;
	    cr.last_data=data[data.length-1];
    }
        
        


    
    
    /**
     * CleanUp:closes connection
     */

    void CleanUp()
    {
        // close connection
        for (SourceRuntime sr:list_source.values())
        {
        	if (sr.source.VerifyConnection())
        		sr.source.CloseRBNBConnection();
        }
        
        list_source.clear();
        list_source_channel.clear();
        return;
    }
}








