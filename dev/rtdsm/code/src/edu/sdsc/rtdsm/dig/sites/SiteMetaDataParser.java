package edu.sdsc.rtdsm.dig.sites;

import java.util.*;

import edu.sdsc.rtdsm.framework.util.*;
import edu.sdsc.rtdsm.dig.sites.lake.*;

public class SiteMetaDataParser {

  public static SensorMetaData parse(String id, String dbStr){

    int stIndex = 0;
    String key;
    String value;
    String keyValStr;
    String token;
    int count;
    StringTokenizer jenaSt;
    StringTokenizer keyValSt;
    StringTokenizer st = new StringTokenizer(dbStr, 
        Constants.SITE_META_DATA_SERVICE_LEV1_DELIM);

    SensorMetaData smd = 
      SensorMetaDataManager.getInstance().getSensorMetaData(id);
    smd.resetInfo(false);

    smd.setWebServiceString(dbStr);
    while (st.hasMoreTokens()) {

      token = st.nextToken();
      Debugger.debug(Debugger.TRACE, token);
      jenaSt = new StringTokenizer(token, 
          Constants.SITE_META_DATA_SERVICE_JENA_DELIM);

      count = 0;
      while(jenaSt.hasMoreTokens()) {

        count++;
        keyValStr  = jenaSt.nextToken();
        // Ignore the first one. This is some Jena specific string
        if(count == 1) continue;
        if(count > 2 ) {
          throw new IllegalStateException("Webservice string corruption. " +
              "There are two key-value pairs without a delimiter");
        }

        Debugger.debug(Debugger.TRACE, "\t" + keyValStr );
        stIndex = keyValStr.indexOf(
            Constants.SITE_META_DATA_SERVICE_KEY_VAL_DELIM);

        if(stIndex == -1) {
          throw new IllegalStateException("The keyVal string \"" + keyValStr +
              "\" does not seem to be a valid webservice return string");
        }
        key = keyValStr.substring(0,stIndex);
        Debugger.debug(Debugger.TRACE, "\t\tkey=" + key);
        value = keyValStr.substring( stIndex +
            (Constants.SITE_META_DATA_SERVICE_KEY_VAL_DELIM.length()),
            keyValStr.length());
        Debugger.debug(Debugger.TRACE, "\t\tvalue=" + value);
        smd.setProperty(key, value);
      }
    }
    smd.printMetaData(Debugger.TRACE);

    //This is the key that we are going to give to the agent
    return smd;
  }
}
