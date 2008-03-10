#!/bin/sh
#
# Script to run the data turbine applications on the NEESit production machine, neestpm.sdsc.edu
# Assumes that turbine is running on the local machine.
# Paul Hubbard
# Lawrence J. Miller

args=`getopt adqh $*`
set -- $args
USAGE="$0 [-adq], where a starts the Axis Source, d starts the DLink Source, and q starts the DAQ Source"

# flag variables to use as switches
DAQ_START=''
AXIS_START=''
DLINK_START=''

for i do
  case "$i" in
    -a)
      AXIS_START='start'
      shift; shift;;
	-d)
	  DLINK_START='start'
      shift; shift;;
	-q)
	  DAQ_START='start'
      shift; shift;;
	-h)
      echo $USAGE; exit 0;
      shift;shift;;
    esac
done

LOG_LOC='/var/log'

# Cache/archive sizes for the DAQ - large, since each datum is small
DAQ_CACHE_SIZE='1000'
DAQ_ARCHIVE_SIZE='10000'

# Video cache (memory) and archive (disk)
FP='10'
CACHE_JPGS='1024'
ARCHIVE_JPGS='10240'

RBNB='localhost'
AX_USER='public'
AX_PASS='public'
DAQBUF='100'
M2_IP='132.249.64.247'

echo Going home. Take that, Dorothy.
cd $HOME

# Simple test
echo "DAQ_START=${DAQ_START} AXIS_START=${AXIS_START} DLINK_START=${DLINK_START}"

# DAQ and fake daq
if [ "$DAQ_START" = "start" ]; then
  # LJM 060119 - unclear if it is valid to expect this program to be installed at this time
  # echo Starting fake DAQ
  # nohup code/fake_daq/fake_daq >> $LOG_LOC/fake-daq-src.log 2>&1 &

  echo Starting fake DAQ feed...
  # note that fake_Daq has the correct UTC timestamps, so we have a 0.0 correction offset
  nohup java org.nees.rbnb.DaqToRbnb -t $DAQBUF -o 0.0 -n "FakeDAQ" -z $DAQ_CACHE_SIZE -Z $DAQ_ARCHIVE_SIZE >> $LOG_LOC/fake-daq.log  2>&1 &

  echo Starting DAQ feed for MiniMOST...
  # Labview, however, requires TZ correction 
 # nohup java org.nees.rbnb.DaqToRbnb -t $DAQBUF -q neesdaq.sdsc.edu -s $RBNB -n "Mini-MOST DAQ" -z $DAQ_CACHE_SIZE -Z $DAQ_ARCHIVE_SIZE >> $LOG_LOC/neesdaq.log  2>&1 &

  echo Starting DAQ feed for shaketable...
 # nohup java org.nees.rbnb.DaqToRbnb -t $DAQBUF -q shaketable.sdsc.edu -s $RBNB -n "Shake table DAQ" -z $DAQ_CACHE_SIZE -Z $DAQ_ARCHIVE_SIZE >> $LOG_LOC/stdaq.log  2>&1 &

  echo Starting DAQ feed for MiniMost-2...
 # nohup java org.nees.rbnb.DaqToRbnb -t $DAQBUF -q $M2_IP -s $RBNB -n "Mini-MOST2 DAQ" -z $DAQ_CACHE_SIZE -Z $DAQ_ARCHIVE_SIZE >> $LOG_LOC/m2daq.log  2>&1 &
fi

# Video (axis, dlink) feeds
if [ "$AXIS_START" = "start" ]; then
  echo Starting Axis source for camera 1
  nohup java org.nees.rbnb.AxisSource -s $RBNB -S "NEES Lab" -A local-axis2 -z $CACHE_JPGS -Z $ARCHIVE_JPGS -f $FPS >> $LOG_LOC/lab1.log  2>&1 &

  # Note that these test sources should be moved to tpm-dev after the SC demo! 
  # pfh 10/24/05
  echo Starting Axis source for Axis241Q/1...
  nohup java org.nees.rbnb.AxisSource -s $RBNB -U $AX_USER -P $AX_PASS -S "Axis 241Q camera 1" -A local-axis -z $CACHE_JPGS -Z $ARCHIVE_JPGS -f $FPS -n 1 >> $LOG_LOC/testcam1.log  2>&1 &

  echo Starting Axis source for Axis241Q/2...
  nohup java org.nees.rbnb.AxisSource -s $RBNB -U $AX_USER -P $AX_PASS -S "Axis 241Q camera 2" -A local-axis -z $CACHE_JPGS -Z $ARCHIVE_JPGS -f $FPS -n 2 >> $LOG_LOC/testcam2.log 2>&1 &

  echo Starting Axis sourcee for Axis241Q/3...
  nohup java org.nees.rbnb.AxisSource -s $RBNB -U $AX_USER -P $AX_PASS -S "Axis 241Q camera 3" -A local-axis -z $CACHE_JPGS -Z $ARCHIVE_JPGS -f $FPS -n 3 >> $LOG_LOC/testcam3.log 2>&1 &

  echo Starting Axis source for Axis241Q/4...
  nohup java org.nees.rbnb.AxisSource -s $RBNB -U $AX_USER -P $AX_PASS -S "Axis 241Q camera 4" -A local-axis -z $CACHE_JPGS -Z $ARCHIVE_JPGS -f $FPS -n 4 >> $LOG_LOC/testcam4.log 2>&1 &
fi

# New DLink code. Thanks, Jason!
if [ "$DLINK_START" = "start" ]; then
  echo DLink 1
  nohup java org.nees.rbnb.DLinkSource -H -A 192.168.0.100 -S "D-Link 900 camera 1" -z $CACHE_JPGS -Z $ARCHIVE_JPGS >> $LOG_LOC/dlink1.log 2>&1 &

  echo DLink 2
  nohup java org.nees.rbnb.DLinkSource -H -A 192.168.0.101 -S "D-Link 900 camera 2" -z $CACHE_JPGS -Z $ARCHIVE_JPGS  >> $LOG_LOC/dlink2.log 2>&1 &

  echo DLink 3
  nohup java org.nees.rbnb.DLinkSource -H -A 192.168.0.102 -S "D-Link 900 camera 3" -z $CACHE_JPGS -Z $ARCHIVE_JPGS  >> $LOG_LOC/dlink3.log 2>&1 &

#  echo DLink 4, wirelesss
#  nohup java org.nees.rbnb.DLinkSource -H -A 132.249.64.131 -S "D-Link 900W wireless camera 4" -z $CACHE_JPGS -Z $ARCHIVE_JPGS  >> $LOG_LOC/dlink4.log 2>&1 &
fi
echo Done.
