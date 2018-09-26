package dalserver.conf;

/**
 * an exception indicating that a dataset exhibited some illegal or inconsitant 
 * condition that prevent proper processing of its contents
 */
public class DataFormatException extends Exception {

    private String _dsname = null;

    /**
     * create the exception with a given message
     */
    public DataFormatException(String msg) { super(msg); }


    /**
     * create the exception with a given message for a given dataset
     * @param msg      a brief explanation of the problem
     * @param dsname   the name of (or path to) the dataset with the problem
     */
    public DataFormatException(String msg, String dsname) { 
        super(msg); 
        setDatasetName(dsname);
    }

    /**
     * wrap a more specific exception with a given message for a given dataset
     * @param msg      a brief explanation of the problem
     * @param cause    the underlying cause; an exception to wrap around.
     * @param dsname   the name of (or path to) the dataset with the problem
     */
    public DataFormatException(String msg, Throwable cause, String dsname) { 
        super(msg, cause);
        setDatasetName(dsname);
    }

    /**
     * wrap a more specific exception for a given dataset.  A default message
     * will be generated from the dataset name and the wrapped exception 
     * message.
     * @param cause    the underlying cause; an exception to wrap around.
     * @param dsname   the name of (or path to) the dataset with the problem
     */
    public DataFormatException(Throwable cause, String dsname) { 
        super("Problem processing "+dsname+": "+cause.getMessage(), cause);
        setDatasetName(dsname);
    }

    /**
     * wrap a more specific exception for an unspecified dataset.  
     * @param cause    the underlying cause; an exception to wrap around.
     */
    public DataFormatException(Throwable cause) { 
        super(cause);
    }

    /**
     * set the name of the dataset exhibiting the problem
     */
    public void setDatasetName(String name) { _dsname = name; }

    /**
     * return the name of the dataset exhibiting the problem.  If null,
     * the dataset is either unknown or has no applicable name (e.g. 
     * because it was read from a stream).  
     */
    public String getDatasetName() { return _dsname; }

}
