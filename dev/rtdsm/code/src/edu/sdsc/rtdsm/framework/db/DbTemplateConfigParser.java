package edu.sdsc.rtdsm.framework.db;

import org.w3c.dom.*;
import org.xml.sax.*;
import javax.xml.parsers.*;
import java.util.*;
import java.io.*;
import edu.sdsc.rtdsm.framework.util.*;

public class DbTemplateConfigParser {

  public static final String DB_TEMPLATE_TAG="DBTemplate";
  public static final String TEMPLATE_TAG="template";
  public static final String REQ_PATH_TAG="reqPath";
  public static final String DB_TABLE_TAG="dbTable";
  public static final String MAP_TAG="map";
  public static final String NAME_TAG = "name";
  public static final String DATATYPE_TAG = "dataType";
  public static final String DB_COL_TAG = "dbCol";
  public static final String SOURCE_KEY_STR = "source";
  public static final String CHANNEL_KEY_STR = "channel";
  public static final String TIME_GEN_STR = "timeGenerated";
  public static final String KEY_TAG = "key";


  public String fileName;
  Hashtable<String,DbTemplateConfig> hash = new Hashtable<String,DbTemplateConfig>();

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
          if ( tagName.equals(DbTemplateConfigParser.TEMPLATE_TAG) ) {

            if(hash.containsKey(tagName)) {

              throw new IllegalStateException("The xml document has repeated " +
                  "template tag");
            }
            Element tlate = (Element) node_i;

            DbTemplateConfig templateConfig = new DbTemplateConfig();

            if(! tlate.hasAttribute(DbTemplateConfigParser.NAME_TAG)){

              throw new IllegalStateException("The xml document has a template tag " +
                  "without a name attribute");
            }

            String tlateName = tlate.getAttribute(DbTemplateConfigParser.NAME_TAG);
            NodeList tlateChildList = tlate.getChildNodes();
            templateConfig.templateName = tlateName;
            hash.put(tlateName,templateConfig);

            if(tlateChildList.getLength() < 1) {

              throw new IllegalStateException("The xml document has a template tag " +
                  "without an reqPath tag");
            }

            int counter = 0;
            for (int j = 0; j < tlateChildList.getLength(); j++) {

              Node node_j = tlateChildList.item(j);
              if (node_j.getNodeType() == Node.ELEMENT_NODE && 
                ((Element) node_j).getTagName().equals(
                  DbTemplateConfigParser.REQ_PATH_TAG)) {

                Element reqPath = (Element) node_j;
                parseReqPath(reqPath, templateConfig);
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

  public void parseReqPath(Element reqPath, DbTemplateConfig templateConfig) {

    if(! reqPath.hasAttribute(DbTemplateConfigParser.NAME_TAG)){

      throw new IllegalStateException("The xml document has a request path tag " +
          "without a name attribute");
    }
    if(! reqPath.hasAttribute(DbTemplateConfigParser.DATATYPE_TAG)){

      throw new IllegalStateException("The xml document has a request path tag " +
          "without a datatype attribute");
    }

    ReqPathConfig reqPathConfig = new ReqPathConfig();
    String reqPathString = reqPath.getAttribute(DbTemplateConfigParser.NAME_TAG);
    String reqPathDatatype = reqPath.getAttribute(DbTemplateConfigParser.DATATYPE_TAG);

    if("double".equals(reqPathDatatype)) {

      reqPathConfig.datatype = Constants.DATATYPE_DOUBLE;
    }
    else {
      throw new IllegalArgumentException("Currently only double data type " +
          "is supported. More options will be supported soon");
    }

    NodeList reqPathChildList = reqPath.getChildNodes();

    int counter = 0;
    for (int j = 0; j < reqPathChildList.getLength(); j++) {

      Node node_j = reqPathChildList.item(j);
      if (node_j.getNodeType() == Node.ELEMENT_NODE) {
        
        if(((Element) node_j).getTagName().equals(
          DbTemplateConfigParser.DB_TABLE_TAG)) {

          Element dbTable = (Element) node_j;
          parseDbTable(dbTable, reqPathConfig);
        }
        else if(((Element) node_j).getTagName().equals(
          DbTemplateConfigParser.MAP_TAG)) {

          Element map = (Element) node_j;
          parseMap(map, reqPathConfig);
        }
      }
    }
    templateConfig.addReqPathConfig(reqPathString, reqPathConfig);
  }


  public void parseDbTable(Element dbTable, ReqPathConfig reqPath) {
    
    reqPath.addDbTable(dbTable.getAttribute(DbTemplateConfigParser.NAME_TAG));
  }

  public void parseMap(Element dbTable, ReqPathConfig reqPath) {

    String mapKey = dbTable.getAttribute(DbTemplateConfigParser.KEY_TAG);
    String mapValue = dbTable.getAttribute(DbTemplateConfigParser.DB_COL_TAG);
    reqPath.addMap(mapKey, mapValue);
  }

  public DbTemplateConfig getTemplateConfig(String templateConfigName) {
    return hash.get(templateConfigName);
  }
}
