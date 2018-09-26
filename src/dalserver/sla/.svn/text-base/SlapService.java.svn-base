/*
 * SlapService.java
 * $ID*
 */

package dalserver.sla;

import dalserver.DalServerException;
import dalserver.RequestResponse;
import dalserver.TableInfo;
import dalserver.Param;
import dalserver.ParamType;
import dalserver.ParamLevel;
import dalserver.KeywordFactory;
import dalserver.conf.KeywordConfig;
import dalserver.sla.SlapParamSet;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;
import java.net.FileNameMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Enumeration;
import java.util.EnumSet;

/**
 * A generic class which implements the operations for a DALServer SLAP
 * service.  A custom data service would subclass this, replacing the
 * <i>queryData</i> operation with a version which implements the same
 * operation for a specific archive and data collection (the custom code
 * should be placed in src/dataServices).  In most cases the remaining
 * SLAP operations (e.g., <i>getCapabilities</i>) should not need to be
 * subclassed by a data service, except to edit the metadata to be returned.
 * The remaining code required to implement a complete service (excluding
 * that required to generate the data product to be returned) is provided
 * by the {@link dalserver} code.
 *
 * <p>By default this generic SLAP implementation will function as an "echo"
 * service, implementing the SLAP protocol but merely echoing back its input
 * parameters and providing a dataless query response including all SLAP
 * metadata but no actual line data.
 *
 * <p>This generic SLAP implementation also provides a limited capability
 * to directly serve line list data, providing a DBMS table containing
 * the line list data is provided.  For this generic code to work the line
 * list data must conform to the spectral line list data model [should
 * say more here about UTYPEs, field names, possible support for pass-through
 * of any extra metadata, etc.].
 *
 * <p>The service implementation at this level should not be protocol
 * specific.  The intention is that the service operations at this level can
 * be used with any distributed computing platform.  Hence we can implement
 * an HTTP GET interface now, and add a SOAP or other interface later,
 * or some other interface which may not even be network based, all sharing
 * the same core service operations.</p>
 *
 * @version	1.0, 3 Dec 2009
 * @author	Doug Tody, Ray Plante
 */
public class SlapService {
    /**
     * The service name, used to identify resources associated with this
     * service instance.
     */
    protected String serviceName = "slap";

    /**
     * The service class assumed by the caller, e.g., if the service
     * implementation supports multiple service protocols such as SCS
     * and TAP.
     */
    protected String serviceClass = "slap";

    /** The type of DBMS to be accessed, if any. */
    protected String dbType;

    /** The name of the database to be accessed. */
    protected String dbName;

    /** The name of the table to be accessed (for the SLAP metadata). */
    protected String tableName;

    /** The JDBC URL (endpoint) of the DBMS. */
    protected String jdbcUrl;

    /** The JDBC driver to be used for the connection. */
    protected String jdbcDriver;

    /** The user name to use to login to the DBMS. */
    protected String dbUser;

    /** The user password to use to login to the DBMS. */
    protected String dbPassword;

    /** Local directory where configuration files are stored. */
    protected String configDir;

    /** Local directory where data files are stored. */
    protected String dataDirURL;

    /** File content (MIME) type of any returned datasets. */
    protected String contentType;


    /**
     * Create a new local SLAP service instance.
     *
     * @param params	Input parameter set.
     */
    public SlapService(SlapParamSet params) {
        this.serviceName = "slap";
        this.serviceClass = "slap";
        this.dbType = null;
        this.dbName = null;
        this.tableName = null;
        this.configDir = "/tmp";
        this.dataDirURL = "/tmp";
        this.contentType = "text/xml";

	if (params != null) {
            this.serviceName = params.getValue("serviceName", this.serviceName);
            this.serviceClass = params.getValue("serviceClass", this.serviceClass);
            this.dbType = params.getValue("dbType", this.dbType);
            this.dbName = params.getValue("dbName", this.dbName);
            this.tableName = params.getValue("tableName", this.tableName);
            this.jdbcUrl = params.getValue("jdbcUrl", this.jdbcUrl);
            this.jdbcDriver = params.getValue("jdbcDriver", this.jdbcDriver);
            this.dbUser = params.getValue("dbUser", this.dbUser);
            this.dbPassword = params.getValue("dbPassword", this.dbPassword);
	    this.configDir = params.getValue("configDir", this.configDir);
	    this.dataDirURL = params.getValue("dataDirURL", this.dataDirURL);
	    this.contentType = params.getValue("contentType", this.contentType);
	}
    }

