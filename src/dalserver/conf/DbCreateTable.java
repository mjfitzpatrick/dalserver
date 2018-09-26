package dalserver.conf;

import dalserver.KeywordFactory;
import dalserver.TableParam;

import java.util.Iterator;
import java.util.Map;
import java.io.Writer;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.File;
import java.io.IOException;

/**
 * This base class for creating the database table that will be searched
 * by a DAL service.  This class is employed usually when setting up the 
 * service (i.e. not at runtime).
 */
public abstract class DbCreateTable {

    /**
     * the KeywordFactory that has the keywrd data (i.e. the definition
     * of the table columns) already loaded in it. 
     */
    protected KeywordFactory factory = null;

    /**
     * the name to give the table within the database
     */
    protected String tbl = null;

    /**
     * instantiate for a given table definition 
     * @param kwf      the KeywordFactory containing the definition of the 
     *                   table
     * @param tblName  the name to give to the table in the database
     */
    public DbCreateTable(KeywordFactory kwf, String tblName) {
        factory = kwf;
        tbl = tblName;
    }

    /**
     * write the SQL script that will create the table
     */
    public void writeSqlScript(Writer w) throws IOException {
        PrintWriter out = new PrintWriter(w);
        boolean started = false;
        String indent = "   ";

        // write the opening clause
        out.print("CREATE TABLE ");
        out.print(tbl);
        out.print(" (");

        // write each column definition
        Object p = null;
        TableParam tp = null;
        Object kw = null;
        for(Iterator<String> i=factory.idIterator(); i.hasNext(); ) {
            kw = factory.getKeyword(i.next());
            if (kw instanceof TableParam) {
                tp = (TableParam) kw;
                if (started) 
                    out.print(",");
                else 
                    started = true;
                out.println();
                out.print(indent);
                out.print(defineColumn(tp));
            }
        }

        // write closing bit
        out.println();
        out.println(");");
        out.println();
        out.flush();
    }

    /**
     * return an SQL column definition for the given column description.
     */
    public String defineColumn(TableParam param) {
        StringBuilder sb = new StringBuilder();
        sb.append(param.getName()).append(' ');
        sb.append(dbTypeFor(param));
        return sb.toString();
    }

    /**
     * return the database type appropriate for the given TableParam
     */
    public abstract String dbTypeFor(TableParam param);

    /**
     * write the table creation script to a named file
     */
    public void saveTo(File out) throws IOException {
        FileWriter w = new FileWriter(out);
        writeSqlScript(w);
        w.close();
    }

    
}
