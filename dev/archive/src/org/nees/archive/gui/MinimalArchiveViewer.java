/*
 * MinimalArchiveViewer.java
 * Created on March 2006 (from a copy of ChannelListSwing of March 2004)
 * 
 * This work is supported in part by the George E. Brown, Jr. Network
 * for Earthquake Engineering Simulation (NEES) Program of the National
 * Science Foundation under Award Numbers CMS-0117853 and CMS-0402490.
 * 
 * Source information...
 *   $LastChangedRevision: 153 $
 *   $LastChangedDate: 2007-09-24 13:10:37 -0700 (Mon, 24 Sep 2007) $
 *   $HeadURL: file:///Users/hubbard/code/cleos-svn/cleos-rbnb-apps-dev/archive/src/org/nees/archive/gui/MinimalArchiveViewer.java $
 *   $LastChangedBy: ljmiller $
 * 
 */
package org.nees.archive.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.Timer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeSelectionModel;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.nees.archive.impl.Archive;
import org.nees.archive.impl.ArchiveUtility;
import org.nees.archive.inter.ArchiveException;
import org.nees.archive.inter.ArchiveInterface;
import org.nees.archive.inter.ArchiveSegmentInterface;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.ChannelTree;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Sink;
import com.rbnb.sapi.ChannelTree.Node;

/**
 * This application presents an Graphical User Interface to a file-based archive.
 * 
 * @author Terry E Weymouth
 *
 */
