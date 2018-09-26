package dalserver.conf;

import java.util.ArrayList;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;

import nom.tam.fits.*;
import nom.tam.util.BufferedDataInputStream;

/**
 * a {@link dalserver.conf.FITSHeaderKeywords FITSHeaderKeywords} container 
 * implemented on top of the nom.tam.fits.Header class.  
 *
 * It is expected that a nom.tam.fits-based FITS reader will populated this 
 * container by calling the {@link #addHeader(Header)} function for each HDU
 * in the file and in the order that they appear in the file. 
 */
public class NTFITSHeaderKeywords extends FITSHeaderKeywords {

    private ArrayList<Header> data = new ArrayList<Header>();

    /**
     * initialize an empty container
     */
    public NTFITSHeaderKeywords() { }

    /**
     * load all HDUs from a FITS file
     * @throws FileNotFoundException  if the file does not exist
     * @throws IOException  if there was trouble reading or parsing the FITS file
     */
    public static NTFITSHeaderKeywords load(File fitsfile) 
        throws IOException, FileNotFoundException
    {
        if (! fitsfile.exists())
            throw new FileNotFoundException(fitsfile.toString());

        NTFITSHeaderKeywords hdrs = new NTFITSHeaderKeywords();
        try {
            Fits f = new Fits(fitsfile);
            for(BasicHDU hdu : f.read()) 
                hdrs.addHeader(hdu.getHeader());
            return hdrs;
        }
        catch (FitsException ex) {
            throw new IOException("Trouble reading FITS file, " + fitsfile +
                                  ": "+ex.getMessage(), ex);
        }
    }

    /**
     * load all HDUs from a FITS file
     * @throws IOException  if there was trouble reading or parsing the FITS file
     */
    public static NTFITSHeaderKeywords load(InputStream fitsds) 
        throws IOException
    {
        NTFITSHeaderKeywords hdrs = new NTFITSHeaderKeywords();
        try {
            Fits f = new Fits(new BufferedDataInputStream(fitsds));
            for(BasicHDU hdu : f.read()) 
                hdrs.addHeader(hdu.getHeader());
            return hdrs;
        }
        catch (FitsException ex) {
            throw new IOException("Trouble reading FITS file from stream: " + 
                                  ex.getMessage(), ex);
        }
    }

    /**
     * add an HDU Header
     * @param hdr   the HDU header to add.
     */
    public void addHeader(Header hdr) {  data.add(hdr); }

    /**
     * return the number of HDUs this container had header values for
     */
    public int getHDUCount() { return data.size(); }

    /**
     * return true if the keyword exists in the given HDU
     * @param hduIndex     the index of the HDU to look for the keyword in
     * @param keywordName  the name of the keyword of interest
     */
    public boolean containsKey(int hduIndex, String keywordName) {
        if (hduIndex >= data.size()) return false;
        return data.get(hduIndex).containsKey(keywordName);
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
    public String getStringValue(int hduIndex, String keywordName, 
                                 String defval) 
    {
        if (hduIndex >= data.size()) return defval;
        String out = data.get(hduIndex).getStringValue(keywordName);
        if (out == null) {
            HeaderCard hc = data.get(hduIndex).findCard(keywordName);
            out = (hc != null) ? hc.getValue() : defval;
        }
        return out;
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
    public int getIntValue(int hduIndex, String keywordName, int defval) {
        if (hduIndex >= data.size()) return defval;
        return data.get(hduIndex).getIntValue(keywordName, defval);
    }

    /**
     * return a long value for the requested header card.  
     * @param hduIndex      the index for the HDU to extract the value from,
     *                         where 0 is the primary (first) HDU.  
     * @param keywordName   the name of the header card to retrieve a value for
     * @param defval        the value to return if keyword cannot be found or 
     *                         is not of a long type.
     * @return long  the header value or defval if a long value does not exist
     */
    public long getLongValue(int hduIndex, String keywordName, long defval) {
        if (hduIndex >= data.size()) return defval;
        return data.get(hduIndex).getLongValue(keywordName, defval);
    }

    /**
     * return a floating-point value for the requested header card.  
     * @param hduIndex      the index for the HDU to extract the value from,
     *                         where 0 is the primary (first) HDU.  
     * @param keywordName   the name of the header card to retrieve a value for
     * @param defval        the value to return if keyword cannot be found or 
     *                         is not of a floating-point type.
     * @return double  the header value or defval if a real value does not exist
     */
    public double getDoubleValue(int hduIndex, String keywordName, 
                                 double defval) 
    {
        if (hduIndex >= data.size()) return defval;
        return data.get(hduIndex).getDoubleValue(keywordName, defval);
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
    public boolean getBooleanValue(int hduIndex, String keywordName, 
                                   boolean defval) 
    {
        if (hduIndex >= data.size()) return defval;
        return data.get(hduIndex).getBooleanValue(keywordName, defval);
    }
}
