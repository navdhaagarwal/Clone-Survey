package com.nucleus.persistence;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.ws.core.entities.ServiceIdentifier;

@Named("serviceIdentifierDao")
public class ServiceIdentifierDaoImpl extends BaseDaoImpl<BaseMasterEntity> implements ServiceIdentifierDao {
	
	
	@Inject
	@Named("entityDao")
	private EntityDao         entityDao;

    @Override
    public List<ServiceIdentifier> getUnmappedServiceIdentifiersListDaoForFields(Class<ServiceIdentifier> entityClass, List<Integer> statusList) {
    	     	 
    	   NamedQueryExecutor<ServiceIdentifier> executor = new NamedQueryExecutor<ServiceIdentifier>(
                   "ServiceIdentifier.getUnmappedServiceIdentifiersListForFields");
    	   executor.addParameter("approvalStatusList", ApprovalStatus.UNAPPROVED_AND_HISTORY_RECORD_STATUS_LIST);
          return entityDao.executeQuery(executor);
      
    }
    
    
    @Override
    public List<ServiceIdentifier> getUnmappedServiceIdentifiersListDaoForPlaceholders(Class<ServiceIdentifier> entityClass, List<Integer> statusList) {
    	     	 
    	   NamedQueryExecutor<ServiceIdentifier> executor = new NamedQueryExecutor<ServiceIdentifier>(
                   "ServiceIdentifier.getUnmappedServiceIdentifiersListForPlaceholders");
    	   executor.addParameter("approvalStatusList", ApprovalStatus.UNAPPROVED_AND_HISTORY_RECORD_STATUS_LIST);
          return entityDao.executeQuery(executor);
      
    }
    
    
    @Override    
	public ServiceIdentifier getServiceIdentifierByCode(String code){
 	   NamedQueryExecutor<ServiceIdentifier> executor = new NamedQueryExecutor<ServiceIdentifier>(
               "ServiceIdentifier.getServiceIdentifierByCode");
 	   executor.addParameter("code", code)
 	   			.addParameter("approvalStatusList", ApprovalStatus.UNAPPROVED_AND_HISTORY_RECORD_STATUS_LIST);
 	   List<ServiceIdentifier> identifiers=entityDao.executeQuery(executor);
 	   if(ValidatorUtils.hasElements(identifiers))
 	   {
 	      return identifiers.get(0);
 	   }
 	   return null;
	}

    
    
}
