/*
 * Created on Aug 2, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.nees.rbnb;

import java.io.*;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Enumeration;

import org.nees.daq.ISOtoRbnbTime;

/**
 * @author terry
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class TransformDataFile {
	
	private void computTransform() {
		
		double d = 0.12192;
		double dx1=0.0, dx2=0.0, lx1=0.0, lx2=0.0;
		
		for (int i = 0; i < values.length; i++)
		{
			dx1 = values[i][indexForDx1];
			dx2 = values[i][indexForDx2];
			lx1 = values[i][indexForLCx1];
			lx2 = values[i][indexForLCx2];
			values[i][indexForDisp] = - (dx1+dx2)/2.0;
			values[i][indexForRot] = (dx1/d - dx2/d)/2.0;
			values[i][indexForForce] = (lx1 + lx2)/2.0;
			values[i][indexForMoment] = lx1*d - lx2*d;
		}
	}

	String inPath,outPath;
	
	BufferedReader rd;
	
	String[] names;
	double [][] values;
	String[] timeStamp;
	String sampleRateLine;

	int indexForDx1 = -1;
	int indexForDx2 = -1;
	int indexForLCx1 = -1;
	int indexForLCx2 = -1;
	int indexForDisp = -1;
	int indexForRot = -1;
	int indexForForce = -1;
	int indexForMoment = -1;

	public static void main(String[] args) {
		TransformDataFile t = new TransformDataFile();
		t.doit(args[0], args[1]);
	}
	
	public void doit(String inFilePath, String outFilePath)
	{
		inPath = inFilePath;
		outPath = outFilePath;
				
		int count = probeFile();
		
		if (count > 0)
		{
			if (setIndexes())
			{
				if (readFileToArrays(count))
				{
					
					System.out.println("size of values: " + values.length + "," + values[0].length);
					
					computTransform();
			
					writeFile();
				}
			}
		}
	}

	/**
	 * @return
	 */
	private int probeFile() {

		File probe = new File(inPath);
		String in = null;
		int lineCount = 0;

		// does it exist and is it readable
		if (probe != null && probe.exists() && probe.canRead())
		{
			try
			{
				rd = new BufferedReader(new FileReader(probe));
				System.out.println("Sucessfully connected to " + inPath);
			}
			catch (IOException e)
			{
				e.printStackTrace();
				return 0;
			}
			if (rd == null)
			{
				System.out.println("Failed to open file stream " + inPath);
				return 0;
			}
		}
		else // data not available
		{
			System.out.println("Data unavailable: path...");
			if (probe == null)
				System.out.println("Could not open file.");
			else if (!probe.exists())
				System.out.println("File does not exist");
			else if (!probe.canRead())
				System.out.println("File is unreadable");
			return 0;
		}
		
		boolean header = true;
				
		try {
			// read one line ahead 
			
			while(header && ((in = rd.readLine()) != null))
			{
				if (!in.startsWith("Time"))
					System.out.println("Skipping header line: " + in);
				else
					header = false;
			}
			
			if (in == null)
			{
				System.out.println("Unexpected end of file.");
			}
			
			// the first non-header line should be a list of channel names
			System.out.println("Channel list line: " + in);
			
			// parse out the channel names and discard any path information
			// e.q. FromDaq/LoadCell_z will be sent as LoadCell_z
			StringTokenizer st = new StringTokenizer(in," "); // seperated by blanks
			Vector channelNames = new Vector();

			while (st.hasMoreTokens())
				channelNames.add(st.nextToken());
		
			names = new String[channelNames.size() + 4];
			
			Enumeration en = channelNames.elements();
			int pos,i = 0;
			String name;
			while (en.hasMoreElements())
			{
				name = (String)en.nextElement();
				pos = name.lastIndexOf('/');
				if (pos > -1) name = name.substring(pos+1);
				System.out.println("name " + i + " = " + name);
				names[i] = name;
				i++;
			}
			names[i] = "Displasment_x"; i++;
			names[i] = "Rotation_x";i++;
			names[i] = "Force_x";i++;
			names[i] = "Moment_x";

			while((in = rd.readLine()) != null)
			{
				// just read and dump data to get line count;
				lineCount++;
			}	

			rd.close();
				
		}
		catch (Throwable t) {
			t.printStackTrace();
			return 0;
		}
		
		return lineCount;

	} // probeFile

	/**
	 * 
	 */
	private boolean setIndexes() {
		// LVDT_x1 LVDT_x2 LoadCell_x1 LoadCell_x2
		// int indexForDx1 = -1;
		// int indexForDx2 = -1;
		// int indexForLCx1 = -1;
		// int indexForLCx2 = -1;
		// int indexForDisp = -1;
		// int indexForRot = -1;
		// int indexForForce = -1;
		// int indexForMoment = -1;
		
		// NOTE: indexes offset by one because time is in names arrays
		// but not in valeus array		
		for (int i = 0; i < names.length; i ++)
		{
			if (names[i].equals("LVDT_x1")) indexForDx1 = i-1;
			if (names[i].equals("LVDT_x2")) indexForDx2 = i-1;
			if (names[i].equals("LoadCell_x1")) indexForLCx1 = i-1;
			if (names[i].equals("LoadCell_x2")) indexForLCx2 = i-1;
		}
		
		if (indexForDx1 < 0) return false;
		if (indexForDx2 < 0) return false;
		if (indexForLCx1 < 0) return false;
		if (indexForLCx2 < 0) return false;
		
		// NOTE: indexes offset by one because time is in names arrays
		// but not in valeus array		
		indexForDisp = names.length - 5;
		indexForRot = names.length - 4;
		indexForForce = names.length - 3;
		indexForMoment = names.length - 2;
		
		System.out.println("indexForDx1 = " + indexForDx1);
		System.out.println("indexForDx2 = " + indexForDx2);
		System.out.println("indexForLCx1 = " + indexForLCx1);
		System.out.println("indexForLCx2 = " + indexForLCx2);
		System.out.println("indexForDisp = " + indexForDisp);
		System.out.println("indexForRot = " + indexForRot);
		System.out.println("indexForForce = " + indexForForce);
		System.out.println("indexForMoment = " + indexForMoment);

		return true;
	}

	/**
	 * 
	 */
	private boolean readFileToArrays(int size) {
		int lineCount = 0;
		File probe = new File(inPath);
		String in = null;

		// NOTE: indexes offset by one because time is not in valeus array
		values = new double[size][names.length-1];
		timeStamp = new String[size];

		try
		{
			rd = new BufferedReader(new FileReader(probe));
			System.out.println("Sucessfully opened " + inPath);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return false;
		}
		if (rd == null)
		{
			System.out.println("Failed to open file stream " + inPath);
			return false;
		}
		
		boolean header = true;
		
		try {
			// read one line ahead 
			
			while(header && ((in = rd.readLine()) != null))
			{
				if (!in.startsWith("Time"))
				{
					System.out.println("Skipping header line: " + in);
					if (in.startsWith("Sample")) sampleRateLine = in;
					continue;
				}
				else
					header = false;
			}
			
			if (in == null)
			{
				System.out.println("Unexpected end of file.");
			}
			
			// skipping channel list line
			// the first non-header line should be a list of channel names
			System.out.println("Channel list line: " + in);
			
			lineCount = -1;

			while((in = rd.readLine()) != null)
			{
				lineCount++;
				StringTokenizer st = new StringTokenizer(in," ");

				// the first token is the time stamp				
				String tsString = st.nextToken();
				timeStamp[lineCount] = tsString;

				// The rest of line is floting point data values
				int i = 0;
				while (st.hasMoreTokens())
				{
					// post data to channel i
					values[lineCount][i] = Double.parseDouble(st.nextToken());
					i++;
				}
			}
		}
		catch (Throwable t) {
			t.getStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 
	 */
	private void writeFile() {
		try
		{
			PrintWriter	out = new PrintWriter(new FileWriter(outPath));
			// write header
			/*
			* Active channels: ATL1,ATT1,ATL3,ATT3
			* Sample rate: 10.000000
			* Channel units: g,g,in,kip (unknown at this time!)
			* Time ATL1 ATT1 ATL3 ATT3
			*/
			out.print("Active channels: ");
			out.print(names[0]); // label for Time
			for (int i = 1; i < names.length; i++)
			{
				out.print("," + names[i]);
			}
			out.println();
	
			if (sampleRateLine != null) out.println(sampleRateLine);
		
			// Channel units are unknown at this time!
		
			// write channel names
			out.print(names[0]); // label for Time
			for (int i = 1; i < names.length; i++)
			{
				out.print(" " + names[i]);
			}
			out.println();
	
			// write data
	
			for (int i = 0; i < timeStamp.length; i++)
			{
				out.print(timeStamp[i]);
				for (int j = 0; j < values[0].length; j++)
				{
					out.print(" " + values[i][j]);
				}
				out.println();
				out.flush();
			}
			out.close();
		
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
	}
}
