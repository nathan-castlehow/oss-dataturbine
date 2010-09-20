
package org.rdv.datapanel.mapAlt;

import java.util.HashSet;
import java.util.Set;

import org.jdesktop.swingx.mapviewer.Waypoint;

public class WaypointGroup extends HashSet<Waypoint>{


  private static final long serialVersionUID = -343787512314649463L;
  protected Set<Waypoint> waypoints;
  
  public WaypointGroup(){  }

  public WaypointGroup(Waypoint[] initialPoints){
    this();
    
    for(Waypoint point: initialPoints)
      add(point);
  }
  
  public void add(double lat, double lng){
    this.add(new Waypoint(lat,lng));
  }
 
}
