/* RBNBBase.java
 * ************************************************************************
 * Created on May 7, 2004
 *
 * A base class for all NEES RBNB widgets
 *
 */
package org.nees.rbnb;

import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;

/**
 * A base for RBNB Widgets. Includes base parameters and constents. For actual usage
 * this class must be extended. Specifically: the arguments must be precessed by
 * calling the methods of the supper class; usage, and other methods
 * need to be overwriten or extended.
 * 
 * @author Terry E Weymouth
 * @author Lawrence J. Miller
 * @author Jason P. Hanley
 * 
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 * $HeadURL$
 */
public abstract class RBNBBase
{
	private static final String DEFAULT_SERVER_NAME = "localhost";
	private static final String DEFAULT_SERVER_PORT = "3333";
	private String serverName = DEFAULT_SERVER_NAME;
	private String serverPort = DEFAULT_SERVER_PORT;
	private String server = serverName + ":" + serverPort;
  
  private final static String DEFAULT_RBNB_CLIENT_NAME = "RBNBClient";
  private String rbnbClientName = DEFAULT_RBNB_CLIENT_NAME;
   
	private String optionNotes = null;
	
	protected boolean parseArgs(String[] args) throws IllegalArgumentException
	{
		try {
			CommandLine cmd = (new PosixParser()).parse(setOptions(), args);
			return setArgs(cmd);
		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalArgumentException("Argument Exception: " + e);
		}
	}
	
	
	/**
	 * Process the parsed command line; will usuall call setBaseArgs
	 * 
	 * @param cmd (org.apache.commons.cli.CommandLine) -- the parsed command line
	 * @return true if the command line processed sucessfully
	 * 
	 * @see #setBaseArgs
	 */
	protected abstract boolean setArgs(CommandLine cmd);
	
  /**
   * Set the arguments handled by this class.
   * 
   * @param cmd  the command line
   * @return     true if the command line is processed successfully, false
   *             otherwise
   */
	protected boolean setBaseArgs(CommandLine cmd) {	
		if (cmd.hasOption('h')) {
			printUsage();
			return false;
		}

    if (cmd.hasOption("v")) {
      System.err.println(getCVSVersionString());
      return false;
    }

		if (cmd.hasOption('s')) {
			String a=cmd.getOptionValue('s');
			if (a!=null) setServerName(a);
		}

    if (cmd.hasOption('p')) {
      String a=cmd.getOptionValue('p');
      if (a!=null) {
        try {
          Integer.parseInt(a);
          setServerPort(a);
        } catch (NumberFormatException nf) {
          System.out.println("Please ensure to enter a numeric value for -p (server port). " + a + " is not valid!");
          return false;
        }
      }
    }
    
    if (cmd.hasOption('S')) {
      String a = cmd.getOptionValue('S');
      if (a != null) {
        rbnbClientName = a;
      }
    }
    
		return true;
	}
  
  /**
   * Get the RBNB server host name.
   * 
   * @param name  the host name of the server
   */
  public void setServerName(String name) {
    serverName = name;
  }

  /**
   * Get the RBNB server port number.
   * 
   * @param port  the port number of the server
   */
  public void setServerPort(String port) {
    serverPort = port;
  }  
	
  /**
   * Get the server name and port string.
   * 
   * @return  the server and port
   */
  public String getServer()
	{
		server = serverName + ":" + serverPort;
		return server;
	}
  
  /**
   * Get the name of this rbnb client.
   * 
   * @return  the name of the client
   */
  public String getRBNBClientName() {
    return rbnbClientName;
  }

  /**
   * Print out the usage of this application to standard output.
   *
   */
	protected void printUsage() {
		HelpFormatter f = new HelpFormatter();
		f.printHelp(this.getClass().getName(),setOptions());
		if (optionNotes != null)
		{
			System.out.println("Note: " + optionNotes);
		}
	}

	/**
	 * Set the Options object for command line parsing; will usually call setBaseOptions
	 * 
	 * @return org.apache.commons.cli.Options
	 * @see #setBaseOptions
	 */
	protected abstract Options setOptions();
	
  /**
   * Set the options supported by this base class.
   * 
   * @param opt  the options instance to add to
   * @return     the options instance with base class options
   */
	protected Options setBaseOptions(Options opt)
	{
		opt.addOption("h",false,"Print help");
		opt.addOption("s",true,"RBNB Server Hostname *" + DEFAULT_SERVER_NAME);
		opt.addOption("p",true,"RBNB Server Port Number *" + DEFAULT_SERVER_PORT);
    opt.addOption("S", true, "RBNB Client Name *" + DEFAULT_RBNB_CLIENT_NAME);
    opt.addOption("v",false,"Print Version information");
		return opt;
	}
	
	protected void setNotes(String n)
	{
		optionNotes = n;
	}

  protected abstract String getCVSVersionString();
}