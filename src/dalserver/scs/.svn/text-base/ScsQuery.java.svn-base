/*
 * ScsQuery.java
 * $ID*
 */

package dalserver.scs;

import java.io.*;
import java.util.*;
import java.sql.*;
import dalserver.*;
import dalserver.scs.*;

/**
 * ScsQuery provides a simple cone search (SCS) capability for any
 * catalog which can be accessed via a JDBC database connection to a
 * supported DBMS.
 *
 * @version	1.0, 05-Aug-2014
 * @author	Doug Tody
 */
public class ScsQuery extends DbmsQuery {
    /** Constructor to generate a new ScsQuery object, providing
     * the functionality to query a remote catalog.
     */
    public ScsQuery(String dbType, String jdbcDriver)
	throws DalServerException {

	super(dbType, jdbcDriver);
    }

    /**
     * Query the remote catalog, writing results to the output
     * request response object.  The response object can later be
     * serialized and returned in various output formats.
     *
     * @param	params		The SCS service input parameters.
     *
     * @param	response	The request response object.
     */
    public void
    query(ScsParamSet params, RequestResponse response)
	throws DalServerException, DalOverflowException {

	Connection conn;
	ConfigTable conf;
	String tableName=null;
	boolean positional=true, allsky=false, metadataQuery=false;
	String idColumn=null, raColumn=null, decColumn=null;
	Param p_ra, p_dec, p_sr;
	double ra=0, dec=0, sr=0;

	// A connection to the DBMS must already be open.
	conn = this.getConnection();

	// Get the name of the table to be queried.
	tableName = params.getValue("tableName");
	if (tableName == null)
	    throw new DalServerException("Table name not specified.");

	// The following parameters are mandatory.
	if ((p_ra = params.getParam("RA")) == null || !p_ra.isSet())
	    throw new DalServerException("Param RA not specified");
	else
	    ra = p_ra.doubleValue();

	if ((p_dec = params.getParam("DEC")) == null || !p_dec.isSet())
	    throw new DalServerException("Param DEC not specified");
	else
	    dec = p_dec.doubleValue();

	if ((p_sr = params.getParam("SR")) == null || !p_sr.isSet())
	    throw new DalServerException("Param SR not specified");
	else
	    sr = p_sr.doubleValue();

	// Check for SR=180 degrees (entire sky).
	allsky = (Math.abs(sr - 180.0) < 0.000001);

	// Check for SR=0 (metadata query).
	metadataQuery = (Math.abs(sr - 0.0) < 0.000001);

	// If no search region specified the entire table will be returned.
	if (allsky)
	    positional = false;

	// Get the field to be used for the record ID.
	Param p = params.getParam("idColumn");
	if (p == null) {
	    throw new DalServerException("Table unique ID field " +
		"must be specified for cone search.");
	} else
	    idColumn = p.stringValue();

	// Get the fields to be used for RA and DEC.
	p = params.getParam("raColumn");
	if (p != null)
	    raColumn = p.stringValue();
	p = params.getParam("decColumn");
	if (p != null)
	    decColumn = p.stringValue();

	if (raColumn == null || decColumn == null)
	    throw new DalServerException("Table RA,DEC fields " +
		"must be specified for a cone search.");

	// Query the DBMS for the table metadata.
	boolean id_found=false, ra_found=false, dec_found=false;
	boolean ra_numeric=false, dec_numeric=false;
	ArrayList<String> fields = new ArrayList<String>();
	DatabaseMetaData dbm;
	int nFields = 0;
	ResultSet rs;

	try {
	    dbm = conn.getMetaData();
	    rs = dbm.getColumns(null, "%", tableName, "%");

	    // Check that we have a valid table.
	    if (!rs.first()) {
		throw new DalServerException(
		    "Empty or nonexistent table (" + tableName + ")");
	    }

	    conf = new ConfigTable(params, null);

	    // Define the fields of the output query result table.  Verify
	    // that the named positional query fields exist if specified.
	    // The optional ConfigTable facility may be used to customize
	    // the output if desired, by omitting specified fields, or adding
	    // additional VO metadata (UCD, unit, etc.) to selected fields.
	    // For cone search ConfigTable cannot be used to add fields as
	    // we are limited to what is in the table being queried.

	    while (rs.next()) { 
		String colName = rs.getString("COLUMN_NAME").toLowerCase();
		int colType = rs.getInt("DATA_TYPE");

		// Define the corresponding field of the output table.
		TableField field = new TableField();

		field.setId(colName);
		field.setName(colName);

		// Check for the ID key field.
		if (!id_found) {
		    if (colName.equalsIgnoreCase(idColumn)) {
			id_found = true;
			field.setUcd("ID_MAIN");
		    }
		}

		// Check for the POS key fields.
		if (!ra_found) {
		    if (colName.equalsIgnoreCase(raColumn)) {
			ra_numeric = (
			    colType == java.sql.Types.REAL ||
			    colType == java.sql.Types.FLOAT ||
			    colType == java.sql.Types.DOUBLE ||
			    colType == java.sql.Types.DECIMAL ||
			    colType == java.sql.Types.NUMERIC);
			ra_found = true;
			field.setUcd("POS_EQ_RA_MAIN");
			colType = java.sql.Types.DOUBLE;
		    }
		}
		if (!dec_found) {
		    if (colName.equalsIgnoreCase(decColumn)) {
			dec_numeric = (
			    colType == java.sql.Types.REAL ||
			    colType == java.sql.Types.FLOAT ||
			    colType == java.sql.Types.DOUBLE ||
			    colType == java.sql.Types.DECIMAL ||
			    colType == java.sql.Types.NUMERIC);
			dec_found = true;
			field.setUcd("POS_EQ_DEC_MAIN");
			colType = java.sql.Types.DOUBLE;
		    }
		}

		switch (colType) {
		case java.sql.Types.BIT:
		case java.sql.Types.TINYINT:
		case java.sql.Types.SMALLINT:
		case java.sql.Types.INTEGER:
		case java.sql.Types.BIGINT:
		    field.setDataType("int");
		    break;
		case java.sql.Types.REAL:
		case java.sql.Types.FLOAT:
		case java.sql.Types.DOUBLE:
		case java.sql.Types.DECIMAL:
		case java.sql.Types.NUMERIC:
		    field.setDataType("double");
		    break;
		case java.sql.Types.CHAR:
		case java.sql.Types.VARCHAR:
		case java.sql.Types.LONGVARCHAR:
		default:
		    field.setDataType("char");
		    field.setArraySize("*");
		    break;
		}

		// UTYPE needs to be set here due to a bug in addField.
		field.setUtype(tableName + "." + colName);
		field.setIndex(nFields++);

		conf.addField(response, field);
		fields.add(colName);
	    }
	} catch (SQLException ex) {
	    throw new DalServerException(ex.getMessage());
	}

	// Verify that the RA and DEC fields were found, if pos query.
	if (!metadataQuery && (!ra_found || !dec_found))
	    throw new DalServerException("Designated RA or DEC field " +
		"not found in table " + tableName);

	// If this a metadata query we are done.
	if (metadataQuery)
	    return;

	// Perform the data query and write rows to the output table.
	int nrows = 0, ncols = 0;;
	ResultSetMetaData md;
	Statement st;
	String key;

	try {
	    String query = "SELECT * FROM " + sqlName(tableName);

	    // If this is a positional query we need to check that RA and
	    // DEC are in range.  A simple DBMS index on either RA or DEC
	    // may help on large tables.  More sophisticated spatial indexing
	    // (HTM etc.) is not yet supported.

	    if (positional && (ra_numeric || dec_numeric)) {
		query += (" WHERE ");
		boolean first_term = true;

		if (ra_numeric) {
		    double ra1 = ra - sr;
		    double ra2 = ra + sr;

		    // Handle the case of SR crossing the 0/360 point.
		    if (ra1 < 0) {
			if (!first_term)
			    query += (" AND ");
			else
			    first_term = false;

			query += ("(" + sqlName(raColumn) +
			    " BETWEEN " + (360.0 + ra1) + " AND " + 360.0);
			query += (" OR " + sqlName(raColumn) +
			    " BETWEEN " + 0.0 + " AND " + ra2 + ")");

		    } else {
			if (!first_term)
			    query += (" AND ");
			else
			    first_term = false;

			query += (sqlName(raColumn) +
			    " BETWEEN " + ra1 + " AND " + ra2);
		    }
		}

		if (dec_numeric) {
		    double dec1 = Math.max(-90.0, Math.min(90.0, dec - sr));
		    double dec2 = Math.max(-90.0, Math.min(90.0, dec + sr));

		    if (!first_term)
			query += (" AND ");
		    else
			first_term = false;

		    query += (sqlName(decColumn) +
			" BETWEEN " + dec1 + " AND " + dec2);
		}
	    }

	    // Execute the query.
	    response.addInfo(key="SQL_QUERY", new TableInfo(key, query));
	    st = conn.createStatement();
	    rs = st.executeQuery(query);
	    md = rs.getMetaData();
	    ncols = md.getColumnCount();

	    // Now check all the returned table rows to see if in ROI.
	    // We assume J2000/ICRS coords here; the supplied cols should
	    // conform (if necessary additional columns can be added to an
	    // existing table).  For larger tables, numeric positions are
	    // more efficient.

	    while (rs.next()) { 
		double pos_ra=ra, pos_dec=dec;
		double obj_ra, obj_dec;
		double dx;

		// Get the RA and DEC fields in type double.
		if (ra_numeric)
		    obj_ra = rs.getDouble(raColumn);
		else
		    obj_ra = parseHMS(rs.getString(raColumn)) * 15.0;
		if (dec_numeric)
		    obj_dec = rs.getDouble(decColumn);
		else 
		    obj_dec = parseHMS(rs.getString(decColumn));

		if (positional) {
		    // Shift the RA coords to zero=180 to avoid wrap when
		    // computing the OBJ_RA to RA distance.
		    // dx = pos_ra - 180.0;  pos_ra -= dx;
		    // obj_ra -= dx;
		    // if (obj_ra < 0)
			// obj_ra += 360.0;
		    // if (obj_ra >= 360)
			// obj_ra -= 360.0;
		    // double ra_dist = Math.abs(obj_ra - pos_ra);
		    // double dec_dist = Math.abs(obj_dec - pos_dec);
		    // if (ra_dist > sr || dec_dist > sr)
			// continue;

		    if (distance(pos_ra, pos_dec, obj_ra, obj_dec) > sr)
			continue;
		}

		response.addRow();
		for (int i=0;  i < fields.size();  i++) {
		    String fieldName = fields.get(i);
		    if (response.getField(fieldName) == null)
			continue;

		    if (fieldName.equals(raColumn))
			response.setValue(fieldName, obj_ra);
		    else if (fieldName.equals(decColumn))
			response.setValue(fieldName, obj_dec);
		    else
			response.setValue(fieldName, rs.getString(fieldName));
		}

		nrows++;
	    }

	} catch (DalOverflowException ex) {
	    throw ex;
	} catch (SQLException ex) {
	    throw new DalServerException(ex.getMessage());
	}

	rs = null;
    }


