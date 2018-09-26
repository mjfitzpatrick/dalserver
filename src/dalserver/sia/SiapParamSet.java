/*
 * SiapParamSet.java
 * $ID*
 */

package dalserver.sia;

import dalserver.DalServerException;
import dalserver.RangeList;
import dalserver.Param;
import dalserver.ParamLevel;
import dalserver.ParamType;
import dalserver.ParamSet;

import java.io.*;
import java.util.*;

/**
 * Construct an initial default parameter set containing the parameters
 * for a SIAP service.  Currently this paramset contains the combined
 * parameters for all SIAP operations.  A list of the currently defined
 * parameters, including their name, type, and description, can be found
 * in <a href="doc-files/siap-params.html">this table</a>.
 * 
 * @version	2.0, 03-Aug-2014
 * @author	Doug Tody, Mark Cresitello-Dittmar
 */
public class SiapParamSet extends ParamSet implements Iterable<Param> {

    /** Create an initial default SIAP parameter set. */
    public SiapParamSet() throws DalServerException {
	// Shorthand for param type and level.
	final EnumSet<ParamType> STR = EnumSet.of(ParamType.STRING);
	final EnumSet<ParamType> BOO = EnumSet.of(ParamType.BOOLEAN);
	final EnumSet<ParamType> INT = EnumSet.of(ParamType.INTEGER);
	final EnumSet<ParamType> FLO = EnumSet.of(ParamType.FLOAT);
	final EnumSet<ParamType> ISO = EnumSet.of(ParamType.ISODATE);
	final EnumSet<ParamType> RIO = EnumSet.of(ParamType.INTEGER, ParamType.RANGELIST, ParamType.ORDERED);
	final EnumSet<ParamType> RIU = EnumSet.of(ParamType.INTEGER, ParamType.RANGELIST);
	final EnumSet<ParamType> RFO = EnumSet.of(ParamType.FLOAT, ParamType.RANGELIST, ParamType.ORDERED);
	final EnumSet<ParamType> RFU = EnumSet.of(ParamType.FLOAT, ParamType.RANGELIST);
	final EnumSet<ParamType> RSO = EnumSet.of(ParamType.STRING, ParamType.RANGELIST, ParamType.ORDERED);
	final EnumSet<ParamType> RSU = EnumSet.of(ParamType.STRING, ParamType.RANGELIST);
	final EnumSet<ParamType> RDO = EnumSet.of(ParamType.ISODATE, ParamType.RANGELIST, ParamType.ORDERED);
	final EnumSet<ParamType> RDU = EnumSet.of(ParamType.ISODATE, ParamType.RANGELIST);
	final EnumSet<ParamType> RLO = EnumSet.of(ParamType.RANGELIST, ParamType.ORDERED);

	// Define all core parameters defined by the SIAP standard.
	// This would be more flexibly done by reading an external
	// schema, but a wired in approach is simpler for now.

	// General protocol-level parameters.
	this.addParam(new Param("VERSION",     STR, "2.0", "SIAP protocol version"));
	this.addParam(new Param("REQUEST",     STR, "Operation to be performed"));

	// Parameters for the queryData operation.
	this.addParam(new Param("POS",         RFU, "Central coordinates of search region"));
	this.addParam(new Param("SIZE",        RFU, "Size (width[,height]) of the search region"));
	this.addParam(new Param("BAND",        RLO, "Spectral bandpass of the search region"));
	this.addParam(new Param("TIME",        RDO, "Range of times for the search region"));
	this.addParam(new Param("POL",         RSU, "Polarization types of interest"));
	this.addParam(new Param("FORMAT",      RSU, "Allowable output data formats"));
	this.addParam(new Param("RESPONSEFORMAT", STR, "Format of query response"));

	this.addParam(new Param("MODE",        STR, "Query mode"));
	this.addParam(new Param("SECTION",     STR, "Image section (accessData)"));
	this.addParam(new Param("REGION",      STR, "STC-S region specification"));
	this.addParam(new Param("INTERSECT",   STR, "Specifies how image footprint may overlap ROI"));

	this.addParam(new Param("SPECRES",     FLO, "Minimum spectral resolution"));
	this.addParam(new Param("SPECRP",      FLO, "Spectral resolving power"));
	this.addParam(new Param("SPATRES",     FLO, "Minimum spatial resolution"));
	this.addParam(new Param("TIMERES",     FLO, "Minimum temporal resolution"));
	this.addParam(new Param("FLUXLIMIT",   FLO, "Maximum RMS noise level"));

	this.addParam(new Param("TargetName",  STR, "Target name"));
	this.addParam(new Param("TargetClass", RSU, "Target class names to search for"));

	this.addParam(new Param("ASTCalib",    STR, "Minimum level of astrometric calibration"));
	this.addParam(new Param("FluxCalib",   STR, "Minimum level of flux calibration"));

	this.addParam(new Param("TYPE",        STR, "Dataset type"));
	this.addParam(new Param("SUBTYPE",     STR, "Dataset subtype"));
	this.addParam(new Param("PubDID",      STR, "Publisher-assigned dataset identifier"));
	this.addParam(new Param("CreatorDID",  STR, "Creator-assigned dataset identifier"));
	this.addParam(new Param("Collection",  RSU, "Data collection name or pattern"));

	this.addParam(new Param("TOP",         INT, "Number of top-ranked items to return"));
	this.addParam(new Param("MAXREC",      INT, "Maximum number of output records"));
	this.addParam(new Param("MODTIME",     RDO, "Range of modification times"));
	this.addParam(new Param("COMPRESS",    BOO, "Allow dataset compression"));
	this.addParam(new Param("VERB",        INT, "Output verbosity level"));
	this.addParam(new Param("RUNID",       STR, "Runtime job ID string"));

	// Define any service-defined extension parameters here.
	// Client-defined parameters can only be specified at runtime.
    }


