package com.nucleus.core.accesslog.service;

import com.nucleus.core.accesslog.entity.AccessLog;

public interface AccessLogService {

	void createAccessLog(AccessLog accessLog);
	boolean isAccessLogEnabled();

}
