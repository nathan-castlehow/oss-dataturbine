/*
 * Created on Feb 22, 2004
 *
 */
package org.nees.rbnb;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author terry
 *
 */
public class MessagePanel 
		extends Panel
		implements ActionListener
{ 
	
	TextArea textArea = null;
	
	MessagePanel()
	{
		setLayout(new BorderLayout());
		Button b = new Button("Clear");
		b.addActionListener(this);
		add("South",b);
		
		textArea = new TextArea("", 10, 40);
		add("Center",textArea);
		validate();
		repaint();
	}
	
	public void message(String text)
	{
		textArea.append(text + "\n");
	}

	public void actionPerformed(ActionEvent arg0) {
		clear();
	}

	private void clear()
	{
		textArea.setText("");
	}	
}
