package edu.ucsd.rbnb.esper.event;

public class Metadata {

	private String server;
	private String source;
	private String device;
	private String model;
	private String type;
	
	public Metadata(String server, String source, String device, String model,
			String type) {
		this.server = server;
		this.source = source;
		this.device = device;
		this.model = model;
		this.type = type;
	}
	
	public String getServer() {
		return server;
	}
	public String getSource() {
		return source;
	}
	public String getDevice() {
		return device;
	}
	public String getModel() {
		return model;
	}
	public String getType() {
		return type;
	}
	
}
