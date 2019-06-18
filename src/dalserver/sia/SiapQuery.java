/*
 * SiapQuery.java
 * $ID*
 */

package dalserver.sia;

import java.io.*;
import java.util.*;
import java.net.*;
import java.sql.*;

import dalserver.*;
import dalserver.sia.*;

/**
 * SiapQuery provides a simple image access capability for any image
 * collection which can be described via image metadata stored in a
 * DBMS-hosted Image table.  Both SIA V1 and V2(proto) are supported.
 *
 * @version	2.0, 15-Apr-2014
 * @author	Doug Tody
 *
 * This is generic DBMS-query code, driven off the primary Image table
 * and per-service Table config mechanisms.  This class supports the new
 * ObsTAP-based version of the SIAV2 primary image table.
 */
public class SiapQuery extends DbmsQuery {
    /** Query response table configuration. */
    private ConfigTable configTable;

    /* Service version. */
    private int version;

    /** Constructor to generate a new DBMS query object, providing
     * the functionality to query a remote DBMS-hosted catalog.
     */
    public SiapQuery(String dbType, String jdbcDriver)
	throws DalServerException {

	super(dbType, jdbcDriver);
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

	String dataModel;
	String serviceVersion = params.getValue("serviceVersion");
	String verb = params.getValue("verb");
        RequestResponse r = response;
        SiapKeywordFactory siap;

	// Get the correct SIA data model for the version of the SIA protocol
	// in use for this query.

	if (serviceVersion.startsWith("2")) {
	    siap = new SiapKeywordFactory("main", "2");
	    dataModel = "Image-2.0";
	    version = 2;
	} else {
	    siap = new SiapKeywordFactory("main", "1");
	    dataModel = "Image-1.0";
	    version = 1;
	}

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

	// Determine how much detailed metadata to return.
	c.setMask(siap.verbosityMask(verb));

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
	c.addParam(r, siap.newParam("datamodel_name", dataModel));
	c.addParam(r, siap.newParam("datamodel_prefix", "im"));
	c.addField(r, siap.newField("dataproduct_type"));
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
    public String
    queryDataset(String tableName, String id, String attribute)
	throws DalServerException {

	// An open DBMS connection is required.
	Connection conn = this.getConnection();

	// Compose the DBMS query.
	String query = "".format("SELECT %s FROM %s WHERE (%s = '%s')",
	    sqlName(attribute), tableName, sqlName("id"), id);

System.err.println ("queryDataset(): '" + query + "'\n");
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
     * @param	siap		The SIAP service instance
     * @param	params		The SIAP service input parameters.
     *
     * @param	response	The request response object.
     */
    public void
    query(SiapService siap, SiapParamSet params, RequestResponse response)
	throws DalServerException, DalOverflowException {

	String tableName="siav2model";		// default
	int maxrec = response.maxrec();
	String sval; Param p;

	// An open DBMS connection is required.
	Connection conn = this.getConnection();

	// Get the name of the SIAV2 primary image table to be queried.
	sval = params.getValue("tableName");
	if (sval != null)
	    tableName = sval;

	// Get the query mode.
	String mode = "archival";
	boolean archival_mode=true, cutout_mode=false;
        if ((p = params.getParam("MODE")) != null && p.isSet()) {
	    mode = p.stringValue();
	    archival_mode = mode.contains("archival");
	    cutout_mode = mode.contains("cutout") || mode.contains("match");
	}

        // SPATIAL Coverage.
	// ------------------
	// If SIZE is omitted, find anything which includes
        // the specified position, otherwise find anything which overlaps.

	boolean spatial_constraint = true, allSky = false;
	String s1Column=getColName("s_ra"), s2Column=getColName("s_dec");
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
	String e1Column=getColName("em_min"), e2Column=getColName("em_max");
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
	String t1Column=getColName("t_min"), t2Column=getColName("t_max");
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
	String polColumn=getColName("pol_states");
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
	// below as we compose the DBMS query.  

	// Compose the DBMS query.
	//-------------------------------------
	String subselect = "(SELECT * FROM " + tableName + 
		" WHERE q3c_radial_query (s_ra,s_dec," + ra + "," + dec + ",4.0)) s ";
	//String query = "SELECT * FROM " + tableName + " WHERE ";
	String query = "SELECT * FROM " + subselect + " WHERE ";
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
 
	    double ra1 = ra - ra_size;  double ra2 = ra + ra_size;
	    double dec1 = Math.max(-90.0, Math.min(90.0, dec - dec_size));
	    double dec2 = Math.max(-90.0, Math.min(90.0, dec + dec_size));

	    String poly = "{ ";
		poly += ra1 + ", " + dec1 + ", ";		// LL
		poly += ra2 + ", " + dec1 + ", ";		// LR
		poly += ra2 + ", " + dec2 + ", ";		// UR
		poly += ra1 + ", " + dec2       ;		// UL
		poly += " }";
System.err.println ("query(): poly: '" + poly + "'\n");


	    // This needs to be generalized to allow for a spatial position
	    // specified as NULL, e.g, for theory data.
	   
	  if (1 < 0) {
	    // Handle the case of RA+SR crossing the 0/360 point.
	    if (ra1 < 0) {
		if (additional_term)
		    query += (" AND ");

		query += ("(" + sqlName(s1Column) +
		    " BETWEEN " + (360.0 + ra1) + " AND " + 360.0);
		query += (" OR " + sqlName(s1Column) +
		    " BETWEEN " + 0.0 + " AND " + ra2 + ")");
		additional_term = true;

	    } else {
		if (additional_term)
		    query += (" AND ");

		query += ("(" + sqlName(s1Column) +
		    " BETWEEN " + ra1 + " AND " + ra2 + ")");
		additional_term = true;
	    }

	    // DEC term
	    if (additional_term)
		query += (" AND ");
	    query += ("(" + sqlName(s2Column) +
		" BETWEEN " + dec1 + " AND " + dec2 + ")");
	    additional_term = true;

	  } else if (1 == 0) {
		if (additional_term)
		    query += (" AND ");

		query += " q3c_poly_query (" + sqlName(s1Column) + ", " +
			sqlName(s2Column) + ", '" + poly + "')";
	        additional_term = true;

	  } else {
		if (additional_term)
		    query += (" AND ");

		query += " (" + ra  + " BETWEEN spat_lolimit1 and spat_hilimit1) AND ";
		query += " (" + dec + " BETWEEN spat_lolimit2 and spat_hilimit2)";
	        additional_term = true;
	  }
	}

	// If we have a BAND term, apply the constraint, or if the metadata
	// is null, ignore the constraint (i.e., constraint term evaluates
	// to TRUE).  [TO DO - add support for multiple ranges]

	if (spectral_constraint) {
	    if (additional_term)
		query += (" AND ");

	    query += ("(" + sqlName(e1Column) + " <= " + e2val +
		" or " + sqlName(e1Column) + " is null)");
	    query += (" AND ");
	    query += ("(" + sqlName(e2Column) + " >= " + e1val +
		" or " + sqlName(e2Column) + " is null)");

	    additional_term = true;
	}

	// If we have a TIME term, apply the constraint, or if the metadata
	// is null, ignore the constraint.
	// [TO DO - add support for multiple ranges]

	if (time_constraint) {
	    if (additional_term)
		query += (" AND ");

	    query += ("(" + sqlName(t1Column) + " <= " + t2val +
		" or " + sqlName(t1Column) + " is null)");
	    query += (" AND ");
	    query += ("(" + sqlName(t2Column) + " >= " + t1val +
		" or " + sqlName(t2Column) + " is null)");

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
		query += ("(" + sqlName(polColumn) + " IS NOT NULL)");
		additional_term = true;
	    } else {
		boolean firstone = true;
		query += ("(");
		if (pol1 != null) {
		    if (!firstone)
			query += (" || ");
		    query += ("(" + sqlName(polColumn) + " LIKE '%" + pol1 + "%')");
		    firstone = false;
		}
		if (pol2 != null) {
		    if (!firstone)
			query += (" || ");
		    query += ("(" + sqlName(polColumn) + " LIKE '%" + pol2 + "%')");
		    firstone = false;
		}
		if (pol3 != null) {
		    if (!firstone)
			query += (" || ");
		    query += ("(" + sqlName(polColumn) + " LIKE '%" + pol3 + "%')");
		    firstone = false;
		}
		if (pol4 != null) {
		    if (!firstone)
			query += (" || ");
		    query += ("(" + sqlName(polColumn) + " LIKE '%" + pol4 + "%')");
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
	    query += "".format("(%s >= %g OR %s is null)",
		sqlName("s_resolution"), p.doubleValue(), sqlName("s_resolution"));
	    additional_term = true;
	}

	// Minimum spectral resolution.
        if ((p = params.getParam("SPECRES")) != null && p.isSet()) {
	    if (additional_term)
		query += (" AND ");
	    query += "".format("(%s >= %g OR %s is null)",
		sqlName("em_resolution"), p.doubleValue(), sqlName("em_resolution"));
	    additional_term = true;
	}

	// Minimum spectral resolving power.
        if ((p = params.getParam("SPECRP")) != null && p.isSet()) {
	    if (additional_term)
		query += (" AND ");
	    query += "".format("(%s >= %g OR %s is null)",
		sqlName("em_res_power"), p.doubleValue(), sqlName("em_res_power"));
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
	    query += ("(" + sqlName("id") + " = '" + datasetID + "')");

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
			query += "".format("(%s like '%s')",
			    sqlName("obs_collection"), "%" + collection + "%");

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
	    String colname = sqlName("s_calib_status");

	    if (sval.equalsIgnoreCase("relative")) {
		if (additional_term)
		    query += (" AND ");
		query += ("(" + colname + " like '%relative%' " +
		    " or " + colname + " like '%absolute%')");
		additional_term = true;
	    } else if (sval.equalsIgnoreCase("absolute")) {
		if (additional_term)
		    query += (" AND ");
		query += ("(" + colname + " like '%absolute%')");
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
		query += ("(" + sqlName("im_naxes") + " = 2)");
	    else if (cube)
		query += ("(" + sqlName("im_naxes") + " >= 3)");
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

System.err.println ("query(): '" + query + "'\n");
	try {
	    // Execute the query.
	    //String null_query = "SELECT * FROM " +
	    //	tableName + " WHERE (" + sqlName("id") + " = 0);";
	    String null_query = "SELECT * FROM " + tableName + " LIMIT 1;";

	    response.addInfo(key="QUERY", new TableInfo(key, query));
	    st = conn.createStatement();
	    rs = st.executeQuery((maxrec > 0) ? query : null_query);
	    md = rs.getMetaData();

	    // Walk through the resultset and output each row.
	    int recnum = (maxrec > 0) ? maxrec : 1;
	    while (rs.next() && recnum > 0) { 
	        double pos_ra=ra, pos_dec=dec;
		double scale, ra_dist, dec_dist;
		double obj_ra, obj_dec, dx;
		long naxis1, naxis2;

                recnum -= 1;

		// Refine the spatial ROI intersect test.  The initial
		// SQL spatial query is crude but fast, and may find images
		// that do not satisfy the spatial constraint.  We do a
		// more rigorous test here as a second pass.

/**/
		if (spatial_constraint) {
		    obj_ra = rs.getDouble(s1Column);
		    obj_dec = rs.getDouble(s2Column);

		    // This assumes the spatial axes are 1 and 2; should be
		    // generalized.
		    String col_scale = getColName("im_scale");
		    String col_naxis1 = getColName("im_naxis1");
		    String col_naxis2 = getColName("im_naxis2");

		    scale = Math.abs(rs.getDouble(col_scale)) / (60.0 * 60.0);
		    naxis1 = rs.getLong(col_naxis1);
		    naxis2 = rs.getLong(col_naxis2);

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
/**/

		// Output a record for each matched image format.  There is
		// one archival image, but there may be several virtual
		// images that can be derived from it if accessed.  These
		// include dynamically formatted datasets such as a graphics
		// rendition, and virtual images such as a cutout.
	
		String imageFormat = getColumn(rs, "access_format").toLowerCase();
		String assocType = "MultiFormat";
		String assocId = null;
		int nAssoc = 0;

		if (nFormats > 1)
		    assocId = assocType + "." + new Integer(nAssoc++).toString(); 

		String[] imageTypes = { "archival", "virtual" };
		String pubDID;

		for (String imageType : imageTypes) {
		    if (retFITS &&
			imageType.equals("archival") && archival_mode) {

			if (imageFormat.contains("fits")) {
			    response.addRow();
			    if (nFormats > 1)
				response.setValue("assoc_id", assocId);

			    // Set the metadata for the archival image.
			    pubDID = setMetadata(params,
				rs, response, "image/fits");
			}
		    }

		    if (retFITS &&
			imageType.equals("virtual") && cutout_mode) {

			if (imageFormat.contains("fits")) {
			    response.addRow();
			    if (nFormats > 1)
				response.setValue("assoc_id", assocId);

			    // Set the metadata for the archival image.
			    pubDID = setMetadata(params,
				rs, response, "image/fits");

			    // Edit the metadata if a virtual image.
			    if (editVirtualImage(siap,
				pubDID, params, response) < 0) {

				response.deleteRow();
			    }
			}
		    }

		    // Virtual graphic images are possible but not currently
		    // supported.

		    if (retGraphic && imageType.equals("archival")) {
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

			    // Set the metadata for the archival image.
			    setMetadata(params, rs, response, outFormat);
			}
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
     *
     * The PubDID of the dataset is returned as the function value.
     */
    private String
    setMetadata (SiapParamSet params,
	ResultSet rs, RequestResponse r, String format)
	throws DalServerException {

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
	String sync = "/sync";

	String serviceName = params.getValue("serviceName");
	String baseUrl = params.getValue("baseUrl");

	if (this.version == 1) {
	    Param p;

	    // For the NOAO service we have the pre-computed accessRef in
	    // the table, so just use that.
	    acRef = getColumn(rs, "access_url");

            if ((p = params.getParam("POS")) != null && p.isSet()) {
	        // Get POS (e.g., "<val1>,<val2>;<property>").
                RangeList rl = p.rangeListValue();
                double ra = rl.doubleValue(0);
                double dec = rl.doubleValue(1);
		acRef += "&POS=" + ra + "," + dec;
	    }
            if ((p = params.getParam("SIZE")) != null && p.isSet()) {
		double ra_size=0.0, ra_sr=0.0, dec_size=0.0, dec_sr=0.0;

                RangeList rl = p.rangeListValue();
		ra_size = rl.doubleValue(0);
		ra_sr = ra_size / 2.0;
		try {
		    dec_size = rl.doubleValue(1);
		    dec_sr = dec_size / 2.0;
		} catch (DalServerException ex) {
		    dec_size = ra_size;
		    dec_sr = ra_sr;
		}
		acRef += "&SIZE=" + ra_size + "," + dec_size;
	    }

	} else {

	    // AccessData is always SIAV2 even if called by SIAV1.
	    // String sync = (this.version == 1) ? "" : "/sync";
	    if (serviceName == null || baseUrl == null)
	        throw new DalServerException("missing serviceName or baseUrl parameter");

	    if (!baseUrl.endsWith("/"))
	        baseUrl += "/";

	    try {
	        acRef = baseUrl + serviceName + sync + "?" +
		    "REQUEST=accessData" + "&" +
		    "FORMAT=" + format + "&" +
		    "PubDID=" + URLEncoder.encode(publisherDID, "UTF-8");

	    } catch (UnsupportedEncodingException ex) {
	        throw new DalServerException("Encoding of access reference failed");
	    }
	}
	if (runId != null)
	    acRef += "&RunID=" + runId;


	// Format the Preview URL for this dataset.  The format is as for the
	// AccessURL, except that PubDID is replaced with "Preview".

	String previewURL = null;
	if (this.version == 1) {
	    // For the NOAO service, previews are just an added argument.
	    previewURL = acRef + "&preview=true";

	} else {
	    try {
	        String url = baseUrl + serviceName + sync + "?" +
		    "REQUEST=accessData" + "&" +
		    "Preview=true" + "&" +
		    "PubDID=" + URLEncoder.encode(publisherDID, "UTF-8");
	        previewURL = url;

	    } catch (UnsupportedEncodingException ex) {
	        throw new DalServerException("Encoding of access reference failed");
	    }
	}
	if (runId != null)
	    previewURL += "&RunID=" + runId;


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
	    if (axlen == null)
		break;
	    if (!axlen.equals("1") && !axlen.equals("0")) {
		naxes++;
		if (i > 1)
		    naxis += " ";
		naxis += axlen;
	    }
	    if (!axlen.equals("1") && !axlen.equals("0")) {
		if (i > 1)
		    WCSAxes += " ";
		String wcsax = getColumn(rs, "im_wcsaxes" + i);
		if (wcsax != null)
		    WCSAxes += wcsax;
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
	    } else if (fieldId.equals(id = "obs_publisher_did")) {
		r.setValue(id, publisherDID);
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

	return (publisherDID);
    }

    /**
     * Edit the metadata for a virtual image.
     *
     * @param	pubDID		PubDID for the archival image
     * @param	siap		SiapService instance
     * @param	params		Request parameter set
     * @param	r		RequestResponse object
     *
     * This routine is only called if a virtual image (e.g. cutout) is being
     * added to the query response.  We invoke a task to compute the metadata
     * for the virtual image, and then edit the changed metadata in the query
     * response record for the virtual dataset.
     *
     * Zero is returned if a virtual image can be returned and the virtual
     * image definition was successful, otherwise -1 is returned.
     */
    private int editVirtualImage (SiapService siap, String pubDID,
	SiapParamSet params, RequestResponse response)
	throws DalServerException {

	RequestResponse r = response;
	String imagefile;
	URL fileURL;

	// Get the internal imagefile pathname for the given pubDID.
	String imageURL = siap.getImageURL(this, pubDID, false);
	if (imageURL != null) {
	    try {
		fileURL = new URL(imageURL);
		imagefile = fileURL.getPath();
	    } catch (Exception ex) {
		throw new DalServerException(ex.getMessage());
	    }
	} else {
	    throw new DalServerException(
		"Invalid image PubDID (" + pubDID + ")");
	}

	// Compute the metadata for the optimum image cutout.
	KeywordTable tab = siap.defineVirtualImage(params, imagefile);
	if (tab == null)
	    return (-1);

	// Edit the acref to reference the MDFILE for the virtual image.
	String acref, val;

	try {
	    val = r.getValue("access_url");
	    acref = URLDecoder.decode(val, "UTF-8");
	    String mdfile = tab.getKeyword("MDFILE");

	    int idoff = acref.indexOf("PubDID=") + 7;
	    String root = acref.substring(0, idoff);
	    String did = acref.substring(idoff);

	    int taboff = did.lastIndexOf("#") + 1;
	    String new_did = did.substring(0,taboff) + mdfile;
	    val = URLEncoder.encode(new_did, "UTF-8");
	    r.setValue("access_url", root + val);

	} catch (UnsupportedEncodingException ex) {
	    return (-1);
	}

	// Process the virtual image metadata and edit the current record.
	// Any keywords that are present in both the keyword table and the
	// response object will be updated.
	
	for (Map.Entry<String,String> entry : tab.entrySet()) {
	    String key = entry.getKey();
	    String value = entry.getValue();

	    if (r.getField(key) != null)
		r.setValue(key, value);
	}

	return (0);
    }

    /**
     * Get a SQL ResultSet value, returning null if it is not found.
     *
     * @param	rs		Result set.
     * @param	columnName	The name of the desired column.
     */
    private String getColumn(ResultSet rs, String columnName) {
    
	// The table config may map logical->physical table column name.
	String colname = getColName(columnName);

	String sval;
	try {
	    sval = rs.getString(colname);
	} catch (SQLException ex) {
	    sval = null;
	}

	return (sval);
    }

    /**
     * Map a canonical column name to a physical table name, applying
     * any table configuration options.
     *
     * @param	columnName	The canonical name of the column.
     */
    private String getColName(String columnName) {
    
	// The table config may map logical->physical table column name.
	String colname = null;
	if (this.configTable != null)
	    colname = this.configTable.getColname(columnName);
	if (colname == null)
	    colname = columnName;

	return (colname);
    }

    /**
     * Test the SIAP DBMS interface.
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
