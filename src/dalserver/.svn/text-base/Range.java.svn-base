/*
 * Range.java
 * $ID*
 */

package dalserver;

import java.util.*;
import java.io.*;


/**
 * The Range class implements a single Range element, and is used to
 * contruct range lists.  Ranges may consist of a single value or two
 * values, maybe open or closed, and range values may be numeric or 
 * string (but the two cannot be mixed).
 *
 * @version	1.0, 28-Nov-2006
 * @author	Doug Tody
 */
public class Range implements Comparable<Range> {
    /** Tells whether the range is a single value, open, closed, etc.  */
    public RangeType rangeType;

    /** Tells whether a range contains numeric values. */
    public boolean numeric;

    /**
     * Tells whether a range contains ISO date values. Once an ISO
     * date value is parsed it is stored internally as an instance of
     * java.util.Date.
     */
    public boolean isoDate;

    // Private class data.
    private String value1_s, value2_s;
    private Double value1_d, value2_d, midval;
    private java.util.Date date1, date2;
    final String ex_nonnum = "nonnumeric range";


    // ----------- Constructors -------------

    /**
     * Create a new Range instance.  Input null for a parameter value
     * if it is not used.
     *
     * @param	type	The type of range.
     * @param	value1	For a ONEVAL, LOVAL, or HIVAL range, the single
     *			value, or for a CLOSED range, the first value.
     * @param	value2	The second value for a CLOSED range.
     * @param	order	Swap the two values if value2 < value1.
     * @param	isoDate	The range contains ISO date strings.
     */
    public Range (RangeType type, String value1, String value2,
	boolean order, boolean isoDate) throws InvalidDateException {

	this.rangeType = type;
	this.isoDate = isoDate;
	value1_s = value1;
	value2_s = value2;

	// ANY is a special case.
	if (type == RangeType.ANY) {
	    numeric = true;
	    return;
	}

	// Does the range contain ISO dates?
	if (isoDate) {
	    DateParser iso;
	    numeric = false;

	    iso = new DateParser();
	    date1 = iso.parse(value1_s);
	    if (value2_s != null) {
		iso = new DateParser();
		date2 = iso.parse(value2_s);
	    } else
		date2 = date1;

	    if (order && date1.after(date2)) {
		String temp_s = value1_s;
		value1_s = value2_s;
		value2_s = temp_s;

		java.util.Date temp_d = date1;
		date1 = date2;
		date2 = temp_d;
	    }

	    return;
	}

	// Check whether this is a numeric valued range.
	try {
	    value1_d = new Double(value1_s).doubleValue();
	    if (value2_s != null) {
		value2_d = new Double(value2_s).doubleValue();
		midval = (value1_d + value2_d) / 2;
	    } else {
		midval = value1_d;
		value2_d = value1_d;
	    }
	    numeric = true;
	} catch (NumberFormatException ex) {
	    numeric = false;
	}

	// If ordered list, make sure value1 <= value2.
	if (order && value2_s != null) {
	    if (numeric) {
		if (value1_d > value2_d) {
		    String temp_s = value1_s;
		    value1_s = value2_s;
		    value2_s = temp_s;

		    double temp_d = value1_d;
		    value1_d = value2_d;
		    value2_d = temp_d;
		}
	    } else if (value1_s.compareTo(value2_s) > 0) {
		String temp_s = value1_s;
		value1_s = value2_s;
		value2_s = temp_s;
	    }
	}
    }

    /** Simple constructor for an ordered range. */
    public Range (RangeType type, String value1, String value2)
	throws InvalidDateException {

	this (type, value1, value2, true, false);
    }

    /** Simple constructor for an ANY range. */
    public Range (RangeType type) throws InvalidDateException {
	this (type, null, null, false, false);
    }

    /** Simple constructor for a single-valued range. */
    public Range (RangeType type, String value1) throws InvalidDateException {
	this (type, value1, null, false, false);
    }

    /** Simple constructor for a single-valued possibly ISO date range. */
    public Range (RangeType type, String value1, boolean isoDate)
	throws InvalidDateException {

	this (type, value1, null, false, isoDate);
    }


    // ----------- Methods -------------

    /** Range value as a string. */
    public String stringValue1() { return (value1_s); }
    /** Range value as a string. */
    public String stringValue2() { return (value2_s); }

    /** Range value as an integer. */
    public int intValue1() throws NumberFormatException {
	if (numeric)
	    return (value1_d.intValue());
	else
	    throw (new NumberFormatException (ex_nonnum));
    }
    /** Range value as an integer. */
    public int intValue2() throws NumberFormatException {
	if (numeric)
	    return (value2_d.intValue());
	else
	    throw (new NumberFormatException (ex_nonnum));
    }

    /** Range value as a double. */
    public double doubleValue1() throws NumberFormatException {
	if (numeric)
	    return (value1_d.doubleValue());
	else
	    throw (new NumberFormatException (ex_nonnum));
    }
    /** Range value as a double. */
    public double doubleValue2() throws NumberFormatException {
	if (numeric)
	    return (value2_d.doubleValue());
	else
	    throw (new NumberFormatException (ex_nonnum));
    }

    /** Range value as a Date. */
    public java.util.Date dateValue1() { return (this.date1); }
    /** Range value as a Date. */
    public java.util.Date dateValue2() { return (this.date2); }


