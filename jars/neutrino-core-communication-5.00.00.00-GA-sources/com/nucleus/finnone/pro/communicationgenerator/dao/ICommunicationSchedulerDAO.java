package com.nucleus.finnone.pro.communicationgenerator.dao;

import java.util.List;

import com.nucleus.core.event.EventCode;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationName;
import com.nucleus.persistence.EntityDao;
import com.nucleus.rules.model.SourceProduct;

public interface ICommunicationSchedulerDAO extends EntityDao {

	 List<EventCode> getUnMappedEventCodesBasedOnModule(SourceProduct sourceProduct);
	
	 List<EventCode> getEventCodeListFromIds(Long[] eventCodeIds);

	 List<CommunicationName> getUnMappedCommunicationsBasedOnModule(SourceProduct sourceProduct);
	
	 List<CommunicationName> getCommunicationListFromIds(Long[] communicationIds);
	 
	 String fetchNumberOfDuplicateSchedulers(String schedulerName,
				boolean eventRequest,SourceProduct sourceProduct,Long id,String uuid);
	 
}
