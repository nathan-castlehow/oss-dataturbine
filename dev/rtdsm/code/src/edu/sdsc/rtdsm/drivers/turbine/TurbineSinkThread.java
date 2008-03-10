package edu.sdsc.rtdsm.drivers.turbine;

import edu.sdsc.rtdsm.framework.util.Debugger;

public class TurbineSinkThread implements Runnable {

  TurbineRawSink sink;
  private volatile boolean threadStopper = false;
  
  public TurbineSinkThread(TurbineRawSink sink){

    this.sink = sink;
  }

  public void run(){

    Debugger.debug(Debugger.TRACE, "Running TurbineRawSinkThread...");
    threadStopper = false;
    sink.waitForData();
  }

  public void terminate(){
    threadStopper = true;
  }
}
