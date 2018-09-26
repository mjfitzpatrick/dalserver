/*
 * SiapOracle.java
 * $ID*
 */

package dalserver.sia;

import dalserver.DalServerException;
import dalserver.DalOverflowException;
import dalserver.RequestResponse;
import dalserver.ConfigTable;
import dalserver.TableField;
import dalserver.Param;
import dalserver.DateParser;
import dalserver.RangeList;
import dalserver.TableInfo;
import dalserver.sia.SiapParamSet;
import dalserver.sia.SiapKeywordFactory;

import java.io.*;
import java.util.*;
import java.net.*;
import java.sql.*;

/**
 * SiapOracle provides a simple image access (SIAP Version 2) capability
 * for any image collection which can be described via image metadata
 * stored in an Oracle database.
 *
 * @version	2.0, 15-Apr-2014
 * @author	Doug Tody
 *
 * This file is temporary, to determine what needs to be changed to do a
 * SIAPV2 query using Oracle.  Once this is better understood the code 
 * will be refactored to separate the DBMS interface from the SIAPV2
 * logic (JDBC probably already gets us most of the way there).
 *
 * Aside from the Oracle-specific bits however, most of this is generic
 * code, driven off the primary Image table and per-service Table config
 * mechanisms.  This class supports the new ObsTAP-based version of the
 * SIAV2 primary image table.
 */
public class SiapOracle {
    /** Connection to the remote DBMS. */
    private Connection conn;
    private String database;
    ConfigTable configTable;

    /** Constructor to generate a new Oracle query object, providing
     * the functionality to query a remote Oracle-hosted catalog.
     */
    public SiapOracle(String jdbcDriver) throws DalServerException {
	try {
	    Class.forName(jdbcDriver).newInstance();
	    conn = null;
	} catch (Exception ex) {
	    throw new DalServerException(ex.getMessage());
	}
    }

    /**
     * Connect to the remote database.
     *
     * @param	url		JDBC URL of the remote DBMS.
     * @param	database	Database name within remote DBMS.
     * @param	username	User name for database login.
     * @param	password	User password for database login.
     */
    public void connect(String url, String database, String username,
	String password) throws DalServerException {

	// Oracle supports schemas, but only one database per server.
	this.database = database;

	try {
	    conn = DriverManager.getConnection(url, username, password);
	} catch (Exception ex) {
	    conn = null;
	    throw new DalServerException(ex.getMessage());
	}
    }

    /**
     * Disconnect from the remote database.
     */
    public void disconnect() {
	if (conn != null) {
	    try {
		conn.close();
	    } catch (SQLException ex) {
		;
	    }
	}
    }

