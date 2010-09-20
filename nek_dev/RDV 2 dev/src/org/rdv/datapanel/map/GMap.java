package org.rdv.datapanel.map;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

import chrriis.common.UIUtils;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserEvent;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserListener;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserNavigationEvent;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserWindowOpeningEvent;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserWindowWillOpenEvent;

/**
 * @author Brian McMahon
 */
public class GMap extends JFrame {
 
  private static final String GMAP_HTML =  System.getProperty("user.dir")+"/resources/gmap.html";
    //"C:\\Users\\Public\\gmap.html";
 // "C:\\Users\\Brian\\Desktop\\workspace\\RDV\\src\\org\\rdv\\datapanel\\map\\gmap.html";
  
  private static final long serialVersionUID = 1L;
  static final JWebBrowser webBrowser = new JWebBrowser();;
  boolean stopFlag = false;
  private CommandQueue queue;
  
  public GMap() { 
    JPanel webBrowserPanel = new JPanel(new BorderLayout());
    
    webBrowser.navigate(GMAP_HTML);
    webBrowserPanel.add(webBrowser, BorderLayout.CENTER);
    
    queue = new CommandQueue(webBrowser);
    
    webBrowser.setMenuBarVisible(false);

    webBrowser.addWebBrowserListener(new WebBrowserListener() {  
      public void windowWillOpen(WebBrowserWindowWillOpenEvent arg0) {}
      public void windowOpening(WebBrowserWindowOpeningEvent arg0) {}
      public void windowClosing(WebBrowserEvent arg0) { }
      public void titleChanged(WebBrowserEvent arg0) { }
      public void statusChanged(WebBrowserEvent arg0) {      }      
      public void locationChanging(WebBrowserNavigationEvent arg0) {}
      public void locationChanged(WebBrowserNavigationEvent arg0) {      }
      public void locationChangeCanceled(WebBrowserNavigationEvent arg0) {      }
      public void loadingProgressChanged(WebBrowserEvent arg0) {        }
      public void commandReceived(WebBrowserEvent arg0, String arg1, String[] arg2) { 
        sendReady();
      }
      
    });

    UIUtils.setPreferredLookAndFeel();
    NativeInterface.open();
    NativeInterface.runEventPump();
    
    add(webBrowserPanel);
    this.setSize(800, 600);
}
  
  
  public void center(double lat, double lng){
    executeCommand("center("+lat+","+lng+");");
  }
  
  public void alert(String msg){
    executeCommand("alert(\'"+msg+"\');");
  }
  
  public void executeCommand(String cmd){
    System.out.println("\nADDED CMD: "+cmd+"\n");
    queue.addCommand(cmd);
  }
  
  public void sendReady(){
    queue.ready(true);
    queue.flush();
    System.out.println("READY");
    
  }
  
}