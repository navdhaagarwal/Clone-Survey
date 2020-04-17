package com.nucleus.finnone.pro.communicationgenerator.dao;

import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationRequestDetail;
import com.nucleus.persistence.EntityDao;

public interface ICommunicationDataPreparationDAO extends EntityDao {

	Object findById(Long id,Class clazz);
	void deleteGeneratedCommunicationRequest(CommunicationRequestDetail communicationRequestDetail);
}
