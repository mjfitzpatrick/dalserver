/*
 * JhuSsapService.java
 * $ID*
 */

package dataServices;

import java.io.*;
import java.util.*;
import java.net.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import cds.savot.pull.*;
import cds.savot.model.*;
import cds.savot.common.*;

import dalserver.*;
import dalserver.ssa.*;

/**
 * JhuSsapService implements a demonstration SSAP service acting
 * as a proxy to the JHU spectrum services, providing a reference SSAP
 * implementation intended for software development purposes, e.g., to
 * test client applications.
 *
 * <p>This version implements a proxy service for the real JHU services,
 * and is intended only for demonstration and software development purposes.
 * Since it is a proxy service it can run anywhere on the Internet without
 * requiring local access to data, hence it is convenient for demonstration
 * purposes and to provide a reference SSAP implementation for software
 * development.  The native JHU/SDSS services should be used instead for
 * production data access.
 *
 * <p>This demonstration service may be used as a working example of how
 * to use the DALServer package to build a SSAP service.  In the case
 * here we issue a query to a remote service and use the query result to
 * build the SSAP query response.  In a more typical data service, what
 * we would do is very similar: issue a query to a local database, and
 * convert the metadata returned into the SSAP query response.  In effect,
 * the remote service replaces the local DBMS query used in a more typical
 * service.
 */
public class JhuSsapService extends SsapService {
    /** The default maximum size for a data array in a spectrum. */
    private final int MAXPTS = 4096;

    /** Params used to build up the GET query string. */
    private LinkedHashMap<String,String> urlParams;

    /** VOTable for a spectrum service SSAP query response. */
    private LinkedHashMap<String,TableParam> jhuQR;
    private SavotVOTable jhuQRVot;
    private TRSet jhuQRData;

    /** Keyword table for a spectrum service XML response. */
    private ArrayList<LinkedHashMap<String,TableParam>> jhuXml;

    /** Base URLs for the version of the JHU Spectrum Services used here. */
    private static final String queryJhuSsap = "http://www.voservices.net" +
	"/spectrum/spectrumssa_v3.1.0/ssa.aspx?";
    private static final String getDataUrl = "http://www.voservices.net" +
	"/spectrum/spectrumssa_v3.1.0/get.aspx?";

    /** Base URL for dataset retrieval. */
    // private static final String dataUrl = "http://localhost:8080" +
    //private static final String dataUrl = "http://webtest.aoc.nrao.edu" + 
    //	"/ivoa-dal/JhuProxySsap?REQUEST=getData&";

    // Internal data.
    private String baseUrl;


    // ------------- Constructors ----------------

    /**
     * Create a new local service instance.
     *
     * @param params	Service parameter set.
     */
    public JhuSsapService(SsapParamSet params, TaskManager taskman) {
	super(params, taskman);
    }


    // ------------- Service Operations ----------------

    /**
     * Process a data query and generate a list of candidate spectral
     * datasets matching the query parameters.
     *
     * @param   params    The fully processed SSAP parameter set representing
     *                    the request to be processed.
     *
     * @param   response  A dalserver request response object to which the
     *                    query response should be written.  Note this is
     *                    not a file, but an object containing the metadata
     *                    to be returned to the client.
     */
    public void queryData(SsapParamSet params, RequestResponse response)
	throws DalServerException {

	// Form the query to the remote database.
	this.buildSsapQuery(params);

	// Execute the query and read the query response.
	this.executeQuery();

	// Build the SSAP request response.
	this.computeResponse(params, response);
    }

    /**
     * Custom getData operation for the JHU proxy service.  This is required
     * to be able to fetch native data from the JHU archives, and convert it
     * to SSA-compliant data to be passed back to the client.  Native data
     * can also be returned, without conversion.
     *
     * <p>The dataset to be returned is specified by a PubDID parameter in
     * the input request.  The value of PubDID is an IVOA dataset identifier
     * as returned in an earlier call to the queryData operation.
     *
     * @param   params  The fully processed SSAP parameter set representing
     *                  the request to be processed.  Upon output a new
     *                  parameter "datasetContentType" is added, to specify
     *                  the content (MIME) type of the dataset to be returned.
     *
     * @param	response A request response object, which may be used if
     *			the response is a VOTable.
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
    @SuppressWarnings("unchecked")
    public InputStream getData(SsapParamSet params, RequestResponse response)
	throws DalServerException {

	// The following would suffice to return a static file:
	// return (super.getData(params));

	// What we need to do here though, is return data from the remote
	// JHU spectral archives.  We can return either native data (just
	// a pass-through or maybe even a redirect), or SSA-compliant data
	// (via on-the-fly transformation).

	// Get the dataset identifier.
	String pubDid = params.getValue("PubDID");
	if (pubDid == null)
	    throw new DalServerException("getData: missing PubDID value");

	// FORMAT in a getData specifies the type of data to be returned.
	String format = params.getValue("FORMAT");

	// Form the data retrieval Query.
	urlParams = new LinkedHashMap<String,String>();
	this.baseUrl = getDataUrl;
	urlParams.put("format", "xml");
	urlParams.put("spectrumId", pubDid);

	// Propagate any client-specified params to the remote service.
	for (Object o : params.entrySet()) {
	    Map.Entry<String,Param> keyVal = (Map.Entry<String,Param>)o;
	    Param p = keyVal.getValue();
	    if (!p.isSet() || (p.getLevel() != ParamLevel.CLIENT))
		continue;
	    if (p.getName().equalsIgnoreCase("PubDid"))
		continue;
	    urlParams.put(p.getName(), p.stringValue());
	}

	InputStream in = null;
	String contentType = null;
	String contentLength = null;

	if (format != null && format.equalsIgnoreCase("native")) {
	    // Pass-through native (or xml) format data unchanged.
	    contentType = "text/xml";

	    // Open a connection to the data.
	    try {
		String query = this.getQueryUrl();
		in = new URL(query).openStream();
	    } catch (MalformedURLException ex) {
		throw new DalServerException(ex.getMessage());
	    } catch (IOException ex) {
		throw new DalServerException(ex.getMessage());
	    }

	} else if (format != null && format.equalsIgnoreCase("votable")) {
	    // Return SSAP compliant data in VOTable format.
	    contentType = "application/x-votable+xml";

	    // Execute the query formed above.
	    this.executeDataQuery();
	    // Read the native format data and transform to a Spectrum.
	    this.transformData(params, response);

	} else if (format != null && format.equalsIgnoreCase("csv")) {
	    // Return SSAP compliant data in CSV format.
	    contentType = "text/csv";

	    // Execute the query formed above.
	    this.executeDataQuery();
	    // Read the native format data and transform to a Spectrum.
	    this.transformData(params, response);

	} else if (format != null && format.equalsIgnoreCase("text")) {
	    // Return SSAP compliant data in Text table format.
	    contentType = "text/plain";

	    // Execute the query formed above.
	    this.executeDataQuery();
	    // Read the native format data and transform to a Spectrum.
	    this.transformData(params, response);

	} else if (format != null && format.equalsIgnoreCase("gif")) {
	    // The spectrum graphics format is GIF.
	    contentType = "image/gif";

	    // Open a connection to the data.
	    try {
		String query = this.getQueryUrl();
		in = new URL(query).openStream();
	    } catch (MalformedURLException ex) {
		throw new DalServerException(ex.getMessage());
	    } catch (IOException ex) {
		throw new DalServerException(ex.getMessage());
	    }

	} else {
	    throw new DalServerException("unrecognized format " + 
		"["+format+"]");
	}

	// Set the data content attributes.
	params.addParam(new Param("datasetContentType",
	    EnumSet.of(ParamType.STRING), contentType,
	    ParamLevel.SERVICE, false, "Content type of dataset"));
	params.addParam(new Param("datasetContentLength",
	    EnumSet.of(ParamType.STRING), contentLength,
	    ParamLevel.SERVICE, false, "Content length of dataset"));

	// Return a stream to read directly from this URL.
	// This could also be done with a redirect, but we may want
	// to add caching here so we just GET the data directly.

	return (in);
    }


    // ------------- Internal Methods ----------------

    /**
     * Build the spectrum services query.  This version is a proxy for the
     * actual JHU SSAP service.  In a more typical data service this would
     * instead build an SQL query to access the local archive database.
     *
     * @param	params	The input SSAP request parameter set.
     */
    private void buildSsapQuery(SsapParamSet params)
	throws DalServerException {

	// Set the base Query URL to be used.
	this.baseUrl = queryJhuSsap;

	urlParams = new LinkedHashMap<String,String>();
	Param p = null;
	String v = null;

	// This is a SSAP queryData request.
	urlParams.put("REQUEST", "queryData");
	
	// Set POS and SIZE if POS is set in the request.
	if ((p = params.getParam("POS")) != null && p.isSet()) {
	    urlParams.put("POS", p.stringValue());

	    // Default to 0.1 degree if no SIZE given.
	    double diam = 0.2;
	    if ((p = params.getParam("SIZE")) != null)
		diam = p.doubleValue();
	    urlParams.put("SIZE", new Double(diam).toString());
	}

	// We want to get data back in XML format as this is what our
	// old proxy service was written to consume.

	urlParams.put("FORMAT", "xml");

	// Pass these through if set in the SSAP request.
	if ((p = params.getParam("TIME")) != null && p.isSet())
	    urlParams.put("TIME", p.stringValue());
	if ((p = params.getParam("SPECRP")) != null && p.isSet())
	    urlParams.put("SPECRP", p.stringValue());
	if ((p = params.getParam("SNR")) != null && p.isSet())
	    urlParams.put("SNR", p.stringValue());
	if ((p = params.getParam("REDSHIFT")) != null && p.isSet())
	    urlParams.put("REDSHIFT", p.stringValue());
	if ((p = params.getParam("VARAMPL")) != null && p.isSet())
	    urlParams.put("VARAMPL", p.stringValue());
	if ((p = params.getParam("TARGETNAME")) != null && p.isSet())
	    urlParams.put("TARGETNAME", p.stringValue());
	if ((p = params.getParam("TARGETCLASS")) != null && p.isSet())
	    urlParams.put("TARGETCLASS", p.stringValue());
	if ((p = params.getParam("FLUXCALIB")) != null && p.isSet())
	    urlParams.put("FLUXCALIB", p.stringValue());

	// The JHU service doesn't appear to work unless Collection
	// is explicitly specified.

	if ((p = params.getParam("COLLECTION")) != null && p.isSet())
	    urlParams.put("COLLECTION", p.stringValue());
	else
	    urlParams.put("COLLECTION", "ivo://jhu/sdss/dr6/spec/2.5");
    }


