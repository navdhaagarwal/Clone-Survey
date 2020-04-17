package com.nucleus.finnone.pro.communicationgenerator.businessobject;

import java.util.List;
import java.util.Locale;

import com.nucleus.entity.Entity;
import com.nucleus.user.UserInfo;


public interface ICommunicationCommonBusinessObject {

	<T extends Entity> List<T> findAll(Class<T> entity);
	<T extends Entity> T findById(Long id,Class<T> entity);
	<T extends Entity> T findMasterByCode(String codeKey,String codeValue,Class<T> inputClass);
	
}
