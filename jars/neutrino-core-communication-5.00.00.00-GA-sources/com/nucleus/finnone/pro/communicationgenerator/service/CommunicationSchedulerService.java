package com.nucleus.finnone.pro.communicationgenerator.service;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Service;

import com.nucleus.core.event.EventCode;
import com.nucleus.finnone.pro.communicationgenerator.businessobject.ICommunicationSchedulerBusinessObject;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationName;
import com.nucleus.finnone.pro.communicationgenerator.serviceinterface.ICommunicationSchedulerService;
import com.nucleus.rules.model.SourceProduct;

@Service("communicationSchedulerService")
public class CommunicationSchedulerService implements ICommunicationSchedulerService{	

	@Inject
	@Named("communicationSchedulerBusinessObject")
	private ICommunicationSchedulerBusinessObject communicationSchedulerBusinessObject;
	
	@Override
	public List<EventCode> getUnMappedEventCodesBasedOnModule(SourceProduct sourceProduct) {
		return communicationSchedulerBusinessObject.getUnMappedEventCodesBasedOnModule(sourceProduct);
		
	}	
	
	@Override
	public List<EventCode> getEventCodeListFromIds(Long[] eventCodeIds) {
		return communicationSchedulerBusinessObject.getEventCodeListFromIds(eventCodeIds);
	}

	@Override
	public List<CommunicationName> getUnMappedCommunicationsBasedOnModule(SourceProduct sourceProduct) {
		return communicationSchedulerBusinessObject.getUnMappedCommunicationsBasedOnModule(sourceProduct);
	}
	
	@Override
	public List<CommunicationName> getCommunicationListFromIds(
			Long[] communicationIds) {
		return communicationSchedulerBusinessObject.getCommunicationListFromIds(communicationIds);
	}
	
	@Override
	public String fetchNumberOfDuplicateSchedulers(String schedulerName,boolean eventRequest,SourceProduct sourceProduct,Long id,String uuid) {
		return communicationSchedulerBusinessObject.fetchNumberOfDuplicateSchedulers(schedulerName,eventRequest,sourceProduct,id,uuid);
	}
}