    /**
     * Process a data query and generate a list of spectral lines
     * matching the query parameters.
     *
     * @param	params	  The fully processed SLAP parameter set representing
     *			  the request to be processed.
     *
     * @param	response  A dalserver request response object to which the
     *			  query response should be written.  Note this is
     *			  not a file, but an object containing the metadata
     *			  to be returned to the client.
     */
    @SuppressWarnings("unchecked")
    public void queryData(SlapParamSet params, RequestResponse response)
	    throws DalServerException 
    {

	boolean debug = (params.getParam("DEBUG") != null);
	KeywordFactory slap = null;
        try {
            slap = new dalserver.sla.SlapKeywordFactory(response);
        }
        catch (IOException ex) {
            String msg = "Trouble configuring service";
            String dmsg = msg + ": " + ex.getMessage();
            System.err.println(dmsg);
            if (debug) msg = dmsg;
            throw new DalServerException(msg);
        }
        catch (KeywordConfig.FormatException ex) {
            String msg = "Trouble configuring service";
            String dmsg = msg + ": " + ex.getMessage();
            System.err.println(dmsg);
            if (debug) msg = dmsg;
            throw new DalServerException(msg);
        }
	RequestResponse r = response;
	String id, key;

	TableInfo dbNameInfo = new TableInfo("dbName", dbName);
	TableInfo tableNameInfo = new TableInfo("tableName", tableName);

	// Set global metadata.
	r.setDescription("DALServer null echo/test SLAP service");
	r.setType("results");

	// This indicates the query executed successfully.  If an exception
	// occurs the output we generate here will never be returned anyway,
	// so OK is always appropriate here.

	r.addInfo(key="QUERY_STATUS", new TableInfo(key, "OK"));
	r.addInfo(key="dbName", dbNameInfo);
	r.addInfo(key="tableName", tableNameInfo);

        // Echo the query parameters as INFOs in the query response.
        for (Object o : params.entrySet()) {
	    Map.Entry<String,Param> keyVal = (Map.Entry<String,Param>)o;
            Param p = keyVal.getValue();
            if (!p.isSet() || (p.getLevel() == ParamLevel.SERVICE && !debug))
		continue;

	    String value = p.stringValue();
	    if (debug && p.getType().contains(ParamType.RANGELIST))
		value += " (" + p.toString() + ")";

            r.addInfo(id=p.getName(), new TableInfo(id, value));
        }

        // This implementation supports only SLAP.
	if (!serviceClass.equalsIgnoreCase("slap"))
	    throw new DalServerException("Service only supports SLAP");

	// Create the table metadata for a standard SLAP query response.
	// Only the fields for which valid values are to be returned should
	// be defined here.

	// For SLAP V1.0 we define (mostly) only the standard fields here.
	// Additional DAL-V2 dataset metadata can optionally be added, and
	// this metadata defined in the provided SLAP keyword dictionary.

	// *** The following is for SIAP and will need to be largel
	// *** replaced for SLAP.

        r.addField(slap.newField("wavelength"));
        r.addField(slap.newField("title"));
        r.addField(slap.newField("status"));
        r.addField(slap.newField("species"));
        r.addField(slap.newField("initialLevelName"));
        r.addField(slap.newField("finalLevelName"));
        r.addField(slap.newField("obsWavelength"));
        r.addField(slap.newField("initialLevelName"));
        r.addField(slap.newField("finalLevelName"));
        r.addField(slap.newField("initialLevelConfiguration"));
        r.addField(slap.newField("finalLevelConfiguration"));
        r.addField(slap.newField("initialLevelState"));
        r.addField(slap.newField("finalLevelState"));
        r.addField(slap.newField("processName"));
        r.addField(slap.newField("targetName"));
        r.addField(slap.newField("location"));
        r.addField(slap.newField("bibcode"));
        r.addField(slap.newField("referenceURL"));
        r.addGroup(slap.newGroup("Line"));
        r.addGroup(slap.newGroup("TimeAxis"));

	// If dbName and dbTable are defined we assume that the service
	// has been configured to directly query a line list database.
	// Perform the query and generate the query response metadata.

	if (dbName != null && tableName != null) {
	    SlapMySql mysql = null;
	    Exception error = null;

	    if (!dbType.equalsIgnoreCase("MySQL"))
		throw new DalServerException("Only MySQL supported currently");

            // Execute the SLAP query.
            try {
                mysql = new SlapMySql(jdbcDriver);
                mysql.connect(jdbcUrl, dbName, dbUser, dbPassword);
                mysql.query(params, response);

            } catch (Exception ex) {
                error = ex;
            } finally {
		if (mysql != null)
		    mysql.disconnect();
		if (error != null)
		    throw new DalServerException(error.getMessage());
	    }
	}

	// Show the number of table rows in the response header.
	r.addInfo(key="TableRows",
	    new TableInfo(key, new Integer(r.size()).toString()));

	// We are done once the information content of the query response
	// is fully specified.  The servlet code will take care of serializing
	// the query response as a VOTable, and anything else required.
    }


