package com.nucleus.core.datastore.access.log.couch.service;

import org.ektorp.CouchDbConnector;
import org.ektorp.support.CouchDbRepositorySupport;

public class AccessLogCouchDBRepository extends CouchDbRepositorySupport<AccessLogBaseEntity> {

	public AccessLogCouchDBRepository(Class<AccessLogBaseEntity> type, CouchDbConnector db) {
		super(type, db);
	}

}
