package tfri;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

import javax.imageio.ImageIO;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import rbnb.HTTPMultipartReader;
import rbnb.HTTPPacket;


import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Source;

/**
 * GenericImageSource.java
 * Created: Dec 7, 2009
 * @author Michael Nekrasov
 * @version 0.8
 * 
 * Description: Takes any URL of an image and PULLS it into RBNB at the specified
 *				frequency.
 */
public class GenericImageSource implements Runnable{	

	/** Do not print messages (used when specifying logging level) */
	public static final int NONE =0;
	
	/** Print Only Error Messages (used when specifying logging level) */
	public static final int ERRORS = 1;
	
	/** Print Error And Connection Messages (used when specifying logging level) */
	public static final int CONNECTION =2;
	
	/** Print All Messages (used when specifying logging level) */
	public static final int ALL=3; 
	
	/** Defaults to 10240 frames*/
	public static final int DEFAULT_CACHESIZE = 10240;
	/** Defaults to DEFAULT_CACHESIZE * 10 */
	public static final int DEFAULT_ARCHIVESIZE = DEFAULT_CACHESIZE*10;
	/** Defaults to 1000 milliseconds (1 second) */
	public static final int DEFAULT_FREQUENCY = 1000;
	/** Defaults to 1000 milliseconds (1 second) */
	public static final int DEFAULT_RETRYFREQUENCY = 1000;
	/** Defaults to CONNECTION level logging*/
	public static final int DEFAULT_REPORTLEVEL = ALL;
	/** Defaults to local host on default port*/
	public static final String DEFAULT_SERVER = "localhost";

    private final int reportLevel;
	private final Source source;
	private final long frequencyOfUpdate, retryFrequency;
	private final ChannelMap cmap;
	private final int ch_Img;
	private final URL imageURL;
	
	/**
	 * Create a new Source with default values
	 * @param imageURL URL of image to pull
	 * @param srcName What to name the RBNB Source
	 * @throws SAPIException if RBNB failure occurs during connection
	 * @throws MalformedURLException if URL is badly formated
	 */
	public GenericImageSource(String imageURL, String srcName) throws SAPIException, MalformedURLException{
		this(imageURL, srcName, DEFAULT_SERVER, DEFAULT_CACHESIZE, DEFAULT_ARCHIVESIZE, DEFAULT_FREQUENCY);
	}
	
	/**
	 * Creates a new source with fully configured parameters
	 * @param imageURL URL of image to pull
	 * @param srcName What to name the RBNB Source
	 * @param rbnbHostName Path to RBNB server (including port denoted by path:port)
	 * @param cacheSize the number of frames to keep in the cache for rapid access
	 * @param archiveSize the number of frames to keep in archival storage
	 * @param frequencyOfUpdate the frequency (in milliseconds to pull images at)
	 * @throws SAPIException if RBNB failure occurs during connection
	 * @throws MalformedURLException if URL is badly formated
	 */
	public GenericImageSource(String imageURL, String srcName, String rbnbHostName,
								int cacheSize, int archiveSize, long frequencyOfUpdate
								) throws MalformedURLException, SAPIException {
		this(imageURL, srcName, rbnbHostName, cacheSize, archiveSize,
				frequencyOfUpdate, DEFAULT_REPORTLEVEL, DEFAULT_RETRYFREQUENCY);
	}
	
	/**
	 * Creates a new source with fully configured parameters
	 * @param imageURL URL of image to pull
	 * @param srcName What to name the RBNB Source
	 * @param rbnbHostName Path to RBNB server (including port denoted by path:port)
	 * @param cacheSize the number of frames to keep in the cache for rapid access
	 * @param archiveSize the number of frames to keep in archival storage
	 * @param frequencyOfUpdate the frequency (in milliseconds to pull images at)
	 * @param reportLevel the level of message reporting (see static variables)
	 * @param retryFrequency Rate at which the the source (endlessly) retry on placing image into RBNB or 0 if it should not retry 
	 * @throws SAPIException if RBNB failure occurs during connection
	 * @throws MalformedURLException if URL is badly formated
	 */
	public GenericImageSource(String imageURL, String srcName, String rbnbHostName,
								int cacheSize, int archiveSize, long frequencyOfUpdate,
								int reportLevel, long retryFrequency)
								throws SAPIException, MalformedURLException {
		this.imageURL = new URL(imageURL);
		this.frequencyOfUpdate = frequencyOfUpdate;
		this.reportLevel = reportLevel;
		
		this.retryFrequency = retryFrequency;
		source = new Source(cacheSize, "append", archiveSize);
		
		print("Starting RBNB Connection to \"" + rbnbHostName +"\"", CONNECTION);
		source.OpenRBNBConnection(rbnbHostName, srcName);
		print("Succesfully Connected to \"" + rbnbHostName + "\" as \"" +
				source.GetClientName()+ "\"", CONNECTION);
       
        cmap = new ChannelMap();
        ch_Img = cmap.Add("Image");
        cmap.PutMime(ch_Img, "image/jpeg");
	}

	

