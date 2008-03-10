package org.nees.rbnb;

/**
*  Orb2RbnbTTask.java
*
*  @author Lawrence Miller, NEESit SDSC
*  @since 8/1/05.
*/

import java.util.TimerTask;
import com.brtt.antelope.OrbPacket;
import org.nees.rbnb.Orb2Rbnb;

public class Orb2RbnbTTask extends TimerTask {
   
   private Orb2Rbnb myCaller;
   /** A constructor that gathers a reference to the @see Orb2Rbnb instance that
   spawned this thread in order to communicate gathered information. */
   public Orb2RbnbTTask(Orb2Rbnb maker) {
      super();
      this.myCaller = maker;
   }
   
   public void run() {
      OrbPacket orbPacket = null;
      
      try {
         if(this.myCaller.getOrb () != null) {
            orbPacket = this.myCaller.getOrb().reap(true);
            //this.myCaller.checkOrb();
         }
      } catch(Exception e)  {
	  System.out.println("$$$ " + Orb2Rbnb.formatDate(System.currentTimeMillis() / 1000.0) + " Problem reaping an orb packet.\n" + e);
	  e.printStackTrace();
      }
      
      try {
	  if (orbPacket != null || this.myCaller.hasFakeOrb()) { 
		    this.myCaller.putOrbData2RBNB(orbPacket);
	  } else if (!this.myCaller.hasFakeOrb() && this.myCaller.isDebugging) {
	      //System.out.println("$$$ NULL orb packet. $$$\n");
	  }
      } catch (Exception e) {
	  System.out.println("$$$ " + Orb2Rbnb.formatDate(System.currentTimeMillis() / 1000.0) + " Problem putting orb to rbnb.\n" + e);
         e.printStackTrace();
      }
   } // run ()
} // class
