package edu.sdsc.rtdsm.framework.data;

import java.util.Vector;

public class ChannelData {

  private String channelName;
  private Vector<Object> dataVec = new Vector<Object>();

  public ChannelData(String channelName){
    this.channelName = channelName;
  }

  public void addData(Object data){
    dataVec.addElement(data);
  }

  public Object getData(int i) {
    return dataVec.elementAt(i);
  }

  public int getNumDataItems(){
    return dataVec.size();
  }
}
