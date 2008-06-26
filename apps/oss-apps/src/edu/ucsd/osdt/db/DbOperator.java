package edu.ucsd.osdt.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;

public class DbOperator {

	public void GenerateRowModelInsertQuery (
			LinkedList <String> tableNames , 
			LinkedList <String> colNames, 
			LinkedList <String>colVals) {
		
		// create a statement for each table.
				
		
		
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
		/**
		String theQuery = insert + tableName + "(" + columnNames
				+ ") VALUES ( " + columnValues + ")";

		System.out.println("Now executing query: \"" + theQuery + "\"\n");

		sql.executeUpdate(theQuery);

		*/
	}
}
