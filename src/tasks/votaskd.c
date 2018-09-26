/*
 * VOTASKD -- VO (DALServer) tasking daemon.
 *
 * This is a simplified initial version of the DALServer/VOClient tasking
 * subsystem, with limited support for things like parameter management
 * or job control.
 *
 * The tasking daemon runs on a server and responds to client requests to
 * execute tasks and manage task execution.  A "task" is anything that can
 * be executed via the exec system call, with arguments on the command line,
 * writing to stdout and stderr.  Tasks execute as subprocesses of the 
 * tasking daemon, with execution managed by the daemon.  During execution
 * the task stdout and stderr output streams are delivered directly to the
 * client application that requested execution of the task.
 *
 * A client connection to the tasking daemon is used to issue requests and
 * read back the response.  The same mechanism is used to execute tasks.
 * The client issues an execute-task request, and the "response" (after a
 * status preamble from the deamon) is the stdout of the task.  The stream
 * is reserved for use by the task stdout during the lifetime of the task,
 * after which the still open connection stream becomes available again for
 * other purposes.  If the client wishes to keep a control connection to the
 * tasking daemon open during task execution, then it would merely open a
 * second connection reserved for control requests.  If multiple tasks are
 * to be run simultaneously, each must execute on a separate connection.
 *
 * By default votaskd functions as a tasking daemon, executing and managing
 * asynchronous tasks on behalf of one or more clients.  A client execution
 * mode is also available that may be used for testing, or to submit tasks
 * to a tasking daemon for execution.
 */

#define _GNU_SOURCE
#include <stdio.h>

#include <sys/socket.h>
#include <netdb.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <stdlib.h>
#include <unistd.h>
#include <getopt.h>
#include <errno.h>
#include <string.h>
#include <signal.h>
#include <dirent.h>
#include <fcntl.h>
#include <sys/types.h>
#include <sys/time.h>
#include <sys/select.h>

#ifdef __linux__
#include <wait.h>
#endif

#define	MAX_CONN		64	/* Max client connections. */
#define MAX_TASKS		32	/* Max executing tasks */
#define MAX_DIRS		32	/* Max directores in search path */
#define MAX_ERRORS		8192	/* Used to avoid infinite loops */
#define	MAX_REQUEST		8192	/* Maximum request size */
#define MAX_ARGS		64	/* Max task arguments */
#define SZ_MSGBUF		1024	/* Message message buffer size */
#define SZ_KEYWORD		32	/* Request keyword name */
#define SZ_NAME			64	/* Task name, file name, etc. */
#define SZ_LINE			256	/* Line of text */
#define SZ_PATHNAME		1024	/* File pathname (eg task path) */
#define DEFPORT			7464	/* Default service port ('t'+'d') */
#define DEFHOST			"127.0.0.1";  /* Localhost */

#define	ARG_CLIENT		'c'
#define	ARG_DIR			'd'
#define	ARG_HOST		'h'
#define	ARG_PORT		'p'
#define	ARG_LOGFILE		'l'
#define	ARG_DAEMON		's'
#define	ARG_TASK		't'
#define ARG_VERBOSE		'v'
#define	ARG_MAXCLIENTS		11
#define	ARG_MAXTASKS		12

/* Task states. */
#define	TASK_INIT		1	/* Task being readied */
#define	TASK_RUNNING		2	/* Task is running */
#define	TASK_COMPLETED		3	/* Task has completed normally */
#define	TASK_INTERRUPTED	4	/* Task was interrupted by client */

/* Task descriptor.  */
typedef struct task_t {
    pid_t	pid;			/* Task process ID */
    int		conn;			/* Client connection number */
    int		connfd;			/* Connection FD of client */
    int		status;			/* Current status */
    int		exit_status;		/* Exit status at completion */
    char	taskName[SZ_NAME];	/* Task name */
    char	taskArgs[SZ_LINE];	/* Task arguments */
} task_t;

/* Task terminated descriptor. */
typedef struct taskDone_t {
    pid_t	pid;			/* PID of process which exited */
    int		status;			/* Status flags from waitpid */
} taskDone_t;

/* Daemon runtime state.  */
static int servport = DEFPORT;		/* Server port */
static char servhost[SZ_NAME];		/* Server IP address, in client mode */
static int maxconn = MAX_CONN;		/* Max client connections */
static int maxtasks = MAX_TASKS;	/* Max executing tasks */
static int connfd[MAX_CONN] = {0};	/* Active client connections */
static int connbusy[MAX_CONN] = {0};	/* Channel in use by a running task */
static struct task_t tasks[MAX_TASKS] = {{0}};	/* Active tasks */
static struct taskDone_t taskDone[MAX_TASKS] = {{0}};	/* Completed tasks */
static char *taskName = NULL;		/* Name of task being processed */
static char *dirs[MAX_DIRS] = {0};	/* Task search path */
static int ndir=0;			/* Number of directories */
static int nconn=0;			/* Number of active connections */
static int ntask=0;			/* Number of active tasks */
static int ndone=0;			/* Number of completed tasks */
static int maxfdno=0;			/* Maximum FD used */
static int curTask= -1;			/* Current task slot */
static int listenfd;			/* Server socket */
static int verbose=0;			/* Debug mode */

