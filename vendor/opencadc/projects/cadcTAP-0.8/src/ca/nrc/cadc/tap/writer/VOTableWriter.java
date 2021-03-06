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

package ca.nrc.cadc.tap.writer;

import ca.nrc.cadc.tap.TableWriter;
import ca.nrc.cadc.tap.schema.FieldRef;
import ca.nrc.cadc.tap.schema.GroupDesc;
import ca.nrc.cadc.tap.writer.votable.TableDataElement;
import ca.nrc.cadc.tap.writer.votable.TableDataXMLOutputter;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.util.List;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import ca.nrc.cadc.tap.schema.ParamDesc;
import ca.nrc.cadc.tap.schema.TapSchema;
import ca.nrc.cadc.tap.writer.formatter.DefaultFormatterFactory;
import ca.nrc.cadc.tap.writer.votable.FieldElement;
import ca.nrc.cadc.tap.writer.formatter.Formatter;
import ca.nrc.cadc.tap.writer.formatter.FormatterFactory;
import ca.nrc.cadc.tap.writer.votable.FieldRefElement;
import ca.nrc.cadc.tap.writer.votable.GroupElement;
import ca.nrc.cadc.tap.writer.votable.ParamElement;
import ca.nrc.cadc.uws.Job;
import ca.nrc.cadc.uws.Parameter;
import ca.nrc.cadc.uws.ParameterUtil;
import org.apache.log4j.Logger;

/**
 * Writes a ResultSet or Throwable in VOTABLE format
 * to an OutputStream.
 * 
 */
public class VOTableWriter implements TableWriter
{
    // VOTable Version number.
    public static final String VOTABLE_VERSION  = "1.2";

    // Uri to the XML schema.
    public static final String XSI_NS_URI = "http://www.w3.org/2001/XMLSchema-instance";

    // Uri to the VOTable schema.
    public static final String VOTABLE_11_NS_URI = "http://www.ivoa.net/xml/VOTable/v1.1";
    public static final String VOTABLE_12_NS_URI = "http://www.ivoa.net/xml/VOTable/v1.2";

    // Uri to the STC schema.
    public static final String STC_NS_URI = "http://www.ivoa.net/xml/STC/v1.30";

    private static Logger log = Logger.getLogger(VOTableWriter.class);

    // List of names used in the select statement.
    protected List<ParamDesc> selectList;
    
    // List of GROUP metadata.
    protected List<GroupElement> groups;
    protected TapSchema tapSchema;
    
    protected Job job;

    protected String info;

    // Maximum number of rows to write.
    protected int maxRows;

    //
    public VOTableWriter()
    {
        maxRows = Integer.MAX_VALUE;
    }

   /**
     * @deprecated
     */
    public void setJobID(String jobID) { }

    /**
     * @deprecated
     */
    public void setParameterList(List<Parameter> params) { }

    /**
     * @deprecated
     */
    public void setTapSchema(TapSchema tapSchema) { this.tapSchema = tapSchema; }

    public void setJob(Job job)
    {
        this.job = job;
    }

    /**
     * The info is currently not used by this class. TODO: add an INFO element to the VOTable.
     *
     * @param info
     */
    public void setQueryInfo(String info)
    {
        this.info = info;
    }

    public String getExtension()
    {
        return "xml";
    }

    public String getContentType()
    {
        // if caller requested a custom format that is a valid mimetype, use that
        String fmt = ParameterUtil.findParameterValue("FORMAT", job.getParameterList());
        if (fmt != null && fmt.startsWith("text/xml")) // HACK: good enough for now
            return fmt;
        return "application/x-votable+xml";
    }

    public void setSelectList(List<ParamDesc> items)
    {
        this.selectList = items;
    }
    
    public void setMaxRowCount(int count)
    {
        this.maxRows = count;
        log.debug("maxRows: " + maxRows);
    }

