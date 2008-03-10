package edu.sdsc.rtdsm.framework.util;

import java.util.*;
import java.io.*;

public class DTProperties {

  Hashtable<String,String> hash = new Hashtable<String,String>();
  private static DTProperties theInstance = null;

  private DTProperties() throws IOException{

  }
  
  private void loadEntries(String propFile) throws IOException{

    RandomAccessFile raf;
    assert (null != propFile);
    String tmpString;
    String key,value;
    int start;
    raf = new RandomAccessFile(propFile, "r");
    assert (null != raf);

    while(null != (tmpString = raf.readLine())) {

      tmpString = tmpString.trim();
      if(tmpString.length() < 1 || '#' == tmpString.charAt(0)) {
        continue;
      }

      start = tmpString.indexOf('=');
      assert (start != -1);
      key = tmpString.substring(0,start);
      key = key.trim();
      value = tmpString.substring(start+1, tmpString.length());
      value = value.trim();
      hash.put(key,value);
    }
  }

  public String getProperty(String key){
    return (String)hash.get(key);
  }

  public static DTProperties getProperties(String fname) throws IOException{
    
    if(null == theInstance){
      theInstance = new DTProperties();
      theInstance.loadEntries(fname);
    }
    return theInstance;
  }
}

