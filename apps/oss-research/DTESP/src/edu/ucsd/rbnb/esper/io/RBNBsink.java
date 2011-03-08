package edu.ucsd.rbnb.esper.io;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;

import edu.ucsd.rbnb.esper.event.Measurement;
import edu.ucsd.rbnb.esper.event.Metadata;

public class RBNBsink extends GenericSink{
	
	private int numChannels =0;
	private Metadata[] meta;
	
	public RBNBsink(Metadata[] meta) throws SAPIException {
		super("ESPER");
		
		this.meta = meta;
		numChannels = meta.length;
		
		ChannelMap cmap = new ChannelMap();
		for(Metadata m: meta)
			cmap.Add(m.getSource());
		
		subscribeTo(cmap, 0, 1, "next");
	}
	
	public Measurement[] getData() throws SAPIException{
		ChannelMap lmap = fetch();

		if(lmap.NumberOfChannels() <= 0) return null;
		
		Measurement[] data = new Measurement[numChannels];
		
		for(int i=0; i<numChannels; i++){
			data[i] = new Measurement(
						lmap.GetDataAsFloat64(i)[0], 
						((long)lmap.GetTimes(i)[0]), 
						meta[i]
					);
		}
		
		return data;
	}
	
}
