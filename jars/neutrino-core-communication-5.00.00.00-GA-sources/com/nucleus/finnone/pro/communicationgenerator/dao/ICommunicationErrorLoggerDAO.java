package com.nucleus.finnone.pro.communicationgenerator.dao;

import java.util.List;

import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationErrorLogDetail;
import com.nucleus.persistence.EntityDao;

public interface ICommunicationErrorLoggerDAO extends EntityDao {
	void createCommunicationProcessErrorLoggerDetail(List<CommunicationErrorLogDetail> communicationErrorLogDetail);
}
