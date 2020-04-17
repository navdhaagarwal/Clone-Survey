package com.nucleus.integration.bean.conditions;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import com.nucleus.integration.constants.MessageChannelConstants;

public class MessageStoreNonJMSCondition implements Condition {
		
	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		boolean isJmsStoreEnabled = Boolean.parseBoolean(context.getEnvironment().getProperty(MessageChannelConstants.ACCESS_LOG_JMS_STORE_ENABLED,"false"));
		if(!isJmsStoreEnabled) {
			return true;
		}
		return false;
	}

}
