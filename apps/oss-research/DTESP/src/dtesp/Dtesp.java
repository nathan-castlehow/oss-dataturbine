package dtesp;
import dtesp.Config.*;





public class Dtesp
{
	/**
	 * 	Main- load config and run
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
	
	
	public long GetRunningTime() 
	{
		return System.currentTimeMillis()-start_time-waiting_duration;
	}
	
	public long GetTime() 
	{
		return System.currentTimeMillis()-start_time;
	}
	
	
	/**
	 * 	Start dtesp
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



		
		if (!co.bSubscribe) 
		{
			sink.UpdateTimeAllChannelReceived();
		}
        System.out.println("start fetching data...");

		
        
        
        
    	start_time=System.currentTimeMillis();
    	
    	int retry_count=0;
		while (true)
		{
			if (!source.Process()) return;
			if (!sink.IsDataExistToFetch())
			{
        		if (!co.wait_for_new_data) 
        		{
        			System.out.println("End of data");
        			break;
        		}

				long current_tick=System.currentTimeMillis();
				if (sink.IsEndOfTheRequest())
				{
		        		System.out.println("End of request range");
		        		while (true) { try {Thread.sleep(1000);} catch (Exception e_) {}}
				}
				
				if (retry_count%100==0)
					System.out.println("Waiting for data...."+sink.current_request_start+" time of run:"+GetRunningTime());
				retry_count++;
				
    			try
    			{Thread.sleep(100);}			catch (Exception e_)   			{}
	    		
	    		// finding out the last time of all channels
	    		sink.UpdateTimeAllChannelReceived();

	            AddWaitDuration(System.currentTimeMillis()-current_tick);
			}
			else
			{
				retry_count=0;
				ReceivedDataSortedByTime rds=sink.Fetch();
				if (rds==null) return;
				if (!rds.IsEmpty()) 
					if (!e.Process(rds)) 
						return;
			}
			
			
		
		}	
	}

	
}








