package com.nucleus.core.accesslog.service;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.IntegrationMessageHeaderAccessor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import com.nucleus.core.accesslog.entity.AccessLog;
import com.nucleus.core.initialization.ProductInformationLoader;

@Component("accessLogService")
public class AccessLogServiceImpl implements AccessLogService{
	
	@Value(value="${accesslog.enabled}")
	private String accessLogEnabled;

	@Value("${message.channel.message.send.timeout:3000}")
	private String messageSendTimeout;

	@Value("${aggregator.message.group.size}")
	private String messageGroupSize;
	
	

	@Inject
	@Named("accessLogMessageChannel")
	private MessageChannel accessLogMessageChannel;

	@Override
	public void createAccessLog(AccessLog accessLog)  {
		Message<AccessLog> message = MessageBuilder.withPayload(accessLog)
									 .setHeader(IntegrationMessageHeaderAccessor.CORRELATION_ID, AccessLog.ACCESS_LOG_CORRELATION_ID)
		 							 .setHeader(IntegrationMessageHeaderAccessor.SEQUENCE_SIZE, Integer.parseInt(messageGroupSize))
		 							 .setHeader("moduleName", ProductInformationLoader.getProductCode()).build();
		
		accessLogMessageChannel.send(message,Integer.parseInt(messageSendTimeout));
	}

	@Override
	public boolean isAccessLogEnabled(){
		return !"false".equalsIgnoreCase(accessLogEnabled);
	}

	
}
