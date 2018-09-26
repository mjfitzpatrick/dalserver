package dalserver.siapv2;

import java.util.ArrayList;
import java.util.Set;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import dalserver.TableGroup;
import dalserver.TableParam;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

public class SiapKeywordFactoryTest {

    // SiapKeywordFactory:
    //   + LinkedHashMap map
    //     - keys = String 
    //     - values = TableGroup or TableParam objects
    //     Each value is stored with 2 keys ("id" and "utype")
    //     For Groups id == utype so only 1 set.
    //
    //   + Constructors
    //     - default SiapKeywordFactory() ==  SiapKeywordFactory("main") 
    //         includes all items up to the 'DATA' section of the SiapKeywordData list.
    //     - SiapKeywordFactory("data") 
    //         includes only items from the 'DATA' section of the SiapKeywordData list.
    //
    //   + Methods:
    //     - addGroup(..), addParam(..) assign object to map
    //     - newGroup(..), newParam(..), newField(..) extract a copy 
    //     - iterator() delivers iterator through EntrySet of Map
    //     - entrySet() delivers EntrySet to Map content.
    // 

    SiapKeywordFactory tab = null;
    SiapKeywordFactory dat = null;

    Boolean verbose = false;

    @Before 
    public void setup(){
        tab = new SiapKeywordFactory();
        dat = new SiapKeywordFactory("data");
    }

    @After 
    public void teardown() {
        tab = null;
    }

    @Test
    public void testContent()
    {
     // Check number of entries
     //   CSV Spreadsheet has 252 rows;
     //                                    Core  Data  Total
     //    - value elements (params)        137    61    198
     //    - node elements (groups)          21     8     29
     //    - heading lines  (garbage)        22     3     26
     //                                    ----  ----  -----
     //                                     180    72    252 rows
     //
     //   The Factory map should have
     //                                    Core  Data  Total
     //    - TableParam entries by 'id'     137    61    198
     //    - TableParam entries by 'utype'  137    61    198
     //    - TableGroup entries              21     8     29
     //                                    ----  ----  -----
     //                                     295   130    425 entries

      Object item;
      Iterator ii;

      int nrecs   = 0;
      int ngroups = 0;
      int nparams = 0;
      int nother  = 0;
      int section;

      nrecs = tab.entrySet().size() + dat.entrySet().size();
      assertEquals(425, nrecs);


      if (verbose)
        System.out.println("testContent:");

      // Check the number of each type of object in the map
      for ( section = 0; section < 2; section++ )
      {
        int ng = 0;
        int np = 0;
        int no = 0;

        ii = section == 0 ? tab.iterator(): dat.iterator();
        while ( ii.hasNext() )
        {
          item = (Object)((Map.Entry)ii.next()).getValue();
  
          if ( item instanceof TableGroup)
            ng += 1;
          else if ( item instanceof TableParam )
            np += 1;
          else
          {
            System.out.println(" Other Type = "+item.getClass().getSimpleName());
            no += 1;
          }
        }
        if (verbose)
        {
          System.out.println("  Section == "+ section );
          System.out.println("  Number of Groups = "+ng);
          System.out.println("  Number of Params = "+np);
          System.out.println("  Number of Other  = "+no);
          System.out.println("");
        }
        if ( section == 0 ) // Core 
        {
          assertEquals( 21, ng);
          assertEquals(274, np);
          assertEquals(  0, no);
        }
        else // Data
        {
          assertEquals(  8, ng);
          assertEquals(122, np);
          assertEquals(  0, no);
        }

        ngroups += ng;
        nparams += np;
        nother  += no;

      }

      // Verify number of each type;
      if (verbose)
      {
        System.out.println("  Total Number of Groups = "+ngroups);
        System.out.println("  Total Number of Params = "+nparams);
        System.out.println("  Total Number of Other  = "+nother);
      }

      assertEquals( 29, ngroups); // No CHAR
      assertEquals(396, nparams);
      assertEquals(  0, nother);

    }

