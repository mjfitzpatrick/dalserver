# DALServer VO Data Service Framework Configuration Files
-----------------------------------------------------------------

The files in this directory provide a working example of how to
dyamically configure the DALServer framework for a local installation.

[NOTE - The current contents of this directory are not what we want to
give users in the final version!  Currently this is just a placeholder
containing a snapshot of my development configuration directory, for
use as an example.]


    ivoa-dal.war	   This is a pre-built copy of the DALServer Web-app.

    server.conf            Root configuration file
    <servlet>.conf         Configuration file for the named servlet
    <servlet>.tab          Table config file for the servlet (optional)

When the DALServer framework is told to reload the service configuration
(there are various ways this can be triggered), it reads the root
"server.conf" file.  All other servlet or table configuration files
referenced directly or indirectly from server.conf are read to build the
final runtime configuration.  Any other files in the configuration
directory (e.g. old or alternate versions of configurations) are ignored.
The default file names and the location of the configuration directory can
be modified from what is shown here if necessary.


Quick Start
------------

The following steps will suffice to get up and going quickly.   This
assumes that you already have a Java applications server or servlet
container (e.g., Apache Tomcat) installed on your system using the default
configuration.

    o   Copy the ivoa-dal.war file to your Web application directory.
	This is the "webapps" subdirectory in the Tomcat root, also known
	as $WEB_DEPLOY if you have the environment configured (or just use
	the application server management Web UI, if available, to install
	the new Web-app).  Give the server a few seconds to deploy the
	Web-app.

    o	Set up your local DALServer configuration directory.  The default
    	location of this is /opt/services/dalserver, but it can be moved
	if necessary (instructions for this will be provided elsewhere).

    o	In your Web browser, go to:

	    http://localhost:8080/ivoa-dal/reload

	This should load all the servlets defined in the server.conf file
	in your configuration directory.

    o   The provided test services should now be active and can be
	accessed via their standard VO REST Web service interface.  The
	page http://localhost:8080/ivoa-dal can also be used to try out the
	included pre-built demonstration/test servlets.  Use these to
	verify that things are working normally [in the current development
	version some of these are broken, i.e, not fully configured].

You can now try building your own service.  For example, to add a cone
search service, add a "<servlet-name>.conf" file, add the servlet name to
the end of your server.conf file, and then reload the configuration as
outlined above.  You can optionally add a "<servlet-name>.tab" file to
customize the table output by the service, if desired.  This assumes that
you already have a DBMS table loaded to be exposed via the VO cone search
protocol.


Examples
---------

The following should normally be left alone, but may be commented out if
not needed:

    scs-messier.conf       Built-in demo/test Messier catalog cone search servlet
    ssap-null.conf         Built-in demo/test SSA null query servlet
    slap-null.conf         Built-in demo/test SLA null query servlet

Sample data service configuration files:

    scs-abell.conf         Old NVO summer school service (uses remote DBMS)
    siapv1-nvoss.conf      Old NVO summer school service (uses remote DBMS)
    ssap-jhu.conf          Old JHU/SDSS proxy SSA reference service

    siapv2-vao.conf        Fall 2013 VAO SIAV2 prototype service
    siapv2-vlafirst.conf   NRAO VLA-FIRST survey prototype SIAV2 service
    siapv2-vlafirst.tab    Image table configuration for the above


Configuration Files
-------------------

The service and servlet configuration files are pretty self-explanatory
and contain inline comments where needed.

The table configuration file is more complicated.  A fully-commented
example follows below.  The actual table configuration file instances
provided here omit most of the comments.

The simplest way to understand the format of config files is to look at
the examples, but some details are covered here.  The format (INI) for
all these files consists of a sequence of contexts, each containing zero
or more parameters.  The beginning of a context is indicated with a line
that starts with "[context-name]", and ends either when a new context
begins, or at EOF.  Context markers ("[context-name]") should be alone
on a line and may not contain whitespace.

Parameters are of the form "keyword = value".  The whitespace
surrounding the "=" may not be omitted (this is done to allow "=" to be
embedded in parameter values, e.g., "fieldname  ucd=x utype=y").  The
"=" may normally be omitted, but is recommended for readability.  The
exception is where the value begins on the next line, in which case the
"=" must be given, with nothing (including a comment) on the rest of the
line.  The value text follows on the next line and is terminated by the
first blank line.  Whitespace at the beginning of continuation lines in
the value block is ignored, i.e., replaced by a single space.

Comments begin with a " #" and extend to the end of the current line.
Comments at the end of parameter lines are permitted, e.g., "keyword =
value # comment [EOL]".  The comment character is only recognized as
beginning a comment if it is preceded by whitespace or EOL, hence '#'
can be embedded in a block of text such as a parameter value so long as
their is no whitespace before the '#'.


========================================================================
========== Sample Table Configuration File (Fully Commented)  ==========
========================================================================

# SIA Image table configuration example

[image-table]

# NRAO VLA FIRST Survey - SIAV2 Prototype Servlet
# This V2 prototype (spring 2014) uses an ObsCore-based Image table.
# --------------------------------------------------------------------------- 

table-name = e2emgr.first_image
table-class = image.vao.nov2013
description = 
    Interim image table used for the fall 2013 VAO SIAV2 prototype.


[standard-fields]

# This section defines the usage of standard fields of the DALServer-defined
# standard Image table.  Note that an Image table instance must include all
# standard fields, even if only null values are supplied for a given field.
# Entries are required here only where the Image table instance differs from
# the standard image table schema.  This includes:
#
#   1) Inclusion of additional standard metadata from the ImageDM
#   2) Usage of non-standard column names
#
# Any standard field of the Image Data Model may be added to this section
# to provide additional standard metadata.  Such standard but optional
# metadata will be propagated to the query response, with the framework
# supplying the UCD, Utype, etc. values defined by the data model.
#
# If the instance table uses a non-standard column name for a standard Image
# table field the column reference should be defined as follows:
#
#   <dm-field-name> [=] col=<colname>
#
# This allows the column name used in the instance table to differ from
# that defined by the Image table specification.
# ---------------------------------------------------------------------------

# Example of column renaming:

dataproduct_type	col=type
dataproduct_subtype	col=subtype

# Example of adding additional standard metadata.  The table instance used must
# contain the named field (added fields may also be renamed, as above). 

obs_logo
contact_email		col=email ucd=xxx [etc.]
target_desc
target_redshift
s_resolution_min
s_resolution_max
em_bandpass
t_midpoint

# To omit a standard field use the attribute "omit":
t_resolution	omit

# If standard fields already present are "added" only a single instance will
# be output:

obs_id
archive_id


[custom-fields]

# Any provider-defined custom fields should be defined here.
# Any VOTable attributes may be provided, as well as "description".
# The VOTable field NAME attribute defaults to the image table column name (at
# the left); DATATYPE defaults to char, otherwise a value must be given.
# All other attributes (ID, UCD, UTYPE, UNIT, etc.) are optional.
# Long attribute names may be abbreviated to 3 or more characters.
# ---------------------------------------------------------------------------

project_code =		# Use all default attributes
file_set_id =		name=fileset_id datatype=char ucd=meta.id
