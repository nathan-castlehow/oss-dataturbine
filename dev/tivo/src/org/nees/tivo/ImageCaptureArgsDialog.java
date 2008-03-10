/*
 * ImageCaptureArgsDialog.java
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
 *   $RCSfile: ImageCaptureArgsDialog.java,v $ 
 * 
 */
package org.nees.tivo;

//TODO: overhall startTime = 0.0 and endTime = 0.0 and replace with flags,
// only deliver 0.0 on output!

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.nees.rbnb.ArchiveUtility;
import org.nees.rbnb.ChannelUtility;

import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Sink;

public class ImageCaptureArgsDialog
    extends JDialog
    implements KeyEventDispatcher, ActionListener {
    ArchiveInterface archive;
    
    static ViewerUtilities v = new ViewerUtilities();

    private static final String NOT_OK = v.htmlRed("Set This");
    private static final String OK = "OK";
    private static final String WINDOW_TITLE = "Parameter Values for Capture";
    private static final String DEFAULT_HOST = "neestpm.sdsc.edu";
//    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "3333";
    private static final String DEFAULT_SINK_NAME = "_CaptureSink";
    private Frame root;

    private static SimpleDateFormat TIMEFORMAT =
        ArchiveUtility.getCommandFormat();

    private JTextField segmentName = new JTextField(15);
    private JTextField host = new JTextField(15);
    private JTextField port = new JTextField(4);
    private JTextField sourcePath = new JTextField(15);
    private JTextField startTimeField = new JTextField(20);
    private JTextField endTimeField = new JTextField(20);
    private JTextField durationField = new JTextField(15);

    private double startTime = 0.0;
    private double endTime = 0.0;
    private double duration = 0.0;
    private boolean useDuration = false;
    private boolean dirtyTime = false;

    private int channelSelectIndex = -1;
    private DefaultComboBoxModel defaultModel;
    private JComboBox comboBox = new JComboBox();
    private String previousServer = null;
    private String previousChannel = null;
    private boolean segmentOK = false;
    private boolean serverOK = false;
    private boolean sourceOK = false;

    // buttons
    private static final String OK_ACTION = "Start Capture";
    private static final String CANCEL_ACTION = "Cancel";
    private static final String RESET_START_ACTION = "Reset start time from channel";
    private static final String RESET_END_ACTION = "Reset end time from channel";
    private static final String ADJUST_TIME_ACTION = "Adjust time";
    private static final String SET_SERVER_ACTION = "Server";
    
    // radio buttons
    private static final String START_NOW_ACTION = "StartNow";
    private static final String START_ABS_ACTION = "StartAbs";
    private static final String END_DUR_ACTION = "EndDur";
    private static final String END_FOR_ACTION = "EndFor";
    private static final String END_ABS_ACTION = "EndAbs";
    
    // comboBox action
    private static final String NEW_CHANNEL_ACTION = "New Channel";

    private JButton setServerButton;
    private JButton resetStartButton;
    private JButton resetEndButton;
    private JButton adjustTimeButton;
    private JButton okButton;
    private JButton cancelButton;
    
    private JRadioButton startNowRB, startAbsRB,
        endForRB, endDurRB, endAbsRB;

    // status labels	
    private JLabel generalStatus = new JLabel();
    private String errorMessage = null;

    private boolean cancled = false;

    private ImageCaptureArgsDialog() {} // for testing

    public ImageCaptureArgsDialog(Frame root, ArchiveInterface archive) {

        super(root, true);
        this.root = root;
        this.archive = archive;

        setTitle(WINDOW_TITLE);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowActivated(WindowEvent e) {
                bindKeys();
            }
            public void windowDeactivated(WindowEvent e) {
                unbindKeys();
            }
        });

        // set up "empty" list for comboBox
        Vector emptyListVoctor = new Vector();
        emptyListVoctor.add(new String("No Source Available"));
        defaultModel = new DefaultComboBoxModel(emptyListVoctor);
        
        // create start and end radio button groups;
        startNowRB = new JRadioButton(START_NOW_ACTION);
        startAbsRB = new JRadioButton(START_ABS_ACTION);
        endDurRB = new JRadioButton(END_DUR_ACTION);
        endForRB = new JRadioButton(END_FOR_ACTION);
        endAbsRB = new JRadioButton(END_ABS_ACTION);

        ButtonGroup startGroup = new ButtonGroup();
        startGroup.add(startNowRB);
        startGroup.add(startAbsRB);
        startNowRB.setSelected(true);
        
        ButtonGroup endGroup = new ButtonGroup();
        endGroup.add(endDurRB);
        endGroup.add(endForRB);
        endGroup.add(endAbsRB);
        endForRB.setSelected(true);

        // set up buttons, sets commands, adds ActionListener

        setServerButton = setupButton(SET_SERVER_ACTION,this);
        resetStartButton = setupButton(RESET_START_ACTION, this);
        resetEndButton = setupButton(RESET_END_ACTION, this);
        adjustTimeButton = setupButton(ADJUST_TIME_ACTION,this);
        okButton = setupButton(OK_ACTION, this);
        cancelButton = setupButton(CANCEL_ACTION, this);

        // set up other ActionListeners
        comboBox.setActionCommand(NEW_CHANNEL_ACTION);
        comboBox.addActionListener(this);

        startNowRB.setText("Start with time as 'now'");
        startAbsRB.setText("Start with absolute time");
        endDurRB.setText("End time set by duration (seconds)");
        endAbsRB.setText("End time from absolute time");
        endForRB.setText("End time is 'forever' (or until haulted)");
        
        startNowRB.setActionCommand(START_NOW_ACTION);
        startAbsRB.setActionCommand(START_ABS_ACTION);
        endDurRB.setActionCommand(END_DUR_ACTION);
        endAbsRB.setActionCommand(END_ABS_ACTION);
        endForRB.setActionCommand(END_FOR_ACTION);

        startNowRB.addActionListener(this);
        startAbsRB.addActionListener(this);
        endDurRB.addActionListener(this);
        endForRB.addActionListener(this);
        endAbsRB.addActionListener(this);
        
        // graphics layout
        
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        JPanel row, holder, subrow;

        // row 1 -- segment name, status, button
        row = new JPanel();
        row.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));
        row.add(new JLabel("Name of new Segment:"));
        row.add(segmentName);
        top.add(row);

        // row 3 -- host and port
        row = new JPanel();
        row.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 4));
        row.add(new JLabel("RBNB Host"));
        row.add(host);
        row.add(new JLabel("Port"));
        row.add(port);
        row.add(setServerButton);
        top.add(row);

        // row 4
        row = new JPanel();
        row.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 4));
        row.add(new JLabel("Capture from RBNB Source: "));
        row.add(comboBox);
        top.add(row);
                
        // row 5
        row = new JPanel();
        row.setLayout(new BoxLayout(row,BoxLayout.Y_AXIS));
        
        holder = new JPanel();
        holder.setLayout(new BoxLayout(holder,BoxLayout.Y_AXIS));
        subrow = new JPanel();
        subrow.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 2));
        subrow.add(new JLabel("StartTime"));
        subrow.add(Box.createHorizontalGlue());
        subrow.add(resetStartButton);
        subrow.add(adjustTimeButton);
        holder.add(subrow);

        subrow = new JPanel();
        subrow.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 2));
        subrow.add(startNowRB);
        holder.add(subrow);
        
        subrow = new JPanel();
        subrow.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 2));
        subrow.add(startAbsRB);
        subrow.add(startTimeField);
        holder.add(subrow);

        row.add(holder);
        top.add(row);

        // row 8
        row = new JPanel();
        row.setLayout(new BoxLayout(row,BoxLayout.Y_AXIS));
 
        holder = new JPanel();
        holder.setLayout(new BoxLayout(holder,BoxLayout.Y_AXIS));
        subrow = new JPanel();
        subrow.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 2));
        subrow.add(new JLabel("EndTime"));
        subrow.add(Box.createHorizontalGlue());
        subrow.add(resetEndButton);
        holder.add(subrow);
        
        subrow = new JPanel();
        subrow.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 2));
        subrow.add(endDurRB);
        subrow.add(durationField);
        holder.add(subrow);
        
        subrow = new JPanel();
        subrow.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 2));
        subrow.add(endAbsRB);
        subrow.add(endTimeField);
        holder.add(subrow);

        subrow = new JPanel();
        subrow.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 2));
        subrow.add(endForRB);
        holder.add(subrow);

        row.add(holder);
        top.add(row);

        // row 9 -- general information and status
        row = new JPanel();
        row.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 4));
        row.add(generalStatus);
        top.add(row);

        JPanel buttonPanel = new JPanel();
        JButton button;

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        top.add(buttonPanel);

        segmentName.addFocusListener(new FocusListener(){
            public void focusGained(FocusEvent ignore) {}
            public void focusLost(FocusEvent arg0) {
                validateSegmentName();
                updateGraphics();
        }});
            
        host.addFocusListener(new FocusListener(){
            public void focusGained(FocusEvent ignore) {}
            public void focusLost(FocusEvent arg0) {
                hostAndPortAction();
        }});

        port.addFocusListener(new FocusListener(){
            public void focusGained(FocusEvent ignore) {}
            public void focusLost(FocusEvent arg0) {
                hostAndPortAction();
        }});

        durationField.addFocusListener(new FocusListener(){
            public void focusGained(FocusEvent ignore) {}
            public void focusLost(FocusEvent arg0) {
                validateDuration();
                dirtyTime = true;
                updateGraphics();
        }});

        startTimeField.addFocusListener(new FocusListener(){
            public void focusGained(FocusEvent ignore) {}
            public void focusLost(FocusEvent arg0) {
                validateStartTime();
                dirtyTime = true;
                updateGraphics();
        }});

        endTimeField.addFocusListener(new FocusListener(){
            public void focusGained(FocusEvent ignore) {}
            public void focusLost(FocusEvent arg0) {
                validateEndTime();
                dirtyTime = true;
                updateGraphics();
        }});

        setGeneralStatus();

        getContentPane().add(top, BorderLayout.CENTER);

    }

    public void start() {
        setDefaults();
        validateSegmentName();
        validateHostAndPort();
        validateSourcePath();
        updateGraphics();
        setVisible(true);
    }

    public void restart(String host, String port, String sourcePath) {
        setDefaults();
        setHost(host);
        setPort(port);
        validateSegmentName();
        previousServer = null;
        previousChannel = null;
        validateHostAndPort();
        if (serverOK) {
            DefaultComboBoxModel m = (DefaultComboBoxModel)comboBox.getModel();
            int i = m.getIndexOf(sourcePath);
            if (i > -1)
                comboBox.setSelectedIndex(i);
        }
        validateSourcePath();
        updateGraphics();
        setVisible(true);
    }

    private void setDefaults() {
        setSegmentName(archive.nextDefaultSegmentName());
        setHost(DEFAULT_HOST);
        setPort(DEFAULT_PORT);
        endForRB.setSelected(true);
        startNowRB.setSelected(true);
        setStartTime(0.0);
        setEndTime(0.0);
        setDuration(0.0);
        useDuration = false;
        dirtyTime = false;
        comboBox.setModel(defaultModel);
    }

    private void updateGraphics() {
        updateButtons();
        setGeneralStatus();
        pack();
    }
    
    private void hostAndPortAction() {
        validateHostAndPort();
        if (serverOK) validateSourcePath();
        updateGraphics();
    }

    private void setGeneralStatus() {
        String s = "";
        if (errorMessage != null)
            s += v.red(errorMessage) + "<br>";
        if (!segmentOK)
            s += v.red("Segment Name invalid") + " -- set Segment Name";
        else if (!serverOK)
            s += v.red("Server invalid") + " -- set Host and/or Port";
        else if (!sourceOK)
            s += v.red("Set Source Path");
        else
            s += "check all values and Set Parameters or Cancel";
        generalStatus.setText(v.htmlSmall(s));
        errorMessage = null;
    }

    private JButton setupButton(String action, ActionListener l) {
        JButton button = new JButton(action);
        button.addActionListener(l);
        button.setActionCommand(action);
        return button;
    }

    private void updateButtons() {

        startNowRB.setEnabled(false);
        startAbsRB.setEnabled(false);
        endDurRB.setEnabled(false);
        endForRB.setEnabled(false);
        endAbsRB.setEnabled(false);

        comboBox.setEnabled(false);
        
        adjustTimeButton.setEnabled(false);
        resetStartButton.setEnabled(false);
        resetEndButton.setEnabled(false);

        cancelButton.setEnabled(true);
        okButton.setEnabled(ready());
        setServerButton.setEnabled(true);
        
        if (serverOK)
            setServerButton.setText("Change Server");
        else
            setServerButton.setText("Set Server");
        
        if (!serverOK) return;

        if (!sourceOK) return;

        comboBox.setEnabled(true);

        if (dirtyTime)
        {
            if (useDuration)
                adjustTimeButton.setText("Adjust End Time");
            else
                adjustTimeButton.setText("Adjust Duration");
            adjustTimeButton.setEnabled(true);
        }
        else
        {
            adjustTimeButton.setText("Times adjusted");
            adjustTimeButton.setEnabled(false);            
        }
        
        resetStartButton.setEnabled(true);
        resetEndButton.setEnabled(true);

        startNowRB.setEnabled(true);
        startAbsRB.setEnabled(true);
        endDurRB.setEnabled(true);
        endForRB.setEnabled(true);
        endAbsRB.setEnabled(true);

    }
    
    private void wait(String message)
    {
        errorMessage = message;
        updateGraphics();
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

        String arg = ev.getActionCommand();

System.out.println("Event Action: " + arg);

        if (arg.equals(OK_ACTION)) {
            if (!ready())
            {
                updateGraphics();
                return;
            }
            cancled = false;
            dispose();
        } else if (arg.equals(CANCEL_ACTION)) {
            cancled = true;
            dispose();
        } else if (arg.equals(SET_SERVER_ACTION)) {
            // nothing needs to be done here;
            // the action is carried out by the fact that the
            // host or port field loose focus!
        } else if (arg.equals(NEW_CHANNEL_ACTION)) {
            validateSourcePath();
            updateGraphics();
        } else if (arg.equals(RESET_START_ACTION)) {
            setStartTimeFromServer();
        } else if (arg.equals(RESET_END_ACTION)) {
            setEndTimeFromServer();
        } else if (arg.equals(ADJUST_TIME_ACTION)) {
            if (dirtyTime)
            {
                dirtyTime = false;
                if (useDuration){
                    validateDuration();
                    setEndTime(startTime + duration);
                }
                else
                {
                    double d = endTime - startTime;
                    if (d < 0.0) d = 0.0;
                    setDuration(d);
                }
                updateButtons();
            }
        } else if (arg.equals(START_NOW_ACTION)) {
            startTime = 0.0;
        } else if (arg.equals(START_ABS_ACTION)) {
            validateStartTime(); 
        } else if (arg.equals(END_DUR_ACTION)) {
            useDuration = true;
            validateDuration();
            updateButtons();
        } else if (arg.equals(END_FOR_ACTION)) {
            useDuration = false;
            endTime = 0.0;
            updateButtons();
        } else if (arg.equals(END_ABS_ACTION)) {
            useDuration = false;
            validateEndTime();
            updateButtons();
        } else
            System.out.println("Unrecognised command: " + arg);
    }

    private boolean ready() {
        return segmentOK && serverOK && sourceOK;
    }

    public boolean cancled() {
        return cancled;
    }

    private void exit() {
        setVisible(false);
    }

    private void validateSegmentName() {
        String segName = getSegmentName();
        setSegmentName(segName); // remove any blanks
        if (segName.length() == 0) {
            setSegmentName(archive.nextDefaultSegmentName());
            validateSegmentName();
            errorMessage = "Segment name is blank - resetting";
            return;
        } else if (archive.getSegmentByName(segName) != null) {
            setSegmentName(archive.nextDefaultSegmentName());
            validateSegmentName();
            errorMessage = "Segment name " + 
                segName + " is in use - resetting";
            return;
        }
        segmentOK = true;
    }

    private void validateHostAndPort() {
        setHost(getHost()); // remove any blanks
        setPort(getPort());
        if (getHost().length() == 0) {
            serverOK = false;
            return;
        }
        if (getPort().length() == 0) {
            serverOK = false;
            return;
        }
        try {
            Sink sink = new Sink();
            sink.OpenRBNBConnection(getServer(), "__test");
            sink.CloseRBNBConnection();
        } catch (SAPIException e) {
            serverOK = false;
            errorMessage = "Failed to connect to server " + getServer();
            return;
        }
        serverOK = true;
        checkForServerChange();
    }

    private void validateSourcePath() {
        if (!serverOK) // server not valid
        {
            sourceOK = false;
            previousChannel = null; // reset source path
            return;
        }
        String path = getSourcePath();
        if (path == null) {
            sourceOK = false;
            return;
        }
        Vector v = new Vector();
        try {
            v =
                ChannelUtility.appendChannelListFromString(
                    getServer(),
                    false,
                    path,
                    v);
        } catch (SAPIException e) {
            errorMessage = "Failed to connect to server to get source path";
            sourceOK = false;
            return;
        }
        if (v.size() < 1) {
            errorMessage = "Source Path is no longer on the server";
            sourceOK = false;
            return;
        }
        sourceOK = true;
        checkForChannelChange();
    }

    private void validateStartTime() {
        setStartTime(getStartTimeText()); // remove blanks; format; zero if empty
    }

    private void validateEndTime() {
        setEndTime(getEndTimeText()); // remove blanks; format; zero if empty
    }
    
    private void validateDuration(){
        setDuration(getDurationText());  // remove blanks; format; zero if empty
    }

    private void checkForServerChange() {
        if (!serverOK)
            return; // not a valid server
        if (getServer().equals(previousServer))
            return; // unchanged
        previousServer = getServer();
        setChannelsFromRBNB();
    }

    private void setChannelsFromRBNB() {
        DefaultComboBoxModel m = defaultModel;
        try {
            Vector v = ChannelUtility.getAllSourcesAsVector(getServer(), false);
            if (v.size() == 0) {
                errorMessage = "No source paths on server";
                comboBox.setModel(m);
                previousChannel = null;
                return;
            }
            String names[] = new String[v.size()];
            int ndx = 0;
            for (Enumeration e = v.elements(); e.hasMoreElements();) {
                ChannelUtility.NodeCover c =
                    (ChannelUtility.NodeCover)e.nextElement();
                names[ndx++] = c.getFullName();
            }
            String mimes[] = ChannelUtility.getMimeTypes(getServer(), names);
            Vector n = new Vector();
            int here = 0;
            for (int i = 0; i < names.length; i++) {
                ComboNodeCover c = new ComboNodeCover(names[i], mimes[i]);
                if (c.isImage())
                    n.add(here++, c);
                else
                    n.add(c);
            }
            m = new DefaultComboBoxModel(n);
        } catch (SAPIException e) {
            errorMessage = "Failed to get source paths from server.";
            comboBox.setModel(m);
            previousChannel = null;
            return;
        }
        comboBox.setModel(m);
        previousChannel = null;
    }

    private void checkForChannelChange() {
        if (!serverOK) // server not valid
        {
            previousChannel = null; // reset channel
            return;
        }
        String path = getSourcePath();
        if (path == null) return;
        if (path.equals(previousChannel))
            return;
        previousChannel = path;
        setStartTimeFromServer();
        setEndTimeFromServer();
    }

    private void setStartTimeFromServer() {
        if (!serverOK) return;
        if (!sourceOK) return;
        String path = getSourcePath();
        if (path == null) return;        
        try {
            setStartTime(ChannelUtility.getEarliestTime(getServer(), path));
        } catch (SAPIException ignore) {
            errorMessage = "Bad value or format for Start Time";
        }
        validateStartTime();
        dirtyTime = true;
    }

    private void setEndTimeFromServer() {
        if (!serverOK) return;
        if (!sourceOK) return;
        String path = getSourcePath();
        if (path == null) return;        
        try {
            setEndTime(ChannelUtility.getLatestTime(getServer(), path));
        } catch (SAPIException e) {
            errorMessage = "Bad value or format for End Time";
        }
        validateEndTime();
        dirtyTime = true;
    }

    /** @return */
    public String getHost() {
        return host.getText().trim();
    }
    /** @return */
    public String getPort() {
        return port.getText().trim();
    }
    /** @return */
    public String getServer() {
        return getHost() + ":" + getPort();
    }
    /** @return */
    public String getSegmentName() {
        return segmentName.getText().trim();
    }
    /** @return */
    public String getSourcePath() {
        if (!serverOK)
            return null;
        if (comboBox.getModel().equals(defaultModel))
            return null;
        if (comboBox.getSelectedIndex() < 0)
            return null;
        ComboNodeCover n = (ComboNodeCover)comboBox.getSelectedItem();
        return n.getName();
    }
    
    /** @return */
    public double getStartTime() {
        return startTime;
    }
    
    
    /** @return */
    public double getEndTime() {
        return endTime;
    }
    
    /** @return */
    public double getDuration(){
        return duration;
    }
    
    /** @return */
    public boolean getUseDurationFlag()
    {
        return useDuration;
    }

    private void setHost(String field) {
        host.setText(field);
    }
    private void setPort(String field) {
        port.setText(field);
    }
    private void setSegmentName(String field) {
        segmentName.setText(field);
    }
    private void setStartTime(double d) {
        startTime = d;
        startTimeField.setText(formatTime(d));
    }
    private void setStartTime(String string) {
        setStartTime(getTimeValueFromString(string));
    }
    private void setEndTime(double d) {
        endTime = d;
        endTimeField.setText(formatTime(d));
    }
    private void setEndTime(String string) {
        setEndTime(getTimeValueFromString(string));
    }

    private void setDuration(double d)
    {
        duration = d;
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(4);
        nf.setMinimumFractionDigits(1);
        durationField.setText(nf.format(duration));
    }

    private void setDuration(String string){
        double value = 0.0;
        if (string != null)
        {
            string = string.trim();
            try {
                value = Double.parseDouble(string);
            } catch (Exception ignore) {}
        }
        setDuration(value);
    }
    
    private double getTimeValueFromString(String string) {
        double value = 0.0;
        if (string == null) return value;
        string = string.trim();
        try {
            value = Double.parseDouble(string);
            return value;
        } catch (Exception ignore) {}
        try {
            long unixTime = TIMEFORMAT.parse(string).getTime();
            value = ((double) (unixTime)) / 1000.0;
            // convert milliseconds to seconds
        } catch (Exception ignore) {
            errorMessage = "Warning TIMEFORMAT parse failed " + string;
        }
        return value;
    }

    private String formatTime(double t) {
        if (t == 0.0)
            return "" + t;
        long unixTime = (long) (t * 1000.0); // convert seconds to milliseconds
        String ret = TIMEFORMAT.format(new Date(unixTime));
        return ret;
    }

    private String getStartTimeText() {
        return startTimeField.getText();
    }
    private String getEndTimeText() {
        return endTimeField.getText();
    }
    private String getDurationText(){
        return durationField.getText();
    }

    public static void main(String[] args) {

        // Schedule a job for the event-dispatching thread:
        // create and show this application's GUI; start the applicaiton
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ImageCaptureArgsDialog wrap = new ImageCaptureArgsDialog();
                TestFrame t = wrap.new TestFrame();
                t.go();
            }
        });
    }

    public class ComboNodeCover {
        String name;
        boolean image = false;
        public ComboNodeCover(String name, String mime) {
            this.name = name;
            if (mime.equals("image/jpg")
                || name.endsWith(".jpg")
                || name.endsWith(".jpeg"))
                image = true;
        }
        public String toString() {
            if (isImage())
                return name;
            return v.htmlGray(name);
        }
        public String getName() {
            return name;
        }
        public boolean isImage() {
            return image;
        }
    }

    private class TestFrame extends JFrame
    {
        public TestFrame() {
            super();
        }
        public void go() {

            JFrame.setDefaultLookAndFeelDecorated(true);
            addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    dispose();
                }
            });

            doTest();
        }

        private void doTest() {
            Archive a = null;

            try {
                a = new Archive();
            } catch (ArchiveException e) {
                e.printStackTrace();
                System.out.println("Could not get an archive");
            }
            if (a == null)
                return;
            ImageCaptureArgsDialog test = new ImageCaptureArgsDialog(this, a);
            test.start();
            printTest(test);
            while (!test.cancled()) {
                test.restart(
                    test.getHost(),
                    test.getPort(),
                    test.getSourcePath());
                printTest(test);
            }
            test.dispose();
            System.exit(0);
        }
        /**
         * @param test
         */
        private void printTest(ImageCaptureArgsDialog test) {
            if (test.cancled()) {
                System.out.println("");
                System.out.println("Test cancled.");
            }
            System.out.println("");
            System.out.print("Segment Name: " + test.getSegmentName());
            System.out.print(
                ";  Server: " + test.getHost() + ":" + test.getPort());
            System.out.print(";  Path of Source: " + test.getSourcePath());
            System.out.print(";  Start Time: " + test.getStartTime());
            System.out.print(";  End Time: " + test.getEndTime());
            System.out.print(";  Duration: " + test.getDuration());
            System.out.print(";  Use Duration flag = " +
                (test.getUseDurationFlag()?"true":"flase"));
            System.out.println("");
        }
    }

    // Utility Tracing 
    private void traceTime(double d) {
        traceTime("", d);
    }
    private void traceTime(String prefix, double d) {
        long unixTime = (long) (d * 1000.0);
        traceTime(prefix, unixTime);
    }
    private void traceTime(long t) {
        traceTime("", t);
    }
    private void traceTime(String prefix, long t) {
        System.out.println(prefix + ": " + TIMEFORMAT.format(new Date(t)));
    }

}
