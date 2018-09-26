package dalserver.conf;

import dalserver.KeywordFactory;
import dalserver.TableParam;

import java.util.Properties;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.Writer;
import java.io.FileWriter;
import java.io.OutputStreamWriter;

/**
 * a class for creating tables in MySQL 
 */
public class MySQLCreateTable extends DbCreateTable {

    Properties useType = new Properties();

    /**
     * instantiate for a given table definition 
     * @param kwf      the KeywordFactory containing the definition of the 
     *                   table
     * @param tblName  the name to give to the table in the database
     */
    public MySQLCreateTable(KeywordFactory kwf, String tblName) {
        super(kwf, tblName);
        useType.setProperty("char", "varchar(128)");
        useType.setProperty("int", "int");
        useType.setProperty("float", "double");
        useType.setProperty("double", "double");
    }

    /**
     * return the database type appropriate for the given TableParam
     */
    public String dbTypeFor(TableParam param) {
        return useType.getProperty(param.getDataType());
    }

    /**
     * Read a table configuration and write a corresponding SQL script 
     * to create the table for a MySQL database to a file.
     *
     * The arguments expected, in order, are:
     * <dl>
     *    <dt> kwfile (req)
     *    <dd> path to the XML table defintion file
     *    <dt> table name (optional; default: "cat")
     *    <dd> the name to give to the databaes file
     *    <dt> outfile (optional; default: stanard out)
     *    <dd> path to the SQL create-table script to write out
     * </dl>
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Missing kwfile argument");
            System.exit(1);
        }
        String kwfile = args[0];
        String tblname = "cat";
        if (args.length > 1) tblname = args[1];
        Writer out = null;

        try {
            if (args.length > 2) 
                out = new FileWriter(args[2]);
            else 
                out = new OutputStreamWriter(System.out);

            XMLConfiguredKeywordFactory kwf = 
                new XMLConfiguredKeywordFactory(new FileInputStream(kwfile));
            MySQLCreateTable ct = new MySQLCreateTable(kwf, tblname);
            ct.writeSqlScript(out);
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
