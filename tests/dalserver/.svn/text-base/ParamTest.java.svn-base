package dalserver;

import java.util.EnumSet;
import java.util.Date;
import java.util.TimeZone;
import java.text.SimpleDateFormat;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Param Class Test Suite
 *   Constructors:
 *     The Param class constructors all fall back to the most complex flavor.
 *     By testing the various combinations of that constructor, the simpler
 *     cases are included.
 *
 *     Param(String name, EnumSet<ParamType> type, String value, ParamLevel level, boolean isSet, String descr)
 *     Param(String name, EnumSet<ParamType> type, String descr)
 *     Param(String name, EnumSet<ParamType> type, String value, String descr)
 *     Param(String name, String value, ParamLevel level)
 *     Param(String name, String value)
 *
 *     ParamType options: STRING, BOOLEAN, INTEGER, FLOAT, ISODATE, ORDERED, RANGELIST
 *
 *   Methods:
 *     void setValue(String newValue)
 *     String getName()
 *     EnumSet<ParamType> getType()
 *     ParamLevel getLevel()
 *     void setLevel(ParamLevel level)
 *     String getDescription()
 *     boolean isSet()
 *     String stringValue()
 *     boolean booleanValue()
 *     int intValue()
 *     double doubleValue()
 *     java.util.Date dateValue()
 *     RangeList rangeListValue()
 *     String toString()
 *
 *   Issues:
 *    1. required to catch InvalidDateException on constructors where value
 *       can not be interpreted as a Date.
 *    2. Date type not protected against null value (NPE).
 *    3. ORDERED type not expected by itself (NPE).
 *    4. No mutator for isSet attribute, only set on construction.  
 *        Can not change setting in conjunction with setValue() and does 
 *        not get changed internally.
 *    5. Param.setValue() numeric param with non-numeric value does not fail unless try to access as number..
 *    6. ParamType list could enforce combination restrictions (e.g. ! "ParamType.FLOAT,ParamType.INTEGER" )
 *
 */
public class ParamTest {

    Param p = null;

    Param sp = null; // String  type parameter
    Param bp = null; // Boolean type parameter
    Param ip = null; // Integer type parameter
    Param fp = null; // Float   type parameter
    Param dp = null; // Date    type parameter

    Param rip = null; // Integer-RangeList type parameter

    String sval = "CCR";
    Boolean bval = new Boolean(true);
    Integer ival = new Integer(7);
    Float   fval = new Float(3.14159);
    String  dval = "1969-07-20";
    String  rvals = "/7,10/12,15/";

    boolean verbose = true;

    @Before 
    public void setup(){

       // Test Params
      try{
       p  = new Param( "band", EnumSet.of(ParamType.STRING), "optical", ParamLevel.CORE, true, "spectral band" );

       sp = new Param( "band", EnumSet.of(ParamType.STRING),  null, ParamLevel.CORE,      false, null );
       bp = new Param( "band", EnumSet.of(ParamType.BOOLEAN), null, ParamLevel.EXTENSION, false, null );
       ip = new Param( "band", EnumSet.of(ParamType.INTEGER), null, ParamLevel.CLIENT,    false, null );
       fp = new Param( "band", EnumSet.of(ParamType.FLOAT)  , null, ParamLevel.SERVICE,   false, null );
       dp = new Param( "band", EnumSet.of(ParamType.ISODATE),   "", ParamLevel.CORE,      false, null );

       rip = new Param( "iset", EnumSet.of(ParamType.RANGELIST,ParamType.INTEGER), null, ParamLevel.CORE,    false, null );

      }
      catch ( DalServerException dse )
      {
        fail( dse.getMessage() );
      }
      catch ( InvalidDateException ide )
      {
        fail( ide.getMessage() );
      }

    }

    @After 
    public void teardown() {
    }

    /**
     *  Param Constructor Tests:
     * 
     */

    @Test 
    /**
     *  Test Constructors
     */
    public void testConstructor1()
    {
      if ( verbose )
        System.out.println("Test Constructor1:");

      System.out.println("".format("  Issue 1: Required to catch InvalidDateException on constructors where value can not be interpreted as a Date.") );

      try{
        p = new Param( null, null );
      }
      catch ( DalServerException dse )
      {
        assertEquals("Attempt to create param with null name", dse.getMessage() );
      }
      catch ( InvalidDateException ide )
      {
        fail( ide.getMessage() );
      }

      try{
        p = new Param( "band", null );
        assertNotNull(p);
      }
      catch ( DalServerException dse )
      {
        fail( dse.getMessage() );
      }
      catch ( InvalidDateException ide )
      {
        fail( ide.getMessage() );
      }

      try{
        p = new Param( "band", "optical" );
        assertNotNull(p);
      }
      catch ( DalServerException dse )
      {
        fail( dse.getMessage() );
      }
      catch ( InvalidDateException ide )
      {
        fail( ide.getMessage() );
      }

    }

