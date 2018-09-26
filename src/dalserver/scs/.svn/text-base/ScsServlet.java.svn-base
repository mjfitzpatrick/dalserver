/*
 * ScsServlet.java
 * $ID*
 */

package dalserver.scs;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import dalserver.*;
import dalserver.scs.*;

/**
 * Generic HTTP Servlet for the Simple Cone Search (SCS) protocol.

 * <p>This is a generic HTTP servlet which implements an SCS service
 * using the service implementation provided by the generic {@link
 * dalserver.scs.ScsService} class.  This implements the SCS protocol
 * includng all input parameters plus some DALServer specific custom
 * parameters (e.g. FORMAT) as well as SCS-specific methods, e.g.,
 * for generating the output table.  This base implementation of SCS
 * provides both a working cone search service using a builtin copy
 * of the 110-row Messier catalog, as well as a base class which can
 * be subclassed to generate custom services.
 * 
 * <p>To turn this into a customized SCS service, one could 1) subclass
 * {@link dalserver.scs.ScsService} and modify it as necessary to query
 * a real data collection, and 2) subclass this class (ScsServlet)
 * and override the {@link #newScsService(ScsParamSet) newScsService}
 * method to call the custom ScsService class provided in step 1).
 * The modified Servlet class should then be runnable in any compliant
 * Java application server such as Apache Tomcat.

 * <p>This implementation of SCS also provides a capability to
 * automatically provide an SCS interface for any external database
 * table for which a JDBC interface is available.  Parameters in the
 * servlet "web.xml" file are used to define the JDBC endpoint and
 * driver, database and table to be queried, and the table fields to
 * be used for RA and DEC.
 *
 * @author	Doug Tody
 */
public class ScsServlet extends HttpServlet {
    private static final long serialVersionUID = 1;

    /**
     * The DALServer engine version.
     */
    private String DalServerVersion = "0.7";

    /**
     * The DALServer runtime context for a request.
     */
    DalContext dalContext = null;

    /**
     * The name of the service or servlet instance.  This is used to
     * construct file names for service-specific configuration files.  
     * For example, if we have two different SCS service instances
     * these should have distinct service names.  Defined locally in
     * the servlet deployment descriptor (web.xml).  Optional.
     */
    protected String serviceName;

    /**
     * The DAL service class to which the service instance belongs,
     * e.g., SCS, SIAP, SSAP.  Defined locally in the servlet deployment
     * descriptor (web.xml).  Optional.
     */
    protected String serviceClass;

    /**
     * The default service interface version supported by the service
     * if no version number is explicitly negotiated.  This is usually
     * the highest standard interface version supported by the service.
     * Defined locally in the servlet deployment descriptor (web.xml).
     */
    protected String serviceVersion;

    /**
     * The type of database to be accessed, e.g., "MySQL", "PostgreSQL",
     * or "builtin".  For the most part JDBC hides the difference between
     * DBMS implementations, but not entirely.
     */
    protected String dbType;

    /**
     * The name of the database to be accessed.  By "database" we mean a
     * SQL catalog or schema containing tables.
     */
    protected String dbName;

    /**
     * The name of the table to be accessed by the SCS service.
     */
    protected String tableName;

    /**
     * The JDBC URL of the database server, e.g., "jdbc:mysql://<host>:3306/".
     */
    protected String jdbcUrl;

    /**
     * The address of the JDBC driver to be used, e.g.,
     * "com.mysql.jdbc.Driver".
     */
    protected String jdbcDriver;

    /**
     * The user name to be used to login to the DBMS.
     */
    protected String dbUser;

    /**
     * The password to be used to login to the DBMS.  This should not be
     * a real user password, but rather the password of a DBMS account
     * used to provide low security, read-only access the database.
     */
    protected String dbPassword;


    // Private data.
    // ----------------
    private final int BUFSIZE = 8192;


    // ---------- Servlet Methods -------------------

    /** Servlet startup and initialization. */
    public void init(ServletConfig config) throws ServletException {
	super.init(config);
        // Add any servlet initialization here.
    }

