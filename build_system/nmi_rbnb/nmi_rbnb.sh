#!/bin/sh
# A simple wrapper script to debug and manage the environment
# $LastChangedDate: 2007-10-03 18:30:42 -0500 (Wed, 03 Oct 2007) $
# $LastChangedRevision: 164 $
# $LastChangedBy: ljmiller $
# $HeadURL: http://nladr-cvs.sdsc.edu/svn-public/CLEOS/nmi_rbnb/nmi_rbnb.sh $

MYPWD=`pwd`
export JMFHOME="${MYPWD}/JMF-2.1.1e"
export CLASSPATH="${CLASSPATH}:${JMFHOME}/lib/jmf.jar"
chmod 755 trunk/Source/DataTurbine/jsharp_code_prep_script
echo 'MATLAB:' ${MATLAB}
ant -buildfile trunk/Source/build.xml clean compile
