package com.nucleus.integration.messageChannel.service;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.dao.DataAccessException;
import org.springframework.messaging.Message;

import com.nucleus.core.accesslog.entity.AccessLog;
import com.nucleus.integration.constants.MessageChannelConstants;

@Named(MessageChannelConstants.ACCESS_LOG_JMS_INBOUND_SERVICE_ACT_BEAN_NAME)
public class JMSInboundServiceActivator {

	
	@Named("accessLogChannelService")
	@Inject
	AccessLogMessageService accessLogChannelService;
	
	public void handleAccessLogMessages(Message<List<AccessLog>> messages) throws DataAccessException, Exception {
		accessLogChannelService.persistAccessLog(messages);
	}
		
}
