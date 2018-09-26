//
// ------
//
// SAVOT Pull Parser
//
// Author:  André Schaaff
// Address: Centre de Donnees astronomiques de Strasbourg
//          11 rue de l'Universite
//          67000 STRASBOURG
//          FRANCE
// Email:   schaaff@astro.u-strasbg.fr, question@simbad.u-strasbg.fr
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
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Hashtable;

// VOTable internal data model
import cds.savot.model.*;

// pull parser
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;

/**
* <p>It has been tested with kXML Pull parser implementation </p>
* <p>but it is possible to use other pull parsers</p>
* <p>Designed to use with Pull parsers complient with Standard Pull Implementation v1</p>
* @author Andre Schaaff
* @version 2.6 Copyright CDS 2002-2005
*  (kickoff 31 May 02)
*/
public class SavotPullParser {

  // the pull parser engine
  private SavotPullEngine engine = null;


  /**
   * Constructor
   * @param file a file to parse
   * @param mode FULL or SEQUENTIAL (for small memory size applications)
  */
  public SavotPullParser(String file, int mode) {
      this(file, mode, false);
  }

  /**
   * Constructor
   * @param file a file to parse
   * @param mode FULL or SEQUENTIAL (for small memory size applications)
  */
  public SavotPullParser(String file, int mode, boolean debug) {

    try {
      // new parser
      XmlPullParser parser = new KXmlParser();

      engine = new SavotPullEngine(parser, file, mode, debug);

      // parse the stream in the given mode
      if (mode == SavotPullEngine.FULL)
        engine.parse(parser, mode);
    } catch (IOException e){
      System.err.println(e);
    } catch (Exception f){
      System.err.println(f);
    }
  }

  /**
   * Constructor
   * @param url url to parse
   * @param mode FULL or SEQUENTIAL (for small memory size applications)
   * @param enc encoding (example : UTF-8)
   */
  public SavotPullParser(URL url, int mode, String enc) {
    this(url, mode, enc, false);
  }
  /**
   * Constructor
   * @param url url to parse
   * @param mode FULL or SEQUENTIAL (for small memory size applications)
   * @param enc encoding (example : UTF-8)
   */
  public SavotPullParser(URL url, int mode, String enc, boolean debug) {

    try {
      // new parser
      KXmlParser parser = new KXmlParser();

      engine = new SavotPullEngine(parser, url, mode, enc, debug);

      // parse the stream in the given mode
      if (mode == SavotPullEngine.FULL)
        engine.parse(parser, mode);

    } catch (IOException e){
      System.err.println(e);
    }
    catch (Exception f){
      System.err.println(f);
    }
   }

   /**
    * Constructor
    * @param instream stream to parse
    * @param mode FULL or SEQUENTIAL (for small memory size applications)
    * @param enc encoding (example : UTF-8)
    */
     public SavotPullParser(InputStream instream, int mode, String enc) {
       this(instream, mode, enc, false);
     }
  /**
   * Constructor
   * @param instream stream to parse
   * @param mode FULL or SEQUENTIAL (for small memory size applications)
   * @param enc encoding (example : UTF-8)
   */
    public SavotPullParser(InputStream instream, int mode, String enc, boolean debug) {
      try {
        // new parser
        KXmlParser parser = new KXmlParser();

        engine = new SavotPullEngine(parser, instream, mode, enc, debug);

        // parse the stream in the given mode
        if (mode == SavotPullEngine.FULL)
          engine.parse(parser, mode);

      } catch (IOException e){
        System.err.println(e);
      }
      catch (Exception f){
        System.err.println(f);
      }
   }

  /**
   * Get the next Resource (sequential mode only)
   * @return a SavotResource
   */
  public SavotResource getNextResource() {
      return engine.getNextResource();
  }

  /**
   * Get a reference to V0TABLE object
   * @return SavotVOTable
   */
  public SavotVOTable getVOTable() {
    return engine.getAllResources();
  }

  /**
  * Get the number of RESOURCE elements in the document (for statistics)
  * @return a long value
  */
  public long getResourceCount() {
    return engine.getResourceCount();
  }

  /**
  * Get the number of TABLE elements in the document (for statistics)
  * @return a long value
  */
  public long getTableCount() {
    return engine.getTableCount();
  }

  /**
  * Get the number of TR elements in the document (for statistics)
  * @return a long value
  */
  public long getTRCount() {
    return engine.getTRCount();
  }

  /**
  * Get the number of DATA elements in the document (for statistics)
  * @return a long value
  */
  public long getDataCount() {
    return engine.getDataCount();
  }

  /**
   * Get a reference on the Hashtable containing the link between ID and ref
   * @return a refernce to the Hashtable
   */
  public Hashtable getIdRefLinks() {
    return engine.getIdRefLinks();
  }

  /**
   * Search a RESOURCE corresponding to an ID ref
   * @param ref
   * @return a reference to a SavotResource object
   */
  public SavotResource getResourceFromRef(String ref) {
    return engine.getResourceFromRef(ref);
  }

  /**
   * Search a FIELD corresponding to an ID ref
   * @param ref
   * @return SavotField
   */
  public SavotField getFieldFromRef(String ref) {
    return engine.getFieldFromRef(ref);
  }

  /**
   * Search a PARAM corresponding to an ID ref
   * @param ref
   * @return SavotParam
   */
  public SavotParam getParamFromRef(String ref) {
    return engine.getParamFromRef(ref);
  }

  /**
   * Search a TABLE corresponding to an ID ref
   * @param ref
   * @return SavotTable
   */
  public SavotTable getTableFromRef(String ref) {
    return engine.getTableFromRef(ref);
  }

  /**
   * Search a RESOURCE corresponding to an ID ref
   * @param ref
   * @return SavotInfo
   */
  public SavotInfo getInfoFromRef(String ref) {
    return engine.getInfoFromRef(ref);
  }

  /**
   * Search a VALUES corresponding to an ID ref
   * @param ref
   * @return SavotValues
   */
  public SavotValues getValuesFromRef(String ref) {
    return engine.getValuesFromRef(ref);
  }

  /**
   * Search a LINK corresponding to an ID ref
   * @param ref
   * @return SavotLink
   */
  public SavotLink getLinkFromRef(String ref) {
    return engine.getLinkFromRef(ref);
  }

  /**
   * Search a COOSYS corresponding to an ID ref
   * @param ref
   * @return SavotCoosys
   */
  public SavotCoosys getCoosysFromRef(String ref) {
    return engine.getCoosysFromRef(ref);
  }

  /**
   * Get all resources
   * @return SavotVOTable
   */
  public SavotVOTable getAllResources() {
    return engine.getAllResources();
  }

  /**
   * Get Parser Version
   * @return String
   */
  public String getVersion() {
    return engine.SAVOTPARSER;
  }

  /**
   * For test only
   *
   */
  public void sequentialTester() {
    SavotResource currentResource = new SavotResource();
    do {
      currentResource = engine.getNextResource();
    }
    while ( currentResource != null );
  }

  /**
   * Enable debug mode
   * @param debug boolean
   */
  public void enableDebug(boolean debug) {
    engine.enableDebug(debug);
  }

  /** Main
    *
    * @param argv
    * @throws IOException
    */
  public static void main  (String [] argv) throws IOException {

    if (argv.length == 0)
      System.out.println("Usage: java SavotPullParser <xml document>");
    else {
      new SavotPullParser(argv[0], SavotPullEngine.FULL);
    }
  }
}
