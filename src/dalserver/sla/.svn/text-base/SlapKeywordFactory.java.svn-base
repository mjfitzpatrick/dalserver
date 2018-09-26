/*
 * SlapKeywordFactory.java
 * $ID*
 */

package dalserver.sla;

import dalserver.conf.XMLConfiguredKeywordFactory;
import dalserver.conf.KeywordConfig;
import dalserver.RequestResponse;
import dalserver.KeywordFactory;
import dalserver.DalServerException;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;

/**
 * SlapKeywordFactory implements a factory class for well-known SLAP keywords,
 * including Groups, Params and Fields.  The use of a factory class frees
 * the client from having to know all the detailed metadata associated
 * with each type of keyword.  Keywords are indexed by both their ID and
 * UTYPE tags.  In general UTYPE is required to ensure uniqueness, but
 * within a limited scope, the ID tag may be sufficient to uniquely identify
 * a keyword without having to know the full UTYPE.
 *
 * <p>A list of the major SLAP keywords including the defined ID and UTYPE
 * keys is shown in <a href="doc-files/slap-metadata.html">this table</a>.
 *
 * @author	Ray Plante
 */
public class SlapKeywordFactory extends XMLConfiguredKeywordFactory {

    static String configfile = "slap-keywords.xml";

    /** Null constructor to generate a new SLAP keyword factory. */
    public SlapKeywordFactory() 
        throws KeywordConfig.FormatException, IOException
    {
	super(confResStream(configfile, SlapKeywordFactory.class));
    }

    /**
     * return an input stream to a resource for this class
     */
    public static InputStream confResStream(String conffile,Class wrtClass) {
        InputStream out =  wrtClass.getResourceAsStream(conffile);
        if (out == null) 
            throw new InternalError("Missing config resource: " + conffile);
        return out;
    }

    /**
     * Create a new SLAP keyword factory and initialize an associated
     * request response to process SLAP keywords.  This is not required,
     * but allows automated initialization of related context such as the      
     * keyword name space.  
     *
     * @param response	RequestResponse object to be linked to the SLAP
     *			keyword factory.
     */
    public SlapKeywordFactory(RequestResponse response)
	throws DalServerException, KeywordConfig.FormatException, IOException
    {

	// Create the keyword factory.
	this();

	// Set the response XML namespace for SLAP metadata.
	// TableParam xmlnsPar = this.newParam("XmlnsSlap", null);
	// response.setXmlns(xmlnsPar.getUtype(), xmlnsPar.getValue());
    }

    public static void main(String[] args) {
        try {
            KeywordFactory kwf = new SlapKeywordFactory();

            String outfile = "dictionary.html";
            if (args.length > 1) 
                outfile = args[1];

            kwf.printDoc(outfile, args[0], null);
        }
        catch (IOException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        catch (KeywordConfig.FormatException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }
}
