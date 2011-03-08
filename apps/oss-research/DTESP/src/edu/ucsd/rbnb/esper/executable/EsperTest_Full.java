package edu.ucsd.rbnb.esper.executable;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.rbnb.sapi.SAPIException;

import edu.ucsd.rbnb.esper.ESPER_to_RBNB_Engine;
import edu.ucsd.rbnb.esper.Monitor;
import edu.ucsd.rbnb.esper.event.Measurement;
import edu.ucsd.rbnb.esper.event.Metadata;
import edu.ucsd.rbnb.esper.io.GenericDataSource;
import edu.ucsd.rbnb.esper.io.RBNBsink;

public class EsperTest_Full {
	
	private static ESPER_to_RBNB_Engine engine;

	static Metadata[] meta = {
		new Metadata(	"iguassu.sdsc.edu:3333", 
						"Thailand/HOBO/kohracha/temp", 
						"HOBO", 
						"#1172881",
						"temp"
					),
		new Metadata(	"iguassu.sdsc.edu:3333", 
						"Thailand/HOBO/kohracha/solar", 
						"HOBO", 
						"#1172881",
						"solar"
					)
	};

	public static void main(String[] args) throws Exception {
		engine = new ESPER_to_RBNB_Engine(new GenericDataSource("ESPER"), new Monitor());
		
		//Split Streams
		engine.query(	"insert into Temperature select * " +
						"from Measurement where meta.type = 'temp' ");
		engine.query(	"insert into Solar select * " +
						"from Measurement where meta.type = 'solar' ");
	
		
		engine.place(	"insert into AverageSolar " + 
						"select avg(value) as value, meta " +
						"from	Solar.win:time(5 hours)",
						"Avg Solar" 		// <-- Target Ch
					);
		
		//Monitor Bleaching
		engine.place(	"insert into AverageTemp " + 
						"select avg(value) as value, meta " +
						"from	Temperature.win:time(5 hours)",
						"Avg Temp" 			// <-- Target Ch
						);
		
		engine.place(	"insert into LongTermAverageTemp " + 
						"select avg(value) as value, meta " +
						"from	Temperature.win:time(20 days)",
						"Long Term Avg Temp" // <-- Target Ch
						);
	
		engine.mark(	"select rT.value as temp, " +
						"rT.value as longTemp, " +
						"s.value as solar, " +
						"rT.meta as tempMeta, " +
						"s.meta as solarMeta " +
						"from AverageSolar.std:lastevent() as s, " +
						"AverageTemp.std:lastevent() as rT," +
						"LongTermAverageTemp.std:lastevent() lT " +
						"where rT.value>lT.value and s.value > 600",
						"Long Term Avg Temp",	// <-- Target Ch
						"BLEACHING EVENT" 		// <-- Label
						);
		
		//Load Sample data
		loadSampleData();
	}
	

	static void loadSampleData() throws IOException, ParseException, 
										SAPIException{
		RBNBsink sink = new RBNBsink(meta);
		
		while(true){
			Measurement[] pts = sink.getData();
			
			if(pts == null) break;
			
			long timestamp = pts[0].getTimestamp()*1000;
			engine.setTime(timestamp);
			
			System.out.print("[Added] ");
			System.out.print( time(timestamp) +" ");
			System.out.print("Temp: "  + pts[1].getValue() +"\t");
			System.out.print("Solar: " + pts[0].getValue() +"\t");
			System.out.println();
			
			engine.loadData(new Measurement(pts[1].getValue(),meta[0]));
			engine.loadData(new Measurement(pts[0].getValue(),meta[1]));
		}
	}

	private static String time(long timestamp){
		return (new SimpleDateFormat("MM/dd/yyyy HH:mm")).format(timestamp);
	}
}
