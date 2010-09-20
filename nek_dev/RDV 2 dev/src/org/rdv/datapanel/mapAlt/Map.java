
package org.rdv.datapanel.mapAlt;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import org.jdesktop.swingx.JXMapKit;
import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.DefaultTileFactory;
import org.jdesktop.swingx.mapviewer.TileFactory;
import org.jdesktop.swingx.mapviewer.TileFactoryInfo;
import org.jdesktop.swingx.mapviewer.WaypointPainter;
import org.jdesktop.swingx.mapviewer.wms.WMSService;
import org.jdesktop.swingx.mapviewer.wms.WMSTileFactory;
import org.jdesktop.swingx.painter.Painter;

public class Map extends JXMapKit{
  
    private static final long serialVersionUID = 4355594836046180525L;
    
    public Map(){
      JButton street = new JButton("Map");
      JButton sat = new JButton("Sat");
      JButton overlay = new JButton("Overlay");
      
      street.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent e) {changeToMapView();}
      });
      sat.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent e) {changeToSatelliteView();setZoom(15);}
      });
      overlay.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {overlay();}
        });
      
      add(street);
      add(sat);
      add(overlay);
      
      //changeToSatelliteView();
      changeToMapView();
    }
    
    public void changeToMapView(){
      final int max = 17;
      TileFactoryInfo info =  new TileFactoryInfo(1,max-2,max, 256, true, true,
          "http://tile.openstreetmap.org", "x","y","z") {
        public String getTileUrl(int x, int y, int zoom) {
          zoom = max-zoom;
          String url = this.baseURL +"/"+zoom+"/"+x+"/"+y+".png";
          return url;
        };
      };
      setTileFactory(new DefaultTileFactory(info) );
    }
    

    public void changeToSatelliteView(){
      WMSService wms = new WMSService();
      wms.setLayer("BMNG");
      wms.setBaseUrl("http://onearth.jpl.nasa.gov/wms.cgi?");
      TileFactory fact = new WMSTileFactory(wms);
      setTileFactory(fact);
    }
    
    public void draw(WaypointGroup pointGroup){
      //crate a WaypointPainter to draw the points
      WaypointPainter painter = new WaypointPainter();
      painter.setWaypoints(pointGroup);
      getMainMap().setOverlayPainter(painter);
    }
    
    
    public void overlay(){
      Painter<JXMapViewer> textOverlay = new Painter<JXMapViewer>() {
        public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
            g.setPaint(new Color(0,0,0,150));
            g.fillRoundRect(50, 10, 182 , 30, 10, 10);
            g.setPaint(Color.WHITE);
            g.drawString("I can do an Overlay", 50+10, 10+20);
        }
      };
      
      getMainMap().setOverlayPainter(textOverlay);
    }
    
}
