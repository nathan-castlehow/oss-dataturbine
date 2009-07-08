package dtesp.tests;

import java.io.*;
import java.util.Vector;

import dtesp.*;
import dtesp.Config.*;





 
public class benchmark 
{


	
	
	static public String MakeQueryString(int channel_n,int channels, int window_time_size)
	{
		String r;

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
	
	
	static public ConfigObj MakeConfiguration(int bn, int hertz, int channels, int window_time_size, int duration)
	{
		ConfigObj co= new ConfigObj();
		
	
		co.output_level=4;
		co.wait_for_new_data=false;
		
		
		SinkItem	sink_item=new SinkItem("Sink","client"+bn,"localhost:3333");
		SourceItem 	source_item=new SourceItem("Source","client"+bn,"localhost:3333");
		
		
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
			
			SinkChannelItem 	snk_c = new SinkChannelItem		(cname				,"Sink"		, "client"+bn+"/"+cname, cname);
			SourceChannelItem 	src_c = new SourceChannelItem	(cname				,"Source"	, cname, "Result", false);
			SourceChannelItem 	src_rc= new SourceChannelItem	(cname+"result"		,"Source"	, cname+"result", "Result", true);
			
			String qstring=MakeQueryString(i,channels,window_time_size);
			
		
			
			QueryItem			q_i= new QueryItem(cname,qstring, src_rc.name);
			
			co.AddSinkChannel(snk_c);
			co.AddSourceChannel(src_c);
			co.AddSourceChannel(src_rc);
			co.AddQuery(q_i);
			
			SaveDataItem sd= new SaveDataItem(duration, hertz, 255, 0, src_c.name);
			
			co.AddSaveData(sd);
		}
		
		
		return co;
	}

	
	public static void main(String args[]) throws IOException
	{
		
		
		Vector<Long> v=new Vector<Long>();
		
		int []hertz				={10		,20			,10			,10						,10};
		int []channels			={2			,2			,2			,4						,2};
		int []window_time_size	={1			,1			,2			,1						,1};
		int []duration			={600		,600		,600		,600					,1200};
	
		int i;
		for (i=0;i<3;i++)
//		for (i=3;i<hertz.length;i++)
		{
			
			Dtesp d=new Dtesp();
			int h=hertz[i];
			int c=channels[i];
			int w=window_time_size[i];
			int du=duration[i];
			
			d.run(MakeConfiguration(i, h, c, w, du));
//			out.println(" "+h+"\t"+c+"\t"+w+"\t"+du+"\trt:\t"+d.GetRunningTime());
			v.add(d.GetRunningTime());
			System.out.println(" running time:"+d.GetRunningTime());
		}
		
		
		for (i=0;i<v.size();i++)
			System.out.println(v.get(i));
		

		 
	}	
	
	
}
