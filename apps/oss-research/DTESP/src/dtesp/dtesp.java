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
		
		Forwarder f=new Forwarder();
		
		f.Init(co);
		
		Receiver r=new Receiver();
		r.Init(co);


		
		f.StartMeasure();
		if (!co.bSubscribe) f.SetTimeAllChannelReceived();
        System.out.println("start fetching data...");

		
		while (true)
		{
			ReceivedDataSortedByTime rds=f.Fetch();
			if (f==null) return;
			if (!rds.IsEmpty()) 
				if (!r.Process(rds)) 
					return;
		
		}	
	}

	
}








