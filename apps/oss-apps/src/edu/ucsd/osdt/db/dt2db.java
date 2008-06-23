package edu.ucsd.osdt.db;

import edu.ucsd.osdt.db.rbnb.*;

public class dt2db {
	
	public static void main (String args[]) {
		(new dt2db()).exec2();
		
	}
	
	public void exec2() {
		String inputFileName =  "/Users/petershin/Downloads/config-parser/config.xml";
		Config aConfig = ConfigUtil.readConfig( inputFileName );
		
		SinkClientManager sMan = new SinkClientManager();
		sMan.setConfig(aConfig);
		sMan.exec();
	}
}
