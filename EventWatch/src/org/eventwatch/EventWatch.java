package org.eventwatch;

//EventWatch
//subscribe to an RBNB channel, update Google Calendar upon new data
//Matt Miller 08/2011

// command line:
// java -jar EventWatch.jar <rbnbServer> <eventName> <monitorChan> <updateInc> <idleTimeout> <gmailUser> <gmailPW>
// example:
// java -jar EventWatch.jar localhost MyCalendar Chat/chat.txt 6 60 matt.miller42 mypasswd

import com.google.gdata.client.calendar.*;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.calendar.*;
import com.google.gdata.data.extensions.*;
import java.net.URL;
import java.util.TimeZone;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import com.rbnb.sapi.*;

public class EventWatch {

private static String rbnbServer = "localhost:3333";
private static String monitorChan = "_Metrics/MemoryUsed";	// silly example but sure to be there
private static String eventName = "Event";					// this is the calendar name on gcal

 private static String gmailUser = "foo";		// separately append @gmail.com suffix
 private static String gmailPW = "bar";			// provided plain-text command line (bleh)

 private static double updateInc = 60.;        	// update/check interval (sec)
 private static double idleTimeOut = 600.;		// idle period timeout (sec)
 
 private static CalendarService myService;		// calendar info globals
 private static URL postUrl;
 private static boolean activeState=false;		// activity status
 private static boolean updateMode=false;		// output every new point 
 private static int errCount=0;
 private static final int MAXERR=5;
 
 //---------------------------------------------------------------------------------
 // constructor
 public EventWatch() {}

 public final static void main(String[] args) {
     Timer myTimer;

     int i=0;
     if(args.length>i) rbnbServer 	= args[i++];
     if(args.length>i) eventName 	= args[i++];
     if(args.length>i) monitorChan 	= args[i++];		// simple position dependent args for now
     if(args.length>i) updateInc 	= Double.parseDouble(args[i++]);
     if(args.length>i) idleTimeOut 	= Double.parseDouble(args[i++]);
     if(args.length>i) gmailUser 	= args[i++];
     if(args.length>i) gmailPW 		= args[i++];
     if(args.length>i) updateMode   = (args[i++].equals("true"))?true:false;
     
     System.err.println("EventWatch, rbnbServer: "+rbnbServer);
     System.err.println("EventWatch, eventName: "+eventName);	// this is the calendar name
     System.err.println("EventWatch, monitorChan: "+monitorChan);
     System.err.println("EventWatch, updateInc: "+updateInc);
     System.err.println("EventWatch, idleTimeOut: "+idleTimeOut);
     System.err.println("EventWatch, gmailUser: "+gmailUser);
     System.err.println("EventWatch, gmailPW: "+gmailPW.charAt(0)+"*****");		// bleh
     
     initCalendar();
     activeState=false;

     myTimer = new Timer();
     myTimer.schedule(new TimerTask() {
         @Override public void run() { TimerMethod(); }
     }, 0,(int)(updateInc*1000.));         // check interval
     
     try {
     	while(true) Thread.sleep(100000);		// ZZZ....
     } catch (InterruptedException e) {}
     finally {
         updateCalendar(eventName+": Stop Monitor.", "Shutdown");	// doesn't catch?
     }
 }

 //---------------------------------------------------------------------------------
 // initialization stuff
 
 public static void initCalendar() {
     // Create a CalenderService and authenticate
     String guser;
     if(gmailUser.endsWith("@gmail.com")) 	guser = gmailUser;
     else									guser = gmailUser+"@gmail.com";
     
     try {  
         myService = new CalendarService(guser);
         myService.setUserCredentials(guser, gmailPW);
         postUrl = null;
         
      // Get list of calendars you own, find the ID of the one with title matching spec
         URL feedUrl = new URL("https://www.google.com/calendar/feeds/default/owncalendars/full");
         CalendarFeed resultFeed = myService.getFeed(feedUrl, CalendarFeed.class);
         System.err.println("Calendars you own:");
         for (int i = 0; i < resultFeed.getEntries().size(); i++) {
           CalendarEntry entry = resultFeed.getEntries().get(i);
           String calTitle = entry.getTitle().getPlainText();
           System.err.println("\t" + calTitle);

           if(eventName.equals(calTitle)) {	// form postUrl for this titled entry
//         	  postUrl = new URL(entry.getId());		// bleh need to massage...
         	  postUrl = new URL(entry.getId().replace("/default/calendars","") + "/private/full");
         	  System.err.println("PostUrl for "+eventName+": "+postUrl);
         	  break;
           }
         }
         if(postUrl == null) {		// didn't find a match
         	System.err.println("ERROR: could not find calendar feed for: \""+eventName+"\"");
         	System.err.println("Exiting.");
         	System.exit(-1);
         }
     } catch (Exception e) {
         e.printStackTrace();
     	System.err.println("Exiting.");
         System.exit(-1);
     }
 }

