/*
************************************************************************
*******************  CANADIAN ASTRONOMY DATA CENTRE  *******************
**************  CENTRE CANADIEN DE DONNÉES ASTRONOMIQUES  **************
*
*  (c) 2014.                            (c) 2014.
*  Government of Canada                 Gouvernement du Canada
*  National Research Council            Conseil national de recherches
*  Ottawa, Canada, K1A 0R6              Ottawa, Canada, K1A 0R6
*  All rights reserved                  Tous droits réservés
*
*  NRC disclaims any warranties,        Le CNRC dénie toute garantie
*  expressed, implied, or               énoncée, implicite ou légale,
*  statutory, of any kind with          de quelque nature que ce
*  respect to the software,             soit, concernant le logiciel,
*  including without limitation         y compris sans restriction
*  any warranty of merchantability      toute garantie de valeur
*  or fitness for a particular          marchande ou de pertinence
*  purpose. NRC shall not be            pour un usage particulier.
*  liable in any event for any          Le CNRC ne pourra en aucun cas
*  damages, whether direct or           être tenu responsable de tout
*  indirect, special or general,        dommage, direct ou indirect,
*  consequential or incidental,         particulier ou général,
*  arising from the use of the          accessoire ou fortuit, résultant
*  software.  Neither the name          de l'utilisation du logiciel. Ni
*  of the National Research             le nom du Conseil National de
*  Council of Canada nor the            Recherches du Canada ni les noms
*  names of its contributors may        de ses  participants ne peuvent
*  be used to endorse or promote        être utilisés pour approuver ou
*  products derived from this           promouvoir les produits dérivés
*  software without specific prior      de ce logiciel sans autorisation
*  written permission.                  préalable et particulière
*                                       par écrit.
*
*  This file is part of the             Ce fichier fait partie du projet
*  OpenCADC project.                    OpenCADC.
*
*  OpenCADC is free software:           OpenCADC est un logiciel libre ;
*  you can redistribute it and/or       vous pouvez le redistribuer ou le
*  modify it under the terms of         modifier suivant les termes de
*  the GNU Affero General Public        la “GNU Affero General Public
*  License as published by the          License” telle que publiée
*  Free Software Foundation,            par la Free Software Foundation
*  either version 3 of the              : soit la version 3 de cette
*  License, or (at your option)         licence, soit (à votre gré)
*  any later version.                   toute version ultérieure.
*
*  OpenCADC is distributed in the       OpenCADC est distribué
*  hope that it will be useful,         dans l’espoir qu’il vous
*  but WITHOUT ANY WARRANTY;            sera utile, mais SANS AUCUNE
*  without even the implied             GARANTIE : sans même la garantie
*  warranty of MERCHANTABILITY          implicite de COMMERCIALISABILITÉ
*  or FITNESS FOR A PARTICULAR          ni d’ADÉQUATION À UN OBJECTIF
*  PURPOSE.  See the GNU Affero         PARTICULIER. Consultez la Licence
*  General Public License for           Générale Publique GNU Affero
*  more details.                        pour plus de détails.
*
*  You should have received             Vous devriez avoir reçu une
*  a copy of the GNU Affero             copie de la Licence Générale
*  General Public License along         Publique GNU Affero avec
*  with OpenCADC.  If not, see          OpenCADC ; si ce n’est
*  <http://www.gnu.org/licenses/>.      pas le cas, consultez :
*                                       <http://www.gnu.org/licenses/>.
*
*  $Revision: 5 $
*
************************************************************************
*/

package ca.nrc.cadc.sia2;

import ca.nrc.cadc.dali.util.UTCTimestampFormat;
import ca.nrc.cadc.date.DateUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 *
 * @author pdowler
 */
public class SiaValidator 
{
    private static final Logger log = Logger.getLogger(SiaValidator.class);
    
