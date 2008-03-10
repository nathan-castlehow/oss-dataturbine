package edu.sdsc.rtdsm.framework.util;


import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.net.*;
import java.util.Enumeration;

public class Utility {

  public static int unsignedByteToInt(byte b) {
    return (int) b & 0xFF;
  }

  public static String readIntoString(byte[] buffer, int offset, 
      int numBytes){

    byte newBuf[] = new byte[numBytes];
    return null;
  }

  public static int readIntoInt(byte[] buffer, int offset, int numBytes){

    int len = 0;
    int pos=0;
    byte newBuf[] = new byte[4];
    // The maximum number of bytes that can be read is 4 (size of int)
    if(numBytes > 4){

      throw new IllegalStateException("Int conversion Error: The number of " +
          "bytes that can be fit into a Java int is 4. The length of bytes " +
          "requested is " + numBytes);
    }
    for(int i=0;i<4 - numBytes;i++) {
      newBuf[i] = 0;
    }
    for(int i=0; i < numBytes;i++) {
      newBuf[4-1-i] = buffer[offset + numBytes - 1 - i];
    }

    // for(int i=0;i<newBuf.length;i++) {
    //   Debugger.debug(Debugger.TRACE, "newBuf["+i+"]="+newBuf[i]);
    // }
    len += Utility.unsignedByteToInt(newBuf[pos++]) << 24;
    len += Utility.unsignedByteToInt(newBuf[pos++]) << 16;
    len += Utility.unsignedByteToInt(newBuf[pos++]) << 8;
    len += Utility.unsignedByteToInt(newBuf[pos++]);
    return len;
  }

  public static byte[] shortToByteArray (final short s){	

    try {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      DataOutputStream dos = new DataOutputStream(bos);
      dos.writeShort((int)s);
      dos.flush();
      return bos.toByteArray();
    }
    catch(java.io.IOException ioe){
      ioe.printStackTrace();
      throw new IllegalStateException("Could not convert the short to byte array");
    }
  }

  public static byte[] intToByteArray (final int integer){	

    try {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      DataOutputStream dos = new DataOutputStream(bos);
      dos.writeInt(integer);
      dos.flush();
      return bos.toByteArray();
    }
    catch(java.io.IOException ioe){
      ioe.printStackTrace();
      throw new IllegalStateException("Could not convert the integer to byte array");
    }
  }

  // public static String convertSrcFromStdToTurbineFormat(String sourceName){
  //   return sourceName + Constants.TURBINE_SPECIFIC_SUFFIX;
  // }

  // public static String convertSrcFromTurbineToStdFormat(String sourceName){
  //   return sourceName.substring(0, sourceName.length()  - 
  //       Constants.TURBINE_SPECIFIC_SUFFIX.length());
  // }

  public static String getIPAddress(String hostname){
    String hostIP=null;
    try {

      java.net.InetAddress host = java.net.InetAddress.getByName(hostname);	
      hostIP = host.getHostAddress();
      Debugger.debug (Debugger.TRACE, "Hostname: "+hostname + " IP: " +
          hostIP);
    }
    catch(java.net.UnknownHostException uhe) {
      throw new IllegalStateException("Utility: Could not find " +
        "the IP address of the host machine" + hostname);
    }
    return hostIP;
  }

  public static String getNoLoopbackIPAddress(String hostname){
    String hostIP=null;
    try {

      java.net.InetAddress host = java.net.InetAddress.getByName(hostname);	
      if(host.isLoopbackAddress()){
        
        // Same host.. so give other interface ip address
        String localhost = null;
        InetAddress lch = host;
        Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
        
        while (e.hasMoreElements()) {
          NetworkInterface i = e.nextElement();
          
          Enumeration<InetAddress> ie = i.getInetAddresses();
          if (ie.hasMoreElements()) {
            lch = ie.nextElement();
            if (!lch.isLoopbackAddress()) {
              hostIP = lch.getHostAddress();
              break;
            }
          }
        }
      }
      else {
        hostIP = host.getHostAddress();
      }
      Debugger.debug (Debugger.TRACE, "Hostname: "+hostname + " IP: " +
          hostIP);
    }
    catch(java.net.UnknownHostException uhe) {
      throw new IllegalStateException("Utility: Could not find " +
        "the IP address of the host machine" + hostname);
    }
    catch(SocketException se){
      throw new IllegalStateException("Utility: Could not find " +
        "the non local address of the host machine" + hostname);
    }
    return hostIP;
  }
}
