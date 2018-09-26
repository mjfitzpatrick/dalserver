package dalserver;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

public class RangeListTest {

    // RangeList class unit test
    // Constructors:
    //   RangeList()
    //   RangeList(ParamType type, boolean ordered)
    //   RangeList(String ranges, ParamType type, boolean ordered)
    //   RangeList(String ranges)
    //
    // Methods:
    //   int  parseRangeList(String ranges)
    //   void addRange(Range r) 
    //
    //   int    length()
    //   Range  getRange(int index)
    //   String stringValue(int index)
    //   double doubleValue(int index)
    //   int    intValue(int index)
    //   Date   dateValue(int index)
    //
    //   Iterator<Range> iterator()
    //
    //   void addProperty(String key, String value)
    //   String getProperty(String keyword)
    //   Iterator propertiesIterator()
    //
    //   String toString()

    // MCD NOTE: ISSUES
    //  
    //  0) not necessarily an issue.  addProperty( key, value ) replaces 
    //     null value with 'true', but allows null key.  
    //     Is that the desired behavior?
    //  
    //  1) add NULL (r=null) Range object does not fail.. however 
    //     For ordered list, subsequent calls to addRange() will cause a NPE when 
    //     Range.compareTo() does not expect a NULL range.
    //     Either this should fail, or comparitor should handle.
    //  
    //  2) Mixing "ANY" type with !"ANY" type in same list causes 
    //     ClassCastException from Range.compareTo().  The list content 
    //     expectations should be documented.. 
    //      a) cannot mix numeric and non-numeric ranges in ordered list.
    //  
    //  3) Ordered list, EQUAL ranges do not grow the list, or overwrites the 
    //     entry.. however, the RangeList length attribute is still incremented.
    //     (so RangeList.length != List.size() )
    //       all "ANY" type are considered EQUAL.
    //  
    //  4) addRangeList() mix numeric with !numeric == CCE, while in 
    //     parseRangeList() the same mix == DSE.
    //
    //  5) could use clear() to clear list and reset flags (esp. typeFixed).
    //     have to re-instantiate the list.
    //
    //  8) Cannot split range parts using ParamType constructors.
    //       RangeList( teststring[0] ) gives different results than.
    //       RangeList( teststring[0], ParamType.STRING, true )
    //
    //  9) RangeList.getRange(index) returns last range for index > Length s/b DalServerException for bad index.

    boolean verbose = false;

    RangeList ranges = null;

    String[] teststrings = { "a,e,b,cc,def,foo/bar,z"  ,  // String ranges
                             "1,7,3/2,5/,,2.5/4,/9,5/9",  // Numeric ranges
                             "1999-01-01,2007-01-01,2003-01-01/2002-01-01,2005-01-01/,,2002-06-01/2004-01-01,/2009-01-01,2005-01-01/2009-01-01",  // Date ranges
                             "52.0,-27.8;source,frame=ICRS",  // Numeric ranges with properties
                           };

    @Before 
    public void setup(){
        ranges = new RangeList();
    }

    @After 
    public void teardown() {
        ranges = null;
    }

    /**
     * Test accessors on empty content.
     *   - expecting DalServerException on most calls.. 
     *
     * Keeping them bundled in the same test because they use a common thread.
     *
     */
    @Test 
    public void testEmpty() 
    {
      int ii;
      Range r = null;
      Object val = null;

      // Check length
      assertEquals( 0, ranges.length() );

      // Get Property from empty properties attribute
      assertNull( ranges.getProperty("blah") );

      // Extract range from empty list.
      try {
	r = ranges.getRange(0);

        fail( "Expected DalServerExcpetion not thrown." );
      }
      catch ( DalServerException dse )
      {
        assertEquals("bad range index", dse.getMessage());
      }
      catch ( Exception e )
      {
        fail( e.getMessage() );
        return;
      }

      // Accessors.. 
      //   These all call getRange() and pull the formatted value from range.
      for ( ii = 0;  ii < 4;  ii++) 
      {
         try {
           switch (ii) 
           {
            case 0:  
              val = (Object)ranges.stringValue(0);
              break;
            case 1:  
              val = (Object)ranges.doubleValue(0);
              break;
            case 2:  
              val = (Object)ranges.intValue(0);
              break;
            case 3:  
              val = (Object)ranges.dateValue(0);
              break;
            default:
              break;
           }
   
           fail( "Expected DalServerExcpetion not thrown." );
         }
         catch ( DalServerException dse )
         {
           assertEquals("bad range index", dse.getMessage());
         }
         catch ( Exception e )
         {
           fail( e.getMessage() );
           return;
         }
      }

    } // end testEmpty

