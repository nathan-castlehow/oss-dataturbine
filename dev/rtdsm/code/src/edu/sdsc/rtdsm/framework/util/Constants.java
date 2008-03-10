package edu.sdsc.rtdsm.framework.util;

public class Constants{

  public static final String DEFAULT_PROP_FILE_NAME="rtdsm.properties";
  
  public static final int DATATYPE_DOUBLE = 0;
  public static final int DATATYPE_STRING = 1;

  public static final Integer DATATYPE_DOUBLE_OBJ = new Integer(DATATYPE_DOUBLE);
  public static final Integer DATATYPE_STRING_OBJ = new Integer(DATATYPE_STRING);

  public static final int JAVA_SIZE_OF_SHORT = 2;

  public static final String TURBINE_SPECIFIC_SUFFIX = "_1";

  public static final String DB_DRIVER_TAG = "dbDriver";
  public static final String DB_JDBC_URL_TAG = "dbJdbcUrl";
  public static final String DB_USERNAME_TAG = "dbUsername";
  public static final String DB_PASSWORD_TAG = "dbPassword";

  public static final String TIMESTAMP_CHANNEL_NAME = "timestamp";

  public static final String DB_TEMPLATE_SRC_TAG = "source";
  public static final String DB_TEMPLATE_CHANNEL_TAG = "channel";
  public static final String DB_TEMPLATE_GEN_TIME_TAG = "timeGenerated";
  public static final String DB_TEMPLATE_VALUE_TAG = "value";

  public static final String CONFIG_XML_NAME_TAG="name";
  public static final String CONFIG_XML_ORB_PARAMS_TAG="orbParams";
  public static final String CONFIG_XML_ORB_TYPE_TAG="orbType";
  public static final String CONFIG_XML_DATA_TURBINE_ORBTYPE_STR = "DataTurbine";
  public static final String CONFIG_XML_MAIN_CHANNEL_TAG = "mainChannels";
  public static final String CONFIG_XML_URI_TAG="uri";
  public static final String CONFIG_XML_USERNAME_TAG="username";
  public static final String CONFIG_XML_PASSWORD_TAG="password";
  public static final String CONFIG_XML_SOURCE_TAG="source";
  public static final String CONFIG_XML_SINK_TAG="sink";

  public static final String SRCCONFIG_XML_SOURCE_TAG=CONFIG_XML_SOURCE_TAG;
  public static final String SRCCONFIG_XML_SRC_NAME_TAG = CONFIG_XML_NAME_TAG;
  public static final String SRCCONFIG_XML_ORB_PARAMS_TAG = CONFIG_XML_ORB_PARAMS_TAG;
  public static final String SRCCONFIG_XML_ORB_TYPE_TAG = CONFIG_XML_ORB_TYPE_TAG;
  public static final String SRCCONFIG_XML_DATA_TURBINE_ORBTYPE_STR = CONFIG_XML_DATA_TURBINE_ORBTYPE_STR;
  public static final String SRCCONFIG_XML_MAIN_CHANNEL_TAG = CONFIG_XML_MAIN_CHANNEL_TAG;
  public static final String SRCCONFIG_XML_FEEDBACK_CHANNEL_TAG = "feedbackChannel";

  public static final String SINKCONFIG_XML_MAIN_CHANNEL_TAG = CONFIG_XML_MAIN_CHANNEL_TAG;

  public static final String SITE_LISTENER_PORT_TAG = "siteListenerPort";

  public static final int SITE_LISTENER_MAX_PACKET_LENGTH = 1000;
  public static final int SITE_LISTENER_HEADER_LENGTH_SIZE = 2;
  public static final int SITE_LISTENER_HEADER_TYPE_SIZE = 1;

  public static final int SITE_LISTENER_GET_META_DATA_TYPE = 0;
  public static final int SITE_LISTENER_SEND_DATA_TYPE = 1;

  public static final int SITE_LISTENER_SEND_DATA_KEY_LENGTH_SIZE = 2;
  public static final int SITE_LISTENER_SEND_DATA_DATA_LENGTH_SIZE = 2;

  public static final int SITE_LISTENER_GET_META_DATA_ID_LENGTH_SIZE = 2;

