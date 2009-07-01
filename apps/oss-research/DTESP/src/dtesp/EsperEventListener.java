package dtesp;
import dtesp.Config.*;
import com.rbnb.sapi.*;
import com.espertech.esper.client.*;


    /**
     * <pre> Listener that listens to an esper event and sends to dt
     */
    public class EsperEventListener implements UpdateListener
    {
    	SourceChannelItem	source_channel_item;
    	String				field_name;
    	SourceStub			source;
    	EsperStub 			dtr;

        public EsperEventListener(SourceChannelItem	source_channel_item_, String	field_name_, SourceStub source_, EsperStub r)
        {
        	source_channel_item=source_channel_item_;
        	field_name=field_name_;
        	source=source_;
        	dtr=r;
        }
        
        public void SendToDT()
        {
        	if (has_data)
        	{

        		source.SendToDT(data, time, source_channel_item.name);

        	    int index=0;
        	    if (data.length==2) index=1;
	    		last_data=data[index];
	    		last_time=time[index];
	    		
	    		
	    		has_data=false;
        	}
        }
        
        
        Boolean 			has_data=false;

	    double[]          	data;
    	double[] 			time;
    	
    	double last_time=-1;			// save last time and data for bar graph form
    	double last_data=0;

    	
        
    	public void update(EventBean[] newEvents, EventBean[] oldEvents) 
    	{
    	    EventBean event=newEvents[0];		// just first event because DT can only save one value not multiple values


	    	
	    	

	    	double v=Double.parseDouble(event.get(field_name).toString());
    	    
    	    if (dtr.config_obj.output_level<3)
    	    	System.out.println("E "+source_channel_item.name+" : " + v);


    	    
    	    
    	    // if case of saving to bar graph form 
	    	if (source_channel_item.is_zero_one_graph  && last_time!=-1)
	    	{
	    		data=new double[2];
	    		time=new double[2];

	    		data[0]=last_data;
	    		data[1]=v;
	    		
	    		time[0]=((double)dtr.last_saved_esper_time-1)/1000;
	    		time[1]=((double)dtr.last_saved_esper_time  )/1000;
	    	}
	    	else
	    	{
	    		data=new double[1];
	    		time=new double[1];
	    		
	    	    data[0] = v ;
	    	    time[0]=((double)dtr.last_saved_esper_time)/1000;
	    	}
    	    
    	    
    	    

	    	has_data=true;
    	    
    	}    	
    }
