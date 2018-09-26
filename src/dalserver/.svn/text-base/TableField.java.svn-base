/*
 * TableField.java
 * $ID*
 */

package dalserver;

import cds.savot.model.*;

/**
 * The TableField class is used to represent a field of a table (or a field
 * of a data model stored in a table).
 *
 * @version	1.0, 11-Dec-2006
 * @author	Doug Tody
 */
public class TableField extends SavotField {
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


    /** Null constructor. */
    public TableField() { }

    /** Constructor to set the most common fields. */
    public TableField(String name, String id, String gid, String datatype,
	String size, String unit, String utype, String ucd,
	String description) {

	super();
	this.setName(name);
	this.setId(id);
	this.setDataType(datatype);
	this.setArraySize(size);
	this.setUnit(unit);
	this.setUtype(utype);
	this.setUcd(ucd);
	this.setDescription(description);

	groupId = gid;
    }

    /**
     * Contruct a SavotField instance with the correct name space set
     * for the UTYPE tag.  The UTYPE name space prefix is used only in
     * XML contexts such as VOTable.
     *
     * @param	ns	XML namespace prefix to be used.
     */
    public SavotField newSavotField(String ns) {
	SavotField p = new SavotField();

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

    /** Set the index of the field in a table record. */
    public void setIndex(int index) {
	this.index = index;
    }

    /** Get the index of the field in a table record. */
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
