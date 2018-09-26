test("Serialize table state to a URL", 16, function ()
{
  // No parameters
  var baseUrl = "http://localhost.com/foo/bar/search";
  var sortColumn, sortDirection, columns, widths, filters, units;

  var serializer = new cadc.vot.ResultStateSerializer(baseUrl, sortColumn,
                                                      sortDirection, columns,
                                                      widths, filters, units);
  var actual = serializer.getResultStateUrl();
  var expected = baseUrl;

  ok(actual, "url should not be empty");

  equal(actual, expected, "Incorrect url returned");

  // No query parameters but state parameters.
  sortColumn = "foo";
  sortDirection = "dsc";

  serializer = new cadc.vot.ResultStateSerializer(baseUrl, sortColumn,
                                                  sortDirection, columns,
                                                  widths, filters, units);
  actual = serializer.getResultStateUrl();
  expected = baseUrl + "#sortCol=foo&sortDir=dsc";

  ok(actual, "url should not be empty");
  equal(actual, expected, "Incorrect url returned");

  // Minimal parameters.
  baseUrl += "?Observation.target.name=alpha%20beta&foo=bar";
  sortColumn = '';
  sortDirection = '';

  serializer = new cadc.vot.ResultStateSerializer(baseUrl, sortColumn,
                                                  sortDirection, columns,
                                                  widths, filters, units);
  actual = serializer.getResultStateUrl();
  expected = baseUrl;

  ok(actual, "url should not be empty");
  equal(actual, expected, "Incorrect url returned");

  // Add sort column and direction.
  sortColumn = "foo";
  sortDirection = "dsc";

  serializer = new cadc.vot.ResultStateSerializer(baseUrl, sortColumn,
                                                  sortDirection, columns,
                                                  widths, filters, units);
  actual = serializer.getResultStateUrl();
  expected = baseUrl + "#sortCol=foo&sortDir=dsc";

  ok(actual, "url should not be empty");
  equal(actual, expected, "Incorrect url returned");

  // Add array of column ID's.
  columns = [
    {id: "id1"},
    {id: "id2"},
    {id: "id3"}
  ];

  serializer = new cadc.vot.ResultStateSerializer(baseUrl, sortColumn,
                                                  sortDirection, columns,
                                                  widths, filters, units);
  actual = serializer.getResultStateUrl();
  expected =
    baseUrl + "#sortCol=foo&sortDir=dsc&col_1=id1;;;&col_2=id2;;;&col_3=id3;;;";

  ok(actual, "url should not be empty");
  equal(actual, expected, "Incorrect url returned");

  // Add column widths
  widths = {id1: 75, id3: 150};

  serializer = new cadc.vot.ResultStateSerializer(baseUrl, sortColumn,
                                                  sortDirection, columns,
                                                  widths, filters, units);
  actual = serializer.getResultStateUrl();
  expected = baseUrl + "#sortCol=foo&sortDir=dsc&col_1=id1;75;;&col_2=id2;;;&col_3=id3;150;;";

  ok(actual, "url should not be empty");
  equal(actual, expected, "Incorrect url returned");

  // Add columns filters
  filters = {id2: "id2 filter", id3: "id3 filter"};

  serializer = new cadc.vot.ResultStateSerializer(baseUrl, sortColumn,
                                                  sortDirection, columns,
                                                  widths, filters, units);
  actual = serializer.getResultStateUrl();
  expected = baseUrl + "#sortCol=foo&sortDir=dsc&col_1=id1;75;;&col_2=id2;;id2%20filter;&col_3=id3;150;id3%20filter;";

  ok(actual, "url should not be empty");
  equal(actual, expected, "Incorrect url returned");

  // Add units
  units = {id1: "H:M:S", id2: "km/s"};

  serializer = new cadc.vot.ResultStateSerializer(baseUrl, sortColumn,
                                                  sortDirection, columns,
                                                  widths, filters, units);
  actual = serializer.getResultStateUrl();
  expected = baseUrl + "#sortCol=foo&sortDir=dsc&col_1=id1;75;;H:M:S&col_2=id2;;id2%20filter;km/s&col_3=id3;150;id3%20filter;";

  ok(actual, "url should not be empty");

  equal(actual, expected, "Incorrect url returned");
});

