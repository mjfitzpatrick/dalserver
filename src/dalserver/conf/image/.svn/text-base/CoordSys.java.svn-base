package dalserver.conf.image;

import dalserver.conf.FITSHeaderKeywords;
import dalserver.conf.NTFITSHeaderKeywords;
import dalserver.conf.DataFormatException;

import ncsa.horizon.coordinates.CoordinateSystem;
import ncsa.horizon.coordinates.FITSCoordMetadata;
import ncsa.horizon.coordinates.CoordMetadata;
import ncsa.horizon.coordinates.CoordTransform;
import ncsa.horizon.coordinates.transforms.SphericalCoordTransform;
import ncsa.horizon.coordinates.PositionBeyondDomainException;
import ncsa.horizon.coordinates.IllegalTransformException;
import ncsa.horizon.coordinates.TransformUndefinedException;
import ncsa.horizon.coordinates.CoordTransformException;
import FITSWCS.TrigD;

import java.util.Arrays;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

/**
 * a class that encapsulates a coordinate system and its mapping onto an 
 * N-dimensional array of data.  It is built from a FITS HDU header. 
 * 
 * This implementation hides use of the NCSA Horizon library which handles
 * the mapping between pixel and world coordinates, implementing the 
 * mathematical models laid out in Papers I and II of the FITS WCS standard.
 * Interpretation of the actual FITS keywords is handled by this class.  
 */
public class CoordSys {

    private CoordinateSystem _cs = null;
    private String _sysstd = "ICRS";
    private double _equinox = 0.0;

    /**
     * create a CoordSys from the FITS header found in a request HDU
     * @param fhk    the FITSHeaderKeywords container containing the header data
     * @param hduIndex   the index of the desired HDU
     */
    public CoordSys(FITSHeaderKeywords fhk, int hduIndex) 
        throws DataFormatException
    {
        try {
            FITSCoordMetadata cmd = createCoordMetadata(fhk, hduIndex);
            _cs = cmd.createCoordSys();
            String radecsys = (String) cmd.getMetadatum("RADECSYS");
            if (radecsys != null) _sysstd = radecsys;
            Double eq = (Double) cmd.getMetadatum("EQUINOX");
            if (eq != null) eq = eq.doubleValue();
        }
        catch (IllegalTransformException ex) {
            String name = fhk.getStringValue(0, "dataset");
            if (name != null) name += " (HDU "+hduIndex+")";
            throw new DataFormatException("Bad FITS metadata: " + 
                                          ex.getMessage(), ex, name);
        }

    }

    /**
     * return the number of axes defining the coordinate system.
     */
    public int getNaxes() { return _cs.getNaxes(); }

    /**
     * return a label for a given axis
     */
    public String getLabel(int axis) {
        return _cs.getAxisLabel(axis);
    }

    /**
     * return the name of the world coordinate system standard this system 
     * represents.  This is, generally speaking, the FITS RADECSYS designation.
     */
    public String getSystemStandard() { return _sysstd; }

    /**
     * return equinox of the system or 0 if one is not applicable.  
     */
    public double getEquinox() { return _equinox; }

    /**
     * return position of a given pixel coordinate
     * @param vox   the voxel (i.e. pixel coordinate) of interest given as 
     *                an array of doubles whose length must be equal to the 
     *                value returned by {@link getNaxes()}.  
     * @return double[]  the corresponding world coordinate position whose 
     *                length must be the same as vox; null is returned if 
     *                the transformation is undefined for the given pixel.
     */
    public double[] getCoordPos(double[] vox) {
        try {
            return _cs.getCoordValue(vox);
        }
        catch (PositionBeyondDomainException ex) {
            return null;
        }
        catch (TransformUndefinedException ex) {
            return null;
        }
        catch (CoordTransformException ex) {
            throw new IllegalArgumentException("Bad conversion: " + 
                                               ex.getMessage(), ex);
        }
    }

