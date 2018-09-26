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
 * VOTable element
 * </p>
 * 
 * @author Andre Schaaff
 * @version 3.0 (kickoff 31 May 02)
 */
public class SavotVOTable extends MarkupComment implements SimpleTypes {

    // VOTable document header : xmlns, etc.
    protected VOTableHead head = null;
    
    // xmlns attribute
    protected char[] xmlns = null;

    // xmlns:xsi attribute
    protected char[] xmlnsxsi = null;

    // xsi:NoNamespaceSchemaLocation attribute
    protected char[] xsinoschema = null;

    // xsi:schemaLocation attribute
    protected char[] xsischema = null;

    // id attribute
    protected char[] id = null;

    // version attribute (default : 1.2)
    protected char[] version = "1.2".toCharArray();

    // DESCRIPTION element
    protected char[] description = null;

    // COOSYS element set - deprecated since 1.2
    protected CoosysSet coosys = null;

    // PARAM element set
    protected ParamSet params = null;
    
    // GROUP element set
    protected GroupSet groups = null;
    
    // INFO element set
    protected InfoSet infos = null;

    // DEFINITIONS element
    @SuppressWarnings("deprecation")
    protected SavotDefinitions definitions = null;

    // RESOURCE element
    protected ResourceSet resources = null;
    
    // INFO element set at the end - since VOTable1.2
    protected InfoSet infosAtEnd = null;

    /**
     * Constructor
     */
    public SavotVOTable() {
    }

    /**
     * Set the global attributes (<VOTABLE .. global attributes ..
     * version="1.1">) (used for the writer)
     * 
     * @param xmlns
     *            String
     */
    public void setXmlns(String xmlns) {
	if (xmlns != null)
	    this.xmlns = xmlns.toCharArray();
    }

    /**
     * Get the global attributes (<VOTABLE .. global attributes ..
     * version="1.1"> (used for the writer)
     * 
     * @return String
     */
    public String getXmlns() {
	if (xmlns != null)
	    return String.valueOf(xmlns);
	else
	    return "";
    }

    /**
     * Set the global attributes (<VOTABLE .. global attributes ..
     * version="1.1">) (used for the writer)
     * 
     * @param xmlnsxsi
     *            String
     */
    public void setXmlnsxsi(String xmlnsxsi) {
	if (xmlnsxsi != null)
	    this.xmlnsxsi = xmlnsxsi.toCharArray();
    }

    /**
     * Get the global attributes (<VOTABLE .. global attributes ..
     * version="1.1"> (used for the writer)
     * 
     * @return String
     */
    public String getXmlnsxsi() {
	if (xmlnsxsi != null)
	    return String.valueOf(xmlnsxsi);
	else
	    return "";
    }

    /**
     * Set the global attributes (<VOTABLE .. global attributes ..
     * version="1.1">) (used for the writer)
     * 
     * @param xsinoschema
     *            String
     */
    public void setXsinoschema(String xsinoschema) {
	if (xsinoschema != null)
	    this.xsinoschema = xsinoschema.toCharArray();
    }

    /**
     * Get the global attributes (<VOTABLE .. global attributes ..
     * version="1.1"> (used for the writer)
     * 
     * @return String
     */
    public String getXsinoschema() {
	if (xsinoschema != null)
	    return String.valueOf(xsinoschema);
	else
	    return "";
    }

    /**
     * Set the global attributes (<VOTABLE .. global attributes ..
     * version="1.1">) (used for the writer)
     * 
     * @param xsischema
     *            String
     */
    public void setXsischema(String xsischema) {
	if (xsischema != null)
	    this.xsischema = xsischema.toCharArray();
    }

