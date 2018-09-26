/*
 * ScsService.java
 * $ID*
 */

package dalserver.scs;

import java.io.*;
import java.net.*;
import java.util.*;
import dalserver.*;
import dalserver.scs.*;

/**
 * A generic class which implements the operations for a DALServer SCS
 * service.  A data-specific service should subclass this, replacing the
 * <i>queryData</i> operation with a version which implements the same
 * operation for a specific archive and data collection.  In most cases
 * the remaining SCS operations (e.g., <i>getCapabilities</i>) should not
 * need to be subclassed by a data service, except to edit the metadata
 * to be returned.  The remaining code required to implement a complete
 * service (excluding that required to generate the data product
 * to be returned) is provided by the {@link dalserver} code.
 *
 * <p>The service implementation at this level should not be protocol
 * specific.  The intention is that the service operations at this level can
 * be used with any distributed computing platform.  Hence we can implement
 * an HTTP GET interface now, and add a SOAP or other interface later,
 * or some other interface which may not even be network based, all sharing
 * the same core service operations.</p>
 */
public class ScsService {
    /**
     * The service name, used to identify resources associated with this
     * service instance.
     */
    protected String serviceName = "scs";

    /**
     * The service class assumed by the caller, e.g., if the service
     * implementation supports multiple service protocols such as SCS
     * and TAP.
     */
    protected String serviceClass = "scs";

    /** The type of DBMS to be accessed. */
    protected String dbType;

    /** The name of the database to be accessed. */
    protected String dbName;

    /** The name of the table to be accessed (for SCS). */
    protected String tableName;

    /** The JDBC URL (endpoint) of the DBMS. */
    protected String jdbcUrl;

    /** The JDBC driver to be used for the connection. */
    protected String jdbcDriver;

    /** The user name to use to login to the DBMS. */
    private String dbUser;

    /** The user password to use to login to the DBMS. */
    private String dbPassword;

    /** Local directory where configuration files are stored. */
    protected String configDir;

    /**
     * Create a new local SCS service instance.
     *
     * @param params	Input parameter set.
     */
    public ScsService(ScsParamSet params) {
	if (params == null) {
	    this.serviceName = "scs";
	    this.serviceClass = "scs";
	    this.dbType = "builtin";
	    this.dbName = "dalserver";
	    this.tableName = "messier";
	} else {
	    this.serviceName = params.getValue("serviceName");
	    this.serviceClass = params.getValue("serviceClass");
	    this.dbType = params.getValue("dbType");
	    this.dbName = params.getValue("dbName");
	    this.tableName = params.getValue("tableName");
	    this.jdbcUrl = params.getValue("jdbcUrl");
	    this.jdbcDriver = params.getValue("jdbcDriver");
	    this.dbUser = params.getValue("dbUser");
	    this.dbPassword = params.getValue("dbPassword");
	}
    }

    /**
     * Process a SCS table data query and return data in the input
     * request response object.  For a cone search the query merely
     * specifies a circular region on the sky for which matching rows
     * of a position-oriented table are returned.
     *
     * @param	params	  The fully processed SCS parameter set representing
     *			  the request to be processed.
     *
     * @param	response  A dalserver request response object to which the
     *			  query response should be written.  Note this is
     *			  not a file, but an object containing the metadata
     *			  to be returned to the client.
     */
    @SuppressWarnings("unchecked")
    public void
    queryData(ScsParamSet params, RequestResponse response)
	throws DalServerException {

	String id, key;
	TableInfo dbNameInfo = new TableInfo("dbName", dbName);
	TableInfo tableNameInfo = new TableInfo("tableName", tableName);

	// Set global metadata.
	response.setDescription("DALServer cone search");
	response.setType("results");

	// This indicates the query executed successfully.  If an exception
	// occurs the output we generate here will never be returned anyway,
	// so OK is always appropriate here.

	response.addInfo(key="QUERY_STATUS", new TableInfo(key, "OK"));
	response.echoParamInfos(params);
	response.addInfo(key="dbName", dbNameInfo);
	response.addInfo(key="tableName", tableNameInfo);
 
 	// This implementation supports only SCS.
	if (!serviceClass.substring(0,3).equalsIgnoreCase("scs"))
	    throw new DalServerException("Service only supports SCS");

	// Create the table metadata for a standard SCS query response.
	if (dbType.equalsIgnoreCase("builtin")) {

	    // Query the builtin test table.  This allows the service
	    // to be exercised and tested without requiring configuration
	    // of a real DBMS.

	    if (!dbName.equalsIgnoreCase("dalserver"))
		throw new DalServerException("unknown database: " + dbName);
	    if (!tableName.equalsIgnoreCase("messier"))
		throw new DalServerException("unknown table: " + tableName);

	    ScsMessier messier = new ScsMessier();
	    try {
		messier.query(params, response);
	    } catch (DalOverflowException ex) {
		;
	    }

	} else {
	    // Query a supported DBMS.
	    ScsQuery dbms = null;
	    Exception error = null;

	    // Allow use of a query param to override the default table name.
	    try {
		Param p = params.getParam("FROM");
		if (p != null && p.isSet()) {
		    tableName = p.stringValue();
		    params.setValue("tableName", tableName);
		    tableNameInfo.setValue(tableName);
		}

		dbms = new ScsQuery(dbType, jdbcDriver);
		dbms.connect(jdbcUrl, dbName, dbUser, dbPassword);
		dbms.query(params, response);

	    } catch (DalOverflowException ex) {
		// Terminate normally; overflow will be indicated.
		error = null;
	    } catch (Exception ex) {
		error = ex;
	    } finally {
		if (dbms != null)
		    dbms.disconnect();
		if (error != null)
		    throw new DalServerException(error.getMessage());
	    }
	}

	// Show the number of table rows in the response header.
	response.addInfo(key="TableRows",
	    new TableInfo(key, new Integer(response.size()).toString()));

	// We are done once the information content of the query response
	// is fully specified.  The servlet code will take care of serializing
	// the query response as a VOTable, and anything else required.
    }


