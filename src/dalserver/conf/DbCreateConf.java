package dalserver.conf;

import java.sql.DatabaseMetaData;
import java.sql.Connection;
import java.sql.ResultSet; 
import java.sql.SQLException;
import java.sql.Types;
import java.sql.DriverManager;

import java.io.Reader;
import java.io.Writer;
import java.io.PrintWriter;
import java.io.PrintStream;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.IOException;

import java.io.OutputStreamWriter;
import java.io.FileWriter;

import java.util.TreeMap;
import java.util.HashMap;

/**
 * A class for seeding a configuration file for a table that is already 
 * loaded into the database.
 */
public class DbCreateConf {

    /**
     * the DBMS vendor type being connected to.
     */
    protected String dbmstype = null;

    /**
     * the host:port where MySQL is running.  If null, "localhost" will be 
     * assumed.  
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

    static TreeMap<Integer, String> types = new TreeMap<Integer, String>();
    static HashMap<String, String> drivers = new HashMap<String, String>();
    static {
        types.put(Types.VARCHAR, "string");
        types.put(Types.CHAR, "string");
        types.put(Types.LONGVARCHAR, "string");
        types.put(Types.REAL, "float");
        types.put(Types.DOUBLE, "double");
        types.put(Types.NUMERIC, "double");
        types.put(Types.INTEGER, "int");
        types.put(Types.SMALLINT, "int");
        types.put(Types.ROWID, "long");
        types.put(Types.BIGINT, "long");
        types.put(Types.BOOLEAN, "boolean");
        types.put(Types.BIT, "bit");

        drivers.put("mysql", "com.mysql.jdbc.Driver");
    }

    /**
     * prepare to inspect a given table in a database
     * @throws ClassNotFoundException  if the jdbcDriver is not available
     */
    public DbCreateConf(String dbtype, String jdbcDriver, 
                        String dbname, String tblname) 
        throws ClassNotFoundException
    {
        dbmstype = dbtype;
        db = dbname;
        tbl = tblname;
	try {
	    Class.forName(jdbcDriver).newInstance();
	} catch (IllegalAccessException ex) {
	    throw new IllegalArgumentException("Illegal Access: "+
                                               ex.getMessage());
	} catch (InstantiationException ex) {
	    throw new IllegalArgumentException("Not instantiatable: "+
                                               ex.getMessage());
	} 
    }


    /**
     * prepare to inspect a given table in a database
     */
    public DbCreateConf(String dbtype, String jdbcDriver, 
                        String dbname, String tblname, 
                        String user, String pw) 
        throws ClassNotFoundException
    {
        this(dbtype, jdbcDriver, dbname, tblname);
        this.user = user;
        pass = pw;
    }

    /**
     * write out a column description of the file to the given output stream
     */
    public int describeColumns(Writer writer, boolean summaryformat) 
        throws SQLException, IOException
    {
        Connection conn = connect();
        ResultSet rs = getColumns(conn, tbl);
        if (! rs.next())
            throw new SQLException("Table not found: " + tbl);
        rs.beforeFirst();

        PrintWriter w = new PrintWriter(writer);
        writePreamble(w);

        int c = 0;
        while (rs.next()) {
            writeParam(w, rs, summaryformat);
            c++;
        }

        writeCloser(w);
        w.flush();
        return c;
    }

    void writeParam(PrintWriter w, ResultSet rs, boolean summaryformat) 
        throws SQLException, IOException 
    {
        if (summaryformat) 
            writeParamSummary(w, rs);
        else
            writeParamXML(w, rs);
    }

    void writeParamXML(PrintWriter w, ResultSet rs) 
        throws SQLException, IOException 
    {
        String fmt = "%14s";
        String type = typeFor(rs.getInt("DATA_TYPE"));
        String arraysize = null;
        if (type == "string") {
            type = "char";
            arraysize = "*";
        }

        w.println("  <param>");
        w.format("    <name>%s</name>", rs.getString("COLUMN_NAME")).println();
        w.format("    <datatype>%s</datatype>", type).println();
        if (arraysize != null) 
            w.format("    <arraysize>%s</arraysize>", arraysize).println();
        w.println("  </param>");
    }

