package edu.ucsd.rbnb.esper.monitor;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.PropertyAccessException;
import com.espertech.esper.client.UpdateListener;
import com.espertech.esper.client.time.CurrentTimeEvent;
import com.rbnb.sapi.SAPIException;

import edu.ucsd.rbnb.esper.event.Measurement;

/**
 * 
 * Monitor.java (  edu.ucsd.rbnb.esper.monitor )
 * Created: Mar 14, 2011
 * @author Michael Nekrasov
 * 
 * Description: Wrapper class that sits over the ESPER API
 *
 */
public class Monitor {

	private EPServiceProvider epService;
	private GenericDataSource src;
	
	public Monitor(GenericDataSource src) {
		this.src = src;
		Configuration config = new Configuration();
		//config.addEventTypeAutoName("edu.ucsd.rbnb.esper.event");
		config.addEventType("Measurement", Measurement.getDefinition());
		epService = EPServiceProviderManager.getDefaultProvider(config);
	}
	
	public EPStatement query(String statement){
		return epService.getEPAdministrator().createEPL(statement);
	}
	
	public EPStatement mark(String statement,  String label) 
			throws SAPIException{
		EPStatement query = query(statement);
		
		final String l = label +"";
		src.addChannel("/_EventsChannel", GenericDataSource.MIME_EVENT);
		
		query.addListener( new UpdateListener() {
			@Override
			public void update(EventBean[] newEvents, EventBean[] oldEvents) {
				try   { 
					EventBean e = newEvents[0];
					Label lb = new Label("annotation", extractTime(e), l);
					src.put("/_EventsChannel", lb.toString(), extractTime(e));
					src.flush();
					
					System.out.println(
							"  ["+Measurement.time((long)extractTime(e))
							+ "] EVENT\t"+l);
				} 
				catch (PropertyAccessException e) 	{ e.printStackTrace(); }
				catch (SAPIException e) 			{ e.printStackTrace(); }
			}
		});
		
		return query;
	}
	
	public EPStatement place(String statement, String targetCh) 
			throws SAPIException{
		EPStatement query = query(statement);
		
		final String ch = targetCh+"";
		src.addChannel(ch);
		
		query.addListener(new UpdateListener() {
			@Override
			public void update(EventBean[] newEvents, EventBean[] oldEvents) {
				try   { 
					EventBean e = newEvents[0];
					src.put(ch, extractValue(e), extractTime(e) ); 
					src.flush();
					
					System.out.println(
							"  [" + Measurement.time((long)extractTime(e))+ 
							"] "  + extractValue(e) + "\t" + ch
							);
				} 
				catch (PropertyAccessException e) { e.printStackTrace(); }
				catch (SAPIException e) { e.printStackTrace(); }
			}
		});
		
		return query;
	}
	
	public void loadData(Measurement m){
		epService.getEPRuntime().sendEvent(m, "Measurement");
	}
	
	public void setTime(long startInMillis){
		epService.getEPRuntime().sendEvent(new CurrentTimeEvent(startInMillis));
	}
	
	public long getTime(){
		return epService.getEPRuntime().getCurrentTime();
	}
	
	private double extractTime(EventBean e){
		return Double.valueOf(e.get("timestamp").toString());
	}
	
	private double extractValue(EventBean e){
		return  Double.valueOf(e.get("value").toString());
	}
	
	public void flush() throws SAPIException{
		src.flush();
	}
}
