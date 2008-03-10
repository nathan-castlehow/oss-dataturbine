package org.nees.jcamera;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;

public class Lim
{
    //~ Static fields/initializers ---------------------------------------------

    static Logger log = Logger.getLogger( Lim.class.getName() );

    public static int DEFAULT_PORT = 44000;

    /**
     * Used to store Camera objects attached to the system.
     */
    private static Hashtable cameras = new Hashtable();

    /**
     * Used to store proposals keyed by TransactionID.
     * Each Propose object has its own ControlPoint object.
     */
    private static Hashtable proposals = new Hashtable();

    /**
     * Used to store DataTurbine objects for cameras.
     */
    private static Hashtable turbines = new Hashtable();

    //~ Instance fields --------------------------------------------------------

    /**
     * Used to read incoming messages from the LabviewNTCPPlugin.
     */
    private BufferedReader input;

    /**
     * Used to send outgoing messages to the LabviewNTCPPlugin.
     */
    private PrintStream output;

    /**
     * Used to accept incoming connections from LabviewNTCPPlugin.
     */
    private ServerSocket serverSocket;

    /**
     * Used to handle accepted connections from LabviewNTCPPlugin.
     */
    private Socket communicationSocket;

    /**
     * Port the server listens on.
     */
    private int localPort = DEFAULT_PORT;

    //~ Constructors -----------------------------------------------------------

    public Lim()
    {
        log.debug( "lim created" );

        initializeCameras();
        openDataTurbines();

        try
        {
            log.debug( "Creating socket" );
            serverSocket = new ServerSocket( localPort );
        }
        catch ( Exception e )
        {
            log.error( "Error creating socket: " + e );
        }

        // Start accepting commands.
        accept();

        ( new ProcessingThread() ).start();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Main
     */
    public static void main( String args[] )
    {
        new Lim();
    }

    /**
     * Initialize cameras.
     */
    public void initializeCameras()
    {
        log.debug( "Number of cameras fixed to one.");
        
        // Create a single dummy camera with a nonsense bus ID
        cameras.put( "camera" + 1, new CameraFromScript());
    }

    /**
     * Initialize data turbines.
     */
    public void openDataTurbines()
    {
        for ( int i = 1; i <= getNumberOfCameras(); i++ )
        {
            log.debug( "Creating turbine for camera" + i );
            DataTurbine dt = new DataTurbine( "camera" + i );
            dt.open();
            turbines.put( "camera" + i, dt );
        }
    }

    /**
     * Accept incoming connections.
     */
    protected void accept()
    {
        try
        {
            log.debug( "Accepting connections" );
            communicationSocket = serverSocket.accept();
            log.debug( "Socket Connected." );
        }
        catch ( Exception e )
        {
            log.error( "Error accepting connections: " + e );
        }

        try
        {
            // Set up output stream
            output = new PrintStream( communicationSocket.getOutputStream() );

            // Set up input stream
            input = new BufferedReader( new InputStreamReader( 
                        communicationSocket.getInputStream() ) );
        }
        catch ( IOException ioe )
        {
            log.error( "Error setting up output stream: " + ioe );
        }
    }

    /**
     * Get a camera.
     */
    public static Camera getCamera( String name )
    {
        return ( Camera )cameras.get( name );
    }

    /**
     * Get the number of cameras.
     */
    public static int getNumberOfCameras()
    {
        return 1;
//        return cameras.size();
    }

    /**
     * Get a proposal.
     */
    public static Command getProposal( String transactionID )
    {
        return ( Command )proposals.get( transactionID );
    }

    /**
     * Add a proposal.
     */
    public static void addProposal( String transactionID, Command proposal )
    {
        proposals.put( transactionID, proposal );
    }

    /**
     * Get a turbine.
     */
    public static DataTurbine getTurbine( String name )
    {
        return ( DataTurbine )turbines.get( name );
    }

    /**
     * Send message.
     */
    protected synchronized void sendMessage( String message )
    {
        output.print( message );
    }

    /**
     * For printing out a nicely formatted message string.
     */
    protected String prettyString( String string )
    {
        StringTokenizer st = new StringTokenizer( string );
        String s = "";

        while ( st.hasMoreTokens() )
        {
            s += st.nextToken() + " ";
        }
        
        return s;
    }

    //~ Inner Classes ----------------------------------------------------------

    class ProcessingThread extends Thread
    {
        //~ Methods ------------------------------------------------------------

        public void run()
        {
            log.debug( "Processing thread started" );
            
            String message = null;

            while ( true )
            {
                try
                {
                    message = input.readLine();
                    if ( message == null )
                    {
                        accept();
                        continue;
                    }

                    log.info( "Incoming: " + prettyString( message ) );
                }
                catch ( IOException ioe )
                {
                    log.error( "Error reading from socket: " + ioe );
                    accept();

                    continue;
                }

                if ( message.equals( "" ) )
                {
                    log.warn( "Received empty message" );

                    continue;
                }

                // This will return when all operations are done
                // so we don't need to wait here (we wait somewhere else).
                Command cmd = new Command( message );

                log.info( "Response: " + prettyString( cmd.getMessage() ) );

                // Send it over the wire
                sendMessage( cmd.getMessage() );
            }
        }
    }
}
