/*
 * SiapTestQuery.java
 * $ID*
 */

package dalserver.sia;

import dalserver.DalServerException;
import dalserver.RequestResponse;
import dalserver.Param;
import dalserver.RangeList;
import dalserver.TableInfo;
import dalserver.sia.SiapParamSet;

import java.io.*;
import java.util.*;
import java.net.*;
import java.sql.*;

/**
 * SiapTestQuery simulates a SIA database query class, implementing the
 * null query, which returns only standard SIA / ImageDM metadata.
 *
 * @version	2.0, 03-Aug-2014
 * @author	Doug Tody
 */
public class SiapTestQuery {
    /** Connection to the remote DBMS. */
    private Connection conn;

    /** Service version. */
    private int version = 1;

    /** Constructor to generate a new DBMS query object, providing
     * the functionality to query a remote DBMS-hosted catalog.
     */
    public SiapTestQuery(String jdbcDriver) throws DalServerException {
	try {
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
    public void connect(String url, String database, String username,
	String password) throws DalServerException {

	try {
	    conn = null;
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
     * Add fields to the request response table corresponding to the
     * defined fields of the metadata DBMS table for the current
     * service.  In principle the metadata table could contain
     * dozens or hundreds of data model attributes; an actual instance of
     * the metadata table will in general be much narrower.  The DM fields
     * that are defined in a table instance will always include a
     * mandatory core set of fields (some of which could have null
     * values), and may also include selected fields from the full data
     * model.  [We should extend this scheme to support custom
     * service-defined metadata as well].
     *
     * @param	params		The SIAP service input parameters.
     * @param	response	The request response object.
     */
    public void addFields(SiapParamSet params, RequestResponse response)
	throws DalServerException {

	String dataModel;
	String serviceVersion = params.getValue("serviceVersion");
	String verb = params.getValue("verb");
        RequestResponse r = response;
        SiapKeywordFactory siap;

	// Get the correct SIA data model for the version of the SIA protocol
	// in use for this query.

	if (serviceVersion.startsWith("2")) {
	    siap = new SiapKeywordFactory("main", "2");
	    dataModel = "Image-2.0";
	    this.version = 2;
	} else {
	    siap = new SiapKeywordFactory("main", "1");
	    dataModel = "Image-1.0";
	    this.version = 1;
	}

	// Step through the SIAPV2 keyword list and output all fields 
	// flagged as mandatory or recommended.  In a real service the
	// DBMS would be queried instead to determine what DM or other
	// fields are used in the actual data service.

	siap.addFields(response, siap.verbosityMask(verb));

	try {
	    response.setValue("datamodel_name", dataModel);
	} catch (DalServerException ex) {
	    ;
	}
    }


    /**
     * Dummy DBMS SIAV1/SIAV2 metadata query, which never finds any data
     * (hence it is a null query).  The method signature matches that of
     * a real SIA DBMS query.
     *
     * @param	params		The SIAP service input parameters.
     * @param	response	The request response object.
     */
    public void query(SiapParamSet params, RequestResponse response)
	throws DalServerException {

	String query = "Dummy SIA Image table query";
	boolean error = false;  // in case we add more logic later

	if (error)
	    throw new DalServerException("dummy error message");
    }
}
