package edu.sdsc.cleos;
/** @brief a struct class to contain static data for the NEON singlstring prototype
 * @author ljmiller
 * @since 070124
 */
/* @note static channels and units from the ISI spec entitled "SSP MEssage Format_12_21_06.doc"
Sensor										Make			Model	Channel 	NameMeasurement		Unit
LICOR-190 Quantum Sensor					Licor			LI-190	LI190QS		PAR					umol/( s m^2)
Delta-T BF3 Sunshine Sensor					Delta-T Devices	BF3		BF3TR		Total Radiation		umol /(s m^2)
Delta-T BF3 Sunshine Sensor					Delta-T Devices	BF3		BF3DR		Diffuse Radiation	umol /(s m^2)
Delta-T BF3 Sunshine Sensor					Delta-T Devices	BF3		BF3SS		Sunshine Presence	Sunshine presence (1/0)
Vaisala PTB210 Barometer					Vaisala			PTB210	PTB210		Pressure			hPa
Vaisala HMP45A Temperature Humidity Probe	Vaisala			HMP45A	HMP45T		Temperature	degrees Celsius
Vaisala HMP45A Temperature Humidity Probe	Vaisala			HMP45A	HMP45H		Relative Humidity	%RH
Vaisala WXT510 Weather Transmitter			Vaisala			WXT510	WXT510T		Temperature	degrees Celsius
Vaisala WXT510 Weather Transmitter			Vaisala			WXT510	WXT510H		Relative Humidity	%RH
Vaisala WXT510 Weather Transmitter			Vaisala			WXT510	WXT510P		Pressure			hPa
Vaisala WXT510 Weather Transmitter			Vaisala			WXT510	WXT510RA	Rain Accumulation	mm
Vaisala WXT510 Weather Transmitter			Vaisala			WXT510	WXT510RD	Rain Duration		s
Vaisala WXT510 Weather Transmitter			Vaisala			WXT510	WXT510RI	Rain Intensity		mm/h
Vaisala WXT510 Weather Transmitter			Vaisala			WXT510	WXT510HA	Hail Accumulation	hits/cm^2
Vaisala WXT510 Weather Transmitter			Vaisala			WXT510	WXT510HD	Hail Duration		s
Vaisala WXT510 Weather Transmitter			Vaisala			WXT510	WXT510HI	Hail Intensity		hits/cm^2h
Vaisala WXT510 Weather Transmitter			Vaisala			WXT510	WXT510RPI	Rain Peak Intensity	mm/h
Vaisala WXT510 Weather Transmitter			Vaisala			WXT510	WXT510HPI	Hail Peak Intensity	hits/cm^2h
Vaisala WXT510 Weather Transmitter			Vaisala			WXT510	WXT510WDMIN	Wind Direction Min	degrees
Vaisala WXT510 Weather Transmitter			Vaisala			WXT510	WXT510WDAVG	Wind Direction Avg	degrees
Vaisala WXT510 Weather Transmitter			Vaisala			WXT510	WXT510WDMAX	Wind Direction Max	degrees
Vaisala WXT510 Weather Transmitter			Vaisala			WXT510	WXT510WSMIN	Wind Speed Min		m/s
Vaisala WXT510 Weather Transmitter			Vaisala			WXT510	WXT510WSAVG	Wind Speed Avg		m/s
Vaisala WXT510 Weather Transmitter			Vaisala			WXT510	WXT510WSMAX	Wind Speed Max		m/s
@note $HeadURL$
@note $LastChangedRevision$
@author $LastChangedBy$
@date $LastChangedDate$
*/

//PTB210,LI190QS,HMP45T,HMP45H,BF3TR,BF3DR,BF3SS,WXT510T,WXT510H,WXT510P,WXT510RA,WXT510RD,WXT510RI,WXT510HA,WXT510HD,WXT510HI,WXT510RPI,WXT510HPI,WXT510WDMIN,WXT510WDAVG,WXT510WDMAX,WXT510WSMIN,WXT510WSAVG,WXT510WSMAX,NWPLOG,GPSLOG

public final class NeonSingleStringData {
	protected static final String[] CHANNEL_LIST = new String[] {
		"PTB210",
		"LI190QS",
		"HMP45T",
		"HMP45H",
		"BF3TR",
		"BF3DR",
		"BF3SS",
		"WXT510T",
		"WXT510H",
		"WXT510P",
		"WXT510RA",
		"WXT510RD",
		"WXT510RI",
		"WXT510HA",
		"WXT510HD",
		"WXT510HI",
		"WXT510RPI",
		"WXT510HPI",
		"WXT510WDMIN",
		"WXT510WDAVG",
		"WXT510WDMAX",
		"WXT510WSMIN",
		"WXT510WSAVG",
		"WXT510WSMAX",
		"NWPLOG",
		"GPSLOG"};
	protected static final String[] UNIT_LIST = new String[] {
		"hPa",
		"umol/(s m^2)",
		"degrees Celsius",
		"%RH",
		"umol/(s m^2)",
		"umol/(s m^2)",
		"Sunshine presence (1|0)",
		"degrees Celsius",
		"%RH",
		"hPa",
		"mm",
		"s",
		"mm/h",
		"hits/cm^2",
		"s",
		"hits/cm^2h",
		"mm/h",
		"hits/cm^2h",
		"degrees",
		"degrees",
		"degrees",
		"m/s",
		"m/s",
		"m/s",
		"string",
		"string"};
	protected static final String[] UB_LIST = null;
	protected static final String[] LB_LIST = null;
	
	public static String[] getChannels() {
		return CHANNEL_LIST;
	} // getChannels()
	
	public static String[] getUnits() {
		return UNIT_LIST;
	} // getUnits()
	
} // class

