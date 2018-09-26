/*
 * DbColumn.java
 * $ID*
 */

package dalserver;

import java.util.*;

/**
 * The DbColumn (table column) class is used to store metadata describing
 * a column of a database table.
 *

Column metadata
  get/set metadata elements?



 * @version	1.0, 1-Nov-2009
 * @author	Doug Tody
 */
public class DbColumn {
    // -------- Class Data -----------

    /** The parameter name. */
    protected String name;

    /** The parameter type (enumeration). */
    protected EnumSet<ParamType> type;

    /**
     * The value of a String parameter, or null if no value has been set.
     * Clients should not change the value of a parameter directly; use
     * the setValue method instead.
     */
    protected String value;

    /** The level of a parameter, e.g., "core" (defined by the relevant
     * standard), "extension" (an extension defined or supported by the
     * service), or "client" (a parameter added by the client, which the
     * service does not recognize as belonging either to the core or
     * and extension)..
     */
    protected ParamLevel level = ParamLevel.CORE;

    /** Brief description of parameter. */
    protected String description;

    /**
     * Set to true if the parameter was set explicitly by the client
     * request, as opposed to a default value being set.
     */
    protected boolean isSet;

    // Cache parsed parameter values internally. */
    private boolean value_b;
    private RangeList rangeList;
    private java.util.Date date;


    // -------- Constructors -----------

    /**
     * Create a new DbColumn instance.
     *
     * @param	name	The parameter name.
     * @param	type	The parameter type (see ParamType).
     * @param	value	The parameter value, represented as a string.
     * @param	level	The parameter standardization level.
     * @param	isSet	Parameter was set explicitly by the client.
     * @param	descr	Short description of the parameter.
     */
    public DbColumn(String name, EnumSet<ParamType> type, String value,
	ParamLevel level, boolean isSet, String descr)
	throws DalServerException, InvalidDateException {

	final String errmsg = "Attempt to create param with null ";
	if (name == null)
	    throw new DalServerException(errmsg + "name");
	if (type == null)
	    throw new DalServerException(errmsg + "type" + "["+name+"]");
	if (level == null)
	    throw new DalServerException(errmsg + "level" + "["+name+"]");

	this.name = name;
	this.type = type;
	this.value = value;
	this.level = level;
	this.isSet = isSet;
	this.description = descr;

	this.setValue(value);
    }

    /**
     * Simple constructor to define a new CORE (standard) parameter.
     *
     * @param	name	The parameter name.
     * @param	type	The parameter type (see ParamType).
     * @param	descr	Short description of the parameter.
     *
     * This method is used to construct new, default parameter sets
     * containing only predefined standard parameters with the values
     * not yet set (except in the case of a default value).  Hence
     * value is null, level is CORE, and isSet is false.
     */
    public DbColumn(String name, EnumSet<ParamType> type, String descr)
	throws DalServerException, InvalidDateException {

	this (name, type, null, ParamLevel.CORE, false, descr);
    }

    /**
     * Simple constructor to define a new CORE parameter with a default
     * value.
     *
     * @param	name	The parameter name.
     * @param	type	The parameter type (see ParamType).
     * @param	value	The default value of the parameter.
     * @param	descr	Short description of the parameter.
     *
     * When a parameter is created with a default value, value will
     * be non-null, but isSet will be false to indicate that the value
     * was not explicitly set.
     */
    public DbColumn(String name, EnumSet<ParamType> type, String value,
	String descr) throws DalServerException, InvalidDateException {

	this (name, type, value, ParamLevel.CORE, false, descr);
    }

    /**
     * Simple constructor for a simple String-valued parameter.
     *
     * @param	name	The parameter name.
     * @param	value	The parameter value, represented as a string.
     *
     * This method is called when processing request parameters to add
     * a new client-specified parameter which does not match any predefined
     * standard or extension parameter.  Such parameters are always
     * string valued and there is no description string available.  Hence
     * type=String, level=CLIENT, isSet=true.
     */
    public DbColumn(String name, String value)
	throws DalServerException, InvalidDateException {

	this (name, EnumSet.of(ParamType.STRING), value, ParamLevel.CLIENT,
	    true, null);
    }


