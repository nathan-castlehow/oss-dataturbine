package edu.sdsc.rtdsm.dig.sites.lake;

import java.util.*;

import edu.sdsc.rtdsm.framework.feedback.FeedbackAgent;
import edu.sdsc.rtdsm.framework.util.Debugger;
import edu.sdsc.rtdsm.framework.util.Constants;
import edu.sdsc.rtdsm.drivers.turbine.*;
import edu.sdsc.rtdsm.drivers.turbine.util.*;

public class LakeFeedbackAgent extends FeedbackAgent {

  private String localHostIP = null;
  Hashtable<String, TurbineSrcConfig> srcConfigHash = 
   new Hashtable<String,TurbineSrcConfig>();
  
  public LakeFeedbackAgent(int port){
    super(port);

    try {

      java.net.InetAddress localMachine = java.net.InetAddress.getLocalHost();	
      localHostIP = localMachine.getHostAddress();
      Debugger.debug (Debugger.TRACE, "Hostname of local machine: " +
          localHostIP);
    }
    catch(java.net.UnknownHostException uhe) {
      throw new IllegalStateException("LakeFeedbackAgent: Could not find " +
        "the IP address of the local machine");
    }
  }

  public void processFeedback(String source, String feedback){

    Debugger.debug(Debugger.TRACE, "Got Feedback : " + source +
        " Feedback Msg:" + feedback);

    // Low frequency data for each source
    // Do not keep the source alive for long time. 
    // Send feedback and disconnect
    TurbineSrcConfig srcConfig = null;

    if(srcConfigHash.containsKey(source)) {

      srcConfig = srcConfigHash.get(source);
    }
    else {
      srcConfig = addConfigInfo(source);
    }

    TurbineSource src = new TurbineSource(srcConfig);
    boolean connected = src.connect();
    Debugger.debug(Debugger.TRACE, "Source connected = " + connected);
    if(connected == false){
      throw new IllegalStateException("Could not connect the feedback source "+
          " for the lake source \"" + source + "\"");
    }
    Debugger.debug(Debugger.TRACE , "Feedback source name: " + srcConfig.getName());
    src.insertData(Constants.FEEDBACK_CHANNEL_NAME,(Object)feedback);
    src.flush();
    src.disconnect();
  }

  public TurbineSrcConfig addConfigInfo(String lakeSource) {

    // The name of the new turbine source will the 
    // name of the actual lake source appended by "FeedbackSrc"
    String srcName = lakeSource + Constants.FEEDBACK_SRC_SUFFIX;
    String channelName = Constants.FEEDBACK_CHANNEL_NAME;
    Vector<String> channelVec = new Vector<String>();
    Vector<Integer> channelDatatypeVec = new Vector<Integer>();

    channelVec.addElement(channelName);
    channelDatatypeVec.addElement(Constants.DATATYPE_STRING_OBJ);

    TurbineSrcConfig srcConfig = new TurbineSrcConfig(srcName);
    srcConfig.setChannelInfo(channelVec, channelDatatypeVec);

    TurbineServer server = new TurbineServer();
    srcConfig.setServer(server);
    server.serverAddr = localHostIP;
    server.userName = Constants.DEFAULT_SERVER_USERNAME;
    server.password = Constants.DEFAULT_SERVER_PASSWORD;
    srcConfigHash.put(lakeSource, srcConfig);
    return srcConfig;
  }

  public static void main(String args[]) {


    int port = Constants.DEFAULT_FEEDBACK_PORT;
    switch(args.length) {
      case 0:
        break;
      case 1:
        port = Integer.parseInt(args[0]);
        break;
      default:
        System.err.println("Usage: java edu.sdsc.rtdsm.dig.sites.lake.FeedbackAgent <port>");
        System.exit(-1);
    }

    LakeFeedbackAgent fa = new LakeFeedbackAgent(port);
    fa.connectAndListen();
  }
}

