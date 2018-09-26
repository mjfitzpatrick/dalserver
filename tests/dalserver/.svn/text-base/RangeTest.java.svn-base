package dalserver;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Range Class Test Suite
 *   Constructors:
 *     The Range class constructors all fall back to the most complex flavor.
 *     By testing the various combinations of that constructor, the simpler
 *     cases are included.
 *
 *   Issues:
 *     1. NPE if value1 is null. Thrown attempting conversion to number.
 *     2. ANY type forces numeric=true for non-numeric values.
 *     3. CLOSED type should require two non-null values.
 *     4. Numeric value is valid Date?? Possible DateParser issue
 *     5. ANY type does not validate values.
 *     6. Single value types allow second non-null value.
 *     7. Single value types sort value pair
 *     8. ANY type does NOT sort value pair
 *     9. Single value types allow access to second value.
 *    10. Constructors without isoDate argument throw InvalidDateException
 *         with NO possiblity of being interpreted as a Date.
 *    11. Single value types, string rep != dtype rep when second value is null.
 *    12. ANY type, generates NPE accessing values as numeric.
 *    13. Single value type, bogus second value effects midval and compare.
 *    14. String comparisons do not handle mixed null on value2. (HIVAL w/ CLOSED)
 *
 */
public class RangeTest {

    Range r = null;

    // Expected results
    String  exp_constructor;
    boolean exp_numeric;
    boolean exp_isdate;
    String  exp_value1;
    String  exp_value2;

    // Test values
    String[] nullvals = {        null,        null};
    String[] datevals = {"1999-08-29","2013-08-29"};
    String[] numvals  = {        "10",       "100"};
    String[] strvals  = {     "alpha",     "omega"};

    int testnum;

    @Before 
    public void setup(){
    }

    @After 
    public void teardown() {
    }

    /**
     *  Range Constructor Tests:
     * 
     * test           type   v1    v2   order   date
     *
     *   1. Range(    ANY,  null,  null, false, false )
     *   2. Range(  LOVAL,  null,  null, false, false )
     *   3. Range(  HIVAL,  null,  null, false, false )
     *   4. Range( ONEVAL,  null,  null, false, false )
     *   5. Range( CLOSED,  null,  null, false, false )
     *   		           
     *   6. Range(    ANY,  null,  null,  true, false )
     *   7. Range(  LOVAL,  null,  null,  true, false )
     *   8. Range(  HIVAL,  null,  null,  true, false )
     *   9. Range( ONEVAL,  null,  null,  true, false )
     *  10. Range( CLOSED,  null,  null,  true, false )
     *			           
     *  11. Range(    ANY,  null,  null, false,  true )
     *  12. Range(  LOVAL,  null,  null, false,  true )
     *  13. Range(  HIVAL,  null,  null, false,  true )
     *  14. Range( ONEVAL,  null,  null, false,  true )
     *  15. Range( CLOSED,  null,  null, false,  true )
     *   		           
     *  16. Range(    ANY,  null,  null,  true,  true )
     *  17. Range(  LOVAL,  null,  null,  true,  true )
     *  18. Range(  HIVAL,  null,  null,  true,  true )
     *  19. Range( ONEVAL,  null,  null,  true,  true )
     *  20. Range( CLOSED,  null,  null,  true,  true )
     *   		    
     *  21. Range(    ANY,  null,  num2, false, false )
     *  22. Range(  LOVAL,  null,  num2, false, false )
     *  23. Range(  HIVAL,  null,  num2, false, false )
     *  24. Range( ONEVAL,  null,  num2, false, false )
     *  25. Range( CLOSED,  null,  num2, false, false )
     *   
     *  TODO (21-25 with order=true and/or date=true ..15 tests)
     *   
     *  41. Range(    ANY,  null, date2, false, false )
     *  42. Range(  LOVAL,  null, date2, false, false )
     *  43. Range(  HIVAL,  null, date2, false, false )
     *  44. Range( ONEVAL,  null, date2, false, false )
     *  45. Range( CLOSED,  null, date2, false, false )
     *   
     *  TODO (41-55 with order=true and/or date=true ..15 tests)
     *   
     *  61. Range(    ANY,  null,  str2, false, false )
     *  62. Range(  LOVAL,  null,  str2, false, false )
     *  63. Range(  HIVAL,  null,  str2, false, false )
     *  64. Range( ONEVAL,  null,  str2, false, false )
     *  65. Range( CLOSED,  null,  str2, false, false )
     *   
     *  TODO (61-65 with order=true and/or date=true ..15 tests)
     *  
     *  81. Range(    ANY,  num1,  null, false, false )
     *  82. Range(  LOVAL,  num1,  null, false, false )
     *  83. Range(  HIVAL,  num1,  null, false, false )
     *  84. Range( ONEVAL,  num1,  null, false, false )
     *  85. Range( CLOSED,  num1,  null, false, false )
     *   
     *  TODO (81-85 with order=true, date=false ..5 tests)
     *   
     *  91. Range(    ANY,  num1,  null, false,  true )
     *  92. Range(  LOVAL,  num1,  null, false,  true )
     *  93. Range(  HIVAL,  num1,  null, false,  true )
     *  94. Range( ONEVAL,  num1,  null, false,  true )
     *  95. Range( CLOSED,  num1,  null, false,  true )
     *   
     *  TODO (91-95 with order=true, date=true ..5 tests)
     *   
     * 101. Range(    ANY, date1,  null, false, false )
     * 102. Range(  LOVAL, date1,  null, false, false )
     * 103. Range(  HIVAL, date1,  null, false, false )
     * 104. Range( ONEVAL, date1,  null, false, false )
     * 105. Range( CLOSED, date1,  null, false, false )
     *   
     *  TODO (101-105 with order=true, date=false ..5 tests)
     *   
     * 111. Range(    ANY, date1,  null, false,  true )
     * 112. Range(  LOVAL, date1,  null, false,  true )
     * 113. Range(  HIVAL, date1,  null, false,  true )
     * 114. Range( ONEVAL, date1,  null, false,  true )
     * 115. Range( CLOSED, date1,  null, false,  true )
     *   
     *  TODO (111-115 with order=true, date=true ..5 tests)
     *   
     * 121. Range(    ANY,  str1,  null, false, false )
     * 122. Range(  LOVAL,  str1,  null, false, false )
     * 123. Range(  HIVAL,  str1,  null, false, false )
     * 124. Range( ONEVAL,  str1,  null, false, false )
     * 125. Range( CLOSED,  str1,  null, false, false )
     *   
     *  TODO (121-125 with order=true, date=false ..5 tests)
     *   
     * 131. Range(    ANY,  str1,  null, false,  true )
     * 132. Range(  LOVAL,  str1,  null, false,  true )
     * 133. Range(  HIVAL,  str1,  null, false,  true )
     * 134. Range( ONEVAL,  str1,  null, false,  true )
     * 135. Range( CLOSED,  str1,  null, false,  true )
     *   
     *  TODO (131-135 with order=true, date=true ..5 tests)
     *   
     * 141. Range(    ANY,  num1,  num2, false, false )
     * 142. Range(  LOVAL,  num1,  num2, false, false )
     * 143. Range(  HIVAL,  num1,  num2, false, false )
     * 144. Range( ONEVAL,  num1,  num2, false, false )
     * 145. Range( CLOSED,  num1,  num2, false, false )
     *   
     * 146. Range(    ANY,  num2,  num1,  true, false )
     * 147. Range(  LOVAL,  num2,  num1,  true, false )
     * 148. Range(  HIVAL,  num2,  num1,  true, false )
     * 149. Range( ONEVAL,  num2,  num1,  true, false )
     * 150. Range( CLOSED,  num2,  num1,  true, false )
     *   
     *  TODO (141-145 with order=false, date=true ..5 tests)
     *  TODO (146-150 with order=true,  date=true ..5 tests)
     *   
     *  TODO (171-175 with order=false, date=false ..5 tests)
     *  TODO (176-180 with order=true,  date=false ..5 tests)
     *   
     * 171. Range(    ANY, date1, date2, false,  true )
     * 172. Range(  LOVAL, date1, date2, false,  true )
     * 173. Range(  HIVAL, date1, date2, false,  true )
     * 174. Range( ONEVAL, date1, date2, false,  true )
     * 175. Range( CLOSED, date1, date2, false,  true )
     *   
     * 176. Range(    ANY, date2, date1,  true,  true )
     * 177. Range(  LOVAL, date2, date1,  true,  true )
     * 178. Range(  HIVAL, date2, date1,  true,  true )
     * 179. Range( ONEVAL, date2, date1,  true,  true )
     * 180. Range( CLOSED, date2, date1,  true,  true )
     *   
     * 181. Range(    ANY,  str1,  str2, false, false )
     * 182. Range(  LOVAL,  str1,  str2, false, false )
     * 183. Range(  HIVAL,  str1,  str2, false, false )
     * 184. Range( ONEVAL,  str1,  str2, false, false )
     * 185. Range( CLOSED,  str1,  str2, false, false )
     *   
     * 186. Range(    ANY,  str2,  str1,  true, false )
     * 187. Range(  LOVAL,  str2,  str1,  true, false )
     * 188. Range(  HIVAL,  str2,  str1,  true, false )
     * 189. Range( ONEVAL,  str2,  str1,  true, false )
     * 190. Range( CLOSED,  str2,  str1,  true, false )
     *   
     *  TODO (181-185 with order=false, date=true ..5 tests)
     *  TODO (186-190 with order=true,  date=true ..5 tests)
     * 
     */