/* Internal functions. */
int executeClient (char *task, int argc, char *argv[]);
int getConnection (char *host, int port);
int addConnection (int fd);
int closeConnection (int client, int kill, int signal);
int handleRequest (int client);
static int matchTask (const struct dirent *entry);
static void sigint_handler (int sig);
static void sigchld_handler (int sig);
static char * taskStatus (task_t *tp);
static void taskCleanup();


/*
 * votaskd - Tasking daemon main routine.
 *
 * @brief    Start up a new tasking daemon instance on the local host.
 * @fn       status = votaskd [args]
 *
 * @param    -d, --dir		Add named directory to task search path
 * @param    -h, --host		Host to connect to (default localhost)
 * @param    -p, --port		Port to be used for client connections
 * @param    -s, --daemon	Fork and execute as a daemon
 * @param    -c, --cli		Execute as client
 * @param    -t, --task		Task to be executed
 * @param    -l, --logfile	Redirect stderr to the named logfile
 * @param    -v, --verbose	Print debugging messages
 * @param    --max-clients	Max connected clients
 * @param    --max-tasks	Max executing tasks
 * @return			Exit status, or PID of daemon for -d
 *
 * If called to execute a task in client mode, the -t/--task argument must
 * be the final votaskd argument, and any following arguments are passed to
 * the executed task.
 */
