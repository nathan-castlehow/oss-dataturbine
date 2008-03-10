package edu.sdsc.rtdsm.framework.db;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.io.IOException;
import java.util.Hashtable;

import edu.sdsc.rtdsm.framework.util.*;

public class ConnectionManager {

  
  private static ConnectionManager theInstance;

  private String dbDriver;
  private String jdbcUrl;
  private String username;
  private String password;
  private Connection conn; 
  
  static {

    try {
      DTProperties dtp = DTProperties.getProperties(
        "rtdsm.properties");
      String dbDriver = dtp.getProperty(Constants.DB_DRIVER_TAG);
      String jdbcUrl = dtp.getProperty(Constants.DB_JDBC_URL_TAG);
      String username = dtp.getProperty(Constants.DB_USERNAME_TAG);
      String password = dtp.getProperty(Constants.DB_PASSWORD_TAG);
      theInstance = new ConnectionManager(dbDriver, jdbcUrl,
          username, password);
    }
    catch (IOException ioe){

      ioe.printStackTrace();
      throw new IllegalStateException("No \"rtdsm.properties\" file found. ");
    }
  }


  private ConnectionManager(String dbDriver, String jdbcUrl,
      String username, String password){

    this.dbDriver = dbDriver;
    this.jdbcUrl = jdbcUrl;
    this.username = username;
    this.password = password;

    try {

      Class.forName(dbDriver);
      conn = DriverManager.getConnection(jdbcUrl, username, password);
    }
    catch (ClassNotFoundException cnfe) {

      cnfe.printStackTrace();
      throw new IllegalArgumentException(
          "Could not find db driver with name \"" + dbDriver + "\"");
    }
    catch (SQLException se) {
      se.printStackTrace();
      throw new IllegalArgumentException(
        "Couldn't connect to database with jdbcUrl \"" + jdbcUrl +
        "\" and username \"" + username + "\" and password \"" + 
        password + "\"");
    }
  }

  public static ConnectionManager getInstance(){
    return theInstance;
  }

  public Connection getConnection(){

    return conn;
  }
}