    /**
     * Get the global attributes (<VOTABLE .. global attributes ..
     * version="1.1"> (used for the writer)
     * 
     * @return String
     */
    public String getXsischema() {
	if (xsischema != null)
	    return String.valueOf(xsischema);
	else
	    return "";
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
     * @return String
     */
    public String getId() {
	if (id != null)
	    return String.valueOf(id);
	else
	    return "";
    }

    /**
     * Set the version attribute
     * 
     * @param version
     *            String
     */
    public void setVersion(String version) {
	if (version != null)
	    this.version = version.toCharArray();
    }

    /**
     * Get the version attribute
     * 
     * @return String
     */
    public String getVersion() {
	if (version != null)
	    return String.valueOf(version);
	else
	    return "";
    }

    /**
     * Set DESCRIPTION element
     * 
     * @param description
     */
    public void setDescription(String description) {
	if (description != null)
	    this.description = description.toCharArray();
    }

    /**
     * Get DESCRIPTION element
     * 
     * @return a String
     */
    public String getDescription() {
	if (description != null)
	    return String.valueOf(description);
	else
	    return "";
    }

    /**
     * Set DEFINITIONS element
     * 
     * @deprecated since VOTable 1.1
     * @param definitions
     */
    public void setDefinitions(SavotDefinitions definitions) {
	this.definitions = definitions;
    }

    /**
     * Get DEFINITIONS element
     * 
     * @deprecated since VOTable 1.1
     * @return SavotDefinitions
     */
    public SavotDefinitions getDefinitions() {
	return definitions;
    }

    /**
     * Set the Coosys elements
     * 
     * @deprecated since VOTable 1.2
     * @param coosys
     */
    public void setCoosys(CoosysSet coosys) {
	this.coosys = coosys;
    }

    /**
     * Get the Coosys elements
     * 
     * @deprecated since VOTable 1.2
     * @return a CoosysSet object
     */
    public CoosysSet getCoosys() {
	if (coosys == null)
	    coosys = new CoosysSet();
	return coosys;
    }

    /**
     * Set the Infos elements
     * 
     * @param infos
     */
    public void setInfos(InfoSet infos) {
	this.infos = infos;
    }

    /**
     * Get the Infos elements
     * 
     * @return a InfoSet object
     */
    public InfoSet getInfos() {
	if (infos == null)
	    infos = new InfoSet();
	return infos;
    }
    
    /**
     * Set the Param elements
     * 
     * @param params
     */
    public void setParams(ParamSet params) {
	this.params = params;
    }

    /**
     * Get the Param elements
     * 
     * @return a ParamSet object
     */
    public ParamSet getParams() {
	if (params == null)
	    params = new ParamSet();
	return params;
    }
    
    /**
     * Set GROUP element set reference
     * @since VOTable 1.2
     * @param groups
     */
    public void setGroups(GroupSet groups) {
	this.groups = groups;
    }
    
    /**
     * Get GROUP element set reference
     * @since VOTable 1.2
     * @return GroupSet
     */
    public GroupSet getGroups() {
	if (groups == null)
	    groups = new GroupSet();
	return groups;
    }

    /**
     * Get RESOURCE set reference (FULL mode only)
     * 
     * @return ResourceSet (always NULL in SEQUENTIAL mode)
     */
    public ResourceSet getResources() {
	if (resources == null)
	    resources = new ResourceSet();
	return resources;
    }

    /**
     * Set RESOURCE set reference
     * 
     * @param resources
     */
    public void setResources(ResourceSet resources) {
	this.resources = resources;
    }
        
    /**
     * Set the InfosAtEnd elements
     * 
     * @param infosAtEnd
     * @since VOTable 1.2
     */
    public void setInfosAtEnd(InfoSet infosAtEnd) {
	this.infosAtEnd = infosAtEnd;
    }

    /**
     * Get the InfosAtEnd elements
     * 
     * @return a InfoSet object
     * @since VOTable 1.2
     */
    public InfoSet getInfosAtEnd() {
	if (infosAtEnd == null)
	    infosAtEnd = new InfoSet();
	return infosAtEnd;
    }
}
