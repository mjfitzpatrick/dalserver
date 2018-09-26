/*
 * SsapServlet.java
 * $ID*
 */

package dalserver.ssa;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import dalserver.*;

/**
 * Generic HTTP Servlet for the Simple Spectral Access (SSA) protocol.
 * 
 * To configure a data service for a spectrum data collection, a Spectrum
 * table (DBMS table) describing each Spectrum dataset is required.  The
 * generic SSA service implementation uses this metadata to service discovery
 * queries and access Spectrum datasets.  An additional Spectrum Data table
 * may optionally be provided containing the actual data (spectral coordinate,
 * flux, and error vectors etc.).  Finally the data service instance must
 * be configured via the DALServer service configuration mechanism.
 *
 * In most cases this purely data-driven approach will be sufficient to
 * provide a SSA service for a Spectrum data collection.  In more advanced
 * cases it is possible to subclass the methods of the generic SSA service
 * to provide customized functionality.
 */
public class SsapServlet extends HttpServlet {
    private static final long serialVersionUID = 1;

    // Some of this metadata is global and not specific to a given
    // service, and should be moved to some common location for use
    // by all services.

    /**
     * The IVOA authority ID for the current publisher/archive.
     * The value should be overriden in the local configuration files.
     */
    protected String authorityID = "ivo://myarchive";

    /**
     * The DALserver engine version.
     */
    private String DalServerVersion = "0.7";
    
    /**
     * The DALServer runtime context for a request.
     */
    DalContext dalContext = null;

    /**
     * The name of the service or servlet instance.  This is used to
     * construct file names for service-specific configuration files.  
     * For example, if we have two different SSA service instances
     * these should have distinct service names.  Defined locally in
     * the servlet deployment descriptor (web.xml).  Optional.
     */
    protected String serviceName;

    /**
     * The default service interface version supported by the service
     * if no version number is explicitly negotiated.  This is usually
     * the highest standard interface version supported by the service.
     * Defined locally in the servlet deployment descriptor (web.xml).
     */
    protected String serviceVersion;

    /**
     * The default service interface version supported by the service
     * if no version number is explicitly negotiated.  This is usually
     * the highest standard interface version supported by the service.
     * Defined locally in the servlet deployment descriptor (web.xml).
     */
    protected String minorVersion;

    /**
     * The directory on the local server machine used to store runtime
     * service configuration files, e.g., for the getCapabilities method.
     * Defined locally in the servlet deployment descriptor (web.xml).
     */
    protected String configDir;

    /**
     * The default root directory (referenced by URL) of the image
     * repository, used to store archival image datasets to be returned
     * by the built-in accessData method.  This may or may not be used
     * depending upon the service configuration.  Optional.
     */
    protected String dataDirURL;

    /**
     * The content (MIME) type to be used for any datasets retrieved from
     * the dataDir.  If set to DYNAMIC, the service will attempt to
     * dynamically determine the content type of each file.
     */
    protected String contentType;
    
    /**
     * The logical name of the tasking daemon, if any.
     */
    protected String tdName;

    /**
     * The network location of the tasking daemon, if any.
     */
    protected String tdLocation;

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
    private SsapParamSet params = null;
    private TaskManager taskman = null;


    // ---------- Servlet Methods -------------------

    /** Servlet startup and initialization. */
    public void init(ServletConfig config) throws ServletException {
	super.init(config);
	
        // Create a new, not yet connected Task manager.
	this.taskman = new TaskManager();
    }

    /** Servlet shutdown. */
    public void destroy() {
	
        // Shutdown the Task manager and any open connections.  The Task
	// manager instance lasts as long as the servlet remains loaded.
	// Once a daemon is connected it normally remains connected for
	// the lifetime of the servlet.

	if (this.taskman != null) {
	    this.taskman.close();
	    this.taskman = null;
	}
    }

    /** Return a brief description of the service.  */
    public String getServletInfo() {
        return ("Implements the Simple Spectrum Access protocol V1 and V2");
    }

