/*
 * SpectrumKeywordFactory.java
 * $ID*
 */

package dalserver.ssa;

import java.io.*;
import java.util.*;
import dalserver.*;
import dalserver.ssa.*;

/**
 * SpectrumKeywordFactory implements a factory class for the Spectrum
 * data model keywords.  The use of a factory class frees the client from
 * having to know all the detailed metadata associated with each type of
 * keyword.  Keywords are indexed by both their ID and UTYPE tags.
 *
 * <p>A list of the major Spectrum data model keywords including their
 * ID (short form) and full UTYPE keys is shown in
 * <a href="doc-files/spectrum-metadata.html">this table</a>.
 *
 * @version	1.0, 17-Jan-2007
 * @author	Doug Tody
 */
public class SpectrumKeywordFactory extends KeywordFactory {

    private int version = 1;

    /** Null constructor to generate a new Spectrum keyword factory. */
    public SpectrumKeywordFactory(String version) {
	super("Spectrum.");

	String gid=null, key=null; 
	String keywordData[];

	// Get the data for the desired data model version.
	if (version == null || version.startsWith("2")) {
	    keywordData = SsapV2KeywordData.data;
	    this.version = 2;
	} else {
	    keywordData = SsapV1KeywordData.data;
	    this.version = 1;
	}

	// Process the keyword dictionary.
	for (String line : keywordData) {
	    String id=null, utype=null, ucd=null, descr=null, fits=null;
	    String csv=null, dataType=null, arraySize=null, unit=null; 
	    String ssapUnit=null, hint=null, defval=null;

	    // Crude parser which assumes that the CSV has the right columns.
	    // (This is only run for code maintenance.)

	    String tok[] = line.split(",", 11+1);
	    if (tok.length < 5)
		continue;

	    id = tok[0].length() == 0 ? null : tok[0].trim();
	    utype = tok[1].length() == 0 ? null : tok[1].trim();
	    ucd = tok[2].length() == 0 ? null : tok[2].trim();
	    descr = tok[3].length() == 0 ? null : tok[3].trim();
	    fits = tok[4].length() == 0 ? null : tok[4].trim();
	    csv = tok[5].length() == 0 ? null : tok[5].trim();

	    if (tok.length > 6)
		dataType = tok[6].length() == 0 ? null : tok[6].trim();
	    if (tok.length > 7)
		arraySize = tok[7].length() == 0 ? null : tok[7].trim();
	    if (tok.length > 8)
		unit = tok[8].length() == 0 ? null : tok[8].trim();
	    if (tok.length > 9)
		ssapUnit = tok[9].length() == 0 ? null : tok[9].trim();
	    if (tok.length > 10)
		hint = tok[10].length() == 0 ? null : tok[10].trim();
	    if (tok.length > 11)
		defval = tok[11].length() == 0 ? null : tok[11].trim();

	    // Skip header line and blank lines.
	    if (utype != null && utype.equals("UTYPE"))
		continue;
	    if (utype == null)
		continue;

	    // Skip keywords which are used only for SSAP.
	    if (hint != null && hint.contains("Q"))
		continue;

	    // For Spectrum, default to PARAM for everything except data fields.
	    if (id != null && utype != null)
		if (!utype.startsWith("Spectrum.Data."))
		    hint = (hint == null) ? "p" : hint + "p";

	    // Process a GROUP followed by a set of PARAMs which belong to
	    // that group.

	    if (id == null) {
		// Start a new GROUP.
		this.addGroup(id=utype, id, gid=id,
		    key=utype, null, descr, hint);

	    } else {
		// Add a PARAM to the group.
		this.addParam(id, defval, id, gid, dataType, arraySize,
		    unit, utype, ucd, descr, fits, csv, hint);
	    }
	}
    }

    /**
     * Create a new Spectrum keyword factory and initialize an associated
     * request response to process Spectrum keywords. This is not required,
     * but allows automated initialization of related context such as the
     * keyword name space.
     *
     * @param response	RequestResponse object to be linked to the Spectrum
     *			keyword factory.
     * @param version	SSA/Spectrum version.
     */
    public SpectrumKeywordFactory(RequestResponse response, String version)
	throws DalServerException{

	// Create the keyword factory.
	this(version);

	// Set the response XML namespace for SSAP metadata.
	TableParam xmlnsPar = this.newParam("XmlnsSpec", null);
	response.setXmlns(xmlnsPar.getUtype(), xmlnsPar.getValue());
    }

    // Print out the Spectrum DM keywords. */
    @SuppressWarnings("unchecked")
    public static void main (String[] args) {
	if (args.length == 0 || args[0].equals("dump")) {
	    SpectrumKeywordFactory keywords = new SpectrumKeywordFactory("1.1");
	    Object last = null;

	    for (Object x : keywords.entrySet()) {
		Map.Entry<String,Object> keyVal = (Map.Entry<String,Object>) x;
		Object o = keyVal.getValue();

		if (o == last)
		    continue;
		if (o instanceof TableGroup) {
		    TableGroup g = (TableGroup) o;
		    System.out.println();
		    System.out.println(g.getName() + " = " +
			g.getDescription());
		} else {
		    TableParam p = (TableParam) o;
		    System.out.println(p.getName() + " (" + p.getUtype() + ")" +
			" = " + p.getDescription());
		}

		last = o;
	    }

	} else if (args[0].equals("doc")) {
            // Generate an HTML version of the Spectrum keyword dictionary.
            // Place the generated file in the dalserver/doc-files directory
            // to have it included in the javadoc.

            SpectrumKeywordFactory keywords = new SpectrumKeywordFactory("1.1");
            keywords.printDoc("spectrum-metadata.html",
		"Spectrum Data Model Keywords", null);

        } else if (args[0].equals("table")) {
            // Generate Java code for the Spectrum DM keywords.  This is
	    // used in service code to generate Spectrum object instances.

            SpectrumKeywordFactory keywords = new SpectrumKeywordFactory("1.1");
            keywords.printCode("spectrum-table.txt", "sdm", null);
        }

    }
}
