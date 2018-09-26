/*
 * ParamSet.java
 * $ID*
 */

package dalserver;

import java.util.*;
import java.io.*;

/**
 * The ParamSet class implements a parameter set mechanism oriented
 * toward service request parameters.  A parameter set is an object instance
 * containing an ordered set of parameters; a parameter has a name, type,
 * value, and other metadata.
 *
 * @version	1.1, 17-Mar-2015
 * @author	Doug Tody
 */
public class ParamSet implements Iterable<Param> {
    // -------- Class Data -----------

    /** Parameter set class. */
    private String psetClass=null;

    /** Parameter set instance identifier. */
    private String psetId=null;

    /** Hash table containing the parameter objects. */
    private LinkedHashMap<String,Param> params;


    // -------- Constructors -----------

    /** Create a new, empty generic parameter set. */
    protected ParamSet() {
	params = new LinkedHashMap<String,Param>();
    }

    /** Create a new, empty parameter set of a given class. */
    protected ParamSet(String psetClass) {
	params = new LinkedHashMap<String,Param>();
	this.psetClass = psetClass;
    }

    // Add additional constructors here to create a ParamSet from a
    // previously saved serialization, from an external schema, from a
    // "wired in" model (as we do here for SSAP), and so forth.

    // Subclass ParamSet and add a custom constructor to create a specific
    // type of parameter set, e.g., for an SSAP or SIAP service.


    // -------- Class Methods -----------

    /**
     * Query the parameter set class.
     */
    public String getPsetClass() {
	return (this.psetClass);
    }

    /**
     * Query the parameter set instance identifier.
     */
    public String getPsetId() {
	return (this.psetId);
    }

    /**
     * Set the parameter set instance identifier.
     */
    public void setPsetId(String psetId) {
	this.psetId = psetId;
    }

    /**
     * Add a parameter to a ParamSet.  The parameter set is ordered,
     * with newly added parameters added at the end of the list.  If a
     * parameter is added and an instance already exists, the existing
     * value is overwritten.
     *
     * @param	param	An object of type Param.
     */
    public void addParam(Param param) {
	params.put(param.name.toLowerCase(), param);
    }

    /**
     * Add a parameter to a ParamSet.
     *
     * @param	name	The string name of a parameter.
     * @param	value	The string value of the parameter.
     */
    public void addParam(String name, String value)
	throws DalServerException {

	Param p = params.get(name.toLowerCase());
	if (p == null)
	    p = new Param(name, value);
	else
	    p.setValue(value);

	params.put(p.name.toLowerCase(), p);
    }

    /**
     * Add a system parameter to a ParamSet.
     *
     * @param	name	The string name of a parameter.
     * @param	value	The string value of the parameter.
     *
     * System parameters are stored in the runtime pset, but are normally
     * hidden from the client.
     */
    public void addSysParam(String name, String value)
	throws DalServerException {

	Param p = params.get(name.toLowerCase());
	if (p == null)
	    p = new Param(name, value, ParamLevel.SERVICE);
	else
	    p.setValue(value);

	params.put(p.name.toLowerCase(), p);
    }

    /**
     * Set the value of a parameter, adding the parameter if necessary.
     *
     * @param	name	The string name of a parameter.
     * @param	value	The string value of the parameter.
     */
    public void setParam(String name, String value)
	throws DalServerException {

	Param p = params.get(name.toLowerCase());
	if (p == null)
	    p = new Param(name, value);
	else
	    p.setValue(value);

	params.put(p.name.toLowerCase(), p);
    }

    /** Lookup a parameter by name. */
    public Param getParam(String name) {
	return (params.get(name.toLowerCase()));
    }

    /**
     * Set or update the value of a parameter (convenience method).
     * Causes the value of the <i>isSet</i> attribute to be set to
     * true if the new value is non-null, false otherwise.  For finer
     * control the methods of the Param class should be called directly.
     *
     * @param	newValue	The new parameter value, as a String.
     */
    public void setValue(String name, String newValue)
	throws DalServerException {

	Param p = params.get(name.toLowerCase());
	if (p == null)
	    throw new DalServerException("param not found: " + name);

	p.setValue(newValue);
	p.isSet = (newValue != null);
    }

