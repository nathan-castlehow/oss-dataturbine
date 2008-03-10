/*!
 * @author Lawrence J. Miller <ljmiller@sdsc.edu>
 * @author Cyberinfrastructure Laboratory for Environmental Observing Systems (CLEOS)
 * @author San Diego Supercomputer Center (SDSC)
 * @note Please see copywrite information at the end of this file.
 * @since $LastChangedDate: 2007-09-24 13:10:37 -0700 (Mon, 24 Sep 2007) $
 * $LastChangedRevision: 153 $
 * @author $LastChangedBy: ljmiller $
 * $HeadURL: file:///Users/hubbard/code/cleos-svn/cleos-rbnb-apps/trunk/src/edu/sdsc/cleos/RbnbToSrb.java $
 */
/*! @class RbnbToSrb utility class to process input messages that have been
 * constructed from RBNB Sink output and to mirror the rbnb channel tree in the
 * srb filesystem and the channel's themselves as files containing data/timestamp pairs */
package edu.sdsc.cleos;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.ChannelTree;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Sink;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import edu.sdsc.cleos.ISOtoRbnbTime;
import edu.sdsc.grid.io.FileFactory;
import edu.sdsc.grid.io.GeneralRandomAccessFile;
import edu.sdsc.grid.io.GeneralFile;
import edu.sdsc.grid.io.GeneralFileSystem;
import edu.sdsc.grid.io.srb.SRBAccount;
import edu.sdsc.grid.io.srb.SRBFileSystem;

public class RbnbToSrb {
	/*! @var SRBAccount srb account for which credentials are to be stored in $HOME/.srb */
	private SRBAccount			srbAccount;
	private GeneralFileSystem	srbFileSystem;
	private boolean 			clearSrb;
	private static Logger		logger = Logger.getLogger(RbnbToSrb.class.getName());
	/*! @var the rbnb server for which to act as a sink */
	private String 				rbnbServer = "localhost:3333";
	/* @note global parameters for rbnb requests */
	private double 				reqStart = Double.MAX_VALUE;
	private double 				reqDuration = -1;
	
	public RbnbToSrb() {
		srbAccount = null;
		srbFileSystem = null;
		clearSrb = false;
		if(! initSRB()) {
			logger.severe("Couldn't initialize SRB. Exiting.");
			System.exit(1);
		} else {
			logger.info("Established SRB connection: " + srbAccount.toString() + 
					"\nto filesystem: " + srbFileSystem.toString());
		}
	}

	/*! @brief initializes the connection to srb and associated filesystem */
	private boolean initSRB() {
		try {
			/*! @note this looks at ~/.srb/.Mdas[Auth Env] for credentials */
			srbAccount = new SRBAccount();
			srbFileSystem = new SRBFileSystem(srbAccount);
		} catch(IOException ioe) {
			logger.severe("Connecting to the SRB");
			return false;
		}
		return true;
	}

	/*! @brief */
	private void populateSRB(GeneralFile path, GeneralFile fle) throws IOException {
		path.mkdir();
		fle.createNewFile();
	}
	
	/*! @brief */
	private void cleanSRB(GeneralFile path, GeneralFile fle) {
		fle.delete();
		path.delete();	
	}
	
	/*! @brief */
	private byte[] readFileContents(GeneralRandomAccessFile fileToRead) throws IOException {
		long fileLen = fileToRead.length();
		byte[] fileContents = new byte[(int)fileLen];
		long filePointer = fileToRead.getFilePointer();
		
		fileToRead.seek(0);
		fileToRead.readFully(fileContents);
		fileToRead.seek(filePointer);
		return fileContents;
	}
	
