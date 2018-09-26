/*
 * DbmsQuery.java
 * $ID*
 */

package dalserver;

import java.io.*;
import java.util.*;
import java.sql.*;

/**
 * DbmsQuery provides a thin layer for the Java DriverManager (JDBC)
 * class to deal with the peculiarities of different supported database
 * managers as used within the DALServer framework.
 *
 * To add support for a new DBMS, install the JDBC driver in LIB, 
 * add file management for it in build.xml, and modify DbmsQuery as
 * necessary to support the DBMS.  In some cases conditional code
 * may be required in service code to deal with differences in SQL
 * expressions, although usually a portable approach can be found
 * for simple queries (adding TAP may eventually solve all these
 * problems).
 *
 * @version	1.0, 4-July-2014
 * @author	Doug Tody
 */
public class DbmsQuery {
    /* Database type. */
    private String dbType;

    /* Database name. */
    private String database;

    /** Connection to the remote DBMS. */
    private Connection conn;

    /** DBMS-specific query processing flags. */
    private boolean quoteNames = false;

    /**
     * Constructor to generate a new DbmsQuery object.
     *
     * @param	dbType		Database type
     * @param	jdbcDriver	JDBC driver class
     */
    public DbmsQuery(String dbType, String jdbcDriver)
	throws DalServerException {

	// Check that the DBMS is a supported type.
	if (dbType.equalsIgnoreCase("MySQL"))
	    this.dbType = "mysql";
	else if (dbType.equalsIgnoreCase("PostgreSQL"))
	    this.dbType = "postgresql";
	else if (dbType.equalsIgnoreCase("Oracle"))
	    this.dbType = "oracle";
	else
	    throw new DalServerException("Unsupported DBMS (" + dbType + ")");

	// Postgres table/col names are case sensitive, but when they appear
	// as identifiers in a SQL expression they are automatically mapped
	// to lower case.  To preserve case they must be quoted.

	if (this.dbType.equals("postgresql"))
	    this.quoteNames = true;

	// Load the JDBC driver (not required for newer Java versions).
	try {
	    Class.forName(jdbcDriver).newInstance();
	    conn = null;
	} catch (Exception ex) {
	    throw new DalServerException(ex.getMessage());
	}
    }

    /**
     * Connect to the remote database.
     *
     * @param	url		JDBC URL of the remote DBMS.
     * @param	database	Database name within remote DBMS.
     * @param	username	User name for database login.
     * @param	password	User password for database login.
     */
    public void
    connect(String url, String database, String username, String password)
	throws DalServerException {

	this.database = database;
	String dburl = url;

	if (this.dbType.equals("mysql") ||
	    this.dbType.equals("postgresql")) {

	    dburl += database;
	}

	try {
	    conn = DriverManager.getConnection(dburl, username, password);
	} catch (Exception ex) {
	    conn = null;
	    throw new DalServerException(ex.getMessage());
	}
    }


    /**
     * Disconnect from the remote database.
     */
    public void disconnect() {
	if (conn != null) {
	    try {
		conn.close();
	    } catch (SQLException ex) {
		;
	    }
	}
    }

    /**
     * Return the Connection handle.
     */
    public Connection getConnection() throws DalServerException {
	if (conn == null)
	    throw new DalServerException("database is not connected");
	else
	    return (conn);
    }

    /**
     * Flag (table,column, etc.) names to be quoted.
     */
    public void quoteNames(boolean flag) {
	this.quoteNames = flag;
    }

    /**
     * Process an identifier, e.g., table or column name, as necessary for
     * use in a SQL expression.
     */
    public String sqlName(String name) {
	if (this.quoteNames)
	    return ("\"" + name + "\"");
	else
	    return (name);
    }

    /**
     * Return the DBMS type.
     */
    public String dbType() {
	return (this.dbType);
    }
}