    @Test 
    /**
     *  Test Constructors 
     *   - tests tolerance to null input arguments.
     */
    public void testConstructor2()
    {
      if ( verbose )
        System.out.println("Test Constructor2:");

      try{
        p = new Param( "band", EnumSet.of(ParamType.STRING), "optical", ParamLevel.CORE, true, "spectral bandpass" );
        assertNotNull( p );
      }
      catch ( DalServerException dse )
      {
        fail( dse.getMessage() );
      }
      catch ( InvalidDateException ide )
      {
        fail( ide.getMessage() );
      }

      try{
        p = new Param( null, EnumSet.of(ParamType.STRING), "optical", ParamLevel.CORE, true, "spectral bandpass" );
        fail( "Expected DalServerException not thrown." );
      }
      catch ( DalServerException dse )
      {
        assertEquals("Attempt to create param with null name", dse.getMessage() );
      }
      catch ( InvalidDateException ide )
      {
        fail( ide.getMessage() );
      }

      try{
        p = new Param( "band", null, "optical", ParamLevel.CORE, true, "spectral bandpass" );
        fail( "Expected DalServerException not thrown." );
      }
      catch ( DalServerException dse )
      {
        assertEquals("Attempt to create param with null type[band]", dse.getMessage() );
      }
      catch ( InvalidDateException ide )
      {
        fail( ide.getMessage() );
      }

      try{
        p = new Param( "band", EnumSet.of(ParamType.STRING), null, ParamLevel.CORE, true, "spectral bandpass" );
        assertNotNull( p );

        p = new Param( "band", EnumSet.of(ParamType.STRING), "optical", null, true, "spectral bandpass" );
        fail( "Expected DalServerException not thrown." );
      }
      catch ( DalServerException dse )
      {
        assertEquals("Attempt to create param with null level[band]", dse.getMessage() );
      }
      catch ( InvalidDateException ide )
      {
        fail( ide.getMessage() );
      }

      try{
        p = new Param( "band", EnumSet.of(ParamType.STRING), "optical", ParamLevel.CORE, true, null );
        assertNotNull( p );
      }
      catch ( DalServerException dse )
      {
        fail( dse.getMessage() );
      }
      catch ( InvalidDateException ide )
      {
        fail( ide.getMessage() );
      }
    }


    @Test 
    /**
     *  Test Constructors
     *   - Value is allowed to be null, is it OK for all Types?
     */
    public void testConstructor3()
    {
      if ( verbose )
        System.out.println("Test Constructor3:");

      for (ParamType type: ParamType.values() )
      {
        if ( verbose )
          System.out.println(" Type = "+type);

        try{
          if ( type == ParamType.ORDERED )
          {
            System.out.println("".format("  Issue 3: Ordered type not expected by itself (NPE).") );
            continue;
          }

          p = new Param( "band", EnumSet.of(type), null, ParamLevel.CORE, true, "param description" );
          assertNotNull(p);

          if ( type == ParamType.ISODATE )
            fail("Expected exception not thrown.. Issue 2 resolved?");
        }
        catch ( DalServerException dse )
        {
          fail( dse.getMessage() );
        }
        catch ( InvalidDateException ide )
        {
          fail( ide.getMessage() );
        }
        catch ( NullPointerException npe )
        {
          if ( type == ParamType.ISODATE )
            System.out.println("".format("  Issue 2: Date type not protected against null value (NPE).") );
          else
            fail( npe.getMessage() );
        }

      }

      // Combination of Types
      try {
        p = new Param( "sample", EnumSet.of(ParamType.RANGELIST,ParamType.INTEGER), null, ParamLevel.CORE,    false, null );
        assertNotNull(p);

        p = new Param( "sample", EnumSet.of(ParamType.RANGELIST,ParamType.ORDERED,ParamType.INTEGER), null, ParamLevel.CORE,    false, null );
        assertNotNull(p);

        p = new Param( "sample", EnumSet.of(ParamType.ORDERED,ParamType.INTEGER), null, ParamLevel.CORE,    false, null );
        assertNotNull(p);

        // For this one, it looks like FLOAT is used, INTEGER ignored.
        p = new Param( "sample", EnumSet.of(ParamType.FLOAT,ParamType.INTEGER), null, ParamLevel.CORE,    false, null );
        assertNotNull(p);
      }
      catch ( DalServerException dse )
      {
        fail( dse.getMessage() );
      }
      catch ( InvalidDateException ide )
      {
        fail( ide.getMessage() );
      }

    }


