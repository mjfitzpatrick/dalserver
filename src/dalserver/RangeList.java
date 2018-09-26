/*
 * RangeList.java
 * $ID*
 */

package dalserver;

import java.util.*;
import java.io.*;


/**
 * The RangeList class implements a multi-element, numeric or string,
 * ordered or unordered range list object.
 *
 * @version	1.0, 29-Nov-2006
 * @author	Doug Tody
 */
public class RangeList implements Iterable<Range> {
    /** The number of ranges in the range list. */
    protected int length = 0;

    /** Tells if the range list is ordered. */
    public boolean ordered;

    /** Tells if the range list contains numbers. */
    public boolean numeric;

    /** Tells if the range list contains ISO 8601 date strings. */
    public boolean isoDate;

    /** Tells if the range list type has been fixed. */
    private boolean typeFixed;

    /** Hash table containing any global list properties. */
    LinkedHashMap<String,String> properties;

    /** An ordered range list is stored in a TreeSet. */
    TreeSet<Range> rlo;

    /** An unordered range list is stored in a LinkedList. */
    LinkedList<Range> rlu;


    // ------------ Constructors ----------------

    /** Create an empty ordered RangeList object. */
    public RangeList() {
	this.ordered = true;
	this.numeric = false;
	this.isoDate = false;
    }

    /**
     * Create an empty RangeList object of the specified type.
     *
     * @param type	The parameter type {@link dalserver.ParamType}.
     * @param ordered	Set to "true" to order the parsed range list.
     */
    public RangeList(ParamType type, boolean ordered)
	throws DalServerException {

	this.ordered = ordered;

	if (type != null) {
	    this.typeFixed = true;

	    switch (type) {
	    case BOOLEAN:
		throw new DalServerException(
		    "boolean range lists not supported");
	    case STRING:
		// Nothing to do, this is the default.
		this.numeric = false;
		this.isoDate = false;
		break;
	    case INTEGER:
	    case FLOAT:
		this.numeric = true;
		this.isoDate = false;
		break;
	    case ISODATE:
		this.numeric = false;
		this.isoDate = true;
		break;
	    }
	}
    }

    /**
     * Parse a range list string and produce a RangeList object.
     *
     * @param ranges	The encoded range-list string.
     * @param type	The base parameter type.
     * @param ordered	Set to true to order the parsed range list.
     */
    public RangeList(String ranges, ParamType type, boolean ordered)
	throws DalServerException {

	this(type, ordered);
	this.parseRangeList(ranges);
    }

    /**
     * Parse a range list string and produce an ordered RangeList object
     * wherein the range type defaults to String.
     *
     * @param ranges	The encoded range-list string.
     */
    public RangeList(String ranges) throws DalServerException {
	this.ordered = true;
	this.numeric = false;
	this.isoDate = false;
	this.typeFixed = false;

	this.parseRangeList(ranges);
    }


    // ------------ Methods ----------------

