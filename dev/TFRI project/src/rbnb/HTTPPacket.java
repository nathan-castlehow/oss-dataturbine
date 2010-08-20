package rbnb;

import java.util.HashMap;

/**
 * HTTPPacket.java (  RBNB )
 * Created: Jan 21, 2010
 * @author Michael Nekrasov
 * 
 * Description: Helper class that can be used to store header
 * 				and content of a http Reponse
 */
public class HTTPPacket {

	private HashMap<String, String> header = new HashMap<String, String>();
	private byte[] data = null;
	
	public HTTPPacket(){}

	/**
	 * Add a parameter to the HTTP Header
	 * @param parameter name of Heading parameter
	 * @param value to store
	 */
	public void addToHeader(String parameter, String value){
		header.put(parameter.trim().toLowerCase(), value.trim().toLowerCase());
	}
	
	/**
	 * Add a raw string in 'parameter: value' format.
	 * @param rawString to parse and add to header
	 */
	public void addToHeader(String rawString){
		String[] s = rawString.split(":",2);
		addToHeader(s[0], s[1]);
	}
	
	/** 
	 * Sets the content of the packet
	 * @param data of HTTP Response
	 */
	public void setData(byte[] data){
		this.data = data;
	}
	
	/**
	 * Gets the HTTP Response data stored in thsi packet
	 * @return the data
	 */
	public byte[] getData(){
		return data;
	}
	
	/**
	 * Get a parameter from HTTP Response header
	 * @param field to read
	 * @return the corresponding value
	 */
	public String getHeader(String field){
		if(header.containsKey(field))
			return header.get(field.trim().toLowerCase());
		else return null;
	}
	
	
	/**
	 * Gets the content type of this packet
	 * @return the content type
	 */
	public String getContentType(){
		return getHeader("content-type");
	}
}
