#!/bin/csh
# Script to start up an RBNB instance, 512MB of memory, archives in /var/rbnb
# parent-child routed to niagara-stage.sdsc.edu, with archives reloaded if possible.
# The Tomcat invocation is commented out but can be invoked as required.
# Dirty/quick hack code.
# NOTE: You still need to mount the DAV filesystem before the cron jobs will work!

cd /var/rbnb/archives

echo -n "Starting RBNB server...."
rm -f ../child-server-log.txt

nohup java -Xmx512M -jar $RBNB_HOME/bin/rbnb.jar -F -n "Aux video" -p niagara-stage.sdsc.edu >& ../child-server-log.txt &

echo " done."

echo -n "Starting Tomcat..."
cd $RBNB_HOME/bin
./Start_Webserver.sh
echo "done."

echo -n "Sleeping while Tomcat starts up...."
sleep 10

echo "OK."

echo -n "Mounting DAV filesystem..."
mount_webdav http://localhost:8080/RBNB/ /Volumes/RBNB/

