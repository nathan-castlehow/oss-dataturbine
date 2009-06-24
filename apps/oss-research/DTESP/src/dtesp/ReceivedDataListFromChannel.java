package dtesp;

import java.util.LinkedList;



/**
 * This class is list of  ReceivedDataFromChannel
 */
public class ReceivedDataListFromChannel extends LinkedList<ReceivedDataFromChannel>
{
	void RemoveFirstData()
	{
		ReceivedDataFromChannel rd=this.getFirst();
		rd.Next();
		
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
		return getFirst().sink_channel.name;
	}

};    