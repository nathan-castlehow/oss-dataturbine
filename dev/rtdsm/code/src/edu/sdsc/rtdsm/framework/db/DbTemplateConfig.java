package edu.sdsc.rtdsm.framework.db;

import java.util.*;


/**
 * Wrapper class
 **/
public class DbTemplateConfig {

  public String templateName;
  Hashtable<String, ReqPathConfig> hash = 
    new Hashtable<String, ReqPathConfig>();
  Vector<String> reqPathNames = new Vector<String>();

  public void addReqPathConfig(String name, ReqPathConfig reqConfig){
    reqPathNames.addElement(name);
    hash.put(name, reqConfig);
  }

  public ReqPathConfig getReqPathConfig(String name){
    return hash.get(name);
  }

  public  Vector<String> getReqPathNames(){
    return reqPathNames;
  }
}
