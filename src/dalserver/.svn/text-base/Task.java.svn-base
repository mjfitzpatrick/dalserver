/*
 * Task.java
 * $ID*
 */

package dalserver;

import java.io.*;
import java.util.*;

/**
 * The Task class is used to create, prepare, execute, and collect the output
 * from a task instance, executing the task via the tasking subsystem.
 *
 * Tasks are executed via connections to remote tasking services (long-running
 * tasking daemons), managed by a TaskManager.  A TaskManager maintains
 * open connections to one or more tasking services running on one or more
 * servers.  An open connection to a tasking service may be used to run any
 * number of tasks.  Tasks execute locally on the server that hosts the
 * tasking service.  Task execution is asynchronous.
 *
 * @version	1.0, 28-Apr-2014
 * @author	Doug Tody
 */
public class Task {

    /* Task attributes. */
    private String taskName;
    private ArrayList ipset;
    protected KeywordTable opset;
    private TaskConnection service;
    private TaskConnection conn;
    private Thread thread;
    protected String status;
    protected int exit_status;
    private int taskno;

    /* I/O control and tasking streams. */
    private BufferedReader c_in;
    private PrintWriter c_out;
    private BufferedReader t_in;
    private PrintWriter t_out;
    private PrintStream stdout;

    /**
     * Constructor to generate a new Task instance.
     *
     * @param	service		Connection to the service to be used
     *
     * A new Task instance is created for the service connected to the given
     * task control connection.  A second connnection is opened to run the
     * task on, and an empty input parmeter set is created.  The client should
     * add any input parameters to the input parameter set, after which the
     * task may be executed.
     */
    public Task (String taskName, TaskConnection service)
	throws DalServerException {

	// The task to be run.
	this.taskName = taskName;

	// Get a new connection to be used to run the task.
	this.conn = new TaskConnection(service);

	// Character io for control stream.
	this.c_in = service.input;
	this.c_out = service.output;

	// Character io for tasking stream.
	this.t_in = conn.input;
	this.t_out = conn.output;

	// Create an empty input parameter set.
	this.ipset = new ArrayList();
	this.status = null;
    }

    /**
     * Close a task and free all resources.  The service connection remains
     * open and is not affected, and may be used to run additional tasks.
     */
    public void close() {
	this.ipset = null; this.opset = null;
	this.conn.disconnect();
    }

    /**
     * Read and process the stdout of a task, generating an output parameter
     * set (keyword table) from the output.
     */
    private class TaskReader
	implements Runnable {

	/* Constructor allowing output stream to be set. */
	public TaskReader (PrintStream out) {
	    stdout = out;
	}

	public void run() {
	    StringBuilder sb = new StringBuilder();
	    String line;

	    try {
		while ((line=t_in.readLine()) != null) {
		    if (line.startsWith("[EOT]")) {
			// End of task indicated by the tasking daemon.
			// Format: [EOT] <taskno> <exit_status>
		
			String sval = line.substring(5).trim();
			String tok[] = sval.split(" ");
			Integer ival = new Integer(tok[1]);
			exit_status = ival.intValue();

			if (exit_status != 0)
			    status = "interrupted";
			else
			    status = "completed";

			break;

		    } else {
			// Accumulate the task output.  If an output stream
			// has been set, try to copy non-keyword lines out.
		
			if (stdout != null) {
			    String tok[] = line.split("=");
			    if (tok.length == 2) {
				sb.append(line);
				sb.append("\n");
			    } else
				stdout.println(line);

			} else {
			    sb.append(line);
			    sb.append("\n");
			}
		    }
		}
	    } catch (IOException ex) {
		;
	    }

	    // Process the accumulated task output into the output pset.
	    if (exit_status != 0)
		opset = null;
	    else {
		KeywordTable tab = new KeywordTable(sb.toString());
		opset = tab;
	    }
	}
    }

    /**
     * Execute the task, passing the parameters entered earlier.
     * The task executes on a separate thread, reading the task stdout
     * until the task terminates.  At task completion the task output
     * pset is available as a keyword table object.
     */
    public void execute(PrintStream out)
	throws DalServerException {

	// Create a thread to run the task.
	thread = new Thread(new TaskReader(out));

	// Start the task executing.
	String cmd = "exec " + taskName + " " + getArgs(ipset);
	this.taskno = conn.statCmd(cmd);
	if (this.taskno < 0)
	    throw new DalServerException(
	    "cannot execute task (" + taskName + ")");

	// Read and process the task stdout until task completion.
	this.status = "running";
	thread.start();
    }

    /** Execute method with no output stream. */
    public void execute() throws DalServerException {
	this.execute(null);
    }

    /**
     * Add a flag input parameter (parameter with no value).
     *
     * @param	param	Parameter name (or any string token).
     */
    public void addParam(String param) {
	this.ipset.add(param);
    }

    /**
     * Add an input parameter-value pair.
     *
     * @param	param	Parameter name (or any string token).
     * @param	value	Parameter value string.
     */
    public void addParam(String param, String value) {
	this.ipset.add(param);
	this.ipset.add(value);
    }

    /**
     * Convert the parameter set into a command line string.
     */
    public String getArgs(ArrayList params) {
	StringBuilder args = new StringBuilder();
	int i, nparams = params.size();

	for (i=0;  i < nparams;  i++) {
	    try {
		args.append(params.get(i));
		args.append(" ");
	    } catch (IndexOutOfBoundsException ex) {
		;
	    }
	}

	return (args.toString());
    }

    /**
     * Get the output parameter set.  The output pset is returned if the
     * task has completed, otherwise null is returned.
     */
    public KeywordTable getOutputPset() {
	return (this.opset);
    }

    /**
     * Wait for task completion and return status.
     */
    public int waitForCompletion() {
	try {
	    thread.join();
	} catch (InterruptedException ex) {
	    this.exit_status = 1;
	}

	return (this.exit_status);
    }

    /**
     * Get the process status.  This is the null string if the task has not
     * yet run, otherwise is one of "running", "completed" or "interrupted".
     */
    public String status() {
	return (this.status);
    }

    /**
     * Get the process exit status.  This is zero for a running task,
     * and the integer process exit status for a completed task.
     */
    public int exit_status() {
	return (this.exit_status);
    }

    /**
     * Kill the task.
     */
    public void kill() {
	if (this.status.equals("running"))
	    service.sendCmd("kill " + this.taskno, true);
    }
}
