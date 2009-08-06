from gnu.io import CommPortIdentifier
from gnu.io import SerialPort
from gnu.io import SerialPortEvent;
from gnu.io import SerialPortEventListener;


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

class TwoWaySerialComm :
    def __init__ (self):
        print 'initialized TwoWaySerialComm'

    def connect (self, portName):
        portIdentifier = CommPortIdentifier.getPortIdentifier (portName)
        print 'port id:' , portIdentifier
        
        if (portIdentifier.isCurrentlyOwned() ):
            print "Error: Port is currently in use"
        else:
            commPort = portIdentifier.open('SerialReceiver',5000)
            
            if isinstance( commPort, SerialPort ):
                
                commPort.setSerialPortParams (9600,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE)
                commPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE)
                self.inStr = commPort.getInputStream()
                #self.outStr = commPort.getOutputStream()
                
                #Thread(SerialWriter(outStream))).start()
                
                commPort.addEventListener( SerialReader (self.inStr))
                commPort.notifyOnDataAvailable(True)

            else:
                print "Error: Only serial ports are handled by this example."



class SerialReader (SerialPortEventListener):

    def __init__ (self, inStr):
        self.inStream = inStr
        self.outFile = open ('MCRRawData.txt', 'a')
        self.outFile.flush()
        self.scParser = seacatParser(5)
        self.dts = DTSrc (100, "append", 10000, "localhost:3333", "New_Seacat_CTD")
        chNames = ['Temperature', 'Conductivity', 'Pressure', 'Salinity' ]
        chInfos = ['units = deg C', 'units = S/m', 'units = decibars', 'units = psu']
        chMIMEs = ['application/octet-stream', 'application/octet-stream', 'application/octet-stream', 'application/octet-stream']
        self.dts.createChannels (chNames, chInfos, chMIMEs)
        

        
    def serialEvent ( self, args):
        pFile =  PyFile (self.inStream)
        dataLine = pFile.readline()
        print 'datatLine = ', dataLine
        self.outFile.write(dataLine)
        self.outFile.flush()
        splitStr = self.scParser.parseData(dataLine)
        print 'after parser', splitStr
        timeStamp = self.scParser.findTimestamp(splitStr[4], splitStr[5])
        print 'timestamp = ', timeStamp
        print 'splitStr = ', splitStr[0:4]
        self.dts.insertData(timeStamp, splitStr[0:4])


class seacatParser:
    def __init__ (self, args):
        self.elementCount = args
        
    def parseData (self, dataString):
        splitString = dataString.split(',')
        for i in range (len(splitString)):
            splitString[i] = splitString[i].replace('#', '')
            splitString[i] = splitString[i].replace('\n', '')
            splitString[i] = splitString[i].lstrip()
            splitString[i] = splitString[i].rstrip()

        for i in range (4):
            splitString[i] = float(splitString[i])

        return splitString
    
    def findTimestamp (self, dateTup, timeTup):
        str_date = dateTup + " " + timeTup + " GMT"
        formatter = SimpleDateFormat("dd MMM yyyy HH:mm:ss z")
        p_date = formatter.parse(str_date)
        parsed_year = p_date.getYear()
        if parsed_year > 3000:
            print "parsed year is too large: " + str(parsed_year)
            return 0
        else:
            sc_tmstmp = Timestamp (p_date.getTime())
            return sc_tmstmp.getTime() / 1000
 
        
    def checkVals (self, splitString):
        numElements = len (splitString)
        if numElements == 5:
            return True
        else:
            return False

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
        print 'We are inserting the data'
        startTime = timeStamp
        durationTime = 0
        self.chMap.PutTime (startTime, durationTime)
    
        # Add data for each channel, through the various PutChannel methods, 
        # such as ChannelMap.PutData(int,byte[],int).
        chIndex = 0
        for dataVal in dataVals:
            print dataVal
            dVal = array([dataVal], 'd')
            self.chMap.PutDataAsFloat64 (chIndex, dVal)
            chIndex = chIndex +1
        
        print self.chMap
        # Flush(ChannelMap) data to RBNB server. 
        self.src.Flush (self.chMap)



        
tc = TwoWaySerialComm()
tc.connect('COM5')

print 'hi'
