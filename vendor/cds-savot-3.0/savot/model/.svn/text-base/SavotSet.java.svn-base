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

import java.util.Vector;

/**
 * <p>
 * Generic class for other set classes
 * </p>
 * 
 * @author Andre Schaaff
 * @version 3.0 (kickoff 31 May 02)
 * @see SavotSet
 */
public class SavotSet {

    // storage of the set elements
    protected Vector<Object> set = null;

    /**
     * Constructor
     */
    public SavotSet() {
    }

    /**
     * Add an item to the set
     * 
     * @param item
     */
    public void addItem(Object item) {
	if (set == null) {
	    set = new Vector<Object>(1, 1);
	}
	set.add(item);
    }

    /**
     * Get an item at a given position (index)
     * 
     * @param index
     * @return Object
     */
    public Object getItemAt(int index) {
	if (set == null)
	    return null;
	if (index >= 0 && index < set.size())
	    return (Object) set.elementAt(index);
	else
	    return null;
    }

    /**
     * Remove an item at a given position (index)
     * 
     * @param index
     */
    public void removeItemAt(int index) {
	if (index >= 0 && index < set.size())
	    set.removeElementAt(index);
    }

    /**
     * Remove all items
     */
    public void removeAllItems() {
	if (set.size() > 0)
	    set.removeAllElements();
    }

    /**
     * Set the whole set to a given set
     * 
     * @param set
     */
    public void setItems(Vector<Object> set) {
	this.set = set;
    }

    /**
     * Get the whole set
     * 
     * @return a Vector
     */
    public Vector<Object> getItems() {
	return set;
    }

    /**
     * Get the number of items
     * 
     * @return int
     */
    public int getItemCount() {
	if (set != null)
	    return set.size();
	else
	    return 0;
    }
}
