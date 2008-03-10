package org.nees.archive.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class HostAndPortDialog extends JDialog 
implements KeyEventDispatcher, ActionListener
{

    // button faces
    private static final String SET = "Set Server";
    private static final String CANCEL = "Cancel";

    private JTextField host = new JTextField();
    private JTextField port = new JTextField();
    
    private JButton selectButton;
    private JButton cancelButton;
    
    private boolean canceled = false;

    public HostAndPortDialog(JFrame frame, String host, String port) {
        super(frame,"Set host and port",true);

        this.host.setText(host);
        this.port.setText(port);
        
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
        BoxLayout b = new BoxLayout(top, BoxLayout.Y_AXIS);
        top.setLayout(b);
        
        JPanel holder = new JPanel();
        holder.setLayout(new FlowLayout());
        holder.add(new JLabel("Host"));
        holder.add(this.host);
        this.host.setColumns(30);
        top.add(holder);
    
        holder = new JPanel();
        holder.setLayout(new FlowLayout());
        holder.add(new JLabel("Port"));
        holder.add(this.port);
        this.port.setColumns(30);
        top.add(holder);

        JPanel buttonPanel = new JPanel();
        JButton button;

        buttonPanel.add(button = new JButton(SET));
        button.addActionListener(this);
        selectButton = button;

        buttonPanel.add(button = new JButton(CANCEL));
        button.addActionListener(this);
        cancelButton = button;

        top.add(buttonPanel);

        getContentPane().add(top, BorderLayout.CENTER);

        updateButtons();
        pack();
        setVisible(true);
    }

    private void updateButtons() {
        cancelButton.setEnabled(true);
        selectButton.setEnabled(true);
    }

    String getPort() {
        return port.getText();
    }

    String getHost() {
        return host.getText();
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
            dispose();
        } else if (arg.equals(CANCEL)) {
            canceled = true;
            dispose();
        }
    }
    
    public boolean isCancled() {
        return canceled;
    }

    public static void main (String[] args){
        try {
            String host = "localhost";
            String port = "3333";
            JFrame frame = new JFrame("Test");
            HostAndPortDialog d = new HostAndPortDialog(frame,host,port);
            host = d.getHost();
            port = d.getPort();
            if (d.isCancled())
                System.out.println("Cancled");
            else
                System.out.println("Selected RBNB server = " + host + ":" + port);
        } catch (Throwable e) {
            e.printStackTrace();
        }    
        System.exit(0);
    } // main

}
