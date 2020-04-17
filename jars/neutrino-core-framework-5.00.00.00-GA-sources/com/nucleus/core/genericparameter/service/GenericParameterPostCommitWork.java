package com.nucleus.core.genericparameter.service;



import static com.nucleus.core.genericparameter.service.GenericParameterServiceImpl.IMPACTED_CACHE_MAP;
import static com.nucleus.core.genericparameter.service.GenericParameterServiceImpl.NEW_GENERIC_PARAMETER;
import static com.nucleus.core.genericparameter.service.GenericParameterServiceImpl.OLD_GENERIC_PARAMETER;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.transaction.annotation.Transactional;

import com.nucleus.core.cache.FWCacheHelper;
import com.nucleus.core.genericparameter.entity.GenericParameter;
import com.nucleus.core.transaction.TransactionPostCommitWork;
import com.nucleus.finnone.pro.cache.entity.ImpactedCache;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;

/**
 * 
 * This class builds the Cache for Parameter.
 * 
 * @author NSEL
 * @since GA 2.5
 */
@Named("genericParameterPostCommitWork")
public class GenericParameterPostCommitWork implements TransactionPostCommitWork{

	
	
	@Inject
	@Named("genericParameterService")
	private GenericParameterService genericParameterService;
	
	@Inject
	@Named("fwCacheHelper")
	private FWCacheHelper fwCacheHelper;
	
	
	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly=true)
	public void work(Object argument) {
		if(ValidatorUtils.notNull(argument)){
			Map<String,Object> genericParameterMap = (Map<String,Object>) argument;
			
			GenericParameter newGenericParameter = (GenericParameter) genericParameterMap.get(NEW_GENERIC_PARAMETER);
			GenericParameter oldGenericParameter = (GenericParameter) genericParameterMap.get(OLD_GENERIC_PARAMETER);
			Map<String, ImpactedCache> impactedCacheMap = (Map<String, ImpactedCache>) genericParameterMap
					.get(IMPACTED_CACHE_MAP);
			
			if(ValidatorUtils.notNull(newGenericParameter) && ValidatorUtils.hasAnyEntry(impactedCacheMap)){
				newGenericParameter = genericParameterService.findById(newGenericParameter.getId(), newGenericParameter.getClass());
				newGenericParameter.initializeAuthorities();
				fwCacheHelper.detachEntity(newGenericParameter);
				genericParameterService.createOrUpdateGenericParameterCache(newGenericParameter, oldGenericParameter, impactedCacheMap);	
			}
				
		}
		
	}
	
	

}