    @Test 
    /**
     * 
     */
    public void test1()
    {
       testnum = 1;

       // Set Result Expectations
       exp_constructor = "OK";
       exp_numeric = true;
       exp_isdate  = false;
       exp_value1  = null;
       exp_value2  = null;

       // execute test
       baseTest( RangeType.ANY, nullvals[0], nullvals[1], false, false );

    }

    @Test 
    /**
     * 
     */
    public void test2()
    {
       testnum = 2;

       // Set Result Expectations
       exp_constructor = "NPE";
       exp_numeric = true;
       exp_isdate  = false;
       exp_value1  = null;
       exp_value2  = null;

       // execute test
       baseTest( RangeType.LOVAL, nullvals[0], nullvals[1], false, false );

    }

    @Test 
    /**
     * 
     */
    public void test3()
    {
       testnum = 3;

       // Set Result Expectations
       exp_constructor = "NPE";
       exp_numeric = true;
       exp_isdate  = false;
       exp_value1  = null;
       exp_value2  = null;

       // execute test
       baseTest( RangeType.HIVAL, nullvals[0], nullvals[1], false, false );

    }

    @Test 
    /**
     * 
     */
    public void test4()
    {
       testnum = 4;

       // Set Result Expectations
       exp_constructor = "NPE";
       exp_numeric = true;
       exp_isdate  = false;
       exp_value1  = null;
       exp_value2  = null;

       // execute test
       baseTest( RangeType.ONEVAL, nullvals[0], nullvals[1], false, false );

    }

    @Test 
    /**
     * 
     */
    public void test5()
    {
       testnum = 5;

       // Set Result Expectations
       exp_constructor = "NPE";
       exp_numeric = true;
       exp_isdate  = false;
       exp_value1  = null;
       exp_value2  = null;

       // execute test
       baseTest( RangeType.CLOSED, nullvals[0], nullvals[1], false, false );

    }

    @Test 
    /**
     * 
     */
    public void test6()
    {
       testnum = 6;

       // Set Result Expectations
       exp_constructor = "OK";
       exp_numeric = true;
       exp_isdate  = false;
       exp_value1  = null;
       exp_value2  = null;

       // execute test
       baseTest( RangeType.ANY, nullvals[0], nullvals[1], true, false );

    }

    @Test 
    /**
     * 
     */
    public void test7()
    {
       testnum = 7;

       // Set Result Expectations
       exp_constructor = "NPE";
       exp_numeric = true;
       exp_isdate  = false;
       exp_value1  = null;
       exp_value2  = null;

       // execute test
       baseTest( RangeType.LOVAL, nullvals[0], nullvals[1], true, false );

    }

    @Test 
    /**
     * 
     */
    public void test8()
    {
       testnum = 8;

       // Set Result Expectations
       exp_constructor = "NPE";
       exp_numeric = true;
       exp_isdate  = false;
       exp_value1  = null;
       exp_value2  = null;

       // execute test
       baseTest( RangeType.HIVAL, nullvals[0], nullvals[1], true, false );

    }

    @Test 
    /**
     * 
     */
    public void test9()
    {
       testnum = 9;

       // Set Result Expectations
       exp_constructor = "NPE";
       exp_numeric = true;
       exp_isdate  = false;
       exp_value1  = null;
       exp_value2  = null;

       // execute test
       baseTest( RangeType.ONEVAL, nullvals[0], nullvals[1], true, false );

    }

    @Test 
    /**
     * 
     */
    public void test10()
    {
       testnum = 10;

       // Set Result Expectations
       exp_constructor = "NPE";
       exp_numeric = true;
       exp_isdate  = false;
       exp_value1  = null;
       exp_value2  = null;

       // execute test
       baseTest( RangeType.CLOSED, nullvals[0], nullvals[1], true, false );

    }

    @Test 
    /**
     * 
     */
    public void test11()
    {
       testnum = 11;

       // Set Result Expectations
       exp_constructor = "OK";
       exp_numeric = true;
       exp_isdate  = true;
       exp_value1  = null;
       exp_value2  = null;

       // execute test
       baseTest( RangeType.ANY, nullvals[0], nullvals[1], false, true );

    }

    @Test 
    /**
     * 
     */
    public void test12()
    {
       testnum = 12;

       // Set Result Expectations
       exp_constructor = "NPE";
       exp_numeric = true;
       exp_isdate  = true;
       exp_value1  = null;
       exp_value2  = null;

       // execute test
       baseTest( RangeType.LOVAL, nullvals[0], nullvals[1], false, true );

    }

    @Test 
    /**
     * 
     */
    public void test13()
    {
       testnum = 13;

       // Set Result Expectations
       exp_constructor = "NPE";
       exp_numeric = true;
       exp_isdate  = true;
       exp_value1  = null;
       exp_value2  = null;

       // execute test
       baseTest( RangeType.HIVAL, nullvals[0], nullvals[1], false, true );

    }

    @Test 
    /**
     * 
     */
    public void test14()
    {
       testnum = 14;

       // Set Result Expectations
       exp_constructor = "NPE";
       exp_numeric = true;
       exp_isdate  = true;
       exp_value1  = null;
       exp_value2  = null;

       // execute test
       baseTest( RangeType.ONEVAL, nullvals[0], nullvals[1], false, true );

    }

    @Test 
    /**
     * 
     */
    public void test15()
    {
       testnum = 15;

       // Set Result Expectations
       exp_constructor = "NPE";
       exp_numeric = true;
       exp_isdate  = true;
       exp_value1  = null;
       exp_value2  = null;

       // execute test
       baseTest( RangeType.CLOSED, nullvals[0], nullvals[1], false, true );

    }


    @Test 
    /**
     * 
     */
    public void test16()
    {
       testnum = 16;

       // Set Result Expectations
       exp_constructor = "OK";
       exp_numeric = true;
       exp_isdate  = true;
       exp_value1  = null;
       exp_value2  = null;

       // execute test
       baseTest( RangeType.ANY, nullvals[0], nullvals[1], true, true );

    }

    @Test 
    /**
     * 
     */
    public void test17()
    {
       testnum = 17;

       // Set Result Expectations
       exp_constructor = "NPE";
       exp_numeric = true;
       exp_isdate  = true;
       exp_value1  = null;
       exp_value2  = null;

       // execute test
       baseTest( RangeType.LOVAL, nullvals[0], nullvals[1], true, true );

    }

