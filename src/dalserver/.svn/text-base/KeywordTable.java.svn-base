/*
 * KeywordTable.java
 * $ID*
 */

package dalserver;

import java.io.*;
import java.util.*;

/**

 * KeywordTable implements a simple keyword-value keyword table.
 * This can be used, for example, to read a sequence of "keyword = value"
 * formatted lines from a text file or input stream, and subsequently
 * iterate through the resulting keyword table, or retrieve individual
 * keywords by name.
 * 
 * @version	1.0, 24-Sep-2013
 * @author	Doug Tody
 */
public class KeywordTable implements Iterable {
    /** The keyword hash table. */
    private LinkedHashMap<String,String> map;

    /** Null constructor to generate a new keyword factory. */
    public KeywordTable() {
	this.map = new LinkedHashMap<String,String>();
    }

    /**
     * Construct a new keyword dictionary from the contents of an input
     * string containing multiple lines of text.  Each line must contain
     * a single keyword-value pair in the format "keyword = value" where
     * value extends to the end of the line.  Blank lines and comment
     * lines are ignored.  Section context markers ("[<section>]") are 
     * ignored at present.
     *
     * @param	text	The text to be processed.
     */
    public KeywordTable(String text) {
	this.map = new LinkedHashMap<String,String>();

	String key, value;
	int ip;

	String lines[] = text.split("\n");
	for (String lbuf : lines) {
	    if (lbuf.startsWith("#"))
		continue;
	    if ((ip = lbuf.indexOf('=')) <= 0)
		continue;

	    key = lbuf.substring(0, ip - 1).trim();
	    value = lbuf.substring(ip + 1).trim();

	    map.put(key, value);
	}
    }

    /**
     * Construct a new keyword dictionary from the contents of the
     * given input stream.  Each line must contain a single keyword-
     * value pair in the format "keyword = value" where value extends
     * to the end of the line.  Blank lines and comments lines are
     * ignored.  Section context markers ("[<section>]") are ignored
     * at present.
     *
     * @param	reader	A BufferedReader for the stream to be read.
     */
    public KeywordTable(BufferedReader reader) {
	this.map = new LinkedHashMap<String,String>();

	String lbuf, key, value;
	int ip;

	try {
	    while ((lbuf = reader.readLine()) != null) {
		if (lbuf.startsWith("#"))
		    continue;
		if ((ip = lbuf.indexOf('=')) <= 0)
		    continue;

		key = lbuf.substring(0, ip - 1).trim();
		value = lbuf.substring(ip + 1).trim();

		map.put(key, value);
	    }
	} catch (Exception ex) {
	    ;
	}
    }

    /**
     * Add a new keyword-value pair to the dictionary.
     *
     * @param	name	The keyword name.
     * @param	value	The keyword value.
     */
    public void addKeyword(String name, String value) {
	map.put(name, value);
    }

    /**
     * Lookup a keyword in the dictionary.
     *
     * @param	name	The keyword name.
     */
    public String getKeyword(String name) {
	return (map.get(name));
    }

    /** Get the number of keywords in the table. */
    public int size() {
	return (map.size());
    }

    /** Get a list iterator for the keyword list. */
    public Iterator iterator() {
	return (map.entrySet().iterator());
    }

    /** Get an entrySet to access the keyword list as a Collection. */
    public Set<Map.Entry<String,String>> entrySet() {
	return (map.entrySet());
    }
}

