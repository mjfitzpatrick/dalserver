/*
************************************************************************
*******************  CANADIAN ASTRONOMY DATA CENTRE  *******************
**************  CENTRE CANADIEN DE DONNÉES ASTRONOMIQUES  **************
*
*  (c) 2011.                            (c) 2011.
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

package ca.onfire.sample;

import ca.onfire.ak.AbstractApplication;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.io.File;

/**
 *
 * @author pdowler
 */
public class SampleApp extends AbstractApplication
{
    public static final String NAME = "CADC Java Test";
    
    private static String[] props = 
    {
        "java.home", 
        "java.runtime.name", "java.runtime.version",
        "java.vendor", "java.version",
        "java.vm.name", "java.vm.vendor", "java.vm.version",
        "os.arch", "os.name", "os.version",
        "user.dir", "user.home", "user.language", "user.name"
    };
    
    public SampleApp(String[] args)
    {
        super(new BorderLayout());
    }

    public boolean quit() { return getConfirmation("OK to quit?"); }

    protected void makeUI()
    {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        
        p.add( adorn(new JTextDisplay(getSystemProperties()), "Java System Properties"));
        p.add( adorn(new JTextDisplay(getFileSystemProps()), "File System Properties"));
        p.add( adorn(new JTextDisplay(getMemoryConfig()), "Java Memory Configuration"));
        this.add(p);
    }

    private JComponent adorn(JComponent comp, String title)
    {
        comp.setBorder(new TitledBorder(new LineBorder(Color.BLACK, 1), title));
        comp.setPreferredSize(new Dimension(400, 200));
        return comp;
    }
    
    private String getMemoryConfig()
    {
        return "total memory: " + bytesToString(Runtime.getRuntime().totalMemory()) + "\n"
            + "free memory: " + bytesToString(Runtime.getRuntime().freeMemory()) + "\n"
            + "maximum heap size: " + bytesToString(Runtime.getRuntime().maxMemory()); 
    }
    private String bytesToString(long nb)
    {
        nb /= 1024;
        if (nb < 10000)
            return nb + "KB";
        nb /= 1024;
        return nb + "MB";
    }
    private String getFileSystemProps()
    {
        StringBuffer sb = new StringBuffer();
        
        checkReadWrite("user.home", sb);
        checkReadWrite("java.io.tmpdir", sb);
        
        return sb.toString();
    }
    
    
    private void checkReadWrite(String systemProp, StringBuffer sb)
    {
        String dirName = null;
        try { dirName = System.getProperty(systemProp); }
        catch(SecurityException oops) 
        { 
            sb.append(oops.toString()).append("\n"); 
        }
        
        File dir = new File(dirName);
        try
        {
            if (dir.canRead())
            {
                String[] dirListing = dir.list();
                sb.append(systemProp).append(" ").append(dirName).append(" is readable\n");
            }
            else
                sb.append(systemProp).append(" ").append(dirName).append(" is not readable (filesystem permissions)\n");
        }
        catch(Exception oops)  
        { 
            sb.append(systemProp).append(" ").append(dirName).append(" is not readable: ").append(oops.toString()).append("\n"); 
        }
        
        try
        {
            if (dir.canWrite())
            {
                File tmp = File.createTempFile("CADC_test", null, dir);
                tmp.delete();
                sb.append(systemProp).append(" ").append(dirName).append(" is writable\n");
            }    
            else
                sb.append(systemProp).append(" ").append(dirName).append(" is not writable (filesystem permissions)\n");
        }
        catch(Exception oops) 
        {
            sb.append(systemProp).append(" ").append(dirName).append(" is not writable: ").append(oops.toString()).append("\n"); 
        }
    }
    
    private String getSystemProperties()
    {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < props.length; i++)
        {
            try
            {
                String val = System.getProperty(props[i]);
                sb.append(props[i]).append(":  ").append(val).append("\n");
            }
            catch(Exception oops)
            {
                sb.append("failed to read property ").append(props[i]).append(": ").append(oops.toString()).append("\n");
            }

        }
        return sb.toString();
    }
}
