package dalserver.conf.image;

import dalserver.conf.FITSHeaderKeywords;
import dalserver.conf.NTFITSHeaderKeywords;
import dalserver.conf.DataFormatException;

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
public class ImageHeaderUtils {

    public FITSHeaderKeywords kws = null;

    enum ImageType {
        /**
         * image type indicating an unknown convention
         */
        UNRECOGNIZED, 
        
        /** 
         * image type indicating simple, single-HDU image data.  
         * The primary HDU contains the image data.
         */
        SIMPLE,

        /**
         * image type indicating a single array not contained in 
         * the primary HDU
         */
        EXTENSION, 

        /**
         * image type indicating a stack of images, where the
         * first plane represents the flux measurement and other 
         * planes measure ancillary data (e.g. error)
         */
        RICH_STACK,

        /**
         * image type indicating a MEF where the first extension 
         * HDU represents the flux measurement and other 
         * extension HDUs measure ancillary data (e.g. error)
         */
        RICH_MEF,

        /**
         * image type indicating a stack of CCDs in the primary HDU
         * where each plane contains a different CCD on the focal 
         * plane.  An extension table may give WCS of each plane.
         * (This is WFPC2 convention for unmosaiced images.)
         */ 
        CCD_STACK,

        /**
         * image type indicating a set of CCDs with each CCD in a 
         * separate extension MEF (empty primary) 
         */
        CCD_MEF
    };


    /** 
     * wrap a setup of FITS headers from a FITS file
     */
    public ImageHeaderUtils(FITSHeaderKeywords hdrdata) {
        kws = hdrdata;
    }

    /**
     * wrap the FITS headers from a given file
     */
    public static ImageHeaderUtils loadFromFile(File fitsfile) 
        throws IOException, FileNotFoundException
    {
        return new ImageHeaderUtils(NTFITSHeaderKeywords.load(fitsfile));
    }

    /**
     * wrap the FITS headers from a stream
     */
    public static ImageHeaderUtils loadFromStream(InputStream fitsds) 
        throws IOException
    {
        return new ImageHeaderUtils(NTFITSHeaderKeywords.load(fitsds));
    }

    /**
     * return the 1-based indices of the spatial axes in a given HDU.  Should 
     * the HDU contain more than one longitude or latitude axis, the first one 
     * encounter will be consider the canonical one.  
     * @param hduIndex   the 0-based index of the HDU of interest; the index of
     *                     the primary HDU is 0.  
     * @return int[2]  where the first element is the index of the longitude 
     *                 axis and the second is the index of the latitude axis.
     *                 if an axis is not represented in the HDU, the index will 
     *                 be 0.  If the HDU does not describe an image, null is 
     *                 returned.  
     */
    public int[] findSpatialAxes(int hduIndex) {
        if (! isImageHDU(hduIndex)) return null;
        int nax = getNaxes(hduIndex);
        int[] out = new int[]{ 0, 0 };

        for(int i=1; i <= nax; i++) {
            if (isLongAxis(hduIndex, i) && out[0] == 0) out[0] = i;
            if (isLatAxis(hduIndex, i) && out[1] == 0) out[1] = i;
        }
        return out;
    }

    /**
     * return the number of axes in the HDU.  Zero is returned if the HDU does 
     * not have an NAXIS keyword (indicating that it is not an image).  
     */
    public int getNaxes() {
        return getNaxes(infoHDU());
    }

    /**
     * return the number of axes in the HDU.  Zero is returned if the HDU does 
     * not have an NAXIS keyword (indicating that it is not an image).  
     */
    public int getNaxes(int hduIndex) {
        return kws.getIntValue(hduIndex, "NAXIS", 0);
    }

    /**
     * return true if the HDU contains an image
     */
    public boolean isImageHDU(int hduIndex) {
        if (getNaxes(hduIndex) == 0) return false;
        if (kws.getBooleanValue(hduIndex, "SIMPLE")) 
            return (getNaxis(hduIndex)[0] != 0);

        String s = kws.getStringValue(hduIndex, "XTENSION");
        if (s != null) {
            if (s.trim().equals("IMAGE") || s.trim().equals("IUEIMAGE")) 
                return true;
        }

        return false;
    }

