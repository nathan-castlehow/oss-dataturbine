package rbnb;

import com.rbnb.sapi.SAPIException;

public class Example {

	public static void main(String[] args) 			//Standard java main class definition
	throws SAPIException, InterruptedException {	//Formality
		
		if(args.length <1){
			System.err.println("Please enter an IP");
			System.exit(0);
		}
		
		//SET UP SOURCE with 2 Channels
		GenericDataSource src = new GenericDataSource("Flowmeter", args[0], 3333, 100, 100);
		src.addChannel("CHAN1");
		src.addChannel("CHAN2");
		

		
		//Add Data into source 
		for(double i=0; i<40; i += 0.05 ){
			
			//src.put("Temp", data, time);				//ADD double i to CHAN1
			src.put("CHAN1", i);				//ADD double i to CHAN1
			src.put("CHAN2", (int) (i+100) );	//ADD int i+100 to CHAN2
			
			src.flush();						//Send DATA
			
			//PRINT msg to user
			System.out.println("ADDED: [" +i+" , "+(int)(i+100) +" ] to "+ src.getName());
			
			//Wait
			Thread.sleep(10);
		}
		
		
		src.close();
	}
	
}
