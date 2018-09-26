//
// Copyright 2002-2011 - Universite de Strasbourg / Centre National de la
// Recherche Scientifique
// ------
//
// Astrores SAX Parser
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
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

// pull parser
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;

/**
 * <p>
 * Savot Pull Parser, t has been tested with kXML Pull parser implementation
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
public class AstroresSAXParser {

    // the pull parser engine
    private AstroresSAXEngine engine = null;

    /**
     * Constructor
     * 
     * @param consumer
     *            AstroresSAXConsumer
     * @param file
     *            a file to parse
     */
    public AstroresSAXParser(AstroresSAXConsumer consumer, String file) {
	this(consumer, file, false);
    }

    /**
     * Constructor
     * 
     * @param consumer
     *            AstroresSAXConsumer
     * @param file
     *            a file to parse
     * @param debug
     *            boolean
     */
    public AstroresSAXParser(AstroresSAXConsumer consumer, String file,
	    boolean debug) {

	try {
	    // new parser
	    XmlPullParser parser = new KXmlParser();

	    engine = new AstroresSAXEngine(consumer, parser, file, debug);

	    engine.parse(parser);

	} catch (IOException e) {
	    System.err.println("AstroresSAXParser : " + e);
	} catch (Exception f) {
	    System.err.println("AstroresSAXParser : " + f);
	}
    }

    /**
     * Constructor
     * 
     * @param consumer
     *            AstroresSAXConsumer
     * @param url
     *            url to parse
     * @param enc
     *            encoding (example : UTF-8)
     */
    public AstroresSAXParser(AstroresSAXConsumer consumer, URL url, String enc) {
	this(consumer, url, enc, false);
    }

    /**
     * Constructor
     * 
     * @param consumer
     *            AstroresSAXConsumer
     * @param url
     *            url to parse
     * @param enc
     *            encoding (example : UTF-8)
     * @param debug
     *            boolean
     */
    public AstroresSAXParser(AstroresSAXConsumer consumer, URL url, String enc,
	    boolean debug) {

	try {
	    // new parser
	    KXmlParser parser = new KXmlParser();

	    engine = new AstroresSAXEngine(consumer, parser, url, enc, debug);

	    engine.parse(parser);

	} catch (IOException e) {
	    System.err.println("AstroresSAXParser : " + e);
	} catch (Exception f) {
	    System.err.println("AstroresSAXParser : " + f);
	}
    }

    /**
     * Constructor
     * 
     * @param consumer
     *            AstroresSAXConsumer
     * @param instream
     *            stream to parse
     * @param enc
     *            encoding (example : UTF-8)
     */
    public AstroresSAXParser(AstroresSAXConsumer consumer,
	    InputStream instream, String enc) {
	this(consumer, instream, enc, false);
    }

    /**
     * Constructor
     * 
     * @param consumer
     *            AstroresSAXConsumer
     * @param instream
     *            stream to parse
     * @param enc
     *            encoding (example : UTF-8)
     * @param debug
     *            boolean
     */
    public AstroresSAXParser(AstroresSAXConsumer consumer,
	    InputStream instream, String enc, boolean debug) {
	try {
	    // new parser
	    KXmlParser parser = new KXmlParser();

	    engine = new AstroresSAXEngine(consumer, parser, instream, enc,
		    debug);

	    engine.parse(parser);

	} catch (IOException e) {
	    System.err.println("AstroresSAXParser : " + e);
	} catch (Exception f) {
	    System.err.println("AstroresSAXParser : " + f);
	}
    }

    /**
     * Main
     * 
     * @param argv
     * @throws IOException
     */
    public static void main(String[] argv) throws IOException {

	if (argv.length == 0)
	    System.err.println("Usage: java AstroresSAXParser <xml document>");
	else {
	    // new AstroresSAXParser(argv[0]);
	}
    }
}