    /**
     * return true if the HDU is a dummy HDU, containing no data.  This 
     * implementatoin returns true if the HDU is an ImageHDU but has NAXIS=0.
     */
    public boolean isDummyHDU(int hduIndex) {
        return (getNaxes(hduIndex) == 0);
    }

    /**
     * return true if the HDU is a dummy HDU, containing no data.  This 
     * implementatoin returns true if the HDU is an ImageHDU but has NAXIS=0.
     */
    public boolean isTableHDU(int hduIndex) {
        return ("TABLE".equals(kws.getStringValue(hduIndex, "XTENSION")));
    }

    /**
     * return true if the specified axis of an HDU is a longitude axis of some
     * type.  
     * @param hduIndex   the 0-based index of the HDU being inspected; the 
     *                      primary HDU has an index of 0.
     * @param axisIndex  the 1-based index of the axis of interest
     */
    public boolean isLongAxis(int hduIndex, int axisIndex) {
        String type = kws.getStringValue(hduIndex, 
                                         "CTYPE"+Integer.toString(axisIndex));
        if (type == null) return false;
        return isLongAxis(type);
    }

    /**
     * return true if the given CTYPE value refers to a longitude axis.
     * @param ctype   the value of the CTYPE axis in question
     */
    public static boolean isLongAxis(String ctype) {
        String[] parts = ctype.split("\\-", 2);
        if ("RA".equals(parts[0]) || 
            parts[0].matches("[A-Z]LON") || parts[0].matches("[A-Z][A-Z]LN"))
          return true;
        return false;
    }

    /**
     * return true if the specified axis of an HDU is a longitude axis of some
     * type.  
     * @param hduIndex   the 0-based index of the HDU being inspected; the 
     *                      primary HDU has an index of 0.
     * @param axisIndex  the 1-based index of the axis of interest
     */
    public boolean isLatAxis(int hduIndex, int axisIndex) {
        String type = kws.getStringValue(hduIndex, 
                                         "CTYPE"+Integer.toString(axisIndex));
        if (type == null) return false;
        return isLatAxis(type);
    }

    /**
     * return true if the given CTYPE value refers to a latitude axis.
     * @param ctype   the value of the CTYPE axis in question
     */
    public static boolean isLatAxis(String ctype) {
        String[] parts = ctype.split("\\-", 2);
        if ("DEC".equals(parts[0]) || 
            parts[0].matches("[A-Z]LAT") || parts[0].matches("[A-Z][A-Z]LT"))
          return true;
        return false;
    }

    /**
     * return true if the axis is a frequency, wavelength, wave-number, 
     * or energy axis
     */
    public boolean isFreqAxis(int hduIndex, int axisIndex) {
        String type = kws.getStringValue(hduIndex, 
                                         "CTYPE"+Integer.toString(axisIndex));
        if (type == null) return false;
        return isFreqAxis(type);
    }

    /**
     * return true if the given CTYPE value refers to a frequency, wavelength, 
     * wave-number, or energy axis.
     * @param ctype   the value of the CTYPE axis in question
     */
    public static boolean isFreqAxis(String ctype) {
        String[] parts = ctype.split("\\-", 2);
        if ("FREQ".equals(parts[0]) || "WAVE".equals(parts[0]) ||
            "ENER".equals(parts[0]) || "WAVN".equals(parts[0]) ||
            "AWAV".equals(parts[0]))
          return true;
        return false;        
    }

    /**
     * return true if the axis is a velocity or redshift axis
     */
    public boolean isVelocityAxis(int hduIndex, int axisIndex) {
        String type = kws.getStringValue(hduIndex, 
                                         "CTYPE"+Integer.toString(axisIndex));
        if (type == null) return false;
        return isVelocityAxis(type);
    }

    /**
     * return true if the given CTYPE value refers to a velocity or redshift 
     * axis.
     * @param ctype   the value of the CTYPE axis in question
     */
    public static boolean isVelocityAxis(String ctype) {
        String[] parts = ctype.split("\\-", 2);
        if ("VELOCITY".equals(parts[0]) || "VRAD".equals(parts[0]) || 
            "VELO".equals(parts[0]) || "FELO".equals(parts[0]) || 
            "ZOPT".equals(parts[0]) || "BETA".equals(parts[0]))
          return true;
        return false;        
    }

