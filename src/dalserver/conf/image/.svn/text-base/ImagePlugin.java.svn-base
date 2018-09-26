package dalserver.conf.image;

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
 ** this class implements PluginFactory for Image files.
 **/
public class ImagePlugin implements PluginFactory {

    ImageHeaderUtils ihu = null;
    String lastfile = null;

    /**
     ** wrap a setup of FITS headers from a FITS file
     **/
    private void initImageHeaderUtils(String pathName) {
        if (pathName == null)
            throw new InternalError("Prog err: pathName is null");

        if (lastfile == null || !lastfile.equals(pathName)) {
            lastfile = pathName;
            try {
                ihu = ImageHeaderUtils.loadFromFile(new File(pathName));
            } catch (IOException ex) {
                throw new InternalError("Prog err: IOException on File open/read: "
                                        + ex.getMessage());
            }
        }
    }

    public String getValue(String pathName, String param, HashMap headerVals) {
        initImageHeaderUtils(pathName);

        if (param == null)
            throw new InternalError("Prog err: param is null");

        try {
            if (param.equalsIgnoreCase("Title")) {
                return pathName.substring(0, pathName.lastIndexOf('.')).
                                       substring(pathName.lastIndexOf(File.separatorChar) +1);
            } else if (param.equalsIgnoreCase("Instrument")) {
                return ihu.getInstrument();
            } else if (param.equalsIgnoreCase("DateObs")) {
                return Double.toString(ihu.getDateObs());
            } else if (param.equalsIgnoreCase("RA")) {
                return Double.toString(ihu.getRADec()[0]);
            } else if (param.equalsIgnoreCase("DECL")) {
                return Double.toString(ihu.getRADec()[1]);
            } else if (param.equalsIgnoreCase("Naxes")) {
                return Integer.toString(ihu.getNaxes());
            } else if (param.equalsIgnoreCase("Naxis")) {
                int[] naxis = ihu.getNaxis();
                if (naxis == null)
                    return null;

                String out = "";
                for (int i = 0; i < naxis.length; i++) {
                    if (i != 0)
                        out += " ";
                    out += Integer.toString(naxis[i]);
                }
                return out;
            } else if (param.equalsIgnoreCase("Naxis1")) {
                int[] naxis = ihu.getNaxis();
                if (naxis == null)
                    return null;

                return Integer.toString(naxis[0]);
            } else if (param.equalsIgnoreCase("Naxis2")) {
                int[] naxis = ihu.getNaxis();
                if (naxis == null)
                    return null;

                return Integer.toString(naxis[1]);
            } else if (param.equalsIgnoreCase("Scale")) {
                double[] scale = ihu.getScale();
                if (scale == null)
                    return null;

                String out = "";
                for (int i = 0; i < scale.length; i++) {
                    if (i != 0)
                        out += " ";
                    out += Double.toString(scale[i]);
                }
                return out;
            } else if (param.equalsIgnoreCase("Scale1")) {
                double[] scale = ihu.getScale();
                if (scale == null)
                    return null;

                return Double.toString(scale[0]);
            } else if (param.equalsIgnoreCase("Scale2")) {
                double[] scale = ihu.getScale();
                if (scale == null)
                    return null;

                return Double.toString(scale[1]);
            } else if (param.equalsIgnoreCase("Fname")) {
                return pathName.substring(pathName.lastIndexOf(File.separatorChar) +1);
            } else if (param.equalsIgnoreCase("Format")) {
                return ihu.getFormat();
            } else if (param.equalsIgnoreCase("CoordProjection")) {
                return ihu.getCoordProjection();
            } else if (param.equalsIgnoreCase("CoordRefPixel")) {
                double[] pixels = ihu.getCoordRefPixel();
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
                double[] values = ihu.getCoordRefValue();
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
                double[] values = ihu.getCoordCDMatrix();
                if (values == null)
                    return null;

                String out = "";
                for (int i = 0; i < values.length; i++) {
                    if (i != 0)
                        out += " ";
                    out += Double.toString(values[i]);
                }
                return out;
            } else if (param.equalsIgnoreCase("PixFlags")) {
                return ihu.getPixFlags();
            } else if (param.equalsIgnoreCase("CoordRefFrame")) {
                return ihu.getCoordRefFrame();
            } else if (param.equalsIgnoreCase("CoordEquinox")) {
                double value = ihu.getCoordEquinox();
                return Double.toString(value);
            } else
                return null;
        } catch (DataFormatException ex) {
                throw new InternalError("Prog err: DataFormatException on " +
                              "processing param: " + param + " " + ex.getMessage());
        }
    }

    public void appendQueryData(String pathName, Properties props) {
        initImageHeaderUtils(pathName);

        if (!props.containsKey("RA")) {
            props.put("RA", getValue(pathName, "RA", null));
        }

        if (!props.containsKey("DECL")) {
            props.put("DECL", getValue(pathName, "DECL", null));
        }

        double[] scale = ihu.getScale();
        if (scale == null)
                throw new InternalError("Prog err: Unable to find SCALE values in FITS headers");
        if (!props.containsKey("SCALE1")) {
            props.put("SCALE1", Double.toString(scale[0]));
        }
        if (!props.containsKey("SCALE2")) {
            props.put("SCALE2", Double.toString(scale[1]));
        }

        int[] naxis = ihu.getNaxis();
        if (naxis == null)
                throw new InternalError("Prog err: Unable to find NAXIS values in FITS headers");
        if (!props.containsKey("NAXIS1")) {
            props.put("NAXIS1", Integer.toString(naxis[0]));
        }
        if (!props.containsKey("NAXIS2")) {
            props.put("NAXIS2", Integer.toString(naxis[1]));
        }
        if (!props.containsKey("FNAME")) {
            props.put("FNAME",
                pathName.substring(pathName.lastIndexOf(File.separatorChar) +1));
        }
    }
}
