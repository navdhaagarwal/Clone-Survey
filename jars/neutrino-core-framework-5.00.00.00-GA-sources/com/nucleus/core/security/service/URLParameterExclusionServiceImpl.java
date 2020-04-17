package com.nucleus.core.security.service;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.Query;

import org.springframework.transaction.annotation.Transactional;

import com.nucleus.core.security.entities.AdditionalBlackListPattern;
import com.nucleus.core.security.entities.UnfilteredRequestUri;
import com.nucleus.persistence.EntityDao;


@Transactional(readOnly=true)
@Named("urlParameterExclusionService")
public class URLParameterExclusionServiceImpl implements URLParameterExclusionService {
	
	@Inject
    @Named("entityDao")
    private EntityDao         entityDao;

	@Override
	public List<UnfilteredRequestUri> findAllUnfilteredRequestUris(){
		return entityDao.findAll(UnfilteredRequestUri.class);
	}

	@Override
	public List<AdditionalBlackListPattern> findAllAdditionalBlackListPatterns() {
		            Query query = entityDao.getEntityManager().createNamedQuery("getAdditionalBlackListPattern");

		            query.setParameter("status", 0);
	       
	        
	        		List<AdditionalBlackListPattern> additionalBlackListPatternsList=query.getResultList();
				
	        		return additionalBlackListPatternsList;
				
	}
}
