var xmlData =
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<VOTABLE xmlns=\"http://www.ivoa.net/xml/VOTable/v1.2\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" version=\"1.2\">\n"
        + "  <RESOURCE>\n"
        + "    <TABLE>\n"
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
        + "      <FIELD name=\"DEC\" />\n"
        + "      <FIELD name=\"Calibration Level\" datatype=\"int\" />\n"
        + "      <DATA>\n"
        + "        <TABLEDATA>\n"
        + "          <TR>\n"
        + "            <TD>735.0</TD>\n"
        + "            <TD>2011.03.66.8.S</TD>\n"
        + "            <TD>m</TD>\n"
        + "            <TD />\n"
        + "            <TD>Idle</TD>\n"
        + "            <TD>sleep</TD>\n"
        + "            <TD>Tomcat</TD>\n"
        + "            <TD>1</TD>\n"
        + "            <TD>3072</TD>\n"
        + "            <TD>0</TD>\n"
        + "            <TD>41.0</TD>\n"
        + "            <TD>1</TD>\n"
        + "          </TR>\n"
        + "          <TR>\n"
        + "            <TD>734.0</TD>\n"
        + "            <TD>2011.03.66.9.S</TD>\n"
        + "            <TD>hello</TD>\n"
        + "            <TD />\n"
        + "            <TD>Idle</TD>\n"
        + "            <TD>sleep</TD>\n"
        + "            <TD>Tomcat</TD>\n"
        + "            <TD>1</TD>\n"
        + "            <TD>3072</TD>\n"
        + "            <TD>0</TD>\n"
        + "            <TD>47.1</TD>\n"
        + "            <TD>2</TD>\n"
        + "          </TR>\n"
        + "          <TR>\n"
        + "            <TD>733.0</TD>\n"
        + "            <TD>2011.03.66.10.N</TD>\n"
        + "            <TD>there</TD>\n"
        + "            <TD />\n"
        + "            <TD>Idle</TD>\n"
        + "            <TD>sleep</TD>\n"
        + "            <TD>Tomcat</TD>\n"
        + "            <TD>1</TD>\n"
        + "            <TD>3072</TD>\n"
        + "            <TD>0</TD>\n"
        + "            <TD>9.76</TD>\n"
        + "            <TD>3</TD>\n"
        + "          </TR>\n"
        + "          <TR>\n"
        + "            <TD>733.0</TD>\n"
        + "            <TD>2011.03.66.10.N</TD>\n"
        + "            <TD>there</TD>\n"
        + "            <TD />\n"
        + "            <TD>Idle</TD>\n"
        + "            <TD>sleep</TD>\n"
        + "            <TD>Tomcat</TD>\n"
        + "            <TD>1</TD>\n"
        + "            <TD>3072</TD>\n"
        + "            <TD>0</TD>\n"
        + "            <TD>-3.59</TD>\n"
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

// Create the options for the Grid.
var options = {
  editable: false,
  enableAddRow: false,
  showHeaderRow: true,
  enableCellNavigation: true,
  asyncEditorLoading: true,
  forceFitColumns: true,
  explicitInitialization: true,
  topPanelHeight: 45,
  headerRowHeight: 45,
  showTopPanel: false,
  sortColumn: "Job ID",
  sortDir: "asc",
  columnOptions: {
    "User": {
      cssClass: "user_column"
    },
    "Started on": {
      cssClass: "started_on_column"
    }
  }
};

test("Numeric filter 30000 times.", 0, function ()
{
  var viewer = new cadc.vot.Viewer("#myGrid", options);

  var start = new Date();
  var mockUserFilterValue = "< 3";
  for (var i = 0; i < 30000; i++)
  {
    viewer.valueFilters(mockUserFilterValue,
                        Math.floor((Math.random() * 10) + 1));
  }

  var end = new Date();

  console.log("Took " + (end.getTime() - start.getTime()) / 1000 + " seconds.");
});
