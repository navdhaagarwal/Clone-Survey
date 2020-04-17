package com.nucleus.core.datastore.access.log.store.strategy;

import javax.annotation.PostConstruct;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Value;

@Named("accessLogStoreStrategy")
public class AccessLogStoreStrategy {

		public static final String MESSAGE_STORE_STRATEGY_NON_JMS = "NON_JMS";
		public static final String MESSAGE_STORE_STRATEGY_JMS = "JMS";
		
		public static String MESSAGE_STORE_TYPE_HEADER = MESSAGE_STORE_STRATEGY_NON_JMS;
		public static boolean MESSAGE_STORE_ON_NO_SQL = false;
		
		@Value("${accessLog.jms.store.enabled}")
		private String isJMSStoreEnabled;
		
		@Value("${accessLog.nosql.store.enabled}")
		private String isNoSQLStoreEnabled;
	
		
		@PostConstruct
		public void initMessageStoreStrategy() {
			if(getJMSStoreFlag()) {
				MESSAGE_STORE_TYPE_HEADER = MESSAGE_STORE_STRATEGY_JMS;
			}
			if(getNoSQLStoreFlag()) {
				MESSAGE_STORE_ON_NO_SQL = true;
			}
		}
		
		public boolean getJMSStoreFlag() {
			if ("${accessLog.jms.store.enabled}".equalsIgnoreCase(isJMSStoreEnabled)) {
				return false;
			}

			return Boolean.parseBoolean(isJMSStoreEnabled);
		}
		
		public boolean getNoSQLStoreFlag() {
			if ("${accessLog.nosql.store.enabled}".equalsIgnoreCase(isNoSQLStoreEnabled)) {
				return false;
			}

			return Boolean.parseBoolean(isNoSQLStoreEnabled);
		}
}