    /**
     * Add fields to the request response table for the discovery query.
     *
     * @param	params		The SIAP service input parameters.
     * @param	response	The request response object.
     *
     * The metadata returned by a service is dynamically configurable.
     * By default certain common fields of the Image data model are 
     * returned (what is considered "common" here is defined by this
     * implementation).  The specific service configuration may restrict
     * or extend this standard set of metadata, adding additional standard
     * DM fields or custom data provider-defined fields, or omitting common
     * fields that are not used.
     *
     * The function of addFields is to dynamically determine, based upon the
     * service configuration, what standard or custom fields to return in
     * a query response, and define these output fields.  When the query
     * response is later built from the Image table query, it will try to
     * provide values for all the fields defined here.
     */
    public void addFields(SiapParamSet params, RequestResponse response)
        throws DalServerException {

        SiapKeywordFactory siap = new SiapKeywordFactory();
        RequestResponse r = response;

        // By default, if no custom table configuration is provided, the
        // standard metadata defined below is output.  Fields for which no
        // metadata is available will have null values, but this is harmless.
        // In the simplest case the data provider merely provides an Image
        // table instance for the service, and the generic framework does
        // the rest.  Per-service table configuration may be used to further
        // restrict, extend, or customize the standard query response metadata.
        //
        // The fields added below basically correspond to the DALServer-defined
        // Image table, or dynamically derived/generate metadata, and may be
        // optionally omitted or customized if specified in the per-serivce
        // configuration.

        // Get the per-service table configuration, if any.
        this.configTable = new ConfigTable(params, siap);
        ConfigTable c = this.configTable;

        // Query Metadata
        // c.addGroup(r, siap.newGroup("Query"));
        // c.addField(r, siap.newField("query_score"));
        // c.addParam(r, siap.newParam("query_token", "UNSET"));

        // Association Metadata
        c.addGroup(r, siap.newGroup("Association"));
        c.addParam(r, siap.newParam("assoc_type", "MultiFormat"));
        c.addParam(r, siap.newParam("assoc_key", "@Format"));
        c.addField(r, siap.newField("assoc_id"));

	// Access Metadata
	c.addGroup(r, siap.newGroup("Access"));
	c.addField(r, siap.newField("access_url"));
	c.addField(r, siap.newField("access_format"));
	c.addField(r, siap.newField("access_estsize"));

	// General Dataset Metadata
	c.addGroup(r, siap.newGroup("Dataset"));
	c.addParam(r, siap.newParam("datamodel_name", "Image-2.0"));
	c.addParam(r, siap.newParam("datamodel_prefix", "im"));
	c.addField(r, siap.newField("dataproduct_type", "Image"));
	c.addField(r, siap.newField("dataproduct_subtype"));
	c.addField(r, siap.newField("calib_level"));
	c.addField(r, siap.newField("dataset_length"));

	// Image-specific Dataset metadata
	c.addGroup(r, siap.newGroup("Image"));
	c.addField(r, siap.newField("im_nsubarrays"));
	c.addField(r, siap.newField("im_naxes"));
	c.addField(r, siap.newField("im_naxis"));
	c.addField(r, siap.newField("im_pixtype"));
	c.addField(r, siap.newField("im_wcsaxes"));
	c.addField(r, siap.newField("im_scale"));

	// Dataset Identification Metadata
	c.addGroup(r, siap.newGroup("DataID"));
	c.addField(r, siap.newField("obs_title"));
	c.addField(r, siap.newField("obs_id"));
	c.addField(r, siap.newField("obs_creator_name"));
	c.addField(r, siap.newField("obs_collection"));
	c.addField(r, siap.newField("obs_creator_did"));
	c.addField(r, siap.newField("obs_dataset_did"));
	c.addField(r, siap.newField("obs_creation_type"));
	c.addField(r, siap.newField("obs_creation_date"));

	// Provenance Metadata
	c.addGroup(r, siap.newGroup("Provenance"));
	c.addField(r, siap.newField("facility_name"));
	c.addField(r, siap.newField("instrument_name"));
	c.addField(r, siap.newField("obs_bandpass"));
	c.addField(r, siap.newField("obs_datasource"));
	c.addField(r, siap.newField("proposal_id"));

	// Curation Metadata
	c.addGroup(r, siap.newGroup("Curation"));
	c.addField(r, siap.newField("obs_publisher_did"));
	c.addField(r, siap.newField("obs_release_date"));
	c.addField(r, siap.newField("preview"));

	// Target Metadata
	c.addGroup(r, siap.newGroup("Target"));
	c.addField(r, siap.newField("target_name"));
	c.addField(r, siap.newField("target_class"));

	// Derived Metadata
	// c.addGroup(r, siap.newGroup("Derived"));

	// Coordinate System Metadata
	//
	// Most of this can be omitted since the query response
	// fixes or defines defaults for the coordinate frames
	// used in the response.

	// c.addGroup(r, siap.newGroup("CoordSys"));
	// Spatial frame metadata
	// c.addGroup(r, siap.newGroup("CoordSys.SpaceFrame"));
	// Time frame metadata
	// c.addGroup(r, siap.newGroup("CoordSys.TimeFrame"));
	// Spectral frame metadata
	// c.addGroup(r, siap.newGroup("CoordSys.SpectralFrame"));
	// Redshift frame metadata
	// c.addGroup(r, siap.newGroup("CoordSys.RedshiftFrame"));
	// Flux (observable) frame metadata
	// c.addGroup(r, siap.newGroup("CoordSys.FluxFrame"));

	// Spatial Axis Characterization
	c.addGroup(r, siap.newGroup("Char.SpatialAxis"));
	c.addField(r, siap.newField("s_ra"));
	c.addField(r, siap.newField("s_dec"));
	c.addField(r, siap.newField("s_fov"));
	// c.addField(r, siap.newField("s_lo_ra"));
	// c.addField(r, siap.newField("s_lo_dec"));
	// c.addField(r, siap.newField("s_hi_ra"));
	// c.addField(r, siap.newField("s_hi_dec"));
	c.addField(r, siap.newField("s_region"));
	// c.addField(r, siap.newField("s_area"));
	// c.addField(r, siap.newField("s_extent"));
	// c.addField(r, siap.newField("s_fillfactor"));
	// c.addField(r, siap.newField("s_stat_error"));
	// c.addField(r, siap.newField("s_sys_error"));
	c.addField(r, siap.newField("s_calib_status"));
	c.addField(r, siap.newField("s_resolution"));

	// Spectral Axis Characterization
	c.addGroup(r, siap.newGroup("Char.SpectralAxis"));
	// c.addField(r, siap.newField("em_ucd"));
	// c.addField(r, siap.newField("em_unit"));
	// c.addField(r, siap.newField("em_bandpass"));
	// c.addField(r, siap.newField("em_bandwidth"));
	c.addField(r, siap.newField("em_min"));
	c.addField(r, siap.newField("em_max"));
	// c.addField(r, siap.newField("em_fill_factor"));
	// c.addField(r, siap.newField("em_stat_error"));
	c.addField(r, siap.newField("em_resolution"));
	c.addField(r, siap.newField("em_res_power"));

	// Time Axis Characterization
	c.addGroup(r, siap.newGroup("Char.TimeAxis"));
	// c.addField(r, siap.newField("t_ucd"));
	// c.addField(r, siap.newField("t_unit"));
	// c.addField(r, siap.newField("t_midpoint"));
	c.addField(r, siap.newField("t_min"));
	c.addField(r, siap.newField("t_max"));
	c.addField(r, siap.newField("t_exptime"));
	// c.addField(r, siap.newField("t_fill_factor"));
	// c.addField(r, siap.newField("t_stat_error"));
	// c.addField(r, siap.newField("t_calib_status"));
	c.addField(r, siap.newField("t_resolution"));

	// Observable Axis Characterization
	c.addGroup(r, siap.newGroup("Char.ObservableAxis"));
	c.addField(r, siap.newField("o_ucd"));
	c.addField(r, siap.newField("o_unit"));
	// c.addField(r, siap.newField("o_calib_status"));

	// Polarization Axis Characterization
	c.addGroup(r, siap.newGroup("Char.PolAxis"));
	c.addField(r, siap.newField("pol_ucd"));
	c.addField(r, siap.newField("pol_states"));

        // Add any additional config-defined standard metadata.
        c.addStandardFields(r);

        // Add any additional config-defined custom metadata.
        c.addCustomFields(r);
    }