    /**
     * Parse a range list string and add the ranges therein to an existing
     * range list.  May be called multiple times to add successive groups
     * of ranges to a single compiled range list object.  In the case of a
     * nonnumeric range list, the string tokens may not contain any of the
     * delimiter metacharacters ',' ';' or '/'.   Returns a count of the
     * number of items (ranges or properties) processed.
     *
     * @param ranges	The encoded range-list string.
     * @throws DalServerException Numeric and nonnumeric are mixed together.
     */
    public int
    parseRangeList(String ranges) throws DalServerException {
	final String ex_mixed = "Types cannot be mixed in range list";

	StringTokenizer s = new StringTokenizer(ranges,
	    (typeFixed && !numeric && !isoDate) ? ",;" : ",;/=", true);
	boolean rangeDone = false, binary = false;
	boolean propertiesSeen = false, properties = false;
	String token, lastToken, s1 = null, s2 = null;
	int nitems = 0;
	Range r;

	while (s.hasMoreTokens()) {
	    // Process the next token.
	    token = s.nextToken();
	    if (token.equals(";")) {
		rangeDone = true;
		propertiesSeen = true;
	    } else if (token.equals(",")) {
		rangeDone = true;
	    } else if (token.equals("/")) {
		binary = true;		// binary includes isoDate here
	    } else if (properties && token.equals("=")) {
		binary = true;
	    } else if (!binary && s1 == null) {
		s1 = token;
	    } else if (s2 == null) {
		s2 = token;
	    } else
		rangeDone = true;

	    // Are we at the end of the string?
	    if (!s.hasMoreTokens())
		rangeDone = true;

	    // Process a completed range.
	    if (rangeDone && properties == false) {
		try {

		if ((binary || isoDate) && s1 != null && s2 != null)
		    r = new Range(RangeType.CLOSED, s1, s2, ordered, isoDate);
		else if (binary && s1 == null && s2 == null)
		    r = new Range(RangeType.ANY);
		else if (s1 == null && s2 == null)
		    { rangeDone = false; continue; }
		else if (binary && s1 != null)
		    r = new Range(RangeType.LOVAL, s1, isoDate);
		else if (binary && s2 != null)
		    r = new Range(RangeType.HIVAL, s2, isoDate);
		else
		    r = new Range(RangeType.ONEVAL, s1, isoDate);

		} catch (InvalidDateException ex) {
		    throw new DalServerException("invalid ISO date string");
		}

		// Set or verify whether the range list is numeric, nonumeric,
		// isoDate, or mixed.  A mixed mode is permitted for unordered
		// lists.

		if (r.rangeType != RangeType.ANY) {
		    if (typeFixed && ordered) {
			if (numeric != r.numeric || isoDate != r.isoDate)
			    throw new DalServerException(ex_mixed);
		    } else {
			numeric = r.numeric;
			isoDate = r.isoDate;
			typeFixed = true;
		    }
		}

		this.addRange(r);  nitems++;
		rangeDone = binary = false;
		s1 = s2 = null;
		r = null;
	    }

	    // Process a global range-list property.
	    if (rangeDone && properties == true) {
		if (s1 != null) {
		    this.addProperty(s1, s2);  nitems++;
		}

		rangeDone = binary = false;
		s1 = s2 = null;
	    }

	    if (propertiesSeen)
		properties = true;

	    lastToken = new String(token);
	}

	return (nitems);
    }

    /** Add a Range object to a RangeList. */
    public void addRange(Range r) {
	// Create the list if we haven't already.
	if (ordered && rlo == null)
	    rlo = new TreeSet<Range>();
	else if (!ordered && rlu == null)
	    rlu = new LinkedList<Range>();

	// Add the element.
	(ordered ? rlo : rlu).add(r);
	length++;
    }

    /** Add a global property to a RangeList. */
    public void addProperty(String key, String value) {
	// Create the property list if we haven't already.
	if (properties == null)
	    properties = new LinkedHashMap<String,String>();

	// Add the element.
	properties.put(key, (value == null) ? "true" : value);
    }

    /** Get a list iterator for the range list. */
    public Iterator<Range> iterator() {
	// Create the list if we haven't already.
	if (ordered && rlo == null)
	    rlo = new TreeSet<Range>();
	else if (!ordered && rlu == null)
	    rlu = new LinkedList<Range>();

	return ((ordered ? rlo : rlu).iterator());
    }

    /**
     * Convenience routine to get a single Range element.  This should
     * only be used for small values of the index, to avoid repeatedly
     * traversing the list (use an iterator to step through the entire
     * list).
     *
     * @param index	The list element to be returned, e.g., 0 or 1.
     *
     * @throws	DalServerException	Throws a DALServerException if the
     *			referenced range element does not exist.
     */
    public Range getRange(int index) throws DalServerException {
	Iterator<Range> it = this.iterator();

	Range r = null;
	for (int i=0;  i < index+1 && it.hasNext();  i++) {
	    r = it.next();
	}

	if (r == null)
	    throw new DalServerException("bad range index");

	return (r);
    }

