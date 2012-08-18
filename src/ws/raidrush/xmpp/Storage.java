package ws.raidrush.xmpp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

public class Storage 
{
  // jdbc sqlite-connection
  Connection dbh;
  
  /**
   * constructor
   * 
   */
  public Storage()
  {
    // use default sqlite location
    this("xmppbot.db");
  }
  
  /**
   * constructor
   * 
   * @param db
   */
  public Storage(String db)
  {
    String jdbc = "jdbc:sqlite:" + db;
    
    try {
      Class.forName("org.sqlite.JDBC");
    } catch (ClassNotFoundException e) {
      Logger.getRootLogger().fatal("Unable to load JDBC Sqlite handler", e);
      System.exit(1);
    }
    
    Logger.getRootLogger().info("Sqlite handler is ready, using JDBC setup: " + jdbc);
    
    try {
      this.dbh = DriverManager.getConnection(jdbc);
    } catch (SQLException e) {
      Logger.getRootLogger().fatal("Failed to get Sqlite Database-handle", e);
      System.exit(1);
      return; // not necessary, but i feel better now
    }
    
    // install tables if necessary
    install();
    
    Logger.getRootLogger().info("Database is ready");
  }
  
  public String[] getPluginsForChannel(String channel)
  {
    Statement stmt;
    
    try {
      stmt = this.dbh.createStatement();
    } catch (SQLException e) {
      return null;
    }
    
    return null;
  }
  
  /**
   * creates core-tables for channels and plugins
   * 
   * @void
   */
  private void install()
  {
    Statement stmt;
    
    try {
      stmt = this.dbh.createStatement();
      stmt.setQueryTimeout(30);
    } catch (SQLException e) {
      Logger.getRootLogger().error("Unable to create Sqlite-statement", e);
      System.exit(1);
      return; // not necessary
    }
    
    Logger.getRootLogger().info("Installing tables");
    
    try {
      // create `channels` table
      stmt.executeUpdate("" +
      		"CREATE TABLE IF NOT EXISTS `channels` (" +
      		  "`id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
      		  "`name` VARCHAR(100) NOT NULL UNIQUE," +
      		  "`owner` VARCHAR(100) NOT NULL" +
      		")"
      );

      // create `plugins` table
      stmt.executeUpdate("" +
      		"CREATE TABLE IF NOT EXISTS `plugins` (" +
      		  "`id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
      		  "`type` INTEGER NOT NULL DEFAULT 1," +
      		  "`package` VARCHAR(100) NOT NULL UNIQUE," +
      		  "`version` VARCHAR(10) NOT NULL DEFAULT '1'," +
      		  "`author` VARCHAR(50) NOT NULL DEFAULT 'anonymus'" +
      		")"
      );
      
      // create `plugins_to_channels` table
      stmt.executeUpdate("" +
      		"CREATE TABLE IF NOT EXISTS `plugins_to_channels` (" +
      		  "`channel_id` INTEGER NOT NULL," +
      		  "`plugin_id` INTEGER NOT NULL," +
      		  "PRIMARY KEY(`channel_id` DESC, `plugin_id` DESC)" +
      		")"
      );
    } catch (SQLException e) {
      Logger.getRootLogger().fatal("Unable to install required Database-tables", e);
      System.exit(1);
    }
  }
}