    /**
     * Query the image table for the dataset identified by the given
     * PubDID, and return the named metadata attribute.
     *
     * @param	tableName	The DBMS table to be queried
     * @param	id		The ID of the record to be accessed
     * @param	attribute	The record field to be returned
     */
    public String queryDataset(String tableName, String id, String attribute)
	throws DalServerException {

	// Compose the Oracle query.
	String query = "SELECT " + attribute + " FROM " + tableName +
	    " WHERE (id = " + id + ")";

	// Perform the data query and write rows to the output table.
	try {
	    // Execute the query.
	    Statement st = conn.createStatement();
	    ResultSet rs = st.executeQuery(query);

	    // Walk through the resultset and output each row.
	    if (rs.next())
		return (getColumn(rs, attribute));

	} catch (SQLException ex) {
	    throw new DalServerException(ex.getMessage());
	}

	return (null);
    }

    /**
     * Query the image metadata table, writing results as a SIAP query
     * response to the output request response object.  The response object
     * can later be serialized and returned in various output formats.
     *
     * @param	siap		The SIAP service instance.
     * @param	params		The SIAP service input parameters.
     *
     * @param	response	The request response object.
     */
    public void
    query (SiapService siap, SiapParamSet params, RequestResponse response)
	throws DalServerException, DalOverflowException {

	String tableName="siav2model";		// default
	int maxrec = response.maxrec();
	String sval; Param p;

	// Get the name of the SIAV2 primary image table to be queried.
	sval = params.getValue("tableName");
	if (sval != null)
	    tableName = sval;

        // SPATIAL Coverage.
	// ------------------
	// If SIZE is omitted, find anything which includes
        // the specified position, otherwise find anything which overlaps.

	boolean spatial_constraint = true, allSky = false;
	String s1Column="s_ra", s2Column="s_dec";
	double ra=0, dec=0, ra_size=0.2, dec_size=0.2, ra_sr=0.1, dec_sr=0.1;

        if ((p = params.getParam("POS")) != null && p.isSet()) {
	    // Get POS (e.g., "<val1>,<val2>;<property>").
            RangeList r = p.rangeListValue();
            ra = r.doubleValue(0);
            dec = r.doubleValue(1);

	    // Verify that the coordinates are in a supported frame.
	    // Frames are specified as a property of the range list.

	    for (Iterator i = r.propertiesIterator();  i.hasNext();  ) {
		Map.Entry me = (Map.Entry) i.next();
		String key = (String) me.getKey();

		// Currently only ICRS is supported.
		if (key != null && !key.equalsIgnoreCase("ICRS"))
		    throw new DalServerException("unsupported coordinate frame '" + key + "'");
	    }

	    // GET SIZE (either a single value, or separate values for RA and DEC).
            if ((p = params.getParam("SIZE")) != null && p.isSet()) {
		r = p.rangeListValue();
		ra_size = r.doubleValue(0);
		ra_sr = ra_size / 2.0;
		try {
		    dec_size = r.doubleValue(1);
		    dec_sr = dec_size / 2.0;
		} catch (DalServerException ex) {
		    dec_size = ra_size;
		    dec_sr = ra_sr;
		}
	    }
        } else
	    spatial_constraint = false;

        // Check for SR=180 degrees (entire sky).
	allSky = (Math.abs(ra_sr - 180.0) < 0.000001);

	// If no search region specified the spatial constraint is 
	// disabled.
	if (allSky)
	    spatial_constraint = false;

        // SPECTRAL Coverage.
	// ------------------

	boolean spectral_constraint = true;
	String e1Column="em_min", e2Column="em_max";
	double e1val=0, e2val=0;

        if ((p = params.getParam("BAND")) != null && p.isSet()) {
            RangeList r = p.rangeListValue();
            e1val = r.getRange(0).doubleValue1();
            e2val = r.getRange(0).doubleValue2();
        } else
	    spectral_constraint = false;

        // TIME Coverage.
	// ------------------

	boolean time_constraint = true;
	String t1Column="t_min", t2Column="t_max";
	double t1val=0, t2val=0;

        if ((p = params.getParam("TIME")) != null && p.isSet()) {
            RangeList r = p.rangeListValue();
	    java.util.Date d1val, d2val;
	    DateParser dp = new DateParser();

            d1val = r.getRange(0).dateValue1();
            d2val = r.getRange(0).dateValue2();
	    t1val = dp.getMJD(d1val);
	    t2val = dp.getMJD(d2val);
        } else
	    time_constraint = false;

        // POLARIZATION Coverage.
	// -----------------------

	boolean pol_constraint = true, polAny = false;
	String polColumn="pol_states";
	String pol1=null, pol2=null, pol3=null, pol4=null;

        if ((p = params.getParam("POL")) != null && p.isSet()) {
	    sval = p.stringValue();
	    if (sval != null && sval.equalsIgnoreCase("any")) {
		polAny = true;
	    } else if (sval != null && sval.equalsIgnoreCase("stokes")) {
		pol1 = "I";
		pol2 = "Q";
		pol3 = "U";
		pol4 = "V";
	    } else if (sval != null) {
		RangeList r = p.rangeListValue();
		pol1 = (r.length() > 0) ?  r.stringValue(0) : null;
		pol2 = (r.length() > 1) ?  r.stringValue(1) : null;
		pol3 = (r.length() > 2) ?  r.stringValue(2) : null;
		pol4 = (r.length() > 3) ?  r.stringValue(3) : null;
	    } else
		pol_constraint = false;
        } else
	    pol_constraint = false;

        // OUTPUT FORMATS.  What formats do we return?
	//---------------------
        boolean retFITS=true, retGraphic=true;
        String formats = null;
        int nFormats = 2;

	formats = params.getValue("FORMAT");
	if (formats == null)
	    formats = "fits";

        if (formats != null) {
            formats = formats.toLowerCase();
            retFITS = retGraphic = false;
            nFormats = 0;

            if (formats.equals("all")) {
		retFITS = retGraphic = true;
                nFormats = 2;
            } else {
                if (formats.contains("fits")) {
                    retFITS = true;
                    nFormats++;
                }
                if (formats.contains("graphic")) {
                    retGraphic = true;
                    nFormats++; 
                }
            }
        }

	// Most of the optional physical constraint parameters are not yet
	// implemented, e.g., SPECRES, SPATRES, TIMERES, etc.

	// Most of the simpler optional query parameters are handled directly
	// below as we compose the Oracle query.  

	// Compose the Oracle query.
	//-------------------------------------
	String query = "SELECT * FROM " + tableName + " WHERE ";
	boolean additional_term = false;

	// Apply the spatial constraint if we have one.
	if (spatial_constraint) {
	    if (additional_term)
		query += (" AND ");

	    // This needs to be converted to a 2D radial test for SIAV2.
	    // We use the old CAR/box test for now.  Note this need
	    // not be exact, as more precise refinement is done in the
	    // second pass below, so the initial search region is larger
	    // than the actual ROI.  (this old looks to have a problem at
	    // the 0/360 boundary for RA, but lets' ignore this for the
	    // moment until the code is redone.)
 
	    double ra1 = Math.max(0.0, Math.min(360.0, ra - ra_size));
	    double ra2 = Math.max(0.0, Math.min(360.0, ra + ra_size));
	    double dec1 = Math.max(-90.0, Math.min(90.0, dec - dec_size));
	    double dec2 = Math.max(-90.0, Math.min(90.0, dec + dec_size));

	    // This needs to be generalized to allow for a spatial position
	    // specified as NULL, e.g, for theory data.

	    query += ("(" +
		s1Column + " BETWEEN " + ra1 + " AND " + ra2 + " AND " +
		s2Column + " BETWEEN " + dec1 + " AND " + dec2 + ")");

	    additional_term = true;
	}

	// If we have a BAND term, apply the constraint, or if the metadata
	// is null, ignore the constraint (i.e., constraint term evaluates
	// to TRUE).  [TO DO - add support for multiple ranges]

	if (spectral_constraint) {
	    if (additional_term)
		query += (" AND ");

	    query += ("(" + e1Column + " <= " + e2val + " or " + e1Column + " is null)");
	    query += (" AND ");
	    query += ("(" + e2Column + " >= " + e1val + " or " + e2Column + " is null)");

	    additional_term = true;
	}

	// If we have a TIME term, apply the constraint, or if the metadata
	// is null, ignore the constraint.
	// [TO DO - add support for multiple ranges]

	if (time_constraint) {
	    if (additional_term)
		query += (" AND ");

	    query += ("(" + t1Column + " <= " + t2val + " or " + t1Column + " is null)");
	    query += (" AND ");
	    query += ("(" + t2Column + " >= " + t1val + " or " + t2Column + " is null)");

	    additional_term = true;
	}

	// Apply the POL constraint if given.  POL=any finds any dataset
	// that has enumerated polarizations.  If an individual polarization
	// is specified we search for it explicitly.  Currently the approach
	// used here is limited to single character states, e.g., I,Q,U,V,R,L.
	// This is easy to fix, but would complicate usage.

	if (pol_constraint) {
	    if (additional_term)
		query += (" AND ");

	    if (polAny) {
		query += ("(" + polColumn + " IS NOT NULL)");
		additional_term = true;
	    } else {
		boolean firstone = true;
		query += ("(");
		if (pol1 != null) {
		    if (!firstone)
			query += (" || ");
		    query += ("(" + polColumn + " LIKE '%" + pol1 + "%')");
		    firstone = false;
		}
		if (pol2 != null) {
		    if (!firstone)
			query += (" || ");
		    query += ("(" + polColumn + " LIKE '%" + pol2 + "%')");
		    firstone = false;
		}
		if (pol3 != null) {
		    if (!firstone)
			query += (" || ");
		    query += ("(" + polColumn + " LIKE '%" + pol3 + "%')");
		    firstone = false;
		}
		if (pol4 != null) {
		    if (!firstone)
			query += (" || ");
		    query += ("(" + polColumn + " LIKE '%" + pol4 + "%')");
		    firstone = false;
		}
		query += (")");
		additional_term = true;
	    }
	}

	// Minimum spatial resolution.
        if ((p = params.getParam("SPATRES")) != null && p.isSet()) {
	    if (additional_term)
		query += (" AND ");
	    query += ("(s_resolution >= " + p.doubleValue() + " or " + "s_resolution is null)");
	    additional_term = true;
	}

	// Minimum spectral resolution.
        if ((p = params.getParam("SPECRES")) != null && p.isSet()) {
	    if (additional_term)
		query += (" AND ");
	    query += ("(em_resolution >= " + p.doubleValue() + " or " + "em_resolution is null)");
	    additional_term = true;
	}

	// Minimum spectral resolving power.
        if ((p = params.getParam("SPECRP")) != null && p.isSet()) {
	    if (additional_term)
		query += (" AND ");
	    query += ("(em_res_power >= " + p.doubleValue() + " or " + "em_res_power is null)");
	    //query += ("NVL((em_min / em_resolution)  >= " +
	    //	p.doubleValue() + ", true)");
	    additional_term = true;
	}

	// Maximum number of output records (0 for a metadata only response).
        if ((p = params.getParam("MAXREC")) != null && p.isSet()) {
	    maxrec = p.intValue();
	    response.setMaxrec(maxrec);
	}

	// Publisher Dataset Identifier.
        if ((p = params.getParam("PubDID")) != null && p.isSet()) {
	    if (additional_term)
		query += (" AND ");

	    // Extract the ID of the dataset within the index table.
	    sval = p.stringValue();
	    int offset = sval.lastIndexOf(":");
	    String datasetID = sval.substring(offset+1);
	    query += ("(id = " + datasetID + ")");

	    additional_term = true;
	}

	// Find only data from the specified collection or collections.
        if ((p = params.getParam("Collection")) != null && p.isSet()) {
	    sval = p.stringValue();

	    // The reserved value "all" searches all collections.
	    if (!sval.equalsIgnoreCase("all")) {
		String collections[] = sval.split(",");
		boolean firstone = true;

		if (collections.length > 0) {
		    if (additional_term)
			query += (" AND ");
		    query += ("(");

		    for (String collection : collections) {
			if (!firstone)
			    query += (" || ");
			query += ("(obs_collection like '%" + collection + "%')");
			firstone = false;
		    }

		    query += (")");
		    additional_term = true;
		}
	    }
	}

	// Dataset has an astrometric calibration (i.e., WCS).
	// Alternative: query += ("(wcsaxes1 IS NOT NULL)");

        if ((p = params.getParam("AstCalib")) != null && p.isSet()) {
	    sval = p.stringValue();
	    if (sval.equalsIgnoreCase("relative")) {
		if (additional_term)
		    query += (" AND ");
		query += ("(s_calib_status like '%relative%' " +
		    " or s_calib_status like '%absolute%')");
		additional_term = true;
	    } else if (sval.equalsIgnoreCase("absolute")) {
		if (additional_term)
		    query += (" AND ");
		query += ("(s_calib_status like '%absolute%')");
		additional_term = true;
	    }
	}

	// Test if dataset is a 2D image or a cube (nD where N >= 3).
        if ((p = params.getParam("type")) != null && p.isSet()) {
	    sval = p.stringValue();
	    boolean image = sval.equalsIgnoreCase("image");
	    boolean cube = sval.equalsIgnoreCase("cube");

	    if (additional_term)
		query += (" AND ");

	    if (image)
		query += ("(im_naxes = 2)");
	    else if (cube)
		query += ("(im_naxes >= 3)");

	    additional_term = true;
	}

	// Ensure a valid SQL query if no constraints were defined.
	if (!additional_term)
	    query = "SELECT * FROM " + tableName;

	// Perform the data query and write rows to the output table.
	//-------------------------------------------------------------
	ResultSetMetaData md;
	ResultSet rs;
	Statement st;
	String key;

	try {
	    // Execute the query.
	    String null_query = "SELECT * FROM " + tableName + " WHERE (id = 0);";

	    response.addInfo(key="QUERY", new TableInfo(key, query));
	    st = conn.createStatement();
	    rs = st.executeQuery((maxrec > 0) ? query : null_query);
	    md = rs.getMetaData();

	    // Walk through the resultset and output each row.
	    while (rs.next()) { 
	        double pos_ra=ra, pos_dec=dec;
		double scale, ra_dist, dec_dist;
		double obj_ra, obj_dec, dx;
		long naxis1, naxis2;
		double ra1, ra2;

		// Refine the spatial ROI intersect test.  The initial
		// SQL spatial query is crude but fast, and may find images
		// that do not satisfy the spatial constraint.  We do a
		// more rigorous test here as a second pass.

		if (spatial_constraint) {
		    obj_ra = rs.getDouble(s1Column);
		    obj_dec = rs.getDouble(s2Column);

		    // This assumes the spatial axes are 1 and 2; should be
		    // generalized.
		    scale = Math.abs(rs.getDouble("im_scale")) / (60.0 * 60.0);
		    naxis1 = rs.getLong("im_naxis1");
		    naxis2 = rs.getLong("im_naxis2");

		    // For SIAP compute the SR separately for RA and DEC.
		    // The offset value computed here is the sum of the ROI
		    // and target image half width/height values.

		    double ra_offset = ra_sr + (naxis1 * scale) / 2.0;
		    double dec_offset = dec_sr + (naxis2 * scale) / 2.0;

                    // Shift the RA coords to zero=180 to avoid wrap when
                    // computing the OBJ_RA to RA distance.

                    dx = pos_ra - 180.0;  pos_ra -= dx;
                    obj_ra -= dx;
                    if (obj_ra < 0)
                        obj_ra += 360.0;
                    if (obj_ra >= 360)
                        obj_ra -= 360.0;

                    ra_dist = Math.abs(obj_ra - pos_ra);
                    dec_dist = Math.abs(obj_dec - pos_dec);

		    if (ra_dist > ra_offset)
			continue;
		    if (dec_dist > dec_offset)
			continue;
		}

		// Output a record for each matched image format.
		String imageFormat = rs.getString("access_format").toLowerCase();
		String assocType = "MultiFormat";
		String assocId = null;
		int nAssoc = 0;

		if (nFormats > 1)
		    assocId = assocType + "." + new Integer(nAssoc++).toString(); 

		if (retFITS) {
		    if (imageFormat.contains("fits")) {
			response.addRow();
			if (nFormats > 1)
			    response.setValue("assoc_id", assocId);
			this.setMetadata(params, rs, response, "image/fits");
		    }
		}

		if (retGraphic) {
		    String outFormat = null;
		    if (imageFormat.contains("gif")) {
			outFormat = "image/gif";
		    } else if (imageFormat.contains("jpg") ||
			    imageFormat.contains("jpeg")) {
			outFormat = "image/jpeg";
		    } else if (imageFormat.contains("png")) {
			outFormat = "image/png";
		    }

		    if (outFormat != null) {
			response.addRow();
			if (nFormats > 1)
			    response.setValue("assoc_id", assocId);
			this.setMetadata(params, rs, response, outFormat);
		    }
		}
	    }

	} catch (DalOverflowException ex) {
	    throw ex;
	} catch (SQLException ex) {
	    throw new DalServerException(ex.getMessage());
	}
    }

