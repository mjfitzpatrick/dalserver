/*
 * SsapKeywordFactory.java
 * $ID*
 */

package dalserver.ssa;

import dalserver.RequestResponse;
import dalserver.KeywordFactory;
import dalserver.TableParam;
import dalserver.DalServerException;

import java.io.*;
import java.util.*;

/**
 * SsapKeywordFactory implements a factory class for well-known SSAP keywords,
 * including Groups, Params and Fields.  The use of a factory class frees
 * the client from having to know all the detailed metadata associated
 * with each type of keyword.  Keywords are indexed by both their ID and
 * UTYPE tags.  In general UTYPE is required to ensure uniqueness, but
 * within a limited scope, the ID tag may be sufficient to uniquely identify
 * a keyword without having to know the full UTYPE.
 *
 * <p>A list of the major SSAP keywords including the defined ID and UTYPE
 * keys is shown in <a href="doc-files/ssap-core-metadata.html">this table</a>.
 *
 * @version	2.0, 21-Apr-2014
 * @author	Doug Tody
 */
public class SsapKeywordFactory extends KeywordFactory {

    // SSA version in use.
    private int version = 1;

    /**
     * Constructor to generate a new SSAP keyword factory.  To generate a
     * keyword factory for only the Data element use model="data".  To
     * return the main model (both Access and Core), use model="main".
     *
     * @param	model	Data model to be used ("main" or "data").
     * @param	version	SSA version (1.x or 2.x).
     */
    public SsapKeywordFactory(String model, String version) {
	super("Spectrum.");

	String gid=null, group=null; 
	boolean dataElement = model.equalsIgnoreCase("data");
	boolean inDataElement = false;
	String keywordData[];

	// Get the data for the desired data model version.
	if (version == null || version.startsWith("2")) {
	    keywordData = SsapV2KeywordData.data;
	    this.version = 2;
	} else {
	    keywordData = SsapV1KeywordData.data;
	    this.version = 1;
	}

	// Process the Keyword data.
	for (String line : keywordData) {
	    String id=null, type=null, utype=null, ucd=null, descr=null, fits=null;
	    String csv=null; String dataType=null, arraySize=null, unit=null,
	    ssapUnit=null; String hint=null, defval=null;

	    // Skip forward if we are building a factory for the Data element,
	    // otherwise stop when we reach the Data element.

	    if (dataElement) {
		if (!inDataElement) {
		    if (line.startsWith("## DATA"))
			inDataElement = true;
		    continue;
		}
	    } else if (line.startsWith("## DATA"))
		break;

	    // Crude parser which assumes that the CSV has the right columns.
	    // This is only run for code maintenance to update the SpectrumDM.
	    // Note embedded commas are not permitted in fields (could be fixed,
	    // but it is easy to avoid embedded commas in the DM spreadsheet).

	    String tok[] = line.split(",", 13+1);
	    if (tok.length < 7)
		continue;

	    id = tok[0].length() == 0 ? null : tok[0].trim();
	    type = tok[1].length() == 0 ? null : tok[1].trim();
	    utype = tok[2].length() == 0 ? null : tok[2].trim();
	    ucd = tok[3].length() == 0 ? null : tok[3].trim();
	    descr = tok[4].length() == 0 ? null : tok[4].trim();
	    fits = tok[5].length() == 0 ? null : tok[5].trim();
	    csv = tok[6].length() == 0 ? null : tok[6].trim();

	    if (tok.length > 7)
		dataType = tok[7].length() == 0 ? null : tok[7].trim();
	    if (tok.length > 8)
		arraySize = tok[8].length() == 0 ? null : tok[8].trim();
	    if (tok.length > 9)
		unit = tok[9].length() == 0 ? null : tok[9].trim();
	    if (tok.length > 10)
		ssapUnit = tok[10].length() == 0 ? null : tok[10].trim();
	    if (tok.length > 11)
		hint = tok[11].length() == 0 ? null : tok[11].trim();
	    if (tok.length > 12)
		defval = tok[12].length() == 0 ? null : tok[12].trim();

	    // Skip header line and blank and comment lines.
	    if (utype != null && utype.equals("UTYPE"))
		continue;
	    if (utype == null)
		continue;
	    if (id != null && id.startsWith("#"))
		continue;

	    // Process a GROUP followed by a set of PARAMs which belong to
	    // that group.

	    if (id == null) {
		// Start a new GROUP.
		this.addGroup(id=utype, id, gid=id, group=utype,
		    null, descr, hint);

	    } else {
		// For groups other than Dataset|Spectrum in the V1.1DM, remove
		// the "Spectrum." prefix, which is used only for a serialized
		// Spectrum dataset.
	
		if (this.version == 1 &&
		    utype.startsWith("Spectrum.") &&
		    !(group.equals("Dataset") || group.equals("Spectrum")))
			utype = utype.substring(9);

		// Add a PARAM to the group.
		this.addParam(id, defval, id, gid, dataType, arraySize,
		    ssapUnit, utype, ucd, descr, fits, csv, hint);
	    }
	}
    }


