/*
 * JhuSsapServlet.java
 * $ID*
 */

package dataServices;

import dalserver.*;
import dalserver.ssa.*;

/**
 * HTTP servlet implementing a proxy SSAP service for the JHU spectrum
 * services.  This implementation is intended only to provide a reference
 * SSAP implementation using the DALServer framework, and for demonstration
 * and test purposes, e.g., developing and testing SSAP client applications.
 */
public class JhuSsapServlet extends SsapServlet {

    /**
     * Get a new instance of the JhuSsapService proxy service.
     *
     * @param params	Service parameter set.
     */
    public SsapService newSsapService(SsapParamSet params, TaskManager taskman) {
	return ((SsapService) new JhuSsapService(params, taskman));
    }
}