    /**
     * return true if the axis is a spectral axis
     */
    public boolean isSpectralAxis(int hduIndex, int axisIndex) {
        String type = kws.getStringValue(hduIndex, 
                                         "CTYPE"+Integer.toString(axisIndex));
        if (type == null) return false;
        return isSpectralAxis(type);
    }

    /**
     * return true if the given CTYPE value refers to a spectral axis
     * @param ctype   the value of the CTYPE axis in question
     */
    public static boolean isSpectralAxis(String ctype) {
        return isFreqAxis(ctype) || isVelocityAxis(ctype);
    }

    /**
     * return a coordinate system for the given HDU
     */
    public CoordSys getCoordSys(int hduIndex) throws DataFormatException {
        return new CoordSys(kws, hduIndex);
    }

    /**
     * return the Instrument name.  null is returned if the HDU does 
     * not have an INSTRUME keyword.
     */
    public String getInstrument() {
        return kws.getStringValue(infoHDU(), "INSTRUME", null);
    }

    /**
     * return the DateObs value.  0 is returned if the HDU does 
     * not have a DATE-OBS keyword.
     */
    public double getDateObs() {
        return kws.getDoubleValue(infoHDU(), "DATE-OBS", 0);
    }

    /**
     * return the Naxis array.  null is returned if the HDU does 
     * not have NAXIS* values.
     */
    public int[] getNaxis() {
        return getNaxis(infoHDU());
    }

    /**
     * return the Naxis array.  null is returned if the HDU does 
     * not have NAXIS* values.
     */
    public int[] getNaxis(int hduIndex) {
        int naxes = getNaxes(hduIndex);
        int[] out = null;

        if (naxes != 0) {
            out = new int[naxes];

            for(int i=0; i<naxes; i++) {
                out[i] = kws.getIntValue(hduIndex, "NAXIS" + Integer.toString(i+1), 0);
            }
        }

        return out;
    }

    /**
     * return the Scale array.  null is returned if the HDU does 
     * not have CDELT* values.
     */
    public double[] getScale() {
        int hduIndex = infoHDU();
        int naxes = getNaxes(hduIndex);
        double[] out = null;

        if (naxes != 0) {
            out = new double[naxes];

            for(int i=0; i<naxes; i++) {
                out[i] = kws.getDoubleValue(hduIndex, "CDELT" + Integer.toString(i), 0);
            }
        }

        return out;
    }

    /**
     * return the Format value.
     */
    public String getFormat() {
        return "image/fits";
    }

    /**
     * return the CoordProjection value.
     */
    public String getCoordProjection() {
        int hduIndex = infoHDU();
        int[] axes = findSpatialAxes(hduIndex);

        if (axes != null) {
            for (int i = 0; i < axes.length; i++) {
                if (axes[i] != 0) {
                    String ctype = kws.getStringValue(hduIndex, 
                                         "CTYPE"+Integer.toString(axes[0]));
                    String[] parts = ctype.split("\\-");
                    // Skip the first element of the array that should have "RA" and such.
                    for (int j = 1; j < parts.length; j++) {
                        if (!parts[j].isEmpty())
                            return parts[j];
                    }
                }
            }
        }

        return null;
    }

    /**
     * return the CoordRefPixel array.  null is returned if the HDU does 
     * not have CRPIX* values.
     */
    public double[] getCoordRefPixel() {
        int hduIndex = infoHDU();
        int naxes = getNaxes(hduIndex);
        double[] out = null;

        if (naxes != 0) {
            out = new double[naxes];

            for(int i=0; i<naxes; i++) {
                out[i] = kws.getDoubleValue(hduIndex, "CRPIX" + Integer.toString(i+1), 0);
            }
        }

        return out;
    }

    /**
     * return the coordinate reference frame
     */
    public String getCoordRefFrame() throws DataFormatException {
        return getCoordSys(infoHDU()).getSystemStandard();
    }

    /**
     * return the coordinate equinox
     */
    public double getCoordEquinox() throws DataFormatException {
        return getCoordSys(infoHDU()).getEquinox();
    }

