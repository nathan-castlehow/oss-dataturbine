package dtesp;





public class dtesp {


	/**
	 * 	Main method to start dtesp
	 */
	public static void main(String args[])
	{
		

		
		
		
		DTESPConfigObjCreator coc=new DTESPConfigObjCreator();
		DTESPConfigObj co;
		
		if (args.length==0)
			co=coc.CreateFromXml("setting.xml");
		else
			co=coc.CreateFromXml(args[0]);
		
		DTESPForwarder f=new DTESPForwarder();
		
		f.Init(co);
		
		DTESPReceiver r=new DTESPReceiver();
		r.Init(co);


		
		f.StartMeasure();
		f.SetTimeAllChannelReceived();
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








