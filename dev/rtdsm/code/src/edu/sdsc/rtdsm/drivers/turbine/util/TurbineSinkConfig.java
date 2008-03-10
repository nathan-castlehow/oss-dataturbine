package edu.sdsc.rtdsm.drivers.turbine.util;

import org.w3c.dom.*;
import org.xml.sax.*;
import javax.xml.parsers.*;
import com.rbnb.sapi.*;
import java.util.*;

import edu.sdsc.rtdsm.framework.sink.*;
import edu.sdsc.rtdsm.framework.util.*;
import edu.sdsc.rtdsm.framework.feedback.SinkFeedbackAgent;

// TurbineSinkConfig
//  TurbineServer1        |
//    TurbineChannel1     | TurbineSink1
//    TurbineChannel2     |
//  TurbineServer2
//    TurbineChannel3     |
//    TurbineChannel4     | TurbineSink2
//    TurbineChannel5     |

public class TurbineSinkConfig implements SinkConfig {

  public static final String SERVER_TAG = "server";
  public static final String URI_TAG = "uri";
  public static final String USERNAME_TAG = "username";
  public static final String PASSWORD_TAG = "password";
  public static final String FEEDBACK_SERVER_TAG = "feedbackServer";
  public static final String FEEDBACK_PORT_TAG = "feedbackPort";
  public static final String CHANNEL_TAG = "channel";
  public static final String CH_NAME_TAG = "name";
  public static final String CH_SRC_NAME_TAG = "source";
  public static final String CH_DATATYPE_TAG = "dataType";
  public static final String CH_TIMEOUT_TAG = "timeout";
  public static final String CH_POLL_INTERVAL_TAG = "pollInterval";
  public static final String CH_REQ_MODE_TAG = "reqMode";

  public static final int DEFAULT_CACHE_SIZE = 100;
  public static final String DEFAULT_ARCHIVE_MODE = "yes";
  public static final int DEFAULT_ARCHIVE_SIZE = 300;

  public static final int MONITOR_MODE = 0;
  public static final int SUBSCRIBE_MODE = 1;
  public static final int POLL_MODE = 2;

  public String sinkName;
  public Hashtable<String,TurbineServer> turbineServerHash = 
    new Hashtable<String, TurbineServer>();

  // Keeping a handle of all servers, so that we dont have to iterate the hash
  // when just the list of servers are needed.
  public Vector<TurbineServer> turbineServerHandleVec = new Vector<TurbineServer>();

  // Needed for feedback
  public Hashtable<String,TurbineServer> sourceToServerHash = 
    new Hashtable<String, TurbineServer>();

  public SinkCallBackListener callbackHandler;

  public TurbineSinkConfig(String name) {
    sinkName = name;
  }

  public String getName(){
    return sinkName;
  }

  public void parse(Element orbType){
    
    NodeList orbList = orbType.getChildNodes();

    for (int k = 0; k < orbList.getLength(); k++) {
      
      Node node_k = orbList.item(k);

      if (node_k.getNodeType() == Node.ELEMENT_NODE) {

        Element ele = (Element)node_k;
        String tagName = ((Element) node_k).getTagName();

        if(tagName.equals(TurbineSinkConfig.SERVER_TAG)) {
          // Process server tag
          parseServer(node_k);
        }
        else{
          throw new IllegalStateException("The xml document has an invalid " +
              "element:" + tagName);
        }
      }
    }

    printSinkData(Debugger.TRACE);
  }

  public void printSinkData(int level) {

    Debugger.debug( level,"Sink params:");
    Debugger.debug( level,"name= " + sinkName);

    Debugger.debug( level,"\nNumber of servers: " + turbineServerHandleVec.size());

    for (int s=0;s<turbineServerHandleVec.size();s++){

      turbineServerHandleVec.elementAt(s).printServerInfo(level);
    }
  }

