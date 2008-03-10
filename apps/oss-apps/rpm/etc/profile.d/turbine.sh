#
# turbine.sh
# Builds the default Java classpath to have neesit jars on it 
# intended usage is for turbine.jar, turbine-dev.jar, and their dependencie
# ljm 060118
#
JAR_LOC='/usr/share/java'
CLASSPATH="${JAR_LOC}/turbine-3.6.0.jar:${CLASSPATH}"
export CLASSPATH