    @Test
    public void testGroups(){
      // Check the stored Groups 

      //
      // List of expected Groups from ImageDM V1.0 2013-08-11, Siap V2.0 2013-08-12
      //  * NOTE: a Group is an organizational group and not necessarily
      //          equivalent to a model Node.
      //          For example, there is no group "Char.SpectralAxis.Coverage" 
      //          even though it is a complex object, but there is a group
      //          "Derived.Redshift" which is much less so.
      //
      ArrayList<String> extras     = new ArrayList<String>();

      ArrayList<String> coreGroups = new ArrayList<String>();
      coreGroups.add("DATASET");
      //coreGroups.add("DATASET.DATAMODEL");  // removed 20130826
      coreGroups.add("DATASET.IMAGE");
      coreGroups.add("DATAID");
      coreGroups.add("PROVENANCE");
      coreGroups.add("CURATION");
      coreGroups.add("TARGET");
      coreGroups.add("DERIVED");
      //coreGroups.add("DERIVED.REDSHIFT");  // removed 20130826
      coreGroups.add("COORDSYS");
      coreGroups.add("COORDSYS.SPACEFRAME");
      coreGroups.add("COORDSYS.TIMEFRAME");
      coreGroups.add("COORDSYS.SPECTRALFRAME");
      coreGroups.add("COORDSYS.REDSHIFTFRAME");
      coreGroups.add("COORDSYS.FLUXFRAME");
      coreGroups.add("CHAR");
      coreGroups.add("CHAR.SPATIALAXIS");
      coreGroups.add("CHAR.SPECTRALAXIS");
      coreGroups.add("CHAR.TIMEAXIS");
      coreGroups.add("CHAR.POLAXIS");
      coreGroups.add("CHAR.FLUXAXIS");
      coreGroups.add("QUERY");
      coreGroups.add("ASSOCIATION");
      coreGroups.add("ACCESS");

      ArrayList<String> dataGroups = new ArrayList<String>();
      dataGroups.add("DATA");
      dataGroups.add("DATA.MAPPING");
      dataGroups.add("DATA.MAPPING.AXIS");
      dataGroups.add("DATA.MAPPING.SPATIALAXIS");
      dataGroups.add("DATA.MAPPING.SPECTRALAXIS");
      dataGroups.add("DATA.MAPPING.TIMEAXIS");
      dataGroups.add("DATA.MAPPING.POLAXIS");
      dataGroups.add("DATA.OBSDATA");

      Iterator ii = tab.iterator();
      Map.Entry me;
      Object item;
      String key;

      if (verbose)
      {
        System.out.println("testGroups:");
        System.out.println( String.format("  %-30s %s", "KEY", "NAME") );
        System.out.println( String.format("  %-30s %s", "---", "----") );
      }

      while ( ii.hasNext() )
      {
	me   = (Map.Entry)ii.next();
        item = (Object)me.getValue();
        key  = (String)me.getKey();

        if ( item instanceof TableGroup) {
          TableGroup group = (TableGroup)item;
          String name = group.getName().toUpperCase();

          if (verbose)
            System.out.println( String.format("  %-30s %s", key, group.getName()) );

          if ( coreGroups.contains( name ) )
            coreGroups.remove( name );
          else
            extras.add( name );
        }
      }

      if (verbose)
      {
        System.out.println("");

        ii = coreGroups.iterator();
        while ( ii.hasNext() )
        {
          System.out.println("  Missing Group = "+ii.next());
        }
  
        System.out.println("");
        ii = extras.iterator();
        while ( ii.hasNext() )
        {
          System.out.println("  Extra Group = "+ii.next());
        }
      }

      // At this point all Core groups should have been found, with no 'extras'
      //assertTrue( coreGroups.isEmpty() );
      assertEquals( 1, coreGroups.size() );
      assertTrue( extras.isEmpty() );

    }