	/*! @brief mirrors an input @param cmap tree structure in the srb filesystem */
	public boolean writeCmapToSrb(ChannelMap cmap) throws IOException  {
		if (clearSrb) {
			logger.info("Clearing out srb.");
		}
		/* @note rbnb variables */
		ChannelTree ctree = ChannelTree.createFromChannelMap(cmap);
		Iterator treeIt = ctree.iterator();
		ChannelTree.Node node = null;
		
		/* @note srb variables */
		GeneralFile genFilePath = null;
		GeneralFile genFile = null;
		
		/*! @note iterates through the channel map and mirrors the directory structure in
		 * srb, filling a file with data/timestamp pairs to represent the actual channels */
		while(treeIt.hasNext()) {
			node = (ChannelTree.Node)(treeIt.next());
			if(node.getType() == ChannelTree.CHANNEL) {
				genFilePath = FileFactory.newFile(srbFileSystem, node.getFullName());
				genFile = FileFactory.newFile(genFilePath, node.getName() + ".dat");
				
				if (clearSrb) {
					cleanSRB(genFilePath, genFile);
				} else {
					populateSRB(genFilePath, genFile);
					
					/* @todo write data from populatd cmap to the new srb file */
					GeneralRandomAccessFile srbFile = FileFactory.newRandomAccessFile(genFile, "rw");
					StringBuffer toWrite = new StringBuffer();
					
					int cmapIndex = cmap.GetIndex(node.getName());

					double[] cmapData, cmapTimes;
					if(0 < cmapIndex) {
						cmapData = cmap.GetDataAsFloat64(cmap.GetIndex(node.getName()));
						cmapTimes = cmap.GetTimes(cmap.GetIndex(node.getName()));
						logger.info("Got data array of size: " + cmapData.length);
					} else { // try with some fake data
						cmapData = new double[5];
						cmapTimes = new double[cmapData.length];
						long now = System.currentTimeMillis();
						
						for(int i=0; i<cmapData.length; i++) {
							cmapData[i] = Math.random()*1E5;
							cmapTimes[i] = now + (long)(Math.random()*1E6);
						}
						java.util.Arrays.sort(cmapTimes);
					} // else fake data
					
					/*! @note loads data and timestamps as tab delimited ascii strings */
					for(int i=0; i<cmapData.length; i++) {
						toWrite.append(cmapData[i]);
						toWrite.append('\t');
						toWrite.append(ISOtoRbnbTime.formatDate((long)cmapTimes[i]));
						toWrite.append('\r');
						toWrite.append('\n');
					}
					
					/*! @note sets the file pointer to the end, so that writes are appended */
					srbFile.seek(srbFile.length());
					/*! @note appends the previously prepared string to the file in srb that
					 * represents the channel */
					srbFile.writeBytes(toWrite.toString());
					logger.info("Wrote file \"" + node.getName() + ".dat\"");
					
					//logger.info("write/read for file: " + srbFile.toString() + ":\n" +
							//new String(readFileContents(srbFile)));
					
					srbFile.close();
				}
			} // if channel
		} // while	
		return true;
	}
	
	/*! @brief gets a channel map of the current registration of child server nodes within
	 *  the rbnb server to which this program is connected */
	private ChannelMap getChannelMap() throws SAPIException {
		ChannelMap initMap = new ChannelMap();
		ChannelTree ctree = null;
		Sink rbnbSink = new Sink();
		ArrayList<String> childServers = new ArrayList<String>();

		/* @note gets a channel map of everything in the ring buffer*/
		initMap.Add("*");
		rbnbSink.OpenRBNBConnection(rbnbServer, "SRBsink");
		rbnbSink.RequestRegistration(initMap);
		rbnbSink.Fetch(-1, initMap);

		ctree = ChannelTree.createFromChannelMap(initMap, "*");
		Iterator treeIt = ctree.iterator();
		ChannelTree.Node node = null;

		/* @note find the child servers and store them in arraylist */
		while(treeIt.hasNext()) {
			node = (ChannelTree.Node)(treeIt.next());
			if(node.getType() == ChannelTree.SERVER) {
				childServers.add(node.getName());
			}
		}
		logger.info("Detected " + childServers.size() + " child rbnb server" + ( (1 < childServers.size())?"s":"" ));
		Iterator childIterator = childServers.iterator();
		/* @note redo the cmap to load up the children's channels from the arraylist */
		initMap.Clear();
		int childCnt = 0;
		while(childIterator.hasNext()) {
			StringBuffer sbuff = new StringBuffer();
			sbuff.append(childIterator.next());
			sbuff.append("/*/*");
			initMap.Add(sbuff.toString());
			childCnt++;
		}

		logger.info("Loaded " + childCnt + " child server name" + ( (1 < childCnt)?"s":"" ) + " into cmap.");
		rbnbSink.RequestRegistration(initMap);
		rbnbSink.Fetch(-1, initMap);

		/* @todo fill cmap up with data */
		ChannelMap retval = validateCmapForData(initMap);
		
		rbnbSink.RequestRegistration(retval);
		rbnbSink.Fetch(-1, retval); // bless the cmap
		double reqEnd = reqStart + reqDuration; 
		logger.info("Got cmap entry count: " + retval.NumberOfChannels() + " start: " + ISOtoRbnbTime.formatDate((long)(reqStart*1000)) + " end: " + ISOtoRbnbTime.formatDate((long)(reqEnd*1000)));
		rbnbSink.Request(retval, 0, 0, "newest");
		rbnbSink.Fetch(1000, retval);
		
		rbnbSink.CloseRBNBConnection();
		return retval;
	}
	
