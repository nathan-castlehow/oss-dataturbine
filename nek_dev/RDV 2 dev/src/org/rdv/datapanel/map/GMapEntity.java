
package org.rdv.datapanel.map;

public class GMapEntity {
  
  private String name;
  public double lat, lng;
  boolean visible; 
  private GMap map;
  
  public GMapEntity(GMap map, String name, String infoString){
    this.map = map;
    this.name = name;
    lat = 0;
    lng = 0;
    visible = true;
    map.executeCommand("createEntity('"+name+"', "+lat+","+lng+",'"+infoString+"');");
  }
  
  public GMapEntity(GMap map, String name, String infoString, int code){
    this.map = map;
    this.name = name;
    lat = 0;
    lng = 0;
    visible = true;
    map.executeCommand("createEntity2('"+name+"', "+lat+","+lng+",'"+infoString+"');");
  }

  public void move(double lat, double lng){
    this.lat = lat;
    this.lng = lng;
    map.executeCommand("moveEntity('"+name+"', "+lat+","+lng+");");
  }
  
  public void destroy(){
    map.executeCommand("visEntity('"+name+"', false);");
  }
  
  public String getName() { return name; }
  public double getLatitude() { return lat; }
  public double getLongitude() { return lng; }
  
  
  @Override public int hashCode(){ return name.hashCode(); }
  @Override public String toString(){ return name;}
}
