import getopt
import sys

import urllib2 as u
import base64


from jreload import makeLoadSet

makeLoadSet('DTunit', ['C:\\Program Files\\RBNB\\V3.2B1\\bin\\rbnb.jar'])

from DTunit.com.rbnb.sapi import Client
from DTunit.com.rbnb.sapi import ChannelMap
from DTunit.com.rbnb.sapi import Source
from jarray import array
from org.python.core import PyFile


import sched, time

from jarray import array
import os


class imageRetriever:

    def __init__ (self, url, username, password, interval):
        successRead = False
        self.url = url
        self.usr = username
        self.pas = password
        self.intv = interval
        

    def getImage(self):
        os.system ('erase image.*')
        os.system ('wget -o image.jpg --user=user --password=password http://mcrlter.dyndns.org/jpg/image.jpg')
        successRead = True 
        return successRead

    def converToByteArray (self):
        tPic = open ('image.jpg.1', 'rb')
        byteImg = array (tPic.read(), 'b')
        tPic.close()
        return byteImg


class AxisSource:
    def __init__ (self):
        self.dts = DTSrc (88, "append", 1000, "localhost:3333", "MCR_Camera")
        chNames = ['CooksBay' ]
        chInfos = ['Axis 223m']
        chMIMEs = ['image/jpeg']
        self.dts.createChannels (chNames, chInfos, chMIMEs)
        
    def runSrc(self, interval):
        while True:
            self.insertImage(interval)
            time.sleep(interval)

    def insertImage(self, interval):
        imageRetrived = False
        while (imageRetrived == False):
            ir = imageRetriever('http://mcrlter.dyndns.org/jpg/image.jpg', 'userName', 'password', interval)
            imageRetrived = ir.getImage()
            if not os.path.exists ("image.jpg.1"):
                imageRetrived = False
                time.sleep(interval)


        self.img = ir.converToByteArray()
        # only one camera - needs to be cleaned
        chIndex = 0
        self.dts.insertData(chIndex, self.img)



class DTSrc:
    def __init__ (self, cacheSize, archMode, archSize, serverAddrs, clientName):
        # create source 
        self.src = Source (cacheSize, archMode, archSize)
        self.src.OpenRBNBConnection (serverAddrs, clientName)
        print self.src

    
    def createChannels (self, chNames, chInfos, chMIMEs):
        #create channel map
        self.chMap = ChannelMap ()

        # add channels
        # Define channels by name, via ChannelMap.Add(java.lang.String).

        for chIndex in range(len(chNames)):
            self.chMap.Add(chNames[chIndex])
            assignedIndex = self.chMap.GetIndex (chNames[chIndex])
            
            # add user info such as units
            self.chMap.PutUserInfo(assignedIndex, chInfos[chIndex])
            
            # put the MIME type
            self.chMap.PutMime (assignedIndex, chMIMEs[chIndex])
            
        # Register the channelMap
        self.src.Register (self.chMap)

    
    def insertData (self, chID,img):
        # create a loop here

        # Set a TimeStamp, 
        # using ChannelMap.PutTime(double,double) or 
        #   ChannelMap.PutTimeAuto(java.lang.String).
        print 'We are inserting the image'
        self.chMap.PutTimeAuto ("server")
        self.chMap.PutDataAsByteArray(chID, img)
        self.chMap.PutMime (chID, 'image/jpeg')
        # Flush(ChannelMap) data to RBNB server. 
        self.src.Flush (self.chMap)


aSrc = AxisSource()
aSrc.runSrc(300)
