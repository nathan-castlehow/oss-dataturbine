/*
 * File Main.java in package adxl-kml
 * Author Paul Hubbard
 * Date Dec 15 2008
 * Starting from the 'tx-skeleton' package, adding KML support via fixed
 * lat/long/elevation.
 * Reads the Crossbow ADXL202EVB 2-axis accelerometer board via a serial
 * port at roughly 50Hz using the RXTX serial library.

 * Quite crude. All hardwired. Known bugs.
 * 
 * Notes: Quite a lot of code cut and pasted from RXTX example and my own
 * sms-rbnb.java
 */

package txskeleton;

import com.rbnb.sapi.*;
import gnu.io.*;
import java.io.*;
import java.util.*;

class Location
{
    static double latitude, longitude, elevation;
    long seed = 10000;
    Random r;

    public Location() {
      r = new Random(seed);
    }

    public static void initialize(double latitude, double longitude, double elevation)
    {
        // Here we would open the gps (or gpsd) device, seed with last pos, etc
        Location.latitude = latitude;
        Location.longitude = longitude;
        Location.elevation = elevation;

        return;
    }

    public double getLat() {

        double d =	r.nextDouble();
        latitude = latitude + (d - 0.5) ;
        return latitude ;
    }
    public double getLong() {

        double d =	r.nextDouble();
        longitude = longitude + (d - 0.5);
        return longitude;
    }
    public double getElev() {return elevation;}

}
class ADXL
{

    static CommPortIdentifier portId;
    static Enumeration portList;
    static SerialPort serialPort = null;
    static InputStream inputStream;
    static OutputStream outputStream;
    Thread readThread;
    int numBytes = 0;
    String outData = "";
    String outDataBuff = "";
    boolean running = true;
    boolean process = true;
    boolean waitForInput = true;
    boolean fatalErr = false;
    String errMessage = "";

    /*
     * Open the port, wait for chip init
     */
    public static int initialize(String portName)
    {
        System.out.println("Enumerating serial ports:");
        portList = CommPortIdentifier.getPortIdentifiers();
        while (portList.hasMoreElements())
        {
            portId = (CommPortIdentifier) portList.nextElement();
            if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL)
            {
                // DDT
                System.out.println(portId.getName());

                if (portId.getName().equals(portName))
                {
                    System.out.println("Name match on port " + portName);
                    // Init the port, matches name we were given
                    try
                    {
                        serialPort = (SerialPort) portId.open("RXTX-RBNB", 64);
                    } catch (PortInUseException e)
                    {
                        System.out.println("Port in use!");
                        return(4);
                    }
                    try
                    {
                        inputStream = serialPort.getInputStream();
                        outputStream = serialPort.getOutputStream();
                    } catch (IOException e)
                    {
                        System.out.println("Unable to connect to I/O streams");
                        return (3);
                    }

                    try
                    {
                        System.out.println("Initializing ADXL202 board...");
                        // 38400N81
                        serialPort.setSerialPortParams(38400, SerialPort.DATABITS_8,
                                SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

                        // ADXL202 is powered via DTR line, so set it on
                        serialPort.setDTR(true);

                        try
                        {
                            // Wait a couple of seconds for chip to initialize (blinking LED)
                            Thread.sleep(2000);
                        } catch (InterruptedException ie)
                        {
                        }

                    } catch (UnsupportedCommOperationException e)
                    {
                        System.out.println("Unable to configure serial port!");
                        return (1);
                    }
                    return (0);
                }
            }
        }

        return (1);


    }

    /*
     * Read a single pair of (X,Y) values from the ADXL202EVB board.
     * TODO: Better return value, or declare as throwing exception for errs
     */
    public static double[] chipRead()
    {
        double[] retval = {0.0, 0.0};
        byte[] rawBuf = {0, 0, 0, 0};
        int[] vals = {0, 0};

        try
        {
            // Read is triggered by sending 'G' to board
            outputStream.write('G');

            // Wait for all 4 result bytes to arrive
            // TODO: Finite wait w/error return if data never arrives
            while (inputStream.available() < 4)
            {
                Thread.sleep(1);
            }

            inputStream.read(rawBuf, 0, 4);

            // Convert from raw bytes to 16-bit signed integers, carefully
            Byte bTmp = new Byte(rawBuf[0]);
            vals[0] = bTmp.intValue() * 256;
            bTmp = new Byte(rawBuf[1]);
            vals[0] += bTmp.intValue();

            bTmp = new Byte(rawBuf[2]);
            vals[1] = bTmp.intValue() * 256;
            bTmp = new Byte(rawBuf[3]);
            vals[1] += bTmp.intValue();

            // See ADXL202EVB specs for details on conversion
            retval[0] = (((vals[0] / 100.0) - 50.0) / 12.5);
            retval[1] = (((vals[1] / 100.0) - 50.0) / 12.5);

//            System.out.println("X: " + retval[0] + " Y: " + retval[1]);

        } catch (Exception ioe)
        {
            System.out.println("Error on data transmission");
        }
        return (retval);
    }

    /*
     * Empty constructor for now
     */
    public ADXL()
    {

    }

    /*
     * Code cribbed from RXTX example
     */
    public static void closePort(SerialPort serialPort)
    {
        if (serialPort != null)
        {
            serialPort.notifyOnDataAvailable(false);
            serialPort.removeEventListener();
            if (inputStream != null)
            {
                try
                {
                    inputStream.close();
                    inputStream = null;
                } catch (IOException e)
                {
                }
            }
            if (outputStream != null)
            {
                try
                {
                    outputStream.close();
                    outputStream = null;
                } catch (IOException e)
                {
                }
            }
            serialPort.close();
            serialPort = null;
        }
    }
}