    /**
     * Process a service metadata query, returning a description of the
     * service to the client as a structured XML document.  In the generic
     * form this operation returns an InputStream to an actual file in
     * the local file system on the server.  The file name is formed by
     * concatenating the serviceName with "Capabilities.xml".  The file
     * is assumed to be available in the directory specified when the
     * ScsService object was created, e.g., "path/myServiceCapabilities.xml".
     * More generally, the service capabilities do not have to be maintained
     * in a static file and could be dynamically generated (e.g., from a
     * database version), so long as a valid InputStream is returned.
     *
     * @param	params	The fully processed SCS parameter set representing
     *			the request to be processed.
     *
     * @return		An InputStream which can be used to read the 
     *			formatted getCapabilities metadata document.
     *			It is up to the caller to close the returned stream
     *			when the data has been read.
     */
    public InputStream getCapabilities(ScsParamSet params)
	throws FileNotFoundException {

	File capabilities = new File (this.configDir,
	    this.serviceName + "Capabilities.xml");
	return ((InputStream) new FileInputStream(capabilities));
    }


    // -------- Testing -----------

    /**
     * This class provides several unit tests to test the builtin
     * and JDBC-based query capabilities.
     */
    public static void main(String[] args)
	throws DalServerException, IOException, FileNotFoundException {

	if (args.length == 0 || args[0].equals("query")) {
	    // Exercise the ScsService class.
	    ScsService service = new ScsService(null);

	    // Simulate a typical query.
	    ScsParamSet params = new ScsParamSet();
	    params.setValue("REQUEST", "queryData");
	    params.setValue("RA", "180.0");
	    params.setValue("DEC", "1.0");
	    params.setValue("SR", "30");

	    // Create an initial, empty request response object.
	    RequestResponse r = new RequestResponse();

	    // Perform the query. 
	    service.queryData(params, r);

	    // Write out the VOTable to a file.
	    OutputStream out = new FileOutputStream("_output.vot");
	    r.writeVOTable(out);
	    out.close();

	} else if (args.length == 0 || args[0].equals("hdfv2")) {
	    // Perform a simple MySQL query.

	    // Simulate a typical query.
	    ScsParamSet params = new ScsParamSet();
	    params.addParam("serviceName", "scs");
	    params.addParam("serviceClass", "scs");
	    params.addParam("dbType", "mysql");
	    params.addParam("dbName", "nvoss");
	    params.addParam("idColumn", "id");
	    params.addParam("raColumn", "ra");
	    params.addParam("decColumn", "decl");
	    params.addParam("tableName", "hdfv2");
	    params.addParam("jdbcUrl", "jdbc:mysql://localhost:3306/");
	    params.addParam("jdbcDriver", "com.mysql.jdbc.Driver");
	    params.addParam("dbUser", "nvoss");
	    params.addParam("dbPassword", "nvoss08");

	    params.setValue("REQUEST", "queryData");
	    params.setValue("RA", "12.611");
	    params.setValue("DEC", "62.20");
	    params.setValue("SR", "0.005");

	    // Get a new SCS service handler.
	    ScsService service = new ScsService(params);

	    // Create an initial, empty request response object.
	    RequestResponse r = new RequestResponse();

	    // Perform the query. 
	    service.queryData(params, r);

	    // Write out the VOTable to a file.
	    OutputStream out = new FileOutputStream("_output.vot");
	    r.writeVOTable(out);
	    out.close();

	} else if (args.length == 0 || args[0].equals("abell")) {
	    // Perform a simple MySQL query.

	    // Simulate a typical query.
	    ScsParamSet params = new ScsParamSet();
	    params.addParam("serviceName", "scs");
	    params.addParam("serviceClass", "scs");
	    params.addParam("dbType", "mysql");
	    params.addParam("dbName", "nvoss");
	    params.addParam("idColumn", "id");
	    params.addParam("raColumn", "ra");
	    params.addParam("decColumn", "decl");
	    params.addParam("tableName", "abell");
	    params.addParam("jdbcUrl", "jdbc:mysql://localhost:3306/");
	    params.addParam("jdbcDriver", "com.mysql.jdbc.Driver");
	    params.addParam("dbUser", "nvoss");
	    params.addParam("dbPassword", "nvoss08");

	    params.setValue("REQUEST", "queryData");
	    params.setValue("RA", "180.0");
	    params.setValue("DEC", "0.0");
	    params.setValue("SR", "30.0");

	    // Get a new SCS service handler.
	    ScsService service = new ScsService(params);

	    // Create an initial, empty request response object.
	    RequestResponse r = new RequestResponse();

	    // Perform the query. 
	    service.queryData(params, r);

	    // Write out the VOTable to a file.
	    OutputStream out = new FileOutputStream("_output.vot");
	    r.writeVOTable(out);
	    out.close();
	}
    }
}
