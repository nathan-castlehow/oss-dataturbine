package edu.sdsc.rtdsm.framework.feedback;

import java.util.*;
import java.io.*;
import java.net.*;

import edu.sdsc.rtdsm.drivers.turbine.*;
import edu.sdsc.rtdsm.drivers.turbine.util.*;
import edu.sdsc.rtdsm.framework.src.*;
import edu.sdsc.rtdsm.framework.sink.*;
import edu.sdsc.rtdsm.framework.util.*;

public abstract class FeedbackAgent {

  int port;
  ServerSocket server;

  public FeedbackAgent( int port) {
    this.port = port;
  }

  public abstract void processFeedback(String source, String feedback);

  public void connect(){

    try {
      server = new ServerSocket(port);
    }
    catch (IOException ioe) {
      ioe.printStackTrace();
      throw new IllegalStateException("Socket Error: Could not connect to " +
          "port:" + port);
    }
  }

  public void accept() {

    Socket acceptSock = null;

    while(true) {

      System.out.println ("Waiting for feedback...");
      try {
        acceptSock = server.accept();
        System.out.println ("Accepted from " + acceptSock.getInetAddress());
      }
      catch (IOException ioe) {

        ioe.printStackTrace();
        throw new IllegalStateException("Socket Error: Could not \"accept\" at " +
            "port:" + port);
      }

      byte[] buffer = readFromSock(acceptSock);
      boolean terminateConn = processPacket(acceptSock, buffer);
      try {
        acceptSock.close();
      }
      catch(IOException ioe){
        ioe.printStackTrace();
        throw new IllegalStateException("Accept socket could not be closed");
      }
      if (terminateConn) {
        closeConnection();
        break;
      }
    }
  }

  private void closeConnection(){

    try {
      server.close();
    }
    catch (IOException ioe) {

      ioe.printStackTrace();
      throw new IllegalStateException("Socket Error: Could not close " +
          "serverSocket");
    }
  }

  private boolean processPacket(Socket sock, byte[] buffer){

    int len = 0;
    int type = 0;
    int offset =0;
    
    try {

      // An integer is 4 bytes in Java
      DataInputStream dis = new DataInputStream(new ByteArrayInputStream(buffer));

      len = dis.readShort();
      Debugger.debug(Debugger.TRACE, "len=" + len + "byteArr.length= " + buffer.length);

      len = dis.readShort();
      Debugger.debug(Debugger.TRACE, "Size of source str =" + len);

      StringBuffer sb = new StringBuffer(len) ;
      for (int i=0; i<len; i++)
        sb.append(dis.readChar()) ;
      String sourceStr = sb.toString();
      Debugger.debug(Debugger.TRACE, "Source str =" + sourceStr);

      len = dis.readShort();
      Debugger.debug(Debugger.TRACE, "Size of feedback str =" + len);

      sb = new StringBuffer(len) ;
      for (int i=0; i<len; i++)
        sb.append(dis.readChar()) ;
      String feedbackStr = sb.toString();
      Debugger.debug(Debugger.TRACE, "Feedback str =" + feedbackStr);

      
      // StructureInputStream sis = new StructureInputStream(buffer);
      // len = sis.readShort();
      // Debugger.debug(Debugger.TRACE, "len=" + len + "byteArr.length= " + buffer.length);

      // len = sis.readShort();
      // Debugger.debug(Debugger.TRACE, "Size of source str =" + len);

      // String sourceStr = sis.readString(len);
      // Debugger.debug(Debugger.TRACE, "Source str =" + sourceStr);

      // len = sis.readShort();
      // Debugger.debug(Debugger.TRACE, "Size of feedback str =" + len);

      // String feedbackStr = sis.readString(len);
      // Debugger.debug(Debugger.TRACE, "Feedback str =" + feedbackStr);

      processFeedback(sourceStr, feedbackStr);
    }
    catch(EOFException eof){
      eof.printStackTrace();
    }
    catch(IOException ioe){
      ioe.printStackTrace();
    }
    
    return false;
  }

  public byte[] readFromSock(Socket sock){

    byte[] buffer = new byte[Constants.JAVA_SIZE_OF_SHORT];
    try {
      BufferedInputStream in = new BufferedInputStream(sock.getInputStream());
      int numBytesRead = 0;

      numBytesRead += readBlocking(Constants.SITE_LISTENER_SEND_DATA_KEY_LENGTH_SIZE,in, buffer, 0, 2);
      short len = new StructureInputStream(buffer).readShort();
      Debugger.debug(Debugger.TRACE, "Packet len=" + len);

      byte[] tbuffer = new byte[len];
      for(int i=0; i<Constants.JAVA_SIZE_OF_SHORT;i++){
        tbuffer[i] = buffer[i];
      }
      buffer = tbuffer;
      numBytesRead += readBlocking(len-numBytesRead,in, buffer, numBytesRead, 2);
      for(int i=0; i<buffer.length;i++){
        Debugger.debug(Debugger.TRACE, "buffer["+i+"]="+buffer[i]);
      }
    }
    catch (IOException ioe) {

      ioe.printStackTrace();
      throw new IllegalStateException("Socket Error: Could not read the " +
          "input stream");
    }
    return buffer;
  }

  private int readBlocking(int numBytes, 
      BufferedInputStream in, byte[] buffer, int offset, int numTries) {

    int numBytesRead = 0;
    int count = 0;
    Debugger.debug(Debugger.TRACE, "Reading "+ numBytes + ". Storing from " + offset );
    try {
      while (numBytesRead < numBytes) {

          System.out.println("Blocking read: Number of bytes available = " + in.available());
          if(in.available() >= numBytes) {

            numBytesRead += in.read(buffer,offset,numBytes);
            count = 0;
          }
          else {
            Debugger.debug(Debugger.TRACE, "Sleep Waiting... " + numBytesRead + "/" +
                numBytes);
            Thread.sleep(100);
            count++;
            if(count > numTries){
              throw new IllegalStateException("Read Error: Unable to wait till the data is available " +
                  "input stream. Retried for " + count + " times" );
            }
          }
      }
    }
    catch(InterruptedException ie){
      ie.printStackTrace();
      throw new IllegalStateException("Read Error: Unable to wait till the data is available " +
          "input stream");
    }
    catch (IOException ioe) {

      ioe.printStackTrace();
      throw new IllegalStateException("Socket Error: Could not read the " +
          "input stream");
    }
    return numBytesRead;
  }

  public void connectAndListen() {
    connect();
    accept();
  }
}

