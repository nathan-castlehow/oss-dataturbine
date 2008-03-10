package edu.sdsc.rtdsm.dig.sites.lake;

import java.util.*;

public class SensorMetaDataManager {

  private static SensorMetaDataManager theInstance = null;


  static {
    theInstance = new SensorMetaDataManager();
  }

  private Hashtable<String,SensorMetaData> hash = 
    new Hashtable<String,SensorMetaData>();

  private SensorMetaDataManager(){
  }

  public static SensorMetaDataManager getInstance(){
    return theInstance;
  }

  public SensorMetaData getSensorMetaDataIfPresent(String id){
    return hash.get(id);
  }

  public SensorMetaData getSensorMetaData(String id){
    if(!hash.containsKey(id)){

      // New sensor
      SensorMetaData smd = new SensorMetaData(id);
      hash.put(id, smd);
    }
    return hash.get(id);
  }

  public void insertMetaData(SensorMetaData smd){
    hash.put(smd.getId(), smd);
  }
}
