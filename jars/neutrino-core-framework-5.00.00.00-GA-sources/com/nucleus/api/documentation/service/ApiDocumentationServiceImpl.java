package com.nucleus.api.documentation.service;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.Query;

import com.nucleus.api.documentation.entity.ApiMessageCode;
import com.nucleus.persistence.EntityDao;

@Named("apiDocumentationService")
public class ApiDocumentationServiceImpl implements ApiDocumentationService {

	@Inject
	@Named("entityDao")
	private EntityDao entityDao;

	@SuppressWarnings("unchecked")
	@Override
	public List<ApiMessageCode> getApiMessageCodesByModuleCode(String moduleCode) {
		Query query = entityDao.getEntityManager().createNamedQuery("getApiMessageCodesByModuleCode")
				.setParameter("moduleCode", moduleCode);
		return query.getResultList();
	}

}
