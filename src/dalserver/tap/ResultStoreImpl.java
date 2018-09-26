/*
 * ResultStoreImpl.java
 * $ID*
 */

package dalserver.tap;

import dalserver.*;
import ca.nrc.cadc.dali.tables.TableWriter;
import ca.nrc.cadc.dali.tables.votable.VOTableWriter;
import ca.nrc.cadc.tap.ResultStore;
import ca.nrc.cadc.uws.Job;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.ResultSet;
import java.util.Properties;
import org.apache.log4j.Logger;

/**
 * Basic ResultStore implementation usable by the cadcTAP QueryRunner for async
 * result storage. This implementation simply writes files to a local directory
 * and then returns a URL to that file. The filesystem-to-URL mapping is
 * configurable via two parameters passed via the service configuration.
 *
 * </p>
 * <ul>
 * <li>baseStorageDir=/path/to/storage
 * <li>baseURL=http://hostname/storage
 * </ul>
 * 
 * @author DTody (based upon the OpenCADC version by pdowler)
 */
public class ResultStoreImpl implements ResultStore {

    private static final Logger log = Logger.getLogger(ResultStoreImpl.class);
    private static final String BASEDIR = "baseDir";
    private static final String BASEURL = "baseURL";

    private Job job;
    private DalContext dalContext;
    private ParamSet params;
    private String contentType;
    private String filename;
    private String baseDir;
    private String baseURL;
    
    /** No-arg constructor. */
    public ResultStoreImpl() { }

    /**
     * Store a query resultSet formatted as a file by the given TableWriter.
     *
     * @param	rs		Query JDBC resultSet
     * @param	writer		The TableWriter instance to be used.
     */
    public URL put(ResultSet rs, TableWriter<ResultSet> writer) 
        throws IOException {

        return (put(rs, writer, null));
    }

    /**
     * Store at most maxRows of a query resultSet, formatted as a file by the
     * given TableWriter.
     *
     * @param	rs		Query JDBC resultSet
     * @param	writer		The TableWriter instance to be used.
     * @param	maxRows		The maximum number of rows to be output
     */
    public URL put(ResultSet rs, TableWriter<ResultSet> writer, Integer maxRows) 
        throws IOException {

        Long num = null;
        if (maxRows != null)
            num = new Long(maxRows.intValue());
        
        File dest = getDestFile(filename);
        URL ret = getURL(filename);
        FileOutputStream ostream = null;
        try {
            ostream = new FileOutputStream(dest);
            writer.write(rs, ostream, num);
        } finally {
            if (ostream != null)
                ostream.close();
        }
        return (ret);
    }

    /**
     * Format an output an error result as a VOTable.
     *
     * @param	t		Error condition to be output
     * @param	writer		VOTable writer
     */
    public URL put(Throwable t, VOTableWriter writer)
	throws IOException {

        File dest = getDestFile(filename);
        URL ret = getURL(filename);
        FileOutputStream ostream = null;
        try {
            ostream = new FileOutputStream(dest);
            writer.write(t, ostream);
        } finally {
            if (ostream != null)
                ostream.close();
        }

        return (ret);
    }

    /**
     * Set a reference to the UWS Job context.
     *
     * @param	job		Job instance
     *
     * The Job context is the primary mechanism for communicating context
     * information to TAP class code.  In particular, a reference to the
     * DalContext for the query and service instance is stored in the Job
     * descriptor, making it possible for DALServer TAP plugins to retrieve
     * the DALServer context.
     */
    public void setJob(Job job) {
        this.job = job;

	// Get the DalServer context.
	this.dalContext = (DalContext) job.appData;
	this.params = dalContext.pset;

	// Get the ResultStore storage management parameters.
	this.baseDir = params.getValue(BASEDIR);
	this.baseURL = params.getValue(BASEURL);

        if (baseDir == null || baseURL == null) {
            log.error("ResultStore config incomplete: " +
		BASEDIR + "=" + baseDir + " " + BASEURL + "=" + baseURL);
        }
    }

    /**
     * Set the HTTP contentType for the file to be created.
     *
     * @param	contentType	HTTP contentType value
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Set the base filename for the file to be created.
     *
     * @param	filename	The base filename
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * Get a File instance for the file to be written.
     *
     * @param	filename	The base filename
     */
    private File getDestFile(String filename) {
        File dir = new File(baseDir);
        if (!dir.exists())
            throw new RuntimeException(BASEDIR + "=" + baseDir + " does not exist");
        if (!dir.isDirectory())
            throw new RuntimeException(BASEDIR + "=" + baseDir + " is not a directory");
        if (!dir.canWrite())
            throw new RuntimeException(BASEDIR + "=" + baseDir + " is not writable");
        
        return (new File(dir, filename));
    }
    
    /**
     * Return a URL reference to the named file.
     *
     * @param	filename	The base filename
     */
    private URL getURL(String filename) {
        StringBuilder sb = new StringBuilder();
        sb.append(baseURL);
        
        if ( !baseURL.endsWith("/") )
            sb.append("/");
        
        sb.append(filename);
        String s = sb.toString();
        try {
            return (new URL(s));
        } catch(MalformedURLException ex) {
            throw new RuntimeException("failed to create URL from " + s, ex);
        }
    }
}
