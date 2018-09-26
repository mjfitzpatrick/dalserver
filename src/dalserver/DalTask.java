/*
 * DalTask.java
 * $ID*
 */

package dalserver;

import java.io.*;
import java.util.*;

/**
 * The DalTask class provides the capability to execute an external task or program,
 * to be executed as a separate process.
 *
 * Initially this is just a thin layer over the Java Runtime.exec and Process
 * classes, but it provides a place to build more sophisticated tasking facilities
 * later.  Only synchronous execution is provided initially.
 *
 * @version	1.0, 23-Sep-2013
 * @author	Doug Tody
 */
public class DalTask {
    private String executable;
    private Process process;
    private ArrayList list;
    private int status = 0;

    /** Constructor to generate a new task instance. */
    public DalTask(String executable) {
	this.executable = executable;
	this.list = new ArrayList();
	this.list.add(executable);
    }

    /**
     * Add a flag parameter (parameter with no value).
     *
     * @param	param	Parameter name (or any string token).
     */
    public void addParam(String param) {
	this.list.add(param);
    }

    /**
     * Add a parameter - value pair.
     *
     * @param	param	Parameter name (or any string token).
     * @param	value	Parameter value string.
     */
    public void addParam(String param, String value) {
	this.list.add(param);
	this.list.add(value);
    }

    /**
     * Execute the task, passing the parameters entered earlier.
     * Returns a BufferedReader input stream to read the stdout
     * of the task.
     */
    public BufferedReader execute() throws DalServerException {
	String[] cmdArray = (String[]) this.list.toArray(new String[0]);

	try {
	    Runtime rt = Runtime.getRuntime();
	    this.process = rt.exec(cmdArray, null);
	} catch (Exception ex) {
	    throw new DalServerException(ex.getMessage());
	}

	InputStream in = process.getInputStream();
	InputStreamReader ir = new InputStreamReader(in);

	return (new BufferedReader(ir));
    }

    /**
     * Get a BufferedReader input stream to read the stderr of the
     * already executing task.
     */
    public BufferedReader errorStream() {
	InputStream in = process.getErrorStream();
	InputStreamReader ir = new InputStreamReader(in);

	return (new BufferedReader(ir));
    }

    /**
     * Wait for task completion and return status.
     */
    public int waitForCompletion() {
	try {
	    this.status = process.waitFor();
	} catch (Exception ex) {
	    this.status = -1;
	}

	return (this.status);
    }

    /**
     * Get the process status.  This is zero for a running task,
     * and the process exit status for a completed task.
     */
    public int status() {
	return (this.status);
    }

    /**
     * Kill the task.
     */
    public void kill() {
	process.destroy();
	this.status = -1;
    }
}
