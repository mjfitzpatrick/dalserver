/*
 * DalOverflowException.java
 * $ID*
 */

package dalserver;

/**
 * Thrown when overflow of the result table occurs.
 */
public class DalOverflowException extends Exception {
    private static final long serialVersionUID = 1;

    public DalOverflowException() { }
    public DalOverflowException(String s) { super(s); }
}
