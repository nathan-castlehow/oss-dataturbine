package edu.ucsd.rbnb.esper;

import java.io.IOException;
import java.text.ParseException;

import com.rbnb.sapi.SAPIException;

import edu.ucsd.rbnb.esper.monitor.ESPERreader;
import edu.ucsd.rbnb.esper.monitor.GenericDataSource;
import edu.ucsd.rbnb.esper.monitor.Monitor;

/**
 * 
 * EsperTest_Bleaching.java (  edu.ucsd.rbnb.esper )
 * Created: Mar 14, 2011
 * @author Michael Nekrasov
 * 
 * Description: Runs a sample program that detects coral bleaching
 *
 */
public class EsperTest_Bleaching {
	
	private static String	FILENAME	= "data.txt";
	private static long		DELAY 		= 20;
	public static void main(String[] args) {
		try{
			Monitor monitor = new Monitor(new GenericDataSource("ESPER"));
			SampleDataLoader.setupSrc();
			ESPERreader reader = new ESPERreader(	"Thailand/Racha/HOBO", 
													monitor, DELAY);
			
			//Split Streams
			monitor.query(	"insert into Temperature select * " +
							"from Measurement where metadata.type = 'temp' ");
			monitor.query(	"insert into Solar select * " +
							"from Measurement where metadata.type = 'solar' ");
			
			//Monitor Bleaching
			monitor.place(	"insert into AverageSolar " + 
							"select avg(value) as value, timestamp, metadata " +
							"from	Solar.win:time(5 hours)",
							"/Averages/Solar" 		// <-- Target Ch
						);
			
			monitor.place(	"insert into AverageTemp " + 
							"select avg(value) as value, timestamp, metadata " +
							"from	Temperature.win:time(5 hours)",
							"/Averages/Temp" 			// <-- Target Ch
							);
			
			monitor.place(	"insert into LongTermAverageTemp " + 
							"select avg(value) as value, timestamp, metadata " +
							"from	Temperature.win:time(20 days)",
							"/Averages/Long-term_Temp" // <-- Target Ch
							);
		
			monitor.mark(	"select rT.value as temp, " +
							"rT.value as longTemp, " +
							"s.value as solar, " +
							"rT.metadata as tempMeta, " +
							"s.metadata as solarMeta, " +
							"s.timestamp as timestamp " +
							"from AverageSolar.std:lastevent() as s, " +
							"AverageTemp.std:lastevent() as rT," +
							"LongTermAverageTemp.std:lastevent() lT " +
							"where rT.value>lT.value and s.value > 600",
							"Bleaching Event Detected" 	// <-- Label
							);
			
			//Start Reading Data
			(new Thread(reader)).start();
			
			//Load data into RBNB
			SampleDataLoader.loadSampleData(FILENAME, DELAY);
		
		} catch (NumberFormatException e) {
			System.err.println("Error reading File: "+FILENAME);
		} catch (IOException e) {
			System.err.println("Error reading File: "+FILENAME);
		} catch (ParseException e) {
			System.err.println("Error reading File: "+FILENAME);
		} catch (SAPIException e) {
			System.err.println("Error Connecting to rbnb server 'localhost'");
		}
	}
}
