//
// Copyright 2002-2011 - Universite de Strasbourg / Centre National de la
// Recherche Scientifique
// ------
//
// Astrores SAX Engine
//
// Author:  Andre Schaaff
// Address: Centre de Donnees astronomiques de Strasbourg
//          11 rue de l'Universite
//          67000 STRASBOURG
//          FRANCE
// Email:   question@simbad.u-strasbg.fr
//
// -------
//
// In accordance with the international conventions about intellectual
// property rights this software and associated documentation files
// (the "Software") is protected. The rightholder authorizes :
// the reproduction and representation as a private copy or for educational
// and research purposes outside any lucrative use,
// subject to the following conditions:
//
// The above copyright notice shall be included.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
// OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON INFRINGEMENT,
// LOSS OF DATA, LOSS OF PROFIT, LOSS OF BARGAIN OR IMPOSSIBILITY
// TO USE SUCH SOFWARE. IN NO EVENT SHALL THE RIGHTHOLDER BE LIABLE
// FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
// TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH
// THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
//
// For any other exploitation contact the rightholder.
//
//                        -----------
//
// Conformement aux conventions internationales relatives aux droits de
// propriete intellectuelle ce logiciel et sa documentation sont proteges.
// Le titulaire des droits autorise :
// la reproduction et la representation a titre de copie privee ou des fins
// d'enseignement et de recherche et en dehors de toute utilisation lucrative.
// Cette autorisation est faite sous les conditions suivantes :
//
// La mention du copyright portee ci-dessus devra etre clairement indiquee.
//
// LE LOGICIEL EST LIVRE "EN L'ETAT", SANS GARANTIE D'AUCUNE SORTE.
// LE TITULAIRE DES DROITS NE SAURAIT, EN AUCUN CAS ETRE TENU CONTRACTUELLEMENT
// OU DELICTUELLEMENT POUR RESPONSABLE DES DOMMAGES DIRECTS OU INDIRECTS
// (Y COMPRIS ET A TITRE PUREMENT ILLUSTRATIF ET NON LIMITATIF,
// LA PRIVATION DE JOUISSANCE DU LOGICIEL, LA PERTE DE DONNEES,
// LE MANQUE A GAGNER OU AUGMENTATION DE COUTS ET DEPENSES, LES PERTES
// D'EXPLOITATION,LES PERTES DE MARCHES OU TOUTES ACTIONS EN CONTREFACON)
// POUVANT RESULTER DE L'UTILISATION, DE LA MAUVAISE UTILISATION
// OU DE L'IMPOSSIBILITE D'UTILISER LE LOGICIEL, ALORS MEME
// QU'IL AURAIT ETE AVISE DE LA POSSIBILITE DE SURVENANCE DE TELS DOMMAGES.
//
// Pour toute autre utilisation contactez le titulaire des droits.

package cds.astrores.sax;

// java
import java.io.*;
import java.net.URL;
import java.util.Vector;

import org.kxml2.io.*;
import org.xmlpull.v1.*; // VOTable internal data model
import cds.savot.model.*;

import cds.table.TableModel;

/**
 * Savot SAX Engine, it has been tested with kXML Pull parser implementation
 * </p>
 * 
 * @author Andre Schaaff
 * @version 3.0 (kickoff 31 May 02)
 */
public class AstroresSAXEngine {

    // markups
    private static String ASTRO = "ASTRO";
    private static String TABLE = "TABLE";
    private static String FIELD = "FIELD";
    private static String TABLEDATA = "TABLEDATA";
    private static String DESCRIPTION = "DESCRIPTION";
    private static String DATA = "DATA";
    private static String RESOURCE = "RESOURCE";
    private static String DEFINITIONS = "DEFINITIONS";
    private static String LINK = "LINK";
    private static String INFO = "INFO";
    private static String ROW = "ROW";
    private static String CELL = "CELL";
    private static String COOSYS = "COOSYS";
    private static String SYSTEM = "SYSTEM";
    private static String OPTION = "OPTION";
    private static String VALUES = "VALUES";
    private static String CSV = "CSV";

