#!/usr/bin/sh

. ${ov.home}/bin/ov.envvars.sh

echo $PWD >> ${opennmsd.home}/output.log 2>&1
ulimit -a >> ${opennmsd.home}/output.log 2>&1
env >> ${opennmsd.home}/output.log 2>&1
echo "Starting opennmsd" >> ${opennmsd.home}/output.log 2>&1

exec ${jre.bin} -Dlog4j.configuration=file:${opennmsd.home}/log4j.properties -Dnnm.jni.library=${opennmsd.home}/libNNM.so -jar ${opennmsd.home}/opennmsd.jar ${opennmsd.home}/opennmsd.xml $OV_CONF/C/trapd.conf >>${opennmsd.home}/output.log 2>&1