/*
 * ConfigTable.java
 * $ID*
 */

package dalserver;

import java.util.*;
import java.io.*;

/**
 * The ConfigTable class implements a dynamic table configuration mechanism
 * used to customize the contents of the table output produced by service
 * queries.
 *
 * The ConfigTable class provides a means to customize the output of a service
 * request on a per-service-instance basis.  By default services output a
 * standard set of most-commonly used metadata.  Fields that are not assigned
 * values are output anyway with a null value, which is harmless.  Table
 * configuration allows unused fields to be omitted, additional standard
 * or custom metadata to be added, or field attributes to be customized.
 * The default field attributes for standard fields are defined by the
 * data model type and version used by the runtime protocol.  Values may
 * be specified at the ConfigField level to override the standard
 * data model field attributes, or to supply attribute values for a
 * custom field.
 *
 * Table config metadata is provided as part of the custom configuration for
 * a service, in a ".tab" file (optional).  Configuration is on a per-service
 * basis.  Table configuration information is passed into the service at
 * runtime servlet context initialization parameters, included in the input
 * parameter set for the servlet.
 *
 * @version	1.0, 13-Apr-2014
 * @author	Doug Tody
 */
public class ConfigTable implements Iterable {
    // -------- Class Data -----------

    /** Hash table containing the table fields. */
    private LinkedHashMap<String,ConfigField> fields;

    /** Keyword factory for this table. */
    private KeywordFactory kwfactory;

    /**
     * Keyword mask.  If a mask value is set, then keywords are hidden
     * (omitted) unless they have a "hint" value in the keyword table
     * containing one of the characters in the mask.
     */
    private String mask = null;


    // -------- Constructors -----------

    /**
     * Create a ConfigTable instance from the table config-related parameters
     * in the servlet input parameter set.
     *
     * @param	params		A parameter set containing table-config params
     * @param	kwfactory	Data model keyword factory to be used
     *
     * The input parameter set is normally the main input parameter set for
     * the calling servlet, but in principle it could be any parameter set
     * containing table config parameters.  The table config parameters are
     * passed as a subgroup named "table.{standard|custom}.<param-name>"
     * within the input parameter set.
     */
    public
    ConfigTable(ParamSet params, KeywordFactory kwfactory)
	throws DalServerException {

	this.fields = new LinkedHashMap<String,ConfigField>();
	this.kwfactory = kwfactory;

	// Scan and process the input parameter set.
	for (Iterator ii = params.entrySet().iterator();  ii.hasNext();  ) {
            Map.Entry me = (Map.Entry) ii.next();
            Param p = (Param) me.getValue();
	    String pname, fullPname = p.getName();

	    // Quickly skip all non-table params.
	    if (!fullPname.startsWith("table."))
		continue;

	    String s_standard = "table.standard.";
	    String s_custom = "table.custom.";
	    boolean custom = false;

	    if (fullPname.startsWith(s_standard)) {
		pname = fullPname.substring(s_standard.length());
	    } else if (fullPname.startsWith(s_custom)) {
		pname = fullPname.substring(s_custom.length());
		custom = true;
	    } else
		continue;

	    // Add the new field.
	    ConfigField field = new ConfigField(pname, custom);
	    fields.put(pname.toLowerCase(), field);

	    // Check if the parameter is to be omitted.
	    String pvalue = p.stringValue();
	    if (pvalue.trim().equals("omit")) {
		field.setOmit(true);
		continue;
	    }

	    // Process and set any optional field attributes.
	    StreamTokenizer in =
		new StreamTokenizer(new StringReader(pvalue));
	    String p_opt = "Malformed options for param= ";
	    int type;

	    in.resetSyntax();
	    in.whitespaceChars(0x00, 0x20);
	    in.wordChars(0x21, 0x7e);
	    in.quoteChar('"');
	    in.ordinaryChar('=');
	    in.ordinaryChar(',');

	    try {
		while ((type = in.nextToken()) != StreamTokenizer.TT_EOF) {
		    if (type == ',')
			continue;

		    // Process a new option=value construct.
		    if (type != StreamTokenizer.TT_WORD)
			throw new DalServerException(p_opt + pname);

		    String attr = in.sval;
		    String value = null;

		    type = in.nextToken();
		    if (type != '=')
			throw new DalServerException(p_opt + pname);

		    switch (type = in.nextToken()) {
		    case StreamTokenizer.TT_WORD:
		    case '"':
			value = in.sval;
			break;
		    case StreamTokenizer.TT_NUMBER:
			Double v = new Double(in.nval);
			value = v.toString();
			break;
		    default:
			throw new DalServerException(p_opt + pname);
		    }

		    // The longer attribute names may be abbreviated
		    // to as few as 3 characters.  Attribute names must
		    // be lower case.

		    if (attr.equals("id"))
			field.id = value;
		    else if (attr.equals("name"))
			field.name = value;
		    else if (attr.equals("unit"))
			field.unit = value;
		    else if (attr.equals("ucd"))
			field.ucd = value;
		    else if (attr.equals("utype"))
			field.utype = value;
		    else if (attr.equals("xtype"))
			field.xtype = value;
		    else if (attr.regionMatches(0, "description", 0, 3))
			field.setDescription(value);
		    else if (attr.regionMatches(0, "colname", 0, 3))
			field.setColname(value);
		    else if (attr.regionMatches(0, "datatype", 0, 3))
			field.datatype = value;
		    else if (attr.regionMatches(0, "arraysize", 0, 3))
			field.arraysize = value;
		    else if (attr.regionMatches(0, "width", 0, 3))
			field.width = value;
		    else if (attr.regionMatches(0, "precision", 0, 3))
			field.precision = value;
		}
	    } catch (IOException ex) {
		throw new DalServerException(p_opt + pname);
	    }
	}
    }


