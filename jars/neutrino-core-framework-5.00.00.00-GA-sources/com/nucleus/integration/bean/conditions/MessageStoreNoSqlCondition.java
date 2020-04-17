package com.nucleus.integration.bean.conditions;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import com.nucleus.integration.constants.MessageChannelConstants;

public class MessageStoreNoSqlCondition implements Condition {

	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		return Boolean.parseBoolean(context.getEnvironment().getProperty(MessageChannelConstants.ACCESS_LOG_NO_SQL_STORE_ENABLED,"false"));

	}

}
