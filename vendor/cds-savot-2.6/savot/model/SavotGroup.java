//
// ------
//
// SAVOT Data Model
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
//

package cds.savot.model;

/**
* <p>Group element </p>
* @author Andre Schaaff
* @version 2.6 Copyright CDS 2002-2005
*  (kickoff 31 May 02)
*/
public class SavotGroup extends MarkupComment implements SimpleTypes {
  // ID attribute
  protected char[] id = null;

  // name attribute
  protected char[] name = null;

  // ref attribute
  protected char[] ref = null;

  // ucd attribute
  protected char[] ucd = null;

  // utype attribute
  protected char[] utype = null;

  // description element
  protected char[] description = null;

  // FIELDRef elements
  protected FieldRefSet fieldsref = null;

  // PARAM elements
  protected ParamSet params = null;

  // PARAMRef elements
  protected ParamRefSet paramsref = null;

  // GROUP elements
  protected GroupSet groups = null;

  /**
   * Constructor
  */
  public SavotGroup() {
  }

  /**
   * Set ID attribute
   * @param id
   */
  public void setId(String id) {
    if (id != null)
      this.id = id.toCharArray();
  }

  /**
   * Get id attribute
   * @return String
   */
  public String getId() {
    if (id != null)
      return String.valueOf(id);
    else return "";
  }

  /**
   * Set name attribute
   * @param name
   */
  public void setName(String name) {
    if (name != null)
      this.name = name.toCharArray();
  }

  /**
   * Get name attribute
   * @return String
   */
  public String getName() {
    if (name != null)
      return String.valueOf(name);
    else return "";
  }

  /**
   * Set ref attribute
   * @param ref
   */
  public void setRef(String ref) {
    if (ref != null)
      this.ref = ref.toCharArray();
  }

  /**
   * Get ref attribute
   * @return String
   */
  public String getRef() {
    if (ref != null)
      return String.valueOf(ref);
    else return "";
  }

  /**
   * Set ucd attribute
   * @param ucd ([A-Za-z0-9_.,-]*)
   */
  public void setUcd(String ucd) {
    if (ucd != null)
      this.ucd = ucd.toCharArray();
  }

  /**
   * Get ucd attribute
   * @return String
   */
  public String getUcd() {
    if (ucd != null)
      return String.valueOf(ucd);
    else return "";
  }

  /**
   * Set utype attribute
   * @param utype
   */
  public void setUtype(String utype) {
    if (utype != null)
      this.utype = utype.toCharArray();
  }

  /**
   * Get utype attribute
   * @return String
   */
  public String getUtype() {
    if (utype != null)
      return String.valueOf(utype);
    else return "";
  }

  /**
  * Set DESCRIPTION element content
  * @param description
  */
  public void setDescription(String description) {
    if (description != null)
      this.description = description.toCharArray();
  }

  /**
  * Get DESCRIPTION element content
  * @return String
  */
  public String getDescription() {
    if (description != null)
      return String.valueOf(description);
    else return "";
  }

  /**
  * Get PARAM elements set reference
  * @return ParamSet
  */
  public ParamSet getParams() {
    if (params == null)
      params = new ParamSet();
    return params;
  }

  /**
   * Set PARAM elements set reference
   * @param params
  */
  public void setParams(ParamSet params) {
    this.params = params;
  }

  /**
   * Set PARAMref elements set reference
   * @param paramsref
  */
  public void setParamsRef(ParamRefSet paramsref) {
    this.paramsref = paramsref;
  }

  /**
  * Get PARAMref elements set reference
  * @return ParamRefSet
  */
  public ParamRefSet getParamsRef() {
    if (paramsref == null)
      paramsref = new ParamRefSet();
    return paramsref;
  }

  /**
  * Get FIELDref elements set reference
  * @return FieldRefSet
  */
  public FieldRefSet getFieldsRef() {
    if (fieldsref == null)
      fieldsref = new FieldRefSet();
    return fieldsref;
  }

  /**
   * Set FIELDref elements set reference
   * @param fieldsref
  */
  public void setFieldsRef(FieldRefSet fieldsref) {
    this.fieldsref = fieldsref;
  }

  /**
  * Get GROUP elements set reference
  * @return GroupSet
  */
  public GroupSet getGroups() {
    if (groups == null)
      groups = new GroupSet();
    return groups;
  }

  /**
   * Set GROUP elements set reference
   * @param groups
  */
  public void setGroups(GroupSet groups) {
    this.groups = groups;
  }
}
