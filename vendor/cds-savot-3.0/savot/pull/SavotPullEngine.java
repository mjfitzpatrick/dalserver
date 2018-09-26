//
// Copyright 2002-2011 - Universite de Strasbourg / Centre National de la
// Recherche Scientifique
// ------
//
// SAVOT Pull Engine
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

package cds.savot.pull;

// java
import java.io.*;
import java.net.URL;
import java.util.Vector;
import java.util.Hashtable;

// kXML packages
import org.kxml2.io.*;
import org.xmlpull.v1.*;

// VOTable internal data model
import cds.savot.model.*;

/**
 * <p>
 * It has been tested with kXML Pull parser implementation
 * </p>
 * <p>
 * but it is possible to use other pull parsers
 * </p>
 * <p>
 * Designed to use with Pull parsers complient with Standard Pull Implementation
 * v1
 * </p>
 * 
 * @author Andre Schaaff
 * @version 3.0 (kickoff 31 May 02)
 */
public class SavotPullEngine implements cds.savot.common.Markups {

    // parsing mode
    public static int FULL = 0; // deprecated and replaced by FULLREAD
    public static int FULLREAD = 0; // all in memory
    public static int SEQUENTIAL = 1; // deprecated and replaced by RESOURCE
    public static int RESOURCEREAD = 1; // resource per resource reading
    public static int ROWREAD = 2; // row per row reading

    // data model objects
    private SavotVOTable currentVOTable = new SavotVOTable();
    private SavotResource currentResource = new SavotResource(); // RESOURCEREAD
								 // mode only
    private SavotTR currentTR = new SavotTR(); // ROWREAD mode only

    // used for statistics
    private long rowCounter = 0;
    private long resourceCounter = 0;
    private long tableCounter = 0;
    private long dataCounter = 0;
    private boolean trace = false;

    // used for recursive management
    Vector<String> father = new Vector<String>();

    // used for recursive resources, LIFO mode
    private Vector<SavotResource> resourcestack = new Vector<SavotResource>();

    // used for recursive options, LIFO mode
    private Vector<SavotOption> optionstack = new Vector<SavotOption>();

    // used for recursives groups, LIFO mode
    private Vector<SavotGroup> groupstack = new Vector<SavotGroup>();

    // anciennement dans parse

    String name = new String();
    String currentMarkup = "XML";

    // used for RESOURCEREAD parsing
    boolean resourceComplete = false;

    // used for ROWREAD parsing
    boolean TRComplete = false;

    // for multi level resource
    int includedResource = 0;
    
    // for multi level option
    int includedOption = 0;
    
    // for multi level group
    int includedGroup = 0;

    SavotTable currentTable = null;
    SavotField currentField = null;
    SavotFieldRef currentFieldRef = null;
    SavotGroup currentGroup = null; /* new in VOTable 1.1 */
    SavotParam currentParam = null;
    SavotParamRef currentParamRef = null;
    SavotData currentData = null;
    SavotValues currentValues = null;
    SavotTableData currentTableData = null;
    String currentDescription = null;
    SavotLink currentLink = null;
    SavotInfo currentInfo = null;
    SavotMin currentMin = null;
    SavotMax currentMax = null;
    SavotOption currentOption = null;
    @SuppressWarnings("deprecation")
    SavotCoosys currentCoosys = null;
    @SuppressWarnings("deprecation")
    SavotDefinitions currentDefinitions = null;
    SavotBinary currentBinary = null;
    SavotFits currentFits = null;
    SavotStream currentStream = null;
    SavotTD currentTD = null;

    SavotStatistics stats;

    /**
     * Hashtable containing object references which have an ID So it is possible
     * to retrieve such object reference Used to resolve ID ref
     */
    @SuppressWarnings("unchecked")
    public Hashtable idRefLinks = new Hashtable();

    // needed for sequential parsing
    protected XmlPullParser parser = null;

    /**
     * Constructor
     * 
     * @param parser
     * @param file
     *            a file to parse
     * @param mode
     *            FULLREAD (all in memory), RESOURCEREAD (per RESOURCE) or
     *            ROWREAD (per ROW, for small memory size applications)
     */
    public SavotPullEngine(XmlPullParser parser, String file, int mode,
	    boolean debug, SavotStatistics stats) {

	try {
	    this.parser = parser;
	    this.stats = stats;

	    enableDebug(debug);

	    // set the input of the parser
	    FileInputStream inStream = new FileInputStream(new File(file));
	    BufferedInputStream dataBuffInStream = new BufferedInputStream(
		    inStream);

	    parser.setInput(dataBuffInStream, "UTF-8");

	    // parser the stream in the given mode
	    if (mode == SavotPullEngine.FULLREAD)
		parse(parser, mode);

	} catch (IOException e) {
	    System.err.println("Exception SavotPullEngine : " + e);
	} catch (Exception f) {
	    System.err.println("Exception SavotPullEngine : " + f);
	}
    }

    /**
     * Constructor
     * 
     * @param parser
     * @param url
     *            url to parse
     * @param mode
     *            FULLREAD (all in memory), RESOURCEREAD (per RESOURCE) or
     *            ROWREAD (per ROW, for small memory size applications)
     * @param enc
     *            encoding (example : UTF-8)
     */
    public SavotPullEngine(XmlPullParser parser, URL url, int mode, String enc,
	    boolean debug, SavotStatistics stats) {

	try {
	    this.parser = parser;
	    this.stats = stats;

	    enableDebug(debug);
	    // set the input of the parser (with the given encoding)
	    parser.setInput(new DataInputStream(url.openStream()), enc);

	    // parser the stream in the given mode
	    if (mode == SavotPullEngine.FULLREAD)
		parse(parser, mode);

	} catch (IOException e) {
	    System.err.println("Exception SavotPullEngine : " + e);
	} catch (Exception f) {
	    System.err.println("Exception SavotPullEngine : " + f);
	}
    }

    /**
     * Constructor
     * 
     * @param parser
     * @param instream
     *            stream to parse
     * @param mode
     *            FULL (all in memory), RESOURCEREAD (per RESOURCE) or ROWREAD
     *            (per TR for small memory size applications)
     * @param enc
     *            encoding (example : UTF-8)
     */
    public SavotPullEngine(XmlPullParser parser, InputStream instream,
	    int mode, String enc, boolean debug, SavotStatistics stats) {
	// public SavotPullEngine(XmlPullParser parser, InputStream instream,
	// int mode, String enc) {
	try {
	    this.parser = parser;
	    this.stats = stats;

	    enableDebug(debug);

	    // DataInputStream dataInStream = new DataInputStream(instream);
	    BufferedInputStream dataBuffInStream = new BufferedInputStream(
		    instream);

	    // set the input of the parser (with the given encoding)
	    // parser.setInput(new DataInputStream(instream), enc);
	    parser.setInput(dataBuffInStream, enc);

	    // parser the stream in the given mode
	    if (mode == SavotPullEngine.FULLREAD)
		parse(parser, mode);

	} catch (IOException e) {
	    System.err.println("Exception SavotPullEngine : " + e);
	} catch (Exception f) {
	    System.err.println("Exception SavotPullEngine : " + f);
	}
    }

