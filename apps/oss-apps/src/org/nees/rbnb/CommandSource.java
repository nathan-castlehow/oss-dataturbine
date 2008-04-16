/*
 * Created on Mar 5, 2004
 */
package org.nees.rbnb;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.rbnb.sapi.*;

/**
 * @author terry
 */
public class CommandSource extends RBNBBase {

	private static final String SOURCE_NAME = "Command";
	private static final String CHANNEL_NAME = "CommandData";
	
	private String sourceName = SOURCE_NAME;
	private String channelName = CHANNEL_NAME;

	private static final int DEFAULT_CACHE_SIZE = 900;
	private int cacheSize = DEFAULT_CACHE_SIZE;
	private static final int DEFAULT_ARCHIVE_SIZE = 0;
	private int archiveSize = DEFAULT_ARCHIVE_SIZE;

	Source source = null;
	ChannelMap sMap;
	int index;
	boolean connected = false;
	
	Thread commandThread;
	boolean runit = false;
	
	public static void main(String[] args) {
		CommandSource w = new CommandSource();
		if (w.parseArgs(args))
		{
			w.exec();
			w.startThread();
		}
	}
	
    protected String getCVSVersionString()
    {
        return
            "  CVS information... \n" +
            "  $Revision$\n" +
            "  $Date$\n" +
            "  $RCSfile: CommandSource.java,v $ \n";
    }

	/* (non-Javadoc)
	 * @see org.nees.rbnb.RBNBBase#setOptions()
	 */
	protected Options setOptions() {
		Options opt = setBaseOptions(new Options()); // uses h, s, p
		opt.addOption("n",true,"source_name *" + SOURCE_NAME);
		opt.addOption("c",true,"channel_name *" + CHANNEL_NAME);
		return opt;
	}
	
	/* (non-Javadoc)
	 * @see org.nees.rbnb.RBNBBase#setArgs(org.apache.commons.cli.CommandLine)
	 */
	protected boolean setArgs(CommandLine cmd) {

		if (!setBaseArgs(cmd)) return false;

		if (cmd.hasOption('n')) {
			String a=cmd.getOptionValue('n');
			if (a!=null) sourceName=a;
		}
		if (cmd.hasOption('c')) {
			String a=cmd.getOptionValue('c');
			if (a!=null) channelName=a;
		}
	
        if ((archiveSize > 0) && (archiveSize < cacheSize)){
            System.err.println(
                "a non-zero archiveSize = " + archiveSize + " must be greater then " +
                    "or equal to cacheSize = " + cacheSize);
            return false;
        }

		System.out.println("Starting CommandSource on " + getServer() + " as " + sourceName);
		System.out.println("  Channel name = " + channelName);
		System.out.println("  Use CommandSource -h to see optional parameters");
		
		return true;
	}

/*	
	public CommandSource(String server_host, String source_name, 
			String channel_name)
	{
		server = server_host;
		sourceName = source_name;
		channelName = channel_name;

		System.out.println("Starting CommandSource on " + server + " as " + sourceName);
		System.out.println("  Channel name = " + channelName);
		System.out.println("  Use CommandSource -h to see optional parameters");
	}
*/
	
	public void exec()
	{
		try {
			// Create a source and connect:
			if (archiveSize > 0)
				source=new Source(cacheSize, "create", archiveSize);
			else
				source=new Source(cacheSize, "none", 0);
			source.OpenRBNBConnection(getServer(),sourceName);
			sMap = new ChannelMap();
			index = sMap.Add(channelName);
			sMap.PutTimeAuto("timeofday");
			connected = true;
			System.out.println("CommandSource: Connection made to server = "
				+ getServer() + " as " + sourceName + " with " + channelName + ".");
		} catch (SAPIException se) { se.printStackTrace(); }
	}
	
	public void startThread()
	{
		
		if (!connected) return;
		
		// Use this inner class to hide the public run method
		Runnable r = new Runnable() {
			public void run() {
			  runWork();
			}
		};
		runit = true;
		commandThread = new Thread(r, "Command");
		commandThread.start();
		System.out.println("CommandSource: Started thread.");
	}

	public void stopThread()
	{
		runit = false;
		commandThread.interrupt();
		System.out.println("CommandSource: Stopped thread.");
	}
	
	private void runWork ()
	{
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		try {
			while(isRunning())
			{
				// get command
				System.out.println("ENTER COMMAND:");
				String commandString = in.readLine();
				
				System.out.println("Command is: " + commandString);
				
				// send command
				sMap.PutDataAsString(index,commandString);
				
				source.Flush(sMap);
			}
		} catch (SAPIException se) {
			se.printStackTrace(); 
		} catch (IOException e) {
			e.printStackTrace();
		}
		commandThread = null;
	}
	
	public boolean isRunning()
	{
		return (connected && runit);
	}

}
