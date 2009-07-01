package dtesp.Config;

import java.util.Random;

import org.w3c.dom.Element;


/**<pre>
 * Save the data to source channel 
 * 
 *  attributes:
 *  source_channel
 *  after
 *  
 *  example:
 *  <SaveData source_channel='sc1' after='1000'>3.0@2;3.2@4</SaveData>
 *  This will save 3.0 at time 2, and 3.2 at time 4. after 1 second is passed
 */
public class 			SaveDataItem
{
	public SaveDataItem(Element e, ConfigObj co) 
	{
		source_channel_name=e.getAttribute("source_channel");
		time_to_insert=Long.parseLong(e.getAttribute("after"));
		text=e.getTextContent().trim();
	}		
					
	public SaveDataItem(String sci_, String text_, long time_to_insert_)
	{
		source_channel_name=sci_;
		text=text_;
		time_to_insert=time_to_insert_;
	}
	
//	public SaveDataItem(int duration, double hertz, int max, int time_to_insert_, String	sci_) 
//	{
//		source_channel_name=sci_;
//		time_to_insert=time_to_insert_;
//		MakeData(duration, hertz, max);
//	}		
	
	
	/**
	 * Parse xml string to data format. format data1@time1;data2@time2
	 */
	
	public SaveDataRuntime ParseData(String s)
	{
		SaveDataRuntime sd=new SaveDataRuntime();
		String []st=s.trim().split(";");

		sd.data = new double[st.length];
		sd.time = new double[st.length];
		
		int i=0;
	     for (String s_:st) 
	     {
	    	 String []r=s_.split("@");
	    	 double d=0,t=Double.parseDouble(r[1]);
	    	 if (r[0].isEmpty())
	    		 d=t;
	    	 else
	    		 d=Double.parseDouble(r[0]);
	    	
	    	 sd.data[i]=d;
	    	 sd.time[i]=t;
	    	
	    	 i++;
	     }
	     
	     sd.conf=this;
	     return sd;
	}
	
	
	/**
	 * Make random data with specific duration, hertz, and maximum
	 */
	
	public SaveDataRuntime MakeData(int duration, double hertz, int max)
	{
		SaveDataRuntime sd=new SaveDataRuntime();
		int i;
		
		
		double t=0,T=1/hertz;
		Random r=new Random();
		int n;
		n=(int)(duration*hertz);

		sd.data = new double[n];
		sd._time = new double[n];
		
		
		
		
		for (i=0;i<n;i++)
		{
			int d=r.nextInt(max);
			sd.data[i]=(double)d;
			sd.time[i]=t;
			
			t+=T;
		}
		
	
		
		return sd;
	}	
	public String							source_channel_name;
	public String							text;
	public long								time_to_insert=-1;
	
	
};	
