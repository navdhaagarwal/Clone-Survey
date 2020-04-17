package com.nucleus.integration.messageChannel.adapters.inbound;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jms.ConnectionFactory;
import javax.jms.Session;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.jms.dsl.Jms;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.util.ErrorHandler;

import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.finnone.pro.base.exception.SystemException;
import com.nucleus.integration.bean.conditions.MessageStoreJMSCondition;
import com.nucleus.integration.constants.MessageChannelConstants;
import com.nucleus.integration.messageChannel.service.AccessLogMessageService;

@Configuration
public class AccessLogMessageInboundAdapter {
	
		@Value("${jms.message.polling.fixed.delay}")
		private String jmsMessageQueuePollingFixedDelay;
		
		@Value("${jms.message.polling.max.messages.per.poll}")
		private String jmsMessageQueuePollingMaxMesssages;
		
		@Value("${jms.inbound.adapter.config.concurrentConsumers}")
		private String concurrentConsumers;
		
		@Value("${jms.inbound.adapter.config.maxConcurrentConsumers}")
		private String maxConcurrentConsumers;		
		
		@Named("accessLogChannelService")
		@Inject
		private AccessLogMessageService accessLogChannelService;
				
		@Bean(name="accessLogJMSInbondAdaptor")
		@Conditional(MessageStoreJMSCondition.class)
		public IntegrationFlow createJMSInboundAdapter(@Qualifier("accessLogPooledConnectionFactory") ConnectionFactory connectionFactory) {
			return  IntegrationFlows.from(Jms.messageDrivenChannelAdapter(connectionFactory)
					.configureListenerContainer(conatiner->{
						final DefaultMessageListenerContainer container = conatiner.get();
		                container.setConcurrentConsumers(Integer.parseInt(concurrentConsumers));
		                container.setMaxConcurrentConsumers(Integer.parseInt(maxConcurrentConsumers));
		                container.setSessionAcknowledgeMode(Session.DUPS_OK_ACKNOWLEDGE);
		                container.setErrorHandler(new MessageListenerContainerErrorHandler());
		                conatiner.messageSelector("moduleName='"+ProductInformationLoader.getProductCode()+"'");
					})
		            .destination(MessageChannelConstants.ACCESSS_LOG_MSG_JMS_DESTINATION_NAME))
		            .channel(MessageChannelConstants.ACCESSS_LOG_INBOUND_MSG_CHANNEL_NAME) 
		            .get();
		}
			
		@Bean(name="accessLogMessageInboundServiceActivator")
		@Conditional(MessageStoreJMSCondition.class)
		public IntegrationFlow createJMSInboundServiceActivator(){
			return IntegrationFlows.from(MessageChannelConstants.ACCESSS_LOG_INBOUND_MSG_CHANNEL_NAME)
					.handle(MessageChannelConstants.ACCESS_LOG_JMS_INBOUND_SERVICE_ACT_BEAN_NAME, MessageChannelConstants.ACCESS_LOG_HANDLER_METHOD_NAME,p->p.poller(getAccessLogMessagePoller())).get();
		}
		
		public PollerMetadata getAccessLogMessagePoller() {
			return Pollers.fixedRate(Integer.parseInt(jmsMessageQueuePollingFixedDelay))
					.maxMessagesPerPoll(Integer.parseInt(jmsMessageQueuePollingMaxMesssages)).get();
		}
		
		private class MessageListenerContainerErrorHandler implements ErrorHandler{
			@Override
			public void handleError(Throwable t) {
				throw new SystemException(t);
			}
			
		}
}
