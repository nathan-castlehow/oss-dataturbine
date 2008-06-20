package edu.ucsd.osdt.db;

/**
 * TableConfigColumn: represents one column entry of the table object in the config file
 */

public class TableConfigColumn {
    // Valid values for 'type' field
	 public static String TYPE_STRING = "STRING";
	 public static String TYPE_INT = "INT";
	 public static String TYPE_DOUBLE = "DOUBLE";

    private String name = null;
	 private String channelMapping = null;
	 private String dataValue = null;
	 private String type = null;

	 public void setName( String name ) {
		this.name = name;
	 }

	 public String getName() {
		return this.name;
	 }

	 public void setChannelMapping( String channelMapping ) {
		this.channelMapping = channelMapping;
	 }

	 public String getChannelMapping() {
		return this.channelMapping;
	 }

	 public void setDataValue( String dataValue ) {
		this.dataValue = dataValue;
	 }

	 public String getDataValue() {
		return this.dataValue;
	 }

	 public void setType( String type ) {
		this.type = type;
	 }

	 public String getType() {
		return this.type;
	 }
}