int
main (int argc, char *argv[])
{
    int stat, done=0, cli=0, daemon=0, nerror=0, ch;
    struct sockaddr_in serv_addr; 
    char *logfile=NULL;

    /* Command line arguments. */
    static char keyopts[] = "cd:h:l:p:st:v";
    static struct option longopts[] = {
	{ "cli",	 no_argument,		NULL,	ARG_CLIENT },
	{ "dir",	 required_argument,	NULL,	ARG_DIR },
	{ "host",	 required_argument,	NULL,	ARG_HOST },
	{ "port",	 required_argument,	NULL,	ARG_PORT },
	{ "logfile",	 required_argument,	NULL,	ARG_LOGFILE },
	{ "daemon",	 no_argument,		NULL,	ARG_DAEMON },
	{ "task",	 required_argument,	NULL,	ARG_TASK },
	{ "verbose",	 no_argument,		NULL,	ARG_VERBOSE },
	{ "max-clients", required_argument,	NULL,	ARG_MAXCLIENTS },
	{ "max-tasks",	 required_argument,	NULL,	ARG_MAXTASKS },
	{ NULL,		 0,			NULL,	0 },
    };

    /* Process command line options. */
    while (!done &&
	(ch = getopt_long(argc, argv, keyopts, longopts, NULL)) != -1) {

	char *endptr;
	long val;

	switch (ch) {
	case ARG_CLIENT:
	    cli = 1;
	    break;
	case ARG_DAEMON:
	    daemon = 1;
	    break;
	case ARG_VERBOSE:
	    verbose++;
	    break;
	case ARG_LOGFILE:
	    logfile = optarg;
	    break;
	case ARG_DIR:
	    if (ndir >= MAX_DIRS) {
		fprintf (stderr, "Maximum search directories exceeded\n");
		exit (1);
	    }
	    dirs[ndir++] = optarg;
	    break;
	case ARG_HOST:
	    strncpy (servhost, optarg, SZ_NAME);
	    break;
	case ARG_PORT:
	    val = strtol (optarg, &endptr, 10);
	    if (endptr == optarg || val <= 0) {
		fprintf (stderr, "Failed to decode port argument\n");
		exit (2);
	    }
	    servport = val;
	    break;
	case ARG_TASK:
	    taskName = optarg;
	    done = cli = 1;
	    break;
	case ARG_MAXCLIENTS:
	    val = strtol (optarg, &endptr, 10);
	    if (endptr == optarg || val > MAX_CONN) {
		fprintf (stderr, "Maximum client connections exceeded\n");
		exit (3);
	    }
	    maxconn = val;

	    break;
	case ARG_MAXTASKS:
	    val = strtol (optarg, &endptr, 10);
	    if (endptr == optarg || val > MAX_TASKS) {
		fprintf (stderr, "Maximum executing tasks exceeded\n");
		exit (4);
	    }
	    maxtasks = val;
	    break;
	default:
	    fprintf (stderr, "unknown option: %s\n", optarg);
	    exit (5);
	}
    }

    argc -= optind;
    argv += optind;

    /* Execute as a client issuing requests to the daemon. */
    if (cli) {
	int status = executeClient(taskName, argc, argv);
	exit(status);
    }

    /* Redirect stderr to a logfile if indicated. */
    if (logfile) {
	freopen (logfile, "a", stderr);
	setlinebuf (stderr);
	fprintf (stderr, "## Tasking daemon logging started\n\n");
    }

    /* Start up execution of the tasking daemon. */
    if (daemon) {
	/* Fork a process to run the daemon. */
	pid_t pid = fork();
	if (pid < 0) {
	    fprintf (stderr, "cannot fork daemon process\n");
	    exit (6);
	} else if (pid > 0)
	    exit (0);

	/* Continue executing as a daemon process. */
    }

    /* Post a handler to cleanly shutdown the daemon. */
    struct sigaction sigact;
    memset (&sigact, 0, sizeof(sigact));
    sigact.sa_handler = sigint_handler;
    sigaction (SIGINT, &sigact, NULL);
    sigaction (SIGHUP, &sigact, NULL);

    /* Post a handler to be called whenever a child process exits. */
    memset (&sigact, 0, sizeof(sigact));
    sigact.sa_handler = sigchld_handler;
    sigact.sa_flags = SA_NOCLDSTOP;
    sigfillset(&sigact.sa_mask);
    sigaction (SIGCHLD, &sigact, NULL);

    /* Set up a service socket for incoming connections.  */
    listenfd = socket(AF_INET, SOCK_STREAM, 0);
    if (listenfd < 0) {
	fprintf (stderr, "socket creation failed (%d)\n", errno);
	exit (1);
    } else {
	int opt = 1;
	setsockopt (listenfd, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt));
    }

    memset (&serv_addr, '0', sizeof(serv_addr));
    serv_addr.sin_family = AF_INET;
    serv_addr.sin_addr.s_addr = htonl(INADDR_ANY);
    serv_addr.sin_port = htons(servport); 

    stat = bind (listenfd, (struct sockaddr*)&serv_addr, sizeof(serv_addr)); 
    if (stat < 0) {
	fprintf (stderr, "cannot bind to port %d (%d)\n", servport, errno);
	exit (1);
    }
    stat = listen (listenfd, maxconn); 
    if (stat < 0) {
	fprintf (stderr, "cannot listen on port %d (%d)\n", servport, errno);
	exit (1);
    }

    if (verbose)
	fprintf (stderr, "server socket opened on fd=%d\n", listenfd);

    while (1) {
	int maxfd=listenfd, result, client, fd, i;
	fd_set read_fds;

	/* Check for and process any completed tasks. */
	taskCleanup();

	/* Watch for new client connections as well as requests from any
	 * already connected clients.
	 */
	do {
	    FD_ZERO(&read_fds);
	    FD_SET(listenfd, &read_fds);
	    for (i=0;  i < maxconn;  i++) {
		/* if ((fd = connfd[i]) != 0 && !connbusy[i]) { */
		if ((fd = connfd[i]) != 0) {
		    FD_SET(fd, &read_fds);
		    if (fd > maxfd)
			maxfd = fd;
		}
	    }

	    if (verbose > 1) {
		fprintf (stderr, "select: maxfd=%d fdset=", maxfd);
		for (i=0;  i < maxconn;  i++) {
		    if (FD_ISSET(i, &read_fds))
			fprintf (stderr, " %d", i);
		}
		fprintf (stderr, "\n");
	    }

	    errno = 0;
	    maxfdno = maxfd;
	    result = select (maxfd+1, &read_fds, NULL, NULL, NULL);
	    if (verbose > 1)
		fprintf (stderr, "main input select returns %d\n", result);

	    /* Check for and process any completed tasks.  If a task completes
	     * during the select() then the sigchld interrupt posts a new
	     * taskDone instance and interrupts the select(), causing it to
	     * return with status -1 and allowing us to finalize the completed
	     * task here.
	     */
	    taskCleanup();

	} while (result == -1 && errno == EINTR);

	if (result > 0) {
	    /* Accept a new client connection. */
	    if (FD_ISSET(listenfd, &read_fds)) {
		fd = accept (listenfd, (struct sockaddr*)NULL, NULL); 
		if (fd > 0)
		    addConnection (fd);
	    }

	    /* Service any client requests. */
	    for (i=0;  i < maxconn && (fd = connfd[i]) > 0;  i++)
		if (FD_ISSET(fd, &read_fds))
		    handleRequest (client=i);

	} else if (result == 0) {
	    ;  /* timeout; just loop again */

	} else if (result < 0) {
	    fprintf (stderr, "votaskd: select() error: %s\n", strerror(errno));
	    if (nerror++ > MAX_ERRORS) {
		fprintf (stderr, "votaskd: too many select() errors\n");
		close (listenfd);
		exit (10);
	    }
	}
     }

    close (listenfd);
    exit (0);
}

