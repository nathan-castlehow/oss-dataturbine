package tfri;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.GregorianCalendar;

import rbnb.GenericDataSource;


import com.rbnb.sapi.SAPIException;


public class MetSource extends GenericDataSource {
	
	
	public MetSource(String srcName) throws SAPIException{
		super(srcName);
		setUpChannels();
	}
	
	
	private void setUpChannels() throws SAPIException{
		addChannel("Temp (C)");
		addChannel("RH");
		addChannel("Rain");
	}
	
	
	public void loadFile(File file, int year) throws NumberFormatException, IOException, SAPIException{
        BufferedReader reader   = new BufferedReader(new FileReader(file));
        
        String line;	
        line = reader.readLine();//skip first line
		while ((line = reader.readLine()) != null) {     
	
			if(line.trim().equals("")) continue;
			
			String[] value = line.split(",");
			for(int i=0; i < value.length; i++) value[i] = value[i].trim(); //clean
			
			int month	= Integer.valueOf(value[0]);
			int day		= Integer.valueOf(value[1]);
			int hour	= Integer.valueOf(value[2])/100;
			 
			double timestamp = (new GregorianCalendar(year, month, day, hour, 0)).getTimeInMillis()/1000;

			System.out.print("ADDED:");
			if(value.length>3 && !value[3].equals("") ){
				float vlu = put("Temp (C)", new Float(value[3]),timestamp);
				System.out.print("\tTemp: "+vlu);
			}
			if(value.length>4 && !value[4].equals("") ){
				float vlu = put("RH", new Float(value[4]),timestamp);
				System.out.print("\tRH: "+vlu);
			}
			if(value.length>5 && !value[5].equals("") ){
				float vlu = put("Rain", new Float(value[5]),timestamp);
				System.out.print("\tRain: "+vlu);
			}
			System.out.println("\tOn: "+year+"/"+month+"/"+day+" "+hour+":00");
			flush(true);
		}
	}
	
	
	public static void main(String[] args) {
		
		try {
			MetSource src = new MetSource("Met Data");
			src.loadFile(new File("shanpin weather(20090225-0720).csv"), 2009);
		} catch (NumberFormatException e) {
			System.err.println("ERROR parsing file");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("ERROR reading file");
		} catch (SAPIException e) {
			System.err.println("ERROR connecting to RBNB");
		}
	}
}
