/*
************************************************************************
*******************  CANADIAN ASTRONOMY DATA CENTRE  *******************
**************  CENTRE CANADIEN DE DONNÉES ASTRONOMIQUES  **************
*
*  (c) 2009.                            (c) 2009.
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
*  $Revision: 4 $
*
************************************************************************
*/

/*
 *  InlineContentHandlerImpl.java
 *  $ID*
 */


package dalserver.tap;

import ca.nrc.cadc.uws.JobInfo;
import ca.nrc.cadc.uws.Parameter;
import ca.nrc.cadc.uws.web.InlineContentException;
import ca.nrc.cadc.uws.web.InlineContentHandler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * Basic ResultStore implementation usable by the cadcTAP QueryRunner for async
 * result storage. This implementation simply writes files to a local directory
 * and then returns a URL to that file. The filesystem-to-URL mapping is
 * configurable via two parameters passed via the service configuration.
 *
 * @author Mike Fitzpatrick (based upon the OpenCADC SampleTAP by pdowler)
 */
        
public class InlineContentHandlerImpl implements InlineContentHandler
{
    private static Logger log=Logger.getLogger(InlineContentHandlerImpl.class);

    private static final String TEXT_XML = "text/xml";
    private static final String APP_XML = "application/x-votable+xml";

    private List<Parameter> parameterList;
    private JobInfo jobInfo;


    public InlineContentHandlerImpl() { }

    public void setParameterList(List<Parameter> parameterList)
    {
        this.parameterList = parameterList;
    }

    public List<Parameter> getParameterList()
    {
        if (parameterList == null)
            parameterList = new ArrayList<Parameter>();
        return parameterList;
    }

    public JobInfo getJobInfo()
    {
        return jobInfo;
    }

    public URL accept(String name, String contentType, InputStream inputStream)
        throws InlineContentException, IOException
    {
        if (inputStream == null)
            throw new IOException("The InputStream is closed");

        log.debug("Content-Type: " + contentType);
        if ( contentType != null && 
	    (contentType.equals(TEXT_XML) || contentType.equals(APP_XML)) )
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null)
                sb.append(line).append("\n");
            jobInfo = new JobInfo(sb.toString(), contentType, true);
        }
        return null;
    }

}
