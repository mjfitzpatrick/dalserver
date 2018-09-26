//
// Copyright 2002-2011 - Universite de Strasbourg / Centre National de la
// Recherche Scientifique
// ------
//
// (Common) Table Model
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

package cds.table;

/**
 * <p>This class describes a common model for tables extracted from different
 * format sources (VOTable, Astrores, ...)</p>
 * @author Andre Schaaff
 * @version 3.0 Copyright CDS 2002-2010
 *  (kickoff 31 May 02)
 */

import java.util.Vector;

public class TableModel {

    // resource properties
    char[] resource_description; // Astrores
    char[] resource_name; // Astrores
    char[] resource_title;// Astrores
    @SuppressWarnings("unchecked")
    Vector resource_info; // Astrores
    @SuppressWarnings("unchecked")
    Vector resource_definitions; // Astrores

    // table properties
    char[] id;

    char[] description;
    char[] name;
    char[] title;
    char[] info;

    String[] properties;

    @SuppressWarnings("unchecked")
    Vector propertyNames = new Vector(); // in each element a table of property
					 // names ( char [] )
    @SuppressWarnings("unchecked")
    Vector propertyValues = new Vector(); // in each element a table of property
					  // values ( char [] )
    // Vector properties = new Vector();

    @SuppressWarnings("unchecked")
    Vector rows = new Vector(); // in each row a table of values ( char [] )

    // data about resource

    /**
   *
   */
    public TableModel() {
    }

    /**
     * Returns the Table properties (description, resource)
     * 
     * @return String[]
     */
    public String[] getTableProperties() {
	return properties;
    }

    /**
     * Sets the Table properties (description, resource)
     */
    public void setTableProperties(String[] properties) {
	this.properties = properties;
    }

    /**
     * 
     * @param propertyNames
     *            Vector
     * @param propertyValues
     *            Vector
     */
    @SuppressWarnings("unchecked")
    public void setTableProperties(Vector propertyNames, Vector propertyValues) {
	this.propertyNames = (Vector) propertyNames.clone();
	this.propertyValues = (Vector) propertyValues.clone();
    }

    /**
     * Returns a property value
     * 
     * @param tablePropName
     *            String
     * @return String
     */
    public String getTableProperty(String tablePropName) {
	return null;
    }

    /**
     * Sets a property value
     * 
     * @param tablePropName
     *            String
     * @param value
     *            String
     */
    public void setTableProperty(String tablePropName, String value) {
    }

    /**
     * Returns a row value of the table
     * 
     * @param index
     *            int
     * @return String[]
     */
    public String[] getRow(int index) {
	return (String[]) rows.elementAt(index);
    }

    /**
     * Adds a row to the table
     * 
     * @param values
     *            String[]
     */
    @SuppressWarnings("unchecked")
    public void addRow(String[] values) {
	// System.out.println("une de plus");
	rows.addElement(values);
    }

    /**
     * Adds a row to the table
     * 
     * @param values
     *            String[]
     */
    @SuppressWarnings("unchecked")
    public void addRow(Vector values) {
	// System.out.println("une de plus");
	rows.addElement(values.toArray());
    }

    /**
     * Sets (replace) a row value of the table
     * 
     * @param index
     *            int
     * @param values
     *            String[]
     */
    @SuppressWarnings("unchecked")
    public void setRow(int index, String[] values) {
	rows.setElementAt(values, index);
    }

    /**
     * Returns the value of a cell
     * 
     * @param row
     *            int
     * @param col
     *            int
     * @return String
     */
    public String getValueAt(int row, int col) {
	String[] localrow;
	localrow = (String[]) rows.elementAt(row);
	return localrow[col];
    }

    /**
     * Sets the value of a cell
     * 
     * @param row
     *            int
     * @param col
     *            int
     * @param value
     *            String
     */
    @SuppressWarnings("unchecked")
    public void setValueAt(int row, int col, String value) {
	String[] localrow;
	localrow = (String[]) rows.elementAt(row);
	localrow[col] = value;
	rows.setElementAt(localrow, row);
    }

    /**
     * Returns the property name list of a column (VOTable FIELD content for
     * example (name, ID, unit, description, UCD, ...)
     * 
     * @param col
     *            int
     * @return String[]
     */
    public String[] getProperties(int col) {
	return null;
    }

    /**
     * Sets the property name list of a column (VOTable FIELD content for
     * example (name, ID, unit, description, UCD, ...)
     * 
     * @param col
     *            int
     * @param values
     *            String[]
     */
    public void getProperties(int col, String[] values) {
    }

    /**
     * Returns the value of a given column
     * 
     * @param col
     *            int
     * @param propName
     *            String
     * @return String
     */
    public String getProperties(int col, String propName) {
	return null;
    }

    /**
     * Returns the value of a given column
     * 
     * @param col
     *            int
     * @param propName
     *            String
     * @param value
     *            String
     */
    public void setProperty(int col, String propName, String value) {
    }

    // Le tout serait accessible via une classe TableParser qui pourrait fournir
    // qq chose comme :

    // Object creation
    // TableParser pt = new TableParser(InputStream in);

    // Lancement de l'analyse
    // Table table[] = pt.parse();

    public void setDescription(String description) {
	this.description = description.toCharArray();
    }

    /**
     * 
     * @return String
     */
    public String getDescription() {
	return description.toString();
    }

    /**
     * 
     * @param id
     *            String
     */
    public void setId(String id) {
	this.id = id.toCharArray();
    }

    /**
     * 
     * @return String
     */
    public String getId() {
	return id.toString();
    }

    /**
     * 
     * @param name
     *            String
     */
    public void setName(String name) {
	this.name = name.toCharArray();
    }

    /**
     * 
     * @return String
     */
    public String getName() {
	return name.toString();
    }

    /**
     * 
     * @return int
     */
    public int getRowCount() {
	return rows.size();
    }

}
