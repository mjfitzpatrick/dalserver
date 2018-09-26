/*
 * JhuProxyService.java
 * $ID*
 */

package dataServices;

import java.io.*;
import java.util.*;
import java.net.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import dalserver.*;
import dalserver.ssa.*;

/**
 * JhuProxyService implements a demonstration SSAP service acting
 * as a proxy to the JHU spectrum services, providing a reference SSAP
 * implementation intended for software development purposes, e.g., to
 * test client applications.
 *
 * [NOTE: This is the older Web service version and is not working as
 * of Aug08 as it the service interface it uses is no longer online
 * at JHU.  We are keeping it around in case it becomes worthwhile to
 * update it.  See JhuSsapService for the new version.]
 *
 * <p>This version implements a proxy service for the real JHU services,
 * and is intended only for demonstration and software development purposes.
 * Since it is a proxy service it can run anywhere on the Internet without
 * requiring local access to data, hence it is convenient for demonstration
 * purposes or to provide a reference SSAP implementation for software
 * development.  However, only the actual data provider can ensure that the
 * data is being represented correctly and that all the software involved is
 * kept up to date.  As a proxy service, this service will break whenever
 * the underlying service interface is changed, and i/o performance will be
 * half that of a native service which has direct access to the data.
 * Hence we do not recommend that this implementation be used for serious
 * data analysis; please use it only for software development purposes.
 *
 * <p>This demonstration class may also be used as a working example of
 * how to use the DALServer package to build a SSAP service.  In the case
 * here we issue a query to a remote service and use the query result to
 * build the SSAP query response.  In a more typical data service, what
 * we would do is very similar: issue a query to a local database, and
 * convert the metadata returned into the SSAP query response.  In effect,
 * the remote service replaces the local DBMS query used in a more typical
 * service.
 */
public class JhuProxyService extends SsapService {
    /** The default maximum size for a data array in a spectrum. */
    private final int MAXPTS = 4096;

    /** Params used to build up the GET query string. */
    private LinkedHashMap<String,String> urlParams;

    /** Keyword table for a spectrum service query response. */
    private ArrayList<LinkedHashMap<String,TableParam>> jhuQR;

