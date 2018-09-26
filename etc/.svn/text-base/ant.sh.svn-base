#! /bin/sh
export ANT_HOME=
[ -z "$ANT_HOME" ] && {
    dalserver_home=`dirname $0`
    [ -z "$dalserver_home" ] && dalserver_home=.
    ANT_HOME=`ls -d ${dalserver_home}/apache-ant-1.[89].* | grep -v '.zip$' | sort | tail -n 1`
}
PATH=${ANT_HOME}/bin:$PATH

exec ${ANT_HOME}/bin/ant $@
