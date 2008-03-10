/*
 * Created on Mar 25, 2004
 */
package org.nees.rbnb;

import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JEditorPane;
import javax.swing.JSplitPane;

import javax.swing.BorderFactory;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import javax.swing.Timer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Date;
import java.util.TimeZone;
import java.text.SimpleDateFormat;

import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;

import com.rbnb.sapi.*;

/**
 * This Applicaiton create an RBNB channel list from the RBNB server specified on the
 * command line. The command line argument -s specifies the server and -p the port.
 * The defaults are localhost and 3333, respectively. The channel list is refreshed
 * automatically. Click on a node in the list to see it's 
 * 
 * @author Terry E Weymouth
 *
 */
public class ChannelListSwing extends JPanel
							  implements TreeSelectionListener
{
	// copied from RBNB Base; it's ugly and should be refactored, but...
	// Terry E. Weymouth, Feb 10, 2005
	// ----
	private static final String SERVER_NAME = "localhost";
	private static final String SERVER_PORT = "3333";
	private String serverName = SERVER_NAME;
	private String serverPort = SERVER_PORT;
	private String server = serverName + ":" + serverPort;
	// -----
	
	private String optionNotes = null;
	

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM d, yyyy h:mm:ss.SSS aa");
	private static final TimeZone TZ = TimeZone.getTimeZone("GMT");
	
	static
	{
		DATE_FORMAT.setTimeZone(TZ);
	}

	private Sink sink = null;
	private ChannelMap sMap;
	private boolean connected = false;

	private JTree tree;
	private JEditorPane htmlPane;
	private boolean needsRefresh = false;
	private Timer checkRefresh;
	private Thread timerThread;
	private boolean runit = false;
	
	public static void main(String[] args) {
		//Schedule a job for the event-dispatching thread:
		//creating and showing this application's GUI and start the applicaiton
		final String[] a = args;
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				ChannelListSwing c = new ChannelListSwing();
				if (c.parseArgs(a))
				{
					c.connect();
					c.createAndShowGUI();
					c.setTimer();
					c.startCheckThread();
				}
			}
		});
	}
	
	protected Options setOptions() {
		Options opt = setBaseOptions(new Options()); // uses h, s, p
		return opt;
	}
	
	protected boolean setArgs(CommandLine cmd){

		if (!setBaseArgs(cmd)) return false;
		
		System.out.println("Starting ChannelList on " + getServer());
		System.out.println("  Use ChannelList -h to see optional parameters");
		
		return true;
	}
	
	public void setNameAndPort(String name, String port)
	{
		serverName = name;
		serverPort = port;
	}
		
	protected void createAndShowGUI() {
		
		//Make sure we have nice window decorations.
		//JFrame.setDefaultLookAndFeelDecorated(true);

		//Create and set up the window.
		JFrame frame = new JFrame("Channel List");
		//frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		initGraphics();
		setOpaque(true); //content panes must be opaque
		frame.setContentPane(this);

		//Display the window.
		frame.pack();
		frame.setVisible(true);
	}
	
	protected void initGraphics() {
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		
		//Create the nodes.
		DefaultMutableTreeNode top = createNodes();

		//Create a tree that allows one selection at a time.
		tree = new JTree(top);
		tree.getSelectionModel().setSelectionMode
				(TreeSelectionModel.SINGLE_TREE_SELECTION);

		//Listen for when the selection changes.
		tree.addTreeSelectionListener(this);

		//Create the scroll pane and add the tree to it. 
		JScrollPane treeView = new JScrollPane(tree);

		//Create the HTML viewing pane.
		htmlPane = new JEditorPane();
		htmlPane.setEditable(false);
		JScrollPane htmlView = new JScrollPane(htmlPane);

		//Add the scroll panes to a split pane.
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setTopComponent(treeView);
		splitPane.setBottomComponent(htmlView);

		Dimension minimumSize = new Dimension(100, 50);
		htmlView.setMinimumSize(minimumSize);
		treeView.setMinimumSize(minimumSize);
		splitPane.setDividerLocation(100); //XXX: ignored in some releases
										   //of Swing. bug 4101306
		//workaround for bug 4101306:
		//treeView.setPreferredSize(new Dimension(100, 100)); 

		splitPane.setPreferredSize(new Dimension(500, 300));

		//Add the split pane to this panel.
		add(splitPane);

		//root and frame
		JFrame itsFrame = new JFrame("ChannelList");
		itsFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		itsFrame.setSize(new Dimension(120, 40));
		itsFrame.getContentPane().add(this, BorderLayout.CENTER);

		//Display the window.
		itsFrame.pack();
		itsFrame.setVisible(true);
	}

	protected void setTimer()
	{
		int delay = 100; //milliseconds; 1/10 second
		ActionListener taskPerformer = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (needsRefresh)
				{
					//recreate the nodes.
					DefaultMutableTreeNode top = createNodes();
					DefaultTreeModel model = new DefaultTreeModel(top);
					tree.setModel(model);
					invalidate();
					needsRefresh = false;
				}
			}
		};
		checkRefresh = new Timer(delay, taskPerformer);
		checkRefresh.start();
	}


	private DefaultMutableTreeNode createNodes() {
		DefaultMutableTreeNode top;
		if (!connected)
			top = new DefaultMutableTreeNode("UNCONNECTED (attempting to connect to "
				+ getServer() + ")");
		else
		{
			top = new DefaultMutableTreeNode("Connected to " + getServer());
			ChannelTree ct = getChannelTree();
			if (ct == null)
			{
				top = new DefaultMutableTreeNode("No Channel Tree (connection dropped?)");
			}
			else
			{
				Iterator i = ct.rootIterator();
				while (i.hasNext())
				{
					top.add(makeNodes((ChannelTree.Node)i.next()));
				}
			}
		}
		
		return top;
	}
	
	private DefaultMutableTreeNode makeNodes(ChannelTree.Node node)
	{
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(new NodeCover(node));
		List l = node.getChildren();
		Iterator i = l.iterator();
		while (i.hasNext())
		{
			root.add(makeNodes((ChannelTree.Node)i.next()));
		}

		return root; 
	}

	/** Required by TreeSelectionListener interface. */
	public void valueChanged(TreeSelectionEvent e) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)
						   tree.getLastSelectedPathComponent();

		if (node == null) return;
		Object nodeInfo = node.getUserObject();
		if (nodeInfo instanceof NodeCover)
		{
			NodeCover nc = (NodeCover)nodeInfo;	
			ChannelTree.Node ctNode = nc.node;
			displayMetaData(ctNode);
		}
		else
		{
			htmlPane.setText(nodeInfo.toString());
		}
	}

	protected void connect()
	{
		try {
			// Create a sink and connect:
			sink=new Sink();
			sink.OpenRBNBConnection(getServer(),"ChannelListRequest");
			connected = true;
			System.out.println("ChannelList: Connection made to server = "
				+ getServer() + " requesting channel list.");
		}
		catch (SAPIException se)
		{
			System.out.println("ChannelList: Cannot connect to "
				+ getServer() + "; exception = " + se);
		}
	}

	private ChannelTree getChannelTree() {
		try {
			sMap = new ChannelMap();
			sink.RequestRegistration();		
			sMap = sink.Fetch(-1,sMap);
			return ChannelTree.createFromChannelMap(sMap);
		}
		catch (SAPIException se)
		{
			System.out.println("ChannelList: get channel tree failed. Reconnect?");
			System.out.println("Exception = " + se);
			connected = false;
		}
		return null;
	}
	
	private ChannelTree getDecendentChannelTree(String pattern){
		try{
			sMap = new ChannelMap();
			sMap.Add(pattern);
			sink.RequestRegistration();		
			sMap = sink.Fetch(-1,sMap);
			return ChannelTree.createFromChannelMap(sMap);
		}
		catch (SAPIException se)
		{
			System.out.println("ChannelList: get decendent channel tree failed. Reconnect?");
			System.out.println("Exception = " + se);
			connected = false;
		}
		return null;
	}

	private void displayMetaData(ChannelTree.Node node) {
		double start = node.getStart();
		double duration = node.getDuration();
		double end = start + duration;
		
		long unixTime = (long)(start * 1000.0); // convert sec to millisec
		String startTime = DATE_FORMAT.format(new Date(unixTime));
		unixTime = (long)(end * 1000.0);
		String endTime = DATE_FORMAT.format(new Date(unixTime));

		if (((node.getType()).toString()).equals("Channel"))
			htmlPane.setText(
				"FullName = " + node.getFullName() + "\n" +
				"Time (start, duration) = " + start + ", " + duration + "\n" +
				"[Assuming \"standard\" time:" + "\n" +
				"    from " + startTime + "\n" + 
				"    to " + endTime + "]\n" +
				"Type = " + node.getType() + "\n" +
				"Mime type = " + node.getMime() + "\n" +
				"Size = " + node.getSize()
			);
		else
		htmlPane.setText(
			"FullName = " + node.getFullName() + "\n" +
			"Type = " + node.getType() + "\n"
			);
	}
	
	protected void startCheckThread()
	{
		// Use this inner class to hide the public run method
		Runnable r = new Runnable() {
			public void run() {
			  runWork();
			}
		};
		runit = true;
		timerThread = new Thread(r, "Timer");
		timerThread.start();
		System.out.println("Started checker thread.");
	}

	private void stopThread()
	{
		runit = false;
		timerThread.interrupt();
		System.out.println("Stopped checker thread.");
	}
	
	private void runWork ()
	{
		int delay = 10000; // 10 seconds
		while(runit)
		{
			if (!connected)
			{
				delay = 10000; // 10 seconds
				// try connecting
				connect();
				if (connected) delay = 1000; // one second
			}
			else if (!sameTree(tree)) 
			{
				System.out.println("Found mismatched tree");
				needsRefresh = true;
			} 
			try {Thread.sleep(delay);} catch (InterruptedException ignore) {}
		}
		timerThread = null;
	}
	
	private boolean sameTree(JTree tree)
	{
		TreeModel m = tree.getModel();
		ChannelTree ct = getChannelTree();
		if (ct == null) return false;
		return sameTreeNodes(ct.rootIterator(),(DefaultMutableTreeNode)m.getRoot());
	}
	
	private boolean sameTreeNodes(Iterator i, DefaultMutableTreeNode tree)
	{
		// its the Iterator of the ChannelTree.Node's of this level of the tree
		
		// if all the ChannelTree.Node's in the iterator are in the children of this tree
		// and all the decendent trees match, then the trees match.
		// (name equivalance is good enough)

		if (tree.isLeaf() && !i.hasNext())
		{
			// expected leaf node
			return true;
		}

		if (tree.isLeaf() && i.hasNext())
		{
			// unexpected leaf node
			return false;
		}

		if (!tree.isLeaf() && !i.hasNext())
		{
			// unexpected empty itereator
			return false;			
		}
		
		// not a leaf node...

		// make a vector of all the names at this level
		Vector nodeList = new Vector();
		int count = tree.getChildCount();
		for (int k = 0; k < count; k++)
		{
			nodeList.add(tree.getChildAt(k));
		}

		while (i.hasNext())
		{
			ChannelTree.Node n = (ChannelTree.Node)i.next();
			Enumeration e = nodeList.elements();
			DefaultMutableTreeNode found = null;
			while(e.hasMoreElements())
			{
				DefaultMutableTreeNode test = (DefaultMutableTreeNode)e.nextElement();
				Object nodeInfo = test.getUserObject();
				NodeCover nc = (NodeCover)nodeInfo;	
				ChannelTree.Node ctNode = nc.node;
				if (n.getName().equals(ctNode.getName()))
				{
					found = test;
					break;
				}
			}
			if (found == null) return false;
			nodeList.remove(found);
			if (!sameTreeNodes(n.getChildren().iterator(),found)) return false;
		}
		if (nodeList.size() > 0)
		{
			// extra nodes at this level
			return false;			
		}
		return true;
	}
	
	private class NodeCover
	{
		ChannelTree.Node node;
		
		NodeCover(ChannelTree.Node node)
		{
			this.node = node;
		}
		
		public String toString()
		{
			return node.getName();
		}
	}
	
	// copied from RBNB Base; it's ugly and should be refactored, but...
	// Terry E. Weymouth, Feb 10, 2004
	// ----
	
	protected boolean parseArgs(String[] args) throws IllegalArgumentException
	{
		try {
			CommandLine cmd = (new PosixParser()).parse(setOptions(), args);
			return setArgs(cmd);
		} catch (Exception e) {
			throw new IllegalArgumentException("Argument Exception: " + e);
		}
	}
		
	protected boolean setBaseArgs(CommandLine cmd)
	{	
		if (cmd.hasOption('h'))
		{
			printUsage();
			return false;
		}
		if (cmd.hasOption('s')) {
			String a=cmd.getOptionValue('s');
			if (a!=null) setServerName(a);
		}
		if (cmd.hasOption('p')) {
			String a=cmd.getOptionValue('p');
			if (a!=null) setServerPort(a);
		}
		return true;
	}
	
	/**
	 * @param name
	 */
	public void setServerName(String name) {
		serverName = name;
	}

	/**
	 * @param port
	 */
	public void setServerPort(String port) {
		serverPort = port;
	}

	public String getServer()
	{
		server = serverName + ":" + serverPort;
		return server;
	}

	protected void printUsage() {
		HelpFormatter f = new HelpFormatter();
		f.printHelp(this.getClass().getName(),setOptions());
		if (optionNotes != null)
		{
			System.out.println("Note: " + optionNotes);
		}
	}

	protected Options setBaseOptions(Options opt)
	{
		opt.addOption("h",false,"Print help");
		opt.addOption("s",true,"Server Hostname *" + SERVER_NAME);
		opt.addOption("p",true,"Server Port Number *" + SERVER_PORT);
		return opt;
	}
	
	protected void setNotes(String n)
	{
		optionNotes = n;
	}
	//----
}
