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
 * Info element
 * </p>
 * 
 * @author Andre Schaaff
 * @version 3.0 (kickoff 31 May 02)
 */
public class SavotInfo extends MarkupComment implements SimpleTypes {

    // id attribute
    char[] id = null;

    // name attribute
    char[] name = null;

    // value attribute
    char[] value = null;

    // INFO element content
    char[] content = null;

    // xtype attribute @since 1.2
    char[] xtype = null;

    // ref attribute @since 1.2
    char[] ref = null;

    // unit attribute @since 1.2
    char[] unit = null;

    // ucd attribute @since 1.2
    char[] ucd = null;

    // utype attribute @since 1.2
    char[] utype = null;

    // DESCRIPTION element - since VOTable 1.2 (not in the standard)
    protected char[] description = null;

    // VALUES element - since VOTable 1.2 (not in the standard)
    protected SavotValues values = null;

    // LINK elements - since VOTable 1.2 (not in the standard)
    protected LinkSet links = null;
    
    /**
     * Constructor
     */
    public SavotInfo() {
    }

    /**
     * Set ID attribute
     * 
     * @param id
     */
    public void setId(String id) {
	if (id != null)
	    this.id = id.toCharArray();
    }

    /**
     * Get ID attribute
     * 
     * @return String
     */
    public String getId() {
	if (id != null)
	    return String.valueOf(id);
	else
	    return "";
    }

    /**
     * Set name attribute
     * 
     * @param name
     */
    public void setName(String name) {
	if (name != null)
	    this.name = name.toCharArray();
    }

    /**
     * Get name attribute
     * 
     * @return String
     */
    public String getName() {
	if (name != null)
	    return String.valueOf(name);
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
     * Set xtype attribute
     * @since VOTable 1.2 
     * @param xtype
     */
    public void setXtype(String xtype) {
	if (xtype != null)
	    this.xtype = xtype.toCharArray();
    }

    /**
     * Get xtype attribute
     * 
     * @since VOTable 1.2 
     * @return String
     */
    public String getXtype() {
	if (xtype != null)
	    return String.valueOf(xtype);
	else
	    return "";
    }
    
    /**
     * Set ref attribute
     * 
     * @since VOTable 1.2
     * @param ref
     */
    public void setRef(String ref) {
	if (ref != null)
	    this.ref = ref.toCharArray();
    }

    /**
     * Get ref attribute
     * 
     * @since VOTable 1.2
     * @return String
     */
    public String getRef() {
	if (ref != null)
	    return String.valueOf(ref);
	else
	    return "";
    }

    /**
     * Set unit attribute
     * 
     * @since VOTable 1.2
     * @param unit
     */
    public void setUnit(String unit) {
	if (unit != null)
	    this.unit = unit.toCharArray();
    }

    /**
     * Get unit attribute
     * 
     * @since VOTable 1.2
     * @return String
     */
    public String getUnit() {
	if (unit != null)
	    return String.valueOf(unit);
	else
	    return "";
    }    

    /**
     * Set ucd attribute
     * 
     * @since VOTable 1.2
     * @param ucd
     */
    public void setUcd(String ucd) {
	if (ucd != null)
	    this.ucd = ucd.toCharArray();
    }

    /**
     * Get ucd attribute
     * 
     * @since VOTable 1.2
     * @return String
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
     * @since VOTable 1.2
     * @param utype
     */
    public void setUtype(String utype) {
	if (utype != null)
	    this.utype = utype.toCharArray();
    }

    /**
     * Get utype attribute
     * 
     * @since VOTable 1.2
     * @return String
     */
    public String getUtype() {
	if (utype != null)
	    return String.valueOf(utype);
	else
	    return "";
    }

    /**
     * Set element content
     * 
     * @param content
     */
    public void setContent(String content) {
	this.content = content.toCharArray();
    }

    /**
     * Get element content
     * 
     * @return a String
     */
    public String getContent() {
	if (content != null)
	    return String.valueOf(content);
	else
	    return "";
    }
    
    /**
     * Set DESCRIPTION content
     * 
     * @since VOTable 1.2 (not in the standard)
     * @param description
     */
    public void setDescription(String description) {
	if (description != null)
	    this.description = description.toCharArray();
    }

    /**
     * Get DESCRIPTION content
     * 
     * @since VOTable 1.2 (not in the standard)
     * @return String
     */
    public String getDescription() {
	if (description != null)
	    return String.valueOf(description);
	else
	    return "";
    }

    /**
     * Set the VALUES element
     * 
     * @since VOTable 1.2 (not in the standard)
     * @param values
     */
    public void setValues(SavotValues values) {
	this.values = values;
    }

    /**
     * Get the VALUES element
     * 
     * @since VOTable 1.2 (not in the standard)
     * @return SavotValues
     */
    public SavotValues getValues() {
	return values;
    }

    /**
     * Get LINK elements set reference
     * 
     * @since VOTable 1.2 (not in the standard)
     * @return LinkSet
     */
    public LinkSet getLinks() {
	if (links == null)
	    links = new LinkSet();
	return links;
    }

    /**
     * Set LINK elements set reference
     * 
     * @since VOTable 1.2 (not in the standard)
     * @param links
     */
    public void setLinks(LinkSet links) {
	this.links = links;
    }
}