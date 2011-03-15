package edu.ucsd.rbnb.esper.event;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * Measurement.java (  edu.ucsd.rbnb.esper.event )
 * Created: Mar 14, 2011
 * @author Michael Nekrasov
 * 
 * Description: 	Contains value and metadata of each entry loaded from RBNR 
 * 					and into ESPER
 *
 */
public class Measurement extends HashMap<String, Object>{
	
	private static final long serialVersionUID = -1400675619524771512L;
    
    public Measurement(double value, Map<String,Object> metadata) {
    	this(value, (long)(System.currentTimeMillis()/1000), metadata);
	}
	
	public Measurement(double value, long timestamp, 
			Map<String,Object> metadata) {
		put("value", 		value);
		put("timestamp",	timestamp);
		put("metadata", 	metadata);
	}
	
	public static Map<String, Object> getDefinition(){
		
		Map<String, Object> metadataDef = new HashMap<String, Object>();
		metadataDef.put("server",	String.class);
		metadataDef.put("source",	String.class);
		metadataDef.put("device",	String.class);
		metadataDef.put("model",	String.class);
		metadataDef.put("type",		String.class);
		
		Map<String, Object> measurementDef = new HashMap<String, Object>();
		measurementDef.put("value",		Double.class);
		measurementDef.put("timestamp", Long.class);
		measurementDef.put("metadata",	metadataDef);
		
		return measurementDef;
	}
	
	public static Map<String, Object> metaLoad(String s){
		HashMap<String, Object> meta = new HashMap<String, Object>();
		for(String token: s.replace("}", "").replace("{", "").split(", ")){
			String[] t = token.split("=");
			meta.put(t[0], t[1]);
		}
		return meta;
	}
	
	public String metaSave(){
		return get("metadata").toString();
	}
	
	@Override
	public String toString() {
		String out="";
		out += "  [";
		out += time((Long)get("timestamp"));
		out += "] ";
		out += get("value");
		out += " \t" + get("metadata");
		
		return out;
	}
	
	public static String time(long timestamp){
		return (new SimpleDateFormat("MM/dd/yyyy HH:mm")).format(timestamp*1000);
	}
}