package com.nucleus.finnone.pro.communicationgenerator.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationErrorLogDetail;
import com.nucleus.persistence.EntityDaoImpl;

@Repository("communicationErrorLoggerDAO")
public class CommunicationErrorLoggerDAO extends EntityDaoImpl implements ICommunicationErrorLoggerDAO{
	@Override
	public void createCommunicationProcessErrorLoggerDetail(List<CommunicationErrorLogDetail> communicationErrorLogDetails) {
		for (CommunicationErrorLogDetail communicationErrorLogDetail:communicationErrorLogDetails) {
		persist(communicationErrorLogDetail);			
		}
	}
}

