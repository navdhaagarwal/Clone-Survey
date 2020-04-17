package com.nucleus.persistence;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.core.dynamicform.entities.ServicePlaceholderMapping;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.master.BaseMasterEntity;

@Named("servicePlaceHolderdao")
public class ServicePlaceHolderdaoImpl extends BaseDaoImpl<BaseMasterEntity> implements ServicePlaceHolderdao {
	
	
	@Inject
	@Named("entityDao")
	private EntityDao         entityDao;

    @Override
    public List<ServicePlaceholderMapping> getServicePlaceholderMappingListFromServiceIdentifier(Long serviceIdentifierId) {
    	     	 
    	   NamedQueryExecutor<ServicePlaceholderMapping> executor = new NamedQueryExecutor<ServicePlaceholderMapping>(
                   "ServicePlaceholderMapping.getServicePlaceholderMappingListFromServiceIdentifier").addParameter("id", serviceIdentifierId);
          return entityDao.executeQuery(executor);
      
    }
}
