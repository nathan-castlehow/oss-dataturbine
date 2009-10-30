import configReader, DBOperator
import sched, time
import os.path as path

from jarray import array
from jreload import makeLoadSet

class DT2DBManager:

    def __init__ (self, cfg):
        # load the rbnb.jar 
        self.dtJarPath = cfg.paramDict [ "DTJarPath"]
        makeLoadSet('DTunit', [self.dtJarPath])
        import DTunit.com.rbnb.sapi as sapi
        self.dt2dbSink = DT2DB (cfg, sapi)
        self.dt2dbSink.run(cfg, sapi)

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
        self.dbop = DBOperator.DBOperator(cfg)
        
        
    def run (self, cfg, sapi):

        self.connectToDT(cfg, sapi)
        self.connectToDB(cfg, sapi)
        # create a channel map
        self.createChannelMap(cfg, sapi)
        self.durationSeconds = cfg.paramDict["durationSeconds"]
        # create a channel tree
        # this function also finds the start time of each channel
        self.createChannelTree(cfg, sapi)
        # either start from the file or the start of the channel
        self.findStartTime (cfg, sapi)
        self.findEndTime(cfg, sapi)
        # subtract one second from the start time
        # to avoid being discarded as a duplicate
        self.startTime = self.startTime - 1.0
        
        # subscribe to the channel map using runStartTime
        self.keepFetchInsert (cfg, sapi)
    
    
    def keepFetchInsert (self, cfg, sapi):
        
        retryInterval = 3
        sucessfulFetch = True
        
        self.currTS = self.startTime + 0.1
        
        # keep fetching and inserting the data into DB
        while sucessfulFetch:
            successfulFetch = True
            dbConnOn = True

            self.duration = 5000
            
            # request
            try:
                if (self.currTS + self.duration) <= self.endTime:
                    print "Requesting data starting from %d to %d seconds" %(self.currTS, self.duration)
                    self.requestDataFromDT(cfg, sapi, self.duration)
                    self.currTS = self.currTS + self.duration
                    requestSuccess = True
                    
                else:  # the end time is sooner
                    lastDuration = self.endTime - self.currTS
                    if lastDuration > 0.0:
                        print "Requesting data starting from %d to %d seconds" %(self.currTS, lastDuration)
                        self.requestDataFromDT(cfg, sapi, lastDuration)
                        self.currTS = self.currTS + lastDuration
                        requestSuccess = True
                    else:
                        requestSuccess = False
                    # find out the new end time
                    self.findChStartTimes(cfg, sapi)
                    self.findEndTime(cfg, sapi)
                                    
            except sapi.SAPIException, se:
                print "Request failed"
                print se
                requestSuccess = False
                self.restartDTConn(cfg, sapi)
            
            if requestSuccess:
                # fetch
                try:
                    # fetch the data from the channel
                    print "Fetching data"
                    self.fetchData(cfg, sapi)
                    print "Fetch successful"
                    successfulFetch = True
                except:
                    print "Fetching failed"
                    successfulFetch = False
                    print 'Restart the DT connection process'
                    self.restartDTConn(cfg, sapi)
            else:
                successfulFetch = False
        
            if successfulFetch:
                # translate the fetched values to the DB queries
                # keep trying to insert the DB queries
                try:
                    # execute the DB queries
                    # move the start subscription time for the next point
                    self.translateDT2DB (cfg, sapi)
                    self.recordStartTime(cfg, sapi)
                except:
                    time.sleep(retryInterval)
                # wait and loop back
                #time.sleep(retryInterval)
        return

    
    def restartDTConn(self, cfg, sapi):
        self.DT2DBSink.CloseRBNBConnection()
        self.connectToDT(cfg, sapi)
        
    
    def translateDT2DB (self, cfg, sapi):
        if cfg.paramDict['dataModel'] == 'EAVModel':
            self.execEAVDBQueries(cfg, sapi)
        elif cfg.paramDict['dataModel'] == 'RowModel':
            self.execRowDBQueries(cfg, sapi)
        return

    def fetchData (self, cfg, sapi):
        blockTimeOut = 10000L
        self.chMap = self.DT2DBSink.Fetch(blockTimeOut, self.chMap)
    
    def createChannelMap (self, cfg, sapi):
        #create channel map
        self.chMap = sapi.ChannelMap ()

        # add channels
        # Define channels by name, via ChannelMap.Add(java.lang.String)
        chNames = cfg.chNames
        for chIndex in range(len(chNames)):
            if chNames[chIndex] != "TimeStampForDB":
                self.chMap.Add(chNames[chIndex])
        # Register the channelMap
        #print self.chMap
        #print 'registering the channel map above'
        

    def findStartTime (self, cfg, sapi):
        # start time
        startFStr = cfg.paramDict["startTimeFilePath"]
        
        if (path.exists(startFStr)):
            startTimeFH = open(startFStr, 'r')
            startTimeLine = startTimeFH.readline()
            self.startTime = float(startTimeLine.strip())
        else:
            minST = "noInit"
            for chST in self.chStartTimes:
                thisST = chST
                if minST == "noInit":
                     minST = thisST
                elif thisST < minST:
                    minST = thisST
            self.startTime = minST
            print "Start time found:", self.startTime


    def findEndTime(self, cfg, sapi):
        minTS = "noInit"
        for chTS in self.chEndTimes:
            thisTS = chTS
            if minTS == "noInit":
                 minTS = thisTS
            elif thisTS < minTS:
                minTS = thisTS
        self.endTime = minTS
        print "The earliest ending time found:", self.endTime

    
    def connectToDT (self, cfg, sapi):
        # connect to DT server persistently
        try:
            self.DT2DBSink.OpenRBNBConnection ( self.dtServer+":"+self.dtPort, self.dtName, 'hi', "hi")
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

    def recordStartTime (self, cfg, sapi):
        return


    def requestDataFromDT (self, cfg, sapi, duration):
        self.DT2DBSink.Request (self.chMap, self.currTS, duration, "absolute")
        return

    
    # each channel maps onto one query
    # pre:  the channels are fetched 
    # post: creates the query strings
    def execEAVDBQueries (self, cfg, sapi):
        listOfCh = self.chMap.GetChannelList()
        self.chMap.NumberOfChannels()
        for chName in listOfCh:
            chInd = self.chMap.GetIndex(chName)
            data = self.chMap.GetDataAsFloat64(chInd)
            tStamps = self.chMap.GetTimes(chInd)
            
            for tsInd in range(len(tStamps)):
                self.dbop.execEAVQuery(self, cfg, chName, tStamps[tsInd], data[tsInd], self.currTS)
        return

    def execRowDBQueries (self, cfg, sapi):
       
        rowQs = cfg.RowQueries.keys()
        # deal one table at a time
        
        #print rowQs
        for rowQ in rowQs:
            #print 'curr row query = ', rowQ
            chDict = cfg.RowQueries[rowQ]
            chNamesForQ = chDict.keys()
            
            # save time and data to create queries for one table
            colsTableTS = {}
            colsTableData = {}
            indOffset = {}
            maxInd={}
            for chName in chNamesForQ:
                if chName != "TimeStampForDB":
                    # get the index using the chName
                    chInd = self.chMap.GetIndex(chName)
                    # get the times and values
                    #print 'ch name = ', chName, 'ch ind = ', chInd
                    if chInd >=0:
                        colsTableTS[chName] = self.chMap.GetTimes(chInd)
                        colsTableData[chName] = self.chMap.GetDataAsFloat64(chInd)
                        indOffset[chName] = len(self.chMap.GetTimes(chInd))
                        maxInd [chName] = len(self.chMap.GetTimes(chInd))
                    
                    #print "chData", colsTableData
                    #print 'chTime', colsTableTS
            # Given the data and their timestamps
            # synchronize them accordingly
            #   1. save current indices across channels
            #   2. find the min time
            #   3. find channels with min time
            #   4. create a query using the channels
            #   5. move the current index for the inserted channels
            #print 'columns for TSs ', colsTableTS
            if len(colsTableTS) >0:
                moreQueries=True
                #print 'more query loop beings'
                while moreQueries:

                    # 2. find min timestamp across channels
                    tempCounter = 0 # for initialization
                    for TSchName in colsTableTS:
                        chTSs = colsTableTS[TSchName]
                        currInd = maxInd[TSchName] - indOffset[TSchName]
                        #print 'chName ', TSchName, ' has ', chTSs, ' and index = ', currInd

                        if currInd <= maxInd[TSchName]:
                            # initialize the min TS
                            if tempCounter == 0:
                                # initialize the min value
                                minTS = chTSs[currInd]
                                tempCounter = tempCounter +1
                            else:
                                currTS = chTSs[currInd]
                                if currTS < minTS:
                                    minTS = currTS
                    
                    # 3.  find the channels with minTime
                    minTimeChans = []
                    minTimeChanVals = []
                    
                    for TSchName in colsTableTS.keys():
                        chTSs = colsTableTS[TSchName]
                        currInd = maxInd[TSchName] - indOffset[TSchName]
                        #print 'index offset ', indOffset[TSchName], maxInd[TSchName] 
                        if currInd <= maxInd[TSchName]:
                            # check if the TS is the min TS
                            currTS = chTSs[currInd]
                            #print 'timestamps = ', currTS, minTS
                            #print minTS-currTS 
                            if currTS == minTS:
                                minTimeChans.append(TSchName)
                                currDataArr = colsTableData[TSchName]
                                currData = currDataArr[currInd]
                                minTimeChanVals.append(currData)
                                # 5. move the cursor one up
                                indOffset[TSchName] = indOffset[TSchName] - 1
                                print 'Moving the indOffset ', indOffset[TSchName], maxInd[TSchName] 

                                #print TSchName, ' index has ', indOffset [TSchName]
                    # 4. create a query using all the channel info
                    self.dbop.execRowQuery (cfg, rowQ, minTimeChans, minTS, minTimeChanVals, self.currTS)
                    print 'DB insert is finished'

                    # 5. Finish the insertion operation if all channels' index offset
                    # are 0
                    if self.checkOffset (colsTableTS, indOffset):
                        moreQueries = False
                #print '----------------------> getting out <_--------------'
        return
    
    def checkOffset (self, colsTableTS, indOffset):
        for TSchName in colsTableTS.keys():
            if indOffset[TSchName] != 0:
                return False
        return True
    
    


    def createChannelTree (self, cfg, sapi):
        self.chMapTree = sapi.ChannelMap()
        
        # get channel names from the cfg
        if cfg.DataModel == "RowModel":
            self.chNames = cfg.chNames
            print 'row model channel names: ', self.chNames
        elif cfg.DataModel == "EAVModel":
            self.chNames = cfg.EAVqueries.keys()
            print 'eav channel names: ', self.chNames
        else:
            print 'please specify the correct data model name'
        
        for chName in self.chNames:
            if chName != "TimeStampForDB":
                self.chMapTree.Add(chName)
        
        #print self.chMapTree
        # Request registration
        self.DT2DBSink.RequestRegistration(self.chMapTree)
        self.DT2DBSink.Fetch (150000, self.chMapTree)
        self.chTree = sapi.ChannelTree.createFromChannelMap (self.chMapTree)
        #print self.chTree
        self.findChStartTimes(cfg, sapi)
        
        print "start times: ", self.chStartTimes
    
    
    
    
    def findChStartTimes (self, cfg, sapi):
        self.chNodes = self.chTree.iterator()
        
        self.chTimeNames = []
        self.chStartTimes = []
        self.chDurationTimes = []
        self.chEndTimes = []

        while (self.chNodes.hasNext()):
            tempChNode = self.chNodes.next()
            if tempChNode.getType() == self.chTree.CHANNEL:
                self.chTimeNames.append(tempChNode.getFullName())
                self.chStartTimes.append(tempChNode.getStart())
                self.chDurationTimes.append(tempChNode.getDuration())
                self.chEndTimes.append(tempChNode.getStart() + tempChNode.getDuration())
        
   

if __name__ == '__main__':
    from sys import argv
    
    cf = configReader.configReader(argv[1])
    cf.parseParams()
    sc = DT2DBManager(cf)
    