    /**
     * Test management of properties attribute.
     */
    @Test 
    public void testProperties() 
    {
      String key;
      String sval;
      Iterator iter;
      int count = 0;

      Map.Entry me;

      try
      {
        // Add a couple properties to the map.
        ranges.addProperty("eyes", "blue" );
        ranges.addProperty("hair", "brown");
        ranges.addProperty("height", "5ft 10in");

        // Get a bogus property 
        sval = ranges.getProperty("line.separator");
        assertNull( sval );

        // Get actual property and verify
        sval = ranges.getProperty("hair");
        assertEquals( "brown", sval );

        // Get property iterator.. 
        iter = ranges.propertiesIterator();
        while ( iter.hasNext() )
        {
          count += 1;
          me = (Map.Entry)iter.next();
          key  = (String)me.getKey();
          if ( key.equals("hair") )
            assertEquals( sval, me.getValue() );
        }
        assertEquals(3, count );


        // Add a couple properties to the map.. null key
        // Q: Should the list allow a null key?
        ranges.addProperty( null, "something" );

        // null value
        //   - RangeList code replaces null value with 'true'
        ranges.addProperty( "something", null );

        // Get property iterator.. 
        count = 0;
        iter = ranges.propertiesIterator();
        while ( iter.hasNext() )
        {
          count += 1;
          me = (Map.Entry)iter.next();
          key  = (String)me.getKey();
          if ( key == null ){
            assertEquals( "something", me.getValue() ); // null key has expected value.
          }
          else if ( key.equals("something") )
            assertEquals( "true", me.getValue() );  // null value replacement
        }
        assertEquals(5, count );

      }
      catch ( Exception e )
      {
	if ( verbose )
          System.out.println("ERROR: "+e.getMessage());
        fail( e.getMessage() );
        return;
      }

    }

    /**
     * Test addRange method to ordered range list
     */
    @Test 
    public void testAddRangeToOrdered() 
    {
      // Call test code.. use default ordered list
      AddRangeTest();
    }

    /**
     * Test addRange method to unordered range list
     */
    @Test 
    public void testAddRangeToUnordered() 
    {
      // make rangelist unordered
      ranges.ordered = false;

      // Call test code.. 
      AddRangeTest();
    }

    /**
     * Test parseRangeList method 
     *   - Basic test. null and empty input strings
     */
    @Test 
    public void testParseRangeListBasic() 
    {
      int nranges;

      try {
        // null input
        try {
          ranges.parseRangeList(null);
          fail( "Expected NullPointerException not thrown." );
  
        }
        catch ( NullPointerException npe )
        {
          if ( verbose )
            System.out.println(" Expected error: caught "+npe.getClass().getSimpleName());
        }
  
        // empty input
        nranges = ranges.parseRangeList("");
        assertEquals( 0, nranges );
        assertEquals( 0, ranges.length() );

      }
      catch ( DalServerException dse )
      {
        fail( dse.getMessage() );
        return;
      }

    }

    /**
     * Test parseRangeList method 
     *   - String type ranges
     */
    @Test 
    public void testParseRangeListofStrings() 
    {
      if ( verbose )
        System.out.println("Test Parse RangeList of Strings:");

      int nranges;

      if ( verbose )
        System.out.println( ranges.toString() );

      // parse set of string ranges
      try {
        nranges = ranges.parseRangeList(teststrings[0]);
        assertEquals( 7, nranges );
        assertEquals( 7, ranges.length() );

      }
      catch ( DalServerException dse )
      {
        fail( dse.getMessage() );
        return;
      }

      if ( verbose )
        System.out.println( ranges.toString() );

      //ranges.clear();
      ranges.rlo.clear();
      ranges.length=0;
      ranges = new RangeList(); // needed to clear typeFixed after loading.

      if ( verbose )
        System.out.println( ranges.toString() );

      // parse set of date ranges as strings
      try {
        nranges = ranges.parseRangeList(teststrings[2]);
        assertEquals( 7, nranges );
        assertEquals( 7, ranges.length() );
        if ( ranges.ordered )
        {
          // This ranges list has only 6 ranges on it.. but should have 7.
          System.out.println("".format("Test addRange - Issue 3: Ordered list, EQUAL ranges and length counter.") );
          System.out.println("".format("Test addRange -          NOTE: Values are not actually EQUAL!.") );
        }
      }
      catch ( DalServerException dse )
      {
        fail( dse.getMessage() );
        return;
      }
      if ( verbose )
        System.out.println( ranges.toString() );

    }