    /** Base URLs for the version of the JHU Spectrum Services used here. */
    private static final String queryUrlCone = "http://voservices.net" +
	"/spectrum/ws_v2_5/search.asmx/FindSpectraCone?";
    private static final String queryUrlAdvanced = "http://voservices.net" +
	"/spectrum/ws_v2_5/search.asmx/FindSpectraAdvanced?";
    private static final String getSpectrumUrl = "http://voservices.net" +
	"/spectrum/ws_v2_5/search.asmx/GetSpectrum?";
    private static final String getSpectrumGraph = "http://voservices.net" +
	"/spectrum/graph.aspx?";

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
    public JhuProxyService(SsapParamSet params, TaskManager taskman) {
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

	// Form the query.  It appears there is no GET interface for an
	// advanced search, so for now we just do a cone search and ignore
	// all the advanced parameters.

	this.buildQueryCone(params);

	// Execute the query, read the query response (which comes back as
	// structured XML in this case) and use a SAX XML parser to convert
	// it into a simple keyword table indexed by UTYPE to permit simple
	// keyword-based data model lookups.

	this.executeQuery();

	// Build the SSAP request response from the metadata provided by
	// the native service.
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
	this.baseUrl = getSpectrumUrl;
	urlParams.put("userGuid", "");
	urlParams.put("returnPoints", "true");
	urlParams.put("id", pubDid);

	InputStream in = null;
	String contentType = null;
	String contentLength = null;

	if (format != null && format.equalsIgnoreCase("native")) {
	    // Pass-through native format data unchanged.
	    contentType = "text/xml";

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
	    contentType = "text/xml;content=votable";

	    // Execute the query formed above.
	    this.executeQuery();

	    // Read the native format data and transform to a Spectrum.
	    this.transformData(params, response);

	} else if (format != null && format.equalsIgnoreCase("csv")) {
	    // Return SSAP compliant data in CSV format.
	    contentType = "text/csv";

	    // Execute the query formed above.
	    this.executeQuery();

	    // Read the native format data and transform to a Spectrum.
	    this.transformData(params, response);

	} else if (format != null && format.equalsIgnoreCase("gif")) {
	    // The spectrum graphics format is GIF.
	    contentType = "image/gif";

	    urlParams = new LinkedHashMap<String,String>();
	    this.baseUrl = getSpectrumGraph;
	    urlParams.put("SpectrumID", pubDid);

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
 * Build an "advanced" spectrum services query.  This is the lower level,
 * native query which is sent to the remote service at JHU.
 *
 * @param	params	The processed SSAP request parameter set.
 */
private void buildQueryCone(SsapParamSet params)
    throws DalServerException {

    // Set the base Query URL to be used.
    this.baseUrl = queryUrlCone;

    urlParams = new LinkedHashMap<String,String>();
    Param p = null;
    String v = null;

	// Dummy user GUID is required.
	urlParams.put("userGuid", "");
	
	// We want only the metadata here, not the spectrum data points.
	urlParams.put("returnPoints", "false");

	// If no collection is specifed default to SDSS DR5.
	if (params.getValue("collection") == null)
	    urlParams.put("collectionId", "ivo://jhu/sdss/dr5");
	else {
	    p = params.getParam("collection");
	    RangeList rangeList = p.rangeListValue();
	    for (Range r : rangeList)
		urlParams.put("collectionId", r.stringValue1());
	}

	// SPATIAL Coverage.  If SIZE is omitted, find anything which includes
	// the specified position, otherwise find anything which overlaps.

	if ((p = params.getParam("POS")) != null && p.isSet()) {
	    RangeList r = p.rangeListValue();
	    double ra = r.doubleValue(0);
	    double dec = r.doubleValue(1);

	    double diam = 0.1;
	    if ((p = params.getParam("SIZE")) != null)
		diam = p.doubleValue();

	    urlParams.put("ra", new Double(ra).toString());
	    urlParams.put("dec", new Double(dec).toString());
	    urlParams.put("sr", new Double(diam / 2.0 * 60.0).toString());
	}

    }

    /**
     * Build an "advanced" spectrum services query.  This is the lower level,
     * native query which is sent to the remote service at JHU.
     *
     * @param	params	The processed SSAP request parameter set.
     */
    private void buildQueryAdvanced(SsapParamSet params)
	throws DalServerException {

	// Set the base Query URL to be used.
	this.baseUrl = queryUrlAdvanced;

	urlParams = new LinkedHashMap<String,String>();
	Param p = null;
	String v = null;

	// We want only the metadata here, not the spectrum data points.
	urlParams.put("returnPoints", "false");

	// If no collection is specifed default to SDSS DR5.
	if (params.getValue("collection") == null)
	    urlParams.put("collectionId", "ivo://jhu/sdss/dr5");
	else {
	    p = params.getParam("collection");
	    RangeList rangeList = p.rangeListValue();
	    for (Range r : rangeList)
		urlParams.put("collectionId", r.stringValue1());
	}

	// TargetName.
	if ((v = params.getValue("TargetName")) != null)
	    urlParams.put("name", v);

	// TargetClass.
	if ((v = params.getValue("TargetClass")) != null)
	    urlParams.put("TargetClass", v);

	// TIME Coverage.  If only a single value is specified, find anything
	// which // includes the specified time value.

	if ((p = params.getParam("TIME")) != null && p.isSet()) {
	    Range r = p.rangeListValue().getRange(0);
	    urlParams.put("DateFrom", r.stringValue1());
	    urlParams.put("DateTo", (v = r.stringValue2()) != null 
		? v : r.stringValue1());
	}

	// SPATIAL Coverage.  If SIZE is omitted, find anything which includes
	// the specified position, otherwise find anything which overlaps.

	if ((p = params.getParam("POS")) != null && p.isSet()) {
	    RangeList r = p.rangeListValue();
	    double ra = r.doubleValue(0);
	    double dec = r.doubleValue(1);

	    double ra_size=0.0, dec_size=0.0;
	    if ((p = params.getParam("SIZE")) != null)
		ra_size = dec_size = p.doubleValue();

	    urlParams.put("RaFrom", new Double(ra - ra_size/2.0).toString());
	    urlParams.put("RaTo", new Double(ra + ra_size/2.0).toString());
	    urlParams.put("DecFrom", new Double(dec - dec_size/2.0).toString());
	    urlParams.put("DecTo", new Double(dec + dec_size/2.0).toString());
	}

	// SPECTRAL Coverage.
	if ((p = params.getParam("BAND")) != null && p.isSet()) {
	    RangeList rangeList = p.rangeListValue();
	    Range r = rangeList.getRange(0);

	    urlParams.put("WavelengthMinFrom", v = r.stringValue1());
	    urlParams.put("WavelengthMinTo", v);
	    urlParams.put("WavelengthMaxFrom",
		v = ((v = r.stringValue2()) != null ? v : r.stringValue1()));
	    urlParams.put("WavelengthMaxTo", v);
	}

	// Redshift.
	if ((p = params.getParam("Redshift")) != null && p.isSet()) {
	    RangeList rangeList = p.rangeListValue();
	    Range r = rangeList.getRange(0);

	    switch (r.rangeType) {
	    case ONEVAL:
		urlParams.put("RedshiftFrom", r.stringValue1());
		break;
	    case LOVAL:
		urlParams.put("RedshiftFrom", r.stringValue1());
		break;
	    case HIVAL:
		urlParams.put("RedshiftTo", r.stringValue1());
		break;
	    case CLOSED:
		urlParams.put("RedshiftFrom", r.stringValue1());
		urlParams.put("RedshiftTo", r.stringValue2());
		break;
	    }
	}

	// SNR.
	if ((p = params.getParam("SNR")) != null && p.isSet()) {
	    RangeList rangeList = p.rangeListValue();
	    Range r = rangeList.getRange(0);

	    switch (r.rangeType) {
	    case ONEVAL:
		urlParams.put("SnrFrom", r.stringValue1());
		break;
	    case LOVAL:
		urlParams.put("SnrFrom", r.stringValue1());
		break;
	    case HIVAL:
		urlParams.put("SnrTo", r.stringValue1());
		break;
	    case CLOSED:
		urlParams.put("SnrFrom", r.stringValue1());
		urlParams.put("SnrTo", r.stringValue2());
		break;
	    }
	}

	// VarAmpl.
	if ((p = params.getParam("VarAmpl")) != null && p.isSet()) {
	    RangeList rangeList = p.rangeListValue();
	    Range r = rangeList.getRange(0);

	    switch (r.rangeType) {
	    case ONEVAL:
		urlParams.put("VarAmplFrom", r.stringValue1());
		break;
	    case LOVAL:
		urlParams.put("VarAmplFrom", r.stringValue1());
		break;
	    case HIVAL:
		urlParams.put("VarAmplTo", r.stringValue1());
		break;
	    case CLOSED:
		urlParams.put("VarAmplFrom", r.stringValue1());
		urlParams.put("VarAmplTo", r.stringValue2());
		break;
	    }
	}

	// FluxCalib.
	if ((p = params.getParam("FluxCalib")) != null && p.isSet()) {
	    urlParams.put("FluxCalib", p.stringValue());
	}
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
     * Execute a previously prepared spectrum services query.  The query
     * response, an XML block, is parsed on the fly and returned as a
     * hash table of UTYPE-value pairs.
     */
    private void executeQuery() throws DalServerException {
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
	jhuQR = new ArrayList<LinkedHashMap<String,TableParam>>();

	// Invoke the URL and parse the results.
	String query = this.getQueryUrl();
	try {
	    parser.parse(query, handler);
	} catch (SAXException ex) {
	    throw new DalServerException(
		"SAX exception during query execution");
	} catch (IOException ex) {
	    throw new DalServerException(
		"IO exception during query execution");
	}
    }


    /**
     * Crude, JHU serialization-specific SAX handler for parsing a spectrum
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
	private int nSed = 0;

	// Called when starting a new element.
	public void startElement(String uri, String localname, String qname,
	    Attributes attributes) {

	    // Record a nested element.
	    if (qname.equals("ArrayOfSed")) {
		// Ignore.

	    } else if (qname.equals("Sed")) {
		// Start a new Sed element.
		nSed++;
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
		jhuQR.add(this.segment);
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

		    if (atName.equalsIgnoreCase("ucd"))
			p.setUcd(atValue);
		    else if (atName.equalsIgnoreCase("unit"))
			p.setUnit(atValue);
		    else if (atName.equalsIgnoreCase("value"))
			p.setValue(atValue);
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
     * Compute the SSAP queryData response.  To do this we convert the
     * native metadata from the underlying query (to the JHU spectrum
     * services in the case here) into the standard query reponse metadata
     * required for the Ssap.QueryData operation.
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
	boolean retCsv=true, retVotable=true, retNative=true, retGif=true;
	int nFormats = 4;

	if (formats != null) {
	    formats = formats.toLowerCase();
	    retCsv = retVotable = retNative = retGif = false;
	    nFormats = 0;

	    if (formats.equals("all")) {
		retCsv = retVotable = retNative = retGif = true;
		nFormats = 4;
	    } else {
		if (formats.contains("csv")) {
		    retCsv = true;
		    nFormats++;
		}
		if (formats.contains("votable")) {
		    retVotable = true;
		    nFormats++;
		}
		if (formats.contains("native")) {
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

        // If the query response is paged, set TOKEN here.
	// r.addParam(ssap.newParam("Token", "UNSET"));

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
	r.addParam(ssap.newParam("DatasetType", "Spectrum"));
	r.addField(ssap.newField("DataLength"));
	// r.addField(ssap.newField("TimeSI"));
	// r.addField(ssap.newField("SpectralSI"));
	// r.addField(ssap.newField("FluxSI"));

	// Dataset Identification Metadata
	r.addGroup(ssap.newGroup("DataID"));
	r.addField(ssap.newField("Title"));
	r.addField(ssap.newField("Creator"));
	r.addField(ssap.newField("Collection"));
	r.addField(ssap.newField("CreatorDID"));
	r.addField(ssap.newField("CreatorDate"));
	r.addField(ssap.newField("CreatorVersion"));
	// r.addField(ssap.newField("DatasetID"));
	r.addField(ssap.newField("Instrument"));
	// r.addField(ssap.newField("Bandpass"));
	r.addParam(ssap.newParam("DataSource", "survey"));
	r.addParam(ssap.newParam("CreationType", "archival"));

	// Curation Metadata
	r.addGroup(ssap.newGroup("Curation"));
	r.addParam(ssap.newParam("Publisher", "DALServer Proxy"));
	r.addField(ssap.newField("PublisherDID"));
	// r.addField(ssap.newField("PublisherDate"));
	// r.addField(ssap.newField("PublisherVersion"));
	r.addParam(ssap.newParam("Rights", "public"));
	// r.addField(ssap.newField("Reference"));

	// Target Metadata
	r.addGroup(ssap.newGroup("Target"));
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
	// r.addField(ssap.newField("SpatialFill"));
	// r.addField(ssap.newField("SpatialStatError"));
	// r.addField(ssap.newField("SpatialSysError"));
	// r.addField(ssap.newField("SpatialCalibration"));
	// r.addField(ssap.newField("SpatialResolution"));

	// Spectral Axis Characterization
	r.addGroup(ssap.newGroup("Char.SpectralAxis"));
	r.addField(ssap.newField("SpectralAxisUcd"));
	r.addField(ssap.newField("SpectralLocation"));
	r.addField(ssap.newField("SpectralExtent"));
	r.addField(ssap.newField("SpectralStart"));
	r.addField(ssap.newField("SpectralStop"));
	// r.addField(ssap.newField("SpectralFill"));
	r.addField(ssap.newField("SpectralBinSize"));
	r.addField(ssap.newField("SpectralStatError"));
	// r.addField(ssap.newField("SpectralSysError"));
	r.addField(ssap.newField("SpectralCalibration"));
	r.addField(ssap.newField("SpectralResolution"));
	// r.addField(ssap.newField("SpectralResPower"));

	// Time Axis Characterization
	r.addGroup(ssap.newGroup("Char.TimeAxis"));
	r.addField(ssap.newField("TimeLocation"));
	r.addField(ssap.newField("TimeExtent"));
	r.addField(ssap.newField("TimeStart"));
	r.addField(ssap.newField("TimeStop"));
	// r.addField(ssap.newField("TimeFill"));
	// r.addField(ssap.newField("TimeBinSize"));
	// r.addField(ssap.newField("TimeStatError"));
	// r.addField(ssap.newField("TimeSysError"));
	// r.addField(ssap.newField("TimeCalibration"));
	// r.addField(ssap.newField("TimeResolution"));

	// Flux Axis Characterization
	r.addGroup(ssap.newGroup("Char.FluxAxis"));
	r.addField(ssap.newField("FluxAxisUcd"));
	r.addField(ssap.newField("FluxStatError"));
	// r.addField(ssap.newField("FluxSysError"));
	r.addField(ssap.newField("FluxCalibration"));

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
	    for (LinkedHashMap<String,TableParam> s : jhuQR) {
		// Here, TOP refers to objects, not file format options.
		if (top > 0 && r.size() >= top)
		    break;

		if (nFormats > 1)
		    assocId = assocType + "." + new Integer(nAssoc++).toString();

		if (retCsv) {
		    r.addRow();
		    if (nFormats > 1)
			r.setValue("AssocID", assocId);
		    this.setMetadata(params, r, s, "csv");
		}
		if (retVotable) {
		    r.addRow();
		    if (nFormats > 1)
			r.setValue("AssocID", assocId);
		    this.setMetadata(params, r, s, "votable");
		}
		if (retNative) {
		    r.addRow();
		    if (nFormats > 1)
			r.setValue("AssocID", assocId);
		    this.setMetadata(params, r, s, "native");
		}
		if (retGif) {
		    r.addRow();
		    if (nFormats > 1)
			r.setValue("AssocID", assocId);
		    this.setMetadata(params, r, s, "gif");
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
	    r.score((ParamSet)params, "Score");
	    
	    // Sort the result set by SCORE.
	    r.sort("Score", -1);
	}
    }

    /**
     * Set the content of one query response record.
     */
    private void setMetadata(SsapParamSet params, RequestResponse r,
	LinkedHashMap<String,TableParam> s, String format)
	throws DalServerException {

	// Query metadata.
	// (Nothing to do here, as SCORE is computed later).

	// Access metadata.  Output one record for each output format
	// available.

	String datasetId = this.getValue(s, "DataId.DatasetId");
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
	r.setValue("DatasetSize", "800000");

	// Supported formats are CSV, VOTable and native format.
	if (format.equals("csv")) {
	    r.setValue("Format", "text/csv");
	    r.setValue("DataModel", "Spectrum 1.0");
	} else if (format.equals("votable")) {
	    r.setValue("Format", "application/x-votable+xml");
	    r.setValue("DataModel", "Spectrum 1.0");
	} else if (format.equals("native")) {
	    r.setValue("Format", "text/xml");
	    r.setValue("DataModel", "JhuSS Spectrum " +
		this.getValue(s, "DataModel"));
	} else if (format.equals("gif")) {
	    r.setValue("Format", "image/gif");
	    r.setValue("DataModel", "None");
	}

	// General dataset metadata.  The dataset type, data model
	// version, etc. are set as PARAMs above.

	// If the exact spectrum length is not known it is ok to
	// approximate it for the purposes of characterization.
	//
	String collection = this.getValue(s, "DataId.Collection");
	if (collection.startsWith("ivo://sdss"))
	    r.setValue("DataLength", 4000);

	// Dataset identification metadata.
	r.setValue("Title",
	    this.getValue(s, "Target.Name") + " " +
	    this.getValue(s, "Target.Class") + " " +
	    this.getValue(s, "Target.Description"));
	r.setValue("Creator", this.getAuth(this.getValue(s, "DataId.CreatorId")));
	r.setValue("Collection", this.getValue(s, "DataId.Collection"));
	r.setValue("CreatorDID", this.getValue(s, "DataId.CreatorId"));
	r.setValue("CreatorDate", this.getValue(s, "DataId.Date"));
	r.setValue("CreatorVersion", this.getValue(s, "DataId.Version"));
	// r.setValue("DatasetID", "UNSET");
	r.setValue("Instrument", this.getValue(s, "DataId.Instrument"));
	r.setValue("DataSource", "survey");
	r.setValue("CreationType", this.getValue(s, "DataId.CreationType"));

	// Dataset curation metadata.
	r.setValue("PublisherDID", this.getValue(s, "DataId.DatasetId"));

	// Astronomical target metadata.
	r.setValue("TargetName", this.getValue(s, "Target.Name"));
	r.setValue("TargetClass", this.getValue(s, "Target.Class"));
	r.setValue("Redshift", this.getValue(s, "Redshift.Value"));
	r.setValue("VarAmpl", this.getValue(s, "Target.VarAmpl"));

	// Derived Metadata
	// (none yet)

	// Coordinate system metadata.
	r.setValue("SpaceFrameName", this.getValue(s, "Frame.Sky.Type"));
	r.setValue("SpaceFrameEquinox", this.getValue(s, "Frame.Sky.Equinox"));
	r.setValue("TimeFrameName", this.getValue(s, "Frame.Time.System"));

	// Spatial Axis Characterization.
	r.setValue("SpatialLocation", this.getValue(s, "Coverage.Location.Sky"));
	r.setValue("SpatialExtent", this.getValue(s, "Coverage.Extent.Sky"));

	// Spectral Axis Characterization.
	String s1 = this.getValue(s, "Coverage.Region.Spectral.Min");
	String s2 = this.getValue(s, "Coverage.Region.Spectral.Max");
	double d1 = new Double(s1).doubleValue();
	double d2 = new Double(s2).doubleValue();

	r.setValue("SpectralAxisUcd", this.getUcd(s, "Points.SpectralCoord.Value"));
	r.setValue("SpectralLocation", new Double((d1 + d2) / 2.0).toString());
	r.setValue("SpectralExtent", this.getValue(s, "Coverage.Extent.Spectral"));
	r.setValue("SpectralStart", s1);
	r.setValue("SpectralStop", s2);
	// r.setValue("SpectralFill", "");
	r.setValue("SpectralBinSize", this.getValue(s, "Points.SpectralCoord.Accuracy.BinSize"));
	r.setValue("SpectralStatError", this.getValue(s, "Points.Flux.Accuracy.StatErrHigh"));
	// r.setValue("SpectralSysError", "");
	r.setValue("SpectralCalibration", this.getValue(s, "Points.Flux.Accuracy.Calibration"));
	r.setValue("SpectralResolution", this.getValue(s, "Points.SpectralCoord.Accuracy.Resolution"));
	// r.setValue("SpectralResPower", "");

	// Time Axis Characterization.
	DateParser dp = new DateParser();
	String time;

	try {
	    time = this.getValue(s, "Coverage.Location.Time");
	    if (time != null)
		r.setValue("TimeLocation", dp.getMJD(dp.parse(time)));
	    r.setValue("TimeExtent", this.getValue(s, "Coverage.Extent.Time"));
	    time = this.getValue(s, "Coverage.Region.Time.Start");
	    if (time != null)
		r.setValue("TimeStart", dp.getMJD(dp.parse(time)));
	    time = this.getValue(s, "Coverage.Region.Time.Stop");
	    if (time != null)
		r.setValue("TimeStop", dp.getMJD(dp.parse(time)));
	} catch (InvalidDateException ex) {
	    throw new DalServerException(ex.getMessage());
	}

	// r.setValue("TimeFill", "");
	// r.setValue("SpectralFill", "");

	// Flux Characterization.
	r.setValue("FluxAxisUcd", this.getUcd(s, "Points.Flux.Value"));
	r.setValue("FluxStatError", this.getValue(s, "Points.Flux.Accuracy.StatErrHigh"));
	r.setValue("FluxCalibration", this.getValue(s, "Points.Flux.Accuracy.Calibration"));
    }


    /**
     * Transform a native JhuSS spectrum dataset into a Spectrum object,
     * conformant to the Spectrum data model.
     */
    @SuppressWarnings("unchecked")
    private void transformData(SsapParamSet params, RequestResponse response)
	throws DalServerException {

	SpectrumKeywordFactory sdm = new SpectrumKeywordFactory(response, "1.1");
	LinkedHashMap<String,TableParam> s = jhuQR.get(0);
        RequestResponse r = response;
        String id, key;

        // Set global metadata.
        r.setDescription("Spectrum dataset generated by DALServer");
        r.setUtype("Spectrum");


	// Define the table data.
	// --------------------------------

	// General Dataset Metadata
	r.addGroup(sdm.newGroup("Spectrum"));
	r.addParam(sdm.newParam("DataModel", "Spectrum 1.0"));
	r.addParam(sdm.newParam("DatasetType", "Spectrum"));
	r.addParam(sdm.newParam("DataLength", "UNSET"));
	// r.addParam(sdm.newParam("TimeSI", "UNSET"));
	// r.addParam(sdm.newParam("SpectralSI", "UNSET"));
	// r.addParam(sdm.newParam("FluxSI", "UNSET"));

	// Dataset Identification Metadata
	r.addGroup(sdm.newGroup("DataID"));
	r.addParam(sdm.newParam("Title",
	    this.getValue(s, "Target.Name") + " " +
	    this.getValue(s, "Target.Class") + " " +
	    this.getValue(s, "Target.Description")));
	r.addParam(sdm.newParam("Creator", this.getAuth(this.getValue(s, "DataId.CreatorId"))));
	r.addParam(sdm.newParam("Collection", this.getValue(s, "DataId.Collection")));
	// r.addParam(sdm.newParam("DatasetID", "UNSET"));
	r.addParam(sdm.newParam("CreatorDID", this.getValue(s, "DataId.CreatorId")));
	r.addParam(sdm.newParam("CreatorDate", this.getValue(s, "DataId.Date")));
	r.addParam(sdm.newParam("CreatorVersion", this.getValue(s, "DataId.Version")));
	r.addParam(sdm.newParam("Instrument", this.getValue(s, "DataId.Instrument")));
	// r.addParam(sdm.newParam("Bandpass", "UNSET"));
	r.addParam(sdm.newParam("DataSource", "survey"));
	r.addParam(sdm.newParam("CreationType", this.getValue(s, "DataId.CreationType")));
	// r.addParam(sdm.newParam("CreatorLogo", "UNSET"));
	// r.addParam(sdm.newParam("Contributor", "UNSET"));

	// Curation Metadata
	r.addGroup(sdm.newGroup("Curation"));
	r.addParam(sdm.newParam("Publisher", "DALServer Proxy"));
	// r.addParam(sdm.newParam("PublisherID", "UNSET"));
	r.addParam(sdm.newParam("PublisherDID", this.getValue(s, "DataId.DatasetId")));
	// r.addParam(sdm.newParam("PublisherDate", "UNSET"));
	// r.addParam(sdm.newParam("PublisherVersion", "UNSET"));
	r.addParam(sdm.newParam("Rights", "public"));
	// r.addParam(sdm.newParam("Reference", "UNSET"));
	// r.addParam(sdm.newParam("ContactName", "UNSET"));
	// r.addParam(sdm.newParam("ContactEmail", "UNSET"));

	// Target Metadata
	r.addGroup(sdm.newGroup("Target"));
	r.addParam(sdm.newParam("TargetName", this.getValue(s, "Target.Name")));
	r.addParam(sdm.newParam("TargetDescription", this.getValue(s, "Target.Description")));
	r.addParam(sdm.newParam("TargetClass", this.getValue(s, "Target.Class")));
	r.addParam(sdm.newParam("TargetPos", this.getValue(s, "Target.Pos")));
	r.addParam(sdm.newParam("SpectralClass", this.getValue(s, "Target.SpectralClass")));
	r.addParam(sdm.newParam("Redshift", this.getValue(s, "Target.Redshift.Value")));
	r.addParam(sdm.newParam("VarAmpl", this.getValue(s, "Target.VarAmpl")));

	// Derived Metadata
	r.addGroup(sdm.newGroup("Derived"));
	// r.addParam(sdm.newParam("DerivedSNR", "UNSET"));
	r.addParam(sdm.newParam("DerivedRedshift", this.getValue(s, "Target.Redshift.Value")));
	r.addParam(sdm.newParam("RedshiftStatError",
	    this.getValue(s, "Target.Redshift.Accuracy.StatErrorLow")));
	r.addParam(sdm.newParam("RedshiftConfidence",
	    this.getValue(s, "Target.Redshift.Accuracy.Confidence")));
	r.addParam(sdm.newParam("DerivedVarAmpl", this.getValue(s, "Target.VarAmpl")));

	// Coordinate System Metadata
	r.addGroup(sdm.newGroup("CoordSys"));
	// r.addParam(sdm.newParam("CoordSysID", "UNSET"));
	r.addParam(sdm.newParam("SpaceFrameName", this.getValue(s, "Frame.Sky.Type")));
	r.addParam(sdm.newParam("SpaceFrameUcd",  this.getUcd(s, "Coverage.Location.Sky")));
	// r.addParam(sdm.newParam("SpaceFrameRefPos", "UNSET"));
	r.addParam(sdm.newParam("SpaceFrameEquinox", this.getValue(s, "Frame.Sky.Equinox")));

	r.addParam(sdm.newParam("TimeFrameName", this.getValue(s, "Frame.Time.System")));
	// r.addParam(sdm.newParam("TimeFrameUcd", "UNSET"));
	r.addParam(sdm.newParam("TimeFrameZero", this.getValue(s, "Frame.Time.Zero")));
	r.addParam(sdm.newParam("TimeFrameRefPos", this.getValue(s, "Frame.Time.RefPos")));

	// r.addParam(sdm.newParam("SpectralFrameName", "UNSET"));
	r.addParam(sdm.newParam("SpectralFrameUcd", this.getUcd(s, "Points.SpectralCoord.Value")));
	r.addParam(sdm.newParam("SpectralFrameRefPos", this.getValue(s, "Frame.Spectral.RefPos")));
	// r.addParam(sdm.newParam("SpectralFrameRedshift", "UNSET"));
	// r.addParam(sdm.newParam("RedshiftFrameName", "UNSET"));
	// r.addParam(sdm.newParam("DopplerDefinition", "UNSET"));
	// r.addParam(sdm.newParam("RedshiftFrameRefPos", "UNSET"));

	// Spatial Axis Characterization
	r.addGroup(sdm.newGroup("Char.SpatialAxis"));
	r.addParam(sdm.newParam("SpatialAxisName", "Sky"));
	r.addParam(sdm.newParam("SpatialAxisUcd", this.getUcd(s, "Coverage.Location.Sky")));
	r.addParam(sdm.newParam("SpatialAxisUnit", this.getUnit(s, "Coverage.Location.Sky")));
	r.addParam(sdm.newParam("SpatialLocation", this.getValue(s, "Coverage.Location.Sky")));
	r.addParam(sdm.newParam("SpatialExtent", this.getValue(s, "Coverage.Extent.Sky")));
	// r.addParam(sdm.newParam("SpatialArea", "UNSET"));
	// r.addParam(sdm.newParam("SpatialFill", "UNSET"));
	// r.addParam(sdm.newParam("SpatialStatError", "UNSET"));
	// r.addParam(sdm.newParam("SpatialSysError", "UNSET"));
	r.addParam(sdm.newParam("SpatialCalibration", "calibrated"));
	// r.addParam(sdm.newParam("SpatialResolution", "UNSET"));

	// Spectral Axis Characterization
	r.addGroup(sdm.newGroup("Char.SpectralAxis"));
	r.addParam(sdm.newParam("SpectralAxisName", "SpectralCoord"));
	r.addParam(sdm.newParam("SpectralAxisUcd", this.getUcd(s, "Points.SpectralCoord.Value")));
	r.addParam(sdm.newParam("SpectralAxisUnit", this.getUnit(s, "Points.SpectralCoord.Value")));
	double w1 = new Double(this.getValue(s, "Coverage.Region.Spectral.Min")).doubleValue();
	double w2 = new Double(this.getValue(s, "Coverage.Region.Spectral.Max")).doubleValue();
	r.addParam(sdm.newParam("SpectralLocation", new Double((w1 + w2) / 2.0).toString()));
	r.addParam(sdm.newParam("SpectralExtent", this.getValue(s, "Coverage.Extent.Spectral")));
	r.addParam(sdm.newParam("SpectralStart", this.getValue(s, "Coverage.Region.Spectral.Min")));
	r.addParam(sdm.newParam("SpectralStop", this.getValue(s, "Coverage.Region.Spectral.Max")));
	// r.addParam(sdm.newParam("SpectralFill", "UNSET"));
	r.addParam(sdm.newParam("SpectralBinSize", this.getValue(s, "Points.SpectralCoord.Accuracy.BinSize")));
	r.addParam(sdm.newParam("SpectralStatError", this.getValue(s, "Points.SpectralCoord.Accuracy.StatErrHigh")));
	r.addParam(sdm.newParam("SpectralSysError", this.getValue(s, "Points.SpectralCoord.Accuracy.SysErr")));
	r.addParam(sdm.newParam("SpectralCalibration", this.getValue(s, "Points.SpectralCoord.Accuracy.Calibration")));
	r.addParam(sdm.newParam("SpectralResolution", this.getValue(s, "Points.SpectralCoord.Accuracy.Resolution")));
	// r.addParam(sdm.newParam("SpectralResPower", "UNSET"));

	// Time Axis Characterization
	r.addGroup(sdm.newGroup("Char.TimeAxis"));
	r.addParam(sdm.newParam("TimeAxisName", "Time"));
	r.addParam(sdm.newParam("TimeAxisUcd", "time"));
	r.addParam(sdm.newParam("TimeAxisUnit", this.getUnit(s, "Coverage.Extent.Time")));

	DateParser dp = new DateParser();
	String time;

	try {
	    time = this.getValue(s, "Coverage.Location.Time");
	    if (time != null)
		r.addParam(sdm.newParam("TimeLocation", dp.getMJD(dp.parse(time))));
	    r.addParam(sdm.newParam("TimeExtent", this.getValue(s, "Coverage.Extent.Time")));
	    time = this.getValue(s, "Coverage.Region.Time.Start");
	    if (time != null)
		r.addParam(sdm.newParam("TimeStart", dp.getMJD(dp.parse(time))));
	    time = this.getValue(s, "Coverage.Region.Time.Stop");
	    if (time != null)
		r.addParam(sdm.newParam("TimeStop", dp.getMJD(dp.parse(time))));
	} catch (InvalidDateException ex) {
	    throw new DalServerException(ex.getMessage());
	}

	// r.addParam(sdm.newParam("TimeFill", "UNSET"));
	// r.addParam(sdm.newParam("TimeBinSize", "UNSET"));
	// r.addParam(sdm.newParam("TimeStatError", "UNSET"));
	// r.addParam(sdm.newParam("TimeSysError", "UNSET"));
	// r.addParam(sdm.newParam("TimeCalibration", "UNSET"));
	// r.addParam(sdm.newParam("TimeResolution", "UNSET"));

	// Flux Axis Characterization
	r.addGroup(sdm.newGroup("Char.FluxAxis"));
	r.addParam(sdm.newParam("FluxAxisName", "Flux"));
	r.addParam(sdm.newParam("FluxAxisUcd", this.getUcd(s, "Points.Flux.Value")));
	r.addParam(sdm.newParam("FluxAxisUnit", this.getUnit(s, "Points.Flux.Value")));
	r.addParam(sdm.newParam("FluxStatError", this.getValue(s, "Points.Flux.Accuracy.StatErrHigh")));
	// r.addParam(sdm.newParam("FluxSysError", "UNSET"));
	r.addParam(sdm.newParam("FluxCalibration", this.getValue(s, "Points.Flux.Accuracy.Calibration")));

	// ------------ DATA Values ------------

	// Spectral Axis Data
	r.addGroup(sdm.newGroup("Data.SpectralAxis"));

	r.addField(sdm.newField("DataSpectralValue"));
	r.addParam(sdm.newParam("DataSpectralUcd", this.getUcd(s, "Points.SpectralCoord.Value")));
	r.addParam(sdm.newParam("DataSpectralUnit", this.getUnit(s, "Points.SpectralCoord.Value")));
	// r.addField(sdm.newField("DataSpectralBinSize"));

	if (s.get("SpectralCoord_Accuracy_BinLow") != null)
	    r.addField(sdm.newField("DataSpectralBinLow"));
	if (s.get("SpectralCoord_Accuracy_BinHigh") != null)
	    r.addField(sdm.newField("DataSpectralBinHigh"));

	// r.addField(sdm.newField("DataSpectralStatError"));
	// r.addField(sdm.newField("DataSpectralStatErrLow"));
	// r.addField(sdm.newField("DataSpectralStatErrHigh"));
	// r.addField(sdm.newField("DataSpectralSysError"));
	// r.addField(sdm.newField("DataSpectralResolution"));

	// Flux Axis Data
	r.addGroup(sdm.newGroup("Data.FluxAxis"));
	r.addField(sdm.newField("DataFluxValue"));
	r.addParam(sdm.newParam("DataFluxUcd", this.getUcd(s, "Points.Flux.Value")));
	r.addParam(sdm.newParam("DataFluxUnit", this.getUnit(s, "Points.Flux.Value")));
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
	TableParam wvP = s.get("SpectralCoord_Value");
	ArrayList<String> waveD = null;
	if (wvP != null)
	    waveD = (ArrayList<String>) wvP.getArrayData();

	TableParam blP = s.get("SpectralCoord_Accuracy_BinLow");
	ArrayList<String> binLowD = null;
	if (blP != null)
	    binLowD = (ArrayList<String>) blP.getArrayData();

	TableParam bhP = s.get("SpectralCoord_Accuracy_BinHigh");
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
	    throw new DalServerException("No spectral data [SpectralCoord_Value]");
	if (fluxD == null)
	    throw new DalServerException("No spectral data [Flux_Value]");

	// Write the output data, which is a table with one data point
	// per row.

	try {
	    for (int i=0;  i < waveD.size();  i++) {
		r.addRow();

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
	} catch (DalOverflowException ex) {
	    ;
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
    private String getValue(LinkedHashMap<String,TableParam> spectrum,
	String key) {

	TableParam p;
	if ((p = spectrum.get(key)) != null)
	    return (p.getValue());
	if ((p = spectrum.get("Segment." + key)) != null)
	    return (p.getValue());
	if ((p = spectrum.get("Target." + key)) != null)
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
	if ((p = spectrum.get("Segment." + key)) != null)
	    return (p.getUcd());
	if ((p = spectrum.get("Target." + key)) != null)
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
	if ((p = spectrum.get("Segment." + key)) != null)
	    return (p.getUnit());
	if ((p = spectrum.get("Target." + key)) != null)
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
	    JhuProxyService service = new JhuProxyService(null, null);

	    service.baseUrl = "http://baseUrl?";
	    service.urlParams = new LinkedHashMap<String,String>();

	    String param = (args.length >= 1) ? args[1] : "PubDID";
	    String ivoid = (args.length >= 2) ? args[2] : "ivo://foo#bar";
	    service.urlParams.put(param, ivoid);

	    String query = service.getQueryUrl();
	    System.out.println("query= " + query);

	} else if (args.length > 0 && args[0].equals("get")) {
	    // Test the getData operation.
	    JhuProxyService service = new JhuProxyService(null, null);
	    RequestResponse response = new RequestResponse();

	    // Formulate a getData request.
	    SsapParamSet params = new SsapParamSet();
	    String format = (args.length > 1) ? args[1] : "votable";

	    params.setValue("VERSION", "1.0");
	    params.setValue("REQUEST", "getData");
	    params.setValue("FORMAT", format);
	    // params.setValue("PubDID", "ivo://jhu/sdss/dr5#80442261455765504");
	    params.setValue("PubDID", "ivo://jhu/2df#468669");

	    // Perform the query.
	    InputStream inStream = service.getData(params, response);

	    // Write the output to a file.

	    if (format.equals("csv")) {
		OutputStream out = new FileOutputStream("_dataset.csv");
		response.writeCsv(out);
		out.close();
	    } else if (format.equals("votable")) {
		OutputStream out = new FileOutputStream("_dataset.vot");
		response.writeVOTable(out);
		out.close();
	    } else
		System.out.println("can only write csv and votable");

	} else {
	    // Exercise the JhuProxyService class.
	    JhuProxyService service = new JhuProxyService(null, null);

	    // Simulate a typical query.
	    SsapParamSet params = new SsapParamSet();
	    params.setValue("VERSION", "1.0");
	    params.setValue("REQUEST", "queryData");
	    params.setValue("FORMAT", "csv");
	    params.setValue("POS", "180.0,1.0;ICRS");
	    params.setValue("SIZE", "0.3333");	// 20 arcmin
	    //params.setValue("Collection", "ivo://jhu/sdss/dr5");
	    params.setValue("Collection", "ivo://jhu/2df");
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
