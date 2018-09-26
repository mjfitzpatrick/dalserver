package dalserver.conf.spectrum;

import dalserver.conf.FITSHeaderKeywords;
import dalserver.conf.NTFITSHeaderKeywords;
import dalserver.conf.DataFormatException;
import dalserver.conf.image.ImageHeaderUtils;

import java.util.Arrays;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import FITSWCS.TrigD;

/**
 * this class can inspect and interpret the header keywords of a FITS 
 * Image file and provide secondary information about it.  
 */
public class SpectrumHeaderUtils extends ImageHeaderUtils {

    /** 
     * wrap a setup of FITS headers from a FITS file
     */
    public SpectrumHeaderUtils(FITSHeaderKeywords hdrdata) {
        super(hdrdata);
    }

    /**
     * wrap the FITS headers from a given file
     */
    public static SpectrumHeaderUtils loadFromFile(File fitsfile) 
        throws IOException, FileNotFoundException
    {
        return new SpectrumHeaderUtils(NTFITSHeaderKeywords.load(fitsfile));
    }

    /**
     * wrap the FITS headers from a stream
     */
    public static SpectrumHeaderUtils loadFromStream(InputStream fitsds) 
        throws IOException
    {
        return new SpectrumHeaderUtils(NTFITSHeaderKeywords.load(fitsds));
    }

    /** 
     * return the TargetPos values. Use RA_TARG and DEC_TARG. 
     */
    public double[] getTargetPos() throws DataFormatException {
      	double[] out = new double[2];
	out[0] = getPos("RA_TARG");
	out[1] = getPos("DEC_TARG");
	return out;
    }

    /** 
     * return the SpatialLocation values. Use RA and DEC. 
     */
    public double[] getSpatialLocation() throws DataFormatException {
      	double[] out = new double[2];
	out[0] = getPos("RA");
	out[1] = getPos("DEC");
	return out;
    }

    /**
     * return a position keyword value
     */
    public double getPos(String key) throws DataFormatException {
	double out = 0.0;
	String val = kws.getStringValue(0, key);  // assume in first HDU
        if (val == null) return out;
        val = val.trim();
        try {
            if (val.matches("^[+-](\\d\\d[: ]){2}\\d\\d(\\.\\d+)?$")) {
                out = sexg2dec(val);
            } else if (val.matches("^(\\d\\d[: ]){2}\\d\\d(\\.\\d+)?$")) {
                out = sexg2dec(val)*15.0;
	    } else {
		out = Double.parseDouble(val);
	    }
        } catch (NumberFormatException ex) {
            out = 0.0;
        }
	return out;
    }
}