    @Test 
    /**
     * 
     */
    public void test18()
    {
       testnum = 18;

       // Set Result Expectations
       exp_constructor = "NPE";
       exp_numeric = true;
       exp_isdate  = true;
       exp_value1  = null;
       exp_value2  = null;

       // execute test
       baseTest( RangeType.HIVAL, nullvals[0], nullvals[1], true, true );

    }

    @Test 
    /**
     * 
     */
    public void test19()
    {
       testnum = 19;

       // Set Result Expectations
       exp_constructor = "NPE";
       exp_numeric = true;
       exp_isdate  = true;
       exp_value1  = null;
       exp_value2  = null;

       // execute test
       baseTest( RangeType.ONEVAL, nullvals[0], nullvals[1], true, true );

    }

    @Test 
    /**
     * 
     */
    public void test20()
    {
       testnum = 20;

       // Set Result Expectations
       exp_constructor = "NPE";
       exp_numeric = true;
       exp_isdate  = true;
       exp_value1  = null;
       exp_value2  = null;

       // execute test
       baseTest( RangeType.CLOSED, nullvals[0], nullvals[1], true, true );

    }

    @Test 
    /**
     * 
     */
    public void test21()
    {
       testnum = 21;

       // Set Result Expectations
       exp_constructor = "OK";
       exp_numeric = true;
       exp_isdate  = false;
       exp_value1  = null;
       exp_value2  = numvals[1];

       // execute test
       baseTest( RangeType.ANY, nullvals[0], numvals[1], false, false );

    }

    @Test 
    /**
     * 
     */
    public void test22()
    {
       testnum = 22;

       // Set Result Expectations
       exp_constructor = "NPE";
       exp_numeric = true;
       exp_isdate  = false;
       exp_value1  = null;
       exp_value2  = numvals[1];

       // execute test
       baseTest( RangeType.LOVAL, nullvals[0], numvals[1], false, false );

    }

    @Test 
    /**
     * 
     */
    public void test23()
    {
       testnum = 23;

       // Set Result Expectations
       exp_constructor = "NPE";
       exp_numeric = true;
       exp_isdate  = false;
       exp_value1  = null;
       exp_value2  = numvals[1];

       // execute test
       baseTest( RangeType.HIVAL, nullvals[0], numvals[1], false, false );

    }

    @Test 
    /**
     * 
     */
    public void test24()
    {
       testnum = 24;

       // Set Result Expectations
       exp_constructor = "NPE";
       exp_numeric = true;
       exp_isdate  = false;
       exp_value1  = null;
       exp_value2  = numvals[1];

       // execute test
       baseTest( RangeType.ONEVAL, nullvals[0], numvals[1], false, false );

    }

    @Test 
    /**
     * 
     */
    public void test25()
    {
       testnum = 25;

       // Set Result Expectations
       exp_constructor = "NPE";
       exp_numeric = true;
       exp_isdate  = false;
       exp_value1  = null;
       exp_value2  = numvals[1];

       // execute test
       baseTest( RangeType.CLOSED, nullvals[0], numvals[1], false, false );

    }

    @Test 
    /**
     * 
     */
    public void test41()
    {
       testnum = 41;

       // Set Result Expectations
       exp_constructor = "OK";
       exp_numeric = false;
       exp_isdate  = false;
       exp_value1  = null;
       exp_value2  = datevals[1];

       // execute test
       baseTest( RangeType.ANY, nullvals[0], datevals[1], false, false );

    }

    @Test 
    /**
     * 
     */
    public void test42()
    {
       testnum = 42;

       // Set Result Expectations
       exp_constructor = "NPE";
       exp_numeric = false;
       exp_isdate  = false;
       exp_value1  = null;
       exp_value2  = datevals[1];

       // execute test
       baseTest( RangeType.LOVAL, nullvals[0], datevals[1], false, false );

    }

    @Test 
    /**
     * 
     */
    public void test43()
    {
       testnum = 43;

       // Set Result Expectations
       exp_constructor = "NPE";
       exp_numeric = false;
       exp_isdate  = false;
       exp_value1  = null;
       exp_value2  = datevals[1];

       // execute test
       baseTest( RangeType.HIVAL, nullvals[0], datevals[1], false, false );

    }

    @Test 
    /**
     * 
     */
    public void test44()
    {
       testnum = 44;

       // Set Result Expectations
       exp_constructor = "NPE";
       exp_numeric = false;
       exp_isdate  = false;
       exp_value1  = null;
       exp_value2  = datevals[1];

       // execute test
       baseTest( RangeType.ONEVAL, nullvals[0], datevals[1], false, false );

    }

    @Test 
    /**
     * 
     */
    public void test45()
    {
       testnum = 45;

       // Set Result Expectations
       exp_constructor = "NPE";
       exp_numeric = false;
       exp_isdate  = false;
       exp_value1  = null;
       exp_value2  = datevals[1];

       // execute test
       baseTest( RangeType.CLOSED, nullvals[0], datevals[1], false, false );

    }

    @Test 
    /**
     * 
     */
    public void test61()
    {
       testnum = 61;

       // Set Result Expectations
       exp_constructor = "OK";
       exp_numeric = false;
       exp_isdate  = false;
       exp_value1  = null;
       exp_value2  = strvals[1];

       // execute test
       baseTest( RangeType.ANY, nullvals[0], strvals[1], false, false );

    }

    @Test 
    /**
     * 
     */
    public void test62()
    {
       testnum = 62;

       // Set Result Expectations
       exp_constructor = "NPE";
       exp_numeric = false;
       exp_isdate  = false;
       exp_value1  = null;
       exp_value2  = strvals[1];

       // execute test
       baseTest( RangeType.LOVAL, nullvals[0], strvals[1], false, false );

    }

    @Test 
    /**
     * 
     */
    public void test63()
    {
       testnum = 63;

       // Set Result Expectations
       exp_constructor = "NPE";
       exp_numeric = false;
       exp_isdate  = false;
       exp_value1  = null;
       exp_value2  = strvals[1];

       // execute test
       baseTest( RangeType.HIVAL, nullvals[0], strvals[1], false, false );

    }

    @Test 
    /**
     * 
     */
    public void test64()
    {
       testnum = 64;

       // Set Result Expectations
       exp_constructor = "NPE";
       exp_numeric = false;
       exp_isdate  = false;
       exp_value1  = null;
       exp_value2  = strvals[1];

       // execute test
       baseTest( RangeType.ONEVAL, nullvals[0], strvals[1], false, false );

    }

    @Test 
    /**
     * 
     */
    public void test65()
    {
       testnum = 65;

       // Set Result Expectations
       exp_constructor = "NPE";
       exp_numeric = false;
       exp_isdate  = false;
       exp_value1  = null;
       exp_value2  = strvals[1];

       // execute test
       baseTest( RangeType.CLOSED, nullvals[0], strvals[1], false, false );

    }

    @Test 
    /**
     * 
     */
    public void test81()
    {
       testnum = 81;

       // Set Result Expectations
       exp_constructor = "OK";
       exp_numeric = true;
       exp_isdate  = false;
       exp_value1  = numvals[0];
       exp_value2  = null;

       // execute test
       baseTest( RangeType.ANY, numvals[0], nullvals[1], false, false );

    }

    @Test 
    /**
     * 
     */
    public void test82()
    {
       testnum = 82;

       // Set Result Expectations
       exp_constructor = "OK";
       exp_numeric = true;
       exp_isdate  = false;
       exp_value1  = numvals[0];
       exp_value2  = null;

       // execute test
       baseTest( RangeType.LOVAL, numvals[0], nullvals[1], false, false );

    }

