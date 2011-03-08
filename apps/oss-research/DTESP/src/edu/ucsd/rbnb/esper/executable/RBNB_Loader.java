package edu.ucsd.rbnb.esper.executable;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.rbnb.sapi.SAPIException;

import edu.ucsd.rbnb.esper.io.GenericDataSource;


public class RBNB_Loader {

	private GenericDataSource src;
	
	public static void main(String[] args) throws NumberFormatException, IOException, ParseException, SAPIException {
		RBNB_Loader loader = new RBNB_Loader();
		loader.loadSampleData("data.txt");
		loader.close();
	}

	public RBNB_Loader() throws SAPIException {
		src = new GenericDataSource("Thailand");
		src.addChannel("/Racha/HOBO/temp", GenericDataSource.MIME_BINARY);
		src.addChannel("/Racha/HOBO/solar", GenericDataSource.MIME_BINARY);
	}
	
	public void loadSampleData(String fileName) throws IOException, ParseException, NumberFormatException, SAPIException{
		BufferedReader in = new BufferedReader(new FileReader(fileName));
		
		String line =in.readLine();
		while((line = in.readLine()) != null){
			if(line.trim().isEmpty()) continue;
			String[] token = line.trim().split(",");
			
			SimpleDateFormat f = new SimpleDateFormat("yyyy/MM/dd HH:mm");
			long timestamp = f.parse(token[0]).getTime()/1000;
			
			System.out.print("[Added] ");
			System.out.print(token[0]+" ");
			System.out.print("Temp: "+token[1]+"\t");
			System.out.print("Solar: "+token[2]+"\t");
			System.out.println();
			
			src.put("/Racha/HOBO/temp", Double.valueOf(token[1]), timestamp);
			src.put("/Racha/HOBO/solar", Double.valueOf(token[2]), timestamp);
		}
	}
	
	public void close(){
		src.close();
	}
}
