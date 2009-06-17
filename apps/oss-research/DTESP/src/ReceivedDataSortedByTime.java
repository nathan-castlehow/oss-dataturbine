


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
	LinkedList<Double>	time_list	=new LinkedList<Double>();		// sorted linked list of time in ascending order of time
	LinkedList<ReceivedDataFromChannel>	rd_list	=new LinkedList<ReceivedDataFromChannel>();		// sorted linked list of ReceivedDataFromChannel in ascending order of time 
	
	public void Clear()
	{
		rd_list.clear();
		time_list.clear();
	}
	
	/**
	 * <pre>
	 * Add time and index. List will be sorted in time order
	 * @param time       time
	 * @param ReceivedDataFromChannel     ReceivedDataFromChannel class that represents data from a channel
	 */
	public void Add(double time, ReceivedDataFromChannel rd)
	{
		int index=0;
		ListIterator<Double> iter			=time_list.listIterator();
		ListIterator<ReceivedDataFromChannel> iter_rd	=rd_list.listIterator();
		for (;iter.hasNext();)
		{
			iter_rd.next();
			if (time<iter.next())
			{
				iter.previous();
				iter_rd.previous();
				
				iter.add(time);
				iter_rd.add(rd);
				return;
			}
		index++;
		}
		time_list.addLast(time);
		rd_list.addLast(rd);
	}

	/**
	 * <pre>
	 * Return the ReceivedDataFromChannel whose data is earliest
	 * @return channel index
	 */
	public ReceivedDataFromChannel GetFirstRd()
	{
		ReceivedDataFromChannel rd=rd_list.getFirst();
		return rd;
	}
	
	public void RemoveFirst()
	{
		rd_list.removeFirst();
		time_list.removeFirst();
	}

	
	
	
	public boolean IsEmpty()
	{
		return time_list.isEmpty();
	}
};