    /**
     * return the CoordRefValue array.  null is returned if the HDU does 
     * not have CRVAL* values.
     */
    public double[] getCoordRefValue() {
        return getCoordRefValue(infoHDU());
    }

    /**
     * return the CoordRefValue array.  null is returned if the HDU does 
     * not have CRVAL* values.
     */
    public double[] getCoordRefValue(int hduIndex) {
        int naxes = getNaxes(hduIndex);
        double[] out = null;

        if (naxes != 0) {
            out = new double[naxes];

            for(int i=0; i<naxes; i++) {
                out[i] = kws.getDoubleValue(hduIndex, "CRVAL" + Integer.toString(i+1), 0);
            }
        }

        return out;
    }

    /**
     * return the CoordCDMatrix array.  null is returned if the HDU does 
     * not have CD* values.
     */
    public double[] getCoordCDMatrix() {
        int hduIndex = infoHDU();
        int naxes = getNaxes(hduIndex);
        double[] out = null;

        if (naxes != 0) {
            out = new double[naxes*naxes];

            int k = 0;
            for(int i=0; i<naxes; i++) {
                for(int j=0; j<naxes; j++) {
                    out[k] = kws.getDoubleValue(hduIndex, "CD" + Integer.toString(i+1) + "_" + Integer.toString(j+1), 0);
                    k++;
                }
            }
        }

        return out;
    }

    /**
     * return true if the dataset appears to be of the SIMPLE image type
     * 
     * This returns true if the primary HDU is an Image HDU and any subsequent
     * HDUs are not.  
     */
    public boolean isSimpleImage() {
        int hduc = kws.getHDUCount();
        if (hduc < 1) return false;

        if (! isImageHDU(0) || getNaxes(0) < 1) return false;
        String sval = kws.getStringValue(0, "CTYPE"+getNaxes(0));
        if (sval == null || "GROUP_NUMBER".equals(sval))
            return false;
        if (kws.getStringValue(0, "CRVAL"+getNaxes(0)) == null)
            return false;
        for(int i=1; i < hduc; i++) {
            if (isImageHDU(i)) return false;
        }
        return true;
    }

    /**
     * return true if the dataset appears to be of the EXTENSION image type
     * 
     * This returns true if one of the HDUs is an Image HDU but not the 
     * first HDU in the dataset.  
     */
    public boolean isExtensionImage() {
        int hduc = kws.getHDUCount();
        if (hduc < 1) return false;

        // first HDU is null image
        if (isImageHDU(0) && getNaxes(0) > 1) return false;

        int ic = 0;
        for(int i=1; i < hduc; i++) {
            if (isImageHDU(i) && getNaxes(i) > 0) ic++;
            if (ic > 1) return false;
        }
        return true;
    }

    /**
     * return true if the given HDU appears to contain a stack of images where
     * layer represents a different measurement.  The most common example would
     * be a stack in which the first plane represents the flux measurement and
     * subsequent planes are ancillary information (e.g. error).  
     * 
     * This returns true if the last axis of the image array has no WCS metadata
     * defined for it.  
     */
    public boolean isRichStackHDU(int hduIndex) {
        if (hduIndex >= kws.getHDUCount()) return false;
        if (! isImageHDU(hduIndex) || getNaxes(hduIndex) < 3) return false;

        // Last axis will not have WCS parameters
        int nax = getNaxes(hduIndex);
        for(int i=1; i < nax; i++) {
            if (kws.getStringValue(hduIndex, "CTYPE"+i) == null) return false;
        }

        String sval = kws.getStringValue(hduIndex, "CTYPE"+nax);
        if (sval == null || "GROUP_NUMBER".equals(sval)) return true;
        return (kws.getStringValue(0, "CRVAL"+getNaxes(0)) == null &&
                getNaxis(hduIndex)[nax-1] > 1);
    }

    /**
     * return true if the dataset appears to contain a single stack 
     * of images representing different measurements with the same sampling.
     */
    public boolean isRichStack() {
        int hduc = kws.getHDUCount();
        if (hduc < 1) return false;

        if (isCCDStack()) return false;

        if (! isRichStackHDU(0)) return false;
        for(int i=1; i < hduc; i++) {
            if (isRichStackHDU(i)) return false;
        }
        return true;
    }