 //---------------------------------------------------------------------------------
 // update Google calendar on activity status change
 
 public static void updateCalendar(String calTitle, String calMsg) {

     try {
         // create event
         CalendarEventEntry myEntry = new CalendarEventEntry();
         myEntry.setTitle(new PlainTextConstruct(calTitle));
         myEntry.setContent(new PlainTextConstruct(calMsg));

         // timestamp at event time
         DateTime startTime; Date dTime;
         long tOffset = 0;    // msec offset for reminder to fire?

         dTime = new Date((long)System.currentTimeMillis() + tOffset);
         startTime = new DateTime(dTime, TimeZone.getDefault());    // avoid UTC-EDT time shift

         DateTime endTime = startTime;
         When eventTimes = new When();
         eventTimes.setStartTime(startTime);
         eventTimes.setEndTime(endTime);
         myEntry.addTime(eventTimes);

         // Send the request (ignore the response):
         myService.insert(postUrl, myEntry);
         System.err.println("Update Calendar: " + calTitle + ", "+dTime);
         errCount = 0;		// reset on success
     } catch (Exception e) {
         e.printStackTrace();
         errCount ++;
         if(errCount > MAXERR) {		// heuristic
        	 System.err.println("Exiting.");
        	 System.exit(-1);		//  don't thrash on google calendar update
         }
     }
 }

 //---------------------------------------------------------------------------------
 // timer-driven RBNB data check
 
 static double lastTime=0.;
 static double clockTime=0.;
 static double lastClockTime=0.;

 private static void TimerMethod()
 {
// 	System.err.println("Timer");
 	long iclockTime = System.currentTimeMillis();
     clockTime = iclockTime / 1000.;        // seconds

     if(lastClockTime == 0.) {
         lastClockTime = clockTime;
         updateCalendar(eventName+": Monitoring", "Startup");
     }

     try {
         // Create a sink
         Sink sink=new Sink();
         sink.OpenRBNBConnection(rbnbServer,"EventWatch");

         // Pull data from the server:
         ChannelMap rMap = new ChannelMap();
         rMap.Add(monitorChan);

         sink.Request(rMap, 0., 0., "newest");
         ChannelMap gMap = sink.Fetch(10000);
         sink.CloseRBNBConnection();        // open/close every time

         String calMsg = "null";
         double eTime = 0.; double dTime = 0.; double idleTime = 0.;

         if(gMap != null && gMap.NumberOfChannels() > 0) {    // got data
             if(gMap.GetType(0) == ChannelMap.TYPE_STRING)
            	 	calMsg = gMap.GetDataAsString(0)[0];
             else	calMsg = "[Binary Data]";
             eTime = gMap.GetTimeStart(0);

             if(lastTime == 0.) lastTime = eTime;
             dTime = eTime - lastTime;

             if(gMap != null && gMap.NumberOfChannels() > 0 && dTime > 0.) {
                 lastClockTime = clockTime;    // active data, update time
             }
             idleTime = clockTime - lastClockTime;
         }

         System.err.println("EventWatch("+eventName+
         		"), active: "+activeState+
         		", "+new Date(iclockTime)+
         		", idle: "+(float)idleTime);


         if((activeState == false) && (dTime>0.)) {
             System.err.println(eventName+": Active!");
             updateCalendar(eventName+": Active!", calMsg);
             activeState=true;
         }
         else if((activeState==true) && (idleTime > idleTimeOut)) {
             System.err.println(eventName+": Idle.");
             updateCalendar(eventName+": Idle.", calMsg);
             activeState=false;
         }
         else if(updateMode && dTime > 0.) {
             System.err.println(eventName+": Update.");
             updateCalendar(eventName+": Update.", calMsg);        	 
         }

         lastTime = eTime;

     } catch (Exception e){ 
     	System.err.println("OOPS, Exception in RBNB fetch: "+e);
//     	System.exit(-1);		// no exit keep trying?
     }
 }
}
