package com.nucleus.finnone.pro.communicationgenerator.service;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.entity.Entity;
import com.nucleus.finnone.pro.communicationgenerator.businessobject.ICommunicationCommonBusinessObject;
import com.nucleus.finnone.pro.communicationgenerator.serviceinterface.ICommunicationCommonService;
import com.nucleus.money.MoneyService;

@Named("communicationCommonService")
public class CommunicationCommonService implements ICommunicationCommonService{

	
	@Inject
	@Named("communicationCommonBusinessObject")
	private ICommunicationCommonBusinessObject communicationCommonBusinessObject;
	
	@Override
	public <T extends Entity> List<T> findAll(Class<T> entity) {
		return communicationCommonBusinessObject.findAll(entity);
	}

	@Override
	public <T extends Entity> T findById(Long id, Class<T> entity) {
		return communicationCommonBusinessObject.findById(id, entity);
	}

	@Override
	public <T extends Entity> T findMasterByCode(String codeKey,
			String codeValue, Class<T> inputClass) {
		return communicationCommonBusinessObject.findMasterByCode(codeKey, codeValue, inputClass);
	}
	
}
