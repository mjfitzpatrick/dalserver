/*
 * ScsMessier.java
 * $ID*
 */

package dalserver.scs;

import java.io.*;
import java.util.*;
import dalserver.*;
import dalserver.scs.*;

/**
 * ScsMessier implements the essential functionality needed to do a simple
 * cone search (SCS) query on the Messier catalog, which is built directly
 * into the DALServer SCS implementation in order to provide a simple
 * demonstration SCS service.
 *
 * @version	1.0, 24-Aug-2007
 * @author	Doug Tody
 */
public class ScsMessier {
    private ArrayList<MessierObj> catalog;
    private ArrayList<String> columnNames;
    private ArrayList<String> columnDatatypes;

    /**
     * A single object within the Messier catalog.
     */
    public static class MessierObj {
	/** The Messier object number, e.g., 31 for "M31". */
	public String id;

	/** Corresponding NGC name of the object. */
	public String ngc;

	/** Familiar name(s) (if any) for the object. */
	public String nickname;

	/** The object type, e.g., galaxy, cluster, etc. */
	public String objtype;

	/** The constellation in which the object appears. */
	public String constellation;

	/** Right ascension (HMS, J2000). */
	public String ra;

	/** Declination (DM, J2000). */
	public String dec;

	/** Apparent visual magnitude.  */
	public String mag;

	/** Apparent angular diameter (arcmin). */
	public String diam;

	/** Distance in Kilo light years. */
	public String dist;
    }


    /** Null constructor to generate a new Messier query object, providing
     * the functionality to query the Messier catalog.
     */
    public ScsMessier() {
	catalog = new ArrayList<MessierObj>();
	columnNames = new ArrayList<String>();
	columnDatatypes = new ArrayList<String>();

	// Generate the column metadata.
	columnNames.add("ID");  columnDatatypes.add("char");
	columnNames.add("NGC");  columnDatatypes.add("char");
	columnNames.add("Nickname");  columnDatatypes.add("char");
	columnNames.add("ObjectType");  columnDatatypes.add("char");
	columnNames.add("Constellation");  columnDatatypes.add("char");
	columnNames.add("RA");  columnDatatypes.add("char");
	columnNames.add("DEC");  columnDatatypes.add("char");
	columnNames.add("Mag");  columnDatatypes.add("char");
	columnNames.add("Diam");  columnDatatypes.add("char");
	columnNames.add("Dist");  columnDatatypes.add("char");

	// Load the Messier catalog into memory.
	for (String line : ScsMessierData.data) {
	    MessierObj obj = new MessierObj();

	    // Crude parser which assumes that the CSV has the right columns.
	    String tok[] = line.split("[|]");
	    if (tok.length < 10)
		continue;

	    obj.id = tok[0].length() == 0 ? null : tok[0].trim();
	    obj.ngc = tok[1].length() == 0 ? null : tok[1].trim();
	    obj.nickname = tok[2].length() == 0 ? null : tok[2].trim();
	    obj.objtype = tok[3].length() == 0 ? null : tok[3].trim();
	    obj.constellation = tok[4].length() == 0 ? null : tok[4].trim();
	    obj.ra = tok[5].length() == 0 ? null : tok[5].trim();
	    obj.dec = tok[6].length() == 0 ? null : tok[6].trim();
	    obj.mag = tok[7].length() == 0 ? null : tok[7].trim();
	    obj.diam = tok[8].length() == 0 ? null : tok[8].trim();
	    obj.dist = tok[9].length() == 0 ? null : tok[9].trim();

	    catalog.add(obj);
	}
    }

