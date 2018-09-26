/*
 * DalServerException.java
 * $ID*
 */

package dalserver;

/**
 * Indicates a generic error occuring during exection of the DAL service.
 */
public class DalServerException extends Exception {
    private static final long serialVersionUID = 1;

    public DalServerException() { }
    public DalServerException(String s) { super(s); }
}
