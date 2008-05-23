/*!
 * @file testLoggerNetParser.java
 * @author Lawrence J. Miller <ljmiller@sdsc.edu>
 * @author $LastChangedBy: ljmiller.ucsd $
 * @author Cyberinfrastructure Laboratory for Environmental Observing Systems (CLEOS)
 * @author San Diego Supercomputer Center (SDSC)
 * @date $LastChangedDate: 2008-05-20 08:48:42 -0700 (Tue, 20 May 2008) $
 * @version $LastChangedRevision: 57 $
 * @note $HeadURL: https://oss-dataturbine.googlecode.com/svn/trunk/apps/oss-apps/src/edu/ucsd/osdt/source/numeric/LoggerNetSource.java $
 */
package edu.ucsd.osdt.test;

import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;
import edu.ucsd.osdt.source.numeric.LoggerNetParser;
import java.io.IOException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class testLoggerNetParser {
	private LoggerNetParser aParser = null;
	private static String loggernetHeader1 = "\"TIMESTAMP\",\"RECORD\",\"AirTemp_C_Max\",\"AirTemp_C_TMx\",\"AirTemp_C_Min\",\"AirTemp_C_TMn\",\"AirTemp_C_Avg\",\"RH_Avg\",\"Rain_mm_Tot\",\"WindSp_ms_Max\",\"WindSp_ms_TMx\",\"BP_mbar_Avg\",\"SoilTemp_C_Avg\",\"SoilWVC_Avg\"";
	private static String loggernetHeader2	= "\"TS\",\"RN\",\"Deg C\",\"Deg C\",\"Deg C\",\"Deg C\",\"Deg C\",\"%\",\"mm\",\"meters/second\",\"meters/second\",\"mbar\",\"Deg C\",\"%\"";
	
	@Before public void setUp() {
		aParser = new LoggerNetParser();
		try {
			aParser.parse(loggernetHeader1 + "\n" + loggernetHeader2 + "\n");
		} catch(IOException ioe) {
			Assert.fail("Got IO Exception: " + ioe);
		} catch(SAPIException sae) {
			Assert.fail("Got a SAPI Exception: " + sae);
		}
	}
	
	@Test public void testParse() {
		ChannelMap map = aParser.getCmap();
		Assert.assertTrue(map.GetChannelList().length == loggernetHeader1.split(",").length);
	}
	
	@Test public void testCmap() {
		ChannelMap map = aParser.getCmap();
		//Assert.fail(map.toString());
	}

	@After public void tearDown() {
		aParser = null;
	}
}

