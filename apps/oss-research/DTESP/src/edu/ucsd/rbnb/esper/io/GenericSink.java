package edu.ucsd.rbnb.esper.io;

import java.util.Iterator;
import java.util.LinkedList;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.ChannelTree;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Sink;

public class GenericSink {
	
	public static final String	DEFAULT_RBNB_SERVER	= "localhost";
	public static final int		DEFAULT_RBNB_PORT	= 3333;

	private ChannelMap subscribedChannels;
	private Sink sink = new Sink();
	
	public GenericSink(String sinkName) throws SAPIException{
		this(sinkName, DEFAULT_RBNB_SERVER,DEFAULT_RBNB_PORT);
	}
	
	public GenericSink(String sinkName, String server, int port) throws SAPIException{
		sink.OpenRBNBConnection(server+":"+port, sinkName);
	}
	
	public ChannelMap getChannelsMap() throws SAPIException{
		sink.RequestRegistration();
		return sink.Fetch(-1);
	}
	
	public String[] getChannelsList() throws SAPIException{
		return getChannelsMap().GetChannelList();
	}
	
	 
	public String[] gerSourcesList() throws SAPIException{
		@SuppressWarnings("rawtypes")
		Iterator treeIterator = getChannelTree().iterator();
		LinkedList<String> sources = new LinkedList<String>();
		
		while(treeIterator.hasNext()){
			ChannelTree.Node node = (ChannelTree.Node)treeIterator.next();
			if(node.getType() == ChannelTree.SOURCE)
				sources.add(node.getName());
		}
		return sources.toArray(new String[0]);
		
	}
	
	public ChannelTree getChannelTree() throws SAPIException{
		return ChannelTree.createFromChannelMap(getChannelsMap());
	}
	
	public void close(){
		sink.CloseRBNBConnection();
	}
	

}
