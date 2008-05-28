#!/bin/bash
# Script to grab a single image from the Axis at SMER northern gorge.
# PFH 1/28/08, modified 4/11/08 for use at calit2
# No error checking, just stupid for first pass. Run from cron, is the plan.
# 5/22/08 Added directory error checking.
# 5/28/08 Updated to use variables, which is easier to maintain but now I can't use
# source names with spaces in them. Drat!

TARGET_DIR=/Volumes/RBNB
SRC_NAME="SMER_camera"
TMP_DIR=/tmp/smer
SEMAPHORE_FILE=/tmp/last-smer-fetch

if [ ! -d $TARGET_DIR ]
then
 echo "Unable to find RBNB filesystem, please mount from finder."
 exit 1
fi

cd $TARGET_DIR
if [ ! -d $TARGET_DIR/$SRC_NAME ]
then
 mkdir -p "${SRC_NAME}"@"a=8064&c=48"
 # Seems to take a second or three for the mkdir to finish
 sleep 3
fi

if [ ! -d $TARGET_DIR/$SRC_NAME ]
then
 echo "Unable to create source directory, crashing out"
 exit 2
fi

# Scratch dir for download and file conversion
if [ ! -d $TMP_DIR ]
then
 mkdir -p $TMP_DIR
fi

if [ ! -d $TMP_DIR ]
then
 echo "Unable to create temp dir, crashing out"
 exit 3
fi

# OK, have a place to put it, now fetch & upload.
cd $TMP_DIR
curl -s -O -f http://172.23.37.63/jpg/image.jpg
mv image.jpg $TARGET_DIR/$SRC_NAME

touch $SEMAPHORE_FILE
