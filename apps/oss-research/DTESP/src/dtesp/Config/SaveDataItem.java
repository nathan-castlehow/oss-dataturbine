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
		
	
	
	/**
	 * Parse xml string to data format. format data1@time1;data2@time2
	 */
	
	public double[][] ParseData()
	{
		String []st=text.trim().split(";");

		double []data = new double[st.length];
		double []time = new double[st.length];
		
		int i=0;
	     for (String s_:st) 
	     {
	    	 String []r=s_.split("@");
	    	 double d=0,t=Double.parseDouble(r[1]);
	    	 if (r[0].isEmpty())
	    		 d=t;
	    	 else
	    		 d=Double.parseDouble(r[0]);
	    	
	    	 data[i]=d;
	    	 time[i]=t;
	    	
	    	 i++;
	     }
	     
	     
	     return new double [][]{data,time};
	     }
	
	
	/**
	 * Make random data with specific duration, hertz, and maximum
	 */
	
	public double[][] MakeData(int duration, double hertz, int max)
	{
		int i;

		
		
		double t=0,T=1/hertz;
		Random r=new Random();
		int n;
		n=(int)(duration*hertz);

	
		double []data = new double[n];
		double []time = new double[n];
		
		
		
		for (i=0;i<n;i++)
		{
			int d=r.nextInt(max);
			data[i]=(double)d;
			time[i]=t;
			
			t+=T;
		}
		
	
		
		return new double [][]{data,time};
	}	
	public String							source_channel_name;
	public String							text;
	public long								time_to_insert=-1;
	
	
};	
