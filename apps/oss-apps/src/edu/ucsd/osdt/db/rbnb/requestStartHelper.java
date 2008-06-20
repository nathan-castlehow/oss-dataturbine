package edu.ucsd.osdt.db.rbnb;

import com.rbnb.sapi.*;
import java.io.*;
import java.util.*;
import java.sql.Date;

public class requestStartHelper {

	public requestStartHelper () {
		double hi = 0.0;
	}
	

	
	// if there is no channels, it returns 0;
	public long findStartTime (ChannelMap cMap){
		int numOfChannels = cMap.NumberOfChannels();
		long minStartTime = 0L;
		
		if (numOfChannels == 0)
			return minStartTime;
		
		else if (numOfChannels ==1) {
			try {
				minStartTime = (long) cMap.GetTimeStart(0);
			}
			catch (ArrayIndexOutOfBoundsException e) {
				e.printStackTrace();
			}
			return minStartTime;
		}
		else {
			
			minStartTime = (long) cMap.GetTimeStart(0);

			long tempStartTime = 0L;
			
			try {
				for (int i = 0; i< numOfChannels; i++) {

					tempStartTime = (long) cMap.GetTimeStart(i);

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

	
	public long readLastTimeFromFile (String fPath) {
		
		BufferedReader is = null;
		try {
			is = new BufferedReader (new FileReader (fPath));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String input = "0.0";
		try {
			input = FileIO.readerToString(is);
			input = input.trim();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return Long.parseLong(input, 10);
		
	}
	
	
	public static String formatDate(long milis) {
		java.text.SimpleDateFormat format = new java.text.SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss.SSSSZ");
		Date date = new Date(milis);
		return format.format(date);
	}
	
	
	
	public static void main (String args[]) {
		requestStartHelper rsh = new requestStartHelper ();
		long timeStamp = rsh.readLastTimeFromFile ("/Users/petershin/Documents/hi.txt");
		
		System.out.println(formatDate(timeStamp));
		
		long systimestamp = System.currentTimeMillis();
		
		System.out.println("current tmstmp is " + systimestamp);
		System.out.println("current time is " + formatDate(systimestamp));
		
		return;
	}
	
}