	/**
	 * Put Image into RBNB 
	 * @param img to put into RBNB
	 * @param timestamp to assign Image
	 * @throws IOException if problem reading image
	 * @throws SAPIException if problem with RBNB
	 */
	private void putImage(BufferedImage img, double timestamp) throws IOException, SAPIException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		ImageIO.write(img,"jpg",out);
		
		double[] times = {timestamp};
		cmap.PutTimes(times);
	    cmap.PutDataAsByteArray(ch_Img, out.toByteArray());
	    source.Flush(cmap, true);
		  
	}
	
	/**
	 * Close the Connection to RBNB
	 */
	public void close(){
		// Tell RBNB to keep the data once we close
		source.Detach();
        source.CloseRBNBConnection();
	}
	
	
	private void readPulled(){
		System.out.println("Reading as Pull Source.");
		while(true){
			try {
				BufferedImage image = ImageIO.read(imageURL);
				long timestamp = System.currentTimeMillis();
				putImage(image, timestamp/1000);

				//Overwrite the line and replace with new put
				if(reportLevel >= ALL) System.out.print(
						"Put \"" + imageURL.getPath() +"\" ("+
						image.getWidth()+"x"+image.getHeight()+") on "
						+ new Date(timestamp) + "\r");
				
				Thread.sleep(frequencyOfUpdate);
			} catch (InterruptedException e) {
				break;
			} catch(IllegalArgumentException e1){
				print("Error accessing Image at this location!", ERRORS);
				if(retryFrequency <= 0) break;
				else print("Retrying...", ERRORS);
				try {Thread.sleep(retryFrequency);} catch (Exception e) {}
			} catch (IOException e1) {
				print("Error accessing Image at this location!", ERRORS);
				if(retryFrequency <= 0) break;
				else print("Retrying...", ERRORS);
				try {Thread.sleep(retryFrequency);} catch (Exception e) {}
			} catch (SAPIException e1) {
				print("Error accessing RBNB!", ERRORS);
				if(retryFrequency <= 0) break;
				else print("Retrying...", ERRORS);
				try {Thread.sleep(retryFrequency);} catch (Exception e) {}
			}
		}
	}
	
	private void readPushed(URLConnection connection) throws IOException{
		System.out.println("Reading as Push Source.");
		
		HTTPMultipartReader reader = new HTTPMultipartReader(connection);
		while(true){
			try{
				HTTPPacket packet = reader.readPacket();
				cmap.PutTimes(new double[]{System.currentTimeMillis()/1000});
			    cmap.PutDataAsByteArray(ch_Img, packet.getData());
			    source.Flush(cmap, true);
			    
			    //Overwrite the line and replace with new put
				if(reportLevel >= ALL) System.out.print(
						"Put \"" + imageURL.getPath() +"\" \r");
				
			}catch(Exception e){
				System.err.println("Error loading image:"+ e.getMessage());
			}
		}
	}
	/**
	 *  Start placing images into RBNB
	 */
	@Override
	public void run() {
		print("\nNow Loading Images from \"" + imageURL.getPath() +"\" as \""
				+ cmap.GetName(ch_Img)+"\" in \"" + source.GetClientName(), CONNECTION);
		
		try {
			URLConnection connection = imageURL.openConnection();
			
			if(HTTPMultipartReader.isMultipart(connection))
				readPushed(connection);
			else
				readPulled();
			
		} catch (IOException e) {
			print("Error accessing Image at this location to determine type!", ERRORS);
		}
		
		print("Terminating Connection", CONNECTION);
		close();
	}
	
	/**
	 * Helper function that prints messages with specified level
	 * @param msg to print
	 */
	private void print(String msg, int level){
		if(reportLevel >= level)
			System.out.println(msg);
	}
	
	/**
	 * Executes the Generic Image Source
	 * @param args command line parameters
	 */
	public static void main(String[] args){
		
	    try {
	        CommandLine cmd = new GnuParser().parse( getOptions(), args );
	        
	        if(cmd.hasOption('h')){
	        	printHelp("");
	        	System.exit(0);
	        }
	    
	        String imageURL = cmd.getOptionValue('u');
	        String srcName = cmd.getOptionValue('n');
	        String rbnbHostName = cmd.getOptionValue('s', DEFAULT_SERVER);
			int cacheSize = Integer.parseInt(cmd.getOptionValue('c', DEFAULT_CACHESIZE+""));
			int archiveSize = Integer.parseInt(cmd.getOptionValue('a', DEFAULT_ARCHIVESIZE+""));
			long frequencyOfUpdate = Long.parseLong(cmd.getOptionValue('f',DEFAULT_FREQUENCY+""));
			int reportLevel = Integer.parseInt(cmd.getOptionValue('l', DEFAULT_REPORTLEVEL+""));
			long retryFrequency = Long.parseLong(cmd.getOptionValue('r',DEFAULT_RETRYFREQUENCY+""));
	        GenericImageSource camera = new GenericImageSource(imageURL, srcName, rbnbHostName, cacheSize, archiveSize, frequencyOfUpdate, reportLevel,  retryFrequency);
	        camera.run();
			
	    }
	    catch( ParseException e ) {
	    	printHelp(e.getMessage());
	    }
	    catch(NumberFormatException e){
	    	printHelp("Invalid Number " + e.getMessage());
	    } catch (SAPIException e) {
	    	printHelp("Failure connecting to the server, please check server path " +
	    			"and port and make sure the server is running");
		} catch (MalformedURLException e) {
			printHelp("Invalid URL: " + e.getMessage());
		}
	}
	
	/**
	 * Makes a set of command Line options that can be parsed using the Apache Cli library
	 */
	@SuppressWarnings("static-access")
	public static Options getOptions(){
		Options options = new Options();

		options.addOption(  OptionBuilder.withLongOpt("url").isRequired()
                			.withDescription("The URL of the image to put into RBNB *REQUIRED*" )
                			.hasArgs(1).withArgName( "image url" )
                			.create('u')
						
		);
		
		options.addOption(  OptionBuilder.withLongOpt("name").isRequired()
			    			.withDescription("Name of source to as it will appear in RBNB *REQUIRED*" )
			    			.hasArgs(1).withArgName( "source name" )
			    			.create('n')
		);
		options.addOption(  OptionBuilder.withLongOpt("cache")
			    			.withDescription("Number of frames to store in Cache (memory)" +
			    							 " [Default to \"" +DEFAULT_CACHESIZE +"\" frames]" )
			    			.hasArgs(1).withArgName( "cache size" )
			    			.create('c')
		);
		options.addOption(  OptionBuilder.withLongOpt("archive")
			    			.withDescription("Number of frames to store in archive (disk))" +
			    							 " [Default to \"" +DEFAULT_ARCHIVESIZE +"\" frames]" )
			    			.hasArgs(1).withArgName( "archive size" )
			    			.create('a')
		);
		options.addOption(  OptionBuilder.withLongOpt("server")
							.withDescription("The url to the RBNB server and the port"  +
									 		 " [Default to \"" +DEFAULT_SERVER +"\"]" )
							.hasArgs(1).withArgName( "server:port" )
							.create('s')
		);		
		options.addOption(  OptionBuilder.withLongOpt("message")
				.withDescription("Level of error messages" +
								" [Default to \"" +DEFAULT_REPORTLEVEL +"\"]" )
				.hasArgs(1).withArgName( "0-3" )
				.create('l')
		);	
		options.addOption(  OptionBuilder.withLongOpt("frequency")
				.withDescription("Frequency (in milleseconds) to fetch images"  +
								 " [Default to \"" +DEFAULT_FREQUENCY +"\" milliseconds]" )
				.hasArgs(1).withArgName( "freq" )
				.create('f')
		);	
		options.addOption(  OptionBuilder.withLongOpt("retry")
				.withDescription("Frequency (in milliseconds) to attempt to retry upon failure or 0 to not retry" +
								" [Default to \"" +DEFAULT_RETRYFREQUENCY +"\" milliseconds]" )
				.hasArgs(1).withArgName( "freq" )
				.create('r')
		);
		options.addOption(  OptionBuilder.withLongOpt("help")
				.withDescription("Print Help Message" )
				.create('h')
		);
		return options;
	}

	/**
	 * Prints usage info
	 */
	public static void printHelp(String errormsg){
		if(!errormsg.equals("")) System.out.println(errormsg+"\n");
		new HelpFormatter().printHelp("GenericImageSource", getOptions());
	}

}

