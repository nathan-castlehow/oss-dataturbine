package org.rdv.datapanel.map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.swing.JLabel;

import org.rdv.DataPanelManager;
import org.rdv.datapanel.AbstractDataPanel;

public class MapPanel extends AbstractDataPanel{

  private static HashMap<String, GMapEntity> entities = new HashMap<String, GMapEntity>();
  private static GMap map = new GMap();
  
  public MapPanel(){
     map.setVisible(true);
     setDataComponent(new JLabel("Please Reference open window"));
  }


  public boolean supportsMultipleChannels() { return false; }

  public boolean addChannel(String channelName) {
    if(!entities.containsKey(channelName))
      entities.put(channelName, new GMapEntity(map, channelName,channelName)); 
    
    // TODO MODIFY
    return super.addChannel(channelName);
  }
  
  public void postTime(double time) {
    super.postTime(time);
    
    
    for(String chName: subscribedChannels()){
      System.out.println(chName +" "+rbnbController.getChannel(chName));
    }

    //loop over all channels and see if there is data for them
    Iterator i = channels.iterator();
    while (i.hasNext() && channelMap!=null) {
      String channelName = (String)i.next();
      
      int channelIndex = channelMap.GetIndex(channelName);
      //if there is data for channel, post it
      if (channelIndex != -1) {
        double lat = channelMap.GetDataAsFloat64(channelIndex)[0];
        double lng = channelMap.GetDataAsFloat64(channelIndex)[1];
        entities.get(channelName).move(lat, lng);
      }
    }
  }
  
  public void closePanel() {
    // TODO MODIFY
    super.closePanel();
  }

  public Properties getProperties() {
    // TODO MODIFY
    return super.getProperties();
  }

  public boolean isChannelSubscribed(String channelName) {
    // TODO MODIFY
    return super.isChannelSubscribed(channelName);
  }

  public void openPanel(DataPanelManager dataPanelManager) {
    // TODO MODIFY
    super.openPanel(dataPanelManager);
  }

  public boolean removeChannel(String channelName) {
    if(entities.containsKey(channelName)){
      entities.get(channelName).destroy();
      entities.remove(channelName);
    }
    return super.removeChannel(channelName);
  }

  public boolean setChannel(String channelName) {
    // TODO MODIFY
    return super.setChannel(channelName);
  }

  public void setProperty(String key, String value) {
    // TODO MODIFY
    super.setProperty(key, value);
  }

  public int subscribedChannelCount() {
    // TODO MODIFY
    return super.subscribedChannelCount();
  }

  public List<String> subscribedChannels() {
    // TODO MODIFY
    return super.subscribedChannels();
  }
  
}