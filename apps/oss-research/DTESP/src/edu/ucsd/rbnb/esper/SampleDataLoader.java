package edu.ucsd.rbnb.esper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import com.rbnb.sapi.SAPIException;

import edu.ucsd.rbnb.esper.monitor.GenericDataSource;

/**
 * 
 * SampleDataLoader.java (  edu.ucsd.rbnb.esper )
 * Created: Mar 14, 2011
 * @author Michael Nekrasov
 * 
 * Description: Loads a sample data file into RBNB along with meta data
 *
 */
public class SampleDataLoader {

	public static void main(String[] args){
		if(args.length != 1)
			System.out.println("Usage: SampleDataLoader [filename]");
		else try {
			loadSampleData(args[0]);
		} catch (NumberFormatException e) {
			System.err.println("Error reading File: "+args[0]);
		} catch (IOException e) {
			System.err.println("Error reading File: "+args[0]);
		} catch (ParseException e) {
			System.err.println("Error reading File: "+args[0]);
		} catch (SAPIException e) {
			System.err.println("Error Connecting to rbnb server 'localhost'");
		}
	}
	
	private static GenericDataSource src;
	
	public static void setupSrc() throws SAPIException{
		//Set up Src
		src = new GenericDataSource("Thailand");
		src.addChannel("/Racha/HOBO/_META", GenericDataSource.MIME_META);
		src.addChannel("/Racha/HOBO/temp", 	GenericDataSource.MIME_BINARY);
		src.addChannel("/Racha/HOBO/solar", GenericDataSource.MIME_BINARY);
		
		//Create MetaData
		HashMap<String,Object> meta1 = new HashMap<String, Object>();
		meta1.put( "server", 	"iguassu.sdsc.edu:3333");
		meta1.put( "source",	"Thailand/Racha/HOBO/temp");
		meta1.put( "device",	"HOBO");
		meta1.put( "model",		"#1172881");
		meta1.put( "type",		"temp");
		
		HashMap<String,Object> meta2 = new HashMap<String, Object>();
		meta2.put( "server", 	"iguassu.sdsc.edu:3333");
		meta2.put( "source",	"Thailand/Racha/HOBO/solar");
		meta2.put( "device",	"HOBO");
		meta2.put( "model",		"#1172881");
		meta2.put( "type",		"solar");
		
		src.put("/Racha/HOBO/_META", meta1+"\n"+meta2);
		src.flush();
	}
	
	public static void loadSampleData(String fileName) throws IOException, 
						ParseException, NumberFormatException, SAPIException{
		loadSampleData(fileName, 0);
	}
	
	public static void loadSampleData(String fileName, long delay) 
						throws IOException, ParseException, 
						NumberFormatException, SAPIException{
		//Read File
		BufferedReader in = new BufferedReader(new FileReader(fileName));
		String line =in.readLine();
		while((line = in.readLine()) != null){
			if(line.trim().isEmpty()) continue;
			String[] token = line.trim().split(",");
			
			long timestamp = (new SimpleDateFormat("yyyy/MM/dd HH:mm"))
							 .parse(token[0]).getTime()/1000;
			
			System.out.println("[Adding]");
			System.out.println("  ["+token[0]+"] " +
					"\tTemp: "+token[1]+
					"\tSolar: "+token[2]);
			
			src.put("/Racha/HOBO/temp", Double.valueOf(token[1]), timestamp);
			src.put("/Racha/HOBO/solar", Double.valueOf(token[2]), timestamp);
			src.flush();
			try { 	Thread.sleep(delay);	} 
			catch (InterruptedException e) {}
		}
		src.close();
	}
}
