/*! @file RBNBBase.java 
 * @brief A base class for RBNB Widgets. Includes base parameters and constents.
 * For actual usage, this class must be extended. Specifically: the arguments must
 * be precessed by calling the methods of the supper class; usage, and other methods
 * need to be overwriten or extended.
 * @author Lawrence J. Miller
 * @note $HeadURL$
 * @note $LastChangedRevision$
 * @author $LastChangedBy$
 * @date $LastChangedDate$
 * 
 * @todo append host address to all source names that get registered on the dataturbine server
 * 
*/
package edu.ucsd.osdt.util;

import edu.ucsd.osdt.source.BaseSource;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;

public class RBNBBase
{
	private static final String DEFAULT_SERVER_NAME = "localhost";
	private static final String DEFAULT_SERVER_PORT = "3333";
	protected String serverName = DEFAULT_SERVER_NAME;
	private String serverPort = DEFAULT_SERVER_PORT;
	private String server = serverName + ":" + serverPort;
	/*! @var myBaseSource accumulated in the case of a source, else null */
	/*! @todo have this acccumulate an osdt-wrapped sink, too; this gets around
	 *  the need for multiple inheritance for sapi and basal functionality that
	 *   we want to add to osdt apps */
	protected BaseSource myBaseSource;
    protected final static String DEFAULT_RBNB_CLIENT_NAME = "OSDT Client";
    protected String rbnbClientName = DEFAULT_RBNB_CLIENT_NAME;
	
    
    /*! @fn constructor */
	public RBNBBase (BaseSource varBaseSource)
	{
		myBaseSource = varBaseSource;
	}
	
	
	/*! @fn cli handling */
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
	 * Process the parsed command line; will usually call setBaseArgs
	 * @param cmd (org.apache.commons.cli.CommandLine) -- the parsed command line
	 * @return true if the command line processed sucessfull
	 * @see #setBaseArgs
	 */
	protected boolean setArgs(CommandLine cmd)
	{
		return setBaseArgs(cmd);
	}
	
	
  /**
   * Set the arguments handled by this class.
   * @param cmd  the command line
   * @return     true if the command line is processed successfully, false otherwise
   */
	protected boolean setBaseArgs(CommandLine cmd)
	{	
		if (cmd.hasOption('h')) {
			printUsage();
			return false;
		} if (cmd.hasOption("v")) {
			System.err.println(getCVSVersionString());
			return false;
		} if (cmd.hasOption('s')) {
			String a=cmd.getOptionValue('s');
			if (a!=null) setServerName(a);
		} if (cmd.hasOption('p')) {
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
		} if (cmd.hasOption('S')) {
			String a = cmd.getOptionValue('S');
			if (a != null) {
				rbnbClientName = a;
			}
		}
		return true;
	}
  
	/* getter/setter block */
	public String getServerName()
	{
		return this.serverName;  
	}

	public void setServerName(String name)
	{
		serverName = name;
	}

	public int getServerPort()
	{
		return Integer.parseInt(this.serverPort);
	}

	public void setServerPort(String port)
	{
		serverPort = port;
	}  

	public String getServer()
	{
		server = serverName + ":" + serverPort;
		return server;
	}

	public String getRBNBClientName()
	{
		return rbnbClientName;
	}
	/* getter/setter block */
  
  /**
   * Print out the usage of this application to standard output.
   */
	public void printUsage()
	{
		HelpFormatter f = new HelpFormatter();
		f.printHelp(this.getClass().getName(),setOptions());
		if (optionNotes != null)
		{
			System.out.println("Note: " + optionNotes);
		}
	}

	
	/**
	 * Set the Options object for command line parsing; will usually call setBaseOptions
	 * @return org.apache.commons.cli.Options
	 * @see #setBaseOptions
	 */
	protected Options setOptions()
	{
		return null;
	}
	
	
  /**
   * Set the options supported by this base class.
   * @param opt  the options instance to add to
   * @return     the options instance with base class options
   */
	protected Options setBaseOptions(Options opt)
	{
		/*! @todo prop file to reflect these common args */
		opt.addOption("h",false,"Print help");
		opt.addOption("s",true,"RBNB Server Hostname *" + DEFAULT_SERVER_NAME);
		opt.addOption("p",true,"RBNB Server Port Number *" + DEFAULT_SERVER_PORT);
		opt.addOption("S", true, "RBNB Client Name *" + DEFAULT_RBNB_CLIENT_NAME);
		opt.addOption("v",false,"Print Version information");
		return opt;
	}
	
	
	/*! @brief version control information */
	public String getSVNVersionString()
	{
		StringBuffer retval = new StringBuffer();
		retval.append("$HeadURL$" + "\n");
		retval.append("$LastChangedRevision$" + "\n");
		retval.append("$LastChangedBy$" + "\n");
		retval.append("$LastChangedDate$");
		return retval.toString();
	}
	
	
	/*! @version legacy version control information */
	public String getCVSVersionString()
	{
		return getSVNVersionString();
	}
}