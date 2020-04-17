package com.nucleus.master.audit.dao;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Component;

import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.master.audit.MasterChangeAuditLog;
import com.nucleus.persistence.EntityDao;
import com.nucleus.service.BaseServiceImpl;

@Component("masterChangeDao")
public class MasterChangeDaoImpl extends BaseServiceImpl implements MasterChangeDao {

	@Inject
	@Named("entityDao")
	private EntityDao entityDao;
	
	
	@Override
	public void saveMasterAuditData(MasterChangeAuditLog log) {
		if(log!=null){
			entityDao.saveOrUpdate(log);
		}
	}

	@Override
	public List<MasterChangeAuditLog> getAuditLogs(String entityURI) {
		NamedQueryExecutor<MasterChangeAuditLog> logs = new NamedQueryExecutor<>("master.getAuditByEntityUri");
		logs.addParameter("entityURI", entityURI);
		return entityDao.executeQuery(logs);
	}

}