    /** Servlet shutdown. */
    public void destroy() {
        // Add any servlet shutdown here.
    }

    /** Return a brief description of the service.  */
    public String getServletInfo() {
        return ("Implements the Simple Cone Search protocol" +
	    " version=" + this.serviceVersion);
    }

    /**
     * Handle a GET or POST request.  Includes all operations for the
     * given service.
     */
    public void doGet(HttpServletRequest servletRequest, HttpServletResponse servletResponse)
        throws ServletException, IOException {

	// Internal data.
	String operation = null;
	ScsParamSet params = null;
	RequestResponse response = null;
	boolean error = false;

	try {
	    params = new ScsParamSet();
	    response = new RequestResponse();
	    ServletContext servletContext = getServletContext();
	    ServletConfig servletConfig = getServletConfig();

	    this.dalContext = new DalContext((ParamSet)params, response,
		servletRequest, servletContext, servletConfig);

	    // Get service config/context parameter values.  If the parameters
	    // are not defined externally they are added within the given
	    // default values.

	    serviceName = params.getSysValue("serviceName", "scs");
	    serviceClass = params.getSysValue("serviceClass", "scs");
	    serviceVersion = params.getSysValue("serviceVersion", "1.0");
	    dbType = params.getSysValue("dbType", "builtin");
	    dbName = params.getSysValue("dbName", "dalserver");
	    tableName = params.getSysValue("tableName", "messier");
	    jdbcUrl = params.getSysValue("jdbcUrl", null);
	    jdbcDriver = params.getSysValue("jdbcDriver", null);
	    dbUser = params.getSysValue("dbUser", "dalserver");
	    dbPassword = params.getSysValue("dbPassword", "");

	    // Identify the service elements and versions.
	    Param p = new Param("ServiceEngine", serviceName +
		": SCS version " + serviceVersion +
		" DALServer version " + DalServerVersion);
	    p.setLevel(ParamLevel.EXTENSION);
	    params.addParam(p);

	} catch (DalServerException ex) {
	    error = this.errorResponse(params, servletResponse, ex);

	} finally {
	    if (error) {
		params = null;
		return;
	    }
	}

	// Handle VERSION and REQUEST.
	try {
	    // Verify the service version matches, if specified.
	    Param p = params.getParam("VERSION");
	    String clientVersion = p.stringValue();
	    if (clientVersion != null)
		if (!clientVersion.equalsIgnoreCase(serviceVersion))
		    throw new DalServerException( "protocol version mismatch");

	    // Get the service operation to be performed.
	    // -- This is not used or required for SCS
	 
	    operation = params.getParam("REQUEST").stringValue();
	    if (operation == null)
		operation = "queryData";

	} catch (DalServerException ex) {
	    error = this.errorResponse(params, servletResponse, ex);
	} finally {
	    if (error) {
		params = null;
		return;
	    }
	}

	/*
	 * ------------------------------------------------------------
	 * Get the service implementation.  Override the newScsService
	 * method below to implement a new data service.  The rest of
	 * this code should not normally have to be modified.
	 * ------------------------------------------------------------
	 */
        ScsService service = newScsService(params);


	// -------- QUERYDATA operation. --------

        if (operation.equalsIgnoreCase("queryData")) {
	    RequestResponse requestResponse = dalContext.response;
	    ServletOutputStream out = null;

	    try {
		// Execute the queryData operation.
		service.queryData(params, requestResponse);

		String format =
		    params.getValue("format", "votable");
		String responseFormat =
		    params.getValue("responseformat", format);

		boolean htmlOut=false, textOut=false, csvOut=false;
		if (responseFormat != null) {
		    if (responseFormat.equalsIgnoreCase("html")) 
			htmlOut = true;
		    else if (responseFormat.equalsIgnoreCase("text")) 
			textOut = true;
		    else if (responseFormat.equalsIgnoreCase("csv")) 
			csvOut = true;
		}

		// Set up the output stream.
		if (htmlOut)
		    servletResponse.setContentType("text/html");
		else if (textOut)
		    servletResponse.setContentType("text/plain");
		else if (csvOut)
		    servletResponse.setContentType("text/plain");
		else
		    servletResponse.setContentType("text/xml;x-votable");

		servletResponse.setBufferSize(BUFSIZE);
		out = servletResponse.getOutputStream();

		// Write the query response output.
		if (htmlOut)
		    requestResponse.writeHTML((OutputStream)out);
		else if (textOut)
		    requestResponse.writeText((OutputStream)out);
		else if (csvOut)
		    requestResponse.writeCsv((OutputStream)out);
		else
		    requestResponse.writeVOTable((OutputStream)out);

	    } catch (DalServerException ex) {
		error = this.errorResponse(params, servletResponse, ex);

	    } finally {
		if (out != null) out.close();
		requestResponse = null;
	    }

	// -------- GETCAPABILITIES operation. --------

        } else if (operation.equalsIgnoreCase("getCapabilities")) {
	    InputStream inStream = service.getCapabilities(params);
	    BufferedReader in =
		new BufferedReader(new InputStreamReader(inStream));

	    // Set up the output stream.
	    servletResponse.setContentType("text/xml");
	    servletResponse.setBufferSize(BUFSIZE);
	    PrintWriter out = servletResponse.getWriter();

	    // Return the document to the client.
	    for (String line;  (line = in.readLine()) != null;  )
		out.println(line);

	    out.close(); in.close();
	    inStream.close();


        } else {
	    DalServerException ex = new DalServerException(
		"unrecognized operation " + "["+operation+"]");
	    error = this.errorResponse(params, servletResponse, ex);
	}
    }


