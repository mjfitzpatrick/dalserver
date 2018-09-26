/*
 * SlapParamSet.java
 * $ID*
 */

package dalserver.sla;

import dalserver.DalServerException;
import dalserver.RangeList;
import dalserver.Param;
import dalserver.ParamLevel;
import dalserver.ParamType;
import dalserver.ParamSet;

import java.util.EnumSet;
import java.util.Map;
import java.util.Iterator;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;

/**
 * Construct an initial default parameter set containing the parameters
 * for a SLAP service.  Currently this paramset contains the combined
 * parameters for all SLAP operations.  A list of the currently defined
 * parameters, including their name, type, and description, can be found
 * in <a href="doc-files/slap-params.html">this table</a>.
 * 
 * @version	1.0, 3-Dec-2009
 * @author	Doug Tody, Ray Plante
 */
public class SlapParamSet extends ParamSet {

    /** Create an initial default SLAP parameter set. */
    public SlapParamSet() throws DalServerException {
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

	// Define all core parameters defined by the SLAP standard.
	// This would be more flexibly done by reading an external
	// schema, but a wired in approach is simpler for now.

	// General protocol-level parameters.
	this.addParam(new Param("VERSION",     STR, "1.0", "SLAP protocol version"));
	this.addParam(new Param("REQUEST",     STR, "Operation to be performed"));

	// Parameters for the queryData operation.
	this.addParam(new Param("WAVELENGTH",  RFO, "Range of allowable line wavelengths (meters)"));
	this.addParam(new Param("CHEMICAL_ELEMENT",  RSU, "Desired chemical elements"));
	this.addParam(new Param("INITIAL_LEVEL_ENERGY",  RFO, "Energy range for initial level of transition (Joules)"));
	this.addParam(new Param("FINAL_LEVEL_ENERGY",  RFO, "Energy range for final level of transition (Joules)"));
	this.addParam(new Param("TEMPERATURE",  FLO, "Temperature of object (Kelvin)"));
	this.addParam(new Param("EINSTEIN_A",  RFO, "Range of allowable transition probabilities (s^-1)"));
	this.addParam(new Param("PROCESS_TYPE",  STR, "Physical process type"));
	this.addParam(new Param("PROCESS_NAME",  STR, "Physical process which generated this line"));

	// Define any service-defined extension parameters here.
	// Client-defined parameters can only be specified at runtime.

	this.addParam(new Param("FORMAT",      STR, "Desired output data format"));
	this.addParam(new Param("Maxrec",      INT, "Maximum number of output records"));
	this.addParam(new Param("Compress",    BOO, "Allow dataset compression"));
	this.addParam(new Param("RunID",       STR, "Runtime job ID string"));

	// Mark these as service defined params as the are not in SLAP V1.0.
	this.getParam("FORMAT").setLevel(ParamLevel.SERVICE);
	this.getParam("Maxrec").setLevel(ParamLevel.SERVICE);
	this.getParam("Compress").setLevel(ParamLevel.SERVICE);
	this.getParam("RunID").setLevel(ParamLevel.SERVICE);
    }

    // Exercise the parameter mechanism.
    public static void main (String[] args) {
	if (args.length == 0 || args[0].equals("test")) {
	    try {
		// Create a new, default SLAP queryData parameter set.
		SlapParamSet p = new SlapParamSet();

		// Set some typical parameter values.
		p.setValue("WAVELENGTH", "5.1E-6/5.6E-6");

		// Print out the edited SLAP parameter set.
		System.out.println ("SLAP: " + p);

	    } catch (DalServerException ex) {
		System.out.println ("DalServerException");
	    }

	} else if (args[0].equals("doc")) {
	    // Generate an HTML version of a SLAP parameter set.
	    // Place the generated file into the dalserver/doc-files directory
	    // to have it included in the Javadoc.

	    String fname = "slap-params.html";
	    Object last = null;

	    try {
		// Create an SLAP parameter set.
		SlapParamSet slap = new SlapParamSet();

		// Output the parameter set in HTML format.
		PrintWriter out = new PrintWriter(new FileWriter(fname));
		out.println("<HTML><HEAD>");
		out.println("<TITLE>SLAP Parameters</TITLE>");
		out.println("</HEAD><BODY>");
		out.println("<TABLE width=700 align=center>");
		out.println("<TR><TD " +
		    "colspan=3 align=center bgcolor=\"LightGray\">" +
		    "SLAP Parameters" + "</TD></TR>");

		for (Iterator i = slap.iterator();  i.hasNext();  ) {
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
		System.out.println ("cannot create SLAP parameter set");
	    } catch (IOException ex) {
		System.out.println ("cannot write file " + fname);
	    }
	}
    }
}
