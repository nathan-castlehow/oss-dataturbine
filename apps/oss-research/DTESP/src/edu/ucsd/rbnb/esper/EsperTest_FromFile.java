package edu.ucsd.rbnb.esper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

import edu.ucsd.rbnb.esper.event.Measurement;
import edu.ucsd.rbnb.esper.event.Metadata;

public class EsperTest_FromFile {
	
	private static Monitor monitor;
	
	public static void main(String[] args) throws Exception {
		monitor = new Monitor();
	
		//Setup Queries
		splitStreams();
		monitorBleaching();
		
		//Start sending data
		loadSampleData();
	}
	
	private static void splitStreams(){
		monitor.query(	"insert into Temperature select * " +
						"from Measurement where meta.type = 'temp' ");
		monitor.query(	"insert into Solar select * " +
						"from Measurement where meta.type = 'solar' ");
	}
	
	
	private static void monitorBleaching(){
		
		monitor.query(	"insert into AverageSolar " + 
						"select avg(value) as value, meta " +
						"from	Solar.win:time(5 hours)"	);
		
		monitor.query(	"insert into AverageTemp " + 
						"select avg(value) as value, meta " +
						"from	Temperature.win:time(5 hours)"	);
	
		monitor.query(	"insert into LongTermAverageTemp " + 
						"select avg(value) as value, meta " +
						"from	Temperature.win:time(20 days)"	);

	
		monitor.query(	"select rT.value as temp, " +
						"rT.value as longTemp, " +
						"s.value as solar, " +
						"rT.meta as tempMeta, " +
						"s.meta as solarMeta " +
						"from AverageSolar.std:lastevent() as s, " +
						"AverageTemp.std:lastevent() as rT," +
						"LongTermAverageTemp.std:lastevent() lT " +
						"where rT.value>lT.value and s.value > 600")
						
		.addListener(new UpdateListener() {
			public void update(EventBean[] newEvents, EventBean[] oldEvents) {
		        EventBean event = newEvents[0];
		        System.out.println(	"#!!! Bleaching EVENT !!!");
		        System.out.format(	"#  avg.temp (rec>long): %.3f > %.3f", 
		        					event.get("temp"), event.get("longTemp"));
		        System.out.println();
		        
		        System.out.print(	"#  |-from  ");
		        System.out.print(	event.get("tempMeta.server") ); 
		        System.out.print(	event.get("tempMeta.source") );
		        System.out.print(	"  "+ event.get("tempMeta.device") );
		        System.out.print(	" "+ event.get("tempMeta.model") );
		        System.out.println();
		        
		        System.out.format(	"#  avg.solar %.3f", event.get("solar"));
		        System.out.println();
		        
		        System.out.print(	"#  |-from  ");
		        System.out.print(	event.get("solarMeta.server") ); 
		        System.out.print(	event.get("solarMeta.source") );
		        System.out.print(	"  "+ event.get("solarMeta.device") );
		        System.out.print(	" "+ event.get("solarMeta.model") );
		        System.out.println();
    			
		        
		    }
		});
	}
	

	static Metadata M_TEMP	= new Metadata(	"iguassu.sdsc.edu:3333", 
											"Thailand/HOBO/kohracha/temp", 
											"HOBO", 
											"#1172881",
											"temp"
											);
	static Metadata M_SOLAR	= new Metadata(	"iguassu.sdsc.edu:3333", 
											"Thailand/HOBO/kohracha/solar", 
											"HOBO", 
											"#1172881",
											"solar"
											);

	static void loadSampleData() throws IOException, ParseException{
		BufferedReader in = new BufferedReader(new FileReader("data.txt" ));
		
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
			
			monitor.setTime(timestamp);
			monitor.loadData(new Measurement(Double.valueOf(token[1]),M_TEMP));
			monitor.loadData(new Measurement(Double.valueOf(token[2]),M_SOLAR));
		}
	}

}
