package edu.ucsd.osdt.db.rbnb;

import com.rbnb.sapi.*;

public class requestStartHelper {

	// if there is no channels, it returns 0;
	
	public double findStartTime (ChannelMap cMap){
		int numOfChannels = cMap.NumberOfChannels();
		double minStartTime = 0.0;
		
		if (numOfChannels == 0)
			return minStartTime;
		
		else if (numOfChannels ==1) {
			try {
				minStartTime = cMap.GetTimeStart(0);
			}
			catch (ArrayIndexOutOfBoundsException e) {
				e.printStackTrace();
			}
			return minStartTime;
		}
		else {
			
			minStartTime = cMap.GetTimeStart(0);

			double tempStartTime = 0.0;
			
			try {
				for (int i = 0; i< numOfChannels; i++) {

					tempStartTime = cMap.GetTimeStart(i);

					if (minStartTime > tempStartTime)
						minStartTime =tempStartTime;

				}
			}
			catch (ArrayIndexOutOfBoundsException e) {
				e.printStackTrace();
			}
			
			return minStartTime;
		}
	}

	
}
