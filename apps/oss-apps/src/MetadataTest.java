/*
 * MetadataTest.java
 * Created on July 27, 2005
 * 
 * COPYRIGHT 2005, THE REGENTS OF THE UNIVERSITY OF MICHIGAN,
 * ALL RIGHTS RESERVED; see the file COPYRIGHT.txt in this folder for details
 * 
 * This work is supported in part by the George E. Brown, Jr. Network
 * for Earthquake Engineering Simulation (NEES) Program of the National
 * Science Foundation under Award Numbers CMS-0117853 and CMS-0402490.
 * 
 * CVS information...
 *   $Revision$
 *   $Date$
 *   $RCSfile: MetadataTest.java,v $ 
 * 
 */

import java.util.Properties;
import java.util.Enumeration;

import org.nees.rbnb.ChannelUtility;

import com.rbnb.sapi.*;

/**
 * @author Terry E. Weymouth
 */
public class MetadataTest {

    private final static String SERVER_NAME = "neestpm.sdsc.edu:3333";
//    private final static String SERVER_NAME = "localhost:3333";
    private final static String SOURCE_NAME = "MetadataTestSource";
    
    private final static String CHANNEL_A = "channel_a";
    private final static String CHANNEL_B = "channel_b";
    
    private final static long THREAD_SLEEP_TIME = 1*1000; // times one second

    static final String CVS_INFO = "CVS Information \n" +
        "  $Revision$ \n" +
        "  $Date$ \n" +
        "  $RCSfile: MetadataTest.java,v $"; 

    public static void main(String[] args)
    {
        System.out.println(CVS_INFO);
        MetadataTest mt = new MetadataTest();
        try
        {
            mt.exec();
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }
    }

    private void exec() throws Throwable
    {
        // Connect a source and set up channes
        Source source = new Source();
        source.OpenRBNBConnection(SERVER_NAME,SOURCE_NAME);

        // set up metadata for each channel
        Properties a = new Properties();
        a.setProperty("name",CHANNEL_A);
        a.setProperty("sampleRate","10");
        a.setProperty("description","This is channel a");
        a.setProperty("type","image");
        a.setProperty("height","256");
        a.setProperty("width","534");
        
        Properties b = new Properties();
        b.setProperty("name",CHANNEL_B);
        b.setProperty("sampleRate","1");
        b.setProperty("description","This is channel b");
        b.setProperty("type","flaot");
        b.setProperty("unit","mm");
        b.setProperty("units","mm");
        
        // post metadata
        org.nees.rbnb.ChannelUtility.sendMetadata(source,CHANNEL_A,a);
        org.nees.rbnb.ChannelUtility.sendMetadata(source,CHANNEL_B,b);
        
        // retreive metatdata
        String channelPath = SOURCE_NAME + "/" + CHANNEL_A;
        Properties geta =
            org.nees.rbnb.ChannelUtility.getMetadata(SERVER_NAME,channelPath);      

        channelPath = SOURCE_NAME + "/" + CHANNEL_B;
        Properties getb =
            org.nees.rbnb.ChannelUtility.getMetadata(SERVER_NAME,channelPath);      

        // print metadata
        String key, value;

        System.out.println("The Metadata for " + CHANNEL_A + "...");
        Enumeration keys = geta.keys();
        while (keys.hasMoreElements())
        {
            key = (String)keys.nextElement();
            value = geta.getProperty(key);
            System.out.println("  " + key + ": " + value);
        }
        
        System.out.println("The Metadata for " + CHANNEL_B + "...");
        keys = getb.keys();
        while (keys.hasMoreElements())
        {
            key = (String)keys.nextElement();
            value = getb.getProperty(key);
            System.out.println("  " + key + ": " + value);
        }

        // drop soruce
        source.CloseRBNBConnection();

    }
}