    /**
     * Convenience routine to get the value of a a single-valued Range
     * element as a String.  Use only for simple single-valued lists.
     */
    public String stringValue (int index) throws DalServerException {
	return (getRange(index).stringValue1());
    }

    /**
     * Convenience routine to get the value of a a single-valued Range
     * element as a double.  Use only for simple single-valued lists.
     */
    public double doubleValue (int index) throws DalServerException {
	return (getRange(index).doubleValue1());
    }

    /**
     * Convenience routine to get the value of a a single-valued Range
     * element as an integer.  Use only for simple single-valued lists.
     */
    public int intValue (int index) throws DalServerException {
	return (getRange(index).intValue1());
    }

    /**
     * Convenience routine to get the value of a a single-valued Range
     * element as a Date object.  Use only for simple single-valued lists.
     */
    public java.util.Date dateValue (int index) throws DalServerException {
	return (getRange(index).dateValue1());
    }

    /** Get the number of range elements in a range list. */
    public int length() {
	return (this.length);
    }


    /**
     * Lookup a global property by keyword name.  Returns the keyword value,
     * or null if the keyword is not found or not defined.
     *
     * @param keyword	Keyword name of the global property to be returned.
     */
    public String getProperty(String keyword) {
	return ((properties != null) ? properties.get(keyword) : null);
    }

    /** Get a list iterator for the properties list. */
    public Iterator propertiesIterator() {
	// Create the property list if we haven't already.
	if (properties == null)
	    properties = new LinkedHashMap<String,String>();

	return (properties.entrySet().iterator());
    }

    /** Custom toString method to print out the RangeList. */
    public String toString() {
	StringBuilder result = new StringBuilder();
	final String newLine = System.getProperty("line.separator");

	result.append("{");
	result.append(this.getClass().getName());

	result.append("{");
	result.append("length=");
	result.append(length);
	result.append(" numeric=");
	result.append(numeric);
	result.append(" isoDate=");
	result.append(isoDate);
	result.append(" ordered=");
	result.append(ordered);
	result.append("} ");

	if (properties != null)
	    result.append(properties.toString());
	else
	    result.append("<empty>");
	result.append(" ");

	result.append("{");
	if (length > 0) {
	    for (Iterator<Range> i = this.iterator(); i.hasNext(); ) {
		result.append(i.next().toString());
		if (i.hasNext())
		    result.append(",");
	    }
	} else
	    result.append("<empty>");
	result.append("}");

	result.append("}");
	return (result.toString());
    }


    //////  PRIVATE  ///////

    /**
     * Exercise the RangeList class.
     */
    public static void main (String[] args) {
	try {
	    String r1_s = "a,e,b,cc,def,foo/bar,z;foo=bar";
	    RangeList r1 = new RangeList (r1_s);
	    System.out.println ("Range list: \"" + r1_s + "\"");
	    System.out.println ("  " + r1.toString());

	    String r2_root = "1,7,3/2,5/,,2.5/4,/9,5/9";
	    String r2_s = r2_root + ";source,frame=ICRS";
	    System.out.println ("Range list: " + r2_s);
	    RangeList r2 = new RangeList (r2_s);
	    System.out.println ("  " + r2.toString());

	    String r3_s = r2_root;
	    System.out.println ("Range list: \"" + r3_s + "\"");
	    RangeList r3 = new RangeList (r3_s, ParamType.FLOAT, false);
	    System.out.println ("  " + r3.toString());

	    String r4_s = "/;star";
	    System.out.println ("Range list: \"" + r4_s + "\"");
	    RangeList r4 = new RangeList (r4_s);
	    System.out.println ("  " + r4.toString());

	} catch (DalServerException ex) {
	    System.out.println ("failed with a DalServerException");
	}
    }
}
