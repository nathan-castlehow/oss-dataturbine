package edu.sdsc.rtdsm.framework.db;

import java.util.*;

public class ReqPathConfig {

  public String reqPathName;
  public int datatype;
  public Vector<String> dbTableNameVec = new Vector<String>();
  public Vector<String> mapKeysVec = new Vector<String>();
  public Hashtable<String,String> mapTable = new Hashtable<String,String>();

  public void addDbTable(String name) {
    dbTableNameVec.addElement(name);
  }

  public void addMap(String key, String value){

    mapKeysVec.addElement(key);
    mapTable.put(key,value);
  }

  public Vector<String> getDbTableNames(){
    return dbTableNameVec;
  }

  public Vector<String> getMapKeys(){
    return mapKeysVec;
  }

  public String getMapValue(String key){
    return mapTable.get(key);
  }
}
