test("Test String util.", 5, function()
{
  var stringUtil = new cadc.web.util.StringUtil("MY&&<>VAL");

  var output = stringUtil.sanitize();

  equal(output, "MY&amp;&amp;&lt;&gt;VAL",
        "Output should be MY&amp;&amp;&lt;&gt;VAL");

  equal(stringUtil.hasText(), true, "Should return true.");

  stringUtil = new cadc.web.util.StringUtil("");
  equal(stringUtil.hasText(), false, "Should return false.");

  stringUtil = new cadc.web.util.StringUtil(-14.567);
  equal(stringUtil.hasText(), true, "Should return true.");

  stringUtil = new cadc.web.util.StringUtil(0);
  equal(stringUtil.hasText(), true, "Should return true.");
});

test("Test Number Format", 5, function()
{
  var testSubject = new cadc.web.util.NumberFormat(88.0, 4);

  equal(testSubject.formatFixation(), "88.0000", "Wrong fixation.");
  equal(testSubject.formatPrecision(), "88.00", "Wrong precision.");

  /*
   * Exponent value is eleven (11).
   */
  testSubject = new cadc.web.util.NumberFormat(0.54842, 4);

  equal(testSubject.formatExponentOrFloat(), "0.5484", "Wrong %.g equivalent");

  testSubject = new cadc.web.util.NumberFormat(548428932789.25684, 4);

  equal(testSubject.formatExponentOrFloat(), "5.4843e+11",
        "Wrong %.g equivalent");

  testSubject = new cadc.web.util.NumberFormat(548428932789.25684, 12);

  equal(testSubject.formatExponentOrFloat(), "548428932789.256835937500",
        "Wrong %.g equivalent");
});
