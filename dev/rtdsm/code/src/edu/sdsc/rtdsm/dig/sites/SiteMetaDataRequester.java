package edu.sdsc.rtdsm.dig.sites;

import java.io.*;
import java.util.*;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;
import javax.xml.rpc.ParameterMode;
import java.rmi.RemoteException;
import java.net.MalformedURLException;
import javax.xml.rpc.ServiceException;

import edu.sdsc.rtdsm.framework.util.*;
import edu.sdsc.rtdsm.dig.sites.lake.*;

public class SiteMetaDataRequester {

  String id;
  String endPoint;
  String method;
  String sensorIdParamName;

  public SiteMetaDataRequester(String id) {

    this.id = id;
    try {

      DTProperties dtp = DTProperties.getProperties(
          Constants.DEFAULT_PROP_FILE_NAME);
      endPoint = dtp.getProperty(
          Constants.SITE_META_DATA_SERVICE_END_POINT_TAG);
      method = dtp.getProperty(
          Constants.SITE_META_DATA_SERVICE_METHOD_NAME_TAG);
      sensorIdParamName = dtp.getProperty(
          Constants.SITE_META_DATA_SERVICE_SENSORID_TAG);
    }
    catch(IOException ioe){

      ioe.printStackTrace();
      throw new IllegalStateException("No \"rtdsm.properties\" file found "+
          "while trying to get Site Listener properties");
    }
  }


  public SensorMetaData call(){

    String ret="";
    try {

      Service service = new Service();
      Call call = (Call) service.createCall();
      call.setTargetEndpointAddress(new java.net.URL(endPoint));
      call.setReturnType(XMLType.XSD_STRING);
      call.addParameter(sensorIdParamName, XMLType.XSD_STRING, ParameterMode.IN);
      call.setOperationName(method);
      call.setReturnType(XMLType.XSD_STRING);
      ret = (String) call.invoke(new Object[] { id });
      Debugger.debug(Debugger.TRACE ,"\nGot result for getSensor: >>" + ret + "<<");
    }
    catch(ServiceException se){
      se.printStackTrace();
    }
    catch(MalformedURLException mue){
      mue.printStackTrace();
    }
    catch(RemoteException re){
      re.printStackTrace();
    }
    return SiteMetaDataParser.parse(id, ret);
  }
}