    /**
     * Test parseRangeList method 
     *   - Numeric type ranges
     */
    @Test 
    public void testParseRangeListofNumeric() 
    {
      int nranges;

      if ( verbose )
        System.out.println( ranges.toString() );

      try {
        // parse set of numeric ranges
        nranges = ranges.parseRangeList(teststrings[1]);
        assertEquals( 7, nranges );
        assertEquals( 7, ranges.length() );

      }
      catch ( DalServerException dse )
      {
        fail( dse.getMessage() );
        return;
      }
      if ( verbose )
        System.out.println( ranges.toString() );
    }

    /**
     * Test parseRangeList method 
     *   - Date type ranges
     */
    @Test 
    public void testParseRangeListofDates() 
    {
      if ( verbose )
        System.out.println( "Test parseRangeList( <dates> )" );

      int nranges;

      try {
        // Set expectations to Date format strings.
        ranges = new RangeList(ParamType.ISODATE, true );
  
        if ( verbose )
          System.out.println( ranges.toString() );

        // parse set of date ranges
        nranges = ranges.parseRangeList(teststrings[2]);

        assertEquals( 7, nranges );
        assertEquals( 7, ranges.length() );

        if ( verbose )
          System.out.println( ranges.toString() );

      }
      catch ( DalServerException dse )
      {
        fail( dse.getMessage() );
        return;
      }

    }

    /**
     * Test parseRangeList method 
     *   - Mix String and Numeric type ranges
     */
    @Test 
    public void testParseRangeListMixed() 
    {
      int nranges;

      // Load with String ranges
      if ( verbose )
        System.out.println( ranges.toString() );

      try {
        nranges = ranges.parseRangeList(teststrings[0]);
        assertEquals( 7, nranges );
        assertEquals( 7, ranges.length() );

      }
      catch ( DalServerException dse )
      {
        fail( dse.getMessage() );
        return;
      }

      // Add set of numeric ranges.  It should not matter if this is a separate call
      // or if the list has mixed types within the same string.  should get DSE.

      if ( verbose )
        System.out.println( ranges.toString() );

      try {
        nranges = ranges.parseRangeList(teststrings[1]);

        fail( "Expected DalServerException not thrown." );
      }
      catch ( DalServerException dse )
      {
        assertEquals("Types cannot be mixed in range list", dse.getMessage());
        ranges.rlo.clear();
        ranges.length=0;

        System.out.println("".format("Test parseRangeList - Issue 4: throws different exception for type mix than addRange.") );
        System.out.println("".format("Test parseRangeList - Issue 5: could use clear() to clear list and reset flags (esp. typeFixed).") );
        ranges = new RangeList(); // needed to clear typeFixed after loading.

      }
    }

    /**
     * Test parseRangeList method 
     *   - Numeric ranges with properties
     */
    @Test 
    public void testParseRangeListWithProperties() 
    {
      int nitems;
      String sval;

      if ( verbose )
        System.out.println( "Test parseRangeList() WithProperties:  "+ranges.toString() );

      // parse set of string ranges
      try {
        nitems = ranges.parseRangeList(teststrings[3]);
        assertEquals( 4, nitems );          // total number of ranges and properties.
        assertEquals( 2, ranges.length() ); // length of range list.

        // Get actual property and verify
        sval = ranges.getProperty("frame");
        assertEquals( "ICRS", sval );
      }
      catch ( DalServerException dse )
      {
        fail( dse.getMessage() );
        return;
      }

      if ( verbose )
        System.out.println( "Test parseRangeList() WithProperties:  "+ranges.toString() );

    }


