/*
 * Created on Feb 23, 2005
 * (Copied from TextChannelList.jave and modified)
 */
package org.nees.rbnb;

import com.rbnb.sapi.*;

import java.util.Vector;

/**
 * @author Terry E Weymouth
 *
 */
public class ChannelListArray {

	Sink sink = null;

	boolean connected = false;
		
	String error = "none";

	String[] channelList = new String[0];
	String[] mimeType = new String[0];

	public static void main(String[] args)
	{
		// to test
		
		ChannelListArray cl = new ChannelListArray();
		if (!cl.connect(args[0])) return;
		cl.setupArrays();	
		String[] names = cl.fetchNameArray();
		String[] types = cl.fetchMimeArray();
		for (int i = 0; i < names.length; i++)
		System.out.println(names[i] + ": " + types[i]);
	}
		
	public void setupArrays()
	{
		Vector collect = new Vector();
		try
		{
			// get registration map
			sink.RequestRegistration();
				
			// look over channel map metadata
			ChannelMap cmap = sink.Fetch(0);
			ChannelTree ctree = ChannelTree.createFromChannelMap(cmap);
			String[] channels = cmap.GetChannelList();
			for (int i=0; i<channels.length; i++) {
				String channelName = channels[i];
				if (channelName.startsWith("_")) continue;
				ChannelTree.Node node = ctree.findNode (channelName);
				if (node != null)
				{
					ChannelTree.NodeTypeEnum type = node.getType();
					if (type == ChannelTree.CHANNEL) {
						String mime = node.getMime();
						collect.add(new Holder(channelName,mime));
					}
				}
			}
			channelList = new String[collect.size()];
			mimeType = new String[collect.size()];
			for (int i = 0; i < collect.size(); i++)
			{
				channelList[i] = ((Holder)collect.elementAt(i)).name;
				mimeType[i] = ((Holder)collect.elementAt(i)).type;
			}
		}
		catch( Throwable t)
		{
			error = t.toString();
		}	
	}

	public String[] fetchNameArray()
	{
		return channelList;
	}
	
	public String[] fetchMimeArray()
	{
		return mimeType;
	}

	public boolean connect(String server)
	{
		connected = false;
		try {
			// Create a sink and connect:
			sink=new Sink();
			sink.OpenRBNBConnection(server,"ChannelListRequest");
			connected = true;
		} catch (SAPIException se) {
			error = se.toString(); 
		}
		return connected;
	}
	
	public void disconnect()
	{
		sink.CloseRBNBConnection();
		connected = false;
	}
	
	public boolean isConnected()
	{
		return connected;
	}	

	private class Holder
	{
		String name;
		String type;
		
		Holder(String n, String t)
		{
			name = n;
			type = t;
		}
	}
}
