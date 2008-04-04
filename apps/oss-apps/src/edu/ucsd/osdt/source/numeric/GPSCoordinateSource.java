package edu.ucsd.osdt.source.numeric;

/*!
 * @author Lawrence J. Miller <ljmiller@sdsc.edu>
 * @author Cyberinfrastructure Laboratory for Environmental Observing Systems (CLEOS)
 * @author San Diego Supercomputer Center (SDSC)
 * @since $LastChangedDate: 2007-12-17 16:13:22 -0800 (Mon, 17 Dec 2007) $
 * $LastChangedRevision: 208 $
 * @author $LastChangedBy: ljmiller $
 * $HeadURL: file:///Users/hubbard/code/cleos-svn/cleos-rbnb-apps/trunk/src/edu/sdsc/cleos/GPSCoordinateSource.java $
 */

/*! @class a classs that will add channels required by the RBNB KML plugin to a specified
  * RBNB Source and channel name */

import edu.ucsd.osdt.util.RBNBBase;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.Source;
import java.util.HashMap;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

public class GPSCoordinateSource extends RBNBBase {

	protected String DEFAULT_TARGET_RBNB_SOURCE_NAME = "coordinateTarget";
	protected HashMap<String, String> coordinateValueHash;
		public HashMap getCoordinateHash() {return this.coordinateValueHash;}
	protected static String[] coordinateChannelNames =
		{"Alt", "Lat", "Lon", "TrackID", "Type", "Classification", "Speed", "Heading"};
	private static Logger logger = Logger.getLogger(GPSCoordinateSource.class.getName());
	
	/*! constructor */
	public GPSCoordinateSource() {
		super();
		coordinateValueHash = new HashMap();
	}
	
	public void putToHash(String hashKey, String hashValue) throws IllegalArgumentException {
		// validate the key
		for(int i=0; i<coordinateChannelNames.length; i++) {
			if(hashKey.compareTo(coordinateChannelNames[i]) == 0) { // then the key is valid
				break;
			} else if(i==coordinateChannelNames.length - 1) { // then key is not valid (not in the list)
				throw new IllegalArgumentException("Invalid coordinate key");
			}
		} // for
		
		coordinateValueHash.put(hashKey, hashValue);
		
	} // putToHash()
	
	/** @note required by interface RBNBBase */
	protected String getCVSVersionString() {
		return getSVNVersionString();
	}
	
	/** @note svn keywords */
	protected String getSVNVersionString() {
		return (
	             "$LastChangedDate: 2007-12-17 16:13:22 -0800 (Mon, 17 Dec 2007) $\n" +
	             "$LastChangedRevision: 208 $\n" +
	             "$LastChangedBy: ljmiller $\n" +
	             "$HeadURL: file:///Users/hubbard/code/cleos-svn/cleos-rbnb-apps/trunk/src/edu/sdsc/cleos/GPSCoordinateSource.java $"
	              );
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		GPSCoordinateSource coordinateSource = new GPSCoordinateSource();
		if (coordinateSource.parseArgs(args)) {
			// do some stuff
		} else System.exit(0);
	} // main()
	
	/** @note required by interface RBNBBase */
	protected Options setOptions() {
		Options opt = setBaseOptions(new Options()); // uses h, v, s, p
		
		opt.addOption("t", true, " target RBNB source name *" + DEFAULT_TARGET_RBNB_SOURCE_NAME);
		opt.addOption("c", true, " target channel name in source");
		
		opt.addOption("a", true, " alt");
		opt.addOption("l", true, " lat");
		opt.addOption("L", true, " long");
		opt.addOption("i", true, " track ID");
		opt.addOption("T", true, " type");
		opt.addOption("C", true, " classification");
		opt.addOption("S", true, " speed");
		opt.addOption("H", true, " heading");
		
		return opt;
	} // setOptions()
	
	/** @note required by interface RBNBBase */
	protected boolean setArgs(CommandLine cmd) {
		if (!setBaseArgs(cmd)) return false;

		if (cmd.hasOption('t')) { // target source name
			String v = cmd.getOptionValue("t");
			logger.info("t=" + v);
		}
		if (cmd.hasOption('c')) { // target channel name
			String v = cmd.getOptionValue("c");
			logger.info("c=" + v);
		}
		if (cmd.hasOption('a')) { // alt
			String v = cmd.getOptionValue("a");
			logger.info("a=" + v);
		}
		if (cmd.hasOption('l')) { // lat
			String v = cmd.getOptionValue("l");
			logger.info("l=" + v);
		}
		if (cmd.hasOption('L')) { // long
			String v = cmd.getOptionValue("L");
			logger.info("L=" + v);
		}
		if (cmd.hasOption('i')) { // track ID
			String v = cmd.getOptionValue("i");
			logger.info("i=" + v);
		}
		if (cmd.hasOption('T')) { // type
			String v = cmd.getOptionValue("T");
			logger.info("T=" + v);
		}
		if (cmd.hasOption('C')) { // classification
			String v = cmd.getOptionValue("C");
			logger.info("C=" + v);
		}
		if (cmd.hasOption('S')) { // speed
			String v = cmd.getOptionValue("S");
			logger.info("S=" + v);
		}
		if (cmd.hasOption('H')) { // heading
			String v = cmd.getOptionValue("H");
			logger.info("H=" + v);
		}
		
		return true;
	} // setArgs()
} // class
