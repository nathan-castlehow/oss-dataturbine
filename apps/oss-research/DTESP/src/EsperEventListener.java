import com.rbnb.sapi.*;
import com.espertech.esper.client.*;


    /**
     * <pre> Listener that listens to an esper event and sends to dt
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
    	    
    	    for (EventBean event:newEvents)
    	    {
	    	    double v=Double.parseDouble(event.get(source_channel_item.event_item.field).toString());
	    	    
	    	    if (dtesp_.config_obj.output_level<3)
	    	    	System.out.println("E "+source_channel_item.name+" : " + v);
	
	    	    ChannelMap 	output_cmap		=source_channel_item.source_item.cmap;
	    	    Source 		source			=source_channel_item.source_item.source;
	    	    int			channel_index	=source_channel_item.channel_index;
	    	    try
	    	    {
		    	    data[0] = v ;
					if (dtesp_.config_obj.bSubscribe)
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
    }
