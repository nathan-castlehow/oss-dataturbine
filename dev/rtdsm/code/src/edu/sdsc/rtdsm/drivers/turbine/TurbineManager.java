package edu.sdsc.rtdsm.drivers.turbine;

import edu.sdsc.rtdsm.drivers.turbine.util.TurbineServer;

/**
 * Class TurbineManager
 *  A singleton class.
 */
public class TurbineManager {
  // Fields
  private static TurbineManager theInstance = null;

  static {
    if(theInstance == null) {
      theInstance = new TurbineManager();
      theInstance.loadServerInfo();
    }
  }

  private TurbineManager ( ) { }

  /**
   * 
   * @return void  
   */
  public void getServerHandle () {
    
  }

  /**
   * 
   * @return void  
   */
  public static TurbineManager getInstance () {
    return theInstance;
  }

  /**
   * 
   */
  public TurbineServer registerSrc () {
    return null;
  }

  /**
   * 
   * @param clientOptions 
   * @return void  
   */
  public void removeServer ( int clientOptions) {
    
  }

  /**
   * 
   * @return void  
   */
  private void loadServerInfo () {
    
  }

  /**
   * 
   * @return turbine.Server  
   */
  public TurbineServer registerSink () {
    return null;
  }
}

