package org.nees.archive.gui;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class SendThreadDialog
extends JDialog 
implements KeyEventDispatcher, ActionListener
{
    // button faces
    private static final String SET = "Set Server";
    private static final String CANCEL = "Cancel";

    private JButton selectButton;
    private JButton cancelButton;
    
    private boolean canceled = false;

    public SendThreadDialog(JFrame frame)
    {
        super(frame,"Set host and port",true);

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

    public static void main(String[] args) {
        // TODO Auto-generated method stub

    }

}