    /**
     * return true if this dataset appears to be an image containing 
     * multiple measurements with common sampling, formatted as an MEF
     * (each measurement as a separate extension).  This implementation 
     * requires the first HDU to be a dummy and subsequent HDUs to contain 
     * image data of the same shape.  Non-Image HDUs are ignored.
     */
    public boolean isRichMEF() {
        int hduc = kws.getHDUCount();
        if (hduc < 3) return false;

        if (! isDummyHDU(0) || ! isImageHDU(1)) return false;
        int[] size = getNaxis(1);
        double[] rval = getCoordRefValue(1);
        String[] labels = new String[rval.length];
        for(int j=1; j <= rval.length; j++) 
            labels[j-1] = kws.getStringValue(1, "CTYPE"+j);
        String[] sval = null;
        
        for(int i=2; i < hduc; i++) {
            if (isImageHDU(i)) {
                if (! Arrays.equals(size, getNaxis(i))) return false;
                if (! Arrays.equals(rval, getCoordRefValue(i))) {
                    double[] dval = getCoordRefValue(i);
                    if (rval.length != dval.length) return false;
                    for(int j=0; j < rval.length; j++)
                        if (Math.abs(rval[j]-dval[j]) > 1.0e9) return false;
                }
                sval = new String[rval.length];
                for(int j=1; j <= sval.length; j++) 
                    sval[j-1] = kws.getStringValue(1, "CTYPE"+j);
                if (! Arrays.equals(labels, sval)) return false;
            }
        }

        return true;
    }

    /**
     * return true if the dataset appears to hold a single image stack where 
     * each plane of the stack contains a different CCDs from the same focal 
     * plane.  Following the HST-WFPC2 convention, it must contain a primary 
     * image followed by a WCS table; the last axis of the image must not 
     * have WCS labelling.  
     */
    public boolean isCCDStack() {
        int hduc = kws.getHDUCount();
        if (hduc < 2) return false;

        if (! isImageHDU(0) || ! isTableHDU(1)) return false;
        int[] imgsize = getNaxis(0);
        if (imgsize.length < 3) return false;
        String sval = kws.getStringValue(0, "CTYPE"+imgsize.length);
        if (sval != null && ! "GROUP_NUMBER".equals(sval)) return false;

        int[] tblsize = getNaxis(1);
        if (tblsize.length != 2) return false;
        return (imgsize[imgsize.length-1] == tblsize[1]);
    }

    /**
     * return true if the dataset appears to be an MEF image containing 
     * different CCDs from the same focal plane.  This will require that the 
     * first HDU be a dummy and at least 2 subsequent HDUs have the same axis 
     * types.  
     */
    public boolean isCCDMEF() {
        int hduc = kws.getHDUCount();
        if (hduc < 3) return false;

        if (! isDummyHDU(0) || ! isImageHDU(1)) return false;
        int nax = getCoordRefValue(1).length;
        String[] labels = new String[nax];
        for(int j=1; j <= nax; j++) 
            labels[j-1] = kws.getStringValue(1, "CTYPE"+j);

        String[] sval = null;
        for(int i=2; i < hduc; i++) {
            if (isImageHDU(i)) {
                sval = new String[nax];
                for(int j=1; j <= nax; j++) 
                    if (labels[j-1] != kws.getStringValue(i, "CTYPE"+j))
                        return false;
                break;
            }
        }

        return true;
    }

    /**
     * determine which FITS Image convention the file appears to 
     * follow.  It may not be able to distinguish between images 
     * and certain conventions for storing spectra.  
     */
    public ImageType determineImageType() {
        if (isCCDMEF()) return ImageType.CCD_MEF;
        if (isCCDStack()) return ImageType.CCD_STACK;
        if (isRichMEF()) return ImageType.RICH_MEF;
        if (isRichStack()) return ImageType.RICH_STACK;
        if (isExtensionImage()) return ImageType.EXTENSION;
        if (isSimpleImage()) return ImageType.SIMPLE;
        return ImageType.UNRECOGNIZED;
    }

