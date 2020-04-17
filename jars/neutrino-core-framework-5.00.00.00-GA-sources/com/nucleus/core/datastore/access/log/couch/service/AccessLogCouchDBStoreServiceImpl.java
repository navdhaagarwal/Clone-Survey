package com.nucleus.core.datastore.access.log.couch.service;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Named;

import org.ektorp.CouchDbConnector;
import org.springframework.beans.factory.annotation.Value;

import com.nucleus.core.NeutrinoSpringAppContextUtil;
import com.nucleus.core.accesslog.entity.AccessLog;

@Named("accessLogCouchDBStoreService")
public class AccessLogCouchDBStoreServiceImpl implements AccessLogCouchDBStoreService {

	private AccessLogCouchDBRepository accessLogCouchDBRepository;

	@Value("${accessLog.nosql.store.enabled:false}")
	private String isNoSqlStoreEnabled;

	@Override
	public void saveAccessLogInNoSQL(List<AccessLog> accessLogs) {
		accessLogCouchDBRepository.add(new AccessLogDocumentEntity(accessLogs));
	}

	@PostConstruct
	private void initializeCouchDBRepository(){

		if (Boolean.parseBoolean(isNoSqlStoreEnabled)) {
			CouchDbConnector couchDbConnector = NeutrinoSpringAppContextUtil.getBeanByName("accessLogCouchDBConnector",
					CouchDbConnector.class);
			this.accessLogCouchDBRepository = new AccessLogCouchDBRepository(AccessLogBaseEntity.class,
					couchDbConnector);
		}
	}
}
