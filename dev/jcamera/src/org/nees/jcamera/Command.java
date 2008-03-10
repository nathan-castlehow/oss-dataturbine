/* File Command.java
 * 
 * @author Scott Gose
 * @author Paul Hubbard
 * @author Jose Calderon
 * @date 2003-4
 * 
 * Originally written by Jose Calderon, rewritten by Scott Gose (who did an imressive job)
 * and now being brutally hacked by Paul Hubbard, who is attempting to replace the 
 * jphoto/jusb back end with command-line tools, and also remove all the platform-specific
 * code so that sites can use this under Windows. 
 * 
 * Yeah, I know. 
 *
 * TODO Remove all usb-specific stuff and maybe threading as well
 */

package org.nees.jcamera;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;

import com.rbnb.sapi.SAPIException;


/**
 * This class defines the command set supported by the LabviewNTCPPlugin.
 */
public class Command
{
    //~ Static fields/initializers ---------------------------------------------

    static Logger log = Logger.getLogger( Command.class.getName() );

    public static final String OPENSESSION = "open-session";
    public static final String SETPARAMETER = "set-parameter";
    public static final String GETPARAMETER = "get-parameter";
    public static final String PROPOSE = "propose";
    public static final String EXECUTE = "execute";
    public static final String GETCONTROLPOINT = "get-control-point";
    public static final String CLOSESESSION = "close-session";

    public static final String OK = "OK";
    public static final String ERROR = "Error";
    public static final String NEWLINE = "\n";
    public static final String DELIMITER = "\t";

    public static final int SUCCESS = 0;
    public static final int NOTRANSACTIONID = 1;
    public static final int WRONGVERB = 2;
    public static final int EXECUTEFAILED = 3;
    public static final int PROPOSEREJECTED = 4;

    //~ Instance fields --------------------------------------------------------

    /**
     * Holder for the tokenized message.
     */
    private StringTokenizer tokens;

    /**
     * The type of command it is.
     */
    private String verb;

    /**
     * Contains the transaction ID of said command.
     */
    private String transactionID;

    /**
     * Holds the original command in its entirety.
     */
    private String command;

    /**
     * The status of the command.
     */
    private String status;

    /**
     * The message contains the status plus additional info if necessary.
     */
    private String message;

    //~ Constructors -----------------------------------------------------------

