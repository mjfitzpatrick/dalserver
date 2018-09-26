package dalserver.conf;

import java.util.ArrayList;

/**
 * a simple container for FITS Header keywords.  Header values are accessed
 * given a HDU number and keyword name.  The primary HDU has a number equal to 
 * 0.  
 * 
 * This class is abstract to allow for different storage implementations, 
 * perhaps leveraging an underlying FITS library class.  For example, the 
 * NTFITSHeaderKeywords class wraps around a list of nom.tam.fits.Header 
 * instances.  
 */
public abstract class FITSHeaderKeywords {

    /**
     * return the number of HDUs this container had header values for
     */
    public abstract int getHDUCount();

    /**
     * return true if the keyword exists in the given HDU
     * @param hduIndex     the index of the HDU to look for the keyword in
     * @param keywordName  the name of the keyword of interest
     */
    public abstract boolean containsKey(int hduIndex, String keywordName);

    /**
     * return true if the keyword exists in any of the HDUs
     * @param keywordName  the name of the keyword of interest
     */
    public boolean containsKey(String keywordName) {
        for(int i=0; i < getHDUCount(); i++) {
            if (containsKey(i, keywordName)) return true;
        }
        return false;
    }

    /**
     * return the HDU indices which contain a value for the given keyword
     * @param keywordName  the name of the keyword of interest
     *
     * @return  int[]    an array of the HDU indices; a zero-length array is 
     *                      returned if the keyword is not found anywhere.
     */
    public int[] hdusWithKey(String keywordName) {
        ArrayList<Integer> hdus = new ArrayList<Integer>();
        for(int i=0; i < getHDUCount(); i++) {
            if (containsKey(i, keywordName)) hdus.add(i);
        }
        int[] out = new int[hdus.size()];
        for(int i=0; i < out.length; i++) out[i] = hdus.get(i);
        return out;
    }

    /**
     * return a string value for the requested header card.  If the value is 
     * not explicitly of a string type, the raw string value from the header 
     * will be returned.  
     * @param hduIndex      the index for the HDU to extract the value from,
     *                         where 0 is the primary (first) HDU.  
     * @param keywordName   the name of the header card to retrieve a value for
     * @param defval        a default value to return if the keyword is not found
     * @return String   a string rendering of the value or deval if the keyword
     *                     is not found.  
     */
    public abstract String getStringValue(int hduIndex, String keywordName,
                                          String defval);

    /**
     * return a string value for the requested header card.  If the value is 
     * not explicitly of a string type, the raw string value from the header 
     * will be returned.  
     * @param hduIndex      the index for the HDU to extract the value from,
     *                         where 0 is the primary (first) HDU.  
     * @param keywordName   the name of the header card to retrieve a value for
     * @return String   a string rendering of the value or null if the keyword
     *                     is not found.  
     */
    public String getStringValue(int hduIndex, String keywordName) {
        return getStringValue(hduIndex, keywordName, null);
    }

    /**
     * return an integer value for the requested header card.  
     * @param hduIndex      the index for the HDU to extract the value from,
     *                         where 0 is the primary (first) HDU.  
     * @param keywordName   the name of the header card to retrieve a value for
     * @param defval        the value to return if keyword cannot be found or 
     *                         is not of an integer type.
     * @return int  the header value or defval if an integer value does not exist
     */
    public abstract int getIntValue(int hduIndex, String keywordName, 
                                    int defval);

    /**
     * return an integer value for the requested header card.  
     *
     * @param hduIndex      the index for the HDU to extract the value from,
     *                         where 0 is the primary (first) HDU.  
     * @param keywordName   the name of the header card to retrieve a value for
     *
     * @return int   the header value or 0 if an integer value does not exist
     */
    public int getIntValue(int hduIndex, String keywordName) {
        return getIntValue(hduIndex, keywordName, 0);
    }

    /**
     * return a long value for the requested header card.  
     *
     * @param hduIndex      the index for the HDU to extract the value from,
     *                         where 0 is the primary (first) HDU.  
     * @param keywordName   the name of the header card to retrieve a value for
     * @param defval        the value to return if keyword cannot be found or 
     *                         is not of a long type.
     * @return long  the header value or defval if a long value does not exist
     */
    public abstract long getLongValue(int hduIndex, String keywordName, 
                                      long defval);

    /**
     * return a long value for the requested header card.  
     *
     * @param hduIndex      the index for the HDU to extract the value from,
     *                         where 0 is the primary (first) HDU.  
     * @param keywordName   the name of the header card to retrieve a value for
     *
     * @return long   the header value or 0L if a long value does not exist
     */
    public long getLongValue(int hduIndex, String keywordName) {
        return getLongValue(hduIndex, keywordName, 0L);
    }

    /**
     * return a floating-point value for the requested header card.  
     *
     * @param hduIndex      the index for the HDU to extract the value from,
     *                         where 0 is the primary (first) HDU.  
     * @param keywordName   the name of the header card to retrieve a value for
     * @param defval        the value to return if keyword cannot be found or 
     *                         is not of a floating-point type.
     *
     * @return double  the header value or defval if a real value does not exist
     */
    public abstract double getDoubleValue(int hduIndex, String keywordName, 
                                          double defval);

    /**
     * return a floating-point value for the requested header card.  
     *
     * @param hduIndex      the index for the HDU to extract the value from,
     *                         where 0 is the primary (first) HDU.  
     * @param keywordName   the name of the header card to retrieve a value for
     *
     * @return double   the header value or 0 if a real value does not exist
     */
    public double getDoubleValue(int hduIndex, String keywordName) {
        return getDoubleValue(hduIndex, keywordName, 0.0);
    }

    /**
     * return a boolean value for the requested header card.  
     * @param hduIndex      the index for the HDU to extract the value from,
     *                         where 0 is the primary (first) HDU.  
     * @param keywordName   the name of the header card to retrieve a value for
     * @param defval        the value to return if keyword cannot be found or 
     *                         is not of a boolean type.
     * @return boolean  the header value or defval if a boolean value does 
     *                         not exist
     */
    public abstract boolean getBooleanValue(int hduIndex, String keywordName, 
                                            boolean defval);

    /**
     * return a boolean value for the requested header card.  
     *
     * @param hduIndex      the index for the HDU to extract the value from,
     *                         where 0 is the primary (first) HDU.  
     * @param keywordName   the name of the header card to retrieve a value for
     * @return boolean   the header value or false if a boolean value does not 
     *                      exist
     */
    public boolean getBooleanValue(int hduIndex, String keywordName) {
        return getBooleanValue(hduIndex, keywordName, false);
    }


}