    /**
     * Convert the query parameters into a URL string.  Characters in the
     * returned URL string are escaped as per the x-www-form-urlencoded
     * specification.
     */
    private String getQueryUrl() {
	if (urlParams == null)
	    return (null);

    	StringBuilder url = new StringBuilder();
	url.append(baseUrl);
	int nkeywords = 0;

	for (Map.Entry<String,String> keyword : urlParams.entrySet()) {
	    String key = keyword.getKey();
	    String value = keyword.getValue();
	    String sval = null;

	    if (nkeywords > 0)
		url.append("&");

	    if (key != null) {
		try {
		    sval = URLEncoder.encode(key, "UTF-8");
		} catch (UnsupportedEncodingException ex) {
		    sval = key;
		}
		url.append(sval);
	    }

	    if (value != null) {
		try {
		    sval = URLEncoder.encode(value, "UTF-8");
		} catch (UnsupportedEncodingException ex) {
		    sval = value;
		}

		url.append("=");
		url.append(sval);
	    }

	    nkeywords++;
	}

	return (url.toString());
    }


    /**
     * Execute a previously prepared spectrum services SSAP query.
     * The query response is read into a VOTable object.
     */
    private void executeQuery() throws DalServerException {
	InputStream in;

	// Execute the query and open a connection for the response.
	try {
	    String query = this.getQueryUrl();
	    in = new URL(query).openStream();
	} catch (MalformedURLException ex) {
	    throw new DalServerException(ex.getMessage());
	} catch (IOException ex) {
	    throw new DalServerException(ex.getMessage());
	}

	// Parse the query response VOTable.
	SavotPullParser savot = new SavotPullParser(in, 0, "utf-8");
	jhuQRVot = savot.getVOTable();
	in = null;

	// Load the Savot VOTable data into HashMaps for random access.
	jhuQR = new LinkedHashMap<String,TableParam>();
        SavotResource r = (SavotResource)jhuQRVot.getResources().getItemAt(0);
	SavotTable table = (SavotTable)r.getTables().getItemAt(0);

	// Read the table PARAMs.
	int paramCount = table.getParams().getItemCount();

        for (int i=0;  i < paramCount;  i++) {
            SavotParam param = (SavotParam)table.getParams().getItemAt(i);

	    // Use UTYPE as the key, otherwise fall back to UCD.
	    String key = param.getUtype();
	    if (key == null) {
		key = param.getUcd();
		if (key == null) {
		    key = param.getName();
		    if (key == null)
			key = new Integer(i).toString();
		}
	    }

	    // Generate a TableParam for the Savot parameter.
	    TableParam p = new TableParam(param.getName(),
		param.getValue(), param.getId(), null, param.getDataType(),
		param.getArraySize(), param.getUnit(), param.getUtype(),
		param.getUcd(), param.getDescription());

	    p.setIndex(-1);
            jhuQR.put(key, p);
        }

        // Read the table FIELDs and add to PARAM list with field index.
	int fieldCount = table.getFields().getItemCount();

        for (int i=0;  i < fieldCount;  i++) {
            SavotField field = (SavotField)table.getFields().getItemAt(i);

	    // Use UTYPE as the key, otherwise fall back to UCD.
	    String key = field.getUtype();
	    if (key == null) {
		key = field.getUcd();
		if (key == null) {
		    key = field.getName();
		    if (key == null)
			key = new Integer(i).toString();
		}
	    }

	    // Generate a TableParam for the Savot field.
	    TableParam p = new TableParam(field.getName(),
		null, field.getId(), null, field.getDataType(),
		field.getArraySize(), field.getUnit(), field.getUtype(),
		field.getUcd(), field.getDescription());

	    p.setIndex(i);
            jhuQR.put(key, p);
        }

        // Access the table data.
	jhuQRData = table.getData().getTableData().getTRs();
    }


    /**
     * Get the value of a query response VOTable PARAM or FIELD.
     * If the item is a PARAM the row index is ignored, otherwise
     * it is used to access the table data value.
     */
    private String getValue(String key, int rowIndex)
	throws DalServerException {

	final String errmsg = "Unknown query response keyword: ";
	TableParam p = jhuQR.get(key);
	if (p == null) {
	    p = jhuQR.get("ssa:" + key);
	    if (p == null)
		throw new DalServerException(errmsg + key);
	}

	// PARAM has a fixed value.
	if (p.getIndex() < 0)
	    return (p.getValue());

	// FIELD requires that we get value from the table data.
	TDSet row = jhuQRData.getTDSet(rowIndex);
	return ((row != null) ? row.getContent(p.getIndex()) : null);
    }


