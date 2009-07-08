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
        String          channel_name2 = "C2";
        String          host_string = "localhost";
        int             chanIndex = 0;
        int             chanIndex2 = 0;
        boolean         connected = false;
        double[]          data = {42.0};
        
        System.out.println("Starting connection to Turbine...");
        
        try
        {
            source = new Source(100,"append",10000); 	
//            source = new Source(); 	
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
            chanIndex2 = cmap.Add(channel_name2);
            
        }catch (SAPIException se) {
            System.out.println("Error adding to channel map!");
            return;
        }
        

        
        Random r=new Random();
        int k,i;
    	double []data_=new double[10000];
    	double []data_2=new double[10000];
    	double []time=new double[10000];

        for (k=0;k<1000;k++)
        {
        for (i=0;i<10000;i++)
        {
        	
        	data_[i]=i%1000;
        	data_2[i]=-i%1000;
        	time[i]=i*100+k*10000*100;
        	
        	
        	
        	try
        	{
        	}
        	catch (Exception e)
        	{
        		
        	}        	
        }
    	SaveDT(cmap, chanIndex ,source, data_,time);
    	SaveDT(cmap, chanIndex2 ,source, data_2,time);
        
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