    public void write(ResultSet resultSet, OutputStream output)
        throws IOException
    {
        if (selectList == null)
            throw new IllegalStateException("SelectList cannot be null, set using setSelectList()");
        FormatterFactory factory = DefaultFormatterFactory.getFormatterFactory();
        factory.setJobID(job.getID());
        factory.setParamList(job.getParameterList());
        List<Formatter> formatters = factory.getFormatters(selectList);

        if (resultSet != null)
            try { log.debug("resultSet column count: " + resultSet.getMetaData().getColumnCount()); }
            catch(Exception oops) { log.error("failed to check resultset column count", oops); }
        
        Document document = createDocument();
        Element root = document.getRootElement();
        Namespace namespace = root.getNamespace();

        // Create the RESOURCE element and add to the VOTABLE element.
        Element resource = new Element("RESOURCE", namespace);
        resource.setAttribute("type", "results");
        root.addContent(resource);

        // Create the INFO element and add to the RESOURCE element.
        Element info = new Element("INFO", namespace);
        info.setAttribute("name", "QUERY_STATUS");
        info.setAttribute("value", "OK");
        resource.addContent(info);

        // Create the TABLE element and add to the RESOURCE element.
        Element table = new Element("TABLE", namespace);
        resource.addContent(table);

        // Add the metadata elements.
        for (ParamDesc paramDesc : selectList)
        {
            table.addContent(new FieldElement(paramDesc, namespace));
        }
        
        for (ParamDesc paramDesc : selectList)
        {
            if (paramDesc.id != null)
            {
                log.debug("found ID: " + paramDesc.id + " " + paramDesc);
                GroupDesc g = tapSchema.getGroupDescs().get(paramDesc.id);
                if (g != null)
                {
                    GroupElement ge = new GroupElement(namespace);
                    ge.setAttribute("utype", g.getUtype());
                    for (ParamDesc p : g.params)
                    {
                        ge.addContent(new ParamElement(p, namespace));
                    }
                    for (FieldRef r : g.refs)
                    {
                        ge.addContent(new FieldRefElement(r.getRef(), namespace));
                    }
                    // TODO: recursive group add
                    table.addContent(ge);
                }
                else
                    log.warn("no GroupDesc for " + paramDesc);
            }
        }

        // Create the DATA element and add to the TABLE element.
        Element data = new Element("DATA", namespace);
        table.addContent(data);

        // Create the TABLEDATA element and add the to DATA element.
        Element tableData = new TableDataElement(resultSet, formatters, namespace);
        data.addContent(tableData);

        // Write out the VOTABLE.
        XMLOutputter outputter = new TableDataXMLOutputter(maxRows);
        outputter.setFormat(Format.getPrettyFormat());
        outputter.output(document, output);
    }

    /**
     *
     * @param thrown
     * @param output
     * @throws IOException
     */
    public void write(Throwable thrown, OutputStream output)
        throws IOException
    {
        Document document = createDocument();
        Element root = document.getRootElement();
        Namespace namespace = root.getNamespace();
        
        // Create the RESOURCE element and add to the VOTABLE element.
        Element resource = new Element("RESOURCE", namespace);
        resource.setAttribute("type", "results");
        root.addContent(resource);

        // Create the INFO element and add to the RESOURCE element.
        Element info = new Element("INFO", namespace);
        info.setAttribute("name", "QUERY_STATUS");
        info.setAttribute("value", "ERROR");
        info.setText(getThrownExceptions(thrown));
        resource.addContent(info);

        // Write out the VOTABLE.
        XMLOutputter outputter = new XMLOutputter();
        outputter.setFormat(Format.getPrettyFormat());
        outputter.output(document, output);
    }
    
    protected Document createDocument()
    {
        // the root VOTABLE element
        Namespace vot = Namespace.getNamespace(VOTABLE_12_NS_URI);
        Namespace xsi = Namespace.getNamespace("xsi", XSI_NS_URI);
        Element votable = new Element("VOTABLE", vot);
        votable.setAttribute("version", VOTABLE_VERSION);
        votable.addNamespaceDeclaration(xsi);
        
        Document document = new Document();
        document.addContent(votable);
        
        return document;
    }

    // Build a String containing the nested Exception messages.
    private String getThrownExceptions(Throwable thrown)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(thrown.getClass().getSimpleName());
        sb.append(": ");
        if (thrown.getMessage() == null)
            sb.append("");
        else
            sb.append(thrown.getMessage());
        while (thrown.getCause() != null)
        {
            thrown = thrown.getCause();
            sb.append(" ");
            sb.append(thrown.getClass().getSimpleName());
            sb.append(": ");
            if (thrown.getMessage() == null)
                sb.append("");
            else
                sb.append(thrown.getMessage());
        }
        return sb.toString();
    }

}
