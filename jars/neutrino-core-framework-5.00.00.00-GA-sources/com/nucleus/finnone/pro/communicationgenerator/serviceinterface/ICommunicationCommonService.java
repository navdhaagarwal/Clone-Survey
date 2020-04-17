package com.nucleus.finnone.pro.communicationgenerator.serviceinterface;

import java.util.List;

import com.nucleus.entity.Entity;


public interface ICommunicationCommonService {
	<T extends Entity> List<T> findAll(Class<T> entity);
	<T extends Entity> T findById(Long id,Class<T> entity);
	<T extends Entity> T findMasterByCode(String codeKey,String codeValue,Class<T> inputClass);
}