/*
 * executeClient -- Executing as a client processing CLI requests.
 *
 * @brief    Execute as a client of the tasking daemon
 * @fn       void = executeClient (int argc, char *argv[])
 *
 * @param    task		Taskname if non-interactive
 * @param    argc		Number of command line arguments
 * @param    argv		Command line arguments
 * @returns			Nothing
 *
 * This mode is intended for testing, but could be used, e.g., in a script,
 * to execute tasks on a remote server using the tasking daemon.
 * The task name should already have been set with -t or --task.
 * If no task name is given interactive mode is entered, reading and
 * executing commands input via stdin.
 *
 * Currently the console client maintains a single task execution connection
 * (taskfd below).  This means that it can only run a single task at a time.
 * If another exec command is entered while a task is running, it will be
 * saved and executed when the first tasks finishes.  To run multiple
 * tasks simultaneously we would merely have to open additional task execution
 * connections.
 */
int
executeClient (char *task, int argc, char *argv[])
{
    int prompt=1, nerror=0, controlfd, taskfd, maxfd=0;
    char *cmd, buf[SZ_MSGBUF];

    /* Get a control connection to the tasking daemon. */
    if ((controlfd = getConnection(servhost,servport)) < 0) {
	fprintf (stderr, "Cannot connect to %s:%d\n", servhost, servport);
	return (-1);
    } else if (controlfd > maxfd)
	maxfd = controlfd;

    /* Get a task exec/data connection to the tasking daemon. */
    if ((taskfd = getConnection(servhost,servport)) < 0) {
	fprintf (stderr, "Cannot connect to %s:%d\n", servhost, servport);
	return (-1);
    } else if (taskfd > maxfd)
	maxfd = taskfd;

    /* Execute a single task. */
    if (task) {
	char *ip, *op=buf;
	int i;

	/* Compose the exec task command. */
	for (ip="exec";  (*op = *ip++);  op++)
	    ;
	*op++ = ' ';
	for (ip=task;  (*op = *ip++);  op++)
	    ;
	*op++ = ' ';
	for (i=0;  i < argc;  i++) {
	    for (ip=argv[i];  (*op = *ip++);  op++)
		;
	    *op++ = ' ';
	}
	*(op-1) = '\n';

	/* Send the command off to be executed. */
	write (taskfd, buf, (op - buf));
    }

    /* Loop executing commands.
     */
    while (1) {
	fd_set read_fds;
	struct timeval delay;
	int result;

	/* Short (100 millisecond) delay. */
	delay.tv_sec = 0;
	delay.tv_usec = 1000 * 100;

	/* Prompt for input if idle. */
	if (prompt > 0) {
	    dprintf (1, "> ");
	    prompt = 0;
	}

	/* Process any command input, and copy anything that comes back from
	 * the tasking daemon to the stdout.
	 */
	do {
	    FD_ZERO(&read_fds);
	    if (!task)
		FD_SET(0, &read_fds);
	    FD_SET(controlfd, &read_fds);
	    FD_SET(taskfd, &read_fds);

	    result = select (maxfd+1, &read_fds, NULL, NULL, &delay);

	} while (result == -1 && errno == EINTR);

	if (result > 0) {
	    /* Copy out any output from the tasking daemon. */
	    if (FD_ISSET(controlfd, &read_fds)) {
		int n = read(controlfd, buf, SZ_MSGBUF);
		if (n > 0) {
		    int eot = (strncmp(buf, "[EOT]", 5) == 0);
		    write (1, buf, n);
		    prompt++;
		    if (eot)
			prompt = 1;
		}
	    }
	    if (FD_ISSET(taskfd, &read_fds)) {
		int n = read(taskfd, buf, SZ_MSGBUF);
		int eot = 0;

		if (n > 0) {
		    eot = (strncmp(buf, "[EOT]", 5) == 0);
		    if (task && eot)
			break;

		    write (1, buf, n);
		    if (eot)
			prompt = 1;
		}
	    }

	    /* Execute a new command. */
	    if (FD_ISSET(0, &read_fds)) {
		if ((cmd = fgets(buf, SZ_MSGBUF, stdin)) != NULL) {
		    prompt++;
		    if (buf[0] == '\n')
			continue;

		    if (strncmp(cmd, "exit", 4) == 0)
			break;
		    else if (strncmp(cmd, "quit", 4) == 0)
			break;

		    if (strncmp(cmd, "exec", 4) == 0) {
			dprintf (taskfd, "%s\n", cmd);
			--prompt;
		    } else {
			dprintf (controlfd, "%s\n", cmd);
			--prompt;
		    }
		}
	    }

	} else if (result == 0) {
	    ;  /* timeout; just loop again */

	} else if (result < 0) {
	    fprintf (stderr, "votaskd: select() error: %s\n", strerror(errno));
	    if (nerror++ > MAX_ERRORS) {
		fprintf (stderr, "votaskd: too many select() errors\n");
		return (-1);
	    }
	}
     }

     dprintf (taskfd, "close\n");
     close (taskfd);

     dprintf (controlfd, "close\n");
     close (controlfd);

     return (0);
}

