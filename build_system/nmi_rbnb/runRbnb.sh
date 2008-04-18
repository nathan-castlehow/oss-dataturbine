#!/bin/sh 
# a script to do nmi runs
# $LastChangedDate$
# $LastChangedRevision$
# $LastChangedBy$
# $HeadURL$

. /nmi/bin/config.sh && nmi_submit $HOME/nmi_rbnb/cleosRbnb.submit 2>&1 > $HOME/nmi_rbnb/lastRbnbBuild.log