    // -------- Class Methods -----------

    /**
     * Set the mask value.
     *
     * @param	mask	Keyword mask.
     */
    public void setMask(String mask) {
	this.mask = mask;
    }

    /**
     * Get the named ConfigField from the ConfigTable instance.
     *
     * @param	name	The name of the field
     *
     * Null is returned if the field is not found.
     */
    public ConfigField getField(String name) {
	return (fields.get(name.toLowerCase()));
    }

    /**
     * Get the table column name for the named ConfigField.
     *
     * @param	name	The name of the field
     *
     * If no custom table column name has been assigned then the
     * ConfigField name is returned.  If the field is not found
     * then null is returned.
     */
    public String getColname(String name) {
	ConfigField field = fields.get(name.toLowerCase());
	if (field == null)
	    return (null);

	String colname = field.colname();
	return ((colname == null) ? name : colname);
    }

    /**
     * Conditionally add a new Group to a Response object.
     *
     * @param	response	A RequestResponse instance
     * @param	group		A TableGroup instance
     *
     * In the current implementation addGroup requests are always passed
     * on to the RequestResponse class since we have no easy way to check
     * for an empty Group; if needed this check can be made higher up.
     */
    public void addGroup (RequestResponse response, TableGroup group) {
	response.addGroup(group);
    }

    /**
     * Conditionally add a new Field to a Response object.
     *
     * @param	response	A RequestResponse instance
     * @param	field		A TableField instance
     *
     * The Field instance is added unless it is flagged to be omitted
     * in the ConfigTable.  Any custom attributes are propagated to
     * the Field instance.
     */
    public void addField (RequestResponse response, TableField field) {
	ConfigField cf = this.getField(field.getName());

	// If a mask value is set, the default mask action depends only
	// on the hint flags set for the keyword in the keyword dictionary.
	// This may be overriden however by an explicit entry for the
	// keyword in the table configuration.

	if (mask != null && !field.hintContains(mask)) {
	    if (cf == null || cf.omit())
		return;
	}

	if (cf == null) {
	    response.addField(field);
	} else if (cf.omit()) {
	    ;
	} else {
	    if (cf.id != null)
		field.setId(cf.id);
	    if (cf.name != null)
		field.setName(cf.name);
	    if (cf.datatype != null)
		field.setDataType(cf.datatype);
	    if (cf.arraysize != null)
		field.setArraySize(cf.arraysize);
	    if (cf.width != null)
		field.setWidth(cf.width);
	    if (cf.precision != null)
		field.setPrecision(cf.precision);
	    if (cf.xtype != null)
		field.setXtype(cf.xtype);
	    if (cf.unit != null)
		field.setUnit(cf.unit);
	    if (cf.ucd != null)
		field.setUcd(cf.ucd);
	    if (cf.utype != null)
		field.setUtype(cf.utype);

	    response.addField(field);
	}
    }

