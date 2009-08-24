import sched, time
import os.path as path

from jarray import array

from jreload import makeLoadSet

makeLoadSet('DTunit', ["/Applications/RBNB/bin/rbnb.jar"])
import DTunit.com.rbnb.sapi as sapi
import SinkProxy

sink = SinkProxy('SinkTest', 'localhost:3333')
sink.OpenRBNBConn()
print sink.VerifyConn()
chMap = sapi.ChannelMap()

chMap.Add('notSyncSrc/ch1')
chMap.Add('notSyncSrc/ch2')
print chMap

print sink.Request (chMap, 0, 30, 'absolute')
chMap = sink.Fetch(-1)
print chMap.GetTimes(0)
print chMap.GetTimes(1)

print 'start time = ', chMap.GetTimeStart(0)
print 'duration time = ', chMap.GetTimeDuration(0)

print sink.Subscribe (chMap, 0, 30, 'absolute')
chMap = sink.Fetch(100)
#print chMap.GetDataAsFloat64 (0)
#print chMap.GetDataAsFloat64 (1)
print chMap.GetTimes(0)
print chMap.GetTimes(1)


chMap2 = sapi.ChannelMap()
chMap2.Add('notSyncSrc/ch1')
chMap2.Add('notSyncSrc/ch2')
sink.RequestRegistration(chMap2)
sink.Fetch(1500, chMap2)
chTree = sapi.ChannelTree.createFromChannelMap (chMap2)
print 'printing channel tree: ', chTree

chNodes = chTree.iterator()

while (chNodes.hasNext()):
    tempChNode = chNodes.next()
    print tempChNode.getType()
    if tempChNode.getType() == chTree.CHANNEL:
        print 'found channel', tempChNode.getFullName()
        print 'start time: ', tempChNode.getStart()
        print 'duration: ', tempChNode.getDuration()
        
print chMap2.GetChannelList()
