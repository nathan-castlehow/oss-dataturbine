package edu.ucsd.osdt.db;

/**
 * ConfigUtil: utility for reading and writing config file
 */

import org.dom4j.Element;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultElement;
import org.dom4j.io.XMLWriter;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;

import java.util.Iterator;
import java.util.List;
import java.io.FileWriter;
import java.io.File;


public class ConfigUtil {

	/**
	 * Write the given Config object to file; 
	 */
	public static void writeConfig( Config aConfig, String outputFileName ) {
		System.out.println( "writeConfig() called" );

		try {
			Document document = DocumentHelper.createDocument();
			Element root = document.addElement( "config" );

			// Output key-value params
			Element parElt = null;

			parElt = new DefaultElement( "param" );
			parElt.addAttribute( "name", "serverAddress" );
			parElt.addAttribute( "value", aConfig.getServerAddress() );
			root.add( parElt );

			parElt = new DefaultElement( "param" );
			parElt.addAttribute( "name", "serverPort" );
			parElt.addAttribute( "value", aConfig.getServerPort() );
			root.add( parElt );

			parElt = new DefaultElement( "param" );
			parElt.addAttribute( "name", "sampleIntParam" );
			parElt.addAttribute( "value", Integer.toString( aConfig.getSampleIntParam() ) );
			root.add( parElt );




			// Output table definitions
			List tables = aConfig.getTableConfigsAsList();

			for( Iterator tableIter = tables.iterator(); tableIter.hasNext(); ) {
				TableConfig aTable = (TableConfig) tableIter.next();

				Element tabElt = new DefaultElement( "table" );
				tabElt.addAttribute( "name", aTable.getName() );

				List columns = aTable.getTableConfigColumnsAsList();

				for( Iterator colIter = columns.iterator(); colIter.hasNext(); ) {
					TableConfigColumn aCol = (TableConfigColumn) colIter.next();

					Element colElt = new DefaultElement( "column" );
					colElt.addAttribute( "name", aCol.getName() );

					if( aCol.getChannelMapping() != null ) 
						colElt.addAttribute( "channelMapping", aCol.getChannelMapping() );

					if( aCol.getDataValue() != null )
						colElt.addAttribute( "dataValue", aCol.getDataValue() );

					if( aCol.getType() != null )
						colElt.addAttribute( "type", aCol.getType() );

					tabElt.add( colElt );
				}

				root.add( tabElt );
			}

			// Write the xml to file 
			OutputFormat format = OutputFormat.createPrettyPrint();
			XMLWriter writer = new XMLWriter( new FileWriter( outputFileName ), format );
			writer.write( document );
			writer.close();

			System.out.println( "Wrote the config to file: " + outputFileName );			

//			Pretty print the document to System.out
			writer = new XMLWriter( System.out, format );
			writer.write( document );

		}
		catch( Exception e ) {
			System.out.println( "Error: exception while writing config file!" );
			e.printStackTrace();
		}
	}

	/**
	 * Reads configuration from file into Config object;
	 * 	WARNING: comments in the file are not currently processed; 
	 * 		thus, if writing this config out to another file, comments will be lost.
	 */
	public static Config readConfig( String inputFileName ) {
		System.out.println( "readConfig() called" );

		Config aConfig = null;

		try {

			File inputFile = new File( inputFileName );	
			SAXReader reader = new SAXReader();
			Document doc = reader.read( inputFile );
			Element root = doc.getRootElement();

			aConfig = new Config();

			// Iterate through child elements of root with element name "param"
			for( Iterator parIter = root.elementIterator( "param" ); parIter.hasNext(); ) {
				Element aParElt = (Element) parIter.next();
				String parName = aParElt.attributeValue( "name" );
				String parValue = aParElt.attributeValue( "value" );

				System.out.println( "Found param: name=" + parName + ", value=" + parValue );

				// Set key value params
				if( parName.equals( "serverAddress" ) ) {
					aConfig.setServerAddress( parValue );
				}
				else if( parName.equals( "serverPort" ) ) {
					aConfig.setServerPort( parValue );
				}
				else if( parName.equals( "sampleIntParam" ) ) {
					aConfig.setSampleIntParam( Integer.parseInt( parValue ) );
				}
				else {
					System.out.println( "Error: unrecognized param (name=" + parName + ")!" );
				}

			}

			// Iterate through child elements of root with element name "table"
			for( Iterator tableIter = root.elementIterator( "table" ); tableIter.hasNext(); ) {
				Element aTableElt = (Element) tableIter.next();
				String tableName = aTableElt.attributeValue( "name" );
				System.out.println( "Found table; name = " + tableName );

				TableConfig aTable = new TableConfig();
				aTable.setName( tableName );

				// Iterate through child elements of current table with element name "column"
				for( Iterator colIter = aTableElt.elementIterator( "column" ); colIter.hasNext(); ) {
					Element aColElt = (Element) colIter.next();
					String colName = aColElt.attributeValue( "name" );
					System.out.print( "Found column; name = " + colName );

					TableConfigColumn aCol = new TableConfigColumn();
					aCol.setName( colName );

					if( aColElt.attributeValue( "channelMapping" ) != null ) {
						aCol.setChannelMapping( aColElt.attributeValue( "channelMapping" ) );
						System.out.print( "... has channelMapping: " + aCol.getChannelMapping() );						
					}

					if( aColElt.attributeValue( "dataValue" ) != null ) {
						aCol.setDataValue( aColElt.attributeValue( "dataValue" ) );
						System.out.print( "... has dataValue: " + aCol.getDataValue() );						
					}

					if( aColElt.attributeValue( "type" ) != null ) {
						aCol.setType( aColElt.attributeValue( "type" ) );
						System.out.print( "... has type: " + aCol.getType() );						
					}

					System.out.println();

//					Casting the value...
					if( aCol.getDataValue() != null ) {
						System.out.println( "Test casting the data value for predefined values..." );	
						String type = aCol.getType();
						System.out.println( "Type: " + type );	 

						if( type.equals( TableConfigColumn.TYPE_STRING ) ) {
							System.out.println( "Value: " + aCol.getDataValue() );		
						}
						else if( type.equals( TableConfigColumn.TYPE_INT ) ) {
							System.out.println( "Value: " + Integer.parseInt( aCol.getDataValue() ) );		
						}
						else if( type.equals( TableConfigColumn.TYPE_DOUBLE ) ) {
							System.out.println( "Value: " + Double.parseDouble( aCol.getDataValue() ) );		
						}
						else {
							System.out.println( "Unknown type!" );
						}
					}


					aTable.putTableConfigColumn( aCol );

				}

				aConfig.putTableConfig( aTable );
			}


		}
		catch( Exception e ) {
			System.out.println( "Error: caught exception while reading config file!" );
			e.printStackTrace();
		}

		return aConfig;
	}

	public static void main( String[] args ) {
		String inputFileName =  "/Users/petershin/Downloads/config-parser/config.xml";
		//String inputFileName =  "./config2.xml";
		//String inputFileName =  "./config3.xml";
		String outputFileName =  "/Users/petershin/Downloads/config-parser/outputConfig.xml";

		Config aConfig = ConfigUtil.readConfig( inputFileName );

		System.out.println();

		ConfigUtil.writeConfig( aConfig, outputFileName );
	}
}
