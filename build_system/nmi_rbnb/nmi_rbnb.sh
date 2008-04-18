#!/bin/sh
# A simple wrapper script to debug and manage the environment
# $LastChangedDate$
# $LastChangedRevision$
# $LastChangedBy$
# $HeadURL$

MYPWD=`pwd`
export JMFHOME="${MYPWD}/JMF-2.1.1e"
export CLASSPATH="${CLASSPATH}:${JMFHOME}/lib/jmf.jar"
chmod 755 trunk/Source/DataTurbine/jsharp_code_prep_script
echo 'MATLAB:' ${MATLAB}
ant -buildfile trunk/Source/build.xml clean compile
