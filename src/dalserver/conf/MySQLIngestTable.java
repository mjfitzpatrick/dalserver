package dalserver.conf;

import dalserver.KeywordFactory;

import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.Writer;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.PrintStream;

/**
 * a class for ingesting a simple catalog table into a MySQL database
 */
public class MySQLIngestTable {

    /**
     * the host:port where MySQL is running
     */
    protected String host = null;

    /**
     * the username to connect to the database with
     */
    protected String user = null;

    /**
     * the username to connect to the database with
     */
    protected String pass = null;

    /**
     * the name of the MySQL database to create table in
     */
    protected String db = null;

    /**
     * the name to give the table within the database
     */
    protected String tbl = null;

    boolean tablecreated = false;
    ArrayList<String> emsgs = null;

    /**
     * prepare to injest into a given database name and table
     */
    public MySQLIngestTable(String dbname, String tblname) {
        db = dbname;
        tbl = tblname;
    }

    /**
     * prepare to injest into a given database name and table
     */
    public MySQLIngestTable(String dbname, String tblname, 
                            String user, String pw) 
    {
        this(dbname, tblname);
        this.user = user;
        pass = pw;
    }

    /**
     * prepare to injest into a given database name and table
     */
    public MySQLIngestTable(String dbname, String tblname,
                            String user, String pw, String host) 
    {
        this(dbname, tblname, user, pw);
        this.host = host;
    }

    /**
     * create the (empty) data table based on the information in the 
     * given table description
     */
    public int createTable(KeywordFactory kwf) 
        throws InterruptedException, IOException 
    {
        return createTable(kwf, false);
    }

    /**
     * create the (empty) data table based on the information in the 
     * given table description
     */
    public int createTable(KeywordFactory kwf, boolean quiet) 
        throws InterruptedException, IOException 
    {
        int status = dropTable(true);
        /*
        if (tablecreated) {
            int status = dropTable();
            // if (status != 0) return status;
        }
        */

        DbCreateTable crtr = new MySQLCreateTable(kwf, tbl);

        Process proc = startDbClient();
        Writer w = new OutputStreamWriter(proc.getOutputStream());
        crtr.writeSqlScript(w);
        w.close();

        proc.waitFor();
        if (! quiet) {
            if (proc.exitValue() > 0) {
                for (String line : emsgs) 
                    System.err.println(line);
            }
            else {
                tablecreated = true;
                System.err.println("Table "+tbl+" created");
            }
        }

        return proc.exitValue();
    }

    /**
     * drop the table from the database;
     */
    public int dropTable() 
        throws InterruptedException, IOException 
    {
        return dropTable(false);
    }

    /**
     * drop the table from the database;
     */
    public int dropTable(boolean quiet) 
        throws InterruptedException, IOException 
    {
        // if (! tablecreated) 
        //    System.err.println("Warning: table not created yet?");

        Process client = startDbClient();
        PrintWriter w = 
            new PrintWriter(new OutputStreamWriter(client.getOutputStream()));
        w.print("DROP TABLE ");
        w.print(tbl);
        w.println(";");
        w.close();

        client.waitFor();
        if (! quiet) {
            if (client.exitValue() > 0) {
                for (String line : emsgs) 
                    System.err.println(line);
            }
            else {
                tablecreated = false;
                System.err.println("Existing table "+tbl+" dropped");
            }
        }

        return client.exitValue();
    }

    /**
     * load the data in the given CSV file into the database
     */
    public int loadData(Reader csv, KeywordFactory kwf) 
        throws InterruptedException, IOException
    {
        return loadData(csv, kwf, false);
    }

    /**
     * load the data in the given CSV file into the database
     */
    public int loadData(Reader csv, KeywordFactory kwf, boolean quiet) 
        throws InterruptedException, IOException
    {
        if (! tablecreated) 
            System.err.println("Warning: table not created yet?");

        DbLoadCSV ldr = new DbLoadCSV(tbl, kwf, csv);
        ldr.addHMSCol("RA");
        ldr.addDMSCol("Dec");
        ldr.addDMSCol("Decl");

        Process proc = startDbClient();
        Writer w = new OutputStreamWriter(proc.getOutputStream());
        ldr.sendData(w);

        w.close();
        proc.waitFor();
        if (!quiet) {
            if (proc.exitValue() > 0) {
                for (String line : emsgs) 
                    System.err.println(line);
            }
            else 
                System.err.println("Table data loaded");
        }

        return proc.exitValue();
    }

