#!/bin/tcsh
# turbine.csh
# ljm 060118
#

setenv JAR_LOC '/usr/share/java'

if ($?CLASSPATH == 0) then
  setenv CLASSPATH ''
endif

setenv CLASSPATH ${JAR_LOC}/turbine-3.6.0.jar:${CLASSPATH}
