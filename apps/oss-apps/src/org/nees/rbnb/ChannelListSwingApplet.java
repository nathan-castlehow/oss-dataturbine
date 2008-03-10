/*
 * Created on Mar 25, 2004
 */
package org.nees.rbnb;

// ChannelListSwingApplet

/*
 * SwingApplication.java is a 1.4 example that requires
 * no other files.
 */
import javax.swing.*;          
import java.awt.*;
import java.awt.event.*;

public class ChannelListSwingApplet extends JApplet implements ActionListener
{

	private ChannelListSwing s = new ChannelListSwing();
	private static final String SERVER_NAME = "localhost";
	private static final String SERVER_PORT = "3333";

	private void setArgs() {
		String serverName = SERVER_NAME;
		String serverPort = SERVER_PORT;
		String param;

		param = getParameter("serverName");
		if (param != null)
			serverName = param;
		
		param = getParameter("serverPort");
		if (param != null)
			serverPort = param;
		
		s.setNameAndPort(serverName,serverPort);		
	}

	public void init() {
		//Execute a job on the event-dispatching thread:
		//creating this applet's GUI.
		try {
			javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					setArgs();
					s.connect();
					s.initGraphics();
					getContentPane().add(s, BorderLayout.CENTER);
					s.setTimer();
					s.startCheckThread();
				}
			});
		} catch (Exception e) {
			System.err.println("createGUI didn't successfully complete");
		}
	}

	public void actionPerformed(ActionEvent arg0) {		
	}
	
}