    /**
     * Get the value of a parameter as a String (convenience method).
     *
     * @param	name	The parameter name.
     *
     * @return		Returns the parameter value as a String, or null
     *			if the parameter is not found or has no value.
     */
    public String getValue(String name) {
	Param p = params.get(name.toLowerCase());
	if (p == null)
	    return (null);

	return (p.stringValue());
    }

    /**
     * Get the value of a parameter as a String (convenience method).
     *
     * @param	name	The parameter name.
     * @param	defval	The default value to return if the parameter is not
     *                  set.
     * @return		Returns the parameter value as a String, or null
     *			if no parameter value is set.
     */
    public String getValue(String name, String defval) {
	Param p = params.get(name.toLowerCase());
	if (p == null)
	    return (defval);

	String val = p.stringValue();
	return (val == null ? defval : val);
    }

    /**
     * Get the string value of a system parameter, adding the parameter
     * with a default value if not already defined.
     *
     * @param	name	The string name of a parameter.
     * @param	defval	The default value of the parameter.
     */
    public String getSysValue(String name, String defval) {

	Param p = params.get(name.toLowerCase());
	if (p == null)
	    try {
		addSysParam(name, defval);
		p = params.get(name.toLowerCase());
	    } catch (Exception ex) {
		p = null;
	    }

	return ((p == null) ? defval : getValue(name));
    }

    /**
     * return true if the named parameter is set 
     */
    public boolean isDefined(String name) {
        return (params.containsKey(name.toLowerCase()));
    }

    /** Get an iterator to access a ParamSet as a list. */
    public Iterator<Param> iterator() {
	// return ((Iterator<Param>) params.values().entrySet().iterator());
	return ((Iterator<Param>) params.values().iterator());
    };

    /** Get an entrySet to access a ParamSet as a Collection. */
    public Set entrySet() {
	return (params.entrySet());
    };

    /** Get the number of parameters in the list. */
    public int size() {
	return (params.entrySet().size());
    };

    // Save a parameter set to external storage.
    // ( load operation would be a constructor.)
    /** Write ParamSet to file.
     *  
     *  @param  filename            The String filename path
     *  
     *  @throws	DalServerException  Error writing file.
     */
    public void write(String filename) throws DalServerException {
	final String newLine = System.getProperty("line.separator");

        File file;
	BufferedWriter bw;
        String line;
        String sval;

        try {
            file = new File(filename);
            file.getParentFile().mkdirs();
            bw = new BufferedWriter(new FileWriter(filename));

            line = "#" + this.getClass().getSimpleName();
            line += newLine;
            bw.write(line, 0, line.length());

	    for (Param p : this) {
		sval = p.stringValue();
		if (sval == null) {
		    line = "".format("%s=null \"%s\"%s",
			p.getName(), p.getDescription(), newLine);
		} else {
		    line = "".format("%s=\"%s\" \"%s\"%s",
			p.getName(), p.stringValue(), p.getDescription(), newLine);
		}

		bw.write( line, 0, line.length() );
            }
            bw.close();
        } catch (Exception ex) {
            throw new DalServerException("problem writing file \'"+filename+"\'");
	}
    }

    /** Custom toString method to print out a parameter set. */
    public String toString() {
        StringBuilder result = new StringBuilder();
	final String newLine = System.getProperty("line.separator");
	int nparams = this.size();

	result.append("{");
	result.append(this.getClass().getName());
	result.append(" {");
	result.append("nparams=");
	result.append(nparams);
	result.append("}");

	for (Param p : this) {
	    result.append(newLine);
	    result.append(p.toString());
	}

	result.append(newLine);
        result.append("}");
	result.append(newLine);

        return (result.toString());
    }
}
