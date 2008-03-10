/*
 * CaptureThreadDialog.java
 * Created on March 2006
 * 
 * This work is supported in part by the George E. Brown, Jr. Network
 * for Earthquake Engineering Simulation (NEES) Program of the National
 * Science Foundation under Award Numbers CMS-0117853 and CMS-0402490.
 * 
 * Source information...
 *   $LastChangedRevision: 153 $
 *   $LastChangedDate: 2007-09-24 13:10:37 -0700 (Mon, 24 Sep 2007) $
 *   $HeadURL: file:///Users/hubbard/code/cleos-svn/cleos-rbnb-apps-dev/archive/src/org/nees/archive/gui/CaptureThreadDialog.java $
 *   $LastChangedBy: ljmiller $
 * 
 */

package org.nees.archive.gui;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.nees.archive.impl.Archive;
import org.nees.archive.impl.ArchiveUtility;
import org.nees.archive.inter.ArchiveInterface;
import org.nees.archive.inter.ArchiveItemInterface.TYPE;

import com.rbnb.sapi.ChannelTree.Node;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

/**
 * This dialog support MinimalArchiveViewer, a Graphical User Interface to a 
 * file-based archive, with a dialog that enables the user to start an thread
 * to capture one or more RBNB channels to the archive.
 * 
 * @author Terry E Weymouth
 *
 */
