package edu.sdsc.rtdsm.framework.sink;

import org.w3c.dom.*;
import org.xml.sax.*;
import javax.xml.parsers.*;
import java.util.*;
import java.io.*;
import edu.sdsc.rtdsm.framework.util.*;

// TODO:Here is the dependency of the framework on 
// turbine drivers. Remove by using reflection
import edu.sdsc.rtdsm.drivers.turbine.util.*;


public class SinkConfigParser {

  public static final String SINK_TAG="sink";
  public static final String SINK_NAME_TAG="name";
  public static final String ORB_PARAMS_TAG="orbParams";
  public static final String ORB_TYPE_TAG="orbType";
  public static final String DATA_TURBINE_ORBTYPE_STR = "DataTurbine";

  public String fileName = "sinkConfig.xml";
  Hashtable<String,SinkConfig> hash = new Hashtable<String,SinkConfig>();

  public void parse(){

    DocumentBuilderFactory factory =
      DocumentBuilderFactory.newInstance();
    Document document = null;
    try {

      Debugger.debug( Debugger.TRACE,"Node types:");
      Debugger.debug( Debugger.TRACE,"Node.ELEMENT_NODE="+Node.ELEMENT_NODE);
      Debugger.debug( Debugger.TRACE,"Node.ATTRIBUTE_NODE="+Node.ATTRIBUTE_NODE);
      Debugger.debug( Debugger.TRACE,"Node.ENTITY_NODE="+Node.ENTITY_NODE);
      Debugger.debug( Debugger.TRACE,"Node.DOCUMENT_NODE ="+Node.DOCUMENT_NODE );
      Debugger.debug( Debugger.TRACE,"Node.CDATA_SECTION_NODE ="+Node.CDATA_SECTION_NODE );
      Debugger.debug( Debugger.TRACE,"Node.ENTITY_REFERENCE_NODE ="+Node.ENTITY_REFERENCE_NODE );
      DocumentBuilder builder = factory.newDocumentBuilder();
      document = builder.parse( new File(fileName) );
      Node topNode = document.getDocumentElement();
      Debugger.debug( Debugger.TRACE," Node type: " + topNode.getNodeType() +" Node name:" + topNode.getNodeName() );
      NodeList topList
        = document.getDocumentElement().getChildNodes();
      for (int i = 0; i < topList.getLength(); i++) {

        Node node_i = topList.item(i);
        if(node_i.getNodeType() == Node.ELEMENT_NODE) {
          String tagName = ((Element) node_i).getTagName();
          if ( tagName.equals(SinkConfigParser.SINK_TAG) ) {

            if(hash.containsKey(tagName)) {

              throw new IllegalStateException("The xml document has repeated " +
                  "sink tag");
            }
            Element sink = (Element) node_i;

            if(! sink.hasAttribute(SinkConfigParser.SINK_NAME_TAG)){

              throw new IllegalStateException("The xml document has a sink tag " +
                  "without a name attribute");
            }

            String sinkName = sink.getAttribute(SinkConfigParser.SINK_NAME_TAG);
            NodeList sinkChildList = sink.getChildNodes();
            Debugger.debug(Debugger.TRACE, "sinkName= "+sinkName);

            if(sinkChildList.getLength() < 1) {

              throw new IllegalStateException("The xml document has a sink tag " +
                  "without an orbParams tab");
            }

            int counter = 0;
            for (int j = 0; j < sinkChildList.getLength(); j++) {

              Node node_j = sinkChildList.item(j);
              if (node_j.getNodeType() == Node.ELEMENT_NODE && 
                ((Element) node_j).getTagName().equals(
                  Constants.SINKCONFIG_XML_MAIN_CHANNEL_TAG)) {

                Element mainChannelNode = (Element) node_j;
                SinkConfig sinkConfig = parseMainChannel(mainChannelNode, sinkName);
                // Add the souce to the hash
                hash.put(sinkName, sinkConfig);
              }
            }
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

  public SinkConfig parseMainChannel(Element mainChannelNode, String sinkName) {

    int counter = 0;
    SinkConfig sinkConfig = null;
    NodeList mainChannelChildList = mainChannelNode.getChildNodes();
    for (int j = 0; j < mainChannelChildList.getLength(); j++) {

      Node mcNode = mainChannelChildList.item(j);
      Debugger.debug(Debugger.TRACE, "Main Channel Child= "+mcNode);
      Debugger.debug(Debugger.TRACE, "Main Channel num Children= " + mainChannelChildList.getLength());
      if (mcNode.getNodeType() == Node.ELEMENT_NODE && 
        ((Element) mcNode).getTagName().equals(
          SinkConfigParser.ORB_PARAMS_TAG)) {

        Debugger.debug(Debugger.TRACE, "Is an orb "+mcNode);
        if(counter != 0) {

          throw new IllegalStateException("Currently only one ORB is supported "
              +" Multiple ORBs will be supported in future");
        }
        counter++;


        Element orb = (Element) mcNode;
        if(! orb.hasAttribute(SinkConfigParser.ORB_TYPE_TAG)){

          throw new IllegalStateException("The xml document has a orb tag " +
              "without an orbType attribute");
        }
        String orbType = orb.getAttribute(SinkConfigParser.ORB_TYPE_TAG);

        if(SinkConfigParser.DATA_TURBINE_ORBTYPE_STR.equals(orbType)){
          
          sinkConfig = new TurbineSinkConfig(sinkName);
        }
        else {

          throw new IllegalStateException("Currently only data turbine ORB " + 
              "is supported. Multiple ORB types will be supported in " +
              "future");
        }
        sinkConfig.parse(orb);
      }
    }
    return sinkConfig;
  }

  public SinkConfig getSinkConfig(String sinkName) {
    return hash.get(sinkName);
  }
}
