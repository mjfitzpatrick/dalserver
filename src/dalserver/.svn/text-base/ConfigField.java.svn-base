/*
 * ConfigField.java
 * $ID*
 */

package dalserver;

import java.util.*;

/**
 * The ConfigField class represents a configurable table field and its
 * attributes. 
 *
 * Table fields provide a means to customize the output of a service request
 * on a per-service-instance basis.  By default services output a standard
 * set of most-commonly used metadata.  Fields that are not assigned values
 * are output anyway with a null value, which is harmless.  Table
 * configuration allows unused fields to be omitted, additional standard
 * or custom metadata to be added, or field attributes to be customized.
 *
 * The default field attributes for standard fields are defined by the
 * data model type and version used by the runtime protocol.  Values should
 * be specified at the ConfigField level only to override the standard
 * data model field attributes, or to supply attribute values for a
 * custom field.  For custom fields, ID defaults to the Field NAME, and
 * the datatype defaults to Char.
 *
 * @version	1.0, 13-Apr-2014
 * @author	Doug Tody
 */
public class ConfigField {
    // ---------- Class Data -------------------

    /** The Field name. */
    protected String fieldName;

    /** Flag to indicate custom or standard field. */
    protected boolean custom = false;

    /** Flag to indicate field is to be omitted. */
    protected boolean omit = false;

    /** Table column name if different than field name. */
    protected String colname = null;

    /** VO attributes (optional; pass through to output). */
    public String desc = null;
    public String id = null;
    public String name = null;
    public String datatype = null;
    public String arraysize = null;
    public String width = null;
    public String precision = null;
    public String xtype = null;
    public String unit = null;
    public String ucd = null;
    public String utype = null;


    // ---------- Constructors -------------------

    /**
     * Create a new Field instance.
     *
     * @param	name	The field name.
     * @param	custom	Custom or standard field
     */
    public ConfigField(String name, boolean custom) {
	this.fieldName = name;
	this.custom = custom;

	if (custom) {
	    this.id = name;
	    this.name = name;
	    this.datatype = "char";
	    this.arraysize = "*";
	}
    }

    // ---------- Class Methods -------------------

    /**
     * Get the field name.
     */
    public String getName() {
	return (this.fieldName);
    }

    /**
     * Set the field description.
     */
    public void setDescription(String desc) {
	this.desc = desc;
    }

    /**
     * Get the field description.
     */
    public String getDescription() {
	return (this.desc);
    }

    /**
     * Test if this is a custom or standard field.
     */
    public boolean isCustom() {
	return (this.custom);
    }

    /**
     * Set the omit property.
     */
    public void setOmit(boolean value) {
	this.omit = value;
    }

    /**
     * Test if a field is to be omitted.
     */
    public boolean omit() {
	return (this.omit);
    }

    /**
     * Set the colname property.
     */
    public void setColname(String value) {
	this.colname = value;
    }

    /**
     * Return the table column name associated with the given Field.
     * This defaults to the Field name if not set.
     */
    public String colname() {
	return ((this.colname == null) ? this.fieldName : this.colname);
    }
}
