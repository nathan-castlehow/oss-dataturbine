package dtesp;
import dtesp.Config.*;





public class dtesp
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
		
		
		run(co);

	}
	
	/**
	 * 	Start dtesp
	 */

	public static void run(ConfigObj co)
	{

		SourceStub source=new SourceStub();
		source.Init(co);
		
		
		SinkStub sink=new SinkStub();
		sink.Init(co);

		
		EsperStub e=new EsperStub();
		e.Init(co);
		e.SetSourceStub(source);
		

		
		sink.StartMeasure();
		if (!co.bSubscribe) sink.SetTimeAllChannelReceived();
        System.out.println("start fetching data...");

		
		while (true)
		{
			ReceivedDataSortedByTime rds=sink.Fetch();
			if (rds==null) return;
			if (!rds.IsEmpty()) 
				if (!e.Process(rds)) 
					return;
		
		}	
	}

	
}








