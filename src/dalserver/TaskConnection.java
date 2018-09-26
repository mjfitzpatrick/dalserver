/*
 * TaskConnection.java
 * $ID*
 */

package dalserver;

import java.io.*;
import java.util.*;
import java.net.Socket;

/**
 * The TaskConnection class provides a connection to a remote tasking daemon.
 *
 * The connections to a tasking daemon include one control stream per daemon,
 * and one tasking stream per task instance.  All streams are actually the
 * same to the daemon; they differ only in how they are used.
 *
 * @version	1.0, 27-Apr-2014
 * @author	Doug Tody
 */
public class TaskConnection {

    /** The logical name of the daemon. */
    String name;

    /** The network location of the daemon. */
    String location;

    /** IO socket for the daemon. */
    Socket socket;
    String serverAddr;
    int serverPort;

    /** Binary IO streams. */
    InputStream b_input;
    OutputStream b_output;

    /** Character IO streams. */
    BufferedReader input;
    PrintWriter output;


    /**
     * Constructor for a named connection.
     *
     * @param	name		The name of the tasking daemon
     * @param	location	The connection information for the daemon
     *
     * Connect to a running tasking daemon instance.  The daemon is
     * asigned a name that may be used by the client to later indicate
     * where a task should be run.  Multiple connections may be made to
     * connect multiple tasking daemons.  The contents of "location" depend
     * upon the mechanism used to connect to and comunicate with the tasking
     * daemon.
     */
    public TaskConnection(String name, String location)
	throws DalServerException {

	this.name = name;
	this.location = location;
	if (this.location == null)
	    this.location = "localhost:7464";

	// The only transport currently supported is sockets.
	String[] tok = this.location.split(":", 2);
	this.serverAddr = tok[0];
	this.serverPort = new Integer(tok[1]).intValue();

	try {
	    // Open the connection and binary i/o streams.
	    this.socket = new Socket(serverAddr, serverPort);
	    this.b_input = socket.getInputStream();
	    this.b_output = socket.getOutputStream();

	    // Character io for control stream.
	    this.input = new BufferedReader(new InputStreamReader(b_input));
	    this.output = new PrintWriter(b_output);

	} catch (IOException ex) {
	    throw new DalServerException(
		"TaskConnector cannot connect to " + this.location);
	}
    }

    /**
     * Constructor used to add a connection to an already connected daemon.
     */
    public TaskConnection(TaskConnection conn)
	throws DalServerException {

	this.name = conn.name;
	this.location = conn.location;
	this.serverAddr = conn.serverAddr;
	this.serverPort = conn.serverPort;

	try {
	    // Open the connection and binary i/o streams.
	    this.socket = new Socket(serverAddr, serverPort);
	    this.b_input = socket.getInputStream();
	    this.b_output = socket.getOutputStream();

	    // Character io for control stream.
	    this.input = new BufferedReader(new InputStreamReader(b_input));
	    this.output = new PrintWriter(b_output);

	} catch (IOException ex) {
	    throw new DalServerException(
		"TaskConnector cannot connect to " + location);
	}
    }

    /**
     * Disconnect a tasking daemon.
     */
    public void disconnect() {
	try {
	    output.println("close"); output.flush();
	    output.close();

	    this.output.close(); this.output = null;
	    this.input.close(); this.input = null;
	    this.socket.close(); this.socket = null;
	} catch (IOException ex) {
	    ;
	}
    }

    /** Get the connection name. */
    public String name() {
	return (this.name);
    }

    /** Get the connection location. */
    public String location() {
	return (this.location);
    }

    /** Get the input stream for the connection. */
    public BufferedReader input() {
	return (this.input);
    }

    /** Get the output stream for the connection. */
    public PrintWriter output() {
	return (this.output);
    }

    
    /**
     * Send a command to the tasking daemon, and return its status.
     *
     * @param	cmd		Command to be executed.
     * @param	roundtrip	Read the command status
     *
     * The command response is returned as a string, or null if an error
     * occurs.  If roundtrip=false only the command is sent, and a null
     * response is returned.
     */
    public String
    sendCmd (String cmd, boolean roundtrip) {

	// Send the command.
	output.println(cmd); output.flush();

	if (!roundtrip)
	    return (null);

	// Get the response.
	String response = null;
	String line;

	try {
	    while ((line=input.readLine()) != null) {
		if (line.startsWith("[ERR]")) {
		    response = null;
		    break;
		} else if (line.startsWith("[OK]")) {
		    response = line.substring(4).trim();
		    break;
		}
	    }
	} catch (IOException ex) {
	    response = null;
	}

	return (response);
    }

    /**
     * Send a command to the tasking daemon, and return an integer status.
     *
     * @param	cmd		Command to be executed.
     *
     * The available IO streams are "control" and "task".  The command
     * response is returned as a string, or null if an error occurs.
     */
    public int
    statCmd (String cmd) {
	String sval = sendCmd(cmd, true);
	if (sval == null)
	    return (-1);

	Integer ival = new Integer(sval);
	return (ival.intValue());
    }
}
