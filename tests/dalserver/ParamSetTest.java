package dalserver;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

public class ParamSetTest {

    ParamSet pset = null;

    @Before 
    public void setup() throws DalServerException, InvalidDateException {
        pset = new ParamSet();
    }

    @After 
    public void teardown() {
        pset = null;
    }

    @Test 
    public void testBasic() 
    {
      // Quick run through of methods to verify basic functionality
      Param p;
      String sval;

      try {

        // Initial size should be 0
        assertEquals(0, pset.size());
  
        // Add some parameters 
        p = new Param( "TargetName", EnumSet.of(ParamType.STRING), "NGC-4676", ParamLevel.CORE, true,"Target Name");
        pset.addParam( p );
  
        p = new Param("MaxRec", EnumSet.of(ParamType.INTEGER), null, ParamLevel.CORE, false, "Maximum number of output records");
        pset.addParam( p );
  
        p = new Param("Top", EnumSet.of(ParamType.INTEGER), null, ParamLevel.CLIENT, false, "Number of top-ranked items to return");
        pset.addParam( p );
  
        pset.addParam( "Type", "cutout" );
  
        // Should be 4 now.
        assertEquals(4, pset.size());
  
        assertTrue(pset.isDefined("TARGETNAME"));
        assertTrue(pset.isDefined("Type"));
        assertTrue(pset.isDefined("MaxRec"));
        assertTrue(pset.isDefined("Top"));
        assertFalse(pset.isDefined("Bogus"));
  
        // Extract Parameters
        p = pset.getParam("targetname");
        assertNotNull( p );
  
        p = pset.getParam("TYPE");
        assertNotNull( p );
  
        p = pset.getParam("MaxRec");
        assertNotNull( p );
  
        p = pset.getParam("Bogus");
        assertNull( p );
  
        // Assign some values  (all but "TOP" should have a value now)
        pset.setValue("maxrec", "100");
        pset.setValue("type", "archival");
  
        // Get Values
        sval = pset.getValue("top");        // "TOP" exists, but has no value.. returns NULL
        assertNull( sval );

        sval = pset.getValue("top", "10");  // "TOP" exists, but has no value.. returns NULL
        assertNull( sval );

        sval = pset.getValue("Bogus","DNE"); // "BOGUS" does not exist.. returns <defval>
        assertTrue( sval.equals("DNE") );

        // Iterate (uses entrySet)
        Iterator ii = pset.iterator();
        while ( ii.hasNext() )
        {
          p = (Param)((Map.Entry)ii.next()).getValue();
          sval = p.getName();
          assertNotNull(sval);
          //System.out.println("Param == "+sval);
        }

      }
      catch (Exception e ){
        fail( e.getMessage() );
        return;
      }

    } // end testBasic

}