    // -------- Class Methods -----------

    /**
     * Set or change the value of a parameter.  
     *
     * @param newValue	The new value as a string, or null, to initialize
     *			the parameter value to an unset state.
     */
    public void setValue(String newValue)
	throws DalServerException, InvalidDateException {

	// Set the value as a string.
	this.value = newValue;

	// Extract the complex elements from the ParamType EnumSet.
	EnumSet<ParamType> paramType = this.type.clone();
	boolean isOrdered = false;
	boolean isRangeList = false;

	if (paramType.remove(ParamType.ORDERED))
	    isOrdered = true;
	if (paramType.remove(ParamType.RANGELIST))
	    isRangeList = true;

	ParamType baseType = null;
	for (Object o : paramType)
	    baseType = (ParamType) o;

	if (isRangeList) {
	    // Process a range list parameter.
	    if (value == null)
		rangeList = null;
	    else
		rangeList = new RangeList(newValue, baseType, isOrdered);

	} else {
	    // Process the primitive non-rangeList types.
	    switch (baseType) {
	    case BOOLEAN:
		// Accept any standard string value for a boolean.
		if (value != null) {
		    value_b = (value.equalsIgnoreCase("true") ||
			value.equalsIgnoreCase("yes") ||
			value.equalsIgnoreCase("y")) ? true : false;
		} else
		    value_b = false;

		// Update the string value.
		this.value = value_b ? "true" : "false";
		break;

	    case STRING:
	    case INTEGER:
	    case FLOAT:
		// Nothing other than the string value required here
		// at the moment.
		break;

	    case ISODATE:
		DateParser dp = new DateParser();
		this.date = dp.parse(value);
		break;

	    default:
		throw new DalServerException("param has no base type");
	    }
	}
    }

    /** Get the parameter name as a string. */
    public String getName() { return (this.name); }

    /** Get the parameter type.  */
    public EnumSet<ParamType> getType() { return (this.type); }

    /** Get the parameter level.  */
    public ParamLevel getLevel() { return (this.level); }

    /** Set the parameter level.  */
    public void setLevel(ParamLevel level) { this.level = level; }

    /** Parameter description as a string (may be null). */
    public String getDescription() { return (this.description); }

    /** The isSet flag is set to true of the parameter was explicitly set. */
    public boolean isSet() { return (this.isSet); }

    /** Parameter value as a string (may be null). */
    public String stringValue() { return (this.value); }

    /** Parameter value as a boolean. */
    public boolean booleanValue() {
	return ((this.value != null) ? this.value_b : false);
    }

    /** Parameter value as an integer. */
    public int intValue() throws NumberFormatException, NullPointerException {
	return (new Double(this.value).intValue());
    }

    /** Parameter value as a double. */
    public double
    doubleValue() throws NumberFormatException, NullPointerException {
	return (new Double(this.value).doubleValue());
    }

    /** Parameter value as a Date object. */
    public java.util.Date dateValue()  { return (this.date); }

    /** Parameter value as a range list. */
    public RangeList rangeListValue() { return (this.rangeList); }


    /** Custom toString method to print a parameter. */
    public String toString() {
	StringBuilder result = new StringBuilder();
	final String newLine = System.getProperty("line.separator");

	// Print the fields of the parameter itself.
	result.append("{");
	result.append(name + "=");
	if (value == null)
	    result.append("null");
	else
	    result.append("\"" + value + "\"");
	result.append(" \t");

	result.append("type=" + type + " ");
	result.append("level=" + level + " ");
	result.append("isSet=" + isSet + " \t");

	if (description == null)
	    result.append("descr=null");
	else
	    result.append("descr=\"" + description + "\"");
	result.append("}");

	// If the parameter value is a range list, print that too.
	if (rangeList != null) {
	    result.append(newLine);
	    result.append("  ");
	    result.append(rangeList.toString());
	}

	return (result.toString());
    }
}
