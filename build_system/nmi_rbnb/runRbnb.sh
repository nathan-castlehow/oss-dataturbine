#!/bin/sh 
# a script to do nmi runs
# $LastChangedDate: 2007-11-09 15:21:54 -0800 (Fri, 09 Nov 2007) $
# $LastChangedRevision: 200 $
# $LastChangedBy: ljmiller $
# $HeadURL: file:///Users/hubbard/code/cleos-svn/nmi_rbnb/runRbnb.sh $

. /nmi/bin/config.sh && nmi_submit $HOME/nmi_rbnb/cleosRbnb.submit 2>&1 > $HOME/nmi_rbnb/lastRbnbBuild.log
