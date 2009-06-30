package dtesp.tests;



import java.util.*;

import com.rbnb.sapi.*;



 
public class SaveToDT {
	
	
static public void SaveDT(ChannelMap cmap, int chanIndex ,Source source, double [] data_,double [] time)
{
	try {
		cmap.PutTimes(time);
		
	    cmap.PutMime(chanIndex, "application/octet-stream");
	    cmap.PutDataAsFloat64(chanIndex, data_);
	    
	    source.Flush(cmap, true);
	}catch (SAPIException se) {
		System.out.println("Error adding to channel map!");
	}
	
	
}

	
	
	
	

    public static void main (String args[])
    {
        Source          source;
        ChannelMap      cmap;
        String          source_name = "S1";
        String          channel_name = "C1";
        String          host_string = "localhost";
        int             chanIndex = 0;
        boolean         connected = false;
        double[]          data = {42.0};
        
        System.out.println("Starting connection to Turbine...");
        
        try
        {
//            source = new Source(100,"append",100); 	
            source = new Source(); 	
            source.OpenRBNBConnection(host_string, source_name);
        
            cmap = new ChannelMap();
            connected = true;
        
        } catch (SAPIException se) {
            System.out.println("Error on Turbine - not connected");
            connected = false;
            return;
        }
        
        
        
        
    
        System.out.println("Turbine connected, creating channel map and source");
        
        // The channel map is constant once set up, so only do it once
        try 
        {
            chanIndex = cmap.Add(channel_name);
            
        }catch (SAPIException se) {
            System.out.println("Error adding to channel map!");
            return;
        }
        

        
        Random r=new Random();
        int i;
        for (i=0;i<10000;i++)
        {
        	double []data_=new double[1];
        	double []time=new double[1];
        	
        	data_[0]=i%1000;
        	time[0]=i*100;
        	
        	SaveDT(cmap, chanIndex ,source, data_,time);
        	
        	if (i%100==0)
        	{
        		int k=0;
        	}
        	
        	try
        	{
        	}
        	catch (Exception e)
        	{
        		
        	}        	
        }

        
        
    	try
    	{
    		while (true)
    			Thread.sleep(100);
    	}
    	catch (Exception e)
    	{
    		
    	}




        if(connected) 
        {
            System.out.println("Closing RBNB connection");
            source.CloseRBNBConnection();
        }
        System.out.println("Done, exiting.");
       
        return;
    }
}
