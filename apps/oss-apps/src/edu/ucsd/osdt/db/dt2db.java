package edu.ucsd.osdt.db;

import edu.ucsd.osdt.db.rbnb.*;

public class dt2db {
	
	
	public static void main (String args[]) {
		
		String filePath = null;
		
		if (args.length > 0) {
			filePath = args[0];
			dt2db d2d = new dt2db();
			d2d.exec(filePath);
			
		}
		else 
		(new dt2db()).exec2();
		
	}
	
	public void exec(String fp) {
		String inputFileName =  fp;
		Config aConfig = ConfigUtil.readConfig( inputFileName );
		
		SinkClientManager sMan = new SinkClientManager();
		sMan.setConfig(aConfig);
		sMan.exec();
		
		
	}
	
	
	public void exec2() {
		String inputFileName =  "/Users/petershin/Downloads/config-parser/config.xml";
		Config aConfig = ConfigUtil.readConfig( inputFileName );
		
		SinkClientManager sMan = new SinkClientManager();
		sMan.setConfig(aConfig);
		sMan.exec();
	}
}
