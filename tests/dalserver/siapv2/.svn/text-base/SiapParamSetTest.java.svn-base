package dalserver.siapv2;

import dalserver.DalServerException;
import dalserver.InvalidDateException;


import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileNotFoundException;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

public class SiapParamSetTest {

    SiapParamSet pset = null;

    @Before 
    public void setup() throws DalServerException, InvalidDateException {
        pset = new SiapParamSet();
    }

    @After 
    public void teardown() {
        pset = null;
    }

    @Test 
    public void testSize() {
        // Check number of parameters created.
        assertEquals(31, pset.size());
    }

    @Test 
    public void testIsDefined() {
        // Random check of some parameters case in-sensitive matching
        assertTrue(pset.isDefined("VERSION"));
        assertTrue(pset.isDefined("REQUEST"));
        assertTrue(pset.isDefined("TARGETNAME"));
        assertTrue(pset.isDefined("MODTIME"));
        assertTrue(pset.isDefined("RunID"));
    }

    @Test 
    public void testDefaults() {
        // Check default value assignments
        String sval;

        try {
	  // no default
  	  sval = pset.getValue("MODE");
          assertNull( sval );

	  // default
  	  sval = pset.getValue("VERSION");
          assertTrue( sval.equals("2.0") );
        }
	catch (Exception e)
        {
	  fail( e.getMessage() );
	  return;
        }
    }

    @Test 
    public void testToString() {
        // check self-rep method

        String sval;

        try {
  	  sval = pset.toString();

          assertNotNull( sval );
          assertEquals( 2988, sval.length() );

	  //System.out.println("TEMP: ToString length = "+sval.length() );
	  //System.out.println("TEMP: ToString result");
	  //System.out.println(sval);

        }
	catch (Exception e)
        {
	  fail( e.getMessage() );
	  return;
        }
    }

    @Test 
    public void testWrite() {
        // check write method

        String base = System.getProperty("test.outdir");
        String sep  = System.getProperty("file.separator");
        String name = "siapv2_paramset.txt";

        String filename = base+sep+"out"+sep+name;
        String baseline = base+sep+"baseline"+sep+name;

        try {
          // Set some typical parameter values.
          pset.setValue("REQUEST", "accessData");
          pset.setValue("POS", "282.1,67.12;ICRS");
          pset.setValue("SIZE", "0.1");
          pset.setValue("BAND", "8.0e-04/1.209e-6;source");
          //pset.setValue("TIME", "1999-01-01/2014-01-01");
          pset.setValue("FORMAT", "fits");
          pset.setValue("MODE", "cutout");
          pset.setValue("SPECRES", "2000.0");
          pset.setValue("COMPRESS", "true");

        }
        catch ( Exception dse )
	{
	  fail( dse.getMessage() );
        }

        try {
          pset.write( filename );

          assertTrue( verifyOutput( baseline, filename ) );
          
        }
	catch (Exception e)
        {
	  fail( e.getMessage() );
	  return;
        }
    }

    private boolean verifyOutput( String baseline, String outfile ) 
    {
      boolean result = true;
      boolean done   = false;

      BufferedReader expected;
      BufferedReader actual;

      String eline;
      String aline;
      int count;

      try {
        expected = new BufferedReader(new FileReader( baseline ) );
        actual   = new BufferedReader(new FileReader( outfile  ) );
      }
      catch (FileNotFoundException fnf ){
         System.out.println( fnf.getMessage() );
         return false;
      }

      count = 0;
      while (!done ){
        try{
          count = count + 1;
          eline = expected.readLine();
          aline = actual.readLine();
	  
          if (( eline == null )&&( aline != null ) )
            result = false;
          else if (( aline == null )&&( eline != null ) )
            result = false;
          else if (( aline == null )&&( eline == null ) )
            done = true;
          else
            result = eline.equals(aline);

          if (!result )
          {
            System.out.println("".format("Line %d differs",count));
            System.out.println("  Expected: "+eline);
            System.out.println("    Actual: "+aline);
            done = true;
          }
        }
        catch ( IOException ie ){
          result = false;
        }

      }
      return result;
    }
}