    /**
     * Test RangeList( String ranges ) constructor
     */
    @Test 
    public void testConstructor2() 
    {
      try {
        // Create range list with string ranges
        ranges = new RangeList( teststrings[0] );
        assertTrue( ranges.ordered );
        assertFalse( ranges.numeric );
        assertFalse( ranges.isoDate );
        assertNull( ranges.properties );
  
        // Create range list with numeric ranges
        //  - since type is not fixed going in, should be numeric when done.
        ranges = new RangeList( teststrings[1] );
        assertTrue( ranges.ordered );
        assertTrue( ranges.numeric );
        assertFalse( ranges.isoDate );
        assertNull( ranges.properties );
  
        // Create range list with date ranges
        //  - date format is not auto-detected, so remains FALSE
        ranges = new RangeList( teststrings[2] );
        assertTrue( ranges.ordered );
        assertFalse( ranges.numeric );
        assertFalse( ranges.isoDate );
        assertNull( ranges.properties );

        // Create range list with numeric ranges including properties
        ranges = new RangeList( teststrings[3] );
        assertTrue( ranges.ordered );
        assertTrue( ranges.numeric );
        assertFalse( ranges.isoDate );
        assertEquals( 2, ranges.properties.size() );
      }
      catch ( DalServerException dse )
      {
        fail( dse.getMessage() );
        return;
      }
    }

    /**
     * Test RangeList( ParamType type, boolean ordered ) constructor
     */
    @Test 
    public void testConstructor3() 
    {
      try {
        // Create range list with string ranges
        ranges = new RangeList( ParamType.STRING, true );
        assertTrue( ranges.ordered );
        assertFalse( ranges.numeric );
        assertFalse( ranges.isoDate );
        assertNull( ranges.properties );
  
        // Create range list with numeric ranges
        ranges = new RangeList( ParamType.INTEGER, false );
        assertFalse( ranges.ordered );
        assertTrue( ranges.numeric );
        assertFalse( ranges.isoDate );
        assertNull( ranges.properties );
        assertNull( ranges.properties );
  
        // Create range list with date ranges
        ranges = new RangeList( ParamType.ISODATE, true );
        assertTrue( ranges.ordered );
        assertFalse( ranges.numeric );
        assertTrue( ranges.isoDate );
        assertNull( ranges.properties );

      }
      catch ( DalServerException dse )
      {
        fail( dse.getMessage() );
        return;
      }

      try {
        // Create range list with Boolean ranges
        ranges = new RangeList( ParamType.BOOLEAN, true );
        fail( "Expected DalServerException not thrown." );
      }
      catch ( DalServerException dse )
      {
        assertEquals("boolean range lists not supported", dse.getMessage());
      }

    }


    /**
     * Test RangeList( String ranges, ParamType type, boolean ordered ) constructor
     */
    @Test 
    public void testConstructor4() 
    {
      try {
        // Create range list with string ranges
        ranges = new RangeList( teststrings[0], ParamType.STRING, true );
        assertTrue( ranges.ordered );
        assertFalse( ranges.numeric );
        assertFalse( ranges.isoDate );
        assertEquals( 7, ranges.length() );
        if ( ranges.stringValue(2).equals("bar") )
        {
          fail("Issue 8 resolved?");
          assertEquals( "bar", ranges.stringValue(3) );
        }
        else
        {
          System.out.println("".format("Test Constructor4 - Issue 8: Cannot split range parts using ParamType constructors."));
        }
        assertNull( ranges.properties );
        System.out.println( "Test Constructor4(): "+ranges.toString() );
  
        // Create range list with numeric ranges
        ranges = new RangeList( teststrings[1], ParamType.INTEGER, true );
        assertTrue( ranges.ordered );
        assertTrue( ranges.numeric );
        assertFalse( ranges.isoDate );
        assertNull( ranges.properties );
        System.out.println( "Test Constructor4(): "+ranges.toString() );
  
        // Create range list with date ranges
        ranges = new RangeList( teststrings[2], ParamType.ISODATE, true );
        assertTrue( ranges.ordered );
        assertFalse( ranges.numeric );
        assertTrue( ranges.isoDate );
        assertNull( ranges.properties );

        // Create range list with numeric ranges with parameters
        ranges = new RangeList( teststrings[3], ParamType.FLOAT, true );
        assertTrue( ranges.ordered );
        assertTrue( ranges.numeric );
        assertFalse( ranges.isoDate );
        assertEquals( 2, ranges.properties.size() );
        System.out.println( "Test Constructor4(): "+ranges.toString() );

      }
      catch ( DalServerException dse )
      {
        fail( dse.getMessage() );
        return;
      }

      try {
        // Create range list with numeric ranges with wrong Type
        // This fails because the Range sees them as numeric, but the List is set to non-numeric.
        //
        ranges = new RangeList( teststrings[1], ParamType.STRING, true );
      }
      catch ( DalServerException dse )
      {
        assertEquals("Types cannot be mixed in range list", dse.getMessage());
      }

    }

