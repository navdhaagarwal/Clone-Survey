package com.nucleus.integration.resource.connection.factory;

import java.net.MalformedURLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbInstance;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.jms.DynamicJmsTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jms.core.JmsTemplate;

import com.nucleus.integration.bean.conditions.MessageStoreDatabaseCondition;
import com.nucleus.integration.bean.conditions.MessageStoreJMSCondition;
import com.nucleus.integration.bean.conditions.MessageStoreNoSqlCondition;
import com.nucleus.integration.constants.MessageChannelConstants;

@Configuration
public class MessageConsumerResourceConnectionConfiguration {

	@Bean("accessLogJdbcTemplate")
	@Conditional(MessageStoreDatabaseCondition.class)
	public JdbcTemplate createJdbcTemplate(@Qualifier("accessLogDataSource") DataSource dataSource) throws Exception {
		return new JdbcTemplate(dataSource);
	}

	@Bean("accessLogJmsTemplate")
	@Conditional(MessageStoreJMSCondition.class)
	public JmsTemplate createJMSTemplate(@Qualifier("accessLogPooledConnectionFactory") PooledConnectionFactory pooledConnectionFactory) {
		DynamicJmsTemplate dynamicJmsTemplate = new DynamicJmsTemplate();
		dynamicJmsTemplate.setConnectionFactory(pooledConnectionFactory);

		return dynamicJmsTemplate;
	}

	@Bean("accessLogPooledConnectionFactory")
	@Conditional(MessageStoreJMSCondition.class)
	public PooledConnectionFactory createPooledConnectionFactory(
			@Qualifier("resourceConnectionProperties") ResourceConnectionProperties resourceConnectionProperties) {
		PooledConnectionFactory pooledConnectionFactory = null;
		Properties jmsConnectionFactoryProperties = resourceConnectionProperties.getJmsConnectionFactoryProperties();

		if (jmsConnectionFactoryProperties != null) {
			ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory();
			activeMQConnectionFactory
					.setBrokerURL(jmsConnectionFactoryProperties.getProperty(MessageChannelConstants.ACTIVEMQ_BROKER_URL));
			activeMQConnectionFactory
					.setUserName(jmsConnectionFactoryProperties.getProperty(MessageChannelConstants.ACTIVEMQ_USERNAME));
			activeMQConnectionFactory
					.setPassword(jmsConnectionFactoryProperties.getProperty(MessageChannelConstants.ACTIVEMQ_PASSWPORD));
			activeMQConnectionFactory.setTrustAllPackages(true);
		
			
			pooledConnectionFactory = new PooledConnectionFactory(activeMQConnectionFactory);
			pooledConnectionFactory.setMaxConnections(Integer.parseInt(jmsConnectionFactoryProperties.getProperty(MessageChannelConstants.ACTIVEMQ_POOLED_CONNECTION_FACTORTY_MAX_CONNECTIONS)));			
			pooledConnectionFactory.setMaximumActiveSessionPerConnection(Integer.parseInt(jmsConnectionFactoryProperties.getProperty(MessageChannelConstants.ACTIVEMQ_POOLED_CONNECTION_FACTORTY_MAX_CONNECTION_PAER_SESSION)));			

		}

		return pooledConnectionFactory;
	}

	@Bean("accessLogCouchDBConnector")
	@Conditional(MessageStoreNoSqlCondition.class)
	public CouchDbConnector createCouchDbConnector(
			@Qualifier("resourceConnectionProperties") ResourceConnectionProperties resourceConnectionProperties) throws NumberFormatException, MalformedURLException{
		Properties couchProperties = resourceConnectionProperties.getCouchDBConnectionProperies();

		HttpClient httpClient = new StdHttpClient.Builder()
				.url(couchProperties.getProperty(MessageChannelConstants.COUCH_URL))
				.port(Integer.valueOf(couchProperties.getProperty(MessageChannelConstants.COUCH_PORT)).intValue())
				.username(couchProperties.getProperty(MessageChannelConstants.COUCH_USERNAME))
				.password(couchProperties.getProperty(MessageChannelConstants.COUCH_PASSWORD))
				.maxConnections(
						Integer.valueOf(couchProperties.getProperty(MessageChannelConstants.COUCH_MAX_CONNECTIONS)).intValue())
				.socketTimeout(Integer.valueOf(couchProperties.getProperty(MessageChannelConstants.COUCH_CONNECTION_TIMEOUT))
						.intValue())
				.build();

		CouchDbInstance couchDbInstance = new StdCouchDbInstance(httpClient);

		return couchDbInstance
				.createConnector(couchProperties.getProperty(MessageChannelConstants.COUCH_COUCH_DB_NAME), true);
	}

}
