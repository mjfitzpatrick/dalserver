var xmlData =
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<VOTABLE xmlns=\"http://www.ivoa.net/xml/VOTable/v1.2\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" version=\"1.2\">\n"
        + "  <RESOURCE>\n"
        + "    <TABLE>\n"
        + "      <FIELD name=\"Job ID\" datatype=\"char\" arraysize=\"*\" />\n"
        + "      <FIELD name=\"User\" datatype=\"char\" arraysize=\"*\" />\n"
        + "      <FIELD name=\"Started\" datatype=\"char\" arraysize=\"*\" />\n"
        + "      <FIELD name=\"Status\" datatype=\"char\" arraysize=\"*\" />\n"
        + "      <FIELD name=\"Command\" datatype=\"char\" arraysize=\"*\" />\n"
        + "      <FIELD name=\"VM Type\" datatype=\"char\" arraysize=\"*\" />\n"
        + "      <FIELD name=\"CPUs\" datatype=\"int\" />\n"
        + "      <FIELD name=\"Memory\" datatype=\"long\" />\n"
        + "      <FIELD name=\"Job Starts\" datatype=\"int\" />\n"
        + "      <DATA>\n"
        + "        <TABLEDATA>\n"
        + "          <TR>\n"
        + "            <TD>735.0</TD>\n"
        + "            <TD>jenkinsd</TD>\n"
        + "            <TD />\n"
        + "            <TD>Idle</TD>\n"
        + "            <TD>sleep</TD>\n"
        + "            <TD>Tomcat</TD>\n"
        + "            <TD>1</TD>\n"
        + "            <TD>3072</TD>\n"
        + "            <TD>0</TD>\n"
        + "          </TR>\n"
        + "          <TR>\n"
        + "            <TD>734.0</TD>\n"
        + "            <TD>jenkinsd</TD>\n"
        + "            <TD />\n"
        + "            <TD>Idle</TD>\n"
        + "            <TD>sleep</TD>\n"
        + "            <TD>Tomcat</TD>\n"
        + "            <TD>1</TD>\n"
        + "            <TD>3072</TD>\n"
        + "            <TD>0</TD>\n"
        + "          </TR>\n"
        + "        </TABLEDATA>\n"
        + "      </DATA>\n"
        + "    </TABLE>\n"
        + "    <INFO name=\"STUFF\" value=\"INFO_TEXT\" />\n"
        + "  </RESOURCE>\n"
        + "</VOTABLE>";

var xmlDOM = (new DOMParser()).parseFromString(xmlData, "text/xml");

test("Read in simple VOTable.", 6, function ()
{
  try
  {
    // Create a DOM to pass in.
    var voTableBuilder = new cadc.vot.VOTableXMLBuilder(xmlDOM);
    var genericBuilder = new cadc.vot.Builder(30000, {"xmlDOM": xmlDOM});
    voTableBuilder.build(genericBuilder.buildRowData);

    equal(voTableBuilder.getVOTable().getResources().length, 1,
          "Should be one resource.");

    console.log("VOTable: " + JSON.stringify(voTableBuilder.getVOTable().getResources()[0]));

    var firstTableObject =
        voTableBuilder.getVOTable().getResources()[0].getTables()[0];
    equal(firstTableObject.getFields().length, 9, "Should have nine fields.");
    equal(firstTableObject.getTableData().getRows().length, 2,
          "Should have two rows.");

    var firstRow = firstTableObject.getTableData().getRows()[0];
    equal(firstRow.getCells()[1].getValue(), "jenkinsd",
          "Should be 'jenkinsd' in second cell of first row.");
    ok(!isNaN(firstRow.getCells()[6].getValue()) && (firstRow.getCells()[6].getValue() == Number(1)),
       "Should be numeric value in seventh cell of first row.");
    ok(!isNaN(firstRow.getCells()[7].getValue()) && (firstRow.getCells()[7].getValue() == Number(3072)),
       "Should be numeric value in eighth cell of first row.");
  }
  catch (error)
  {
    console.log(error.stack);
  }
});

test ("XPath resolution.", 3, function()
{
  var testSubject = new cadc.vot.xml.VOTableXPathEvaluator(xmlDOM, "votable");

  var result1 = testSubject.evaluate("/VOTABLE/RESOURCE[1]/INFO");

  equal(1, result1.length, "Should be one item.");
  equal(result1[0].getAttribute("name"), "STUFF",
        "Wrong name in /VOTABLE/RESOURCE[1]/INFO");
  equal(result1[0].getAttribute("value"), "INFO_TEXT",
        "Wrong value in /VOTABLE/RESOURCE[1]/INFO");
});