    // attributes
    private static String ARRAYSIZE = "arraysize";
    private static String DATATYPE = "datatype";
    private static String EPOCH = "epoch";
    private static String EQUINOX = "equinox";
    private static String INCLUSIVE = "inclusive";
    private static String MAX = "max";
    private static String MIN = "min";
    private static String PRECISION = "precision";
    private static String REF = "ref";
    private static String TYPE = "type";
    private static String UTYPE = "utype"; /* new 1.1 */
    private static String UCD = "ucd";
    private static String UNIT = "unit";
    private static String VALUE = "value";
    private static String WIDTH = "width";
    private static String ID = "ID";
    private static String CONTENTROLE = "content-role";
    private static String CONTENTTYPE = "content-type";
    private static String HREF = "href";
    private static String GREF = "gref";
    private static String ACTION = "action";
    private static String VERSION = "version";
    private static String INVALID = "invalid";

    // element or attribute
    private static String NAME = "NAME";
    private static String TITLE = "TITLE";
    private static String NULL = "NULL";

    // data model global objects
    private Vector allTables = new Vector(); // contains all tables

    // used for statistics
    private long rowCounter = 0;
    private long resourceCounter = 0;
    private long tableCounter = 0;
    private long dataCounter = 0;
    private boolean trace = false;

    TableModel tb = new TableModel();

    // needed for sequential parsing
    protected XmlPullParser parser = null;

    // Astrores SAX consumer
    protected AstroresSAXConsumer consumer;

    /**
     * Constructor
     * 
     * @param parser
     * @param file
     *            a file to parse
     */
    public AstroresSAXEngine(AstroresSAXConsumer consumer,
	    XmlPullParser parser, String file, boolean debug) {

	try {
	    this.parser = parser;
	    this.consumer = consumer;

	    // set the input of the parser
	    FileInputStream inStream = new FileInputStream(new File(file));
	    BufferedInputStream dataBuffInStream = new BufferedInputStream(
		    inStream);

	    parser.setInput(dataBuffInStream, "UTF-8");

	    parse(parser);

	} catch (IOException e) {
	    System.err.println("AstroresSAXEngine : " + e);
	} catch (Exception f) {
	    System.err.println("AstroresSAXEngine : " + f);
	}
    }

    /**
     * Constructor
     * 
     * @param parser
     * @param url
     *            url to parse
     * @param enc
     *            encoding (example : UTF-8)
     */
    public AstroresSAXEngine(AstroresSAXConsumer consumer,
	    XmlPullParser parser, URL url, String enc, boolean debug) {

	try {
	    this.parser = parser;
	    this.consumer = consumer;

	    // set the input of the parser (with the given encoding)
	    parser.setInput(new DataInputStream(url.openStream()), enc);

	    parse(parser);

	} catch (IOException e) {
	    System.err.println("AstroresSAXEngine : " + e);
	} catch (Exception f) {
	    System.err.println("AstroresSAXEngine : " + f);
	}
    }

    /**
     * Constructor
     * 
     * @param parser
     * @param instream
     *            stream to parse
     * @param enc
     *            encoding (example : UTF-8)
     */
    public AstroresSAXEngine(AstroresSAXConsumer consumer,
	    XmlPullParser parser, InputStream instream, String enc,
	    boolean debug) {
	try {
	    this.parser = parser;
	    this.consumer = consumer;

	    // DataInputStream dataInStream = new DataInputStream(instream);
	    BufferedInputStream dataBuffInStream = new BufferedInputStream(
		    instream);

	    // set the input of the parser (with the given encoding)
	    parser.setInput(dataBuffInStream, enc);

	    parse(parser);

	} catch (IOException e) {
	    System.err.println("AstroresSAXEngine : " + e);
	} catch (Exception f) {
	    System.err.println("AstroresSAXEngine : " + f);
	}
    }

