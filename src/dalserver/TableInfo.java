/*
 * TableInfo.java
 * $ID*
 */

package dalserver;

import cds.savot.model.*;

/**
 * The TableInfo class is used to add an INFO element to a RequestResponse
 * object (in the VOTable serialization the INFO will be written to the
 * RESOURCE element, not to the table itself).
 *
 * @version	1.0, 11-Dec-2006
 * @author	Doug Tody
 */
public class TableInfo extends SavotInfo {
    // Null constructor.
    public TableInfo() { }

    /**
     * The usual case of a keyword=value INFO.
     *
     * @param	name	The INFO name, e.g., "QUERY_STATUS".
     * @param	value	The INFO value.
     */
    public TableInfo(String name, String value) {
	super.setName(name);
	super.setValue(value);
    }
}
