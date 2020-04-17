package com.nucleus.integration.constants;

public class MessageChannelConstants {

	public static final int QUEUE_CHANNEL_SIZE = 1000;

	// Channel Names
	public static final String ACCESSS_LOG_MSG_CHANNEL_NAME = "accessLogMessageChannel";
	public static final String ACCESSS_LOG_MSG_AGGREGATOR_OUTPUT_CHANNEL_NAME = "accessLogMessageAggregatorOutChannel";
	public static final String ACCESSS_LOG_INBOUND_MSG_CHANNEL_NAME = "accessLogInBoundMessageChannel";

	// JMS Queue Destination Names
	public static final String ACCESSS_LOG_MSG_JMS_DESTINATION_NAME = "accessLogMessageQueue";

	// Flag Constants
	public static final String ACCESS_LOG_JMS_STORE_ENABLED = "accessLog.jms.store.enabled";
	public static final String ACCESS_LOG_NO_SQL_STORE_ENABLED = "accessLog.nosql.store.enabled";

	// Service activator bean names and its handler method name.
	public static final String ACCESS_LOG_JMS_INBOUND_SERVICE_ACT_BEAN_NAME = "jmsInboundServiceActivator";
	public static final String ACCESS_LOG_NON_JMS_OUTBOUND_SERVICE_ACT_BEAN_NAME = "nonJMSOutboundServiceActivator";

	public static final String ACCESS_LOG_HANDLER_METHOD_NAME = "handleAccessLogMessages";

	public static final String ACTIVEMQ_BROKER_URL = "brokerUrl";
	public static final String ACTIVEMQ_USERNAME = "userName";
	public static final String ACTIVEMQ_PASSWPORD = "password";

	public static final String ACTIVEMQ_POOLED_CONNECTION_FACTORTY_MAX_CONNECTIONS = "maxConnections";
	public static final String ACTIVEMQ_POOLED_CONNECTION_FACTORTY_MAX_CONNECTION_PAER_SESSION = "maxActiveSessionPerConnection";

	public static final String COUCH_URL = "url";
	public static final String COUCH_PORT = "port";
	public static final String COUCH_MAX_CONNECTIONS = "maxConnections";
	public static final String COUCH_CONNECTION_TIMEOUT = "connectionTimeout";
	public static final String COUCH_USERNAME = "username";
	public static final String COUCH_PASSWORD = "password";
	public static final String COUCH_COUCH_DB_NAME = "accessLogCouchDbName";

}
