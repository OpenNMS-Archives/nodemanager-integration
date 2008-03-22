#!/usr/bin/sh

exec >> ${opennmsd.log}/opennmsd.out 2>&1

. ${ov.home}/bin/ov.envvars.sh

echo $PWD 
ulimit -a
env 
echo "Starting opennmsd" 

exec ${jre.bin} -Dnnm.api.logLevel=5 -Dlog4j.configuration=file:${opennmsd.home}/log4j.properties -Dnnm.jni.library=${opennmsd.home}/libNNM.so -jar ${opennmsd.home}/opennmsd-${project.version}.jar ${opennmsd.home}/opennmsd.xml $OV_CONF/C/trapd.conf 
