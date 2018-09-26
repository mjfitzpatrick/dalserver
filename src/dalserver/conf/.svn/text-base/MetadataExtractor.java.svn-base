/*
  * MetadataExtractor.java
  * $ID*
  */

package dalserver.conf;

import dalserver.KeywordFactory;
import dalserver.TableParam;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.*;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import nom.tam.fits.*;
//import uk.ac.starlink.table.*;

/**
 * MetadataExtractor extracts relevant metadata from supported data formats.
 */
public class MetadataExtractor {

    private static int FITS_FORMAT = 1;
    private static int VOTABLE_FORMAT = 2;
    private static int CSV_FORMAT = 3;
    private Hashtable<String, Properties> metadata;

    /** Constructor to generate a new metadata extractor **/
    public MetadataExtractor() {
	metadata = new Hashtable<String, Properties>();
    }    


    /**
     * Load an XML column description template file 
     */
    public XMLv1KeywordConfig load(String filename) throws IOException, KeywordConfig.FormatException {
	// Initialize column description file
	KeywordFactory kwf = new KeywordFactory();
	XMLv1KeywordConfig conf = new XMLv1KeywordConfig(kwf);
	conf.load(new File(filename));
        return conf;
    }


    /**
     * Read a FITS file and extract the metadata
     */
    public HashMap<String, String> readFits(String filename) {
	LinkedHashMap<String, String> headerVals = new LinkedHashMap<String, String>();
	try {
	    // Read the FITS headers
    	    Fits fits = new Fits(filename);
	    int hduCount = 0;
	    for (BasicHDU hdu : fits.read()) {
		// Use HDU number to select correct keyword...
		for (Iterator iter = hdu.getHeader().iterator(); iter.hasNext();) {
		    HeaderCard card = (HeaderCard) iter.next();
		    String key = card.getKey();
		    // Enumerate duplicate keywords 
		    if (headerVals.containsKey(key)) {
			key = key + "_" + Integer.toString(hduCount);
		    }
		    if (card.getValue() != null) {
		        headerVals.put(key, card.getValue());
		    } else {
			headerVals.put(key, "");
		    }
		}
		hduCount++;
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	return headerVals;
    } 


    /**
     * Read a VOTable file and extract the metadata
     */
    public HashMap<String, String> readVOTable(String filename) {
	LinkedHashMap<String, String> headerVals = new LinkedHashMap<String, String>();
	try {
	    // File handling details to go here...
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	return headerVals;
    } 


    /**
     * Read a CSV file and extract the metadata
     */
    public HashMap<String, String> readCSV(String filename) {
	LinkedHashMap<String, String> headerVals = new LinkedHashMap<String, String>();
	try {
	    // File handling details to go here...
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
	return headerVals;
    } 


    /**
     * Parse a hashtable of header keyword-value pairs to extract required service metadata
     */
    public void parseHeader(String filename, HashMap<String, String> headerVals, XMLv1KeywordConfig conf, int format, PluginFactory factory) {
	// Extract the necessary metadata according to the column description file
	Properties props = new Properties();
	try {
	    for (Iterator iter = conf.factory.idIterator(); iter.hasNext();) {
	        String value = "";
		String id = (String) iter.next();
		TableParam param = (TableParam) conf.factory.getKeyword(id);
		// Get parameter name
		String name = param.getName();
		if (name == null) name = param.getId();
		if (name == null) name = param.getUcd();
		// If no FITS keyword, use built-in algorithm (plug-in) to generate value; fail to ""
		if (format == FITS_FORMAT) {
		    String fitsKey = param.getFitsKeyword(); // Assume duplicated keywords end in "_HDU#"
		    if (fitsKey != null) {
			value = headerVals.get(fitsKey);
			// If no simple value returned, try plugin (derived value)
			if (value == null) value = factory.getValue(filename, name, headerVals);
		    } else if (factory != null) {
			value = factory.getValue(filename, name, headerVals);
		    }
		} else if (format == VOTABLE_FORMAT) {
		    value = headerVals.get(param.getUcd());
		    if (value == null) value = headerVals.get(param.getUtype());
		} else if (format == CSV_FORMAT) {
		    value = headerVals.get(param.getCsvKeyword());
		}
		if (value != null) {
		    props.setProperty(name.toUpperCase(), value);
		} else {
		    props.setProperty(name.toUpperCase(), "");
		}
	    }
            // Get any query data appended
            factory.appendQueryData(filename, props);
	    // Save the metadata for this file
	    metadata.put(filename, props);
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(1);
	}
    }	


    /**
     * Output the extracted metadata to the specified IO stream
     */
    public void output(OutputStream os) {
	try {
	    PrintWriter out = new PrintWriter(os);
	    boolean header = false;
	    for (String key: metadata.keySet()) {
		Properties props = (Properties) metadata.get(key);
		StringBuffer sbuf = new StringBuffer();
		if (!header) {
                    boolean first = true;
		    for (String prop: props.stringPropertyNames()) {
                        if (first) {
			    sbuf.append(prop);
                            first = false;
                        } else
			    sbuf.append("," + prop);
		    }
		    sbuf.append("\n");
		    header = true;
		}   
                boolean first = true;
		for (String prop: props.stringPropertyNames()) {
                    if (first) {
		        sbuf.append(props.getProperty(prop));
                            first = false;
                    } else
			    sbuf.append("," + props.getProperty(prop));
		}
		sbuf.append("\n");
		out.write(sbuf.toString());
	    }
	    out.flush();
	    out.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }


    /**
     * Run the metadata extractor on the current directory
     */
    public void run(String xmlFile, String directory, String outFile, String plugins) {
	int format = 0;
	HashMap<String, String> header = new HashMap<String, String>();
	PluginFactory factory = null;
	try {
	    // Get plugin factory class
	    if (!plugins.isEmpty()) { 
                try {
                    Class factoryClass = Class.forName(plugins);
                    factory = (PluginFactory) factoryClass.newInstance();
                } catch (ClassNotFoundException ex) {
                    System.err.println("Plugin not found: "+plugins);
                }
	    }
	    // Load column description file template
	    XMLv1KeywordConfig conf = load(xmlFile);
	    // Parse data files
	    for (String fname : new File(directory).list()) {
		fname = directory + File.separator + fname;
		// FITS file
		if (fname.endsWith(".fits")) {
		    header = readFits(fname);
		    format = MetadataExtractor.FITS_FORMAT;
		    // VOTable file
		} else if (fname.endsWith(".vot")) {	  
		    header = readVOTable(fname);
		    format = MetadataExtractor.VOTABLE_FORMAT;
		    // CSV file
		} else if (fname.endsWith(".csv")) {
		    header = readCSV(fname);
		    format = MetadataExtractor.CSV_FORMAT;
		}
		// Retrieve service metadata
		if (header.size() > 0) {
		    parseHeader(fname, header, conf, format, factory);
		    header.clear();
		}
	    }
	    // Output metadata
	    output(new FileOutputStream(new File(outFile)));
	} catch (Exception e) {
	    e.printStackTrace(System.err);
	}
    }


    // -------- Testing --------
    // java MetadataExtractor <xml template> <directory> <output file> <plugin class>
    public static void main(String[] args)
    {
        if (args.length > 0) {
	    MetadataExtractor extract = new MetadataExtractor();
            String plugin = "";
            if (args.length > 3) plugin = args[3];
	    extract.run(args[0], args[1], args[2], plugin);
        }
    }
}

