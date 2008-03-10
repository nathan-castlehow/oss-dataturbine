import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

import java.util.*;
import com.rbnb.sapi.*;
import java.io.*;


  /**
  * This is the main class of the "Hello, World" demo.
  * 
  * @author Carol Hamer 
  */
public class Hello extends MIDlet implements CommandListener 
{
	public static boolean connected;
	/**
	 * The canvas is the region of the screen that has been allotted 
	 * to the game. 
	 */
	HelloCanvas myCanvas;
	/**
	 * The Command objects appear as buttons in this example.
	 */
	private Command exitCommand = new Command("Exit", Command.EXIT, 99);
	/**
	 * The Command objects appear as buttons in this example.
	 */
	private Command toggleCommand = new Command("Toggle Msg", Command.SCREEN, 1);
	/**
	 * Initialize the canvas and the commands.
	 */
	public Hello() 
	{
		myCanvas = new HelloCanvas();  
		myCanvas.addCommand(exitCommand);
		myCanvas.addCommand(toggleCommand);
		// you set one command listener to listen to all
		// of the commands on the canvas:  
		myCanvas.setCommandListener(this);
	}
	//---------------------------------------------------------// implementation of MIDlet
	/**
	 * Start the application.
	 */
	public void startApp() throws MIDletStateChangeException 
	{
		// display my canvas on the screen:
		
		
		//tsink.main(null);
		tsource.main(null);
		if(connected)
			myCanvas.word = "connected!";
		Display.getDisplay(this).setCurrent(myCanvas);  
		myCanvas.repaint();

	}
	/**
	 * If the MIDlet was using resources, it should release 
	 * them in this method.
	 */
	public void destroyApp(boolean unconditional) 
		throws MIDletStateChangeException 
	{
	}
	/**
	 * This method is called to notify the MIDlet to enter a paused
	 * state. The MIDlet should use this opportunity to release 
	 * shared resources. 
	 */
	public void pauseApp() 
	{
	}
	//--------------------------------------------------------- // implementation of CommandListener
	/*
	 * Respond to acommand issued on the Canvas. 
	 * (either reset or exit).
	 */
	public void commandAction(Command c, Displayable s) 
	{
		if(c == toggleCommand) 
		{
			myCanvas.toggleHello();
		} 
		else if(c == exitCommand) 
		{ 
			try 
			{
				destroyApp(false);
				notifyDestroyed();
			} 
			catch (MIDletStateChangeException ex)
			{
			}
		}
	}
}