    @Test 
    /**
     * 
     */
    public void test83()
    {
       testnum = 83;

       // Set Result Expectations
       exp_constructor = "OK";
       exp_numeric = true;
       exp_isdate  = false;
       exp_value1  = numvals[0]; // Single value type populates value1 
       exp_value2  = null;

       // execute test
       baseTest( RangeType.HIVAL, numvals[0], nullvals[1], false, false );

    }

    @Test 
    /**
     * 
     */
    public void test84()
    {
       testnum = 84;

       // Set Result Expectations
       exp_constructor = "OK";
       exp_numeric = true;
       exp_isdate  = false;
       exp_value1  = numvals[0]; // Single value type populates value1 
       exp_value2  = null;       // extra

       // execute test
       baseTest( RangeType.ONEVAL, numvals[0], nullvals[1], false, false );

    }

    @Test 
    /**
     * 
     */
    public void test85()
    {
       testnum = 85;

       // Set Result Expectations
       exp_constructor = "IAE";
       exp_numeric = true;
       exp_isdate  = false;
       exp_value1  = numvals[0];
       exp_value2  = null;

       // execute test
       baseTest( RangeType.CLOSED, numvals[0], nullvals[1], false, false );

    }

    @Test 
    /**
     * 
     */
    public void test91()
    {
       testnum = 91;

       // Set Result Expectations
       exp_constructor = "IDE";
       exp_numeric = false;
       exp_isdate  = true;
       exp_value1  = numvals[0];
       exp_value2  = null;

       // execute test
       baseTest( RangeType.ANY, numvals[0], nullvals[1], false, true );

    }

    @Test 
    /**
     * 
     */
    public void test92()
    {
       testnum = 92;

       // Set Result Expectations
       exp_constructor = "IDE";
       exp_numeric = false;
       exp_isdate  = true;
       exp_value1  = numvals[0];
       exp_value2  = null;

       // execute test
       baseTest( RangeType.LOVAL, numvals[0], nullvals[1], false, true );

    }

    @Test 
    /**
     * 
     */
    public void test93()
    {
       testnum = 93;

       // Set Result Expectations
       exp_constructor = "IDE";
       exp_numeric = false;
       exp_isdate  = true;
       exp_value1  = numvals[0]; // Single value type populates value1 
       exp_value2  = null;

       // execute test
       baseTest( RangeType.HIVAL, numvals[0], nullvals[1], false, true );

    }

    @Test 
    /**
     * 
     */
    public void test94()
    {
       testnum = 94;

       // Set Result Expectations
       exp_constructor = "IDE";
       exp_numeric = false;
       exp_isdate  = true;
       exp_value1  = numvals[0]; // Single value type populates value1 
       exp_value2  = null;       // extra

       // execute test
       baseTest( RangeType.ONEVAL, numvals[0], nullvals[1], false, true );

    }

    @Test 
    /**
     * 
     */
    public void test95()
    {
       testnum = 95;

       // Set Result Expectations
       exp_constructor = "IAE";
       exp_numeric = false;
       exp_isdate  = true;
       exp_value1  = numvals[0];
       exp_value2  = null;

       // execute test
       baseTest( RangeType.CLOSED, numvals[0], nullvals[1], false, true );

    }

    @Test 
    /**
     * 
     */
    public void test101()
    {
       testnum = 101;

       // Set Result Expectations
       exp_constructor = "OK";
       exp_numeric = false;
       exp_isdate  = false;
       exp_value1  = datevals[0];
       exp_value2  = null;

       // execute test
       baseTest( RangeType.ANY, datevals[0], nullvals[1], false, false );

    }

    @Test 
    /**
     * 
     */
    public void test102()
    {
       testnum = 102;

       // Set Result Expectations
       exp_constructor = "OK";
       exp_numeric = false;
       exp_isdate  = false;
       exp_value1  = datevals[0];
       exp_value2  = null;

       // execute test
       baseTest( RangeType.LOVAL, datevals[0], nullvals[1], false, false );

    }

    @Test 
    /**
     * 
     */
    public void test103()
    {
       testnum = 103;

       // Set Result Expectations
       exp_constructor = "OK";
       exp_numeric = false;
       exp_isdate  = false;
       exp_value1  = datevals[0]; // Single value type populates value1 
       exp_value2  = null;

       // execute test
       baseTest( RangeType.HIVAL, datevals[0], nullvals[1], false, false );

    }

    @Test 
    /**
     * 
     */
    public void test104()
    {
       testnum = 104;

       // Set Result Expectations
       exp_constructor = "OK";
       exp_numeric = false;
       exp_isdate  = false;
       exp_value1  = datevals[0]; // Single value type populates value1 
       exp_value2  = null;       // extra

       // execute test
       baseTest( RangeType.ONEVAL, datevals[0], nullvals[1], false, false );

    }

    @Test 
    /**
     * 
     */
    public void test105()
    {
       testnum = 105;

       // Set Result Expectations
       exp_constructor = "IAE";
       exp_numeric = false;
       exp_isdate  = false;
       exp_value1  = datevals[0];
       exp_value2  = null;

       // execute test
       baseTest( RangeType.CLOSED, datevals[0], nullvals[1], false, false );

    }

    @Test 
    /**
     * 
     */
    public void test111()
    {
       testnum = 111;

       // Set Result Expectations
       exp_constructor = "OK";
       exp_numeric = false;
       exp_isdate  = true;
       exp_value1  = datevals[0];
       exp_value2  = null;

       // execute test
       baseTest( RangeType.ANY, datevals[0], nullvals[1], false, true );

    }

    @Test 
    /**
     * 
     */
    public void test112()
    {
       testnum = 112;

       // Set Result Expectations
       exp_constructor = "OK";
       exp_numeric = false;
       exp_isdate  = true;
       exp_value1  = datevals[0];
       exp_value2  = null;

       // execute test
       baseTest( RangeType.LOVAL, datevals[0], nullvals[1], false, true );

    }

    @Test 
    /**
     * 
     */
    public void test113()
    {
       testnum = 113;

       // Set Result Expectations
       exp_constructor = "OK";
       exp_numeric = false;
       exp_isdate  = true;
       exp_value1  = datevals[0]; // Single value type populates value1 
       exp_value2  = null;

       // execute test
       baseTest( RangeType.HIVAL, datevals[0], nullvals[1], false, true );

    }

    @Test 
    /**
     * 
     */
    public void test114()
    {
       testnum = 114;

       // Set Result Expectations
       exp_constructor = "OK";
       exp_numeric = false;
       exp_isdate  = true;
       exp_value1  = datevals[0]; // Single value type populates value1 
       exp_value2  = null;       // extra

       // execute test
       baseTest( RangeType.ONEVAL, datevals[0], nullvals[1], false, true );

    }

    @Test 
    /**
     * 
     */
    public void test115()
    {
       testnum = 115;

       // Set Result Expectations
       exp_constructor = "IAE";
       exp_numeric = false;
       exp_isdate  = true;
       exp_value1  = datevals[0];
       exp_value2  = null;

       // execute test
       baseTest( RangeType.CLOSED, datevals[0], nullvals[1], false, true );

    }

    @Test 
    /**
     * 
     */
    public void test121()
    {
       testnum = 121;

       // Set Result Expectations
       exp_constructor = "OK";
       exp_numeric = false;
       exp_isdate  = false;
       exp_value1  = strvals[0];
       exp_value2  = null;

       // execute test
       baseTest( RangeType.ANY, strvals[0], nullvals[1], false, false );

    }

    @Test 
    /**
     * 
     */
    public void test122()
    {
       testnum = 122;

       // Set Result Expectations
       exp_constructor = "OK";
       exp_numeric = false;
       exp_isdate  = false;
       exp_value1  = strvals[0];
       exp_value2  = null;

       // execute test
       baseTest( RangeType.LOVAL, strvals[0], nullvals[1], false, false );

    }

