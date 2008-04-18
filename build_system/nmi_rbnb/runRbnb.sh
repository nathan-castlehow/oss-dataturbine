#!/bin/sh 
# a script to do nmi runs
# $LastChangedDate: 2007-11-09 17:21:54 -0600 (Fri, 09 Nov 2007) $
# $LastChangedRevision: 200 $
# $LastChangedBy: ljmiller $
# $HeadURL: http://nladr-cvs.sdsc.edu/svn-public/CLEOS/nmi_rbnb/runRbnb.sh $

. /nmi/bin/config.sh && nmi_submit $HOME/nmi_rbnb/cleosRbnb.submit 2>&1 > $HOME/nmi_rbnb/lastRbnbBuild.log
