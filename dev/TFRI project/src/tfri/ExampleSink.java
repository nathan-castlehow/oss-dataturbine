package tfri;

import java.io.IOException;

import rbnb.GenericDataSource;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Sink;


public class ExampleSink {

	public static void main(String[] args) throws IOException, SAPIException, InterruptedException {
		System.out.println("Source:\n---");		
		//putDataIn();							// Call Method To Send Data to RBNB
		
		Thread.sleep(5000); 					// Wait 5000ms for data to settle
		
		System.out.println("\nSink:\n---");		 
		takeDataOut();							// Call Method to Take Data Out of RBNB
	}
	

	
	/**
	 * Method that acts as an example Sources
	 */
	public static void putDataIn() throws SAPIException, InterruptedException{
	
		// Create a source 
		// Name: 		SRC
		// Channels:	chan1, chan2
		// Server:		localhost:3333	
		GenericDataSource src = new GenericDataSource("SRC");
		src.addChannel("chan1");
		src.addChannel("chan2");
		
		for(double i=0; i<10; i += 0.1 ){	// Generate some data
			src.put("chan1", i);			// Put data into chan1
			src.put("chan2", -i);			// Put data into chan2
			src.flush();					// Commit the data to RBNB
			
			// Print to screen and wait for 100ms
			System.out.println("ADDED:\t" +i+" , "+ (-i));
			Thread.sleep(100);
		}
		
		src.close();	// Close the Connection
	}
	
	/**
	 * Method that acts as an example Sink
	 */
	public static void takeDataOut() throws SAPIException{
		
		// Create a new Sink
		// Server:		localhost:3333
		// Name: 		NAMEME
		// Channels:	SRC/chan1, SRC/chan2
		// Mode:		Newest
		// Timeout:		1000000
		
		
		// Create Sink
		Sink sink = new Sink();
		sink.OpenRBNBConnection("localhost:3333", "NAMEME");
		
		// Specify Channels to request
		ChannelMap cmap = new ChannelMap();
		cmap.Add("SRC/chan1");
		cmap.Add("SRC/chan2");
		sink.Subscribe(cmap, 0, 1,  "oldest");
		
		// Fetch data
		while(true){
			ChannelMap lmap = sink.Fetch(1000000);
			double data1 = lmap.GetDataAsFloat64(0)[0];
			double data2 = lmap.GetDataAsFloat64(1)[0];
			
			// Write to console
			System.out.println("READ:\t" + data1 + " , " + data2);
		}
	}
}
