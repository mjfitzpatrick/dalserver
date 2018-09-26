/*
 * SsapTestQuery.java
 * $ID*
 */

package dalserver.ssa;

import dalserver.DalServerException;
import dalserver.RequestResponse;
import dalserver.Param;
import dalserver.RangeList;
import dalserver.TableInfo;
import dalserver.ssa.SsapParamSet;

import java.io.*;
import java.util.*;
import java.net.*;
import java.sql.*;

/**
 * SsapTestQuery simulates a SSA database query class, implementing the
 * null query, which returns only standard SSA / SpectrumDM metadata.
 *
 * @version	2.0, 21-Apr-2014
 * @author	Doug Tody
 */
public class SsapTestQuery {
    /** Connection to the remote DBMS. */
    private Connection conn;

    /** Service version. */
    private int version = 1;

    /** Constructor to generate a new DBMS query object, providing
     * the functionality to query a remote DBMS-hosted catalog.
     */
    public SsapTestQuery(String jdbcDriver) throws DalServerException {
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
     * defined fields of the SSA metadata DBMS table for the current
     * service.  In principle the SSA metadata table could contain
     * dozens or hundreds of data model attributes; an actual instance of
     * the metadata table will in general be much narrower.  The DM fields
     * that are defined in a table instance will always include a
     * mandatory core set of fields (some of which could have null
     * values), and may also include selected fields from the full data
     * model.  [We should extend this scheme to support custom
     * service-defined metadata as well].
     *
     * @param	params		The SSAP service input parameters.
     * @param	response	The request response object.
     */
    public void addFields(SsapParamSet params, RequestResponse response)
	throws DalServerException {

	String dataModel;
	String serviceVersion = params.getValue("serviceVersion");
	String verb = params.getValue("verb");
        RequestResponse r = response;
        SsapKeywordFactory ssap;

	// Get the correct SSA data model for the version of the SSA protocol
	// in use for this query.

	if (serviceVersion.startsWith("2")) {
	    ssap = new SsapKeywordFactory("main", "2");
	    dataModel = "Spectrum-2.0";
	    this.version = 2;
	} else {
	    ssap = new SsapKeywordFactory("main", "1");
	    dataModel = "Spectrum-1.0";
	    this.version = 1;
	}

	// Step through the SIAPV2 keyword list and output all fields 
	// flagged as mandatory or recommended.  In a real service the
	// DBMS would be queried instead to determine what DM or other
	// fields are used in the actual data service.

	ssap.addFields(response, ssap.verbosityMask(verb));

	try {
	    response.setValue("datamodel_name", dataModel);
	} catch (DalServerException ex) {
	    ;
	}
    }


    /**
     * Dummy DBMS SSA metadata query, which never finds any data (hence
     * it is a null query).  The method signature matches that of a real
     * SSAV2 DBMS query.
     *
     * @param	params		The SSAP service input parameters.
     * @param	response	The request response object.
     */
    public void query(SsapParamSet params, RequestResponse response)
	throws DalServerException {

	String query = "Dummy SSA Spectrum table query";
	boolean error = false;  // in case we add more logic later

	if (error)
	    throw new DalServerException("dummy error message");
    }
}