    @Test 
    /**
     * 
     */
    public void test123()
    {
       testnum = 123;

       // Set Result Expectations
       exp_constructor = "OK";
       exp_numeric = false;
       exp_isdate  = false;
       exp_value1  = strvals[0]; // Single value type populates value1 
       exp_value2  = null;

       // execute test
       baseTest( RangeType.HIVAL, strvals[0], nullvals[1], false, false );

    }

    @Test 
    /**
     * 
     */
    public void test124()
    {
       testnum = 124;

       // Set Result Expectations
       exp_constructor = "OK";
       exp_numeric = false;
       exp_isdate  = false;
       exp_value1  = strvals[0]; // Single value type populates value1 
       exp_value2  = null;       // extra

       // execute test
       baseTest( RangeType.ONEVAL, strvals[0], nullvals[1], false, false );

    }

    @Test 
    /**
     * 
     */
    public void test125()
    {
       testnum = 125;

       // Set Result Expectations
       exp_constructor = "IAE";
       exp_numeric = false;
       exp_isdate  = false;
       exp_value1  = strvals[0];
       exp_value2  = null;

       // execute test
       baseTest( RangeType.CLOSED, strvals[0], nullvals[1], false, false );

    }

    @Test 
    /**
     * 
     */
    public void test131()
    {
       testnum = 131;

       // Set Result Expectations
       exp_constructor = "IDE";
       exp_numeric = false;
       exp_isdate  = true;
       exp_value1  = strvals[0];
       exp_value2  = null;

       // execute test
       baseTest( RangeType.ANY, strvals[0], nullvals[1], false, true );

    }

    @Test 
    /**
     * 
     */
    public void test132()
    {
       testnum = 132;

       // Set Result Expectations
       exp_constructor = "IDE";
       exp_numeric = false;
       exp_isdate  = true;
       exp_value1  = strvals[0];
       exp_value2  = null;

       // execute test
       baseTest( RangeType.LOVAL, strvals[0], nullvals[1], false, true );

    }

    @Test 
    /**
     * 
     */
    public void test133()
    {
       testnum = 133;

       // Set Result Expectations
       exp_constructor = "IDE";
       exp_numeric = false;
       exp_isdate  = true;
       exp_value1  = strvals[0]; // Single value type populates value1 
       exp_value2  = null;

       // execute test
       baseTest( RangeType.HIVAL, strvals[0], nullvals[1], false, true );

    }

    @Test 
    /**
     * 
     */
    public void test134()
    {
       testnum = 134;

       // Set Result Expectations
       exp_constructor = "IDE";
       exp_numeric = false;
       exp_isdate  = true;
       exp_value1  = strvals[0]; // Single value type populates value1 
       exp_value2  = null;       // extra

       // execute test
       baseTest( RangeType.ONEVAL, strvals[0], nullvals[1], false, true );

    }

    @Test 
    /**
     * 
     */
    public void test135()
    {
       testnum = 135;

       // Set Result Expectations
       exp_constructor = "IDE";
       exp_numeric = false;
       exp_isdate  = true;
       exp_value1  = strvals[0];
       exp_value2  = null;

       // execute test
       baseTest( RangeType.CLOSED, strvals[0], nullvals[1], false, true );

    }

    @Test 
    /**
     * 
     */
    public void test141()
    {
       testnum = 141;

       // Set Result Expectations
       exp_constructor = "OK";
       exp_numeric = true;
       exp_isdate  = false;
       exp_value1  = numvals[0];
       exp_value2  = numvals[1];

       // execute test
       baseTest( RangeType.ANY, numvals[0], numvals[1], false, false );

    }

    @Test 
    /**
     * 
     */
    public void test142()
    {
       testnum = 142;

       // Set Result Expectations
       exp_constructor = "IAE";
       exp_numeric = true;
       exp_isdate  = false;
       exp_value1  = numvals[0];
       exp_value2  = numvals[1];

       // execute test
       baseTest( RangeType.LOVAL, numvals[0], numvals[1], false, false );

    }

    @Test 
    /**
     * 
     */
    public void test143()
    {
       testnum = 143;

       // Set Result Expectations
       exp_constructor = "IAE";
       exp_numeric = true;
       exp_isdate  = false;
       exp_value1  = numvals[0]; // Single value type populates value1 
       exp_value2  = numvals[1]; // Should this be allowed?

       // execute test
       baseTest( RangeType.HIVAL, numvals[0], numvals[1], false, false );

    }

    @Test 
    /**
     * 
     */
    public void test144()
    {
       testnum = 144;

       // Set Result Expectations
       exp_constructor = "IAE";
       exp_numeric = true;
       exp_isdate  = false;
       exp_value1  = numvals[0]; // Single value type populates value1 
       exp_value2  = numvals[1];

       // execute test
       baseTest( RangeType.ONEVAL, numvals[0], numvals[1], false, false );

    }

    @Test 
    /**
     * 
     */
    public void test145()
    {
       testnum = 145;

       // Set Result Expectations
       exp_constructor = "OK";
       exp_numeric = true;
       exp_isdate  = false;
       exp_value1  = numvals[0];
       exp_value2  = numvals[1];

       // execute test
       baseTest( RangeType.CLOSED, numvals[0], numvals[1], false, false );

    }

    @Test 
    /**
     * 
     */
    public void test146()
    {
       testnum = 146;

       // Set Result Expectations
       exp_constructor = "OK";
       exp_numeric = true;
       exp_isdate  = false;
       exp_value1  = numvals[0];
       exp_value2  = numvals[1];

       // execute test
       baseTest( RangeType.ANY, numvals[1], numvals[0], true, false );

    }

    @Test 
    /**
     * 
     */
    public void test147()
    {
       testnum = 147;

       // Set Result Expectations
       exp_constructor = "IAE";
       exp_numeric = true;
       exp_isdate  = false;
       exp_value1  = numvals[1];
       exp_value2  = numvals[0]; // expect second value to be ignored

       // execute test
       baseTest( RangeType.LOVAL, numvals[1], numvals[0], true, false );

    }

    @Test 
    /**
     * 
     */
    public void test148()
    {
       testnum = 148;

       // Set Result Expectations
       exp_constructor = "IAE";
       exp_numeric = true;
       exp_isdate  = false;
       exp_value1  = numvals[1]; 
       exp_value2  = numvals[0]; // expect second value to be ignored

       // execute test
       baseTest( RangeType.HIVAL, numvals[1], numvals[0], true, false );

    }

    @Test 
    /**
     * 
     */
    public void test149()
    {
       testnum = 149;

       // Set Result Expectations
       exp_constructor = "IAE";
       exp_numeric = true;
       exp_isdate  = false;
       exp_value1  = numvals[1];
       exp_value2  = numvals[0]; // expect second value to be ignored

       // execute test
       baseTest( RangeType.ONEVAL, numvals[1], numvals[0], true, false );

    }

    @Test 
    /**
     * 
     */
    public void test150()
    {
       testnum = 150;

       // Set Result Expectations
       exp_constructor = "OK";
       exp_numeric = true;
       exp_isdate  = false;
       exp_value1  = numvals[0];
       exp_value2  = numvals[1];

       // execute test
       baseTest( RangeType.CLOSED, numvals[1], numvals[0], true, false );

    }

    @Test 
    /**
     * 
     */
    public void test171()
    {
       testnum = 171;

       // Set Result Expectations
       exp_constructor = "OK";
       exp_numeric = false;
       exp_isdate  = true;
       exp_value1  = datevals[0];
       exp_value2  = datevals[1];

       // execute test
       baseTest( RangeType.ANY, datevals[0], datevals[1], false, true );

    }

    @Test 
    /**
     * 
     */
    public void test172()
    {
       testnum = 172;

       // Set Result Expectations
       exp_constructor = "IAE";
       exp_numeric = false;
       exp_isdate  = true;
       exp_value1  = datevals[0];
       exp_value2  = datevals[1];

       // execute test
       baseTest( RangeType.LOVAL, datevals[0], datevals[1], false, true );

    }

