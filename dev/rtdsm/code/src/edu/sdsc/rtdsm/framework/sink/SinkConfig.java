package edu.sdsc.rtdsm.framework.sink;

import org.w3c.dom.*;
import org.xml.sax.*;
import javax.xml.parsers.*;

/**
 *
 * An interface for all SinkConfig parameters. Essentially an empty interface
 * 
 **/

public interface SinkConfig{
  public void parse(Element orb);
  public String getName();
  public void setCallBackListener(SinkCallBackListener callback);
}
