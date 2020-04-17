package com.nucleus.master.audit.dao;

import java.util.List;

import com.nucleus.master.audit.MasterChangeAuditLog;

public interface MasterChangeDao {

	public void saveMasterAuditData(MasterChangeAuditLog log);
	
	public List<MasterChangeAuditLog> getAuditLogs(String entityURI);
	
}
