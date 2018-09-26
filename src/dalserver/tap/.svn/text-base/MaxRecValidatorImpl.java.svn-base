/*
 * MaxRecValdatorImpl.java
 * $ID*
 */

package dalserver.tap;

import dalserver.*;
import ca.nrc.cadc.tap.MaxRecValidator;
import org.apache.log4j.Logger;

/**
 * This class controls the maximum number of records to be returned in
 * a single request.
 *
 * Values for the default and limiting values of maxrec may be set in
 * the DALServer configuration via the parameters MaxrecDefault and
 * MaxrecLimit.  This class has access only to the global DALServer
 * parameters.  [TODO] A later service-specific override is possible
 * in the service code, e.g., queryRunner.
 * 
 * @author DTody (based upon OpenCADC template by PDowler)
 */
public class MaxRecValidatorImpl extends MaxRecValidator {
    private static final Logger log = Logger.getLogger(MaxRecValidatorImpl.class);

    private static final Integer DEFAULT_LIMIT = 1000;
    private static final Integer MAX_LIMIT = 10000;

    private DalContext dalContext;
    private ParamSet params;
    public int defaultValue;
    public int maxValue;
    
    /** No-arg constructor. */
    public MaxRecValidatorImpl() throws DalServerException {
        super();

	TapParamSet tapPset = new TapParamSet();
	this.dalContext = new DalContext((ParamSet)tapPset, null);
	this.params = dalContext.pset;
	Param p;

	// Default maximum number of output records (0 for no limit).
        if ((p = params.getParam("maxrecDefault")) != null && p.isSet()) {
            defaultValue = p.intValue();
        } else
            defaultValue = DEFAULT_LIMIT;

	// Upper limit for maxrec.
        if ((p = params.getParam("maxrecLimit")) != null && p.isSet()) {
            maxValue = p.intValue();
        } else
            maxValue = MAX_LIMIT;

        setDefaultValue(defaultValue);
        setMaxValue(maxValue);
    }

    /**
     * Dynamically set the maxrec defaults, depending upon whether the service
     * was called sync or async.  [I left the semantics as in OpenCADC for now,
     * but it is not clear if these are correct.  Reasonable default limits
     * should always be in place, so that the client has to requests a very large
     * or no limit maxrec to get a very large response back.]
     */
    @Override
    public Integer validate() {
        log.info("");
        if (super.sync) {
            try {
                // No default limit for a sync request.
                super.setDefaultValue(null);
                super.setMaxValue(null); 
                Integer ret = super.validate();
                log.debug("final MAXREC: " + ret);
                return (ret);
            } finally {
                // Restore values.
                super.setDefaultValue(defaultValue);
                super.setMaxValue(maxValue);
            }
        }

        // Async uses limits as above
        Integer ret = super.validate();
        log.debug("final MAXREC: " + ret);
        return (ret);
    }
}
