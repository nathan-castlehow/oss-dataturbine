package org.nees.rbnb;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Source;

/**
 * A base class for all RBNB sources.
 *
 * @author Jason P. Hanley
 */
public abstract class RBNBSource extends RBNBBase {
  private static final int DEFAULT_CACHE_SIZE = 1024;
  protected int cacheSize = DEFAULT_CACHE_SIZE;
  
  private static final int DEFAULT_ARCHIVE_SIZE = 0;
  protected int archiveSize = DEFAULT_ARCHIVE_SIZE;
  
  private Source source = null;
  
  private boolean connected = false;
  
  protected Options setBaseOptions(Options opt) {
    super.setBaseOptions(opt);
    
    opt.addOption("z", true, "cache size *" + DEFAULT_CACHE_SIZE);
    opt.addOption("Z", true, "archive size *" + DEFAULT_ARCHIVE_SIZE);    
    
    return opt;
  }
  
  protected boolean setBaseArgs(CommandLine cmd) {
    if (!super.setBaseArgs(cmd)) {
      return false;
    }
    
    if (cmd.hasOption('z')) {
      String a = cmd.getOptionValue('z');
      if (a != null) {
        try {
          Integer i = new Integer(a);
          int value = i.intValue();
          cacheSize = value;
        } catch (NumberFormatException nfe) {
          System.out.println("Please ensure to enter a numeric value for -z option. " + a + " is not valid!");
          return false;   
        }
      }
    }
   
    if (cmd.hasOption('Z')) {
      String a = cmd.getOptionValue('Z');
      if (a != null) {
        try {
          Integer i = new Integer(a);
          int value = i.intValue();
          archiveSize = value;
        } catch (NumberFormatException nfe) {
          System.out.println("Please ensure to enter a numeric value for -Z option. " + a + " is not valid!");
          return false;   
        }
      } 
    } else {
      archiveSize = cacheSize * 10;
    }
    
    if ((archiveSize > 0) && (archiveSize < cacheSize)){
      System.err.println(
        "a non-zero archiveSize = " + archiveSize + " must be greater then " +
        "or equal to cacheSize = " + cacheSize);
      return false;
    }    

    return true;
  }
  
  protected boolean connect() {
    if (connected) {
      return true;
    }
    
    try {
      // Create a source and connect:
      if (archiveSize > 0) {
          source = new Source(cacheSize, "append", archiveSize);
      } else {
          source = new Source(cacheSize, "none", 0);
      }
      
      source.OpenRBNBConnection(getServer(), getRBNBClientName());
      connected = true;
      
      String cString =
          "Connecting to RBNB server with... \n"
              + " RBNB Server = "
              + getServer()
              + "; RBNB Cache Size = "
              + cacheSize
              + "; RBNB Archive Size = "
              + archiveSize
              + "; RBNB Source name = "
              + getRBNBClientName();
      System.out.println(cString);
    } catch (SAPIException se) {
      System.err.println("Failed to connect to the RBNB server.");
    }

    return connected;
  }

  protected void disconnect() { 
    if ((cacheSize != 0 || archiveSize != 0) && source != null) {
      source.Detach (); // close and keep cache and archive
    } else if (source != null) { // they are both zero; close and scrap
      source.CloseRBNBConnection();
    }
    
    source = null;
    connected = false;
  }
 
  /**
   * See if the source is connected to the server.
   * 
   * @return  true if connected, false if not
   */
  public boolean isConnected() {
    return connected;
  }
  
  /**
   * Get the RBNB source.
   * 
   * @return  the RBNB source 
   */
  public Source getSource() {
    return source;
  }
  
  /**
   * Get the cache size (in frames) for the RBNB source.
   * 
   * @return  the cache size
   */
  public int getCacheSize() {
    return cacheSize;
  }
  
  /**
   * Get the archive size (in frames) for the RBNB source.
   * 
   * @return  the archive size
   */
  public int getArchiveSize() {
    return archiveSize;
  }
}