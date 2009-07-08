package dtesp.Config;

import java.util.Random;

import org.w3c.dom.Element;


/**<pre>
 * Save temporary/sample data to source channel 
 * 
 *  example1:
 *  <SaveData source_channel='sc1' after='1000'>3.0@2;3.2@4</SaveData>
 *  This will save 3.0 at time 2, and 3.2 at time 4. after 1 second is passed at channel sc1
 */
public class 			SaveDataItem
{
	public SaveDataItem(Element e, ConfigObj co) 
	{
		source_channel_name=e.getAttribute("source_channel");
		time_to_insert=Long.parseLong(e.getAttribute("after"));
		text=e.getTextContent().trim();
	}		
					

	/**
	 * Create with parameters
	 * @param duration_				duration of data
	 * @param hertz_				frequency of data
	 * @param max_					maximum number
	 * @param time_to_insert_		time (runtime) to insert data 
	 * @param sci_					channel name to save
	 */
	
	public SaveDataItem(int duration_, int hertz_, int max_, long time_to_insert_, String sci_)
	{
		duration=duration_;
		hertz=hertz_;
		max=max_;
		
		source_channel_name=sci_;
		time_to_insert=time_to_insert_;		
	}

	public String							source_channel_name;
	public String							text;
	public long								time_to_insert=-1;
	public int duration;
	public int hertz;
	public int max;
	
};	
