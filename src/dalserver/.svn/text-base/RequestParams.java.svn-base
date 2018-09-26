/*
 * RequestParams.java
 * $ID*
 */

package dalserver;

import java.net.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * The RequestParams class renders an HTTP GET/POST servlet request
 * into a regularized parameter set.  Support is included for parameter
 * features such as range lists.
 *
 * @version	1.1, 11-Sep-2014
 * @author	Doug Tody
 */
public class RequestParams {
    /** Number of parameters seen in request. */
    public int nRequestParams = 0;

    /**
     * Read an HTTP request and process all request parameters therein
     * into the given parameter set.  Starting with an input default
     * parameter set of the desired type, the HTTP request parameters are
     * read and used to set or override the corresponding values in the input
     * paramSet.  Any extra, unrecognized request parameters are appended
     * to the input paramSet as ParamLevel.CLIENT-specified parameters.
     *
     * @param request		Servlet request object.
     * @param params		Service parameter set to be updated.
     */
    public void getRequestParams (HttpServletRequest request, ParamSet params)
	throws DalServerException {

	getRequestParams(request, params, null);
    }

    /**
     * Get the HTTP request parameters and process these into the given parameter
     * set, optionally omitting any named parameters.
     *
     * @param request		Servlet request object.
     * @param params		Service parameter set to be updated.
     * @param omit		String array of params to be omitted.
     *
     * The omit feature may be used to prevent a client from overriding the values
     * of named parameters.
     */
    public void
	getRequestParams (HttpServletRequest request, ParamSet params, String[] omit)
	throws DalServerException {

	// Process request parameters into parameter set.
	Set s = request.getParameterMap().entrySet();

	for (Iterator i = s.iterator();  i.hasNext();  ) {
	    Map.Entry me = (Map.Entry) i.next();
	    String name = (String) me.getKey();
	    String[] values = (String[]) me.getValue();
	    String value = null;
	    Param p;

	    // The protocol currently does not permit multiple values of a
	    // parameter; the range-list syntax should be used instead.

	    if (values != null && values.length != 1) {
		throw new DalServerException(
		    "multiple values of same parameter not allowed");
	    }

	    try {
		if (values != null && values[0].length() > 0)
		    value = URLDecoder.decode(values[0], "UTF-8");
		//String value = (values == null) ? null : values[0];
	    } catch (Exception ex) {
		throw new DalServerException(ex.getMessage());
	    }

	    // Omit any protected parameters.
	    if (omit != null)
		for (String key : omit) {
		    if (name.equalsIgnoreCase(key))
			throw new DalServerException(
			    "attempt to set a protected parameter (" + key + ")");
		}

	    if ((p = params.getParam(name)) != null) {
		// Update the value of a predefined parameter.  Booleans
		// are a special case: specifying the name of a boolean
		// parameter in a request, without an explicit value, is
		// the same as "<boolean-par>=true".

		if (p.type.contains(ParamType.BOOLEAN) && (value == null)) {
		    p.setValue("true");
		    p.isSet = true;
		} else if (value != null) {
		    p.setValue(value);
		    p.isSet = true;
		}

	    } else {
		// Add a new client-defined parameter to the paramSet.
		params.addParam(new Param(name, value));
	    }

	    nRequestParams++;
	}
    }
}