/*
 * getConnection -- Open a connection to the given host and port.
 *
 * @brief    Open a connection to the given host and port.
 * @fn       fd = getConnection (char *host, int port)
 *
 * @param    host		Host to connect to
 * @param    port		Port to connect to
 * @returns			File descriptor
 */
int
getConnection (char *host, int port)
{
    struct sockaddr_in serv_addr;
    struct hostent *hent;
    char *hostip;
    int sockfd;

    /* Connect to the tasking daemon. */
    if ((sockfd = socket(AF_INET, SOCK_STREAM, 0)) < 0) {
	fprintf (stderr, "Cannot create socket\n");
	return (-1);
    }

    memset(&serv_addr, '0', sizeof(serv_addr)); 
    serv_addr.sin_family = AF_INET;
    serv_addr.sin_port = htons(port); 

    /* Try to resolve the host name or IP to an IP address. */
    if (!host || host[0] == '\0') {
	hostip = "127.0.0.1";
    } else if ((hent = gethostbyname(host)) != NULL) {
	struct in_addr **addr_list;
	addr_list = (struct in_addr **) hent->h_addr_list;
	hostip = inet_ntoa(*addr_list[0]);
    } else
	hostip = host;

    if (inet_pton(AF_INET, hostip, &serv_addr.sin_addr) <= 0) {
	fprintf (stderr, "Cannot convert hostname to IP (%s)\n", host);
	return (-1);
    } 

    if (connect(sockfd,
	(struct sockaddr *)&serv_addr, sizeof(serv_addr)) < 0) {

	fprintf (stderr, "Connnect to server socket failed (%s)\n", host);
	return (-1);
    } 

    if (verbose)
	fprintf (stderr, "socket opened for %s:%d\n", host, port);

    return (sockfd);
}

/*
 * addConnection -- Add a client connection to the tasking daemon.
 *
 * @brief    Process a new client connection
 * @fn       status = addConnection (int fd)
 *
 * @param    fd			Socket file descriptor for the new connection
 * @returns			The connection number, or -1 if error occurs
 *
 * The client has requested a connection to the tasking daemon, and the daemon
 * has already accepted the connection, resulting in a direct 2-way socket
 * connected to the client.  All we need to do is set up the client connection
 * and send an acknowledgement back to the client.  The client may then send
 * requests to the tasking daemon.
 */
int
addConnection (int fd)
{
    int conn;

    /* Some error occurred upstream; just bail out. */
    if (fd < 0)
	return (-1);

    /* Get an empty connection slot. */
    for (conn=0;  conn < maxconn;  conn++)
	if (connfd[conn] == 0)
	    break;

    /* If overflow occurs refuse the connection. */
    if (conn >= maxconn) {
	dprintf (fd, "[ERR] too many client connections\n");
	close (fd);
	return (-1);
    }

    /* Set up the new client connection. */
    connfd[conn] = fd;
    connbusy[conn] = 0;
    nconn++;

    if (verbose)
	fprintf (stderr, "added client connection conn=%d fd=%d\n", conn, fd);

    return (conn);
}

/*
 * closeConnection -- Close a client connection to the tasking daemon.
 *
 * @brief    Close an existing client connection
 * @fn       status = closeConnection (int fd)
 *
 * @param    client		Client connection number
 * @param    killtasks		If set, kill any active tasks
 * @param    sig		If nonzero, signal to be sent
 * @returns			Zero, or -1 if error occurs
 *
 * If the client has any tasks running they may optionally be killed.
 * [TODO: replace integer signal code by signal name].
 */
int
closeConnection (int conn, int killtasks, int sig)
{
    int i;

    /* Some error occurred upstream; just bail out. */
    if (connfd[conn] <= 0)
	return (-1);

    /* Kill (interrupt) any active tasks if requested. */
    for (i=0;  killtasks && i < maxtasks;  i++) {
	task_t *tp = &tasks[i];
	if (tp == NULL)
	    continue;
	if (tp->conn != conn)
	    continue;

	kill (tp->pid, (sig != 0) ? sig : SIGINT);
    }

    /* Free the client connection descriptor. */
    close (connfd[conn]);
    connbusy[conn] = 0;
    connfd[conn] = 0;
    nconn--;

    if (verbose)
	fprintf (stderr, "closing client connection conn=%d fd=%d\n",
	conn, connfd[conn]);
    
    return (0);
}

