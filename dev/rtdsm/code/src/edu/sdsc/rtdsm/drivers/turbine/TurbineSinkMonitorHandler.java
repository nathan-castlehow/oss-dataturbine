package edu.sdsc.rtdsm.drivers.turbine;

import com.rbnb.sapi.*;
import java.util.*;
import java.text.DateFormat;

import edu.sdsc.rtdsm.framework.sink.*;
import edu.sdsc.rtdsm.framework.data.DataPacket;
import edu.sdsc.rtdsm.framework.util.*;

public class TurbineSinkMonitorHandler extends TurbineSinkFetchHandler {

  SinkCallBackListener callback;
  Thread dataCollector;	
  long timeout;
  private volatile boolean threadStopper = false;

  public TurbineSinkMonitorHandler(TurbineRawSink sink, 
      SinkCallBackListener callback, long timeout){

    super(sink);
    this.callback = callback;
    this.timeout = timeout;
  }

  public void initFetch(){
    
    try {
      // Gap control is still not implemented on
      // RBNB sinks. Hence setting it to zero
      sinkHandle.getSink().Monitor(sinkHandle.getMap(), 1);
      // sinkHandle.getSink().Subscribe(sinkHandle.getMap());
    }
    catch(SAPIException se){
      se.printStackTrace();
    }

    // Spawn a thread for handling the callbacks
    dataCollect();
    // dataCollector=new Thread(this);
    // dataCollector.start();
  }
  

	public void run()	{	
    dataCollect();
	} 

  public void terminate(){
    threadStopper = true;
  }

  public void dataCollect() {

    ChannelMap m = null;
    long timestamp;


    while(sinkHandle.isConnected() && !threadStopper) {

      try{
        if( m== null || m.NumberOfChannels() != 0) {
          Debugger.debug( Debugger.TRACE,"Waiting for data....");
        }
        Debugger.debug( Debugger.TRACE,".");
        m = sinkHandle.getSink().Fetch(timeout);
        if(Thread.interrupted()){

          Debugger.debug( Debugger.TRACE,"===================================");
          Debugger.debug( Debugger.TRACE,"CURRENT THREAD HAS BEEN INTERRUPTED");
          Debugger.debug( Debugger.TRACE, "Is Connected:? " + sinkHandle.isConnected());
          Debugger.debug( Debugger.TRACE,"===================================");
        }

        if( null == m ){

          Debugger.debug( Debugger.RECORD,"Channel Map fetched is null");
          continue;
        }

        if( m.NumberOfChannels() != 0) {
          Debugger.debug( Debugger.RECORD,"Number of channels in the map: " + 
              m.NumberOfChannels()); 
        }
        //continue;

        if(m.NumberOfChannels() != 0) {

          DataPacket chData = new DataPacket();
          for(int i=0;i<sinkHandle.getReqChannelNames().size(); i++) {

            String chanName = sinkHandle.getReqChannelNames().elementAt(i);
            // Debugger.debug( Debugger.TRACE,"Checking for channel" + chanName);
            int index = m.GetIndex(chanName);
            if( index != -1 ) {
              Vector<Long> timestampVec = new Vector<Long>();
              timestamp = (long) (m.GetTimeStart(index) * 1000);
              // Do not modify x. It is just a reference to 
              // turbine internal data structure
              double[] x = m.GetTimes(index);
              for(int t=0; t < x.length; t++){

                timestampVec.addElement(new Long( (long)(x[t]*1000)));
              }

              switch(sinkHandle.getReqDatatypes().elementAt(i).intValue()) {

                case Constants.DATATYPE_DOUBLE:
                  chData.addData(handleDouble(m, index),
                      sinkHandle.getReqIndicies().elementAt(i),
                      chanName, timestamp, timestampVec);
                  break;

                case Constants.DATATYPE_STRING:
                  chData.addData(handleString(m, index),
                      sinkHandle.getReqIndicies().elementAt(i),
                      chanName, timestamp, timestampVec);
                  break;
                  
                default:
                  throw new IllegalStateException("Only \"double\" data " +
                      "is currently handled. More to be supported in future");
              }
            }
          }
          callback.callBack(chData);
        }
        if( m== null || m.NumberOfChannels() != 0) {
          Debugger.debug( Debugger.TRACE,"-----------------------------------");
        }
      }
      catch(SAPIException se){
        se.printStackTrace();
      }
      catch(IllegalStateException ise) {
        // If a source is terminated in between during
        // sink's query, Fetch operation throws this error
        System.err.println("Possible Source abnormal termination during "+ 
            "query: Proceeding further without sink termination.");
        throw ise;
      }
      // catch(InterruptedException ie){

      //   System.err.println("Interrupt received while waiting for data");
      //   throw ie;
      // }
    }
    Debugger.debug( Debugger.TRACE,"Done with the loop waiting for data");
  }

  private Object handleDouble(ChannelMap m, int index) {
    return (Object)m.GetDataAsFloat64(index);
  }

  private Object handleString(ChannelMap m, int index) {
    return (Object)m.GetDataAsString(index);
  }
}

