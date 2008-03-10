/*
 * AboutDialog.java
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
 *   $RCSfile: LongTimeDialog.java,v $ 
 */
package org.nees.tivo;

import java.awt.BorderLayout;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class LongTimeDialog extends JDialog implements KeyEventDispatcher {

    public LongTimeDialog(JFrame owner, String processName) {
        super(owner);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowActivated(WindowEvent e) {
                bindKeys();
            }
            public void windowDeactivated(WindowEvent e) {
                unbindKeys();
            }
        });
        
        setTitle("This Takes Some Time");

        getContentPane().setLayout(new BorderLayout());

        JPanel container = new JPanel();
        getContentPane().add(container, BorderLayout.CENTER);
		container.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        container.setLayout(new BorderLayout());

        String about = "<html><center>"; 
        about += "<b>" + processName + "</b><br>" +
            "<i><b>This process will take some time.</b><br>" +
            "</i>Please Wait." +
            "</center></html>";

        container.add(new JLabel(about),BorderLayout.CENTER);
        pack();

        setVisible(true);
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
    
    public static void main (String[] args){
        new LongTimeDialog(new JFrame(),"Test");
    }
}
