/*
 * PluginFactory..java
 * $ID*
 */

package dalserver.conf;

import java.util.HashMap;
import java.util.Properties;

/**
 * PluginFactory provides methods to calculate specific values for particular metadata terms
 */
public interface PluginFactory {

    /**
     * Generic method to return the value of a metadata term based on the values of other metadata
     */
    public String getValue(String pathName, String param, HashMap headerVals);

   /**
    * appends the data used for performing queries, as needed.
    */
   public void appendQueryData(String pathName, Properties props);
}