    /**
     * Compute the SSAP queryData response.  To do this we load the
     * metadata from the JHU query into the DALServer SSAP/Spectrum
     * data model in memory.
     */
    @SuppressWarnings("unchecked")
    private void computeResponse(SsapParamSet params, RequestResponse response)
	throws DalServerException {

	SsapKeywordFactory ssap = new SsapKeywordFactory(response, "main", "1.1");
        RequestResponse r = response;
	boolean overflow = false;
        String id, key;

        // Set global metadata.
        r.setDescription("DALServer proxy service for JHU spectrum services");
        r.setType("results");

        // This indicates the query executed successfully.  If an exception
        // occurs the output we generate here will never be returned anyway,
        // so OK is always appropriate here.

        r.addInfo(key="QUERY_STATUS", new TableInfo(key, "OK"));

        // Echo the query parameters as INFOs in the query response.
	for (Object o : params.entrySet()) {
	    Map.Entry<String,Param> keyVal = (Map.Entry<String,Param>)o;
	    Param p = keyVal.getValue();
	    if (!p.isSet() || p.getLevel() == ParamLevel.SERVICE)
		continue;
	    r.addInfo(id=p.getName(), new TableInfo(id, p.stringValue()));
	}

	// What formats do we return?
	String formats = params.getValue("FORMAT");
	boolean retCsv=true, retText=true, retVotable=true, retNative=true,
	    retGif=true;
	int nFormats = 5;

	if (formats != null) {
	    formats = formats.toLowerCase();
	    retCsv = retText = retVotable = retNative = retGif = false;
	    nFormats = 0;

	    if (formats.equals("all")) {
		retCsv = retText = retVotable = retNative = retGif = true;
		nFormats = 4;
	    } else {
		if (formats.contains("csv")) {
		    retCsv = true;
		    nFormats++;
		}
		if (formats.contains("text")) {
		    retText = true;
		    nFormats++;
		}
		if (formats.contains("votable")) {
		    retVotable = true;
		    nFormats++;
		}
		if (formats.contains("native") || formats.contains("xml")) {
		    retNative = true;
		    nFormats++;
		}
		if (formats.contains("gif") || formats.contains("graphic")) {
		    retGif = true;
		    nFormats++;
		}
	    }
	}

        // Create the table metadata for a standard SSAP query response.
	// Any fields which have a constant value for the table may be
	// output as PARAMs.

	// Query Metadata
	r.addGroup(ssap.newGroup("Query"));
	r.addField(ssap.newField("Score"));

	// Association Metadata.  We only have one Association here.
	String assocType = "MultiFormat";
	String assocId = null;
	int nAssoc = 0;

	if (nFormats > 1) {
	    r.addGroup(ssap.newGroup("Association"));
	    r.addParam(ssap.newParam("AssocType", assocType));
	    r.addParam(ssap.newParam("AssocKey", "@Format"));
	    r.addField(ssap.newField("AssocID"));
	}

	// Access Metadata
	r.addGroup(ssap.newGroup("Access"));
	r.addField(ssap.newField("AcRef"));
	r.addField(ssap.newField("Format"));
	r.addParam(ssap.newParam("DatasetSize", 1200*1024));

	// General Dataset Metadata
	r.addGroup(ssap.newGroup("Dataset"));
	r.addField(ssap.newField("DataModel"));
	r.addField(ssap.newField("DatasetType"));
	r.addField(ssap.newField("DataLength"));
	r.addField(ssap.newField("TimeSI"));
	r.addField(ssap.newField("SpectralSI"));
	r.addField(ssap.newField("FluxSI"));

	// Dataset Identification Metadata
	r.addGroup(ssap.newGroup("DataID"));
	r.addField(ssap.newField("Title"));
	r.addField(ssap.newField("Creator"));
	r.addField(ssap.newField("Collection"));
	r.addField(ssap.newField("CreatorDID"));
	r.addField(ssap.newField("CreatorDate"));
	r.addField(ssap.newField("CreatorVersion"));
	r.addField(ssap.newField("DatasetID"));
	r.addField(ssap.newField("Instrument"));
	r.addField(ssap.newField("Bandpass"));
	r.addField(ssap.newField("DataSource"));
	r.addField(ssap.newField("CreationType"));

	// Curation Metadata
	r.addGroup(ssap.newGroup("Curation"));
	r.addField(ssap.newField("Publisher"));
	r.addField(ssap.newField("PublisherDID"));
	// r.addField(ssap.newField("PublisherDate"));
	// r.addField(ssap.newField("PublisherVersion"));
	r.addField(ssap.newField("Rights"));
	// r.addField(ssap.newField("Reference"));

	// Target Metadata
	r.addGroup(ssap.newGroup("Target"));
	r.addField(ssap.newField("TargetPos"));
	r.addField(ssap.newField("TargetName"));
	r.addField(ssap.newField("TargetClass"));
	r.addField(ssap.newField("Redshift"));
	r.addField(ssap.newField("VarAmpl"));

	// Derived Metadata
	// r.addGroup(ssap.newGroup("Derived"));
	// r.addField(ssap.newField("DerivedSNR"));

	// Coordinate System Metadata
	r.addGroup(ssap.newGroup("CoordSys"));
	r.addField(ssap.newField("SpaceFrameName"));
	r.addField(ssap.newField("SpaceFrameEquinox"));
	r.addField(ssap.newField("TimeFrameName"));

	// Spatial Axis Characterization
	r.addGroup(ssap.newGroup("Char.SpatialAxis"));
	r.addField(ssap.newField("SpatialLocation"));
	r.addField(ssap.newField("SpatialExtent"));
	// r.addField(ssap.newField("SpatialArea"));
	// r.addField(ssap.newField("SpatialFillFactor"));
	// r.addField(ssap.newField("SpatialStatError"));
	// r.addField(ssap.newField("SpatialSysError"));
	// r.addField(ssap.newField("SpatialCalibration"));
	// r.addField(ssap.newField("SpatialResolution"));

	// Spectral Axis Characterization
	// r.addGroup(ssap.newGroup("Char.SpectralAxis"));
	// r.addField(ssap.newField("SpectralAxisUcd"));
	// r.addField(ssap.newField("SpectralLocation"));
	// r.addField(ssap.newField("SpectralExtent"));
	// r.addField(ssap.newField("SpectralStart"));
	// r.addField(ssap.newField("SpectralStop"));
	// r.addField(ssap.newField("SpectralFillFactor"));
	// r.addField(ssap.newField("SpectralBinSize"));
	// r.addField(ssap.newField("SpectralStatError"));
	// r.addField(ssap.newField("SpectralSysError"));
	// r.addField(ssap.newField("SpectralCalibration"));
	// r.addField(ssap.newField("SpectralResolution"));
	// r.addField(ssap.newField("SpectralResPower"));

	// Time Axis Characterization
	// r.addGroup(ssap.newGroup("Char.TimeAxis"));
	// r.addField(ssap.newField("TimeLocation"));
	// r.addField(ssap.newField("TimeExtent"));
	// r.addField(ssap.newField("TimeStart"));
	// r.addField(ssap.newField("TimeStop"));
	// r.addField(ssap.newField("TimeFillFactor"));
	// r.addField(ssap.newField("TimeBinSize"));
	// r.addField(ssap.newField("TimeStatError"));
	// r.addField(ssap.newField("TimeSysError"));
	// r.addField(ssap.newField("TimeCalibration"));
	// r.addField(ssap.newField("TimeResolution"));

	// Flux Axis Characterization
	// r.addGroup(ssap.newGroup("Char.FluxAxis"));
	// r.addField(ssap.newField("FluxAxisUcd"));
	// r.addField(ssap.newField("FluxStatError"));
	// r.addField(ssap.newField("FluxSysError"));
	// r.addField(ssap.newField("FluxCalibration"));

	// Any extra service-defined metadata could be defined here.

	// Return the first TOP rows?
	int top = 0;
	try {
	    Param p = params.getParam("TOP");
	    if (p != null && p.isSet())
		top = p.intValue();
	} catch (Exception ex) {
	    top = 0;
	}

	// Generate the table data.
	try {
	    for (int i=0;  i < jhuQRData.getItemCount();  i++) {
		// Here, TOP refers to objects, not file format options.
		if (top > 0 && i >= top)
		    break;

		if (nFormats > 1)
		    assocId = assocType + "." + new Integer(nAssoc++).toString();

		if (retCsv) {
		    r.addRow();
		    if (nFormats > 1)
			r.setValue("AssocID", assocId);
		    this.setMetadata(params, r, i, "csv");
		}
		if (retText) {
		    r.addRow();
		    if (nFormats > 1)
			r.setValue("AssocID", assocId);
		    this.setMetadata(params, r, i, "text");
		}
		if (retVotable) {
		    r.addRow();
		    if (nFormats > 1)
			r.setValue("AssocID", assocId);
		    this.setMetadata(params, r, i, "votable");
		}
		if (retNative) {
		    r.addRow();
		    if (nFormats > 1)
			r.setValue("AssocID", assocId);
		    this.setMetadata(params, r, i, "native");
		}
		if (retGif) {
		    r.addRow();
		    if (nFormats > 1)
			r.setValue("AssocID", assocId);
		    this.setMetadata(params, r, i, "gif");
		}
	    }
	} catch (DalOverflowException ex) {
	    overflow = true;
	}

	// Show the number of table rows in the response header.
	r.addInfo(key="TableRows",
	    new TableInfo(key, new Integer(r.size()).toString()));

	if (!overflow) {
	    // Compute a default normalized SCORE heuristic for each matched
	    // dataset, placing the result in the named table field.
	    r.score((dalserver.ParamSet)params, "Score");
	    
	    // Sort the result set by SCORE.
	    r.sort("Score", -1);
	}
    }

