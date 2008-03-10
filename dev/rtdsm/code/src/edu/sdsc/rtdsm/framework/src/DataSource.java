package edu.sdsc.rtdsm.framework.src;

import java.util.Vector;


/**
 * Interface DataSource
 * 
 */
public interface DataSource {
  // Methods
  // Constructors
  // Accessor Methods
  // Operations
  /**
   * 
   * @param numChannels 
   * @param channelDescriptor 
   * @param bufferingTypes 
   * @return void  
   */
  public void initSrc ( SrcConfig srcConfig);
    
  
  /**
   * 
   * @param channel 
   * @param datatype 
   * @param data 
   * @return void  
   */
  public void insertData (int channel, Object data);
  public int flush();
}

