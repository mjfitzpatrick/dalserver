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
 * Link element
 * </p>
 * 
 * @author Andre Schaaff
 * @version 3.0 (kickoff 31 May 02)
 */
public class SavotLink extends MarkupComment implements SimpleTypes {

    // content
    protected char[] content = null;

    // ID attribute
    char[] id = null;

    // content-role attribute
    char[] contentRole = null;

    // content-type attribute
    char[] contentType = null;

    // title attribute
    char[] title = null;

    // value attribute
    char[] value = null;

    // href attribute
    char[] href = null;

    // gref attribute -  removed since 1.1
    char[] gref = null;

    // action attribute - extension since 1.1
    char[] action = null;

    /**
     * Constructor
     */
    public SavotLink() {
    }

    /**
     * Set content
     * 
     * @param content
     */
    public void setContent(String content) {
	if (content != null)
	    this.content = content.toCharArray();
    }

    /**
     * Get content
     * 
     * @return String
     */
    public String getContent() {
	if (content != null)
	    return String.valueOf(content);
	else
	    return "";
    }

    /**
     * Set ID attribute
     * 
     * @param id
     */
    public void setID(String id) {
	if (id != null)
	    this.id = id.toCharArray();
    }

    /**
     * Get ID attribute
     * 
     * @return String
     */
    public String getID() {
	if (id != null)
	    return String.valueOf(id);
	else
	    return "";
    }

    /**
     * Set contentRole attribute
     * 
     * @param contentRole
     *            (query, hints, doc, location)
     */
    public void setContentRole(String contentRole) {
	if (contentRole != null)
	    this.contentRole = contentRole.toCharArray();
    }

    /**
     * Get contentRole attribute
     * 
     * @return String
     */
    public String getContentRole() {
	if (contentRole != null)
	    return String.valueOf(contentRole);
	else
	    return "";
    }

    /**
     * Set contentType attribute
     * 
     * @param contentType
     */
    public void setContentType(String contentType) {
	if (contentType != null)
	    this.contentType = contentType.toCharArray();
    }

    /**
     * Get contentType attribute
     * 
     * @return String
     */
    public String getContentType() {
	if (contentType != null)
	    return String.valueOf(contentType);
	else
	    return "";
    }

    /**
     * Set title attribute
     * 
     * @param title
     */
    public void setTitle(String title) {
	if (title != null)
	    this.title = title.toCharArray();
    }

    /**
     * Get title attribute
     * 
     * @return String
     */
    public String getTitle() {
	if (title != null)
	    return String.valueOf(title);
	else
	    return "";
    }

    /**
     * Set value attribute
     * 
     * @param value
     */
    public void setValue(String value) {
	if (value != null)
	    this.value = value.toCharArray();
    }

    /**
     * Get value attribute
     * 
     * @return String
     */
    public String getValue() {
	if (value != null)
	    return String.valueOf(value);
	else
	    return "";
    }

    /**
     * Set href attribute
     * 
     * @param href
     */
    public void setHref(String href) {
	if (href != null)
	    this.href = href.toCharArray();
    }

    /**
     * Get href attribute
     * 
     * @return String
     */
    public String getHref() {
	if (href != null)
	    return String.valueOf(href);
	else
	    return "";
    }

    /**
     * Set gref attribute removed in VOTable 1.1
     * 
     * @param gref
     */
    public void setGref(String gref) {
	if (gref != null)
	    this.gref = gref.toCharArray();
    }

    /**
     * Get gref attribute removed in VOTable 1.1
     * 
     * @return String
     */
    public String getGref() {
	if (gref != null)
	    return String.valueOf(gref);
	else
	    return "";
    }

    /**
     * Set action attribute
     * 
     * @param action
     */
    public void setAction(String action) {
	if (action != null)
	    this.action = action.toCharArray();
    }

    /**
     * Get action attribute
     * 
     * @return String
     */
    public String getAction() {
	if (action != null)
	    return String.valueOf(action);
	else
	    return "";
    }
}
