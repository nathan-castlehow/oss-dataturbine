package edu.sdsc.rtdsm.framework.feedback;

import java.io.*;
import java.net.*;

import edu.sdsc.rtdsm.framework.util.Constants;
import edu.sdsc.rtdsm.framework.util.Debugger;

public class SinkFeedbackAgent {

  String serverName;
  int port = -1;
  
  public SinkFeedbackAgent(){
  }

  public SinkFeedbackAgent(String serverName, int port){
    this.serverName = serverName;
    this.port = port;
  }

  public String getHostName(){
    return serverName;
  }

  public void send(String source, String data){

    Socket socket = null;
    DataOutputStream output;
    try {
      socket = new Socket(serverName, port);
    }
    catch (IOException e) {
      throw new IllegalStateException("ERROR: Could not connect to feedback "+ 
          "server");
    }

    try {

      output = new DataOutputStream(socket.getOutputStream());
      writePacket(output, source, data);
      output.close();
      socket.close();
    }
    catch (IOException e) {
      throw new IllegalStateException("ERROR: Could not write to feedback "+ 
          "server");
    }
  }

  public void writePacket(DataOutputStream output, String source, String data){

    try {

      // In java each char needs 2 bytes of storage
      int packetSize = 3 * Constants.JAVA_SIZE_OF_SHORT + 
        source.length()*2 + data.length()*2;
        
      output.writeShort(packetSize);
      output.writeShort(source.length());
      output.writeChars(source);
      output.writeShort(data.length());
      output.writeChars(data);
      Debugger.debug(Debugger.TRACE, "Sent a packet: len="+packetSize);
    }
    catch (IOException e) {
      throw new IllegalStateException("ERROR: Could not write to feedback "+ 
          "server");
    }
  }

  public int getPort(){
    return port;
  }

  public void setHostName(String hostName){
    this.serverName = hostName;
  }

  public void setPort(int port){
    this.port = port;
  }
}
