DALServer VO Data Services Framework V0.9
D.Tody May 2015

D.Tody, R.Plante, M.Cresitello-Dittmar, M.Graham, V.Vekkirala,
O.Pevunova September 2014 (VAO version)
--------------------------------------------------------

This package contains code for implementing IVOA Data Access Layer (DAL)
services.  This version supports SCS (simple cone search), SIAP
(images), SSAP (spectra), and SLAP (spectral line lists).

Full Javadoc-format documentation for the DALServer packages is given in
the "dist/docs" subdirectory.  Additional documentation is given in the
README files in each major source or runtime directory.

A brief summary of what is required to install the DALServer framework
and configure local data services follows.  More detailed documentation
is available on the Web, or via the DALServer administration Web-UI.


Prerequisites
------------------

To install and run the DALServer framework one should already have a
Java application server or servlet container installed, e.g., Apache
Tomcat.  This is sufficient to merely install and run the framework.

To configure any local data services one also needs to have database
management system (DBMS) configured, e.g., MySql, PostgreSql, or Oracle.
To publish a catalog via the cone search protocol, the catalog should
already be loaded into a database table.  To publish an image or
spectral data collection, an index table is required containing metadata
describing the datasets comprising the data collection to be published.


Installation
------------------

To install DALServer, merely download the DALServer distribution tarball
from the Web, unpack it (unpacking to /opt/services/dalserver would
simplify things later, but is not required) and deploy the provided
"ivoa-dal.war" app file to your local application server, e.g., Tomcat.
How a Web-app is deployed depends upon the application server, but this
is typically done using the administration Web GUI of the application
server, or by merely copying the Web-app file to the "webapps" directory
of the installed application server.  By default the application will be
automatically deployed and available for use.

The DALServer console Web-UI should then be available at the following
URL:

    http://<host>[:<port]/ivoa-dal

e.g., http://localhost:8080/ivoa-dal would work for the usual case where
the application server is running on the local host computer on the
default port, 8080.

The generic DALServer application contains several builtin services that
may then be run via the Web UI to verify that everything is working
correctly.  These builtin test/demo service instances exercise the same
generic service code that will be used for any real data services you
add later.


Configuration
------------------

Service configuration is performed by editing 1 or 2 service
configuration text files, typically be copying and modifying an existing
configuration or a template file.

Local configuration data is stored externally to the DALServer Webapp
(ivoa-dal.war) to ensure that it is not lost when a new version of the
Webapp is installed.  By default local configuration data is stored in
the directory "/opt/services/dalserver" on the server running the Java
application server (refer to the instructions given in the detailed
documentation if you need to use a different directory).  If you
unpacked the DALServer tarball into this directory earlier, you will
already have sample and template configuration files available in this
directory, ready to be used to configure new services.

Every service instance requires at least a service configuration
(".conf") file.  Each service may optionally have a table configuration
(".tab") file, used to customize the metadata data to be returned by the
service, or the interface to the DBMS table used to drive the service.
A global "server.conf" file defines the global DALServer configuration
and points to the locally added data services.

An example of a local service configuration file follows:

    README               server.conf             siapv2-vlafirst.conf
    reload.conf          siap-null.conf          siapv2-vlafirst.tab
    scs-messier.conf     siapv1-vlafirst.conf    slap-null.conf
    scs-vlafirst.conf    siapv1-vlafirst.tab     ssap-null.conf
    scsVaoImage.conf     siapv2-vao.conf
    scsVaoImage.tab      siapv2-vao.tab

For example, if we wanted to configure a new cone service for the Abell
catalog, we would add a new service configuration file, e.g.,
"scs-abell.conf", optionally a table configuration file as well
("scs-abell.tab"), and add a line to server.conf telling it to create a
service instance for the new cone search service.

Once the service configuration has been edited, the DALServer "reload"
resource is used to reload the local configuration (for all service
instances) [note - we need to add login/password authentication to
prevent unauthorized use of reload].  For example,

    http://localhost:8080/ivoa-dal/reload

A reload should be performed whenever the configuration changes or
whenever a new version of the DALServer Web-app is installed.  If
"reload" executes successfully it prints a summary of the new service
configuration, e.g.:

    WebApp: DALServer Framework Test Configuration

    reload		0 parameters
    scs-messier		12 parameters
    siap-null		3 parameters
    ssap-null		3 parameters
    scsVaoImage		14 parameters tableconfig
    scs-vlafirst	13 parameters
    siapv2-vlafirst	18 parameters tableconfig
    siapv1-vlafirst	18 parameters tableconfig
    siapv2-vao		20 parameters tableconfig

    Successfully created 9 servlets
    2014/08/21 15:12:16

More detailed instructions for configuring new services are given in the
DALServer documentation.


Data Ingest
------------------

[TBA - Describe how to use data collection ingest tools]


Custom Extensions
------------------

[TBA - Basically, install the source, modify or extend as desired, and
rebuild using Java Ant.  The prebuilt ivoa-dal.war includes full Javadoc
documentation for the DALServer classes.  Note, a custom build is not
required for normal use of DALServer.]