    /**
     * return a central position for a simple image.  This simply returns 
     * the central position of the first HDU.
     */
    public double[] getCentralPosition(int hduIndex) throws DataFormatException {
        if (! isImageHDU(hduIndex) || getNaxes(hduIndex) < 1) 
            throw new IllegalArgumentException("HDU "+hduIndex+" not an Image");
        int[] size = getNaxis(hduIndex);
        double[] pos = new double[size.length];
        for(int i=0; i < pos.length; i++) pos[i] = size[i] / 0.5;

        CoordSys cs = getCoordSys(0);
        return cs.getCoordPos(pos);
    }

    /**
     * return the central position of a CCD_MEF type image by computing a 
     * average of CCD central positions, weighted by the CCD's area on the 
     * sky.  
     */
    double[] getRADecForMEF(int startHdu) throws DataFormatException {
        double[] txyz = new double[3];
        double[] xyz = new double[3];
        double[] out = new double[2];
        double[] cel = null;
        int[] spax = null;
        int[] size = null;
        double area = 0.0, tarea = 0.0;

        int chdu = kws.getHDUCount();
        if (chdu < startHdu || ! isImageHDU(1))
            throw new IllegalArgumentException("Not an CCDMEF dataset");
        for(int i=startHdu; i < chdu; i++) {
            if (! isImageHDU(i)) break;
            cel = getRADec(i);

            // convert to unit vector
            xyz[0] = TrigD.cos(cel[0]);
            xyz[1] = TrigD.sin(cel[0]);
            xyz[2] = TrigD.sin(cel[1]);

            // find image area
            spax = findSpatialAxes(i);
            cel[0] = kws.getDoubleValue(i, "CDELT"+(spax[0]), 1.0/3600.0);
            cel[1] = kws.getDoubleValue(i, "CDELT"+(spax[1]), 1.0/3600.0);
            size = getNaxis(i);
            area = cel[0]*cel[1]*size[0]*size[1];
            tarea += area;
            
            txyz[0] += xyz[0]*area;
            txyz[1] += xyz[1]*area;
            txyz[2] += xyz[2]*area;
        }

        out[0] = TrigD.atan(txyz[1]/txyz[0]);
        out[1] = TrigD.asin(txyz[2]/tarea);

        return out;
    }

    public int infoHDU() {
        ImageType type = determineImageType();
        int hdu = 0;

        if (type == ImageType.SIMPLE || type == ImageType.RICH_STACK) {
            hdu = 0;
        }
        else if (type == ImageType.RICH_MEF) {
            hdu = 1;
        }
        else if (type == ImageType.CCD_STACK) {
            hdu = 0;
        }
        else if (type == ImageType.CCD_MEF) {
            hdu = 1;
        }
        else {
            // otherwise just return position for first image HDU found
            int c = kws.getHDUCount();
            for(hdu=0; hdu < c; hdu++) {
                if (isImageHDU(hdu)) break;
            }
        }

        return hdu;
    }

    public double[] getRADec() throws DataFormatException {
        ImageType type = determineImageType();
        int hdu = 0;

        if (type == ImageType.SIMPLE || type == ImageType.RICH_STACK) {
            hdu = 0;
        }
        else if (type == ImageType.RICH_MEF) {
            hdu = 1;
        }
        else if (type == ImageType.CCD_STACK) {
            hdu = 0;
        }
        else if (type == ImageType.CCD_MEF) {
            return getRADecForMEF(1);
        }
        else {
            // otherwise just return position for first image HDU found
            int c = kws.getHDUCount();
            for(hdu=0; hdu < c; hdu++) {
                if (isImageHDU(hdu)) break;
            }
        }

        double[] center = getCentralPosition(hdu);
        double[] out = new double[2];
        int[] spax = findSpatialAxes(hdu);  // 1-based
        spax[0]--; spax[1]--;
        out[0] = (spax[0] < 0) ? getAltRA(hdu) : center[spax[0]];
        out[1] = (spax[1] < 0) ? getAltDec(hdu) : center[spax[1]];
        return out;
    }