/*
 * handleRequest -- Handle a client request.
 *
 * @brief    Handle a request sent from a connected client.
 * @fn       status = handleRequest (int fd)
 *
 * @param    client		Client connection number
 * @returns			Zero, or -1 if error occurs
 *
 * When this is called all we know is that there is pending input on the
 * connection to the client.  We need to read the request and process it.
 * The currently supported requests are pretty simple: the request name
 * followed an arbitrary amount of text, terminated by a newline.
 */
int
handleRequest (int conn)
{
    char reqbuf[MAX_REQUEST];
    char msgbuf[SZ_MSGBUF];
    char *request, *args, *ip, *op=reqbuf;
    int done=0, ch, nb, i, j;

    /* Accumulate the request - should be EOS or newline terminated to
     * ensure that the full message is read if the read is fragmented.
     * Multi-line requests are not supported (or needed).
     */
    int fd = connfd[conn];
    while (!done && (nb = read(fd, msgbuf, SZ_MSGBUF)) > 0) {
	for (ip=msgbuf, i=0;  i < nb;  i++) {
	    ch = (*op++ = *ip++);
	    if (ch == '\0' || ch == '\n') {
		done++;
		break;
	    }
	}
    }

    /* Make sure the string is EOS terminated. */
    *op = '\0';

    /* Get the request name. */
    request = strtok (reqbuf, " \n");
    if (request == NULL)
	return (-1);

    if (verbose)
	fprintf (stderr, "execute request: conn=%d, request='%s'\n",
	conn, reqbuf);

    /*
     * Process the request.
     * ------------------------
     */

    if (strncmp (request, "execute", 4) == 0) {
	/*
	 * -- Execute a task --
	 */
	char *taskPath=NULL, path[SZ_PATHNAME];
	char *ip, *op, *argv[MAX_ARGS];
	int nargs=0;

	/* Get the task name. */
	taskName = strtok (NULL, " \n");
	if (taskName == NULL) {
	    dprintf (fd, "[ERR] task name missing in exec request\n");
	    return (-1);
	}

	/* Assemble task args. */
	memset (&args, '0', sizeof(args));
	argv[nargs++] = taskName;
	while ((argv[nargs] = strtok(NULL, " \n")) != NULL)
	    nargs++;

	/* Search the task search path for the task. */
	if (taskName[0] == '/') {
	    strncpy (path, taskName, SZ_PATHNAME);
	    taskPath = path;
	} else {
	    for (i=0;  i < ndir;  i++) {
		struct dirent **namelist;
		int n = scandir (dirs[i], &namelist, matchTask, alphasort);
		if (n > 0) {
		    sprintf (taskPath=path, "%s/%s", dirs[i], taskName);
		    for (j=0;  j < n;  j++)
			free (namelist[j]);
		    break;
		}
	    }
	}
	if (taskPath == NULL) {
	    dprintf (fd, "[ERR] exec cannot find task: %s\n", taskName);
	    return (-1);
	}

	/* Allocate an unused task descriptor for the new task.
	 * This is done in a round-robin status, reusing idle slots
	 * when it wraps around.
	 */
	task_t *tp = NULL;
	int taskSlot = -1;

	for (i=0;  i < maxtasks;  i++) {
	    taskSlot = ++curTask;
	    if (taskSlot >= maxtasks)
		taskSlot = curTask = 0;

	    tp = &tasks[taskSlot];
	    if (!tp->pid) {
		tp->status = TASK_INIT;
		tp->conn = conn;
		tp->connfd = fd;
		tp->exit_status = 0;

		strncpy (tp->taskName, taskName, SZ_NAME);
		for (i=1, op = tp->taskArgs;  i < nargs;  i++) {
		    for (ip=argv[i];  (*op = *ip++) != '\0';  op++)
			;
		    *op++ = ' ';
		    if ((SZ_LINE - (op - tp->taskArgs)) < SZ_NAME) {
			*op++ = '.'; *op++ = '.'; *op++ = '.';
			break;
		    }
		}
		*op++ = '\0';
		break;
	    }
	}
	if (taskSlot < 0) {
	    dprintf (fd, "[ERR] exec out space for tasks (%s)\n", taskName);
	    return (-1);
	}

	/* Fork a process to run the task. */
	tp->pid = fork();
	if (tp->pid == -1) {
	    dprintf (fd, "[ERR] exec process fork failed (%s)\n", taskName);
	    tp->pid = 0;
	    return (-1);
	}

	if (tp->pid != 0) {
	    /* Parent (server) process. */

	    if (verbose) {
		fprintf (stderr,
		    "execute task: conn=%d pid=%d task='%s %s'\n",
		    conn, tp->pid, taskPath, tp->taskArgs);
	    }

	    tp->status = TASK_RUNNING;
	    connbusy[conn] = 1;
	    ntask++;

	} else {
	    /* Child (task) process.  The stdout of the task goes directly to
	     * the client over the same socket used to submit the task exec
	     * request.  The task stderr currently just goes to the stderr
	     * of the tasking daemon.  The daemon maintains a task descriptor
	     * for the running task and can monitor and control execution.
	     *
	     * The client sees a single line formatted as "[OK] <taskno>"
	     * and then all subsequent output is the task stdout (which must
	     * be textual in this implementation).  When the task completes,
	     * "[EOT]-TN <exit_status>", where TN is the task number, will be
	     * seen as a single line of text.
	     */
	    dup2 (fd, 1);    /* task stdout -> client socket */
	    for (i=3;  i <= maxfdno;  i++)
		close (i);

	    dprintf (1, "[OK] %d\n", taskSlot);

	    /* Execute the task (does not return here if successful). */
	    if (execv(taskPath, argv) == -1) {
		dprintf (1, "[ERR] task exec failed (%d)\n", errno);
		exit (1);
	    }
	}

    } else if (strcmp (request, "nop") == 0) {
	/*
	 * -- NoOperation. --
	 */
	return (0);

    } else if (strcmp (request, "status") == 0) {
	/*
	 * -- Return the status of task, or all tasks if no arg --
	 */
	char *token = strtok (NULL, " \n");
	if (token == NULL) {
	    /* Return full status. */
	    dprintf (fd, "[OK] nconn=%d ntasks=%d\n", nconn, ntask); 

	    for (i=0;  i < maxtasks;  i++) {
		task_t *tp = &tasks[i];
		if (!tp->connfd)
		    continue;
		dprintf (fd, "task=%d conn=%d stat=%s exit=%d cmd: %s %s\n",
		    i, tp->conn, taskStatus(tp), tp->exit_status,
		    tp->taskName, tp->taskArgs);
	    }
	    dprintf (fd, "[EOT]\n");

	} else {
	    /* Return the status of a single task. */
	    int taskno = strtol(token, NULL, 10);
	    if (taskno < 0 || taskno > maxtasks) {
		dprintf (fd, "[ERR] invalid task number (%d)\n", taskno);
		return (-1);
	    } else {
		task_t *tp = &tasks[taskno];
		dprintf (fd, "[OK] task %3d %12s %3d (%s)\n", taskno, taskStatus(tp),
		    tp->exit_status, tp->taskName);
	    }
	}

    } else if (strcmp (request, "kill") == 0) {
	/*
	 * -- Kill (signal) a running task --
	 */
	char *token = strtok (NULL, " \n");
	if (token == NULL) {
	    dprintf (fd, "[ERR] missing task number\n");
	    return (-1);
	}

	int taskno = strtol(token, NULL, 10);
	if (taskno < 0 || taskno > maxtasks) {
	    dprintf (fd, "[ERR] invalid task number (%d)\n", taskno);
	    return (-1);
	} else {
	    int sig = SIGHUP;
	    if ((token = strtok (NULL, " \n")) != NULL)
		sig = strtol(token, NULL, 10);

	    task_t *tp = &tasks[taskno];
	    kill (tp->pid, sig);
	    dprintf (fd, "[OK]\n");
	}

    } else if (strncmp (request, "nconnections", 5) == 0) {
	/*
	 * -- Return the number of client connections --
	 */
	dprintf (fd, "[OK] %d\n", nconn);

    } else if (strncmp (request, "ntasks", 5) == 0) {
	/*
	 * -- Return the number of running tasks --
	 *
	 * With no argument the total number of all running tasks is
	 * returned.  Otherwise the argument specifies the connection
	 * number and the count of running tasks for the given
	 * connection is returned.
	 */
	char *token = strtok (NULL, " \n");
	if (!token)
	    dprintf (fd, "[OK] %d\n", ntask);
	else {
	    int conn = strtol(token, NULL, 10);
	    int count = 0;

	    for (i=0;  i < maxtasks;  i++) {
		task_t *tp = &tasks[i];
		if (tp->pid && tp->conn == conn)
		    count++;
	    }
	    dprintf (fd, "[OK] %d\n", count);
	}

    } else if (strncmp (request, "killall", 7) == 0) {
	/*
	 * -- Kill all running tasks --
	 */
	for (i=0;  i < maxtasks;  i++) {
	    task_t *tp = &tasks[i];
	    if (tp->pid)
		kill (tp->pid, SIGHUP);
	}
	dprintf (fd, "[OK] %d\n", ntask);

    } else if (strncmp (request, "close", 5) == 0) {
	/*
	 * -- Close the client connection --
	 */
	if (verbose)
	    fprintf (stderr,
	    "closing client connection conn=%d fd=%d\n", conn, connfd[conn]);

	dprintf (fd, "[OK]\n");
	close (connfd[conn]);
	connfd[conn] = 0;
	nconn--;

    } else if (strncmp (request, "shutdown", 8) == 0) {
	/*
	 * -- Shutdown the tasking daemon --
	 */
	int shutdown_now = 0;
	char *token = strtok (NULL, " \n");
	if (token && strcmp(token, "now") == 0)
	    shutdown_now = 1;

	if (ntask > 0 && !shutdown_now)
	    dprintf (fd, "[ERR] %d tasks are still running\n", ntask);
	else {
	    dprintf (fd, "[OK]\n");
	    exit (0);
	}

    } else {
	dprintf (fd, "[ERR] unknown request: %s\n", request);
    }

    return (0);
}