    @Test 
    /**
     * 
     */
    public void test173()
    {
       testnum = 173;

       // Set Result Expectations
       exp_constructor = "IAE";
       exp_numeric = false;
       exp_isdate  = true;
       exp_value1  = datevals[0]; // Single value type populates value1 
       exp_value2  = datevals[1]; // Should this be allowed?

       // execute test
       baseTest( RangeType.HIVAL, datevals[0], datevals[1], false, true );

    }

    @Test 
    /**
     * 
     */
    public void test174()
    {
       testnum = 174;

       // Set Result Expectations
       exp_constructor = "IAE";
       exp_numeric = false;
       exp_isdate  = true;
       exp_value1  = datevals[0]; // Single value type populates value1 
       exp_value2  = datevals[1];

       // execute test
       baseTest( RangeType.ONEVAL, datevals[0], datevals[1], false, true );

    }

    @Test 
    /**
     * 
     */
    public void test175()
    {
       testnum = 175;

       // Set Result Expectations
       exp_constructor = "OK";
       exp_numeric = false;
       exp_isdate  = true;
       exp_value1  = datevals[0];
       exp_value2  = datevals[1];

       // execute test
       baseTest( RangeType.CLOSED, datevals[0], datevals[1], false, true );

    }

    @Test 
    /**
     * 
     */
    public void test176()
    {
       testnum = 176;

       // Set Result Expectations
       exp_constructor = "OK";
       exp_numeric = false;
       exp_isdate  = true;
       exp_value1  = datevals[0];
       exp_value2  = datevals[1];

       // execute test
       baseTest( RangeType.ANY, datevals[1], datevals[0], true, true );

    }

    @Test 
    /**
     * 
     */
    public void test177()
    {
       testnum = 177;

       // Set Result Expectations
       exp_constructor = "IAE";
       exp_numeric = false;
       exp_isdate  = true;
       exp_value1  = datevals[1];
       exp_value2  = datevals[0]; // expect second value to be ignored

       // execute test
       baseTest( RangeType.LOVAL, datevals[1], datevals[0], true, true );

    }

    @Test 
    /**
     * 
     */
    public void test178()
    {
       testnum = 178;

       // Set Result Expectations
       exp_constructor = "IAE";
       exp_numeric = false;
       exp_isdate  = true;
       exp_value1  = datevals[1]; 
       exp_value2  = datevals[0]; // expect second value to be ignored

       // execute test
       baseTest( RangeType.HIVAL, datevals[1], datevals[0], true, true );

    }

    @Test 
    /**
     * 
     */
    public void test179()
    {
       testnum = 179;

       // Set Result Expectations
       exp_constructor = "IAE";
       exp_numeric = false;
       exp_isdate  = true;
       exp_value1  = datevals[1];
       exp_value2  = datevals[0]; // expect second value to be ignored

       // execute test
       baseTest( RangeType.ONEVAL, datevals[1], datevals[0], true, true );

    }

    @Test 
    /**
     * 
     */
    public void test180()
    {
       testnum = 180;

       // Set Result Expectations
       exp_constructor = "OK";
       exp_numeric = false;
       exp_isdate  = true;
       exp_value1  = datevals[0];
       exp_value2  = datevals[1];

       // execute test
       baseTest( RangeType.CLOSED, datevals[1], datevals[0], true, true );

    }
//HERE
    @Test 
    /**
     * 
     */
    public void test181()
    {
       testnum = 181;

       // Set Result Expectations
       exp_constructor = "OK";
       exp_numeric = false;
       exp_isdate  = false;
       exp_value1  = strvals[0];
       exp_value2  = strvals[1];

       // execute test
       baseTest( RangeType.ANY, strvals[0], strvals[1], false, false );

    }

    @Test 
    /**
     * 
     */
    public void test182()
    {
       testnum = 182;

       // Set Result Expectations
       exp_constructor = "IAE";
       exp_numeric = false;
       exp_isdate  = false;
       exp_value1  = strvals[0];
       exp_value2  = strvals[1];

       // execute test
       baseTest( RangeType.LOVAL, strvals[0], strvals[1], false, false );

    }

    @Test 
    /**
     * 
     */
    public void test183()
    {
       testnum = 183;

       // Set Result Expectations
       exp_constructor = "IAE";
       exp_numeric = false;
       exp_isdate  = false;
       exp_value1  = strvals[0]; // Single value type populates value1 
       exp_value2  = strvals[1]; // Should this be allowed?

       // execute test
       baseTest( RangeType.HIVAL, strvals[0], strvals[1], false, false );

    }

    @Test 
    /**
     * 
     */
    public void test184()
    {
       testnum = 184;

       // Set Result Expectations
       exp_constructor = "IAE";
       exp_numeric = false;
       exp_isdate  = false;
       exp_value1  = strvals[0]; // Single value type populates value1 
       exp_value2  = strvals[1];

       // execute test
       baseTest( RangeType.ONEVAL, strvals[0], strvals[1], false, false );

    }

    @Test 
    /**
     * 
     */
    public void test185()
    {
       testnum = 185;

       // Set Result Expectations
       exp_constructor = "OK";
       exp_numeric = false;
       exp_isdate  = false;
       exp_value1  = strvals[0];
       exp_value2  = strvals[1];

       // execute test
       baseTest( RangeType.CLOSED, strvals[0], strvals[1], false, false );

    }

    @Test 
    /**
     * 
     */
    public void test186()
    {
       testnum = 186;

       // Set Result Expectations
       exp_constructor = "OK";
       exp_numeric = false;
       exp_isdate  = false;
       exp_value1  = strvals[0];
       exp_value2  = strvals[1];

       // execute test
       baseTest( RangeType.ANY, strvals[1], strvals[0], true, false );

    }

    @Test 
    /**
     * 
     */
    public void test187()
    {
       testnum = 187;

       // Set Result Expectations
       exp_constructor = "IAE";
       exp_numeric = false;
       exp_isdate  = false;
       exp_value1  = strvals[1];
       exp_value2  = strvals[0]; // expect second value to be ignored

       // execute test
       baseTest( RangeType.LOVAL, strvals[1], strvals[0], true, false );

    }

    @Test 
    /**
     * 
     */
    public void test188()
    {
       testnum = 188;

       // Set Result Expectations
       exp_constructor = "IAE";
       exp_numeric = false;
       exp_isdate  = false;
       exp_value1  = strvals[1]; 
       exp_value2  = strvals[0]; // expect second value to be ignored

       // execute test
       baseTest( RangeType.HIVAL, strvals[1], strvals[0], true, false );

    }

    @Test 
    /**
     * 
     */
    public void test189()
    {
       testnum = 189;

       // Set Result Expectations
       exp_constructor = "IAE";
       exp_numeric = false;
       exp_isdate  = false;
       exp_value1  = strvals[1];
       exp_value2  = strvals[0]; // expect second value to be ignored

       // execute test
       baseTest( RangeType.ONEVAL, strvals[1], strvals[0], true, false );

    }

    @Test 
    /**
     * 
     */
    public void test190()
    {
       testnum = 190;

       // Set Result Expectations
       exp_constructor = "OK";
       exp_numeric = false;
       exp_isdate  = false;
       exp_value1  = strvals[0];
       exp_value2  = strvals[1];

       // execute test
       baseTest( RangeType.CLOSED, strvals[1], strvals[0], true, false );

    }

