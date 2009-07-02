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
	long waiting_duration;
	
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

		SourceStub source=new SourceStub();
		source.Init(co,this);
		
		
		SinkStub sink=new SinkStub();
		sink.Init(co,this);

		
		EsperStub e=new EsperStub();
		e.Init(co,this);
		e.SetSourceStub(source);
		

		
		if (!co.bSubscribe) sink.SetTimeAllChannelReceived();
        System.out.println("start fetching data...");

		
        
    	start_time=System.currentTimeMillis();
    	
		while (true)
		{
			if (!source.Process()) return;
			
			ReceivedDataSortedByTime rds=sink.Fetch();
			if (rds==null) return;
			if (!rds.IsEmpty()) 
				if (!e.Process(rds)) 
					return;
		
		}	
	}

	
}








