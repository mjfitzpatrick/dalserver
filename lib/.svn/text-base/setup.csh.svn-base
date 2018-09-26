#! /bin/csh -f
#
# Set up the Java environment for VOClient / dalserver
#
if (! $?JAVA_HOME) then
    echo -n "Where is Java installed?"
    set ans = "$<"
    if ($ans != "") setenv JAVA_HOME $ans
endif

if (! $?NVOSS_HOME) then
    echo -n "Where is the top level directory of your NVOSS distribution?"
    set ans = "$<"
    if ($ans != "") setenv NVOSS_HOME $ans
endif

# Local DALServer development directory
setenv DAL_HOME /u2/dtody/zz/dalserver

setenv ANT_HOME $NVOSS_HOME/java/apache-ant-1.6.5
setenv CATALINA_HOME $NVOSS_HOME/java/apache-tomcat-5.5.17
setenv AXIS_DEPLOY $CATALINA_HOME/webapps/axis
setenv WEB_DEPLOY $CATALINA_HOME/webapps

set path = ($JAVA_HOME/bin $NVOSS_HOME/bin $ANT_HOME/bin $path)

setenv ALIB $NVOSS_HOME/java/axis-1_2_1/lib
set lib = $NVOSS_HOME/java/lib

setenv CLASSPATH .:./classes:$DAL_HOME/lib/cds.jar:$DAL_HOME/build/WEB-INF/classes

# add the other needed jars
setenv CLASSPATH ${CLASSPATH}:$lib/xercesImpl.jar:$lib/xmlParserAPIs.jar:$lib/mail.jar:$lib/activation.jar:$lib/junit.jar:${CATALINA_HOME}/common/lib/servlet-api.jar

echo NVOSS_HOME = $NVOSS_HOME
echo JAVA_HOME = $JAVA_HOME
echo ANT_HOME = $ANT_HOME
echo CATALINA_HOME = $CATALINA_HOME

echo All set up for DAL Java development

