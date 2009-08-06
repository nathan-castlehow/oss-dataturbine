from java.util import Date
from java.text import DateFormat, SimpleDateFormat
from java.sql import Timestamp

from jreload import makeLoadSet

makeLoadSet('DTunit', ['C:\\Program Files\\RBNB\\V3.2B1\\bin\\rbnb.jar'])

from DTunit.com.rbnb.sapi import Client
from DTunit.com.rbnb.sapi import ChannelMap
from DTunit.com.rbnb.sapi import Source
from jarray import array
from org.python.core import PyFile
import time
import os

class LoggerNetSrc:
    def __init__ (self, logFile, lineFile):
        print 'initialized LoggerNetSrc'
        self.logFilePath = logFile
        self.dataFile = open (self.logFilePath, 'r')
        self.currLine = 0
        self.lineFile = lineFile


    def initDT (self):
        self.dataFile.readline()
        self.currLine = self.currLine + 1
        
        self.dts = DTSrc (100, "append", 1000000, "localhost:3333", "Weather_Station")
        chNames = self.readFieldNames()
        chNames = chNames[1:]

        unitInfos = self.readFieldNames()
        unitInfos = unitInfos[1:]

        chInfos = self.readFieldNames()
        chInfos = chInfos[1:]

        for indNum in range(len(unitInfos)):
            
            chInfos[indNum] = "units = " + unitInfos[indNum].strip() + " , " + chInfos[indNum].strip()
            #print chInfos[indNum]

        chMIMEs = []
        for chInfo in chInfos:
            chMIMEs = chMIMEs + ['application/octet-stream']
        self.dts.createChannels (chNames, chInfos, chMIMEs)

        # if restarted 
        if os.path.exists (self.lineFile):
            lf = open (self.lineFile,'r')
            numStr = lf.readline()
            lf.close()
            numStr = numStr.strip()
            self.currLine = int(numStr)
            print "Current Line = ", str(self.currLine)
            self.reopenFile()
        else:
           lf = open (self.lineFile,'w')
           numStr = lf.write (str(self.currLine))
           lf.close()
           
    def readFieldNames (self):
        # get lines
        fieldNames = self.dataFile.readline()
        self.currLine = self.currLine + 1
        fieldNames = fieldNames.strip()
        fieldNames = fieldNames.replace('"', '')
        fieldNames = fieldNames.split(',')
        return fieldNames

    def runDataProcessor (self, interval):
        while True:
            # find the number of lines
            self.processDataLines()
            time.sleep(interval)
            self.reopenFile()

    def reopenFile (self):
        self.dataFile.close()
        tempLineCount = 0
        self.dataFile = open (self.logFilePath, 'r')
        while tempLineCount <= self.currLine:
            line = self.dataFile.readline()
            tempLineCount = tempLineCount+1
        print "reopned the file at line number: ", str (self.currLine)

    def getLastLineNum (self):
        lineCountF = open (self.logFilePath, 'r')
        textFile = lineCountF.read()
        count = 0
        for line in textFile:
            count = count + 1
        count = count -1
        return count

    def processDataLines(self):
        while True:
            dataLine = self.dataFile.readline()
            if dataLine.strip() == '':
                #print "reached the end of file"
                lf = open(self.lineFile, 'w')
                lf.write (str(self.currLine))
                lf.close()
                return
            else:
                self.currLine = self.currLine + 1
                self.processDataLine(dataLine)

    def processDataLine(self, dataLine):
         data = dataLine.strip()
         data = data.replace('"', '')
         data = data.split(',')
         timeField = data[0]
         tStamp = self.findTimestamp (timeField)
         self.dts.insertData (tStamp, data[1:])
         
    def findTimestamp (self, timeField):
        print "converting time: " + timeField
        formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss z")
        p_date = formatter.parse(timeField + " HST")
        sc_tmstmp = Timestamp (p_date.getTime())
        return sc_tmstmp.getTime() / 1000
 

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

    
    def insertData (self, timeStamp, dataVals):
        # create a loop here

        # Set a TimeStamp, 
        # using ChannelMap.PutTime(double,double) or 
        #   ChannelMap.PutTimeAuto(java.lang.String).
        print 'We are inserting the data ' + str (timeStamp)
        startTime = timeStamp
        durationTime = 0
        self.chMap.PutTime (startTime, durationTime)
    
        # Add data for each channel, through the various PutChannel methods, 
        # such as ChannelMap.PutData(int,byte[],int).
        chIndex = 0
        for dataVal in dataVals:
            if dataVal != 'NAN':
                dataVal = float(dataVal)
                dVal = array([dataVal], 'd')
                self.chMap.PutDataAsFloat64 (chIndex, dVal)
            chIndex = chIndex +1
        
        # print self.chMap
        # Flush(ChannelMap) data to RBNB server. 
        self.src.Flush (self.chMap)



        
lsrc = LoggerNetSrc('C:\\Campbellsci\\LoggerNet\\CR1000_Table1.dat', 'C:\\Campbellsci\\LoggerNet\\line_num.txt')
lsrc.initDT()
lsrc.runDataProcessor(300)