    /**
     * [It is not clear if this has any use for SLAP - do we have any case
     * [where we need to return actual files? (maybe raw line lists).  Let's
     * [leave it in for now.]
     *
     * Retrieve an actual dataset.  The dataset to be returned is specified
     * by the PubDID parameter in the input request.  The value of PubDID is
     * an ivoa dataset identifier as returned in an earlier call to the
     * queryData operation.
     *
     * <p>While to the client the access reference is a simple URL, at the
     * level of the service (in this implementation at least) the access
     * reference URL resolves into an explicit getData service operation.
     * The interpretation of PubDID is entirely up to the service.  In
     * a simple case it provides a key which can be used to retrieve an 
     * archival dataset.  In another case, the service might generate a
     * unique PubDID on the fly for each virtual dataset described in the
     * query response (e.g., for a cutout or other virtual dataset), either
     * building sufficient information into the PubDID (and hence the URL)
     * to specify the dataset to be generated, or saving internally a 
     * persistent description of the virtual dataset, indexed by the PubDID.
     * The service can later generate the dataset on the fly if and when it
     * is subsequently requested by the client.
     *
     * @param	params	The fully processed SLAP parameter set representing
     *			the request to be processed.  Upon output the
     *			parameters "datasetContentType" and
     *			"datasetContentLength" are added to specify
     *			the content (MIME) type of the dataset to be returned,
     *			and the size of the data entity to be returned, if
     *			known.  Since data entities may be dynamically
     *			computed or may be dynamic streams, the content
     *			length is not always known in advance, in which
     *			case the value should be set to null.
     *
     * @param response  A request response object (not used in the SLAP
     *			implementation).
     *
     * @return		A getData operation may return data in either of two
     *			forms.  An InputStream may be returned which can be
     *			used to return the dataset as a byte stream; it is
     *			up to the caller to close the returned stream when
     *			the data has been read.  Alternatively, if a VOTable
     *			is to be returned, the VOTable content should be
     *			written to the request response object, and null
     *			should be returned as the InputStream.
     */
    public InputStream getData(SlapParamSet params, RequestResponse response)
	throws DalServerException {

	String fileType = null;
	long fileLength = -1;
	URLConnection conn;
	InputStream in;
	URL fileURL;

	// Get the dataset identifier.
	String pubDid = params.getValue("PubDID");
	if (pubDid == null)
	    throw new DalServerException("getData: missing PubDID value");

	// In our case here the PubDID is the publisher internal URL (not
	// necessarily externally accessible) of the file to be returned.

	try {
	    fileURL = new URL(pubDid);
	    conn = fileURL.openConnection();
	    fileType = conn.getContentType();
	    fileLength = conn.getContentLength();

	    if (fileType.equals("content/unknown")) {
		FileNameMap map = URLConnection.getFileNameMap();
		fileType = map.getContentTypeFor(pubDid);
	    }

	    // Don't trust the system idea of contentType if FITS file.
	    if (pubDid.toLowerCase().endsWith(".fits"))
		fileType = "image/fits";
	    else if (pubDid.toLowerCase().endsWith(".fit"))
		fileType = "image/fits";

	    if (fileLength <= 0) {
		String fileName = fileURL.getFile();
		if (fileName.length() > 0) {
		    File dataset = new File(fileName);
		    fileLength = dataset.length();
		}
	    }

	} catch (MalformedURLException ex) {
	    throw new DalServerException(ex.getMessage());
	} catch (IOException ex) {
	    throw new DalServerException(ex.getMessage());
	}

	// Tell servlet what type of data stream we are returning.

	String contentType = this.contentType;
	if (contentType.equalsIgnoreCase("DYNAMIC"))
	    contentType = fileType;
	params.addParam(new Param("datasetContentType",
	    EnumSet.of(ParamType.STRING), contentType,
	    ParamLevel.SERVICE, false, "Content type of dataset"));

	params.addParam(new Param("datasetContentLength",
	    EnumSet.of(ParamType.STRING),
	    (fileLength < 0) ? null : new Long(fileLength).toString(),
	    ParamLevel.SERVICE, false, "Content length of dataset"));

	// Return an InputStream to stream the dataset out.
	try {
	    in = conn.getInputStream();
	} catch (IOException ex) {
	    throw new DalServerException(ex.getMessage());
	}

	return (in);
    }


