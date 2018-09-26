test("Read in simple CSV VOTable.", 32, function ()
{
  var csvData = "observationURI,collection,COORD1,COORD2,target_name,time_bounds_cval1,time_exposure,instrument_name,energy_bandpassName,observationID,calibrationLevel,energy_bounds_cval1,energy_bounds_cval2,proposal_id,proposal_pi,productID,dataRelease,AREA,position_sampleSize,dataProductType,position_timeDependent,provenance_name,provenance_keywords,intent,target_type,target_standard,type,metaRelease,sequenceNumber,algorithm_name,proposal_project,position_bounds,energy_emBand,provenance_reference,provenance_version,provenance_project,provenance_producer,provenance_runID,provenance_lastExecuted,provenance_inputs,planeID,isDownloadable,planeURI\n" +
                "caom:JCMT/scuba2_00023_20100311T051654,JCMT,73.54986721085682,-3.003333358643712,MS0451-03,55266.2200694,25.4223194122,SCUBA-2,,scuba2_00023_20100311T051654,2,8.149255583144998E-4,8.849191645501601E-4,M09BGT01,Wayne S. Holland,reduced_850,2011-08-01T23:59:59.000,0.027577705408836195,3.999999999996,image,,REDUCE_SCAN_FAINT_POINT_SOURCES,,science,,0,scan,2011-08-01T23:59:59.000,23,exposure,,POLYGON ICRS 73.63499073921241 -3.084440649974604 73.46474435295522 -3.0844413605593197 73.46475638668525 -2.92221941555282 73.63497737762808 -2.9222187424078596,Millimeter,,59b81e729415a81b6e97b4051ecf3417aca9cc1f,JCMT_STANDARD_PIPELINE,,10896,2012-04-16T01:50:40.000,caom:JCMT/scuba2_00023_20100311T051654/raw_850,-3569382803230013662,caom:JCMT/scuba2_00023_20100311T051654/reduced_850,caom:JCMT/scuba2_00023_20100311T051654/reduced_850\n" +
                "caom:JCMT/scuba2_00022_20100311T050059,JCMT,73.54875457336536,-3.0027778071108178,MS0451-03,55266.2090162,23.9776592255,SCUBA-2,,scuba2_00022_20100311T050059,2,8.149255583144998E-4,8.849191645501601E-4,M09BGT01,Wayne S. Holland,reduced_850,2011-08-01T23:59:59.000,0.026657956705587793,3.9999999999959996,image,,REDUCE_SCAN_FAINT_POINT_SOURCES,,science,,0,scan,2011-08-01T23:59:59.000,22,exposure,,POLYGON ICRS 73.63276512139471 -3.082218618006282 73.46474451782498 -3.0822191435987447 73.464756304275 -2.923330522614301 73.632752359519 -2.9233300241652036,Millimeter,,59b81e729415a81b6e97b4051ecf3417aca9cc1f,JCMT_STANDARD_PIPELINE,,10896,2012-04-16T01:50:40.000,caom:JCMT/scuba2_00022_20100311T050059/raw_850,5547310217451419004,caom:JCMT/scuba2_00022_20100311T050059/reduced_850,caom:JCMT/scuba2_00022_20100311T050059/reduced_850\n" +
                "caom:JCMT/scuba2_00039_20100311T071406,JCMT,73.54875457312625,-3.0027778071080697,MS0451-03,55266.3014583,24.0102710724,SCUBA-2,,scuba2_00039_20100311T071406,2,8.149255583144998E-4,8.849191645501601E-4,M09BGT01,Wayne S. Holland,reduced_850,2011-08-01T23:59:59.000,0.027011040946520737,3.999999999996,image,,REDUCE_SCAN_FAINT_POINT_SOURCES,,science,,0,scan,2011-08-01T23:59:59.000,39,exposure,,POLYGON ICRS 73.63387783880593 -3.0822185263467166 73.46363180003102 -3.0822190589006224 73.4636437490521 -2.9233304422902817 73.63386491435918 -2.9233299372392154,Millimeter,,59b81e729415a81b6e97b4051ecf3417aca9cc1f,JCMT_STANDARD_PIPELINE,,10896,2012-04-16T01:50:40.000,caom:JCMT/scuba2_00039_20100311T071406/raw_850,-3317462699073968167,caom:JCMT/scuba2_00039_20100311T071406/reduced_850,caom:JCMT/scuba2_00039_20100311T071406/reduced_850\n" +
                "caom:JCMT/scuba2_00019_20100313T052405,JCMT,73.54986720823958,-3.00277780509287,MS0451-03,55268.2250579,24.4709205627,SCUBA-2,,scuba2_00019_20100313T052405,2,8.149255583144998E-4,8.849191645501601E-4,M09BGT01,Wayne S. Holland,reduced_850,2011-08-01T23:59:59.000,0.028129553999884038,3.999999999996,image,,REDUCE_SCAN_FAINT_POINT_SOURCES,,science,,0,scan,2011-08-01T23:59:59.000,19,exposure,,POLYGON ICRS 73.63610345876306 -3.0844405559266708 73.4636316328872 -3.084441275799983 73.46364391614664 -2.921108228299123 73.63608983855647 -2.9211075466147545,Millimeter,,59b81e729415a81b6e97b4051ecf3417aca9cc1f,JCMT_STANDARD_PIPELINE,,10896,2012-04-16T01:50:40.000,caom:JCMT/scuba2_00019_20100313T052405/raw_850,1853232623100797660,caom:JCMT/scuba2_00019_20100313T052405/reduced_850,caom:JCMT/scuba2_00019_20100313T052405/reduced_850\n" +
                "caom:JCMT/scuba2_00035_20100312T070209,JCMT,73.5493108907036,-3.0027778062353763,MS0451-03,55267.2931597,18.2698383331,SCUBA-2,,scuba2_00035_20100312T070209,2,8.149255583144998E-4,8.849191645501601E-4,M09BGT01,Wayne S. Holland,reduced_850,2011-08-01T23:59:59.000,0.02756782837912297,3.999999999996,image,,REDUCE_SCAN_FAINT_POINT_SOURCES,,science,,0,scan,2011-08-01T23:59:59.000,35,exposure,,POLYGON ICRS 73.63499064768128 -3.0833295417760085 73.46363171645942 -3.083330167375596 73.46364383259966 -2.9222193352593853 73.63497737762808 -2.9222187424078596,Millimeter,,59b81e729415a81b6e97b4051ecf3417aca9cc1f,JCMT_STANDARD_PIPELINE,,10896,2012-04-16T01:50:40.000,caom:JCMT/scuba2_00035_20100312T070209/raw_850,2757611723272132797,caom:JCMT/scuba2_00035_20100312T070209/reduced_850,caom:JCMT/scuba2_00035_20100312T070209/reduced_850\n" +
                "caom:JCMT/scuba2_00029_20100313T070049,JCMT,73.54875457312491,-3.00277780807104,MS0451-03,55268.2922338,21.6085166931,SCUBA-2,,scuba2_00029_20100313T070049,2,8.149255583144998E-4,8.849191645501601E-4,M09BGT01,Wayne S. Holland,reduced_850,2011-08-01T23:59:59.000,0.027388817211326355,3.999999999996,image,,REDUCE_SCAN_FAINT_POINT_SOURCES,,science,,0,scan,2011-08-01T23:59:59.000,29,exposure,,POLYGON ICRS 73.63387792919976 -3.083329634629377 73.46363171645942 -3.083330167375596 73.46364383259966 -2.9222193352593853 73.6338648239917 -2.9222188304006784,Millimeter,,59b81e729415a81b6e97b4051ecf3417aca9cc1f,JCMT_STANDARD_PIPELINE,,10896,2012-04-16T01:50:40.000,caom:JCMT/scuba2_00029_20100313T070049/raw_850,1340666690510709564,caom:JCMT/scuba2_00029_20100313T070049/reduced_850,caom:JCMT/scuba2_00029_20100313T070049/reduced_850\n" +
                "caom:JCMT/scuba2_00028_20100313T064510,JCMT,73.54931089268801,-3.00333336076359,MS0451-03,55268.2813657,23.7395915985,SCUBA-2,,scuba2_00028_20100313T064510,2,8.149255583144998E-4,8.849191645501601E-4,M09BGT01,Wayne S. Holland,reduced_850,2011-08-01T23:59:59.000,0.027757951244863577,3.999999999996,image,,REDUCE_SCAN_FAINT_POINT_SOURCES,,science,,0,scan,2011-08-01T23:59:59.000,28,exposure,,POLYGON ICRS 73.63499073921241 -3.084440649974604 73.4636316328872 -3.084441275799983 73.46364383259966 -2.9222193352593853 73.63497737762808 -2.9222187424078596,Millimeter,,59b81e729415a81b6e97b4051ecf3417aca9cc1f,JCMT_STANDARD_PIPELINE,,10896,2012-04-16T01:50:40.000,caom:JCMT/scuba2_00028_20100313T064510/raw_850,-5313401758929201789,caom:JCMT/scuba2_00028_20100313T064510/reduced_850,caom:JCMT/scuba2_00028_20100313T064510/reduced_850\n" +
                "caom:JCMT/scuba2_00039_20100303T072646,JCMT,73.5415224228061,-3.008333346943164,MS0451-03,55258.3102546,30.6219978333,SCUBA-2,,scuba2_00039_20100303T072646,2,8.149255583144998E-4,8.849191645501601E-4,M09BGT01,Wayne S. Holland,reduced_850,2011-08-01T23:59:59.000,0.02756782985378159,3.999999999996,image,,REDUCE_SCAN_FAINT_POINT_SOURCES,,science,,0,scan,2011-08-01T23:59:59.000,39,exposure,,POLYGON ICRS 73.62720203480237 -3.0888857089753605 73.45584222806089 -3.088885082246605 73.45585549824929 -2.9277742771225164 73.62718991853836 -2.927774871103267,Millimeter,,59b81e729415a81b6e97b4051ecf3417aca9cc1f,JCMT_STANDARD_PIPELINE,,10896,2012-04-16T01:50:40.000,caom:JCMT/scuba2_00039_20100303T072646/raw_850,-3235834955827922946,caom:JCMT/scuba2_00039_20100303T072646/reduced_850,caom:JCMT/scuba2_00039_20100303T072646/reduced_850\n";

  var tableFields = [];
  var csvAsArray = $.csv.toArrays(csvData);
  var limit = csvAsArray[0].length;
  for (var ii = 0; ii < limit; ii++)
  {
    tableFields.push(new cadc.vot.Field(csvAsArray[0][ii]));
  }

  var tm = new cadc.vot.Metadata(
      null, null, null, null, tableFields, null);

  try
  {
    var input =
    {
      tableMetadata: tm,
      csv: "csvData",
      pageSize: 3
    };

    var eventCounter = 0;
    var pageEventCounter = 0;

    var rowBuilder = new cadc.vot.RowBuilder();
    var testSubject = new cadc.vot.CSVBuilder(30000, input, rowBuilder.buildRowData);

    var onRowAddEventHandler = function (rowData)
    {
      eventCounter++;
    };

    var onPageAddEventHandler = function ()
    {
      pageEventCounter++;
    };

    var testPrivate = function (builder, expectedRowCount, expectedLastMatch)
    {
      // these tests rely on 'private' data in the function
      equal(builder.getCurrent().rowCount, expectedRowCount, "row count wrong");
      equal(builder.getCurrent().lastMatch, expectedLastMatch, "lastMatch wrong");
    };

    testSubject.subscribe(cadc.vot.onRowAdd, onRowAddEventHandler);
    testSubject.subscribe(cadc.vot.onPageAddEnd, onPageAddEventHandler);

    var firstReturn = csvData.indexOf("\n");
    var secondReturn = csvData.indexOf("\n", firstReturn + 1);
    var thirdReturn = csvData.indexOf("\n", secondReturn + 1);
    var secondLastReturn = csvData.lastIndexOf("\n", csvData.length - 1);

    console.log("test case - an in-complete starting chunk");
    testSubject.append(csvData.slice(0, 22));
    equal(eventCounter, 0, "eventHandler should not have been called yet.");
    equal(pageEventCounter, 0,
          "pageEventHandler should not have been called yet.");
    testPrivate(testSubject, 0, 0);

    console.log("test case - the starting chunk, but only headers are a valid chunk");
    testSubject.append(csvData.slice(0, firstReturn + 22));
    equal(eventCounter, 0, "eventHandler should not have been called yet.");
    equal(pageEventCounter, 0,
          "pageEventHandler should not have been called yet.");

    // Data always ends with a new line, so row count is skewed by one.
    testPrivate(testSubject, 1, firstReturn);

    console.log("test case - the starting chunk - first row");
    testSubject.append(csvData.slice(0, secondReturn + 22));
    equal(eventCounter, 1, "eventHandler should have been called once by now.");
    equal(pageEventCounter, 0,
          "pageEventHandler should not have been called yet.");

    // Data always ends with a new line, so row count is skewed by one.
    testPrivate(testSubject, 2, secondReturn);

    console.log("test case - another row");
    testSubject.append(csvData.slice(0, thirdReturn + 22));
    equal(eventCounter, 2, "eventHandler should have been called twice by now.");
    equal(pageEventCounter, 0,
          "pageEventHandler should not have been called yet.");

    // Data always ends with a new line, so row count is skewed by one.
    testPrivate(testSubject, 3, thirdReturn);

    console.log("test case - chunk-less");
    testSubject.append(csvData.slice(0, thirdReturn + 44));
    equal(eventCounter, 2, "eventHandler should have been called twice by now.");
    equal(pageEventCounter, 0,
          "pageEventHandler should not have been called yet.");

    // Data always ends with a new line, so row count is skewed by one.
    testPrivate(testSubject, 3, thirdReturn);

    console.log("test case - the ending chunk");
    testSubject.append(csvData);
    testSubject.loadEnd();
    equal(eventCounter, 8,
          "eventHandler should have been called eight times by now.");
    equal(pageEventCounter, 3,
          "pageEventHandler should have been called three times.");

    // Data always ends with a new line, so row count is skewed by one.
    testPrivate(testSubject, 8 + 1, secondLastReturn);

    console.log("test case - new with first row of data");
    testSubject = new cadc.vot.CSVBuilder(30000, input, 
                                             rowBuilder.buildRowData);

    testSubject.subscribe(cadc.vot.onRowAdd, onRowAddEventHandler);
    testSubject.subscribe(cadc.vot.onPageAddEnd, onPageAddEventHandler);

    eventCounter = 0;
    pageEventCounter = 0;

    testSubject.append(csvData.slice(0, secondReturn + 22));
    equal(eventCounter, 1, "eventHandler should have been called once by now.");
    equal(pageEventCounter, 0, "pageEventCounter should not have been called.");
    testPrivate(testSubject, 2, secondReturn);

    console.log("test case - new with all data");
    testSubject = new cadc.vot.CSVBuilder(30000, input, 
                                             rowBuilder.buildRowData);

    testSubject.subscribe(cadc.vot.onRowAdd, onRowAddEventHandler);
    testSubject.subscribe(cadc.vot.onPageAddEnd, onPageAddEventHandler);

    eventCounter = 0;
    pageEventCounter = 0;

    testSubject.append(csvData);
    testSubject.loadEnd();
    equal(eventCounter, 8,
          "eventHandler should have been called eight times by now.");
    equal(pageEventCounter, 3,
          "pageEventHandler should have been called three times.");

    // Data always ends with a new line, so row count is skewed by one.
    testPrivate(testSubject, 8 + 1, secondLastReturn);

  }
  catch (error)
  {
    console.log(error.stack);
  }

});


