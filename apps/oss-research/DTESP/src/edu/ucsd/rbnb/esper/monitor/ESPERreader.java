package edu.ucsd.rbnb.esper.monitor;

import java.util.ArrayList;
import java.util.Map;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;

import edu.ucsd.rbnb.esper.event.Measurement;

/**
 * 
 * ESPERreader.java (  edu.ucsd.rbnb.esper.monitor )
 * Created: Mar 14, 2011
 * @author Michael Nekrasov
 * 
 * Description: Reads data and meta data from RBNB and puts it into ESPER
 *
 */
public class ESPERreader extends GenericSink implements Runnable{
	private ArrayList<Map<String, Object>> metadata;
	
	private Monitor monitor;
	private int numChannels =0;
	private double startTime;
	private double duration;
	private String timeReference;
	private long delay;
	
	public ESPERreader(String source, Monitor monitor, long delay) 
			throws SAPIException {
		this(source, monitor, 0, 1, "next", delay);
	}
	
	public ESPERreader(String source, Monitor monitor, double startTime, 
			double duration, String timeReference, long delay) 
			throws SAPIException {
		super("ESPER");
		this.monitor = monitor;
		this.startTime = startTime;
		this.duration = duration;
		this.timeReference = timeReference;
		this.delay = delay;
		getMeta(source);
	}
	
	public void getMeta(String source) throws SAPIException{
		metadata = 	new ArrayList<Map<String,Object>>();
		
		if(isOpen()) close();
		
		//Read metadata
		open();
		ChannelMap cmap = new ChannelMap();
		cmap.Add(source+"/_META");
		subscribeTo(cmap, 0, 1, "Newest");
		
		String data = fetch().GetDataAsString(0)[0];
		for(String meta: data.split("\n"))
			metadata.add(Measurement.metaLoad(meta));
		close();
		
		//Ready to read data
		open();
		numChannels = metadata.size();
		cmap = new ChannelMap();
		for(Map<String,Object> m: metadata)
			cmap.Add(m.get("source").toString());
		
		subscribeTo(cmap, startTime, duration, timeReference);
	}
	
	public Measurement[] getData() throws SAPIException{
		
		ChannelMap lmap = fetch();
		if(lmap.NumberOfChannels() <= 0) return null;
	
		Measurement[] data = new Measurement[numChannels];
		for(int i=0; i<numChannels; i++){
			Map<String, Object> meta = metadata.get(i);
			int channelId = lmap.GetIndex(meta.get("source").toString());
			
			data[i] = new Measurement(
						lmap.GetDataAsFloat64(channelId)[0], 
						((long)lmap.GetTimes(channelId)[0]), 
						metadata.get(i)
					);
		}	
		return data;
	}

	@Override
	public void run() {
		while(true) try {
			Measurement data[] = getData();
			if(data == null) continue;
			
			System.out.println("[Reading] ");
			for(Measurement m : data) System.out.println(m);
			
			for(Measurement m : data){
				monitor.setTime(((Long)m.get("timestamp"))*1000);
				monitor.loadData(m);
			}
			
			System.out.println();
			
			monitor.flush();
			Thread.sleep(delay);
		}catch (Exception e) { e.printStackTrace();}
	}
	
}