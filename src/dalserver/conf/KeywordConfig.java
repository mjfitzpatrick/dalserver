/*
 * KeywordConfig.java
 * $ID$
 */

package dalserver.conf;

import dalserver.KeywordFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.File;

/**
 * a class for configuring a KeywordFacotry with parameters and groups 
 * described in an XML file.
 * 
 * This class takes a KeywordFacotry instance to load keywords into (via 
 * the constructor) and an input stream that contains the XML description
 * (via the parse() funciton).  
 */
public abstract class KeywordConfig {

    /**
     * the KeywordFactory that will be loaded with parameter and group 
     * definitions.
     */
    protected KeywordFactory factory = null;

    /**
     * attach this configurator to a KeywordFactory.
     * @param kwf    the KeywordFactory to load parameters and groups into
     */
    public KeywordConfig(KeywordFactory kwf) {
        factory = kwf;
    }

    /**
     * load the definitions contained in the given input stream
     * @param strm   the input stream set at the start of the configuration
     *                   file
     */
    public abstract void load(InputStream strm) 
        throws IOException, FormatException;

    /**
     * load the definitions contained in the given input stream
     * @param configfile   the file containing the definitions
     */
    public void load(File configfile) throws IOException, FormatException {
        load(new FileInputStream(configfile));
    }

    /**
     * an exception indicating that the configuration file includes illegal
     * or otherwise unparsable content for its assumed format.  This would 
     * include syntax errors and data corruption errors (that do not 
     * trigger IOExceptions).  
     */
    public static class FormatException extends Exception {

        /**
         * create the exception with a given explanation.
         */
        public FormatException(String msg) {  super(msg); }
    }
}

