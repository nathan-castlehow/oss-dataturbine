import com.ziclix.python.sql as sql
import configReader as cr
import time
import java.text.SimpleDateFormat as sdf
import java.sql.Date as date1
import java.sql.SQLException as SQLExcept
import java.sql.SQLWarning as SQLWarn

class DBOperator:
    def __init__ (self, cfg):
        self.dbURL = cfg.paramDict["dbServerName"]
        self.user = cfg.paramDict["dbUserName"]
        self.pw = cfg.paramDict["dbPassword"]
        self.drv = cfg.paramDict['jdbcDriverName']

    def connect (self):
        
        connectToDB = False
        print 'Trying to connect to the DB connection'
        self.dbConn = sql.zxJDBC.connect(self.dbURL, self.user, self.pw, self.drv)
        print "DB connection established", self.dbConn
        
        
    def establishDBConn(self):
        self.dbConn= None
        try:
            self.dbConn = sql.zxJDBC.connect(self.dbURL, self.user, self.pw, self.drv)
            print "DB connection established"
            return True
        except sql.zxJDBC.DatabaseError, dbe:
            print 'DB error'
            print dbe.message
            time.sleep(10)
            pass
            return False
        except sql.zxJDBC.ProgrammingError, pge:
            print 'Programming error'
            print pge.message
            time.sleep(10)
            pass
            return False
        except sql.zxJDBC.noSupportedError, nse:
            print 'Not supported error'
            print nse.message
            time.sleep(10)
            pass
            return False
        except sql.zxJDBC.Error, e:
            print 'zxJDBC error'
            print e.message
            time.sleep(10)
            pass
            return False
        except:
            print 'zxJDBC error'
            print e.message
            time.sleep(10)
            pass
            return False

    def execQuery (self, qStr):
        try:
            print 'Before creating the cursor'
            cursor = self.dbConn.cursor()
            print 'After creating the cursor', self.dbConn
            print cursor
            
            cursor.datahandler = sql.handler.MySQLDataHandler(cursor.datahandler)
            print 'Before executing the query', qStr
            cursor.execute(qStr)
            print 'executed the query', qStr
            self.dbConn.commit()
            cursor.close()
        except:
            print 'SQL error'
        

            
            
    def execEAVQuery(self, cfg, chName, tStamp, val, currTS):
        queries = cfg.EAVqueries[chName]
        # some queries are multilines
        queries = queries.split(";")
        for q1 in queries:
            q1 = q1.replace ("$$$$$", val)
            q1 = q1.replace ("%%%%%", "'" + (self.convertTime(cfg.paramDict['DBTimeFormat'], tStamp))+"'")
            print q1
            if tStamp == currTS:
                print 'Duplicate timestamp'
            else:
                self.execQuery(q1)
    
    def convertTime(self, timeFormatStr, ts):
        # from milliseconds to seconds
        #print ts
        ts = ts * 1000L
        ts = long(ts)
        #print ts
        d1 = date1(ts)
        formatter = sdf(timeFormatStr)
        formattedDate = "'" + formatter.format(d1) +"'"
        #print formattedDate
        return formattedDate
        
    def execRowQuery (self, cfg, oneDataRowQ, chNames, tStamp, vals, currTS):

        #print 'execRowQuery'
        # just in case for multi-line queries
        queries = oneDataRowQ.split(";")
        
        #print 'queries', queries
        for q1 in queries:
            # start with the time stamp
            chMapping = cfg.RowQueries[oneDataRowQ]
            colNames = chMapping ["TimeStampForDB"]
            values = str( self.convertTime(cfg.paramDict['DBTimeFormat'], tStamp))
            
            # build a list of column names
            #   and a list of values 
            for chN in chNames:
                colNames = colNames + ", " + chMapping[chN]
                #print colNames
            for v1 in vals:
                values = values + " ," + str(v1)
                #print values
                    
            q1 = q1.replace ("#####", colNames)
            q1 = q1.replace ("$$$$$", values)
        
            print q1
            if tStamp == currTS:
                print 'duplicate time stamp'
            else:
                self.execQuery (q1)
        
        print 'execRow is finished'

    def close(self):
        print 'DB connection closing'
        self.dbConn.close()

if __name__=='__main__':
    cfg = cr.configReader("eav.xml")
    cfg.parseParams()
    dbop = DBOperator (cfg)
    dbop.connect()
    #dbop.execEAVQuery(cfg, "/FakeDAQ/ch0", 1234, '10000')
    #dbop.execQuery("select * from test;")
    dbop.execRowQuery(cfg, ["FakeDAQ/0", "FakeDAQ/1"],  1211342323.1234, [0,1])
    dbop.close()
    dstr = dbop.convertTime("yyyy-MM-dd HH:mm:ss.SSS", 1211342323.1234)
    print dstr