/**
 *
 * @author hubbard
 */
public class Main
{

    public static boolean ctrlC;
    private static boolean DTconnected = false;
    private static boolean portConnected = false;

    /* Traps control-c
     */
    private static void DoHook()
    {
        ctrlC = false;

        /* Add in a hook for ctrl-c's and other abrupt death */
        Runtime.getRuntime().addShutdownHook(new Thread()
        {

            public void run()
            {
                ctrlC = true;
                System.out.println("Shutdown hook for " + Main.class.getName());
            } // run ()
        }); // addHook
    }

    
    /**
     * @param args the command line arguments
     */
    @SuppressWarnings("static-access")
    public static void main(String[] args)
    {
        String hostname = "localhost:3333";
        String portName = "/dev/tty.KeySerial1";
        String srcName = "ADXL-RBNB-GEO";
        String[] chanNames = {"X", "Y", "Lat", "Lon", "Alt"};
        int[] chanIdx = {0, 1};
        double[] vals = {0, 0};
        String unitsMetadata = "units=G,scale=1,offset=0";
        String LLMetadata = "units=degrees";
        String AltMetadata = "units=meters";
        Source source;
        ChannelMap cmap;
        int cacheSize = 10240;               //! @todo parse from command line
        int archiveSize = cacheSize * 10;    //! @todo parse from command line        
        int idx;
        int chanCount = 2;
        ADXL  chip;
        Location whereami;

        // Setup interrupt handler
        DoHook();

        System.out.println("Opening serial port");  
        chip = new ADXL();
        if (chip.initialize(portName) == 0)
        {
            portConnected = true;
            System.out.println("Serial port initialized OK");
        } else
        {
            System.out.println("Error opening serial port");
            return;
        }

        /* @Note: Lat/long from google maps for my office. 
        See http://www.accessgrid.org/node/767
         */
        whereami = new Location();
        whereami.initialize(37.0625, -95.677068, 0);
        
        // RBNB connection setup
        try
        {
            System.out.println("Opening connection to RBNB on " + hostname);
            // Create both a source and a sink, connect both:
            source = new Source(cacheSize, "append", archiveSize);
            source.OpenRBNBConnection(hostname, srcName);
            DTconnected = true;

            System.out.println("OK.");

            // Setup channel map - names of channels, units
            cmap = new ChannelMap();
            for (idx = 0; idx < chanCount; idx++)
            {
                chanIdx[idx] = cmap.Add(chanNames[idx]);

                // Hardwired units (G) for all three sources
                cmap.PutUserInfo(chanIdx[idx], unitsMetadata);

                // Not sure if we still need the MIME type or not
                cmap.PutMime(chanIdx[idx], "application/octet-stream");
            }
            // Lat
            idx =  cmap.Add(chanNames[2]);
            cmap.PutUserInfo(idx, LLMetadata);
            
            // Long
            idx =  cmap.Add(chanNames[3]);
            cmap.PutUserInfo(idx, LLMetadata);
            
            // Alt/elevation
            idx = cmap.Add(chanNames[4]);
            cmap.PutUserInfo(idx, AltMetadata);
          
            source.Register(cmap);
            source.Flush(cmap);

        } // We don't distinguish between errors in setup phase, just bail out
        catch (SAPIException se)
        {
            se.printStackTrace();
            System.out.println("Error on Turbine - not connected");
            DTconnected = false;
            chip.closePort(chip.serialPort);
            
            return;
        }
        
        // ********************************************************************
        // Main data loop: read, scale, write.
        try
        {
            System.out.println("Turbine connected, running. Press control-c to end");

            // Loop - runs until control-c or error.
            do
            {
                for (idx = 0; idx < chanCount; idx++)
                    chanIdx[idx] = cmap.Add(chanNames[idx]); 
                
                // Read the data from the accelerometer
                vals = chip.chipRead();

                // Timestamp all channels with client-side time
                cmap.PutTimeAuto("timeofday");

                for (idx = 0; idx < chanCount; idx++)
                {
                    double valBuf[] = {vals[idx]};
                    cmap.PutDataAsFloat64(cmap.GetIndex(chanNames[idx]), valBuf);
                }

                int latIdx = cmap.Add(chanNames[2]);
                int longIdx = cmap.Add(chanNames[3]);
                int altIdx = cmap.Add(chanNames[4]);

                cmap.PutTimeAuto("timeofday");

                double valBuf[] = {whereami.getLat()};
                cmap.PutDataAsFloat64(latIdx, valBuf);

                double valBuf2[] = {whereami.getLong()};
                cmap.PutDataAsFloat64(longIdx, valBuf2);

                double valBuf3[] = {whereami.getElev()};
                cmap.PutDataAsFloat64(altIdx, valBuf3);
                
                source.Flush(cmap);

                cmap.Clear();

                Thread.sleep(20);
            } while (!ctrlC);
        } catch (SAPIException mse)
        {
            System.out.println("Error saving data!");
            mse.printStackTrace();
        } catch (InterruptedException ie)
        {
            System.out.println("Interrupted, exiting.");
        }

        // Shutdown and exit
        if (portConnected)
        {
            chip.closePort(chip.serialPort);
        }
        if (DTconnected)
        {
            System.out.println("Closing RBNB connection");

            // Tell RBNB to keep the data once we close
            source.Detach();

            source.CloseRBNBConnection();
        }
        System.out.println("Done, exiting.");

        return;
    }
}
