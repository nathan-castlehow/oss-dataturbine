package edu.sdsc.rtdsm.drivers.turbine.util;

import java.util.Vector;
import java.util.Hashtable;

import edu.sdsc.rtdsm.framework.util.Debugger;
import edu.sdsc.rtdsm.framework.feedback.SinkFeedbackAgent;

public class TurbineServer{
  // Fields
  // 
  public String serverAddr;
  // 
  public String userName;
  // 
  public String password;
  public SinkFeedbackAgent fbAgent = null;
  public TurbineSinkWrapper sinkWrapper;
  public Hashtable<String,SinkFeedbackAgent> sinkFbAgentHash = 
    new Hashtable<String, SinkFeedbackAgent>();


  public TurbineServer() { }
  public TurbineServer ( String addr, String userName, String password) { 

    this.serverAddr = addr;
    this.userName = userName;
    this.password = password;
    sinkWrapper = new TurbineSinkWrapper(this);
  }

  public void addSinkChannel(String channelName, Integer datatype, Integer mode, Integer intervalOrtimeout){
    if(sinkWrapper == null){
      sinkWrapper = new TurbineSinkWrapper(this);
    }
    sinkWrapper.addChannel(channelName, datatype, mode, intervalOrtimeout);

  }

  public TurbineSinkWrapper getSinkWrapper(){
    return sinkWrapper;
  }

  public void printServerInfo(int level) {

    Debugger.debug( level,"\n\tServer params:");
    Debugger.debug( level,"\tServer name:" + serverAddr);
    Debugger.debug( level,"\tServer username:" + userName);
    Debugger.debug( level,"\tServer password:" + password);

    if(sinkWrapper != null){
      sinkWrapper.printSinkInfo(level);
    }
    if(fbAgent != null) {
      Debugger.debug( level,"\n\tFeedback Server name:" + fbAgent.getHostName());
      Debugger.debug( level,"\tFeedback Server port:" + fbAgent.getPort());
    }
  }

  public void resetSinkWrapperChannelVecs(Vector<String> channelVec, 
        Vector<Integer> channelDatatypeVec, Vector<Integer> reqModeVec,
        Vector<Integer> intervalOrToutVec) {

    if(sinkWrapper == null){
      sinkWrapper = new TurbineSinkWrapper(this);
    }
    sinkWrapper.resetChannelVecs(channelVec, channelDatatypeVec,
        reqModeVec, intervalOrToutVec);
  }

  public SinkFeedbackAgent getFeedbackAgentForSource(String sourceName){

    return sinkFbAgentHash.get(sourceName);
  }

  public SinkFeedbackAgent getFeedbackAgentOfTheServer(){
    return fbAgent;
  }

  public void associateFeedbackAgent(String sourceName){
    Debugger.debug(Debugger.TRACE, "fbAgent = " + fbAgent);
    if(fbAgent != null && !sinkFbAgentHash.containsKey(sourceName)) {
      sinkFbAgentHash.put(sourceName, fbAgent);
    }
  }

  public void createFeedbackAgent(String hostName, int port, boolean resetFbHost){

    if(fbAgent == null) {
      
      fbAgent = new SinkFeedbackAgent();
      fbAgent.setHostName(hostName);
      fbAgent.setPort(port);
    }
    else if(resetFbHost) {
      fbAgent.setHostName(hostName);
      fbAgent.setPort(port);
    }
  }

  public SinkFeedbackAgent getSinkFeedbackAgent(){
    return fbAgent;
  }

  public String getServerAddr(){
    return serverAddr;
  }

  public String getUsername(){
    return userName;
  }

  public String getPassword(){
    return password;
  }

}
