package dtesp;

import java.util.Random;

import dtesp.Config.*;


/**
 * Runtime information of temporary/Sample data to be saved in source
 */
class SaveDataRuntime
{
	public SaveDataRuntime(SaveDataItem sd)
	{
		if (sd.text==null || sd.text.isEmpty())
			CreateData(sd.duration,sd.hertz,sd.max);
		else
			ParseData(sd.text);
		conf=sd;
	}
	double []data;
	double []time;
	
	SaveDataItem conf;
	
	
	
	
	/**
	 * Parse xml string to data format. format data1@time1;data2@time2
	 */
	
	public void ParseData(String text)
	{
		String []st=text.trim().split(";");

		data = new double[st.length];
		time = new double[st.length];
		
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
	     
	     
     }
	
	
	
	/**
	 * Create random data 
	 * @param duration		
	 * @param hertz
	 * @param max
	 */
	public void CreateData(int duration, int hertz, int max)
	{
		int i;

		
		
		double t=0,T=1/(double)hertz;
		Random r=new Random();
		int n;
		n=(int)(duration*hertz);

	
		data = new double[n];
		time = new double[n];
		
		
		
		for (i=0;i<n;i++)
		{
			int d=r.nextInt(max);
			data[i]=(double)d;
			time[i]=t;
			
			t+=T;
		}
		
	}		
	
}
