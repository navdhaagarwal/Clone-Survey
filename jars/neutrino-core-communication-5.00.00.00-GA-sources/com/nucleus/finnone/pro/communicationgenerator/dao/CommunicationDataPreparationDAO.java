package com.nucleus.finnone.pro.communicationgenerator.dao;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationRequestDetail;
import com.nucleus.persistence.EntityDaoImpl;

@Repository("communicationDataPreparationDAO")
public class CommunicationDataPreparationDAO extends EntityDaoImpl implements ICommunicationDataPreparationDAO {

	
	public Object findById(Long id, Class clazz) {
		return getEntityManager().find(clazz, id);
	}


	@Override
	public void deleteGeneratedCommunicationRequest(
			CommunicationRequestDetail communicationRequestDetail) {
		Query namedQuery=getEntityManager().createNamedQuery("deleteGeneratedCommunication");
		namedQuery.setParameter("id", communicationRequestDetail.getId());
		namedQuery.executeUpdate();
	}

}
