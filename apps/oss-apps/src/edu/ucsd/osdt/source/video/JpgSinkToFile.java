package org.nchc.rbnb;

import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Sink;


/**
 * This is a re-write of org.nees.rbnb.JpgSaverSink
 * <p>This class grabs the latest image from an RBNB image source and save it as a regular file into a
 * directory, or writes it to a named pipe (Linux only).
 * <p>The named pipe can be used to read and display the data stream with for example VLC. 
 * 
 * @see org.nees.rbnb.JpgLoaderSink
 * 
 * @author Ebbe Strandell @ NCHC, Taiwan
 */
public class JpgSinkToFile extends org.nees.rbnb.RBNBBase {

	/** the default RBNB sink name */
	private static final String DEFAULT_SINK_NAME = "JpgSaver";

	/** the default RBNB source name */
	private static final String DEFAULT_SOURCE_NAME = "RBNBClient";

	/** the default RBNB channel name */
	private static final String DEFAULT_CHANNEL_NAME = "video.jpg";

	/** the default RBNB output file */
	private static final String DEFAULT_OUTPUT_FILE = "JpgSaver/out.jpg";

	/** the RBNB sink */
	private final Sink sink;

	/** the RBNB sink name */
	private String sinkName = DEFAULT_SINK_NAME;

	/** the RBNB source name */
	private String sourceName = DEFAULT_SOURCE_NAME;

	/** the RBNB channel name */
	private String channelName = DEFAULT_CHANNEL_NAME;

	/** the RBNB output file */
	private File outputFile = null;

	/** the full RBNB channel path */
	private String channelPath = sourceName + "/" + channelName;

	/** a flag to indicate if we are connected to the RBNB server or not */
	private boolean connected = false;

	/** a flag to control the export process */
	private boolean doExport = false;

	
	/**
	 * Runs JpgSinkToFile.
	 */
	public static void main(String[] args) {
		JpgSinkToFile jpgSaver = new JpgSinkToFile();

		if (jpgSaver.parseArgs(args)) {
			setupShutdownHook(jpgSaver);

			jpgSaver.printSetup();
			jpgSaver.export();      
		}
	}

	/**
	 * Adds a shutdown hook to stop the export when called.
	 */
	private static void setupShutdownHook(final JpgSinkToFile jpgSaver) {
		final Thread workerThread = Thread.currentThread();

		Runtime.getRuntime ().addShutdownHook (new Thread () {
			public void run () {
				jpgSaver.stopExport();
				try { workerThread.join(); } catch (InterruptedException e) {}
			}
		});    
	}

	/**
	 * Creates JpgSaverSink.
	 */
	public JpgSinkToFile() {
		super();
		sink = new Sink();
	}

	protected String getCVSVersionString() {
		return ("$LastChangedDate$\n"
				+ "$LastChangedRevision$"
				+ "$LastChangedBy$"
				+ "$HeadURL$"); 
	}

	protected Options setOptions() {
		Options opt = setBaseOptions(new Options()); // uses h, s, p
		opt.addOption("k", true, "Sink Name *" + DEFAULT_SINK_NAME);
		opt.addOption("n", true, "Source Name *" + DEFAULT_SOURCE_NAME);
		opt.addOption("c", true, "Source Channel Name *" + DEFAULT_CHANNEL_NAME);
		opt.addOption("o", true, "Output file (or named pipe) *" + DEFAULT_OUTPUT_FILE);
		
	    setNotes("Subscribes to a RBNB data channel and outputs JPEG images to either a regular " +
	    		 "file or a named pipe. To create a named pipe in Linux: mkfifo myPipe. " + 
	    		 "The named pipe can later be used as an intermediate to read the stream with " +
	    		 "for example VLC.");
	    
		return opt;
	}

	protected boolean setArgs(CommandLine cmd) {

		if (!setBaseArgs(cmd))
			return false;

		if (cmd.hasOption('n')) {
			String a = cmd.getOptionValue('n');
			if (a != null)
				sourceName = a;
		}

		if (cmd.hasOption('c')) {
			String a = cmd.getOptionValue('c');
			if (a != null)
				channelName = a;
		}

		if (cmd.hasOption('k')) {
			String a = cmd.getOptionValue('k');
			if (a != null)
				sinkName = a;
		}

		if (cmd.hasOption('o')) {
			String a = cmd.getOptionValue('o');
			if (a != null)
				outputFile = new File(a);
		}else{
			outputFile = new File( DEFAULT_OUTPUT_FILE );
		}

		channelPath = sourceName + "/" + channelName;

		if (!connect()) {
			return false;
		}
		return true;
	}