test("Deserialize an url to voview options", 12, function ()
{
  var expected = {};

  // Missing start of parameters parameter.
  var baseUrl = "http://localhost.com/search?Observation.target.name=alpha%20beta&foo=bar";
  var url = baseUrl;
  var deserializer = new cadc.vot.ResultStateDeserializer(url);
  var actual = deserializer.getViewerOptions();

  ok(actual, "State should not be undefined or null");
  deepEqual(actual, expected, "State does not match");

  // State parameters with sort column and direction.
  url = baseUrl + "#sortCol=foo&sortDir=asc";
  deserializer = new cadc.vot.ResultStateDeserializer(url);
  actual = deserializer.getViewerOptions();
  expected = {};
  expected.sortColumn = "foo";
  expected.sortDir = "asc";

  ok(actual, "State should not be undefined or null");

  console.log("Found: \n" + JSON.stringify(actual) + " but expected \n" + JSON.stringify(expected));

  deepEqual(actual, expected, "State does not match");

  // State parameters with columns id's.
  url = baseUrl + "#sortCol=foo&sortDir=asc&col_1=id1&col_3=id3&col_2=id2";
  deserializer = new cadc.vot.ResultStateDeserializer(url);
  actual = deserializer.getViewerOptions();
  expected = {};
  expected.sortColumn = "foo";
  expected.sortDir = "asc";
  expected.defaultColumnIDs = ["id1", "id2", "id3"];

  ok(actual, "State should not be undefined or null");
  deepEqual(actual, expected, "State does not match");

  // State parameter with id's and widths.
  url = baseUrl + "#sortCol=foo&sortDir=asc&col_1=id1;50&col_3=id3;;;&col_2=id2;75";
  deserializer = new cadc.vot.ResultStateDeserializer(url);
  actual = deserializer.getViewerOptions();
  expected = {};
  expected.sortColumn = "foo";
  expected.sortDir = "asc";
  expected.defaultColumnIDs = ["id1", "id2", "id3"];
  expected.columnOptions =
  {
    id1: {width: "50"},
    id2: {width: "75"}
  };

  ok(actual, "State should not be undefined or null");
  deepEqual(actual, expected, "State does not match");

  // State parameters with id's, width, filters.
  url = baseUrl + "#sortCol=foo&sortDir=asc&col_1=id1;50;filter%201&col_3=id3;;filter%203;&col_2=id2;75";
  deserializer = new cadc.vot.ResultStateDeserializer(url);
  actual = deserializer.getViewerOptions();
  expected = {};
  expected.sortColumn = "foo";
  expected.sortDir = "asc";
  expected.defaultColumnIDs = ["id1", "id2", "id3"];
  expected.columnOptions = {id1: {width: "50"}, id2: {width: "75"}};
  expected.columnFilters = {id1: "filter 1", id3: "filter 3"};

  ok(actual, "State should not be undefined or null");
  deepEqual(actual, expected, "State does not match");

  // State parameters with id's, width, filters, and units.
  url = baseUrl + "#sortCol=foo&sortDir=asc&col_1=id1;50;filter%201&col_3=id3;;filter%203;km/s&col_2=id2;75";
  deserializer = new cadc.vot.ResultStateDeserializer(url);
  actual = deserializer.getViewerOptions();
  expected = {};
  expected.sortColumn = "foo";
  expected.sortDir = "asc";
  expected.defaultColumnIDs = ["id1", "id2", "id3"];
  expected.columnOptions =
  {
    id1: {width: "50"},
    id2: {width: "75"},
    id3: {header: {
      units: [
        {label: "km/s", value: "km/s", default: true}
      ]
    }
    }
  };
  expected.columnFilters = {id1: "filter 1", id3: "filter 3"};

  ok(actual, "State should not be empty");
  deepEqual(actual, expected, "State does not match");
});

test("Merge url viewer options into a default viewer options", 1, function ()
{
  var defaultOptions =
  {
    sortColumn: "bar",
    sortDir: "dsc",
    columnOptions: {
      id2: {
        width: "100"
      },
      id3: {
        width: "100",
        header: {units: [
          {label: "m/s", value: "m/s", default: true}
        ]}
      }
    },
    columnFilters: {id1: "default filter", id3: "default filter"},
    defaultColumnIDs: ["col1", "col2", "col3"]
  };

  var expectedOptions =
  {
    sortColumn: "foo",
    sortDir: "asc",
    columnOptions: {
      id1: {
        width: "50"
      },
      id2: {
        width: "75"
      },
      id3: {
        width: "100",
        header: {units: [
          {label: "km/s", value: "km/s", default: true}
        ]}
      }
    },
    columnFilters: {id1: "filter 1", id3: "filter 3"},
    defaultColumnIDs: ["id1", "id2", "id3"]
  };

  var baseUrl = "http://localhost.com/search?Observation.target.name=alpha%20beta&foo=bar";
  var url = baseUrl + "#sortCol=foo&sortDir=asc&col_1=id1;50;filter%201&col_3=id3;;filter%203;km/s&col_2=id2;75";

  var deserilaizer = new cadc.vot.ResultStateDeserializer(url);
  var urlOptions = deserilaizer.getViewerOptions();

  $.extend(true, defaultOptions, urlOptions);

  deepEqual(defaultOptions, expectedOptions, "Objects do not match");
});
