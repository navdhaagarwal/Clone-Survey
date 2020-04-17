
package com.nucleus.persistence;

import java.util.Properties;

import javax.jms.Queue;
import javax.jms.QueueConnectionFactory;

import org.hibernate.search.backend.jms.impl.JndiJMSBackendQueueProcessor;

import com.nucleus.core.NeutrinoSpringAppContextUtil;

public class CustomJMSBackendQueueProcessor extends JndiJMSBackendQueueProcessor {

	@Override
	protected QueueConnectionFactory initializeJMSQueueConnectionFactory(Properties properties) {
		return NeutrinoSpringAppContextUtil.getBeanByName("jmsConnectionFactory",
				org.apache.activemq.ActiveMQConnectionFactory.class);
	}

	@Override
	protected Queue initializeJMSQueue(QueueConnectionFactory factory, Properties properties) {
		return NeutrinoSpringAppContextUtil.getBeanByName("hibernateSearchQueue",
				org.apache.activemq.command.ActiveMQQueue.class);

	}
}
