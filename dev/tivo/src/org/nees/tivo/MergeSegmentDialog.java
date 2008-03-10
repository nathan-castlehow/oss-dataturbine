/*
 * MergeSegmentDialog.java
 * Created June, 2005
 * 
 * COPYRIGHT © 2005, THE REGENTS OF THE UNIVERSITY OF MICHIGAN,
 * ALL RIGHTS RESERVED; see the file COPYRIGHT.txt in this folder for details
 * 
 * This work is supported in part by the George E. Brown, Jr. Network
 * for Earthquake Engineering Simulation (NEES) Program of the National
 * Science Foundation under Award Numbers CMS-0117853 and CMS-0402490.
 * 
 * CVS information...
 *   $Revision: 153 $
 *   $Date: 2007-09-24 13:10:37 -0700 (Mon, 24 Sep 2007) $
 *   $RCSfile: MergeSegmentDialog.java,v $ 
 * 
 */
package org.nees.tivo;

import java.awt.BorderLayout;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class MergeSegmentDialog
    extends JDialog
    implements KeyEventDispatcher, ActionListener {

    private ArchiveInterface archive;

    // button faces for GetSegmentDialog
    private static final String SELECT_SEGMENT = "Start Merge of Segments";
    private static final String CANCEL = "Cancel";

    private Viewer itsViewer;

    private JButton selectButton;
    private JButton cancelButton;
    private boolean canceled = true;

    private SegmentListPanel seg1Panel;
    private SegmentListPanel seg2Panel;

    private JTextField segmentName;
    
    public MergeSegmentDialog(
        ArchiveInterface archive,
        JFrame theViewer,
        ArchiveSegmentInterface current) {
        super(theViewer, true);
        this.archive = archive;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowActivated(WindowEvent e) {
                bindKeys();
            }
            public void windowDeactivated(WindowEvent e) {
                unbindKeys();
            }
        });

        ArchiveSegmentInterface[] segArray = archive.getSegmentsArray();
        if (segArray.length == 0) {
            dispose();
            return;
        }
        Arrays.sort(segArray);

        setTitle("Select segments for Merge");

        JPanel top = new JPanel();
        top.setLayout(new BorderLayout());

        JPanel labelPanel = new JPanel();
        JLabel says = new JLabel("Name of merged segment");
        segmentName = new JTextField(15);
        segmentName.setText(archive.nextDefaultSegmentName());
        labelPanel.add(says);
        labelPanel.add(segmentName);
        top.add(labelPanel,BorderLayout.NORTH);
       
        seg1Panel = new SegmentListPanel(
            "First Segment...", current, segArray);
        top.add(seg1Panel,BorderLayout.WEST);

        seg2Panel = new SegmentListPanel(
             "Second Segment...", current, segArray);
        top.add(seg2Panel,BorderLayout.EAST);

        JPanel buttonPanel = new JPanel();
        JButton button;

        buttonPanel.add(button = new JButton(SELECT_SEGMENT));
        button.addActionListener(this);
        selectButton = button;

        buttonPanel.add(button = new JButton(CANCEL));
        button.addActionListener(this);
        cancelButton = button;

        top.add(buttonPanel,BorderLayout.SOUTH);

        getContentPane().add(top, BorderLayout.CENTER);

        updateButtons();

        segmentName.addFocusListener(new FocusListener(){
            public void focusGained(FocusEvent ignore) {}
            public void focusLost(FocusEvent arg0) {
                validateSegmentName();
                updateButtons();
            }
        });

        seg1Panel.makeSelectionVisible();
        seg2Panel.makeSelectionVisible();

        pack();
        setVisible(true);
    }

    private void validateSegmentName() {
        String segName = segmentName.getText();
        segmentName.setText(segName.trim());
        if (segName.length() == 0) {
            segmentName.setText(archive.nextDefaultSegmentName());
            return;
        } else if (archive.getSegmentByName(segName) != null) {
            segmentName.setText(archive.nextDefaultSegmentName());
            return;
        }
    }

    private void updateButtons() {
        cancelButton.setEnabled(true);
        selectButton.setEnabled(false);
        if (seg1Panel.getSelectedSegment() == null) return;
        if (seg2Panel.getSelectedSegment() == null) return;
        if (segmentName.getText().length() == 0) return;
        selectButton.setEnabled(true);
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

        System.out.println("action = " + ev.getActionCommand());
        
        if (!(ev.getSource() instanceof JButton))
            return;
        String arg = ev.getActionCommand();

        if (arg.equals(SELECT_SEGMENT)) {
            canceled = false;
            dispose();
        } else if (arg.equals(CANCEL)) {
            canceled = true;
            dispose();
        }
    }

    public boolean isCancled() {
        return canceled;
    }

    public ArchiveSegmentInterface getSeg1() {
        if (isCancled()) return null;
        return seg1Panel.getSelectedSegment();
    }
    public ArchiveSegmentInterface getSeg2() {
        if (isCancled()) return null;
        return seg2Panel.getSelectedSegment();
    }

    public String getMergeSegmentName() {
        return segmentName.getText();
    }

    public static void main (String[] args){
        try {
            JFrame frame = new JFrame("Test");
            Archive a = new Archive();
            MergeSegmentDialog d = new MergeSegmentDialog(a,frame,null);
            d.dispose();
            if (d.isCancled())
                System.out.println("It was cancled");
            else
            {
                System.out.println("Parameters...");
                System.out.println("  Merge segment name = " 
                    + d.getMergeSegmentName());                System.out.println("  Segment 1 = " + d.getSeg1());
                System.out.println("  Segment 2 = " + d.getSeg2());
            }
        } catch (ArchiveException e) {
            e.printStackTrace();
        }    
        System.exit(0);
    } // main
    
    private class SegmentListPanel
        extends JPanel
        implements ListSelectionListener
    {
        private DefaultListModel segmentList = new DefaultListModel();
        private JList selectionView;
        private ArchiveSegmentInterface selectedSegment = null;
        private JLabel selectedSegmentLabel;
        private int selectedIndex;
        
        SegmentListPanel(String title, ArchiveSegmentInterface current,
            ArchiveSegmentInterface[] segArray)
        {
            for (int i = 0; i < segArray.length; i++) {
                ArchiveSegmentInterface seg = segArray[i];
                if ((current != null) && seg.equals(current))
                    selectedIndex = i;
                segmentList.addElement(seg);
            }

            BoxLayout b = new BoxLayout(this, BoxLayout.Y_AXIS);
            setLayout(b);

            JLabel label = new JLabel(title + ": ");
            selectionView = new JList(segmentList);
            selectionView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            selectionView.setVisibleRowCount(5);
            if (selectedIndex > -1) {
                selectionView.setSelectedIndex(selectedIndex);
                ArchiveSegmentInterface segment =
                    (ArchiveSegmentInterface)selectionView.getSelectedValue();
                selectedSegment = segment;
            }
            selectionView.addListSelectionListener(this);
            JScrollPane listScrollPane = new JScrollPane(selectionView);

            JPanel selectionHolder = new JPanel();
            selectionHolder.setLayout(new BorderLayout());
            selectionHolder.add(label, BorderLayout.NORTH);
            selectionHolder.add(listScrollPane, BorderLayout.CENTER);
            add(selectionHolder);

            selectedSegmentLabel = new JLabel("");
            setSelectedSegmentLabel();
            JPanel labelHolder = new JPanel();
            labelHolder.add(selectedSegmentLabel);
            add(labelHolder);
        }  // ChannelListPanel -- constructor

        public ArchiveSegmentInterface getSelectedSegment() {
            return selectedSegment;
        }
        
        private void makeSelectionVisible() {
            if (selectedIndex > -1)
                selectionView.ensureIndexIsVisible(selectedIndex);
        }

        public void valueChanged(ListSelectionEvent ev) {
            if (ev.getValueIsAdjusting())
                return;
            Object x = selectionView.getSelectedValue();
            ArchiveSegmentInterface segment =
                (ArchiveSegmentInterface)selectionView.getSelectedValue();
            selectedSegment = segment;
            setSelectedSegmentLabel();
            updateButtons();
        }
        
        private void setSelectedSegmentLabel() {
            String text = "";
            if (selectedSegment == null)
                text = "There is no selected segment.";
            else{
                text = "Selected segment = " + selectedSegment.getName();
                String channel = selectedSegment.getProperty(
                    ArchiveSegmentInterface.PROPERTY_KEY_Channel);
                String dateCreated = selectedSegment.getProperty(
                    ArchiveSegmentInterface.PROPERTY_KEY_Date_Created);
                String server = selectedSegment.getProperty(
                    ArchiveSegmentInterface.PROPERTY_KEY_Server);
                if ((channel != null) && (channel.length() > 0))
                    text += "<br>Channel = " + channel;
                if ((dateCreated != null) && (dateCreated.length() > 0))
                    text += "<br>Data Created = " + dateCreated;
                if ((server != null) && (server.length() > 0))
                    text += "<br>Server = " + server;
            }
            text = "<html>" + text + "</html>";
            selectedSegmentLabel.setText(text);
            pack();
        } // setSelectedSegmentLabel

    } // ChannelListPanel
} // MergeSegmentDialog