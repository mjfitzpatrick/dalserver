/*
 * Config.java
 * $ID*
 */

package dalserver;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * The Config class manages the DALServer Web-app local configuration,
 * including global framework configuration, and configuration of all
 * locally defined services.
 *
 * The DALServer Web-app is packaged as a single WAR file containing
 * generic framework code for all supported VO services.  A configured
 * instance of the Web-app may support any number of services, each of
 * which consists of one or more servlets, each serving a distinct HTTP
 * endpoint or set of endpoints/resources.  The DALServer Web-app is fully
 * data-driven; all configuration data and all served data content is stored
 * externally to the deployed Web-app, and the Web-app does not have to be
 * rebuilt to add new service instances.
 *
 * The Config class allows all service configuration info to be expressed
 * concisely in simple text files, describing the configuration of the
 * framework at a particular site.  Since all configuration data is stored
 * separately from the Web-app a single framework WAR file can be prepared and
 * used unchanged regardless of the local configuration.  The DALServer WAR
 * can be updated to a new version with no effect on the local configuration.
 *
 * When the configuration is reloaded, the Config class reads the
 * configuration files stored in an external directory, and updates the
 * web.xml file used internally to define the Web-app runtime configuration.
 * Some configuration data may also be written to runtime resource files
 * (e.g., Java property files or custom class files) stored in the deployed
 * Web-app.  All configuration information is cached in the web.xml and
 * resource files where it remains unchanged until the configuration is
 * subsequently reloaded.
 *
 * Various techniques are possible to trigger a configuration reload.
 * The recommended technique is to invoke the "reload" servlet.  A password
 * may be configured to limit reload permission to a service administrator.
 *
 * A root configuration file ("server.conf" by default) is read to reload
 * the configuration.  The root configuration file defines the Web-app, and
 * global context parameters shared by all services.  It also lists all
 * services and global parameter sets to be included in the configuration.
 * Each of these is defined in a separate file.  As the configuration is
 * loaded the configuration mechanism processes all active, directly or
 * indirectly referenced configuration files, starting with the root
 * configuration file.
 *
 * The structure of a service configuration file is as follows:
 *
 * 	[service]
 * 	  service-wide config parameters
 *
 * 	[servlet]
 * 	  servlet definition parameters
 * 	[init-params]
 * 	  servlet initialization parameters
 * 	      ...
 * 	[servlet]             # optional
 * 	  servlet definition parameters
 * 	[init-params]
 * 	  servlet initialization parameters
 *
 *	[servlet-mapping]
 *	[servlet-mapping]
 *	      ...
 *
 * Both Web-app context parameters and servlet init parameters may be used
 * to pass parameters to servlet code.  However, services (e.g. TAP) that
 * use external class libraries may not provide called classes access to
 * parameters passed in this way.  In such a case, or whenever WebApp-wide
 * named global parameter sets are required, the Java property file mechanism
 * may be used instead to pass global parameters to service code.  Java
 * property file psets may be used to pass parameters to either service code,
 * or globally to any code that recognizes the pset by name.  The DALServer
 * parameter set subsystem allows either approach to be used, transparent to
 * the service code.
 *
 * Parameters defined in the [service] context of a service configuration are
 * represented via Java property files that can be read directly by the
 * DALServer parameter mechanism, hence reliably passed whether not the
 * application has access to the servlet definition and init parameters.
 * These parameters are local to the service and may be accessed transparently
 * by the service.  Native DALServer services can also read parameters
 * defined as Web-app context-params or servlet init-params.  Which form of
 * parameter is used to configure a service is transparent to the service code.
 *
 * The scope of global parameter sets is the entire Web application.  The
 * parameter set name must be unique within the scope of the Web-app (but may
 * be a pathname relative to the Web-app root "classes" directory).  Service
 * code must know the name of a global pset to be able to access it, however
 * the name of a global pset to be used by a service instance can be passed
 * as a service parameter.
 *
 * This version of the Config class was modified from the original to
 * support multiple servlets per service, and the optional use of Java
 * property files to pass configuration parameters.
 *
 * @version	1.1, 26-Mar-2015
 * @author	Doug Tody
 */
public class Config extends HttpServlet {

