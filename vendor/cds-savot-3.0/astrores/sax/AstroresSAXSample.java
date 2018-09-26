//
// Copyright 2002-2011 - Universite de Strasbourg / Centre National de la
// Recherche Scientifique
// ------
//
// Astrores SAX Sample
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

import java.util.Vector;

/**
 * <p>
 * Astrores SAX Sample, this sample shows how to use the Astrores SAX parser
 * </p>
 * 
 * @author Andre Schaaff
 * @version 3.0 (kickoff 31 May 02)
 */

public class AstroresSAXSample implements AstroresSAXConsumer {
    public AstroresSAXSample() {
    }

    // attributes is a Vector containing couples of (attribute name, attribute
    // value)
    // exemple : (attributes.elementAt(0), attributes.elementAt(1)),
    // (attributes.elementAt(2), attributes.elementAt(3)), ...

    // start elements
    public void startAstrores(Vector attributes) {
    }

    public void startDescription() {
    }

    public void startResource(Vector attributes) {
    }

    public void startTable(Vector attributes) {
    }

    public void startField(Vector attributes) {
    }

    public void startFieldref(Vector attributes) {
    }

    public void startValues(Vector attributes) {
    }

    public void startStream(Vector attributes) {
    }

    public void startTR() {
	System.out.println("Start new ROW");
    }

    public void startTD(Vector attributes) {
    }

    public void startData() {
    }

    public void startBinary() {
    }

    public void startFits(Vector attributes) {
    }

    public void startTableData() {
    }

    public void startParam(Vector attributes) {
    }

    public void startParamRef(Vector attributes) {
    }

    public void startLink(Vector attributes) {
    }

    public void startInfo(Vector attributes) {
    }

    public void startMin(Vector attributes) {
    }

    public void startMax(Vector attributes) {
    }

    public void startOption(Vector attributes) {
    }

    public void startGroup(Vector attributes) {
    }

    public void startCoosys(Vector attributes) {
    }

    public void startDefinitions() {
    }

    // end elements

    public void endAstrores() {
    }

    public void endDescription() {
    }

    public void endResource() {
    }

    public void endTable() {
    }

    public void endField() {
    }

    public void endFieldref() {
    }

    public void endValues() {
    }

    public void endStream() {
    }

    public void endTR() {
    }

    public void endTD() {
    }

    public void endData() {
    }

    public void endBinary() {
    }

    public void endFits() {
    }

    public void endTableData() {
    }

    public void endParam() {
    }

    public void endParamRef() {
    }

    public void endLink() {
    }

    public void endInfo() {
    }

    public void endMin() {
    }

    public void endMax() {
    }

    public void endOption() {
    }

    public void endGroup() {
    }

    public void endCoosys() {
    }

    public void endDefinitions() {
    }

    // TEXT

    public void textTD(String text) {
	System.out.println(text);
    }

    public void textMin(String text) {
    }

    public void textMax(String text) {
    }

    public void textCoosys(String text) {
    }

    public void textLink(String text) {
    }

    public void textOption(String text) {
    }

    public void textGroup(String text) {
    }

    public void textInfo(String text) {
    }

    public void textDescription(String text) {
    }

    // document
    public void startDocument() {
    }

    public void endDocument() {
    }
}