    public Command( String command )
    {
        // Make a backup of this, execute will use it;
        this.command = command;

        tokens = new StringTokenizer( command );

        verb = tokens.nextToken();

        if ( tokens.hasMoreTokens() )
        {
            transactionID = tokens.nextToken();
        }
        else
        {
            log.error( "No TransactionID or bad delimiter" );
            status = ERROR + DELIMITER + NOTRANSACTIONID +
                DELIMITER + "No TransactionID or bad delimiter";
            message = status + NEWLINE;

            return;
        }

        if ( verb.equals( OPENSESSION ) )
        {
            openSession();
        }
        else if ( verb.equals( SETPARAMETER ) )
        {
            setParameter();
        }
        else if ( verb.equals( GETPARAMETER ) )
        {
            getParameter();
        }
        else if ( verb.equals( PROPOSE ) )
        {
            propose();
        }
        else if ( verb.equals( EXECUTE ) )
        {
            execute();
        }
        else if ( verb.equals( GETCONTROLPOINT ) )
        {
            getControlPoint();
        }
        else if ( verb.equals( CLOSESESSION ) )
        {
            closeSession();
        }
        else
        {
            error();
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Get message.
     */
    public String getMessage()
    {
        return message;
    }

    /**
     * Return the original string.
     */
    public String toString()
    {
        return command;
    }

    /**
     * Open session
     */
    private void openSession()
    {
        log.debug("openSession called");

        status = OK + DELIMITER + SUCCESS;
        message = status + DELIMITER + transactionID + NEWLINE;
        
        log.debug("Message is " + message);
    }

    /**
     * TODO
     * Set a parameter.
     */
    private void setParameter()
    {
        status = OK + DELIMITER + SUCCESS;
        message = status + DELIMITER + transactionID + NEWLINE;
    }

    /**
     * TODO
     * Get a parameter.
     */
    private void getParameter()
    {
        // NOTE Fixed ntcp get/set paremeter string "K 0.0". Dorky.
        String parameter = "K" + DELIMITER + "2.731";

        status = OK + DELIMITER + SUCCESS;
        message = status + DELIMITER + transactionID + DELIMITER + 
            parameter + NEWLINE;
    }

    /**
     * Get current Control Point. This is really a status/feedback query.
     */
    private void getControlPoint()
    {
        String name;
        String state;
        Camera camera;

        try
        {
            name = tokens.nextToken();

            // If we get a generic request, we can't send back the state of all
            // control points, so just send back the status of the first one.
            if ( name.equals( "camera" ) )
            {
                camera = Lim.getCamera( "camera1" );
            }
            else
            {
                camera = Lim.getCamera( name );
            }

            int x = 0;
            int y = 0;
            int z = camera.getFocalLength();

            state = name + DELIMITER +
                "x" + DELIMITER + "rotation" + DELIMITER + x + DELIMITER +
                "y" + DELIMITER + "rotation" + DELIMITER + y + DELIMITER +
                "z" + DELIMITER + "displacement" + DELIMITER + z;

            status = OK + DELIMITER + SUCCESS;
            message = status + DELIMITER + transactionID + DELIMITER + 
                state + DELIMITER + NEWLINE;
        }
        catch ( NoSuchElementException nsee )
        {
            log.error( "ControlPoint name not found in request." );
            status = OK + DELIMITER + SUCCESS;
            message = status + DELIMITER + transactionID + DELIMITER + 
                "ControlPoint name not found in request" + NEWLINE;
        }
    }

    /**
     * Close session.
     */
    private void closeSession()
    {
        status = OK + DELIMITER + SUCCESS;
        message = status + DELIMITER + transactionID + NEWLINE;
    }

    /**
     * Error
     */
    private void error()
    {
        status = ERROR + DELIMITER + WRONGVERB + DELIMITER;
        message = status + DELIMITER + transactionID + DELIMITER + 
            "wrong verb or bad delimiter" + NEWLINE;
    }

    /**
     * Propose a transacation to be executed.  An error is returned if the
     * transaction is malformed or cannot be executed.  If the transaction is
     * accepted, it is added to a hashtable for later execution.
     */
    private void propose()
    {
        String name = null;
        String error = null;
        Camera camera = null;

        try
        {
            name = tokens.nextToken();

            // This will be null on a generic control point,
            // but that is ok since we deal with it below.
            camera = Lim.getCamera( name );
        }
        catch ( NoSuchElementException nsee )
        {
            error = "No control point specified";
            log.error( error );
        }

        if ( camera == null )
        {
            if ( name.equals( "camera" ) )
            {
                log.debug( "Generic control point being used" );
            }
            else
            {
                error = "Control point NOT found";
                log.error( error + ": " + name );
            }
        }
        else
        {
            log.debug( "Control point found: " + name );
        }

        // Empty proposal will take a picture with settings as is.
        while ( tokens.hasMoreTokens() )
        {
            String parameter = tokens.nextToken();

            if ( ! tokens.hasMoreTokens() )
            {
                error = "no parameter type specified";
                continue;
            }

            String type = tokens.nextToken();

            if ( ! tokens.hasMoreTokens() )
            {
                error = "no parameter value specified";
                continue;
            }

            String value = tokens.nextToken();
                
            log.debug( "parameter type value: " + 
                parameter + " " + type + " " + value );

            // We don't really care what values are passed in for x, y, and z.
            // x and y aren't even used and if a bad z value is given, the
            // closest focal length is used anyway.  We're mainly just concerned
            // that they came in formatted properly.  In the future though, when
            // we do care, the processing checks for these values will be here.
            switch ( parameter.charAt( 0 ) )
            {
                case 'x':
                    // TODO
                    // error = "Incorrect value for geomAxis x";
                    break;

                case 'y':
                    // TODO
                    // error = "Incorrect value for geomAxis y";
                    break;

                case 'z':
                    // TODO
                    // error = "Incorrect value for geomAxis z";
                    break;

                default:
                    error = "Unknwon GeomAxisType";
            }

            if ( error == null )
            {
                Lim.addProposal( transactionID, this );

                status = OK + DELIMITER + SUCCESS;
                message = status + DELIMITER + transactionID + NEWLINE;
            }
            else
            {
                status = ERROR + DELIMITER + PROPOSEREJECTED;
                message = status + DELIMITER + transactionID + DELIMITER + 
                    error + NEWLINE;
            } 
        }
    }

    /**
     * Execute a command
     */
    private void execute()
    {
        Command propose = Lim.getProposal( transactionID );

        if ( propose == null )
        {
            log.error( "Proposal not found for transaction ID: " + transactionID );

            status = ERROR + DELIMITER + NOTRANSACTIONID;
            message = status + DELIMITER + transactionID + DELIMITER + 
                "TransactionID not proposed" + NEWLINE;

            return;
        }

        StringTokenizer st = new StringTokenizer( propose.toString() );
        String error = null;  // this might need to be a member
        String name;
        String tmp;
        
        tmp = st.nextToken();     // Discard verb
        tmp = st.nextToken();     // Discard transaction ID
        name = st.nextToken();    // Get name

        String args = "";

        while ( st.hasMoreTokens() )
        {
            args += st.nextToken() + DELIMITER;
        }

        if ( name.equals( "camera" ) )
        {
            log.debug( "Generic control point detected" );
            
            String execArgs[] = {"/bin/sh", "echo" };
            
            // TODO try this!
            // See http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html
            try {
                Runtime.getRuntime().exec(execArgs);
            }catch (java.io.IOException ioe) {
                log.error("Error running " + execArgs);
                return;
            }

            // Spawn off threads for each camera
            for ( int i = 1; i <= Lim.getNumberOfCameras(); i++ )
            {
                ExecutionThread et = new ExecutionThread( name + i , args );
                et.start();
            }

            // Wait until all threads are done to return
            /* OMITTED TO SPEED THINGS UP, DRAWBACK IS NO ERROR MESSAGES
            for ( int i = 1; i <= JCamera.getNumberOfCameras(); i++ )
            {
                Camera c = JCamera.getCamera( "camera" + i );
                while ( c.locked() )
                {
                    log.debug( "Waiting for camera" + i + " to finish" );
                    try { Thread.sleep( 500 ); } catch ( Exception e ) { }
                     
                }
            }
            */
        }
        else
        {
            ExecutionThread et = new ExecutionThread( name, args );
            et.start();

            // Wait for the camera to finish before returning
            /* OMITTED TO SPEED THINGS UP, DRAWBACK IS NO ERROR MESSAGES
            Camera c = JCamera.getCamera( name );

            while ( c.locked() )
            {
                log.debug( "Waiting for " + name + " to finish" );
                try { Thread.sleep( 500 ); } catch ( Exception e ) { }
            }
            */
        }

        if ( error == null )
        {
            status = OK + DELIMITER + SUCCESS;
            message = status + DELIMITER + transactionID + NEWLINE;
        }
        else
        {
            status = ERROR + DELIMITER + EXECUTEFAILED;
            message = status + DELIMITER + transactionID + DELIMITER + 
                error + NEWLINE;
        } 
    }

    /**
     * This class is the thread in which the execution takes place.
     */
    class ExecutionThread extends Thread
    {
        //~ Instance fields ----------------------------------------------------

        Camera camera;
        String name;
        String error;
        StringTokenizer args;

        //~ Constructors -------------------------------------------------------

        public ExecutionThread( String name, String args )
        {
            this.name = name;
            this.args = new StringTokenizer( args );
            camera = Lim.getCamera( name );
            camera.lock();
        }

        //~ Methods ------------------------------------------------------------

        public void run()
        {
            camera = Lim.getCamera( name );
            camera.lock();
            
            while ( args.hasMoreTokens() )
            {
                String parameter = args.nextToken();
                String type = args.nextToken();
                String value = args.nextToken();

                switch ( parameter.charAt( 0 ) )
                {
                    case 'x':
                        break;

                    case 'y':
                        break;

                    case 'z':
                        Float f = new Float( value );
                        if ( camera.getFocalLength() != f.intValue() )
                        {
                            camera.setFocalLength( f.intValue() );
                        }
                        break;

                    default:
                        error = "Unknwon GeomAxisType";
                }
            }

            try
            {
                // Take a picture
                camera.capture();

                // upload file to data turbine
                DataTurbine dt = Lim.getTurbine( name );
                dt.upload( camera );

                // Delete it from the camera
                camera.deleteRecentImage();
                
            }
            catch ( UnsupportedOperationException uoe )
            {
                error = "Device does not support operation: " + uoe.getMessage();
                log.error( error );
            }
            catch ( SecurityException se )
            {
                error = "Security exception: " + se.getMessage();
                log.error( error );
            } catch (SAPIException e) {
                error = "Data Turbine exception: " + e.getMessage();
                log.error( error );
            } catch (IOException e) {
                error = "File exception: " + e.getMessage();
                log.error( error );
            } 
            if ( error == null )
            {
                status = OK + DELIMITER + SUCCESS;
                message = status + DELIMITER + transactionID + NEWLINE;
            }
            else
            {
                status = ERROR + DELIMITER + EXECUTEFAILED;
                message = status + DELIMITER + transactionID + DELIMITER + 
                    error + NEWLINE;
            }

            camera.unlock();
        }
    }
}
