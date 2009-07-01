package dtesp;



import java.util.*;


/**
* <pre>
 * 
 * Temporary class needed for processing data in time order
 * This implements a sorted linked list of time and ReceivedDataFromChannel pair in time order.
 * So first node will be ReceivedDataFromChannel class with earliest data.
 * We add all the first data and ReceivedDataFromChannel class into the sorted linked list. 
 * And we process data in the channel whose index is first node in the sorted linked list. 
 * 
 */

public class ReceivedDataSortedByTime
{
	// sorted linked list of time in ascending order of time
	LinkedList<Double>					time_list	=new LinkedList<Double>();						
	
	

	// hashmap to access list of ReceivedDataFromChannel by its channel name
	HashMap<String, ReceivedDataListFromChannel>	channel_hash_map=new HashMap<String, ReceivedDataListFromChannel>();		 
	
	// sorted linked list of ReceivedDataFromChannel in ascending order of time
	// one node of LinkedList contains List of ReceivedDataFromChannel for one channel
	LinkedList<ReceivedDataListFromChannel>			rd_list			=new LinkedList<ReceivedDataListFromChannel>();		 
	
		
	public void Clear()
	{
		rd_list.clear();
		time_list.clear();
	}
	
	/**
	 * <pre>
	 * Add time and index. List will be sorted in time order
	 */
	public void Add(double time, ReceivedDataFromChannel rd)
	{
		if (channel_hash_map.containsKey(rd.sink_channel_name))
		{
			// the channel is already in the list, append to it in the end
			channel_hash_map.get(rd.sink_channel_name).add(rd);
			return;
		}
		
		// it's new channel, create list for the channel
		ReceivedDataListFromChannel l=new ReceivedDataListFromChannel();
		l.add(rd);
		Add(l);
		channel_hash_map.put(rd.sink_channel_name, l);
	}

	/**
	 * <pre>
	 * Add ReceivedDataSortedByTime
	 * 
	 * (*) Assumes ReceivedDataSortedByTime has at most one ReceivedData for each channel ( one fetch )
	 */
	public void Add(ReceivedDataSortedByTime rsl)
	{
		for (ReceivedDataListFromChannel rl: rsl.rd_list)
			Add(rl.GetFirstTime(),rl.getFirst());
	}
	
	/**
	 * <pre>
	 * Add ReceivedDataListFromChannel
	 * 
	 * Doesn't assume that the ReceivedDataListFromChannel for the channel already exists
	 */
	protected void Add(ReceivedDataListFromChannel rl)
	{
		double time=rl.GetFirstTime();
		int index=0;
		ListIterator<Double> iter							=time_list.listIterator();
		ListIterator<ReceivedDataListFromChannel> iter_rd	=rd_list.listIterator();
		for (;iter.hasNext();)
		{
			iter_rd.next();
			if (time<iter.next())
			{
				iter.previous();
				iter_rd.previous();
				
				iter.add(time);
				iter_rd.add(rl);
				return;
			}
		index++;
		}
		time_list.addLast(time);
		rd_list.addLast(rl);
		}
	
	

	/**
	 * <pre>
	 * Return the ReceivedDataFromChannel whose data is earliest
	 * @return channel index
	 */
	public ReceivedDataFromChannel GetFirstRd()
	{
		ReceivedDataFromChannel rd=rd_list.getFirst().getFirst();
		return rd;
	}
	
	public void RemoveFirstElementAndSort()
	{
		ReceivedDataListFromChannel rl=rd_list.getFirst();

		
		time_list.removeFirst();
		
		channel_hash_map.remove(channel_hash_map.get(rl.GetChannelName()));
	
		rl.RemoveFirstData();
		rd_list.removeFirst();
		
		if (!rl.isEmpty()) Add(rl);
	}

	/**
	 * return min time of last data received time of all channels
	 */
	
	public double GetMinTimeOfLastTimeOfAllChannels()
	{
		double lasttime=-1;
		for (ReceivedDataListFromChannel rl: rd_list)
			if (lasttime==-1 || rl.GetLastTime()<lasttime) lasttime=rl.GetLastTime();
		return lasttime;
	}
	
	
	
	public boolean IsEmpty()
	{
		return time_list.isEmpty();
	}
	
	
//	static public void Test()
//	{
//		ReceivedDataFromChannel 	[] r	= new ReceivedDataFromChannel[8];
//		ReceivedDataListFromChannel [] rl	= new ReceivedDataListFromChannel [4];
//		ReceivedDataSortedByTime 	[] sl	= new ReceivedDataSortedByTime[2];
//		
//		SinkChannelItem []sci= new SinkChannelItem[2];
//		SinkItem si=new SinkItem("","","",null);
//		
//		int i;
//		for (i=0;i<8;i++) r[i]=new ReceivedDataFromChannel();
//		for (i=0;i<4;i++) rl[i]=new ReceivedDataListFromChannel();
//		for (i=0;i<2;i++) sl[i]=new ReceivedDataSortedByTime();
//		for (i=0;i<2;i++) sci[i]=new SinkChannelItem(Integer.toString(i),si,"",null);
//		
//		
//		
//		sci[0].name="1";
//		sci[1].name="2";
//
//		
//		for (i=0;i<8;i++)
//		{
//			r[i].data		=new double []{i};
//			r[i].data_time	=new double []{i};
//			
//			r[i].sink_channel=sci[i%2];
//			
//			sl[i/4].Add(r[i].GetTime(), r[i]);
//		}
//		
//		
//		
//		sl[0].Add(sl[1]);
//		
//		while (!sl[0].IsEmpty())
//		{
//	        System.out.println(""+sl[0].GetFirstRd().GetData()+" "+sl[0].GetFirstRd().GetTime());
//	        sl[0].RemoveFirstElementAndSort();
//		}	
//	}
};