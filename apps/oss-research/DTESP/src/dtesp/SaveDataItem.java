package dtesp;

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
	public SaveDataItem(Element e, DTESPConfigObj co) 
	{
		sci=co.GetSourceChannel(e.getAttribute("source_channel"));
		time_to_insert=Long.parseLong(e.getAttribute("after"));
		ParseData(e.getTextContent().trim());
	}		
					
	public SaveDataItem(SourceChannelItem sci_, String data_, long time_to_insert_)
	{
		sci=sci_;
		ParseData(data_);
		time_to_insert=time_to_insert_;
	}
	
	/**
	 * Parse xml string to data format. format data1@time1;data2@time2
	 */
	
	void ParseData(String s)
	{
		String []st=s.trim().split(";");

		list_data = new double[st.length];
		list_time = new double[st.length];
		
		int i=0;
	     for (String s_:st) 
	     {
	    	 String []r=s_.split("@");
	    	 double d=0,t=Double.parseDouble(r[1]);
	    	 if (r[0].isEmpty())
	    		 d=t;
	    	
	    	 list_data[i]=d;
	    	 list_time[i]=t;
	    	
	    	 i++;
	     }
	}
	
	SourceChannelItem				sci;
	long							time_to_insert=-1;
	double [] 						list_data;
	double [] 						list_time;
	
	
};	
