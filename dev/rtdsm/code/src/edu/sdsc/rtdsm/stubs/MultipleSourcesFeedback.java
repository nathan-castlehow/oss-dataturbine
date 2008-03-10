package edu.sdsc.rtdsm.stubs;

import java.util.*;

import edu.sdsc.rtdsm.dig.sites.*;
import edu.sdsc.rtdsm.framework.src.*;

public class MultipleSourcesFeedback implements Runnable {

  SrcConfig srcConfig;
  String srcName;
  SiteSrcStub siteSrc;
  long sleepTime;

  public MultipleSourcesFeedback(String sourceName, SrcConfig srcConfig, 
      long sleepTime) {

    this.srcConfig = srcConfig;
    this.srcName = sourceName;
    this.sleepTime = sleepTime;
    this.siteSrc = new SiteSrcStub(srcConfig, srcName);
    siteSrc.connect();
  }

  public void run() {

    Thread myThread = Thread.currentThread();
    siteSrc.startPumping(sleepTime);
  }

  public void spawn(){
    Thread t = new Thread(this);
    t.start();
  }

  public static void main(String[] args){
    
    if(args.length != 2){
      System.err.println("Usage: java stubs.MultipleSourcesFeedback " +
          "<srcConfig xml file> <sleepTimeForEachsrc>");
      return;
    }
    String configFile = args[0];
    long sleepTime = (long) (Integer.parseInt(args[1]));
    
    SrcConfigParser parser = new SrcConfigParser();
    parser.fileName = configFile;
    parser.parse();
    Vector<String> srcList = parser.getSourceList();
    Vector<MultipleSourcesFeedback> ms = new Vector<MultipleSourcesFeedback>();

    for(int i=0; i < srcList.size();i++) {
      String srcName = srcList.elementAt(i);
      SrcConfig srcConfig = parser.getSourceConfig(srcName);
      MultipleSourcesFeedback src = new MultipleSourcesFeedback(srcName, srcConfig, sleepTime);
      ms.addElement(src);
    }
    for(int i=0; i < srcList.size();i++) {
      ms.elementAt(i).spawn();
    }
  }
}
