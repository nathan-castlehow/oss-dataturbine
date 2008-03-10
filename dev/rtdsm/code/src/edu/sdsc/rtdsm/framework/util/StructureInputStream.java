package edu.sdsc.rtdsm.framework.util;
import java.io.*;

public class StructureInputStream extends java.io.ByteArrayInputStream {

  static int curr;
  private DataInputStream dis = null ;

  public StructureInputStream(byte[] b) {
    super(b);
    curr=1;
    dis = new DataInputStream((InputStream)this) ;
  }

  public final long readLong() throws IOException, EOFException {
    return dis.readLong() ;
  }

  public final double readDouble() throws IOException, EOFException {
    return dis.readDouble() ;
  }

  public final int readInt() throws IOException, EOFException {
    return dis.readInt() ;
  }

  public final int readUnsignedShort() throws IOException, EOFException {
    return dis.readUnsignedShort() ;
  }
  public final float readFloat() throws IOException, EOFException {
    return dis.readFloat() ;
  }

  public final short readShort() throws IOException, EOFException {
    return dis.readShort() ;
  }

  public final char readChar() throws IOException, EOFException {
    return (char)(dis.readByte()) ;
  }

  public final byte readByte() throws IOException, EOFException {
    return (dis.readByte()) ;
  }

  public final String readString(int strlen) throws IOException, EOFException {

    StringBuffer sb = new StringBuffer(strlen) ;
    for (int i=0; i<strlen; i++)
    sb.append((char)(dis.readByte())) ;
    return sb.toString() ;
  }

  public void skipBytes(int n) throws IOException {

    int no_of_bytes_to_skip = n ;
    int x = 0 ;
    do {

      x = dis.skipBytes(no_of_bytes_to_skip) ;
      no_of_bytes_to_skip -= x ;
    } while(no_of_bytes_to_skip > 0) ;
  }

  public byte[] getByte(int len) {

    byte[] b = new byte[len+1];
    int i;
    for(i=0;i<len;i++)
    b[i]=buf[curr+i];
    curr+=len;
    return b;
  }

  public String getStringValue(int startpos, int bytesToRead) throws EOFException, IOException{

    StringBuffer value = new StringBuffer(10);
    for (int i=startpos; i< (startpos + bytesToRead); i++)
    // value.append((char)buf[i]) ;
    value.append((char)readChar()) ;
    return value.toString() ;
  }

  public char getCharValue(int posn) {
    return (char)buf[posn] ;
  }
}
