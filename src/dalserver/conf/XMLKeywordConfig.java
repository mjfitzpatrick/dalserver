/*
 * XMLKeywordConfig.java
 * $ID$
 */

package dalserver.conf;

import dalserver.KeywordFactory;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;

/**
 * a class for configuring a KeywordFacotry with parameters and groups 
 * described in an XML file.
 * 
 * This class takes a KeywordFacotry instance to load keywords into (via 
 * the constructor) and an input stream that contains the XML description
 * (via the parse() funciton).  
 */
public abstract class XMLKeywordConfig extends KeywordConfig {

    protected XMLReader xrdr = null;

    /**
     * attach this configurator to a KeywordFactory.
     * @param kwf    the KeywordFactory to load parameters and groups into
     * @throws InternalError   if the internal XML parser cannot be created.
     */
    public XMLKeywordConfig(KeywordFactory kwf) {
        super(kwf);

        try {
            SAXParserFactory saxf = SAXParserFactory.newInstance();
            SAXParser saxp = saxf.newSAXParser();
            xrdr = saxp.getXMLReader();
        }
        catch (ParserConfigurationException ex) {
            throw new InternalError("Default JAXP parser config failed: " + 
                                    ex.getMessage());
        }
        catch (SAXException ex) {
            throw new InternalError("SAX error while creating default JAXP " + 
                                    "parser: " + ex.getMessage());
        }
    }

    /**
     * return a new instance of the SAX DefaultHandler.  This is called 
     * with each call to parse() to obtain a hanlder ready for a new 
     * configuration file.  This should be overridden to return a handler
     * that understands a particular XML format.  
     */
    protected abstract DefaultHandler newDefaultHandler();

    /**
     * load the definitions contained in the given XML stream
     * @param xstrm    the input stream set at the start of the XML 
     *                    configuration file.
     */
    public void parse(InputSource xstrm) throws IOException, SAXException {
        DefaultHandler dh = newDefaultHandler();
        xrdr.setContentHandler(dh);
        xrdr.setErrorHandler(dh);

        xrdr.parse(xstrm);
    }

    /**
     * load the definitions contained in the given input stream
     * @param strm   the input stream set at the start of the configuration
     *                   file
     */
    public void load(InputStream strm) 
        throws IOException, KeywordConfig.FormatException 
    {
        try {
            parse(new InputSource(strm));
        }
        catch (SAXException ex) {
            throw new KeywordConfig.FormatException("SAX: " + ex.getMessage());
        }
    }
}

