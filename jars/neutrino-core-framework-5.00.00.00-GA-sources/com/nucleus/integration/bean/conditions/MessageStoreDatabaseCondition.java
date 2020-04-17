package com.nucleus.integration.bean.conditions;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import com.nucleus.integration.constants.MessageChannelConstants;


public class MessageStoreDatabaseCondition implements Condition {

	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		boolean isNoSqlStoreEnabled = Boolean.parseBoolean(
				context.getEnvironment().getProperty(MessageChannelConstants.ACCESS_LOG_NO_SQL_STORE_ENABLED, "false"));
		if (!isNoSqlStoreEnabled) {
			return true;
		}
		return false;

	}
}