/*
 * matchTask -- Taskname match filter for scandir().
 */
static int
matchTask (const struct dirent *entry)
{
    return (strcmp(entry->d_name, taskName) == 0);
}

/*
 * sigint_handler -- Handle an interrupt and shutdown the daemon.
 */
static void
sigint_handler (int sig)
{
	if (verbose)
	    fprintf (stderr, "shutting down daemon\n");

	/* Free the server socket. */
	close (listenfd);
	exit (sig);
}

/*
 * sigchld_handler -- Handle a child process status change signal.
 *
 * This is invoked in the tasking daemon when a task (child subprocess)
 * completes or otherwise terminates.  All we do is create a new
 * taskDone instance to be later processed in the main thread of the
 * tasking daemon.
 */
static void
sigchld_handler (int sig)
{
    pid_t pid;
    int status, i;

    while ((pid=waitpid(-1, &status, 0)) != -1) {
	/* Add a new task done instance.  This gets processed later by
	 * the tasking daemon.  Trying to process task completion in a
	 * signal handler can lead to race conditions.
	 */
	for (i=0;  i < maxtasks;  i++) {
	    taskDone_t *td = &taskDone[i];
	    if (td->pid == 0) {
		td->pid = pid;
		td->status = status;
		ndone++;
		break;
	    }
	}
    }
}