    /**
     * Generate a new SSAP keyword factory for the main part of the SpectrumDM
     * (excluding the Data element).
     */
    public SsapKeywordFactory() {
	this("main", null);
    }

    /**
     * Create a new SSAP keyword factory and initialize an associated
     * request response to process SSAP keywords.  This is not required,
     * but allows automated initialization of related context such as the      
     * keyword name space.  
     *
     * @param response	RequestResponse object to be linked to the SSAP
     *			keyword factory.
     */
    public SsapKeywordFactory(RequestResponse response,
	String model, String version) throws DalServerException {

	// Create the keyword factory.
	this(model, version);

	// Get the SSA version.
	String dataModel;
	if (this.version == 2)
	    dataModel = "Spectrum-2.0";
	else
	    dataModel = "Spectrum-1.1";

	// Set the response Utype namespace for SSAP metadata.
	TableParam DMname = this.newParam("datamodel_name", dataModel);
	TableParam DMprefix = this.newParam("datamodel_prefix", "im");
	TableParam DMurl = this.newParam("datamodel_url", null);
	response.setXmlns(DMprefix.getValue(), DMurl.getValue());
    }

    /**
     * Return the "hint" flag mask corresponding to a given verbosity level.
     *
     * @param verbosity	The verbosity level (0,1,2,...) as a string.
     *
     * Each keyword may have hint characters hinting at how the keyword is
     * to be used.  In this case we use the hint to determine whether a
     * data model keyword is to be returned for a given SIA verbosity level.
     */
    public String verbosityMask(String verbosity) {
	if (verbosity == null)
	    return (null);

	if (verbosity.equals("0"))
	    return ("m");
	else if (verbosity.equals("1"))
	    return ("mq");
	else
	    return (null);
    }

    /**
     * SSAP keyword-related utilities.
     *
     * <pre>
     *   ingest [csv-file]	Turn a CSV version of the SSAP data model
     *				into a SsapData class which contains raw data
     *				defining the data model.
     *
     *   doc [type]		Generate an HTML version of the SSAP keyword
     * 				dictionary.
     *
     *   table [type]		Generate Java code to create the indicated
     *				keywords in a RequestResponse object.
     * </pre>
     */
    public static void main (String[] args) {
	if (args.length == 0 || args[0].equals("ingest")) {
	    // Read a CSV version of the SSAP/Spectrum data models, and use
	    // this to generate code for a compiled SsapData class which
	    // encodes the raw keyword data.
	
	    for (int v=1;  v <= 2;  v++) {
		String inFile = (args.length > 1) ?
		    args[1] : "lib/ssapv" + v + "-keywords.csv";
		String outFile = (args.length > 2) ?
		    args[2] : "src/dalserver/ssa/SsapV" + v + "KeywordData.java";

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
		    out.println("package dalserver.ssa;");
		    out.println("/**");
		    out.println(" * Raw data for the SSAP and Spectrum " +
			"data models (this class is autogenerated).");
		    out.println(" * See {@link dalserver.ssa.SsapKeywordFactory}.");
		    out.println(" */");

		    out.println("public class SsapV" + v + "KeywordData {");
		    out.println("  /** CSV data for the SSAP and Spectrum " +
			"data models. */");
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

	} else if (args[0].equals("doc")) {
	    // Generate an HTML version of the SSAP keyword dictionary.
	    // Place the generated file in the dalserver/doc-files directory
	    // to have it included in the javadoc.

	    SsapKeywordFactory mainKeywords = new SsapKeywordFactory();
	    mainKeywords.printDoc("ssap-core-metadata.html", "SSA Main Model Keywords", null);
	    SsapKeywordFactory dataKeywords = new SsapKeywordFactory("data", null);
	    dataKeywords.printDoc("ssap-data-metadata.html", "SSA Data Element Keywords", null);

	} else if (args[0].equals("table")) {
	    // Generate Java code for the SSAP keywords.  This is included
	    // in files like SsapService to generate RequestResponse objects

	    SsapKeywordFactory keywords = new SsapKeywordFactory();
	    keywords.printCode("ssa-table.txt", "ssap", "Qqm");
	}
    }
}
