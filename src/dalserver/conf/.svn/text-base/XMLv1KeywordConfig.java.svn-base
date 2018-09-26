/*
 * XMLKeywordConfig.java
 * $ID$
 * 
 * TODO: enforce unique IDs
 */

package dalserver.conf;

import dalserver.KeywordFactory;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Stack;
import java.util.Properties;
import java.util.LinkedList;
import java.util.ListIterator;
import java.io.File;
import java.io.StringReader;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * the parser class for our so-called version 1 of the XML keyword definition
 * file.
 */
public class XMLv1KeywordConfig extends XMLKeywordConfig {

    /**
     * attach this configurator to a KeywordFactory.
     * @param kwf    the KeywordFactory to load parameters and groups into
     * @throws InternalError   if the internal XML parser cannot be created.
     */
    public XMLv1KeywordConfig(KeywordFactory kwf) {
        super(kwf);
    }

    /**
     * return a new instance of the SAX DefaultHandler.  This is called 
     * with each call to parse() to obtain a hanlder ready for a new 
     * configuration file.  This should be overridden to return a handler
     * that understands a particular XML format.  
     */
    protected DefaultHandler newDefaultHandler() {
        return new Handler();
    }

    public void loadParam(Properties metadata) 
        throws KeywordConfig.FormatException 
    {
        String utype = metadata.getProperty("utype");
        if (utype != null && ! utype.contains(":")) {
            String prefix = metadata.getProperty("prefix");
            if (prefix != null)
                utype = prefix + ":" + utype;
        }
        String id = metadata.getProperty("id");
        if (id == null) 
            id = metadata.getProperty("name");
            
        String name = metadata.getProperty("name");
        if (name == null) 
            name = metadata.getProperty("id");
        if (name == null) 
            name = utype;

        factory.addParam(name, 
                         metadata.getProperty("default"),
                         id,
                         metadata.getProperty("groupid"),
                         metadata.getProperty("datatype"),
                         metadata.getProperty("arraysize"),
                         metadata.getProperty("unit"),
                         utype,
                         metadata.getProperty("ucd"),
                         metadata.getProperty("description"),
                         metadata.getProperty("fits"),
                         metadata.getProperty("cvs"),
                         metadata.getProperty("hint"));
    }

    public void loadGroup(Properties metadata) 
        throws KeywordConfig.FormatException 
    {
        String utype = metadata.getProperty("utype");
        if (utype != null && ! utype.contains(":")) {
            String prefix = metadata.getProperty("prefix");
            if (prefix != null)
                utype = prefix + ":" + utype;
        }
        String id = metadata.getProperty("id");
        if (id == null) 
            id = metadata.getProperty("name");
            
        String name = metadata.getProperty("name");
        if (name == null) 
            name = metadata.getProperty("id");
        if (name == null) 
            name = utype;

        factory.addGroup(name, id, 
                         metadata.getProperty("groupid"),
                         utype,
                         metadata.getProperty("ucd"),
                         metadata.getProperty("description"),
                         metadata.getProperty("hint"));
    }

    protected class Handler extends DefaultHandler {

        Stack<Properties> groups = new Stack<Properties>();
        Properties current = null;
        Properties defprefix = null;
        LinkedList<String> path = new LinkedList<String>();
        StringBuffer chdata = null;
        int nextAnonGrpN = 0;

        public void handleSummary(String val) throws SAXException {
            // parse the summary contents
            BufferedReader data = new BufferedReader(new StringReader(val));
            String line = null, pname = null, pval = null;
            int col = -1;
            try {
                while ((line = data.readLine()) != null) {
                    line = line.trim();
                    if (line.length() == 0) break;
                    col = line.indexOf(':');
                    if (col < 1) 
                        syntaxError("Bad summary syntax (no colon): " + line);
                    pname = line.substring(0, col).trim();
                    pval = line.substring(col+1).trim();
                    if (pname.length() == 0) 
                        syntaxError("Bad summary syntax (no property name): " +
                                    line);
                    if (pval.length() > 0)
                        handleMetadatum(pname, pval, false);
                }
            }
            catch (IOException ex) {
                // should not happen
                throw new InternalError("Prog err: IOException on string read: "
                                        + ex.getMessage());
            }
        }

        public void handleMetadatum(String name, String val, boolean override) 
            throws SAXException
        {
            if (! override && current.containsKey(name))
                return;

            BufferedReader data = new BufferedReader(new StringReader(val));
            try {
                String line = null;
                StringBuffer out = new StringBuffer();
                while ((line = data.readLine()) != null) {
                    line = line.trim();
                    if (line.length() == 0) continue;
                    out.append(line).append(' ');
                }
                val = out.toString().trim();
            }
            catch (IOException ex) {
                // should not happen
                throw new InternalError("Prog err: IOException on string read: "
                                        + ex.getMessage());
            }
            current.setProperty(name, val);
        }

