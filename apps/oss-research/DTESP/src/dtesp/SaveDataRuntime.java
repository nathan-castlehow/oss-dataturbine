package dtesp;

import dtesp.Config.*;


/**
 */

class SaveDataRuntime
{
	public SaveDataRuntime(SaveDataItem sd)
	{
		double [][]r=sd.ParseData();
		conf=sd;
		data=r[0];
		time=r[1];
	}
	double []data;
	double []time;
	
	SaveDataItem conf;
}
