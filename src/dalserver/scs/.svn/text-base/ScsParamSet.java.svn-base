/*
 * ScsParamSet.java
 * $ID*
 */

package dalserver.scs;

import java.io.*;
import java.util.*;
import dalserver.*;
import dalserver.scs.*;

/**
 * Construct an initial default parameter set containing the parameters
 * for Simple Cone Search (SCS) service.  A list of the currently defined
 * parameters, including their name, type, and description, can be found
 * in <a href="doc-files/scs-params.html">this table</a>.
 * 
 * @version	1.0, 24-Aug-2008
 * @author	Doug Tody
 */
public class ScsParamSet extends ParamSet implements Iterable<Param> {

    /** Create an initial default SCS parameter set. */
    public ScsParamSet() throws DalServerException {
	// Shorthand for param type and level.
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

	// Define all core parameters defined by the SCS standard.
	// This would be more flexibly done by reading an external
	// schema, but a wired in approach is simpler for now.

	// General protocol-level parameters.
	this.addParam(new Param("VERSION",     STR, "1.0", "SCS protocol version"));
	this.addParam(new Param("REQUEST",     STR, "Operation to be performed"));

	// Parameters for the SCS queryData operation (implicit).
	this.addParam(new Param("RA",          FLO, "Right ascension of search region (ICRS)"));
	this.addParam(new Param("DEC",         FLO, "Declination of search region (ICRS)"));
	this.addParam(new Param("SR",          FLO, "Radius of search region (decimal degrees)"));
	this.addParam(new Param("VERB",        INT, "Verbosity level of output"));

	// Define any service-defined extension parameters here.
	// Client-defined parameters can only be specified at runtime.

	this.addParam(new Param("FROM",        STR, "Table to be queried"));
	this.addParam(new Param("FORMAT",      STR, "Desired output data format"));
	this.addParam(new Param("RESPONSEFORMAT", STR, "Format of query response"));
	this.addParam(new Param("Maxrec",      INT, "Maximum number of output records"));
	this.addParam(new Param("RunID",       STR, "Runtime job ID string"));

	// Mark these as service-defined extensions as they are not in the SCS standard.
	this.getParam("FROM").setLevel(ParamLevel.EXTENSION);
	this.getParam("FORMAT").setLevel(ParamLevel.EXTENSION);
	this.getParam("Maxrec").setLevel(ParamLevel.EXTENSION);
	this.getParam("RunID").setLevel(ParamLevel.EXTENSION);
    }


    // Exercise the parameter mechanism.
    public static void main (String[] args) {
	if (args.length == 0 || args[0].equals("test")) {
	    try {
		// Create a new, default SCS queryData parameter set.
		ScsParamSet p = new ScsParamSet();

		// Set some typical parameter values.
		p.setValue("RA", "12.0");
		p.setValue("DEC", "1.0");
		p.setValue("SR", "0.5");
		p.setValue("FROM", "messier");

		// Print out the edited SCS parameter set.
		System.out.println ("SCS: " + p);

	    } catch (DalServerException ex) {
		System.out.println ("DalServerException");
	    }

	} else if (args[0].equals("doc")) {
	    // Generate an HTML version of a SCS parameter set.
	    // Place the generated file into the dalserver/doc-files directory
	    // to have it included in the Javadoc.

	    String fname = "scs-params.html";
	    Object last = null;

	    try {
		// Create an SCS parameter set.
		ScsParamSet scs = new ScsParamSet();

		// Output the parameter set in HTML format.
		PrintWriter out = new PrintWriter(new FileWriter(fname));
		out.println("<HTML><HEAD>");
		out.println("<TITLE>SCS Parameters</TITLE>");
		out.println("</HEAD><BODY>");
		out.println("<TABLE width=700 align=center>");
		out.println("<TR><TD " +
		    "colspan=3 align=center bgcolor=\"LightGray\">" +
		    "SCS Parameters" + "</TD></TR>");

		for (Iterator i = scs.iterator();  i.hasNext();  ) {
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
		System.out.println ("cannot create SCS parameter set");
	    } catch (IOException ex) {
		System.out.println ("cannot write file " + fname);
	    }
	}
    }
}
