/*
 * TaskManager.java
 * $ID*
 */

package dalserver;

import java.io.*;
import java.util.*;

/**
 * The TaskManager class provides a distributed asynchronous tasking
 * capability.  A TaskManager instance maintains open control connections
 * to one or more local or remote tasking daemons, that are used to run the
 * tasks.  A client uses the TaskManager to create a new Task instance,
 * set the task parameters, and then execute the task.  The task is physically
 * run by a tasking daemon instance running on some local or remote node.
 * Tasks execute asynchronously, streaming output back to the client during
 * execution.  Any number of tasks may execute simultaneously (subject to
 * a configurable maximum value).
 *
 * This implementation of tasking has some limitations compared to what is
 * planned.  The main limitation is in the parameter mechanism.  The input
 * parameters used to run a task are essentially just the Unix argv strings.
 * The task output is whatever the task writes to stdout (stderr goes to the
 * tasking daemon's stderr).  However, if the task formats its stdout as
 * a sequence of keyword=value lines, then this provides a basic output
 * parameter mechanism.  The tasking code will compile such output into a
 * keywordTable instance.  Output may be generated during task execution,
 * hence output parameters may be output during task execution to reflect
 * the state of the running task in near real-time.
 *
 * The TaskManager can manage multiple tasking daemons.  Each daemon is
 * assigned a name by the client when created.  The significance of the daemon
 * name is up to the client.  For example, it may represent a host or server
 * name used for some type of functionality, or it may represent the name of
 * a logical "package" of tasks.  How tasks are organized into packages and
 * deployed onto remote hosts or servers, is up to the client.
 *
 * The TaskManger does not currently have the capability to start tasking
 * daemons - rather it merely connects to an already running tasking
 * daemon.  Tasking is a capability of the back-end and should be up and
 * running before a servlet runs.  The servlet merely connects to one or
 * more running task daemons and can then run any number of tasks during
 * the servlet lifetime, which is often quite short.
 *
 * @version	1.0, 27-Apr-2014
 * @author	Doug Tody
 */
public class TaskManager {

    /** Named tasking daemon connnections. */
    private LinkedHashMap<String,TaskConnection> connections;

    /** Constructor to generate a new TaskManager instance. */
    public TaskManager() {
	this.connections = new LinkedHashMap<String,TaskConnection>();
    }

    /**
     * Connect to a running tasking daemon instance.  The daemon is
     * asigned an arbitrary name that may be used by the client to later
     * indicate the connection (service) to be used to run a task.
     *
     * @param	servname	The service name
     * @param	location	The connection information for the daemon
     *
     * Multiple calls to connect() may be made to connect multiple tasking
     * daemons.  The contents of "location" depend upon the mechanism
     * used to connect to and comunicate with the tasking daemon.
     * One tasking stream is required per executing task.  We only use
     * one at present.
     */
    public void connect (String servname, String location)
	throws DalServerException {

	TaskConnection conn = new TaskConnection (servname, location);
	connections.put(servname, conn);
    }

    /**
     * Verify that the given connection is still connected, and if not
     * attempt to reconnect.
     *
     * @param	servname	The service name
     */
    public void checkConnection (String servname)
	throws DalServerException {

	TaskConnection conn = connections.get(servname);
	TaskConnection newConn;

	if ((conn.sendCmd ("nconn", true)) == null) {
	    newConn = new TaskConnection (servname, conn.location());
	    conn.disconnect();
	    connections.remove(servname);
	    connections.put(servname, newConn);
	}
    }

    /**
     * Disconnect a tasking daemon.
     *
     * @param	servname	The service name
     *
     * The connection to the tasking daemon is closed, and the connection
     * descriptor is removed.
     */
    public void disconnect (String servname) throws DalServerException {
	TaskConnection conn = connections.get(servname);
	connections.remove(servname);
	conn.disconnect();
	conn = null;
    }

    /**
     * Close the task manager and all connections.
     */
    public void close() {
	for (Map.Entry<String,TaskConnection> entry : connections.entrySet()) {
	    TaskConnection conn = entry.getValue();
	    conn.disconnect();
	}

	this.connections = null;
    }

    /**
     * Get an existing named connection.
     *
     * @param	servname	The service name
     *
     * Lookup the named connection and return the connection descriptor.
     */
    public TaskConnection getConnection (String servname) {
	TaskConnection conn = connections.get(servname);
	return (conn);
    }

    /**
     * Create a new task instance associated with the named connection.
     *
     * @param	taskName	Task name
     * @param	servname	Service (tasking daemon) name
     *
     * A new task instance is created with the given task name.  The task
     * parameters may then be edited, and the task subsequently run.  Where
     * the task runs is determined at execution time.
     */
    public Task newTask (String taskName, String servname)
	throws DalServerException {

	TaskConnection service = connections.get(servname);
	return (new Task(taskName, service));
    }

    /**
     * Client tasking application for testing.
     */
    public static void main (String[] args)
	throws DalServerException {

	BufferedReader in =
	    new BufferedReader(new InputStreamReader(System.in));
	PrintStream out = System.out;
	String host = null;

	for (String arg : args) {
	    if (arg.equals("-h"))
		host = arg;
	    else {
		out.println("Unknown argument: " + arg);
		System.exit(1);
	    }
	}

	TaskManager taskman = new TaskManager();
	taskman.connect("localhost", null);
	String cmd;

	while (true) {
	    out.print("> "); out.flush();
	    try {
		cmd = in.readLine();
	    } catch (IOException ex) {
		break;
	    }

	    if ((cmd.trim()).equals(""))
		continue;
	    if (cmd.equals("quit") || cmd.equals("exit"))
		break;

	    String tok[] = cmd.split(" ");
	    String taskName = tok[0];

	    Task task = taskman.newTask(taskName, "localhost");
	    for (int i=1;  i < tok.length;  i++)
		task.addParam(tok[i]);

	    try {
		task.execute(out);
		task.waitForCompletion();
		task.close();
	    } catch (DalServerException ex) {
		out.println(ex.getMessage());
	    }
	}

	taskman.disconnect("localhost");
	System.exit(0);
    }
}
