package edu.ucsd.rbnb.esper.monitor;

/**
 * 
 * Label.java (  edu.ucsd.rbnb.esper.monitor )
 * Created: Mar 14, 2011
 * @author Michael Nekrasov
 * 
 * Description: Class to handle putting labels into RDV
 *
 */
public class Label {
	
	public static final String ANNOTATION		= "annotation";
	public static final String MIN				= "min";
	public static final String MAX 				= "max";
	public static final String START 			= "start";
	public static final String STOP 			= "stop";
	
	private String type;
	private double timestamp;
	private String content;

	public Label(String type, double timestamp, String content) {
		this.type = type;
		this.timestamp = timestamp;
		this.content = content;
	}
	
	@Override
	public String toString() {
		String out ="";
		out += "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n";
		out += "<!DOCTYPE properties " +
				"SYSTEM \"http://java.sun.com/dtd/properties.dtd\">\n";
		out += "<properties>\n";
		out += "<entry key=\"type\">"+type+"</entry>\n";
		out += "<entry key=\"content\">"+content+"</entry>\n";
		out += "<entry key=\"timestamp\">"+timestamp+"</entry>\n";
		out += "</properties>\n";
		return out;
	}
	
}