    /**
     * Convert a coordinate value in various formats into a floating
     * point value.  Both sexagesimal and decimal formats are permitted.
     */
    public double parseHMS(String hms) {
	boolean negative = false;
	String str = hms.trim();
	if (str.startsWith("-")) {
	    str = str.substring(1);
	    negative = true;
	}

	StringTokenizer tok = new StringTokenizer(str);
	double scale = 60.0;
	double value = 0;

	for (int i=0;  i < 3;  i++) {
	    try {
		String token = tok.nextToken(" :");
		if (i == 0)
		    value = new Float(token);
		else {
		    value += (new Float(token) / scale);
		    scale *= 60;
		}
	    } catch (NoSuchElementException ex) {
		continue;
	    } catch (NullPointerException ex) {
		continue;
	    }
	}

	return (negative ? -value : value);
    }

    
    /**
     * Compute the distance in degrees between two points on the sky.
     * Adapted from some SkyServer code.
     */
    public static double distance(double ra1, double dec1,
	double ra2, double dec2) {
	   
	double d2r, nx1, ny1, nz1, nx2, ny2, nz2, dist;
	d2r = Math.PI/180.0;

	nx1 = Math.cos(dec1*d2r)*Math.cos(ra1*d2r);
	ny1 = Math.cos(dec1*d2r)*Math.sin(ra1*d2r);
	nz1 = Math.sin(dec1*d2r);
	nx2 = Math.cos(dec2*d2r)*Math.cos(ra2*d2r);
	ny2 = Math.cos(dec2*d2r)*Math.sin(ra2*d2r);
	nz2 = Math.sin(dec2*d2r);

	dist = 2 * Math.toDegrees(Math.asin(Math.sqrt(
	    Math.pow(nx1 - nx2, 2) +
	    Math.pow(ny1 - ny2, 2) +
	    Math.pow(nz1 - nz2, 2)) / 2));

	return (dist);
    }

