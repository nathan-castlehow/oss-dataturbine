package org.rdv.datapanel.mapAlt;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.mapviewer.Waypoint;
import org.rdv.DataPanelManager;
import org.rdv.datapanel.AbstractDataPanel;
import org.rdv.datapanel.mapAlt.WaypointGroup;

public class MapPanel extends AbstractDataPanel{
  private Map map = new Map();

  private HashMap<String, WaypointGroup> chMap = new HashMap<String, WaypointGroup>();
  
  public MapPanel(){
    setDataComponent(map);

    map.setZoom(15);
    map.setCenterPosition(new GeoPosition(20,20));
    
    
    WaypointGroup pointGroup = new WaypointGroup(
        new Waypoint[]{ 
          new Waypoint(20,20),
          new Waypoint(22,18),
          new Waypoint(21,15)}
        );
    
    
    map.draw(pointGroup);
  }



  public boolean supportsMultipleChannels() { return true; }

  public boolean addChannel(String channelName) {
    
    chMap.put(channelName, new WaypointGroup());
    
    // TODO MODIFY
    return super.addChannel(channelName);
  }
  
  public void postTime(double time) {
    super.postTime(time);
    
    WaypointGroup points = new WaypointGroup();


    for(String chName: subscribedChannels()){
      System.out.println(chName +" "+rbnbController.getChannel(chName));
    }


  //loop over all channels and see if there is data for them
    Iterator i = channels.iterator();
    while (i.hasNext()) {
      String channelName = (String)i.next();
      
      int channelIndex = channelMap.GetIndex(channelName);
      //if there is data for channel, post it
      if (channelIndex != -1) {
        double lat = channelMap.GetDataAsFloat64(channelIndex)[0];
        double lng = channelMap.GetDataAsFloat64(channelIndex)[1];
        points.add(lat,lng);
      }
    }

    if(!points.isEmpty())
    
      map.draw(points);

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
    // TODO MODIFY
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