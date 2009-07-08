package dtesp;

import java.util.LinkedList;



/**
 * <pre>
 * This class is list of  ReceivedDataFromChannel
 * Contains received data list for a channel (not over different channels)
 * Assumes ReceivedDataFromChannel are added in ascending time order because we always fetch data ascending time order   
 */
public class ReceivedDataListFromChannel extends LinkedList<ReceivedDataFromChannel>
{
	void RemoveFirstData()
	{
		// get earliest fetched data (ReceivedDataFromChannel)
		ReceivedDataFromChannel rd=this.getFirst();
		
		// move to next data 
		rd.Next();
		
		// if there is no more data from first ReceivedDataFromChannel, remove it   
		if (rd.IsEmpty())
			this.removeFirst();		
	}
	
	double GetFirstTime()
	{
		if (this.isEmpty())
			return -1;
		return this.getFirst().GetTime();
	}

	double GetLastTime()
	{
		if (this.isEmpty())
			return -1;
		return this.getLast().GetTime();
	}
	
	
	String GetChannelName()
	{
		return getFirst().sink_channel_name;
	}

};    