/*
 * XMLConfiguredKeywordConfig.java
 * $ID$
 */

package dalserver.conf;

import dalserver.KeywordFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.File;

/**
 * a general purpose KeywordFactory that can be configured by an XML file
 */
public class XMLConfiguredKeywordFactory extends KeywordFactory {

    /**
     * configure the keyword factory from a given stream
     * @param xstrm   a stream set at the start of the XML configuration file.
     */
    public XMLConfiguredKeywordFactory(InputStream xstrm) 
        throws KeywordConfig.FormatException, IOException
    {
        super();
        loadKeywords(xstrm);
    }

    /**
     * configure the keyword factory from a given file
     * @param configfile   the file containing the XML definitions
     */
    public XMLConfiguredKeywordFactory(File configfile) 
        throws KeywordConfig.FormatException, IOException
    {
        this(new FileInputStream(configfile));
    }

    /**
     * load additional keywords in via a config file:
     * @param xstrm   a stream set at the start of the XML configuration file.
     */
    public void loadKeywords(InputStream xstrm) 
        throws KeywordConfig.FormatException, IOException
    {
        // TODO: extend this to accept as an input the KeywordConfig class
        // to use.  
        KeywordConfig kc = new XMLv1KeywordConfig(this);
        kc.load(xstrm);
    }

    /**
     * load additional keywords in via a config file:
     * @param configfile   the file containing the XML definitions
     */
    public void loadKeywords(File configfile) 
        throws KeywordConfig.FormatException, IOException
    {
        loadKeywords(new FileInputStream(configfile));
    }
}
