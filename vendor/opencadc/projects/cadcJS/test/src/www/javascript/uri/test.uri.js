test("Test URI components from full URL.", 1, function()
{
  var myURI = new cadc.web.util.URI("http://www.mysite.com/path/1/2/item.txt");

  equal(myURI.getPath(), "/path/1/2/item.txt",
        "Output should be /path/1/2/item.txt");
});

test("Test URI components from full URL 2.", 3, function()
{
  var myURI = new cadc.web.util.URI("http://www.mysite.com/path/item.txt?a=b&c=d");

  equal(myURI.getPath(), "/path/item.txt",
        "Path should be /path/item.txt");

  var q = myURI.getQuery();

  equal("b", q.a[0], "Query string param a is wrong.");
  equal("d", q.c[0], "Query string param a is wrong.");
});

test("Test empty query.", 1, function()
{
  var myURI = new cadc.web.util.URI("http://www.mysite.com/path/");

  var q = myURI.getQuery();

  equal("{}", JSON.stringify(q), "Query object should be empty.");
});

test("Test URI components from URI.", 1, function()
{
  var myURI = new cadc.web.util.URI("caom2:path/a/b/item.fits");

  equal(myURI.getPath(), "path/a/b/item.fits",
        "Output should be path/a/b/item.fits");
});

test("Test parse out full relative URI.", 1, function()
{
  var testSubject =
      new cadc.web.util.URI("http://www.mysite.com/path/item.txt?a=b&c=d");

  equal(testSubject.getRelativeURI(), "/path/item.txt?a=b&c=d",
        "Relative URI should be: /path/item.txt?a=b&c=d");
});

test("Test parse out path only relative URI.", 2, function()
{
  var testSubject =
      new cadc.web.util.URI("http://www.mysite.com/path/item.txt");

  equal(testSubject.getRelativeURI(), "/path/item.txt",
        "Relative URI should be: /path/item.txt");

  // Test for encoded query parameters.
  testSubject = new cadc.web.util.URI(
      "http://www.mysite.com/my/path?A=B%20C.D%20AS%20%22E%22");

  equal(testSubject.getRelativeURI(), "/my/path?A=B%20C.D%20AS%20%22E%22",
        "Relative URI should be: /my/path?A=B%20C.D%20AS%20%22E%22");
});

test("Test decode query parameter components.", 2, function()
{
  var testSubject =
      new cadc.web.util.URI("http://www.mysite.com/path/item.txt?A=");

  deepEqual(testSubject.getQueryValues("A"), [""],
            "Query values for 'A' should be empty array.");

  // Test for encoded query parameters.
  testSubject = new cadc.web.util.URI(
      "http://www.mysite.com/my/path?A=B%20C.D%20AS%20%22E%22");

  deepEqual(testSubject.getQueryValues("A"), ["B C.D AS \"E\""],
            "Query values for 'A' should have item with spaces.");
});

test("Handle multiple values for single key.", 1, function()
{
  var testSubject =
      new cadc.web.util.URI("http://www.mysite.com/path/item.txt?A=Eh&A=S");

  deepEqual(testSubject.getQueryValues("A"), ["Eh", "S"],
            "Query values for 'A' should have two items ['Eh', 'S'].");
});