    void writeParamSummary(PrintWriter w, ResultSet rs) 
        throws SQLException, IOException 
    {
        String fmt = "%-14s";
        String type = typeFor(rs.getInt("DATA_TYPE"));
        String arraysize = null;
        if (type == "string") {
            type = "char";
            arraysize = "*";
        }

        w.println("<param> <summary>");
        w.format(fmt, "name:").println(rs.getString("COLUMN_NAME"));

        w.format(fmt, "datatype:").println(type);
        if (arraysize != null) {
            w.format(fmt, "arraysize:").println(arraysize);
        }

        w.format(fmt, "ucd:").println();
        w.format(fmt, "utype:").println();

        w.println("</summary>");
        w.println("<description>");
        w.println("   ");
        w.println("</description>");
        w.println("</param>");
        w.println();
    }

    void writePreamble(PrintWriter w) throws IOException {
        w.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");

        InputStream p = getClass().getResourceAsStream("confhelp.txt");
        if (p != null) {
            char[] buf = new char[2048];
            int c = 0;
            Reader pre = new InputStreamReader(p);
            while ((c = pre.read(buf)) > -1) 
                w.write(buf, 0, c);
            w.flush();
        }

        w.println("<keywords>");
        w.println();
    }

    void writeCloser(PrintWriter w) {
        w.println("</keywords>");
    }

    protected String typeFor(int sqltype) {
        return types.get(sqltype);
    }

    /**
     * return a listing of the columns in the given table
     */
    protected ResultSet getColumns(Connection conn, String table) 
        throws SQLException
    {
        String[] tbl = parseTableName(table);
        return conn.getMetaData().getColumns(tbl[0],tbl[1],tbl[2],null);
    }

    protected String[] parseTableName(String tblname) {
        String any = "%";
        String[] out = new String[3];
        String[] parts = tblname.split("\\.", 3);

        int i=0;
        if (parts.length > 2) 
            out[0] = parts[i++];
        if (parts.length > 1)
            out[1] = parts[i++];
        out[2] = parts[i];

        return out;
    }

    /** 
     * connect to the database
     */
    protected Connection connect() throws SQLException {
        return DriverManager.getConnection(getJDBCURL(), user, pass);
    }

    /**
     * return the JDBC URL for the database
     */
    protected String getJDBCURL() {
        String host_ = host;
        if (host_ == null) host_ = "localhost";
        String dbms = getDBMSType();
        return "jdbc:"+dbms+"://"+host_+"/"+db;
    }

    /**
     * return a label identifying the DBMS vendor type.  
     */
    protected String getDBMSType() {
        return dbmstype;
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            StringBuilder sb = new StringBuilder();
            String[] names = { " dbms", " dbame", " tablename" };
            for(int i=2; i >= args.length; i--) {
                sb.insert(0, names[i]);
            }
            sb.insert(0, "Missing args:");
            System.err.println(sb.toString());
            usage(System.err);
            System.exit(1);
        }
        String driver = driverFor(args[0]);
        if (driver == null) {
            System.err.println("DBMS "+args[0]+" not yet supported");
            System.exit(1);
        }

        String user = null, pw = null;
        if (args.length > 4) user = args[4];
        if (args.length > 5) pw = args[5];

        Writer out = null;
        try {
            if (args.length > 3 && ! "-".equals(args[3]) )
                out = new FileWriter(args[3]);
            else 
                out = new OutputStreamWriter(System.out);

            DbCreateConf conf = new DbCreateConf(args[0],  driver, args[1],
                                                 args[2], args[4], args[5] );
            conf.describeColumns(out, true);

            if (out instanceof FileWriter) out.close();
        }
        catch (IOException ex) {
            ex.printStackTrace();
            System.err.println("Error: "+ex.getMessage());
            System.exit(2);
        }
        catch (SQLException ex) {
            ex.printStackTrace();
            System.err.println("Error: "+ex.getMessage());
            System.exit(3);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            System.exit(4);
        }
        
    }

    public static void usage(PrintStream ps) {
        ps.println("Usage: dbcreateconf dbtype dbname tblname [outfile] [user pw]");
    }

    public static String driverFor(String dbtype) {
        return drivers.get(dbtype);
    }
}
