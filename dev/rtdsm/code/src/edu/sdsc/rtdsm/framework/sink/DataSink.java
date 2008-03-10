package edu.sdsc.rtdsm.framework.sink;

import java.util.*;

/**
 * Interface DataSink
 */
public interface DataSink {

  public void connectAndWait();
    
  
  /**
   * @param channelId 
   * @return foreign.Object  
   */
  public Object pullData (int channelId);
    
  
}