    FITSCoordMetadata createCoordMetadata(FITSHeaderKeywords fhk, int hduIndex) {
        FITSCoordMetadata md = new FITSCoordMetadata();

        int nax = fhk.getIntValue(hduIndex, "NAXIS", 0);
        nax = fhk.getIntValue(hduIndex, "WCSAXES", nax);
        if (nax < 1) 
            throw new IllegalArgumentException("Header HDU does not describe "+
                                               "a legal Image");
        md.setNAXIS(nax);

        String ax = null;
        String name = null;
        int i, j;
        for(i=1; i <= nax; i++) {
            ax = Integer.toString(i);
            if (fhk.containsKey(hduIndex, "CRPIX"+ax))
                md.setCRPIX(i, fhk.getDoubleValue(hduIndex, "CRPIX"+ax, 1.0));
            if (fhk.containsKey(hduIndex, "CTYPE"+ax))
                md.setCTYPE(i, fhk.getStringValue(hduIndex, "CTYPE"+ax));
            if (fhk.containsKey(hduIndex, "CRVAL"+ax))
                md.setCRVAL(i, fhk.getDoubleValue(hduIndex, "CRVAL"+ax, 0.0));
            if (fhk.containsKey(hduIndex, "CDELT"+ax))
                md.setCDELT(i, fhk.getDoubleValue(hduIndex, "CDELT"+ax, 0.0));
            if (fhk.containsKey(hduIndex, "CROTA"+ax))
                md.setCROTA(i, fhk.getDoubleValue(hduIndex, "CROTA"+ax, 0.0));
            for (j=0; j < 20; j++) {
                name = "PV"+ax+"_"+j;
                if (fhk.containsKey(hduIndex, name))
                    md.setPROJP(j, fhk.getDoubleValue(hduIndex, name, 0.0));
                else if (i > 2)
                    break;
            }
        }

        double dval = 0.0;
        for(i=1; i <= nax; i++) {
            for(j=1; j <= nax; j++) {
                name = "PC"+i+"_"+j;
                if (fhk.containsKey(hduIndex, name))
                    md.setPCMatrix(i, j, 
                                   fhk.getDoubleValue(hduIndex, name, 0.0));
            }
        }
        for(i=1; i <= nax; i++) {
            for(j=1; j <= nax; j++) {
                name = "CD"+i+"_"+j;
                if (fhk.containsKey(hduIndex, name)) {
                    md.setPCMatrix(i, j, 
                                   fhk.getDoubleValue(hduIndex, name, 0.0));
                    md.setCDELT(i, 1.0);
                    md.setCDELT(j, 1.0);
                } 
            }
        }

        String sval = null;
        dval = 0.0;
        if (fhk.containsKey(hduIndex, "EQUINOX")) {
            dval = fhk.getDoubleValue(hduIndex, "EQUNOX");
            md.setEQUINOX( dval );
        }
        else if (fhk.containsKey(hduIndex, "EPOCH")) {
            dval = fhk.getDoubleValue(hduIndex, "EPOCH");
            md.setEQUINOX( dval );
        }
        // WCS Paper II: if RADECSYS and EPOCH/EQUINOX not given, it 
        // defaults to ICRS.  If EPOCH/EQUINOX < 1984, it's FK4
        sval = (dval <= 0.0) ? "ICRS" : ((dval < 1984.0) ? "FK4" : "FK5");
        if (fhk.containsKey(hduIndex, "RADESYS"))
            md.setRADECSYS( fhk.getStringValue(hduIndex, "RADESYS", sval) );
        else 
            md.setRADECSYS( fhk.getStringValue(hduIndex, "RADECSYS", sval) );
        // WCS Paper II: default equinoxes: FK4 -> 1950, FK5 -> 2000
        sval = (String) md.getMetadatum("RADECSYS");
        if (dval <= 0.0) {
            if (sval != null && sval.startsWith("FK4"))
                md.setEQUINOX(1950.0);
            else if ("FK5".equals(sval))
                md.setEQUINOX(2000.0);
        }

        if (fhk.containsKey(hduIndex, "LONPOLE")) 
            md.setLONGPOLE( fhk.getDoubleValue(hduIndex, "LONPOLE", 0.0) );
        if (fhk.containsKey(hduIndex, "LATPOLE")) 
            md.setLATPOLE( fhk.getDoubleValue(hduIndex, "LATPOLE", 90.0) );

        md.modernize();
        return md;
    }

    static final double[] galpolpos = { 192.859508, 27.128336 };
    static final double celpolgallon = 122.932;
    static CoordTransform _gal2cel = 
        new SphericalCoordTransform(galpolpos[0], galpolpos[1], celpolgallon);

    // calculated these via
    // https://heasarc.gsfc.nasa.gov/cgi-bin/Tools/convcoord/convcoord.pl
    static final double[] b50polpos = { 180.316337, 89.721687 };
    static final double celpolb50lon = 359.675668;
    static CoordTransform _b502j00 = 
        new SphericalCoordTransform(b50polpos[0], b50polpos[1], celpolb50lon);

    /**
     * convert galactic coordinates to J2000
     * @param lon   the galactic longitude of the position
     * @param lat   the galactic latitude of the position
     * @param double[]  the right acension and declination as a 2-element array
     */
    public static double[] gal2cel(double lon, double lat) {
        final int[] axes = { 0, 1 };
        final double[] in = { lon, lat };
        try {
            return _gal2cel.reverse(in, axes);
        } catch (CoordTransformException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * convert galactic coordinates to J2000
     * @param lon   the galactic longitude of the position
     * @param lat   the galactic latitude of the position
     * @param double[]  the right acension and declination as a 2-element array
     */
    public static double[] b1950toJ2000(double lon, double lat) {
        final int[] axes = { 0, 1 };
        final double[] in = { lon, lat };
        try {
            return _b502j00.reverse(in, axes);
        } catch (CoordTransformException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    
}