    /**
     * Conditionally add a new Param to a Response object.
     *
     * @param	response	A RequestResponse instance
     * @param	param		A TableParam instance
     *
     * The Param instance is added unless it is flagged to be omitted
     * in the ConfigTable.  Note at the level of ConfigTable we do not
     * distinguish between FIELDs and PARAMs.  Any custom attributes are
     * propagated to the created Param instance.
     */
    public void addParam (RequestResponse response, TableParam param) {
	ConfigField cf = this.getField(param.getName());

	// Apply the keyword mask as for Field above.
	if (mask != null && !param.hintContains(mask)) {
	    if (cf == null || cf.omit())
		return;
	}

	if (cf == null) {
	    response.addParam(param);
	} else if (cf.omit()) {
	    ;
	} else {
	    if (cf.id != null)
		param.setId(cf.id);
	    if (cf.name != null)
		param.setName(cf.name);
	    if (cf.datatype != null)
		param.setDataType(cf.datatype);
	    if (cf.arraysize != null)
		param.setArraySize(cf.arraysize);
	    if (cf.width != null)
		param.setWidth(cf.width);
	    if (cf.precision != null)
		param.setPrecision(cf.precision);
	    if (cf.xtype != null)
		param.setXtype(cf.xtype);
	    if (cf.unit != null)
		param.setUnit(cf.unit);
	    if (cf.ucd != null)
		param.setUcd(cf.ucd);
	    if (cf.utype != null)
		param.setUtype(cf.utype);

	    response.addParam(param);
	}
    }

    /**
     * Add any remaining standard fields not explicitly added to the output
     * Response object.
     *
     * @param	response	A RequestResponse instance
     *
     * For each standard Field in the ConfigTable, check to see if it has
     * already been added to the output Response object, and if not, create
     * an instance of the keyword from the given data model keyword factory,
     * and add it to the Response object.  Note that ConfigTable only
     * deals with table Fields (not constant-valued Params) since it is used
     * to map data from a DBMS table into the output Response table.
     *
     * The DM keyword factory keeps track of which keywords are in which
     * data model group, and the Response object sorts out the groups when
     * the table is eventually output, so we can add keyword instances
     * (fields) in any order.  The keywords added here are likely to be
     * added long after the first block of keywords were added to the group
     * to which the keyword belongs.
     */
    public int addStandardFields (RequestResponse response)
	throws DalServerException {

	int nfields = 0;

	for (Iterator ii = fields.entrySet().iterator();  ii.hasNext();  ) {
            Map.Entry me = (Map.Entry) ii.next();
            ConfigField cf = (ConfigField) me.getValue();

	    // We only want standard DM fields here.
	    if (cf.isCustom() || cf.omit())
		continue;

	    // Skip the field if it has already been added to the Response.
	    TableField tf = response.getField(cf.getName());
	    if (tf != null)
		continue;

	    // Add the named field.
	    tf = this.kwfactory.newField(cf.getName());
	    this.addField(response, tf);
	    nfields++;
	}

	return (nfields);
    }

    /**
     * Add any custom fields, not already explicitly added, to the output
     * Response object.
     *
     * @param	response	A RequestResponse instance
     *
     * For each custom Field in the ConfigTable, check to see if it has
     * already been added to the output Response object, and if not, create
     * a generic TableField for the ConfigField and add it to the Response
     * object.
     */
    public int addCustomFields (RequestResponse response)
	throws DalServerException {

	int nfields = 0;

	for (Iterator ii = fields.entrySet().iterator();  ii.hasNext();  ) {
            Map.Entry me = (Map.Entry) ii.next();
            ConfigField cf = (ConfigField) me.getValue();

	    // We only want custom data-provider fields here.
	    if (!cf.isCustom() || cf.omit())
		continue;

	    // Skip the field if it has already been added to the Response.
	    TableField tf = response.getField(cf.getName());
	    if (tf != null)
		continue;

	    // Add the named field.
	    tf = new TableField(cf.name, cf.id, null, cf.datatype,
		cf.arraysize, cf.unit, cf.utype, cf.ucd, cf.desc);

	    this.addField(response, tf);
	    nfields++;
	}

	return (nfields);
    }

    /** Get an iterator to access the ConfigTable as a list. */
    public Iterator iterator() {
        return ((Iterator) fields.entrySet().iterator());
    };

    /** Get an entrySet to access a ConfigTable as a Collection. */
    public Set entrySet() {
        return (fields.entrySet());
    };

    /** Get the number of fields in the ConfigTable. */
    public int size() {
        return (fields.entrySet().size());
    };
}