    /**
     * Handle a GET or POST request.  Includes all operations for the
     * given service.  The HTTP resource including /sync or /async has
     * already been processed by this point; we just see the request
     * parameters here.
     */
    public void doGet(HttpServletRequest servletRequest, HttpServletResponse servletResponse)
        throws ServletException, IOException {

	// Internal data.
	String operation = null;
	SsapParamSet params = null;
	RequestResponse response = null;
	boolean error = false;

	try {
	    params = new SsapParamSet();
	    response = new RequestResponse();
	    ServletContext servletContext = getServletContext();
	    ServletConfig servletConfig = getServletConfig();

	    this.dalContext = new DalContext((ParamSet)params, response,
		servletRequest, servletContext, servletConfig);

	    // Get service config/context parameter default values.  If a
	    // parameter is not yet defined, it is created with the given
	    // default value.

	    serviceName = params.getSysValue("serviceName", "ssap");
	    serviceVersion = params.getSysValue("serviceVersion", "1.1");
	    minorVersion = params.getSysValue("minorVersion", "generic");
	    tdName = params.getSysValue("tdName", "localhost");
	    tdLocation = params.getSysValue("tdLocation", null);
	    configDir = params.getSysValue("configDir", "/tmp");
	    dataDirURL = params.getSysValue("dataDirURL", "/tmp");
	    contentType = params.getSysValue("contentType", "text/xml;content=x-votable");

	    // Identify the service elements and versions.
	    Param p = new Param("ServiceEngine", serviceName +
		": SSAP version " + serviceVersion +
		" (" + minorVersion + ")" +
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

	    if (p.isSet() && clientVersion != null) {
		if (clientVersion.startsWith("1")) {
		    this.serviceVersion = clientVersion;
		    params.setValue("serviceVersion", clientVersion);
		} else if (clientVersion.startsWith("2")) {
		    this.serviceVersion = clientVersion;
		    params.setValue("serviceVersion", clientVersion);
		} else if (!clientVersion.equalsIgnoreCase(this.serviceVersion))
		    throw new DalServerException( "protocol version mismatch");

		// Update the service engine protocolversion.
		String engine = serviceName +
		    ": SSAP version " + serviceVersion +
		    " (" + minorVersion + ")" +
		    " DALServer version " + DalServerVersion;
		params.setValue("ServiceEngine", engine);
	    }

	    // Get the service operation to be performed.
	    p = params.getParam("REQUEST");
	    operation = p.stringValue();
	    if (operation == null)
		throw new DalServerException("No service operation specified");

	    // Set VERB=1 as the default for SSAV1
	    if (serviceVersion.startsWith("1")) {
		String verbosity = params.getValue("VERB");
		if (verbosity == null)
		    params.addParam("VERB", verbosity = "1");
	    }

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
	 * Get the service implementation.  Override the newSsapService
	 * method below to implement a new data service.  The rest of
	 * this code should not normally have to be modified.
	 * ------------------------------------------------------------
	 */
        SsapService service = newSsapService(params, taskman);


	// -------- QUERYDATA operation. --------

        if (operation.equalsIgnoreCase("queryData")) {
	    RequestResponse requestResponse = null;
	    ServletOutputStream out = null;

	    try {
		// Execute the queryData operation.
		requestResponse = new RequestResponse();
		service.queryData(params, requestResponse);

		String responseFormat =
		    params.getValue("responseformat", "votable");

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


	// -------- ACCESSDATA operation. --------

        } else if (operation.equalsIgnoreCase("accessData")) {
	    RequestResponse requestResponse = new RequestResponse();
	    InputStream inStream = null;
	    String contentType = null;
	    String contentLength = null;
	    String contentDisp = null;

	    try {
		// Call the service's accessData method.
		inStream = service.accessData(params, requestResponse);

		// Get the dataset content type.
		contentType = params.getValue("datasetContentType");
		if (contentType == null)
		    contentType = "text/xml;content=x-votable";
		contentType = contentType.toLowerCase();

		// Get the dataset content length (null if unknown).
		contentLength = params.getValue("datasetContentLength");
		
		// Get the dataset content disposition, e.g., filename.
		contentDisp = params.getValue("datasetContentDisposition");

	    } catch (DalServerException ex) {
		if (this.errorResponse(params, servletResponse, ex))
		    return;
	    }

	    // Set up the output stream and return the dataset.
	    // For dynamically generated or streaming data, the content
	    // length is unknown and should be omitted.

	    servletResponse.setBufferSize(BUFSIZE);
	    servletResponse.setContentType(contentType);
	    if (contentLength != null)
		servletResponse.setContentLength(
		    new Integer(contentLength).intValue());
	    if (contentDisp != null)
		servletResponse.setHeader("Content-Disposition",
		    "attachment;filename=" + contentDisp);

	    if (inStream != null) {
		// Write a binary-formatted data stream.

		ServletOutputStream out = servletResponse.getOutputStream();
		byte[] b = new byte[BUFSIZE];
		int count;

		while ((count = inStream.read(b, 0, BUFSIZE)) > 0)
		    out.write(b, 0, count);

		out.close();
		service.accessDataClose(inStream);
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
     * Get a new SsapService instance.  By default the generic
     * {@link dalserver.ssa} class is used; this provides support for
     * typical data-driven user data services.  To implement a more
     * advanced custom data service, subclass SsapServlet and replace
     * the newSsapService method with one which calls a custom replacement
     * for the builtin generic SsapService class.
     * 
     * @param   params  The input and service parameters.
     * @param   taskman The Task manager for the servlet session.
     */
    public SsapService
    newSsapService(SsapParamSet params, TaskManager taskman) {
	return (new SsapService(params, taskman));
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
    private boolean errorResponse(SsapParamSet params,
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

	    // Echo the query parameters as INFOs in the query response.
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