    private static final String POS = "POS";
    private static final String BAND = "BAND";
    private static final String TIME = "TIME";
    private static final String POL = "POL";
    private static final String FOV = "FOV";
    private static final String SPATRES = "SPATRES";
    private static final String EXPTIME = "EXPTIME";
    // used by the SiaRunner to pick out supported params only
    static final List<String> QUERY_PARAMS = Arrays.asList(POS, BAND, TIME, POL, FOV, SPATRES, EXPTIME);
    
    // pol_states values are always upper case so use List
    static final List<String> POL_STATES = Arrays.asList("I", "Q", "U", "V", "RR", "LL", "RL", "LR", "XX", "YY", "XY", "YX");
    
    private static final String CIRCLE = "CIRCLE";
    private static final String RANGE = "RANGE";
    private static final String POLYGON = "POLYGON";
    
    public SiaValidator() { }
    
    public List<Shape> validatePOS(Map<String,List<String>> params)
    {
        List<Shape> ret = new ArrayList<Shape>();
        if (params == null)
            return ret;
        List<String> values = params.get(POS);
        if (values == null)
            return ret;
        for (String v : values)
        {
            log.debug("validatePos: " + v);
            String[] tokens = v.split(" ");
            if ( CIRCLE.equalsIgnoreCase(tokens[0]) )
            {
                if (tokens.length != 4)
                    throw new IllegalArgumentException("POS invalid CIRCLE: " + v);
                try
                {
                    double ra = Double.parseDouble(tokens[1]);
                    double dec = Double.parseDouble(tokens[2]);
                    double rad = Double.parseDouble(tokens[3]);
                    ret.add(new CoordCircle(ra, dec, rad));
                }
                catch(NumberFormatException ex)
                {
                    throw new IllegalArgumentException("POS number in: " + v);
                }
            }
            else if (RANGE.equalsIgnoreCase(tokens[0]))
            {
                if (tokens.length != 3)
                    throw new IllegalArgumentException("POS invalid RANGE: " + v);
                try
                {
                    Range<String> s1 = parseStringRange(tokens[1]);
                    Range<String> s2 = parseStringRange(tokens[2]);
                    Range<Double> ra = parseDoubleRange("POS", s1);
                    Range<Double> dec = parseDoubleRange("POS", s2);
                    ret.add(new CoordRange(ra, dec));
                }
                catch(NumberFormatException ex)
                {
                    throw new IllegalArgumentException("POS number in: " + v);
                }
            }
            else if (POLYGON.equalsIgnoreCase(tokens[0]))
            {
                int len = tokens.length - 1;
                if (len < 6)
                    throw new IllegalArgumentException("POS invalid POLYGON (not enough coordinate values): " + v);
                if (len % 2 != 0)
                    throw new IllegalArgumentException("POS invalid POLYGON (odd number of coordinate values): " + v);
                CoordPolygon poly = new CoordPolygon();
                for (int i=1; i<=len; i+=2)
                {
                    try
                    {
                        Double d1 = new Double(tokens[i]);
                        Double d2 = new Double(tokens[i+1]);
                        poly.getVertices().add(new CoordPolygon.Vertex(d1, d2));
                    }
                    catch(NumberFormatException ex)
                    {
                        throw new IllegalArgumentException("POS invalid POLYGON ("+ex+"): " + v);
                    }
                }
                ret.add(poly);
            }
            else
                throw new IllegalArgumentException("POS invalid shape: " + v);
        }
        
        return ret;
    }
    
    public List<Range<Double>> validateTIME(Map<String,List<String>> params)
    {
        List<Range<Double>> ret = new ArrayList<Range<Double>>();
        if (params == null)
            return ret;
        List<String> values = params.get(TIME);
        if (values == null)
            return ret;
        for (String v : values)
        {
            log.debug("validateTime: " + v);
            Range<String> sr = parseStringRange(v);
            ret.add( parseTimeRange(TIME, sr) );
        }
        
        return ret;
    }
    
    public List<String> validatePOL(Map<String,List<String>> params)
    {
        return validateString(POL, params, POL_STATES);
    }
    