    public double[] getRADec(int hduIndex) throws DataFormatException {
        if (! isImageHDU(hduIndex) || getNaxes(hduIndex) < 1) 
            throw new IllegalArgumentException("HDU "+hduIndex+" not an Image");
        int[] size = getNaxis(hduIndex);
        double[] pos = new double[size.length];
        for(int i=0; i < pos.length; i++) pos[i] = size[i] / 0.5;

        CoordSys cs = getCoordSys(0);
        pos = cs.getCoordPos(pos);

        double[] out = new double[2];
        int[] spax = findSpatialAxes(hduIndex);  // 1-based
        if (spax == null) {
            out[0] = getAltRA(hduIndex); 
            out[1] = getAltDec(hduIndex);
        }
        else {
            spax[0]--; spax[1]--;
            if ( (spax[0] >= 0 && cs.getLabel(spax[0]).startsWith("GLON") && spax[1] < 0) ||
                 (spax[1] >= 0 && cs.getLabel(spax[1]).startsWith("GLAT") && spax[0] < 0) )
                throw new DataFormatException("Incomplete Galactic coordinates");
            if ( spax[0] >= 0 && spax[1] >= 0 &&
                 (cs.getLabel(spax[0]).startsWith("GLON") ||
                  cs.getLabel(spax[0]).startsWith("GLAT")   ))
            {
                if ( (cs.getLabel(spax[0]).startsWith("GLON") && 
                      ! cs.getLabel(spax[0]).startsWith("GLAT")) ||
                     (cs.getLabel(spax[0]).startsWith("GLAT") && 
                      ! cs.getLabel(spax[0]).startsWith("GLON")) )
                throw new DataFormatException("Bad mix of Galactic coordinates");

                out = CoordSys.gal2cel(pos[spax[0]], pos[spax[1]]);
            }
            else {

                out[0] = (spax[0] < 0) ? getAltRA(hduIndex) : pos[spax[0]];
                out[1] = (spax[1] < 0) ? getAltDec(hduIndex) : pos[spax[1]];

                if (cs.getSystemStandard().equals("FK4")) 
                    out = CoordSys.b1950toJ2000(out[0], out[1]);
            }
        }

        return out;
    }

    public double getAltRA(int hduIndex) {
        double out = 0.0;
        final String[] keys = { "RA", "RA_OBS", "RA_PNT", "RA_NOM", "NOMRA",
                                "RA_TARG", "RA_APER", "RA_OBJ" };
        String val = null;
        for(String key : keys) {
            val = kws.getStringValue(hduIndex, key);
            if (val != null) break;
        }
        if (val == null) return out;
        val = val.trim();

        try {
            if (val.matches("^(\\d\\d[: ]){2}\\d\\d(\\.\\d+)?$"))
                return sexg2dec(val)*15.0;

            out = Double.parseDouble(val);
        } catch (NumberFormatException ex) {
            out = 0.0;
        }

        return out;
    }
    public double getAltDec(int hduIndex) {
        double out = 0.0;
        final String[] keys = { "DEC", "DEC_OBS", "DEC_PNT", "DEC_NOM", "NOMDEC",
                                "DEC_TARG", "DEC_APER", "DEC_OBJ" };
        String val = null;
        for(String key : keys) {
            val = kws.getStringValue(hduIndex, key);
            if (val != null) break;
        }
        if (val == null) return out;
        val = val.trim();

        try {
            if (val.matches("^[+-](\\d\\d[: ]){2}\\d\\d(\\.\\d+)?$"))
                return sexg2dec(val);

            out = Double.parseDouble(val);
        } catch (NumberFormatException ex) {
            out = 0.0;
        }

        return out;
    }

    public double sexg2dec(String val) throws NumberFormatException {
        double out = 0.0;

        char sign = '+';
        if (val.charAt(0) == '+' || val.charAt(0) == '-') {
            sign = val.charAt(0);
            val = val.substring(1);
        }

        String[] flds = val.split("[: ]", 3);
        if (flds.length > 0) out += Integer.parseInt(flds[0]);
        if (flds.length > 1) out += Integer.parseInt(flds[1]) / 60.0;
        if (flds.length > 2) out += Integer.parseInt(flds[2]) / 3600.0;
        return out;
    }

    public String getPixFlags() {
        return "C";
    }
}