    /**
     * Set the content of one query response record.
     */
    private void setMetadata(SsapParamSet params, RequestResponse r,
	int rowIndex, String format) throws DalServerException {

	// Query metadata.
	// (Nothing to do here, as SCORE is computed later).

	// Access metadata.  Output one record for each output format
	// available.

	String datasetId = this.getValue("Curation.PublisherDID", rowIndex);
	String runId = params.getValue("RunID");
	String serviceName = params.getValue("serviceName");
	String baseUrl = params.getValue("baseUrl");
	if (!baseUrl.endsWith("/"))
	    baseUrl += "/";

	try {
	    String acRef = baseUrl + serviceName + "?" +
		"REQUEST=getData" + "&" +
		"FORMAT=" + format + "&" +
		"PubDID=" + URLEncoder.encode(datasetId, "UTF-8");
	    if (runId != null)
		acRef += "&RunID=" + runId;
	    r.setValue("AcRef", acRef);

	} catch (UnsupportedEncodingException ex) {
	    throw new DalServerException("URL encoding failed");
	}

	// We don't know the dataset size, but an estimate is ok.
	r.setValue("DatasetSize", "800");

	// Supported formats are CSV, Text, VOTable and native format.
	if (format.equals("csv")) {
	    r.setValue("Format", "text/csv");
	    r.setValue("DataModel", "Spectrum-1.0");
	} else if (format.equals("text")) {
	    r.setValue("Format", "text/plain");
	    r.setValue("DataModel", "Spectrum-1.0");
	} else if (format.equals("votable")) {
	    r.setValue("Format", "application/x-votable+xml");
	    r.setValue("DataModel", "Spectrum-1.0");
	} else if (format.equals("native")) {
	    r.setValue("Format", "text/xml");
	    r.setValue("DataModel", "Spectrum-1.0");
	} else if (format.equals("gif")) {
	    r.setValue("Format", "image/gif");
	    r.setValue("DataModel", "None");
	}

	// General dataset metadata.
	r.setValue("DataModel", this.getValue("Dataset.DataModel", rowIndex));
	r.setValue("DatasetType", this.getValue("Dataset.Type", rowIndex));
	r.setValue("DataLength", 4000);   // approximate value ok here
	r.setValue("TimeSI", this.getValue("Dataset.TimeSI", rowIndex));
	r.setValue("SpectralSI", this.getValue("Dataset.SpectralSI", rowIndex));
	r.setValue("FluxSI", this.getValue("Dataset.FluxSI", rowIndex));

	// Dataset identification metadata.
	r.setValue("Title",
	    this.getValue("Target.Name", rowIndex) + " " +
	    this.getValue("Target.Class", rowIndex));
	r.setValue("Creator", this.getAuth(this.getValue("DataId.CreatorDID", rowIndex)));
	r.setValue("Collection", this.getValue("DataId.Collection", rowIndex));
	r.setValue("CreatorDID", this.getValue("DataId.CreatorDID", rowIndex));
	r.setValue("CreatorDate", this.getValue("DataId.Date", rowIndex));
	r.setValue("CreatorVersion", this.getValue("DataId.Version", rowIndex));
	r.setValue("DatasetID", this.getValue("DataId.CreatorDID", rowIndex));
	r.setValue("Instrument", this.getValue("DataId.Instrument", rowIndex));
	r.setValue("Bandpass", this.getValue("DataId.Bandpass", rowIndex));
	r.setValue("DataSource", this.getValue("DataId.DataSource", rowIndex));
	r.setValue("CreationType", this.getValue("DataId.CreationType", rowIndex));

	// Dataset curation metadata.
	r.setValue("Publisher", this.getValue("Curation.Publisher", rowIndex));
	r.setValue("PublisherDID", this.getValue("Curation.PublisherDID", rowIndex));
	// r.setValue("PublisherDate", this.getValue("Curation.PublisherDate", rowIndex));
	// r.setValue("PublisherVersion", this.getValue("Curation.PublisherVersion", rowIndex));
	r.setValue("Rights", this.getValue("Curation.Rights", rowIndex));
	// r.setValue("Reference", this.getValue("Curation.Reference", rowIndex));

	// Astronomical target metadata.
	r.setValue("TargetPos", this.getValue("Target.Pos", rowIndex));
	r.setValue("TargetName", this.getValue("Target.Name", rowIndex));
	r.setValue("TargetClass", this.getValue("Target.Class", rowIndex));
	r.setValue("Redshift", this.getValue("Target.Redshift", rowIndex));
	r.setValue("VarAmpl", this.getValue("Target.VarAmpl", rowIndex));

	// Derived Metadata
	// (none yet)

	// Coordinate system metadata.
	r.setValue("SpaceFrameName", this.getValue("CoordSys.SpaceFrame.Name", rowIndex));
	r.setValue("SpaceFrameEquinox", this.getValue("CoordSys.SpaceFrame.Equinox", rowIndex));
	r.setValue("TimeFrameName", this.getValue("CoordSys.TimeFrame.Name", rowIndex));

	// Spatial Axis Characterization.
	r.setValue("SpatialLocation", this.getValue("Target.Pos", rowIndex));
	// r.setValue("SpatialExtent", this.getValue("Coverage.Extent.Sky", rowIndex));

	// Spectral Axis Characterization.

	// Time Axis Characterization.

	// Flux Characterization.
    }


    /**
     * Execute a spectrum services get data query returning XML data.
     * The query response, an XML block, is parsed on the fly and returned
     * as a hash table of UTYPE-value pairs.
     */
    private void executeDataQuery() throws DalServerException {
	// Create a parser factory and use it to create a parser.
	SAXParserFactory parserFactory = null;
	SAXParser parser = null;

	try {
	    parserFactory = SAXParserFactory.newInstance();
	    parser = parserFactory.newSAXParser();
	} catch (ParserConfigurationException ex) {
	    throw new DalServerException("XML parser creation fails");
	} catch (SAXException ex) {
	    throw new DalServerException(
		"SAX exception during query execution");
	}

	// Create a handler for the SAX events.
	SAXHandler handler = new SAXHandler();

	// Create an initial empty JhuQR reponse table.
	jhuXml = new ArrayList<LinkedHashMap<String,TableParam>>();

	// Invoke the URL and parse the results.

	try {
	    // InputStream in = new URL(query).openStream();
	    // BufferedReader reader =
	    // new BufferedReader(new InputStreamReader(in, "US-ASCII"));
	    // InputSource is = new InputSource(reader);
	    // parser.parse(is, handler);

	    String query = this.getQueryUrl();
	    parser.parse(query, handler);

	} catch (MalformedURLException ex) {
	    throw new DalServerException(
		"Illegal data URL");
	} catch (SAXException ex) {
	    throw new DalServerException(
		"SAX exception during query execution");
	} catch (IOException ex) {
	    throw new DalServerException(
		"IO exception during query execution");
	}
    }


