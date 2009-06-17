import org.w3c.dom.Element;
import java.util.*;
import com.rbnb.sapi.*;




/**
 * <pre>
 * Data structure to save a configuration of a sink (DT server)
 * fields
 *   name- name of the sink
 *   sink- sink class created for this sink
 *   connection_string- string used for connection
 *   channel_item_list- list of sink channel configurations 
 */	
public class 			SinkItem
{
	
	/**
	 * <pre>
	 * Parse from xml file.
	 * 
	 *   attributes:
	 *   name
	 *   client
	 *   connection_string
	 */
	public SinkItem(Element e) 
	{
		name= 				e.getAttribute("name");
		client=				e.getAttribute("client");
		connection_string=	e.getAttribute("connection_string");
	}		
	
	public SinkItem(String name_, String client_, String connection_string_)
	{
		name= name_;
		client=client_;
		connection_string=connection_string_;
	}
	
	public void AddChannel(SinkChannelItem sci)
	{
		channel_item_list.add(sci);
	}
	String					name;
	String					client;
	Sink          			sink;
    String					connection_string;
    ChannelMap				cmap;
    List<SinkChannelItem>	channel_item_list= new LinkedList<SinkChannelItem>();		
};

