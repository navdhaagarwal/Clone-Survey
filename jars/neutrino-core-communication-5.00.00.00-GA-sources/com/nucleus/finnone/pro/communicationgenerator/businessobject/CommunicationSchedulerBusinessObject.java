package com.nucleus.finnone.pro.communicationgenerator.businessobject;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.core.event.EventCode;
import com.nucleus.finnone.pro.communicationgenerator.dao.ICommunicationSchedulerDAO;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationName;
import com.nucleus.rules.model.SourceProduct;

@Named("communicationSchedulerBusinessObject")
public class CommunicationSchedulerBusinessObject implements ICommunicationSchedulerBusinessObject{

	
	@Inject
	@Named("communicationSchedulerDAO")
	private ICommunicationSchedulerDAO communicationSchedulerDAO;
	
 	@Override
	public List<EventCode> getUnMappedEventCodesBasedOnModule(SourceProduct sourceProduct) {
		return communicationSchedulerDAO.getUnMappedEventCodesBasedOnModule(sourceProduct);
	}

	@Override
	public List<EventCode> getEventCodeListFromIds(Long[] eventCodeIds) {
		return communicationSchedulerDAO.getEventCodeListFromIds(eventCodeIds);
	}

	
	@Override
	public List<CommunicationName> getUnMappedCommunicationsBasedOnModule(SourceProduct sourceProduct) {
		return communicationSchedulerDAO.getUnMappedCommunicationsBasedOnModule(sourceProduct);
	}

	@Override
	public List<CommunicationName> getCommunicationListFromIds(
			Long[] communicationIds) {
		return communicationSchedulerDAO.getCommunicationListFromIds(communicationIds);
	}
	
	@Override
	public String fetchNumberOfDuplicateSchedulers(String schedulerName,boolean eventRequest,SourceProduct sourceProduct,Long id,String uuid) {
		return communicationSchedulerDAO.fetchNumberOfDuplicateSchedulers(schedulerName,eventRequest,sourceProduct,id,uuid);
	}
}
