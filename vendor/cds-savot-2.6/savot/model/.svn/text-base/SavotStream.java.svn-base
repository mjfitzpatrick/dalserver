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
* <p>Stream element </p>
* @author Andre Schaaff
* @version 2.6 Copyright CDS 2002-2005
*  (kickoff 31 May 02)
*/
public class SavotStream extends MarkupComment implements SimpleTypes {

  // content
  protected char[] content = null;

  // type attribute (locator, other)
  protected char[] type = "locator".toCharArray(); // default

  // href attribute
  protected char[] href = null;

  // actuate attribute
  protected char[] actuate = null;

  // width encoding
  protected char[] encoding = null;

  // expires attribute
  protected char[] expires = null;

  // rights attribute
  protected char[] rights = null;

  /**
   * Constructor
  */
  public SavotStream() {
  }

  /**
   * Set type attribute
   * @param type (locator, other)
   */
  public void setType(String type) {
    if (type != null)
      this.type = type.toCharArray();
  }

  /**
   * Get type attribute
   * @return String
   */
  public String getType() {
    if (type != null)
      return String.valueOf(type);
    else return "";
  }

  /**
   * Set href attribute
   * @param href (URI)
   */
  public void setHref(String href) {
    if (href != null)
      this.href = href.toCharArray();
  }

  /**
   * Get href attribute
   * @return String
   */
  public String getHref() {
    if (href != null)
      return String.valueOf(href);
    else return "";
  }

  /**
   * Set actuate attribute
   * @param actuate (onLoad, onRequest, other, none)
   */
  public void setActuate(String actuate) {
    if (actuate != null)
      this.actuate = actuate.toCharArray();
  }

  /**
   * Get actuate attribute
   * @return String
   */
  public String getActuate() {
    if (actuate != null)
      return String.valueOf(actuate);
    else return "";
  }

  /**
   * Set encoding attribute
   * @param encoding (gzip, base64, dynamic, none)
   */
  public void setEncoding(String encoding) {
    if (encoding != null)
      this.encoding = encoding.toCharArray();
  }

  /**
   * Get encoding attribute
   * @return String
   */
  public String getEncoding() {
    if (encoding != null)
      return String.valueOf(encoding);
    else return "";
  }

  /**
   * Set expires attribute
   * @param expires
   */
  public void setExpires(String expires) {
    if (expires != null)
      this.expires = expires.toCharArray();
  }

  /**
   * Get width attribute
   * @return String
   */
  public String getExpires() {
    if (expires != null)
      return String.valueOf(expires);
    else return "";
  }

  /**
   * Set rights attribute
   * @param rights
   */
  public void setRights(String rights) {
    if (rights != null)
      this.rights = rights.toCharArray();
  }

  /**
   * Get rights attribute
   * @return String
   */
  public String getRights() {
    if (rights != null)
      return String.valueOf(rights);
    else return "";
  }

  /**
   * Set content
   * @param content
   */
  public void setContent(String content) {
    if (content != null)
      this.content = content.toCharArray();
  }

  /**
   * Get content
   * @return String
   */
  public String getContent() {
    if (content != null)
      return String.valueOf(content);
    else return "";
  }
}