    // Exercise the parameter mechanism.
    public static void main (String[] args) {
	if (args.length == 0 || args[0].equals("test")) {
	    try {
		// Create a new, default SIAP queryData parameter set.
		SiapParamSet p = new SiapParamSet();

		// Set some typical parameter values.
		p.setValue("POS", "12.0,0.0;ICRS");
		p.setValue("SIZE", "0.2,0.03");
		p.setValue("BAND", "1.23e-08/3.2e-7,3.5e-7/5.2e-7,0.5e-8/0.85e-8;source");
		p.setValue("POL", "Q,V");
                p.setValue("SPECRES", "2000.0");
		p.setValue("TargetClass", "qso,agn,grb");
		p.setValue("Collection", "SDSS,2MASS");
		p.setValue("TOP", "10");
		p.setValue("Compress", "true");

		// Print out the edited SIAP parameter set.
		System.out.println ("SIAP: " + p);

	    } catch (DalServerException ex) {
		System.out.println ("DalServerException");
	    }

	} else if (args[0].equals("doc")) {
	    // Generate an HTML version of a SIAP parameter set.
	    // Place the generated file into the dalserver/doc-files directory
	    // to have it included in the Javadoc.

	    String fname = "siap-params.html";
	    Object last = null;

	    try {
		// Create an SIAP parameter set.
		SiapParamSet siap = new SiapParamSet();

		// Output the parameter set in HTML format.
		PrintWriter out = new PrintWriter(new FileWriter(fname));
		out.println("<HTML><HEAD>");
		out.println("<TITLE>SIAP Parameters</TITLE>");
		out.println("</HEAD><BODY>");
		out.println("<TABLE width=700 align=center>");
		out.println("<TR><TD " +
		    "colspan=3 align=center bgcolor=\"LightGray\">" +
		    "SIAP Parameters" + "</TD></TR>");

		for (Iterator i = siap.iterator();  i.hasNext();  ) {
		    Map.Entry me = (Map.Entry) i.next();
		    Object obj = (Object) me.getValue();
		    if (obj == last)
			continue;
		    Param o = (Param)obj;

		    out.println("<TR><TD>" + o.getName() + 
			"</TD><TD>" + o.getType() + 
			"</TD><TD>" + o.getDescription() + 
			"</TD></TR>");

		    last = obj;
		}

		out.println("</TABLE>");
		out.println("</BODY></HTML>");
		out.close();

	    } catch (DalServerException ex) {
		System.out.println ("cannot create SIAP parameter set");
	    } catch (IOException ex) {
		System.out.println ("cannot write file " + fname);
	    }
	}
    }
}