    /**
     * initialize the database by creating and loading the data table
     */
    public int initDb(KeywordFactory kwf, Reader csv) 
        throws InterruptedException, IOException
    {
        int ex = 0;
        ex = createTable(kwf);
        if (ex > 0) return ex;

        ex = loadData(csv, kwf);
        return ex;
    }

    /**
     * initialize the database by creating and loading the data table
     */
    public int initDb(File coldesc, File csvfile) 
        throws InterruptedException, IOException, KeywordConfig.FormatException
    {
        KeywordFactory kwf = new XMLConfiguredKeywordFactory(coldesc);
        FileReader csv = new FileReader(csvfile);
        return initDb(kwf, csv);
    }

    Process startDbClient() throws IOException {
        ProcessBuilder pb = new ProcessBuilder(clientCmd());
        Process proc = pb.start();
        /*
        if (pass != null) {
            // send the password
            OutputStream is = proc.getOutputStream();
            is.write(pass.getBytes());
            is.write("\n".getBytes());
            is.flush();
        }
        */

        captureErrors(proc.getErrorStream());

        return proc;
    }

    void collectClientErrors(BufferedReader err) {
        String line = null;
        try {
            while((line = err.readLine()) != null) {
                emsgs.add(line);
            }
        }
        catch (IOException ex) {
            emsgs.add("client comm error: " + ex.getMessage());
        }
    }

    void captureErrors(InputStream is) {
        emsgs = new ArrayList<String>();
        final InputStream istrm = is;
        Runnable capture = new Runnable() {
                BufferedReader es = 
                    new BufferedReader(new InputStreamReader(istrm));
                public void run() {
                    collectClientErrors(es);
                }
            };
        Thread t = new Thread(capture);
        t.start();
    }

    List<String> clientCmd() {
        ArrayList<String> cmd = new ArrayList<String>(2);
        cmd.add("mysql");
        if (host != null) 
            cmd.add("-h"+host);
        if (user != null)
            cmd.add("-u"+user);
        if (pass != null) {
            if (pass.length() > 1) 
                cmd.add("-p"+pass);
            else 
                cmd.add("-p");
        }
        cmd.add(db);
        return cmd;
    }

    /**
     * initialize the database from the command line
     */
    public static void main(String[] args) {
        if (args.length < 1) 
            fail("Missing db name, table name, table description, & data file", 1);
        if (args.length < 2) 
            fail("Missing table name, table description, & data file", 1);
        if (args.length < 3) 
            fail("Missing table description, & data file", 1);
        if (args.length < 4) 
            fail("Missing data file", 1);

        String user = null, pass = null, host = null;
        if (args.length > 4) user = args[4];
        if (args.length > 5) pass = args[5];
        if (args.length > 6) host = args[6];

        MySQLIngestTable ingester = new MySQLIngestTable(args[0], args[1],
                                                         user, pass, host);
        File coldesc = new File(args[2]);
        if (! coldesc.exists()) 
            fail("File not found: " + args[2], 2);
        File datacsv = new File(args[3]);
        if (! datacsv.exists()) 
            fail("File not found: " + args[3], 2);

        int exval = 0;
        try {
            /*
            KeywordFactory kwf = new XMLConfiguredKeywordFactory(coldesc);
            exval = ingester.createTable(kwf);
            */
            exval = ingester.initDb(coldesc, datacsv);
        }
        catch (IOException ex) {
            fail(ex.getMessage(), 3);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            System.exit(4);
        }
        if (exval > 0) exval += 10;
        System.exit(exval);
    }

    static void fail(String msg, int exval) {
        System.err.println(msg);
        if (exval == 1) usage(System.err);
        System.exit(exval);
    }

    public static void usage(PrintStream out) {
        out.println("Usage: ingesttable dbname tblname tbldesc csvdata [user] [pass] [host]");
    }
}
