package com.nucleus.integration.messageChannel.adapters.outbound;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.jms.dsl.Jms;
import org.springframework.jms.core.JmsTemplate;

import com.nucleus.integration.bean.conditions.MessageStoreJMSCondition;
import com.nucleus.integration.bean.conditions.MessageStoreNonJMSCondition;
import com.nucleus.integration.constants.MessageChannelConstants;
import com.nucleus.integration.messageChannel.service.AccessLogMessageService;

@Configuration
public class AccessLogMessageOutboundAdapter {
	
		@Named("accessLogChannelService")
		@Inject
		private AccessLogMessageService accessLogChannelService;
	
		
		@Bean(name="accessLogJMSOutboundAdaptor")
		@Conditional(MessageStoreJMSCondition.class)
		public IntegrationFlow createJMSOutboundAdapter(@Qualifier("accessLogJmsTemplate") JmsTemplate jmsTemplate) {
			
			return IntegrationFlows.from(MessageChannelConstants.ACCESSS_LOG_MSG_AGGREGATOR_OUTPUT_CHANNEL_NAME)
  					.handle(Jms.outboundAdapter(jmsTemplate).destination(MessageChannelConstants.ACCESSS_LOG_MSG_JMS_DESTINATION_NAME)).get();
		}
				
		@Bean(name="accessLogNonJMSOutboundAdaptor")
		@Conditional(MessageStoreNonJMSCondition.class)
		public IntegrationFlow createNonJMSOutboundAdapter()  {
			return IntegrationFlows.from(MessageChannelConstants.ACCESSS_LOG_MSG_AGGREGATOR_OUTPUT_CHANNEL_NAME)
					.handle(MessageChannelConstants.ACCESS_LOG_NON_JMS_OUTBOUND_SERVICE_ACT_BEAN_NAME, MessageChannelConstants.ACCESS_LOG_HANDLER_METHOD_NAME).get();
		}		
}