    @Test 
    /**
     * Tests non-string accessors to values
     */
    public void testConversionAccess()
    {
       Range a = null;
       Range b = null;
       Range c = null;
       Range d = null;
       Range e = null;
       Range f = null;
       Range g = null;
       Range h = null;

       int    ival;
       double dval;

       //Create test ranges..
       try {
          //  - single value type, with numeric, string, date values
          a = new Range( RangeType.LOVAL,  numvals[0], nullvals[1], false, false );
          b = new Range( RangeType.LOVAL,  strvals[0], nullvals[1], false, false );
          c = new Range( RangeType.LOVAL, datevals[0], nullvals[1], false, true  );
          
          //  -    two value type, with numeric, string, date values
          d = new Range( RangeType.CLOSED,  numvals[0],  numvals[1], false, false );
          e = new Range( RangeType.CLOSED,  strvals[0],  strvals[1], false, false );
          f = new Range( RangeType.CLOSED, datevals[0], datevals[1], false, true  );

          //  - ANY type, with null, string values
          g = new Range( RangeType.ANY, nullvals[0], nullvals[1], false, true  );
          h = new Range( RangeType.ANY,  strvals[0],  strvals[1], false, true  );
       }
       catch ( InvalidDateException ide )
       {
         fail( ide.getMessage() );
       }


       // Access first value as Numeric
       try {
         // These should succeed.. all numeric type ranges
         assertEquals( a.intValue1(), new Integer( numvals[0]).intValue() );
         assertEquals( d.intValue1(), new Integer( numvals[0]).intValue() );

         assertEquals( a.doubleValue1(), new Double( numvals[0]).doubleValue(), 1e-7 );
         assertEquals( d.doubleValue1(), new Double( numvals[0]).doubleValue(), 1e-7 );
         
       }
       catch ( NumberFormatException nfe )
       {
         fail( nfe.getMessage() );
       }

       // These should fail.
       //   - string and date values should generate NFE
       failCheckValue1( b );
       failCheckValue1( c );
       failCheckValue1( e );
       failCheckValue1( f );

       // Access second value as Numeric
       try {
         // These should succeed.. all numeric type ranges
         assertEquals( a.intValue2(), new Integer( numvals[0]).intValue() );
         assertEquals( d.intValue2(), new Integer( numvals[1]).intValue() );

         assertEquals( a.doubleValue2(), new Double( numvals[0]).doubleValue(), 1e-7 );
         assertEquals( d.doubleValue2(), new Double( numvals[1]).doubleValue(), 1e-7 );
         
       }
       catch ( NumberFormatException nfe )
       {
         fail( nfe.getMessage() );
       }

       // These should fail.
       //   - string and date values should generate NFE
       failCheckValue2( b );
       failCheckValue2( c );
       failCheckValue2( e );
       failCheckValue2( f );

       // Access first value as Date
       assertNull( a.dateValue1() );
       assertNull( b.dateValue1() );
       assertNotNull( c.dateValue1());
       assertNull( d.dateValue1() );
       assertNull( e.dateValue1() );
       assertNotNull( f.dateValue1());

       // Access second value as Date
       assertNull( a.dateValue2() );
       assertNull( b.dateValue2() );
       assertNotNull( c.dateValue2());
       assertNull( d.dateValue2() );
       assertNull( e.dateValue2() );
       assertNotNull( f.dateValue2());

       // Issues:
       System.out.println("".format("Test ConversionAccess - Issue 11: Single value types, string rep != dtype rep when second value is null.") );
       System.out.println("".format("   String  rep: value1=%s value2=%s ",a.stringValue1(),a.stringValue2() ));
       System.out.println("".format("   Integer rep: value1=%2d value2=%2d ",a.intValue1(),a.intValue2() ));

       // ANY Type - string values are set on construction, numeric left null.
       System.out.println("".format("Test ConversionAccess - Issue 12: ANY type, generates NPE accessing values as numeric.") );
       npeCheckValue(g);
       npeCheckValue(h);

    }

    @Test 
    /**
     * Tests CompareTo method
     */
    public void testCompareTo()
    {
       Range a = null;
       Range b = null;
       Range c = null;
       Range d = null;
       Range e = null;
       Range f = null;
       Range g = null;
       Range h = null;
       Range i = null;
       Range j = null;
       Range k = null;

       Range s1 = null;
       Range s2 = null;
       Range s3 = null;
       Range s4 = null;
       Range s5 = null;

       Range d1 = null;
       Range d2 = null;
       Range d3 = null;

       int result;

       //Create test ranges..
       try {
         a = new Range(RangeType.ONEVAL, "5");
         b = new Range(RangeType.ONEVAL, "1");
         c = new Range(RangeType.ONEVAL, "9");
         d = new Range(RangeType.CLOSED, "3", "7");
         e = new Range(RangeType.CLOSED, "7", "3");
         f = new Range(RangeType.HIVAL,  "5");
         g = new Range(RangeType.HIVAL,  "6");
         h = new Range(RangeType.ANY);
         i = new Range(RangeType.ANY, datevals[0], datevals[1], false, true );
         j = new Range(RangeType.ONEVAL, "1", "9");
         k = new Range(RangeType.CLOSED, "1", "9");
         
         s1 = new Range(RangeType.ONEVAL, "foo");
         s2 = new Range(RangeType.ONEVAL, "bar");
         s3 = new Range(RangeType.CLOSED, "aaa", "eee");
         s4 = new Range(RangeType.CLOSED, "aaa", "jjj");
         s5 = new Range(RangeType.ONEVAL, "jjj");

         d1 = new Range(RangeType.HIVAL, "1999-01-01", null, false, true );
         d2 = new Range(RangeType.LOVAL, "2003-01-01", null, false, true );
         d3 = new Range(RangeType.CLOSED,"1999-01-01", "2003-01-01", false, true );
         
       }
       catch ( InvalidDateException ide )
       {
         fail( ide.getMessage() );
       }

       // 'that' == null results in NPE
       try
       {
         result = d.compareTo(null);
         fail( "Expected exception not thrown." );
       }
       catch ( NullPointerException npe ){} // good

       // compare to itself == EQUAL
       assertEquals( 0, a.compareTo(a) );
       assertEquals( 0, s1.compareTo(s1) );

       // ANY == ANY
       assertEquals( 0, h.compareTo(i) );

       // numeric vs non-numeric == ClassCaseException
       try
       {
         result = c.compareTo(s2);
         fail( "Expected exception not thrown." );
       }
       catch ( ClassCastException cce ){} // good
       try
       {
         result = s1.compareTo(c);
         fail( "Expected exception not thrown." );
       }
       catch ( ClassCastException cce ){} // good

       // ANY vs <OTHER> type == ClassCastException
       try
       {
         result = h.compareTo(f);
         fail( "Expected exception not thrown." );
       }
       catch ( ClassCastException cce ){} // good
       try
       {
         result = f.compareTo(h);
         fail( "Expected exception not thrown." );
       }
       catch ( ClassCastException cce ){} // good
       try
       {
         result = h.compareTo(s1);
         fail( "Expected exception not thrown." );
       }
       catch ( ClassCastException cce ){} // good
       try
       {
         result = s1.compareTo(h);
         fail( "Expected exception not thrown." );
       }
       catch ( ClassCastException cce ){} // good

       // Numeric comparisons..
       assertTrue( b.compareTo(a) < 0 );
       assertTrue( c.compareTo(a) > 0 );
       assertTrue( d.compareTo(a) < 0 );
       assertTrue( e.compareTo(a) < 0 );
       assertTrue( f.compareTo(a) < 0 );
       assertTrue( g.compareTo(a) > 0 );

       System.out.println("".format("Test CompareTo - Issue 13: Single value type, bogus second value effects midval and compre.") );
       System.out.println("".format("  ONEVAL b value1=%2d  value2=%2d ",b.intValue1(),b.intValue2() ) );
       System.out.println("".format("  ONEVAL j value1=%2d  value2=%2d ",j.intValue1(),j.intValue2() ) );
       System.out.println("".format("    b.compareTo(j) == %s ", b.compareTo(j) ));
       if ( b.compareTo(j) == 0 )
       {
         fail("Issue 13 resolved?");
         //assertTrue( b.compareTo(j) == 0); // Should be true!
       }
       assertTrue( b.compareTo(j) < 0);

       System.out.println("".format("Test CompareTo - Issue 13: Single value type, bogus second value effects midval and compre.") );
       System.out.println("".format("  CLOSED k value1=%2d  value2=%2d ",k.intValue1(),k.intValue2() ) );
       System.out.println("".format("  ONEVAL j value1=%2d  value2=%2d ",j.intValue1(),j.intValue2() ) );
       System.out.println("".format("    k.compareTo(j) == %s ", k.compareTo(j) ));
       if ( k.compareTo(j) > 0 )
       {
         fail("Issue 13 resolved?");
         //assertTrue( k.compareTo(j) > 0); // Should be true!
       }
       assertTrue( k.compareTo(j) == 0);

       assertTrue( s2.compareTo(s1) < 0 );
       assertTrue( s3.compareTo(s1) < 0 );
       assertTrue( s4.compareTo(s1) < 0 );
       assertTrue( s5.compareTo(s1) > 0 );

       // Date comparisons..
       System.out.println("".format("Test CompareTo - Date Compare") );
       System.out.println("   HIVAL  d1 value1="+d1.stringValue1()+"  d1 value2="+d1.stringValue2() );
       System.out.println("   LOVAL  d2 value1="+d2.stringValue1()+"  d2 value2="+d2.stringValue2() );
       System.out.println("  CLOSED  d3 value1="+d3.stringValue1()+"  d3 value2="+d3.stringValue2() );
       System.out.println("     d1.compareTo(d2) = "+d1.compareTo(d2) );
       assertTrue( d1.compareTo(d2) < 0 );
       System.out.println("     d1.compareTo(d3) = "+d1.compareTo(d3) );
       System.out.println("".format("Test CompareTo - Issue 14. String comparisons do not handle mixed null on value2, bad EQUAL result.") );
       if ( d1.compareTo(d3) < 0 )
       {
         fail("Issue 14 resolved?");
         //assertTrue( d1.compareTo(d3) < 0 );
       }
       System.out.println("     d2.compareTo(d3) = "+d2.compareTo(d3) );
       assertTrue( d2.compareTo(d3) > 0 );
    }