	/**
	 * Setup the paramters for JPEG export.
	 * 
	 * @param serverName         the RBNB server name
	 * @param serverPort         the RBNB server port
	 * @param sinkName           the RBNB sink name
	 * @param channelPath        the full channel path
	 * @return                   true if the setup succeeded, false otherwise
	 */
	public boolean setup(String serverName, int serverPort, String sinkName, String channelPath ) {

		setServerName(serverName);
		setServerPort(serverPort);

		this.sinkName = sinkName;
		this.channelPath = channelPath;

		if (!connect()) {
			return false;
		}

		return true;
	}


	/**
	 * Prints the setup parameters.
	 */
	private void printSetup() {
		writeMessage("Starting JpgSaverToPipe on " + getServer() +
				" as " + sinkName);
		writeMessage("  Archiving channel " + channelPath + " to file/pipe " + outputFile);
	}


	/**
	 * Export JPEG image's to disk.
	 */
	public boolean export() {
		doExport = true;

		if (!runWork()) {
			return false;
		}

		doExport = false;

		return true;
	}

	/**
	 * Stop's exporting JPEG's to disk. This will return immediately.
	 */
	public void stopExport() {
		doExport = false;
	}

	/**
	 * Sees if JPEG's are being exported.
	 * 
	 * @return  true if exporting, false otherwise
	 */
	public boolean isExporting() {
		return isConnected();
	}

	/**
	 * Exports the JPEG's.
	 */
	private boolean runWork() {
		int imagesExported = 0;

		try {
			ChannelMap sMap = new ChannelMap();
			sMap.Add(channelPath);

			if (!connect()) {
				return false;
			}
			imagesExported = exportVideo(sMap);

		} catch (SAPIException e) {
			writeMessage("Error getting data from server: " + e.getMessage() + ".");
			return false;
		} catch (IOException e) {
			writeMessage("Error writing data to file: " + e.getMessage() + ".");
			return false;
		} finally {
			disconnect();
		}

		writeMessage("Export stopped. Wrote " + imagesExported + " JPEG images.               ");
		return true;
	}

	/**
	 * Exports JPEG's for a time range.
	 * 
	 * @param map             the channel map
	 * @return                the number of JPEG's images written to disk
	 * @throws SAPIException  if there is an error getting the data from the
	 *                        server
	 * @throws IOException    if there is an error writing the file
	 */
	private int exportVideo(ChannelMap map )
	throws SAPIException, IOException {

		if( outputFile.getParent() != null ){
			File oDir  = new File(outputFile.getParent());
			org.nees.rbnb.ArchiveUtility.confirmCreateDirPath(oDir);
		}

		sink.Subscribe(map, 0.0, 0.0, "newest");

		int fileCount = 0;

		while (doExport) {
			ChannelMap m = sink.Fetch(1000);

			if (m.GetIfFetchTimedOut()) {
				writeMessage("Warning: Request for data timed out, retrying.");
				continue;
			}
			
			int index = m.GetIndex(channelPath);
			if (index < 0) {
				writeMessage("Warning: Channel index < 0, retrying.");
				continue;
			}

			byte[][] data = m.GetDataAsByteArray(index);
			for (int i=0; i<data.length; i++) {
				//Write to file or named pipe:
				FileOutputStream out = null;
				try {
					out = new FileOutputStream(outputFile);
					out.write(data[i]);
				}catch (IOException e){
					writeMessage("Warning: IOException (" + e.getMessage() + "), retrying.");
				}finally{
					if( out != null )
						out.close();
				}

				fileCount++;
			}
		}
		return fileCount;
	}

	/**
	 * Connect to the RBNB server.
	 * 
	 * @return  true if connected, false otherwise
	 */
	private boolean connect() {
		if (isConnected()) {
			return true;
		}

		try {
			sink.OpenRBNBConnection(getServer(), sinkName);
		} catch (SAPIException e) {
			writeMessage("Error: Unable to connect to server.");
			disconnect();
			return false;
		}

		connected = true;

		return true;
	}

	/**
	 * Disconnects from the RBNB server.
	 */
	private void disconnect() {
		if (!isConnected()) {
			return;
		}
		sink.CloseRBNBConnection();
		connected = false;
	}

	/**
	 * Sees if we are connected to the RBNB server.
	 * 
	 * @return  true if connected, false otherwise
	 */
	public boolean isConnected() {
		return connected;
	}

}