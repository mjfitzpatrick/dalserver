//
// ------
//
// SAVOT SAX Consumer
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


package cds.savot.sax;

import java.util.Vector;

/**
* <p>This interface must be implemented to use the Savot SAX parser </p>
* @author Andre Schaaff
* @version 2.6 Copyright CDS 2002-2005
*  (kickoff 31 May 02)
*/

public interface SavotSAXConsumer {

  // start elements

  public abstract void startVotable(Vector attributes);

  public abstract void startDescription();

  public abstract void startResource(Vector attributes);

  public abstract void startTable(Vector attributes);

  public abstract void startField(Vector attributes);

  public abstract void startFieldref(Vector attributes);

  public abstract void startValues(Vector attributes);

  public abstract void startStream(Vector attributes);

  public abstract void startTR();

  public abstract void startTD(Vector attributes);

  public abstract void startData();

  public abstract void startBinary();

  public abstract void startFits(Vector attributes);

  public abstract void startTableData();

  public abstract void startParam(Vector attributes);

  public abstract void startParamRef(Vector attributes);

  public abstract void startLink(Vector attributes);

  public abstract void startInfo(Vector attributes);

  public abstract void startMin(Vector attributes);

  public abstract void startMax(Vector attributes);

  public abstract void startOption(Vector attributes);

  public abstract void startGroup(Vector attributes);

  public abstract void startCoosys(Vector attributes);

  public abstract void startDefinitions(); // deprecated since VOTable 1.1

  // end elements

  public abstract void endVotable();

  public abstract void endDescription();

  public abstract void endResource();

  public abstract void endTable();

  public abstract void endField();

  public abstract void endFieldref();

  public abstract void endValues();

  public abstract void endStream();

  public abstract void endTR();

  public abstract void endTD();

  public abstract void endData();

  public abstract void endBinary();

  public abstract void endFits();

  public abstract void endTableData();

  public abstract void endParam();

  public abstract void endParamRef();

  public abstract void endLink();

  public abstract void endInfo();

  public abstract void endMin();

  public abstract void endMax();

  public abstract void endOption();

  public abstract void endGroup();

  public abstract void endCoosys();

  public abstract void endDefinitions(); // deprecated since VOTable 1.1

  // TEXT

  public abstract void textTD(String text);

  public abstract void textMin(String text);

  public abstract void textMax(String text);

  public abstract void textCoosys(String text);

  public abstract void textLink(String text);

  public abstract void textOption(String text);

  public abstract void textGroup(String text);

  public abstract void textInfo(String text);

  public abstract void textDescription(String text);

  public abstract void textStream(String text);

  // document

  public abstract void startDocument();

  public abstract void endDocument();
}
