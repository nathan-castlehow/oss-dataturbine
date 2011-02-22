package edu.ucsd.rbnb.esper.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.rbnb.sapi.SAPIException;


public class RBNB_Loader {

	private GenericDataSource src;
	
	public static void main(String[] args) throws NumberFormatException, IOException, ParseException, SAPIException {
		RBNB_Loader loader = new RBNB_Loader();
		loader.loadSampleData("data.txt");
	}

	public RBNB_Loader() throws SAPIException {
		src = new GenericDataSource("Test");
		src.addChannel("temp");
		src.addChannel("solar");
	}
	
	public void loadSampleData(String fileName) throws IOException, ParseException, NumberFormatException, SAPIException{
		BufferedReader in = new BufferedReader(new FileReader(fileName));
		
		String line =in.readLine();
		while((line = in.readLine()) != null){
			if(line.trim().isEmpty()) continue;
			String[] token = line.trim().split(",");
			
			SimpleDateFormat f = new SimpleDateFormat("yyyy/MM/dd HH:mm");
			long timestamp = f.parse(token[0]).getTime();
			
			System.out.print("[Added] ");
			System.out.print(token[0]+" ");
			System.out.print("Temp: "+token[1]+"\t");
			System.out.print("Solar: "+token[2]+"\t");
			System.out.println();
			
			src.put("temp", Double.valueOf(token[1]), timestamp);
			src.put("solar", Double.valueOf(token[2]), timestamp);
		}
	}
}