    /**
     * Reset of the engine before another parsing
     */
    public void reset() {
	// data model global classes
	currentVOTable = new SavotVOTable();
	currentResource = new SavotResource();
	rowCounter = 0;
	resourceCounter = 0;
	tableCounter = 0;
	dataCounter = 0;
	idRefLinks.clear();
	// used for recursive resources, LIFO mode
	resourcestack.removeAllElements();
	// used for recursive options, LIFO mode
	optionstack.removeAllElements();
	// used for recursive groups, LIFO mode
	groupstack.removeAllElements();
    }

    /**
     * Put a resource on the resourcestack
     * 
     * @param res
     */
    private void putResourceStack(SavotResource res) {
	resourcestack.addElement(res);
    }

    /**
     * Put an option on the optionstack
     * 
     * @param res
     */
    private void putOptionStack(SavotOption res) {
	optionstack.addElement(res);
    }

    /**
     * Put a group on the groupstack
     * 
     * @param res
     */
    private void putGroupStack(SavotGroup res) {
	groupstack.addElement(res);
    }

    /**
     * Get the last element from the resourcestack
     * 
     * @return SavotResource
     */
    private SavotResource getResourceStack() {
	SavotResource res = (SavotResource) resourcestack.lastElement();
	resourcestack.removeElementAt(resourcestack.size() - 1);
	return res;
    }

    /**
     * Get the last element from the optionstack
     * 
     * @return SavotOption
     */
    private SavotOption getOptionStack() {
	SavotOption res = (SavotOption) optionstack.lastElement();
	optionstack.removeElementAt(optionstack.size() - 1);
	return res;
    }

    /**
     * Get the last element from the groupstack
     * 
     * @return SavotGroup
     */
    private SavotGroup getGroupStack() {
	SavotGroup res = (SavotGroup) groupstack.lastElement();
	groupstack.removeElementAt(groupstack.size() - 1);
	return res;
    }

