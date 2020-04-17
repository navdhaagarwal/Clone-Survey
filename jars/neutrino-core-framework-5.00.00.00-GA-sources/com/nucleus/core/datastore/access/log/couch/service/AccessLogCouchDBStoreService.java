package com.nucleus.core.datastore.access.log.couch.service;

import java.util.List;

import com.nucleus.core.accesslog.entity.AccessLog;

public interface AccessLogCouchDBStoreService {

	public void saveAccessLogInNoSQL(List<AccessLog> list);

}
