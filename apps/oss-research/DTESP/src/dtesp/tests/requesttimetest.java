package dtesp.tests;



import java.text.DateFormat;
import java.util.*;

import com.rbnb.sapi.*;



 
public class requesttimetest {
	
	
static public void SaveDT(ChannelMap cmap, int chanIndex ,Source source, double [] data_,double [] time)
{
	try {
		cmap.PutTimes(time);
		
	    cmap.PutMime(chanIndex, "application/octet-stream");
	    cmap.PutDataAsFloat64(chanIndex, data_);
	    
	    source.Flush(cmap, false);
	}catch (SAPIException se) {
		System.out.println("Error adding to channel map!");
	}
}

	
	static public double Cal2Double(Calendar c)
	{
		double r=c.getTimeInMillis()/1000;
		DateFormat df=DateFormat.getInstance();
		
		System.out.println("End time "+df.format(c.getTime()));

		return r;
	}
	

	static public Calendar ToTime(int year, int month, int date, int hour, int min, int sec)
	{


		
		Calendar c=new GregorianCalendar();
    	c.clear();
    	
		c.set(Calendar.YEAR,year);
		c.set(Calendar.MONTH,month-1);
		c.set(Calendar.DATE,date);
		c.set(Calendar.HOUR,hour);
		c.set(Calendar.MINUTE,min);
		c.set(Calendar.SECOND,sec);
	
		return c;
	}	
	
	
	static public class SendData
	{
		double [] data;
		double [] time;
	}
	
	public static SendData MakeDataForADay(int year, int month, int date)
	{
		SendData a=new SendData();
		a.data= new double [5*4];
		a.time= new double [5*4];
		
		int i,index=0;
		for (i=0;i<24-4-2;i+=4)
		{
			double t=Cal2Double(ToTime(year,month,date,i,0,0));
			a.data[index]=0;
			a.time[index]=t;
			index++;
			t=Cal2Double(ToTime(year,month,date,i+1,0,0));
			a.data[index]=1;
			a.time[index]=t;
			index++;
			t=Cal2Double(ToTime(year,month,date,i+2,0,0));
			a.data[index]=1;
			a.time[index]=t;
			index++;
			t=Cal2Double(ToTime(year,month,date,i+3,0,0));
			a.data[index]=0;
			a.time[index]=t;
			
			index++;
		}
		
		

		return a;
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
            source = new Source(); 	// Default source is 100-frame cache, no archive
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
        

        SendData s;
        
//        double [] t={300,400};
//        
//        s=MakeDataForADay(2000,1,1);
//        SaveDT(cmap, chanIndex ,source, t,t);

        
        s=MakeDataForADay(2000,2,1);
        SaveDT(cmap, chanIndex ,source, s.data,s.time);


        s=MakeDataForADay(2000,2,3);
        SaveDT(cmap, chanIndex ,source, s.data,s.time);
        
        s=MakeDataForADay(2000,7,1);
        SaveDT(cmap, chanIndex ,source, s.data,s.time);
                
        
        s=MakeDataForADay(2001,1,1);
        SaveDT(cmap, chanIndex ,source, s.data,s.time);
        
        
        
        
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