	/*! @brief filters out empty and rbnb log and metric channels from @param cmap channel map */
	private ChannelMap validateCmapForData(ChannelMap cmap) throws SAPIException {
		ChannelMap retval = new ChannelMap();
		for(int i=0; i < cmap.NumberOfChannels(); i++) {
			/* @todo this filter should generalize to look at the channeltree parent node and filter out all hidden channels */
			if (0 < cmap.GetTimeDuration(i) &&
					!cmap.GetName(i).matches(".*ChannelListRequest") &&
					!cmap.GetName(i).matches(".*ArchiveDataBytes") &&
					!cmap.GetName(i).matches(".*CacheDataBytes") &&
					!cmap.GetName(i).matches(".*MemoryUsed") &&
					!cmap.GetName(i).matches(".*SocketBytes") &&
					!cmap.GetName(i).matches(".*SocketRate") &&
					!cmap.GetName(i).matches(".*TotalMemory")
			) {
				retval.Add(cmap.GetName(i));
				if(cmap.GetTimeStart(i) < this.reqStart) {
					this.reqStart = cmap.GetTimeStart(i);
				}
				if(this.reqDuration < cmap.GetTimeDuration(i)) {
					this.reqDuration = cmap.GetTimeDuration(i);
				}
			} // if
		} // for	
		return retval;
	}
	
	/* @brief main has functionality mostly for testing; this class is intended to be use as a callable
	 * object in an accumulator design pattern */
	public static void main(String[] args) {
		RbnbToSrb serb = new RbnbToSrb();
///////////////////////////////////// CLI handling
		Options opts = new Options ();
		CommandLineParser parser = new BasicParser();
		CommandLine cmd = null;
		opts.addOption ("a", false, "about");
	    opts.addOption ("e", false, "delete the filepath in the srb");
	    opts.addOption ("h", false, "print usage");
	    opts.addOption ("s", true,  "rbnb server for which to be a data sink");
	    HelpFormatter formatter = new HelpFormatter(); 
	    try {
	         cmd = parser.parse(opts, args); 
	      } catch (ParseException pe) {
	    	  logger.severe("Trouble parsing command line: " + pe);
	    	  System.exit(0);
	      }
	  
	      if (cmd.hasOption ("a")) {
	         System.out.println("About: this program accepts an rbnb ChannelMap " +
	                              "and then forwards the data and metadata to SRB");
	         System.exit(0);
	      } if (cmd.hasOption ("e")) {
	    	  serb.clearSrb = true;
	      } if (cmd.hasOption ("h")) {
	    	  formatter.printHelp ("RbnbToSrb", opts);
	    	  System.exit(0);
	      } if (cmd.hasOption ("s")) {
	    	  String a = cmd.getOptionValue("s");
	    	  serb.rbnbServer = a;
	      }
///////////////////////////////////// CLI handling
	      try {
			if(serb.writeCmapToSrb(serb.getChannelMap())) {
				logger.info("Wrote to SRB.");
			}
		 } catch(SAPIException sae) {
	    	  logger.severe("Cannot get a channelmap: " + sae);
	    	  sae.printStackTrace();
		} catch(IOException ioe) {
			logger.severe("Writing cmap to srb: " + ioe);
		}
	} // main
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