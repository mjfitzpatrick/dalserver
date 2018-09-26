/*
 * TapJobManager.java
 * $ID*
 */

package dalserver.tap;

import ca.nrc.cadc.uws.server.JobExecutor;
import ca.nrc.cadc.uws.server.MemoryJobPersistence;
import ca.nrc.cadc.uws.server.SimpleJobManager;
import ca.nrc.cadc.uws.server.ThreadPoolExecutor;
import org.apache.log4j.Logger;

/**
 * UWS JobManager implementation for the DALServer TAP service.
 *
 * This class is called by the OpenCADC implementation of the VO Universal
 * Worker Service (UWS) framework to execute a TAP query as a job via the UWS
 * framework.  This is not a servlet, it is a Java implementation (frontend or
 * runner) of a job to be executed within the UWS framework in a HTTP REST
 * context.  The actual servlet exposed to the outside world is a generic
 * connector provided by the Restlet (restlet.org) framework, that calls UWS
 * which invokes this class to execute the TAP query.
 *
 * @author DTody (based upon OpenCADC template by PDowler)
 */
public class TapJobManager extends SimpleJobManager {
    private static Logger log = Logger.getLogger(SimpleJobManager.class);

    /**
     * This class extends the OpenCADC UWS SimpleJobManager and sets up the
     * persistence and executor classes in the constructor.  This initial
     * version for the DALServer TAP implementation uses the OpenCADC
     * MemoryJobPersistence and ThreadExecutor implementations with a minimal
     * initial testing configuration, and can handle both sync and async jobs.
     * This will be extended later for the production version of the service.
     */
    public TapJobManager() {
        super();
        MemoryJobPersistence jobPersist = new MemoryJobPersistence();
        log.debug("created: " + jobPersist.getClass().getName());
        
        /* This implementation spawns a new thread for every async job,
	 * using a thread pool with 4 threads (for the moment).  The generic
	 * OpenCADC TAP QueryRunner class, extended by the plugins in this
	 * directory, executes the TAP query and processes the response.
	 */
        JobExecutor jobExec = new ThreadPoolExecutor(jobPersist, TapQueryRunner.class, 4);
        log.debug("created: " + jobExec.getClass().getName());

        super.setJobPersistence(jobPersist);
        super.setJobExecutor(jobExec);

        // these are the default values from super class SimpleJobManager
        //setMaxExecDuration(3600L);     // one hour
        //setMaxQuote(3600L);            // one hour 
        //setMaxDestruction(7*24*3600L); // 7 days
    }
}