    /**
     * JHU serialization-specific SAX handler for parsing a spectrum
     * service structured XML document and converting it into a hash table of
     * UTYPE vs string value keywords.  For "returnPoints=true" documents
     * the data arrays will be extracted (as an array of strings) and stored
     * in the TableParam object returned for the corresponding data model
     * element.  For simple data models, this keyword lookup approach is
     * preferred for simple, fast, and representation- independent lookup of
     * data model elements in subsequent computations.  
     */
    private class SAXHandler extends DefaultHandler {
	static final int MAXLEVELS = 32;

	// Private data for SAX parser.
	private LinkedHashMap<String,TableParam> segment;
	private ArrayList<String> arrayData;
	private String elements[] = new String[MAXLEVELS];
	private Attributes attributes[] = new Attributes[MAXLEVELS];
	private String values[] = new String[MAXLEVELS];
	private int level = 0;  private int nSegments = 0;
	private int nSpectrum = 0;

	// Called when starting a new element.
	public void startElement(String uri, String localname, String qname,
	    Attributes attributes) {

	    if (qname.equals("Spectrum")) {
		// Start a new Spectrum element.
		nSpectrum++;
		nSegments = 0;
		level = 0;
		this.elements[level] = null;
		this.attributes[level] = null;
		this.values[level] = null;

		// Start a new Segment (spectrum).  This isn't entirely
		// correct for the old Sed model, but works for Spectrum where
		// there is only one segment per Sed, and the Sed metadata is
		// part of Spectrum.

		this.arrayData = null;
		this.segment = new LinkedHashMap<String,TableParam>();
		jhuXml.add(this.segment);
		nSegments++;

	    } else if (qname.equals("double") || qname.equals("long")) {
		// Add a numeric value to the arrayData for current element.
		if (this.arrayData == null)
		    this.arrayData = new ArrayList<String>(MAXPTS);

	    } else {
		// Add element name to the UTYPE.
		this.elements[level] = qname;
		this.attributes[level] = attributes;
		this.values[level] = null;
		level++;
	    }
	}
	
	// Called when the end of an element is reached.
	public void endElement(String uri, String localname, String qname) {

	    // Array data is a special case as we do not pop the stack.
	    if (arrayData != null &&
		    qname.equals("double") || qname.equals("long")) {

		String text = this.values[level-1];
		if (text != null && text.length() > 0)
		    this.arrayData.add(text.trim());

		this.values[level-1] = null;
		return;
	    }

	    // Pop a level (will go negative on elements ignored above).
	    if (--level < 0)
		level = 0;

	    // Store only elements with values.
	    Attributes at = attributes[level];
	    if ((at != null) && (at.getLength() > 0) && (level >= 0) ||
		arrayData != null) {

		// Form the UTYPE from the element names.
		StringBuilder utype = new StringBuilder();
		for (int i=0;  i <= level;  i++) {
		    if (!elements[i].startsWith("Segment")) {
			utype.append(elements[i]);
			if (i < level)
			    utype.append(".");
		    }
		}

		// Create a TableParam object for the element.
		TableParam p = new TableParam();
		p.setUtype(utype.toString());

		// Store the element attributes, if any.
		for (int i = 0;  i < at.getLength();  i++) {
		    String atName = at.getQName(i);
		    String atValue = at.getValue(i);
		    String str;

		    // Kludge to avoid Unicode in string values.
		    // A better way to do this would be to force
		    // Savot to write the VOTable out in UTF-8,
		    // rather than the default JVM charset.

		    try {
			str = new String(atValue.getBytes("US-ASCII"));
		    } catch (UnsupportedEncodingException ex) {
			str = atValue;
		    }

		    if (atName.equalsIgnoreCase("ucd"))
			p.setUcd(str);
		    else if (atName.equalsIgnoreCase("unit"))
			p.setUnit(str);
		    else if (atName.equalsIgnoreCase("value"))
			p.setValue(str);
		    else if (arrayData != null && values[level] != null)
			p.setValue(values[level]);
		}

		// Set the array data if this is an array-valued parameter.
		if (this.arrayData != null)
		    p.setArrayData(this.arrayData, false);

		// Store the Param.
		if (this.segment != null) {
		    String key = utype.toString();
		    this.segment.put(key, p);
		}
	    }

	    // Clear the now unused element storage.
	    elements[level] = null;
	    attributes[level] = null;
	    arrayData = null;
	}

	// Called to process any text contained within an element.  This is
	// used for the element value if no value is explicitly specified.

	public void characters(char[] text, int start, int length) {
	    String s = new String(text, start, length);

	    if (s.length() > 0 && level > 0) {
		String val = this.values[level-1];

		// This is tricky as multiple calls may be necessary to
		// process a single character sequence.  Hence we need to
		// append text to build up the value.

		if (val != null)
		    val = new String(val + s);
		else
		    val = s;

		this.values[level-1] = val;
	    }
	}
    }


