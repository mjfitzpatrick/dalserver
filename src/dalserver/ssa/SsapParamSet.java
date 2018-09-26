/*
 * SsapParamSet.java
 * $ID*
 */

package dalserver.ssa;

import java.io.*;
import java.util.*;
import dalserver.*;
import dalserver.ssa.*;

/**
 * Construct an initial default parameter set containing the parameters
 * for an SSAP service.  Currently this paramset contains the combined
 * parameters for all SSAP operations.  A list of the currently defined
 * parameters, including their name, type, and description, can be found
 * in <a href="doc-files/ssap-params.html">this table</a>.
 * 
 * @version	1.0, 3-Dec-2006, 21-Apr-2014
 * @author	Doug Tody
 */
public class SsapParamSet extends ParamSet implements Iterable<Param> {

    /** Create an initial default SSAP parameter set. */
    public SsapParamSet() throws DalServerException {

	// Define some shorthand tags for param type and level.
	final EnumSet<ParamType> STR = EnumSet.of(ParamType.STRING);
	final EnumSet<ParamType> BOO = EnumSet.of(ParamType.BOOLEAN);
	final EnumSet<ParamType> INT = EnumSet.of(ParamType.INTEGER);
	final EnumSet<ParamType> FLO = EnumSet.of(ParamType.FLOAT);
	final EnumSet<ParamType> ISO = EnumSet.of(ParamType.ISODATE);
	final EnumSet<ParamType> RFO = EnumSet.of(ParamType.FLOAT, ParamType.RANGELIST, ParamType.ORDERED);
	final EnumSet<ParamType> RFU = EnumSet.of(ParamType.FLOAT, ParamType.RANGELIST);
	final EnumSet<ParamType> RSO = EnumSet.of(ParamType.STRING, ParamType.RANGELIST, ParamType.ORDERED);
	final EnumSet<ParamType> RSU = EnumSet.of(ParamType.STRING, ParamType.RANGELIST);
	final EnumSet<ParamType> RDO = EnumSet.of(ParamType.ISODATE, ParamType.RANGELIST, ParamType.ORDERED);
	final EnumSet<ParamType> RDU = EnumSet.of(ParamType.ISODATE, ParamType.RANGELIST);
	final EnumSet<ParamType> RLO = EnumSet.of(ParamType.RANGELIST, ParamType.ORDERED);

	// Define all core parameters defined by the SSAP standard.
	// This would be more flexibly done by reading an external
	// schema, but a wired in approach is simpler for now.

	// General protocol-level parameters.
	this.addParam(new Param("VERSION",     STR, "1.1", "SSAP protocol version"));
	this.addParam(new Param("REQUEST",     STR, "Operation to be performed"));

	// Parameters for the queryData operation.
	this.addParam(new Param("POS",         RFU, "Central coordinates of search region"));
	this.addParam(new Param("SIZE",        FLO, "Size (diameter) of the search region"));
	this.addParam(new Param("BAND",        RLO, "Spectral bandpass of the search region"));
	this.addParam(new Param("TIME",        RDO, "Range of times for the search region"));
	this.addParam(new Param("FORMAT",      RSU, "Allowable output data formats"));
	this.addParam(new Param("RESPONSEFORMAT", STR, "Format of query response"));

	this.addParam(new Param("APERTURE",    FLO, "Aperture diameter for spectral extraction"));
	this.addParam(new Param("SPECRES",     FLO, "Minimum spectral resolution"));
	this.addParam(new Param("SPECRP",      FLO, "Spectral resolving power"));
	this.addParam(new Param("SPATRES",     FLO, "Minimum spatial resolution"));
	this.addParam(new Param("TIMERES",     FLO, "Minimum temporal resolution"));

	this.addParam(new Param("SNR",         RFO, "Minimum signal to noise ratio"));
	this.addParam(new Param("Redshift",    RFO, "Redshift range"));
	this.addParam(new Param("VarAmpl",     RFO, "Variability amplitude range"));
	this.addParam(new Param("TargetName",  STR, "Target name"));
	this.addParam(new Param("TargetClass", RSU, "Target class names to search for"));
	this.addParam(new Param("FluxCalib",   STR, "Spectrum is flux calibrated"));
	this.addParam(new Param("WaveCalib",   STR, "Spectrum is wavelength calibrated"));

	this.addParam(new Param("PubDID",      STR, "Publisher-assigned dataset identifier"));
	this.addParam(new Param("CreatorDID",  STR, "Creator-assigned dataset identifier"));
	this.addParam(new Param("Collection",  RSU, "Data collection name or pattern"));

	this.addParam(new Param("Top",         INT, "Number of top-ranked items to return"));
	this.addParam(new Param("Maxrec",      INT, "Maximum number of output records"));
	this.addParam(new Param("Mtime",       RDO, "Range of modification times"));
	this.addParam(new Param("Compress",    BOO, "Allow dataset compression"));
	this.addParam(new Param("Verb",        INT, "Output verbosity level"));
	this.addParam(new Param("RunID",       STR, "Runtime job ID string"));

	// Define any service-defined extension parameters here.
	// Client-defined parameters can only be specified at runtime.
    }


    // Exercise the parameter mechanism.
    public static void main (String[] args) {
	if (args.length == 0 || args[0].equals("test")) {
	    try {
		// Create a new, default SSAP queryData parameter set.
		SsapParamSet p = new SsapParamSet();

		// Set some typical parameter values.
		p.setValue("POS", "12.0,0.0;ICRS");
		p.setValue("SIZE", "0.2");
		p.setValue("BAND", "1.23e-08/3.2e-7,3.5e-7/5.2e-7,0.5e-8/0.85e-8;source");
		p.setValue("REDSHIFT", "2.5/4");
		p.setValue("TargetClass", "qso,agn,grb");
		p.setValue("Collection", "SDSS,2MASS");
		p.setValue("TOP", "10");
		p.setValue("Compress", "true");

		// Print out the edited SSAP parameter set.
		System.out.println ("SSAP: " + p);

	    } catch (DalServerException ex) {
		System.out.println ("DalServerException");
	    }

	} else if (args[0].equals("doc")) {
	    // Generate an HTML version of a SSAP parameter set.
	    // Place the generated file into the dalserver/doc-files directory
	    // to have it included in the Javadoc.

	    String fname = "ssap-params.html";
	    Object last = null;

	    try {
		// Create an SSAP parameter set.
		SsapParamSet ssap = new SsapParamSet();

		// Output the parameter set in HTML format.
		PrintWriter out = new PrintWriter(new FileWriter(fname));
		out.println("<HTML><HEAD>");
		out.println("<TITLE>SSAP Parameters</TITLE>");
		out.println("</HEAD><BODY>");
		out.println("<TABLE width=700 align=center>");
		out.println("<TR><TD " +
		    "colspan=3 align=center bgcolor=\"LightGray\">" +
		    "SSAP Parameters" + "</TD></TR>");

		for (Iterator i = ssap.iterator();  i.hasNext();  ) {
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
		System.out.println ("cannot create SSAP parameter set");
	    } catch (IOException ex) {
		System.out.println ("cannot write file " + fname);
	    }
	}
    }
}
