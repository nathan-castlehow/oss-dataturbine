package edu.ucsd.osdt.source.numeric;

import java.io.FileInputStream;
import java.util.Properties;


public class LoggerNetParams {
    
	public static void main(String[] args) {
    	
        LoggerNetParams lxp = new LoggerNetParams();
        
        try {
            Properties properties = lxp.readProperties();
            /*
             * Display all properties information
             */
            properties.list(System.out);
 
            /*
             * Read the value of data.folder and jdbc.url configuration
             */
            String cfgFilePath = properties.getProperty("ConfigFilePath");
            System.out.println("Config file path = " + cfgFilePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 
    public Properties readProperties() throws Exception {
        Properties properties = new Properties();
        FileInputStream fis = new FileInputStream("/Users/petershin/Documents/workspace/LoggerNetParam.xml");
        properties.loadFromXML(fis);
 
        return properties;
    }

    public Properties readProperties(String fp) throws Exception {
        Properties properties = new Properties();
        FileInputStream fis = new FileInputStream(fp);
        properties.loadFromXML(fis);
 
        return properties;
    }

}

 