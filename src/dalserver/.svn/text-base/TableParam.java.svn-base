/*
 * TableParam.java
 * $ID*
 */

package dalserver;

import java.util.*;
import cds.savot.model.*;

/**
 * The TableParam class is used to represent a global parameter in a table,
 * that is, a parameter which applies to the entire table.  A Param is
 * equivalent to a Field with the addition of a value string; in some cases
 * within the DALServer code, both Fields and Params may be stored as
 * Param objects.  TableParams may have vector as well as scalar values.
 *
 * @version	1.0, 11-Dec-2006
 * @author	Doug Tody
 */
public class TableParam extends SavotParam {
    /** The groupID string, if the field belongs to a group. */
    private String groupId;

    /** Index of the field into a table row. */
    private int index;

    /** FITS keyword equivalent. */
    private String fitsKeyword;

    /** CSV keyword and label equivalent. */
    private String csvKeyword;
    
    /** Tag used for finer indication of the usage of a parameter. */
    private String hint;

    /**
     * Storage for an array-valued parameter.  This is provided for more
     * efficient use within DALServer and does not use the Savot mechanism,
     * which is a simple long string concatenating all the values, which is
     * set with the setValue method.  To get array-valued output in VOTable
     * output the Savot setValue method should be used, whether or not the
     * array mechanism is used as well.
     */
    private ArrayList<String> arrayData;

    /** Null constructor. */
    public TableParam() { }

    /** Constructor to set the most common fields. */
    public TableParam(String name, String value, String id, String gid,
	String datatype, String size, String unit, String utype, String ucd,
	String description) {

	super();
	this.setName(name);
	this.setValue(value);
	this.setId(id);
	this.setDataType(datatype);
	this.setArraySize(size);
	this.setUnit(unit);
	this.setUtype(utype);
	this.setUcd(ucd);
	this.setDescription(description);

	this.groupId = gid;
    }

    /**
     * Contruct a SavotParam instance with the correct name space set
     * for the UTYPE tag.  The UTYPE name space prefix is used only in
     * XML contexts such as VOTable.
     *
     * @param	ns	XML namespace prefix to be used.
     */
    public SavotParam newSavotParam(String ns) {
	SavotParam p = new SavotParam();

	p.setId(this.getId());
	p.setUnit(this.getUnit());
	p.setDataType(this.getDataType());
	p.setPrecision(this.getPrecision());
	p.setWidth(this.getWidth());
	p.setRef(this.getRef());
	p.setName(this.getName());
	p.setUcd(this.getUcd());

	String utype = this.getUtype();
	if (utype != null && utype.length() > 0)
	    p.setUtype((ns != null ? ns + ":" : "") + utype);

	p.setArraySize(this.getArraySize());
	p.setValue(this.getValue());
	p.setDescription(this.getDescription());
	p.setValues(this.getValues());
	p.setLinks(this.getLinks());

	return (p);
    }

    /** Set the group ID string. */
    public void setGroupId(String id) {
	groupId = id;
    }

    /** Get the group ID string. */
    public String getGroupId() {
	return (groupId);
    }

    /**
     * Set the array data element of the parameter.  The entire array element
     * is stored; use the ArrayList API directly to manipulate the array.
     *
     * @param	arrayData	The array object, which is an array of
     *				strings (binary data is not supported).
     *
     * @param	updateValue	If set to true, the string value of the
     *				parameter and the associated ArraySize
     *				attribute are updated as well.  For an array
     *				valued parameter the parameter value will be
     *				a long string concatenating all the array
     *				elements.
     */
    public void setArrayData(ArrayList<String> arrayData, boolean updateValue) {
	this.arrayData = arrayData;
	this.setValue(arrayData.toString());
	this.setArraySize(new Integer(arrayData.size()).toString());
    }

    /** Get the array data element of the parameter. */
    public List getArrayData() {
	return (this.arrayData);
    }

    /** Set the index of the param in a table record. */
    public void setIndex(int index) {
	this.index = index;
    }

    /** Get the index of the param in a table record. */
    public int getIndex() {
	return (index);
    }

    /** Set the Hint flag. */
    public void setHint(String hint) {
	this.hint = hint;
    }

    /** Get the Hint tag. */
    public String getHint() {
	return (this.hint);
    }

    /** Check the Hint field for the presence of specific characters. */
    public boolean hintContains(String chars) {
	if (hint != null)
	    for (char c : chars.toCharArray()) {
		for (int i=0;  i < hint.length();  i++)
		    if (hint.charAt(i) == c)
			return (true);
	    }

	return (false);
    }

    /** Set the FITS keyword name. */
    public void setFitsKeyword(String fitsKeyword) {
	this.fitsKeyword = fitsKeyword;
    }

    /** Get the FITS keyword name. */
    public String getFitsKeyword() {
	return (this.fitsKeyword);
    }

    /** Set the CSV keyword name. */
    public void setCsvKeyword(String csvKeyword) {
	this.csvKeyword = csvKeyword;
    }

    /** Get the CSV keyword name. */
    public String getCsvKeyword() {
	return (this.csvKeyword);
    }
}
