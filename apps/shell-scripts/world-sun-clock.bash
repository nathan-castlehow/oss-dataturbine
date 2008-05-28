#!/bin/bash
# Script to grab a single world clock image from the web and
# put it into Webdav as a video feed.
# pfh 4/10/08, based on SMER axis script
# No error checking, just stupid for first pass. Run from cron, is the plan.
# pfh 5/22/08 added directory error checking.
# 5/28/08 Updated to use variables, which is easier to maintain but now I can't use
# source names with spaces in them. Drat!

TARGET_DIR=/Volumes/RBNB
TMP_DIR=/tmp
SEMAPHORE_FILE=/tmp/last-wclock-fetch
SRC_NAME="World_time"

if [ ! -d $TARGET_DIR ]
then
 echo "Unable to find RBNB filesystem, please mount from finder."
 exit 1
fi

cd $TARGET_DIR
if [ ! -d $TARGET_DIR/$SRC_NAME ]
 then mkdir -p "${SRC_NAME}"@"a=8064&c=48"
 # mkdir seems to take a bit on webdav
 sleep 3
fi

echo $TARGET_DIR/$SRC_NAME 
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
# This sunclock is free and easy to get, but we have to convert to JPG via imagemagick.
# PNG works on some versions of java but not others, reasons unknown.
rm -f $TMP_DIR/image.png $TMP_DIR/image.jpg
curl -O f "http://www.cru.uea.ac.uk/~timo/sunclock.png" -o $TMP_DIR/image.png >& /dev/null
/sw/bin/convert $TMP_DIR/image.png $TMP_DIR/image.jpg
mv $TMP_DIR/image.jpg $TARGET_DIR/$SRC_NAME
rm $TMP_DIR/image.png

# Leave a zero-byte marker of when we last ran for cron debugging
touch $SEMAPHORE_FILE

