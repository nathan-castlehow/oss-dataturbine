package org.nees.rbnb;

/**
 * @author Lawrence J. Miller <ljmiller@sdsc.edu>
 * @author NEES Cyberinfrastructure Center (NEESit), San Diego Supercomputer Center
 * Please see copywrite information at the end of this file.
 * @since 051108
 *
 * svn keywords:
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 * $HeadURL$
 */
 
 /** Copyright (c) 2005, 2006, Lawrence J. Miller and NEESit
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *    * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the 
 * documentation and/or other materials provided with the distribution.
 *   * Neither the name of the San Diego Supercomputer Center nor the names of
 * its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Date;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import com.brtt.antelope.Orb;
import com.brtt.antelope.OrbPacket;
import com.brtt.antelope.OrbPacketChannel;
import com.brtt.antelope.OrbStat;
import com.brtt.antelope.OrbWaveformPacket;
import com.brtt.antelope.SourceName;
import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.Source;
import com.rbnb.sapi.SAPIException;
import org.nees.rbnb.Orb2RbnbTTask;

public class Orb2Rbnb {
    
  /** A variable of type @see com.brtt.antelope.Orb that represents a
   * persistent connection to an Antelope orb. */
   private Orb		myOrb;
   private String	orbName;
   private String	orbSourceFilter;
   public static	HashMap	orbSegTypeUnits = new HashMap();
   private double	scaleFactor = 1;
   /** Seed value for fake orb data. */
   private static double point = 0.0;
   
   /** Variables to manage @see com.rbnb.sapi.Source to manage
   * connections to RBNB. */ 
   public static int	DEFAULT_CACHE_SIZE	= 1024;
   public static String	DEFAULT_ARCHIVE_MODE	= "append";
   public static int	DEFAULT_ARCHIVE_SIZE	= 10240;
   private String	rbnbServer		= "localhost:3333";
   private int		rbnbCacheSize		= DEFAULT_CACHE_SIZE;
   private String	rbnbArchiveMode		= DEFAULT_ARCHIVE_MODE;
   private int		rbnbArchiveSize		= DEFAULT_ARCHIVE_SIZE;
   private HashMap	turbanHash;
   /** A variable made global for instantiation efficiency - make sure to clear before use
    * @see com.rbnb.sapi.ChannelMap */
   private ChannelMap cmap;
   
   /** A variable that defines the period at which the Antelope orb is queried. */
   private double samplingPeriod = 0.0;
   /** A variable that will serve as control amd timer for the thread that will
   * talk to te Antelope orb and RBNB. */
   private	Timer	orb2rbnbTimer;
   private	Date	theDate;
   public	boolean	isDebugging	= false;
   private	boolean	PHONY_ORB	= false;
   public	boolean dumpData	= false;
   
   private static final SimpleDateFormat ISO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS z");
   private static final SimpleDateFormat FULL_DATE_FORMAT = new SimpleDateFormat("EEEE, MMMM d, yyyy h:mm a");
   private static final SimpleDateFormat DAY_DATE_FORMAT = new SimpleDateFormat("EEEE h:mm a");
   private static final SimpleDateFormat TIME_DATE_FORMAT = new SimpleDateFormat("h:mm:ss a");
  
   /** constructor */
   public Orb2Rbnb (String orbToCall) {
      super ();
      this.theDate = new Date ();
      this.setOrbName (orbToCall);
      try {
         this.setOrb (this.getOrbName ());
      } catch (Exception e) {
         debugPrintln ("*** Couldn't talk to the orb \"" + 
                             this.getOrbName () + "\"  ***\n");
         e.printStackTrace ();
         System.exit (1);
      } try {
         this.setOrbSourceFilter (".*");
      } catch (Exception e) {
         debugPrintln ("*** Problem setting the orb search filter. ***\n");
         e.printStackTrace ();
      } 
  
      initOrbSegTypes();
      
      this.turbanHash = new HashMap();
      this.cmap = new ChannelMap();
      
      /* Add in a hook for ctrl-c's and other abrupt death */
      Runtime.getRuntime().addShutdownHook (new Thread() {
         public void run() {
           try {
              Iterator turbans = turbanHash.values().iterator();
              while (turbans.hasNext()) {
        	  Source someTurban = (Source)turbans.next();
        	  someTurban.Detach();
              }
              debugPrintln("Shutdown hook for " + Orb2Rbnb.class.getName());
           } catch (Exception e) {
              debugPrintln("Unexpected error closing " + Orb2Rbnb.class.getName());
           }
         } // run()
      }); //addHook()
   }
   
   /*Accessor/mutators*********************************************************/
   /** Antelope orb accessor */
   public Orb getOrb () {
      return this.myOrb;
   }
   
   /** Antelope orb accessor */
   public String getOrbName () {
      return this.orbName;
   }
   
   /** Antelope orb accessor */
   public String getOrbSourceFilter () {
      return this.orbSourceFilter;
   }
   
   /** Antelope orb accessor
   * @return the period in miliseconds between successive packet reaps os the
   * Antelope orb. */
   public double getSamplingPeriod () {
      return this.samplingPeriod;
   }
   
   /** A method that indicates whther or not this instance is returning fake
   * orb data.
   * @return PHONY_ORB */
   public boolean hasFakeOrb () {
      return this.PHONY_ORB;
   }
   
   /** Thread timer accessor. */
   public Timer getOrb2rbnbTimer () {
      return this.orb2rbnbTimer;
   }
   
   /** A method to poulate the orb segtypes reference hash */
   private void initOrbSegTypes() {
       orbSegTypeUnits = new HashMap();
       
       orbSegTypeUnits.put("A", "nm/sec/sec");		// acceleration                                                
       orbSegTypeUnits.put("B", "25mw/m/m");		// UV (sunburn) index (NOAA)                                   
       orbSegTypeUnits.put("D", "nm");			// displacement                                                
       orbSegTypeUnits.put("H", "pascal");		// hydroacoustic                                               
       orbSegTypeUnits.put("I", "pascal");		// infrasound                                                  
       orbSegTypeUnits.put("K", "kpascal");		// pore pressure 
       orbSegTypeUnits.put("P", "millibar");		// barometric pressure                                         
       orbSegTypeUnits.put("S", "nm/m");		// strain                                                      
       orbSegTypeUnits.put("T", "seconds");		// time                                                        
       orbSegTypeUnits.put("V", "nm/sec");		// velocity                                                    
       orbSegTypeUnits.put("W", "watts/m/m");		// insolation                                                  
       orbSegTypeUnits.put("a", "degrees");		// azimuth                                                     
       orbSegTypeUnits.put("b", "bits/second");		// bit rate                                                    
       orbSegTypeUnits.put("c", "counts");		// dimensionless integer                                       
       orbSegTypeUnits.put("d", "meters");		// depth or height (eg, water)                                 
       orbSegTypeUnits.put("h", "pH");			// hydrogen ion concentration                                  
       orbSegTypeUnits.put("i", "amperes");		// electric current                                            
       orbSegTypeUnits.put("m", "bitmap");		// dimensionless bitmap                                        
       orbSegTypeUnits.put("o", "milligrams/liter");	// dilution of oxygen (Mark VanScoy)                           
       orbSegTypeUnits.put("p", "percent");		// percentage                                                  
       orbSegTypeUnits.put("s", "meter/second");	// speed (eg, wind)                                            
       orbSegTypeUnits.put("t", "degrees_Celsius");	// temperature                                                 
       orbSegTypeUnits.put("u", "microsiemens/cm");	// conductivity                                                
       orbSegTypeUnits.put("v", "volts");		// electric potential         
       
   } // initOrbSegTypes()
   
   /** A method that will @return a String containing commonly applicable
   * debugging values. */
   public String getDebugString () {
      String retVal = "\n*** Debug Output ***\n" +                               
      	 "*** system time = " + formatDate(System.currentTimeMillis() / 1000.0) + " ***\n" + 
         "*** orb name: " + this.getOrbName () + "\n" +
         "*** orb: " + this.getOrb () + "\n" +
         "*** dataTurbine server: " + this.rbnbServer + "\n" +
         "*** dataTurbine sources: " + this.turbanHash.toString();                                 
      return null; //retVal;
   }
   
   /** Accessor */
   public Date getDate () {
      return this.theDate;
   }

   /** Mutator that sets to the current time. */
   public void resetDate () {
      this.theDate = new Date ();
   }

   /** Antelope orb mutator
      * @param the period in miliseconds between successive packet reaps of the
      * Antelope orb */
   protected void setSamplingPeriod (double newSamplingPeriod) {
      this.samplingPeriod = newSamplingPeriod;
   }
   
   /** Antelope orb mutator
   * @throws Exception */
   protected void setOrbSourceFilter (String newFilter) throws Exception {
      this.orbSourceFilter = newFilter;
      int numSrcs = 0;
      if (this.getOrb () != null) {
         numSrcs = this.getOrb ().select (newFilter);
      } 
      debugPrintln ("*** The filter \"" + this.orbSourceFilter + "\" represents " + Integer.toString (numSrcs) +
                 " sources in the orb \"" + this.getOrbName () + "\" ***");
   } // setOrbSourceFilter()
   
   /** Antelope orb mutator
   * @throws Exception */
   protected void setOrb (String newOrbName) throws Exception {
      if (newOrbName != null) {
         this.myOrb = new Orb (newOrbName, "r&");
      } else {
         this.myOrb = null;
      }
      this.setOrbName (newOrbName);
   } // setOrb ()
   
   /** Antelope orb mutator */
   protected void setOrbName (String newName) {
      this.orbName = newName;
   }
   /*Accessor/mutators*********************************************************/
   
   /***************************************************************************/
   /** A mehod to examine the sourcename fields of an Antelope orb packet */
   public String examineChannelSourceName(OrbPacketChannel anOrbPacketChannel) {
       String retval = "";
       SourceName anOrbPacketChannelSourceName = anOrbPacketChannel.srcname;
       
       retval += "&&& ORB channel sourcename TOSTRING: ";
       retval += anOrbPacketChannelSourceName + "\n";
       // composed of net + sta + chan + loc + /type;
       
       retval += "&&& ORB channel sourcename CHAN: ";
       retval += anOrbPacketChannelSourceName.chan + "\n";
       
       retval += "&&& ORB channel sourcename FORMAT: ";
       retval += anOrbPacketChannelSourceName.format + "\n";

       retval += "&&& ORB channel sourcename LOC: ";
       retval += anOrbPacketChannelSourceName.loc + "\n";

       retval += "&&& ORB channel sourcename NAME: ";
       retval += anOrbPacketChannelSourceName.name + "\n";

       retval += "&&& ORB channel sourcename NET: ";
       retval += anOrbPacketChannelSourceName.net + "\n";

       retval += "&&& ORB channel sourcename STA: ";
       retval += anOrbPacketChannelSourceName.sta + "\n";

       retval += "&&& ORB channel sourcename TYPE: ";
       retval += anOrbPacketChannelSourceName.type + "\n";
       
       retval += "&&& ORB channel CALIB: ";
       retval += anOrbPacketChannel.calib + "\n";
       
       retval += "&&& ORB channel CALPER: ";
       retval += anOrbPacketChannel.calper + "\n";
       
       retval += "&&& ORB channel DATASIZE: ";
       retval += anOrbPacketChannel.datasize + "\n";
       
       retval += "&&& ORB channel SAMPRATE: ";
       retval += anOrbPacketChannel.samprate + "\n";
       
       retval += "&&& ORB channel SEGTYPE: ";
       retval += anOrbPacketChannel.segtype + "\n";
       
       retval += "&&& ORB channel TIME: ";
       retval += anOrbPacketChannel.time + "\n";
       
       return retval;
   } // examineChannelSourceName ()
  
   /***************************************************************************/
   /** a ghetto method to do debug output... needed because log4j cannot be
     * reliably shipped and bndled with orb2rbnb.jar */
   public void debugPrintln(String printMe) { 
       if (this.isDebugging) { System.out.println( formatDate(System.currentTimeMillis() / 1000.0) + "\n" +
	       			printMe );
       } else return;
   } // debugPrintln()
   
   /***************************************************************************/
   /** A mehod to replace the @see com.brtt.antelope.SourceName toString method
    * @param sourecname to be printed*/
   public String orbSourceNameToString(SourceName srcName) {
       String retval = "";
       retval +=	srcName.net  + "_" +
       			srcName.sta  + "_" +
       			srcName.chan + "_" +
       			srcName.loc;
       return retval;
   } // orbSourceNameToString()
   
   /***************************************************************************/
   /** A mehod that will generate @return an array of timestamps for each data point in
    * an orb channel based on the @param sampling rate from the channel and
    * the start time from the @param orb packet
    * @see com.brtt.antelope.OrbPacketChannel
    */
   double[] getOrbPacketChannelTimes(OrbPacketChannel anOrbPacketChannel, OrbPacket anOrbPacket) {
       double[] retval = new double[anOrbPacketChannel.datasize];
       double t0 = anOrbPacket.time;
       double dt = 1.0 / anOrbPacketChannel.samprate;
       
       for (int i=0; i<anOrbPacketChannel.datasize; i++) {
	   retval[i] = t0 + i*dt;
	   if (this.dumpData) { // -D flag
	       debugPrintln("TIME" + Integer.toString(i) +" " + formatDate(retval[i]));
	   }
       }
       
       return retval;
   } // getOrbPacketChannelTimes()
   
   /***************************************************************************/
   /** A mehod that will @return data with calibration factor applied from
    * @param an input orb packet channel
    * @see com.brtt.antelope.OrbPacketChannel
    */
   float[] getCalibratedPacketOrbData(OrbPacketChannel anOrbPacketChannel) {
       float[] retval = new float[anOrbPacketChannel.datasize];
       for (int i=0; i<anOrbPacketChannel.datasize; i++) {
	   if (this.dumpData) { // -D flag
	       debugPrintln("RAW" + Integer.toString(i) +"  " + Integer.toString(anOrbPacketChannel.data[i])); 
	       debugPrintln("CAL" + Integer.toString(i) +"  " + Float.toString((float)(anOrbPacketChannel.data[i] * anOrbPacketChannel.calib * this.scaleFactor)) ); 
	   }
	   retval[i] = (float)(anOrbPacketChannel.data[i] * anOrbPacketChannel.calib * this.scaleFactor);
       } // for
       return retval;
   } // getCalibratedPacketOrbData()
   
   /***************************************************************************/
   /** A mehod that will format dates nicely when timestamps are input as
     * milliseconds since the epoch and such */
   public static String formatDate(double date) {
	return ISO_DATE_FORMAT.format(new Date(((long)(date*1000))));
   } // formatDate()
  
   /***************************************************************************/
   /** A method that is the heart of the programme. This grabs data from an
   * Antelope orb and then translates it and forwards it to RBNB.
   * @see orb2RbnbTask
   * @parameter sampling rate - the period at which data will be queried and
   * forwarded */
   public void doAntelopeToRBNB (double frequency) throws Exception {
      if (this.hasFakeOrb ()) {
         debugPrintln ("*** Fake orb enabled - using phony data ***");
      }
      
      if (0 < frequency) {
         this.orb2rbnbTimer = new Timer();
         Orb2RbnbTTask orbTask = new Orb2RbnbTTask(this);
         
         this.getOrb2rbnbTimer().scheduleAtFixedRate(orbTask, this.getDate(), (long)frequency);
      
      } else { // do interactive key-hit DAQ
         BufferedReader in = new BufferedReader(new InputStreamReader (System.in));
         String retVal = null;
         OrbPacket orbPacket = null;
         
         while (true) {
            System.out.print("Hit <enter> to orb2rbnb the orb: \"" +
                              this.getOrbName() + "\" or 'q' to quit >\n");
            try {
               if (this.getOrb() != null) {
        	   this.getOrb().select(orbSourceFilter);
        	   orbPacket = this.getOrb().reap(true);
                  if (orbPacket != null) { // then the reap reaped
                      System.out.println("Reaped an ORB packet.");
                  }
               }
               this.putOrbData2RBNB(orbPacket); 
               //this.checkOrb ();
               debugPrintln (this.getDebugString());
            } catch (Exception e) {
               debugPrintln("*** Problem putting the orb to rbnb. ***");
               e.printStackTrace();
            }
            try {
               if ((char)in.read() == 'q') System.exit(0);
            } catch (Exception e) {
               /* keep going */
            }
         } // while
      } // else
   } // doAntelopeToRBNB()
   
   /***************************************************************************/
   /** A mehod that will extract the data from an
     * @param Antelope orb packet
     * and then put the parsed data into DataTurbine. */
   public void putOrbData2RBNB(OrbPacket anOrbPacket) throws Exception {
     if (anOrbPacket == null) {
	 // debugPrintln("NULL orb packet");
	 return;
     }
     debugPrintln("&&& REAPED an ORB packet id: " + Integer.toString(anOrbPacket.pktid));
     String anOrbPacketType = anOrbPacket.srcname.type;
     debugPrintln("&&& ORB packet type: " + anOrbPacketType);
     
     if (anOrbPacket.srcname.type.equals("waveform")) {
	 double orbPacketTime = anOrbPacket.time;
	 debugPrintln("&&& ORB packet time: " + Double.toString(orbPacketTime));
	 debugPrintln("&&& ORB packet nice time: " + formatDate(orbPacketTime)); 
     
	 List orbPacketChannels = ((OrbWaveformPacket)(anOrbPacket)).channels;
	 debugPrintln("&&& ORB channel count: " + 
		 Integer.toString(orbPacketChannels.size()));
	 
	 if (! orbPacketChannels.isEmpty()) {
	     Iterator orbChannelIterator = orbPacketChannels.iterator();
	     int iterationCounter = 0;
	     while (orbChannelIterator.hasNext()) {
		 iterationCounter++;
		 OrbPacketChannel anOrbPacketChannel = (OrbPacketChannel)orbChannelIterator.next();
		 
		 debugPrintln(examineChannelSourceName(anOrbPacketChannel));
		 
		 String dtSourceName = orbSourceNameToString(anOrbPacketChannel.srcname);
		 Source dtSource = addRbnbSource(dtSourceName);
		 cmap.Clear();
		 // data channels with and without calibrations applied
		 int chanDexRaw = cmap.Add(Integer.toString(iterationCounter) + "_raw");
		 int chanDexCal = cmap.Add(Integer.toString(iterationCounter) + "_cal");
		 String userInfoStringRaw = "calib=" + Double.toString(anOrbPacketChannel.calib) + "segtype=" + anOrbPacketChannel.segtype;
		 String userInfoStringCal = "";
		 if (this.scaleFactor != 1) {
		     DecimalFormat dFormat = new DecimalFormat("#.###E00");
		     StringBuffer formBuf = new StringBuffer();
		     FieldPosition formatFp = new FieldPosition(DecimalFormat.INTEGER_FIELD);
		     
		     dFormat.format((1 / this.scaleFactor), formBuf, formatFp);
		     userInfoStringCal = "units=" + formBuf.toString() + " x " + (String)orbSegTypeUnits.get(anOrbPacketChannel.segtype);
		 } else {
		     userInfoStringCal = "units=" + (String)orbSegTypeUnits.get(anOrbPacketChannel.segtype);
		 }
		 
		 if (this.scaleFactor != 1) {
		     userInfoStringCal += ",scale=" + Double.toString(this.scaleFactor);
		 }
		 cmap.PutUserInfo(chanDexRaw, userInfoStringRaw);
		 cmap.PutUserInfo(chanDexCal, userInfoStringCal);
		 dtSource.Register(cmap);
		 cmap.PutTimes(getOrbPacketChannelTimes(anOrbPacketChannel, anOrbPacket));
		 cmap.PutDataAsInt32(chanDexRaw, anOrbPacketChannel.data);
		 cmap.PutDataAsFloat32(chanDexCal, getCalibratedPacketOrbData(anOrbPacketChannel));
		 dtSource.Flush(cmap);
	     } // while
	 } // if channels
     } // if waveform
   }
   
   /***************************************************************************/
   /** A mehod that will check @see turbanHash for @param a dt source
    * and create that source and add it to the hash if it is not present.
    * @return the source referree to by the input label
    */
    Source addRbnbSource(String sourceLabel) throws SAPIException {
	if (! this.turbanHash.containsKey(sourceLabel)) {
	    Source aSource = new Source(this.rbnbCacheSize, this.rbnbArchiveMode, this.rbnbArchiveSize);
	    aSource.OpenRBNBConnection(this.rbnbServer, sourceLabel);
	    this.turbanHash.put(sourceLabel, aSource);
	} // if
	return (Source)( this.turbanHash.get(sourceLabel) );
    }
   
   /***************************************************************************/
   /** A mehod that will examine the orb to which orb2rbnb is currently
   * connected and display the diagnostic information if in debug mode.
   * @see com.brtt.antelope.OrbStat
   * @see com.brtt.antelope.Orb */
   public void checkOrb () {
      if (this.getOrb () == null) {
         debugPrintln ("*** NULL orb ***");
         return;
      } else {
         try {
            OrbStat orbStat = this.getOrb ().stat ();
            debugPrintln (orbStat.getList ());
            //int tellPkt = this.getOrb ().tell ();
            //debugPrintln ("*** orb tell pkt id: " + Integer.toString (tellPkt));
         } catch (Exception e) {
            debugPrintln ("*** Problem getting orb status ***");
            e.printStackTrace ();
         }
      }
      return;
   }

   /** Standard toString override that queries and displays current orb data. */
   public String toString () {
      return this.getDebugString ();
   } // toString ()
   
   protected static String getCVSVersionString() {
      return (
            "$LastChangedDate$\n" +
            "$LastChangedRevision$" +
            "$LastChangedBy$" +
            "$HeadURL$"
             );
   }
   
   /** main method */
   public static void main (String[] args) {
      Orb2Rbnb testOrb = new Orb2Rbnb (null);
      String testFilter = null;
      
      /************************************************************************/
      /** command-line argument handling */
      Options opts = new Options ();
      CommandLineParser parser = new BasicParser();
      CommandLine cmd = null;
      
      opts.addOption ("a", false, "about");
      opts.addOption ("d", false, "print debug output");
      opts.addOption ("D", false, "dump data to stdout; extremely VERBOSE; only valid with -d");
      opts.addOption ("f", true,  "filter");
      opts.addOption ("o", true,  "orbname");
      opts.addOption ("S", true,  "scale factor");
      opts.addOption ("r", true,  "DataTurbine server that this program is a source for");
      opts.addOption ("s", true,  "sampling period (ms)");
      opts.addOption ("v", false, "print version info");
      opts.addOption ("z", true,  "RBNB cache size (frames)");
      opts.addOption ("Z", true,  "RBNB archive size (frames)");
 
      try {
         cmd = parser.parse (opts, args); 
      } catch (ParseException pe) {
         HelpFormatter formatter = new HelpFormatter ();
         formatter.printHelp ("Orb2Rbnb", opts);
         System.exit (0); }
  
      if (cmd.hasOption ("a")) {
         System.out.println("About: this a program to talk to an " +
                              "Antelope ORB, get its waveform data, " +
                              "translate this data into a form that RDV " +
                              "likes, and then transmit this data to DataTurbine.");
         System.exit (0);
      } if (cmd.hasOption ("d")) {
	  testOrb.isDebugging = true;
      } if (cmd.hasOption ("D")) {
	  testOrb.dumpData = true;
      } if (cmd.hasOption ("f")) {
         try {
            testFilter = cmd.getOptionValue ("f");
         } catch (Exception e) {
             System.out.println ("*** Problem setting the search filter. ***");
            e.printStackTrace ();
         }
      } if (cmd.hasOption ("o")) {
         try {
            testOrb.setOrb (cmd.getOptionValue ("o"));
         } catch (Exception e) {
             System.out.println ("*** Couldn't talk to the orb \"" + 
                                cmd.getOptionValue ("o") + "\" ***");
            e.printStackTrace ();
         }
      } if (cmd.hasOption ("S")) {
	  double sftmp;
	  try {
	      sftmp = Double.parseDouble(cmd.getOptionValue ("S"));
	      testOrb.scaleFactor = sftmp;
	  } catch(Exception e) {
	      System.out.println("Enter a numeric value for the -s option.");
	      System.exit(1);
	  }
      } if (cmd.hasOption ("r")) {
	  testOrb.rbnbServer = (cmd.getOptionValue ("r"));
      } if (cmd.hasOption ("s")) {
            testOrb.setSamplingPeriod (Double.parseDouble (cmd.getOptionValue ("s")));
      } if (cmd.hasOption ("v")) {
           System.out.println(getCVSVersionString());
           System.exit (0);
      } if (cmd.hasOption ("z")) {
	try {
	    testOrb.rbnbCacheSize = Integer.parseInt(cmd.getOptionValue("z"));
	} catch(Exception e) {
	    System.out.println(e + "Enter a numeric value for the -z option.");
	    System.exit(1);
	}
      } if (cmd.hasOption ("Z")) {
	  try {
	      testOrb.rbnbArchiveSize = Integer.parseInt(cmd.getOptionValue("Z"));
	  } catch(Exception e) {
	      System.out.println("Enter a numeric value for the -Z option.");
	      System.exit(1);
	 }
      } // opts
      
      /* End of command-line handling */
      /************************************************************************/
      if (testFilter != null ) {
         try {
            testOrb.setOrbSourceFilter (testFilter);
         } catch (Exception e) {
            testOrb.debugPrintln ("*** Problem setting the ORB search filter. ***");
            e.printStackTrace ();
         }
      }
      testOrb.debugPrintln ("*** The period at which to grab data from the Antelope orb is set to " +
                 Double.toString (testOrb.getSamplingPeriod ()) + "ms, or " + 
                 Double.toString (1000.0 / testOrb.getSamplingPeriod ()) + "Hz. ***");
      try {
         testOrb.doAntelopeToRBNB (testOrb.getSamplingPeriod ());
      } catch (Exception e) {
	  testOrb.debugPrintln ("*** Problem launching the sandwich-ware task. ***");
         e.printStackTrace ();
      }
      
   } // main ()

} // class
