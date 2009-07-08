package dtesp;
import dtesp.Config.*;



/**
 * <pre>
 * Main Dtesp class
 */

public class Dtesp
{
	/**
	 * 	Main function
	 *  Load configuration file(default "setting.xml") and run.
	 */

	public static void main(String args[])
	{
		
		ConfigObjCreator coc=new ConfigObjCreator();
		ConfigObj co;
		
		if (args.length==0)
			co=coc.CreateFromXml("setting.xml");
		else
			co=coc.CreateFromXml(args[0]);
		
		Dtesp d =new Dtesp();
		d.run(co);

	}
	
	
	/**
	 * Keeping time
	 */
	long start_time;					
	long waiting_duration=0;
	
	long GetStartTime()				{return start_time;}
	long GetWaitDuration()			{return waiting_duration;};
	void AddWaitDuration(long n)	{waiting_duration+=n;}
	
	/**
	 * <pre>
	 * Millisec of time dtesp ran.
	 * GetTimeFromStart() - time spend waiting for data
	 */
	
	public long GetRunningTime() 
	{
		return System.currentTimeMillis()-start_time-waiting_duration;
	}
	
	/**
	 * Millisec spend from start
	 * @return
	 */
	
	public long GetTimeFromStart() 
	{
		return System.currentTimeMillis()-start_time;
	}
	
	
	/**
	 * <pre>
	 * 	Start dtesp 
	 *  Create & Initialize SourceStub, SinkStub, and EsperStub.
 	 *  Run
	 * 
	 */

	public void run(ConfigObj co)
	{

		System.out.println("Config name:"+co.config_name);
		
		SourceStub source=new SourceStub();
		source.Init(co,this);

		SinkStub sink=new SinkStub();
		sink.Init(co,this,source);

		
		EsperStub e=new EsperStub();
		e.Init(co,this,source);



		

        System.out.println("start fetching data...");

		
        
        
        // set start time
    	start_time=System.currentTimeMillis();
    	
    	int retry_count=0;
		while (true)
		{
			// save data including test data(SaveDataItem) to source if necessary. 
			if (!source.Process()) return;
			
			// check if there is data to fetch
			if (!sink.IsDataExistToFetch())
			{
        		if (!co.wait_for_new_data) 
        		{
        			System.out.println("End of data");
        			break;
        		}

				long current_tick=System.currentTimeMillis();
				// if all the data requested is done, sleep infinitely
				if (sink.IsEndOfTheRequest())
				{
		        		System.out.println("End of request range");
		        		while (true) { try {Thread.sleep(1000);} catch (Exception e_) {}}
				}
				
				// sleep 100 millisec and wait for new data
				if (retry_count%100==0)
					System.out.println("Waiting for data...."+sink.current_request_start+" time of run:"+GetRunningTime());
				retry_count++;
				
    			try
    			{Thread.sleep(100);}			catch (Exception e_)   			{}
	    		
	    		// finding out the last time of all channels
	    		sink.UpdateTimeAllChannelReceived();

	    		// subtract time waited
	            AddWaitDuration(System.currentTimeMillis()-current_tick);
			}
			else
			{
				retry_count=0;
				
				// fetch data
				ReceivedDataSortedByTime rds=sink.Fetch();
				if (rds==null) break;
				
				if (!rds.IsEmpty())
					// send data to esper stub
					if (!e.Process(rds)) 
						break;
			}
			
			
		
		}	
				
		source.CleanUp();
		sink.CleanUp();
		
	}

	
}








