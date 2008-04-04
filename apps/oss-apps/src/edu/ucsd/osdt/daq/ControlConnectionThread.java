/*! @brief a thread used to manage a tcp listener spawned from
@see edu.sdsc.cleos.NwpToRbnb
@author Lawrence J. Miller
@since 3/9/07
@note $HeadURL: file:///Users/hubbard/code/cleos-svn/cleos-rbnb-apps/trunk/src/edu/sdsc/cleos/ControlConnectionThread.java $
@note $LastChangedRevision: 153 $
@author $LastChangedBy: ljmiller $
@date $LastChangedDate: 2007-09-24 13:10:37 -0700 (Mon, 24 Sep 2007) $
 */
package edu.ucsd.osdt.daq;

import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Sink;
import edu.ucsd.osdt.source.numeric.NwpToRbnb;
import edu.ucsd.osdt.daq.ControlPort;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ControlConnectionThread extends Thread {
	private ServerSocket controlListener = null;
	/*! @var a Hashtable of host ids that have open command connections to this program with the time of connection as value */
	private Hashtable<String, Date> commanderHashtable;
	private int portToListen = 0;
	private boolean listening = false;
	private ControlPort nwpConnection = null;
	private NwpToRbnb myCaller = null;
	/*! @var runtime the Java runtime environment reference */
	private Runtime runtime;
	/*! @var commands a list of commands as described in the document "NwpToRbnb Command Specification"
	 http://nladr-cvs.sdsc.edu/svn-private/NEON/telepresence/dataturbine/trunk/doc/nwpToRbnbCmdSpec.doc
	 @note commands i this list are implemented on line 189 */
	private static String[] commands = {
			"bye",
			"forward",
			"get-command-connections",
			"get-nwptorbnb-memory",
			"get-nwptorbnb-network-statistics",
			"get-nwptorbnb-uptime",
			"get-rbnb-network-statistics",
			"get-rbnb-parent-network-statistics",
			"get-rbnb-memory",
			"get-rbnb-ringbuffer-resources",
			"get-rbnb-uptime",
			"get-rbnb-parent",
			"help",
			"ping-rbnb-parent",
			"verify-connections"
	};	
	/*! @var nwpCommands nwp commands as described in the document 
	 * "NEON Single-String Prototype NWP Command Protocol Specification"
	 * @note this list is a filter that must be kept in sync with the spec */
	private static String[] nwpCommands = {"reboot", "list-channels", "get-nwp-status",
			"get-gps-status", "enable-gps", "disable-gps", "get-nwp-location",
			"set-nwp-location", "get-nwp-time", "set-nwp-time", "get-channel-mapping",
			"get-channel-info", "get-channel-status", "open-channels",
			"close-channels", "set-channel-method", "get-channel-method"};
	static Log log = LogFactory.getLog(ControlConnectionThread.class.getName());
	/*! @var dateFormatHr a human-readable date format */
	public static SimpleDateFormat dateFormatHr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	/*! @brief a thread that will listen for incoming tcp connections that will receive
	 commands as text strings. It then spawns a thread per active connection to manage
	 said connection.
	 @param the control port connection to the connected nwp
	 @param the tcp port to listen on for incoming control connections
	 @param a pointer to the calling program */
	public ControlConnectionThread(ControlPort nwpControlSocket, int portToListen, NwpToRbnb caller) throws IOException {
		super("ControlConnectionThread");
		this.nwpConnection = nwpControlSocket;
		this.portToListen = portToListen;
		commanderHashtable = new Hashtable<String, Date>();
		this.listening = true;
		this.myCaller = caller;
		runtime = Runtime.getRuntime();
		/* need to sort these for binary searches to work */
		Arrays.sort(commands);
		Arrays.sort(nwpCommands);
		
		try {
			controlListener = new ServerSocket(portToListen);
		} catch (IOException e) {
			log.error("Opening command tcp listener " + e);
			throw e;
		}
		log.info("Created command/control tcp listener on port: " + this.portToListen);
	}

	public boolean isListening(boolean isListening) {
		return this.listening;
	}
	
	public void setListening(boolean setListening) {
		this.listening = setListening;
	}
	
	public static String[] getCommands() {
		return commands;
	}
	
	public static String[] getNwpCommands() {
		return nwpCommands;
	}
	
	/*! @brief always listens for connections, which are handed off to @see ControlConnectionHandler
	 * threads that spawned on demand */
	public void run() {
		
		while (listening) {
			try {
				new ControlConnectionHandler(controlListener.accept(), nwpConnection).start();
			} catch (IOException e) {
				log.error("Accepting incoming connection " + e);
			}
		} // while
		
		/*! @note if here, done listening */
		try {
			controlListener.close();
		} catch (IOException e) {
			log.error("Closing tcp listener " + e);
		}
		
	} // run ()

	/*! @brief an inner class to manage a control connection to forward commands to the
	 * connected nwp, once the listener establishes such a connection */
	private class ControlConnectionHandler extends Thread {
		private Socket socketConnection = null;
		private InetAddress remoteHost = null;
		private BufferedReader read = null;
		private BufferedWriter write = null;
		private ControlPort daqControl = null;
		
		ControlConnectionHandler (Socket socketconnection, ControlPort control) {
			super("ControlConnectionHandler");
			this.socketConnection = socketconnection;
			this.daqControl = control;
			///! @todo get the connecting host
			this.remoteHost = socketConnection.getInetAddress();
			//String remoteHostString = remoteHost.getHostAddress() + remoteHost.getCanonicalHostName();
			addCommander(remoteHost.toString(), new Date(System.currentTimeMillis()));
			log.info("Established commanding connection from: " + remoteHost.toString());
			try {
				this.read = new BufferedReader(new InputStreamReader(socketConnection.getInputStream()));
				this.write = new BufferedWriter(new OutputStreamWriter(socketConnection.getOutputStream()));
			} catch (IOException e) {
				log.error("Setting reader for command/control socket " + e);
			}
			
		} // constructor()
		
		/*! @brief abstract specialized command handling */
		private String processInput(String rawInput) {
			/*! @note this is assigned ot a temp String to accomodate future transformation */
			String inputCmd = rawInput;
			String retval = "OK:\t";
			/*! @var nwpCmd command to be forwarded to the nwp */
			String nwpCmd = "noop";
			
			log.debug(handlingAckMsg(inputCmd));
			
			/*! @note this checks for the 'forward' command
			 * @note this must be done prior to validity checking so that 'foward' will be validity checked*/
			Pattern fwdRegexPattern = Pattern.compile("^(forward)(.*)");
			Matcher fwdRegexMatcher = fwdRegexPattern.matcher(inputCmd);
			if (fwdRegexMatcher.find()) { // then the 'foward' command prefixed the input
				inputCmd = fwdRegexMatcher.group(1).trim();
				nwpCmd = fwdRegexMatcher.group(2).trim();
			}
			
			/*! @note the following case block must be arranged in alphabetical
			 *  order by the proggrammer, as the list of commands is sorted by
			 *  this program  in order to make this binary search work */
			int cmdIndex = Arrays.binarySearch(commands, inputCmd);
			if (cmdIndex < 0) { // then the input command is not in the list of valid commands
				return "ERROR:\t\"" + inputCmd + "\" is not a recognized command";
			}
////////////////////////////////////////////////- command implementation block	
			/*! @brief a large conditional block to dispatch on each command for specific actions */
	
			if(inputCmd.equals("bye")) {
				try {
					log.debug(handlingAckMsg(inputCmd));
					this.write.write("bye-bye\n");
					this.write.flush();
					this.socketConnection.close();
				} catch (IOException e) {
					log.error("Closing command/control in response to \"bye\"");
				}
			} else if(inputCmd.equals("forward")) {
				/* chops up the string to be forwarded */
				String[] cmdSplit = fwdRegexMatcher.group(2).trim().split("\\s");
				String nwpCmdToForward = cmdSplit[0].trim();
				
				/* validate the command */
				int nwpCmdIndex = Arrays.binarySearch(nwpCommands, nwpCmdToForward);
				log.debug(handlingAckMsg(inputCmd));
				
				if(nwpCmdIndex < 0) {
					log.warn("received INVALID nwp command: " + nwpCmdToForward);
					retval =  "ERROR:\t\"" + nwpCmdToForward + "\" is not a vaild NWP command and will not be forwarded";
				} else {
					log.info("received VALID nwp command: " + nwpCommands[nwpCmdIndex]);

					log.debug("Forwarding command: \"" + nwpCmdToForward + "\" from: " + remoteHost);
					String cmdArgs = "";
					for(int i=1; i<cmdSplit.length; i++) {
						cmdArgs += cmdSplit[i] + " ";
					}
					log.debug("With args: " + cmdArgs);

					String cmdResponse = null;
					try {
						/*! @note sends in the entire input, less the 'forward' prefix */
						cmdResponse = daqControl.writeReadControl(fwdRegexMatcher.group(2).trim());
					} catch(Exception e) {
						log.error("nwp command/response: " + e);
					}
					/*! @note the nwp comm protocol prepends OK, so it is ommitted in this case */
					retval = cmdResponse;
				} // else valid nwp command block
			} else if(inputCmd.equals("get-command-connections")) {
				retval += listCommanders();
			} else if(inputCmd.equals("get-nwptorbnb-memory")) {				
				retval += Long.toString(runtime.totalMemory()/1024) + "k/" + Long.toString(runtime.maxMemory()/1024) + "k totalMem/maxMem";	
			} else if(inputCmd.equals("get-nwptorbnb-network-statistics")) {
				long bytesXferred = 0;
				bytesXferred = myCaller.getSource().getSapiSource().BytesTransferred();
				retval += Long.toString(bytesXferred/1024) + "k";
			} else if(inputCmd.equals("get-nwptorbnb-uptime")) {
				long upTime = System.currentTimeMillis() - myCaller.getStartTime().getTime();
				return retval += Long.toString(upTime/1000) + "s\tStarted: " + dateFormatHr.format(myCaller.getStartTime().getTime());
			} else if(inputCmd.equals("get-rbnb-network-statistics")) {
				/*! @todo make a sink and read the RBNB server's hidden channels */
				retval = "command not implemented";
			} else if(inputCmd.equals("get-rbnb-parent-network-statistics")) {
				/*! @todo make a sink and read the RBNB server's hidden channels */
				retval = "command not implemented";
			} else if(inputCmd.equals("get-rbnb-memory")) {
				try {
					String[] psCmd = {"ps", "auxww"};
					BufferedReader in = new BufferedReader(new InputStreamReader(runtime.exec(psCmd).getInputStream()));
					String nextLine = "";
					while ((nextLine=in.readLine()) != null) {
						if(nextLine.matches(".*rbnb.jar.*")) {
							String[] psTokens = nextLine.split("[\\s]+");				
							retval += psTokens[5] + "k/" + psTokens[4] + "k rss/vsize";
						}
					}
				} catch(IOException e) {retval = "ERROR: Unable to query the system about rbnb";}
			} else if(inputCmd.equals("get-rbnb-ringbuffer-resources")) {
				int cacheSize = myCaller.getSource().getSapiSource().GetCacheSize();
				int archSize = myCaller.getSource().getSapiSource().GetArchiveSize();
				retval += Integer.toString(cacheSize) + "/" + Integer.toString(archSize);
			} else if(inputCmd.equals("get-rbnb-uptime")) {
				try {
					String[] psCmd = {"ps", "auxww"};
					BufferedReader in = new BufferedReader(new InputStreamReader(runtime.exec(psCmd).getInputStream()));
					String nextLine = "";
					while ((nextLine=in.readLine()) != null) {
						if(nextLine.matches(".*rbnb.jar.*")) {
							String[] psTokens = nextLine.split("[\\s]+");				
							retval += psTokens[9];
						}
					}
				} catch(IOException e) {retval = "ERROR: Unable to query the system about rbnb";}
			} else if(inputCmd.equals("get-rbnb-parent")) {
				try {
					getRbnbParent();
				} catch (Exception e) {retval = "ERROR: Unable to query the system about rbnb";}
			} else if(inputCmd.equals("help")) {
				retval += "\n" + cmdHelpString;
			} else if(inputCmd.equals("ping-rbnb-parent")) {
				try {
					String rbnbParent = getRbnbParent();
					Sink tmpSink = new Sink();
					tmpSink.OpenRBNBConnection(rbnbParent, "nwpToRbnb");
					tmpSink.CloseRBNBConnection();
					retval += "RBNB parent server on host: " + rbnbParent + " is alive";
				} catch(IOException e) {retval = "ERROR: Unable to query the system about rbnb";
				} catch(SAPIException sae) {retval = "ERROR: Unable to contact RBNB parent server";}
			} else if(inputCmd.equals("verify-connections")) {
				retval += "SNC:" + ( (myCaller.getControlSocket().isConnected())? "alive" : "dead" );
				retval += "\tSND:" + ( (myCaller.getDataSocket().isConnected())? "alive" : "dead" );
				retval += "\tSR:" + ( (myCaller.getSource().getSapiSource().VerifyConnection())? "alive" : "dead" );
				/*! @todo add the rbnb parent server status */
			} else {log.warn("processInput command filter has failed");} // should never get here - input has been filtered
			
			return retval;	
		} // processInput()
	
		
		/*! @brief an accessor that adds a new host address to commanderHashtable
		 * @note appends a hash of the timestamp if a collision is detected */
		public void addCommander(String newCommander, Date connectionTime) {
			commanderHashtable.put(( newCommander + Integer.toString(connectionTime.hashCode()) ), connectionTime);
		}
		
		
		/*! @brief an accessor that removes a host address from commanderHashtable */
		public void removeCommander(String dedCommander) {
			commanderHashtable.remove(dedCommander);
		}
		
		
		/*! @brief a method that dumps the key/values from commanderHashtable using human readable formatting for the timestamp */
		public String listCommanders() {
			String retval = "";
			String currKey = "";
			Enumeration<String> commanderHashtableKeys = commanderHashtable.keys();
			while(commanderHashtableKeys.hasMoreElements()) {
				currKey = (String)commanderHashtableKeys.nextElement();
				retval += (
						currKey + '\t' +
						dateFormatHr.format((Date)commanderHashtable.get(currKey))
						);
			}
			return retval;
		}
//////////////////////////////////////////////// - command implementation block
		
		
		/*! @brief parses -p arg out of ps to get the rbnb parent server host adress */
		public String getRbnbParent() throws IOException{
			String retval = "No RBNB parent server";
			String[] psCmd = {"ps", "auxww"};
			BufferedReader in = new BufferedReader(new InputStreamReader(runtime.exec(psCmd).getInputStream()));
			String nextLine = "";
			while ((nextLine=in.readLine()) != null) {
				if(nextLine.matches(".*rbnb.jar.*")) { // then this the command line that started the rbnb server
					String[] psTokens = nextLine.split("[\\s]+");				
					for(int i=0; i<psTokens.length; i++) {
						if(psTokens[i].matches("-p")) { // then the next arg is the parent rbnb server
							retval =  psTokens[i+1];
						} //  if -p
					} // for
				} // if rbnb.jar
			} // while
		return retval;
		}
		
		
		/*! @brief format acknowledgment messsages for cmd handling */
		public String handlingAckMsg(String cmd) {
			return "Handling command \"" + cmd + "\" from: " + remoteHost;
		}
		
		
		public void run() {
			/*! @todo accept commands and forward them to both the nwp and the rbnb
			server in a string channel */
			String greeting = "NWP interface reporting. Command me.\n";
			try {
				this.write.write(greeting);
				this.write.flush();
			} catch (IOException e) {
				log.error("Writing to command/control socket " + e);
			}
			
			String inputLine = "";
			try {
				while ((inputLine = read.readLine()) != null) {
					String processedCommand = processInput(inputLine);
					if (inputLine.compareToIgnoreCase("bye") != 0) {
						///! @todo split this up for different nwp commands
						this.write.write(processedCommand + '\n');
						this.write.flush();
					} else { // we got "bye"
						log.debug("Stopping command/control thread connected to: " + remoteHost);
						stop();
					}
				}
			} catch (IOException e) {
				log.error("Reading command/control socket input" + e);
				e.printStackTrace();
			}
			
		} // run()
	} // inner class
	
	public String cmdHelpString =
		"SNC - NwpToRbnb source to NWP control tcp connection\n" +
		"SND - NwpToRbnb source to NWP data tcp connection\n" +
		"CS  - external commanding tcp connection to NwpToRbnb source\n" +
		"SR  - NwpToRbnb source to RBNB server tcp connection\n" +
		"RP  - RBNB server to RBNB parent server tcp connection\n" +
		"* bye - closes the current CS session\n" +
		"return: bye\n" +
		"* forward - forwards a command from the published NWP command specification to the NWP over SNC after doing validity checking\n" +
		"return: NWP response to forwarded command\n" +
		"* get-command-connections - reports a list of hosts that have active CS' that have been initiated on port 55057\n" +
		"return: comma delimited list  of host IP addresses\n" +
		"* get-nwptorbnb-memory - reports the memory usage of this program instance\n" + 
		"return: # of bytes active/maximum available\n" +
		"* get-nwptorbnb-network-statistics - reports the amount of data transferred to the RBBN server by this program instance\n" +
		"return: # of bytes transferred\n" +
		"* get-nwptorbnb-uptime - report how long this program instance has been running\n" + 
		"return: time in seconds\n" +
		"* get-rbnb-network-statistics - report the tcp metrics measured over SR\n" +
		"return: socket bytes/socket rate\n" +
		"* get-rbnb-parent-network-statistics - report the tcp metrics measured over RP\n" +
		"return: socket bytes/socket rate\n" +
		"* get-rbnb-memory - report the cpu and memory usage of the RBNB server on the other end of SR\n" +
		"return: # of bytes active/maximum available\n" +
		"* get-rbnb-ringbuffer-resources - report the cache and archive allocation for the RBNB ring buffer  created by this source program in SR\n" +
		"return: cache bytes/ archive bytes\n" +
		"* get-rbnb-uptime - report how long the RBNB server for which this program instance is a source has been running\n" + 
		"return: time in seconds\n" +
		"* get-rbnb-parent - report the host of RP, if any\n" + 
		"return: host DNS name\n" +
		"* help - displays this list of command descriptions\n" + 
		"return: command list\n" +
		"* ping-rbnb-parent - verify liveness of SR\n" +
		"return: [alive | dead]\n" +
		"* verify-connections - verify liveness of SNC, SND, SR, and RP\n" +
		"return: SNC:[alive | dead]\tSND:[alive | dead]\tSR:[alive | dead]\t RP:[alive | dead]\t";
	
} // class
