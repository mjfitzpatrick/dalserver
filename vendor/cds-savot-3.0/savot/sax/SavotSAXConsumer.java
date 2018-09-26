//
// Copyright 2002-2011 - Universite de Strasbourg / Centre National de la
// Recherche Scientifique
// ------
//
// SAVOT SAX Consumer
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

package cds.savot.sax;

import java.util.Vector;

import cds.savot.model.SavotCoosys;
import cds.savot.model.SavotField;
import cds.savot.model.SavotFieldRef;
import cds.savot.model.SavotFits;
import cds.savot.model.SavotGroup;
import cds.savot.model.SavotInfo;
import cds.savot.model.SavotLink;
import cds.savot.model.SavotMax;
import cds.savot.model.SavotMin;
import cds.savot.model.SavotOption;
import cds.savot.model.SavotParam;
import cds.savot.model.SavotParamRef;
import cds.savot.model.SavotResource;
import cds.savot.model.SavotStream;
import cds.savot.model.SavotTD;
import cds.savot.model.SavotTable;
import cds.savot.model.SavotVOTable;
import cds.savot.model.SavotValues;

/**
 * <p>
 * This interface must be implemented to use the Savot SAX parser
 * </p>
 * 
 * @author Andre Schaaff
 * @version 3.0 (kickoff 31 May 02)
 */

@SuppressWarnings("deprecation")
public interface SavotSAXConsumer {

    // start elements

    public abstract void startVotable(Vector<SavotVOTable> attributes);

    public abstract void startDescription();

    public abstract void startResource(Vector<SavotResource> attributes);

    public abstract void startTable(Vector<SavotTable> attributes);

    public abstract void startField(Vector<SavotField> attributes);

    public abstract void startFieldref(Vector<SavotFieldRef> attributes);

    public abstract void startValues(Vector<SavotValues> attributes);

    public abstract void startStream(Vector<SavotStream> attributes);

    public abstract void startTR();

    public abstract void startTD(Vector<SavotTD> attributes);

    public abstract void startData();

    public abstract void startBinary();

    public abstract void startFits(Vector<SavotFits> attributes);

    public abstract void startTableData();

    public abstract void startParam(Vector<SavotParam> attributes);

    public abstract void startParamRef(Vector<SavotParamRef> attributes);

    public abstract void startLink(Vector<SavotLink> attributes);

    public abstract void startInfo(Vector<SavotInfo> attributes);

    public abstract void startMin(Vector<SavotMin> attributes);

    public abstract void startMax(Vector<SavotMax> attributes);

    public abstract void startOption(Vector<SavotOption> attributes);

    public abstract void startGroup(Vector<SavotGroup> attributes);

    /**
     * @deprecated since VOTable 1.2
     * @param attributes
     */
    public abstract void startCoosys(Vector<SavotCoosys> attributes);

    /**
     * @deprecated since VOTable 1.1
     */
    public abstract void startDefinitions(); 

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

    /**
     * @deprecated since VOTable 1.2
     */
    public abstract void endCoosys();

    /**
     * @deprecated since VOTable 1.1
     */
    public abstract void endDefinitions();

    // TEXT

    public abstract void textTD(String text);

    public abstract void textMin(String text);

    public abstract void textMax(String text);

    /**
     * @deprecated since VOTable 1.2
     * @param text
     */
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
