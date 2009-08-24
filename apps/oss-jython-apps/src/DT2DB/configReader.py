import xml.dom.minidom 

class configReader:

    def __init__ (self, fileH):
        self.fh = open(fileH,"r")
        self.doc = xml.dom.minidom.parse(self.fh)
        self.paramDict ={}
        self.DataModel = "RowModel"
        self.EAVqueries={}
        self.RowQueries ={}

    def parseParams (self):
        params = self.doc.getElementsByTagName("param")
        for param in params:
            param.normalize()
            v1 = param.getAttribute("value")
            n1 = param.getAttribute("name")
            #print n1,":  ", v1
            # insert the name value pair 
            self.paramDict[n1]=v1

        if self.paramDict.has_key("dataModel"):
            if self.paramDict["dataModel"] == "EAVModel":
                self.parseEAV()
                self.DataModel = "EAVModel"
            else:
                self.parseRow()

    def parseEAV (self):
        maps = self.doc.getElementsByTagName("query")
        for oneMap in maps:
            chName = oneMap.getAttribute("chName")
            oneMap.normalize()
            qStr = oneMap.firstChild.data
            qStr = qStr.strip()
            self.EAVqueries[chName] = qStr

    def parseRow (self):
        qs = self.doc.getElementsByTagName("query")
        for q1 in qs:
            q1.firstChild.normalize()
            qStr = q1.firstChild.data
            qStr = qStr.strip()
            print qStr
            cols = q1.getElementsByTagName("column")
            RowMap = {}
            for param in cols:
                param.normalize()
                colName = param.getAttribute("name")
                chName = param.getAttribute("channelMapping")
                print colName,":  ", chName
                RowMap[chName] = colName
            self.RowQueries[qStr] = RowMap    
        
                
cr = configReader("row.xml")
cr.parseParams()
print cr.RowQueries
