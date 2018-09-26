/*
 * DemoPluginFactory..java
 * $ID*
 */

package dalserver.conf;

import java.util.HashMap;
import java.util.Properties;

/**
 * DemoPluginFactory demonstrates how to calculate specific values for 
 * particular metadata terms
 */
public class DemoPluginFactory implements PluginFactory {

    /** Constructor to generate a new demonstration plugin factory */
    public DemoPluginFactory() {}

    /**
    * Generic method to return the value of a metadata term based on the values of other metadata
    */
    public String getValue(String pathName, String param, HashMap headerVals) {
	String value = "";
	if (param.equals("RA")) {
	    value =  "123.456";
	}
	return value;
    }

    /**
     * Generic method that appends any needed Query data.
     */
    public void appendQueryData(String pathName, Properties props) {
        if (!props.containsKey("RA")) {
            props.put("RA", getValue(pathName, "RA", null));
        }
    }

}
