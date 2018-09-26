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
 * Values element
 * </p>
 * 
 * @author Andre Schaaff
 * @version 3.0 (kickoff 31 May 02)
 */
public class SavotValues extends MarkupComment implements SimpleTypes {

    // ID attribute
    char[] id = null;

    // type attribute
    char[] type = "legal".toCharArray();

    // null content
    char[] nul = null;

    // ref content
    char[] ref = null;

    // invalid content - deprecated since VOTable 1.1
    char[] invalid = null;

    // MIN element
    SavotMin min = null;

    // MAX element
    SavotMax max = null;

    // OPTION element
    OptionSet options = null;

    /**
     * Constructor
     */
    public SavotValues() {
    }

    /**
     * Set the id attribute
     * 
     * @param id
     *            String
     */
    public void setId(String id) {
	if (id != null)
	    this.id = id.toCharArray();
    }

    /**
     * Get the id attribute
     * 
     * @return a String
     */
    public String getId() {
	if (id != null)
	    return String.valueOf(id);
	else
	    return "";
    }

    /**
     * Set the type attribute
     * 
     * @param type
     *            String (legal, actual)
     */
    public void setType(String type) {
	if (type != null)
	    this.type = type.toCharArray();
    }

    /**
     * Get the type attribute
     * 
     * @return a String
     */
    public String getType() {
	if (type != null)
	    return String.valueOf(type);
	else
	    return "";
    }

    /**
     * Set the null attribute
     * 
     * @param nul
     *            String
     */
    public void setNull(String nul) {
	if (nul != null)
	    this.nul = nul.toCharArray();
    }

    /**
     * Get the null attribute
     * 
     * @return a String
     */
    public String getNull() {
	if (nul != null)
	    return String.valueOf(nul);
	else
	    return "";
    }

    /**
     * Set the ref attribute
     * 
     * @param ref
     *            ref
     */
    public void setRef(String ref) {
	if (ref != null)
	    this.ref = ref.toCharArray();
    }

    /**
     * Get the ref attribute
     * 
     * @return a String
     */
    public String getRef() {
	if (ref != null)
	    return String.valueOf(ref);
	else
	    return "";
    }

    /**
     * Set the invalid attribute deprecated since VOTable 1.1
     * 
     * @param invalid
     *            String
     */
    public void setInvalid(String invalid) {
	if (invalid != null)
	    this.invalid = invalid.toCharArray();
    }

    /**
     * Get the invalid attribute deprecated since VOTable 1.1
     * 
     * @return a String
     */
    public String getInvalid() {
	if (invalid != null)
	    return String.valueOf(invalid);
	else
	    return "";
    }

    /**
     * Set MIN element
     * 
     * @param min
     */
    public void setMin(SavotMin min) {
	this.min = min;
    }

    /**
     * Get MIN element
     * 
     * @return a SavotMin object
     */
    public SavotMin getMin() {
	return min;
    }

    /**
     * Set MAX element
     * 
     * @param max
     */
    public void setMax(SavotMax max) {
	this.max = max;
    }

    /**
     * Get MAX element
     * 
     * @return a SavotMax object
     */
    public SavotMax getMax() {
	return max;
    }

    /**
     * Get OPTION element set reference
     * 
     * @return OptionSet object
     */
    public OptionSet getOptions() {
	if (options == null)
	    options = new OptionSet();
	return options;
    }

    /**
     * Set OPTION element set reference
     * 
     * @param options
     */
    public void setOptions(OptionSet options) {
	this.options = options;
    }
}
