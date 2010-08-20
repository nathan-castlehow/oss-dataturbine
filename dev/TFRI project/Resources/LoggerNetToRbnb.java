/*! 
@file sms_rbnb.java
@author Sameer Tilak sameer@sdsc.edu
@date Jan 24 2007
@brief Java program to read data from the text file written by LoggerNet and stuff it into the DataTurbine. Think portable demo.
@based on Code by Paul Hubbard 
*/

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Source;


public class LoggerNetToRbnb {

    public static void main (String args[]) 
    {
        Source      source;
        ChannelMap  cmap;
        String      srcName = "Campbell Datalogger";
        String[]    chanNames = {"BatteryVoltage", "AirTemp"};
        int[]       chanIdx = {0, 1};
        String      rbnbHostName = "localhost";      //! @todo Grab from argv[1]
        boolean     connected = false;
        int         cacheSize = 10240;               //! @todo parse from command line
        int         archiveSize = cacheSize * 10;    //! @todo parse from command line
        int         idx;
		float[]     BattVolt = {(float) 0.0};
		float []    AirTemp = {(float) 0.0};
		String	    buff;
		StringTokenizer st = null;
		String DELIM = ",";
		String inputFileName  = "loggernet.dat";

        
        // Try to connect to RBNB first, append mode
        System.out.println("Starting connection to DataTurbine on " + rbnbHostName + "...");
        
        try
        {
            source = new Source(cacheSize, "append", archiveSize);
            source.OpenRBNBConnection(rbnbHostName, srcName);
            
            
            cmap = new ChannelMap();
            connected = true;            
        } catch (SAPIException se) 
        {
            System.out.println("Error on Turbine - not connected");
            connected = false;
            return;
        }
        
        System.out.println("Turbine connected, creating channel map and source");
        
        // The channel map is constant once set up, so only do it once
        try 
        {
            for(idx = 0; idx < 2; idx++)
                chanIdx[idx] = cmap.Add(chanNames[idx]);
            
        }catch (SAPIException se) 
        {
            System.out.println("Error adding to channel map!");
            return;
        }
        
        // Main data loop
        try 
        {            
            FileReader inputFileReader = new FileReader(inputFileName);

            BufferedReader inputStream   = new BufferedReader(inputFileReader);

            try 
			{
				 String inLine = null;	
				while ((inLine = inputStream.readLine()) != null) {     
		
					st = new StringTokenizer(inLine, DELIM);


					buff = st.nextToken();
					BattVolt[0]	= Float.valueOf(buff.trim()).floatValue();

					buff = st.nextToken();
					AirTemp[0]	=  Float.valueOf(buff.trim()).floatValue();

					System.out.println("Battery Voltage " + BattVolt[0] + " and AirTemperature are  = " + AirTemp[0]);

					// Timestamp all channels
					cmap.PutTimeAuto("timeofday");
                
					for(idx = 0; idx < 2; idx++)
					{
						chanIdx[idx] = cmap.Add(chanNames[idx]);
						cmap.PutMime(chanIdx[idx], "application/octet-stream");
                    }
					
						cmap.PutDataAsFloat32(chanIdx[0], BattVolt);
						cmap.PutDataAsFloat32(chanIdx[1], AirTemp);
						
						source.Flush(cmap);
                
            }
        }catch (SAPIException mse) 
			{
            System.out.println("Error saving data!");
			}
		} catch (IOException e) 
			{
            e.printStackTrace();
			}
	/*	} catch (InterruptedException ie) 
			{
            System.out.println("Interrupted");
			} */
	
        if(connected) 
        {
            System.out.println("Closing RBNB connection");
         
            // Tell RBNB to keep the data once we close
            source.Detach();
            
            source.CloseRBNBConnection();
        } 
        System.out.println("Done, exiting.");

        return;
    }
}