test("Read a single row for events.", 5, function()
{
  var csvData = "observationURI,collection,COORD1,COORD2,target_name,time_bounds_cval1,time_exposure,instrument_name,energy_bandpassName,observationID,calibrationLevel,energy_bounds_cval1,energy_bounds_cval2,proposal_id,proposal_pi,productID,dataRelease,AREA,position_sampleSize,dataProductType,position_timeDependent,provenance_name,provenance_keywords,intent,target_type,target_standard,type,metaRelease,sequenceNumber,algorithm_name,proposal_project,position_bounds,energy_emBand,provenance_reference,provenance_version,provenance_project,provenance_producer,provenance_runID,provenance_lastExecuted,provenance_inputs,planeID,isDownloadable,planeURI\n" +
                "caom:JCMT/scuba2_00023_20100311T051654,JCMT,73.54986721085682,-3.003333358643712,MS0451-03,55266.2200694,25.4223194122,SCUBA-2,,scuba2_00023_20100311T051654,2,8.149255583144998E-4,8.849191645501601E-4,M09BGT01,Wayne S. Holland,reduced_850,2011-08-01T23:59:59.000,0.027577705408836195,3.999999999996,image,,REDUCE_SCAN_FAINT_POINT_SOURCES,,science,,0,scan,2011-08-01T23:59:59.000,23,exposure,,POLYGON ICRS 73.63499073921241 -3.084440649974604 73.46474435295522 -3.0844413605593197 73.46475638668525 -2.92221941555282 73.63497737762808 -2.9222187424078596,Millimeter,,59b81e729415a81b6e97b4051ecf3417aca9cc1f,JCMT_STANDARD_PIPELINE,,10896,2012-04-16T01:50:40.000,caom:JCMT/scuba2_00023_20100311T051654/raw_850,-3569382803230013662,caom:JCMT/scuba2_00023_20100311T051654/reduced_850,caom:JCMT/scuba2_00023_20100311T051654/reduced_850\n";

  var tableFields = [];
  var csvAsArray = $.csv.toArrays(csvData);
  var limit = csvAsArray[0].length;
  for (var ii = 0; ii < limit; ii++)
  {
    tableFields.push(new cadc.vot.Field(csvAsArray[0][ii]));
  }

  var tm = new cadc.vot.Metadata(
      null, null, null, null, tableFields, null);

  try
  {
    var input =
    {
      tableMetadata: tm,
      csv: csvData,
      pageSize: 100
    };

    var rowAddEventCounter = 0;
    var pageStartEventCounter = 0;
    var pageEndEventCounter = 0;

    var rowBuilder = new cadc.vot.RowBuilder();
    var testSubject = new cadc.vot.CSVBuilder(30000, input, rowBuilder.buildRowData);

    var onRowAddEventHandler = function (rowData)
    {
      rowAddEventCounter++;
    };

    var onPageAddEndEventHandler = function ()
    {
      pageEndEventCounter++;
    };

    var onPageAddStartEventHandler = function ()
    {
      pageStartEventCounter++;
    };

    var testPrivate = function (builder, expectedRowCount, expectedLastMatch)
    {
      // these tests rely on 'private' data in the function.
      // Data always ends with a new line, so row count is skewed by one.
      equal(builder.getCurrent().rowCount - 1, expectedRowCount, "row count wrong");
      equal(builder.getCurrent().lastMatch, expectedLastMatch, "lastMatch wrong");
    };

    testSubject.subscribe(cadc.vot.onRowAdd, onRowAddEventHandler);
    testSubject.subscribe(cadc.vot.onPageAddStart, onPageAddStartEventHandler);
    testSubject.subscribe(cadc.vot.onPageAddEnd, onPageAddEndEventHandler);

    testSubject.append(csvData);
    testSubject.loadEnd();
    equal(rowAddEventCounter, 1,
          "rowAddEventHandler should have been called.");
    equal(pageStartEventCounter, 1,
          "pageStartEventHandler should have been called.");
    equal(pageEndEventCounter, 1,
          "pageEndEventHandler should have been called.");
    testPrivate(testSubject, 1, csvData.length - 1);
  }
  catch (error)
  {
    console.log(error.stack);
  }
});
