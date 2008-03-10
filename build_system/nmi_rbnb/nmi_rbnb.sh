#!/bin/sh
# A simple wrapper script to debug and manage the environment
# $LastChangedDate: 2007-10-03 16:30:42 -0700 (Wed, 03 Oct 2007) $
# $LastChangedRevision: 164 $
# $LastChangedBy: ljmiller $
# $HeadURL: file:///Users/hubbard/code/cleos-svn/nmi_rbnb/nmi_rbnb.sh $

MYPWD=`pwd`
export JMFHOME="${MYPWD}/JMF-2.1.1e"
export CLASSPATH="${CLASSPATH}:${JMFHOME}/lib/jmf.jar"
chmod 755 trunk/Source/DataTurbine/jsharp_code_prep_script
ant -buildfile trunk/Source/build.xml clean compile
