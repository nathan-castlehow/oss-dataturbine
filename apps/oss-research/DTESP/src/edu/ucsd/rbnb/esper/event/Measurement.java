package edu.ucsd.rbnb.esper.event;

public class Measurement{
 
	private Metadata meta;
    private double value;
    
	public Measurement(double value, Metadata meta ) {
		this.value = value;
		this.meta = meta;
	}
	
	public Metadata getMeta() {
		return meta;
	}
	
	public double getValue() {
		return value;
	}

    

}