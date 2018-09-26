/*
 * JhuProxyServlet.java
 * $ID*
 */

package dataServices;

import dalserver.*;
import dalserver.ssa.*;

/**
 * HTTP servlet implementing a proxy SSAP service for the JHU spectrum
 * services.  This implementation is intended only for demonstration/test
 * purposes, e.g., developing and testing SSAP client applications.
 */
public class JhuProxyServlet extends SsapServlet {

    /**
     * Get a new instance of the JhuProxyService proxy service.
     *
     * @param params	Service parameter set.
     * @param taskman	Task manager.
     */
    public SsapService newProxyService(SsapParamSet params, TaskManager taskman) {
	return ((SsapService) new JhuProxyService(params, taskman));
    }
}
