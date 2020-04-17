package com.nucleus.core.datastore.access.log.couch.service;

import java.util.List;

import com.nucleus.core.accesslog.entity.AccessLog;

public class AccessLogDocumentEntity extends AccessLogBaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private List<AccessLog> accessLogs;
	
	public AccessLogDocumentEntity(List<AccessLog> accessLogs) {
		this.accessLogs = accessLogs;
	}	
	
	public List<AccessLog> getAccessLogs() {
		return accessLogs;
	}

	public void setAccessLogs(List<AccessLog> accessLogs) {
		this.accessLogs = accessLogs;
	}
}
