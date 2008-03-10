package edu.sdsc.rtdsm.framework.src;

import org.w3c.dom.*;
import org.xml.sax.*;
import javax.xml.parsers.*;

/**
 *
 * An interface for all SrcConfig parameters. Essentially an empty interface
 * 
 **/

public interface SrcConfig{
  public void parse(Element orb);
  public String getName();
}
