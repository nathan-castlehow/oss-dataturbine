#!/bin/bash
# Script to grab a single image from the Axis at SMER northern gorge.
# PFH 1/28/08, modified 4/11/08 for use at calit2
# No error checking, just stupid for first pass. Run from cron, is the plan.
# 5/22/08 Added directory error checking.

TARGET_DIR=/Volumes/RBNB
SRC_NAME="SMER camera"

if [ ! -d $TARGET_DIR ]
then
 echo "Unable to find RBNB filesystem, please mount from finder."
 exit 1
fi

cd $TARGET_DIR
if [ ! -d $TARGET_DIR/$SRC_NAME ]
then
 mkdir -p "SMER camera"@"a=8064&c=48"
fi

if [ ! -d $TARGET_DIR/$SRC_NAME ]
then
 echo "Unable to create source directory, crashing out"
 exit 2
fi

# Save image to temp directory
mkdir -p /tmp/smer
cd /tmp/smer
curl -s -O -f http://172.23.37.63/jpg/image.jpg

# move back using duration and timestamp metadata
# LJM 080424 original command
# mv image.jpg /Volumes/RBNB/"SMER camera"/image.jpg@"t=`date +%s`&d=300&r=newest"
mv image.jpg /Volumes/RBNB/"SMER camera"/image.jpg

touch /tmp/last-smer-fetch