    @Test 
    /**
     *  Test mutator to value attribute
     * 
     */
    public void testSetValue()
    {
      // Set value with compatible strings.
      try {
        this.setParams();
      }
      catch ( RuntimeException e ) {
        fail( e.getMessage() );
      }

      // Set Date type with non-date string
      try {
        dp.setValue("blah");
        fail("Expected InvalidDateException not thrown");
      }
      catch ( DalServerException dse )
      {
        fail( dse.getMessage() );
      }
      catch ( InvalidDateException ide ) {}

      // Set Numeric type with non-date string
      try {
        fp.setValue("blah");
        System.out.println("".format("  Issue 5: set numeric param with non-numeric value does not fail unless try to access as number.") );

        fp.doubleValue();
        fail("Expected exception not thrown.");
      }
      catch ( DalServerException dse )
      {
        fail( dse.getMessage() );
      }
      catch ( InvalidDateException ide )
      {
        fail( ide.getMessage() );
      }
      catch ( NumberFormatException nfe ){}

    }

    @Test 
    /**
     *  Test accessor to name attribute
     * 
     */
    public void testGetName()
    {
      if ( verbose )
        System.out.println("Test getName():");

      assertEquals("band", p.getName());
    }

    @Test 
    /**
     *  Test accessor to type attribute
     * 
     */
    public void testGetType()
    {
      EnumSet types = null;

      if ( verbose )
        System.out.println("Test getType():");

      types = sp.getType();
      assertTrue( types.contains(ParamType.STRING) );

      types = fp.getType();
      assertTrue( types.contains(ParamType.FLOAT) );

      System.out.println("  MCD TODO: Param with >1 Type.. ORDERED,INTEGER ");
    }

    @Test 
    /**
     *  Test accessor to level attribute
     * 
     */
    public void testGetLevel()
    {
      if ( verbose )
        System.out.println("Test getLevel():");

      assertEquals( ParamLevel.CORE,      sp.getLevel() );
      assertEquals( ParamLevel.EXTENSION, bp.getLevel() );
      assertEquals( ParamLevel.CLIENT,    ip.getLevel() );
      assertEquals( ParamLevel.SERVICE,   fp.getLevel() );
    }

    @Test 
    /**
     *  Test mutator to level attribute
     * 
     */
    public void testSetLevel()
    {

      if ( verbose )
        System.out.println("Test setLevel():");

      p.setLevel( null );
      assertNull( p.getLevel() );

      p.setLevel( ParamLevel.CLIENT );
      assertEquals( ParamLevel.CLIENT, p.getLevel() );

    }

    @Test 
    /**
     *  Test accessor to description attribute
     * 
     */
    public void testGetDescription()
    {
      if ( verbose )
        System.out.println("Test getDescription():");

      assertEquals( "spectral band", p.getDescription() );
      assertNull( ip.getDescription() );
    }

    @Test 
    /**
     *  Test accessor to isSet attribute
     * 
     */
    public void testIsSet()
    {
      if ( verbose )
        System.out.println("Test isSet():");

      assertTrue( p.isSet() );
      assertFalse( ip.isSet() );
    }

    @Test 
    /**
     *  Test accessor for value as String
     * 
     */
    public void testStringValue()
    {
      try {
        this.setParams();
      }
      catch ( RuntimeException e ) {
        fail( e.getMessage() );
      }

      assertEquals( sval, sp.stringValue() );
      assertEquals( "true", bp.stringValue() );
      assertEquals( "7", ip.stringValue() );
      assertEquals( "3.14159", fp.stringValue() );
      assertEquals( "1969-07-20", dp.stringValue() );

    }

