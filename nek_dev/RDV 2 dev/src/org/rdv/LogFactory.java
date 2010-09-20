
package org.rdv;

import org.apache.commons.logging.Log;

public class LogFactory{

  private static TheLog log = new TheLog();
  
  public static Log getLog(String className){
    return log;
  }
  

}