    public List<String> validateString(String paramName, Map<String,List<String>> params, Collection<String> allowedValues)
    {
        List<String> ret = new ArrayList<String>();
        if (params == null)
            return ret;
        List<String> values = params.get(paramName);
        if (values == null)
            return ret;
        for (String s : values)
        {
            log.debug("validateString " + paramName + ": " + s);
            if (allowedValues == null)
                ret.add(s);
            else if (allowedValues.contains(s))
                ret.add(s);
            else
                throw new IllegalArgumentException(paramName + " invalid value: " + s);
        }
        return ret;
    }
    
    public List<Range<Double>> validateBAND(Map<String,List<String>> params)
    {
        return validateNumeric(BAND, params);
    }
    public List<Range<Double>> validateFOV(Map<String,List<String>> params)
    {
        return validateNumeric(FOV, params);
    }
    public List<Range<Double>> validateSPATRES(Map<String,List<String>> params)
    {
        return validateNumeric(SPATRES, params);
    }
    public List<Range<Double>> validateEXPTIME(Map<String,List<String>> params)
    {
        return validateNumeric(EXPTIME, params);
    }
    
    List<Range<Double>> validateNumeric(String paramName, Map<String,List<String>> params)
    {
        List<Range<Double>> ret = new ArrayList<Range<Double>>();
        if (params == null)
            return ret;
        List<String> values = params.get(paramName);
        if (values == null)
            return ret;
        for (String v : values)
        {
            log.debug("validateNumeric " + paramName + ": "  + v);
            Range<String> sr = parseStringRange(v);
            ret.add( parseDoubleRange(paramName, sr) );
        }
        
        return ret;
    }
    
    static Range<Double> parseDoubleRange(String pname, Range<String> sr)
    {
        try
        {
            Double lb = null;
            Double ub = null;
            if (sr.getLower() != null)
                lb = new Double(sr.getLower());
            if (sr.getUpper() != null)
                ub = new Double(sr.getUpper());
            return new Range<Double>(lb, ub);
        }
        catch(NumberFormatException ex)
        {
            throw new IllegalArgumentException(pname + " cannot parse to double: " + sr);
        }
    }
    
    static Range<Double> parseTimeRange(String pname, Range<String> sr)
    {
        try
        {
            Range<Double> ret = parseDoubleRange(pname, sr);
            return ret;
        }
        catch(IllegalArgumentException iex)
        {
            UTCTimestampFormat df = new UTCTimestampFormat();
            try
            {
                Double d1 = null;
                Double d2 = null;
                if (sr.getLower() != null)
                {
                    String s = sr.getLower();
                    if (s.indexOf('T') == -1) // no time component
                        s += "T00:00:00.000";
                    Date d = df.parse(s);
                    d1 = new Double(DateUtil.toModifiedJulianDate(d, DateUtil.UTC));
                }
                if (sr.getUpper() != null)
                {
                    String s = sr.getUpper();
                    if (s.indexOf('T') == -1) // no time component
                        s += "T23:59:59.999";
                    Date d = df.parse(s);
                    d2 = new Double(DateUtil.toModifiedJulianDate(d, DateUtil.UTC));
                }
                return new Range<Double>(d1, d2);
            }
            catch(Exception ex)
            {
                throw new IllegalArgumentException(pname + " cannot parse as timestamps " + sr);
            }
            finally { }
        }
    }
    
    static Range<String> parseStringRange(String v)
    {
        int i = v.indexOf('/');
        int j = v.lastIndexOf('/');
        if (i != j)
            throw new IllegalArgumentException("invalid range - found / separator at positions " + i + " and " + j);
        
        if (i == 0 && v.length() == 1)
            return new Range<String>(null, null); // open
        if (i == -1)
            return new Range<String>(v, v); // scalar
        
        String[] sa = v.split("/");
        if (i == 0)
            return new Range<String>(null, sa[1]); // leading zero-length string before /
        if (sa.length == 1)
            return new Range<String>(sa[0], null); // no trailing zero-length after /
        return new Range<String>(sa[0], sa[1]);
    }
}
