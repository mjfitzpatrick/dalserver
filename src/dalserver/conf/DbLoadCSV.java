package dalserver.conf;

import dalserver.KeywordFactory;
import dalserver.TableParam;

import java.util.HashSet;
import java.util.TreeSet;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * a class for loading a CSV table into a database
 *
 * The table must be in comma-separated value format where the first line of 
 * the table provides the column names.  
 */
public class DbLoadCSV {

    /**
     * the name to give the table within the database
     */
    protected String tbl = null;

    /**
     * the name to give the table within the database
     */
    protected KeywordFactory kwf = null;

    BufferedReader csvin = null;
    HashSet<String> hms = new HashSet<String>();
    HashSet<String> dms = new HashSet<String>();
    HashSet<String> noq = new HashSet<String>();

    /**
     * ready to write into a given table
     */
    public DbLoadCSV(String tblname, KeywordFactory coldesc) { 
        tbl = tblname;
        kwf = coldesc;
        if (kwf != null) setDataFormatting(kwf);
    }

    /**
     * ready to write into a given table from a given stream holding the CSV data
     */
    public DbLoadCSV(String tblname, KeywordFactory coldesc, Reader csv) 
        throws IOException 
    { 
        this(tblname, coldesc);
        openCSV(csv);
    }

    /**
     * determine how the data should be formatted based on the 
     * types of the columns configured in the given columns description.  
     * If this is not called, all column types will be assumed to be strings.
     */
    public void setDataFormatting(KeywordFactory kwf) {
        TableParam tp = null;
        Object kw = null;
        String type = null;
        for(Iterator<String> i=kwf.idIterator(); i.hasNext(); ) {
            kw = kwf.getKeyword(i.next());
            if (kw instanceof TableParam) {
                tp = (TableParam) kw;
                type = tp.getDataType();
                if (type.equals("float") || type.equals("double"))
                    noq.add(tp.getName());
            }
        }
    }

    /**
     * attach a CSV file to this loader
     */
    public void openCSV(Reader csv) throws IOException {
        csvin = new BufferedReader(csv);
    }

    /**
     * identify a column containing right ascension valuess in 
     * sexigesimal (HMS) format which will nee to be converted to decimal 
     * egrees prior to loading.  
     */
    public void addHMSCol(String colname) {
        hms.add(colname);
    }

    /**
     * identify a column containing angle (e.g. declination) valuess in 
     * sexigesimal (DMS) format which will nee to be converted to decimal 
     * egrees prior to loading.  
     */
    public void addDMSCol(String colname) {
        dms.add(colname);
    }

    /**
     * Convert a coordinate value in various formats into a floating
     * point value.  Both sexagesimal and decimal formats are permitted.
     */
    public double parseDMS(String hms) {
	boolean negative = false;
	String str = hms.trim();
	if (str.startsWith("-")) {
	    str = str.substring(1);
	    negative = true;
	}

	StringTokenizer tok = new StringTokenizer(str);
	double value = 0;

	for (int i=0;  i < 3;  i++) {
	    try {
		String token = tok.nextToken(" :");
		if (i == 0)
		    value = new Float(token);
		else
		    value += (new Float(token) / 60.0);
	    } catch (NoSuchElementException ex) {
		continue;
	    } catch (NullPointerException ex) {
		continue;
	    }
	}

	return (negative ? -value : value);
    }

    String DMS2deg(String val) {
        return Double.toString(parseDMS(val));
    }

    String HMS2deg(String val) {
        return Double.toString(parseDMS(val)*15.0);
    }

    /**
     * read the data from the input CSV stream and write them to an 
     * output stream destined for the database.  
     */
    public void sendData(Writer db) throws IOException {
        if (csvin == null)
            throw new IllegalStateException("No input CSV file attached.");
        PrintWriter out = new PrintWriter(db);
        String[] vals = null;
        int i = 0;

        // Get column names
        String cols = csvin.readLine();
        if (cols == null) 
            throw new IllegalStateException("Empty input CSV stream");
        cols = cols.trim();

        TreeSet<Integer> hmscol = new TreeSet<Integer>();
        TreeSet<Integer> dmscol = new TreeSet<Integer>();
        TreeSet<Integer> noqcol = new TreeSet<Integer>();

        vals = cols.split("\\s*,\\s*");
        for(i=0; i < vals.length; i++) {
            // find the RA & Dec collumns
            if (hms.contains(vals[i])) 
                hmscol.add(i);
            else if (dms.contains(vals[i])) 
                dmscol.add(i);

            // which columns should not be quoted
            if (noq.contains(vals[i]))
                noqcol.add(i);
        }

        String line = null;
        Pattern seg = Pattern.compile(".*[ :].*");
        while ((line = csvin.readLine()) != null) {
            vals = line.split(",");
            for(i=0; i < vals.length; i++) {
                if (hmscol.contains(i) && seg.matcher(vals[i]).matches())
                    vals[i] = HMS2deg(vals[i]);
                else if (dmscol.contains(i) && seg.matcher(vals[i]).matches())
                    vals[i] = DMS2deg(vals[i]);

                if (! noqcol.contains(i)) {
                    vals[i] = vals[i].replaceAll("'", "\\\\'");
                    vals[i] = "'"+vals[i]+"'";
                }
            }
            line = strJoin(vals,",");

            out.print("INSERT INTO ");
            out.print(tbl);
            out.println(" (");
            out.print("    ");
            out.println(cols);
            out.println("  ) VALUES (");
            out.print("    ");
            out.println(line);
            out.println("  );");
            out.flush();
        }
    }

    static String strJoin(String[] aArr, String sSep) {
        StringBuilder sbStr = new StringBuilder();
        for (int i = 0; i < aArr.length; i++) {
            if (i > 0) sbStr.append(sSep);
            sbStr.append(aArr[i]);
        }
        return sbStr.toString();
    }

    /**
     * Read a CSV table and conert it to an SQL insert script.
     *
     * The arguments expected, in order, are:
     * <dl>
     *    <dt> csvfile (req)
     *    <dd> path to the CSV table file
     *    <dt> kwfile (optional)
     *    <dd> path to the XML table defintion file
     *    <dt> table name (optional; default: "cat")
     *    <dd> the name of the table to load data into 
     *    <dt> outfile (optional; default: stanard out)
     *    <dd> path to the SQL insert-table script to write out
     * </dl>
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Missing csvfile argument");
            System.exit(1);
        }
        File kwcfg = null;
        if (args.length > 1) kwcfg = new File(args[1]);
        String tblname = "cat";
        if (args.length > 2) tblname = args[2];
        Writer out = null;

        File csvfile = new File(args[0]);
        if (! csvfile.exists()) {
            System.err.println("File not found: " + args[0]);
            System.exit(2);
        }
        if (kwcfg != null && ! kwcfg.exists()) {
            System.err.println("File not found: " + args[1]);
            System.exit(2);
        }

        try {
            KeywordFactory kwf = null;
            if (kwcfg != null) 
                kwf = new XMLConfiguredKeywordFactory(
                                                  new FileInputStream(kwcfg));

            if (args.length > 3) 
                out = new FileWriter(args[3]);
            else 
                out = new OutputStreamWriter(System.out);

            DbLoadCSV ldr = new DbLoadCSV(tblname, kwf, new FileReader(csvfile));
            ldr.addHMSCol("RA");
            ldr.addDMSCol("Dec");
            ldr.addDMSCol("Decl");
            ldr.sendData(out);
            if (args.length > 2) out.close();
        }
        catch (IOException ex) {
            System.err.println("Error: "+ex.getMessage());
            System.exit(2);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            System.exit(3);
        }
    }
}