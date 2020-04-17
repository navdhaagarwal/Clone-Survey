package com.nucleus.integration.messageChannel.service;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.dao.DataAccessException;
import org.springframework.messaging.Message;

import com.nucleus.core.accesslog.entity.AccessLog;
import com.nucleus.core.datastore.access.log.couch.service.AccessLogCouchDBStoreService;
import com.nucleus.core.datastore.access.log.db.service.AccessLogDatabaseStoreService;
import com.nucleus.core.datastore.access.log.store.strategy.AccessLogStoreStrategy;

public class AccessLogMessageServiceImpl implements AccessLogMessageService {
	
	@Named("accessLogCouchDBStoreService")
	@Inject
	private AccessLogCouchDBStoreService accessLogCouchDBStoreService;
	
	@Named("accessLogDatabaseStoreService")
	@Inject
	private AccessLogDatabaseStoreService accessLogDatabaseStoreService;

	@Override
	public void persistAccessLog(Message<List<AccessLog>> message) throws DataAccessException, Exception {
		if(AccessLogStoreStrategy.MESSAGE_STORE_ON_NO_SQL) {
			accessLogCouchDBStoreService.saveAccessLogInNoSQL(message.getPayload());
		}else{
			accessLogDatabaseStoreService.saveAccessLog(message.getPayload());
		}		
	}
}
