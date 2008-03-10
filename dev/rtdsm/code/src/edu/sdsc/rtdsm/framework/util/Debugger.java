package edu.sdsc.rtdsm.framework.util;

import java.util.*;
import java.io.*;

public class Debugger {

  public static final String DEBUG_LEVEL_STR = "debugLevel";
  public static final int TRACE = 0;
  public static final int RECORD = 1;
  public static final int IMP = 2;
  public static final int CRIT = 3;

  private static DTProperties dtp;

  private static int debugLevel;

  static{
    try {

      dtp = DTProperties.getProperties(
        "rtdsm.properties");
      debugLevel = Integer.parseInt(dtp.getProperty(DEBUG_LEVEL_STR));
    }
    catch (IOException ioe){

      ioe.printStackTrace();
      System.err.println("No \"rtdsm.properties\" file found. " + 
          "Proceeding with default debug level");
      debugLevel = RECORD;
    }
  }

  public static void debug(int level, String debugStr){

    if(level >= debugLevel) {
      System.out.println(Thread.currentThread() + ": " + debugStr);
    }
  }
}
