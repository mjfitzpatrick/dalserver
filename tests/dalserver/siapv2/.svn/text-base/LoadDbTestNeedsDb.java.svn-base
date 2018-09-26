package dalserver.siapv2;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;

import java.io.IOException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

public class LoadDbTestNeedsDb {

    private Object driver = null;
    String dbname = System.getProperty("db.name", "siav2proto");
    String dbuser = System.getProperty("db.user", "demo");
    String dbpass = System.getProperty("db.pass", "demo");
    String tableName = System.getProperty("db.tblname", "siav2model");
    String url = System.getProperty("db.url", 
                                    "jdbc:mysql://localhost:3306");

    public Connection connect() throws SQLException {
        if (url.charAt(url.length()-2) != '/')
            url = url + '/';

        if (driver == null) {
            String jdbcDriver = System.getProperty("db.driver", 
                                                   "com.mysql.jdbc.Driver");
            try {
                driver = Class.forName(jdbcDriver).newInstance();
            }
            catch (Exception ex) {
                throw new RuntimeException("Failed to load JDBC driver (" +
                                           jdbcDriver+ "): " + 
                                           ex.getClass().getName() + ": " +
                                           ex.getMessage());
            }
        }
        return DriverManager.getConnection(url+dbname, dbuser, dbpass);
    }

    @Test
    public void dbExists() throws SQLException {
        connect();
    }

    public int countInputRecords() throws IOException {
        File protodir = new File(System.getProperty("test.protodir",
                                                    "prototype"));
        assertTrue(protodir.toString()+": Not an existing directory", 
                   protodir.isDirectory());
        File loadfile = new File(protodir, 
                                 System.getProperty("test.db.datafile",
                                                    "vocube.bar"));
        assertTrue(loadfile.toString()+": load-file not found", 
                   loadfile.exists());
        BufferedReader rdr = new BufferedReader(new FileReader(loadfile));

        String line = null;
        int c = 0;
        try {
            while ((line = rdr.readLine()) != null) c++;
        } finally {
            rdr.close();
        }
        assertTrue("load-file ("+loadfile.getName()+") appearsl to be empty", 
                   c > 0);
        // System.err.println("Found "+c+" records");
        return c;
    }

    @Test
    public void testRowCount() throws SQLException, IOException {
        int expect = countInputRecords();
        expect = 415;
        Connection conn = connect();
        String query = "SELECT count(*) FROM " + tableName + ";";
        try {
            ResultSet rs = conn.createStatement().executeQuery(query);
            rs.next();
            assertEquals(expect, rs.getInt(1));
        } finally {
            conn.close();
        }
    }


    @Test
    public void testInitialData() throws SQLException {
        String id = "alma/HD163296_Band6_ReferenceImages/HD163296_Band6.CalibCont.sc2.image.fits";
        Connection conn = connect();
        String query = "SELECT format FROM " + tableName + 
            " WHERE archiveid='"+id+"';";
        int c = 1;
        try {
            ResultSet rs = conn.createStatement().executeQuery(query);
            assertTrue("Failed to find record for "+id, rs.next());
            assertEquals("image/fits", rs.getString("format"));

            while (rs.next()) c++;
            assertEquals(1, c);
        } finally {
            conn.close();
        }
    }

    @Test
    public void testSpectralData() throws SQLException {
        String id = "ned/sting/ngc6951.co.cmnse.fits";
        Connection conn = connect();
        String query = 
            "SELECT spectralstart,spectrallocation,spectralstop FROM " + 
            tableName + " WHERE archiveid='"+id+"';";
        int c = 1;
        try {
            ResultSet rs = conn.createStatement().executeQuery(query);
            assertTrue("Failed to find record for "+id, rs.next());
            String[] cols = { "spectralstart", "spectrallocation", 
                              "spectralstop" };
            double val = 0.0;
            for(String col : cols) {
                val = rs.getDouble(col);
                assertTrue(col + " not set", val != 0.0);
                assertTrue(col+" out of expected range: "+Double.toString(val),
                           val > 0.002 && val < 0.003);
            }

            while (rs.next()) c++;
            assertEquals(1, c);
        } finally {
            conn.close();
        }
    }

    @Test
    public void testCorrection() throws SQLException {
        String id = "vla/NGC_520:I:1.4GHz:hg1996.LMV.fits.gz";
        Connection conn = connect();
        String query = "SELECT creationtype,fluxaxisunit FROM " + tableName + 
            " WHERE archiveid='"+id+"';";
        int c = 1;
        try {
            ResultSet rs = conn.createStatement().executeQuery(query);
            assertTrue("Failed to find record for "+id, rs.next());
            assertEquals("archival", rs.getString("creationtype"));
            assertEquals("JY/BEAM", rs.getString("fluxaxisunit"));

            while (rs.next()) c++;
            assertEquals(1, c);
        } finally {
            conn.close();
        }
    }

    @Test
    public void testMandatory() throws SQLException {
        Connection conn = connect();
        String[] cols = { "naxes", "nsubarrays", "format", "naxis1", 
                          "wcsaxes1" };

        try {
            for(String col : cols) {

                String query = "SELECT archiveid,"+col+" FROM " + tableName + 
                    " WHERE "+col+" IS NULL;";
                int c = 1;
                ResultSet rs = conn.createStatement().executeQuery(query);
                if (rs.next()) {
                    System.err.println("NULL values for "+col+":");
                    System.err.println(rs.getString("archiveid"));
                    while (rs.next()) {
                        System.err.println(rs.getString("archiveid"));
                    }
                    fail(col+" column includes NULL values");
                }
            } 
        } finally {
            conn.close();
        }   
    }

}