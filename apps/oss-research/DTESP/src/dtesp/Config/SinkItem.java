package dtesp.Config;


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
 *   copy_to_source- copy current sink to a source
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
	 *   copy_to_source
	 */
	public SinkItem(Element e, ConfigObj co) 
	{
		name= 				e.getAttribute("name");
		client=				e.getAttribute("client");
		connection_string=	e.getAttribute("connection_string");
		copy_to_source=		co.GetSource(e.getAttribute("copy_to_source"));
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
	public String					name;
	public String					client;
	public Sink          			sink;
	public String					connection_string;
    public ChannelMap				cmap;
    public List<SinkChannelItem>	channel_item_list= new LinkedList<SinkChannelItem>();
    public SourceItem 				copy_to_source;
};

