#!/bin/tcsh
#
# Script to run the data turbine applications on the NEESit production machine, neestpm.sdsc.edu
# Assumes that turbine is running on the local machine.
# Paul Hubbard

# Log location... defaults to the main system log
setenv LOG_LOC '/var/log'

# Cache/archive sizes for the DAQ - large, since each datum is small
setenv DAQ_CACHE_SIZE 1000
setenv DAQ_ARCHIVE_SIZE 10000

# Video cache (memory) and archive (disk)
setenv FPS 10
setenv CACHE_JPGS 1024
setenv ARCHIVE_JPGS 10240

setenv RBNB localhost
setenv AX_USER public
setenv AX_PASS public
setenv DAQBUF 100
setenv M2_IP 132.249.64.247

echo Going home. Take that, Dorothy.
cd $HOME

# LJM 060119 - unclear if it is valid to expect this program to be installed at this time
# DAQ and fake daq
# echo Starting fake DAQ
# nohup code/fake_daq/fake_daq >>& $LOG_LOC/fake-daq-src.log &

echo Starting fake DAQ feed...
# note that fake_Daq has the correct UTC timestamps, so we have a 0.0 correction offset
nohup java org.nees.rbnb.DaqToRbnb -t $DAQBUF -o 0.0 -n "FakeDAQ" -z $DAQ_CACHE_SIZE -Z $DAQ_ARCHIVE_SIZE >>& $LOG_LOC/fake-daq.log &

echo Starting DAQ feed for MiniMOST...
# Labview, however, requires TZ correction 
nohup java org.nees.rbnb.DaqToRbnb -t $DAQBUF -q neesdaq.sdsc.edu -s $RBNB -n "Mini-MOST DAQ" -z $DAQ_CACHE_SIZE -Z $DAQ_ARCHIVE_SIZE >>& $LOG_LOC/neesdaq.log &

echo Starting DAQ feed for shaketable...
nohup java org.nees.rbnb.DaqToRbnb -t $DAQBUF -q shaketable.sdsc.edu -s $RBNB -n "Shake table DAQ" -z $DAQ_CACHE_SIZE -Z $DAQ_ARCHIVE_SIZE >>& $LOG_LOC/stdaq.log &

echo Starting DAQ feed for MiniMost-2...
nohup java org.nees.rbnb.DaqToRbnb -t $DAQBUF -q $M2_IP -s $RBNB -n "Mini-MOST2 DAQ" -z $DAQ_CACHE_SIZE -Z $DAQ_ARCHIVE_SIZE >>& $LOG_LOC/m2daq.log &

# Video (axis, dlink) feeds
echo Starting Axis source for camera 1 of MiniMOST
nohup java org.nees.rbnb.AxisSource -s $RBNB -S "Mini-MOST camera 1" -A neescam1.sdsc.edu -n 1 -z $CACHE_JPGS -Z $ARCHIVE_JPGS -f $FPS >>& $LOG_LOC/m1cam1.log &

echo Starting Axis source for camera 2 of MiniMOST
nohup java org.nees.rbnb.AxisSource -s $RBNB -S "Mini-MOST camera 2" -A neescam1.sdsc.edu -n 2 -z $CACHE_JPGS -Z $ARCHIVE_JPGS -f $FPS >>& $LOG_LOC/m1cam2.log &

echo Starting Axis source for shaketable...
nohup java org.nees.rbnb.AxisSource -s $RBNB -U $AX_USER -P $AX_PASS -S "Shake table camera" -A neescam2.sdsc.edu -z $CACHE_JPGS -Z $ARCHIVE_JPGS -f $FPS >>& $LOG_LOC/stcam.log &

# Note that these test sources should be moved to tpm-dev after the SC demo! 
# pfh 10/24/05
echo Starting Axis source for Axis241Q/1...
nohup java org.nees.rbnb.AxisSource -s $RBNB -U $AX_USER -P $AX_PASS -S "Axis 241Q camera 1" -A 132.249.102.218 -z $CACHE_JPGS -Z $ARCHIVE_JPGS -f $FPS -n 1 >>& $LOG_LOC/testcam1.log &

echo Starting Axis source for Axis241Q/2...
nohup java org.nees.rbnb.AxisSource -s $RBNB -U $AX_USER -P $AX_PASS -S "Axis 241Q camera 2" -A 132.249.102.218 -z $CACHE_JPGS -Z $ARCHIVE_JPGS -f $FPS -n 2 >>& $LOG_LOC/testcam2.log &

echo Starting Axis sourcee for Axis241Q/3...
nohup java org.nees.rbnb.AxisSource -s $RBNB -U $AX_USER -P $AX_PASS -S "Axis 241Q camera 3" -A 132.249.102.218 -z $CACHE_JPGS -Z $ARCHIVE_JPGS -f $FPS -n 3 >>& $LOG_LOC/testcam3.log &

echo Starting Axis source for Axis241Q/4...
nohup java org.nees.rbnb.AxisSource -s $RBNB -U $AX_USER -P $AX_PASS -S "Axis 241Q camera 4" -A 132.249.102.218 -z $CACHE_JPGS -Z $ARCHIVE_JPGS -f $FPS -n 4 >>& $LOG_LOC/testcam4.log &

# New DLink code. Thanks, Jason!
echo DLink 1
nohup java org.nees.rbnb.DLinkSource -H -A 192.168.0.100 -S "D-Link 900 camera 1" -z $CACHE_JPGS -Z $ARCHIVE_JPGS >>& $LOG_LOC/dlink1.log &

echo DLink 2
nohup java org.nees.rbnb.DLinkSource -H -A 192.168.0.101 -S "D-Link 900 camera 2" -z $CACHE_JPGS -Z $ARCHIVE_JPGS  >>& $LOG_LOC/dlink2.log &

echo DLink 3
nohup java org.nees.rbnb.DLinkSource -H -A 192.168.0.102 -S "D-Link 900 camera 3" -z $CACHE_JPGS -Z $ARCHIVE_JPGS  >>& $LOG_LOC/dlink3.log &

echo DLink 4, wirelesss
nohup java org.nees.rbnb.DLinkSource -H -A 132.249.64.131 -S "D-Link 900W wireless camera 4" -z $CACHE_JPGS -Z $ARCHIVE_JPGS  >>& $LOG_LOC/dlink4.log &

echo Done.