public class CaptureThreadDialog
extends JDialog 
implements KeyEventDispatcher, ActionListener
{
    private Vector<RBNBChannelNodeCover> listData = new Vector<RBNBChannelNodeCover>();
    
    // button faces
    private static final String SET = "Start Capture Thread";
    private static final String CANCEL = "Cancel";

    private JButton selectButton;
    private JButton cancelButton;
    
    private boolean canceled = false;
    
    private CaptureThreadListModel listModel;
    private JList list;
    private JEditorPane archiveSegmentMetadata;

    private JTextArea startTimeText, endTimeText, segNameText;

    private ArchiveInterface itsArchive;
    private MinimalArchiveViewer itsViewer;
    
    public CaptureThreadDialog(MinimalArchiveViewer viewer, ArchiveInterface archive)
    {
        super(viewer.getMainFrame(),"Start Capture Thread",false);

        itsViewer = viewer;
        itsArchive = archive;
        viewer.setChannelsDragEnabled(true);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
     
        addWindowListener(new WindowAdapter() {
            public void windowActivated(WindowEvent e) {
                bindKeys();
            }
            public void windowDeactivated(WindowEvent e) {
                unbindKeys();
            }
        });

        JPanel top = new JPanel();
        top.setLayout(new BorderLayout());
        
        JPanel holder = new JPanel();
        holder.add(initCaptureNodeList());
        top.add(holder, BorderLayout.WEST);
        
        holder = new JPanel();
        holder.add(initParameterPannel());
        top.add(holder, BorderLayout.EAST);
        
        JPanel buttonPanel = new JPanel();
        JButton button;

        buttonPanel.add(button = new JButton(SET));
        button.addActionListener(this);
        selectButton = button;

        buttonPanel.add(button = new JButton(CANCEL));
        button.addActionListener(this);
        cancelButton = button;

        top.add(buttonPanel,BorderLayout.SOUTH);

        getContentPane().add(top, BorderLayout.CENTER);

        updateButtons();
        pack();
        setVisible(true);

    }

    private JPanel initParameterPannel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JPanel part;
        JLabel l;
        JTextArea tx;
        
        part = new JPanel();
        l = new JLabel("Server Host: " + itsViewer.getHost());
        part.add(l);
        panel.add(part);
        
        part = new JPanel();
        l = new JLabel("Server Port: " + itsViewer.getPort());
        part.add(l);
        panel.add(part);

        part = new JPanel();
        l = new JLabel("Start Time");
        tx = new JTextArea();
        tx.setColumns(15);
        part.add(l);
        part.add(tx);
        panel.add(part);
        startTimeText = tx;
        
        part = new JPanel();
        l = new JLabel("End Time");
        tx = new JTextArea();
        tx.setColumns(15);
        part.add(l);
        part.add(tx);
        panel.add(part);
        endTimeText = tx;
        
        part = new JPanel();
        l = new JLabel("Segment Name");
        tx = new JTextArea();
        tx.setColumns(15);
        part.add(l);
        part.add(tx);
        panel.add(part);
        segNameText = tx;
        
        segNameText.setText(itsArchive.nextDefaultSegmentName());
        
        return panel;
    }

    private JPanel initCaptureNodeList() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JPanel labelPanel = new JPanel();
        labelPanel.add(new JLabel("List of Selected Channels"));
        panel.add(labelPanel,BorderLayout.NORTH);
        
        listModel = new CaptureThreadListModel(listData);
        list = new JList(listModel);
        JScrollPane segmentScrollPane = new JScrollPane(list);
        list.addListSelectionListener(
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
        
        list.setTransferHandler(new RBNBTreeNodeTransferHandler());

        return panel;
    }

    private void updateButtons() {
        cancelButton.setEnabled(true);
        selectButton.setEnabled(false);

        // if no list, bail out early
        if (list.getModel().getSize() == 0) return;

        // if only one element any known type is ok
        if (list.getModel().getSize() == 1)
        {
            TYPE type = ((RBNBChannelNodeCover)list.getModel().getElementAt(0)).getDataType();
            selectButton.setEnabled(!type.equals(TYPE.unknown));
            return;
        }
        
        // otherwise all the nodes must be numeric
        boolean ok = true;
        Iterator i = ((CaptureThreadListModel)list.getModel()).iterator();
        while (ok && (i.hasNext()))
        {
            RBNBChannelNodeCover cover = (RBNBChannelNodeCover)i.next();
            if (!cover.getDataType().equals(TYPE.multiChannelNumeric)) ok = false;
        }
        if (ok) 
            selectButton.setEnabled(true);
    }

    private void listSelectionValueChanged(ListSelectionEvent e) {
        Object sel = list.getSelectedValue();
        if (!(sel instanceof RBNBChannelNodeCover)) return;
        RBNBChannelNodeCover cover = (RBNBChannelNodeCover)sel;
        Node selectedNode = cover.node;
        if (selectedNode == null) return;

        double start = selectedNode.getStart();
        double dur = selectedNode.getDuration();
        double end = start + dur;

        String startTime = pptime(start);
        String endTime = pptime(end);
        
        double minStart = getMinStartTime();
        double maxEnd = getMaxEndTime();
        
        String minStartString = pptime(minStart);
        String maxEndString = pptime(maxEnd);

        startTimeText.setText(minStartString);
        endTimeText.setText(maxEndString);
        
        DecimalFormat f = new DecimalFormat("#0.00");
        String duration = f.format(dur);

        String mimeType = selectedNode.getMime();
        TYPE type = cover.getDataType();

        String inferedType = type.toString();
        if (type.equals(TYPE.multiChannelNumeric))
            inferedType = "numeric";

        archiveSegmentMetadata.setText(
                "Node: " + selectedNode.getFullName() + "\n" +
                "  Start time = " + startTime + "\n" +
                "  End time   = " + endTime + "\n" +
                "  Duration = " + duration + "\n" +
                "  Mine Type = " + mimeType + "\n" + 
                "  Infered Type = " + inferedType
        );
        
        updateButtons();
    }

    private double getMaxEndTime() {
        double maxTime = 0;
        Iterator i = listModel.iterator();
        if (i.hasNext())
        {
            RBNBChannelNodeCover cover = (RBNBChannelNodeCover)i.next();
            Node node = cover.node;
            maxTime = node.getStart() + node.getDuration();
            while (i.hasNext())
            {
                cover = (RBNBChannelNodeCover)i.next();
                node = cover.node;
                double time = node.getStart() + node.getDuration();
                if (time > maxTime) maxTime = time;
            }
        }
        return maxTime;
    }

    private double getMinStartTime() {
        double minTime = 0;
        Iterator i = listModel.iterator();
        if (i.hasNext())
        {
            RBNBChannelNodeCover cover = (RBNBChannelNodeCover)i.next();
            Node node = cover.node;
            minTime = node.getStart();
            while (i.hasNext())
            {
                cover = (RBNBChannelNodeCover)i.next();
                node = cover.node;
                double time = node.getStart();
                if (time < minTime) minTime = time;
            }
        }
        return minTime;
    }

    private void bindKeys() {
        KeyboardFocusManager focusManager =
            KeyboardFocusManager.getCurrentKeyboardFocusManager();
        focusManager.addKeyEventDispatcher(this);
    }

    private void unbindKeys() {
        KeyboardFocusManager focusManager =
            KeyboardFocusManager.getCurrentKeyboardFocusManager();
        focusManager.removeKeyEventDispatcher(this);
    }

    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        int keyCode = keyEvent.getKeyCode();

        if (keyCode == KeyEvent.VK_ESCAPE) {
            dispose();
            return true;
        } else {
            return false;
        }
    }

    public void actionPerformed(ActionEvent ev) {
        if (!(ev.getSource() instanceof JButton))
            return;
        String arg = ev.getActionCommand();

        if (arg.equals(SET)) {
            canceled = false;
            startActionThread();
            dispose();
        } else if (arg.equals(CANCEL)) {
            canceled = true;
            dispose();
        }
    }
    
    public boolean isCancled() {
        return canceled;
    }

    private void startActionThread() {
        ParameterHolder p = fillParamters(new ParameterHolder());
        if (validateParameters(p))
        {
            System.out.println("Process for segment = " + p.segmentName);
            System.out.println("  segment path = " + p.fullPath.getAbsolutePath());
            System.out.println("  start time = " + pptime(p.startTime));
            System.out.println("  end time = " + pptime(p.endTime));
            System.out.println("  data type = " + p.type.toString());
            System.out.println("  server = " + p.serverName + ":" + p.serverPort);
            System.out.println("  sink name = " + p.sinkName);
            if (p.channelPathArray.length > 1)
                System.out.println("  channel(s)= ");
            else System.out.print("  channel = ");
            for (int i = 0; i < p.channelPathArray.length; i ++)
                System.out.println("   " + p.channelPathArray[i]);
            // TODO Auto-generated method stub

            CaptureProcessWatcher w = new CaptureProcessWatcher(itsViewer);
            w.createCaptureThreadForType(p.type,p.serverName,p.serverPort,
                    p.sinkName,p.channelPathArray,p.startTime,p.endTime,
                    p.fullPath);
            w.makeVisible();
            w.start();
            dispose();
        }
        else
        {
            dispose();
            JOptionPane.showInternalMessageDialog(itsViewer.getMainFrame().getContentPane(), 
                "Unable to start capture thread.",
                "Process Error", JOptionPane.INFORMATION_MESSAGE);
        }

    }
    
    private String pptime(double time) {
        long unixTime = (long)(time * 1000.0);
        return ArchiveUtility.DATE_FORMAT.format(new Date(unixTime));
    }

    private ParameterHolder fillParamters(ParameterHolder p) {
        p.serverName = itsViewer.getHost();
        p.serverPort = itsViewer.getPort();
        p.baseArchiveDir = ((Archive)itsArchive).getBaseDir();
        p.segmentName = segNameText.getText();
        p.channelPathArray = new String[listModel.getSize()];
        for (int i = 0; i < listModel.getSize(); i ++)
            p.channelPathArray[i] = 
                ((RBNBChannelNodeCover)listModel.getElementAt(i)).node.getFullName();
        if (listModel.getSize() > 0)
            p.type = ((RBNBChannelNodeCover)listModel.getElementAt(0)).getDataType();
        else p.type = TYPE.unknown;
        
        p.startTime = parseTime(startTimeText.getText());
        p.endTime = parseTime(endTimeText.getText());
        
        return p;
    }

    private double parseTime(String text) {
        double time = 0.0;
        try
        {
            Date date = ArchiveUtility.DATE_FORMAT.parse(text);
            long lt = date.getTime();
            time = ((double)lt)/1000.0; // convert to seconds
        } catch (Throwable ignore){}
        return time;
    }

    private boolean validateParameters(ParameterHolder p) {
        if (p == null) return false;
        if (!p.checkRequired()) return false;
        if (p.startTime == 0.0) return false;
        if (p.endTime == 0.0) return false;
        if (p.startTime > p.endTime) return false;
        if (p.type.equals(TYPE.unknown)) return false;
        if ((p.channelPathArray.length > 1)
                && (!p.type.equals(TYPE.multiChannelNumeric))) return false;
        
        //TODO: handle other types
        if (!p.type.equals(TYPE.multiChannelNumeric)) return false;
        
        return true;
    }

    class ParameterHolder
    {
        TYPE type;
        String serverName = "";
        String serverPort = "";
        String sinkName = "ArchiveCapture";
        String[] channelPathArray;
        File baseArchiveDir;
        File segmentPath;
        File fullPath;
        String segmentName;
        double startTime,endTime;
    
        File getFullArchivePath()
        {
            if (fullPath != null) return fullPath;
            File segmentDir = getSegmentDir();
            if (segmentDir == null) return null;
            if (type == null) return null;
            if (type.equals(TYPE.unknown)) return null;
            File full = new File(segmentDir,type.name());
            fullPath = full;
            return full;
        }
    
        public boolean checkRequired() {
            if (type == null) return false;
            if (serverName.length() == 0) return false;
            if (serverPort.length() == 0) return false;
            if (channelPathArray == null) return false;
            if (channelPathArray.length == 0) return false;
            if (getFullArchivePath() == null) return false;
            return true;
        }

        File getSegmentDir() {
            if (segmentPath != null) return segmentPath;
            if (baseArchiveDir == null) return null;
            if (!baseArchiveDir.exists()) return null;
            if (!baseArchiveDir.isDirectory()) return null;
            if (!baseArchiveDir.canRead()) return null;
            if (!baseArchiveDir.canWrite()) return null;
            File segFile = new File(baseArchiveDir,segmentName);
            segmentPath = segFile;
            return segFile;
        }
        
        long getLongStartTime()  // in milliseconds
        {
            if (startTime == 0) return -1;
            return (long)(startTime * 1000.0);
        }

        long getLongEndTime()  // in milliseconds
        {
            if (endTime == 0) return -1;
            return (long)(endTime * 1000.0);
        }
    }
}
