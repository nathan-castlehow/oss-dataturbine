package dtesp;
import java.util.LinkedList;
import java.util.List;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.Sink;

import dtesp.Config.*;


/**
 *   sink- sink class created for this sink
 *   channel_list- list of sink channel configurations 
 */

class SinkRuntime
{
	public Sink          			sink;
    public ChannelMap				cmap;
    public List<SinkChannelRuntime>	channel_list= new LinkedList<SinkChannelRuntime>();
	SinkItem						conf;
}
