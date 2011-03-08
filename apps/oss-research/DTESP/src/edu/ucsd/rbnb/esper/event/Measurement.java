package edu.ucsd.rbnb.esper.event;

public class Measurement{
	
    private double value;
    private long timestamp;
    private Metadata meta;
    
    public Measurement(double value, Metadata meta) {
		this.value = value;
		this.timestamp = (long)(System.currentTimeMillis()/1000);
		this.meta = meta;
	}
	
    
	public Measurement(double value, long timestamp, Metadata meta) {
		this.value = value;
		this.timestamp = timestamp;
		this.meta = meta;
	}
	
	public double getValue() {
		return value;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
	public Metadata getMeta() {
		return meta;
	}
    
    

}