    // ---------- Generic Service Implementation -------------------

    /**
     * Get a new ScsService instance.  By default the generic dataless
     * {@link dalserver.scs.ScsService} class is used.  To build a real data
     * service, subclass ScsServlet and replace the newScsService method
     * with one which calls a custom replacement for the builtin generic
     * ScsService class.
     *
     * @param	params	The input and service parameters.
     */
    public ScsService newScsService(ScsParamSet params) {
	return (new ScsService(params));
    }


    // ---------- Private Methods -------------------

    /**
     * Handle an exception, returning an error response to the client.
     * This version return a VOTable.  If any further errors occur while
     * returning the error response, a servlet-level error is returned
     * instead.
     *
     * @param	params		The input service parameter set.
     *
     * @param	response	Servlet response channel.  This will be
     *				reset to ensure that the output stream is
     *				correctly setup for the error response.
     *
     * @param	ex		The exception which triggered the error
     *				response.
     */
    @SuppressWarnings("unchecked")
    private boolean errorResponse(ScsParamSet params,
	HttpServletResponse servletResponse, Exception ex)
	throws ServletException {

	boolean error = true;
	ServletOutputStream out = null;
	RequestResponse r = null;
	TableInfo info = null;

	try {
	    // Set up a response object with QUERY_STATUS=ERROR. */
	    r = new RequestResponse();
	    r.setType("results");

	    String id, key = "QUERY_STATUS";
	    info = new TableInfo(key, "ERROR");
	    if (ex.getMessage() != null)
		info.setContent(ex.getMessage());
	    r.addInfo(key, info);
	    r.echoParamInfos(params);

	    // Set up the output stream.
	    servletResponse.resetBuffer();
	    servletResponse.setContentType("text/xml;x-votable");
	    servletResponse.setBufferSize(BUFSIZE);
	    out = servletResponse.getOutputStream();

	    // Write the output VOTable.
	    r.writeVOTable((OutputStream)out);

	} catch (Exception ex1) {
	    throw new ServletException(ex1);

	} finally {
	    if (out != null)
		try {
		    out.close();
		} catch (IOException ex2) {
		    throw new ServletException(ex2);
		}
	    if (r != null)
		r = null;
	    if (info != null)
		info = null;
	}

	return (error);
    }
}
