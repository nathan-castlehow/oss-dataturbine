package edu.ucsd.rbnb.esper.executable;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

import edu.ucsd.rbnb.esper.Monitor;
import edu.ucsd.rbnb.esper.event.Measurement;
import edu.ucsd.rbnb.esper.event.Metadata;

public class EsperTest {
	
	private static Monitor monitor;
	
	public static void main(String[] args) throws Exception {
		monitor = new Monitor();
	
		//Setup Queries
		splitStreams();
		monitorBleaching();
		monitorRunoff();
		
		//Start sending data
		//loadData();
		loadSampleData();
	}
	
	private static void splitStreams(){
		monitor.query(	"insert into Temperature select * " +
						"from Measurement where meta.type = 'temp' ");
		monitor.query(	"insert into Solar select * " +
						"from Measurement where meta.type = 'solar' ");
		monitor.query(	"insert into Salinity select * " +
						"from Measurement where meta.type = 'sali' ");
		monitor.query(	"insert into Rainfall select * " +
						"from Measurement where meta.type = 'rain' ");
	}
	
	private static void monitorBleaching(){
		
		monitor.query(	"select avg(rT.value) as temp, " +
				"avg(s.value) as solar " +
				"from Solar.win:time(5 hours) as s, " +
				"Temperature.win:time(5 hours) as rT," +
				"Temperature.win:time(20 days) as uT ")
		.addListener(new UpdateListener() {
			public void update(EventBean[] newEvents, EventBean[] oldEvents) {
		        EventBean event = newEvents[0];
		        System.out.println("[Bleaching]\tTemp (avg): "+ 
		        	event.get("temp")+ "\tSolar (avg): "+ event.get("solar"));
		    }
		});
	}

	static void monitorRunoff(){
		
		monitor.query(	"select sum(r.value) as rain, " +
						"avg(s.value) as sali " +
						"from Rainfall.win:time(1 hour) as r, " +
						"Salinity.win:time(1 hour) as s")
		.addListener(new UpdateListener() {
			public void update(EventBean[] newEvents, EventBean[] oldEvents) {
		        EventBean event = newEvents[0];
		        System.out.println("[Runoff]\tRainfall (total): "+
		        		event.get("rain")+"\tSalinity (avg): "
		        		+ event.get("sali"));
		    }
		});
	}
	
	
	static void loadRndData(){
		while(true){
			monitor.loadData(generateMeasurement(M_RAIN));
			monitor.loadData(generateMeasurement(M_TEMP));
			monitor.loadData(generateMeasurement(M_SOLAR));
			monitor.loadData(generateMeasurement(M_SALI));
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {}
		}
	}
	
	static void loadSampleData() throws IOException{
		BufferedReader in = new BufferedReader(new FileReader("data.txt" ));
		
		String line =in.readLine();
		while((line = in.readLine()) != null){
			if(line.trim().isEmpty()) continue;
			String[] token = line.trim().split(",");
			
			monitor.loadData(new Measurement(Double.valueOf(token[1]),M_TEMP));
			monitor.loadData(new Measurement(Double.valueOf(token[2]),M_SOLAR));
			
			System.out.print("[Added]:\t");
			System.out.print("Time: "+token[0]+"\t");
			System.out.print("Temp: "+token[1]+"\t");
			System.out.print("Solar: "+token[2]+"\t");
			System.out.println();
		}
	}
	
	static Metadata M_TEMP	= new Metadata(	"iguassu.sdsc.edu:3333", 
											"/i/temp", "HOBO", "t007", "temp");
	static Metadata M_SOLAR	= new Metadata(	"iguassu.sdsc.edu:3333", 
											"/i/solar", "HOBO", "s202", "solar");
	static Metadata M_SALI	= new Metadata(	"iguassu.sdsc.edu:3333", 
											"/i/sali", "davis", "x330", "sali");
	static Metadata M_RAIN	= new Metadata(	"iguassu.sdsc.edu:3333", 
											"/i/rain", "davis", "r100", "rain");
	
	static Measurement generateMeasurement(Metadata m){
		return generateMeasurement((new Random()).nextDouble(), m);
	}

	static Measurement generateMeasurement(double v, Metadata m){
		return new Measurement(v, m);
	}


}