    /**
     * Set the content of one query response record.
     *
     * @param	params		SIAP parameter set
     * @param	rs		SQL query result set
     * @param	r		RequestResponse object
     * @param	format		MIME type of output image
     */
    private void setMetadata(SiapParamSet params, ResultSet rs,
	RequestResponse r, String format) throws DalServerException {
	String sval = null;

	/*
	 * Compute the values of all dynamic service-specific metadata.
	 * Some metadata such as the access URL, pubDID, creation type, etc.
	 * is dynamically computed by the service and not stored as static
	 * values in the Image table.
	 * ------------------------------------------------------------------
	 */

	// Compute the access reference URL.
	String acRef = null;

	String authorityID = params.getValue("authorityID");
	String tableName = params.getValue("tableName");
	String datasetID = getColumn(rs, "id");
	if (authorityID == null || datasetID == null)
	    throw new DalServerException("missing authorityID or datasetID");

	// Format the AccessURL (acref) for this dataset.  The format is
	// <IVO-authority>#<internal-id>, where for <internal-id>
	// we use the tablename of the index table, plus the unique
	// dataset ID within the table.  The identity or location of
	// the dataset within the archive is not exposed externally.

	String publisherDID = authorityID;
	if (!publisherDID.endsWith("#"))
	    publisherDID += "#";
	publisherDID += tableName + ":" + datasetID;

	String runId = params.getValue("RunID");
	String serviceName = params.getValue("serviceName");
	String baseUrl = params.getValue("baseUrl");
	if (serviceName == null || baseUrl == null)
	    throw new DalServerException("missing serviceName or baseUrl parameter");

	if (!baseUrl.endsWith("/"))
	    baseUrl += "/";

	try {
	    acRef = baseUrl + serviceName + "/sync" + "?" +
		"REQUEST=accessData" + "&" +
		"FORMAT=" + format + "&" +
		"PubDID=" + URLEncoder.encode(publisherDID, "UTF-8");
	    if (runId != null)
		acRef += "&RunID=" + runId;

	} catch (UnsupportedEncodingException ex) {
	    throw new DalServerException("Encoding of access reference failed");
	}

	// Format the Preview URL for this dataset.  The format is as for the
	// AccessURL, except that PubDID is replaced with "Preview".

	String previewURL = null;
	try {
	    String url = baseUrl + serviceName + "/sync" + "?" +
		"REQUEST=accessData" + "&" +
		"Preview=true" + "&" +
		"PubDID=" + URLEncoder.encode(publisherDID, "UTF-8");
	    if (runId != null)
		url += "&RunID=" + runId;
	    previewURL = url;

	} catch (UnsupportedEncodingException ex) {
	    throw new DalServerException("Encoding of access reference failed");
	}

	// The creation type is computed by the service, not merely copied
	// from the Image table.  Currently we only support whole-file
	// retrival, so the value is fixed as "archival".
	
	String creation_type = "archival";

	// Compute image axes descriptive metadata.  Items like Naxis and
	// WCSAxes are stored in the Image table split into per-axis values
	// since we cannot easily manage array values in a RDBMS.  We want
	// to return these in the query response as arrays, so must construct
	// the arrays here.

	String WCSAxes = "";
	String naxis = "";
	int naxes = 0;
	String axlen = "";

	for (int i=1;  i <= 4;  i++) {
	    axlen = getColumn(rs, "im_naxis" + i);
	    if (!axlen.equals("1") && !axlen.equals("0")) {
		naxes++;
		if (i > 1)
		    naxis += " ";
		naxis += axlen;
	    }
	    if (!axlen.equals("1") && !axlen.equals("0")) {
		if (i > 1)
		    WCSAxes += " ";
		WCSAxes += getColumn(rs, "im_wcsaxes" + i);
	    }
	}

	/*
	 * Set the value of each Field of the Response table row.
	 * What Fields are defined depends upon the table configuration, set
	 * up by the addFields method earlier.
	 * ------------------------------------------------------------------
	 */

	String lastId = "";
	for (Iterator ii = r.fieldIterator();  ii.hasNext();  ) {
            Map.Entry me = (Map.Entry) ii.next();
            TableField field = (TableField) me.getValue();

	    // Get the ID of the next field to be set.
	    String fieldId = field.getId();
	    if (fieldId == null || fieldId.length() == 0)
		continue;

	    // Each Field appears twice in the hash table (Fields are hashed
	    // by both ID and UTYPE), so skip the second occurence of each
	    // field.

	    if (fieldId.equals(lastId))
		continue;
	    else
		lastId = fieldId;

	    // Handle the special cases for which values were computed above.
	    String id;
	    if (fieldId.equals(       id = "access_url")) {
		r.setValue(id, acRef);
	    } else if (fieldId.equals(id = "preview")) {
		r.setValue(id, previewURL);
	    } else if (fieldId.equals(id = "access_format")) {
		r.setValue(id, format);
	    } else if (fieldId.equals(id = "obs_creation_type")) {
		r.setValue(id, creation_type);
	    } else if (fieldId.equals(id = "im_naxes")) {
		r.setValue(id, naxes);
	    } else if (fieldId.equals(id = "im_naxis")) {
		r.setValue(id, naxis);
	    } else if (fieldId.equals(id = "im_wcsaxes")) {
		r.setValue(id, WCSAxes);

	    } else {
		// For most fields we copy the value from the Image table,
		// possibly with a table column rename.  Null values copy
		// through.  Custom metadata is pass-through as well.

		r.setValue(fieldId, getColumn(rs, fieldId));
	    }
	}

    }

