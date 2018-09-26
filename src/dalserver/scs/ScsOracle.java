/*
 * ScsOracle.java
 * $ID*
 */

package dalserver.scs;

import java.io.*;
import java.util.*;
import java.sql.*;
import dalserver.*;
import dalserver.scs.*;

/**
 * ScsOracle provides a simple cone search (SCS) capability for any
 * catalog which can be accessed via JDBC to a MySQL database.
 *
 * @version	1.0, 24-Aug-2007
 * @author	Doug Tody
 *
 * NOTE - This class is obsolete and is being replaced by the generic ScsQuery
 * class, but is retained for the moment.
 */
public class ScsOracle {
    /** Connection to the remote DBMS. */
    private Connection conn;
    private String database;

    /** Constructor to generate a new Oracle query object, providing
     * the functionality to query a remote MySQL-hosted catalog.
     */
    public ScsOracle(String jdbcDriver) throws DalServerException {
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

	// Oracle supports schemas but only one database per server.
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
     * Query the remote catalog, writing results to the output
     * request response object.  The response object can later be
     * serialized and returned in various output formats.
     *
     * @param	params		The SCS service input parameters.
     *
     * @param	response	The request response object.
     */
    public void query(ScsParamSet params, RequestResponse response)
	throws DalServerException, DalOverflowException {

	String tableName=null;
	boolean positional=true, allsky=false, metadataQuery=false;
	String idColumn=null, raColumn=null, decColumn=null;
	Param p_ra, p_dec, p_sr;
	double ra=0, dec=0, sr=0;

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
	if (p != null)
	    idColumn = p.stringValue();
	else {
	    throw new DalServerException("Table unique ID field " +
		"must be specified for cone search.");
	}

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

	    // Define the fields of the output query result table.  Verify
	    // that the named positional query fields exist if specified.

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

		response.addField(field);
		fields.add(colName);
	    }
	} catch (SQLException ex) {
	    throw new DalServerException(ex.getMessage());
	}

	// If this a metadata query we are done.
	if (metadataQuery)
	    return;

	// Verify that the RA and DEC fields were found, if pos query.
	if (!ra_found || !dec_found)
	    throw new DalServerException("Designated RA or DEC field " +
		"not found in table " + tableName);

	// Perform the data query and write rows to the output table.
	String query = "SELECT * FROM " + tableName;

	int nrows = 0, ncols = 0;
	ResultSetMetaData md;
	Statement st;
	String key;

	try {
	    // If this is a positional query we need to check that RA and
	    // DEC are in range.

	    if (positional) {
		// If DEC is numeric (decimal degrees) we can apply a WHERE
		// clause on DEC; an index can be used on DEC to speed queries
		// on large tables.  More sophisticated spatial queries are
		// possible (HTM etc.) but are not supported here for generic
		// DBMS tables.

		if (dec_numeric) {
		    double dec1 = Math.max(-90.0, Math.min(90.0, dec - sr));
		    double dec2 = Math.max(-90.0, Math.min(90.0, dec + sr));

		    query += (" WHERE " + decColumn + " BETWEEN " +
			dec1 + " AND " + dec2);
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
	    throw new DalServerException(ex.getMessage() + "[" + query + "]");
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
     * Test the SCS MySQL interface.
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
	    ScsOracle dbms;
	    try {
		dbms = new ScsOracle("com.oracle.jdbc.OracleDriver");
	    } catch (DalServerException ex) {
		return;
	    }

	    String hms1 = (args.length > 1) ? args[1] : "12:30:30.5";
	    String hms2 = (args.length > 2) ? args[2] : "12 30 30.5";

	    System.out.println("Decode " + hms1 + " = " + dbms.parseHMS(hms1));
	    System.out.println("Decode " + hms2 + " = " + dbms.parseHMS(hms2));
	
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
