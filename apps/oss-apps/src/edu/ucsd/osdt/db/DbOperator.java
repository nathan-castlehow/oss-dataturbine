package edu.ucsd.osdt.db;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;

public class DbOperator {

	public DbOperator() {
		
	}
	
	String dbServerName ="";
	String jdbcDriverName ="";
	String dbName="";
	String dbUserName="";
	String dbPassword="";
	String timeStampColName = "";
	String UTCTimeStampColName = "";
	int UTCOffset = 0;
	
	public void setConfig (Config cfg) {
		
		this.dbServerName = cfg.getDbServerName();
		this.jdbcDriverName = cfg.getJdbcDriverName();
		this.dbName = cfg.getDbName();
		this.dbUserName = cfg.getDbUserName();
		this.dbPassword = cfg.getDbPassword();
		
		this.timeStampColName = cfg.getTimeStampColName();
		this.UTCTimeStampColName = cfg.getUTCTimeStampColName();
		this.UTCOffset = cfg.getUTCOffset();
		
	}
	
	
	public LinkedList <String> GenerateRowModelInsertQueries (
			LinkedList <String> tableNames , 
			LinkedList <String> colNames, 
			LinkedList <String>colVals,
			double timeStamp)
	{
		
		boolean done = false;
		
		System.out.println("GenerateRowModel");
		// create a statement for each table.
		LinkedList <Integer>indexList = new LinkedList <Integer> ();
		LinkedList <String> resultQueries = new LinkedList<String>();
		
		if (tableNames.size() >0) {
			while (done) {
				// find all the ones with the same table. 
				String tempTableName = tableNames.get(0);
				indexList.add(new Integer(0));

				if (tableNames.size() >= 1) {
					for (int i=1; i< tableNames.size(); i++) {
						String otherTableName = tableNames.get(i);
						
						if (tempTableName.equals(otherTableName)) {
							indexList.add(new Integer(i));
						}
					}
					
					// prepared the names for column names and values for
					// ones with the same table name
					LinkedList <String> colNamesInTable = new LinkedList <String>();
					LinkedList <String> colValsInTable = new LinkedList<String>();
					
					// selecting the ones with the same table names.
					for (int i=0; i< indexList.size();i++) {
						int selectedIndex = indexList.get(i);
						colNamesInTable.add(colNames.get(selectedIndex));
						colValsInTable.add(colVals.get(selectedIndex));
					}

					String resultQuery = GenerateRowModelInsertQuery (tempTableName, colNamesInTable, colValsInTable, timeStamp);
					resultQueries.add(resultQuery);

					// remove the finished ones from the list
					for (int i= (indexList.size()-1); i >=0 ;i--) {
						int selectedIndex = indexList.get(i);
						colNames.remove(selectedIndex);
						colVals.remove(selectedIndex);
					}
					
					if (colNames.size() == 0) done=true;
				}

				else { // there is only one item left
					String resultQuery = GenerateRowModelInsertQuery (tempTableName, colNames, colVals, timeStamp);
					resultQueries.add(resultQuery);
					done = true;
				}
				
			}
			
			return resultQueries;
		}
		
		else {
			return resultQueries;
		}
		
	}
		
	
	public String GenerateRowModelInsertQuery (
			String tableName , 
			LinkedList <String> colNames, 
			LinkedList <String>colVals,
			double timeStamp) 
	{
		// create a statement for later use

		String insert = "INSERT INTO ";

		String columnNames = "";
		String columnValues = "";
		for (int i = 0; i < (colNames.size() - 1); i++) {
			columnNames += colNames.get(i) + ",";
			columnValues += colVals.get(i) + ",";
		}

		columnNames += colNames.get(colNames.size() - 1);
		columnValues += colVals.get(colVals.size() - 1);
		
				
		// generate an insert query for a particular table with the same timeStamp
		
		String theQuery = insert + tableName + "(" + columnNames
				+ ") VALUES ( " + columnValues + ")";

		System.out.println("Now executing query: \"" + theQuery + "\"\n");

		return theQuery;
	}
	
	public String formatDate(long milis) {
		java.text.SimpleDateFormat format = new java.text.SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss.SSS");
		Date date = new Date(milis);
		return format.format(date);
	}

		
}