    /**
     * Get a SQL ResultSet value, returning null if it is not found.
     *
     * @param	rs		Result set.
     * @param	columnName	The name of the desired column.
     */
    private String getColumn(ResultSet rs, String columnName) {
    
	// The table config may map logical->physical table column name.
	String colname = null;
	if (this.configTable != null)
	    colname = this.configTable.getColname(columnName);
	if (colname == null)
	    colname = columnName;

	String sval;
	try {
	    sval = rs.getString(colname);
	} catch (SQLException ex) {
	    sval = null;
	}

	return (sval);
    }

    /**
     * Test the SIAP Oracle interface.
     *
     * <pre>
     *   ingest [csv-file]	Turn a CSV version of the SIAP data model
     *				into a SiapData class which contains raw data
     *				defining the data model.
     *
     *   doc [type]		Generate an HTML version of the SIAP keyword
     * 				dictionary.
     *
     *   table [type]		Generate Java code to create the indicated
     *				keywords in a RequestResponse object.
     * </pre>
     */
    public static void main (String[] args) {
	if (args.length == 0 || args[0].equals("ingest")) {
	    // Read a CSV version of the SIAP/Spectrum data models, and use
	    // this to generate code for a compiled SiapData class which
	    // encodes the raw keyword data.

	    String inFile = (args.length > 1) ?
		args[1] : "lib/messier.csv";
	    String outFile = (args.length > 2) ?
		args[2] : "src/dalserver/SiapMessierData.java";

	    BufferedReader in = null;
	    PrintWriter out = null;

	    try {
		in = new BufferedReader(new FileReader(inFile));
	    } catch (FileNotFoundException ex) {
		System.out.println("Cannot open file " + "["+inFile+"]");
	    }

	    try {
		out = new PrintWriter(outFile);
	    } catch (FileNotFoundException ex) {
		System.out.println("Cannot open file " + "["+outFile+"]");
		System.exit(1);
	    }

	    try {
		out.println("package dalserver;");
		out.println("/**");
		out.println(" * Raw data for Messier catalog" +
		    " (this class is autogenerated).");
		out.println(" * See {@link dalserver.SiapMessier}.");
		out.println(" */");

		out.println("public class SiapMessierData {");
		out.println("  /** Messier catalog. */");
		out.println("  public static final String[] data = {");
		for (String line;  (line = in.readLine()) != null;  ) {
		    out.println("  \"" + line + "\",");
		}
		out.println("  };");
		out.println("}");

		out.close();
		in.close();

	    } catch (IOException ex) {
		System.out.println(ex.getMessage());
	    }
	}
    }
}