    /**
     * testDateHandling()
     *   - It appears that the various paths to loading DATE ranges 
     *     produce different results.
     */
    @Test 
    public void testDateHandling() 
    {
       String[] results = new String[3];

       if ( verbose )
         System.out.println("Test DateHandling");

       try {
         System.out.println(" In - "+teststrings[2]);

         // Default RangeList constructor
         // ranges.isoDate = true;  // Set expectations to Date format strings. (FAILS Parse due to modified token list)
         ranges.isoDate = false;    // Set expectations to Strings
         ranges.parseRangeList( teststrings[2] );
         results[0] = ranges.toString();
         System.out.println(" T1 - "+ranges.toString());
         
         // RangeList constructor with specified type.
         ranges = new RangeList( ParamType.ISODATE, true );  // Does NOT set isoDate TRUE, Does use modified token list
         ranges = new RangeList( ParamType.STRING, true );   //  this also uses modifed token list.. so bad parse of date.
         ranges.parseRangeList( teststrings[2] );            // generates range from '1999' of '1999-01-01' as Numeric
         results[1] = ranges.toString();                     // generates Exception.
         System.out.println(" T2 - "+ranges.toString());
         
         // RangeList construct and parse in one go.
         ranges = new RangeList( teststrings[2], ParamType.ISODATE, true ); // Fails for the same reasons as above.
         results[2] = ranges.toString();
         System.out.println(" T3 - "+ranges.toString());

         // So the only successful interpretation of the Date range list is as a String set
         //  when we do NOT specify the data type in advance.

       }
       catch ( Exception e )
       {
        System.out.println("Caught "+e.getClass().getSimpleName()+": "+e.getMessage());
        fail( e.getMessage() );
       }

      System.out.println("Test DateHandling: 1 =: "+results[0]);
      System.out.println("Test DateHandling: 2 =: "+results[1]);
      System.out.println("Test DateHandling: 3 =: "+results[2]);

    }


    /**
     * testIssue8()
     *    Cannot split range parts using ParamType constructors.
     *       RangeList( teststrings[0] ) gives different results than.
     *       RangeList( teststrings[0], ParamType.STRING, true )
     */
    @Test 
    public void testIssue8() 
    {
      if ( verbose )
        System.out.println("Test Issue8():");

      String a = null;
      String b = null;

      try{
        ranges = new RangeList( teststrings[0] );
        a = ranges.toString();

        System.out.println("test Issue8 - A:" );
        System.out.println("test Issue8 - Ranges(2) = "+ranges.stringValue(2) );
        System.out.println("test Issue8 - Ranges(5) = "+ranges.stringValue(5) );
        System.out.println("test Issue8 - "+a);
        
        ranges = new RangeList( teststrings[0], ParamType.STRING, true );
        b = ranges.toString();

        System.out.println("test Issue8 - B:" );
        System.out.println("test Issue8 - Ranges(2) = "+ranges.stringValue(2) );
        System.out.println("test Issue8 - Ranges(5) = "+ranges.stringValue(5) );
        System.out.println("test Issue8 - "+b);
      }
      catch (DalServerException dse )
      {
        fail( dse.getMessage() );
      }

      if ( a.equals(b) ){ fail("Issue 8 resolved?");}
      //assertEquals(a, b);

      System.out.println("test Issue8 - Cannot split range parts using ParamType constructors.");
      System.out.println("test Issue8 -    RangeList( teststrings[0] ) gives different results than.");
      System.out.println("test Issue8 -    RangeList( teststrings[0], ParamType.STRING, true )");

    }