  public static final String SITE_META_DATA_SERVICE_END_POINT_TAG = "metaDataEndPoint";
  public static final String SITE_META_DATA_SERVICE_METHOD_NAME_TAG = "metaDataMethodName";
  public static final String SITE_META_DATA_SERVICE_SENSORID_TAG = "metaDataSensorIdParamName";
  public static final String SITE_META_DATA_SERVICE_LEV1_DELIM = ",";
  public static final String SITE_META_DATA_SERVICE_JENA_DELIM = "#";
  public static final String SITE_META_DATA_SERVICE_KEY_VAL_DELIM = "->";
  public static final String SITE_META_DATA_SERVICE_DATA_IDS_DELIM = ";";

  public static final String SITE_META_DATA_SERVICE_BUOY_ID = "BuoyID";
  public static final String SITE_META_DATA_SERVICE_DATA_ID = "DataID";
  public static final String SITE_META_DATA_SERVICE_LAKE_ID = "LakeID";
  public static final String SITE_META_DATA_SERVICE_LOGGER_ID = "LoggerID";
  public static final String SITE_META_DATA_SERVICE_SAMPLE_RATE = "SampleRate";
  public static final String SITE_META_DATA_SERVICE_SENSOR_TYPE = "SensorType";
  public static final String SITE_META_DATA_SERVICE_TABLE_NAME = "TableName";

  public static final String LAKE_SENSORS_DB_TIME_FIELD_NAME = "sample_datetime";

  public static final String LAKE_CONFIG_XML_SINK_TAG = CONFIG_XML_SINK_TAG;
  public static final String LAKE_CONFIG_XML_SERVER_TAG = "server";
  public static final String LAKE_CONFIG_XML_DATA_TURBINE_ORBTYPE_STR = CONFIG_XML_DATA_TURBINE_ORBTYPE_STR;
  public static final String LAKE_CONFIG_XML_MAIN_CHANNEL_TAG = CONFIG_XML_MAIN_CHANNEL_TAG;
  public static final String LAKE_CONFIG_XML_ORBPARAMS_TAG = CONFIG_XML_ORB_PARAMS_TAG;
  public static final String LAKE_CONFIG_XML_ORB_TYPE_TAG = CONFIG_XML_ORB_TYPE_TAG;
  public static final String LAKE_CONFIG_XML_SERVER_URI_TAG = CONFIG_XML_URI_TAG;
  public static final String LAKE_CONFIG_XML_SERVER_USERNAME_TAG = CONFIG_XML_USERNAME_TAG;
  public static final String LAKE_CONFIG_XML_SERVER_PASSWORD_TAG = CONFIG_XML_PASSWORD_TAG;
  public static final String LAKE_CONFIG_XML_SOURCE_TAG = CONFIG_XML_SOURCE_TAG;
  public static final String LAKE_CONFIG_XML_NAME_TAG = CONFIG_XML_NAME_TAG;
  public static final String LAKE_CONFIG_XML_SRC_FEEDBACK_REQD_TAG = "feedbackReqd";

  public static final String DEFAULT_SERVER_ADDRESS = "localhost";
  public static final int DEFAULT_SERVER_PORT = 3333;
  public static final String DEFAULT_SERVER_USERNAME = "";
  public static final String DEFAULT_SERVER_PASSWORD = "";

  public static final int DEFAULT_FEEDBACK_PORT = 7878;
  public static final String FEEDBACK_CHANNEL_NAME = "FeedbackChannel";
  public static final String FEEDBACK_SRC_SUFFIX = "FeedbackSrc";
  public static final String FEEDBACK_SINK_SUFFIX = "FeedbackSink";
  public static final int DEFAULT_MONITOR_TIMEOUT = -1; // Time is in milliseconds

  public static final String LAKE_FEEDBACK_THRESH_KEY = "sinkFeedbackThresh";
  public static final String LAKE_FEEDBACK_SEPARATOR = "~~";
  public static final String LAKE_FEEDBACK_FIELD_SEPARATOR = "@";

  public static final String LAKE_CONTROL_SINK_SUFFIX = "ControlSink";
  public static final String LAKE_CONTROL_SOURCE_NAME = "ControlSource";
  public static final String LAKE_CONTROL_SOURCE_CHANNEL_NAME = "ControlChannel";
  public static final String LAKE_CONTROL_SEPARATOR = "@";
  public static final String LAKE_CONTROL_LOOKUP_PREFIX = "lookup" + LAKE_CONTROL_SEPARATOR;
  public static final String NONEMPTY_DUMMY_USER_NAME_OR_PASSWORD=" ";
}
