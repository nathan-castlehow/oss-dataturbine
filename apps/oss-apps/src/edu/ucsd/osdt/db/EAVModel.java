package edu.ucsd.osdt.db;

/*  author: Peter Shin (pshin@sdsc.edu)
 * 
 */
import java.sql.*;
import java.text.*;
import java.io.*;

public class EAVModel {

	// the constructor does all the work in this simple example

	public Connection DBConnect() {

		String connectionUrl = "jdbc:sqlserver://disrupter.sdsc.edu:1433;" +
         "databaseName=rbnb;user=rbnb;password=rbnbRBNB";
 
		Connection db = null;

		try {
			// load the JDBC driver for PostgreSQL
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
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
			db = DriverManager.getConnection(connectionUrl);
			
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
				"yyyy-MM-dd HH:mm:ss.SSS");
		Date date = new Date(milis);
		return format.format(date);
	}

	public static int getMaxID () {
		try {
			EAVModel db = new EAVModel();
			Connection dbc = db.DBConnect();

			// Declare the JDBC objects.
			Connection con = null;
			Statement stmt = null;
			ResultSet rs = null;

			String SQL = "SELECT max(valueID) FROM DataValues";
			stmt = dbc.createStatement();
			rs = stmt.executeQuery(SQL);

			String maxIDstr = null;
			// Iterate through the data in the result set and display it.
			while (rs.next()) {
				maxIDstr =	rs.getString(1);
			}

			int maxID = Integer.parseInt(maxIDstr);

			db.DBCloseConnection(dbc);
			
			return maxID;

		} catch (Exception ex) {
			System.out.println("Caught Exception:\n" + ex);
			ex.printStackTrace();
			return 0;
		}
	}
	
	public static void main(String args[]) {
		try {
			EAVModel db = new EAVModel();
			Connection dbc = db.DBConnect();

			// Declare the JDBC objects.
			Connection con = null;
			Statement stmt = null;
			ResultSet rs = null;

			String SQL = "SELECT max(valueID) FROM DataValues";
			stmt = dbc.createStatement();
			rs = stmt.executeQuery(SQL);

			// Iterate through the data in the result set and display it.
			while (rs.next()) {
				System.out.println(rs.getString(1) + " " + rs.getString(1));
			}


			db.DBCloseConnection(dbc);
		} catch (Exception ex) {
			System.out.println("Caught Exception:\n" + ex);
			ex.printStackTrace();
		}
	}
}