    /**
     * Process a service metadata query, returning a description of the
     * service to the client as a structured XML document.  In the generic
     * form this operation returns an InputStream to an actual file in
     * the local file system on the server.  The file name is formed by
     * concatenating the serviceName with "Capabilities.xml".  The file
     * is assumed to be available in the directory specified when the
     * SlapService object was created, e.g., "path/myServiceCapabilities.xml".
     * More generally, the service capabilities do not have to be maintained
     * in a static file and could be dynamically generated (e.g., from a
     * database version), so long as a valid InputStream is returned.
     *
     * @param	params	The fully processed SLAP parameter set representing
     *			the request to be processed.
     *
     * @return		An InputStream which can be used to read the 
     *			formatted getCapabilities metadata document.
     *			It is up to the caller to close the returned stream
     *			when the data has been read.
     */
    public InputStream getCapabilities(SlapParamSet params)
	throws FileNotFoundException {

	File capabilities = new File (this.configDir,
	    this.serviceName + "Capabilities.xml");
	return ((InputStream) new FileInputStream(capabilities));
    }


    // -------- Testing -----------

    /**
     * Unit test to do a simple query.
     */
    public static void main(String[] args)
	throws DalServerException, IOException, FileNotFoundException {

	if (args.length == 0 || args[0].equals("query")) {
	    // Exercise the SlapService class.
	    SlapService service = new SlapService(null);

	    // Simulate a typical query.
	    SlapParamSet params = new SlapParamSet();
	    params.setValue("WAVELENGTH", "5.1E-6,5.6E-6");

	    // Create an initial, empty request response object.
	    RequestResponse r = new RequestResponse();

	    // Set the request response context for SLAP.
	    KeywordFactory slap = null;
            try {
                slap = new dalserver.sla.SlapKeywordFactory(r);
            }
            catch (IOException ex) {
                String dmsg = "Trouble configuring keywords: "+ex.getMessage();
                throw new DalServerException(dmsg);
            }
            catch (KeywordConfig.FormatException ex) {
                String dmsg = "Trouble configuring keywords: "+ex.getMessage();
                throw new DalServerException(dmsg);
            }   

	    // Perform the query. 
	    service.queryData(params, r);

	    // Write out the VOTable to a file.
	    OutputStream out = new FileOutputStream("_output.vot");
	    r.writeVOTable(out);
	    out.close();
	}
    }
}
