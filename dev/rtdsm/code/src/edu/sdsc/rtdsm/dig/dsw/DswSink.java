package edu.sdsc.rtdsm.dig.dsw;

import java.util.*;

import edu.sdsc.rtdsm.drivers.turbine.util.*;
import edu.sdsc.rtdsm.drivers.turbine.*;
import edu.sdsc.rtdsm.framework.sink.*;
import edu.sdsc.rtdsm.framework.src.*;
import edu.sdsc.rtdsm.framework.feedback.SinkFeedbackAgent;

/**
 * Class DswSink
 * 
 */
public class DswSink {

  TurbineSink sink;
  SinkFeedbackAgent fbAgent = null;
  boolean feedbackReqd = false;
  SinkConfig sinkConfig;

  public DswSink(SinkConfig sinkConfig) { 
    this(sinkConfig, false);
  }

  public DswSink( SinkConfig sinkConfig, boolean feedbackReqd) { 

    if(sinkConfig instanceof TurbineSinkConfig) {

      sink = new TurbineSink((TurbineSinkConfig)sinkConfig);
    }
    else {
      throw new IllegalStateException("Dsws currently support only data turbine");
    }

    this.feedbackReqd = feedbackReqd;
    this.sinkConfig = sinkConfig;
  }

  public void connectAndWait(){
    sink.connectAndWait();
  }

  public boolean isFeedbackEnabled(){
    return feedbackReqd;
  }

  public boolean isFeedbackEnabledForSource(String source){
    SinkFeedbackAgent fbAgent = ((TurbineSinkConfig)sinkConfig).getSinkFeedbackAgentForSource(source);
    return (feedbackReqd && fbAgent!= null )?true : false;
  }

  // public void sendFeedback1(String source, String channel, Object data) {

  //   String reqPath;
  //   if(feedbackSrcHash == null) {
  //     throw new IllegalStateException("Feedback is not enabled in " +
  //         "this sink");
  //   }
  //   TurbineSource src = feedbackSrcHash.get(source);

  //   if(src == null) {
  //     throw new IllegalStateException("Source with name \"" +source +
  //         "\" cannot be found in the sink configuration");
  //   }
  //   src.insertData(channel, data);
  // }

  public SinkCallBackListener getCallBackListener() {
    return ((TurbineSinkConfig)sinkConfig).getCallBackListener();
  }
  public void sendFeedback(String source, String data) {

    if(feedbackReqd) {
      SinkFeedbackAgent fbAgent = ((TurbineSinkConfig)sinkConfig).getSinkFeedbackAgentForSource(source);
      if(fbAgent != null) {
          fbAgent.send(source, data);
      }
      else {
        throw new IllegalStateException("Feedback is not enabled for the " +
            "source \"" + source + "\"");
      }
    }
  }

  public String getName() {
    return sink.getName();
  }

  public void disconnect(){

    sink.disconnect();
    // Enumeration<String> sources = ((TurbineSinkConfig)sinkConfig).getFeedbackEnabledSourceList();

    // while(sources.hasMoreElements()){
    //   String sourceName = sources.nextElement();
    //   SinkFeedbackAgent fbAgent = 
    //     ((TurbineSinkConfig)sinkConfig).getSinkFeedbackAgentForSource(source);
    //   if(fbAgent) {
    //   }
    // }
  }
}