    /**
     * testGetRange()
     *   Verify getRange() behavior
     */
    @Test 
    public void testGetRange() 
    {
      if ( verbose )
        System.out.println("Test getRange():");

      Range r = null;

      // Extract range from empty list.
      try {
	r = ranges.getRange(0);

        fail( "Expected DalServerExcpetion not thrown." );
      }
      catch ( DalServerException dse )
      {
        assertEquals("bad range index", dse.getMessage());
      }
      catch ( Exception e )
      {
        fail( e.getMessage() );
        return;
      }

      try {
        // Add some ranges..
        ranges.parseRangeList( teststrings[1] );

        // should be 7 ranges on list
        // Get first and check correct
        r = ranges.getRange(0);
        assertEquals( 1, r.intValue1() );

        // Get inneer and check correct
        r = ranges.getRange(5);
        assertEquals( 7, r.intValue1() );

      }
      catch ( Exception e )
      {
        fail( e.getMessage() );
      }

      try {
	r = ranges.getRange(10);
        System.out.println("Test getRange - Issue 9: getRange(index) returns last range for index > Length");
        System.out.println( r.toString() );
        //fail( "Expected DalServerExcpetion not thrown." );
      }
      catch ( DalServerException dse )
      {
        fail( "Issue 9 resolved?" );
        //assertEquals("bad range index", dse.getMessage());
      }
      catch ( Exception e )
      {
        fail( e.getMessage() );
        return;
      }


    }

    private void AddRangeTest() 
    {
      Range r = null;
      Iterator iter;
      int count;

      if ( ranges.ordered )
      {
        // Issue 1: when resolved, this can reduce to attempted add of null range to 
	//          ranges catching expected exception.
        try
        {
          // add a null range
           ranges.addRange( r );

          // add another range
          r = new Range( RangeType.ANY );
          ranges.addRange( r );

          fail("Issue 1 resolved?");
        }
        catch ( NullPointerException npe )
        {
          System.out.println("".format("Test addRange - Issue 1: null range makes ordered list unusable (NPEs).") );
        }
        catch ( Exception e )
        {
          fail( e.getMessage() );
        }

        // Start over
        ranges = new RangeList();
      }


      try
      {
        if ( !ranges.ordered )
        {
          // add a null range
           ranges.addRange( r );
        }

        // Add some "ANY" type ranges
        r = new Range( RangeType.ANY );
        assertNotNull( r );
        ranges.addRange( r );

        r = new Range( RangeType.ANY );
        assertNotNull( r );
        ranges.addRange( r );

        // add a couple other ranges
        r = new Range( RangeType.CLOSED, "1", "10" );
        assertNotNull( r );

        try {
          ranges.addRange( r );

          if ( ranges.ordered )
          {
            fail( "Issue 2 resolved?");
          }
        }
        catch ( ClassCastException cce )
        {
          if ( ranges.ordered )
          {
            System.out.println("".format("Test addRange - Issue 2: mix ANY and !ANY types on ordered list (CCE).") );
            ranges.rlo.clear();
            ranges.length = 0;
            ranges.addRange( r );
          }
          else
          {
            fail( cce.getMessage() );
            return;
          }
        }

        // add some other compatible ranges
        r = new Range( RangeType.HIVAL, "7");
        assertNotNull( r );
        ranges.addRange( r );

        r = new Range( RangeType.LOVAL, "5");
        assertNotNull( r );
        ranges.addRange( r );

        r = new Range( RangeType.CLOSED, "-10", "20" ); // same mid as LOVAL
        assertNotNull( r );
        ranges.addRange( r );

        r = new Range( RangeType.CLOSED, "1", "10" ); // same as first
        assertNotNull( r );
        ranges.addRange( r );

        // add a non-numeric range
        r = new Range( RangeType.CLOSED, "aaa", "zzz" );
        assertNotNull( r );

        try {
          ranges.addRange( r );
          if ( ranges.ordered )
          {
            fail( "Issue 2a resolved?");
          }
        }
        catch ( ClassCastException cce )
        {
          if ( ranges.ordered )
          {
            System.out.println("".format("Test addRange - Issue 2a: mix numeric and !numeric types on ordered list (CCE).") );
          }
          else
          {
            fail( cce.getMessage() );
            return;
          }
        }

        iter = ranges.iterator();
        count = 0;
        while ( iter.hasNext() )
        {
          count += 1;
          r = (Range)iter.next();
          if ( verbose )
          {
            if ( r == null )
              System.out.println("Range "+count+" -- null");
            else
              System.out.println("Range "+count+" -- "+r.toString());
          }
        }
        if ( verbose )
          System.out.println("RangeList length == "+ranges.length() );
        if ( ranges.ordered )
        {
          System.out.println("".format("Test addRange - Issue 3: Ordered list, EQUAL ranges and length counter.") );
          assertEquals(" Issue 3 resolved?", ranges.length(), count+1 );
        }
        else
          assertEquals( ranges.length(), count );



      }
      catch ( Exception e )
      {
        fail( e.getMessage() );
        return;
      }

      return;
    }

 }
