package com.nucleus.integration.resource.connection.factory;

import java.util.Properties;

public class ResourceConnectionProperties {
	
	private Properties jmsConnectionFactoryProperties;
	
	private Properties couchDBConnectionProperies;
	
	public ResourceConnectionProperties() {
	}
	
	public ResourceConnectionProperties(Properties jmsConnectionFactoryProperties,Properties couchDBConnectionProperies) {
		this.jmsConnectionFactoryProperties = jmsConnectionFactoryProperties;
		this.couchDBConnectionProperies = couchDBConnectionProperies;
	}

	public Properties getJmsConnectionFactoryProperties() {
		return jmsConnectionFactoryProperties;
	}

	public Properties getCouchDBConnectionProperies() {
		return couchDBConnectionProperies;
	}
	
	

}