    /**
     * Transform a native JhuSS spectrum dataset into a Spectrum object,
     * conformant to the Spectrum data model.
     */
    @SuppressWarnings("unchecked")
    private void transformData(SsapParamSet params, RequestResponse response)
	throws DalServerException {

	SpectrumKeywordFactory sdm = new SpectrumKeywordFactory(response, "1.1");
	LinkedHashMap<String,TableParam> s = jhuXml.get(0);
        RequestResponse r = response;
        String id, key;

        // Set global metadata.
        r.setDescription("Spectrum dataset generated by DALServer");
        r.setUtype("Spectrum");


	// Define the table data.
	// --------------------------------

	// General Dataset Metadata
	r.addGroup(sdm.newGroup("Spectrum"));
	r.addParam(sdm.newParam("DataModel", "Spectrum-1.0"));
	r.addParam(sdm.newParam("DatasetType", "Spectrum"));
	r.addParam(sdm.newParam("DataLength", this.getXmlValue(s, "Spectrum.Length")));
	r.addParam(sdm.newParam("TimeSI", this.getXmlValue(s, "Spectrum.TimeSI")));
	r.addParam(sdm.newParam("SpectralSI", this.getXmlValue(s, "Spectrum.SpectralSI")));
	r.addParam(sdm.newParam("FluxSI", this.getXmlValue(s, "Spectrum.FluxSI")));


	// Dataset Identification Metadata
	r.addGroup(sdm.newGroup("DataID"));
	r.addParam(sdm.newParam("Title",
	    this.getXmlValue(s, "Target.Name") + " " +
	    this.getXmlValue(s, "Target.Class") + " " +
	    this.getXmlValue(s, "Target.Description")));
	r.addParam(sdm.newParam("Creator", this.getAuth(this.getXmlValue(s, "DataId.CreatorDID"))));
	r.addParam(sdm.newParam("Collection", this.getXmlValue(s, "DataId.Collection")));
	r.addParam(sdm.newParam("DatasetID", this.getXmlValue(s, "DataId.CreatorDID")));
	r.addParam(sdm.newParam("CreatorDID", this.getXmlValue(s, "DataId.CreatorDID")));
	r.addParam(sdm.newParam("CreatorDate", this.getXmlValue(s, "DataId.Date")));
	r.addParam(sdm.newParam("CreatorVersion", this.getXmlValue(s, "DataId.Version")));
	r.addParam(sdm.newParam("Instrument", this.getXmlValue(s, "DataId.Instrument")));
	r.addParam(sdm.newParam("Bandpass", this.getXmlValue(s, "DataId.Bandpass")));
	r.addParam(sdm.newParam("DataSource", this.getXmlValue(s, "DataId.DataSource")));
	r.addParam(sdm.newParam("CreationType", this.getXmlValue(s, "DataId.CreationType")));
	// r.addParam(sdm.newParam("CreatorLogo", "UNSET"));
	// r.addParam(sdm.newParam("Contributor", "UNSET"));


	// Curation Metadata
	r.addGroup(sdm.newGroup("Curation"));
	r.addParam(sdm.newParam("Publisher", this.getXmlValue(s, "Curation.Publisher")));
	// r.addParam(sdm.newParam("PublisherID", "UNSET"));
	r.addParam(sdm.newParam("PublisherDID", this.getXmlValue(s, "Curation.PublisherDID")));
	r.addParam(sdm.newParam("PublisherDate", this.getXmlValue(s, "Curation.Date")));
	r.addParam(sdm.newParam("PublisherVersion", this.getXmlValue(s, "Curation.Version")));
	r.addParam(sdm.newParam("Rights", this.getXmlValue(s, "Curation.Rights")));
	// r.addParam(sdm.newParam("Reference", "UNSET"));
	r.addParam(sdm.newParam("ContactName", this.getXmlValue(s, "Curation.Contact.ContactName")));
	r.addParam(sdm.newParam("ContactEmail", this.getXmlValue(s, "Curation.Contact.ContactEmail")));


	// Target Metadata
	r.addGroup(sdm.newGroup("Target"));
	r.addParam(sdm.newParam("TargetName", this.getXmlValue(s, "Target.Name")));
	r.addParam(sdm.newParam("TargetDescription", this.getXmlValue(s, "Target.Description")));
	r.addParam(sdm.newParam("TargetClass", this.getXmlValue(s, "Target.Class")));
	r.addParam(sdm.newParam("TargetPos", this.getXmlValue(s, "Target.Pos")));
	r.addParam(sdm.newParam("SpectralClass", this.getXmlValue(s, "Target.SpectralClass")));
	r.addParam(sdm.newParam("Redshift", this.getXmlValue(s, "Target.Redshift")));
	r.addParam(sdm.newParam("VarAmpl", this.getXmlValue(s, "Target.VarAmpl")));


	// Derived Metadata
	r.addGroup(sdm.newGroup("Derived"));
	r.addParam(sdm.newParam("DerivedSNR", this.getXmlValue(s, "Derived.SNR")));
	r.addParam(sdm.newParam("DerivedRedshift", this.getXmlValue(s, "Derived.Redshift.Value")));
	r.addParam(sdm.newParam("RedshiftStatError",
	    this.getXmlValue(s, "Derived.Redshift.StatError")));
	r.addParam(sdm.newParam("RedshiftConfidence",
	    this.getXmlValue(s, "Derived.Redshift.Confidence")));
	r.addParam(sdm.newParam("DerivedVarAmpl", this.getXmlValue(s, "Derived.VarAmpl")));


	// Coordinate System Metadata
	r.addGroup(sdm.newGroup("CoordSys"));
	// r.addParam(sdm.newParam("CoordSysID", "UNSET"));
	r.addParam(sdm.newParam("SpaceFrameName", this.getXmlValue(s, "CoordSys.SpaceFrame.Name")));
	// r.addParam(sdm.newParam("SpaceFrameUcd", this.getUcd(s, "Coverage.Location.Sky")));
	r.addParam(sdm.newParam("SpaceFrameRefPos", this.getXmlValue(s, "CoordSys.SpaceFrame.RefPos")));
	r.addParam(sdm.newParam("SpaceFrameEquinox", this.getXmlValue(s, "CoordSys.SpaceFrame.Equinox")));

	r.addParam(sdm.newParam("TimeFrameName", this.getXmlValue(s, "CoordSys.TimeFrame.Name")));
	r.addParam(sdm.newParam("TimeFrameUcd", this.getXmlValue(s, "CoordSys.TimeFrame.UCD")));
	r.addParam(sdm.newParam("TimeFrameZero", this.getXmlValue(s, "CoordSys.TimeFrame.Zero")));
	r.addParam(sdm.newParam("TimeFrameRefPos", this.getXmlValue(s, "CoordSys.TimeFrame.RefPos")));

	// r.addParam(sdm.newParam("SpectralFrameName", "UNSET"));
	// r.addParam(sdm.newParam("SpectralFrameUcd", this.getUcd(s, "Points.SpectralCoord.Value")));
	r.addParam(sdm.newParam("SpectralFrameRefPos", this.getXmlValue(s, "CoordSys.SpectralFrame.RefPos")));
	r.addParam(sdm.newParam("SpectralFrameRedshift", this.getXmlValue(s, "CoordSys.SpectralFrame.Redshift")));
	// r.addParam(sdm.newParam("RedshiftFrameName", "UNSET"));
	// r.addParam(sdm.newParam("DopplerDefinition", "UNSET"));
	// r.addParam(sdm.newParam("RedshiftFrameRefPos", "UNSET"));


	// Spatial Axis Characterization
	r.addGroup(sdm.newGroup("Char.SpatialAxis"));
	r.addParam(sdm.newParam("SpatialAxisName", "Sky"));
	r.addParam(sdm.newParam("SpatialAxisUcd", this.getUcd(s, "Data.SpatialAxis.Coverage.Location.Value")));
	r.addParam(sdm.newParam("SpatialAxisUnit", this.getUnit(s, "Data.SpatialAxis.Accuracy.StatError")));
	r.addParam(sdm.newParam("SpatialLocation", this.getXmlValue(s, "Data.SpatialAxis.Coverage.Location")));
	r.addParam(sdm.newParam("SpatialExtent", this.getXmlValue(s, "Data.SpatialAxis.Coverage.Bounds.Extent")));
	// r.addParam(sdm.newParam("SpatialArea", "UNSET"));
	// r.addParam(sdm.newParam("SpatialFillFactor", "UNSET"));
	// r.addParam(sdm.newParam("SpatialStatError", "UNSET"));
	// r.addParam(sdm.newParam("SpatialSysError", "UNSET"));
	r.addParam(sdm.newParam("SpatialCalibration", this.getXmlValue(s, "Data.SpatialAxis.Calibration")));
	// r.addParam(sdm.newParam("SpatialResolution", "UNSET"));


	// Spectral Axis Characterization
	r.addGroup(sdm.newGroup("Char.SpectralAxis"));
	r.addParam(sdm.newParam("SpectralAxisName", "SpectralCoord"));
	r.addParam(sdm.newParam("SpectralAxisUcd", this.getUcd(s, "Data.SpectralAxis.Value")));
	r.addParam(sdm.newParam("SpectralAxisUnit", this.getUnit(s, "Data.SpectralAxis.Value")));

	double w1 = new Double(this.getXmlValue(s, "Data.SpectralAxis.Coverage.Bounds.Start")).doubleValue();
	double w2 = new Double(this.getXmlValue(s, "Data.SpectralAxis.Coverage.Bounds.Stop")).doubleValue();
	r.addParam(sdm.newParam("SpectralLocation", new Double((w1 + w2) / 2.0).toString()));
	r.addParam(sdm.newParam("SpectralExtent", this.getXmlValue(s, "Data.SpectralAxis.Coverage.Bounds.Extent")));
	r.addParam(sdm.newParam("SpectralStart", this.getXmlValue(s, "Data.SpectralAxis.Coverage.Bounds.Start")));
	r.addParam(sdm.newParam("SpectralStop", this.getXmlValue(s, "Data.SpectralAxis.Coverage.Bounds.Stop")));
	r.addParam(sdm.newParam("SpectralFillFactor",
	    this.getXmlValue(s, "Data.SpectralAxis.SamplingPrecision.SamplingPrecisionRefVal.FillFactor")));
	// r.addParam(sdm.newParam("SpectralBinSize", this.getXmlValue(s, "BinSize")));
	// r.addParam(sdm.newParam("SpectralStatError", this.getXmlValue(s, "StatErrHigh")));
	// r.addParam(sdm.newParam("SpectralSysError", this.getXmlValue(s, "SysErr")));
	r.addParam(sdm.newParam("SpectralCalibration", this.getXmlValue(s, "Data.SpectralAxis.Calibration")));
	// r.addParam(sdm.newParam("SpectralResolution", this.getXmlValue(s, "Resolution")));
	// r.addParam(sdm.newParam("SpectralResPower", "UNSET"));


	// Time Axis Characterization
	r.addGroup(sdm.newGroup("Char.TimeAxis"));
	r.addParam(sdm.newParam("TimeAxisName", "Time"));
	r.addParam(sdm.newParam("TimeAxisUcd", this.getXmlValue(s, "Data.TimeAxis.Value")));
	r.addParam(sdm.newParam("TimeAxisUnit", this.getUnit(s, "Data.TimeAxis.Value")));
	r.addParam(sdm.newParam("TimeExtent", this.getXmlValue(s, "Data.TimeAxis.Coverage.Support.Extent")));

	double t1 = new Double(this.getXmlValue(s, "Data.TimeAxis.Coverage.Bounds.Start")).doubleValue();
	double t2 = new Double(this.getXmlValue(s, "Data.TimeAxis.Coverage.Bounds.Stop")).doubleValue();
	t1 = t1 / (24 * 60 * 60);  t2 = t2 / (24 * 60 * 60);

	r.addParam(sdm.newParam("TimeLocation", new Double((w1 + w2) / 2.0).toString()));
	r.addParam(sdm.newParam("TimeStart", new Double(w1).toString()));
	r.addParam(sdm.newParam("TimeStop", new Double(w2).toString()));
	r.addParam(sdm.newParam("TimeFillFactor",
	    this.getXmlValue(s, "Data.TimeAxis.SamplingPrecision.SamplingPrecisionRefVal.FillFactor")));

	// r.addParam(sdm.newParam("TimeFillFactor", "UNSET"));
	// r.addParam(sdm.newParam("TimeBinSize", "UNSET"));
	// r.addParam(sdm.newParam("TimeStatError", "UNSET"));
	// r.addParam(sdm.newParam("TimeSysError", "UNSET"));
	r.addParam(sdm.newParam("TimeCalibration", this.getXmlValue(s, "Data.TimeAxis.Calibration")));
	// r.addParam(sdm.newParam("TimeCalibration", "UNSET"));
	// r.addParam(sdm.newParam("TimeResolution", "UNSET"));


	// Flux Axis Characterization
	r.addGroup(sdm.newGroup("Char.FluxAxis"));
	r.addParam(sdm.newParam("FluxAxisName", "Flux"));
	r.addParam(sdm.newParam("FluxAxisUcd", this.getXmlValue(s, "Data.FluxAxis.Value")));
	r.addParam(sdm.newParam("FluxAxisUnit", this.getUnit(s, "Data.FluxAxis.Value")));
	r.addParam(sdm.newParam("FluxCalibration", this.getXmlValue(s, "Data.FluxAxis.Calibration")));
	// r.addParam(sdm.newParam("FluxStatError", this.getXmlValue(s, "StatErrHigh")));
	// r.addParam(sdm.newParam("FluxSysError", "UNSET"));


	// ------------ DATA Values ------------
	// What we have:
	//
	//    Spectral_Value
	//    Spectral_Accuracy_BinLow
	//    Spectral_Accuracy_BinHigh
	//    Flux_Value
	//    Flux_Accuracy_StatErrLow
	//    Flux_Accuracy_StatErrHigh
	//    Flux_Accuracy_Quality

	// Spectral Axis Data
	r.addGroup(sdm.newGroup("Data.SpectralAxis"));
	r.addField(sdm.newField("DataSpectralValue"));
	r.addParam(sdm.newParam("DataSpectralUcd", this.getUcd(s, "Data.SpectralAxis.Value")));
	r.addParam(sdm.newParam("DataSpectralUnit", this.getUnit(s, "Data.SpectralAxis.Value")));
	// r.addField(sdm.newField("DataSpectralBinSize"));

	if (s.get("Spectral_Accuracy_BinLow") != null)
	    r.addField(sdm.newField("DataSpectralBinLow"));
	if (s.get("Spectral_Accuracy_BinHigh") != null)
	    r.addField(sdm.newField("DataSpectralBinHigh"));

	// r.addField(sdm.newField("DataSpectralStatError"));
	// r.addField(sdm.newField("DataSpectralStatErrLow"));
	// r.addField(sdm.newField("DataSpectralStatErrHigh"));
	// r.addField(sdm.newField("DataSpectralSysError"));
	// r.addField(sdm.newField("DataSpectralResolution"));

	// Flux Axis Data
	r.addGroup(sdm.newGroup("Data.FluxAxis"));
	r.addField(sdm.newField("DataFluxValue"));
	r.addParam(sdm.newParam("DataFluxUcd", this.getUcd(s, "Data.FluxAxis.Value")));
	r.addParam(sdm.newParam("DataFluxUnit", this.getUnit(s, "Data.FluxAxis.Value")));

	// r.addField(sdm.newField("DataFluxStatError"));
	r.addField(sdm.newField("DataFluxStatErrLow"));
	r.addField(sdm.newField("DataFluxStatErrHigh"));
	// r.addField(sdm.newField("DataFluxSysError"));
	r.addField(sdm.newField("DataFluxQuality"));
	// r.addField(sdm.newField("DataFluxQualityDesc"));

	// Time Axis Data
	// r.addGroup(sdm.newGroup("Data.TimeAxis"));
	// r.addField(sdm.newField("DataTimeValue"));
	// r.addField(sdm.newField("DataTimeUcd"));
	// r.addField(sdm.newField("DataTimeUnit"));
	// r.addField(sdm.newField("DataTimeBinSize"));
	// r.addField(sdm.newField("DataTimeBinLow"));
	// r.addField(sdm.newField("DataTimeBinHigh"));
	// r.addField(sdm.newField("DataTimeStatError"));
	// r.addField(sdm.newField("DataTimeStatErrLow"));
	// r.addField(sdm.newField("DataTimeStatErrHigh"));
	// r.addField(sdm.newField("DataTimeSysError"));
	// r.addField(sdm.newField("DataTimeResolution"));

	// Background Model Data
	// r.addGroup(sdm.newGroup("Data.BackgroundModel"));
	// r.addField(sdm.newField("DataBkgModelValue"));
	// r.addField(sdm.newField("DataBkgModelUcd"));
	// r.addField(sdm.newField("DataBkgModelUnit"));
	// r.addField(sdm.newField("DataBkgModelStatError"));
	// r.addField(sdm.newField("DataBkgModelStatErrLow"));
	// r.addField(sdm.newField("DataBkgModelStatErrHigh"));
	// r.addField(sdm.newField("DataBkgModelSysError"));
	// r.addField(sdm.newField("DataBkgModelQuality"));


	// Generate the table data.
	// --------------------------------

	// Get the input spectral coordinate and binning vectors.
	TableParam wvP = s.get("Spectral_Value");
	ArrayList<String> waveD = null;
	if (wvP != null)
	    waveD = (ArrayList<String>) wvP.getArrayData();

	TableParam blP = s.get("Spectral_Accuracy_BinLow");
	ArrayList<String> binLowD = null;
	if (blP != null)
	    binLowD = (ArrayList<String>) blP.getArrayData();

	TableParam bhP = s.get("Spectral_Accuracy_BinHigh");
	ArrayList<String> binHighD = null;
	if (bhP != null)
	    binHighD = (ArrayList<String>) bhP.getArrayData();

	// Get the input flux, flux error, and quality vectors.
	TableParam fluxP = s.get("Flux_Value");
	ArrayList<String> fluxD = null;
	if (fluxP != null)
	    fluxD = (ArrayList<String>) fluxP.getArrayData();

	TableParam elP = s.get("Flux_Accuracy_StatErrLow");
	ArrayList<String> errLowD = null;
	if (elP != null)
	    errLowD = (ArrayList<String>) elP.getArrayData();

	TableParam ehP = s.get("Flux_Accuracy_StatErrHigh");
	ArrayList<String> errHighD = null;
	if (ehP != null)
	    errHighD = (ArrayList<String>) ehP.getArrayData();

	TableParam qualP = s.get("Flux_Accuracy_Quality");
	ArrayList<String> qualD = null;
	if (qualP != null)
	    qualD = (ArrayList<String>) qualP.getArrayData();

	// Assign the UCD and Unit values to field attributes.
	r.setUcd("DataSpectralValue", r.getValue("DataSpectralUcd"));
	r.setUnit("DataSpectralValue", r.getValue("DataSpectralUnit"));

	if (binLowD != null) {
	    r.setUcd("DataSpectralBinLow", r.getValue("DataSpectralUcd"));
	    r.setUnit("DataSpectralBinLow", r.getValue("DataSpectralUnit"));
	}
	if (binHighD != null) {
	    r.setUcd("DataSpectralBinHigh", r.getValue("DataSpectralUcd"));
	    r.setUnit("DataSpectralBinHigh", r.getValue("DataSpectralUnit"));
	}

	r.setUcd("DataFluxValue", r.getValue("DataFluxUcd"));
	r.setUnit("DataFluxValue", r.getValue("DataFluxUnit"));
	r.setUcd("DataFluxStatErrLow", r.getValue("DataFluxUcd"));
	r.setUnit("DataFluxStatErrLow", r.getValue("DataFluxUnit"));
	r.setUcd("DataFluxStatErrHigh", r.getValue("DataFluxUcd"));
	r.setUnit("DataFluxStatErrHigh", r.getValue("DataFluxUnit"));

	// Sanity check.
	if (waveD == null)
	    throw new DalServerException("No spectral data [Spectral_Value]");
	if (fluxD == null)
	    throw new DalServerException("No spectral data [Flux_Value]");

	// Write the output data, which is a table with one data point
	// per row.

	for (int i=0;  i < waveD.size();  i++) {
	    try {
		r.addRow();
	    } catch (DalOverflowException ex) {
		;
	    }

	    if (waveD != null)
		r.setValue("DataSpectralValue", waveD.get(i));
	    if (binLowD != null)
		r.setValue("DataSpectralBinLow", binLowD.get(i));
	    if (binHighD != null)
		r.setValue("DataSpectralBinHigh", binHighD.get(i));

	    if (fluxD != null)
		r.setValue("DataFluxValue", fluxD.get(i));
	    if (errLowD != null)
		r.setValue("DataFluxStatErrLow", errLowD.get(i));
	    if (errHighD != null)
		r.setValue("DataFluxStatErrHigh", errHighD.get(i));

	    if (qualD != null)
		r.setValue("DataFluxQuality", qualD.get(i));
	}

	// Patch up earlier values now that we know more.
	r.setValue("DataLength", r.getRowCount());

	// Set CSV output to use DataFluxStatErrHigh for the error value.
	r.setCsvKeyword("DataFluxStatErrHigh", "3;DataFluxStatErr");
    }

