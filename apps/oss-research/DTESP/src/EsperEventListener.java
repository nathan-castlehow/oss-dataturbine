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
    	    for (EventBean event:newEvents)
    	    {

        	    double[]          	data={0};
    	    	double[] 			time={0};
    	    	
    	    	
    	    	// if this is channel is a form of a bar graph need to save two values 
    	    	if (source_channel_item.is_zero_one_graph)
    	    	{
    	    		data=new double[2];
    	    		time=new double[2];
    	    	}
    	    	
    	    	
    	    	double v=Double.parseDouble(event.get(source_channel_item.event_item.field).toString());
	    	    
	    	    if (dtesp_.config_obj.output_level<3)
	    	    	System.out.println("E "+source_channel_item.name+" : " + v);
	
	    	    ChannelMap 	output_cmap		=source_channel_item.source_item.cmap;
	    	    Source 		source			=source_channel_item.source_item.source;
	    	    int			channel_index	=source_channel_item.channel_index;
	    	    try
	    	    {
		    	    
			    	if (source_channel_item.is_zero_one_graph)
			    	{
			    		// bar graph form
			    		data[0]= 1-v;
			    	    data[1] = v ;
			    	}
			    	else
			    	    data[0] = v ;
		    	    
					if (dtesp_.config_obj.bSubscribe)
		    	    	// if real time
		    	    	output_cmap.PutTimeAuto("timeofday");
		    	    else
		    	    {
		    	    	// if not, use esper time
		    	    	time[0]=dtesp_.last_saved_esper_time;
		    	    	
				    	if (source_channel_item.is_zero_one_graph)
				    	{
				    		// increment 1
				    		time[1]=Double.longBitsToDouble(Double.valueOf(time[0]).longValue()+1);
				    	}

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
	    	    catch (Exception mse) 
		    	{
		    	}
	    	    
    	    }
    	    
    	}    	
    }