    /**
     * Parsing engine
     * 
     * @param parser
     *            an XML pull parser (example : kXML)
     * @param parsingType
     *            FULLREAD (all in memory), RESOURCEREAD (per RESOURCE) or
     *            ROWREAD (per ROW, for small memory size applications)
     * @return SavotResource
     * @throws IOException
     * 
     */
    // public SavotResource parse (XmlPullParser parser, int parsingType) throws
    // IOException {
    @SuppressWarnings({ "deprecation", "unchecked" })
    public int parse(XmlPullParser parser, int parsingType) throws IOException {

	if (parsingType != ROWREAD && parsingType != RESOURCEREAD) {
	    name = new String();
	    currentMarkup = "XML";

	    // used for sequential parsing
	    resourceComplete = false;

	    // for multi level resource
	    includedResource = 0;
	    // for multi level option
	    includedOption = 0;
	    // for multi level group
	    includedGroup = 0;

	    currentTable = new SavotTable();
	    currentField = new SavotField();
	    currentFieldRef = new SavotFieldRef();
	    currentGroup = new SavotGroup(); /* new in VOTable 1.1 */
	    currentParam = new SavotParam();
	    currentParamRef = new SavotParamRef();
	    currentData = new SavotData();
	    currentValues = new SavotValues();
	    currentTableData = new SavotTableData();
	    currentDescription = new String();
	    currentLink = new SavotLink();
	    currentInfo = new SavotInfo();
	    currentMin = new SavotMin();
	    currentMax = new SavotMax();
	    currentOption = new SavotOption();
	    currentCoosys = new SavotCoosys();
	    currentDefinitions = new SavotDefinitions();
	    currentBinary = new SavotBinary();
	    currentFits = new SavotFits();
	    currentStream = new SavotStream();
	}
	TRComplete = false;
	currentTR = null;
	currentTD = new SavotTD();

	try {

	    // envent type
	    int eventType = parser.getEventType();
	    // System.out.println("ENTREE DANS PARSER");
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

			    // VOTABLE
			    if (name.equalsIgnoreCase(VOTABLE)) {
				int counter = parser.getAttributeCount();
				for (int i = 0; i < counter; i++) {
				    // partie revoir pour permettre la prise en compte de plusieurs namespaces
				    if (parser.getAttributeName(i)
					    .compareToIgnoreCase(VERSION) == 0)
					currentVOTable.setVersion(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(XMLNSXSI) == 0)
					currentVOTable.setXmlnsxsi(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(XSINOSCHEMA) == 0)
					currentVOTable.setXsinoschema(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(XSISCHEMA) == 0)
					currentVOTable.setXsischema(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(XMLNS) == 0)
					currentVOTable.setXmlns(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(ID) == 0) {
					currentVOTable.setId(parser
						.getAttributeValue(i));
					idRefLinks.put(parser
						.getAttributeValue(i),
						currentVOTable);
				    }
				}
				if (trace)
				    System.err.println("VOTABLE begin");
				currentMarkup = VOTABLE;
			    } else // DESCRIPTION
			    if (name.equalsIgnoreCase(DESCRIPTION)) {
				currentMarkup = DESCRIPTION;
				if (trace)
				    System.err.println("DESCRIPTION begin");
			    } //
			    else if (name.equalsIgnoreCase(RESOURCE)) {

				stats.iResourcesInc();

				if (includedResource > 0) {
				    // inner case (multi level resources)
				    putResourceStack(currentResource);
				    if (trace)
					System.err
						.println("RESOURCE - included");
				} else if (trace)
				    System.err
					    .println("RESOURCE - not included");
				includedResource++;

				if (trace == true)
				    System.err.println("RESOURCE begin");
				currentMarkup = RESOURCE;

				// for statistics only
				resourceCounter++;

				if (parsingType == FULL
					|| currentResource == null
					|| parsingType == ROWREAD)
				    currentResource = new SavotResource();
				else
				    currentResource.init();

				currentResource.setType(""); // correct the
							     // "results"
							     // default value
				int counter = parser.getAttributeCount();
				for (int i = 0; i < counter; i++) {
				    if (parser.getAttributeName(i)
					    .compareToIgnoreCase(NAME) == 0)
					currentResource.setName(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(TYPE) == 0)
					currentResource.setType(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(UTYPE) == 0)
					currentResource.setUtype(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(ID) == 0) {
					currentResource.setId(parser
						.getAttributeValue(i));
					if (parsingType == FULL)
					    idRefLinks.put(parser
						    .getAttributeValue(i),
						    currentResource);
				    }
				}
			    }
			    // TABLE
			    else if (name.equalsIgnoreCase(TABLE)) {
				stats.iTablesGlobalInc(); // ++ to the number of
							  // tables of the file
							  // or stream
				stats.iTablesLocalInc(); // ++ to the number of
							 // tables in the
							 // RESOURCE

				currentTable = new SavotTable();
				currentMarkup = TABLE;
				if (trace)
				    System.err
					    .println("on passe dans Name ---> "
						    + "TABLE");

				// for statistics only
				tableCounter++;

				int counter = parser.getAttributeCount();
				for (int i = 0; i < counter; i++) {
				    if (parser.getAttributeName(i)
					    .compareToIgnoreCase(NAME) == 0)
					currentTable.setName(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(UCD) == 0) // new
									    // since
									    // VOTable
									    // 1.1
					currentTable.setUcd(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(UTYPE) == 0) // new
									      // since
									      // VOTable
									      // 1.1
					currentTable.setUtype(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(REF) == 0)
					currentTable.setRef(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(NROWS) == 0) // new
									      // since
									      // VOTable
									      // 1.1
					currentTable.setNrows(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(ID) == 0) {
					currentTable.setId(parser
						.getAttributeValue(i));
					if (parsingType == FULL) {
					    idRefLinks.put(parser
						    .getAttributeValue(i),
						    currentTable);
					    if (trace)
						System.err.println(parser
							.getAttributeValue(i));
					}
				    }
				}
			    } // FIELD
			    else if (name.equalsIgnoreCase(FIELD)) {
				currentField = new SavotField();
				currentMarkup = FIELD;
				if (trace)
				    System.err
					    .println("on passe dans Name ---> "
						    + "FIELD");
				int counter = parser.getAttributeCount();
				for (int i = 0; i < counter; i++) {
				    if (parser.getAttributeName(i)
					    .compareToIgnoreCase(UNIT) == 0)
					currentField.setUnit(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(DATATYPE) == 0)
					currentField.setDataType(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(PRECISION) == 0)
					currentField.setPrecision(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(WIDTH) == 0)
					currentField.setWidth(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(REF) == 0)
					currentField.setRef(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(NAME) == 0)
					currentField.setName(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(UCD) == 0)
					currentField.setUcd(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(ARRAYSIZE) == 0)
					currentField.setArraySize(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(TYPE) == 0) // deprecated
									     // since
									     // VOTable
									     // 1.1
					currentField.setType(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(UTYPE) == 0)
					currentField.setUtype(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(ID) == 0) {
					currentField.setId(parser
						.getAttributeValue(i));
					idRefLinks.put(parser
						.getAttributeValue(i),
						currentField);
				    }
				}
			    } // FIELDREF
			    else if (name.equalsIgnoreCase(FIELDREF)) {
				currentFieldRef = new SavotFieldRef();
				currentMarkup = FIELDREF;
				int counter = parser.getAttributeCount();
				for (int i = 0; i < counter; i++) {
				    if (parser.getAttributeName(i)
					    .compareToIgnoreCase(REF) == 0)
					currentFieldRef.setRef(parser
						.getAttributeValue(i));
				    if (parser.getAttributeName(i)
					    .compareToIgnoreCase(UCD) == 0)
					currentFieldRef.setUcd(parser
						.getAttributeValue(i));
				    if (parser.getAttributeName(i)
					    .compareToIgnoreCase(UTYPE) == 0)
					currentFieldRef.setUtype(parser
						.getAttributeValue(i));
				}
			    } // VALUES
			    else if (name.equalsIgnoreCase(VALUES)) {
				currentValues = new SavotValues();
				currentMarkup = VALUES;
				int counter = parser.getAttributeCount();
				for (int i = 0; i < counter; i++) {
				    if (parser.getAttributeName(i)
					    .compareToIgnoreCase(TYPE) == 0)
					currentValues.setType(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(NULL) == 0)
					currentValues.setNull(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(INVALID) == 0)
					currentValues.setInvalid(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(REF) == 0)
					currentValues.setRef(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(ID) == 0) {
					currentValues.setId(parser
						.getAttributeValue(i));
					idRefLinks.put(parser
						.getAttributeValue(i),
						currentValues);
				    }
				}
			    } // STREAM
			    else if (name.equalsIgnoreCase(STREAM)) {
				currentStream = new SavotStream();
				currentMarkup = STREAM;
				int counter = parser.getAttributeCount();
				for (int i = 0; i < counter; i++) {
				    if (parser.getAttributeName(i)
					    .compareToIgnoreCase(TYPE) == 0)
					currentStream.setType(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(HREF) == 0)
					currentStream.setHref(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(ACTUATE) == 0)
					currentStream.setActuate(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(ENCODING) == 0)
					currentStream.setEncoding(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(EXPIRES) == 0)
					currentStream.setExpires(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(RIGHTS) == 0)
					currentStream.setRights(parser
						.getAttributeValue(i));
				}
			    } // TR
			    else if (name.equalsIgnoreCase(TR)) {
				if (trace)
				    System.err.println("TR begin");
				currentMarkup = TR;

				stats.iTDLocalReset();
				stats.iTDLocalInc();
				stats.iTDGlobalInc();

				// create a new row
				currentTR = new SavotTR();
				currentTR.setLineInXMLFile(parser
					.getLineNumber());
			    } // TD
			    else if (name.equalsIgnoreCase(TD)) {
				if (trace)
				    System.err.println("TD begin");
				currentMarkup = TD;

				// create a new data
				currentTD = new SavotTD();
				int counter = parser.getAttributeCount();
				for (int i = 0; i < counter; i++) {
				    if (parser.getAttributeName(i)
					    .compareToIgnoreCase(ENCODING) == 0) /*
										  * new
										  * 1.1
										  */
					currentTD.setEncoding(parser
						.getAttributeValue(i));
				}
				// for statistics only
				dataCounter++;
			    } // DATA
			    else if (name.equalsIgnoreCase(DATA)) {
				currentData = new SavotData();
				currentMarkup = DATA;
			    } // BINARY
			    else if (name.equalsIgnoreCase(BINARY)) {
				currentBinary = new SavotBinary();
				currentMarkup = BINARY;
			    } // FITS
			    else if (name.equalsIgnoreCase(FITS)) {
				currentFits = new SavotFits();
				currentMarkup = FITS;
				int counter = parser.getAttributeCount();
				for (int i = 0; i < counter; i++) {
				    if (parser.getAttributeName(i)
					    .compareToIgnoreCase(REF) == 0)
					currentFits.setExtnum(parser
						.getAttributeValue(i));
				}
			    } // TABLEDATA
			    else if (name.equalsIgnoreCase(TABLEDATA)) {
				currentTableData = new SavotTableData();
				currentMarkup = TABLEDATA;

				if (parsingType == ROWREAD) { // if row
							      // sequential
							      // reading then
							      // storage of the
							      // metadata
				    if (stats.iTablesLocal > 1) // not the first
								// TABLE of the
								// RESOURCE
					currentVOTable
						.getResources()
						.removeItemAt(
							currentVOTable
								.getResources()
								.getItemCount() - 1);
				    currentResource.getTables().addItem(
					    currentTable);
				    currentVOTable.getResources().addItem(
					    currentResource);
				}
			    } // PARAM
			    else if (name.equalsIgnoreCase(PARAM)) {
				currentParam = new SavotParam();
				currentMarkup = PARAM;
				int counter = parser.getAttributeCount();
				for (int i = 0; i < counter; i++) {
				    if (parser.getAttributeName(i)
					    .compareToIgnoreCase(UNIT) == 0)
					currentParam.setUnit(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(DATATYPE) == 0)
					currentParam.setDataType(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(PRECISION) == 0)
					currentParam.setPrecision(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(WIDTH) == 0)
					currentParam.setWidth(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(REF) == 0)
					currentParam.setRef(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(NAME) == 0)
					currentParam.setName(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(UCD) == 0)
					currentParam.setUcd(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(UTYPE) == 0)
					currentParam.setUtype(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(VALUE) == 0)
					currentParam.setValue(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(XTYPE) == 0)
					currentParam.setXtype(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(ARRAYSIZE) == 0)
					currentParam.setArraySize(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(XTYPE) == 0)
					currentParam.setXtype(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(ID) == 0) {
					currentParam.setId(parser
						.getAttributeValue(i));
					idRefLinks.put(parser
						.getAttributeValue(i),
						currentParam);
				    }
				}
			    } // PARAMREF
			    else if (name.equalsIgnoreCase(PARAMREF)) {
				currentParamRef = new SavotParamRef();
				currentMarkup = PARAMREF;
				int counter = parser.getAttributeCount();
				for (int i = 0; i < counter; i++) {
				    if (parser.getAttributeName(i)
					    .compareToIgnoreCase(REF) == 0)
					currentParamRef.setRef(parser
						.getAttributeValue(i));
				    if (parser.getAttributeName(i)
					    .compareToIgnoreCase(UCD) == 0)
					currentParamRef.setUcd(parser
						.getAttributeValue(i));
				    if (parser.getAttributeName(i)
					    .compareToIgnoreCase(UTYPE) == 0)
					currentParamRef.setUtype(parser
						.getAttributeValue(i));
				}
			    }
			    // LINK
			    else if (name.equalsIgnoreCase(LINK)) {
				currentLink = new SavotLink();
				currentMarkup = LINK;

				int counter = parser.getAttributeCount();
				for (int i = 0; i < counter; i++) {
				    if (parser.getAttributeName(i)
					    .compareToIgnoreCase(CONTENTROLE) == 0)
					currentLink.setContentRole(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(CONTENTTYPE) == 0)
					currentLink.setContentType(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(TITLE) == 0)
					currentLink.setTitle(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(VALUE) == 0)
					currentLink.setValue(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(HREF) == 0)
					currentLink.setHref(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(GREF) == 0) // deprecated
									     // since
									     // VOTable
									     // 1.1
					currentLink.setGref(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(ACTION) == 0)
					currentLink.setAction(parser
						.getAttributeValue(i));
				    else

				    if (parser.getAttributeName(i)
					    .compareToIgnoreCase(ID) == 0) {
					currentLink.setID(parser
						.getAttributeValue(i));
					idRefLinks.put(parser
						.getAttributeValue(i),
						currentLink);
				    }
				}
				if (trace)
				    System.err.println("LINK");
			    }
			    // INFO
			    else if (name.equalsIgnoreCase(INFO)) {
				currentInfo = new SavotInfo();
				currentMarkup = INFO;
				int counter = parser.getAttributeCount();
				for (int i = 0; i < counter; i++) {
				    if (parser.getAttributeName(i)
					    .compareToIgnoreCase(NAME) == 0)
					currentInfo.setName(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(VALUE) == 0)
					currentInfo.setValue(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(XTYPE) == 0)
					currentInfo.setXtype(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(UNIT) == 0)
					currentInfo.setUnit(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(UCD) == 0)
					currentInfo.setUcd(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(UTYPE) == 0)
					currentInfo.setUtype(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(REF) == 0)
					currentInfo.setRef(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(ID) == 0) {
					currentInfo.setId(parser
						.getAttributeValue(i));
					idRefLinks.put(parser
						.getAttributeValue(i),
						currentInfo);
				    }
				}
				if (trace)
				    System.err.println("INFO");
			    } // MIN
			    else if (name.equalsIgnoreCase(MIN)) {
				if (trace)
				    System.err.println("MIN");
				currentMarkup = MIN;
				currentMin = new SavotMin();
				int counter = parser.getAttributeCount();
				for (int i = 0; i < counter; i++) {
				    if (parser.getAttributeName(i)
					    .compareToIgnoreCase(VALUE) == 0)
					currentMin.setValue(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(INCLUSIVE) == 0)
					currentMin.setInclusive(parser
						.getAttributeValue(i));
				}
			    }
			    // MAX
			    else if (name.equalsIgnoreCase(MAX)) {
				if (trace)
				    System.err.println("MAX");
				currentMarkup = MAX;
				currentMax = new SavotMax();
				int counter = parser.getAttributeCount();
				for (int i = 0; i < counter; i++) {
				    if (parser.getAttributeName(i)
					    .compareToIgnoreCase(VALUE) == 0)
					currentMax.setValue(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(INCLUSIVE) == 0)
					currentMax.setInclusive(parser
						.getAttributeValue(i));
				}
			    }
			    // OPTION
			    else if (name.equalsIgnoreCase(OPTION)) {
				if (includedOption > 0) {
				    // inner case (multi level options)
				    putOptionStack(currentOption);
				    if (trace)
					System.err.println("OPTION - included");
				} else if (trace)
				    System.err.println("OPTION - not included");
				includedOption++;

				currentMarkup = OPTION;
				if (trace)
				    System.err.println("OPTION");
				currentOption = new SavotOption();

				int counter = parser.getAttributeCount();
				for (int i = 0; i < counter; i++) {
				    if (parser.getAttributeName(i)
					    .compareToIgnoreCase(NAME) == 0)
					currentOption.setName(parser
						.getAttributeValue(i));
				    if (parser.getAttributeName(i)
					    .compareToIgnoreCase(VALUE) == 0)
					currentOption.setValue(parser
						.getAttributeValue(i));
				}
			    } // GROUP new 1.1
			    else if (name.equalsIgnoreCase(GROUP)) {
				stats.iGroupsGlobalInc();
				if (includedGroup > 0) {
				    // inner case (multi level groups)
				    putGroupStack(currentGroup);
				    if (trace)
					System.err.println("GROUP - included");
				} else if (trace)
				    System.err.println("GROUP - not included");
				includedGroup++;

				currentMarkup = GROUP;
				if (trace)
				    System.err.println("GROUP");
				currentGroup = new SavotGroup();

				int counter = parser.getAttributeCount();
				for (int i = 0; i < counter; i++) {
				    if (parser.getAttributeName(i)
					    .compareToIgnoreCase(REF) == 0)
					currentGroup.setRef(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(NAME) == 0)
					currentGroup.setName(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(UCD) == 0)
					currentGroup.setUcd(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(UTYPE) == 0)
					currentGroup.setUtype(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(ID) == 0) {
					currentGroup.setId(parser
						.getAttributeValue(i));
					idRefLinks.put(parser
						.getAttributeValue(i),
						currentGroup);
				    }
				}
			    } // COOSYS
			    else if (name.equalsIgnoreCase(COOSYS)) {
				currentMarkup = COOSYS;
				if (trace)
				    System.err.println("COOSYS");

				currentCoosys = new SavotCoosys();

				int counter = parser.getAttributeCount();
				for (int i = 0; i < counter; i++) {
				    if (parser.getAttributeName(i)
					    .compareToIgnoreCase(EQUINOX) == 0)
					currentCoosys.setEquinox(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(EPOCH) == 0)
					currentCoosys.setEpoch(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(SYSTEM) == 0)
					currentCoosys.setSystem(parser
						.getAttributeValue(i));
				    else if (parser.getAttributeName(i)
					    .compareToIgnoreCase(ID) == 0) {
					currentCoosys.setId(parser
						.getAttributeValue(i));
					idRefLinks.put(parser
						.getAttributeValue(i),
						currentCoosys);
				    }
				}
			    }
			    // DEFINITIONS - deprecated since VOTable 1.1
			    else if (name.equalsIgnoreCase(DEFINITIONS)) {
				currentMarkup = DEFINITIONS;
				currentDefinitions = new SavotDefinitions();
				if (trace)
				    System.err.println("DEFINITIONS");
			    } else
				System.err.println("VOTable markup error : "
					+ name + " at line "
					+ parser.getLineNumber());
			}
			currentMarkup = name;
		    } catch (Exception e) {
			System.err.println("Exception START_TAG : " + e
				+ " at line " + parser.getLineNumber());
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
			    if (((String) father.elementAt(father.size() - 1))
				    .equalsIgnoreCase(VOTABLE)) {
				currentVOTable
					.setDescription(currentDescription);
				currentMarkup = "";
			    } else if (((String) father
				    .elementAt(father.size() - 1))
				    .equalsIgnoreCase(RESOURCE)) {
				currentResource
					.setDescription(currentDescription);
				currentMarkup = "";
				// return currentResource;
			    } else if (((String) father
				    .elementAt(father.size() - 1))
				    .equalsIgnoreCase(PARAM)) {
				currentParam.setDescription(currentDescription);
				currentMarkup = "";
			    } else if (((String) father
				    .elementAt(father.size() - 1))
				    .equalsIgnoreCase(TABLE)) {
				currentTable.setDescription(currentDescription);
				currentMarkup = "";
			    } else if (((String) father
				    .elementAt(father.size() - 1))
				    .equalsIgnoreCase(FIELD)) {
				currentField.setDescription(currentDescription);
				currentMarkup = "";
			    } else if (((String) father
				    .elementAt(father.size() - 1))
				    .equalsIgnoreCase(GROUP)) {
				currentGroup.setDescription(currentDescription);
				currentMarkup = "";
			    }
			} // TABLE
			else if (name.equalsIgnoreCase(TABLE)) {
			    currentResource.getTables().addItem(currentTable);
			    currentMarkup = "";

			    if (trace)
				System.err.println(currentTable.getName());
			} // FIELD - several fathers are possible

