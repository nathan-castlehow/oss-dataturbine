package edu.ucsd.osdt.db;

/**
 * Config.java: represents the whole config file
 */

import java.util.ArrayList;
import java.util.List;

import edu.ucsd.osdt.db.log.SysDLogger;
import java.util.LinkedList;
import edu.ucsd.osdt.db.dt2dbMap;
import java.util.HashMap;

public class Config {
	// List of TableConfig objects 
	private ArrayList tables = new ArrayList();

	String rbnbServerAddress = "localhost";
	String rbnbServerPort =	"3333";
	
	HashMap mapper = null;
	
	public void setDt2dbMap (HashMap <String, dt2dbMap> m) {
		this.mapper = m;
	}
	
	public HashMap getDt2dbMap () {
		return this.mapper;
	}
	
	
	String timeStampColName = "";
	String UTCTimeStampColName = "";
	int UTCOffset = -8;
	
	public void setTimeStampColName(String colName) {
		this.timeStampColName = colName;
	}
	
	public String getTimeStampColName() {
		return this.timeStampColName;
	}
	
	public void setUTCTimeStampColName(String cN) {
		this.UTCTimeStampColName = cN;
	}
	
	public String getUTCTimeStampColName() {
		return this.UTCTimeStampColName;
	}
	
	public void setUTCOffset (String offset) {
		try {
			this.UTCOffset = Integer.parseInt(offset);
		}
		catch (NumberFormatException e1) {
			e1.printStackTrace();
		}
		
	}
	
	public int getUTCOffset (){
		return this.UTCOffset;
	}
	
	
	
	LinkedList <String> rbnbChannelPaths = null;
	LinkedList <String> dbColPaths = null;
	LinkedList <LinkedList> dbExtraInfos = null;

	public void setChNames (LinkedList l) {
		this.rbnbChannelPaths = l;	
	}
	public LinkedList <String> getChNames(){
		return this.rbnbChannelPaths;
	}
	
	
	private int sampleIntParam = -1;

	public String getRbnbServerAddress() {
		return this.rbnbServerAddress;
	}

	public void setRbnbServerAddress( String serverAddress ) {
		this.rbnbServerAddress = serverAddress;
	}

	public String getRbnbServerPort() {
		return this.rbnbServerPort;
	}


	public void setRbnbServerPort( String serverPort ) {
		this.rbnbServerPort = serverPort;
	}


	String sysLoggerServerName = null;
	
	public String getSysLogServerAddress() {
		return this.sysLoggerServerName;
	}
	public void setSysLogServerAddress(String s) {
		this.sysLoggerServerName = s;
	}
	
	
	
	// DB params
	String dbServerName = "localhost";
	String jdbcDriverName = "org.postgresql.Driver";
	String dbName = "testDB";
	String dbUserName = "postgres";
	String dbPassword = null;
	
	public String getDbServerName () {
		return this.dbServerName;
	}
	public void setDbServerName(String serv) {
		this.dbServerName = serv;
	}
	
	public String getJdbcDriverName () {
		return this.jdbcDriverName;
	}
	public void setJdbcDriverName(String drv) {
		this.jdbcDriverName = drv;
	}
	
	public String getDbName() {
		return this.dbName;
	}
	public void setDbName(String n) {
		this.dbName=n;
	}
	
	public String getDbUserName() {
		return this.dbUserName;
	}
	public void setDbUserName(String uName) {
		this.dbUserName = uName;
	}
	
	public String getDbPassword () {
		return this.dbPassword;
	}
	public void setDbPassword (String p) {
		this.dbPassword = p;
	}
	
	
	
	// for SinkClientManger program
	String startTimeFilePath = null;
	double durationSeconds = 20.0;
	boolean stopAtError = false;
	String continueFlagFile = null;
	String emailContact = "pshin@sdsc.edu";
	
	
	public String getStartTimeFilePath () {
		return this.startTimeFilePath;
	}
	public void setStartTimeFilePath(String fp) {
		this.startTimeFilePath = fp;
	}
	
	public double getDurationSeconds() {
		return this.durationSeconds;
	}
	public void setDurationSeconds(double d) {
		this.durationSeconds = d;
	}
	
	public boolean getStopAtError () {
		return this.stopAtError;
	}
	public void setStopAtError (boolean ans) {
		this.stopAtError = ans;
	}
	
	public String getContinueFlagFile () {
		return this.continueFlagFile;
	}
	public void setContinueFlagFile(String fp) {
		this.continueFlagFile = fp;
	}
	
	public String getEmailContact() {
		return this.emailContact;
	}
	public void setEmailContact(String em) {
		this.emailContact = em;
	}
	
	
	
	String dataModel = null;
	
	public String getDataModel() {
		return this.dataModel;
	}
	public void setDataModel(String dm) {
		this.dataModel = dm;
	}
	
	
	
	
	public int getSampleIntParam() {
		return this.sampleIntParam;
	}

	public void setSampleIntParam( int sampleIntParam ) {
		this.sampleIntParam = sampleIntParam;
	}



	public TableConfig getTableConfig( String tableName ) {
		int idx = getIndexForName( tableName );

		if( idx >= 0 )
			return (TableConfig) tables.get( idx );
		else
			return null;
	}


	public void putTableConfig( TableConfig aTable ) {
		this.tables.add( aTable );
	}

	public void removeTableConfig( String tableName ) {
		int idx = getIndexForName( tableName );

		if( idx >= 0 )
			tables.remove( idx );
	}

	/**
	 * Returns index of item with given name
	 */
	private int getIndexForName( String aName ) {

		for( int i=0; i < tables.size(); i++ ) {
			TableConfig aTable = (TableConfig) tables.get(i);

			if( ( aTable.getName() != null ) && aTable.getName().equals( aName ) ) {
				return i;
			}
		}

		// Didn't find a match
		return -1;
	}

	public List getTableConfigsAsList() {
		return tables;
	}
}