    /**
     * 
     * @param buffer
     *            String
     */
    @SuppressWarnings("unchecked")
    private void CSVCut(String buffer) {
	// char[] ca = buffer.toCharArray();
	int begin = 0;
	int linecount = 0;
	int headlines = 3;
	char colsep = 9; // TAB
	char recsep = 10; // TAB

	System.err.println("Entrée CSVCut");

	for (int i = 0; i < buffer.length(); i++) {

	    if (buffer.charAt(i) == recsep) {
		linecount++;

		Vector row = new Vector();

		if (linecount < headlines)
		    System.err.println("L" + linecount + " ---> "
			    + buffer.substring(begin, i));
		int begin2 = begin;
		for (int j = begin; j <= i; j++) {

		    if (buffer.charAt(j) == colsep
			    || buffer.charAt(j) == recsep) {
			if (linecount < headlines) {
			    System.err.println("colsep-->");
			    System.err.println("---> "
				    + buffer.substring(begin2, j));
			} else { // line storage
			    // add value to the current row
			    row.addElement(buffer.substring(begin2, j));
			}
			begin2 = j + 1;
		    }
		}
		begin = i + 1;
		tb.addRow(row);
	    }

	    /*
	     * if (buffer.charAt(i) == 10) { System.err.println("---> " +
	     * buffer.substring(debut, i)); StringTokenizer sb = new
	     * StringTokenizer(buffer.substring(debut, i)); while
	     * (sb.hasMoreTokens()) System.err.println(sb.nextToken());
	     * 
	     * debut = i + 1; }
	     */
	}
	System.err.println("row count : " + tb.getRowCount());
	/*
	 * StringTokenizer sb = new StringTokenizer(buffer);
	 * System.err.println("Token count : " + sb.countTokens()); while
	 * (sb.hasMoreTokens()) System.err.println(sb.nextToken());
	 */
    }