			else if (name.equalsIgnoreCase(FIELD)) {

			    if (trace)
				System.err.println("FIELD from father = "
					+ (String) father.elementAt(father
						.size() - 1));

			    if (((String) father.elementAt(father.size() - 1))
				    .equalsIgnoreCase(TABLE)) {
				currentTable.getFields().addItem(currentField);
				if (trace)
				    System.err
					    .println("FIELD from TABLE father = "
						    + father);
			    }
			} // FIELDREF
			else if (name.equalsIgnoreCase(FIELDREF)) {
			    if (((String) father.elementAt(father.size() - 1))
				    .equalsIgnoreCase(GROUP)) {
				currentGroup.getFieldsRef().addItem(
					currentFieldRef);
				if (trace)
				    System.err
					    .println("FIELDRef from GROUP father = "
						    + father);
			    }
			} // TR
			else if (name.equalsIgnoreCase(TR)) {
			    if (trace)
				System.err.println("TR end");
			    currentMarkup = "";

			    stats.iTRGlobalInc();
			    stats.iTRLocalInc();
			    stats.iTDLocalReset();

			    // add the row to the table

			    // ////////////////////////
			    if (parsingType != ROWREAD)
				currentTableData.getTRs().addItem(currentTR);
			    else
				TRComplete = true;
			    // else TR will be used without storage in the model
			    // ///////////////////////

			    // for statistics only
			    rowCounter++;

			    if (trace)
				System.err.println("ADD row");

			} // DATA
			else if (name.equalsIgnoreCase(DATA)) {
			    currentMarkup = "";
			    currentTable.setData(currentData);

			} // TD
			else if (name.equalsIgnoreCase(TD)) {
			    stats.iTDGlobalInc();
			    stats.iTDLocalInc();
			    currentMarkup = "";
			    if (trace)
				System.err.println("TD end");
			    currentTR.getTDs().addItem(currentTD);

			} // RESOURCE
			else if (name.equalsIgnoreCase(RESOURCE)) {
			    if (trace)
				System.err.println("RESOURCE end");
			    currentMarkup = "";
			    if (includedResource > 1) {
				SavotResource tempo = currentResource;
				currentResource = getResourceStack();
				currentResource.getResources().addItem(tempo);
			    } else {
				if (parsingType == FULL)
				    currentVOTable.getResources().addItem(
					    currentResource);
				if (trace)
				    System.err
					    .println(">>>>>>>> RESOURCE COMPLETED");
				resourceComplete = true;
			    }
			    includedResource--;
			} // OPTION
			else if (name.equalsIgnoreCase(OPTION)) {
			    if (trace)
				System.err.println("OPTION end");
			    currentMarkup = "";
			    if (includedOption > 1) {
				SavotOption tempo = currentOption;
				currentOption = getOptionStack();
				currentOption.getOptions().addItem(tempo);
				includedOption--;
			    } else {
				if (parsingType == FULL)
				    // OPTION - several fathers are possible
				    if (((String) father.elementAt(father
					    .size() - 1))
					    .equalsIgnoreCase(VALUES)) {
					currentValues.getOptions().addItem(
						currentOption);
					if (trace)
					    System.err
						    .println("OPTION from VALUES father = "
							    + father);
					includedOption--;
				    }
			    }
			} // GROUP
			else if (name.equalsIgnoreCase(GROUP)) {
			    if (trace)
				System.err.println("GROUP end");
			    currentMarkup = "";
			    if (includedGroup > 1) {
				SavotGroup tempo = currentGroup;
				currentGroup = getGroupStack();
				currentGroup.getGroups().addItem(tempo);
				includedGroup--;
			    } else {
				if (parsingType == FULL
					|| parsingType == ROWREAD)
				    // GROUP
				    if (((String) father.elementAt(father
					    .size() - 1))
					    .equalsIgnoreCase(TABLE)) {
					currentTable.getGroups().addItem(
						currentGroup);
					if (trace)
					    System.err
						    .println("GROUP from TABLE father = "
							    + father);
					includedGroup--;
				    }
			    }
			}
			// TABLEDATA
			else if (name.equalsIgnoreCase(TABLEDATA)) {

			    stats.iTRLocalReset();

			    currentMarkup = "";
			    currentData.setTableData(currentTableData);
			    if (trace)
				System.err.println(currentTable.getName());
			}
			// COOSYS
			else if (name.equalsIgnoreCase(COOSYS)) {
			    currentMarkup = "";
			    // COOSYS - several fathers are possible
			    if (((String) father.elementAt(father.size() - 1))
				    .equalsIgnoreCase(DEFINITIONS)) {
				// deprecated since VOTable 1.1
				currentDefinitions.getCoosys().addItem(
					currentCoosys);
				if (trace)
				    System.err
					    .println("COOSYS from DEFINITIONS father = "
						    + father);
			    } else if (((String) father
				    .elementAt(father.size() - 1))
				    .equalsIgnoreCase(RESOURCE)) {
				currentResource.getCoosys().addItem(
					currentCoosys);
				if (trace)
				    System.err
					    .println("COOSYS from RESOURCE father = "
						    + father);
			    } else if (((String) father
				    .elementAt(father.size() - 1))
				    .equalsIgnoreCase(VOTABLE)) {
				currentVOTable.getCoosys().addItem(
					currentCoosys);
				if (trace)
				    System.err
					    .println("COOSYS from VOTABLE father = "
						    + father);
			    }
			}
			// PARAM - several fathers are possible
			else if (name.equalsIgnoreCase(PARAM)) {
			    if (((String) father.elementAt(father.size() - 1))
				    .equalsIgnoreCase(DEFINITIONS)) {
				// deprecated since VOTable 1.1
				currentDefinitions.getParams().addItem(
					currentParam);
				if (trace)
				    System.err
					    .println("PARAM from DEFINITIONS father = "
						    + father);
			    } else if (((String) father
				    .elementAt(father.size() - 1))
				    .equalsIgnoreCase(RESOURCE)) {
				currentResource.getParams().addItem(
					currentParam);
				// 7 MAI resourceComplete = true;
				if (trace)
				    System.err
					    .println("PARAM from RESOURCE father = "
						    + father);
			    } else if (((String) father
				    .elementAt(father.size() - 1))
				    .equalsIgnoreCase(TABLE)) {
				currentTable.getParams().addItem(currentParam);
				if (trace)
				    System.err
					    .println("PARAM from TABLE father = "
						    + father);
			    } else if (((String) father
				    .elementAt(father.size() - 1))
				    .equalsIgnoreCase(GROUP)) {
				currentGroup.getParams().addItem(currentParam);
				if (trace)
				    System.err
					    .println("PARAM from GROUP father = "
						    + father);
			    } else if (((String) father
				    .elementAt(father.size() - 1))
				    .equalsIgnoreCase(VOTABLE)) {
				currentVOTable.getParams()
					.addItem(currentParam);
				if (trace)
				    System.err
					    .println("PARAM from VOTABLE father = "
						    + father);
			    }
			} else if (name.equalsIgnoreCase(PARAMREF)) {
			    if (((String) father.elementAt(father.size() - 1))
				    .equalsIgnoreCase(GROUP)) {
				currentGroup.getParamsRef().addItem(
					currentParamRef);
				if (trace)
				    System.err
					    .println("PARAMRef from GROUP father = "
						    + father);
			    }
			}
			// LINK
			else if (name.equalsIgnoreCase(LINK)) {
			    currentMarkup = "";
			    // LINK - several fathers are possible
			    if (((String) father.elementAt(father.size() - 1))
				    .equalsIgnoreCase(RESOURCE)) {
				currentResource.getLinks().addItem(currentLink);
				if (trace)
				    System.err
					    .println("LINK from RESOURCE father = "
						    + father);
			    } else if (((String) father
				    .elementAt(father.size() - 1))
				    .equalsIgnoreCase(TABLE)) {
				currentTable.getLinks().addItem(currentLink);
				if (trace)
				    System.err
					    .println("LINK from TABLE father = "
						    + father);
			    } else if (((String) father
				    .elementAt(father.size() - 1))
				    .equalsIgnoreCase(FIELD)) {
				currentField.getLinks().addItem(currentLink);
				if (trace)
				    System.err
					    .println("LINK from FIELD father = "
						    + father);
			    } else if (((String) father
				    .elementAt(father.size() - 1))
				    .equalsIgnoreCase(PARAM)) {
				currentParam.getLinks().addItem(currentLink);
				if (trace)
				    System.err
					    .println("LINK from PARAM father = "
						    + father);
			    }
			} // VALUES
			else if (name.equalsIgnoreCase(VALUES)) {
			    currentMarkup = "";
			    // VALUES - several fathers are possible
			    if (((String) father.elementAt(father.size() - 1))
				    .equalsIgnoreCase(PARAM)) {
				currentParam.setValues(currentValues);
				if (trace)
				    System.err
					    .println("VALUES from PARAM father = "
						    + father
						    + " ID : "
						    + currentValues.getId());
			    } else if (((String) father
				    .elementAt(father.size() - 1))
				    .equalsIgnoreCase(FIELD)) {
				currentField.setValues(currentValues);
				if (trace)
				    System.err
					    .println("VALUES from FIELD father = "
						    + father
						    + " ID : "
						    + currentValues.getId());
			    }
			} // MIN
			else if (name.equalsIgnoreCase(MIN)) {
			    currentMarkup = "";
			    currentValues.setMin(currentMin);
			    if (trace)
				System.err.println("MIN");
			} // MAX
			else if (name.equalsIgnoreCase(MAX)) {
			    currentMarkup = "";
			    currentValues.setMax(currentMax);
			    if (trace)
				System.err.println("MAX");
			}
			// STREAM
			else if (name.equalsIgnoreCase(STREAM)) {
			    currentMarkup = "";
			    // STREAM - several fathers are possible
			    if (((String) father.elementAt(father.size() - 1))
				    .equalsIgnoreCase(BINARY)) {
				currentBinary.setStream(currentStream);
				if (trace)
				    System.err
					    .println("STREAM from BINARY father = "
						    + father);
			    } else if (((String) father
				    .elementAt(father.size() - 1))
				    .equalsIgnoreCase(FITS)) {
				currentFits.setStream(currentStream);
				if (trace)
				    System.err
					    .println("STREAM from FITS father = "
						    + father);
			    }
			}
			// BINARY
			else if (name.equalsIgnoreCase(BINARY)) {
			    currentMarkup = "";
			    currentData.setBinary(currentBinary);
			    if (trace)
				System.err.println("BINARY");
			}
			// FITS
			else if (name.equalsIgnoreCase(FITS)) {
			    currentMarkup = "";
			    currentData.setFits(currentFits);
			    if (trace)
				System.err.println("FITS");
			}
			// INFO
			else if (name.equalsIgnoreCase(INFO)) {
			    currentMarkup = "";
			    if (trace)
				System.err.println("INFO father = " + father);
			    // INFO - several fathers are possible
			    if (((String) father.elementAt(father.size() - 1))
				    .equalsIgnoreCase(VOTABLE)) {
				
				// since VOTable 1.2 - if RESOURCE then INFO at the end 
				if (currentVOTable.getResources() != null && currentVOTable.getResources().getItemCount() != 0) {
					currentVOTable.getInfosAtEnd().addItem(currentInfo);
					System.out.println("ds Infos at End");
				}
				else
					currentVOTable.getInfos().addItem(currentInfo);
				
				if (trace)
				    System.err
					    .println("INFO from VOTABLE father = "
						    + father);
			    } else if (((String) father
				    .elementAt(father.size() - 1))
				    .equalsIgnoreCase(RESOURCE)) {
				
				// since VOTable 1.2 - if RESOURCE or LINK or TABLE then INFO at the end 
				if ((currentResource.getResources() != null && currentResource.getResources().getItemCount() != 0) || (currentResource.getTables() != null && currentResource.getTables().getItemCount() != 0) || (currentResource.getLinks() != null && currentResource.getLinks().getItemCount() != 0))
				    currentResource.getInfosAtEnd().addItem(currentInfo);
				else
				    currentResource.getInfos().addItem(currentInfo);
								
				if (trace)
				    System.err
					    .println("INFO from RESOURCE father = "
						    + father);
			    } else if (((String) father
				    .elementAt(father.size() - 1))
				    .equalsIgnoreCase(TABLE)) {
				
				// since VOTable 1.2 
				currentTable.getInfosAtEnd().addItem(currentInfo);
								
				if (trace)
				    System.err
					    .println("INFO from TABLE father = "
						    + father);
			    }
			}
			// DEFINITIONS
			else if (name.equalsIgnoreCase(DEFINITIONS)) {
			    // deprecated since VOTable 1.1
			    currentMarkup = "";
			    if (trace)
				System.err.println("DEFINITIONS");
			    currentVOTable.setDefinitions(currentDefinitions);
			}
			// VOTABLE
			else if (name.equalsIgnoreCase(VOTABLE)) {
			    currentMarkup = "";
			    if (trace)
				System.err.println("VOTABLE");
			} else
			    System.err.println("VOTable markup error : " + name
				    + " at line " + parser.getLineNumber());
		    } catch (Exception e) {
			System.err.println("Exception FATHER : " + father
				+ " END_TAG (" + name + ") : " + e
				+ " at line " + parser.getLineNumber());
		    }
		    break;

		case KXmlParser.END_DOCUMENT:
		    try {
			if (trace)
			    System.err.println("Document end reached!");
		    } catch (Exception e) {
			System.err.println("Exception END_DOCUMENT : " + e
				+ " at line " + parser.getLineNumber());
		    }
		    break;

		case KXmlParser.TEXT:
		    try {
			// add a data to the current row
			if (currentMarkup.equalsIgnoreCase(TD)) {
			    if (trace)
				System.err.println("TD : "
					+ (parser.getText()).trim());
			    currentTD.setContent((parser.getText()).trim());
			} else if (currentMarkup.equalsIgnoreCase(STREAM)) {
			    if (trace)
				System.err.println("STREAM : "
					+ (parser.getText()).trim());
			    currentStream.setContent((parser.getText()).trim());
			} else if (currentMarkup.equalsIgnoreCase(DESCRIPTION)) {
			    if (trace)
				System.err.println("DESCRIPTION : "
					+ (parser.getText()).trim());
			    currentDescription = (parser.getText()).trim();
			} else if (currentMarkup.equalsIgnoreCase(MIN)) {
			    if (trace)
				System.err.println("MIN : "
					+ (parser.getText()).trim());
			    currentMin.setContent((parser.getText()).trim());
			} else if (currentMarkup.equalsIgnoreCase(MAX)) {
			    if (trace)
				System.err.println("MAX : "
					+ (parser.getText()).trim());
			    currentMax.setContent((parser.getText()).trim());
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
			} else if (currentMarkup.equalsIgnoreCase(GROUP)) {
			    if (trace)
				System.err.println("GROUP : "
					+ (parser.getText()).trim());
			} else if (currentMarkup.equalsIgnoreCase(INFO)) {
			    currentInfo.setContent((parser.getText()).trim());
			    if (trace)
				System.err.println("INFO : "
					+ (parser.getText()).trim());
			}
		    } catch (Exception e) {
			System.err.println("Exception TEXT : " + e
				+ " at line " + parser.getLineNumber());
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

		// new values
		eventType = parser.next();

		// start tag
		if (parser.getEventType() == KXmlParser.START_TAG) {
		    if (trace)
			System.err.println("> FATHER, add : "
				+ parser.getName());
		    father.addElement((parser.getName()));
		} else // end tag
		if (parser.getEventType() == KXmlParser.END_TAG) {
		    if (parser.getName() != null) {
			if (trace)
			    System.err.println("> FATHER, remove : "
				    + parser.getName());
			father.removeElementAt(father.size() - 1);
		    } else if (trace)
			System.err.println("> FATHER, case null"); // when a lf
								   // or cd is
								   // reached
		}

		if ((parsingType == RESOURCEREAD) && resourceComplete) {
		    eventType = XmlPullParser.END_DOCUMENT;
		    if (trace)
			System.err
				.println(">>>>>>>>>>>>>>> RESOURCEREAD case : RESOURCE end");
		    // if (parser.getName().equalsIgnoreCase(TR)) return -1;

		}
		if ((parsingType == ROWREAD) && TRComplete) {
		    eventType = XmlPullParser.END_DOCUMENT;
		    if (trace)
			System.err
				.println(">>>>>>>>>>>>>>> ROWREAD case : TR end");
		}
	    }
	} catch (Exception f) {
	    if (trace)
		System.err.println("Exception parse : " + f + " at line "
			+ parser.getLineNumber());
	}
	return 0;
    }

    /**
     * Get the next Resource (warning : RESOURCEREAD mode only)
     * 
     * @return a SavotResource (always NULL if other mode)
     */
    public SavotResource getNextResource() {
	currentResource = null;
	try {
	    parse(parser, RESOURCEREAD);
	} catch (IOException e) {
	    if (trace)
		System.err.println("Exception getNextResource : " + e);
	}
	return currentResource;
    }

    /**
     * Get the next TR (warning : ROWREAD mode only)
     * 
     * @return a SavotTR (always NULL if other mode)
     */
    public SavotTR getNextTR() {
	currentTR = null;
	try {
	    parse(parser, ROWREAD);
	} catch (IOException e) {
	    if (trace)
		System.err.println("Exception getNextTR : " + e);
	}
	return currentTR;
    }

    /**
     * Get a reference to V0TABLE object
     * 
     * @return SavotVOTable
     */
    public SavotVOTable getVOTable() {
	return currentVOTable;
    }

    /**
     * For test only
     * 
     */
    public void sequentialTester() {
	do {
	    currentResource = getNextResource();
	} while (currentResource != null);
    }

    /**
     * Get the number of RESOURCE elements in the document (for statistics)
     * 
     * @return a long value
     */
    public long getResourceCount() {
	return resourceCounter;
    }

    /**
     * Get the number of TABLE elements in the document (for statistics)
     * 
     * @return a long value
     */
    public long getTableCount() {
	return tableCounter;
    }

    /**
     * Get the number of TR elements in the document (for statistics)
     * 
     * @return a long value
     */
    public long getTRCount() {
	return rowCounter;
    }

    /**
     * Get the number of DATA elements in the document (for statistics)
     * 
     * @return a long value
     */
    public long getDataCount() {
	return dataCounter;
    }

    /**
     * Get a reference on the Hashtable containing the link between ID and ref
     * 
     * @return a refernce to the Hashtable
     */
    @SuppressWarnings("unchecked")
    public Hashtable getIdRefLinks() {
	return idRefLinks;
    }

    /**
     * Search a RESOURCE corresponding to an ID ref
     * 
     * @param ref
     * @return a reference to a SavotResource object
     */
    public SavotResource getResourceFromRef(String ref) {
	return (SavotResource) idRefLinks.get(ref);
    }

    /**
     * Search a FIELD corresponding to an ID ref
     * 
     * @param ref
     * @return SavotField
     */
    public SavotField getFieldFromRef(String ref) {
	return (SavotField) idRefLinks.get(ref);
    }
    
    /**
     * Search a FIELDref corresponding to an ID ref
     * 
     * @param ref
     * @return SavotFieldRef
     */
    public SavotFieldRef getFieldRefFromRef(String ref) {
	return (SavotFieldRef) idRefLinks.get(ref);
    }
    
    /**
     * Search a PARAM corresponding to an ID ref
     * 
     * @param ref
     * @return SavotParam
     */
    public SavotParam getParamFromRef(String ref) {
	return (SavotParam) idRefLinks.get(ref);
    }

    /**
     * Search a PARAMref corresponding to an ID ref
     * 
     * @param ref
     * @return SavotParamRef
     */
    public SavotParamRef getParamRefFromRef(String ref) {
	return (SavotParamRef) idRefLinks.get(ref);
    }

    /**
     * Search a TABLE corresponding to an ID ref
     * 
     * @param ref
     * @return SavotTable
     */
    public SavotTable getTableFromRef(String ref) {
	return (SavotTable) idRefLinks.get(ref);
    }

    /**
     * Search a GROUP corresponding to an ID ref
     * 
     * @param ref
     * @return SavotGROUP
     */
    public SavotGroup getGroupFromRef(String ref) {
	return (SavotGroup) idRefLinks.get(ref);
    }

    /**
     * Search a RESOURCE corresponding to an ID ref
     * 
     * @param ref
     * @return SavotInfo
     */
    public SavotInfo getInfoFromRef(String ref) {
	return (SavotInfo) idRefLinks.get(ref);
    }

    /**
     * Search a VALUES corresponding to an ID ref
     * 
     * @param ref
     * @return SavotValues
     */
    public SavotValues getValuesFromRef(String ref) {
	return (SavotValues) idRefLinks.get(ref);
    }

    /**
     * Search a LINK corresponding to an ID ref
     * 
     * @param ref
     * @return SavotLink
     */
    public SavotLink getLinkFromRef(String ref) {
	return (SavotLink) idRefLinks.get(ref);
    }

    /**
     * Search a COOSYS corresponding to an ID ref
     * 
     * @param ref
     * @return SavotCoosys
     */
    @SuppressWarnings("deprecation")
    public SavotCoosys getCoosysFromRef(String ref) {
	return (SavotCoosys) idRefLinks.get(ref);
    }

    /**
     * Get current VOTable (all resources)
     * 
     * @return SavotVOTable
     */
    public SavotVOTable getAllResources() {
	return currentVOTable;
    }

    /**
     * Enable debug mode
     * 
     * @param debug
     *            boolean
     */
    public void enableDebug(boolean debug) {
	trace = debug;
    }
}
