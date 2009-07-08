package dtesp;
import dtesp.Config.*;
import com.espertech.esper.client.*;


    /**
     * <pre> Wait for an esper event and sends to data turbine
     */
    public class EsperEventListener implements UpdateListener
    {
    	/**
    	 * Configuration of source channel
    	 */
    	SourceChannelItem	source_channel_item;
    	/**
    	 * Name of the field to pass to data turbime 
    	 */
    	String				field_name;
    	/**
    	 * SourceStub to send data to data turbine
    	 */
    	SourceStub			source;
    	/**
    	 * Esperstub to find out current esper time
    	 */
    	EsperStub 			esper_stub;
    	
    	
    	/**
    	 * Creator
    	 * @param source_channel_item_				Configuration of source channel
    	 * @param field_name_						Name of the field to pass to data turbime
    	 * @param source_							SourceStub to send data to data turbine
    	 * @param r									Esperstub to find out current esper time
    	 */

        public EsperEventListener(SourceChannelItem	source_channel_item_, String	field_name_, SourceStub source_, EsperStub r)
        {
        	source_channel_item=source_channel_item_;
        	field_name=field_name_;
        	source=source_;
        	esper_stub=r;
        }
        
        
        /**
         * Sends data to data turbine using SourceSub
         */
        public void SendToDT()
        {
        	if (has_data)
        	{

        		source.SendToDT(data, time, source_channel_item.name);

	    		
	    		has_data=false;
        	}
        }
        
        
        public Boolean 				has_data=false;
        
        /**
         * is added to send data list
         */
        public Boolean				is_added=false;

	    double[]          	data;
    	double[] 			time;
    	
    	
        
    	/**
    	 * <PRE>
    	 * This function is called if there is new result of the query.
    	 * Get the desired field out and keep it to send in the future
    	 */
    	public void update(EventBean[] newEvents, EventBean[] oldEvents) 
    	{
    	    EventBean event=newEvents[0];		// just first event because DT can only save one value not multiple values


	    	
	    	

	    	double v=Double.parseDouble(event.get(field_name).toString());
    	    
    	    if (esper_stub.config_obj.output_level<3)
    	    	System.out.println("E "+source_channel_item.name+" : " + v);


    	    

    		data=new double[1];
    		time=new double[1];
    		
    	    data[0] = v ;
    	    time[0]=((double)esper_stub.esper_time)/1000;
    	    
    	    
    	    

	    	has_data=true;
	    	
	    	
	    	// if not added, add to send sata list
	    	if (!is_added)
	    	{
	    		esper_stub.list_event_listener_send_data.add(this);
	    		is_added=true;
	    	}
    	    
    	}    	
    }
