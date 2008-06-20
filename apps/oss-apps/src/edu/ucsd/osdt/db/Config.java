package edu.ucsd.osdt.db;

/**
 * Config.java: represents the whole config file
 */

import java.util.ArrayList;
import java.util.List;

public class Config {
    // List of TableConfig objects 
    private ArrayList tables = new ArrayList();

	 private String serverAddress = null;
	 private String serverPort = null;

	 private int sampleIntParam = -1;

	 public String getServerAddress() {
		return this.serverAddress;
	 }

	 public void setServerAddress( String serverAddress ) {
		this.serverAddress = serverAddress;
	 }

	 public String getServerPort() {
		return this.serverPort;
	 }

	 public void setServerPort( String serverPort ) {
		this.serverPort = serverPort;
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