    /**
     * Query the Messier catalog, writing results to the output
     * request response object.  The response object can later be
     * serialized and returned in various output formats.
     *
     * @param	params		The SCS service input parameters.
     *
     * @param	response	The request response object.
     */
    public void query(ScsParamSet params, RequestResponse response)
	throws DalServerException, DalOverflowException {

	// Get the input parameters.
	double ra, dec, sr;
	boolean allSky;
	Param p;

	// The following parameters are mandatory.
	if ((p = params.getParam("RA")) == null || !p.isSet())
	    throw new DalServerException("Param RA not specified");
	else
	    ra = p.doubleValue();

	if ((p = params.getParam("DEC")) == null || !p.isSet())
	    throw new DalServerException("Param DEC not specified");
	else
	    dec = p.doubleValue();

	if ((p = params.getParam("SR")) == null || !p.isSet())
	    throw new DalServerException("Param SR not specified");
	else
	    sr = p.doubleValue();

        // Check for SR=180 degrees (entire sky).
	allSky = (Math.abs(sr - 180.0) < 0.000001);

	// Construct the output table.
	for (int i=0;  i < getColumnCount();  i++) {
	    String name = getColumnName(i);
	    String type = getColumnDatatype(i);
	    TableField field = new TableField();

	    field.setId(name);
	    field.setName(name);
	    field.setUtype("Messier." + name);
	    field.setIndex(i);

	    if (name.equalsIgnoreCase("ID")) {
		field.setUcd("ID_MAIN");
		field.setDataType(type);
		if (type.equals("char"))
		    field.setArraySize("*");
	    } else if (name.equalsIgnoreCase("RA")) {
		field.setUcd("POS_EQ_RA_MAIN");
		field.setDataType("double");
		field.setArraySize(null);
	    } else if (name.equalsIgnoreCase("DEC")) {
		field.setUcd("POS_EQ_DEC_MAIN");
		field.setDataType("double");
		field.setArraySize(null);
	    } else if (type.equals("char")) {
		field.setDataType(type);
		if (type.equals("char"))
		    field.setArraySize("*");
	    }

	    response.addField(field);
	}

	// Perform the query and write rows to the output table.
	for (int i=0;  i < catalog.size();  i++) {
	    MessierObj obj = catalog.get(i);

	    // This is not completely general; assumes ICRS for now.
	    double obj_ra = parseHMS(obj.ra) * 15.0;
	    double obj_dec = parseHMS(obj.dec);

	    if (!allSky) {
		double pos_ra=ra, pos_dec=dec;
		double dx;

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

	    try {
		response.addRow();
		response.setValue("ID", obj.id);
		response.setValue("RA", obj_ra);
		response.setValue("DEC", obj_dec);
		response.setValue("NGC", obj.ngc);
		response.setValue("Nickname", obj.nickname);
		response.setValue("ObjectType", obj.objtype);
		response.setValue("Constellation", obj.constellation);
		response.setValue("Mag", obj.mag);
		response.setValue("Diam", obj.diam);
		response.setValue("Dist", obj.dist);
	    } catch (DalOverflowException ex) {
		throw ex;
	    }
	}
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
	double value = 0;

	for (int i=0;  i < 3;  i++) {
	    try {
		String token = tok.nextToken(" :");
		if (i == 0)
		    value = new Float(token);
		else
		    value += (new Float(token) / 60.0);
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
     * Get the number of table columns.
     */
    public int getColumnCount() {
	return (columnNames.size());
    }

    /**
     * Get a column name.
     *
     * @param	index		The zero-index column index.
     */
    public String getColumnName(int index) {
	return (columnNames.get(index));
    }

    /**
     * Get a column data type ("int", "float", "string", etc.).
     *
     * @param	index		The zero-index column index.
     */
    public String getColumnDatatype(int index) {
	return (columnDatatypes.get(index));
    }

    /**
     * Get the metadata for a single Messier catalog object.
     *
     * @param	index		The zero-index column index.
     */
    public MessierObj getMessierObj(int index) {
	return (catalog.get(index-1));
    }

    /**
     * Messier catalog utilities.
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
	if (args.length == 0 || args[0].equals("ingest")) {
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