    @Test
    public void testCoreParams(){
      // Spot Check the stored Params

      //  There are a LOT of params.. we spot check some example records for accuracy
      //  This test loops through ALL the param records and will list them under verbose.
      //  For verification, we just look closely at a few.

      ArrayList<String> params = new ArrayList<String>();
      params.add("DATAMODELPREFIX");
      params.add("PUBLISHERDID");
      params.add("TARGETPOS");
      params.add("SPECTRALFRAMEREFPOS");
      params.add("DATAID");
      params.add("SPATIALLOLIMIT");
      params.add("REFPIXEL");
      params.add("SPATIALLONPOLE");
      params.add("OBSFORMAT");
      params.add("POLAXISENUM");

      Iterator ii = tab.iterator();
      Object item;
      Map.Entry me;
      String key;

      if (verbose)
      {
        System.out.println("testParams:");
        System.out.println( String.format("  %-70s %30s %70s", "KEY", "NAME", "UType") );
        System.out.println( String.format("  %-70s %30s %70s", "---", "----", "-----") );
      }

      while ( ii.hasNext() )
      {
        me   = (Map.Entry)ii.next();
        item = (Object)me.getValue();
        key  = (String)me.getKey();

        if ( item instanceof TableParam) {
          TableParam  p = (TableParam)item;
          String name = p.getName().toUpperCase();

          if (verbose)
            System.out.println( String.format("  %-70s %30s %70s", key, p.getName(), p.getUtype()) );
            //System.out.println("  Param = "+p.getName()+"  "+p.getUtype());

          if ( params.contains( name ) )
          {
            // interrogate.
            if ( name.equals("DATAMODELPREFIX") )
            {
              assertTrue( p.getUtype().toUpperCase().equals("DATASET.DATAMODEL.PREFIX") );
              assertTrue( p.getDataType().toUpperCase().equals("CHAR") );
              assertEquals( p.getArraySize(), "*" );
              assertEquals( p.getUnit(), "" );
              assertEquals( p.getUcd(), "" );
              assertTrue( p.getDescription().equals("Data model prefix"));
              assertNull( p.getFitsKeyword() );  // This is NULL? others are empty strings
              //assertEquals( p.getHint().toUpperCase(), "" );  // Is blank in spreadsheet 
              //assertEquals( p.getHint().toUpperCase(), "QP" );
              assertEquals( p.getHint().toUpperCase(), "MP" );  // 20130829

            }
            if ( name.equals("PUBLISHERDID") )
            {
              assertTrue( p.getUtype().toUpperCase().equals("CURATION.PUBLISHERDID") );
              assertTrue( p.getDataType().toUpperCase().equals("CHAR") );
              assertEquals( p.getArraySize(), "*" );
              assertEquals( p.getUnit(), "" );
              assertTrue( p.getUcd().equals("meta.ref.url;meta.curation" ) );
              assertTrue( p.getDescription().equals("Publisher's ID for the dataset ID" ) );
              assertTrue( p.getFitsKeyword().equals("DS_IDPUB") );
              assertEquals( p.getHint().toUpperCase(),"Q" );
            }
            if ( name.equals("TARGETPOS") )
            {
              assertTrue( p.getUtype().toUpperCase().equals("TARGET.POS") );
              assertTrue( p.getDataType().toUpperCase().equals("DOUBLE") );
              assertEquals( p.getArraySize(), "2" );
              assertEquals( p.getUnit().toUpperCase(), "DEG" );
              assertEquals( p.getUcd(), "pos.eq;src" );
              assertEquals( p.getDescription(),"Target RA and Dec" );
              assertEquals( p.getFitsKeyword(), "RA_TARG DEC_TARG" );
              //assertEquals( p.getHint().toUpperCase(), "" );  // Is blank in spreadsheet 
              //assertEquals( p.getHint().toUpperCase(), "Q" ); // last iteration
              assertNull( p.getHint() ); // 20130829
            }
            if ( name.equals("SPECTRALFRAMEREFPOS") )
            {
              assertTrue( p.getUtype().toUpperCase().equals("COORDSYS.SPECTRALFRAME.REFPOS") );
              assertTrue( p.getDataType().toUpperCase().equals("CHAR") );
              assertEquals( p.getArraySize(), "*" );
              assertEquals( p.getUnit(), "" );
              assertEquals( p.getUcd(), "spect.frame" );
              assertEquals( p.getDescription(), "Spectral frame origin");
              assertEquals( p.getFitsKeyword(), "SPECSYS" );
              //assertEquals( p.getHint().toUpperCase(), "QP" );
              assertEquals( p.getHint().toUpperCase(), "P" ); // 20130829
            }
            if ( name.equals("DATAID") )
            {
              assertTrue( p.getUtype().toUpperCase().equals("DATA.ID") );
              assertTrue( p.getDataType().toUpperCase().equals("CHAR") );
              assertEquals( p.getArraySize(), "*" );
              assertEquals( p.getUnit(), "" );
              assertEquals( p.getUcd(), "" );
              assertEquals( p.getDescription(), "ID string of data element" );
              assertNull( p.getFitsKeyword() );
              assertNull( p.getHint() );
            }
            if ( name.equals("SPATIALLOLIMIT") )
            {
              assertTrue( p.getUtype().toUpperCase().equals("CHAR.SPATIALAXIS.COVERAGE.BOUNDS.LIMITS.LOLIMIT2VEC") );
              assertTrue( p.getDataType().toUpperCase().equals("DOUBLE") );
              assertEquals( p.getArraySize(), "2" );
              assertEquals( p.getUnit(), "deg" );
              assertEquals( p.getUcd(), "" );
              assertEquals( p.getDescription(), "Lower bounds of image spatial coordinates" );
              assertNull( p.getFitsKeyword() );
              assertEquals( p.getHint().toUpperCase(), "Q" );
            }
            if ( name.equals("REFPIXEL") )
            {
              assertTrue( p.getUtype().toUpperCase().equals("DATA.MAPPING.REFPIXEL") );
              assertTrue( p.getDataType().toUpperCase().equals("DOUBLE") );
              assertEquals( p.getArraySize(), "*" );
              assertEquals( p.getUnit(), "" );
              assertEquals( p.getUcd(), "" );
              assertEquals( p.getDescription(), "Reference pixel" );
              assertEquals( p.getFitsKeyword(), "CRPIX" );
              //assertEquals( p.getHint().toUpperCase(), "" );  // Is blank in spreadsheet 
              assertEquals( p.getHint().toUpperCase(), "Q" );
            }
            if ( name.equals("SPATIALLONPOLE") )
            {
              assertTrue( p.getUtype().toUpperCase().equals("DATA.MAPPING.SPATIALAXIS.LONPOLE") );
              assertTrue( p.getDataType().toUpperCase().equals("DOUBLE") );
              assertEquals( p.getArraySize(), "" );
              assertEquals( p.getUnit(), "" );
              assertEquals( p.getUcd(), "" );
              assertEquals( p.getDescription(), "Native longitude of the celestial pole" );
              assertEquals( p.getFitsKeyword(), "LONPOLE" );
              assertNull( p.getHint() );
            }
            if ( name.equals("OBSFORMAT") )
            {
              assertTrue( p.getUtype().toUpperCase().equals("DATA.OBSDATA.FORMAT") );
              assertTrue( p.getDataType().toUpperCase().equals("CHAR") );
              assertEquals( p.getArraySize(), "*" );
              assertEquals( p.getUnit(), "" );
              assertEquals( p.getUcd(), "" );
              assertEquals( p.getDescription(), "Content format of the dataset" );
              assertNull( p.getFitsKeyword() );
              //assertEquals( p.getHint().toUpperCase(), "" );  // Is blank in spreadsheet 
              assertEquals( p.getHint().toUpperCase(), "Q" );
            }
            if ( name.equals("POLAXISENUM") )
            {
              assertTrue( p.getUtype().toUpperCase().equals("CHAR.POLAXIS.ENUMERATION") );
              assertTrue( p.getDataType().toUpperCase().equals("CHAR") );
              assertEquals( p.getArraySize(), "*" );
              assertEquals( p.getUnit(), "" );
              assertEquals( p.getUcd(), "" );
              assertEquals( p.getDescription(), "List of polarization states present" );
              assertNull( p.getFitsKeyword() );
              assertEquals( p.getHint().toUpperCase(), "Q" );
            }
          }
        }
      }

    }

}