    /**
     * Convenience routine to get a metadata value from a JhuQR spectrum.
     *
     * @param	key	The UTYPE name for the data model attribute,
     *			with or without a leading "Segment" or "Target".
     *			The leading "Sed" from the old data model is never
     *			used here.
     */
    private String getXmlValue(LinkedHashMap<String,TableParam> spectrum,
	String key) {

	TableParam p;
	if ((p = spectrum.get(key)) != null)
	    return (p.getValue());
	if ((p = spectrum.get("Spectrum." + key)) != null)
	    return (p.getValue());

	return (null);
    }

    /**
     * Convenience routine to get the UCD of a JhuQR spectrum attribute.
     *
     * @param	key	The UTYPE name for the data model attribute,
     *			with or without a leading "Segment" or "Target".
     *			The leading "Sed" from the old data model is never
     *			used here.
     */
    private String getUcd(LinkedHashMap<String,TableParam> spectrum,
	String key) {

	TableParam p;
	if ((p = spectrum.get(key)) != null)
	    return (p.getUcd());
	if ((p = spectrum.get("Spectrum." + key)) != null)
	    return (p.getUcd());

	return (null);
    }

    /**
     * Convenience routine to get the Unit of a JhuQR spectrum attribute.
     *
     * @param	key	The UTYPE name for the data model attribute,
     *			with or without a leading "Segment" or "Target".
     *			The leading "Sed" from the old data model is never
     *			used here.
     */
    private String getUnit(LinkedHashMap<String,TableParam> spectrum,
	String key) {

	TableParam p;
	if ((p = spectrum.get(key)) != null)
	    return (p.getUnit());
	if ((p = spectrum.get("Spectrum." + key)) != null)
	    return (p.getUnit());

	return (null);
    }

