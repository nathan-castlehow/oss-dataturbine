package dtesp;
import dtesp.Config.*;


import java.util.*;

import com.rbnb.sapi.*;







public class SourceStub {


	
	
	
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
	 * Runtime list
	 */
	public HashMap<String, SourceRuntime> 			list_source					= new HashMap<String, SourceRuntime>();
	public HashMap<String, SourceChannelRuntime> 	list_source_channel			= new HashMap<String, SourceChannelRuntime>();
	
    
    
    /**
     * <pre>
     * Prepare DT connection. Create and connect source, source channel
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
        	SourceChannelRuntime cr=new SourceChannelRuntime();
        	
        	SourceRuntime sr=list_source.get(c.source_name);
        	cr.source=sr;
        	cr.conf=c;

        	System.out.println("Adding SourceChannel "+c.name+": "+c.channel_string);        	
	        try 
	        {
	            cr.channel_index = sr.cmap.Add(c.channel_string);
	        }catch (SAPIException se) {
	            System.out.println("Error adding to channel map!");
	            return;
	        }
	        
	        list_source_channel.put(c.name,cr);
        }

     
        
        


          

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
     * Send data to Data Turbine 
     * @param data			array of data (double[])
     * @param time			array of time (double[])
     * @param cr			SourceChannelRuntime
     */

    void SendToDT(double []data, double []time, SourceChannelRuntime cr)
    {
       	ChannelMap 	output_cmap		=cr.source.cmap;
	    Source 		source			=cr.source.source;
	    int			channel_index	=cr.channel_index;
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
        
        


    
    
    


    void DT_CleanUp()
    {
        // close connection
        for (SourceRuntime sr:list_source.values())
        {
        	if (sr.source.VerifyConnection())
        		sr.source.CloseRBNBConnection();
        }
        System.out.println("Done, exiting.");
        
        return;
    }
}








