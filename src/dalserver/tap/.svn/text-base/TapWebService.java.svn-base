/*
 * TapWebService.java
 * $ID*
 */

package dalserver.tap;

import ca.nrc.cadc.vosi.AvailabilityStatus;
import ca.nrc.cadc.vosi.WebService;
import ca.nrc.cadc.vosi.avail.CheckDataSource;
import ca.nrc.cadc.vosi.avail.CheckException;
import org.apache.log4j.Logger;


/**
 * WebService implementation for VOSI-availability. The class name for this class
 * is used to configure the VOSI-availability servlet in the web.xml file.
 * 
 * @author DTody (based upon OpenCADC version by pdowler)
 */
public class TapWebService implements WebService
{
    private static final Logger log = Logger.getLogger(TapWebService.class);
    
    private static String TAPDS_NAME = "jdbc/tapuser";
    private String TAPDS_TEST = "select schema_name from tap_schema.schemas where schema_name='tap_schema'";
    
    /** No-arg constructor. */
    public TapWebService() { }
    

    /**
     * Query the service status.
     */
    public AvailabilityStatus getStatus() {
        boolean isGood = true;
        String note = "service is accepting queries";

        try {
            // Test query using standard TAP data source.
            CheckDataSource checkDataSource = new CheckDataSource(TAPDS_NAME, TAPDS_TEST);
            checkDataSource.check();
            
            // check for a certficate needed to perform network ops
            //File cert = ...
            //CheckCertificate checkCert = new CheckCertificate(cert);
            //checkCert.check();

            // check some other web service availability since we depend it
            //URL avail = ...
            //CheckWebService cws = new CheckWebService(avail);
            //cws.check();

        } catch(CheckException ce) {
            // tests determined that the resource is not working
            isGood = false;
            note = ce.getMessage();
        } catch (Throwable t) {
            // The test itself failed
            log.error("web service status test failed", t);
            isGood = false;
            note = "test failed, reason: " + t;
        }

        return (new AvailabilityStatus(isGood, null, null, null, note));
    }

    /**
     * Set the service status (not implemented).
     */
    public void setState(String string) {
        throw new UnsupportedOperationException();
    }
}