    @Test 
    /**
     *  Test accessor for value as boolean
     * 
     */
    public void testBooleanValue()
    {
      try {
        this.setParams();
      }
      catch ( RuntimeException e ) {
        fail( e.getMessage() );
      }

      // Should be fine for boolean parameter.
      Boolean value;

      value = bp.booleanValue();
      assertEquals( bval, value );

      // Should be false for non-boolean parameter. (default)
      value = sp.booleanValue();
      assertEquals( false, value );

      value = ip.booleanValue();
      assertEquals( false, value );

      value = fp.booleanValue();
      assertEquals( false, value );

      value = dp.booleanValue();
      assertEquals( false, value );
    }

    @Test 
    /**
     *  Test accessor for value as int
     * 
     */
    public void testIntValue()
    {
      try {
        this.setParams();
      }
      catch ( RuntimeException e ) {
        fail( e.getMessage() );
      }

      // Should be fine for numerical  parameters, int and float.
      Integer value;

      value = ip.intValue();
      assertEquals( ival, value );

      value = fp.intValue();
      assertEquals( fval.intValue(), value.intValue() );

      // Throws NumberFormatException for others
      try {
        value = bp.intValue();
        fail("Expected NumberFormatException not thrown.");
      }
      catch ( NumberFormatException nfe ){}

      try {
        value = sp.intValue();
        fail("Expected NumberFormatException not thrown.");
      }
      catch ( NumberFormatException nfe ){}

      try {
        value = dp.intValue();
        fail("Expected NumberFormatException not thrown.");
      }
      catch ( NumberFormatException nfe ){}

    }

    @Test 
    /**
     *  Test accessor for value as double
     * 
     */
    public void testDoubleValue()
    {
      try {
        this.setParams();
      }
      catch ( RuntimeException e ) {
        fail( e.getMessage() );
      }

      // Should be fine for numerical  parameters, int and float.
      Double value;

      value = fp.doubleValue();
      assertEquals( fval.doubleValue(), value.doubleValue(), 1e-5 );

      value = ip.doubleValue();
      assertEquals( ival.doubleValue(), value.doubleValue(), 1e-5 );

      // Throws NumberFormatException for others
      try {
        value = bp.doubleValue();
        fail("Expected NumberFormatException not thrown.");
      }
      catch ( NumberFormatException nfe ){}

      try {
        value = sp.doubleValue();
        fail("Expected NumberFormatException not thrown.");
      }
      catch ( NumberFormatException nfe ){}

      try {
        value = dp.doubleValue();
        fail("Expected NumberFormatException not thrown.");
      }
      catch ( NumberFormatException nfe ){}
    }

    @Test 
    /**
     *  Test accessor for value as Date
     * 
     */
    public void testDateValue()
    {
      try {
        this.setParams();
      }
      catch ( RuntimeException e ) {
        fail( e.getMessage() );
      }

      // Should be fine for Date parameter, others NULL
      Date value;

      value = sp.dateValue();
      assertNull( value );

      value = bp.dateValue();
      assertNull( value );

      value = ip.dateValue();
      assertNull( value );

      value = fp.dateValue();
      assertNull( value );

      value = dp.dateValue();

      SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
      df.setTimeZone( TimeZone.getTimeZone("GMT") );

      assertEquals( dval, df.format(value) );

    }

    @Test 
    /**
     *  Test accessor for value as RangeList
     * 
     */
    public void testRangeListValue()
    {
      try {
        this.setParams();
      }
      catch ( RuntimeException e ) {
        fail( e.getMessage() );
      }

      RangeList value;

      // All non-range parameters should return null
      value = sp.rangeListValue();
      assertNull( value );

      value = bp.rangeListValue();
      assertNull( value );

      value = ip.rangeListValue();
      assertNull( value );

      value = fp.rangeListValue();
      assertNull( value );

      value = dp.rangeListValue();
      assertNull( value );

      value = rip.rangeListValue();
      assertNotNull( value );
    }


    private void setParams() throws RuntimeException
    {
      // For use by specialty accessor method tests.
      //
      // Assign 'good' values to each of the test parameters

      try{
        sp.setValue(sval);
        bp.setValue(bval.toString());
        ip.setValue(ival.toString());
        fp.setValue(fval.toString());
        dp.setValue(dval);

        rip.setValue(rvals);
      }
      catch ( DalServerException dse )
      {
        throw new RuntimeException( dse.getMessage() );
      }
      catch ( InvalidDateException ide )
      {
        throw new RuntimeException( ide.getMessage() );
      }
    }
}