    // Some built-in defaults; normally set in external configuration.
    private String configDir = "/opt/services/dalserver";
    private String configFile = "server.conf";
    private String dalServerConfig = "server.conf";
    private String webAppConfig = null;
    private String webAppClasses = null;
    ServletContext context;

    // Private data.
    private final String GLOBALS = "dalserver.properties";
    private final int BUFSIZE = 8192;
    private final int MAXLINE = 80;

    private ParamSet entityList = null;
    private int num_entities = 0;
    private int num_services = 0;
    private int num_servlets = 0;
    private int num_globalPsets = 0;
    private int num_fileCopies = 0;
    private int nContextParams = 0;
    private int nWebappParams = 0;


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
        return ("DALServer framework auto-configuration servlet");
    }

    /**
     * Handle a GET or POST request.  Includes all operations for the
     * given service or servlet (config reload in this case).
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

	// Internal data.
	this.context = getServletContext();
	ServletConfig config = getServletConfig();
	String operation = null;
	boolean error = false;

        HttpSession session = request.getSession(true);
        ResourceBundle messages =
            (ResourceBundle) session.getAttribute("messages");

	// Construct the service parameter set.  This is a single ParamSet
	// containing all context, config, and request parameters.  Any
	// locally defined context/config parameters are automatically
	// passed through.

	RequestParams reqHandler = null;
	ParamSet params = null;
	entityList = new ParamSet();

	try {
	    Enumeration contextPars = context.getInitParameterNames();
	    Enumeration configPars = config.getInitParameterNames();
	    params = new ParamSet();

	    // Get the servlet context parameters.
	    while (contextPars.hasMoreElements()) {
		String name = (String) contextPars.nextElement();
		String value = (String) context.getInitParameter(name);

		Param p = params.getParam(name);
		if (p == null) {
		    params.addParam(p = new Param(name, value));
		    p.setLevel(ParamLevel.SERVICE);
		} else
		    p.setValue(value);
	    }

	    // Get the servlet config parameters.  If already defined,
	    // these values will override any context parameter values.

	    while (configPars.hasMoreElements()) {
		String name = (String) configPars.nextElement();
		String value = (String) config.getInitParameter(name);

		Param p = params.getParam(name);
		if (p == null) {
		    params.addParam(p = new Param(name, value));
		    p.setLevel(ParamLevel.SERVICE);
		} else
		    p.setValue(value);
	    }

	    // Get the request parameters.
	    String[] omit = { "configKey" };
	    reqHandler = new RequestParams();
	    reqHandler.getRequestParams(request, params, omit);

	} catch (DalServerException ex) {
	    error = this.errorResponse(params, response, ex);
	} finally {
	    reqHandler = null;
	    if (error) {
		params = null;
		return;
	    }
	}

	// Execute the service framework configuration reload.
	// This overwrites the Web-app web.xml file, which is automatically
	// reloaded (if enabled) by the Web application server.

	try {
	    num_entities = reload(params, response, entityList);
	} catch (DalServerException ex) {
	    error = this.errorResponse(params, response, ex);
	}

	// Set up the output stream.
	response.setBufferSize(BUFSIZE);
	response.setContentType("text/plain");
	PrintWriter out = response.getWriter();

	// Briefly summarize the entities created.
	for (Param p : entityList) {
	    String pname = p.getName();

	    if (pname.equals("WebApp")) {
		out.println(pname + ": " + p.stringValue());
		out.println("  " +
		    nContextParams + " Webapp system params, " +
		    nWebappParams + " DALServer global params");
		out.println();
	    } else {
		String line;
		if (pname.startsWith("--"))
		    line = "  " + pname.substring(2) + "\t";
		else
		    line = pname + "\t";
		if (pname.length() < 8)
		    line += "\t";
		line += p.stringValue();
		out.println(line);
	    }
	}

	out.println();
	out.println("Successfully created " + num_services + " services and " +
	    num_servlets + " servlets");
	out.println("Successfully created " + num_globalPsets +
	    " global property files");
	out.println("Successfully copied " + num_fileCopies + " files");
	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date(); out.println(dateFormat.format(date));
	out.flush();
	entityList = null;
    }

    /**
     * Reload the current framework configuration.
     *
     * @param	params		Parameter set for HTTP reload request
     * @param	response	Response context
     * @param	entityList	Receives list of created entities
     *
     * The root input configuration file is read, along with any files
     * it references, and a Web-app file (web.xml) is generated and
     * written to the deployed Web-app, along with any runtime property
     * files defined by the configuration.
     */
    public int reload (ParamSet params, HttpServletResponse response,
	ParamSet entityList) throws DalServerException, ServletException {

	String configKey = null;
	int nentities = 0;
	boolean error;
	Param p;

	// Apply password protection if enabled.
	p = params.getParam("configKey");
	if (p != null && !p.stringValue().equals("none")) {
	    configKey = p.stringValue();

	    // If so, check for a valid password (param "key").
	    p = params.getParam("key");
	    if (p == null || !p.stringValue().equals(configKey))
		throw new DalServerException("missing or invalid password");
	}

	// Get the input configuration file location.
	p = params.getParam("configDir");
	if (p != null)
	    this.configDir = p.stringValue();
	p = params.getParam("configFile");
	if (p != null)
	    this.configFile = p.stringValue();

	// Get the file path for the output web.xml file.
	p = params.getParam("webAppConfig");
	if (p != null)
	    this.webAppConfig = context.getRealPath(p.stringValue());
	else
	    throw new DalServerException("webAppConfig not defined");

	// Get the path to the output directory for Java properties files.
	p = params.getParam("webAppClasses");
	if (p != null)
	    this.webAppClasses = context.getRealPath(p.stringValue());
	else
	    throw new DalServerException("webAppClasses not defined");

	try {
	    String configPath = configDir + "/" + configFile;
	    String tempfile = webAppConfig + ".temp";
	    PrintWriter out;

	    // Write the new web.xml to a temporary file.
	    out = new PrintWriter(new FileWriter(tempfile));
	    nentities = compile(out, configPath, entityList);

	    // If this succeeds, install the new web.xml.
	    File file = new File(tempfile);
	    File dest = new File(webAppConfig);
	    file.renameTo(dest);

	} catch (DalServerException ex) {
	    error = this.errorResponse(params, response, ex);
	} catch (IOException ex) {
	    error = this.errorResponse(params, response, ex);
	}

	return (nentities);
    }


    /**
     * Compile the current framework configuration in web.xml format.
     *
     * @param	out		Output stream
     * @param	config		Root input configuration file
     * @param	entityList	Paramset describing the entities
     *
     * The root input configuration file is read, along with any files
     * it references, and the content for a Web-app definition file (web.xml)
     * is generated and written to the output stream.  The number of entities
     * (services, servlets, or property files) processed is returned as the
     * function value, and a description of each entities created is written
     * to the entityList pset.  Only the web.xml content is generated here;
     * output of any Java property files happens elsewhere.
     */
    public int compile (PrintWriter out, String config, ParamSet entityList)
	throws DalServerException {

	ArrayList<ParamSet> psetList = new ArrayList<ParamSet>();
	String configDir;
	int nentities = 0;
	ParamSet pset;

	// Extract the configuration directory from the config file path.
	int off = config.lastIndexOf('/');
	if (off >= 0)
	    configDir = config.substring(0, off);
	else
	    configDir = "./";

	// Parse the root server config file.
	try {
	    if (parseIni(config, psetList) <= 0)
		throw new DalServerException("config file not found (" +
		    config + ")");
	} catch (FileNotFoundException ex) {
	    throw new DalServerException("config file not found (" +
		config + ")");
	}

	// Output the lead portion of the web.xml file.
	out.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
	out.println("<web-app>");
	int indent = 0;

	indent += 4;
	pset = findPset(psetList, "web-app", 0);
	nContextParams += pset.size();

	putElement(out, "display-name", pset.getValue("display-name"), indent);
	entityList.addParam("WebApp", pset.getValue("display-name"));
	putElement(out, "description", pset.getValue("description"), indent);

	out.println();
	putText(out, "<!-- Web-App Context Initialization Parameters. -->", indent);
	out.println();

	// Output the Web-app context initialization parameters.
	pset = findPset(psetList, "context-params", 0);
	if (pset != null ) {
	    for (Param p : pset) {
		putElement(out, null, "<context-param>", indent);
		putElement(out, "param-name", p.getName(), indent + 2);
		putElement(out, "param-value", p.stringValue(), indent + 2);
		putElement(out, null, "</context-param>", indent);
		nContextParams++;
	    }
	}

	// Output the Web-app application parameters.  These go to a Java
	// properties file rather than to web.xml, then we resume output to
	// web.xml.

	pset = findPset(psetList, "webapp-params", 0);
	if (pset != null) {
	    String fname = webAppClasses + "/" + GLOBALS;
	    if (writeProperties(pset, fname) > 0)
		nentities++;
	    nWebappParams = pset.size();
	}

	// Execute any file copies.  Here, the "param name" is the name of
	// the local file relative to the configuration directory, and the
	// param "value" is the destination file pathname relative to the
	// Web-app root directory.  The files to be copied may be anything;
	// typical examples are the Web-app context.xml, custom Java plugin
	// classes, Java property files, etc.

	pset = findPset(psetList, "file-copies", 0);
	if (pset != null) {
	    for (Param p : pset) {
		String source = configDir + "/" + p.getName();
		String dest = context.getRealPath(p.stringValue());

		// Verify that the input file exists.
		File file = new File(source);
		if (!file.isFile())
		    throw new DalServerException("file does not exist(" +
			source + ")");

		// Verify that a destination was specified.
		if (dest == null)
		    throw new DalServerException("file copy error (" +
			source + ")");

		// Copy the file.
		try {
		    copyFile(source, dest);
		} catch (Exception ex) {
		    throw new DalServerException("file copy error (" +
			ex.getMessage() + ")");
		}

		num_fileCopies++;
	    }
	}

	out.println();
	putText(out, "<!-- Service Definitions. -->", indent);
	out.println();

	// Output the service configuration for each service.  A service
	// will usually consist of a number of service global context
	// parameters, output as a per-service Java property file, and one
	// or more servlets, configured via servlet definition and
	// initialization parameters.  Optional elements include table
	// configuration and named Web-app wide global parameter sets
	// (Java property files).

	ParamSet services = findPset(psetList, "services", 0);
	if (services == null)
	    throw new DalServerException("missing [services] context");

	for (Param service : services) {
	    boolean hasTableConfig = false;
	    int nparams = 0;

	    String serviceName = service.getName();
	    String serviceClass = null;
	    String servletName = null;

	    // Parse the service configuration.
	    String servconf = configDir + "/" + serviceName;
	    if (!servconf.endsWith(".conf"))
		servconf += ".conf";
	    ArrayList<ParamSet> servicePsets = new ArrayList<ParamSet>();

	    try {
		if (parseIni(servconf, servicePsets) <= 0) {
		    out.flush();
		    throw new DalServerException("config file not found (" +
			servconf + ")");
		}
	    } catch (FileNotFoundException ex) {
		out.flush();
		throw new DalServerException("config file not found (" +
		    servconf + ")");
	    }

	    // Process the overall service definition context.
	    pset = findPset(servicePsets, "service", 0);
	    if (pset == null)
		throw new DalServerException("missing [service] context (" +
		    serviceName + ")");
	    nparams = pset.size();

	    // The service name defaults to that of the conf file, but may be
	    // renamed with an explicit serviceName parameter.

	    String serviceLocalName = pset.getValue("serviceName");
	    if (serviceLocalName != null)
		serviceName = serviceLocalName;
	    else {
		serviceLocalName = pset.getValue("service-name");
		if (serviceLocalName != null)
		    serviceName = serviceLocalName;
	    }
	    pset.setParam("serviceName", serviceName);

	    // Allow "service-class" as an alias for "serviceClass".
	    serviceClass = pset.getValue("serviceClass");
	    if (serviceClass == null) {
		String service_class = pset.getValue("service-class");
		if (service_class != null)
		    serviceClass = service_class;
		else
		    serviceClass = "unknown";
		pset.addParam("serviceClass", serviceClass);
	    }

	    // Service table configuration parameters.  These are
	    // passed to the service as another block of parameters.
	    // Not all services will have a table configuration section.

	    Param p = pset.getParam("tableConfig");
	    if (p != null && p.isSet()) {
		String tableConfig = p.stringValue();
		putTableConfig(tableConfig, configDir, pset);
		hasTableConfig = true;
	    }

	    // Write the service global params as a properties file.
	    String fname =
		webAppClasses + "/" + serviceName + ".properties";
	    if (writeProperties(pset, fname) > 0)
		nentities++;

	    // Output service description.
	    String sdesc =
		serviceClass + " service with " + nparams + " parameters";
	    if (hasTableConfig)
		sdesc += " tableconfig";
	    entityList.addParam(serviceName, new String(sdesc));
	    num_services++;

	    // Process the remaining service definition contexts.
	    for (ParamSet ps : servicePsets) {
		String context = ps.getPsetClass();

		if (context.equalsIgnoreCase("servlet")) {
		    // Output a servlet definition.
		    putText(out, "<servlet>", indent);
		    servletName = ps.getValue("servlet-name");
		    indent += 2;
		    nparams = 0;

		    // Servlet top level attributes. 
		    for (Param sp : ps)
			putElement(out, sp.getName(), sp.stringValue(), indent);
		    num_servlets++;

		} else if (context.equalsIgnoreCase("init-params")) {
		    // Every servlet definition must include an init-params
		    // context, and it must immediately follow the servlet
		    // context.

		    out.println();
		    putText(out, "<!-- Servlet Parameters. -->", indent);
		    out.println();

		    // Add the service name as a servlet init-param, so that
		    // the servlet init code for can find the service
		    // configuration parameters for the containing service.

		    putElement(out, null, "<init-param>", indent);
		    putElement(out, "param-name", "serviceName", indent + 2);
		    putElement(out, "param-value", serviceName, indent + 2);
		    putElement(out, null, "</init-param>", indent);

		    // Servlet init (servlet-internal) parameters. 
		    for (Param ip : ps) {
			putElement(out, null, "<init-param>", indent);
			putElement(out, "param-name", ip.getName(), indent + 2);
			putElement(out, "param-value", ip.stringValue(), indent + 2);
			putElement(out, null, "</init-param>", indent);
			nparams++;
		    }

		    indent -= 2;
		    putText(out, "</servlet>", indent);

		    if (servletName != null) {
			String pdesc = "servlet with " + nparams + " parameters";
			entityList.addParam("--" + servletName, new String(pdesc));
			nentities++;
		    }

		} else if (context.equalsIgnoreCase("servlet-mapping")) {
		    // Output the servlet to HTTP endpoint mapping element.
		    out.println();
		    putText(out, "<servlet-mapping>", indent);
		    indent += 2;

		    for (Param map : ps) {
			// Check for a missing leading "/" in the servlet mapping.
			// This would cause the Web-app to fail to reload.

			String pname = map.getName();
			String pvalue = map.stringValue();
			if (pname.equalsIgnoreCase("url-pattern")) {
			    if (!pvalue.startsWith("/")) {
				out.flush();
				throw new DalServerException(
				"malformed url-pattern (" + pvalue + ")");
			    }
			}

			putElement(out, pname, pvalue, indent);
		    }

		    indent -= 2;
		    putText(out, "</servlet-mapping>", indent);
		    out.println();
		}
	    }
	}

	// Output any global parameter sets.
	ParamSet globalParams = findPset(psetList, "global-params", 0);
	if (globalParams != null) {
	    num_globalPsets =
		writePsets(globalParams, webAppClasses, entityList);
	    nentities += num_globalPsets;
	}
    
	// Output the closing portion of the web.xml file.
	out.println("</web-app>");
	out.close();

	return (nentities);
    }


    /**
     * Output any global parameter sets.
     *
     * @param	globalParams	A pset-list of globalParams psets
     * @param	dest		Root directory for output files
     * @param	entityList	List of entities created
     *
     * Each global-params file defines one or more psets, each pset beginning
     * with a "[pset]" context (usually a pset definition file will contain
     * a single pset, but multiple psets are allowed to adhere to INI syntax).
     * The parameter set is read and output as a Java properties file to the
     * Webapp directory.
     *
     * The number of global parameter sets generated is returned as the
     * function value.  A reference to each global pset created is added to
     * entityList.
     */
    private int writePsets(ParamSet globalParams, String dest, ParamSet entityList)
	throws DalServerException {

	// The global-params context is passed as a pset wherein each
	// parameter name is the name of a global-params pset.  Pset
	// config files are required to have a ".pset" extension.

	int npsets = 0;

	for (Param psetId : globalParams) {

	    // Parse each global-params file.  This returns a list of global
	    // param psets, each containing actual atomic parameters.

	    String psetConf = configDir + "/" + psetId.getName();
	    if (!psetConf.endsWith(".pset"))
		psetConf += ".pset";
	    ArrayList<ParamSet> psetList = new ArrayList<ParamSet>();
	   
	    // Parse the file.
	    try {
		if (parseIni(psetConf, psetList) <= 0) {
		    throw new DalServerException(
			"malformed global-params file (" + psetConf + ")");
		}
	    } catch (FileNotFoundException ex) {
		throw new DalServerException(
		    "global-params file not found (" + psetConf + ")");
	    }

	    // Output each pset defined in the global-params file.
	    for (ParamSet pset : psetList) {
		String psetName = pset.getValue("psetName");
		if (psetName == null) {
		    psetName = pset.getValue("pset-name");
		    if (psetName == null) {
			throw new DalServerException(
			"Missing pset name in pset '" + psetConf + ")");
		    }
		}

		// Output the Java properties content.
		String fname = dest + "/" + psetName;
		if (!fname.endsWith(".properties"))
		    fname += ".properties";
		if (writeProperties(pset, fname) > 0)
		    npsets++;
		entityList.addParam(psetId.getName(), "global pset with " +
		    pset.size() + " properties");
	    }
	}

	return (npsets);
    }

    /**
     * Format a parameter set as a sequence of Java properties and write the
     * formatted version to the given output file.
     *
     * @param	pset		Pset to be output
     * @param	fname		Output file pathname
     *
     * The output file pathname should normally be relative to the Web
     * application server runtime root directory, i.e., "webapps/...".
     */
    private int writeProperties(ParamSet pset, String fname)
	throws DalServerException {

	int nparams=0;
	if (pset == null)
	    return (0);

	// Open output file.
	try {
	    PrintWriter out = new PrintWriter(new FileWriter(fname));

	    for (Param p : pset) {
		out.println(p.getName() + " = " + p.stringValue());
		nparams++;
	    }

	    // Cleanup.
	    out.close();

	} catch (Exception ex) {
	    throw new DalServerException(
	    "Cannot write Java properties file (" + fname + ")");
	}

	return (nparams);
    }


    /**
     * Parse the optional table configuration file for a service and
     * append the content to the service parameter set.
     *
     * @param	tableConfig	Table configuration file
     * @param	configDir	Configuration directory
     * @param	out		Output pset
     *
     * Table configuration parameters are named "table.standard.<param>"
     * or "table.custom.<param>" and are appended to the given output
     * parameter set.  This allows the table configuration to be passed
     * in via the existing service parameter mechanism without any
     * additional overhead.
     */
    private void putTableConfig (String tableConfig, String configDir, ParamSet out)
	throws DalServerException {

	// Parse the table configuration.
	String config = configDir + "/" + tableConfig;
	if (!config.endsWith(".tab"))
	    config += ".tab";
	ArrayList<ParamSet> tablePsets = new ArrayList<ParamSet>();

	try {
	    if (parseIni(config, tablePsets) <= 0) {
		throw new DalServerException("config file not found (" +
		    config + ")");
	    }
	} catch (FileNotFoundException ex) {
	    throw new DalServerException("config file not found (" +
		config + ")");
	}
	
	// Table top level attributes. 
	ParamSet pset = findPset(tablePsets, "table", 0);
	String section, pname;

	for (Param p : pset)
	    out.addParam(p.getName(), p.stringValue());

	// Standard table parameters, if any. 
	section = "standard";
	pset = findPset(tablePsets, section, 0);

	for (Param p : pset) {
	    pname = "table." + section + "." + p.getName();
	    out.addParam(pname, p.stringValue());
	}

	// Custom table parameters, if any. 
	section = "custom";
	pset = findPset(tablePsets, section, 0);

	if (pset != null) {
	    for (Param p : pset) {
		pname = "table." + section + "." + p.getName();
		out.addParam(pname, p.stringValue());
	    }
	}
    }


    /** 
     * Private method to print an XML element.
     *
     * @param	out		Output stream.
     * @param	element		XML Element name.
     * @param	value		Element value text.
     * @param	indent		Number of spaces to indent.
     */
    private void putElement (PrintWriter out,
	String element, String value, int indent) {

	StringBuilder line = new StringBuilder();

	// Indent the line.
	for (int i=0;  i < indent;  i++)
	    line.append(' ');

	// Format the XML element.
	if (element != null)
	    line.append("<" + element + ">");

	if (element != null && value.length() > MAXLINE) {
	    out.println(line.toString());
	    line.setLength(0);
	    for (int i=0;  i < (indent+2);  i++)
		line.append(' ');
	    line.append(value);
	    out.println(line.toString());
	    line.setLength(0);
	    for (int i=0;  i < indent;  i++)
		line.append(' ');
	} else
	    line.append(value);

	if (element != null)
	    line.append("</" + element + ">");

	// Output the line.
	out.println(line.toString());
    }

    /**
     * Output some text with the current indent.
     */
    private void putText(PrintWriter out, String text, int indent) {
	putElement(out, null, text, indent);
    }


    /**
     * Parse a config file in INI Format.
     *
     * @param	config		Input config file in INI Format
     * @param	psetList	List of named psets for output
     *
     * The input config file is parsed, and the content returned in the form
     * of zero or more ParamSet objects, one ParamSet per INI context, that
     * are added to the provided pset arrayList.  The number of psets output
     * is returned.
     */
    public int parseIni (String config,
	ArrayList<ParamSet> psetList)
	throws FileNotFoundException, DalServerException {

	ParamSet pset = null;
	int ncontexts = 0;

	// Open the config file.
	BufferedReader br = new BufferedReader(new FileReader(config));

	try {
	    // Get the entire text file as a String.
	    StringBuilder sb = new StringBuilder();
	    boolean skipWhitespace = false;
	    String line, token;

	    try {
		for (line=br.readLine();  line != null;  line=br.readLine()) {
		    // Join lines if newline is escaped.
		    if (line.endsWith("\\")) {
			if (skipWhitespace) {
			    sb.append(" ");
			    line = line.trim();
			}
			sb.append(line.substring(0, line.length()-1));
			skipWhitespace = true;
		    } else {
			if (skipWhitespace) {
			    sb.append(" ");
			    sb.append(line.trim());
			    sb.append(" | ");
			    skipWhitespace = false;
			} else {
			    sb.append(line);
			    sb.append(" | ");
			}
		    }
		}
	    } catch (IOException ex) {
		;
	    }

	    // Get the final, long line of text.
	    String text = sb.toString();

	    // Access the string as a sequence of tokens.
	    // TODO: Rewrite this to use StreamTokenizer.

	    StringTokenizer in = new StringTokenizer(text);
	    StringTokenizer pushTok=null;
	    StringBuilder pvalue=null;
	    String pname=null;

	    try {
		while (pushTok != null || in.hasMoreTokens()) {
		    // Retrieve a pushed tokenizer.
		    if (!in.hasMoreTokens() && pushTok != null) {
			in = pushTok;
			pushTok = null;
		    }

		    token = in.nextToken(" \t");

		    // Skip a blank line.
		    if (token.equals("|"))
			continue;

		    // Skip a comment line.
		    if (token.startsWith("#")) {
			while (in.hasMoreTokens()) {
			    token = in.nextToken(" \t");
			    if (token.equals("|"))
				break;
			}
			if (in.hasMoreTokens())
			    continue;
			else
			    break;
		    }

		    // Start a new context.
		    if (token.startsWith("[")) {
			String contextName =
			    token.substring(1, token.indexOf(']'));
			pset = new ParamSet(contextName);
			psetList.add(pset);
			ncontexts++;
			continue;
		    }

		    // Add a parameter to the current context.
		    boolean eqseen=false, skipToEol=false;
		    pvalue = new StringBuilder();
		    pname = token;

		    token = in.nextToken(" \t");
		    if (eqseen = token.equals("="))
			token = in.nextToken(" \t");

		    // The param value is either "param = value", all on one
		    // line, or "param =" followed by end of line, in which
		    // case the value is the entire next line or lines, until
		    // a blank line is encountered.  Continuation lines must
		    // begin with a TAB character.
		   
		    if (eqseen && token.equals("|")) {
			// Either a missing or multi-line pvalue.
		
			// Skip forward unless TAB is seen (token=" ").
			token = in.nextToken("|");

			// if (!token.equals(" ")) {
			if (!Character.isWhitespace(token.charAt(0))) {
			    // Handle a null-valued param.
			    if (pset != null)
				pset.addParam(pname, "");
			    pname = null; pvalue = null;

			    // Process the line just read in.
			    pushTok = in;
			    in = new StringTokenizer(token);
			    continue;
			} else
			    pvalue.append(token.trim() + " ");

			// Append any following lines to the pvalue.
			while (in.hasMoreTokens()) {
			    token = in.nextToken("|");
			    if (token.equals("  "))
				break;
			    pvalue.append(token.trim() + " ");
			}

		    } else if (token.startsWith("#")) {
			// Param with no value, followed by comment.
			while (in.hasMoreTokens()) {
			    token = in.nextToken(" \t");
			    if (token.equals("|"))
				break;
			}
		    } else if (token.equals("|")) {
			// Param has no value.
			;
		    } else {
			// Param = value.
			if (!skipToEol)
			    pvalue.append(token + " ");

			while (in.hasMoreTokens()) {
			    token = in.nextToken(" \t");
			    if (token.equals("|"))
				break;
			    else if (token.equals("#"))
				skipToEol = true;
			    if (!skipToEol)
				pvalue.append(token + " ");
			}
		    }

		    // Add the parameter to the context pset.
		    if (pset != null) {
			String pval = pvalue.toString().trim();
			pset.addParam(pname, pval);
		    }
		    pname = null; pvalue = null;
		}
	    } catch (NoSuchElementException ex) {
		if (pset != null && pname != null && pvalue != null) {
		    String pval = pvalue.toString().trim();
		    pset.addParam(pname, pval);
		    pname = null; pvalue = null;
		}
	    }

	} finally {
	    try {
		br.close();
	    } catch (IOException ex) {
		;
	    }
	}

	return (ncontexts);
    }

    /**
     * Find a pset of the given class in a pset list.
     *
     * @param	psetList	The psetList to be searched.
     * @param	type		The pset class to search for.
     * @param	index		Instance to be returned.
     *
     * If index=0 the first instance found is returned.  Null is returned
     * if a pset of the given class(type) is not found, or is not found
     * at the given index value.
     *
     * The implementation here is crude, but in actual usage these lists
     * are quite short.
     */
    private ParamSet findPset(ArrayList<ParamSet> psetList, String type, int index) {
	int counter = 0;

	for (ParamSet pset : psetList) {
	    String psetClass = pset.getPsetClass();
	    if (psetClass == null)
		continue;

	    if (psetClass.equals(type)) {
		if (counter == index)
		    return (pset);
		counter++;
	    }
	}

	return (null);
    }

    /**
     * Simple routine to copy a file.
     *
     * @param	source		The source file.
     * @param	dest		The destination pathname.
     *
     * Any directories referenced in the destination pathname must exist.
     */
    private void copyFile(String source, String dest)
        throws IOException {

	InputStream input = null;
	OutputStream output = null;

	try {
	    input = new FileInputStream(source);
	    output = new FileOutputStream(dest);
	    byte[] buf = new byte[1024];
	    int bytesRead;

	    while ((bytesRead = input.read(buf)) > 0) {
		output.write(buf, 0, bytesRead);
	    }
	} finally {
	    input.close();
	    output.close();
	}
    }

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
    private boolean errorResponse(ParamSet params,
	HttpServletResponse response, Exception ex)
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
	    response.resetBuffer();
	    response.setContentType("text/xml;x-votable");
	    response.setBufferSize(BUFSIZE);
	    out = response.getOutputStream();

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