    /**
     * Test the ScsQuery class.
     *
     * <pre>
     *   ingest [csv-file]	Turn a CSV version of the SCS data model
     *				into a ScsData class which contains raw data
     *				defining the data model.
     *
     *   doc [type]		Generate an HTML version of the SCS keyword
     * 				dictionary.
     *
     *   table [type]		Generate Java code to create the indicated
     *				keywords in a RequestResponse object.
     * </pre>
     */
    public static void main (String[] args) {
	if (args.length == 0 || args[0].equals("hms")) {
	    ScsQuery mysql;
	    try {
		mysql = new ScsQuery("MySql", "com.mysql.jdbc.Driver");
	    } catch (DalServerException ex) {
		return;
	    }

	    String hms1 = (args.length > 1) ? args[1] : "12:30:30.5";
	    String hms2 = (args.length > 2) ? args[2] : "12 30 30.5";

	    System.out.println("Decode " + hms1 + " = " + mysql.parseHMS(hms1));
	    System.out.println("Decode " + hms2 + " = " + mysql.parseHMS(hms2));
	
	} else if (args.length == 0 || args[0].equals("ingest")) {
	    // Read a CSV version of the SCS/Spectrum data models, and use
	    // this to generate code for a compiled ScsData class which
	    // encodes the raw keyword data.

	    String inFile = (args.length > 1) ?
		args[1] : "lib/messier.csv";
	    String outFile = (args.length > 2) ?
		args[2] : "src/dalserver/ScsMessierData.java";

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
		out.println(" * See {@link dalserver.ScsMessier}.");
		out.println(" */");

		out.println("public class ScsMessierData {");
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
