/**
 * DbmsDataSourceFactory.java
 * $ID*
 */

package dalserver;

import javax.sql.DataSource;
import javax.naming.*;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;
import java.io.*;
import java.util.*;

/**
 * JDBC DataSource management.  This class hides the details of how to obtain
 * a DataSource.  When called from within a Java application server context
 * (e.g., Tomcat), JNDI lookup is used to obtain the DataSource, and the
 * DataSource instance may be persistent.  If JNDI is not available then
 * a new DataSource may be created by reading the JDBC connection properties
 * from a Java properties file.
 *
 * @author DTody
 */
public class DbmsDataSourceFactory {
    private static final Logger log = Logger.getLogger(DbmsDataSourceFactory.class);

    /** No-arg constructor. */
    public DbmsDataSourceFactory() { }

    /**
     * Return a DataSource given an DataSource reference expressed as a
     * custom-formatted string.
     *
     * @param	dsref			Reference to the DataSource
     *
     * The reference to the DataSource defaults to JNDI lookup, and should be
     * a JNDI name such as "java/tapuser".  In a Java applications server
     * context such as Tomcat, the actual DataSource would be defined in the
     * Tomcat configuration, would persist beyond the servlet lifetime,
     * possibly support connection pooling, etc.
     *
     * In the case of a file-based DataSource, the JDBC connection parameters
     * are read from a Java properties file.  This case is indicated by a
     * DataSource reference of the form "file:<path>", where <path> is the
     * pathname of the Java properties file.  A new DataSource instance is
     * created with the specified properties.
     */
    public DataSource getDataSource(String dsref)
	throws DalServerException {

	// Check if we have a JNDI or file DataSource reference.
	if (dsref.startsWith("file:"))
	    return (fileDataSource(dsref.substring(5)));
	else
	    return (jndiDataSource(dsref));
    }

    /**
     * Get a DataSource via JNDI lookup.
     *
     * @param	dsref			Reference to the DataSource
     */
    public DataSource jndiDataSource(String dsref)
	throws DalServerException {

	DataSource ds;

	try {
	    Context initContext = new InitialContext();
	    Context envContext = (Context) initContext.lookup("java:comp/env");
	    ds = (DataSource) envContext.lookup(dsref);
	} catch (NamingException ex) {
	    throw new DalServerException(
		"JNDI naming exception: " + ex.getMessage());
	}

	return (ds);
    }

    /**
     * Get a DataSource by creating a new instance from a properties file.
     *
     * @param	dsref			Reference to the DataSource
     */
    public DataSource fileDataSource(String dsref)
	throws DalServerException {

	final String DB_URL = "url";
	final String DB_DRIVER_CLASS = "driverClassName";
	final String DB_USERNAME = "username";
	final String DB_PASSWORD = "password";

	Properties props = new Properties();
        BasicDataSource ds = new BasicDataSource();
        FileInputStream in = null;
         
	// Load the DataSource properties.
        try {
            in = new FileInputStream(dsref);
            props.load(in);
        } catch (IOException ex){
	    throw new DalServerException(
		"Cannot read properties file: " + ex.getMessage());
        }

	// Apache seems to have trouble finding the JDBC driver class
	// ds.setDriverClassLoader(DbmsDataSourceFactory.class.getClassLoader());

	// Set the essential properties.
	ds.setDriverClassName(props.getProperty(DB_DRIVER_CLASS));
	ds.setUrl(props.getProperty(DB_URL));
	ds.setUsername(props.getProperty(DB_USERNAME));
	ds.setPassword(props.getProperty(DB_PASSWORD));

	// Additional optional BasicDataSource parameters.
	// [none at present]

	return (ds);
    }
}
