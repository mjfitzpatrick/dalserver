/*
 * ParamType.java
 * $ID*
 */

package dalserver;

/**
 * All defined parameter types.  The primary parameter types are string,
 * boolean, integer, and float.  These may be represented as single values,
 * or used in ordered (sorted) or unordered range lists.  In Java, the
 * {@link java.util.EnumSet} construct, of type ParamType, is used to
 * constuct complex parameter types such as range lists of a specific type.
 */
public enum ParamType {
    /** A raw, unparsed string value. */
    STRING,

    /**
     * A boolean value, parsed as true/false in Java.  Values may be
     * input as "true", "false", "yes", "no, "y", "n".  Case is not
     * significant.
     */
    BOOLEAN,

    /** A signed integer value (store as a long). */
    INTEGER,

    /** A floating point value (stored as a double). */
    FLOAT,

    /**
     * A time-date value formatted in ISO 8601 format.
     */
    ISODATE,

    /**
     * An ordered sequence (e.g. range list).  Ordered means that when
     * the sequence is parsed it should be sorted, and any range should
     * be ordered in order of increasing value (at this time, ordering
     * always implies a sort in ascending order).
     */
    ORDERED,

    /**
     * A range list.  The range list values may contain any type (string,
     * numeric, or isodate, but not boolean), but types cannot be mixed.
     * Ranges are expressed as "value1" "/" "value2", e.g., "3/5".  A range
     * which is open towards smaller values omits the first value (e.g.,
     * "/5"); a range which is open towards larger values omits the second
     * value (e.g., "3/").  If both values are omitted (e.g., "/") then any
     * value is acceptable.
     */
    RANGELIST,
}
