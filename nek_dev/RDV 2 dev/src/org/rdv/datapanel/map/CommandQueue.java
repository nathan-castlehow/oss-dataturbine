
package org.rdv.datapanel.map;

import java.util.LinkedList;

import javax.swing.SwingUtilities;

import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;

public class CommandQueue {
  
  
  private JWebBrowser webBrowser;
  private boolean isReady = false;
  
  private LinkedList<String> queue = new LinkedList<String>();
  
  public CommandQueue(JWebBrowser webBrowser){
    this.webBrowser= webBrowser;
  }
  
/*  public void run(){
//    while(true){
//      System.out.print("<<"+queue.size());
//
//      if(readyFlag && queue.size() > 0){
//          String js = queue.removeFirst();
//          System.err.println("\nEXECUTING CMD\n");
//          
//          //deal with swing being a pain in the ass
//          try{
//            execJS(js);
//          }catch (Exception e) {
//            queue.addFirst(js);
//            System.err.println(e.getMessage());
//          }
//            //readyFlag = false;
//       }
//      try {sleep(1000);} catch (Exception e){}
//    }
  }*/
  
  public void ready(boolean ready){
    isReady = ready;
  }
  
  public void addCommand(String command){
    queue.addLast(command);
    if(isReady) flush();
  }
  
  private void  execJS(String js){
    SwingUtilities.invokeLater(new EXJS(js));
  }

  public void flush(){
    while(!queue.isEmpty()){
        execJS(queue.removeFirst());
    }
  }
 
  private class EXJS extends Thread {
    String js;
    public EXJS(String js){
      this.js=js;
    }
    public void run() {
      webBrowser.executeJavascript(js);
      System.out.println("EXEC: " + js);
    }
  }
  
}
