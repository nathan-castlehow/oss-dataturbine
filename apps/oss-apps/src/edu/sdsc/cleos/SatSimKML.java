/*!
 * @author Lawrence J. Miller <ljmiller@sdsc.edu>
 * @author Cyberinfrastructure Laboratory for Environmental Observing Systems (CLEOS)
 * @author San Diego Supercomputer Center (SDSC)
 * @note Please see copywrite information at the end of this file.
 * @since $LastChangedDate:2007-10-09 11:05:08 -0700 (Tue, 09 Oct 2007) $
 * $LastChangedRevision:171 $
 * @author $LastChangedBy:ljmiller $
 * $HeadURL:http://nladr-cvs.sdsc.edu/svn-public/CLEOS/cleos-rbnb-apps/trunk/src/edu/sdsc/cleos/SatSim.java $
 */

package edu.sdsc.cleos;

import java.util.*;
import com.rbnb.sapi.*;
import java.io.*;
import java.lang.*;
import java.util.Random;

/**
 *
 * @author Sameer Tilak sameer@sdsc.edu
 */

 class RunnableThread implements Runnable{
	
	Thread runner;
	public RunnableThread() {
	}
	
	public RunnableThread(String threadName) {
        runner = new Thread(this, threadName);   // (1) Create a new thread.
        System.out.println(runner.getName());
        runner.start();                          // (2) Start the thread.
	}
	
	public void run(){
		//Display info about this particular thread
		System.out.println(Thread.currentThread());
	}
	
	
}

 
class RemindTask extends TimerTask 
{ 
             SatSimKML mySatSimKML;
             
             public void RemindTask ()
             {
             }
            
             public RemindTask (SatSimKML satsimobj)
             {
                 mySatSimKML = satsimobj;
             } 
             public void run() { 
              if (mySatSimKML.cursamples < mySatSimKML.numsamples || mySatSimKML.numsamples == -1) {   
                //System.out.println("Hello World!!!");
                mySatSimKML.DataGenerator();
                mySatSimKML.FeedDataIntoRbnb();
                mySatSimKML.cursamples = mySatSimKML.cursamples + 1 ;
              }
              else {
                mySatSimKML.DisconnectFromRbnb();
                System.exit(0);
              }
             }
}

public class SatSimKML {
    
     Source      source;
     ChannelMap  cmap;
     String      srcName = "SatSimKML";
     String[]    chanNames = {"Relative Humidity", "AirTemp",
    		 					"Alt", "Lat", "Lon", "TrackID", "Type", "Classification", "Speed", "Heading"}; ///! @note convention for the KML plugin
     private double satAlt[] = {7920.0}; // in feet, this is 1.5mi
     private double satLatLong[] = {1.23};
     private float satSpeed[] = {(float)123.45};
     
     String[]	 chanUnits = {"%", "C"};
     int[]       chanIdx = {0, 1};
     int[]       vals = {0, 0};
     String      rbnbHostName = "localhost";      //! @todo Grab from argv[1]
     boolean     connected = false;
     int         cacheSize = 10240;               //! @todo parse from command line
     int         bufDepth = 10;                   // ditto
     float       WaterVapor;     // a priori, may be incorrect
     float	 Airtemp;
     int         archiveSize = cacheSize * 10;    //! @todo parse from command line
     int         idx;
     double      samplespersec;
     int         numsamples;
     int         cursamples = 0;
     float[]     WaterVaporArr = {(float) 0.0};
     float []    AirTempArr = {(float) 0.0};
     long        delay = 1;
     long        period = 2;



    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
     //   System.out.println("Hello World!!!");
         

        SatSimKML satsim = new SatSimKML();
        RemindTask  reminder = new RemindTask(satsim);

        satsim.ParseArguments(args);
        satsim.ConnectToRbnb(); 
        satsim.SetupchannelMap();
           
