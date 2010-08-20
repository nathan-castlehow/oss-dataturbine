package rbnb;

import java.io.IOException;
import java.net.URLConnection;

/**
 * HTTPMultipartReader.java (  RBNB )
 * Created: Jan 21, 2010
 * @author Michael Nekrasov
 * 
 * Description: Reads MultiPart HTTP Responses and returns the
 * 				individual HTTP components.
 *
 */
public class HTTPMultipartReader {

	private URLConnection conection ;
	private HTTPInputStream input ;
	private final String boundryString;
	
	/**
	 * Create a new Reader
	 * @param connection to HTTP  Server
	 * @throws IOException
	 */
	public HTTPMultipartReader(URLConnection connection) throws IOException{
		this.conection = connection;
		 
		//Verify that its multipart
		if(isMultipart(connection)){
			int boundryIndx = connection.getContentType().indexOf("boundary=");
			boundryString = connection.getContentType().substring(boundryIndx+("boundary=").length());
			
		}else{
			throw new IOException("Not a Multipart File");
		}
		
		input = new HTTPInputStream(conection.getInputStream());
		input.readLine();//skip boundry
	}
	
	/**
	 * Reads a part of the HTTP Response and returns it
	 * @return the component HTTP response in a multipart response
	 * @throws IOException
	 */
	public HTTPPacket readPacket() throws IOException{
		HTTPPacket packet = new HTTPPacket();
		String line;
		
		//READ Header
		while(( line = input.readLine()) != null && !line.equals(""))
			packet.addToHeader(line);
		
		//READ Data
		input.startBuffering();
		while((line = input.readLine()) != null && !line.equals(boundryString)){}
		input.stopBuffering();
		input.undoLastRead();
		packet.setData(input.getBufferContent());
		
		//Return built packet
		return packet;
	}

	/**
	 * Tests if the connection a multipart one
	 * @param connection to test
	 * @return
	 */
	public static boolean isMultipart(URLConnection connection){
		return connection.getContentType().contains("multipart/x-mixed-replace");
	}
	
}
