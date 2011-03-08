package edu.ucsd.rbnb.esper;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.PropertyAccessException;
import com.espertech.esper.client.UpdateListener;
import com.rbnb.sapi.SAPIException;

import edu.ucsd.rbnb.esper.event.Measurement;
import edu.ucsd.rbnb.esper.io.GenericDataSource;
import edu.ucsd.rbnb.esper.io.Label;

public class ESPER_to_RBNB_Engine {

	private GenericDataSource src;
	private Monitor monitor;
	
	public ESPER_to_RBNB_Engine(GenericDataSource src, Monitor monitor) {
		this.src = src;
		this.monitor = monitor;
	}
	

	public void mark(String query, String targetCh, String label) throws SAPIException{
		final String ch = targetCh+"";
		final String l = label +"";
		src.addChannel(ch, Label.META_TYPE);
		
		monitor.query(query).addListener(new UpdateListener() {
			@Override
			public void update(EventBean[] newEvents, EventBean[] oldEvents) {
				try   { src.put(ch, (new Label("annotation", (Double)newEvents[0].get("current_timestamp()"), l)).toString() ); } 
				catch (PropertyAccessException e) { e.printStackTrace(); }
				catch (SAPIException e) { e.printStackTrace(); }
				System.out.println((new Label("annotation", (Double)newEvents[0].get("current_timestamp()"), l)).toString());
			}
		});
	}
	
	public void place(String query, String targetCh) throws SAPIException{
		final String ch = targetCh+"";
		src.addChannel(ch);
		
		monitor.query(query).addListener(new UpdateListener() {
			@Override
			public void update(EventBean[] newEvents, EventBean[] oldEvents) {
				try   { src.put(ch, (Double)newEvents[0].get("value") ); } 
				catch (PropertyAccessException e) { e.printStackTrace(); }
				catch (SAPIException e) { e.printStackTrace(); }
			}
		});
	}
	
	public void query(String expression){
		monitor.query(expression);
	}
	
	public void setTime(long startInMillis){
		monitor.setTime(startInMillis);
	}
	
	public void loadData(Measurement m){
		monitor.loadData(m);
	}
	
}