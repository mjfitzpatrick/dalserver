/*
 * TableGroup.java
 * $ID*
 */

package dalserver;

import cds.savot.model.*;

/**
 * The TableGroup class is used to represent the Group construct of a table,
 * that is, a construct which associates several Param, Field, or Group
 * elements of a table as part of a higher level logical group or
 * association.
 *
 *
 * @version	1.0, 11-Dec-2006
 * @author	Doug Tody
 */
public class TableGroup extends SavotGroup {
    private String groupId;
    private String hint;

    /** Constructor to set the most common fields. */
    public TableGroup(String name, String id, String gid,
	String utype, String ucd, String description) {

	super();
	this.setName(name);
	this.setId(id);
	groupId = gid;
	this.setUtype(utype);
	this.setUcd(ucd);
	this.setDescription(description);
    }

    /**
     * Contruct a SavotGroup instance with the correct name space set
     * for the UTYPE tag.  The UTYPE name space prefix is used only in
     * XML contexts such as VOTable.
     *
     * @param	ns	XML namespace prefix to be used.
     */
    public SavotGroup newSavotGroup(String ns) {
	SavotGroup p = new SavotGroup();

	p.setId(this.getId());
	p.setRef(this.getRef());
	p.setName(this.getName());
	p.setUcd(this.getUcd());

	String utype = this.getUtype();
	if (utype != null && utype.length() > 0)
	    p.setUtype((ns != null ? ns + ":" : "") + utype);

	p.setDescription(this.getDescription());
	p.setFieldsRef(this.getFieldsRef());
	p.setParams(this.getParams());
	p.setParamsRef(this.getParamsRef());
	p.setGroups(this.getGroups());

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
}
