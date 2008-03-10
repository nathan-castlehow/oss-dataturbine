package edu.sdsc.rtdsm.drivers.turbine;


public abstract class TurbineSinkFetchHandler {

  protected TurbineRawSink sinkHandle;
  protected TurbineSinkFetchHandler(TurbineRawSink s) {
    this.sinkHandle = s;
  }

  public abstract void initFetch();
  public abstract void terminate();
}
