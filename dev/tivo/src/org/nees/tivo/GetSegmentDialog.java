/*
 * GetSegmentDialog.java
 * Created May, 2005
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
 *   $RCSfile: GetSegmentDialog.java,v $ 
 * 
 */
package org.nees.tivo;

import java.awt.BorderLayout;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class GetSegmentDialog
    extends JDialog
    implements KeyEventDispatcher, ActionListener, ListSelectionListener {
    private ArchiveInterface itsArchive;

    // button faces for GetSegmentDialog
    private static final String SELECT_SEGMENT = "Select Segment";
    private static final String CANCEL = "Cancel";

    private Viewer itsViewer;
    private DefaultListModel segmentList = new DefaultListModel();
    private ArchiveSegmentInterface selectedSegment = null;

    private JButton selectButton;
    private JButton cancelButton;

    private JList selectionView;
    private JLabel selectedSegmentLabel;

    private boolean canceled = true;

    public GetSegmentDialog(
        String message,
        ArchiveInterface archive,
        JFrame theViewer,
        ArchiveSegmentInterface prev) {
        super(theViewer, true);
        itsArchive = archive;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowActivated(WindowEvent e) {
                bindKeys();
            }
            public void windowDeactivated(WindowEvent e) {
                unbindKeys();
            }
        });
        int selectedIndex = -1;
        ArchiveSegmentInterface[] segArray = itsArchive.getSegmentsArray();
        if (segArray.length == 0) {
            dispose();
            return;
        }
        Arrays.sort(segArray);
        for (int i = 0; i < segArray.length; i++) {
            ArchiveSegmentInterface seg = segArray[i];
            if ((prev != null) && seg.equals(prev))
                selectedIndex = i;
            addSegment(seg);
        }

        setTitle(message);

        JPanel top = new JPanel();
        BoxLayout b = new BoxLayout(top, BoxLayout.Y_AXIS);
        top.setLayout(b);

        JLabel label = new JLabel(message + ": ");
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
        top.add(selectionHolder);

        selectedSegmentLabel = new JLabel("");
        setSelectedSegmentLabel();
        JPanel labelHolder = new JPanel();
        labelHolder.add(selectedSegmentLabel);
        top.add(labelHolder);

        JPanel buttonPanel = new JPanel();
        JButton button;

        buttonPanel.add(button = new JButton(SELECT_SEGMENT));
        button.addActionListener(this);
        selectButton = button;

        buttonPanel.add(button = new JButton(CANCEL));
        button.addActionListener(this);
        cancelButton = button;

        top.add(buttonPanel);

        getContentPane().add(top, BorderLayout.CENTER);

        updateButtons();

        if (selectedIndex > -1)
            selectionView.ensureIndexIsVisible(selectedIndex);

        pack();
        setVisible(true);
    }

    private void updateButtons() {
        cancelButton.setEnabled(true);
        selectButton.setEnabled(selectedSegment != null);
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
    }

    private void addSegment(ArchiveSegmentInterface segment) {
        segmentList.addElement(segment);
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

    public ArchiveSegmentInterface getSelectedSegment() {
        if (!isCancled())
            return selectedSegment;
        return null;
    }

    public void valueChanged(ListSelectionEvent ev) {
        if (ev.getValueIsAdjusting())
            return;
        ArchiveSegmentInterface segment =
            (ArchiveSegmentInterface)selectionView.getSelectedValue();
        selectedSegment = segment;
        setSelectedSegmentLabel();
        updateButtons();
    }


    public static void main (String[] args){
        try {
            JFrame frame = new JFrame("Test");
            Archive a = new Archive();
            GetSegmentDialog d = new GetSegmentDialog("Test",a,frame,null);
            d.dispose();
        } catch (ArchiveException e) {
            e.printStackTrace();
        }    
        System.exit(0);
    } // main
    
} // GetSegmentDialog