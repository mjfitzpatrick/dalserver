/**
 *
Implements the server side of the VO data access layer (DAL) interfaces.

<p>This package contains Java code for implementing IVOA Data
Access Layer (DAL) services.  The dalserver package provides two
main top level externally-callable classes for use in writing
DAL services: <i>RequestParams</i>, and <i>RequestResponse</i>.
{@link dalserver.RequestParams} is used to read an HTTP GET or
POST request and render the request as a fully processed service
parameter set (e.g., for a specific service such as SSAP).  {@link
dalserver.RequestResponse} and the related dalserver classes are used
to build the response to the request.  The response is built up as
a data model in memory and subsequently serialized, for example as
a VOTable, to respond to the request. </p>

<p>The "business logic" for the service (the part which differs
locally) is responsible for reading the local DBMS or data collection,
and computing the metadata which will be returned in response to the
request.  For example, in the case of SSAP, the service should subclass
the {@link dalserver.ssa.SsapService} class to provide these functions
for the local environment.  This class is then used in the {@link
dalserver.ssa.SsapServlet} class to build a servlet implementing the service.

<h2>Package Specification</h2>

<ul>
  <li><a href="http://www.ivoa.net/Documents/latest/SSA.html">
  Simple Spectral Access protocol</a>
  <li><a href="http://www.ivoa.net/Documents/latest/SIA.html">
  Simple Image Access protocol</a>
</ul>

<h2>Related Documentation</h2>

For overviews, tutorials, examples, guides, and tool documentation, please see:
<ul>
  <li><a href="http://www.ivoa.net/twiki/bin/view/IVOA/IvoaDAL">
  IVOA Data Access Layer TWiki</a>
</ul>
 *
 */
package dalserver;