    private void npeCheckValue( Range range )
    {
       int    ival;
       double dval;

       try {
         ival =  range.intValue1();
         fail( "Expected exception not thrown - value 1" );
       }
       catch ( NullPointerException npe )
       {
         //System.out.println("NPE caught - ival 1");
       }
       try {
         ival =  range.intValue2();
         fail( "Expected exception not thrown - value2" );
       }
       catch ( NullPointerException npe )
       {
         //System.out.println("NPE caught - ival 2");
       }

       try {
         dval =  range.doubleValue1();
         fail( "Expected exception not thrown - value 1" );
       }
       catch ( NullPointerException npe )
       {
         //System.out.println("NPE caught - dval 1");
       }
       try {
         dval =  range.doubleValue2();
         fail( "Expected exception not thrown - value 1" );
       }
       catch ( NullPointerException npe )
       {
         //System.out.println("NPE caught - dval 2");
       }
    }

    private void failCheckValue1( Range range )
    {
       int    ival;
       double dval;

       try {
         ival =  range.intValue1();
         fail( "Expected exception not thrown." );
       }
       catch ( NumberFormatException nfe )
       {
         assertEquals( "nonnumeric range", nfe.getMessage() );
       }

       try {
         dval =  range.doubleValue1();
         fail( "Expected exception not thrown." );
       }
       catch ( NumberFormatException nfe )
       {
         assertEquals( "nonnumeric range", nfe.getMessage() );
       }
    }

    private void failCheckValue2( Range range )
    {
       int    ival;
       double dval;

       try {
         ival =  range.intValue2();
         fail( "Expected exception not thrown." );
       }
       catch ( NumberFormatException nfe )
       {
         assertEquals( "nonnumeric range", nfe.getMessage() );
       }

       try {
         dval =  range.doubleValue2();
         fail( "Expected exception not thrown." );
       }
       catch ( NumberFormatException nfe )
       {
         assertEquals( "nonnumeric range", nfe.getMessage() );
       }
    }


   /**
    * The various combinations of arguments result in a few expected results for
    * each of the tested fields.. this method consolidates the code.
    *
    * Behaviour outside of expectations or that appear inconsistent are indicated 
    * by Issue statements.  These may or may not be actual issues.
    *
    */
    private void baseTest( RangeType type, String value1, String value2,
                           boolean order, boolean isDate)
    {
      r = null;

      // Construct Range object.
      if ( exp_constructor.equals("OK") )
      {
         try {
           r = new Range( type, value1, value2, order, isDate );
         }
         catch ( InvalidDateException e )
         {
           fail( e.getMessage() );
         }
         assertNotNull( r );
      }
      else if ( exp_constructor.equals("NPE") )
      {
         try {
           r = new Range( type, value1, value2, order, isDate );
           fail( "Expected exception not thrown.  Issue #1 resolved?" );
         }
         catch ( NullPointerException np )
         {
           System.out.println("".format("Test %3d - %s",testnum,"Issue 1: NPE if value1 is null.") );
           assertNull( r );
           return;
         }
         catch ( InvalidDateException e )
         {
           fail( e.getMessage() );
         }
      }
      else if ( exp_constructor.equals("IAE") )
      { // Invalid Argument Exception 
         try {
           r = new Range( type, value1, value2, order, isDate );
         }
         catch ( InvalidDateException e )
         {
           fail( e.getMessage() );
         }
         assertNotNull( r );
         if ( (type == RangeType.CLOSED)&&(value2 == null) ) 
	   System.out.println("".format("Test %3d - %s",testnum,"Issue 3: CLOSED type should require two non-null values.") );
         else if ( (value2 != null)&&((type == RangeType.LOVAL)||(type == RangeType.HIVAL)||(type == RangeType.ONEVAL)) ) 
	   System.out.println("".format("Test %3d - %s",testnum,"Issue 6: Single value types allow second non-null value.") );

      }
      else if ( exp_constructor.equals("IDE") )
      { // Invalid Date Exception 
         try {
           r = new Range( type, value1, value2, order, isDate );
           if ( type == RangeType.ANY )
             System.out.println("".format("Test %3d - %s",testnum,"Issue 5: ANY type does not validate values.") );
           else
             System.out.println("".format("Test %3d - %s",testnum,"Issue 4: Numeric value is valid Date? Possible DateParser issue.") );
           //fail( "Expected exception not thrown." );
         }
         catch ( InvalidDateException ide )
         {
           assertNull( r );
           return;
         }
      }

      // Verify numeric flag value
      if ( (type == RangeType.ANY)&&(exp_numeric == false)&&(r.numeric == true ) )
        System.out.println("".format("Test %3d - %s",testnum,"Issue 2: ANY type forces numeric=true for non-numeric values.") );
      else
        assertEquals( exp_numeric, r.numeric );

      // Verify isDate flag value
      assertEquals( exp_isdate, r.isoDate );

      // Verify values
      if ( ((type == RangeType.LOVAL)||(type == RangeType.HIVAL)||(type == RangeType.ONEVAL))&&(order == true ) )
      {
        System.out.println("".format("Test %3d - %s",testnum,"Issue 7: Single value types sort value pair.") );
        //System.out.println("".format("   VALUE 1 = %s  VALUE 2 = %s",r.stringValue1(),r.stringValue2()) );

       if (value2 != null )
         System.out.println("".format("Test %3d - %s",testnum,"Issue 9: Single value types allow access to second value.") );
      }
      else if ( (type == RangeType.ANY)&&(order == true ) )
        System.out.println("".format("Test %3d - %s",testnum,"Issue 8: ANY type does NOT sort value pair.") );
      else
      {
        assertEquals( exp_value1, r.stringValue1() );

        if (((type == RangeType.LOVAL)||(type == RangeType.HIVAL)||(type == RangeType.ONEVAL))&&(value2 != null) )
          System.out.println("".format("Test %3d - %s",testnum,"Issue 9: Single value types allow access to second value.") );

        assertEquals( exp_value2, r.stringValue2() );
      }

    }


}
