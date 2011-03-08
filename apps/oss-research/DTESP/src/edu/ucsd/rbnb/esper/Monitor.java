package edu.ucsd.rbnb.esper;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.time.CurrentTimeEvent;

import edu.ucsd.rbnb.esper.event.Measurement;

public class Monitor {

	private EPServiceProvider epService;
	
	public Monitor() {
		Configuration config = new Configuration();
		config.addEventTypeAutoName("edu.ucsd.rbnb.esper.event");
		epService = EPServiceProviderManager.getDefaultProvider(config);
	}
	
	public void loadData(Measurement m){
		epService.getEPRuntime().sendEvent(m);
	}
	
	public EPStatement query(String expression){
		return epService.getEPAdministrator().createEPL(expression);
	}
	
	public void setTime(long startInMillis){
		epService.getEPRuntime().sendEvent(new CurrentTimeEvent(startInMillis));
	}
	
	public long getTime(){
		return epService.getEPRuntime().getCurrentTime();
	}
	
	

}