        Timer timer = new Timer();
        long period = (long)(1000.0/satsim.samplespersec);
        System.out.println("Period is: " + period + "ms");
        timer.schedule(reminder,0,period);
        
        
   //     satsim.DisconnectFromRbnb();
        
        
    }
    
    public void ParseArguments(String[] args)
    {

        if (args.length > 0) {
      //      System.out.println("Arguments have been passed");
        	try {
            samplespersec = Integer.parseInt(args[0]);
            numsamples = Integer.parseInt(args[1]);
            System.out.println("Samples per second: " + " " + samplespersec + " Number of samples: " + numsamples);
            } catch (NumberFormatException e) {
                System.err.println("Argument must be an integer");
                System.exit(1);
            }
        } else if (args.length == 0) { // no args
        	samplespersec = 1/2.0;
        	numsamples = -1;
        }
    }
   
     public void ConnectToRbnb ()
    {
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

    }
    
   
        public int SetupchannelMap ()  
        {
            // The channel map is constant once set up, so only do it once
            try 
            {
                for(idx = 0; idx < 2; idx++) {
                chanIdx[idx] = cmap.Add(chanNames[idx]);
                cmap.PutMime(chanIdx[idx], "application/octet-stream");
        		cmap.PutUserInfo(chanIdx[idx], "units=" + chanUnits[idx]);
                }
                
                /*! @note add the trackKML channels */
                for(int i=0; i<chanNames.length; i++) {
                	if(i<2) {
                		continue;
                	} else {
                		cmap.Add(chanNames[i]);
                        cmap.PutMime(cmap.GetIndex(chanNames[i]), "application/octet-stream");
                	}
                }
                
                source.Register(cmap);
                
            } catch (SAPIException se) {
                System.out.println("Error adding to channel map!");          
                return (-1);
            }
            return (1);
        }
        
        public void DataGenerator ()
        {
		String inLine = null;					
		WaterVaporArr[0] = (float) (int)(Math.random() * 100);
		AirTempArr[0] =  (float) (int)(Math.random() * 25);
		/*! @note wraps coordinates around a 360 degree sphere */
		satLatLong[0] = (2*satLatLong[0] < 360)? 2*satLatLong[0] : 16;
                System.out.println("Relative Humidity: " + WaterVaporArr[0] + chanUnits[0] +
                		" AirTemperature: " + AirTempArr[0] + chanUnits[1] +
                		" Coordinates: " + satLatLong[0]);
			
        }
                
        public void FeedDataIntoRbnb () {
        	try {
        		// Timestamp all channels
        		cmap.PutTimeAuto("timeofday");
        		cmap.PutDataAsFloat32(chanIdx[0], WaterVaporArr);
        		cmap.PutDataAsFloat32(chanIdx[1], AirTempArr);
        		// KML puts
        		cmap.PutDataAsFloat64(cmap.GetIndex("Alt"), satAlt);
        		cmap.PutDataAsFloat64(cmap.GetIndex("Lat"), satLatLong);
        		cmap.PutDataAsFloat64(cmap.GetIndex("Lon"), satLatLong);
  // "Alt", "Lat", "Lon", "TrackID", "Type", "Classification", "Speed", "Heading"}
        		cmap.PutDataAsString(cmap.GetIndex("TrackID"), "trackid");
        		cmap.PutDataAsString(cmap.GetIndex("Type"), "type");
        		cmap.PutDataAsString(cmap.GetIndex("Classification"), "classification");
        		cmap.PutDataAsFloat32(cmap.GetIndex("Speed"), satSpeed);
        		
        		source.Flush(cmap);

        		// Throttle?
        		//! @todo Set rate from command line

        	}catch (SAPIException mse) 
        	{
        		System.out.println("Error saving data!" + mse);		
        	}
        }
        
         public void DisconnectFromRbnb ()
        {
           if(connected) 
           {
            System.out.println("Closing RBNB connection");
            
            // Tell RBNB to keep the data once we close
            source.Detach();
            
            /*! @note redundant */
            // source.CloseRBNBConnection();
        } 
        System.out.println("Done, exiting.");

        return;

        }
         
  /**
  * Implements TimerTask's abstract run method.
  */
  public void run(){
    //toy implementation
    System.out.println("Fetching mail...");
  }
  
      /*   public void InitiateTimerClass (){   
             scheduleAtFixedRate(reminder,delay, period);
         } */
      
   
}

