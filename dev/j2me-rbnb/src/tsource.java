//
//  tsource.java
//  tsource
//
//  Created by Paul Hubbard on Thu Jan 29 2004.
//  tsource is an extremely simple data turbine source program that feeds ints
//  into the turbine at one per second with fixed host, channel and source
//  names. Think of it as 'hello, world'.

import java.util.*;
import com.rbnb.sapi.*;
import java.io.*;

 
public class tsource {

    public static void main (String args[])
    {
        Source          source;
        ChannelMap      cmap;
        String          source_name = "TSource";
        String          channel_name = "Channel0";
        String          host_string = "192.168.1.101";
        int             chanIndex = 0;
        boolean         connected = false;
        double[]          data = {42.0};
        
        System.out.println("Starting connection to Turbine...");
        
        try
        {
            source = new Source(); 	// Default source is 100-frame cache, no archive
            source.OpenRBNBConnection(host_string, source_name);
        
            cmap = new ChannelMap();
            connected = true;
        
        } catch (SAPIException se) {
            System.out.println("Error on Turbine - not connected");
            connected = false;
            return;
        }
    
        System.out.println("Turbine connected, creating channel map and source");
        
        // The channel map is constant once set up, so only do it once
        try 
        {
            chanIndex = cmap.Add(channel_name);
            
        }catch (SAPIException se) {
            System.out.println("Error adding to channel map!");
            return;
        }
        
        // Main data loop
        try 
        {
            for(int idx = 0; idx < 100000; idx++) {
                data[0] = idx * 1.0;
                cmap.PutTimeAuto("timeofday");
                
                // On nees, we assume that octet-stream data is double-precision float
                cmap.PutMime(chanIndex, "application/octet-stream");
                cmap.PutDataAsFloat64(chanIndex, data);
                
                source.Flush(cmap, false);
                
                // Print a message every 10 samples, i.e. every 10 seconds
                if((idx % 10) == 0)
                    System.out.println(idx + " samples sent OK");
                
                Thread.sleep(1000);
            }

        }catch (SAPIException mse) 
        {
            System.out.println("Error saving data!");
        }
        catch (InterruptedException ie) {
            System.out.println("Interrupted");
        }

        if(connected) 
        {
            System.out.println("Closing RBNB connection");
            source.CloseRBNBConnection();
        }
        System.out.println("Done, exiting.");
       
        return;
    }
}