  public void parseServer(Node serverNode) {

    if(serverNode.getNodeType() == Node.ELEMENT_NODE &&
        ((Element) serverNode).getTagName().equals(
        TurbineSinkConfig.SERVER_TAG)) {

      Element serverEle = (Element)serverNode;
      if(! serverEle.hasAttribute(TurbineSinkConfig.URI_TAG)){
        throw new IllegalStateException("The server element in " +
            " the data config xml document has " + 
            "has missing attribute: " + TurbineSinkConfig.URI_TAG);
      }

      String serverAddr = serverEle.getAttribute(TurbineSinkConfig.URI_TAG);
      String userName = null;
      String password = null;

      if(serverEle.hasAttribute(TurbineSinkConfig.USERNAME_TAG)){

        userName = serverEle.getAttribute(
            TurbineSinkConfig.USERNAME_TAG);
      }
      if(serverEle.hasAttribute(TurbineSinkConfig.PASSWORD_TAG)){

        password = serverEle.getAttribute(
            TurbineSinkConfig.PASSWORD_TAG);
      }
      TurbineServer server = getTurbineServer(serverAddr, userName, password);

      String feedbackHost = null;
      int feedbackPort = -1;
      if(serverEle.hasAttribute(TurbineSinkConfig.FEEDBACK_SERVER_TAG)){

        feedbackHost = serverEle.getAttribute(
          TurbineSinkConfig.FEEDBACK_SERVER_TAG);
      }
      if(serverEle.hasAttribute(TurbineSinkConfig.FEEDBACK_PORT_TAG)){

        feedbackPort = Integer.parseInt(serverEle.getAttribute(
            TurbineSinkConfig.FEEDBACK_PORT_TAG));
      }

      if(feedbackHost != null && feedbackPort != -1) {
        server.createFeedbackAgent(feedbackHost, feedbackPort, false);
      }

      // Now parse for the channels
      NodeList channelList = serverNode.getChildNodes();
      parseChannels(channelList, server);
    }
  }

  public Vector<TurbineServer> getTurbineServerVec(){
    return turbineServerHandleVec;
  }

  public TurbineServer getTurbineServer(String addr, String userName, 
      String password) {

    String key=addr + "~" + userName;
    TurbineServer server = null;
    if(turbineServerHash.containsKey(key)){
      server = turbineServerHash.get(key);
      if(!server.getPassword().equals(password)){
        throw new IllegalArgumentException("Two conflicting server " +
            "configurations found for the same server \"" + addr +
            "\" and username \"" + userName + "\"");
      }
    }
    else {
      server = new TurbineServer(addr,userName,password);
      turbineServerHash.put(key,server);
      turbineServerHandleVec.addElement(server);
    }
    return server;
  }

  public void parseChannels(NodeList channelList, TurbineServer server) {

    for (int ch = 0; ch < channelList.getLength(); ch++) {
      Node channelNode = channelList.item(ch);

      Debugger.debug( Debugger.TRACE,"ch="+ ch + " Node type: " + channelNode.getNodeType() +
          " Node name:" + channelNode.getNodeName() );
      if(channelNode.getNodeType() == Node.ELEMENT_NODE &&
          ((Element) channelNode).getTagName().equals(
          TurbineSinkConfig.CHANNEL_TAG)) {

        Element channelEle = (Element)channelNode;
        if(! channelEle.hasAttribute(TurbineSinkConfig.CH_NAME_TAG)){

          throw new IllegalStateException("The channel element in " +
              " the data config xml document has "
            + "has missing attribute: " + TurbineSinkConfig.CH_NAME_TAG);
        }
        if(! channelEle.hasAttribute(TurbineSinkConfig.CH_SRC_NAME_TAG)){

          throw new IllegalStateException("The channel element in " +
            " the data config xml document has " +
            "has missing source attribute: " + 
            TurbineSinkConfig.CH_SRC_NAME_TAG);
        }

        String source = channelEle.getAttribute(TurbineSinkConfig.CH_SRC_NAME_TAG);
        String reqPath= source +
          "/" + channelEle.getAttribute(TurbineSinkConfig.CH_NAME_TAG); 

        if(! channelEle.hasAttribute(
              TurbineSinkConfig.CH_DATATYPE_TAG)){

          throw new IllegalStateException("The channel element in " +
              " the data config xml document has "
            + "has missing attribute: " + 
            TurbineSinkConfig.CH_DATATYPE_TAG);
        }

        String dt = channelEle.getAttribute(
              TurbineSinkConfig.CH_DATATYPE_TAG);
        Integer datatype = null;
        if("double".equals(dt)){

          datatype=Constants.DATATYPE_DOUBLE_OBJ;
        }
        else {
          throw new IllegalStateException("Only double data " +
              "are supported for testing. More will be " +
              "included later");
        }

        if(! channelEle.hasAttribute(
              TurbineSinkConfig.CH_REQ_MODE_TAG)){

          throw new IllegalStateException("The channel element in " +
              " the data config xml document has "
            + "has missing attribute: " + 
            TurbineSinkConfig.CH_REQ_MODE_TAG);
        }

        dt = channelEle.getAttribute(
              TurbineSinkConfig.CH_REQ_MODE_TAG);
        Integer intervalOrTimeout = null;
        int reqMode = TurbineSinkConfig.MONITOR_MODE;
        if("monitor".equals(dt)){

          // TODO: This has to be in a vector.. one for each channel
          reqMode = TurbineSinkConfig.MONITOR_MODE; 
          intervalOrTimeout = handleMonitorMode(channelNode);
        }
        else if("poll".equals(dt)) {

          reqMode = TurbineSinkConfig.POLL_MODE; 
          intervalOrTimeout = handlePollMode(channelNode);
        }
        else{

          throw new IllegalStateException("Only monitor mode " +
              " and poll mode " +
              "are supported for testing. More will be " +
              "included later");
        }
        server.addSinkChannel(reqPath, datatype, new Integer(reqMode), intervalOrTimeout);
        enableFeedback(server, source);
      }
    }
  }