        public void startElement(String uri, String localName, String qName, 
                                 Attributes atts)
            throws SAXException
        {
            descendPath(qName);
            String parent = parent();

            if (qName.equals("param")) {
                // check for proper syntax
                if (parent == null)
                    syntaxError("illegal root node found: "+ qName);
                if (! parent.equals("keywords") && ! parent.equals("group"))
                    syntaxError("<"+parent+"> should not contain <param>");

                // create container of metadata for new parameter
                current = new Properties(defprefix);
                String prefix = atts.getValue("nsprefix");
                if (prefix != null)  current.setProperty("prefix", prefix);

                // if this param is part of a group, we need to make sure
                // that group has an id value we can use with this param.
                if (! groups.empty() && groups.peek().getProperty("id") == null)
                    setDefaultGroupId(groups.peek());
            }

            else if (qName.equals("group")) {
                // check for proper syntax
                if (parent == null)
                    syntaxError("illegal root node found: "+ qName);
                if (! parent.equals("keywords") && ! parent.equals("group"))
                    syntaxError("<"+parent+"> should not contain <group>");

                // create container of metadata for new group
                current = new Properties();
                groups.push(current);
                String prefix = atts.getValue("nsprefix");
                if (prefix != null)  current.setProperty("prefix", prefix);
            }

            else if (qName.equals("summary")) {
                // check for proper syntax
                if (parent == null)
                    syntaxError("illegal root node found: "+ qName);
                if (! parent.equals("param") && ! parent.equals("group"))
                    syntaxError("<"+parent+"> should not contain <summary>");

                // prepare to get the element contents.  SAX will give us the 
                // contents in chunks, so we will gather it up in a buffer
                chdata = new StringBuffer();
            }

            else if (qName.equals("keywords")) {
                // check for proper syntax
                if (depth() != 1)
                    syntaxError("<keywords> should only appear as a root " + 
                                "element");

                // grab the default prefix if given; save it to a default 
                // Properties that will be used by each group Properties 
                // we encounter later.  
                String prefix = atts.getValue("nsprefix");
                if (prefix != null)  {
                    defprefix = new Properties();
                    defprefix.setProperty("prefix", prefix);
                }
            }

            else {
                // check for proper syntax
                if (parent == null)
                    syntaxError("illegal root node found: "+ qName);
                if (! parent.equals("param") && ! parent.equals("group"))
                    syntaxError("unrecognized element name (in this position): "
                                + qName);

                // prepare to get the element contents.  SAX will give us the 
                // contents in chunks, so we will gather it up in a buffer
                chdata = new StringBuffer();
            }
        }

        public void endElement(String uri, String localName, String qName) 
            throws SAXException
        {
            if (qName.equals("summary")) {
                // we have now gathered up the contents into the chdata
                // StringBuffer; now process it.
                String data = chdata.toString().trim();
                chdata = null;
                handleSummary(data);
            }
            else if (qName.equals("param")) {
                if (! groups.empty()) {
                    String gid = groups.peek().getProperty("id");
                    if (gid != null) 
                        current.setProperty("groupid", gid);
                    String prefix = groups.peek().getProperty("prefix");
                    if (prefix != null) current.setProperty("prefix", prefix);
                }
                try {
                    loadParam(current);
                }
                catch (KeywordConfig.FormatException ex) {
                    throw new SAXException("Content Error: " + ex.getMessage());
                }
                current = null;
            }
            else if (qName.equals("group")) {
                if (! groups.empty()) {
                    current = groups.pop();
                    String gid = current.getProperty("id");
                    if (gid != null) 
                        current.setProperty("groupid", gid);

                    // inherit the prefix, if necessary and available
                }
                try {
                    loadGroup(current);
                }
                catch (KeywordConfig.FormatException ex) {
                    throw new SAXException("Content Error: " + ex.getMessage());
                }
                current = null;
            }
            else if (qName.equals("keywords")) {
                defprefix = null;
            }
            else if (chdata != null) {
                // This is an arbitrary metadatum name. 
                // We have now gathered up the contents of the element into 
                // the chdata StringBuffer; now process it.
                handleMetadatum(qName, chdata.toString().trim(), true);
                chdata = null;
            }
            ascendPath();
        }

        public void characters(char[] ch, int start, int length) 
            throws SAXException
        {
            if (chdata != null) 
                chdata.append(ch, start, length);
            else {
                String data = new String(ch, start, length).trim();
                if (data.length() > 0) {
                    if (depth() == 0)
                        syntaxError("Loose text before root node found: " +
                                    data + "...");
                    else 
                        syntaxError("<"+element()+
                                    "> must not contain simple text: " + data);
                }
            }
        }

        private void syntaxError(String msg) throws SAXException {
            throw new SAXException(msg);
        }

        private void descendPath(String elname) {
            path.addFirst(elname);
        }
        private void ascendPath() {
            path.remove(0);
        }
        private int depth() {
            return path.size();
        }
        private String parent() {
            if (depth() < 2) 
                return null;
            else
                return path.get(1);
        }
        private String element() {
            if (depth() == 0) 
                return null;
            else
                return path.getFirst();
        }

        String currentPath() {
            StringBuffer out = new StringBuffer();
            ListIterator i = null;
            for (i = path.listIterator(path.size()); i.hasPrevious();) {
                out.append('/').append(i.previous());
            }
            return out.toString();
        }

        private void setDefaultGroupId(Properties group) {
            // try setting the id to the same as name
            String id = group.getProperty("name");

            // if name is not set, try setting it to the utype value
            if (id == null) {
                String utype = group.getProperty("utype");
                if (utype != null && ! utype.contains(":")) {
                    String prefix = group.getProperty("prefix");
                    if (prefix != null)
                        utype = prefix + ":" + utype;
                }
            }

            // if all else fails create an "anonymous" one
            if (id == null)
                id = generateDefaultGroupId();

            group.setProperty("id", id);
        }
        private String generateDefaultGroupId() {
            return "grp" + Integer.toString(nextAnonGrpN++);
        }

    }

    public static void main(String[] args) {
        KeywordFactory kwf = new KeywordFactory();
        KeywordConfig kconfig = new XMLv1KeywordConfig(kwf);
        try {
            kconfig.load(new File(args[0]));
        }
        catch (IOException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        catch (KeywordConfig.FormatException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        kwf.printDoc("dictionary.html", args[0], null);
    }
}
