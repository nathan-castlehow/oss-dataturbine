import sched, time

from jarray import array

from jreload import makeLoadSet

makeLoadSet('DTunit', ["/Applications/RBNB/bin/rbnb.jar"])
from DTunit.com.rbnb.sapi import Source
from DTunit.com.rbnb.sapi import ChannelMap
from DTunit.com.rbnb.sapi import Client

from org.python.core import PyFile

DTSrc = Source(100, 'create', 88888)
DTSrc.OpenRBNBConnection ('localhost:3333', 'FakeDAQ')

chMap = ChannelMap()

#################
# create channels
#################

# add channel
chMap.Add('ch0')
assignedIndex = chMap.GetIndex ('ch0')
chMap.PutUserInfo(assignedIndex, 'units = test')
chMap.PutMime (assignedIndex, 'application/octet-stream')

chMap.Add('ch1')
assignedIndex = chMap.GetIndex ('ch1')
chMap.PutMime (assignedIndex, 'application/octet-stream')
            
print chMap
##########################
# register the channel map
##########################
# register
DTSrc.Register (chMap)

cnt1 = 1
cnt2 = 1

while True:
    ## put time ##
    chMap.PutTime (cnt1,0)

    ## put data ##
    ## ch1  ##
    dVal = array([cnt1+0.5], 'd')
    chMap.PutDataAsFloat64 (0, dVal)
    DTSrc.Flush (chMap, False)
    cnt1 = cnt1 + 1

    ## put data ##
    ## ch2 ##
    if (cnt1 % 5)==0:
        chMap.PutTime(cnt2,0)
        dVal2 = array([cnt2+0.1], 'd')
        chMap.PutDataAsFloat64 (1, dVal2)
        DTSrc.Flush (chMap, False)
        cnt2 = cnt2 + 1

    time.sleep(2)
    print DTSrc.BytesTransferred()
    
print 'done'
