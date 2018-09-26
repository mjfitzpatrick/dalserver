var xmlData =
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<VOTABLE xmlns=\"http://www.ivoa.net/xml/VOTable/v1.2\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" version=\"1.2\">\n"
        + "  <RESOURCE>\n"
        + "    <TABLE>\n"
        + "      <DESCRIPTION>TEST VOTABLE</DESCRIPTION>\n"
        + "      <FIELD name=\"Job ID\" datatype=\"char\" arraysize=\"*\" />\n"
        + "      <FIELD name=\"Project\" datatype=\"char\" arraysize=\"*\" />\n"
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
        + "            <TD>2011.03.66.8.S</TD>\n"
        + "            <TD>m</TD>\n"
        + "            <TD />\n"
        + "            <TD>Idle</TD>\n"
        + "            <TD>ls</TD>\n"
        + "            <TD>Tomcattime</TD>\n"
        + "            <TD>1</TD>\n"
        + "            <TD>3072</TD>\n"
        + "            <TD>0</TD>\n"
        + "          </TR>\n"
        + "          <TR>\n"
        + "            <TD>734.0</TD>\n"
        + "            <TD>2011.03.66.9.S</TD>\n"
        + "            <TD>hello</TD>\n"
        + "            <TD />\n"
        + "            <TD>Idle</TD>\n"
        + "            <TD>sle</TD>\n"
        + "            <TD>Tomcat</TD>\n"
        + "            <TD>1</TD>\n"
        + "            <TD>3072</TD>\n"
        + "            <TD>0</TD>\n"
        + "          </TR>\n"
        + "          <TR>\n"
        + "            <TD>733.0</TD>\n"
        + "            <TD>2011.03.66.10.N</TD>\n"
        + "            <TD>there</TD>\n"
        + "            <TD />\n"
        + "            <TD>Idle</TD>\n"
        + "            <TD>s</TD>\n"
        + "            <TD>t</TD>\n"
        + "            <TD>1</TD>\n"
        + "            <TD>3072</TD>\n"
        + "            <TD>0</TD>\n"
        + "          </TR>\n"
        + "        </TABLEDATA>\n"
        + "      </DATA>\n"
        + "    </TABLE>\n"
        + "  </RESOURCE>\n"
        + "</VOTABLE>";

// Create a DOM to pass in.
var xmlDOM = new DOMParser().parseFromString(xmlData, "text/xml");

var targetNode = document.createElement("div");
targetNode.setAttribute("id", "myGrid");
document.body.appendChild(targetNode);

test("Test table functions.", 2, function ()
{
  try
  {
    new cadc.vot.Builder(30000, 
                         {
                           xmlDOM: xmlDOM
                         },
                         function (voTableBuilder)
                         {
                           voTableBuilder.build(voTableBuilder.buildRowData);

                           var voTable = voTableBuilder.getVOTable();
                           var resources = voTable.getResources();

                           for (var r in resources)
                           {
                             var tables = resources[r].getTables();
                             for (var t in tables)
                             {
                               var tableData = tables[t].getTableData();
                               equal(tableData.getLongestValues()["Command"],
                                     3, "Longest value for Command should be 3");
                               equal(tableData.getLongestValues()["VM Type"],
                                     10, "Longest value for VM Type should be 10");
                             }
                           }
                         },
                         function ()
                         {

                         });
  }
  catch (error)
  {
    console.log(error.stack);
  }
});

test("Field insertions to Metadata.", 4, function()
{
  var testSubject = new cadc.vot.Metadata(null, null, null, null, null, null);

  var f1 = new cadc.vot.Field("F1", "F1", "UCD1", "UTYPE1", "UNIT1",
                              null, null, null, null, "F1");
  var f2 = new cadc.vot.Field("F2", "F2", "UCD2", "UTYPE2", "UNIT2",
                              null, null, null, null, "F2");
  var f3 = new cadc.vot.Field("F3", "F3", "UCD3", "UTYPE3", "UNIT3",
                              null, null, null, null, "F3");

  testSubject.insertField(3, f1);
  testSubject.insertField(13, f2);
  testSubject.insertField(9, f3);

  var fr1 = testSubject.getFields()[3];
  equal(fr1.getID(), "F1", "Field should be F1 at index 3.");

  var frNull = testSubject.getFields()[0];
  equal(frNull, null, "Field frNull should be null at 0.");

  var fr2 = testSubject.getFields()[13];
  equal(fr2.getID(), "F2", "Field should be F1 at index 13.");

  var fr3 = testSubject.getFields()[9];
  equal(fr3.getID(), "F3", "Field should be F1 at index 9.");
});