  private Integer handleMonitorMode(Node channelNode) {

    Integer timeout = null;
    Element channelEle = (Element)channelNode;
    if(! channelEle.hasAttribute(
          TurbineSinkConfig.CH_TIMEOUT_TAG)){

      throw new IllegalStateException("The channel element in " +
          " the data config xml document has "
        + "has missing attribute: " + 
        TurbineSinkConfig.CH_TIMEOUT_TAG);
    }
    String dt = channelEle.getAttribute(
          TurbineSinkConfig.CH_TIMEOUT_TAG);
    // TODO: This has to be in a vector.. one for each channel
    timeout = Integer.parseInt(dt);
    return timeout;
  }

  private Integer handlePollMode(Node channelNode) {

    Integer pollInterval = null;
    Element channelEle = (Element)channelNode;
    if(! channelEle.hasAttribute(
          TurbineSinkConfig.CH_POLL_INTERVAL_TAG)){

      throw new IllegalStateException("The channel element in " +
          " the data config xml document has "
        + "has missing attribute: " + 
        TurbineSinkConfig.CH_POLL_INTERVAL_TAG);
    }
    String dt = channelEle.getAttribute(
          TurbineSinkConfig.CH_POLL_INTERVAL_TAG);
    // TODO: This has to be in a vector.. one for each channel
    pollInterval = Integer.parseInt(dt);
    return pollInterval;
  }

  // public TurbineServer getServer() {
  //   return server;
  // }
  // public Vector<String> getSubChannelNames() {
  //   return channelNames;
  // }
  // public Vector<Integer> getSubChannelDataTypes() {
  //   return channelDataTypes;
  // }
  // public Vector<Integer> getPollIntervals() {
  //   return channelPollIntervals;
  // }
  public void setCallBackListener(SinkCallBackListener callback) {
    this.callbackHandler = callback;
  }

  public SinkCallBackListener getCallBackListener(){
    return callbackHandler;
  }

  // public void resetChannelVecs(Vector<String> channelVec, 
  //     Vector<Integer> channelDatatypeVec) {

  //   if(channelVec.size() != channelDatatypeVec.size()) {

  //     throw new IllegalArgumentException( "The channel vector and its " +
  //         "datatypes should be of the same size");
  //   }
  //   channelNames = channelVec;
  //   channelDataTypes = new Vector<Integer>();

  //   for(int i=0; i<channelDatatypeVec.size(); i++){

  //     if (channelDatatypeVec.elementAt(i) == Constants.DATATYPE_DOUBLE_OBJ){
  //       channelDataTypes.addElement(Constants.DATATYPE_DOUBLE_OBJ);
  //     }
  //     else {
  //       throw new IllegalStateException("Only \"double\" data " +
  //           "is currently handled. More to be supported in future");
  //     }
  //   }
  // }

  // public void setRequestMode(int mode) {
  //   requestMode = mode;
  // }

  // public void setTimeout(int tOut) {
  //   timeout = tOut;
  // }

  // public TurbineServer getTurbineServer(){
  //   return server;
  // }

  public SinkFeedbackAgent getSinkFeedbackAgentForSource(String sourceName){
    TurbineServer server = null;
    SinkFeedbackAgent sfb = null;
    if(sourceToServerHash.containsKey(sourceName)) {
      server = sourceToServerHash.get(sourceName);
      sfb = server.getFeedbackAgentForSource(sourceName);
    }
    return sfb;
  }

  public Enumeration<String> getFeedbackEnabledSourceList(){
    return sourceToServerHash.keys();
  }

  public void enableFeedback(TurbineServer server, String source){

    if(! sourceToServerHash.containsKey(source)) {
      associateServerToSource(source, server);
    }
    sourceToServerHash.get(source).associateFeedbackAgent(source);
  }

  public void associateServerToSource(String source, TurbineServer server){

    if(! sourceToServerHash.containsKey(source)) {
      sourceToServerHash.put(source,server);
    }
    else {
      throw new IllegalStateException("The source \"" + source  + "\" has " +
          "already been associlated with the server");
    }
  }
}
