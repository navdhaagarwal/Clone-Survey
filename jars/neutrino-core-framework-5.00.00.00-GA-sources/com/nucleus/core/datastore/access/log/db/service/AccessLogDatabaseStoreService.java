package com.nucleus.core.datastore.access.log.db.service;

import java.util.List;

import org.springframework.dao.DataAccessException;

import com.nucleus.core.accesslog.entity.AccessLog;

public interface AccessLogDatabaseStoreService {

	public void saveAccessLog(List<AccessLog> list) throws DataAccessException, Exception;

}
