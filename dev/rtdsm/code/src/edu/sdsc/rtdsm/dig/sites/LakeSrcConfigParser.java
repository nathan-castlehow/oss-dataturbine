package edu.sdsc.rtdsm.dig.sites;

import org.w3c.dom.*;
import org.xml.sax.*;
import javax.xml.parsers.*;
import java.util.*;
import java.io.*;

import edu.sdsc.rtdsm.framework.util.*;
import edu.sdsc.rtdsm.framework.sink.*;
import edu.sdsc.rtdsm.framework.src.*;
import edu.sdsc.rtdsm.dig.sites.lake.SensorMetaData;

// TODO:Here is the dependency of the framework on 
// turbine drivers. Remove by using reflection
import edu.sdsc.rtdsm.drivers.turbine.*;
import edu.sdsc.rtdsm.drivers.turbine.util.*;


public class LakeSrcConfigParser {


  public String fileName = "srcConfig.xml";
  Hashtable<String,SrcConfig> hash = new Hashtable<String,SrcConfig>();
  Hashtable<String,SinkConfig> feedbackHash = null;
  Vector<String> srcList = new Vector<String>();

  public LakeSrcConfigParser(String fileName) {
    this.fileName = fileName;
  }

  public LakeSrcConfigParser(){
    this.fileName = null;
  }

  public void parse(){

    DocumentBuilderFactory factory =
      DocumentBuilderFactory.newInstance();
    Document document = null;
    try {

      DocumentBuilder builder = factory.newDocumentBuilder();
      document = builder.parse( new File(fileName) );
      Node topNode = document.getDocumentElement();
      NodeList topList
        = document.getDocumentElement().getChildNodes();
      for (int i = 0; i < topList.getLength(); i++) {

        Node node_i = topList.item(i);
        if(node_i.getNodeType() == Node.ELEMENT_NODE) {
          String tagName = ((Element) node_i).getTagName();
          if (node_i.getNodeType() == Node.ELEMENT_NODE
              && tagName.equals(Constants.SRCCONFIG_XML_SOURCE_TAG)) {

            if(hash.containsKey(tagName)) {

              throw new IllegalStateException("The xml document has repeated " +
                  "source tag");
            }
            Element src = (Element) node_i;
            SrcConfig srcConfig = null;
            SinkConfig feedbackSinkConfig = null;

            if(! src.hasAttribute(Constants.SRCCONFIG_XML_SRC_NAME_TAG)){

              throw new IllegalStateException("The xml document has a source tag " +
                  "without a name attribute");
            }

            String srcName = src.getAttribute(Constants.SRCCONFIG_XML_SRC_NAME_TAG);
            Debugger.debug(Debugger.TRACE, "srcName="+srcName);
            NodeList srcChildList = src.getChildNodes();

            if(srcChildList.getLength() < 1) {

              throw new IllegalStateException("The xml document has a source tag " +
                  "without an orbParams tab");
            }

            for (int j = 0; j < srcChildList.getLength(); j++) {

              Node node_j = srcChildList.item(j);
              if (node_j.getNodeType() == Node.ELEMENT_NODE && 
                ((Element) node_j).getTagName().equals(
                  Constants.SRCCONFIG_XML_MAIN_CHANNEL_TAG)) {

                Element mainChannelNode = (Element) node_j;
                srcConfig = parseMainChannel(srcName, mainChannelNode);
              }
              else if (node_j.getNodeType() == Node.ELEMENT_NODE && 
                  ((Element) node_j).getTagName().equals(
                    Constants.SRCCONFIG_XML_FEEDBACK_CHANNEL_TAG)) {

                Element feedbackChannelNode = (Element) node_j;
                if(feedbackHash == null) {
                  feedbackHash = new Hashtable<String,SinkConfig>();
                }
                feedbackSinkConfig = parseFeedbackChannel(srcName, 
                    feedbackChannelNode);
              }
            }

            // Add the souce to the hash
            hash.put(srcName, srcConfig);
            if(feedbackHash != null) {
              feedbackHash.put(srcName, feedbackSinkConfig);
            }
            srcList.addElement(srcName);
          }
        }
      }
    } 
    catch(ClassCastException cce) {
      cce.printStackTrace();
    }
    catch (SAXException sxe) {
    // Error generated during parsing)
     Exception  x = sxe;
     if (sxe.getException() != null)
       x = sxe.getException();
       x.printStackTrace();
     } catch (ParserConfigurationException pce) {
      // Parser with specified options can't be built
      pce.printStackTrace();
     } catch (IOException ioe) {
       // I/O error
       ioe.printStackTrace();
    }
  }

  public boolean addConfig(SensorMetaData smd, boolean overwrite) {

    if(smd == null) {
      throw new IllegalArgumentException("A null value is passed to add to " +
          "the SensorMetaData table");
    }

    if(hash.get(smd.getId()) == null || overwrite) {

      //Either the hash does not contain the TurbineSrcConfig
      // Or the overwrite is set(anyway overwrite the older one)

      TurbineSrcConfig srcConfig = new TurbineSrcConfig(smd.getId());
      srcConfig.setChannelInfo(smd.getChannels(), smd.getChannelDatatypes());
      TurbineServer server = new TurbineServer();
      srcConfig.setServer(server);
      server.serverAddr = Constants.DEFAULT_SERVER_ADDRESS;
      server.userName = Constants.DEFAULT_SERVER_USERNAME;
      server.password = Constants.DEFAULT_SERVER_PASSWORD;
      hash.put(smd.getId(), srcConfig);
      return true;
    }
    return false;
  }

  public SinkConfig parseFeedbackChannel(String srcName,  
      Element feedbackChannelNode) {

    SinkConfig sinkConfig = null;
    return sinkConfig;
  }

  public SrcConfig parseMainChannel(String srcName,  
      Element mainChannelNode) {

    SrcConfig srcConfig = null;
    NodeList mainChannelChildList = mainChannelNode.getChildNodes();
    int counter = 0;
    for (int j = 0; j < mainChannelChildList.getLength(); j++) {

      Node childNode = mainChannelChildList.item(j);
      if (childNode.getNodeType() == Node.ELEMENT_NODE && 
        ((Element) childNode).getTagName().equals(
          Constants.SRCCONFIG_XML_ORB_PARAMS_TAG)) {

        if(counter != 0) {

          throw new IllegalStateException("Currently only one ORB is supported "
              +" Multiple ORBs will be supported in future");
        }
        counter++;


        Element orb = (Element) childNode;
        if(! orb.hasAttribute(Constants.SRCCONFIG_XML_ORB_TYPE_TAG)){

          throw new IllegalStateException("The xml document has a orb tag " +
              "without an orbType attribute");
        }
        String orbType = orb.getAttribute(Constants.SRCCONFIG_XML_ORB_TYPE_TAG);

        if(Constants.SRCCONFIG_XML_DATA_TURBINE_ORBTYPE_STR.equals(orbType)){

          srcConfig = new TurbineSrcConfig(srcName);
        }
        else {

          throw new IllegalStateException("Currently only data turbine ORB " + 
              "is supported. Multiple ORB types will be supported in " +
              "future");
        }
        srcConfig.parse(orb);
      }
      //-------------------------------------------------------
    }
    return srcConfig;
  }

  public SrcConfig getSourceConfig(String srcName) {
    return hash.get(srcName);
  }

  public Vector<String> getSourceList() {
    return srcList;
  }
}
