package com.nucleus.finnone.pro.communicationgenerator.businessobject;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.isNull;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.config.persisted.vo.ConfigurationVO;
import com.nucleus.entity.Entity;
import com.nucleus.entity.SystemEntity;
import com.nucleus.finnone.pro.communicationgenerator.dao.ICommunicationCommonDAO;
import com.nucleus.user.UserInfo;

@Named("communicationCommonBusinessObject")
public class CommunicationCommonBusinessObject implements ICommunicationCommonBusinessObject{

	@Inject
	@Named("communicationCommonDAO")
	private  ICommunicationCommonDAO communicationCommonDAO;
	
	@Inject
	@Named("configurationService")
	private ConfigurationService configurationService;

	@Override
	public <T extends Entity> List<T> findAll(Class<T> entity) {
		return communicationCommonDAO.findAll(entity);
	}

	@Override
	public <T extends Entity> T findById(Long id, Class<T> entity) {
		return communicationCommonDAO.find(entity, id);
	}

	@Override
	public <T extends Entity> T findMasterByCode(String codeKey,
			String codeValue, Class<T> inputClass) {
		if(isNull(codeValue)){
			return null;
		}
	
		Map<String,Object> map=new HashMap<String, Object>();
		map.put(codeKey, codeValue);
		return communicationCommonDAO.findMasterByCode(inputClass, map);
	}
	
	
}
