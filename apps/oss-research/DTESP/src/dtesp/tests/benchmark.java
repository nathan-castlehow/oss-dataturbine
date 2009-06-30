package dtesp.tests;

import java.util.Vector;

import dtesp.*;
import dtesp.Config.*;





 
public class benchmark 
{


	
	
	static public String MakeQueryString(int channel_n,int channels, int window_time_size)
	{
		String r;

//		r="select 1 as result from C"+Integer.toString(channel_n); return r;
		
//		r="select select C"+Integer.toString(channel_n)+".data as result from ";
		r="select count(*) as result from ";
		
		int i;
		for (i=0;i<channels;i++)
		{
			r+="C"+Integer.toString(i)+".win:time( "+Integer.toString(window_time_size)+" sec)";
			if (i!=channels-1)
				r+=",";
		}
		
		r+=" where ";
		boolean first=true;
		for (i=0;i<channels;i++)
		{
			if (i==channel_n) continue;
			if (!first)
				r+=" or ";
			first=false;
			r+="C"+Integer.toString(i)+".data=C"+Integer.toString(channel_n)+".data";
		}
		r+="";
		
		return r;
	}
	
	
	static public ConfigObj MakeConfiguration(double hertz, int channels, int window_time_size, int duration)
	{
		ConfigObj co= new ConfigObj();
		
	
		
		SinkItem	sink_item=new SinkItem("Sink","client","localhost:3333");
		SourceItem 	source_item=new SourceItem("Source","client","localhost:3333");
		
		
		co.AddSource(source_item);
		co.AddSink(sink_item);
		
		EventItem ev_result=new EventItem("Result","result");
		co.AddEvent(ev_result);
		int i;
		
		for (i=0;i<channels;i++)
		{
			String cname="C"+Integer.toString(i);
			EventItem ev=new EventItem(cname,"data");
			co.AddEvent(ev);
			
			SinkChannelItem 	snk_c = new SinkChannelItem(cname, sink_item, "client/"+cname, ev);
			SourceChannelItem 	src_c = new SourceChannelItem(cname, source_item, cname, ev_result, false);
			SourceChannelItem 	src_rc= new SourceChannelItem(cname+"result", source_item, cname+"result", ev_result, false);
			
			String qstring=MakeQueryString(i,channels,window_time_size);
			
			QueryItem			q_i= new QueryItem(cname,qstring, src_rc);
			
			co.AddSinkChannel(snk_c);
			co.AddSourceChannel(src_c);
			co.AddSourceChannel(src_rc);
			co.AddQuery(q_i);
			
			SaveDataItem sd= new SaveDataItem(duration, hertz, 255, 0, src_c);
			
			co.AddSaveData(sd);
		}
		
		
		return co;
	}

	
	public static void main(String args[])
	{
		dtesp.run(MakeConfiguration(10, 2, 1, 600));
	}	
	
	
}
