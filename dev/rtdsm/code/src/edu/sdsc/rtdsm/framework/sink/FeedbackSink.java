package edu.sdsc.rtdsm.framework.sink;

public class FeedbackSink implements Runnable {

  DataSink sink;

  public FeedbackSink(DataSink sink){

    this.sink = sink;
  }

  public boolean spawnFeedbackThread(){

    Thread t = new Thread(this);
    t.start();
    return true;
  }
  
  public void run() {
    sink.connectAndWait();
  }
}
