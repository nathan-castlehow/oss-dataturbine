/*! 
@file sms_rbnb.java
@author Paul Hubbard hubbard@sdsc.edu
@date Jan 24 2007
@brief Java program to read data from the Apple accelerometer and stuff it into data turbine. Think portable demo.
@note Code from http://www.shiffman.net/p5/sms and http://members.optusnet.com.au/lbramsay/programs/unimotion.html
*/

import java.util.*;
import sms.*;
import com.rbnb.sapi.*;
import java.io.*;

public class sms_rbnb 
{
    public static boolean ctrlC;
    /* Ctrl-C hook, just set boolean */
    private static void DoHook() 
    {        
        ctrlC = false;

        /* Add in a hook for ctrl-c's and other abrupt death */
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() 
        {
                ctrlC = true;
                System.out.println ("Shutdown hook for " + sms_rbnb.class.getName());
        } // run ()
        }); // addHook
    }
    
    public static void main (String args[]) 
    {
        Source      source;
        ChannelMap  cmap;
        String      srcName = "Apple Accelerometer";
        String[]    chanNames = {"X", "Y", "Z"};
        String      unitsMetadata = "units=G,scale=1,offset=0";
        int[]       vals = {0, 0, 0};
        String      rbnbHostName = "niagara-dev.sdsc.edu";      //! @todo Grab from argv[1]
        boolean     connected = false;
        int         cacheSize = 10240;               //! @todo parse from command line
        int         bufDepth = 10;                   // ditto
        double      scaleFactor = 1.0 / 251.0;     // a priori, may be incorrect
        int         archiveSize = cacheSize * 10;    //! @todo parse from command line
        int         idx;
        int         chanCount = 3;
        

        // Setup interrupt handler
        DoHook();
        
        // Try to connect to RBNB first, append mode
        System.out.println("Starting connection to DataTurbine on " + rbnbHostName + "...");
        
        try
        {
            source = new Source(cacheSize, "append", archiveSize);
            source.OpenRBNBConnection(rbnbHostName, srcName);
            connected = true;            

            // Setup channel map - names of channels, units
            cmap = new ChannelMap();
            for(idx = 0; idx < 3; idx++)
            {
                cmap.Add(chanNames[idx]);
                
                // Hardwired units (G) for all three sources
                cmap.PutUserInfo(cmap.GetIndex(chanNames[idx]), unitsMetadata);
                
                // Not sure if we still need the MIME type or not
                cmap.PutMime(cmap.GetIndex(chanNames[idx]), "application/octet-stream");
            }
            source.Register(cmap);
            source.Flush(cmap);
        }
        // We don't distinguish between errors in setup phase, just bail out
        catch (SAPIException se) 
        {
            System.out.println("Error on Turbine - not connected");
            connected = false;
            return;
        }
                
        // Main data loop: read, scale, write.
        try 
        {                        
            System.out.println("Turbine connected, running. Press control-c to end");
            
            // Loop - runs until control-c or error.
            do 
            {
                for(idx = 0; idx < chanCount; idx++)
                    cmap.Add(chanNames[idx]);
                
                // Read the data from the accelerometer
                vals = Unimotion.getSMSArray();
                
                // Timestamp all channels with client-side time of day, same across all three axes
                cmap.PutTimeAuto("timeofday");
                
                // Convert each value from counts into acceleration
                for(idx = 0; idx < chanCount; idx++)
                {
                    // Convert to G via empirical scale factor.
                    double valBuf[] = {vals[idx] * scaleFactor};                
                    cmap.PutDataAsFloat64(cmap.GetIndex(chanNames[idx]), valBuf);
                }
                
                source.Flush(cmap);
                cmap.Clear();

                // Throttle data. Chip can read at up to 500Hz, but for this demo 20Hz is enough, so sleep 50 millseconds between samples.
                //! @todo Set rate from command line
                Thread.sleep(40);                
            } while(true);
        }catch (SAPIException mse) 
        {
            System.out.println("Error saving data!");
            mse.printStackTrace();
        }
        catch (InterruptedException ie) {
            System.out.println("Interrupted, exiting.");
        }

        // Shutdown and exit
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