    /**
     * Parsing engine
     * 
     * @param parser
     *            an XML pull parser (example : kXML)
     * @throws IOException
     * 
     */
    @SuppressWarnings({ "deprecation", "unchecked" })
    public void parse(XmlPullParser parser) throws IOException {

	String name = new String();
	Vector father = new Vector();
	String currentMarkup = "XML";

	Vector currentRow = new Vector();
	@SuppressWarnings("unused")
	String description = null; // global description
	@SuppressWarnings("unused")
	String id = null; // current resource id
	@SuppressWarnings("unused")
	String type = null; // current resource type

	Vector fieldPropertyNames = new Vector();
	Vector fieldPropertyValues = new Vector();

	@SuppressWarnings("unused")
	SavotData currentData = new SavotData();
	SavotValues currentValues = new SavotValues();
	// SavotTableData currentTableData = new SavotTableData();
	String currentDescription = new String();
	SavotLink currentLink = new SavotLink();
	SavotInfo currentInfo = new SavotInfo();
	SavotOption currentOption = new SavotOption();
	SavotCoosys currentCoosys = new SavotCoosys();
	SavotDefinitions currentDefinitions = new SavotDefinitions();

	try {

	    // envent type
	    int eventType = parser.getEventType();
	    int previousDepth = 0;

	    // while the end of the document is not reach
	    while (eventType != XmlPullParser.END_DOCUMENT) {
		// treatment depending on event type
		switch (eventType) {
		// if a start tag is reach
		case KXmlParser.START_TAG:
		    try {
			// the name of the current tag
			name = parser.getName();

			if (trace)
			    System.err.println("Name ---> " + parser.getName());

			if (name != null) {

			    // ASTRO
			    if (name.equalsIgnoreCase(ASTRO)) {
				if (trace)
				    System.err.println("VOTABLE begin");
				currentMarkup = ASTRO;
			    } else // DESCRIPTION
			    if (name.equalsIgnoreCase(DESCRIPTION)) {
				currentMarkup = DESCRIPTION;
				if (trace)
				    System.err.println("DESCRIPTION begin");
			    } // RESOURCE
			    else if (name.equalsIgnoreCase(RESOURCE)) {

				if (trace == true)
				    System.err.println("RESOURCE begin");
				currentMarkup = RESOURCE;

				// for statistics only
				resourceCounter++;

				// all resource properties init
				id = "";
				type = "";

				if (parser.getAttributeCount() != 0) {
				    if (parser.getAttributeValue(null, TYPE) != null)
					type = parser.getAttributeValue(null,
						TYPE);
				    if (parser.getAttributeValue(null, ID) != null) {
					id = parser.getAttributeValue(null, ID);
				    }
				}
			    }
			    // TABLE
			    else if (name.equalsIgnoreCase(TABLE)) {
				tb = new TableModel();
				currentMarkup = TABLE;

				// for statistics only
				tableCounter++;

				if (parser.getAttributeCount() != 0) {
				    if (parser.getAttributeValue(null, ID) != null) {
					tb.setId(parser.getAttributeValue(null,
						ID));
				    }
				}
			    } // FIELD
			    else if (name.equalsIgnoreCase(FIELD)) {
				fieldPropertyNames = new Vector();
				fieldPropertyValues = new Vector();
				currentMarkup = FIELD;
				if (parser.getAttributeCount() != 0) {
				    if (parser.getAttributeValue(null, UNIT) != null) {
					fieldPropertyNames.addElement(UNIT);
					fieldPropertyValues.addElement(parser
						.getAttributeValue(null, UNIT));
				    }
				    if (parser
					    .getAttributeValue(null, DATATYPE) != null) {
					fieldPropertyNames.addElement(DATATYPE);
					fieldPropertyValues.addElement(parser
						.getAttributeValue(null,
							DATATYPE));
				    }
				    if (parser.getAttributeValue(null,
					    PRECISION) != null) {
					fieldPropertyNames
						.addElement(PRECISION);
					fieldPropertyValues.addElement(parser
						.getAttributeValue(null,
							PRECISION));
				    }
				    if (parser.getAttributeValue(null, WIDTH) != null) {
					fieldPropertyNames.addElement(WIDTH);
					fieldPropertyValues
						.addElement(parser
							.getAttributeValue(
								null, WIDTH));
				    }
				    if (parser.getAttributeValue(null, REF) != null) {
					fieldPropertyNames.addElement(REF);
					fieldPropertyValues.addElement(parser
						.getAttributeValue(null, REF));
				    }
				    if (parser.getAttributeValue(null, NAME) != null) {
					fieldPropertyNames.addElement(NAME);
					fieldPropertyValues.addElement(parser
						.getAttributeValue(null, NAME));
				    }
				    if (parser.getAttributeValue(null, UCD) != null) {
					fieldPropertyNames.addElement(UCD);
					fieldPropertyValues.addElement(parser
						.getAttributeValue(null, UCD));
				    }
				    if (parser.getAttributeValue(null,
					    ARRAYSIZE) != null) {
					fieldPropertyNames
						.addElement(ARRAYSIZE);
					fieldPropertyValues.addElement(parser
						.getAttributeValue(null,
							ARRAYSIZE));
				    }
				    if (parser.getAttributeValue(null, TYPE) != null) {
					fieldPropertyNames.addElement(TYPE);
					fieldPropertyValues.addElement(parser
						.getAttributeValue(null, TYPE));
				    }
				    if (parser.getAttributeValue(null, UTYPE) != null) {
					fieldPropertyNames.addElement(UTYPE);
					fieldPropertyValues
						.addElement(parser
							.getAttributeValue(
								null, UTYPE));
				    }
				    if (parser.getAttributeValue(null, ID) != null) {
					fieldPropertyNames.addElement(ID);
					fieldPropertyValues.addElement(parser
						.getAttributeValue(null, ID));
				    }
				}
			    } // VALUES
			    else if (name.equalsIgnoreCase(VALUES)) {
				currentValues = new SavotValues();
				currentMarkup = VALUES;
				if (parser.getAttributeCount() != 0) {
				    if (parser.getAttributeValue(null, TYPE) != null)
					currentValues.setType(parser
						.getAttributeValue(null, TYPE));
				    if (parser.getAttributeValue(null, NULL) != null)
					currentValues.setNull(parser
						.getAttributeValue(null, NULL));
				    if (parser.getAttributeValue(null, INVALID) != null) /*
											  * 1.0
											  * mais
											  * non
											  * 1.1
											  */
					currentValues.setInvalid(parser
						.getAttributeValue(null,
							INVALID));
				    if (parser.getAttributeValue(null, REF) != null) /*
										      * 1.0
										      * mais
										      * non
										      * 1.1
										      */
					currentValues.setRef(parser
						.getAttributeValue(null, REF));
				    if (parser.getAttributeValue(null, ID) != null) {
					currentValues.setId(parser
						.getAttributeValue(null, ID));
				    }
				}
			    } // ROW
			    else if (name.equalsIgnoreCase(ROW)) {
				if (trace)
				    System.err.println("ROW begin");
				currentMarkup = ROW;

				// create a new row
				currentRow = new Vector();
			    } // CELL
			    else if (name.equalsIgnoreCase(CELL)) {
				if (trace)
				    System.err.println("CELL begin");
				currentMarkup = CELL;

				// for statistics only
				dataCounter++;
			    } // DATA
			    else if (name.equalsIgnoreCase(DATA)) {
				currentData = new SavotData();
				currentMarkup = DATA;
			    } // TABLEDATA
			    else if (name.equalsIgnoreCase(TABLEDATA)) {
				// currentTableData = new SavotTableData();
				currentMarkup = TABLEDATA;
			    } // LINK
			    else if (name.equalsIgnoreCase(LINK)) {
				currentLink = new SavotLink();
				currentMarkup = LINK;

				if (parser.getAttributeCount() != 0) {
				    if (parser.getAttributeValue(null,
					    CONTENTROLE) != null)
					currentLink.setContentRole(parser
						.getAttributeValue(null,
							CONTENTROLE));
				    if (parser.getAttributeValue(null,
					    CONTENTTYPE) != null)
					currentLink.setContentType(parser
						.getAttributeValue(null,
							CONTENTTYPE));
				    if (parser.getAttributeValue(null, TITLE) != null)
					currentLink
						.setTitle(parser
							.getAttributeValue(
								null, TITLE));
				    if (parser.getAttributeValue(null, VALUE) != null)
					currentLink
						.setValue(parser
							.getAttributeValue(
								null, VALUE));
				    if (parser.getAttributeValue(null, HREF) != null)
					currentLink.setHref(parser
						.getAttributeValue(null, HREF));
				    if (parser.getAttributeValue(null, GREF) != null)
					currentLink.setGref(parser
						.getAttributeValue(null, GREF));
				    if (parser.getAttributeValue(null, ACTION) != null)
					currentLink
						.setAction(parser
							.getAttributeValue(
								null, ACTION));
				    if (parser.getAttributeValue(null, ID) != null) {
					currentLink.setID(parser
						.getAttributeValue(null, ID));
				    }
				}
				if (trace)
				    System.err.println("LINK");
			    }
			    // INFO
			    else if (name.equalsIgnoreCase(INFO)) {
				currentInfo = new SavotInfo();
				currentMarkup = INFO;
				if (parser.getAttributeCount() != 0) {
				    if (parser.getAttributeValue(null, NAME) != null)
					currentInfo.setName(parser
						.getAttributeValue(null, NAME));
				    if (parser.getAttributeValue(null, VALUE) != null)
					currentInfo
						.setValue(parser
							.getAttributeValue(
								null, VALUE));
				    if (parser.getAttributeValue(null, ID) != null) {
					currentInfo.setId(parser
						.getAttributeValue(null, ID));
				    }
				}
				if (trace)
				    System.err.println("INFO");
			    } // OPTION
			    else if (name.equalsIgnoreCase(OPTION)) {
				currentMarkup = OPTION;
				if (trace)
				    System.err.println("OPTION");
				currentOption = new SavotOption();
				if (parser.getAttributeCount() != 0) {
				    if (parser.getAttributeValue(null, NAME) != null)
					currentOption.setName(parser
						.getAttributeValue(null, NAME));
				    if (parser.getAttributeValue(null, VALUE) != null)
					currentOption
						.setValue(parser
							.getAttributeValue(
								null, VALUE));
				}
			    } else if (name.equalsIgnoreCase(COOSYS)) {
				currentMarkup = COOSYS;
				if (trace)
				    System.err.println("COOSYS");
				currentCoosys = new SavotCoosys();
				if (parser.getAttributeCount() != 0) {
				    if (parser.getAttributeValue(null, EQUINOX) != null)
					currentCoosys.setEquinox(parser
						.getAttributeValue(null,
							EQUINOX));
				    if (parser.getAttributeValue(null, EPOCH) != null)
					currentCoosys
						.setEpoch(parser
							.getAttributeValue(
								null, EPOCH));
				    if (parser.getAttributeValue(null, SYSTEM) != null)
					currentCoosys
						.setSystem(parser
							.getAttributeValue(
								null, SYSTEM));
				    if (parser.getAttributeValue(null, ID) != null) {
					currentCoosys.setId(parser
						.getAttributeValue(null, ID));
				    }
				}
			    }
			    // DEFINITIONS
			    else if (name.equalsIgnoreCase(DEFINITIONS)) {
				currentMarkup = DEFINITIONS;
				currentDefinitions = new SavotDefinitions();
				if (trace)
				    System.err.println("DEFINITIONS");
			    }
			}
			currentMarkup = name;
		    } catch (Exception e) {
			System.err.println("START_TAG : " + e);
		    }
		    break;

		// if an end tag is reach
		case KXmlParser.END_TAG:
		    name = parser.getName();
		    try {

			if (trace)
			    System.err.println("End ---> " + name);

			// DESCRIPTION - several fathers are possible
			if (name.equalsIgnoreCase(DESCRIPTION)) {
			    if (((String) father.elementAt(father.size() - 2))
				    .equalsIgnoreCase(ASTRO)) {
				description = currentDescription;
				currentMarkup = "";
			    } else if (((String) father
				    .elementAt(father.size() - 2))
				    .equalsIgnoreCase(RESOURCE)) {
				description = currentDescription;
				currentMarkup = "";
			    } else if (((String) father
				    .elementAt(father.size() - 2))
				    .equalsIgnoreCase(TABLE)) {
				tb.setDescription(currentDescription);
				currentMarkup = "";
			    } else if (((String) father
				    .elementAt(father.size() - 2))
				    .equalsIgnoreCase(FIELD)) {
				fieldPropertyNames.addElement(DESCRIPTION);
				fieldPropertyValues
					.addElement(currentDescription);
				currentMarkup = "";
			    }
			} // TABLE
			else if (name.equalsIgnoreCase(TABLE)) {
			    allTables.addElement(tb);
			    currentMarkup = "";

			    if (trace)
				System.err.println(tb.getName());
			} // FIELD - several fathers are possible

			else if (name.equalsIgnoreCase(FIELD)) {
			    if (((String) father.elementAt(father.size() - 2))
				    .equalsIgnoreCase(TABLE)) {
				tb.setTableProperties(fieldPropertyNames,
					fieldPropertyValues);
				if (trace)
				    System.err
					    .println("FIELD from TABLE father = "
						    + father);
			    }
			} // TR
			else if (name.equalsIgnoreCase(ROW)) {
			    if (trace)
				System.err.println("TR end");
			    currentMarkup = "";

			    // add the row to the table
			    tb.addRow((String[]) currentRow.toArray());

			    // for statistics only
			    rowCounter++;

			    if (trace)
				System.err.println("ADD row");

			} // DATA
			else if (name.equalsIgnoreCase(DATA)) {
			    currentMarkup = "";
			    // currentTable.setData(currentData);
			} // CELL
			else if (name.equalsIgnoreCase(CELL)) {
			    currentMarkup = "";
			    if (trace)
				System.err.println("CELL end");
			} // RESOURCE
			else if (name.equalsIgnoreCase(RESOURCE)) {
			    if (trace)
				System.err.println("RESOURCE end");
			    currentMarkup = "";
			} // OPTION
			else if (name.equalsIgnoreCase(OPTION)) {
			    if (trace)
				System.err.println("OPTION end");
			    currentMarkup = "";
			} // TABLEDATA
			else if (name.equalsIgnoreCase(TABLEDATA)) {
			    currentMarkup = "";
			    // currentData.setTableData(currentTableData);
			    if (trace)
				System.err.println(tb.getName());
			}
			// COOSYS
			else if (name.equalsIgnoreCase(COOSYS)) {
			    currentMarkup = "";
			    // COOSYS - several fathers are possible
			    if (((String) father.elementAt(father.size() - 2))
				    .equalsIgnoreCase(DEFINITIONS)) {
				currentDefinitions.getCoosys().addItem(
					currentCoosys);
				if (trace)
				    System.err
					    .println("COOSYS from DEFINITIONS father = "
						    + father);
			    } else if (((String) father
				    .elementAt(father.size() - 2))
				    .equalsIgnoreCase(RESOURCE)) {
				// currentResource.getCoosys().addItem(currentCoosys);
				if (trace)
				    System.err
					    .println("COOSYS from RESOURCE father = "
						    + father);
			    }
			} // LINK
			else if (name.equalsIgnoreCase(LINK)) {
			    currentMarkup = "";
			    // LINK - several fathers are possible
			    if (((String) father.elementAt(father.size() - 2))
				    .equalsIgnoreCase(RESOURCE)) {
				// currentResource.getLinks().addItem(currentLink);
				if (trace)
				    System.err
					    .println("LINK from RESOURCE father = "
						    + father);
			    } else if (((String) father
				    .elementAt(father.size() - 2))
				    .equalsIgnoreCase(TABLE)) {
				// currentTable.getLinks().addItem(currentLink);
				if (trace)
				    System.err
					    .println("LINK from TABLE father = "
						    + father);
			    } else if (((String) father
				    .elementAt(father.size() - 2))
				    .equalsIgnoreCase(FIELD)) {
				// currentField.getLinks().addItem(currentLink);
				if (trace)
				    System.err
					    .println("LINK from FIELD father = "
						    + father);
			    }
			} // VALUES
			else if (name.equalsIgnoreCase(VALUES)) {
			    currentMarkup = "";
			    // VALUES - several fathers are possible
			    if (((String) father.elementAt(father.size() - 2))
				    .equalsIgnoreCase(FIELD)) {
				// currentField.setValues(currentValues);
				if (trace)
				    System.err
					    .println("VALUES from FIELD father = "
						    + father
						    + " ID : "
						    + currentValues.getId());
			    }
			} // INFO
			else if (name.equalsIgnoreCase(INFO)) {
			    currentMarkup = "";
			    if (trace)
				System.err.println("INFO father = " + father);
			    // INFO - several fathers are possible
			    if (((String) father.elementAt(father.size() - 2))
				    .equalsIgnoreCase(ASTRO)) {
				if (trace)
				    System.err
					    .println("INFO from VOTABLE father = "
						    + father);
			    } else if (((String) father
				    .elementAt(father.size() - 2))
				    .equalsIgnoreCase(RESOURCE)) {
				if (trace)
				    System.err
					    .println("INFO from RESOURCE father = "
						    + father);
			    }
			}
			// DEFINITIONS
			else if (name.equalsIgnoreCase(DEFINITIONS)) {
			    currentMarkup = "";
			    if (trace)
				System.err.println("DEFINITIONS");
			    // allResources.setDefinitions(currentDefinitions);
			}
		    } catch (Exception e) {
			System.err.println("FATHER : " + father + " END_TAG ("
				+ name + ") : " + e);
		    }
		    break;

		case KXmlParser.END_DOCUMENT:
		    try {
			if (trace)
			    System.err.println("Document end reached!");
		    } catch (Exception e) {
			System.err.println("END_DOCUMENT : " + e);
		    }
		    break;

		case KXmlParser.TEXT:
		    try {
			if (currentMarkup.equalsIgnoreCase(CSV)) {
			    // System.err.println((parser.getText()).trim());
			    CSVCut((parser.getText()).trim());
			    if (trace)
				System.err.println((parser.getText()).trim());

			    // currentRow.addElement(parser.getText());
			} else
			// add a data to the current row
			if (currentMarkup.equalsIgnoreCase(CELL)) {
			    if (trace)
				System.err.println("CELL : "
					+ (parser.getText()).trim());
			    currentRow.addElement(parser.getText());
			} else if (currentMarkup.equalsIgnoreCase(DESCRIPTION)) {
			    if (trace)
				System.err.println("DESCRIPTION : "
					+ (parser.getText()).trim());
			    currentDescription = (parser.getText()).trim();
			} else if (currentMarkup.equalsIgnoreCase(COOSYS)) {
			    if (trace)
				System.err.println("COOSYS : "
					+ (parser.getText()).trim());
			    currentCoosys.setContent((parser.getText()).trim());
			} else if (currentMarkup.equalsIgnoreCase(LINK)) {
			    if (trace)
				System.err.println("LINK : "
					+ (parser.getText()).trim());
			    currentLink.setContent((parser.getText()).trim());
			} else if (currentMarkup.equalsIgnoreCase(OPTION)) {
			    if (trace)
				System.err.println("OPTION : "
					+ (parser.getText()).trim());
			} else if (currentMarkup.equalsIgnoreCase(INFO)) {
			    currentInfo.setContent((parser.getText()).trim());
			    if (trace)
				System.err.println("INFO : "
					+ (parser.getText()).trim());
			}
		    } catch (Exception e) {
			System.err.println("TEXT : " + e);
		    }
		    break;

		case KXmlParser.START_DOCUMENT:
		    break;

		default:
		    if (trace)
			System.err
				.println(" ignoring some other (legacy) event at line : "
					+ parser.getLineNumber());
		}

		// save previous values
		previousDepth = parser.getDepth();

		// new values
		eventType = parser.next();
		if (parser.getDepth() > previousDepth) {
		    father.addElement((parser.getName()));
		} else if (parser.getDepth() == previousDepth) {
		    if (((String) father.lastElement()).equals((parser
			    .getName()))) {
			father.removeElementAt(father.size() - 1);
			father.addElement((parser.getName()));
		    }
		} else {
		    father.removeElementAt(father.size() - 1);
		}
		if (trace)
		    System.err.println("father = " + father);
	    }

	} catch (Exception f) {
	    if (trace)
		System.err.println("parse : " + f);
	}
    }
}