    /**
     * The compareTo method is needed to sort range lists.
     *
     * @param that Range instance to compare to.
     * @throws NullPointerException if "that" is null.
     */
    public int compareTo (Range that) {
	final int BEFORE = -1;
	final int EQUAL = 0;
	final int AFTER = 1;

	// Quick check for self-comparison.
	if (this == that)
	    return (EQUAL);

	// Cannot compare numeric and nonnumeric ranges.
	if (this.numeric != that.numeric)
	    throw new ClassCastException();

	// ANY (all values) can only be compared to another ANY.
	if (this.rangeType == RangeType.ANY || that.rangeType == RangeType.ANY)
	    if (this.rangeType == that.rangeType)
		return (EQUAL);
	    else
		throw new ClassCastException();

	if (this.numeric) {
	    // Compare two numeric range lists.

	    if (this.midval < that.midval)
		return (BEFORE);
	    if (this.midval > that.midval)
		return (AFTER);

	    // Midvals are equal.
	    switch (this.rangeType) {
	    case HIVAL:
		if (that.rangeType == RangeType.HIVAL)
		    return (EQUAL);
		else
		    return (BEFORE);
	    case LOVAL:
		if (that.rangeType == RangeType.LOVAL)
		    return (EQUAL);
		else
		    return (AFTER);
	    }

	    // Ranges have same midval but may still vary in width.
	    if (this.value1_d < that.value1_d)
		return (BEFORE);
	    if (this.value1_d > that.value1_d)
		return (AFTER);

	    return (EQUAL);

	} else {
	    // Compare two string valued ranges.
	    int compare_str1 = value1_s.compareTo(that.value1_s);

	    if (compare_str1 == EQUAL) {
		if (value2_s == null && that.value2_s == null)
		    return (EQUAL);
		if (value2_s != null && that.value2_s != null)
		    return (value2_s.compareTo(that.value2_s));
	    } else
		return (compare_str1);
	}

	return (EQUAL);
    }

    /**
     * Equality test for two ranges.
     */
    public boolean equals (Object obj) {
	Range that = (Range) obj;

    	// Simple cases.
	if (this == that)
	    return true;
	if (!(obj instanceof Range))
	    return false;

	// Special case ANY.
	if (this.rangeType == RangeType.ANY) {
	    if (that.rangeType == RangeType.ANY)
		return (true);
	    else
		return (false);
	} else if (that.rangeType == RangeType.ANY)
	    return (false);

	// RangeType does not match.
	if (this.rangeType != that.rangeType ||
	    this.numeric != that.numeric) {
	    return (false);
	}

	// Compare range values.
	if (this.numeric) {
	    if (!this.value1_d.equals(that.value1_d))
		return (false);
	    if (!this.value2_d.equals(that.value2_d))
		return (false);
	} else {
	    if (!this.value1_s.equals(that.value1_s))
		return (false);

	    if (this.value2_s == that.value2_s)
		return (true);
	    if (this.value2_s == null || that.value2_s == null)
		return (false);
	    if (!this.value2_s.equals(that.value2_s))
		return (false);
	}

	return (true);
    }

    /**
     * A class that overrides equals must also override hashCode.
     */
    public int hashCode() {
	int result = HashCodeUtil.SEED;

	result = HashCodeUtil.hash(result, rangeType);
	result = HashCodeUtil.hash(result, numeric);
	result = HashCodeUtil.hash(result, value1_s);
	if (value2_s != null)
	    result = HashCodeUtil.hash(result, value2_s);

	return (result);
    }

    /** Custom toString method to print out the Range. */
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("{");

	switch (this.rangeType) {
	    case ONEVAL:
		result.append(value1_s);
		break;
	    case LOVAL:
		result.append(value1_s + "/");
		break;
	    case HIVAL:
		result.append("/" + value1_s);
		break;
	    case CLOSED:
		result.append(value1_s + "/" + value2_s);
		break;
	    case ANY:
		result.append("/");
		break;
	    default:
		result.append("<badRange>");
	}

        result.append("}");
        return (result.toString());
    }


    ////  PRIVATE  ///////

    /**
     * Exercise compareTo.
     */
    public static void main (String[] args) {
	try {
	    Range a = new Range (RangeType.ONEVAL, "5");
	    Range b = new Range (RangeType.ONEVAL, "1");
	    Range c = new Range (RangeType.ONEVAL, "9");
	    Range d = new Range (RangeType.CLOSED, "3", "7");
	    Range e = new Range (RangeType.CLOSED, "7", "3");
	    Range f = new Range (RangeType.HIVAL,  "5");
	    Range g = new Range (RangeType.HIVAL,  "6");

	    System.out.println ("a: " + a.compareTo(a));
	    System.out.println ("b: " + b.compareTo(a));
	    System.out.println ("c: " + c.compareTo(a));
	    System.out.println ("d: " + d.compareTo(a));
	    System.out.println ("e: " + e.compareTo(a));
	    System.out.println ("f: " + f.compareTo(a));
	    System.out.println ("g: " + g.compareTo(a));

	    Range s1 = new Range (RangeType.ONEVAL, "foo");
	    Range s2 = new Range (RangeType.ONEVAL, "bar");
	    Range s3 = new Range (RangeType.CLOSED, "aaa", "eee");
	    Range s4 = new Range (RangeType.CLOSED, "aaa", "jjj");
	    Range s5 = new Range (RangeType.ONEVAL, "jjj");

	    System.out.println ("s1: " + s1.compareTo(s1));
	    System.out.println ("s2: " + s2.compareTo(s1));
	    System.out.println ("s3: " + s3.compareTo(s1));
	    System.out.println ("s4: " + s4.compareTo(s1));
	    System.out.println ("s5: " + s5.compareTo(s1));
	} catch (InvalidDateException ex) {
	    System.out.println ("invalid date exception");
	}
    }
}
