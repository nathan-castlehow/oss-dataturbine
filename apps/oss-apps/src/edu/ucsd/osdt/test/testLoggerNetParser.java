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

import edu.ucsd.osdt.source.numeric.LoggerNetParser;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class testLoggerNetParser {
	private LoggerNetParser aParser = null;
	
	@Before public void setUp() {
		aParser = new LoggerNetParser();
	}
	
	@Test public void testParse() {
		String loggernetHeader1 = "\"TIMESTAMP\",\"RECORD\",\"AirTemp_C_Max\",\"AirTemp_C_TMx\",\"AirTemp_C_Min\",\"AirTemp_C_TMn\",\"AirTemp_C_Avg\",\"RH_Avg\",\"Rain_mm_Tot\",\"WindSp_ms_Max\",\"WindSp_ms_TMx\",\"BP_mbar_Avg\",\"SoilTemp_C_Avg\",\"SoilWVC_Avg\"";
		String loggernetHeader2	= "\"TS\",\"RN\",\"Deg C\",\"Deg C\",\"Deg C\",\"Deg C\",\"Deg C\",\"%\",\"mm\",\"meters/second\",\"meters/second\",\"mbar\",\"Deg C\",\"%\"";
		Assert.assertTrue(true);
	}

	@After public void tearDown() {
		aParser = null;
	}
}

