#!/bin/sh

RBNBPI=${RBNB_HOME}/apache-tomcat-5.5.12/webapps/webTurbine/WEB-INF/classes

java -cp ${RBNBPI}:${RBNB_HOME}/bin/rbnb.jar TrackDataPlugIn -d

