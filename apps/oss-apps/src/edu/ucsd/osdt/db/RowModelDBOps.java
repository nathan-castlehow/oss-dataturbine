package edu.ucsd.osdt.db;

/** @brief DB operation for Rbnb2db program.  
 *   It handles connecting to db
 * 	            formatting dates
 * 	            generating insert queries
 * 				executing a command
 * 				disconnecting from the db
 * @author Peter Shin
 * @since January 28 2007
 */


import java.sql.*;
import java.text.*;
import java.io.*;
 
public class RowModelDBOps {

	// the constructor does all the work in this simple example

	public Connection DBConnect() {
		String database = "template1";
		String username = "cleos";
		String password = "cleos";
		Connection db = null;

		try {
			// load the JDBC driver for PostgreSQL
			Class.forName("org.postgresql.Driver");
		}

		catch (ClassNotFoundException cnfe) {

			System.err.println("Unable to load database driver");
			System.err.println("Deatils : " + cnfe);
			System.exit(0);
		}

		try {
			// connect to the datbase server over TCP/IP 
			// (requires that you edit pg_hba.conf 
			// as shown in the "Authentication" section of this article)
			db = DriverManager.getConnection("jdbc:postgresql:" + database,
					username, password);
		}

		catch (SQLException sqle) {

			System.err.println("Unable to load database driver");
			System.err.println("Deatils : " + sqle);
			System.exit(0);
		}

		return db;

	}

	public void DBInsert(Connection db, String tableName, String colNames[],
			String colVals[]) throws SQLException {
		// create a statement for later use
		Statement sql = db.createStatement();

		String insert = "INSERT INTO ";

		String columnNames = "";
		String columnValues = "";
		for (int i = 0; i < (colNames.length - 1); i++) {
			columnNames += colNames[i] + ",";
			columnValues += colVals[i] + ",";
		}

		columnNames += colNames[colNames.length - 1];
		columnValues += colVals[colVals.length - 1];

		String theQuery = insert + tableName + "(" + columnNames
				+ ") VALUES ( " + columnValues + ")";

		System.out.println("Now executing query: \"" + theQuery + "\"\n");

		sql.executeUpdate(theQuery);

	}

	public void DBInsert(Connection db, String q) throws SQLException {
		// create a statement for later use
		Statement sql = db.createStatement();
		
		System.out.println("Now executing query: \"" + q + "\"\n");
		sql.executeUpdate(q);

	}

	public void DBCloseConnection(Connection db) throws SQLException {
		db.close();
	}

	public static void showUsage() {
		System.out.println("\nUsage:\n "
				+ "java PgTest <database> <username> <password>   \n");
		System.exit(1);
	}

	public static String formatDate(long milis) {
		java.text.SimpleDateFormat format = new java.text.SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss.SSSSZ");
		Date date = new Date(milis);
		return format.format(date);
	}

	public static void main(String args[]) {
		try {

			RowModelDBOps db = new RowModelDBOps();
			Connection dbc = db.DBConnect();

			String colNames[] = { "time_tag" };
			String colVals[] = { "'2004-10-19 10:23:00+02'" };

			db.DBInsert(dbc, "SS.Weather", colNames, colVals);

			db.DBCloseConnection(dbc);
		} catch (Exception ex) {
			System.out.println("Caught Exception:\n" + ex);
			ex.printStackTrace();
		}
	}
}
