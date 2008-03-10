//
//  tsink.java
//  tsink
//  Created by Paul Hubbard on Thu Jan 29 2004.
//  tsink is the pair program to tsource, and is a very simple data turbine
//  sink that displays the data sent by tsource. 

import java.util.*;
import com.rbnb.sapi.*;
import java.io.*;


public class tsink {
    
public static void main(String args[])
{
        Sink            sink;
        ChannelMap      cmap;
        String		   sink_name = "Tsink";
        String          source_name = "TSource";
        String          channel_name = "Channel0";
        String          host_string = "localhost";
        boolean         connected = false;
        
        System.out.println("Starting connection to Turbine...");
        
        try
        {
            sink = new Sink();
            sink.OpenRBNBConnection(host_string, sink_name);

			System.out.println("connection opened");
//            if(true) return;
            cmap = new ChannelMap();
            connected = true;
            
        } catch (SAPIException se) {
			//se.printStackTrace();
			Hello.connected = false;
            System.out.println("Error on Turbine - not connected");
            connected = false;
            return;
        }
        
        System.out.println("Turbine connected, creating channel map");
        
        // The channel map is constant once set up, so only do it once
        try 
        {
            System.out.println("Adding '" + source_name + "/" + channel_name + "'");
            
            // The channel map defines what we're looking for in the turbine
            cmap.Add(source_name + "/" + channel_name);
            // Subscribe means that we want every sample sent, as oppsed to 
            // Monitor
            sink.Subscribe(cmap);

        }catch (SAPIException se) {
            System.out.println("Error adding to channel map!");
            return;
        }
        
        // Main data loop
        try 
        {
            double[] data = new double[10];
            
            System.out.println("Waiting for data...");
            
            for(int idx = 0; idx < 1000; idx++) 
            {
                // Wait 2 seconds for data
			
                ChannelMap outmap = sink.Fetch(2000);
                // Look up channel index
				
                int chanIdx = outmap.GetIndex(source_name + "/" + channel_name);
                // If channel index is less than zero, then no data in the fetch                
                if(chanIdx >= 0)
                {
                    // Note that we have to know how to parse the data type!
                    data = outmap.GetDataAsFloat64(chanIdx);
                    
                    // Get data returns an array, be cautious about parsing same
						for(int i = 0; i < data.length; i++)
						{
							System.out.println(cmap.GetName(i) + " = " + data[i]);
						}
                }
            }
        }catch (SAPIException mse) 
        {
			System.out.println("msg is " + mse.getMessage());
			mse.printStackTrace();
            System.out.println("Error reading data!");
        }
       
        if(connected) 
        {
            System.out.println("Closing RBNB connection");
            sink.CloseRBNBConnection();
        }
        System.out.println("Done, exiting.");
        
        return;
    }
}

