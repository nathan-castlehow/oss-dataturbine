package edu.sdsc.rtdsm.drivers.turbine;

import com.rbnb.sapi.*;
import java.util.*;
import java.text.DateFormat;

import edu.sdsc.rtdsm.framework.sink.*;
import edu.sdsc.rtdsm.framework.data.DataPacket;
import edu.sdsc.rtdsm.framework.util.*;

public class TurbineSinkPollHandler extends TurbineSinkFetchHandler {

  SinkCallBackListener callback;
  private volatile boolean threadStopper = false;

  public TurbineSinkPollHandler(TurbineRawSink sink,
      SinkCallBackListener callback){

    super(sink);
    this.callback = callback;
  }

  public void initFetch(){
    
    dataCollect();
  }
  

  public void dataCollect() {

    ChannelMap m;
    long timestamp;
    long timeOfLastData=0;


    while(sinkHandle.isConnected() && !threadStopper) {

      try{
        String time = DateFormat.getDateTimeInstance().format(
          new Date((long)(timeOfLastData+1)));
        Debugger.debug(Debugger.TRACE,"Waiting for data from " + time + "....");
        sinkHandle.getSink().Request(sinkHandle.getMap(),(double)(timeOfLastData + 1)/1000.00,10,"after");
        m = sinkHandle.getSink().Fetch(-1);

        if( null == m ){

          Debugger.debug(Debugger.RECORD,"Channel Map fetched is null");
          continue;
        }

        Debugger.debug(Debugger.RECORD,"Number of channels in the map: " + 
            m.NumberOfChannels()); 
        //continue;

        if(m.NumberOfChannels() != 0) {

          DataPacket chData = new DataPacket();
          for(int i=0;i<sinkHandle.getReqChannelNames().size(); i++) {

            String chanName = sinkHandle.getReqChannelNames().elementAt(i);
            // Debugger.debug(Debugger.TRACE,"Checking for channel" + chanName);
            int index = m.GetIndex(chanName);
            if( index != -1 ) {
              Vector<Long> timestampVec = new Vector<Long>();
              timestamp = (long) (m.GetTimeStart(index) * 1000);
              time = DateFormat.getDateTimeInstance().format(
                new Date((long)(timestamp)));
              timeOfLastData = (long) (m.GetTimeStart(index) * 1000 + 
                  m.GetTimeDuration(index)*1000 );
              time = DateFormat.getDateTimeInstance().format(
                new Date((long)(timeOfLastData)));

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

                default:
                  throw new IllegalStateException("Only \"double\" data " +
                      "is currently handled. More to be supported in future");
              }
            }
          }
          callback.callBack(chData);
        }
        try{

          // Right now we support single poll interval for all channels
          long sleepTime = 
            (long)(sinkHandle.getReqPollIntervals().elementAt(0).intValue());
          Debugger.debug(Debugger.TRACE,"Sleeping for " + sleepTime + " millisecs....");
          Thread.sleep(sleepTime);
        }
        catch(InterruptedException ie){

          ie.printStackTrace();
        }
        Debugger.debug(Debugger.TRACE,"-----------------------------------");
      }
      catch(SAPIException se){
        se.printStackTrace();
      }
    }
  }

  private Object handleDouble(ChannelMap m, int index) {
    return (Object)m.GetDataAsFloat64(index);
  }
  public void terminate(){
    threadStopper = true;
  }
}

