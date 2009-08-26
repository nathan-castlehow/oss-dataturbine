import configReader, DBOperator
import sched, time
import os.path as path

from jarray import array
from jreload import makeLoadSet

class SinkClientManager:

    def __init__ (self, cfg):
        # load the rbnb.jar 
        self.dtJarPath = cfg.paramDict [ "DTJarPath"]
        makeLoadSet('DTunit', [self.dtJarPath])
        import DTunit.com.rbnb.sapi as sapi
        self.dtSink = DT2DB (cfg, sapi)
       

class DT2DB:
    
    # initialize the parameters

    def __init__ (self, cfg, sapi):

        self.dtServer = cfg.paramDict["DTServerAddress"]
        self.dtPort = cfg.paramDict["DTServerPort"]
        self.dtURL = self.dtServer + ":" + self.dtPort
        print "DT url: " + self.dtURL
        
        self.DT2DBSink = sapi.Sink()
        self.dtName = cfg.paramDict["DTSinkName"]        

        self.dataModel = cfg.DataModel
        self.dbop = DBOperator(cfg)
        
        
    

    def run (self, cfg, sapi):

        self.connectToDT(cfg, sapi)

        # start time
        startFStr = cfg.paramDict["startTimeFilePath"]
        
        if (path.exists(startFStr)):
            startTimeFH = open(startFStr, 'r')
            startTimeLine = startTimeFH.readline()
            self.startTime = float(startTimeLine.strip())
        else:
            # TODO: infer from the DT server
            print 'please specify the start time file'
            
        self.durationSeconds = cfg.paramDict["durationSeconds"]
        self.createChannelTree(cfg, sapi)


    def connectToDT (self, cfg, sapi):
        # connect to DT server persistently
        try:
            DT2DBSink.OpenRBNBConnection ( self.dtServer+":"+self.dtPort, self.dtName, 'hi', "hi")
        except:
            print 'The DT client could not connect to the DT server'
            print 'waiting to try again in 60 sec'
            time.sleep (60)
            self.connectToDT (cfg, sapi)
    
    def connectToDB (self, cfg, sapi):
        self.dbop.connect()
        
    
    def saveLastTimestamp (self, cfg, sapi):
        self.startTimeFilePath = cfg.paramDict['startTimeFilePath']
        #todo save the last timestamp to a file

    
    def subscribeToDT (self, cfg, sapi):
        # sink client continuously receives the data
        return

    def createEAVDBQuery (self, cfg, sapi):
        # get one reading at a time
        # create a DB query
        # map the channel names to DB query
        return

    def createRowDBQuery (self, cfg, sapi):
        # organize the insert query 
        return
    
    def executDBQuery (self, cfg, sapi):
        return


    def createChannelTree (self, cfg, sapi):
        self.chMapTree = sapi.ChannelMap()
        
        # get channel names from the cfg
        if cfg.DataModel == "RowModel":
            self.chNames = cfg.RowMap.keys()
            print 'row model channel names: ', self.chNames
        elif cfg.DataModel == "EAVModel":
            self.chNames = cfg.EAVqueries.keys()
            print 'eav channel names: ', self.chNames
        else:
            print 'please specify the correct data modle name'
        
        for chName in self.chNames:
            self.chMapTree.Add(chName)
            
        # Request registration
        self.DT2DBSink.RequestRegistration(self.chMapTree)
        self.DT2DBSink.Fetch (1500, self.chMapTree)
        self.chTree = sapi.ChannelTree.createFromChannelMap (self.chMapTree)
        print self.chTree
        self.findStartTime(cfg, sapi)
        
        print "start times: ", self.chStartTimes
        

    def findStartTime (self, cfg, sapi):
        self.chNodes = self.chTree.iterator()
        
        self.chTimeNames = []
        self.chStartTimes = []
        self.chDurationTimes = []
        self.chEndTimes = []

        while (self.chNodes.hasNext()):
            tempChNode = self.chNodes.next()
            print tempChNode.getType()
            if tempChNode.getType() == self.chTree.CHANNEL:
                print 'found a node type'
                self.chTimeNames.append(tempChNode.getFullName())
                self.chStartTimes.append(tempChNode.getStart())
                self.chDurationTimes.append(tempChNode.getDuration())
                self.chEndTimes.append(tempChNode.getStart() + tempChNode.getDuration())
        
   

if __name__ == '__main__':
    cf = configReader.configReader("eav.xml")
    cf.parseParams()
    sc = SinkClientManager(cf)
