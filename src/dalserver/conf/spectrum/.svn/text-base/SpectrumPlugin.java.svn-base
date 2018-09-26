package dalserver.conf.spectrum;

import dalserver.conf.FITSHeaderKeywords;
import dalserver.conf.NTFITSHeaderKeywords;
import dalserver.conf.DataFormatException;

import dalserver.conf.PluginFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


/**
 *  * this class implements PluginFactory for Spectrum files.
 *   */
public class SpectrumPlugin implements PluginFactory {

    SpectrumHeaderUtils shu = null;
    String lastfile = null;

    /**
     ** wrap a setup of FITS headers from a FITS file
     **/
    public String getValue(String pathName, String param, HashMap headerVals) {
        if (pathName == null)
            throw new InternalError("Prog err: pathName is null");

        if (param == null)
            throw new InternalError("Prog err: param is null");

        if (lastfile == null || !lastfile.equals(pathName)) {
            lastfile = pathName;
            try {
                shu = SpectrumHeaderUtils.loadFromFile(new File(pathName));
            } catch (IOException ex) {
                throw new InternalError("Prog err: IOException on File open/read: "
                                        + ex.getMessage());
            }
        }

        try {
            if (param.equalsIgnoreCase("Title")) {
                return pathName.substring(0, pathName.lastIndexOf('.')).
                                       substring(pathName.lastIndexOf(File.separatorChar) +1);
            } else if (param.equalsIgnoreCase("Instrument")) {
                return shu.getInstrument();
	    } else if (param.equalsIgnoreCase("TargetPos")) {
		double[] radec = shu.getTargetPos();
                return Double.toString(radec[0]) + " " + Double.toString(radec[1]);
	    } else if (param.equalsIgnoreCase("SpatialLocation")) {
		double[] radec = shu.getSpatialLocation();
                return Double.toString(radec[0]) + " " + Double.toString(radec[1]);
            } else if (param.equalsIgnoreCase("DateObs")) {
                return Double.toString(shu.getDateObs());
            } else if (param.equalsIgnoreCase("RA")) {
                return Double.toString(shu.getRADec()[0]);
            } else if (param.equalsIgnoreCase("DEC")) {
                return Double.toString(shu.getRADec()[1]);
            } else if (param.equalsIgnoreCase("Naxes")) {
                return Integer.toString(shu.getNaxes());
            } else if (param.equalsIgnoreCase("Naxis")) {
                int[] naxis = shu.getNaxis();
                if (naxis == null)
                    return null;

                String out = "";
                for (int i = 0; i < naxis.length; i++) {
                    if (i != 0)
                        out += " ";
                    out += Integer.toString(naxis[i]);
                }
                return out;
            } else if (param.equalsIgnoreCase("Scale")) {
                double[] scale = shu.getScale();
                if (scale == null)
                    return null;

                String out = "";
                for (int i = 0; i < scale.length; i++) {
                    if (i != 0)
                        out += " ";
                    out += Double.toString(scale[i]);
                }
                return out;
            } else if (param.equalsIgnoreCase("Scale")) {
                return shu.getFormat();
            } else if (param.equalsIgnoreCase("CoordProjection")) {
                return shu.getCoordProjection();
            } else if (param.equalsIgnoreCase("CoordRefPixel")) {
                double[] pixels = shu.getCoordRefPixel();
                if (pixels == null)
                    return null;

                String out = "";
                for (int i = 0; i < pixels.length; i++) {
                    if (i != 0)
                        out += " ";
                    out += Double.toString(pixels[i]);
                }
                return out;
            } else if (param.equalsIgnoreCase("CoordRefValue")) {
                double[] values = shu.getCoordRefValue();
                if (values == null)
                    return null;

                String out = "";
                for (int i = 0; i < values.length; i++) {
                    if (i != 0)
                        out += " ";
                    out += Double.toString(values[i]);
                }
                return out;
            } else if (param.equalsIgnoreCase("CoordCDMatrix")) {
                double[] values = shu.getCoordCDMatrix();
                if (values == null)
                    return null;

                String out = "";
                for (int i = 0; i < values.length; i++) {
                    if (i != 0)
                        out += " ";
                    out += Double.toString(values[i]);
                }
                return out;
            } else
                return "TEST".toString();
        } catch (DataFormatException ex) {
                throw new InternalError("Prog err: DataFormatException on " +
                              "processing param: " + param + " " + ex.getMessage());
        }
    }

    public void appendQueryData(String pathName, Properties props) {
    }
}