    /**
     * Get the "authority" field from an IVOA identifier.  An IVOA ID is
     * like "ivo://sdss/dr5/spec".  The authority field is the first "/"
     * delimited field after the "ivo://".  If desired, the "ivo://" can
     * be omitted.
     */
    private String getAuth(String ivoaId) {
	int index=0, lastIndex = 0;

	if (ivoaId.regionMatches(true, 0, "ivo://", 0, 6))
	    index = 6;
	lastIndex = ivoaId.indexOf("/", index);
	if (lastIndex < 0)
	    return (null);

	return (ivoaId.substring(index, lastIndex));
    }


    // -------- Testing -----------

    public static void main(String[] args)
	throws DalServerException, IOException, FileNotFoundException {

	if (args.length > 0 && args[0].equals("encode")) {
	    // Test parameter encoding.
	    JhuSsapService service = new JhuSsapService(null, null);

	    service.baseUrl = "http://baseUrl?";
	    service.urlParams = new LinkedHashMap<String,String>();

	    String param = (args.length >= 1) ? args[1] : "PubDID";
	    String ivoid = (args.length >= 2) ? args[2] : "ivo://foo#bar";
	    service.urlParams.put(param, ivoid);

	    String query = service.getQueryUrl();
	    System.out.println("query= " + query);

	} else if (args.length > 0 && args[0].equals("get")) {
	    // Test the getData operation.
	    JhuSsapService service = new JhuSsapService(null, null);
	    RequestResponse response = new RequestResponse();

	    // Formulate a getData request.
	    SsapParamSet params = new SsapParamSet();
	    String format = (args.length > 1) ? args[1] : "votable";

	    params.setValue("VERSION", "1.0");
	    params.setValue("REQUEST", "getData");
	    params.setValue("FORMAT", format);
	    params.setValue("PubDID", "ivo://jhu/sdss/dr6/spec/2.5#368115424144916480");
	    // params.setValue("PubDID", "ivo://jhu/2df#468669");

	    // Perform the query.
	    InputStream inStream = service.getData(params, response);

	    // Write the output to a file.

	    if (format.equals("csv")) {
		OutputStream out = new FileOutputStream("_dataset.csv");
		response.writeCsv(out);
		out.close();
	    } else if (format.equals("text")) {
		OutputStream out = new FileOutputStream("_dataset.txt");
		response.writeText(out);
	    } else if (format.equals("votable")) {
		OutputStream out = new FileOutputStream("_dataset.vot");
		response.writeVOTable(out);
		out.close();
	    } else
		System.out.println("can only write csv, text, and votable");

	} else {
	    // Exercise the JhuSsapService class.
	    JhuSsapService service = new JhuSsapService(null, null);

	    // Simulate a typical query.
	    SsapParamSet params = new SsapParamSet();

	    Param p = new Param("baseUrl", "http://localhost:8080/ivoa-dal/");
	    params.addParam(p);
	    Param q = new Param("serviceName", "JhuProxySsap");
	    params.addParam(q);

	    params.setValue("VERSION", "1.0");
	    params.setValue("REQUEST", "queryData");
	    params.setValue("FORMAT", "csv");
	    params.setValue("POS", "180.0,1.0;ICRS");
	    params.setValue("SIZE", "0.3333");	// 20 arcmin
	    params.setValue("Collection", "ivo://jhu/sdss/dr6/spec/2.5");
	    params.setValue("TOP", "20");

	    // Create an initial, empty request response object.
	    RequestResponse r = new RequestResponse();

	    // Set the XML namespace for SSAP metadata.
	    SsapKeywordFactory ssap = new SsapKeywordFactory();
	    TableParam xmlnsPar = ssap.newParam("XmlnsSsap", null);
	    r.setXmlns(xmlnsPar.getUtype(), xmlnsPar.getValue());

	    // Perform the query (this is a real query!).
	    service.queryData(params, r);

	    // Write out the VOTable to a file.
	    OutputStream out = new FileOutputStream("_output.vot");
	    r.writeVOTable(out);
	    out.close();
	}
    }
}