/*
 * taskCleanup -- Check for any finalize any completed tasks.
 *
 * This is invoked in the tasking daemon when a task (child subprocess)
 * completes or otherwise terminates.  A completion message is sent on the
 * client output stream indicating that the task has terminated.  The
 * task completion status is saved for a time, but will be overwritten when
 * the task slot is later reused.
 */
static void
taskCleanup()
{
    struct task_t *tp=NULL;
    int taskSlot, status, i;
    pid_t pid;

    for (i=0;  ndone && i < maxtasks;  i++) {
	taskDone_t *td = &taskDone[i];
	if (td->pid == 0)
	    continue;

	/* Get the saved waitpid() data. */
	pid = td->pid;
	status = td->status;

	/* Find the task slot for this pid. */
	for (taskSlot=0, tp=NULL;  taskSlot < maxtasks;  taskSlot++) {
	    if (tasks[taskSlot].pid == pid) {
		tp = &tasks[taskSlot];
		break;
	    }
	}

	/* Update task status and free task slot.  The end-of-task message
	 * and exit status is sent to the client.  The task completion status
	 * is retained until the task slot is reused.
	 */
	if (tp) {
	    if (WIFEXITED(status))
		tp->status = TASK_COMPLETED;
	    else
		tp->status = TASK_INTERRUPTED;

	    tp->exit_status = WEXITSTATUS(status);
	    connbusy[tp->conn] = 0;

	    if (verbose > 1) {
		fprintf (stderr, "task cleanup: conn=%d, task=%s, stat=%d\n",
		    tp->conn, tp->taskName, tp->exit_status);
	    }

	    dprintf (tp->connfd, "[EOT] %d %d\n", taskSlot, tp->exit_status);
	    if (verbose) {
		fprintf (stderr, "task completion: [EOT] %d %d\n",
		    taskSlot, tp->exit_status);
	    }

	    tp->pid = 0; ntask--;
	    td->pid = 0; ndone--;

	} else
	    fprintf (stderr, "taskCleanup: no task slot for pid=%d\n", pid);
    }
}

/*
 * taskStatus -- Return the status of a task as a string.
 */
static char *
taskStatus (task_t *tp)
{
    switch (tp->status) {
    case TASK_INIT:
	return ("init");
	break;
    case TASK_RUNNING:
	return ("running");
	break;
    case TASK_COMPLETED:
	return ("completed");
	break;
    case TASK_INTERRUPTED:
	return ("interrupted");
	break;
    }

    return ("unknown");
}
