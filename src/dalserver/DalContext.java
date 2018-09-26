/**
 * DalContext.java
 * $ID*
 */

package dalserver;

import ca.nrc.cadc.uws.Job;
import ca.nrc.cadc.uws.Parameter;
import org.apache.log4j.Logger;
import java.io.InputStream;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * DALServer context class.  This class persists the DALServer context
 * (request and service parameters, and response object) for the duration
 * of a service request or UWS job.  In the case of a synchronous HTTP
 * GET or POST request DalContext marshalls all parameters and stores
 * the RequestResponse object.  In the case of a UWS job such as for a
 * TAP service, the DalContext instance is used to make the DALServer context
 * available to the service plugins.
 *
 * Plugins to the OpenCADC UWS and TAP service code have no knowledge of the
 * DALServer framework within which these services run.  DalContext provides
 * access to the framework context for the duration of the UWS job.  This
 * context includes access to DALServer global and service-instance
 * parameters to control service execution, and a RequestResponse object
 * to process the output of a query.  A pointer to the DalContext instance
 * for a job is stored in the UWS job descriptor, which is available to
 * all plugins, allowing them to retrieve the DALServer context for the
 * current job.
 *
 * @author DTody 2-Apr-2015
 */
public class DalContext {
    /** Context parameters. */
    public Job job = null;
    public ParamSet pset = null;
    public RequestResponse response = null;

    /** Private parameters. */
    private static final String DALGLOBALS = "dalserver.properties";
    private static final Logger log = Logger.getLogger(DalContext.class);

    /**
     * This no-arg constructor has no Job descriptor, hence merely reads
     * the DALServer global parameters.  This is the most basic way to
     * get global configuration parameter defaults into any class code.
     *
     * @param	params			Parameter set
     * @param	response		RequestResponse instance
     *
     * A valid parameter set is required.  The DALServer global parameters
     * (all that is available in the simplest cases) are read and stored
     * in the given parameter set.  References to the request parameter
     * set and given RequestResponse instance are stored in the DalContext
     * instance.  If a RequestResponse instance is not required a null
     * value may be input.
     */
    public DalContext(ParamSet params, RequestResponse response)
	throws DalServerException {

	log.debug("read minimal DalContext, only " + DALGLOBALS);
	this.pset = params;
	this.response = response;
	readProperties(DALGLOBALS, params, true);
    }

    /**
     * Construct a DalContext instance (input parameters and output
     * RequestResponse object) for a UWS job and save a reference to this
     * in the UWS Job descriptor.
     *
     * @param	params			Parameter set
     * @param	response		RequestResponse instance
     * @param	job			Job descriptor context
     *
     * All parameters are gathered and added to the given parameter set.
     * A reference to the new DalContext instance is saved in the given
     * Job descriptor.
     */
    public DalContext(ParamSet params, RequestResponse response, Job job)
	throws DalServerException {

	log.debug("read DalContext for a UWS job: " + job);
	this.pset = params;
	this.response = response;
	this.job = job;

	// Gather the parameters for this HTTP request.
	// We start with the TAP default pset and update the values of any
	// parameters passed explicitly in the request.  UWS manages the
	// HTTP request and constructs the Job object, returning the request
	// parameters via the getParameterList() method.

	List<Parameter>plist = job.getParameterList();
	for (Parameter p : plist)
	    params.addParam(p.getName(), p.getValue());

	// Add the DALServer global framework system parameters.
	readProperties(DALGLOBALS, params, true);

	// Add the config parameters for this UWS service instance.
	// We can determine the service instance name from the requestPath,
	// and use this to retrieve the service config parameters.
	// These may override the default values defined in DALGLOBALS.

	String path[] = job.getRequestPath().split("/");
	String serviceName = path[path.length-2];
	String serviceParams = serviceName + ".properties";
	readProperties(serviceParams, params, true);

	// Store the DalContext instance reference in the Job descriptor.
	job.appData = (Object) this;
    }

    /**
     * Construct a DalContext instance for a synchronous HTTP GET or POST
     * request.
     *
     * @param	params			Parameter set
     * @param	response		RequestResponse instance
     * @param	servletRequest		HTTP servlet request instance
     * @param	servletContext		HTTP servlet context
     * @param	servletConfig		HTTP servlet config pars
     */
    public DalContext(ParamSet params, RequestResponse response,
	HttpServletRequest servletRequest, ServletContext servletContext,
	ServletConfig servletConfig) throws DalServerException {

	log.debug("read DalContext for a servlet");
	this.pset = params;
	this.response = response;
	this.job = null;

	// Internal data.
        HttpSession session = servletRequest.getSession(true);
        ResourceBundle messages =
            (ResourceBundle) session.getAttribute("messages");

	// Construct the service parameter set.  This is a single ParamSet
	// containing all context, config, and request parameters.  Any
	// locally defined context/config parameters are automatically
	// passed through.

	RequestParams reqHandler = null;
	Enumeration contextPars = servletContext.getInitParameterNames();
	Enumeration configPars = servletConfig.getInitParameterNames();

	// Get the DALServer global framework system parameters.
	readProperties(DALGLOBALS, params, true);

	// Get the servlet context parameters.
	while (contextPars.hasMoreElements()) {
	    String name = (String) contextPars.nextElement();
	    String value = (String) servletContext.getInitParameter(name);

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
	    String value = (String) servletConfig.getInitParameter(name);

	    Param p = params.getParam(name);
	    if (p == null) {
		params.addParam(p = new Param(name, value));
		p.setLevel(ParamLevel.SERVICE);
	    } else
		p.setValue(value);
	}

	// Add the config parameters for this service instance.
	// These may override the default values defined in DALGLOBALS,
	// or in the service context.

	String serviceName = params.getValue("serviceName");
	String serviceParams = serviceName + ".properties";
	readProperties(serviceParams, params, true);

	// Get the request parameters.
	reqHandler = new RequestParams();
	reqHandler.getRequestParams(servletRequest, params);
    }

    /**
     * Read a Java properties file and add any parameters defined therein
     * to the given parameter set.
     *
     * @param	propfile	The property file to be read
     * @param	pset		An existing pset to receive the params
     * @param	sys		These parameters are system (hidden) params
     *
     * The named property file is accessed via classLoader.  Any parameters
     * defined therein are appended to the provided parameter set.  The
     * number of parameters added or set is returned as the function value.
     */
    public int readProperties(String propfile, ParamSet pset, boolean sys)
	throws DalServerException {

	Properties prop = new Properties();
	InputStream in = null;
	int nparams = 0;

	// Read the properties file.
	try {
	    log.debug("read Java properties file: " + propfile);
	    in = DalContext.class.getClassLoader().getResourceAsStream(propfile);
	    prop.load(in);
	} catch (Exception ex) {
	    throw new DalServerException("cannot read properties file (" +
		propfile + ")");
	}

	// Append key,value pairs as params to output pset.  If a parameter
	// already exists its value is overwritten (hence the order in which
	// parameter sets are added or merged allows local values to override
	// more global defaults).

	try {
	    for (String key : prop.stringPropertyNames()) {
		if (sys)
		    pset.addSysParam(key, prop.getProperty(key));
		else
		    pset.setParam(key, prop.getProperty(key));
		nparams++;
	    }
	} catch (Exception ex) {
	    throw new DalServerException("error reading property file (" +
		propfile + ")");
	}

	return (nparams);
    }
}
