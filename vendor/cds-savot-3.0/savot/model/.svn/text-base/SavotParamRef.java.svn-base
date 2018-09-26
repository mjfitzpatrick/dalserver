//
// Copyright 2002-2011 - Universite de Strasbourg / Centre National de la
// Recherche Scientifique
// ------
//
// SAVOT Data Model
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
//

package cds.savot.model;

/**
 * <p>
 * Reference to Param element
 * </p>
 * 
 * @author Andre Schaaff
 * @version 3.0 (kickoff 31 May 02)
 */
public class SavotParamRef extends MarkupComment implements SimpleTypes {

    // ref attribute
    protected char[] ref = null;

    // ucd attribute - since VOTable 1.2
    protected char[] ucd = null;

    // utype attribute - since VOTable 1.2
    protected char[] utype = null;
    
    /**
     * Constructor
     */
    public SavotParamRef() {
    }

    /**
     * Set ref attribute
     * 
     * @param ref
     */
    public void setRef(String ref) {
	if (ref != null)
	    this.ref = ref.toCharArray();
    }

    /**
     * Get ref attribute
     * 
     * @return String
     */
    public String getRef() {
	if (ref != null)
	    return String.valueOf(ref);
	else
	    return "";
    }
    
    
    /**
     * Set ucd attribute
     * 
     * @param ucd
     * @since VOTable 1.2
     */
    public void setUcd(String ucd) {
	if (ucd != null)
	    this.ucd = ucd.toCharArray();
    }

    /**
     * Get ucd attribute
     * 
     * @return String
     * @since VOTable 1.2
     */
    public String getUcd() {
	if (ucd != null)
	    return String.valueOf(ucd);
	else
	    return "";
    }

    /**
     * Set utype attribute
     * 
     * @param utype
     * @since VOTable 1.2
     */
    public void setUtype(String utype) {
	if (utype != null)
	    this.utype = utype.toCharArray();
    }

    /**
     * Get utype attribute
     * 
     * @return String
     * @since VOTable 1.2
     */
    public String getUtype() {
	if (utype != null)
	    return String.valueOf(utype);
	else
	    return "";
    }

}