public class MinimalArchiveViewer
//    implements TreeSelectionListener
{

    private static final String SERVER_NAME = "neestpm.sdsc.edu";
	private static final String SERVER_PORT = "3333";
	private String serverName = SERVER_NAME;
	private String serverPort = SERVER_PORT;
	private String server = serverName + ":" + serverPort;
	
    private static final String DEFAULT_ARCHIVE_PATH = "ExampleArchive";
    private String archivePath = DEFAULT_ARCHIVE_PATH;
    private ArchiveInterface archive;
        
	private String optionNotes = null;

	private Sink sink = null;
	private ChannelMap sMap;
	private boolean connected = false;

    private ChannelTree.Node selectedNode;

    private ArchiveSegmentInterface selectedSegment;
    
    // graphics conponents
    private JFrame mainFrame;
    
    private JTree channelTree;
	private JEditorPane rbnbChannelMetadata;
	private boolean needsRefresh = false;
	private Timer checkRefresh;
	private Thread timerThread;
	private boolean runit = false;

    private JLabel selectedSegmentLabel, channelsTitle;
    private JLabel archivePathLabel;
    private JButton startCaptureButton, captureHelpButton, resetRbnbServer,
        selectArchiveRoot, startSendThread;
    private JEditorPane archiveSegmentMetadata;
    private JList archiveSegmentList;

    // button faces
    private static final String START_CAPTURE = "Start RBNB Capture Dialog";
    private static final String HELP_CAPTURE = "Help on Capture";
    
    public static String getChannelListInstructions()
    {
        return HtmlUtility.htmlText(HtmlUtility.italicText(
                "Start the upload of channel(s) from RBNB <br>" +
                "to the Archive by clicking on the <br>" +
                "'" +  START_CAPTURE + "'" + " button. <br>" +
                "Drag channels for download (source channes only) <br> " +
                "to channel list in the dialog. Vidio channels and audio <br>" +
                "channes can only be run 'one to a thread.' Multiple numeric <br>" +
                "channels can be combined into one archive segment <br>" +
                "and, hence, run in one thread. Each thread results<br>" +
                "in a seperate, new archive segment." 
                ));
    }
    
    public static String getVersionString()
    {
        return
            "Version information... \n" +
            " $LastChangedRevision:543 $\n" +
            " $LastChangedDate:2006-03-13 14:08:34 -0500 (Mon, 13 Mar 2006) $\n" +
            " $HeadURL:https://svn.nees.org/svn/telepresence/dataturbine-dev/archive/src/org/nees/archive/inter/ArchiveAudioStreamInterface.java $\n" +
            " $LastChangedBy:weymouth $\n";
    }
    
	public static void main(String[] args) {
        // Schedule a job for the event-dispatching thread:
        // creating and showing this application's GUI and start the applicaiton
        final String[] a = args;
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    MinimalArchiveViewer c = new MinimalArchiveViewer();
                    if (c.parseArgs(a)) {
                        c.connect();
                        c.initArchive();
                        c.createAndShowGUI();
                        c.setTimer();
                        c.startCheckThread();
                    }
                } catch (ArchiveException e) {
                    e.printStackTrace();
                }
            }
        });
    }
	
	private void initArchive() throws ArchiveException {
        archive = new Archive(archivePath);
    }

    protected Options setOptions() {
		Options opt = setBaseOptions(new Options()); // uses h, s, p
		return opt;
	}
	
	protected boolean setArgs(CommandLine cmd){

		if (!setBaseArgs(cmd)) return false;
		
		System.out.println("Starting MinimalArchiveViewer on " + getServer());
		System.out.println("  Use MinimalArchiveViewer -h to see optional parameters");
		
		return true;
	}
	
	public void setNameAndPort(String name, String port)
	{
		serverName = name;
		serverPort = port;
	}
		
    public JFrame getMainFrame() {
        return mainFrame;
    }

    protected void createAndShowGUI() {
		
		//Make sure we have nice window decorations.
		//JFrame.setDefaultLookAndFeelDecorated(true);

		//Create and set up the window.
		JFrame frame = new JFrame("MinimalArchiveViewer");
        mainFrame = frame;
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(new Dimension(120, 40));
        BoxLayout bl = new BoxLayout(frame.getContentPane(),BoxLayout.X_AXIS);
        frame.getContentPane().setLayout(bl);
        
        frame.getContentPane().add(initChannelList());
        frame.getContentPane().add(new JSeparator(JSeparator.VERTICAL));
        frame.getContentPane().add(initArchiveList());

        setButtonActions();
        
        updateButtonsAndLabels();
        
		//Display the window.
		frame.pack();
		frame.setVisible(true);
	}
	
    private JPanel initChannelList() {

        JPanel topHolder = new JPanel();
        topHolder.setLayout(new BorderLayout());

        JPanel part = new JPanel();
        part.setLayout(new FlowLayout());
        resetRbnbServer = new JButton("Select New RBNB Host");
        resetRbnbServer.setEnabled(false);
        part.add(resetRbnbServer);
        topHolder.add(part, BorderLayout.NORTH);
        
        JPanel clPanel = new JPanel();
        clPanel.setLayout(new BorderLayout());
        clPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JPanel labelPanel = new JPanel();
        channelsTitle = new JLabel("Channels for " + getServer());
        labelPanel.add(channelsTitle);
        clPanel.add(labelPanel,BorderLayout.NORTH);
        
		//Create the nodes.
		DefaultMutableTreeNode top = createNodes();

		//Create a tree that allows one selection at a time.
		channelTree = new JTree(top);
		channelTree.getSelectionModel().setSelectionMode
				(TreeSelectionModel.SINGLE_TREE_SELECTION);
        
        // set up the Drag Transfer Handler for drag and drop to dialog
        channelTree.setTransferHandler(new RBNBTreeNodeTransferHandler());
        channelTree.setDragEnabled(true);

		//Listen for when the selection changes.
		channelTree.addTreeSelectionListener(
                new TreeSelectionListener(){
                    public void valueChanged(TreeSelectionEvent e) {
                        treeSelectionValueChanged(e);                        
                    }
                }
                );

		//Create the scroll pane and add the tree to it. 
		JScrollPane treeView = new JScrollPane(channelTree);

		//Create the HTML viewing pane.
		rbnbChannelMetadata = new JEditorPane();
		rbnbChannelMetadata.setEditable(false);
		JScrollPane htmlView = new JScrollPane(rbnbChannelMetadata);

		//Add the scroll panes to a split pane.
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setTopComponent(treeView);
		splitPane.setBottomComponent(htmlView);

		Dimension minimumSize = new Dimension(100, 50);
		// htmlView.setMinimumSize(minimumSize);
		treeView.setMinimumSize(minimumSize);
		// splitPane.setDividerLocation(100); //Note: ignored in some releases
										 //of Swing. bug 4101306
        // For information, see bug #4131528
        // in the Bug Parade at the Java Developer's Connection.
        // workaround for bug 4101306:
		treeView.setPreferredSize(new Dimension(200, 100)); 

		splitPane.setPreferredSize(new Dimension(300, 300));

		//Add the split pane to this panel.
        clPanel.add(splitPane);
        
        topHolder.add(clPanel,BorderLayout.CENTER);

        JPanel buttonHolder = new JPanel();
        buttonHolder.setLayout(new BoxLayout(buttonHolder,BoxLayout.PAGE_AXIS));
        part = new JPanel();
        part.setLayout(new FlowLayout());
        startCaptureButton = new JButton(START_CAPTURE);
        startCaptureButton.setEnabled(false);
        part.add(startCaptureButton);
        buttonHolder.add(part);
        part = new JPanel();
        part.setLayout(new FlowLayout());
        captureHelpButton = new JButton(HELP_CAPTURE);
        captureHelpButton.setEnabled(true);
        part.add(captureHelpButton);
        buttonHolder.add(part);
        topHolder.add(buttonHolder,BorderLayout.SOUTH);
        
		return topHolder;
	}

    private Component initArchiveList() {

        JPanel topHolder = new JPanel();
        BoxLayout bl = new BoxLayout(topHolder,BoxLayout.PAGE_AXIS);
        topHolder.setLayout(bl);
        
        JPanel part;
        
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JPanel labelPanel = new JPanel();
        labelPanel.add(new JLabel("Archive Segment List"));
        panel.add(labelPanel,BorderLayout.NORTH);
        
        archiveSegmentList = new JList(archive.getSegmentsVector());
        JScrollPane segmentScrollPane = new JScrollPane(archiveSegmentList);
        archiveSegmentList.addListSelectionListener(
                new ListSelectionListener(){
                    public void valueChanged(ListSelectionEvent e) {
                        listSelectionValueChanged(e);
                    }
                }
        );
        
        archiveSegmentMetadata = new JEditorPane();
        archiveSegmentMetadata.setEditable(false);
        JScrollPane segmentMetadataScrollPane = new JScrollPane(archiveSegmentMetadata);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(segmentScrollPane);
        splitPane.setBottomComponent(segmentMetadataScrollPane);

        splitPane.setPreferredSize(new Dimension(300, 300));
        
        panel.add(splitPane,BorderLayout.CENTER);

        topHolder.add(panel);
        
        part = new JPanel();
        part.setLayout(new FlowLayout());
        archivePathLabel = new JLabel("Archive path: unspecified");
        part.add(archivePathLabel);
        topHolder.add(part);
        
        part = new JPanel();
        part.setLayout(new FlowLayout());
        selectArchiveRoot = new JButton("Select New Archive Root");
        selectArchiveRoot.setEnabled(false);
        part.add(selectArchiveRoot);
        topHolder.add(part);
        
        part = new JPanel();
        part.setLayout(new FlowLayout());
        JLabel l = new JLabel("Selected Segment: ");
        part.add(l);
        l = new JLabel("--- No Segment Selected ---");
        part.add(l);
        selectedSegmentLabel = l;
        topHolder.add(part);
        
        part = new JPanel();
        part.setLayout(new FlowLayout());
        startSendThread = new JButton("Start Send Of Selected Segment");
        startSendThread.setEnabled(false);
        part.add(startSendThread);
        topHolder.add(part);

        return topHolder;
    }

    private void updateButtonsAndLabels(){
        enableButtons();
        updateLabels();
    }
    
    private void enableButtons() {
        resetRbnbServer.setEnabled(true);

        selectArchiveRoot.setEnabled(true);

        startCaptureButton.setEnabled(true);
        captureHelpButton.setEnabled(true);

        startSendThread.setEnabled(segmentSelected());
    }
    
    private void updateLabels(){
        channelsTitle.setText("Channels for " + getServer());
        if (archive == null)
            archivePathLabel.setText("Archive path: unspecified");
        else
            archivePathLabel.setText("Archive path: " + archivePath);
        if (!segmentSelected())
            selectedSegmentLabel.setText("--- No Segment Selected ---");
        else
        {
            selectedSegmentLabel.setText(selectedSegment.toString());
        }
    }

    private void setButtonActions() {
        startCaptureButton.addActionListener(
                new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        startCaptureThread();
                    }
                }
        );
        captureHelpButton.addActionListener(
                new ActionListener(){
                    public void actionPerformed(ActionEvent e)
                    {
                        showCaptureHelpDialog();
                    }
                }
        );
        resetRbnbServer.addActionListener(
                new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        resetRbnbServer();
                    }
                }
        );
        selectArchiveRoot.addActionListener(
                new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        selectArchiveRoot();
                    }
                }
        );
        startSendThread.addActionListener(
                new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        startSendThread();
                    }
                }
        );
    }

	private void setTimer()
	{
		int delay = 100; //milliseconds; 1/10 second
		ActionListener taskPerformer = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (needsRefresh)
				{
					//recreate the nodes.
					DefaultMutableTreeNode top = createNodes();
					DefaultTreeModel model = new DefaultTreeModel(top);
					channelTree.setModel(model);
					channelTree.invalidate();
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
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(new RBNBChannelNodeCover(node));
		List l = node.getChildren();
		Iterator i = l.iterator();
		while (i.hasNext())
		{
			root.add(makeNodes((ChannelTree.Node)i.next()));
		}

		return root; 
	}

	private void treeSelectionValueChanged(TreeSelectionEvent e) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)
						   channelTree.getLastSelectedPathComponent();
		setSelectedNode(null);
        if (node == null) return;
        Object nodeInfo = node.getUserObject();
		if (nodeInfo instanceof RBNBChannelNodeCover)
		{
			RBNBChannelNodeCover nc = (RBNBChannelNodeCover)nodeInfo;	
			ChannelTree.Node ctNode = nc.node;
             setSelectedNode(ctNode);
			displayMetaData(ctNode);
		}
		else
		{
			rbnbChannelMetadata.setText(nodeInfo.toString());
		}
        updateButtonsAndLabels();
	}

    private void listSelectionValueChanged(ListSelectionEvent e) {
        Object sel = archiveSegmentList.getSelectedValue();
        System.out.println("Selected object class = " + sel.getClass().getName());
        if (sel instanceof ArchiveSegmentInterface)
        {
            setSelectedSegment((ArchiveSegmentInterface)sel);
        }
        else
        {
            System.out.println("List Selection: odd value = " + sel.toString());
        }
    }

    private void connect()
	{
		try {
			// Create a sink and connect:
			sink=new Sink();
			sink.OpenRBNBConnection(getServer(),"ChannelListRequest");
			connected = true;
			System.out.println("Connection made to server = "
				+ getServer() + " requesting channel list.");
		}
		catch (SAPIException se)
		{
			System.out.println("Cannot connect to "
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
	
	private void displayMetaData(ChannelTree.Node node) {
		double start = node.getStart();
		double duration = node.getDuration();
		double end = start + duration;
		
		long unixTime = (long)(start * 1000.0); // convert sec to millisec
		String startTime = ArchiveUtility.DATE_FORMAT.format(new Date(unixTime));
		unixTime = (long)(end * 1000.0);
		String endTime = ArchiveUtility.DATE_FORMAT.format(new Date(unixTime));

		if (((node.getType()).toString()).equals("Channel"))
			rbnbChannelMetadata.setText(
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
		rbnbChannelMetadata.setText(
			"FullName = " + node.getFullName() + "\n" +
			"Type = " + node.getType() + "\n"
			);
	}

    private void setSelectedNode(Node ctNode) {
        selectedNode = ctNode;
        if (selectedNode == null) return;
    }
    
    private void setSelectedSegment(ArchiveSegmentInterface seg) {
        selectedSegment = seg;
        archiveSegmentMetadata.setText(seg.getInfo());
        updateButtonsAndLabels();
    }

    private boolean segmentSelected() {
        return (selectedSegment != null);
    }

    private void resetRbnbServer() {
        // dialog to get host and port
        HostAndPortDialog d = new HostAndPortDialog(mainFrame, getHost(), getPort());
        if (d.isCancled()) return;
        String host = d.getHost();
        String port = d.getPort();        
        if (getServer().equals(host + ":" + port)) return;

        // replace connection
        connected=false;
        DefaultMutableTreeNode top = createNodes();
        DefaultTreeModel model = new DefaultTreeModel(top);
        channelTree.setModel(model);
        channelTree.invalidate();
        setServerName(host);
        setServerPort(port);
        timerThread.interrupt();
        updateButtonsAndLabels();
    }

    private void selectArchiveRoot() {
        // get archive dir
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        int returnVal = chooser.showOpenDialog(mainFrame);
        if (returnVal == JFileChooser.CANCEL_OPTION) // user cancled
        {
            System.out.println("User Cancled the selection.");
            return;
        }
        File f = chooser.getSelectedFile();

        // validate
        if (!f.exists())
        {
            System.out.println("Archvie file does not exist. File = " + f.getName());
            return;
        }
        if (!f.canWrite())
        {
            System.out.println("Archvie file is not writable. File = " + f.getName());
            return;
        }
        if (!f.canRead())
        {
            System.out.println("Archvie file is not readable. File = " + f.getName());
            return;
        }

        // set
        ArchiveInterface testArchive = null;
        try {
            testArchive = new Archive(f.getAbsolutePath());
        } catch (ArchiveException e) {
            System.out.println("Exception = " + e);
            System.out.println("Malformed archive. File = " + f.getName());
            return;
        }
        archivePath = f.getAbsolutePath();
        archive = testArchive;
        archiveSegmentList.setListData(archive.getSegmentsVector());
        updateLabels();
    }

    private void startCaptureThread() {
        // this dialog runs seperatly, on it's own
        // drag and drop channels to it and start an independent capture thread
        new CaptureThreadDialog(this, archive);
    }
    
    public void setChannelsDragEnabled(boolean b) {
        this.channelTree.setDragEnabled(true);
    }

    private void startSendThread() {
        System.out.println("--------------- startSendThread ----------");
        System.out.println("                 unimplemented ");
//        SendThreadDialog d = new SendThreadDialog(mainFrame);
//        if (d.isCancled())
//        {
//            System.out.println("User cancled startCaptureButton action.");
//            return;
//        }
//        System.out.println("Starting startCaptureButton action.");        
    }

	private void startCheckThread()
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

//  **** Unused ****
//  private void stopThread()
//	{
//		runit = false;
//		timerThread.interrupt();
//		System.out.println("Stopped checker thread.");
//	}
	
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
			else if (!sameTree(channelTree)) 
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
        if (m == null) return false;
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
				RBNBChannelNodeCover nc = (RBNBChannelNodeCover)nodeInfo;	
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

    private void showCaptureHelpDialog()
    {
        JOptionPane.showInternalMessageDialog(mainFrame.getContentPane(), 
                getChannelListInstructions(),
                "Capture Help", JOptionPane.INFORMATION_MESSAGE);
    }
    
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

    public String getHost() {return serverName; }
    public String getPort() {return serverPort; }

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

}
