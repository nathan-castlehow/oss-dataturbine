#!/bin/bash
# Script to grab a single world clock image from the web and
# put it into Webdav as a video feed.
# pfh 4/10/08, based on SMER axis script
# No error checking, just stupid for first pass. Run from cron, is the plan.
# pfh 5/22/08 added directory error checking.

TARGET_DIR=/Volumes/RBNB
SRC_NAME="World time"

if [ ! -d $TARGET_DIR ]
then
 echo "Unable to find RBNB filesystem, please mount from finder."
 exit 1
fi

cd $TARGET_DIR
if [ ! -d $TARGET_DIR/$SRC_NAME ]
then mkdir -p "World time"@"a=8064&c=48"
fi

if [ ! -d $TARGET_DIR/$SRC_NAME ]
then
 echo "Unable to create source directory, crashing out"
 exit 2
fi

# This one is free and easy to get, but we have to convert to JPG via imagemagick.
rm -f /tmp/image.png /tmp/image.jpg
curl -O f "http://www.cru.uea.ac.uk/~timo/sunclock.png" -o /tmp/image.png >& /dev/null
/sw/bin/convert /tmp/image.png /tmp/image.jpg

#mv /tmp/image.jpg /Volumes/RBNB/"World time"/image.jpg
# Move to destination and annotate with 15-minute duration and
# current timestamp
mv /tmp/image.jpg /Volumes/RBNB/"World time"/image.jpg@"t=`date +%s`&d=900&r=newest"

# Leave a zero-byte marker of when we last ran for cron debugging
touch /tmp/last-wc-fetch

