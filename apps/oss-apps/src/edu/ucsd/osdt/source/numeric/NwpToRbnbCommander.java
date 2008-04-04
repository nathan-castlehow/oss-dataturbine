/*!
 * @author Lawrence J. Miller <ljmiller@sdsc.edu>
 * @author Cyberinfrastructure Laboratory for Environmental Observing Systems (CLEOS)
 * @author San Diego Supercomputer Center (SDSC)
 * @note Please see copywrite information at the end of this file.
 * @since $LastChangedDate: 2007-09-24 13:10:37 -0700 (Mon, 24 Sep 2007) $
 * $LastChangedRevision: 153 $
 * @author $LastChangedBy: ljmiller $
 * $HeadURL: file:///Users/hubbard/code/cleos-svn/cleos-rbnb-apps/trunk/src/edu/sdsc/cleos/NwpToRbnbCommander.java $
 */
/*! @class NwpToRbnbCommander
 * @brief interacts with edu.sdsc.sdsc.cleos.NwpToRbnb's
 * commanding interface that is exposed via edu.sdsc.cleos.controlConnectionThread  */

package edu.ucsd.osdt.source.numeric;

import edu.ucsd.osdt.daq.ControlConnectionThread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Logger;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class NwpToRbnbCommander {
	/*! @var nwptorbnbServer NwpToRbnb's tcp listener is a 'server' from the perspective of this program */
	private String         nwptorbnbServer = "localhost";
	private int            serverPort      = NwpToRbnb.DEFAULT_COMMAND_CONTROL_PORT;
	private Socket         socket          = null;
	private PrintWriter    write           = null;
	private BufferedReader read            = null;
	private static Logger  logger          = Logger.getLogger(NwpToRbnbCommander.class.getName());
	/*! @var execPause used as a sleep pause (miliseconds) to allow time for data streams through tcp
	 * sockets to settle reliably */
	private long           execPause       = 900;
	
	public NwpToRbnbCommander() {
		super();
	}

	/*! @brief opens tcp connection to NWpToRbnb and wraps its i/o streams as 'read' and 'write' */
	public void connect() throws IOException {
		this.socket = new Socket(nwptorbnbServer, serverPort);
		this.write = new PrintWriter(socket.getOutputStream(), true);
		this.read = new BufferedReader(new InputStreamReader(
				socket.getInputStream() ));
		logger.info("Connected to: " + nwptorbnbServer + ":" + Integer.toString(serverPort));
		logger.info("Response: " + read.readLine());
	}
	
	public void disconnect() throws IOException {
		logger.info("Disconnecting. Response: " + this.writeString("bye"));
		this.read = null;
		this.write = null;
		this.socket.close();
	}
	
	/*! @brief a wrapper for a command/response couplet to/from NwpToRbnb */
	public synchronized String writeString(String stringToWrite) throws IOException {
		String retval = "";
		write.println(stringToWrite);
		while(read.ready()) {
			retval += read.readLine();
		}
		return retval;
	}
	
	/*! @brief runs all commands in sequence. Forwarded commands are interleaved with @param pause */
	public void runAllCmds (float pause) {
		/*! @todo nested for loops for commands and forwarded commands */
		String[] cmds = ControlConnectionThread.getCommands();
		String[] nwpCmds = ControlConnectionThread.getNwpCommands();
		
		for(int i=0; i<cmds.length; i++) {
			String currCmd = cmds[i];
		}
		
		logger.info("stub of run all commands with pause: " + Float.toString(pause) + " seconds");
	}
	
	public String getServer() {
		return this.nwptorbnbServer;
	}
	
	public int getPort() {
		return this.serverPort;
	}
	
	/*! @brief hadles command-line arguments and implements the functionality to send NwpToRbnb
	 * a command that is cli specified */
	public static void main(String[] args) {
		NwpToRbnbCommander commander = new NwpToRbnbCommander();
		boolean doCommand = false;
		String commandToDo = "noop";
		boolean doCmdSuite = false;
		float cmdSuitePeriod = 0;
		///////////////////////////////////// CLI handling
		Options opts = new Options ();
		CommandLineParser parser = new BasicParser();
		CommandLine cmd = null;
		opts.addOption ("a", false, "about");
	    opts.addOption ("n", true,  "nwptorbnb server to connect to");
	    opts.addOption ("p", true,  "tcp port on server to connect to");
	    opts.addOption ("h", false, "print usage");
	    opts.addOption ("c", true,  "submit command");
	    opts.addOption ("st",true,  "send all commands in sequence at the specified period (s)");
	    HelpFormatter formatter = new HelpFormatter (); 
	    try {
	         cmd = parser.parse(opts, args); 
	      } catch (ParseException pe) {
	    	  logger.severe("Trouble parsing command line: " + pe);
	    	  System.exit(0);
	      }
	  
	      if (cmd.hasOption ("a")) {
	         System.out.println("About: this program sends commands " +
	                              "to a running instance of NwpToRbnb " +
	                              "over a tcp connection on its command/control port");
	         System.exit(0);
	      } if (cmd.hasOption ("n")) {
	    	  String a = cmd.getOptionValue("n");
	    	  commander.nwptorbnbServer = a;
	      } if (cmd.hasOption ("p")) {
	    	  String a = cmd.getOptionValue("p");
	    	  commander.serverPort = Integer.parseInt(a);
	      } if (cmd.hasOption ("h")) {
	    	  formatter.printHelp ("NwpToRbnbCommander", opts);
	    	  System.exit(0);
	      } if (cmd.hasOption ("c")) {
	    	  String a = cmd.getOptionValue("c");
	    	  commandToDo = a;
	    	  doCommand = true;
	      /*! @note the single command symantic will be reatained if a single vs. repetetive suite is specified */
	      } if (cmd.hasOption ("st") && ! cmd.hasOption ("c")) {
	    	  String a = cmd.getOptionValue("st");
	    	  cmdSuitePeriod = Float.parseFloat(a);
	    	  doCmdSuite = true;
	      }  
	      ///////////////////////////////////// CLI handling
	      
	      try {
				commander.connect();
				if(doCommand) {
					try {
						/*! @note pause needed for sockets to reset between program invocations */
						Thread.sleep(commander.execPause);
					} catch (InterruptedException ie) {
						logger.severe("Pausing main for " + Long.toString(commander.execPause) + " seconds: " + ie);
					}
					logger.info("Response: " + commander.writeString(commandToDo));
				} else if (doCmdSuite) {
					try {
						/*! @note pause needed for sockets to set up before going into this loop */
						Thread.sleep(commander.execPause);
					} catch (InterruptedException ie) {
						logger.severe("Pausing main for " + Long.toString(commander.execPause) + " seconds: " + ie);
					}
					
					// do alll commands
					
				} // elif
				commander.disconnect();
			} catch (IOException ioe) {
		        logger.severe("Trouble connecting to: " + commander.getServer() + ":" + Integer.toString(commander.getPort()) + ioe);
		        System.exit(1);
			}
			System.exit(0);
	} // main()
} // class

/** Copyright (c) 2007, Lawrence J. Miller and CLEOS
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *    * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the 
 * documentation and/or other materials provided with the distribution.
 *   * Neither the name of the San Diego Supercomputer Center nor the names of
 * its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */